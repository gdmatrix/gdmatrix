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
package org.santfeliu.security.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.Role;
import org.matrix.security.RoleFilter;
import org.matrix.security.SecurityConstants;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class RoleSearchBean extends BasicSearchBean
{
  private List<SelectItem> typeSelectItems;
  private RoleFilter filter;
  private String roleIdInput;
  
  public RoleSearchBean()
  {
    filter = new RoleFilter();
  }

  public RoleFilter getFilter()
  {
    return filter;
  }

  public void setFilter(RoleFilter filter)
  {
    this.filter = filter;
  }

  public String getRoleIdInput()
  {
    return roleIdInput;
  }

  public void setRoleIdInput(String roleIdInput)
  {
    this.roleIdInput = roleIdInput;
  }

  public int countResults()
  {
    try
    {
      setFilterRoleId();
      return SecurityConfigBean.getPort().countRoles(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      setFilterRoleId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return SecurityConfigBean.getPort().findRoles(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @CMSAction
  @Override
  public String show()
  {
    return "role_search";
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      TypeBean typeBean = (TypeBean)getBean("typeBean");
      String[] actions = {DictionaryConstants.READ_ACTION};
      typeSelectItems = typeBean.getAllSelectItems(DictionaryConstants.ROLE_TYPE,
        SecurityConstants.SECURITY_ADMIN_ROLE, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  }
  
  public String selectRole()
  {
    Role row = (Role)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String roleId = row.getRoleId();
    return getControllerBean().select(roleId);
  }

  public String showRole()
  {
    return getControllerBean().showObject("Role",
      (String)getValue("#{row.roleId}"));
  }

  private void setFilterRoleId()
  {
    filter.getRoleId().clear();
    for (String roleId : roleIdInput.split(";"))
    {
      filter.getRoleId().add(roleId);
    }
  }

}
