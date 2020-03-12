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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.State;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;
import org.matrix.classif.Class;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.doc.Content;
import org.matrix.security.AccessControl;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.RelatedDocument;
import org.matrix.security.SecurityConstants;
import org.santfeliu.classif.web.ClassBean;
import org.santfeliu.classif.web.ClassificationConfigBean;
import static org.santfeliu.doc.web.DocumentSearchBean.DISABLE_ROLES_PROPERTY;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.util.SetObjectManager;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author unknown
 */
public class DocumentMainBean extends DynamicTypifiedPageBean
{
  public static final String RELATED_DOCUMENT_MESSAGE_PROPERTY =
    "relatedDocumentMessage.";
  public static final String STORE_NEW_VERSION = "storeNewVersion";
  
  private static final String CLASSID_VALUES_SEPARATOR = ";";

//  private ParametersManager parametersManager;
  private Document document;
  private String classIdString; // ex: T023;T021;G004
  private String classTitle; // title of first classId in classIdString

  public DocumentMainBean()
  {
    super(DictionaryConstants.DOCUMENT_TYPE, "DOC_ADMIN");
  }

  public void setDocument(Document document)
  {
    this.document = document;
  }

  public Document getDocument()
  {
    return document;
  }

  public void setClassId(String classId) // call from search & select
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    if (classIdString == null || classIdString.trim().length() == 0)
    {
      setClassIdString(classBean.getClassId(classId));
    }
    else
    {
      setClassIdString(classIdString +
        CLASSID_VALUES_SEPARATOR + classBean.getClassId(classId));
    }
  }

  public String getClassIdString()
  {
    return classIdString;
  }

  public void setClassIdString(String classIdString)
  {
    this.classIdString = classIdString;
    // get class title
    if (classIdString != null && classIdString.trim().length() > 0)
    {
      int index = classIdString.indexOf(CLASSID_VALUES_SEPARATOR);
      String classId = (index != -1) ?
        classIdString.substring(0, index) : classIdString;
      try
      {
        // get current title
        ClassificationManagerPort port = ClassificationConfigBean.getPort();
        Class clazz = port.loadClass(classId, null);
        classTitle = clazz.getTitle();
      }
      catch (Exception ex)
      {
        this.classTitle = null;
      }
    }
    else
    {
      this.classTitle = null;
    }
  }

  public boolean isValidClassId()
  {
    return classTitle != null ||
     (classIdString == null || classIdString.trim().length() == 0);
  }

  public String getClassTitle()
  {
    return classTitle;
  }

  @Override
  public String show()
  {
    return "document_main";
  }

  public String showClass()
  {
    return getControllerBean().showObject("Class", document.getClassId().get(0));
  }

  public String searchClass()
  {
    return getControllerBean().searchObject("Class",
      "#{documentMainBean.classId}");
  }

  public String searchType()
  {
    return searchType("#{documentMainBean.currentTypeId}");
  }

  public String showType()
  {
    return getControllerBean().showObject("Type", getCurrentTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getCurrentTypeId() != null && getCurrentTypeId().trim().length() > 0;
  }

  public String getRelatedDocumentMessage()
  {
    RelatedDocument relDoc = (RelatedDocument)getValue("#{relDoc}");
    String relationType = relDoc.getRelationType().toString();
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    return cursor.getProperty(RELATED_DOCUMENT_MESSAGE_PROPERTY + relationType);
  }

  public String showRelatedDocument()
  {
    RelatedDocument relDoc = (RelatedDocument)getValue("#{relDoc}");
    return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
      DocumentConfigBean.toObjectId(relDoc.getDocId(), relDoc.getVersion()));
  }

  @Override
  public String store()
  {
    try
    {
      ControllerBean controllerBean = getControllerBean();
      if (controllerBean.existsBean("documentContentBean"))
      {
        DocumentContentBean contentBean =
          (DocumentContentBean)getBean("documentContentBean");
        Content content = contentBean.createContent();
        if (content != null && (content.getData() != null || !isNew()))
          document.setContent(content);
      }
      
      if (isNew())
      {
        String disableRoles = getProperty(DISABLE_ROLES_PROPERTY);
        if ("true".equals(disableRoles))
        {
          AccessControl acl = new AccessControl();                
          acl.setAction("Read");
          acl.setRoleId(SecurityConstants.EVERYONE_ROLE);
          document.getAccessControl().clear();
          document.getAccessControl().add(acl);
        }
        else if ("false".equals(disableRoles) || disableRoles == null)
        {
          List<String> viewRoles = getSelectedMenuItem().getViewRoles();
          if (viewRoles != null && !viewRoles.isEmpty())
          {
            document.getAccessControl().clear();
            for (String viewRole : viewRoles)
            {
              AccessControl acl = new AccessControl();                    
              acl.setAction("Read");
              acl.setRoleId(viewRole);
              document.getAccessControl().add(acl);              
            }
          }
        } //any other value, don't put any role
      }
        
      document.getClassId().clear();
      List<String> classIdList = 
        TextUtils.stringToList(classIdString, CLASSID_VALUES_SEPARATOR);
      if (classIdList != null)
        document.getClassId().addAll(classIdList);

      document.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        document.getProperty().addAll(properties);

      document.setIncremental(false);
      document.setDocTypeId(getCurrentTypeId());

      if (((DocumentBean)getObjectBean()).isCreateNewVersion()) 
        document.setVersion(DocumentConstants.NEW_VERSION);

      document = DocumentConfigBean.getClient().storeDocument(document);
      setObjectId(DocumentConfigBean.toObjectId(document.getDocId(),
        document.getVersion()));

      setFormDataFromProperties(document.getProperty());
      setClassIdString(TextUtils.collectionToString(
        document.getClassId(), CLASSID_VALUES_SEPARATOR));
    }
    catch (Exception ex)
    {
      error(ex);
      List<String> details = WSExceptionFactory.getDetails(ex);
      if (details.size() > 0) error(details);
    }
    return show();
  }

  public void load()
  {
    if (isNew())
    {
      this.document = new Document();
//      parametersManager = new ParametersManager();
      SetObjectManager setObjectManager = 
        new SetObjectManager(document);
      setObjectManager.execute(getRequestParameters());
      setCurrentTypeId(document.getDocTypeId());
      setFormDataFromProperties(document.getProperty());     
    }
    else
    {
      try
      {
        String[] objectId = DocumentConfigBean.fromObjectId(getObjectId());
        this.document =
          DocumentConfigBean.getClient().loadDocument(objectId[0],
          0 - Integer.valueOf(objectId[1]), ContentInfo.METADATA);

        setCurrentTypeId(document.getDocTypeId());
        setFormDataFromProperties(document.getProperty());
        data.remove("classId");
        setClassIdString(TextUtils.collectionToString(document.getClassId(),
          CLASSID_VALUES_SEPARATOR));
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();        
        this.document = new Document();
        setClassIdString(null);
        error(ex);
      }
    }
    ((DocumentBean)getObjectBean()).setCreateNewVersion(isStoreNewVersion());
  }

  public void reload(boolean createNewVersion)
  {
    this.load();
    ((DocumentBean)getObjectBean()).setCreateNewVersion(createNewVersion);
  }

  public String lockDocument()
  {
    try
    {
      String[] objectId = DocumentConfigBean.fromObjectId(getObjectId());
      DocumentConfigBean.getClient().lockDocument(objectId[0],
        Integer.valueOf(objectId[1]).intValue());
      load();
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String unlockDocument()
  {
    try
    {
      String[] objectId = DocumentConfigBean.fromObjectId(getObjectId());
      DocumentConfigBean.getClient().unlockDocument(objectId[0],
        Integer.valueOf(objectId[1]).intValue());
      load();
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public SelectItem[] getDocumentStateSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.doc.web.resources.DocumentBundle", getLocale());
    return FacesUtils.getEnumSelectItems(State.class, bundle);
  }

  public Date getCaptureDateTime()
  {
    if (document != null && document.getCaptureDateTime() != null)
    {
      try
      {
        return new SimpleDateFormat("yyyyMMddHHmmss").parse(document.getCaptureDateTime());
      }
      catch (ParseException ex)
      {
        Logger.getLogger(DocumentMainBean.class.getName()).log(Level.INFO, null, ex);
        return null;
      }
    }
    else
      return null;
  }

  public Date getChangeDateTime() throws ParseException
  {
    if (document != null && document.getChangeDateTime() != null)
      return new SimpleDateFormat("yyyyMMddHHmmss").parse(document.getChangeDateTime());
    else
      return null;
  }

  public boolean isEditable() throws Exception
  {
    if (document == null || document.getDocId() == null)
      return true;

    //If is Admin user
    if (UserSessionBean.getCurrentInstance().isUserInRole(
      DocumentConstants.DOC_ADMIN_ROLE))
      return true;
    
    //If document is deleted
    if (isDeletedDocument())
      return false;

    Type currentType = getCurrentType();
    if (currentType == null)
      return true;

    HashSet<AccessControl> acls = new HashSet();
    acls.addAll(currentType.getAccessControl());
    acls.addAll(document.getAccessControl());

    if (acls != null)
    {
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
    }
    return false;
  }
  
  public boolean isDeletedDocument()
  {
    return document != null && State.DELETED.equals(document.getState());
  }

  private boolean isStoreNewVersion()
  {
    String prop = getProperty(STORE_NEW_VERSION);
    if (prop != null)
      return "true".equalsIgnoreCase(prop);
    else
      return false;
  }
}
