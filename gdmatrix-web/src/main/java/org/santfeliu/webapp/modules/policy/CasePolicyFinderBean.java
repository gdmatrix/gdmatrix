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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.policy.CasePolicy;
import org.matrix.policy.CasePolicyFilter;
import org.matrix.policy.CasePolicyView;
import org.matrix.policy.PolicyState;
import org.santfeliu.util.BigList;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.BaseBean;
import org.santfeliu.webapp.NavigatorBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class CasePolicyFinderBean extends BaseBean
{
  private CasePolicyFilter filter = new CasePolicyFilter();
  private List<CasePolicyView> rows;
  private int firstRow;
  private boolean outdated;

  private Map<String, StateMapValue> statesMap;

  private static final String OUTCOME = "/pages/policy/case_policy.xhtml";

  @Inject
  NavigatorBean navigatorBean;

  @Override
  public PolicyObjectBean getObjectBean()
  {
    return null;
  }

  @PostConstruct
  public void init()
  {
    statesMap = new HashMap();
  }

  public String show()
  {
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  public String getContent()
  {
    return OUTCOME;
  }

  public CasePolicyFilter getFilter()
  {
    return filter;
  }

  public void setFilter(CasePolicyFilter filter)
  {
    this.filter = filter;
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

  public Map<String, StateMapValue> getStatesMap()
  {
    return statesMap;
  }

  public void setStatesMap(Map<String, StateMapValue> statesMap)
  {
    this.statesMap = statesMap;
  }

  public void setRowState(PolicyState newValue)
  {
    CasePolicyView casePolicyView =
      (CasePolicyView) getValue("#{row}");
    CasePolicy casePolicy = casePolicyView.getCasePolicy();


    String casePolicyId = casePolicy.getCasePolicyId();
    StateMapValue stateValue = statesMap.get(casePolicyId);
    if (stateValue == null)
      statesMap.put(casePolicyId, new StateMapValue(casePolicy, newValue));
    else if (stateValue.casePolicy.getState() != newValue)
      stateValue.newState = newValue;
    else
      statesMap.remove(casePolicyId);
  }

  public PolicyState getRowState()
  {
    CasePolicyView casePolicyView =
      (CasePolicyView) getValue("#{row}");

    StateMapValue stateValue =
      statesMap.get(casePolicyView.getCasePolicy().getCasePolicyId());
    return stateValue != null ? stateValue.newState :
      casePolicyView.getCasePolicy().getState();
  }

  public boolean isRowStateChanged(CasePolicyView row)
  {
    String casePolicyId = row.getCasePolicy().getCasePolicyId();
    StateMapValue stateValue = statesMap.get(casePolicyId);

    return stateValue != null
      && stateValue.newState != row.getCasePolicy().getState();
  }

  public void changeState() throws Exception
  {
    if (statesMap != null)
    {
      for (Map.Entry<String, StateMapValue> entry : statesMap.entrySet())
      {
        StateMapValue stateValue = entry.getValue();
        if (stateValue != null)
        {
          stateValue.casePolicy.setState(stateValue.newState);
          PolicyModuleBean.getPort(false)
            .storeCasePolicy(stateValue.casePolicy);
        }
      }
      find();
      statesMap.clear();
    }
  }

  public void cancelChanges()
  {
    statesMap.clear();
  }

  public void find()
  {
    doFind(false);
    firstRow = 0;
  }

  public void outdate()
  {
    outdated = true;
  }

  public void update()
  {
    if (outdated)
    {
      doFind(false);
    }
  }

  @Override
  public void clear()
  {
    super.clear();
    filter = new CasePolicyFilter();
    rows = null;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ filter, firstRow, rows, outdated };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] stateArray = (Object[])state;
    filter = (CasePolicyFilter)stateArray[0];
    firstRow = (Integer)stateArray[1];
    rows = (List<CasePolicyView>)stateArray[2];
    outdated = (Boolean)stateArray[3];
  }

  private void doFind(boolean autoLoad)
  {
    try
    {

      rows = new BigList(20, 10)
      {
        @Override
        public int getElementCount()
        {
          try
          {
            return PolicyModuleBean.getPort(false)
              .countCasePolicies(filter);
          }
          catch (Exception ex)
          {
            error(ex);
            return 0;
          }
        }

        @Override
        public List getElements(int firstResult, int maxResults)
        {
          try
          {
            filter.setFirstResult(firstResult);
            filter.setMaxResults(maxResults);
            List<CasePolicyView> casePolicyViewList =
              PolicyModuleBean.getPort(false).findCasePolicyViews(filter);

            return casePolicyViewList;
          }
          catch (Exception ex)
          {
            error(ex);
            return null;
          }
        }
      };

      outdated = false;

      if (autoLoad)
      {
        if (rows.size() == 1)
        {
          navigatorBean.view(rows.get(0).getCasePolicy().getPolicyId());
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public class StateMapValue implements Serializable
  {
    public CasePolicy casePolicy;
    public PolicyState newState;

    public StateMapValue(CasePolicy casePolicy, PolicyState newState)
    {
      this.casePolicy = casePolicy;
      this.newState = newState;
    }
  }

}
