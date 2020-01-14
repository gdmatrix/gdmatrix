/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.signature.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Map;

import javax.activation.DataHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.matrix.doc.DocumentConstants;
import org.matrix.doc.Document;
import org.matrix.signature.SignatureManagerPort;
import org.matrix.signature.SignatureManagerService;
import org.matrix.signature.SignedDocument;

import org.matrix.translation.TranslationConstants;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.signature.PropertyListConverter;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author unknown
 */
public class SignatureServlet extends HttpServlet
{
  public static final String PARAMETER_XSL = "xsl";
  public static final String PROPERTY_NAME = "workflow.xsl";
  public static final String SIGNATURE_XSL = "XSL";
  public static final String TEMPLATE_DOCTYPEID = "TEMPLATE";

  public SignatureServlet()
  {
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    try
    {
      String requestLanguage = request.getLocale().getLanguage();
      String responseLanguage = TranslationConstants.UNIVERSAL_LANGUAGE;
      String uri = request.getRequestURI();
      int semicolon = uri.indexOf(";");
      if (semicolon > 0)
        uri = uri.substring(0, semicolon);

      int index = uri.lastIndexOf("/");
      if (index != -1)
      {
        // ****** read signature document ******
        Map documentProperties = null;
        byte[] data = null;
        try
        {
          String sigId = uri.substring(index + 1);
          SignatureManagerPort port = getSignatureManagerPort();
          SignedDocument document = port.getDocument(sigId);
          // get data byte[]
          data = document.getData();
          // get properties
          documentProperties =
            PropertyListConverter.toMap(document.getProperties());
          // set responseLanguage from document language
          responseLanguage = 
            (String)documentProperties.get(DocumentConstants.LANGUAGE);
        }
        catch (Exception ex)
        {
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }

        // ****** read xsl document ******
        InputStream xslStream = null;
        try
        {
          String xslName = request.getParameter(PARAMETER_XSL);
          if ("default".equals(xslName))
          {
            // take xsl defined in document properties.
            xslName = (String)documentProperties.get(SIGNATURE_XSL);
          }
          if (xslName != null) // get xslStream
          {
            Document xslDocument = getXslDocument(xslName, requestLanguage);
            if (xslDocument != null)
            {
              DataHandler dh = xslDocument.getContent().getData();
              xslStream = (InputStream)dh.getInputStream();
              if (TranslationConstants.UNIVERSAL_LANGUAGE.equals(responseLanguage))
              {
                // set responseLanguage from xsl language
                responseLanguage = xslDocument.getLanguage();
              }
            }
          }
        }
        catch (Exception ex)
        {
          // ignore xsl find error
        }

        // ****** send document ******
        if (xslStream == null) // no xsl, return xml
        {
          // TODO: different signature types?
          response.setContentType("text/xml");
          response.getOutputStream().write(data);
        }
        else // apply xsl transformation
        {
          response.setContentType("text/html");
          if (!TranslationConstants.UNIVERSAL_LANGUAGE.equals(responseLanguage))
          {
            response.setHeader("Content-Language", responseLanguage);
          }
          TransformerFactory factory = TransformerFactory.newInstance();
          javax.xml.transform.Transformer transformer = factory.newTransformer(
            new StreamSource(xslStream));

          // Setup input for XSLT transformation
          Source src = new StreamSource(new ByteArrayInputStream(data));

          // Resulting SAX events must be piped through XSLT
          Result res = new StreamResult(response.getOutputStream());

          // Start XSLT transformation
          transformer.transform(src, res);
        }
        response.flushBuffer();
      }
      else response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    catch (Exception ex)
    {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    ObjectInputStream in = 
      new ObjectInputStream(request.getInputStream());
    try
    {
      ObjectOutputStream out = 
        new ObjectOutputStream(response.getOutputStream());
      try
      {
        SignatureManagerPort port = getSignatureManagerPort();

        Object result = null;
        String operation = (String)in.readObject();
        if (operation.equalsIgnoreCase("addSignature"))
        {
          result = port.addSignature(
            (String)in.readObject(), // sigId
            (byte[])in.readObject()); // certData
        }
        else if (operation.equalsIgnoreCase("endSignature"))
        {
          result = port.endSignature(
            (String)in.readObject(), // sigId
            (byte[])in.readObject()); // signatureData
        }
        else if (operation.equalsIgnoreCase("abortSignature"))
        {
          result = port.abortSignature((String)in.readObject());
        }
        else throw new Exception("INVALID_OPERATION");
        out.writeObject(result);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        out.writeObject(new Exception(e.getMessage()));
      }
      finally
      {
        out.close();
      }
    }
    finally
    {
      in.close();
    }
  }

  private Document getXslDocument(String xslName, String requestLanguage)
    throws Exception
  {
    Document xslDocument = null;
    int dotIndex = xslName.lastIndexOf(".");
    xslName = (dotIndex == -1) ? 
      xslName : xslName.substring(0, dotIndex);
    DocumentManagerClient client = getDocumentManagerClient();

    xslDocument =  client.loadDocumentByName(TEMPLATE_DOCTYPEID, 
      PROPERTY_NAME, xslName, requestLanguage, 0);

    return xslDocument;
  }

  private String getDocumentLanguage(SignedDocument document)
  {
    Map map = PropertyListConverter.toMap(document.getProperties());
    return (String)map.get(DocumentConstants.LANGUAGE);
  }
  
  private SignatureManagerPort getSignatureManagerPort()
    throws Exception
  {
    String userId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String password =
      MatrixConfig.getProperty("adminCredentials.password");

    WSEndpoint endpoint =
      WSDirectory.getInstance().getEndpoint(SignatureManagerService.class);
    return endpoint.getPort(SignatureManagerPort.class, userId, password);
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");
    return new DocumentManagerClient(userId, password);
  }
}
