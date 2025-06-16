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
package org.santfeliu.webapp.modules.query;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.misc.query.Query;
import org.santfeliu.misc.query.io.QueryFinder;
import org.santfeliu.misc.query.io.QueryReader;
import org.santfeliu.misc.query.io.QueryWriter;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@RequestScoped
public class QueryMainBean extends WebBean implements Serializable, QueryFinder
{
  // Query Type
  public static String QUERY_TYPEID = "QUERY";
  
  // Query EnumTypes
  public static final String QUERY_SCOPE_TYPEID = "QueryScope";
  public static final String QUERY_TYPE_TYPEID = "QueryType";
  public static final String QUERY_OBJECT_TYPEID = "QueryObject";

  // Query properties
  public static final String QUERY_NAME_PROPERTY = "query_name";
  public static final String QUERY_DESCRIPTION_PROPERTY = "description";
  public static final String QUERY_UPDATE_PROPERTY = "update_sql";
  public static final String QUERY_SCOPE_PROPERTY = "query_scope";
  public static final String QUERY_TYPE_PROPERTY = "query_type";
  public static final String QUERY_BASE_PROPERTY = "query_base";
  public static final String QUERY_OBJECT_PROPERTY = "query_object";
  public static final String QUERY_CODE_PROPERTY = "query_code";

  private Query query = new Query();
  private boolean persistent = false;
  private final Set<String> readRoles = new HashSet();
  private final Set<String> writeRoles = new HashSet();
  private String queryScope;
  private String queryObject;
  private String queryType;
  private String queryCode;
  private boolean createNewVersion = true;
  
  private String view = "query_list";

  @Inject
  QueryListBean queryListBean;   
  
  public QueryMainBean()
  {
  }

  public Query getQuery()
  {
    return query;
  }

  public void copyQuery()
  {
    query.setName(null);
    persistent = false;
  }
  
  public boolean isPersistent()
  {
    return persistent;
  }

  public String getQueryScope()
  {
    return queryScope;
  }

  public void setQueryScope(String queryScope)
  {
    this.queryScope = queryScope;
  }

  public String getQueryObject()
  {
    return queryObject;
  }

  public void setQueryObject(String queryObject)
  {
    this.queryObject = queryObject;
  }

  public String getQueryType()
  {
    return queryType;
  }

  public void setQueryType(String queryType)
  {
    this.queryType = queryType;
  }

  public String getQueryCode()
  {
    return queryCode;
  }

  public void setQueryCode(String queryCode)
  {
    this.queryCode = queryCode;
  }

  public boolean isCreateNewVersion() 
  {
    return createNewVersion;
  }

  public void setCreateNewVersion(boolean createNewVersion) 
  {
    this.createNewVersion = createNewVersion;
  }

  public String getView()
  {    
    return view;
  }

  public void setView(String view)
  {
    this.view = view;
  }
  
  public String getContent()
  {
    if ("query_results".equals(view))
    {
      return "/pages/sqlweb/sqlweb.xhtml";
    }
    else if ("query_list".equals(view))
    {
      return "/pages/query/query_list.xhtml";
    }
    else if ("query_edit".equals(view))
    {
      return "/pages/query/edit/query_edit.xhtml";
    }
    else if ("query_view".equals(view))
    {
      return "/pages/query/view/query_view.xhtml";      
    }
    else
    {
      return "/pages/query/query_list.xhtml";
    }
  }
  
  public String show()
  {
    if ("query_list".equals(view)) queryListBean.search();
    
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";    
  }
  
  public String getReadRolesString()
  {
    return TextUtils.joinWords(readRoles);
  }

  public void setReadRolesString(String roles)
  {
    readRoles.clear();
    TextUtils.splitWords(roles, readRoles);
  }

  public String getWriteRolesString()
  {
    return TextUtils.joinWords(writeRoles);
  }

  public void setWriteRolesString(String roles)
  {
    writeRoles.clear();
    TextUtils.splitWords(roles, writeRoles);
  }

  public Set<String> getReadRoles()
  {
    return readRoles;
  }

