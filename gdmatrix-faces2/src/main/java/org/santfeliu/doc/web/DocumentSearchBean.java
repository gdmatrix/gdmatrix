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
package org.santfeliu.doc.web;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.model.SelectItem;

import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.santfeliu.classif.web.ClassBean;
import org.santfeliu.classif.web.ClassSearchBean;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.faces.convert.DateTimeConverter;
import org.santfeliu.faces.convert.FileSizeConverter;
import org.santfeliu.faces.convert.TypeIdConverter;
import org.santfeliu.faces.matrixclient.model.DefaultMatrixClientModel;
import org.santfeliu.faces.matrixclient.model.DocMatrixClientModels;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.DynamicTypifiedSearchBean;
import org.santfeliu.web.obj.util.ColumnDefinition;
import org.santfeliu.web.obj.util.SetObjectManager;
import org.santfeliu.web.obj.util.CKEditorManager;
import org.santfeliu.web.obj.util.ParametersManager;
import org.santfeliu.web.obj.util.RequestParameters;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class DocumentSearchBean extends DynamicTypifiedSearchBean
{
  //Node configuration properties
  @CMSProperty
  public static final String FILTERPROPERTYNAME_PROPERTY =
    "filterProperty.name";
  @CMSProperty
  public static final String FILTERPROPERTYVALUE_PROPERTY =
    "filterProperty.value";
  @CMSProperty
  public static final String RENDER_PICKUP_BUTTON = "renderPickUpButton";
  @CMSProperty
  public static final String FILTERPANEL_PROPERTY = "filterPanel.render";
  @CMSProperty
  public static final String RENDERDATA_PROPERTY = "filterPanel.data.render";
  @CMSProperty
  public static final String RENDERLANGUAGE_PROPERTY =
    "filterPanel.language.render";
  @CMSProperty
  public static final String LANGUAGE_VALUES =
    "filterPanel.language.values";  
  @CMSProperty
  public static final String RENDERSTATE_PROPERTY = "filterPanel.state.render";
  @CMSProperty
  public static final String RENDERTITLE_PROPERTY = "filterPanel.title.render";
  @CMSProperty
  public static final String RENDERDOCID_PROPERTY = "filterPanel.docId.render";
  @CMSProperty
  public static final String RENDERVERSION_PROPERTY =
    "filterPanel.version.render";
  @CMSProperty
  public static final String RENDERCONTENTID_PROPERTY =
    "filterPanel.contentId.render";
  @CMSProperty
  public static final String RENDERTYPE_PROPERTY = "filterPanel.type.render";
  @CMSProperty
  public static final String DEFAULTTYPE_PROPERTY = "filterPanel.type.default";
  @CMSProperty
  public static final String RENDERDATE_PROPERTY = "filterPanel.date.render";
  @CMSProperty
  public static final String RENDERCLASS_PROPERTY = "filterPanel.class.render";
  @CMSProperty
  public static final String RENDERDESCCOLUMN_PROPERTY =
    "body.descColumn.render";
  @CMSProperty
  public static final String RENDERDATECOLUMN_PROPERTY =
    "body.dateColumn.render";
  @CMSProperty
  public static final String RENDERURLCOLUMN_PROPERTY = "body.urlColumn.render";
  @CMSProperty
  public static final String RENDERSIZECOLUMN_PROPERTY =
    "body.sizeColumn.render";
  @CMSProperty
  public static final String RENDERLANGCOLUMN_PROPERTY =
    "body.langColumn.render";
  @CMSProperty
  public static final String RENDERDOCIDCOLUMN_PROPERTY =
    "body.docIdColumn.render";
  @CMSProperty
  public static final String RENDERVERSIONCOLUMN_PROPERTY =
    "body.versionColumn.render";
  @CMSProperty
  public static final String RENDERDOCTYPECOLUMN_PROPERTY =
    "body.docTypeColumn.render";
  @CMSProperty
  public static final String RENDEREXTENSION_PROPERTY = 
    "body.extension.render";
  
  public static final String RENDERDRAFTSTATE_PROPERTY =
    "filterPanel.draftState.render";
  @CMSProperty
  public static final String RENDERCOMPLETESTATE_PROPERTY =
    "filterPanel.completeState.render";
  @CMSProperty
  public static final String RENDERRECORDSTATE_PROPERTY =
    "filterPanel.recordState.render";
  @CMSProperty
  public static final String RENDERDELETEDSTATE_PROPERTY =
    "filterPanel.deletedState.render";
  @CMSProperty
  public static final String RENDER_OUTPUT_SEARCH_EXPR_PROPERTY =
    "filterPanel.outputSearchExpression.render";
  @CMSProperty
  public static final String RENDER_DYNAMIC_FORM_PROPERTY = "renderDynamicForm";
  @CMSProperty
  public static final String RENDER_PROPERTY_VALUE_FILTER = 
    "renderPropertyValueFilter";
  @CMSProperty
  public static final String RENDER_CLEAR_BUTTON = "renderClearButton";

  @CMSProperty
  public static final String CHECKDRAFTSTATE_PROPERTY =
    "filterPanel.draftState.check";
  @CMSProperty
  public static final String CHECKCOMPLETESTATE_PROPERTY =
    "filterPanel.completeState.check";
  @CMSProperty
  public static final String CHECKRECORDSTATE_PROPERTY =
    "filterPanel.recordState.check";
  @CMSProperty
  public static final String CHECKDELETEDSTATE_PROPERTY =
    "filterPanel.deletedState.check";
  @CMSProperty
  public static final String ORDERBY_PROPERTY =
    "orderBy";

  @CMSProperty @Deprecated
  public static final String HEADER_DOCUMENT_PROPERTY = "header.document";
  @CMSProperty @Deprecated
  public static final String FOOTER_DOCUMENT_PROPERTY = "footer.document";
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String HEADER_RENDER_PROPERTY = "header.render";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String FOOTER_RENDER_PROPERTY = "footer.render";

  @CMSProperty
  public static final String SEARCH_HELP_DOCUMENT_URL_PROPERTY =
    "searchHelpDocumentURL";
  @CMSProperty
  public static final String UPDATEROLE_PROPERTY = "roles.update";
  @CMSProperty
  public static final String FIRSTLOAD_PROPERTY =
    "resultList.showOnFirstRequest";
  @CMSProperty
  public static final String SEARCH_EXPRESSION_PROPERTY =
    "searchExpression";
  @CMSProperty
  public static final String REPLACE_BY_WHITESPACE_PATTERN_PROPERTY =
    "replaceByWhitespacePattern";

  @Deprecated
  //Use PAGE_SIZE instead
  public static final String ROWSPERPAGE_PROPERTY = "resultList.rowsPerPage";
  @CMSProperty
  public static final String DISABLE_ROLES_PROPERTY = "disableRoles";

  private static final String FLAGS_PATH_URL =
    "/common/translation/images/flags/";
  private static final String DOC_SERVLET_URL = "/documents/";

  private String headerBrowserUrl;
  private String footerBrowserUrl;
  
  private final String docServletURL;

  private DocumentFormFilter filter;
  private Document selectedDocument;

  private transient List<SelectItem> typeSelectItems;
  private transient List<SelectItem> classSelectItems;

  private SetObjectManager setObjectManager;
  private CKEditorManager ckEditorManager;
  
  private DocMatrixClientModels models;

  public DocumentSearchBean()
  {
    super("org.santfeliu.doc.web.resources.DocumentBundle", "documentSearch_", 
      "docTypeId");

    filter = new DocumentFormFilter();
    setObjectManager = new SetObjectManager(filter);
    
    ckEditorManager = new CKEditorManager();  

    docServletURL = getContextURL() + DOC_SERVLET_URL;
    DocumentConfigBean configBean =
      (DocumentConfigBean)getBean("documentConfigBean");
    models = new DocMatrixClientModels(configBean.getDocTypes());  
  }
  
  public String getDocId()
  {
    return filter.getDocId();
  }

  public void setDocId(String docIdInput)
  {
    filter.setDocId(docIdInput);
  }

  public String getVersion()
  {
    return filter.getVersion();
  }

  public void setVersion(String versionInput)
  {
    this.filter.setVersion(versionInput);
  }

  public void setFilter(DocumentFilter filter)
  {
    this.filter.setDocumentFilter(filter);
  }

  public DocumentFilter getFilter()
  {
    return filter.getDocumentFilter();
  }

  public String getTitleFilter()
  {
    return filter.getTitle();
  }

  public void setTitleFilter(String titleFilter)
  {
    this.filter.setTitle(titleFilter);
  }

  public String getClassId()
  {
    return filter.getClassId();
  }

  public void setClassId(String objectId)
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    this.filter.setClassId(classBean.getClassId(objectId));
  }

  public String getPropertyName1()
  {
    return filter.getPropertyName1();
  }

  public void setPropertyName1(String propertyNameFilter1)
  {
    this.filter.setPropertyName1(propertyNameFilter1);
  }

  public String getPropertyName2()
  {
    return filter.getPropertyName2();
  }

  public void setPropertyName2(String propertyNameFilter2)
  {
    this.filter.setPropertyName2(propertyNameFilter2);
  }

  public String getPropertyValue1()
  {
    return filter.getPropertyValue1();
  }

  public void setPropertyValue1(String propertyValueFilter1)
  {
    this.filter.setPropertyValue1(propertyValueFilter1);
  }

  public String getPropertyValue2()
  {
    return filter.getPropertyValue2();
  }

  public void setPropertyValue2(String propertyValueFilter2)
  {
    this.filter.setPropertyValue2(propertyValueFilter2);
  }

  public boolean isIncludeCompleteCBValue()
  {
    return filter.isIncludeCompleteCBValue();
  }

  public void setIncludeCompleteCBValue(boolean includeCompleteCBValue)
  {
    this.filter.setIncludeCompleteCBValue(includeCompleteCBValue);
  }

  public boolean isIncludeDeletedCBValue()
  {
    return filter.isIncludeDeletedCBValue();
  }

  public void setIncludeDeletedCBValue(boolean includeDeletedCBValue)
  {
    this.filter.setIncludeDeletedCBValue(includeDeletedCBValue);
  }

  public boolean isIncludeDraftCBValue()
  {
    return filter.isIncludeDraftCBValue();
  }

  public void setIncludeDraftCBValue(boolean includeDraftCBValue)
  {
    this.filter.setIncludeDraftCBValue(includeDraftCBValue);
  }

  public boolean isIncludeRecordCBValue()
  {
    return filter.isIncludeRecordCBValue();
  }

  public void setIncludeRecordCBValue(boolean includeRecordCBValue)
  {
    this.filter.setIncludeRecordCBValue(includeRecordCBValue);
  }

  public String getHeaderBrowserUrl()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(HEADER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(HEADER_DOCUMENT_PROPERTY);

    if (docId != null)
    {
      headerBrowserUrl = docServletURL + docId;
      return headerBrowserUrl;
    }
    else
    {
      return null;
    }
  }

  public void setHeaderBrowserUrl(String headerBrowserUrl)
  {
    this.headerBrowserUrl = headerBrowserUrl;
  }

  public String getFooterBrowserUrl()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(FOOTER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(FOOTER_DOCUMENT_PROPERTY);

    if (docId != null)
    {
      footerBrowserUrl = docServletURL + docId;
      return footerBrowserUrl;
    }
    else
    {
      return null;
    }
  }

  public void setFooterBrowserUrl(String footerBrowserUrl)
  {
    this.footerBrowserUrl = footerBrowserUrl;
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      if (typeSelectItems == null)
      {
        TypeBean typeBean = (TypeBean)getBean("typeBean");
        typeSelectItems =
          typeBean.getSelectItems(DictionaryConstants.DOCUMENT_TYPE,
            filter.getDocTypeId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  }

  public List<SelectItem> getClassSelectItems()
  {
    try
    {
      if (classSelectItems == null)
      {
        ClassBean classBean = (ClassBean)getBean("classBean");
        classSelectItems =
          classBean.getSelectItems(filter.getClassId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return classSelectItems;
  }
  
  public List<SelectItem> getLanguageValues()
  {
    List<SelectItem> results = new ArrayList();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor menuItem =
      userSessionBean.getMenuModel().getSelectedMenuItem();
    Locale currentLocale = new Locale(userSessionBean.getViewLanguage());    
    List<String> languages = menuItem.getMultiValuedProperty(LANGUAGE_VALUES);
    if (languages != null && !languages.isEmpty())
    {
      for (String language : languages)
      {
        if (language.equals("%%"))
          results.add(new SelectItem("%%", "universal"));
        else
        {
          Locale locale = Locale.forLanguageTag(language);
          results.add(new SelectItem(locale.getLanguage(), 
            locale.getDisplayLanguage(currentLocale)));
        }
      }
    }
    else
    {
      List<Locale> locales = userSessionBean.getSupportedLocales();
      for (Locale locale : locales)
      {
        String language = locale.getLanguage();
        String displayLanguage = locale.getDisplayLanguage(currentLocale);
        SelectItem item = new SelectItem(language, displayLanguage);
        results.add(item);
      }
    }
    return results;
  }

  //MatrixClient model methods & actions
  public DefaultMatrixClientModel getSendModel()
  {
    //Set default document type & document types list parameters
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem(); 
    String docTypeId = mic.getProperty(DEFAULTTYPE_PROPERTY);
    if (docTypeId != null)
      models.setMandatoryDocTypeId(docTypeId);
    else if (filter.getDocTypeId() != null)
      models.setDefaultDocTypeId(filter.getDocTypeId());
    else
      models.setDefaultDocTypeId(null); 
    
    return  models.getSendModel();
  }
  
  public DefaultMatrixClientModel getEditModel()
  {
    return models.getEditModel();
  }

  public DefaultMatrixClientModel getUpdateModel()
  {
    return models.getUpdateModel();
  }  
  
  /**
   * MatrixClient post sendDocument command action
  */
  public String documentSent()
  {
    try
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.web.resources.MessageBundle", getLocale());
      
      String docId = (String)models.getSendModel().parseResult();
      if (docId != null)
      {
        //Success
        Map documentProperties = getDocumentProperties();
        if (documentProperties != null && !documentProperties.isEmpty())
        {
          Document document = getClient().loadDocument(docId, 
            DocumentConstants.LAST_VERSION, ContentInfo.ID);
          
          //docTypeId not reset
          documentProperties.remove(DocumentConstants.DOCTYPEID);
          //roles
          if (documentProperties.containsKey(DocumentConstants.READ_ROLE) 
            ||documentProperties.containsKey(DocumentConstants.WRITE_ROLE)
            ||documentProperties.containsKey(DocumentConstants.DELETE_ROLE))
          {
            //Clear ACL before WS call to put roles defined in node 
            //configuration.
            document.getAccessControl().clear();
          }

          DocumentUtils.setProperties(document, documentProperties);
          getClient().storeDocument(document);
        }
        //If no filters established set filter by docId before refresh
        if (filter.isEmpty())
          filter.setDocId(docId);
        
        info(bundle.getString("DOCUMENT_UPLOADED"), new Object[]{docId});
        return search();
      }
      else
        error("UPLOAD_FAILED");
    }
    catch (Exception ex)
    {
      if (!"NO_FILE".equals(ex.getMessage()))
        error(ex);
    }

    return null;
  }
  
  /**
  * MatrixClient post updateDocument command action
  */
  public String documentUpdated()
  {
    try
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.web.resources.MessageBundle", getLocale());

      String docId = (String)models.getUpdateModel().parseResult();
      if (docId != null)
      {
        info(bundle.getString("DOCUMENT_UPDATED"), new Object[]{docId});
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return searchIfFilterNotEmpty();
  }  
  
  /**
  * MatrixClient post editDocument command action
  */ 
  public String documentEdited()
  {
    try
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.web.resources.MessageBundle", getLocale());      
      models.getEditModel().parseResult();
      info(bundle.getString("DOCUMENT_EDITED"));      
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return searchIfFilterNotEmpty();
  }    
  
  //Actions
  @CMSAction
  public String show()
  {
    
    ParametersManager[] managers = 
      {jumpManager, setObjectManager, ckEditorManager};
    String outcome = executeParametersManagers(managers);
    if (outcome != null)
      return outcome;
    else
    {
      configureColumns();
      if (isRenderDynamicForm() && !isRenderType()) 
        refreshForms(); //reset dynamic form
      if (!setObjectManager.isObjectModified())
        resetFilterValues();
      
      if (hasFirstLoadProperty())
      {
        search();
      }

      return "document_search";
    }
  }

  @CMSAction
  public String showFirst()
  {
    filter.clearAll();
    filter.setIncludeDraftCBValue(isCheckDraftState());
    filter.setIncludeCompleteCBValue(isCheckCompleteState());
    filter.setIncludeRecordCBValue(isCheckRecordState());
    filter.setIncludeDeletedCBValue(isCheckDeletedState());
    return show();
  }

  public String searchIfFilterNotEmpty()
  {
    if (!filter.isEmpty())
      return search();
    else
      return null;
  }
  
  public String clearFilter()
  {
    //Clear formFilter
    if (filter != null)
      filter.clearAll();

    //Clear dynamicForm data
    if (dynamicFormsManager != null)
      dynamicFormsManager.getData().clear();

    //Set default values
    setCurrentTypeId(getProperty(DEFAULTTYPE_PROPERTY));
    filter.setIncludeDraftCBValue(isCheckDraftState());
    filter.setIncludeCompleteCBValue(isCheckCompleteState());
    filter.setIncludeRecordCBValue(isCheckRecordState());
    filter.setIncludeDeletedCBValue(isCheckDeletedState());

    //Reset results
    reset();

    return "document_search";
  }

  @Override
  public String showObject(String typeId, String docId)
  {
    RequestParameters params = getRequestParameters();
    String version = params.getParameterValue("version");
    int ver = version != null ? Integer.valueOf(version) : 0;
    return super.showObject(typeId, DocumentConfigBean.toObjectId(docId, ver));
  }

  public String newSearch()
  {
    resetFilterValues();
    return super.search();
  }

  public String showDocument()
  {
    Document row = (Document)getFacesContext().getExternalContext().
     getRequestMap().get("row");
    String docId = row.getDocId();
    int version = row.getVersion();
    return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
      DocumentConfigBean.toObjectId(docId, version));
  }

  public String searchType()
  {
    return searchType(DictionaryConstants.DOCUMENT_TYPE,
      "#{documentSearchBean.currentTypeId}");
  }
  
  @Override
  public void setCurrentTypeId(String typeId)
  {
    this.typeSelectItems = null;
    super.setCurrentTypeId(typeId);
  }  

  public String searchClass()
  {
    ClassSearchBean classSearchBean = 
      (ClassSearchBean)getBean("classSearchBean");
    if (classSearchBean == null)
      classSearchBean = new ClassSearchBean();

    classSearchBean.search();

    return getControllerBean().searchObject("Class",
      "#{documentSearchBean.classId}");
  }

  public String pickUpDocument()
  {
    reset();
    backupFilter();
    return getControllerBean().searchObject("Document",
      "#{documentSearchBean.selectedDocumentId}");
  }

  public void setSelectedDocumentId(String docId)
  {
    try
    {
      String[] objectId = DocumentConfigBean.fromObjectId(docId);
      docId = objectId[0];
      int version = Integer.valueOf(objectId[1]);
      CachedDocumentManagerClient client = getClient();
      selectedDocument = client.loadDocument(docId, version, ContentInfo.ID);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String selectDocument()
  {
    Document row = (Document)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String docId = row.getDocId();
    int version = row.getVersion();
    restoreFilter();
    return getControllerBean().select(
      DocumentConfigBean.toObjectId(docId, version));
  }

  public void backupFilter()
  {
    if (filter != null)
      filter.backup();
  }

  public void restoreFilter()
  {
    if (filter != null)
    {
      filter.restore();
      setCurrentTypeId(filter.getDocTypeId());
    }
  }

  public Document getSelectedDocument()
  {
    return selectedDocument;
  }

  public void addSelectedDocument()
  {
    try
    {
      String docTypeId = getProperty(DEFAULTTYPE_PROPERTY);
      boolean notMatch = 
        docTypeId != null && !docTypeId.equals(selectedDocument.getDocTypeId());
      if (notMatch)
        error("DOCTYPEID_NOT_MATCH");
      else
      {
        MenuItemCursor mic = getSelectedMenuItem();
        List<String> propertyNames =
          mic.getMultiValuedProperty(FILTERPROPERTYNAME_PROPERTY);
        List<String> propertyValues =
          mic.getMultiValuedProperty(FILTERPROPERTYVALUE_PROPERTY);
        for (int i = 0; i < propertyNames.size(); i++)
        {
          String name = propertyNames.get(i);
          String value = propertyValues.get(i);
          Property property = 
            DictionaryUtils.getProperty(selectedDocument, name);
          if (property == null)
            DictionaryUtils.setProperty(selectedDocument, name, value);
          else if (!property.getValue().contains(value))
            DictionaryUtils.setProperty(selectedDocument, name, value, true);
        }
        getClient().storeDocument(selectedDocument);
        search();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      selectedDocument = null;
    }
  }

  public void discardSelectedDocument()
  {
    selectedDocument = null;
  }

  public int countResults()
  {
    try
    {
      setFilterValues();

      if (!filter.isEmpty())
        return getClient().countDocuments(filter.getDocumentFilter());
      else
        error("FILTER_IS_EMPTY");
    }
    catch (Exception ex)
    {
      //error(ex);
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.doc.web.resources.DocumentBundle", getLocale());
      error(bundle.getString("SEARCH_ERROR"));

    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      List result = new ArrayList();
      setFilterValues();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      filter.setIncludeContentMetadata(true);
      if (!filter.isEmpty())
        result = getClient().findDocuments(filter.getDocumentFilter());
      else
        error("FILTER_IS_EMPTY");
      return result;
    }
    catch (Exception ex)
    {
      //error(ex);
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.doc.web.resources.DocumentBundle", getLocale());
      error(bundle.getString("SEARCH_ERROR"));
    }
    return null;
  }

  public boolean isCurrentDocument()
  {
    String objectId = getObjectId();
    Document document = (Document)getValue("#{row}");
    if (document != null)
    {
      String docId = document.getDocId();
      int version = document.getVersion();
      String rowObjectId = DocumentConfigBean.toObjectId(docId, version);
      return rowObjectId.equals(objectId);
    }
    else
      return false;
  }

  public Map getDocumentProperties()
    throws Exception
  {
    HashMap documentProperties = new HashMap();

    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();

    //Put roles to documents

    String disableRoles = mic.getProperty(DISABLE_ROLES_PROPERTY);
    if ("true".equalsIgnoreCase(disableRoles))
    {
      documentProperties.put(DocumentConstants.READ_ROLE, "EVERYONE");
    }
    else if ("false".equalsIgnoreCase(disableRoles) || disableRoles == null)
    {
      List<String> viewRoles = mic.getViewRoles();
      documentProperties.put(DocumentConstants.READ_ROLE, viewRoles);
    } //any other value, don't put any role

    List<String> editRoles = mic.getEditRoles();
    documentProperties.put(DocumentConstants.WRITE_ROLE, editRoles);

    List<String> deleteRoles = mic.getMultiValuedProperty("roles.delete");
    documentProperties.put(DocumentConstants.DELETE_ROLE, deleteRoles);

    //Put other properties
    List<String> propertyNames =
      mic.getMultiValuedProperty(FILTERPROPERTYNAME_PROPERTY);
    List<String> propertyValues =
      mic.getMultiValuedProperty(FILTERPROPERTYVALUE_PROPERTY);

    for (int i = 0; i < propertyNames.size(); i++)
    {
      String propname = (String)propertyNames.get(i);
      String propvalue = (String)propertyValues.get(i);
      List<String> values = (List<String>)documentProperties.get(propname);
      if (values == null) values = new ArrayList();
      values.add(propvalue);
      documentProperties.put(propname, values);
    }

    //docTypeId
    String docTypeId = mic.getProperty(DEFAULTTYPE_PROPERTY);
    if (docTypeId != null)
    documentProperties.put(DocumentConstants.DOCTYPEID, docTypeId);

    return documentProperties;
  }

  public String getUrl()
  {
    Document document = (Document) getValue("#{row}");
    return DocumentUrlBuilder.getDocumentUrl(document, false);
  }
  
  public String getDownloadUrl()
  {
    Document document = (Document) getValue("#{row}");
    return DocumentUrlBuilder.getDocumentUrl(document, true);
  }
  
  public String getProtectedUrl()
  {
    Document document = (Document) getValue("#{row}");
    return DocumentUrlBuilder.getProtectedDocumentUrl(document);
  }

  public String getSearchHelpDocumentUrl()
  {
    String contextPath = getExternalContext().getRequestContextPath();

    MenuItemCursor mic =
    UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String helpDocURL = mic.getProperty(SEARCH_HELP_DOCUMENT_URL_PROPERTY);
    if (helpDocURL == null)
      return null;
    else if (helpDocURL.startsWith("http://"))
    {
      //Absolute
      return helpDocURL;
    }
    else if (helpDocURL.startsWith("/"))
    {
      //Relative to context path
      return contextPath + helpDocURL;
    }
    else
    {
      //Relative to document manager
      return contextPath + DOC_SERVLET_URL + helpDocURL;
    }
  }

  public String getMarkupUrl()
  {
    String searchExpression = 
      filter.getDocumentFilter().getContentSearchExpression();
    Document document = (Document)getValue("#{row}");
    return DocumentUrlBuilder.getMarkupUrl(document, searchExpression);
  }

  public String getLanguageFlag()
  {
    Document document = (Document) getValue("#{row}");
    if (document != null)
      return DocumentUtils.getLanguageFlag(FLAGS_PATH_URL,
        document.getLanguage());
    else
      return null;
  }

  public String getLanguage()
  {
    Document document = (Document) getValue("#{row}");
    if (document != null)
      return DocumentUtils.extendLanguage(document.getLanguage());
    else
      return "";
  }

  public String getFileTypeImage()
  {
    Document document = (Document) getValue("#{row}");
    if (document != null)
    {
      Content content = document.getContent();
      if (content != null)
      {
        return DocumentBean.getContentTypeIcon(content.getContentType());
      }
      else
      {
        return DocumentBean.getContentTypeIcon(null);
      }
    }
    else
      return "";
  }
  
  public String getExtension()
  {
    Document document = (Document) getValue("#{row}");
    if (document != null)
    {
      Content content = document.getContent();
      String extension = 
        MimeTypeMap.getMimeTypeMap().getExtension(content.getContentType());
      return extension;
    }
    return "";
  }

  public Date getVersionDate()
  {
    Document document = (Document) getValue("#{row}");
    if (document != null)
    {
      String changeDate = document.getChangeDateTime();
      try
      {
        return new SimpleDateFormat("yyyyMMddHHmmss").parse(changeDate);
      }
      catch (Exception e)
      {
        return null;
      }
    }
    else
      return null;
  }

  public String getSize()
  {
    String result = " ";
    Document document = (Document) getValue("#{row}");
    if (document != null)
    {
      Content content = document.getContent();
      if (content != null && content.getSize() != null)
      {
        result = DocumentUtils.getSizeString(content.getSize().longValue());
      }
    }

    return result;
  }

  public String getDocumentTitle()
  {
    String title = "";
    Document document = (Document)getValue("#{row}");
    if (document != null)
    {
      title = document.getTitle();
      String pattern = getProperty(REPLACE_BY_WHITESPACE_PATTERN_PROPERTY);
      if (pattern != null && title != null)
      {
        title = title.replaceAll(pattern, " ");
      }
    }
    return title;
  }

  public boolean isRenderFilterPanel()
  {
    return this.render(FILTERPANEL_PROPERTY);
  }

  public boolean isRenderCollapsiblePanel()
  {
    return isRenderDocId() || isRenderVersion() || isRenderContentId() ||
      isRenderData() || isRenderLanguage() || isRenderState() ||
      isRenderType() || isRenderClass() || isRenderDate() ||
      isRenderOutputSearchExpression() || isRenderTitle();
  }

  public boolean isRenderDynamicForm()
  {
    return this.render(RENDER_DYNAMIC_FORM_PROPERTY) && getSelector() != null;
  }

  public boolean isRenderPropertyValueFilter()
  {
    return this.render(RENDER_PROPERTY_VALUE_FILTER, false);
  }

  public boolean isRenderTitle()
  {
    return this.render(RENDERTITLE_PROPERTY);
  }

  public boolean isRenderDocId()
  {
    return this.render(RENDERDOCID_PROPERTY, false);
  }

  public boolean isRenderVersion()
  {
    return this.render(RENDERVERSION_PROPERTY, false);
  }

  public boolean isRenderContentId()
  {
    return this.render(RENDERCONTENTID_PROPERTY, false);
  }

  public boolean isRenderData()
  {
    return this.render(RENDERDATA_PROPERTY);
  }

  public boolean isRenderLanguage()
  {
    return this.render(RENDERLANGUAGE_PROPERTY);
  }

  public boolean isRenderState()
  {
    return isRenderDraftState() || isRenderCompleteState() ||
      isRenderRecordState() || isRenderDeletedState();
  }

  public boolean isRenderType()
  {
    return this.render(RENDERTYPE_PROPERTY, false);
  }

  public boolean isRenderDate()
  {
    return this.render(RENDERDATE_PROPERTY, false);
  }

  public boolean isRenderClass()
  {
    return this.render(RENDERCLASS_PROPERTY, false);
  }

  public boolean isRenderDraftState()
  {
    return this.render(RENDERDRAFTSTATE_PROPERTY, false);
  }

  public boolean isRenderCompleteState()
  {
    return this.render(RENDERCOMPLETESTATE_PROPERTY, false);
  }

  public boolean isRenderRecordState()
  {
    return this.render(RENDERRECORDSTATE_PROPERTY, false);
  }

  public boolean isRenderDeletedState()
  {
    return this.render(RENDERDELETEDSTATE_PROPERTY, false);
  }

  public boolean isRenderOutputSearchExpression()
  {
    return this.render(RENDER_OUTPUT_SEARCH_EXPR_PROPERTY, false);
  }

  public boolean isRenderPickUpButton()
  {
    return this.render(RENDER_PICKUP_BUTTON, false);
  }

  public boolean isCheckDraftState()
  {
    return this.render(CHECKDRAFTSTATE_PROPERTY, false);
  }

  public boolean isCheckCompleteState()
  {
    return this.render(CHECKCOMPLETESTATE_PROPERTY);
  }

  public boolean isCheckRecordState()
  {
    return this.render(CHECKRECORDSTATE_PROPERTY, false);
  }

  public boolean isCheckDeletedState()
  {
    return this.render(CHECKDELETEDSTATE_PROPERTY, false);
  }

  public boolean isRenderDescColumn()
  {
    return this.render(RENDERDESCCOLUMN_PROPERTY);
  }

  public boolean isRenderDateColumn()
  {
    return this.render(RENDERDATECOLUMN_PROPERTY);
  }

  public boolean isRenderSizeColumn()
  {
    return this.render(RENDERSIZECOLUMN_PROPERTY);
  }

  public boolean isRenderUrlColumn()
  {
    return this.render(RENDERURLCOLUMN_PROPERTY);
  }

  public boolean isRenderLangColumn()
  {
    return this.render(RENDERLANGCOLUMN_PROPERTY);
  }

  public boolean isRenderDocIdColumn()
  {
    return this.render(RENDERDOCIDCOLUMN_PROPERTY, false);
  }

  public boolean isRenderDocTypeColumn()
  {
    return this.render(RENDERDOCTYPECOLUMN_PROPERTY, false);
  }

  public boolean isRenderVersionColumn()
  {
    return this.render(RENDERVERSIONCOLUMN_PROPERTY, false);
  }

  public boolean isRenderSearchButton()
  {
    //return (isRenderData() || isRenderTitle() || isRenderLanguage());
    return true;
  }

  public boolean isRenderClearButton()
  {
    return render(RENDER_CLEAR_BUTTON, false);
  }

  public boolean isHeaderRender()
  {
    return render(HEADER_RENDER_PROPERTY, true);
  }

  public boolean isFooterRender()
  {
    return render(FOOTER_RENDER_PROPERTY, true);
  }
  
  public boolean isExtensionRender()
  {
    return render(RENDEREXTENSION_PROPERTY, false);
  }

  @Override
  public int getCacheSize()
  {
    int superPageSize = super.getPageSize();
    if (superPageSize != PAGE_SIZE)
      return superPageSize;

    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();

    String rowsPerPage = mic.getProperty(ROWSPERPAGE_PROPERTY);
    if (rowsPerPage != null)
      return Integer.parseInt(rowsPerPage) + 5;
    else
      return super.getCacheSize();
  }

  @Override
  public int getPageSize()
  {
    int superPageSize = super.getPageSize();
    if (superPageSize != PAGE_SIZE)
      return superPageSize;

    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();

    String rowsPerPage = mic.getProperty(ROWSPERPAGE_PROPERTY);
    if (rowsPerPage != null)
      return Integer.parseInt(rowsPerPage);
    else
      return superPageSize;
  }

  private void setFilterValues() throws Exception
  {
    filter.clearLists();

    setOrderByProperties();

    List<Property> formProperties = getFormDataAsProperties();
    filter.setFormProperties(formProperties);
    //apply input properties
    filter.setInputProperties(formProperties);

    //apply node filters
    String docTypeId = getProperty(DEFAULTTYPE_PROPERTY);
    if (docTypeId != null)
      setCurrentTypeId(docTypeId);

    String searchExpression = getProperty(SEARCH_EXPRESSION_PROPERTY);
    if (searchExpression != null)
      filter.setMetadataSearchExpression(searchExpression);

    String propName = getProperty(FILTERPROPERTYNAME_PROPERTY);
    List<String> propertyValues =
      getSelectedMenuItem().getMultiValuedProperty(FILTERPROPERTYVALUE_PROPERTY);
    filter.setProperty(propName, propertyValues);

    //set additional values
    if (getCurrentTypeId() != null)
      filter.setDocTypeId(getCurrentTypeId());
    if (!isRenderFilterPanel())
    {
      filter.getDocumentFilter().setContentSearchExpression(null);
      filter.getDocumentFilter().setLanguage(null);
      filter.getDocumentFilter().setTitle(null);
    }
  }

  private void resetFilterValues()
  {
    DocumentFilter docFilter = filter.getDocumentFilter();
    if (!isRenderFilterPanel() || !isRenderDocId())
      docFilter.getDocId().clear();
    if (!isRenderFilterPanel() || !isRenderVersion())
      docFilter.setVersion(0);
    if (!isRenderFilterPanel() || !isRenderContentId())
      docFilter.setContentId(null);
    if (!isRenderFilterPanel() || !isRenderTitle())
      docFilter.setTitle(null);
    if (!isRenderFilterPanel() || !isRenderType())
    {
      docFilter.setDocTypeId(null);
      setCurrentTypeId(null);
    }

    docFilter.getStates().clear();
    
    if (!isRenderFilterPanel() || !isRenderClass())
      docFilter.getClassId().clear();
    if (!isRenderFilterPanel() || !isRenderDate())
    {
      docFilter.setStartDate(null);
      docFilter.setEndDate(null);
    }
    if (!isRenderFilterPanel() || !isRenderData())
      docFilter.setContentSearchExpression(null);
    if (!isRenderFilterPanel() || !isRenderLanguage())
      docFilter.setLanguage(null);
    if (!isRenderFilterPanel() || !isRenderOutputSearchExpression())
      docFilter.setMetadataSearchExpression(null);

    filter.setDocumentFilter(docFilter);
  }

  private boolean hasFirstLoadProperty()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String firstLoad = mic.getProperty(FIRSTLOAD_PROPERTY);

    return !"false".equals(firstLoad);
  }

  //Renderer methods

  private void setOrderByProperties()
  {
    MenuItemCursor mic = getSelectedMenuItem(); 
    List<String> orderby = mic.getMultiValuedProperty(ORDERBY_PROPERTY);
    if (orderby.isEmpty())
      orderby = mic.getMultiValuedProperty(ORDERBY_PROPERTY.toLowerCase());
    Iterator it = orderby.iterator();
    while (it.hasNext())
    {
      String orderByElement = (String) it.next();
      String[] parts = orderByElement.split(":");
      OrderByProperty orderByProperty = new OrderByProperty();
      orderByProperty.setName(parts[0]);
      if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
      {
        orderByProperty.setDescending(true);
      }
      filter.getOrderByProperties().add(orderByProperty);
    }
  }

  private CachedDocumentManagerClient getClient()
  {
    CachedDocumentManagerClient client = new CachedDocumentManagerClient(
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());
    return client;
  }

  private void configureColumns()
  {
    resultsManager.getDefaultColumnNames().clear();

    if (isRenderDocIdColumn())
      resultsManager.addDefaultColumn("docId");

    if (isRenderVersionColumn())
      resultsManager.addDefaultColumn("version");

    if (isRenderDocTypeColumn())
    {
      ColumnDefinition docTypeColDef = new ColumnDefinition("docTypeId");
      docTypeColDef.setConverter(new TypeIdConverter());
      docTypeColDef.setStyleClass("sizeColumn");
      resultsManager.addDefaultColumn(docTypeColDef);
    }

    if (isRenderDescColumn())
    {
      ColumnDefinition titleColDef = new ColumnDefinition("title");
      titleColDef.setType(ColumnDefinition.CUSTOM_TYPE);
      titleColDef.setStyleClass("descColumn");
      resultsManager.addDefaultColumn(titleColDef);
    }

    if (isRenderDateColumn())
    {
      ColumnDefinition changeDateTimeColDef = 
        new ColumnDefinition("changeDateTime");
      changeDateTimeColDef.setConverter(new DateTimeConverter("dd/MM/yyyy"));
      changeDateTimeColDef.setStyleClass("dateColumn");
      resultsManager.addDefaultColumn(changeDateTimeColDef);
    }

    if (isRenderUrlColumn())
    {
      ColumnDefinition urlColDef = new ColumnDefinition("url");
      urlColDef.setType(ColumnDefinition.CUSTOM_TYPE);
      resultsManager.addDefaultColumn(urlColDef);
    }

    if (isRenderSizeColumn())
    {
      ColumnDefinition sizeColDef = new ColumnDefinition("content.size");
      sizeColDef.setConverter(new FileSizeConverter());
      sizeColDef.setStyleClass("sizeColumn");
      resultsManager.addDefaultColumn(sizeColDef);
    }

    if (isRenderLangColumn())
    {
      ColumnDefinition langColDef = new ColumnDefinition("language");
      langColDef.setType(ColumnDefinition.CUSTOM_TYPE);
      langColDef.setStyleClass("langColumn");
      resultsManager.addDefaultColumn(langColDef);
    }

    ColumnDefinition actionsColDef = new ColumnDefinition("actions");
    actionsColDef.setType(ColumnDefinition.CUSTOM_TYPE);
    actionsColDef.setAlias("");
    actionsColDef.setStyleClass("actionsColumn");
    resultsManager.addDefaultColumn(actionsColDef);
  }

  public String getAdminRole()
  {
    return DocumentConstants.DOC_ADMIN_ROLE;
  }
  
  public String getRowStyleClass()
  {
    String defaultStyleClass = null;
    Document row = (Document)getValue("#{row}");
    if (row != null && row.getDocId().equals(getObjectId()))
      defaultStyleClass = "selectedRow";

    return getRowStyleClass(defaultStyleClass);
  }
  
  public boolean isCKEditorCall()
  {
    return ckEditorManager.getEditorInstance() != null;
  }
  
  public String getEditorCallback()
  {
    return ckEditorManager.getCallbackReference();
  } 
    
}
