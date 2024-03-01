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
package org.santfeliu.form.builder;

import java.util.Collections;
import java.util.List;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public class DocumentFormBuilder extends MatrixFormBuilder
{
  public static final String PREFIX = "doc";

  public List<FormDescriptor> findForms(String selector)
  {
    String docId = getDocId(selector);
    if (docId != null)
    {
      try
      {
        DocumentManagerClient client = getDocumentManagerClient();
        Document document = client.loadDocument(docId, 0, ContentInfo.ID);
        // check document is a form
        FormDescriptor descriptor = new FormDescriptor();
        descriptor.setTitle(getTitle(document));
        descriptor.setSelector(selector);
        return Collections.singletonList(descriptor);
      }
      catch (Exception ex)
      {
      }
    }
    return Collections.EMPTY_LIST;
  }

  public Form getForm(String selector)
  {
    String docId = getDocId(selector);
    if (docId != null)
    {
      try
      {
        DocumentManagerClient client = getDocumentManagerClient();
        Document document = client.loadDocument(docId, 0, ContentInfo.ID);
        // check document is a form
        String contentId = document.getContent().getContentId();
        HtmlForm form = new HtmlForm();
        form.read(getDocumentStream(contentId));
        setup(form);
        return form;
      }
      catch (Exception ex)
      {
      }
    }
    return null;
  }

  private String getDocId(String selector)
  {
    if (selector.startsWith(PREFIX + ":"))
    {
      String docId = selector.substring(PREFIX.length() + 1);
      return docId;
    }
    return null;
  }

  private String getTitle(Document doc)
  {
    String prettyTitle = 
      DictionaryUtils.getPropertyValue(doc.getProperty(), "prettyTitle");
    return (prettyTitle != null ? prettyTitle : doc.getTitle());
  }
  
}
