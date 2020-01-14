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
package org.santfeliu.swing.form.util;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.swing.form.FormDesigner;
import org.santfeliu.swing.form.ie.HtmlFormImporter;

/**
 *
 * @author realor
 */
public class FormChecker
{
  private DocumentManagerClient client;

  public FormChecker(DocumentManagerClient client)
  {
    this.client = client;
  }

  public void checkForms(PrintWriter writer) throws Exception
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId("FORM");
    filter.setMaxResults(0);
    List<Document> documents = client.findDocuments(filter);
    for (Document document : documents)
    {
      document = client.loadDocument(document.getDocId());
      checkForm(document, writer);
    }
  }

  public void checkForm(Document document, PrintWriter writer) throws Exception
  {
    System.out.println("Checking form: " + document.getTitle());
    FormDesigner designer = new FormDesigner();
    HtmlFormImporter importer = new HtmlFormImporter();
    InputStream is = document.getContent().getData().getInputStream();
    try
    {
      importer.importPanel(is, designer);
      Dimension size = designer.getMinimumSize();
      double width = size.getWidth();      
      Property prop = DictionaryUtils.getPropertyByName(document.getProperty(), "workflow.form");
      if (prop != null)
      {
        String name = prop.getValue().get(0);
        System.out.println(name + " => width: " + width);
        if (width < 640)
        {
          writer.println(document.getDocId() + ": " + name +
              " => width: " + ((int)width) + "px");
        }
      }
    }
    finally
    {
      is.close();
    }    
  }

  public static void main(String[] args)
  {
    try
    {
      DocumentManagerClient client = new DocumentManagerClient(
        new URL("http://xxxxxx/wsdirectory"), "admin", "*****");
      FormChecker checker = new FormChecker(client);
      PrintWriter writer = new PrintWriter("c:/forms.txt");
      try
      {
        checker.checkForms(writer);
      }
      finally
      {
        writer.close();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
