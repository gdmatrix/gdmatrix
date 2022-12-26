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

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.activation.DataHandler;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.OrderByProperty;
import org.matrix.doc.State;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
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
  private transient List<Document> versions;
  private transient final PropertyHelper propertyHelper = new PropertyHelper()
  {
    @Override
    public List<Property> getProperties()
    {
      return document.getProperty();
    }
  };

  /* new content */
  File fileToStore;
  String fileNameToStore;
  String urlToStore;
  String contentIdToStore;

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

  public File getFileToStore()
  {
    return fileToStore;
  }

  public String getFileNameToStore()
  {
    return fileNameToStore;
  }

  public String getUrlToStore()
  {
    return urlToStore;
  }

  public void setUrlToStore(String urlToStore)
  {
    this.urlToStore = urlToStore;
  }

  public String getContentIdToStore()
  {
    return contentIdToStore;
  }

  public void setContentIdToStore(String contentIdToStore)
  {
    this.contentIdToStore = contentIdToStore;
  }

  public Content getContent()
  {
    Content content = document.getContent();
    if (content == null)
    {
      content = new Content();
      document.setContent(content);
    }
    return content;
  }

  public String getVersionLabel()
  {
    // TODO: localize text
    int version = document.getVersion();
    return version > 0 ? "Version " + version : "New version";
  }

  public String getContentSize()
  {
    Content content = getContent();
    if (content.getSize() == null) return "";

    long size = content.getSize();
    return DocumentUtils.getSizeString(size);
  }

  public String getContentStorageType()
  {
    Content content = getContent();

    if (content.getContentId() == null)
    {
      return "NO CONTENT";
    }
    if (content.getUrl() != null)
    {
      return "EXTERNAL";
    } else
    {
      return "INTERNAL";
    }
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
    versions = null;

    if (fileToStore != null)
    {
      fileNameToStore = null;
      fileToStore.delete();
      fileToStore = null;
    }

    if (!NEW_OBJECT_ID.equals(objectId))
    {
      document = DocModuleBean.getPort(false).loadDocument(
        objectId, 0, ContentInfo.METADATA);
    } else
    {
      document = new Document();
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    if (fileToStore != null)
    {
      FileDataSource ds = new FileDataSource(fileToStore);
      DataHandler dh = new DataHandler(ds);
      Content content = new Content();
      content.setData(dh);
      document.setContent(content);
    }
    else if (!StringUtils.isBlank(urlToStore))
    {
      System.out.println("urlToStore: " + urlToStore);
      Content content = new Content();
      content.setUrl(urlToStore.trim());
      document.setContent(content);
      urlToStore = null;
    }
    else if (!StringUtils.isBlank(contentIdToStore))
    {
      System.out.println("contentId: " + contentIdToStore);
      Content content = new Content();
      content.setContentId(contentIdToStore.trim());
      document.setContent(content);
      contentIdToStore = null;
    }

    document = DocModuleBean.getPort(false).storeDocument(document);
    setObjectId(document.getDocId());

    System.out.println(">>> stored URL: " + document.getContent().getUrl());

    if (fileToStore != null)
    {
      fileNameToStore = null;
      fileToStore.delete();
      fileToStore = null;
    }
    versions = null;
  }

  @Override
  public void loadTabs()
  {
    tabs = new ArrayList<>();
    tabs.add(new Tab("Main", "/pages/doc/document_main.xhtml"));
    tabs.add(new Tab("Content", "/pages/doc/document_content.xhtml"));
  }

  public void lock()
  {
    try
    {
      DocumentManagerPort port = DocModuleBean.getPort(false);
      port.lockDocument(document.getDocId(), document.getVersion());
      document = port.loadDocument(document.getDocId(), document.getVersion(),
        ContentInfo.METADATA);
      info("Document locked by " + document.getLockUserId());
    } catch (Exception ex)
    {
      error(ex);
    }
  }

  public void unlock()
  {
    try
    {
      DocumentManagerPort port = DocModuleBean.getPort(false);
      port.unlockDocument(document.getDocId(), document.getVersion());
      document = port.loadDocument(document.getDocId(), document.getVersion(),
        ContentInfo.METADATA);
      info("Document unlocked.");
    } catch (Exception ex)
    {
      error(ex);
    }
  }

  public void handleFileUpload(FileUploadEvent event)
  {
    try
    {
      UploadedFile uploadedFile = event.getFile();
      fileNameToStore = uploadedFile.getFileName();
      fileToStore = File.createTempFile("upload", ".bin");
      try (InputStream is = uploadedFile.getInputStream())
      {
        IOUtils.writeToFile(is, fileToStore);
      }
      uploadedFile.delete();
    } catch (Exception ex)
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

  public void loadVersions()
  {
    if (versions == null)
    {
      try
      {
        if (isNew())
        {
          versions = Collections.EMPTY_LIST;
        } else
        {
          DocumentFilter filter = new DocumentFilter();
          filter.getDocId().add(document.getDocId());
          filter.setVersion(-1);
          filter.setIncludeContentMetadata(false);
          filter.getStates().add(State.DRAFT);
          filter.getStates().add(State.COMPLETE);
          filter.getStates().add(State.RECORD);
          filter.getStates().add(State.DELETED);
          OrderByProperty order = new OrderByProperty();
          order.setName(DocumentConstants.VERSION);
          order.setDescending(false);
          filter.getOrderByProperty().add(order);
          versions = DocModuleBean.getPort(false).findDocuments(filter);
        }
      } catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public List<Document> getVersions()
  {
    return versions;
  }

  public void newVersion()
  {
    document.setVersion(DocumentConstants.NEW_VERSION);
  }

  public void loadVersion(int version)
  {
    if (!NEW_OBJECT_ID.equals(objectId) && document.getVersion() != version)
    {
      try
      {
        document = DocModuleBean.getPort(false).loadDocument(
          objectId, version, ContentInfo.METADATA);
      } catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  @Override
  public Serializable saveState()
  {
    return document;
  }

  @Override
  public void restoreState(Serializable state)
  {
    this.document = (Document) document;
  }

}
