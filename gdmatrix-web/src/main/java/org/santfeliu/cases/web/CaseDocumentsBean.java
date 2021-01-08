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
package org.santfeliu.cases.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;

import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.doc.web.DocumentUrlBuilder;
import org.santfeliu.faces.matrixclient.model.DocMatrixClientModels;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.TypifiedPageBean;


/**
 *
 * @author blanquepa
 */
public class CaseDocumentsBean extends TypifiedPageBean
{
  @CMSProperty
  public static final String REPLACE_BY_WHITESPACE_PATTERN_PROPERTY =
    "replaceByWhitespacePattern";
  public static final String ALL_TYPES_VISIBLE_PROPERTY = 
    "_documentsAllTypesVisible";  
  public static final String ROOT_TYPE_ID_PROPERTY = 
    "_documentRootTypeId";
  public static final String UPLOAD_TYPE_ID_PROPERTY = 
    
    "_documentsUploadTypeId";  
  public static final String GROUPBY_PROPERTY = 
    "_documentsGroupBy";
  public static final String GROUP_SELECTION_MODE_PROPERTY = 
    "_documentsGroupSelectionMode";
  public static final String ORDERBY_PROPERTY = 
    "_documentsOrderBy";
  public static final String SPREAD_ROLES_PROPERTY = 
    "_documentsSpreadRoles";
  public static final String ALLOWED_DOC_TYPES = 
    "_documentsAllowedTypes";
  public static final String DEFAULT_DOC_TYPE = 
    "_documentsDefaultType";

  private static final String FLAGS_PATH_URL = 
    "/common/translation/images/flags/";
  
  private CaseDocument editingDocument;

  private String command;
  private String docId;
  private String currentVolume = CaseConstants.UNDEFINED_VOLUME;
  private List<SelectItem> volumeSelectItems;
  private Map userDocTypes;
  private String defaultDocType;
   
  private DocMatrixClientModels models;

  public CaseDocumentsBean()
  {
    super(DictionaryConstants.CASE_DOCUMENT_TYPE, "CASE_ADMIN");

    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();
    
    //Get list of doc types that user is allowed to create documents.
    DocumentConfigBean configBean =
      (DocumentConfigBean)getBean("documentConfigBean");
    userDocTypes = configBean.getDocTypes();
    
    //Set bean properties from type.
    Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
    if (caseType != null)
    {
      String pdValue = 
        getPropertyDefinitionValue(caseType, ROOT_TYPE_ID_PROPERTY);
      if (pdValue != null)
        setRootTypeId(pdValue);

      pdValue = 
        getPropertyDefinitionValue(caseType, GROUPBY_PROPERTY);
      if (pdValue != null)
        this.groupBy = pdValue;

      pdValue = 
        getPropertyDefinitionValue(caseType, GROUP_SELECTION_MODE_PROPERTY);
      if (pdValue != null)
        this.groupSelectionMode = pdValue;

      pdValue =
        getPropertyDefinitionValue(caseType, ORDERBY_PROPERTY);
      if (pdValue != null)
      {
        String[] array = pdValue.split(",");
        if (array != null)
          orderBy = Arrays.asList(array);
      }
      if (orderBy == null)
        orderBy = Arrays.asList(new String[]{"document.creationDate"});
      
      pdValue =
        getPropertyDefinitionValue(caseType, ALL_TYPES_VISIBLE_PROPERTY);
      if (pdValue != null)
        setAllTypesVisible(Boolean.parseBoolean(pdValue));      
      
      //if only restricted types are allowed then filters userDocTypes.
      pdValue =
        getPropertyDefinitionValue(caseType, ALLOWED_DOC_TYPES);
      if (pdValue != null)
      {
        String[] array = pdValue.split(",");
        if (array != null)
        {
          Map allowedUserDocTypes = new HashMap();
          for (String item : array)
          {
            String userDocType = (String)userDocTypes.get(item);
            if (userDocType != null)
              allowedUserDocTypes.put(item, userDocType);
          }
          userDocTypes = allowedUserDocTypes;
        }        
      }
      
      //Mark user favorite types
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      if (userSessionBean.isMatrixClientEnabled())
      {
        List<String> docTypePreferences;
        try
        {
          docTypePreferences =
            userSessionBean.getUserPreferences().getPreferences("Type");
          for (String typePreference : docTypePreferences)
          {
            if (userDocTypes.containsKey(typePreference))
            {
              String key = "*" + typePreference;
              userDocTypes.put(key, userDocTypes.get(typePreference));
            }
          }
        }
        catch (Exception ex)
        {
        }      
      }        
      
      pdValue = 
        getPropertyDefinitionValue(caseType, DEFAULT_DOC_TYPE);
      if (pdValue != null)
        defaultDocType = pdValue;         
    }     
    
    //Set MatrixClientModels.
    String maxSize = 
      MatrixConfig.getProperty("org.santfeliu.doc.uploadMaxFileSize");
    if (maxSize != null)
      models = new DocMatrixClientModels(userDocTypes, maxSize);
    else
      models = new DocMatrixClientModels(userDocTypes); 
    
    if (defaultDocType != null)
      models.getSendModel().putParameter(DocumentConstants.DOCTYPEID, 
        defaultDocType);
    
    load();
  }
  
