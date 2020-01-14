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
package org.santfeliu.util.script.function;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.doc.State;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.enc.HtmlEncoder;

/*
 * Usage: linkDocuments(filter, options)
 *
 * returns: a HTML fragment that contains links to show and edit (when forEdit)
 * the document that satisfies the specified filter.
 *
 * Example:
 *
 *   ${linkDocuments({ref:"0018214", docTypeId:"sf:IMAGE",layer:"PARCELLES"},
 *       {label: "Foto", mid:"11614", forEdit:true})}
 *
 * Supported options (all are optional):
 * -mid: menuItem to jump
 * -topic: topic to jump ("Documents" by default if mid & topic are ommitted)
 * -forEdit: enables edit/create button
 * -onclick: javacript code to call when buttons are clicked
 * -viewLabel: view button label
 * -createLabel: create button label
 * -editLabel: edit button label
 */

/**
 *
 * @author unknown
 */
public class LinkDocumentsFunction extends BaseFunction
{
  public static final String DEFAULT_DOCUMENT_TOPIC = "Document";

  @Override
  public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    Object[] args)
  {
    if (args.length < 1) return "";

    Scriptable filterHash = (Scriptable)args[0];
    Scriptable optionsHash = null;
    String mid = null;
    String topic = null;
    boolean forEdit = false;
    String onclick = null;
    String viewLabel = "View";
    String createLabel = "Create";
    String editLabel = "Edit";

    if (args.length >= 2)
    {
      optionsHash = (Scriptable)(args[1]);
      if (optionsHash.has("mid", scope))
      {
        mid = Context.toString(optionsHash.get("mid", scope));
      }
      if (optionsHash.has("topic", scope))
      {
        topic = Context.toString(optionsHash.get("topic", scope));
      }
      if (optionsHash.has("forEdit", scope))
      {
        forEdit = 
          "true".equals(Context.toString(optionsHash.get("forEdit", scope)));
      }
      if (optionsHash.has("onclick", scope))
      {
        onclick = Context.toString(optionsHash.get("onclick", scope));
      }
      if (optionsHash.has("viewLabel", scope))
      {
        viewLabel = Context.toString(optionsHash.get("viewLabel", scope));
      }
      if (optionsHash.has("createLabel", scope))
      {
        createLabel = Context.toString(optionsHash.get("createLabel", scope));
      }
      if (optionsHash.has("editLabel", scope))
      {
        editLabel = Context.toString(optionsHash.get("editLabel", scope));
      }
    }

    DocumentFilter filter = new DocumentFilter();
    StringBuilder paramsBuffer = new StringBuilder();
    Object[] ids = filterHash.getIds();
    int i = 0;
    boolean undefined = false;
    while (i < ids.length && !undefined)
    {
      undefined = true;
      String name = ids[i].toString();
      if (filterHash.has(name, scope))
      {
        Object actualValue = filterHash.get(name, scope);
        if (actualValue != null)
        {
          String value = Context.toString(actualValue);
          if (!StringUtils.isBlank(value))
          {
            undefined = false;
            if (name.equals(DocumentConstants.DOCID))
            {
              // ignore docid
            }
            else if (name.equals(DocumentConstants.TITLE))
            {
              addParameter(paramsBuffer, name, value);
            }
            else if (name.equals(DocumentConstants.DOCTYPEID))
            {
              addParameter(paramsBuffer, name, value);
            }
            else
            {
              addParameter(paramsBuffer, name, value);
              Property property = new Property();
              property.setName(name);
              property.getValue().add(value);
              filter.getProperty().add(property);
            }
          }
        }
      }
      i++;
    }

    StringBuilder buffer = new StringBuilder();    

    String contextPath = MatrixConfig.getProperty("contextPath");
    if (!undefined)
    {
      filter.setFirstResult(0);
      OrderByProperty order = new OrderByProperty();
      order.setName(DocumentConstants.TITLE);
      filter.getOrderByProperty().add(order);
      filter.setIncludeContentMetadata(true);
      filter.getStates().add(State.DRAFT);
      filter.getStates().add(State.COMPLETE);
      filter.getStates().add(State.RECORD);

      String docId = null;      
      String userId = MatrixConfig.getProperty("adminCredentials.userId");
      String password = MatrixConfig.getProperty("adminCredentials.password");
      DocumentManagerClient client = new DocumentManagerClient(userId, password);
      List<Document> documents = client.findDocuments(filter);

      if (!documents.isEmpty())
      {
        buffer.append("<table class=\"linkDocuments\">");
        int count = 0;
        for (Document document : documents)
        {
          docId = document.getDocId();
          Content content = document.getContent();
          String contentId = content.getContentId();
          String mimeType = content.getContentType();

          buffer.append("<tr");
          buffer.append(" class=\"row");
          buffer.append(count % 2);
          buffer.append("\">");

          buffer.append("<td class=\"title\">");
          buffer.append(HtmlEncoder.encode(getTitle(document.getTitle())));
          buffer.append("</td>");

          buffer.append("<td class=\"actions\">");

          if (contentId != null) // show open button
          {
            buffer.append("<a href=\"/documents/");
            buffer.append(docId);
            buffer.append("\" target=\"document\" title=\"");
            buffer.append(HtmlEncoder.encode(viewLabel));
            buffer.append("\"><img src=\"");
            String imageUrl = DocumentUtils.typeToImage(contextPath +
              "/common/doc/images/extensions/", mimeType);
            buffer.append(imageUrl);
            buffer.append("\"></a>");
          }

          if (forEdit) // show edit button
          {
            buffer.append("<a ");
            if (onclick != null)
            {
              buffer.append(" onclick=\"");
              buffer.append(onclick);
              buffer.append(";return true;\" ");
            }
            if (mid != null)
            {
              buffer.append("href=\"/go.faces?xmid=");
              buffer.append(mid);
            }
            else if (topic != null)
            {
              buffer.append("href=\"/go.faces?topic=");
              buffer.append(topic);
            }
            else
            {
              buffer.append("href=\"/go.faces?topic=" + DEFAULT_DOCUMENT_TOPIC);
            }
            buffer.append("&docid=");
            buffer.append(docId);
            buffer.append("\" class=\"edit\" target=\"document\" title=\"");
            buffer.append(HtmlEncoder.encode(editLabel));
            buffer.append("\"><img src=\"");
            String editUrl = contextPath + "/common/misc/images/edit.png";
            buffer.append(editUrl);
            buffer.append("\"></a>");
          }
          buffer.append("</td>");

          buffer.append("</tr>");
          count++;
        }
        buffer.append("</table>");
      }
      if (forEdit) // add button
      {
        buffer.append("<a ");
        if (onclick != null)
        {
          buffer.append(" onclick=\"");
          buffer.append(onclick);
          buffer.append(";return true;\" ");
        }
        if (mid != null)
        {
          buffer.append("href=\"/go.faces?xmid=");
          buffer.append(mid);
        }
        else if (topic != null)
        {
          buffer.append("href=\"/go.faces?topic=");
          buffer.append(topic);
        }
        else
        {
          buffer.append("href=\"/go.faces?topic=" + DEFAULT_DOCUMENT_TOPIC);
        }
        buffer.append("&docid=new");
        buffer.append(paramsBuffer);
        buffer.append("\" class=\"addDocument\" target=\"document\" title=\"");
        buffer.append(HtmlEncoder.encode(createLabel));
        buffer.append("\"><img src=\"");
        String addUrl = contextPath + "/common/misc/images/add.png";
        buffer.append(addUrl);
        buffer.append("\"></a>");
      }
      if (documents.isEmpty() && !forEdit)
      {
        buffer.append("No documents.");
      }
    }
    else
    {
      buffer.append("No linkable documents.");
    }
    return buffer.toString();
  }

  private void addParameter(StringBuilder buffer, String name, String value)
  {
    try
    {
      value = URLEncoder.encode(value, "UTF-8");
      buffer.append("&_").append(name).append("=").append(value);
    }
    catch (UnsupportedEncodingException ex)
    {
    }
  }

  private String getTitle(String title)
  {
    int index = title.lastIndexOf(":");
    if (index != -1)
    {
      title = title.substring(index + 1);
    }
    return title;
  }
}
