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
package org.santfeliu.policy.ant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.matrix.cases.Case;
import org.matrix.policy.CasePolicy;
import org.matrix.policy.CasePolicyFilter;
import org.matrix.policy.CasePolicyView;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyState;
import static org.matrix.policy.PolicyState.APPROVED;
import static org.matrix.policy.PolicyState.CANCELLED;
import static org.matrix.policy.PolicyState.EXECUTED;
import static org.matrix.policy.PolicyState.EXECUTING;
import static org.matrix.policy.PolicyState.FAILED;
import static org.matrix.policy.PolicyState.PENDENT;

/**
 *
 * @author lopezrj
 */
public class ApplyCasePoliciesTask extends ApplyPoliciesTask
  implements TaskContainer
{
  //Input
  private String caseVar;
  //Output
  private String casePol1Var;
  private String casePol2Var;

  private final ArrayList<Task> tasks = new ArrayList();

  public String getCaseVar()
  {
    return caseVar;
  }

  public void setCaseVar(String caseVar)
  {
    this.caseVar = caseVar;
  }

  public String getCasePol1Var()
  {
    return casePol1Var;
  }

  public void setCasePol1Var(String casePol1Var)
  {
    this.casePol1Var = casePol1Var;
  }

  public String getCasePol2Var()
  {
    return casePol2Var;
  }

  public void setCasePol2Var(String casePol2Var)
  {
    this.casePol2Var = casePol2Var;
  }

  @Override
  public void execute()
  {
    loadPolicyStates();

    Case _case = (Case)getVariable(caseVar);

    if (_case != null)
    {
      PolicyManagerPort port = getPolicyManagerPort();

      List<String> classIds = _case.getClassId();
      log("classIds:" + classIds, Project.MSG_INFO);

      ArrayList<CasePolicy> toKeep = new ArrayList();
      ArrayList<CasePolicy> toRemove = new ArrayList();

      log("analize current CasePolicies...", Project.MSG_INFO);
      // analize current documentPolcies

      CasePolicyFilter filter = new CasePolicyFilter();
      filter.setCaseId(_case.getCaseId());
      List<CasePolicyView> casePolicyViews = port.findCasePolicyViews(filter);

      analizeCurrentCasePolicies(casePolicyViews, _case, toKeep, toRemove);

      // analize new documentPolicies
      log("analize new CasePolicies...", Project.MSG_INFO);
      HashMap<String, WeightedPolicy> policies =
        findPoliciesForClassIds(classIds);
      for (WeightedPolicy policy : policies.values())
      {
        addNewCasePolicy(_case, policy, toKeep, casePolicyViews);
      }
      // cartesian product
      for (int i = 0; i < toKeep.size(); i++)
      {
        for (int j = 0; j < toKeep.size(); j++)
        {
          if (i != j)
          {
            if (casePol1Var != null)
            {
              CasePolicy casePol1 = toKeep.get(i);

              setVariable(casePol1Var, casePol1);

              if (pol1Var != null)
              {
                WeightedPolicy p1 = policies.get(casePol1.getPolicyId());
                if (p1 == null) p1 = getPolicy(casePol1.getPolicyId());

                setVariable(pol1Var, p1);

              }
            }
            if (casePol2Var != null)
            {
              CasePolicy casePol2 = toKeep.get(j);

              setVariable(casePol2Var, casePol2);

              if (pol2Var != null)
              {
                WeightedPolicy p2 = policies.get(casePol2.getPolicyId());
                if (p2 == null) p2 = getPolicy(casePol2.getPolicyId());

                setVariable(pol2Var, p2);
              }
            }

            for (Task task : tasks) task.perform();

          }
        }
      }

      // store docPolicies
      storeCasePolicies(port, toKeep);

      // remove docPolicies
      removeCasePolicies(port, toRemove);
    }
  }

  @Override
  public void addTask(Task task)
  {
    tasks.add(task);
  }

  private void analizeCurrentCasePolicies(List<CasePolicyView> casePolicyViews,
    Case _case, List<CasePolicy> toKeep, List<CasePolicy> toRemove)
  {
    for (CasePolicyView view : casePolicyViews)
    {
      CasePolicy casePolicy = view.getCasePolicy();
      Policy policy = view.getPolicy();

      PolicyState state = casePolicy.getState();
      if (state.equals(EXECUTED) ||
          state.equals(EXECUTING) ||
          state.equals(FAILED) ||
          state.equals(CANCELLED))
      {
        toKeep.add(casePolicy);
      }
      else
      {
        // must evaluate again
        if (evalActivationCondition(_case, null, policy, casePolicyViews))
        {
          String activationDate = evalActivationDate(_case, null,
            policy, casePolicyViews);
          if (activationDate == null)
          {
            toRemove.add(casePolicy);
          }
          else
          {
            toKeep.add(casePolicy);

            casePolicy.setActivationDate(activationDate);
            if (casePolicy.getState().equals(PENDENT) &&
                policy.isAutomaticExecution())
            {
              casePolicy.setState(APPROVED);
            }
          }
        }
        else
        {
          toRemove.add(casePolicy);
        }
      }
    }
  }

  private void addNewCasePolicy(Case _case, Policy policy,
    List<CasePolicy> toKeep, List<CasePolicyView> casePolicyViews)
  {
    log("Trying to apply policy " + policy.getPolicyId(), Project.MSG_INFO);

    if (evalActivationCondition(_case, null, policy, casePolicyViews))
    {
      String activationDate = evalActivationDate(_case, null,
        policy, casePolicyViews);
      if (activationDate != null)
      {
        if (!existsCasePolicy(toKeep, policy.getPolicyId(), activationDate))
        {
          CasePolicy casePolicy = new CasePolicy();
          casePolicy.setActivationDate(activationDate);
          casePolicy.setCaseId(_case.getCaseId());
          casePolicy.setPolicyId(policy.getPolicyId());
          if (policy.isAutomaticExecution())
          {
            casePolicy.setState(APPROVED);
          }
          else
          {
            casePolicy.setState(PENDENT);
          }
          toKeep.add(casePolicy);
        }
      }
    }
  }

  private boolean existsCasePolicy(List<CasePolicy> toKeep,
    String policyId, String activationDate)
  {
    boolean found = false;
    Iterator<CasePolicy> iter = toKeep.iterator();
    while (iter.hasNext() && !found)
    {
      CasePolicy casePolicy = iter.next();
      if (casePolicy.getPolicyId().equals(policyId) &&
        casePolicy.getActivationDate().equals(activationDate)) found = true;
    }
    return found;
  }

  private void storeCasePolicies(PolicyManagerPort port,
    ArrayList<CasePolicy> toStore)
  {
    log("toStore:", Project.MSG_INFO);
    for (CasePolicy cp : toStore)
    {
      port.storeCasePolicy(cp);
      log("Info: store policy " + cp.getPolicyId(), Project.MSG_INFO);
    }
  }

  private void removeCasePolicies(PolicyManagerPort port,
    ArrayList<CasePolicy> toRemove)
  {
    log("toRemove:", Project.MSG_INFO);
    for (CasePolicy dp : toRemove)
    {
      port.removeCasePolicy(dp.getCasePolicyId());
      log("Info: remove policy " + dp.getPolicyId(), Project.MSG_INFO);
    }
  }
}
