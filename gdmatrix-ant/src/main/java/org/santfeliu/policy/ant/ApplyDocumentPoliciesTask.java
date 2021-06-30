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
import org.matrix.doc.Document;
import org.matrix.policy.DocumentPolicy;
import org.matrix.policy.DocumentPolicyFilter;
import org.matrix.policy.DocumentPolicyView;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyState;

/**
 *
 * @author lopezrj
 */
public class ApplyDocumentPoliciesTask extends ApplyPoliciesTask
  implements TaskContainer
{
  //Input
  private String docVar;
  private String caseVar;
  //Output
  private String docPol1Var;
  private String docPol2Var;
  private String keepDocPolVar;

  private ArrayList<Task> tasks = new ArrayList();

  public String getDocVar()
  {
    return docVar;
  }

  public void setDocVar(String docVar)
  {
    this.docVar = docVar;
  }

  public String getCaseVar()
  {
    return caseVar;
  }

  public void setCaseVar(String caseVar)
  {
    this.caseVar = caseVar;
  }

  public String getDocPol1Var()
  {
    return docPol1Var;
  }

  public void setDocPol1Var(String docPol1Var)
  {
    this.docPol1Var = docPol1Var;
  }

  public String getDocPol2Var()
  {
    return docPol2Var;
  }

  public void setDocPol2Var(String docPol2Var)
  {
    this.docPol2Var = docPol2Var;
  }

  public String getKeepDocPolVar()
  {
    return keepDocPolVar;
  }

  public void setKeepDocPolVar(String keepDocPolVar)
  {
    this.keepDocPolVar = keepDocPolVar;
  }

  @Override
  public void execute()
  {
    loadPolicyStates();

    Document document = (Document)getVariable(docVar);

    if (document != null)
    {
      Case _case = null;
      if (caseVar != null) _case = (Case)getVariable(caseVar);

      PolicyManagerPort port = getPolicyManagerPort();

      List<String> classIds = document.getClassId();
      log("classIds:" + classIds, Project.MSG_INFO);

      ArrayList<DocumentPolicy> toKeep = new ArrayList();
      ArrayList<DocumentPolicy> toRemove = new ArrayList();

      log("analize current DocumentPolicies...", Project.MSG_INFO);
      // analize current documentPolcies
      analizeCurrentDocumentPolicies(port, document, _case, toKeep, toRemove);

      // analize new documentPolicies
      log("analize new DocumentPolicies...", Project.MSG_INFO);
      HashMap<String, WeightedPolicy> policies =
        findPoliciesForClassIds(classIds);
      for (WeightedPolicy policy : policies.values())
      {
        addNewDocumentPolicy(document, _case, policy, toKeep);
      }
      // cartesian product
      for (int i = 0; i < toKeep.size(); i++)
      {
        for (int j = 0; j < toKeep.size(); j++)
        {
          if (i != j)
          {
            if (docPol1Var != null)
            {
              DocumentPolicy docPol1 = toKeep.get(i);

              setVariable(docPol1Var, docPol1);

              if (pol1Var != null)
              {
                WeightedPolicy p1 = policies.get(docPol1.getPolicyId());
                if (p1 == null) p1 = getPolicy(docPol1.getPolicyId());
                                
                setVariable(pol1Var, p1);

              }
            }
            if (docPol2Var != null)
            {
              DocumentPolicy docPol2 = toKeep.get(j);

              setVariable(docPol2Var, docPol2);

              if (pol2Var != null)
              {
                WeightedPolicy p2 = policies.get(docPol2.getPolicyId());
                if (p2 == null) p2 = getPolicy(docPol2.getPolicyId());
                
                setVariable(pol2Var, p2);

              }
            }
            
            for (Task task : tasks) task.perform();  

          }
        }
      }

      // store docPolicies
      storeDocumentPolicies(port, toKeep);

      // remove docPolicies
      removeDocumentPolicies(port, toRemove);
      
      setVariable(keepDocPolVar, toKeep);
    }
  }

  public void addTask(Task task)
  {
    tasks.add(task);
  }

  private void analizeCurrentDocumentPolicies(PolicyManagerPort port,
    Document document, Case _case, List<DocumentPolicy> toKeep,
    List<DocumentPolicy> toRemove)
  {
    DocumentPolicyFilter filter = new DocumentPolicyFilter();
    filter.setDocId(document.getDocId());
    List<DocumentPolicyView> views = port.findDocumentPolicyViews(filter);
    for (DocumentPolicyView view : views)
    {
      DocumentPolicy docPolicy = view.getDocPolicy();
      Policy policy = view.getPolicy();
      if (evalActivationCondition(_case, document, policy))
      {
        if (docPolicy.getState().equals(PolicyState.EXECUTED))
        {
          toKeep.add(docPolicy);
        }
        else
        {
          String activationDate = evalActivationDate(_case, document, policy);
          if (activationDate == null)
          {
            toRemove.add(docPolicy);
          }
          else
          {
            docPolicy.setActivationDate(activationDate);
            if (docPolicy.getState().equals(PolicyState.PENDENT) && 
                policy.isAutomaticExecution())
            {
              docPolicy.setState(PolicyState.APPROVED);
            }
            toKeep.add(docPolicy);
          }
        }
      }
      else
      {
        toRemove.add(docPolicy);
      }
    }
  }

  private void addNewDocumentPolicy(Document document, Case _case,
    Policy policy, List<DocumentPolicy> toKeep)
  {
    if (evalActivationCondition(null, document, policy))
    {
      String activationDate = evalActivationDate(_case, document, policy);
      if (activationDate != null)
      {
        if (!existsDocumentPolicy(toKeep, policy.getPolicyId(), activationDate))
        {
          DocumentPolicy docPolicy = new DocumentPolicy();
          docPolicy.setActivationDate(activationDate);
          docPolicy.setDocId(document.getDocId());
          docPolicy.setPolicyId(policy.getPolicyId());
          if (policy.isAutomaticExecution())
          {
            docPolicy.setState(PolicyState.APPROVED);
          }
          else
          {
            docPolicy.setState(PolicyState.PENDENT);
          }
          toKeep.add(docPolicy);
        }
      }
    }
  }

  private boolean existsDocumentPolicy(List<DocumentPolicy> toKeep,
    String policyId, String activationDate)
  {
    boolean found = false;
    Iterator<DocumentPolicy> iter = toKeep.iterator();
    while (iter.hasNext() && !found)
    {
      DocumentPolicy docPolicy = iter.next();
      if (docPolicy.getPolicyId().equals(policyId) &&
        docPolicy.getActivationDate().equals(activationDate)) found = true;
    }
    return found;
  }

  private void storeDocumentPolicies(PolicyManagerPort port, 
    ArrayList<DocumentPolicy> toStore)
  {    
    log("toStore:", Project.MSG_INFO);
    for (DocumentPolicy dp : toStore)
    {
      port.storeDocumentPolicy(dp);
      log("Info: store policy " + dp.getPolicyId(), Project.MSG_INFO);
    }
  }

  private void removeDocumentPolicies(PolicyManagerPort port, 
    ArrayList<DocumentPolicy> toRemove)
  {
    log("toRemove:", Project.MSG_INFO);
    for (DocumentPolicy dp : toRemove)
    {
      port.removeDocumentPolicy(dp.getDocPolicyId());
      log("Info: remove policy " + dp.getPolicyId(), Project.MSG_INFO);
    }
  }
}
