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
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
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
import org.santfeliu.faces.matrixclient.model.DefaultMatrixClientModel;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.webapp.ObjectBean;
import static org.santfeliu.webapp.modules.doc.DocModuleBean.getPort;
import static org.santfeliu.doc.web.DocumentUrlBuilder.DOC_SERVLET_URL;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import static org.matrix.doc.DocumentConstants.DELETE_OLD_VERSIONS;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class DocumentObjectBean extends ObjectBean
{
  private Document document = new Document();
  private List<Document> versions;
  private String formSelector;

  /* new content */
  private File fileToStore;
  private String fileNameToStore;
  private String urlToStore;
  private String contentIdToStore;

  @Inject
  DocumentTypeBean documentTypeBean;

  @Inject
  DocumentFinderBean documentFinderBean;

  private DefaultMatrixClientModel clientModel;

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

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
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
    return document.getContent();
  }

  public DefaultMatrixClientModel getClientModel()
  {
    if (clientModel == null)
    {
      clientModel = new DefaultMatrixClientModel();
    }
    return clientModel;
  }

  public void setClientModel(DefaultMatrixClientModel clientModel)
  {
    this.clientModel = clientModel;
  }

  public void documentEdited()
  {
    try
    {
      clientModel.parseResult();
      loadObject();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String getVersionLabel()
  {
    int version = document.getVersion();
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.doc.web.resources.DocumentBundle", getLocale());

    return version > 0 ?
      bundle.getString("document_version") + " " + version :
      bundle.getString("document_newVersion");
  }

  public String getContentSize()
  {
    Content content = getContent();
    if (content.getSize() == null || content.getUrl() != null) return "";

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
    }
    else
    {
      return "INTERNAL";
    }
  }

  public String getContentLanguage()
  {
    Content content = getContent();
    String language = content.getLanguage();

    return language == null ? "" : DocumentUtils.extendLanguage(language);
  }

  public boolean isRenderDocumentURL()
  {
    if (versions == null) //first load
      return true;
    else if (versions.isEmpty()) //new document
      return false;
    else
      return (document.getVersion() == versions.get(0).getVersion());
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

  public void setNewClassId(String classId)
  {
    if (!StringUtils.isBlank(classId))
    {
      List<String> currentClassIdList = document.getClassId();
      if (!currentClassIdList.contains(classId))
      {
        currentClassIdList.add(classId);
      }
    }
  }

  public String getDocumentURL(boolean withContentId, boolean downloadable,
    boolean fullUrl, int maxFilenameLength)
  {
    String url;

    Content content = document.getContent();

    if (content.getUrl() != null)
    {
      url = content.getUrl();
    }
    else
    {
      String title = document.getTitle() == null ?
        "document" : document.getTitle();

      ExternalContext extContext = getExternalContext();
      HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
      String contextPath = request.getContextPath();

      String extension =
        MimeTypeMap.getMimeTypeMap().getExtension(content.getContentType());

      String filename = DocumentUtils.getFilename(title);
      if (filename.length() == 0) filename = "document";
      else if (filename.length() > maxFilenameLength)
      {
        filename = filename.substring(0, maxFilenameLength - 1);
      }
      filename = filename + "." + extension;

      StringBuilder urlBuffer = new StringBuilder();

      if (fullUrl)
      {
        String serverName = HttpUtils.getServerName(request);
        String protocol = HttpUtils.getScheme(request);
        String port = MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
        port = !"80".equals(port) ? ":" + port : "";

        urlBuffer.append(protocol).append("://")
          .append(serverName).append(port);
      }

      urlBuffer.append(contextPath).append(DOC_SERVLET_URL);

      if (withContentId)
      {
        urlBuffer.append(content.getContentId());
      }
      else
      {
        urlBuffer.append(document.getDocId());
      }

      urlBuffer.append("/").append(filename);

      if (downloadable)
      {
        urlBuffer.append("?saveas=").append(filename);
      }
      url = urlBuffer.toString();
    }
    return url;
  }

  @Override
  protected void processParameters(Map<String, Object> parameters)
  {
    if (parameters != null && document != null)
    {
      for (String key : parameters.keySet())
      {
        Object value = parameters.get(key);
        if (!"xmid".equals(key) &&
            !"smid".equals(key) &&
            !NavigatorBean.OBJECTID_PARAMETER.equals(key))
        {
          DictionaryUtils.setProperty(document, key, value);
        }
      }
    }
    super.processParameters(parameters);
  }

  @Override
  public void loadObject() throws Exception
  {
    versions = null;
    formSelector = null;

    if (fileToStore != null)
    {
      fileNameToStore = null;
      fileToStore.delete();
      fileToStore = null;
    }

    if (!NEW_OBJECT_ID.equals(objectId))
    {
      try
      {
        document = getPort(false).loadDocument(
          objectId, 0, ContentInfo.METADATA);
      }
      catch (Exception ex) //deleted file?
      {
        loadVersions(objectId);
        if (versions != null && !versions.isEmpty())
        {
          Document lastDoc = versions.get(0);
          int lastVersion = lastDoc.getVersion();
          document = getPort(false).loadDocument(
            objectId, -lastVersion, ContentInfo.METADATA);
        }
      }
    }
    else
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
      String contentType = dh.getContentType();
      if ("application/octet-stream".equals(contentType))
        contentType = null;
      content.setContentType(contentType);
      document.setContent(content);
    }
    else if (!StringUtils.isBlank(urlToStore))
    {
      Content content = new Content();
      content.setUrl(urlToStore.trim());
      URLDataSource ds = new URLDataSource(new URL(urlToStore));
      content.setContentType(ds.getContentType());
      document.setContent(content);
      urlToStore = null;
    }
    else if (!StringUtils.isBlank(contentIdToStore))
    {
      Content content = new Content();
      content.setContentId(contentIdToStore.trim());
      document.setContent(content);
      contentIdToStore = null;
    }

    document = getPort(false).storeDocument(document);
    setObjectId(document.getDocId());

    documentFinderBean.outdate();

    if (fileToStore != null)
    {
      fileNameToStore = null;
      fileToStore.delete();
      fileToStore = null;
    }
    versions = null;
  }

  @Override
  public void removeObject() throws Exception
  {
    DocumentManagerPort port = getPort(false);
    if (document.getState().equals(State.DELETED))
    {
      // second remove
      port.removeDocument(document.getDocId(), DocumentConstants.PERSISTENT_DELETE);
    }
    else
    {
      // first remove
      port.removeDocument(document.getDocId(), DocumentConstants.DELETE_ALL_VERSIONS);
    }
    documentFinderBean.outdate();
  }

  public void lock()
  {
    try
    {
      DocumentManagerPort port = getPort(false);
      port.lockDocument(document.getDocId(), document.getVersion());
      document = port.loadDocument(document.getDocId(), document.getVersion(),
        ContentInfo.METADATA);
      info("DOCUMENT_LOCKED_BY_ANOTHER_USER",
        new Object[]{document.getLockUserId()});
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
      DocumentManagerPort port = getPort(false);
      port.unlockDocument(document.getDocId(), document.getVersion());
      document = port.loadDocument(document.getDocId(), document.getVersion(),
        ContentInfo.METADATA);
      info("DOCUMENT_UNLOCKED");
    }
    catch (Exception ex)
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
      int index = fileNameToStore.lastIndexOf(".");
      String extension = ".bin";
      if (index != -1)
      {
        extension = fileNameToStore.substring(index);
      }
      fileToStore = File.createTempFile("upload", extension);
      try (InputStream is = uploadedFile.getInputStream())
      {
        IOUtils.writeToFile(is, fileToStore);
      }
      uploadedFile.delete();
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

  public void loadVersions()
  {
    loadVersions(document.getDocId());
  }

  public List<Document> getVersions()
  {
    return versions;
  }

  public void purgeVersions()
  {
    try
    {
      getPort(false).removeDocument(objectId, DELETE_OLD_VERSIONS);
      versions = null;
      loadVersion(0);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void showVersions()
  {
    if (versions == null)
    {
      loadVersions();
    }
  }

  public boolean isVersionDeleted(Document document)
  {
    return document.getState().equals(State.DELETED);
  }

  public void newVersion()
  {
    document.setVersion(DocumentConstants.NEW_VERSION);
  }

  public void loadVersion(int version)
  {
    System.out.println(">>> loadVersion " + version);
    if (!NEW_OBJECT_ID.equals(objectId) && document.getVersion() != version)
    {
      try
      {
        document = getPort(false).loadDocument(
          objectId, version, ContentInfo.METADATA);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public void removeVersion(int version)
  {
    try
    {
      getPort(false).removeDocument(objectId, version);
      versions = null;
      loadVersion(0);
      growl("REMOVE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[] { document, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.document = (Document) array[0];
    this.formSelector = (String)array[1];
  }

  @Override
  public boolean isEditable()
  {
    if (UserSessionBean.getCurrentInstance().isUserInRole(
      DocumentConstants.DOC_ADMIN_ROLE))
      return true;

    if (!super.isEditable()) return false; //tab protection

    if (document == null || document.getDocId() == null ||
      document.getDocTypeId() == null)
      return true;

    if (isVersionDeleted(document)) return false;

    Type currentType =
      TypeCache.getInstance().getType(document.getDocTypeId());
    if (currentType == null) return true;

    Set<AccessControl> acls = new HashSet();
    acls.addAll(currentType.getAccessControl());
    acls.addAll(document.getAccessControl());
    for (AccessControl acl : acls)
    {
      String action = acl.getAction();
      if (DictionaryConstants.WRITE_ACTION.equals(action))
      {
        String roleId = acl.getRoleId();
        if (UserSessionBean.getCurrentInstance().isUserInRole(roleId))
          return true;
      }
    }
    return false;
  }

  private void loadVersions(String docId)
  {
    try
    {
      if (isNew())
      {
        versions = Collections.EMPTY_LIST;
      }
      else
      {
        DocumentFilter filter = new DocumentFilter();
        filter.getDocId().add(docId);
        filter.setVersion(-1);
        filter.setIncludeContentMetadata(false);
        filter.getStates().add(State.DRAFT);
        filter.getStates().add(State.COMPLETE);
        filter.getStates().add(State.RECORD);
        filter.getStates().add(State.DELETED);
        OrderByProperty order = new OrderByProperty();
        order.setName(DocumentConstants.VERSION);
        order.setDescending(true);
        filter.getOrderByProperty().add(order);
        versions = getPort(false).findDocuments(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
