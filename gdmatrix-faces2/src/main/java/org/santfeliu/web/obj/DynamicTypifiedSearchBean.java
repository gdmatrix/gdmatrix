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


import java.util.List;
import java.util.Map;
import java.util.Set;
import org.matrix.dic.Property;
import org.matrix.dic.TypeFilter;
import org.santfeliu.dic.web.TypeSearchBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.form.Form;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.util.ResultsManager;
import org.santfeliu.web.obj.util.DynamicFormsManager;
import org.santfeliu.web.obj.util.FormFilter;

/**
 *
 * @author blanquepa
 */
public abstract class DynamicTypifiedSearchBean extends BasicSearchBean 
  implements DynamicPropertiesSearch, RoleProtected
{
  @CMSProperty
  private static final String SEARCH_PROPERTY_NAME = "searchPropertyName";
  @CMSProperty
  private static final String SEARCH_PROPERTY_VALUE = "searchPropertyValue";
  
  protected ResultsManager resultsManager;
  protected DynamicFormsManager dynamicFormsManager;
  
  public DynamicTypifiedSearchBean(
    String bundleClassName, String bundlePrefix, String typeIdPropName)
  {
    dynamicFormsManager =
      new DynamicFormsManager(TypeFormBuilder.SEARCH_PREFIX);
    resultsManager =
      new ResultsManager(bundleClassName, bundlePrefix, typeIdPropName);
  }

  public String getCurrentTypeId()
  {
    return dynamicFormsManager.getCurrentTypeId();
  }

  public void setCurrentTypeId(String currentTypeId)
  {
    dynamicFormsManager.setCurrentTypeId(currentTypeId);
  }

  public void refreshForms()
  {
    dynamicFormsManager.refreshForms();
  }

  public Map getData()
  {
    return dynamicFormsManager.getData();
  }

  public void setData(Map data)
  {
    dynamicFormsManager.setData(data);
  }

  public String getSelector()
  {
    return dynamicFormsManager.getSelector();
  }

  public void setSelector(String selector)
  {
    this.dynamicFormsManager.setSelector(selector);
  }

  public boolean isTypeUndefined()
  {
    return dynamicFormsManager.isTypeUndefined();
  }

  public Form getForm()
  {
    try
    {
      return dynamicFormsManager.getForm();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  protected org.santfeliu.dic.Type getSelectedType()
  {
    return dynamicFormsManager.getSelectedType();
  }

  public List<Property> getFormDataAsProperties()
  {
    return dynamicFormsManager.transformFormDataToProperties();
  }

  public void setFormDataFromProperties(List<Property> properties)
  {
    dynamicFormsManager.setFormDataFromProperties(properties, null);
  }

  @Override
  public String refresh()
  {
    dynamicFormsManager.transformFormDataToProperties();
    return super.refresh();
  }

  @Override
  public String search()
  {
    dynamicFormsManager.transformFormDataToProperties();
    return super.search();
  }

  //Columns
  public List<String> getColumnNames()
  {
    if (resultsManager != null)
    {
      resultsManager.setColumns(getSelectedMenuItem().getMid());
      return resultsManager.getColumnNames();
    }
    else
      return null;
  }

  public String getLocalizedColumnName()
  {
    if (resultsManager != null)
      return resultsManager.getLocalizedColumnName();
    else
      return null;
  }

  public String getColumnName()
  {
    if (resultsManager != null)
      return resultsManager.getColumnName();
    else
      return null;
  }

  public Object getColumnValue()
  {
    if (resultsManager != null)
        return resultsManager.getColumnValue();
    else
      return null;
  }

  public boolean isValueEscaped()
  {
    boolean escaped = true;
    if (resultsManager != null && resultsManager.getColumnDefinition() != null)
    {
      escaped =  resultsManager.getColumnDefinition().getRenderer().isValueEscaped();
    }
    return escaped;
  }

  public boolean isLinkColumn()
  {
    return resultsManager.isLinkColumn();
  }

  public boolean isImageColumn()
  {
    return resultsManager.isImageColumn();
  }

  public boolean isCustomColumn()
  {
    return resultsManager.isCustomColumn();
  }

  public boolean isSubmitColumn()
  {
    return resultsManager.isSubmitColumn();
  }

  public boolean isShowParametersOnUrl()
  {
    return (resultsManager.isSubmitColumn() &&
      getProperty(DefaultDetailBean.SHORTCUT_URL_MID) == null);
  }

  public String getColumnStyle()
  {
    if (resultsManager != null)
      return resultsManager.getColumnStyle();
    else
      return null;
  }

  public String getColumnStyleClass()
  {
    if (resultsManager != null)
      return resultsManager.getColumnStyleClass();
    else
      return null;
  }

  public String getColumnDescription()
  {
    if (resultsManager != null)
      return resultsManager.getColumnDescription();
    else
      return null;
  }

  public String getShowLinkUrl(String idParameterName, String idValue)
  {
    String mid = getSelectedMenuItem().getMid();
    String url = "/go.faces?xmid=" + mid + "&" + idParameterName + "=" + idValue;

    return url;
  }

  //Rows
  public abstract String getRowStyleClass();
  
  public String getRowStyleClass(String defaultStyleClass)
  {
    String styleClass = defaultStyleClass;
    if (resultsManager != null && resultsManager.getRowStyleClass() != null)
      styleClass = (styleClass != null ? styleClass + " " : "")
        + resultsManager.getRowStyleClass();

    return styleClass;
  }
  
  protected boolean render(String property, boolean defaultValue)
  {
    String propValue = getProperty(property);
    if (propValue != null)
      return Boolean.valueOf(propValue);
    else
      return defaultValue;
  }
  
  protected boolean render(String propertyName)
  {
    return render(propertyName, true);
  }
  
  public String getSearchPropertyName()
  {
    return SEARCH_PROPERTY_NAME;    
  }
  
  public String getSearchPropertyValue()
  {
    return SEARCH_PROPERTY_VALUE;
  }
  
  public void setSearchDynamicProperties(FormFilter filter)
  {
    String nodePropName = getProperty(getSearchPropertyName());
    List<String> nodePropValue =
      getSelectedMenuItem().getMultiValuedProperty(getSearchPropertyValue());

    int i = 1;

    if (nodePropName == null)
    {
      nodePropName = 
        getProperty(getSearchPropertyName() + ":" + String.valueOf(i));
      nodePropValue =
        getSelectedMenuItem().getMultiValuedProperty(getSearchPropertyValue() 
          + ":" + String.valueOf(i));
    }

    while (nodePropName != null)
    {
      filter.setProperty(nodePropName, nodePropValue);
      nodePropName = 
        getProperty(getSearchPropertyName() + ":" + String.valueOf(i));
      nodePropValue =
        getSelectedMenuItem().getMultiValuedProperty(getSearchPropertyValue() 
          + ":" + String.valueOf(i));

      i++;
    }
  }    
  
  public boolean isEditorUser() throws Exception
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();

    Set userRoles = UserSessionBean.getCurrentInstance().getRoles();
    if (userRoles.contains(getAdminRole()))
      return true;

    List<String> rolesUpdate = mic.getMultiValuedProperty(ROLES_UPDATE_PROPERTY);
    if (rolesUpdate != null && rolesUpdate.size() > 0)
    {
      for (String roleUpdate : rolesUpdate)
      {
        if (userRoles.contains(roleUpdate))
        {
          return true;
        }
      }
      return false;
    }
    else
    {
      return true;
    }
  } 
  
  public boolean isAdminUser()
  {
    Set userRoles = UserSessionBean.getCurrentInstance().getRoles();
    return userRoles.contains(getAdminRole());
  }
  
  public abstract String searchType();
  
  public String searchType(String rootTypeId, String valueBinding)
  {
    TypeSearchBean typeSearchBean = (TypeSearchBean)getBean("typeSearchBean");
    if (typeSearchBean == null)
      typeSearchBean = new TypeSearchBean();

    typeSearchBean.setRootTypeId(rootTypeId);
    typeSearchBean.setFilter(new TypeFilter());
    typeSearchBean.search();

    return getControllerBean().searchObject("Type",
      valueBinding);
  }  

}