  //Accessors
  public void setCommand(String command)
  {
    this.command = command;
  }

  public String getCommand()
  {
    return command;
  }
  
  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getDocId()
  {
    return docId;
  }
  
  public DocMatrixClientModels getModels()
  {
    return models;
  }
  
  public void setModels(DocMatrixClientModels models) 
  {
    this.models = models;
  }
  
  public Map getUserDocTypes() 
  {
    return userDocTypes;
  }

  public void setUserDocTypes(Map userDocTypes)
  {
    this.userDocTypes = userDocTypes;
  }
  
  public String getCurrentVolume()
  {
    return currentVolume;
  }

  public void setCurrentVolume(String currentVolume)
  {
    this.currentVolume = currentVolume;
  }

  public List<SelectItem> getVolumeSelectItems()
  {
    return volumeSelectItems;
  }
  
  public void setVolumeSelectItems(List<SelectItem> volumeSelectItems)
  {
    this.volumeSelectItems = volumeSelectItems;
  }
  
  public List<String> selectVolumes(String query)
  {
    List<String> result = new ArrayList();
    for (SelectItem selectItem : volumeSelectItems)
    {
      String volume = selectItem.getLabel();
      if (volume.startsWith(query))
        result.add(volume);
    }
    return result;
  }
  
  public boolean isRenderVolumeSelector()
  {
    return volumeSelectItems != null && !volumeSelectItems.isEmpty() 
      && !(volumeSelectItems.size() == 1 
      && CaseConstants.UNDEFINED_VOLUME.equals(currentVolume));
  }

  public CaseDocument getEditingDocument()
  {
    return editingDocument;
  }

  public void setEditingDocument(CaseDocument editingDocument)
  {
    this.editingDocument = editingDocument;
  }
  
