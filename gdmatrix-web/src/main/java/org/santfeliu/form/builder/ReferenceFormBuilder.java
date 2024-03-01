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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.type.faces.XhtmlForm;
import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public class ReferenceFormBuilder extends MatrixFormBuilder
{
  private static final Entry[] REFERENCE_TABLE = new Entry[]
  {
    new Entry("html", "workflow.html", FORM_TYPEID, HtmlForm.class),
    new Entry("form", "workflow.form", FORM_TYPEID, HtmlForm.class),
    new Entry("xhtml", "xhtml", FORM_TYPEID, XhtmlForm.class)
  };

  @Override
  public List<FormDescriptor> findForms(String selectorBase)
  {
    try
    {
      Entry entry = getEntry(selectorBase);
      if (entry != null)
      {
        String name = entry.getName(selectorBase);
        List<Document> documents = findDocuments(entry, name);
        if (!documents.isEmpty())
        {
          List<FormDescriptor> descriptors = new ArrayList(documents.size());
          for (Document document : documents)
          {
            FormDescriptor descriptor = new FormDescriptor();
            String title = document.getTitle();
            String selector;
            int index = title.indexOf(":");
            if (index == -1)
            {
              selector = title;
            }
            else
            {
              selector = entry.prefix + ":" + title.substring(0, index);
              title = title.substring(index + 1);
            }
            descriptor.setTitle(title);
            descriptor.setSelector(selector);
            descriptors.add(descriptor);
          }
          return descriptors;
        }
      }
    }
    catch (Exception ex)
    {
      // return empty list
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  public Form getForm(String selector)
  {
    try
    {
      Entry entry = getEntry(selector);
      if (entry != null)
      {
        String name = entry.getName(selector);
        List<Document> documents = findDocuments(entry, name);
        if (!documents.isEmpty())
        {
          Document document = documents.get(0);
          String contentId = document.getContent().getContentId();

          Form form = (Form)entry.formClass.newInstance();
          form.read(getDocumentStream(contentId));
          form.setLastModified(document.getChangeDateTime());
          setup(form);
          return form;
        }
      }
    }
    catch (Exception ex)
    {
      // return null
    }
    return null;
  }

  private Entry getEntry(String selector)
  {
    if (selector == null) return null;
    
    Entry entry = null;
    int i = 0;
    while (i < REFERENCE_TABLE.length && entry == null)
    {
      Entry current = REFERENCE_TABLE[i];
      if (selector.startsWith(current.prefix + ":")) entry = current;
      i++;
    }
    return entry;
  }
  
  private List<Document> findDocuments(Entry entry, String name)
    throws Exception
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setVersion(0);
    filter.setDocTypeId(entry.docTypeId);
    Property property = new Property();
    property.setName(entry.property);
    property.getValue().add(name);
    filter.getProperty().add(property);
    OrderByProperty orderBy = new OrderByProperty();
    orderBy.setName(DocumentConstants.TITLE);
    orderBy.setDescending(false);
    filter.getOrderByProperty().add(orderBy);
    DocumentManagerClient client = getDocumentManagerClient();
    return client.findDocuments(filter);
  }
  
  static class Entry
  {
    String prefix;
    String property;
    String docTypeId;
    Class formClass;

    Entry(String prefix, String property, String docTypeId, Class formClass)
    {
      this.prefix = prefix;
      this.property = property;
      this.docTypeId = docTypeId;
      this.formClass = formClass;
    }

    String getName(String selector)
    {
      return selector.substring(prefix.length() + 1);
    }
  }
}
