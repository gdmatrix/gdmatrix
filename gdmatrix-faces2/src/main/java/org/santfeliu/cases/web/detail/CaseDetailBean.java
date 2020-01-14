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
package org.santfeliu.cases.web.detail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.AccessControl;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.ObjectDumper;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.DetailBean;

/**
 *
 * @author blanquepa
 */
public class CaseDetailBean extends DetailBean
{
  private Case cas;

  public CaseDetailBean()
  {
  }

  public CaseDetailBean(Case cas)
  {
    this.cas = cas;
  }

  public Case getCase()
  {
    return cas;
  }
  
  public Map getCaseAsMap()
  {
    ObjectDumper dumper = new ObjectDumper();
    Type type = TypeCache.getInstance().getType(cas.getCaseTypeId());
    if (type != null)
      return dumper.dumpAsMap(cas, type);
    else
      return Collections.EMPTY_MAP;
  }

  public void setCase(Case cas)
  {
    this.cas = cas;
  }
  
  public String getCaseId()
  {
    if (cas != null)
      return cas.getCaseId();
    else
      return null;
  }

  public String show(String caseId)
  {
    try
    {
      cas = CaseConfigBean.getPort().loadCase(caseId);
      loadPanels();
      return "case_detail";
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public String editCase()
  {
    ControllerBean controllerBean = getControllerBean();
    return controllerBean.showObject(
      DictionaryConstants.CASE_TYPE, getCaseId(), ControllerBean.EDIT_VIEW);
  }

  public String getShortcutURLObjectIdParameter()
  {
    return "caseid=" + getCaseId();
  }

  public boolean isEditable() throws Exception
  {
    if (cas == null || cas.getCaseId() == null)
      return true;

    if (UserSessionBean.getCurrentInstance().isUserInRole(
      CaseConstants.CASE_ADMIN_ROLE))
      return true;

    TypeCache typeCache = TypeCache.getInstance();
    if (cas.getCaseTypeId() != null && cas.getCaseTypeId().length() > 0)
    {
      Type currentType = typeCache.getType(cas.getCaseTypeId());
      if (currentType == null)
        return true;

      Set<AccessControl> acls = new HashSet();
      acls.addAll(currentType.getAccessControl());
      acls.addAll(cas.getAccessControl());

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
    }

    return false;
  }

}
