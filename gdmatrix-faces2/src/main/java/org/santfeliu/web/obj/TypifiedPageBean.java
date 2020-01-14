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
package org.santfeliu.web.obj;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;
import org.matrix.cases.CaseConstants;

import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.TypeFilter;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.dic.web.TypeSearchBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.util.ParametersManager;

public abstract class TypifiedPageBean extends GroupablePageBean
{
  //Node properties
  @CMSProperty
  public static final String TAB_INDEX_PROPERTY = "tabIndex";

  protected String rootTypeId = null;
  protected String rowTypeId = null;
  protected boolean allTypesVisible = true;
  protected boolean showTypePath = true;
  protected String adminRole = null;

  protected ParametersManager parametersManager;  

  public TypifiedPageBean(String rootTypeId, String adminRole)
  {
    this.rootTypeId = rootTypeId;
    this.adminRole = adminRole;
  }

  public void setRootTypeId(String rootTypeId)
  {
    this.rootTypeId = rootTypeId;
  }
  
  public String getRootTypeId()
  {
    return rootTypeId;
  }
  
  public boolean isShowTypePath()
  {
    return showTypePath;
  }

  public void setShowTypePath(boolean showTypePath)
  {
    this.showTypePath = showTypePath;
  }

  public List<SelectItem> getAllTypeItems()
  {
    return getAllTypeItems(DictionaryConstants.READ_ACTION,
      DictionaryConstants.CREATE_ACTION, DictionaryConstants.WRITE_ACTION);
  }

  public List<SelectItem> getAllTypeItems(String ... actions)
  {
    try
    {
      TypeBean typeBean = (TypeBean) getBean("typeBean");
      return typeBean.getAllSelectItems(rootTypeId, adminRole, actions,
        allTypesVisible);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return Collections.EMPTY_LIST;
  }
  
  public String searchType(String valueBinding)
  {
    TypeSearchBean typeSearchBean = (TypeSearchBean)getBean("typeSearchBean");
    if (typeSearchBean == null)
      typeSearchBean = new TypeSearchBean();

    typeSearchBean.setRootTypeId(rootTypeId);
    typeSearchBean.setFilter(new TypeFilter());
    typeSearchBean.search();

    return getControllerBean().searchObject("Type", valueBinding);
  }

  public boolean isAllTypesVisible()
  {
    return allTypesVisible;
  }

  public void setAllTypesVisible(boolean allTypesVisible)
  {
    this.allTypesVisible = allTypesVisible;
  }

  public boolean isUserAdmin()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole(adminRole);
  }
  
  @Override
  protected org.santfeliu.dic.Type getSelectedType()
  {
    TypeCache typeCache = TypeCache.getInstance();
    if (rootTypeId != null && rootTypeId.length() > 0)
      return typeCache.getType(rootTypeId);
    else return null;
  }
  
  public Object getSelectedRow()
  {
    return null;
  }
  
  public void preStore() throws Exception 
  {
    executeTypeAction("preTabStore");
  }
  
  public void postStore() throws Exception
  {
    executeTypeAction("postTabStore");
  }

