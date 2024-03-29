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
package org.santfeliu.webapp.modules.cases;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.ActionObject;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class CaseObjectBean extends ObjectBean
{
  private Case cas = new Case();
  private String formSelector;

  @Inject
  CaseTypeBean caseTypeBean;

  @Inject
  CaseFinderBean caseFinderBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CASE_TYPE;
  }

  @Override
  public CaseTypeBean getTypeBean()
  {
    return caseTypeBean;
  }

  @Override
  public CaseFinderBean getFinderBean()
  {
    return caseFinderBean;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : cas.getTitle();
  }

  @Override
  public Case getObject()
  {
    return isNew() ? null : cas;
  }

  public Case getCase()
  {
    return cas;
  }

  public void setCase(Case cas)
  {
    this.cas = cas;
  }

  public void setNewClassId(String classId)
  {
    if (!StringUtils.isBlank(classId))
    {
      List<String> currentClassIdList = cas.getClassId();
      if (!currentClassIdList.contains(classId))
      {
        currentClassIdList.add(classId);
      }
    }
  }
  public Date getStartDateTime()
  {
    if (cas != null && cas.getStartDate() != null)
    {
      return getDate(cas.getStartDate(), cas.getStartTime());
    }
    else
    {
      return null;
    }
  }

  public Date getEndDateTime()
  {
    if (cas != null && cas.getEndDate() != null)
    {
      return getDate(cas.getEndDate(), cas.getEndTime());
    }
    else
    {
      return null;
    }
  }

  public void setStartDateTime(Date date)
  {
    if (cas != null)
    {
      if (date == null)
      {
        date = new Date();
      }
      cas.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
      cas.setStartTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }

  public void setEndDateTime(Date date)
  {
    if (date != null && cas != null)
    {
      cas.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
      cas.setEndTime(TextUtils.formatDate(date, "HHmmss"));
    }
    else if (date == null && cas != null)
    {
      cas.setEndDate(null);
      cas.setEndTime(null);
    }
      
  }

  public String getPropertyLabel(String propName, String altName)
  {
    return caseTypeBean.getPropertyLabel(cas, propName, altName);
  }

  /**
   * Not rendered when base Type has "classId" PropertyDefinition with default
   * value set, minium occurrences greater than zero, and read only.
   */
  public boolean isRenderClassId()
  {
    String typeId = getBaseTypeInfo().getBaseTypeId();
    PropertyDefinition pd =
      caseTypeBean.getPropertyDefinition(typeId, "classId");

    return !(pd != null && pd.getValue() != null && pd.getMinOccurs() > 0
      && pd.isReadOnly());
  }

  private Date getDate(String date, String time)
  {
    String dateTime = TextUtils.concatDateAndTime(date, time);
    return TextUtils.parseInternalDate(dateTime);
  }

  @Override
  public void loadObject() throws Exception
  {
    formSelector = null;

    executeAction("preLoad");
    if (!NEW_OBJECT_ID.equals(objectId))
      cas = CasesModuleBean.getPort(false).loadCase(objectId);
    else
    {
      cas = new Case();
      cas.setCaseTypeId(getBaseTypeInfo().getBaseTypeId());
    }
    executeAction("postLoad");
  }

  @Override
  public void storeObject() throws Exception
  {
    executeAction("preStore");
    cas = CasesModuleBean.getPort(false).storeCase(cas);
    setObjectId(cas.getCaseId());
    executeAction("postStore");
    caseFinderBean.outdate();
  }
         
  @Override
  public void removeObject() throws Exception
  {
    executeAction("preRemove");
    CasesModuleBean.getPort(false).removeCase(cas.getCaseId());
    executeAction("postRemove");

    caseFinderBean.outdate();
  }
  
  @Override
  protected void setActionResult(ActionObject result)
  {
    if (result != null)
    {
      if (result.getObject() != null)
        cas = (Case) result.getObject();      
    }
  }  

  @Override
  public Serializable saveState()
  {
    return new Object[] { cas, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.cas = (Case) array[0];
    this.formSelector = (String)array[1];
  }
  
  @Override
  public boolean isEditable()
  {
    if (UserSessionBean.getCurrentInstance().isUserInRole(
      CaseConstants.CASE_ADMIN_ROLE))
      return true;
    
    if (!super.isEditable()) return false; //tab protection

    if (cas == null || cas.getCaseId() == null || cas.getCaseTypeId() == null)
      return true;
    
    Type currentType =
      TypeCache.getInstance().getType(cas.getCaseTypeId());
    if (currentType == null) return true;

    Set<AccessControl> acls = new HashSet();
    acls.addAll(currentType.getAccessControl());
    acls.addAll(cas.getAccessControl());
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
    return false;
  }
  
  public boolean isRowEditable(String rowTypeId)
  {
    return isRowActionEnabled(rowTypeId, DictionaryConstants.WRITE_ACTION);
  }
  
  public boolean isRowRemovable(String rowTypeId)
  {
    return isRowActionEnabled(rowTypeId, DictionaryConstants.DELETE_ACTION);
  }
  
  //Private methods

  private boolean isRowActionEnabled(String rowTypeId, String actionName)
  {
    if (UserSessionBean.getCurrentInstance().isUserInRole(
      CaseConstants.CASE_ADMIN_ROLE))
      return true;    
    
    if (rowTypeId == null) return true;
    
    TypeCache typeCache =  TypeCache.getInstance();
    
    String auxTypeId = rowTypeId;     
    org.matrix.dic.Type auxType = typeCache.getType(auxTypeId);
    if (auxType == null)
      return true;

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();    
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
          if (userSessionBean.isUserInRole(roleId)) return true;
          searchAscendants = false; 
        }
      }
      String superTypeId = auxType.getSuperTypeId();
      auxType = (superTypeId == null ? null : typeCache.getType(superTypeId));
    }
    return false;
  }  

}
