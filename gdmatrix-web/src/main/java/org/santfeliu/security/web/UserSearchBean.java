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
import org.matrix.security.User;
import org.matrix.security.UserFilter;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class UserSearchBean extends BasicSearchBean
{
  private UserFilter filter;
  private String userIdInput;
  
  public UserSearchBean()
  {
    filter = new UserFilter();
  }

  public UserFilter getFilter()
  {
    return filter;
  }

  public void setFilter(UserFilter filter)
  {
    this.filter = filter;
  }

  public String getUserIdInput()
  {
    return userIdInput;
  }

  public void setUserIdInput(String userIdInput)
  {
    this.userIdInput = userIdInput;
  }
  
  @Override
  public int countResults()
  {
    try
    {
      setFilterUserId();
      return SecurityConfigBean.getPort().countUsers(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      setFilterUserId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return SecurityConfigBean.getPort().findUsers(filter);
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
    return "user_search";
  }

  public String selectUser()
  {
    User row = (User)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String userId = row.getUserId();
    return getControllerBean().select(userId);
  }

  public String showUser()
  {
    return getControllerBean().showObject("User", 
      (String)getValue("#{row.userId}"));
  }

  private void setFilterUserId()
  {
    filter.getUserId().clear();
    for (String userId : userIdInput.split(";"))
    {
      filter.getUserId().add(userId);
    }
  }

}