  //Object actions
  @Override
  public String show()
  {
    return "case_documents";
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        List<String> volumes = 
          CaseConfigBean.getPort().findCaseVolumes(getObjectId());
        Collections.replaceAll(volumes, null, CaseConstants.UNDEFINED_VOLUME);
        if (volumes != null && !volumes.isEmpty() 
          && !volumes.contains(currentVolume))
            currentVolume = volumes.get(0);
        createVolumeSelectItems(volumes);
        List<CaseDocumentView> rows = getResults();
        setGroups(rows, getGroupExtractor());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public String store()
  {
    if (editingDocument != null)
    {
      storeCaseDocument();
    }
    else
    {
      load();
    }
    return show();
  }
  
  public String reload()
  {
    resetFirstRowIndexes();
    load();
    return null;
  }
  
  public String refresh()
  {
    editingDocument = null;
    if (isSendFileCommand())
    {
      try
      {
        spreadDocumentRoles(docId);
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setCaseId(getObjectId());
        caseDocument.setDocId(docId);
        if (currentVolume != null && !"".equals(currentVolume) 
            && !CaseConstants.UNDEFINED_VOLUME.equals(currentVolume))
          caseDocument.setVolume(currentVolume);
        String caseDocTypeId = getUploadTypeId();
        if (caseDocTypeId == null) caseDocTypeId = getRootTypeId();
        caseDocument.setCaseDocTypeId(caseDocTypeId);
        CaseConfigBean.getPort().storeCaseDocument(caseDocument);
        setCommand(null);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    resetFirstRowIndexes();
    load();
    return null;
  }
  
  public String documentSent()
  {
    editingDocument = null;
    
    try
    {
      docId = (String)models.getSendModel().parseResult();
      if (docId != null)
      {
        spreadDocumentRoles(docId);
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setCaseId(getObjectId());
        caseDocument.setDocId(docId);
        if (currentVolume != null && !"".equals(currentVolume) 
            && !CaseConstants.UNDEFINED_VOLUME.equals(currentVolume))
          caseDocument.setVolume(currentVolume);
        String caseDocTypeId = getUploadTypeId();
        if (caseDocTypeId == null) caseDocTypeId = getRootTypeId();
        caseDocument.setCaseDocTypeId(caseDocTypeId);
        CaseConfigBean.getPort().storeCaseDocument(caseDocument);

        refreshData();
      }
    }
    catch (Exception ex)
    {
      if (!"NO_FILE".equals(ex.getMessage()))
        error(ex);
      ex.printStackTrace();
    }

    return null;
  }
  
  public String documentUpdated()
  {
    try
    {
      models.getUpdateModel().parseResult();
      refreshData();
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return null;
  }  
  
  public String documentEdited()
  {
    try
    {
      models.getEditModel().parseResult();
      refreshData();
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return null;
  }
  
  private void refreshData()
  {
    resetFirstRowIndexes();
    load();
  }
  
  //Page actions
  public String createCaseDocument()
  {
    editingDocument = new CaseDocument();
    return null;
  }

  public String editCaseDocument()
  {
    try
    {
      CaseDocumentView row = (CaseDocumentView)getExternalContext().
        getRequestMap().get("row");
      String caseDocId = row.getCaseDocId();
      if (caseDocId != null)
        editingDocument =
          CaseConfigBean.getPort().loadCaseDocument(caseDocId);
      else
        editingDocument = new CaseDocument();
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }  
  
  public String removeCaseDocument()
  {
    try
    {
      CaseDocumentView row = (CaseDocumentView)getExternalContext().
        getRequestMap().get("row");
      preRemove();
      CaseConfigBean.getPort().removeCaseDocument(row.getCaseDocId());
      postRemove();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String storeCaseDocument()
  {
    try
    {
      String[] docIdArray = 
        DocumentConfigBean.fromObjectId(editingDocument.getDocId());
      String documentId = docIdArray[0];
      if (documentId == null || documentId.isEmpty())
      {
        throw new Exception("DOCUMENT_MUST_BE_SELECTED");
      }
      spreadDocumentRoles(documentId);
      CaseDocument caseDocument = new CaseDocument();
      caseDocument.setCaseDocId(editingDocument.getCaseDocId());
      caseDocument.setCaseId(getObjectId());
      String caseDocTypeId = editingDocument.getCaseDocTypeId();
      if (caseDocTypeId == null)
        caseDocTypeId = getRootTypeId();
      caseDocument.setCaseDocTypeId(caseDocTypeId);
      caseDocument.setComments(editingDocument.getComments());
      caseDocument.setVolume(editingDocument.getVolume());
      caseDocument.setDocId(documentId);
      CaseConfigBean.getPort().storeCaseDocument(caseDocument);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      editingDocument = null;
      load();
    }
    return null;
  }

  public String cancelCaseDocument()
  {
    editingDocument = null;
    return null;
  }

  public String showDocument()
  {
    CaseDocumentView row = (CaseDocumentView)getValue("#{row}");
    return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
      DocumentConfigBean.toObjectId(
        row.getDocument().getDocId(), row.getDocument().getVersion()));
  }

  public String createDocument()
  {
    return getControllerBean().createObject(DictionaryConstants.DOCUMENT_TYPE,
      "#{caseDocumentsBean.editingDocument.docId}");
  }

  public String searchDocument()
  {
    return getControllerBean().searchObject(DictionaryConstants.DOCUMENT_TYPE,
      "#{caseDocumentsBean.editingDocument.docId}");
  }
  
  @Override
  public boolean isModified()
  {
    return editingDocument != null;
  }  

  //Auxiliar getters
  public List<SelectItem> getDocumentSelectItems()
  {
    DocumentBean documentBean = (DocumentBean)getBean("document2Bean");
    return documentBean.getSelectItems(editingDocument.getDocId());
  }

  public String getMimeType() 
  {
    CaseDocumentView row = (CaseDocumentView)getExternalContext().
      getRequestMap().get("row");
    Content content =
      (row.getDocument() != null ? row.getDocument().getContent() : null);
    if (content != null)
      return DocumentBean.getContentTypeIcon(content.getContentType());
    else
      return DocumentBean.getContentTypeIcon(null);
  }  
  
  public String getLanguageFlag()
  { 
    CaseDocumentView row = (CaseDocumentView)getExternalContext().
      getRequestMap().get("row");

    String language = null;
    if (row != null)
    {
      language = row.getCaseDocId() != null && row.getDocument() != null
        ? row.getDocument().getLanguage() : "";
    }

    return DocumentUtils.getLanguageFlag(FLAGS_PATH_URL, language);
  }
  
  public String getLanguage()
  {
    CaseDocumentView row = (CaseDocumentView)getExternalContext().
      getRequestMap().get("row");

    String language = null;
    if (row != null)
    {
      language = row.getCaseDocId() != null && row.getDocument() != null
        ? row.getDocument().getLanguage() : "";
    }

    return DocumentUtils.extendLanguage(language);
  }

  public String getDocumentTitle()
  {
    String title = "";
    CaseDocumentView caseDocument = (CaseDocumentView)getValue("#{row}");
    if (caseDocument != null)
    {
      Document document = caseDocument.getDocument();
      if (document != null)
      {
        title = document.getTitle();
        String pattern = getProperty(REPLACE_BY_WHITESPACE_PATTERN_PROPERTY);
        if (pattern != null && title != null)
        {
          title = title.replaceAll(pattern, " ");
        }
      }
    }
    return title;
  }

  public String getDocumentUrl()
  { 
    CaseDocumentView row = (CaseDocumentView)getValue("#{row}");
    if (row != null)
    {
      Document document = row.getDocument();
      return DocumentUrlBuilder.getDocumentUrl(document);
    }
    return "";
  }

  public String getDocumentCreationDate()
  {
    CaseDocumentView row = (CaseDocumentView)getValue("#{row}");

    Document document = row.getDocument();
    if (document != null)
    {
      String creationDate = row.getDocument().getCreationDate();
      if (creationDate != null)
        return TextUtils.formatDate(
          TextUtils.parseInternalDate(creationDate), "dd/MM/yyyy");
    }

    return null;
  }

  public Date getCreationDateTime()
  {
    if (editingDocument != null 
      && editingDocument.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(editingDocument.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (editingDocument != null && editingDocument.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(editingDocument.getChangeDateTime());
    else
      return null;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
  
  @Override
  protected String getRowTypeId(Object row)
  {
    CaseDocumentView cdRow = (CaseDocumentView)row;
    return cdRow.getCaseDocTypeId();
  }
  
  //Private methods
  private int countResults()
  {
    try
    {
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(getObjectId());
      filter.setVolume(currentVolume);
      if (groupBy != null && groupBy.startsWith("document.property["))
      {
        String property;
        property = groupBy.replaceAll("document.property[", "");
        property = property.replaceAll("]", "");
        String[] outProps = property.split(",");
        filter.getOutputProperty().addAll(Arrays.asList(outProps));
      }
      int count = CaseConfigBean.getPort().countCaseDocuments(filter);
      return count;
    }
    catch (Exception ex)
    {
      return 0;
    }
  }

  private List getResults()
  {
    try
    {
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(getObjectId());
      filter.setVolume(currentVolume);
      if (groupBy != null && groupBy.startsWith("document.property["))
      {
        String property = groupBy.replaceAll("document\\.property\\[", "");
        property = property.replaceAll("\\]", "");
        String[] outProps = property.split(",");
        filter.getOutputProperty().addAll(Arrays.asList(outProps));
      }
      List<CaseDocumentView> rows =
        CaseConfigBean.getPort().findCaseDocumentViews(filter);
      return rows;
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  private void createVolumeSelectItems(List<String> volumes)
  {
    volumeSelectItems = new ArrayList<SelectItem>();
    for (String volume : volumes)
    {
      SelectItem item = new SelectItem("", "");
      if (volume != null)
        item = new SelectItem(volume, 
          (volume.equals(CaseConstants.UNDEFINED_VOLUME) ? "" : volume));
      volumeSelectItems.add(item);
    }
    Collections.sort(volumeSelectItems, new Comparator() {
      @Override
      public int compare(Object o1, Object o2)
      {
        SelectItem item1 = (SelectItem)o1;
        SelectItem item2 = (SelectItem)o2;
        return item1.getLabel().compareTo(item2.getLabel());
      }
    });
  }

  private void spreadDocumentRoles(String docId) throws Exception
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    String caseTypeId = caseMainBean.getCase().getCaseTypeId();
    if (caseTypeId != null)
    {
      Type caseType = TypeCache.getInstance().getType(caseTypeId);
      String spreadRoles = getSpreadRoles(caseType);
      if (spreadRoles != null)
      {
        Document document = 
          DocumentConfigBean.getClient().loadDocument(docId, 0, ContentInfo.ID);
        if (document != null)
        {
          boolean update = false;
          if ("true".equalsIgnoreCase(spreadRoles)) //Spread Case roles
          {
            List<AccessControl> accessControlList = new ArrayList();
            accessControlList.addAll(caseType.getAccessControl());
            accessControlList.addAll(caseMainBean.getCase().getAccessControl());            
            for (AccessControl ac : accessControlList)
            {
              if (!containsAC(document.getAccessControl(), ac))
              {
                update = true;
                document.getAccessControl().add(ac);
              }
            }
          }
          else //Spread role defined in SPREAD_ROLES_PROPERTY value
          {
            String[] actions = {DictionaryConstants.READ_ACTION, 
              DictionaryConstants.WRITE_ACTION, 
              DictionaryConstants.DELETE_ACTION};
            for (String action : actions)
            {
              AccessControl ac = new AccessControl();
              ac.setAction(action);
              ac.setRoleId(spreadRoles);
              if (!containsAC(document.getAccessControl(), ac))
              {
                document.getAccessControl().add(ac);
                update = true;
              }
            }
          }
          if (update)
          {
            try
            {
              DocumentConfigBean.getClient().storeDocument(document);
              info("DOCUMENT_SECURITY_UPDATED");
            }
            catch (Exception ex) 
            {
            }
          }
          else
          {
            warn("DOCUMENT_SECURITY_NOT_UPDATED");
          }
        }
      }
    }
  }

  private String getSpreadRoles(Type caseType)
  {
    if (caseType != null)
    {
      PropertyDefinition pd =
        caseType.getPropertyDefinition(SPREAD_ROLES_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
      {
        String value = pd.getValue().get(0);
        if ("false".equals(value))
          return null;
        else 
          return value;
      }
    }
    return null;
  }

  private String getUploadTypeId()
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    String caseTypeId = caseMainBean.getCase().getCaseTypeId();
    if (caseTypeId != null)
    {
      Type caseType = TypeCache.getInstance().getType(caseTypeId);    
      if (caseType != null)
      {
        PropertyDefinition pd =
          caseType.getPropertyDefinition(UPLOAD_TYPE_ID_PROPERTY);
        if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
          return (pd.getValue().get(0));
      }
    }
    return null;
  }
  
  private boolean containsAC(List<AccessControl> acl, AccessControl ac)
  {
    for (AccessControl item : acl)
    {
      if (ac.getAction().equals(item.getAction()) &&
          ac.getRoleId().equals(item.getRoleId()))
        return true;
    }
   return false;
  }
  
  private boolean isSendFileCommand()
  {
    boolean mc = UserSessionBean.getCurrentInstance().isMatrixClientEnabled();
    return (!mc && "sendFile".equals(command));
  }
  
 
}
