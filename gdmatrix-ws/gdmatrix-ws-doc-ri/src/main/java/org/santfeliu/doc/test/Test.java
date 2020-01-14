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
package org.santfeliu.doc.test;

import java.net.URL;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.client.DocumentManagerClient;


/**
 *
 * @author unknown
 */
public class Test
{
  public Test()
  {  
  }
  
  public static DocumentManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(DocumentManagerService.class);
    return endpoint.getPort(DocumentManagerPort.class,
      null, null);
  }
  
  public static void main(String[] args)
  {
    try
    {
      DocumentManagerClient client = 
        new DocumentManagerClient(new URL("http://localhost/wsdirectory"));
      Document d = new Document();
      d.setDocId("1139");
      d.setIncremental(false);
      d.setTitle("Test Incremental");
      d.setDocTypeId("Document");
      client.storeDocument(d);
      


/*      Content content = new Content();
      content.setData(new DataHandler(new FileDataSource("c:/dades_padronals.jrxml")));
      Document document = new Document();
      document.setDocId("56485");
      document.setTitle("Carpeta ciutadana: Dades padronals");
      document.setDocTypeId("report");
      document.setContent(content);
      Property property = new Property();
      property.setName("report");
      property.getValue().add("dades_padronals");
      document.getProperty().add(property);
      document = client.storeDocument(document);
      System.out.println(document.getDocId() + " " + document.getVersion());
  */


/*      DocumentManagerPort port = getPort();
      Content content = port.loadContent("6620f0c7-9d10-4751-9c72-acfbc7491a39");
      System.out.println(content.getCaptureDate());
*/
//      DocumentFilter df = new DocumentFilter();
//      df.setMaxResults(300);
//      Property p = new Property();
//      p.setName("test");
//      p.getValue().add("%");
//      df.getProperty().add(p);
//      DocumentUtils.setOrderByProperty(df, "author", false);
//      df.getOutputProperty().add("author");
//      df.setIncludeContentMetadata(false);
//      df.setVersion(0);
//      df.getStates().add(State.DRAFT);
//      df.getStates().add(State.COMPLETE);
//      df.getStates().add(State.RECORD);
//      DocumentManagerPort port = getPort();
//      System.out.println(port.countDocuments(df));
//      int i = 0;
//      for (Document d : port.findDocuments(df))
//      {
//        System.out.println(i++ + " " + d.getDocId() + " " + DocumentUtils.getPropertyValue(d, "author"));
//      }

  /*    Content content = new Content();
      FileDataSource ds = new FileDataSource("c:/test.pdf");
      content.setData(new DataHandler(ds));
      content.setLanguage("ca");
      //content = port.storeContent(content);

      Document document = new Document();
      document.setDocTypeId("ACTA");
      document.setLanguage("es");
      document.setState(State.DRAFT);
      document.setTitle("Acta de la reunión en CATCert");
      document.setContent(content);
      port.storeDocument(document);
*/
      
/*
      System.out.println("reading...");
      content = port.loadContent(content.getContentId());
      content.getData().writeTo(new FileOutputStream("c:/test2.pdf"));
*/
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }  
}
