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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.Type;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.web.DictionaryConfigBean;
import org.santfeliu.security.web.ACLPageBean;

/**
 *
 * @author blanquepa
 */
public class DocumentACLBean extends ACLPageBean
{
  private String currentTypeId;

  @Override
  public String show()
  {
    return "document_acl";
  }

  public String searchRole()
  {
    return getControllerBean().searchObject("Role",
      "#{documentACLBean.editingAccessControlItem.accessControl.roleId}");
  }

  public List<SelectItem> getActions()
  {
    if (!isNew())
     return DictionaryConfigBean.getActionSelectItems(
        DictionaryConstants.DOCUMENT_TYPE);
    else return Collections.EMPTY_LIST;
  }

  public List<AccessControl> getTypeRows()
  {
    if (getMainTypeId() == null)
    {
      typeRows = null;
    }
    else
    {
      if (typeRows == null || checkTypeChange())
      {
        typeRows = new ArrayList<AccessControl>();
        try
        {
          DictionaryManagerPort port = DictionaryConfigBean.getPort();
          Type type = port.loadType(currentTypeId);
          typeRows = type.getAccessControl();
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
    }
    return typeRows;
  }

  protected List<AccessControl> getMainAccessControlList()
  {
    DocumentMainBean documentMainBean = 
      (DocumentMainBean)getBean("documentMainBean");
    return documentMainBean.getDocument().getAccessControl();
  }

  private boolean checkTypeChange()
  {
    boolean result = !getMainTypeId().equals(currentTypeId);
    if (result)
    {
      currentTypeId = getMainTypeId();
      typeRows = null;
    }
    return result;
  }

  private String getMainTypeId()
  {
    DocumentMainBean documentMainBean =
      (DocumentMainBean)getBean("documentMainBean");
    return documentMainBean.getCurrentTypeId();
  }

}
