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
package org.santfeliu.dic.web;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.security.AccessControl;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.security.web.ACLPageBean;

/**
 *
 * @author realor
 */
public class TypeACLBean extends ACLPageBean
{

  @Override
  public String show()
  {
    return "type_acl";
  }

  public String searchRole()
  {
    return getControllerBean().searchObject("Role",
      "#{typeACLBean.editingAccessControlItem.accessControl.roleId}");
  }
  
  public List<SelectItem> getActions()
  {
    List<SelectItem> items;
    if (!isNew())
    {
      String typeId = getObjectId();
      // get actions for that type
      items = DictionaryConfigBean.getActionSelectItems(typeId);
    }
    else // return standard actions
    {
      items = new ArrayList<SelectItem>();
      for (String action : DictionaryConstants.standardActions)
      {
        items.add(new SelectItem(action,
          DictionaryConfigBean.getLocalizedAction(action)));
      }
    }
    items.add(new SelectItem(
      DictionaryConstants.DERIVE_DEFINITION_ACTION,
      DictionaryConfigBean.getLocalizedAction(
        DictionaryConstants.DERIVE_DEFINITION_ACTION)));

    items.add(new SelectItem(
      DictionaryConstants.MODIFY_DEFINITION_ACTION,
      DictionaryConfigBean.getLocalizedAction(
        DictionaryConstants.MODIFY_DEFINITION_ACTION)));

    return items;
  }

  public List<AccessControl> getTypeRows()
  {
    return null;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  protected List<AccessControl> getMainAccessControlList()
  {
    TypeMainBean typeMainBean = (TypeMainBean)getBean("typeMainBean");
    return typeMainBean.getType().getAccessControl();
  }

}
