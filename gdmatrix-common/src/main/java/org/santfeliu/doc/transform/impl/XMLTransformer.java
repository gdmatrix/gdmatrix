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
package org.santfeliu.doc.transform.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.transform.Transformation;
import org.santfeliu.doc.transform.TransformationException;
import org.santfeliu.doc.transform.Transformer;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.util.system.TimeoutProcess;
import org.santfeliu.util.template.Template;

/**
 *
 * @author realor
 */
public class XMLTransformer extends Transformer
{
  private static final int WK_TIMEOUT = 10000; //in millis
  private static final int FOP_TIMEOUT = 10000; //in millis
  private static final String WKCOMMAND_PROPERTY = "wkCommand";
  private static final String FOPCOMMAND_PROPERTY = "fopCommand";

  public static final String XSL_PROPERTY = "XSL";
  public static final String PROPERTY_NAME = "workflow.xsl";
  public static final String TEMPLATE_DOCTYPEID = "TEMPLATE";

  public ArrayList<Transformation> transformations;

  @Override
  public String getDescription()
  {
    return "XMLTransformer v1.0";
  }

  @Override
  public List<Transformation> getSupportedTransformations()
  {
    if (transformations == null)
    {
      transformations = new ArrayList<Transformation>();

      transformations.add(new Transformation(id, "xsl",
        "text/xml", null,
        "text/html", null,
        null,
        "XSL transformation"));

      transformations.add(new Transformation(id, "pdf",
        "text/xml", null,
        "application/pdf", null,
        null,
        "XML-PDF transformation"));
    }
    return transformations;
  }

  public DataHandler transform(Document document,
    String transformationName, Map options) throws TransformationException
  {
    try
    {
      Content content = document.getContent();
      InputStream in = content.getData().getInputStream();
      
      String xslName = (String)options.get(XSL_PROPERTY);
      if (xslName == null) xslName = getXslName(document, XSL_PROPERTY);

      if (xslName != null)
      {
        Document xslDocument = getXslDocument(xslName);
        if (xslDocument != null)
        {
          DataHandler xslDataHandler = xslDocument.getContent().getData();
          InputStream xslStream = (InputStream)xslDataHandler.getInputStream();

          TransformerFactory factory = TransformerFactory.newInstance();
          javax.xml.transform.Transformer transformer = factory.newTransformer(
            new StreamSource(xslStream));

          // Setup input for XSLT transformation
          Source src = new StreamSource(in);

          // Resulting SAX events must be piped through XSLT
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          Result res = new StreamResult(out);

          // Start XSLT transformation
          transformer.transform(src, res);
          byte[] resultData = out.toByteArray();

          DataSource outDataSource;
          if ("xsl".equals(transformationName))
          {
            if (resultContains(resultData, "<html"))
            {
              System.out.println(">>>> xml to html");
              outDataSource = new MemoryDataSource(out.toByteArray(),
                "xsl_transform", "text/html");
              return new DataHandler(outDataSource);
            }
          }
          else if ("pdf".equals(transformationName))
          {
            if (resultContains(resultData, "<html"))
            {
              System.out.println(">>>> html to pdf");
              // result is html
              return htmlToPDF(resultData);
            }
            else if (resultContains(resultData, "http://www.w3.org/1999/XSL/Format"))
            {
              System.out.println(">>>> fo to pdf");
              // result is formatting objects
              return foToPDF(resultData);        
            }
          }
        }
      }
      // return same data
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOUtils.writeToStream(in, out);
      DataSource outDataSource =
        new MemoryDataSource(out.toByteArray(), "xsl_transform",
        content.getContentType()); // reset content type
      return new DataHandler(outDataSource);
    }
    catch (Exception ex)
    {
      throw new TransformationException(ex);
    }
  }

  private Document getXslDocument(String xslName) throws Exception
  {
    Document xslDocument = null;
    int dotIndex = xslName.lastIndexOf(".");
    xslName = (dotIndex == -1) ?
      xslName : xslName.substring(0, dotIndex);
    DocumentManagerClient client = getDocumentManagerClient();

    xslDocument =  client.loadDocumentByName(TEMPLATE_DOCTYPEID,
      PROPERTY_NAME, xslName, null, 0);

    return xslDocument;
  }

  public boolean resultContains(byte[] data, String match)
  {
    byte[] innerArray = match.getBytes();
    for (int i = 0; i < data.length - innerArray.length + 1; ++i)
    {
      boolean found = true;
      for (int j = 0; j < innerArray.length; ++j)
      {
        byte b = data[i + j];
        byte compareValue = innerArray[j];
        if (b >= 65 && b <= 90) b += 32; //toLowercase
        if (compareValue >= 65 && compareValue <= 90) compareValue += 32; //toLowercase        
        if (b != compareValue)
        {
          found = false;
          break;
        }
      }
      if (found) return true;
    }
    return false;
  }
  
  private String getXslName(Document document, String propertyName)
  {
    String xslName = null;
    Property property = DictionaryUtils.getProperty(document, propertyName);
    if (property != null)
    {
      xslName = property.getValue().get(0);
    }
    return xslName;
  }
  
  private DataHandler htmlToPDF(byte[] resultData) throws Exception
  {
    File inputFile = File.createTempFile("input", ".html");
    IOUtils.writeToFile(new ByteArrayInputStream(resultData), inputFile);
    File outputFile = File.createTempFile("output", ".pdf");
    String wkCommand = properties.get(WKCOMMAND_PROPERTY);
    Template template = Template.create(wkCommand);
    HashMap vars = new HashMap();
    vars.put("htmlFilePath", inputFile.getAbsolutePath());
    vars.put("pdfFilePath", outputFile.getAbsolutePath());
    String command = template.merge(vars);
    System.out.println("Convert command: " + command);
    Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec(command);
    TimeoutProcess wkProcess = new TimeoutProcess(process);
    int exitCode = wkProcess.waitFor(WK_TIMEOUT);
    if (exitCode == Integer.MIN_VALUE)
    {
      throw new Exception("WK conversion timeout");// Timeout
    }
    //Return DataHandler
    DataSource fds = new FileDataSource(outputFile, "application/pdf");
    return new DataHandler(fds);
  }
  
  private DataHandler foToPDF(byte[] resultData) throws Exception
  {
    //Create temporary xsl-fo file from datasource
    File inputFile = File.createTempFile("input", ".fo");
    IOUtils.writeToFile(new ByteArrayInputStream(resultData), inputFile);
    //Run converter
    File outputFile = File.createTempFile("output", ".pdf");
    String fopCommand = properties.get(FOPCOMMAND_PROPERTY);
    Template template = Template.create(fopCommand);
    HashMap vars = new HashMap();
    vars.put("foFilePath", inputFile.getAbsolutePath());
    vars.put("pdfFilePath", outputFile.getAbsolutePath());
    String command = template.merge(vars);
    System.out.println("Convert command: " + command);
    Runtime runtime = Runtime.getRuntime();
    Process process = runtime.exec(command);
    TimeoutProcess fopProcess = new TimeoutProcess(process);
    int exitCode = fopProcess.waitFor(FOP_TIMEOUT);
    System.out.println("Exit code: " + exitCode);
    if (exitCode == Integer.MIN_VALUE)
    {
      throw new Exception("FOP conversion timeout");// Timeout
    }
    //Return DataHandler
    DataSource fds = new FileDataSource(outputFile, "application/pdf");
    return new DataHandler(fds);
  }

  private DocumentManagerClient getDocumentManagerClient() throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");
    return new DocumentManagerClient(userId, password);
  }  
}
