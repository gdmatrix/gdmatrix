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

import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.ManualScoped;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;

/**
 *
 * @author realor
 */
@Named("documentObjectBean")
@ManualScoped
public class DocumentObjectBean extends ObjectBean
{
  private Document document = new Document();

  @Inject
  DocumentFinderBean documentFinderBean;

  public DocumentObjectBean()
  {
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.DOCUMENT_TYPE;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : document.getTitle();
  }

  @Override
  public DocumentFinderBean getFinderBean()
  {
    return documentFinderBean;
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
  public String show()
  {
    return "/pages/doc/document.xhtml";
  }

  @Override
  public void loadObject() throws Exception
  {
    if (!NEW_OBJECT_ID.equals(objectId))
    {
      document = DocumentConfigBean.getPort().loadDocument(
        objectId, 0, ContentInfo.METADATA);
    }
    else
    {
      document = new Document();
    }
  }

  @Override
  public void loadTabs()
  {
    tabs = new ArrayList<>();
    tabs.add(new Tab("Main", "/pages/doc/document_main.xhtml"));
    tabs.add(new Tab("Content", "/pages/doc/document_content.xhtml", "documentContentTabBean"));
  }

  @Override
  public Serializable saveState()
  {
    return document;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.document = (Document)document;
  }

}
