package org.santfeliu.workflow.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.component.html.HtmlInputText;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.doc.web.UploadFileManager;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.util.Table;
import org.santfeliu.workflow.form.Form;


public class UploadDocumentsFormBean extends FormBean implements Serializable
{
  //parameters
  private String message;
  private String reference = "doc";

  private Table documentsTable;
  private Map documentProperties;

  public static final String INSTANCE_ID = "workflow.instanceId";
  public static final String DOCREFERENCE = "workflow.documentReference";
  public static final String DOCDESC = "description";
  public static final String UUID = "uuid";
  private String DOCUMENT_SERVLET_PATH = "/documents/"; //TODO

  private transient HtmlInputText descriptionComponent;

  private UploadFileManager uploadFileManager = new UploadFileManager();

  public UploadDocumentsFormBean()
  {
  }

  //Accessors
  public void setDescriptionComponent(HtmlInputText descriptionComponent)
  {
    this.descriptionComponent = descriptionComponent;
  }

  public HtmlInputText getDescriptionComponent()
  {
    return descriptionComponent;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public void setDocumentsTable(Table documentsTable)
  {
    this.documentsTable = documentsTable;
  }

  public Table getDocumentsTable()
  {
    return documentsTable;
  }

  public void setDocumentProperties(Map documentProperties)
  {
    this.documentProperties = documentProperties;
  }

  public Map getDocumentProperties()
  {
    if (documentProperties == null) documentProperties = new HashMap();

    InstanceBean instanceBean = (InstanceBean) getBean("instanceBean");
    documentProperties.put(INSTANCE_ID, instanceBean.getInstanceId());
    documentProperties.put(DOCREFERENCE, 
                           instanceBean.getInstanceId() + ":" + reference);

    return documentProperties;
  }

  public UploadFileManager getUploadFileManager()
  {
    return uploadFileManager;
  }

  public void setUploadFileManager(UploadFileManager uploadFileManager)
  {
    this.uploadFileManager = uploadFileManager;
  }

  public UploadedFile getUploadedFile()
  {
    return uploadFileManager.getUploadedFile();
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    try
    {
      this.uploadFileManager.setUploadedFile(uploadedFile);
    }
    catch (IOException ex)
    {
      error(ex);
    }
  }

  public String getDocumentURL()
  {
    Map document = (Map) getValue("#{document}");
    String uuid = (String) document.get("uuid");
    String description = (String) document.get("description");
    return DOCUMENT_SERVLET_PATH + uuid + "/" + description;
  }

  // Actions
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
      if (value != null)
        uploadFileManager.setMaxFileSize(String.valueOf(value));
      value = parameters.get("validExtensions");
      if (value != null)
        uploadFileManager.setValidExtensions(String.valueOf(value));

      if (parameters.containsKey("title"))
      {
        value = parameters.get("title");
        uploadFileManager.setDocLanguage(String.valueOf(value));
        uploadFileManager.setRenderTitle(false);
      }
      if (parameters.containsKey("docTypeId"))
      {
        value = parameters.get("docTypeId");
        uploadFileManager.setDocTypeId(String.valueOf(value));
      }
      if (parameters.containsKey("language"))
      {
        value = parameters.get("language");
        uploadFileManager.setDocLanguage(String.valueOf(value));
        uploadFileManager.setRenderDocLanguage(false);
      }

      loadWorkflowDocumentProperties(parameters);

      documentsTable = new Table(new String[]
        { "uuid", "description", "size", "mimetype", "docid", "language" });

      loadDocumentsTable();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "upload_documents_form";
  }

  public String refreshDocumentsTable()
  {
    try
    {
      loadDocumentsTable();
    }
    catch (Exception e)
    {
      error(e.getMessage());
    }
    return "upload_documents";
  }

  public String deleteDocument()
  {
    try
    {
      Table.Row documentRow = (Table.Row) getRequestMap().get("document");
      String docId = (String) documentRow.get("docid");
      deleteFromDocumentManager(docId);
      
      loadDocumentsTable();
    }
    catch (Exception e)
    {
      this.error(e.getMessage());
    }
    return "upload_documents";
  }

