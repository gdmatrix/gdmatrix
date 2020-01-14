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
package org.santfeliu.doc.util;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.activation.DataHandler;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.fmt.p7m.P7MDocument;
import org.santfeliu.doc.fmt.p7m.P7MUtils;

/**
 *
 * @author realor
 */
public class DocumentTimeStamper
{
  public void start(String wsdirectory, String tsaUrl,
     String tsProperty, String userId, String password,
     String dbUsername, String dbPassword, int count)
  {
    try
    {
      DocumentManagerClient client = new DocumentManagerClient(
        new URL(wsdirectory), userId, password);
      Class.forName("oracle.jdbc.driver.OracleDriver");
      Connection connection = DriverManager.getConnection(
        "jdbc:oracle:thin:@oracle:1521:ajfeliu", dbUsername, dbPassword);
      String query = "select docid from dom_document d, cnt_content c " +
        "where d.contentid = c.uuid and c.mimetype = 'application/pkcs7-mime' " +
        "and not exists (select 0 from dom_metadata m where m.docid = d.docid " +
        "and m.version = d.version and propname = '" + tsProperty + "')" +
        "and d.lastversion = 'T' order by c.capturedate, d.docid asc";
      int total = 0;
      int valids = 0;
      try
      {
        Statement stmt = connection.createStatement();
        try
        {
          stmt.setMaxRows(count);
          ResultSet rs = stmt.executeQuery(query);
          try
          {
            while (rs.next())
            {
              String docId = rs.getString(1);
              Document document = client.loadDocument(docId);
              System.out.println("\nDocument " + document.getDocId() + ": " + document.getTitle());
              DataHandler data = document.getContent().getData();
              P7MDocument p7m = new P7MDocument(data.getInputStream());
              boolean valid;
              try
              {
                valid = p7m.checkSignaturesAndTimeStamps(true);
              }
              catch (Exception ex)
              {
                valid = false;
              }
              System.out.println("Document Valid: " + valid);
              if (valid) valids++;
              total++;

              ByteArrayOutputStream os = new ByteArrayOutputStream();
              data.writeTo(os);
              byte[] dataBytes = os.toByteArray();
              String tsBase64 = P7MUtils.createBase64TimeStamp(
                tsaUrl, dataBytes);

              Property property1 = new Property();
              property1.setName(tsProperty);
              property1.getValue().add(tsBase64);
              document.getProperty().add(property1);

              Property property2 = new Property();
              property2.setName("validSignatures");
              property2.getValue().add(String.valueOf(valid));
              document.getProperty().add(property2);

              document.getContent().setData(null);
              client.storeDocument(document);
              System.out.println(tsBase64);
            }
            Thread.sleep(500);
          }
          finally
          {
            rs.close();
          }
        }
        finally
        {
          stmt.close();
        }
      }
      finally
      {
        connection.close();
      }
      System.out.println("Valid documents " + valids + "/" + total);
    }
    catch (Exception ex)
    {
      ex.printStackTrace(System.out);
    }
  }

  public static void main(String[] args)
  {
    try
    {
      DocumentTimeStamper ts = new DocumentTimeStamper();
      int count = Integer.parseInt(args[0]);
      long t0 = System.currentTimeMillis();
      ts.start("http://dione.esantfeliu.org/wsdirectory",
         "http://psis.catcert.net/psis/catcert/tsp",
         "externalTimeStamp", "admin", "****", "ajuntament", "****", count);
      long t1 = System.currentTimeMillis();
      double ellapsed = (t1 - t0) / 1000.0;
      double timePerDocument = ellapsed / count;
      System.out.println("Ellapsed: " + ellapsed + " seconds");
      System.out.println("Time/doc: " + timePerDocument + " seconds");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