  public Set<String> getWriteRoles()
  {
    return writeRoles;
  }

  public boolean isEditionEnabled()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole("QUERY_EDITOR");
  }

  public boolean isSaveEnabled()
  {
    if (!persistent) return true;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(writeRoles) || 
      userSessionBean.isUserInRole(DocumentConstants.DOC_ADMIN_ROLE);
  }

  public void createQuery()
  {
    query = new Query();
    persistent = false;
    setView("query_edit");
  }
  
  public void setQueryName()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(StringUtils.isBlank(queryScope) ? "x" : queryScope); 
    buffer.append("_");
    buffer.append(StringUtils.isBlank(queryObject) ? "x" : queryObject); 
    buffer.append("_");
    buffer.append(StringUtils.isBlank(queryType) ? "x" : queryType); 
    buffer.append("_");
    buffer.append(StringUtils.isBlank(queryCode) ? "x" : queryCode);
    String newName = buffer.toString();
    query.setName(newName);
    persistent = false;
  }

  public void reloadQuery() throws Exception
  {
    loadQuery(query.getName());
  }

  public void loadQuery(String queryName) throws Exception
  {
    Document document = getDocumentManagerClient().loadDocumentByName(
      QUERY_TYPEID, QUERY_NAME_PROPERTY, queryName, null, -1);
    if (document == null) throw new Exception("QUERY_NOT_FOUND");
    
    QueryReader reader = new QueryReader();
    reader.setQueryFinder(this);
    InputStream is = document.getContent().getData().getInputStream();
    try
    {
      query = reader.readQuery(is);
    }
    finally
    {
      is.close();
    }
    query.setName(queryName);
    loadRoles(document);
    loadClassification(document);
    loadAuditData(document);
    persistent = true;    
  }

  public void saveQuery() throws Exception
  {
    saveQuery(true);
  }

  public void saveQuery(boolean refreshDocuments) throws Exception
  {
    if (StringUtils.isBlank(query.getName())) 
      throw new Exception("INVALID_QUERY_NAME");

    Document document = getDocumentManagerClient().
      loadDocumentByName(QUERY_TYPEID, QUERY_NAME_PROPERTY, 
      query.getName(), null, -1);
    if (document == null)
    {
      document = new Document();
    }
    else if (!persistent)
    {
      throw new Exception("QUERY_ALREADY_EXISTS");
    }
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try
    {
      QueryWriter writer = new QueryWriter();
      writer.setQueryFinder(this);
      writer.writeQuery(query, os);
    }
    finally
    {
      os.close();
    }
    document.setTitle(query.getTitle());
    document.setDocTypeId(QUERY_TYPEID);
    document.getProperty().clear();
    Property property = new Property();
    property.setName(QUERY_NAME_PROPERTY);
    property.getValue().add(query.getName());
    document.getProperty().add(property);
    property = new Property();
    property.setName(QUERY_DESCRIPTION_PROPERTY);
    property.getValue().add(String.valueOf(query.getDescription()));
    document.getProperty().add(property);
    property = new Property();
    property.setName(QUERY_UPDATE_PROPERTY);
    property.getValue().add(String.valueOf(query.isUpdateSql()));
    document.getProperty().add(property);
    property = new Property();
    property.setName(QUERY_SCOPE_PROPERTY);
    property.getValue().add(String.valueOf(queryScope));
    document.getProperty().add(property);
    property = new Property();
    property.setName(QUERY_TYPE_PROPERTY);
    property.getValue().add(String.valueOf(queryType));
    document.getProperty().add(property);
    property = new Property();
    property.setName(QUERY_OBJECT_PROPERTY);
    property.getValue().add(String.valueOf(queryObject));
    document.getProperty().add(property);
    property = new Property();
    property.setName(QUERY_CODE_PROPERTY);
    property.getValue().add(String.valueOf(queryCode));
    document.getProperty().add(property);
    property = new Property();
    property.setName(QUERY_BASE_PROPERTY);
    property.getValue().add(String.valueOf(query.getBase()));
    document.getProperty().add(property);

    DataSource dataSource =
      new MemoryDataSource(os.toByteArray(), "xml", "text/xml");
    DataHandler dataHandler = new DataHandler(dataSource);
    Content content = new Content();
    content.setData(dataHandler);
    document.setContent(content);
    document.setIncremental(true);
    putRoles(document);
    if (createNewVersion)
    {
      document.setVersion(DocumentConstants.NEW_VERSION);
    }
    document = getDocumentManagerClient().storeDocument(document);
    loadRoles(document);
    persistent = true;

    if (refreshDocuments) queryListBean.clearDocuments();
  }
  
  public void removeQuery()
  {
    DocumentManagerClient docClient = getDocumentManagerClient();
    Document document = docClient.
      loadDocumentByName(QUERY_TYPEID, QUERY_NAME_PROPERTY, 
      query.getName(), null, -1);
    if (document != null)
    {
      docClient.removeDocument(document.getDocId(), document.getVersion());
    }

    queryListBean.clearDocuments();
    query = new Query();
    persistent = false;  
  }
  
  @Override
  public InputStream getQueryStream(String queryName) throws Exception
  {
    Document document = getDocumentManagerClientForAdmin().loadDocumentByName(
      QUERY_TYPEID, QUERY_NAME_PROPERTY, queryName, null, -1);    
    if (document == null) throw new Exception("QUERY_NOT_FOUND");
    return document.getContent().getData().getInputStream();
  }    

  public static DocumentManagerClient getDocumentManagerClientForAdmin()
  {
    String adminUserId = 
      MatrixConfig.getProperty("adminCredentials.userId");
    String adminPassword = 
      MatrixConfig.getProperty("adminCredentials.password");
    DocumentManagerClient docClient = new DocumentManagerClient(
      adminUserId, adminPassword);
    return docClient;
  }
  
  public static DocumentManagerClient getDocumentManagerClient()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    DocumentManagerClient docClient = new DocumentManagerClient(
      userSessionBean.getUserId(), userSessionBean.getPassword());
    return docClient;
  }

  private void loadClassification(Document document)
  {
    queryScope = DictionaryUtils.getPropertyValue(document.getProperty(), 
      QUERY_SCOPE_PROPERTY);      
    queryType = DictionaryUtils.getPropertyValue(document.getProperty(), 
      QUERY_TYPE_PROPERTY);
    queryObject = DictionaryUtils.getPropertyValue(document.getProperty(), 
      QUERY_OBJECT_PROPERTY);
    queryCode = DictionaryUtils.getPropertyValue(document.getProperty(), 
      QUERY_CODE_PROPERTY);
  }
  
  private void loadRoles(Document document)
  {
    readRoles.clear();
    writeRoles.clear();    
    List<AccessControl> aclList = document.getAccessControl();
    for (AccessControl acl : aclList)
    {
      String action = acl.getAction();
      String roleId = acl.getRoleId();
      if (action.equals(DictionaryConstants.READ_ACTION))
      {
        readRoles.add(roleId);
      }
      else if (action.equals(DictionaryConstants.WRITE_ACTION))
      {
        writeRoles.add(roleId);
      }
    }  
  }
  
  private void putRoles(Document document)
  {
    List<AccessControl> aclList = document.getAccessControl();
    aclList.clear();
    for (String roleId : readRoles)
    {
      AccessControl acl = new AccessControl();
      acl.setAction(DictionaryConstants.READ_ACTION);
      acl.setRoleId(roleId);
      aclList.add(acl);
    }
    for (String roleId : writeRoles)
    {
      AccessControl acl = new AccessControl();
      acl.setAction(DictionaryConstants.WRITE_ACTION);
      acl.setRoleId(roleId);
      aclList.add(acl);
    }    
  }
  
  private void loadAuditData(Document document)
  {
    query.setCaptureDateTime(document.getCaptureDateTime());
    query.setCaptureUserId(document.getCaptureUserId());
    query.setChangeDateTime(document.getChangeDateTime());
    query.setChangeUserId(document.getChangeUserId());
  }
}
