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
package org.santfeliu.policy.web;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import org.matrix.cases.Case;
import org.matrix.dic.DictionaryConstants;
import org.matrix.policy.CasePolicy;
import org.matrix.policy.CasePolicyFilter;
import org.matrix.policy.CasePolicyView;
import org.matrix.policy.PolicyState;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class CasePolicySearchBean extends BasicSearchBean
{
  private Map<String, StateMapValue> statesMap;
  private CasePolicyFilter filter;
  private SelectItem[] stateSelectItems;
  private Locale locale;

  public CasePolicySearchBean()
  {
    locale = getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.policy.web.resources.PolicyBundle", locale);
    this.stateSelectItems =
      FacesUtils.getEnumSelectItems(PolicyState.class, bundle);

    statesMap = new HashMap();
    filter = new CasePolicyFilter();
  }

  public CasePolicyFilter getFilter()
  {
    return filter;
  }

  public void setFilter(CasePolicyFilter filter)
  {
    this.filter = filter;
  }

  public Map<String, StateMapValue> getStatesMap()
  {
    return statesMap;
  }

  public void setStatesMap(Map<String, StateMapValue> statesMap)
  {
    this.statesMap = statesMap;
  }

  public SelectItem[] getStateSelectItems()
  {
    if (!getLocale().equals(locale))
    {
      locale = getLocale();
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.policy.web.resources.PolicyBundle", locale);
      this.stateSelectItems =
        FacesUtils.getEnumSelectItems(PolicyState.class, bundle);
    }

    return this.stateSelectItems;
  }

  public void setStateSelectItems(SelectItem[] stateSelectItems)
  {
    this.stateSelectItems = stateSelectItems;
  }

  public PolicyState getRowState()
  {
    CasePolicyView casePolicyView =
      (CasePolicyView) getValue("#{row}");
    String casePolicyId = casePolicyView.getCasePolicy().getCasePolicyId();

    StateMapValue stateMapValue = statesMap.get(casePolicyId);
    return (stateMapValue.newValue != null ?
      stateMapValue.newValue : stateMapValue.oldCasePolicy.getState());
  }

  public void setRowState(PolicyState newValue)
  {
    CasePolicyView casePolicyView =
      (CasePolicyView) getValue("#{row}");
    String casePolicyId = casePolicyView.getCasePolicy().getCasePolicyId();

    StateMapValue stateMapValue = statesMap.get(casePolicyId);
    if (newValue.equals(stateMapValue.oldCasePolicy.getState()))
    {
      stateMapValue.newValue = null;
    }
    else
    {
      stateMapValue.newValue = newValue;
    }
  }

  public boolean isRowStateChanged()
  {
    CasePolicyView casePolicyView =
      (CasePolicyView) getValue("#{row}");
    String casePolicyId = casePolicyView.getCasePolicy().getCasePolicyId();
    StateMapValue stateMapValue = statesMap.get(casePolicyId);

    return (stateMapValue != null && stateMapValue.newValue != null);
  }

  public Date getRowActivationDate()
  {
    CasePolicyView casePolicyView =
      (CasePolicyView) getValue("#{row}");

    String activationDate = casePolicyView.getCasePolicy().getActivationDate();

    return TextUtils.parseInternalDate(activationDate);
  }

  public String getCaseClassId()
  {
    CasePolicyView casePolicyView =
      (CasePolicyView) getValue("#{row}");
    Case cas = casePolicyView.getCase();
    if (cas == null)
      return null;

    List<String> classIds = cas.getClassId();
    if (classIds != null && classIds.size() > 0)
      return TextUtils.collectionToString(classIds);
    else return null;
  }

  public int countResults()
  {
    try
    {
      return PolicyConfigBean.getPort().countCasePolicies(filter);
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
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);

      List<CasePolicyView> casePolicyViewList =
        PolicyConfigBean.getPort().findCasePolicyViews(filter);
      for (CasePolicyView casePolicyView : casePolicyViewList)
      {
        String key = casePolicyView.getCasePolicy().getCasePolicyId();
        CasePolicy casePolicy = casePolicyView.getCasePolicy();

        StateMapValue stateValue = statesMap.get(key);
        if (stateValue == null || stateValue.newValue == null)
        {
          stateValue = new StateMapValue();
          stateValue.oldCasePolicy = casePolicy;
          statesMap.put(key, stateValue);
        }
      }
      return casePolicyViewList;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  @CMSAction
  public String show()
  {
    return "case_policy_search";
  }

  public String showCase()
  {
    CasePolicyView casePolicyView =
      (CasePolicyView) getValue("#{row}");
    String caseId = casePolicyView.getCase().getCaseId();
    return getControllerBean().showObject(DictionaryConstants.CASE_TYPE, caseId);
  }

  public String showPolicy()
  {
    return getControllerBean().showObject(DictionaryConstants.POLICY_TYPE,
      (String)getValue("#{row.policy.policyId}"));
  }
  
  public String searchPolicy()
  {
    return getControllerBean().searchObject("Policy",
      "#{casePolicySearchBean.filter.policyId}");
  }    

  public String changeState() throws Exception
  {
    if (statesMap != null)
    {
      for (Map.Entry<String, StateMapValue> entry : statesMap.entrySet())
      {
        String casePolicyId = entry.getKey();
        StateMapValue value = entry.getValue();
        if (value != null && value.newValue != null)
        {
          String line = "Policy " + casePolicyId +
            ": State changed from " + value.oldCasePolicy.getState() +
            " to " + value.newValue;
          FacesMessage facesMessage =
            new FacesMessage(FacesMessage.SEVERITY_INFO, line, null);
          getFacesContext().addMessage(null, facesMessage);

          value.oldCasePolicy.setState(value.newValue);
          PolicyConfigBean.getPort().storeCasePolicy(value.oldCasePolicy);
          value.newValue = null;
        }
      }
    }
    return search();
  }

  public String cancelChanges()
  {
    statesMap.clear();
    return search();
  }

  public class StateMapValue implements Serializable
  {
    public CasePolicy oldCasePolicy;
    public PolicyState newValue;
  }
}
