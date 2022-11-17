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
package org.matrix.pf.web;

import java.util.List;
import javax.inject.Named;
import org.matrix.web.Describable;
import org.matrix.web.WebUtils;
import static org.matrix.web.WebUtils.OBJECT_TYPEID_PROPERTY;
import org.santfeliu.cms.web.NodeEditBean;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
@Named("webBacking")
public class WebBacking extends FacesBean
{   
  
  protected MenuItemCursor getSelectedMenuItem()
  {
    return UserSessionBean.getCurrentInstance().getMenuModel()
      .getSelectedMenuItem();
  }
  
  protected String getMenuItemTypeId(MenuItemCursor mic)
  {
    return mic.getProperty(OBJECT_TYPEID_PROPERTY);
  }  
  
  protected String getMenuItemTypeId()
  {
    return getMenuItemTypeId(getSelectedMenuItem());
  } 
    
  protected String getBackingName()
  {
    return getClass().getAnnotation(Named.class).value();  
  }
  
  protected String getMenuItemProperty(String name)
  {
    return WebUtils.getMenuItemProperty(name);
  }
  
  protected List<String> getMultivaluedMenuItemProperty(String name)
  {
    return WebUtils.getMultivaluedMenuItemProperty(name);    
  }
  
  protected String getDirectMenuItemProperty(String name)
  {
    return WebUtils.getDirectMenuItemProperty(name);
  }  

  protected List<String> getDirectMultivaluedMenuItemProperty(String name)
  {
    return WebUtils.getDirectMultivaluedMenuItemProperty(name);
  }
  
  protected String getProperty(String name, boolean direct)
  {
    return (direct ? 
      getDirectMenuItemProperty(name) : 
      getMenuItemProperty(name));
  }
  
  protected List<String> getMultivaluedProperty(String name, boolean direct)
  {
    return (direct ? 
      getDirectMultivaluedMenuItemProperty(name) : 
      getMultivaluedMenuItemProperty(name));
  }  

  protected NodeEditBean getNodeEditBean()
  {
    return (NodeEditBean)getBean("nodeEditBean");
  }
  
  protected String getDescription(Describable describable, String objectId)
  {
    return ObjectDescriptions.getDescription(describable, objectId);
  }    
     
}