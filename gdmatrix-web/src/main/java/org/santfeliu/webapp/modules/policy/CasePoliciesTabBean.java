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
package org.santfeliu.webapp.modules.policy;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.policy.CasePolicy;
import org.matrix.policy.CasePolicyFilter;
import org.matrix.policy.CasePolicyView;
import org.matrix.policy.PolicyState;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.cases.CaseObjectBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import static org.santfeliu.webapp.modules.policy.PolicyModuleBean.getPort;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CasePoliciesTabBean extends TabBean
{
  private List<CasePolicyView> rows;
  private int firstRow;
  private CasePolicy editing; 
  
  @Inject
  private CaseObjectBean caseObjectBean;

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public List<CasePolicyView> getRows()
  {
    return rows;
  }

  public void setRows(List<CasePolicyView> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public CasePolicy getEditing()
  {
    return editing;
  }

  public void setEditing(CasePolicy editing)
  {
    this.editing = editing;
  }
  
  public SelectItem[] getPolicyStateSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.policy.web.resources.PolicyBundle", getLocale());
    return FacesUtils.getEnumSelectItems(PolicyState.class, bundle);
  }

  public String getPolicyState(CasePolicyView row)
  {
    if (row != null && row.getCasePolicy() != null)
    {
      PolicyState state = row.getCasePolicy().getState();
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.policy.web.resources.PolicyBundle", getLocale());
      String key = state.getClass().getName() + "." + state.toString();
      return bundle.getString(key);
    }
    return "";
  }  
  
  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CasePolicyFilter filter = new CasePolicyFilter();
        filter.setCaseId(getObjectId());
        rows = getPort(false).findCasePolicyViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else rows = Collections.EMPTY_LIST;
  }

  @Override
  public void store()
  {
    try
    {
      editing.setCaseId(getObjectId());
      try
      {
        getPort(false).storeCasePolicy(editing);
      }
      catch (Exception ex)
      {
        throw new Exception("INVALID_OPERATION");
      }
      load();
      editing = null;
      growl("STORE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }  
  
  public void create()
  {
    editing = new CasePolicy();
  }

  public void edit(CasePolicyView casePolicyView)
  {
    if (casePolicyView != null)
    {
      try
      {
        editing = getPort(false)
          .loadCasePolicy(casePolicyView.getCasePolicy().getCasePolicyId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }

  public void remove(CasePolicyView casePolicyView)
  {
    if (casePolicyView != null)
    {
      try
      {
        String casePolicyId = casePolicyView.getCasePolicy().getCasePolicyId();
        getPort(false).removeCasePolicy(casePolicyId);
        load();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CasePolicy)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
}
