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
package org.santfeliu.workflow.form;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.net.HttpClient;
import org.santfeliu.util.template.WebTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

/**
 *
 * @author unknown
 */
public class CustomForm extends Form
{
  protected static final String CHARSET = "utf-8";
  protected static final String WSDL_LOCATION = "wsdlLocation";

  private transient Set readVariables;
  private transient Set writeVariables;

  public CustomForm()
  {
  }

  @Override
  public Set getReadVariables()
  {
    if (readVariables == null)
      parseCustomForm();
    return readVariables;
  }

  @Override
  public Set getWriteVariables()
  {
    if (writeVariables == null)
      parseCustomForm();
    return writeVariables;
  }

  private void parseCustomForm()
  {
    readVariables = Collections.EMPTY_SET;
    writeVariables = Collections.EMPTY_SET;
    try
    {
      String url = null;
      Object otype = parameters.get("type");
      Object oref = parameters.get("ref");
      if (otype != null && oref != null)
      {
        String type = String.valueOf(otype);
        String ref = String.valueOf(oref);
        if ("url".equals(type))
        {
          url = ref;
        }
        else
        {
          url = getFormUrl(type, ref);
        }
        if (url != null)
        {
          System.out.println("**** PARSE CUSTOM FORM: " + ref);

          readVariables = new HashSet();
          writeVariables = new HashSet();

          HttpClient httpClient = new HttpClient();
          httpClient.setURL(url);
          httpClient.setForceHttp(true);
          httpClient.setDownloadContentType("text/");
          httpClient.setMaxContentLength(524288);
          httpClient.setRequestProperty("User-Agent", 
                                        HttpClient.USER_AGENT_IE6);
          httpClient.setRequestProperty("Accept-Charset", "utf-8");
          httpClient.connect();

          String content = httpClient.getContentAsString();
          if (content != null)
          {
            WebTemplate template = WebTemplate.create(content);
            readVariables = template.getReferencedVariables();
            content = template.merge(new HashMap());
            Document document = parseDocument(content);
            findVariables(document);
          }
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private String getFormUrl(String type, String ref)
  {
    String url = null;
    try
    {
      String formName = ref;
      DocumentManagerClient client = getDocumentManagerClient();
      String docTypeId = "FORM";
      org.matrix.doc.Document document = 
        client.loadDocumentByName(docTypeId, "workflow." + type, formName,
        null, 0);
      if (document != null)
      {
        url = "http://localhost:" 
         + MatrixConfig.getProperty("org.santfeliu.web.defaultPort")
         + "/documents/" +
          document.getContent().getContentId(); // TODO: fix url
      }
    }
    catch (Exception ex)
    {
    }
    return url;
  }

  private Document parseDocument(String content)
    throws IOException
  {
    Tidy tidy = new Tidy();
    tidy.setOnlyErrors(true);
    tidy.setShowWarnings(false);

    tidy.setInputEncoding(CHARSET);
    ByteArrayInputStream bi = 
      new ByteArrayInputStream(content.getBytes(CHARSET));
    Document document = tidy.parseDOM(bi, null);
    return document;
  }

  private void findVariables(Node node)
  {
    if (node instanceof Element) // look for writeVariables
    {
      Element element = (Element) node;
      String elemName = element.getNodeName().toLowerCase();
      if (elemName.equals("input") || elemName.equals("select") || 
          elemName.equals("textarea"))
      {
        String var = element.getAttribute("name");
        if (var != null)
        {
          writeVariables.add(var);
        }
      }
    }
    // find into children
    Node child = node.getFirstChild();
    while (child != null)
    {
      findVariables(child);
      child = child.getNextSibling();
    }
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    String userId = 
      MatrixConfig.getProperty("adminCredentials.userId");
    String password = 
      MatrixConfig.getProperty("adminCredentials.password");

    return new DocumentManagerClient(userId, password);
  }
}
