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

import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.webapp.TypeBean;
import static org.santfeliu.webapp.modules.doc.DocModuleBean.getPort;

/**
 *
 * @author realor
 */
@Named
@ApplicationScoped
public class DocumentTypeBean extends TypeBean<Document, DocumentFilter>
{
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.DOCUMENT_TYPE;
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
      if (!query.startsWith("%")) query = "%" + query;
      if (!query.endsWith("%")) query += "%";
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

}
