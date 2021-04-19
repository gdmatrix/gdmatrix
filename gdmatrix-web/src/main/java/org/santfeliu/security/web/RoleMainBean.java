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

import static org.matrix.dic.DictionaryConstants.ROLE_TYPE;
import org.matrix.security.Role;
import static org.matrix.security.SecurityConstants.SECURITY_ADMIN_ROLE;
import org.santfeliu.web.obj.TypifiedPageBean;

/**
 *
 * @author realor
 */
public class RoleMainBean extends TypifiedPageBean
{
  private Role role;

  public RoleMainBean()
  {
    super(ROLE_TYPE, SECURITY_ADMIN_ROLE);
    load();
  }

  //Accessors
  public Role getRole()
  {
    return role;
  }

  public void setRole(Role role)
  {
    this.role = role;
  }

  public boolean isRenderShowTypeButton()
  {
    return getRole().getRoleTypeId() != null &&
      getRole().getRoleTypeId().trim().length() > 0;
  }

  //Actions  
  
  public String showType()
  {
    return getControllerBean().showObject("Type",
      getRole().getRoleTypeId());
  }

  @Override
  public String show()
  {
    return "role_main";
  }

  @Override
  public String store()
  {
    try
    {
      role = SecurityConfigBean.getPort().storeRole(role);
      setObjectId(role.getRoleId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  private void load()
  {
    if (isNew())
    {
      role = new Role();
    }
    else
    {
      try
      {
        role = SecurityConfigBean.getPort().loadRole(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        role = new Role();
      }
    }
  }
}
