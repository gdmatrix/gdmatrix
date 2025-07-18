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
package org.santfeliu.webapp.modules.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;
import static org.santfeliu.webapp.modules.doc.DocModuleBean.getPort;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.SearchTab;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class DocumentTypeBean extends TypeBean<Document, DocumentFilter>
{
  private static final String BUNDLE_PREFIX = "$$documentBundle.";

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.DOCUMENT_TYPE;
  }

  @Override
  public String getObjectId(Document document)
  {
    return document.getDocId();
  }

  @Override
  public String describe(Document document)
  {
    return document.getTitle() + " (" + document.getDocId() + ")";
  }

  @Override
  public Document loadObject(String objectId)
  {
    try
    {
      return getPort(true).loadDocument(objectId, 0, ContentInfo.METADATA);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Document document)
  {
    return document.getDocTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/doc/document.xhtml");

    List<SearchTab> searchTabs = new ArrayList();
    SearchTab searchTab =
      new SearchTab("List", "/pages/doc/document_list.xhtml");
    searchTab.getTableProperties().add(new TableProperty("docId",
      BUNDLE_PREFIX + "documentSearch_docId", "col-1"));
    searchTab.getTableProperties().add(new TableProperty("docTypeId",
      BUNDLE_PREFIX + "documentSearch_docTypeId", "col-3"));
    TableProperty titleColumn = new TableProperty("title",
      BUNDLE_PREFIX + "documentSearch_title", "col-8");
    titleColumn.setIcon("text-xl mr-1");    
    searchTab.getTableProperties().add(titleColumn);
    
    searchTab.getOrderBy().add("changeDateTime:desc");

    searchTabs.add(searchTab);

    objectSetup.setSearchTabs(searchTabs);

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", "pi pi-file-o", "/pages/doc/document_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_content", "pi pi-save", "/pages/doc/document_content.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_cases", "pi pi-folder", "/pages/doc/document_cases.xhtml", "documentCasesTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_documents", "pi pi-file-o", "/pages/doc/document_documents.xhtml", "documentDocumentsTabBean"));  
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_acl", "pi pi-key", "/pages/doc/document_acl.xhtml", "documentACLTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_policies", "material-icons-outlined mi-policy text-lg", "/pages/policy/document_policies.xhtml", "documentPoliciesTabBean"));      
    objectSetup.setEditTabs(editTabs);
    
    objectSetup.getProperties().put("showTypeId", "true");

    return objectSetup;
  }

  @Override
  public DocumentFilter queryToFilter(String query, String typeId)
  {
    if (query == null) query = "";

    DocumentFilter filter = new DocumentFilter();
    if (query.matches(".{0,4}[0-9]+"))
    {
      filter.getDocId().add(query);
    }
    else
    {
      filter.setTitle(query);
    }
    if (typeId != null)
    {
      filter.setDocTypeId(typeId);
    }
    filter.setMaxResults(10);

    return filter;
  }

  @Override
  public String filterToQuery(DocumentFilter filter)
  {
    if (!filter.getDocId().isEmpty())
    {
      return filter.getDocId().get(0);
    }
    else if (filter.getTitle() != null)
    {
      String query = filter.getTitle();
      if (query.startsWith("%")) query = query.substring(1);
      if (query.endsWith("%")) query = query.substring(0, query.length() - 1);
      return query;
    }
   return "";
  }

  @Override
  public List<Document> find(DocumentFilter filter)
  {
    try
    {
      return getPort(true).findDocuments(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  public String getValue(Object object)
  {
    return String.valueOf(object);
  }

  public static String formatTitle(Document document)
  {
    String title = document.getTitle();
    return title.replace("_", " ");
  }

  public static String getContentIcon(Document document)
  {
    Content content = document.getContent();

    if (content == null) return "fa-regular fa-file";
    String contentType = content.getContentType();

    return getContentIcon(contentType);
  }

  public static String getContentIcon(String contentType)
  {
    if (contentType == null) return "fa-regular fa-file";

    switch (contentType)
    {
      case "application/java-archive":
      case "text/html":
      case "text/xml":
        return "fa-regular fa-file-code";

       // Excel
      case "application/vnd.ms-excel":
      case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
        return "fa-regular fa-file-excel";

      // Word
      case "application/msword":
        return "fa-regular fa-file-word";

      // Powerpoint
      case "application/vnd.ms-powerpoint":
        return "fa-regular fa-file-powerpoint";

      // PDF
      case "application/pdf":
        return "fa-regular fa-file-pdf";

      // ZIP
      case "application/zip":
        return "fa-regular fa-file-zipper";

      // CSV
      case "text/csv":
        return "fa-regular fa-file-csv";

      // Text
      case "text/plain":
        return "fa-regular fa-file-lines";

      default:
        if (contentType.indexOf("image/") == 0)
          return "fa-regular fa-file-image";
        if (contentType.indexOf("video/") == 0)
          return "fa-regular fa-file-video";
        else
          return "fa-regular fa-file";
    }
  }
}
