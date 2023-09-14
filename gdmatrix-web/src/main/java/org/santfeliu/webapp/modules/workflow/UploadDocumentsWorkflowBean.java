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
package org.santfeliu.webapp.modules.workflow;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.primefaces.event.FileUploadEvent;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.util.Table;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import org.santfeliu.webapp.modules.doc.DocumentTypeBean;
import org.santfeliu.workflow.form.Form;


/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class UploadDocumentsWorkflowBean extends WorkflowBean implements Serializable
{
  //parameters
  private String message;
  private String reference = "doc";

  private List<DocumentView> documents;
  private Map documentProperties;

  public static final String INSTANCE_ID = "workflow.instanceId";
  public static final String DOCREFERENCE = "workflow.documentReference";
  public static final String DOCDESC = "description";
  public static final String UUID = "uuid";
  private static String DOCUMENT_SERVLET_PATH = "/documents/"; //TODO

  String title;
  String docTypeId = "Document";
  String language;
  File fileToStore;

  @Inject
  WorkflowInstanceBean instanceBean;

  public UploadDocumentsWorkflowBean()
  {
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public List<DocumentView> getDocuments()
  {
    return documents;
  }

  public Map getDocumentProperties()
  {
    if (documentProperties == null) documentProperties = new HashMap();

    documentProperties.put(INSTANCE_ID, instanceBean.getInstanceId());
    documentProperties.put(DOCREFERENCE,
                           instanceBean.getInstanceId() + ":" + reference);

    return documentProperties;
  }

  public File getFileToStore()
  {
    return fileToStore;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDocTypeId()
  {
    return docTypeId;
  }

  public void setDocTypeId(String docTypeId)
  {
    this.docTypeId = docTypeId;
  }

  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  public void handleFileUpload(FileUploadEvent event)
  {
    try
    {
      org.primefaces.model.file.UploadedFile uploadedFile = event.getFile();
      title = uploadedFile.getFileName();
      fileToStore = File.createTempFile("upload", ".bin");
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

  public String getDocumentURL()
  {
    DocumentView documentView = (DocumentView) getValue("#{document}");
    String uuid = (String) documentView.getUuid();
    String description = (String)documentView.getTitle();
    return DOCUMENT_SERVLET_PATH + uuid + "/" + description;
  }

  // Actions
  @Override
  public String show(Form form)
  {
    try
    {
      if (documentProperties != null) documentProperties.clear();
      Properties parameters = form.getParameters();
      Object value;
      value = parameters.get("message");
      if (value != null) message = String.valueOf(value);
      value = parameters.get("reference");
      if (value != null) reference = String.valueOf(value);
      else reference = "";

      value = parameters.get("maxFileSize");
      value = parameters.get("validExtensions");
      if (parameters.containsKey("title"))
      {
        value = parameters.get("title");
        title = String.valueOf(value);
      }
      if (parameters.containsKey("docTypeId"))
      {
        value = parameters.get("docTypeId");
        docTypeId = String.valueOf(value);
      }
      if (parameters.containsKey("language"))
      {
        value = parameters.get("language");
        language = String.valueOf(value);
      }

      loadWorkflowDocumentProperties(parameters);

      loadDocuments();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "/pages/workflow/upload_documents_form.xhtml";
  }

  public void refreshDocumentsTable()
  {
    try
    {
      loadDocuments();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void deleteDocument()
  {
    try
    {
      DocumentView documentView = (DocumentView)getRequestMap().get("document");
      deleteFromDocumentManager(documentView.getDocId());
      loadDocuments();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Map submit()
  {
    try
    {
      return getVariablesFromTable();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public void storeFile()
  {
    try
    {
      Document document = new Document();
      document.setTitle(title);
      document.setDocTypeId(docTypeId);
      document.setLanguage(language);

      Property property = new Property();
      property.setName(DOCREFERENCE);
      property.getValue().add(getInstanceId() + ":" + reference);
      document.getProperty().add(property);
      DocumentUtils.setProperties(document, documentProperties);

      Content content = new Content();
      content.setData(new DataHandler(new FileDataSource(fileToStore)));
      document.setContent(content);
      DocModuleBean.getPort(false).storeDocument(document);

      loadDocuments();

      fileToStore.delete();
      fileToStore = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancelFile()
  {
    fileToStore.delete();
    fileToStore = null;
  }

  // private methods
  private void loadWorkflowDocumentProperties(Map parameters)
  {
    if (documentProperties == null)
      documentProperties = new HashMap();
    documentProperties.putAll(parameters);
    documentProperties.remove("message");
    documentProperties.remove("reference");
    documentProperties.remove("maxFileSize");
    documentProperties.remove("validExtensions");
  }

  private Map getVariablesFromTable() throws Exception
  {
    Map variables = new HashMap();

    int docCount = documents.size();
    for (int i = 0; i < docCount; i++)
    {
      DocumentView documentView = documents.get(i);
      // load variables
      variables.put(reference + "docid_" + i, documentView.getDocId());
      variables.put(reference + "uuid_" + i, documentView.getUuid());
      variables.put(reference + "desc_" + i, documentView.getTitle());
    }
    variables.put(reference + "count", docCount);

    // remove old document variables
    int oldDoccount = 0;
    Object num = instanceBean.getVariables().get(reference + "count");
    if (num instanceof Number) oldDoccount = ((Number) num).intValue();
    for (int i = docCount; i < oldDoccount; i++)
    {
      variables.put(reference + "docid_" + i, null);
      variables.put(reference + "uuid_" + i, null);
      variables.put(reference + "desc_" + i, null);
    }
    return variables;
  }

  private void loadDocuments() throws Exception
  {
    if (documents == null)
    {
      documents = new ArrayList<>();
    }
    else
    {
      documents.clear();
    }
    List<Document> docManagerDocs = getInstanceRelatedDocuments();
    if (docManagerDocs != null)
    {
      for (int i = 0; i < docManagerDocs.size(); i++)
      {
        Document doc = docManagerDocs.get(i);
        DocumentView documentView = new DocumentView();
        documentView.setDocId(doc.getDocId());
        documentView.setTitle(doc.getTitle());
        documentView.setUuid(doc.getContent().getContentId());
        long size = doc.getContent().getSize();
        documentView.setSize(DocumentUtils.getSizeString(size));
        String mimeType = doc.getContent().getContentType();
        documentView.setIcon(DocumentTypeBean.getContentIcon(mimeType));
        documentView.setLanguage(doc.getLanguage());
        documents.add(documentView);
      }
    }
  }

  private List<Document> getInstanceRelatedDocuments()
    throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient();
    DocumentFilter filter = new DocumentFilter();
    Property property = new Property();
    property.setName(DOCREFERENCE);
    property.getValue().add(getInstanceId() + ":" + reference);
    filter.getProperty().add(property);
    filter.setVersion(0);
    filter.setIncludeContentMetadata(true);
    List<Document> documentList = client.findDocuments(filter);
    return documentList;
  }

  private void deleteFromDocumentManager(String docId)
    throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient();
    client.removeDocument(docId, -2);
  }

  private String getInstanceId()
  {
    return String.valueOf(instanceBean.getInstanceId());
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    String userId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String password =
      MatrixConfig.getProperty("adminCredentials.password");

    return new DocumentManagerClient(userId, password);
  }

  private String getLocalizedMessage(String key)
  {
    String value = null;
    try
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.workflow.web.resources.WorkflowBundle", getLocale());
      value = bundle.getString(key);
    }
    catch (Exception ex)
    {
      value = key;
    }
    return value;
  }

  public class DocumentView implements Serializable
  {
    String docId;
    String uuid;
    String title;
    String size;
    String icon;
    String language;

    public String getUuid()
    {
      return uuid;
    }

    public void setUuid(String uuid)
    {
      this.uuid = uuid;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getSize()
    {
      return size;
    }

    public void setSize(String size)
    {
      this.size = size;
    }

    public String getIcon()
    {
      return icon;
    }

    public void setIcon(String icon)
    {
      this.icon = icon;
    }

    public String getDocId()
    {
      return docId;
    }

    public void setDocId(String docId)
    {
      this.docId = docId;
    }

    public String getLanguage()
    {
      return language;
    }

    public void setLanguage(String language)
    {
      this.language = language;
    }
  }
}
