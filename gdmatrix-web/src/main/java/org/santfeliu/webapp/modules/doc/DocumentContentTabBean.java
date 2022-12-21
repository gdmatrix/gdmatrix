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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named
@ManualScoped
public class DocumentContentTabBean extends TabBean
{
  @Inject
  DocumentObjectBean documentObjectBean;

  List<SelectItem> versionSelectItems;

  @Override
  public DocumentObjectBean getObjectBean()
  {
    return documentObjectBean;
  }

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public int getVersion()
  {
    return documentObjectBean.getDocument().getVersion();
  }

  public void setVersion(int version)
  {
    documentObjectBean.getDocument().setVersion(version);
  }

  public List<SelectItem> getVersionSelectItems()
  {
    if (documentObjectBean.isNew()) return null;

    if (versionSelectItems == null)
    {
      try
      {
        versionSelectItems = new ArrayList<>();
        Document document = documentObjectBean.getDocument();

        DocumentFilter filter = new DocumentFilter();
        filter.getDocId().add(document.getDocId());
        List<Document> docVersions =
          DocModuleBean.getPort(false).findDocuments(filter);
        for (Document docVersion : docVersions)
        {
          SelectItem selectItem = new SelectItem();
          String strVersion = String.valueOf(docVersion.getVersion());
          selectItem.setLabel(strVersion + " - " +
            docVersion.getCaptureDateTime());
          selectItem.setValue(String.valueOf(docVersion.getVersion()));
          versionSelectItems.add(selectItem);
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return versionSelectItems;
  }

  public Content getContent()
  {
    Content content = documentObjectBean.getDocument().getContent();
    if (content == null) content = new Content();

    return content;
  }

  @Override
  public boolean isModified()
  {
    return true;
  }

  @Override
  public void store()
  {
    Content content = getContent();
    System.out.println("save content " + content.getContentType());
  }

  public String getContentSize()
  {
    Content content = getContent();
    long size = content.getSize();

    return DocumentUtils.getSizeString(size);
  }

  public String getContentStorageType()
  {
    Content content = getContent();

    if (content.getContentId() == null)
      return "NO CONTENT";
    if (content.getUrl() != null)
      return "EXTERNAL";
    else
      return "INTERNAL";
  }

}
