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

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.web.WebUtils;
import org.santfeliu.doc.web.DocumentConfigBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author realor
 */
@Named("documentMainTabBean")
@SessionScoped
public class DocumentMainTabBean extends TabBean
{
  private Document document = new Document();
  private String docId;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  public Document getDocument()
  {
    return document;
  }

  public void setDocument(Document document)
  {
    this.document = document;
  }

  @Override
  public DocumentObjectBean getObjectBean()
  {
    return WebUtils.getBacking("documentObjectBean");
  }

  @Override
  public void load()
  {
    String objectId = getObjectId();
    if (!objectId.equals(docId))
    {
      docId = objectId;
      if (!NEW_OBJECT_ID.equals(docId))
      {
        try
        {
          document = DocumentConfigBean.getPort().loadDocument(
            docId, 0, ContentInfo.METADATA);
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
      else
      {
        document = new Document();
      }
    }
  }

  public void save()
  {
    System.out.println(document.getTitle());
  }
}