  public void preRemove() throws Exception
  {
    Object row = getSelectedRow();
    String typeId = getRowTypeId(row);
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        executeTypeAction("preTabRemove", type);        
      }      
    }
  }
  
  public void postRemove() throws Exception
  {
    Object row = getSelectedRow();    
    String typeId = getRowTypeId(row);
    if (typeId != null)
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        executeTypeAction("postTabRemove", type);        
      }
    }
  }
  
  public void preShow() throws Exception
  {
    if (rowTypeId != null || rootTypeId != null)
    {
      String typeId = (rowTypeId != null ? rowTypeId : rootTypeId);
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        executeTypeAction("preTabShow", type);
        return;
      }
    }
    executeTypeAction("preTabShow");
  }
  
  public void postShow() throws Exception
  {
    if (rowTypeId != null || rootTypeId != null)
    {
      String typeId = (rowTypeId != null ? rowTypeId : rootTypeId);
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        executeTypeAction("postTabShow", type);
        return;
      }
    }
    executeTypeAction("postTabShow");
  }
  
  public void preLoad() throws Exception
  {
    if (rowTypeId != null || rootTypeId != null)
    {
      String typeId = (rowTypeId != null ? rowTypeId : rootTypeId);
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        executeTypeAction("preTabLoad", type);
        return;
      }
    }
    executeTypeAction("preTabLoad");    
  }

  public void postLoad() throws Exception
  {
    if (rowTypeId != null || rootTypeId != null)
    {
      String typeId = (rowTypeId != null ? rowTypeId : rootTypeId);
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        executeTypeAction("postTabLoad", type);
        return;
      }
    }
    executeTypeAction("postTabLoad");    
  }
  
  @Override
  public GroupExtractor getGroupExtractor()
  {
    if (groupBy == null)
      return null;
    else
    {
      if (groupBy.endsWith("TypeId"))
        return new TypeGroupExtractor(groupBy);
      else
        return new DefaultGroupExtractor(groupBy);
    }
  }
  
  public boolean isRowEditable()
  {
    return isRowActionEnabled(DictionaryConstants.WRITE_ACTION);
  }
  
  public boolean isRowRemovable()
  {
    return isRowActionEnabled(DictionaryConstants.DELETE_ACTION);
  }
  
  //Private methods

  private boolean isRowActionEnabled(String actionName)
  {
    if (UserSessionBean.getCurrentInstance().isUserInRole(
      CaseConstants.CASE_ADMIN_ROLE))
      return true;    
    
    Object row = (Object)getValue("#{row}");
    if (row == null) return true;    
    
    String auxTypeId = getRowTypeId(row);
    if (auxTypeId == null) return true;
        
    org.matrix.dic.Type auxType = 
      TypeCache.getInstance().getType(auxTypeId);
    if (auxType == null)
      return true;

    boolean searchAscendants = true;
    while (auxType != null && searchAscendants)
    {
      Set<AccessControl> acls = new HashSet();
      acls.addAll(auxType.getAccessControl());    
      for (AccessControl acl : acls)
      {
        String action = acl.getAction();
        if (actionName.equals(action))
        {
          String roleId = acl.getRoleId();
          if (UserSessionBean.getCurrentInstance().isUserInRole(roleId)) return true;
          searchAscendants = false; 
        }
      }
      String superTypeId = auxType.getSuperTypeId();
      auxType = (superTypeId == null ? null : TypeCache.getInstance().getType(superTypeId));
    }
    return false;
  }  
  
  protected String getIndexedDicProperty(Type type, String propertyName, String defaultValue)
  {    
    if (type != null)
    {
      PropertyDefinition pd = type.getPropertyDefinition(propertyName + getTabSuffix());
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
      {
        return pd.getValue().get(0);
      }
      else if (!"".equals(getTabSuffix()))
      {
        pd = type.getPropertyDefinition(propertyName);
        if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        {
          return pd.getValue().get(0);
        }
      }
    }
    return defaultValue;
  }
    
  protected String getRowTypeId(String propertyName)
  {
    String typeId = getObjectBean().getActualTypeId();
    TypeCache typeCache = TypeCache.getInstance();
    Type type = typeCache.getType(typeId);
    return getIndexedDicProperty(type, propertyName, null);
  }
  
  protected String getRowTypeId(Object row)
  {
    return null;
  }
  
  private String getTabSuffix()
  {
    String value = getSelectedMenuItem().getProperty(TAB_INDEX_PROPERTY);
    return ((value == null || value.equals("1")) ? "" : String.valueOf(value));
  }

  protected class TypeGroupExtractor extends DefaultGroupExtractor
  {
    public TypeGroupExtractor(String typeIdPropertyName)
    {
      super(typeIdPropertyName);
    }

    @Override
    protected String getName(Object view)
    {
      String name = super.getName(view);
      if (name != null)
        return name;
      else
        return NULL_GROUP.getName();
    }
    
    @Override
    protected String getDescription(String keyName)
    {
      if (keyName != null && !keyName.equals(NULL_GROUP.getName()))
      {
        Type type = TypeCache.getInstance().getType(keyName);
        if (type != null)
          return type.getDescription();
        else
          return keyName;
      }
      else
        return NULL_GROUP.getDescription();
    }
  }
}
