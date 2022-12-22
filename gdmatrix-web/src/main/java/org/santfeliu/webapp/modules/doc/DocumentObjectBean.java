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
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.State;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.ManualScoped;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.Tab;
import org.santfeliu.webapp.helpers.PropertyHelper;

/**
 *
 * @author realor
 */
@Named
@ManualScoped
public class DocumentObjectBean extends ObjectBean
{
  private Document document = new Document();
  private final PropertyHelper propertyHelper = new PropertyHelper()
  {
    @Override
    public List<Property> getProperties()
    {
      return document.getProperty();
    }
  };

  @Inject
  DocumentTypeBean documentTypeBean;

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
  public Document getObject()
  {
    return isNew() ? null : document;
  }

  @Override
  public DocumentTypeBean getTypeBean()
  {
    return documentTypeBean;
  }

  @Override
  public DocumentFinderBean getFinderBean()
  {
    return documentFinderBean;
  }

  public PropertyHelper getPropertyHelper()
  {
    return propertyHelper;
  }
  
  @Override
  public String getDescription()
  {
    return isNew() ? "" : document.getTitle();
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
      document = DocModuleBean.getPort(false).loadDocument(
        objectId, 0, ContentInfo.METADATA);
    }
    else
    {
      document = new Document();
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    DocModuleBean.getPort(false).storeDocument(document);
  }

  @Override
  public void loadTabs()
  {
    tabs = new ArrayList<>();
    tabs.add(new Tab("Main", "/pages/doc/document_main.xhtml"));
    tabs.add(new Tab("Content", "/pages/doc/document_content.xhtml", "documentContentTabBean"));
  }

  public void lock()
  {
    try
    {
      DocumentManagerPort port = DocModuleBean.getPort(false);
      port.lockDocument(document.getDocId(), 0);
      document = port.loadDocument(document.getDocId(), 0, ContentInfo.METADATA);
      info("Document locked by " + document.getLockUserId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void unlock()
  {
    try
    {
      DocumentManagerPort port = DocModuleBean.getPort(false);
      port.unlockDocument(document.getDocId(), 0);
      document = port.loadDocument(document.getDocId(), 0, ContentInfo.METADATA);
      info("Document unlocked.");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public SelectItem[] getDocumentStateSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.doc.web.resources.DocumentBundle", getLocale());
    return FacesUtils.getEnumSelectItems(State.class, bundle);
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