  public Map submit()
  {
    try
    {
      return getVariablesFromTable();
    }
    catch (Exception e)
    {
      error(e.getMessage());
    }
    return null;
  }

  public void uploadFile(javax.faces.event.ValueChangeEvent ev)
  {
    try
    {
      if (ev.getNewValue() != null)
      {
        UploadedFile uploadedFile = (UploadedFile)ev.getNewValue();

        if (!uploadFileManager.validFileSize(uploadedFile.getSize()))
        {
          error("INVALID_INPUT_DATA", getLocalizedMessage("INVALID_FILE_SIZE") +
            ". " + getLocalizedMessage("maxFileSize") +
            " (" + DocumentUtils.getSizeString(uploadFileManager.getMaxFileSize()) + ")");
          return;
        }
        if (!uploadFileManager.validFileExtension(uploadedFile.getName()))
        {
          error("INVALID_INPUT_DATA", getLocalizedMessage("INVALID_FILE_EXTENSION") +
            ". " + getLocalizedMessage("validExtensions") + 
            " " + uploadFileManager.getValidExtensions());
          return;
        } 
        uploadFileManager.uploadFile(uploadedFile);
        
        if (!uploadFileManager.isRenderTitle()
         && !uploadFileManager.isRenderDocLanguage())
        {
          storeFile();
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String storeFile()
  {
    try
    {
      uploadFileManager.storeFile(getDocumentProperties());
      refreshDocumentsTable();
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }

    return null;
  }

  public String cancelFile()
  {
    uploadFileManager.cancelUpload();

    return null;
  }

  public boolean isRenderDocumentForm()
  {
    return !uploadFileManager.isEmptyFile();
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

    int doccount = documentsTable.getRowCount();
    if (doccount > 0) doccount--;
    for (int i = 0; i < doccount; i++)
    {
      // load variables
      String docid = (String) documentsTable.getElementAt(i, "docid");
      String uuid = (String) documentsTable.getElementAt(i, "uuid");
      String desc = (String) documentsTable.getElementAt(i, "description");
      variables.put(reference + "docid_" + i, docid);
      variables.put(reference + "uuid_" + i, uuid);
      variables.put(reference + "desc_" + i, desc);
    }
    variables.put(reference + "count", new Integer(doccount));

    // remove old document variables
    InstanceBean instanceBean = (InstanceBean) getBean("instanceBean");
    int oldDoccount = 0;
    Object num = instanceBean.getVariables().get(reference + "count");
    if (num instanceof Number) oldDoccount = ((Number) num).intValue();
    for (int i = doccount; i < oldDoccount; i++)
    {
      variables.put(reference + "docid_" + i, null);
      variables.put(reference + "uuid_" + i, null);
      variables.put(reference + "desc_" + i, null);
    }
    return variables;
  }

  private void loadDocumentsTable() throws Exception
  {
    documentsTable.clear();
    List<Document> docManagerDocs = getInstanceRelatedDocuments();
    if (docManagerDocs != null)
    {
      for (int i = 0; i < docManagerDocs.size(); i++)
      {
        Document doc = docManagerDocs.get(i);
        String uuid = doc.getContent().getContentId();
        long lSize = doc.getContent().getSize();
        String size = DocumentUtils.getSizeString(lSize);
        String mimeType = doc.getContent().getContentType();
        String mimeTypeIcon = DocumentBean.getContentTypeIcon(mimeType);
        String docId = doc.getDocId();
        String language = doc.getLanguage();
        String desc = doc.getTitle();
        documentsTable.addRow(uuid, desc, size, mimeTypeIcon, docId, language);
      }
    }
    documentsTable.addRow(null, "", "", "", "", "");
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
    InstanceBean instanceBean = (InstanceBean) getBean("instanceBean");
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
}
