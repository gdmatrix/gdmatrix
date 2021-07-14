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

import org.santfeliu.ant.ws.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.tools.ant.Project;
import org.matrix.cases.Case;
import org.matrix.classif.Class;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.classif.ClassificationManagerService;
import org.matrix.doc.Document;
import org.matrix.policy.ClassPolicy;
import org.matrix.policy.ClassPolicyFilter;
import org.matrix.policy.ClassPolicyView;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.PolicyManagerService;
import org.matrix.policy.PolicyState;
import org.matrix.util.WSEndpoint;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.script.DefaultScriptable;

/**
 *
 * @author lopezrj
 */
public abstract class ApplyPoliciesTask extends WSTask
{
  //Output
  protected String pol1Var;
  protected String pol2Var;
    
  public String getPol1Var()
  {
    return pol1Var;
  }

  public void setPol1Var(String pol1Var)
  {
    this.pol1Var = pol1Var;
  }

  public String getPol2Var()
  {
    return pol2Var;
  }

  public void setPol2Var(String pol2Var)
  {
    this.pol2Var = pol2Var;
  }

  protected HashMap<String, WeightedPolicy> findPoliciesForClassIds(
    List<String> classIds)
  {
    HashMap<String, WeightedPolicy> policies =
      new HashMap<String, WeightedPolicy>();
    if (classIds.size() > 0)
    {
      for (int i = classIds.size() - 1; i >= 0; i--)
      {
        String classId = classIds.get(i);
        try
        {
          List<String> classPath = getClassPath(classId);
          for (int j = 0; j < classPath.size(); j++)
          {
            int weight = j + 1000 / (i + 1);
            findPoliciesForClassId(classPath.get(j), policies, weight);
          }
        }
        catch (Exception ex)
        {
          //Avoid CLASS_NOT_FOUND interrumpt when class doesn't exists.
        }
      }
    }
    else
    {
      findPoliciesForClassId("0000", policies, 0);
    }
    return policies;
  }

  protected void findPoliciesForClassId(String classId,
    HashMap<String, WeightedPolicy> policies, int weight)
  {
    // look for policies of classPath
    PolicyManagerPort policyPort = getPolicyManagerPort();
    ClassPolicyFilter filter = new ClassPolicyFilter();
    filter.setClassId(classId);
    List<ClassPolicyView> views = policyPort.findClassPolicyViews(filter);
    for (ClassPolicyView view : views)
    {
      ClassPolicy classPolicy = view.getClassPolicy();
      String startDate = classPolicy.getStartDate();
      String endDate = classPolicy.getEndDate();
      String nowDate = TextUtils.formatDate(new Date(), "yyyyMMdd");
      if (startDate != null && nowDate.compareTo(startDate) < 0)
      {
        // Do not apply: nowDate < startDate
      }
      else if (endDate != null && endDate.compareTo(nowDate) < 0)
      {
        // Do not apply: endDate < nowDate
      }
      else
      {
        WeightedPolicy policy = new WeightedPolicy(view.getPolicy(), weight);
        String policyId = policy.getPolicyId();
        policies.put(policyId, policy);
      }
    }
  }

  protected List<String> getClassPath(String classId)
  {
    ArrayList<String> classPath = new ArrayList();
    ClassificationManagerPort classifPort = getClassificationManagerPort();

    while (classId != null)
    {
      Class classObject = classifPort.loadClass(classId, null);
      classPath.add(0, classId);
      classId = classObject.getSuperClassId();
    }
    return classPath;
  }

  protected String evalActivationDate(
    Case _case, Document document, Policy policy)
  {
    String activationDate = null;
    String activationDateExpression = policy.getActivationDateExpression();
    if (activationDateExpression != null &&
      activationDateExpression.trim().length() > 0)
    {
      org.mozilla.javascript.Context cx =
        ContextFactory.getGlobal().enterContext();
      try
      {
        Scriptable scriptable = new DefaultScriptable(cx);
        scriptable.put("c", scriptable, _case);
        scriptable.put("d", scriptable, document);
        if (document != null)
        {
          scriptable.put("o", scriptable, document);
        }
        else
        {
          scriptable.put("o", scriptable, _case);
        }
        Object result = cx.evaluateString(scriptable,
          activationDateExpression, "<expr>", 1, null);
        if (result != null)
        {
          Date date = TextUtils.parseInternalDate(
            org.mozilla.javascript.Context.toString(result));
          if (date != null)
          {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            activationDate = df.format(date);
          }
        }
      }
      catch (Exception ex)
      {        
        log(ex.toString(), Project.MSG_WARN);
      }
      finally
      {
        org.mozilla.javascript.Context.exit();
      }
    }
    return activationDate;
  }

  protected boolean evalActivationCondition(
    Case _case, Document document, Policy policy)
  {
    boolean apply = true;
    String activationCondition = policy.getActivationCondition();
    if (activationCondition != null && activationCondition.trim().length() > 0)
    {
      org.mozilla.javascript.Context cx =
        ContextFactory.getGlobal().enterContext();
      try
      {
        Scriptable scriptable = new DefaultScriptable(cx);
        scriptable.put("c", scriptable, _case);
        scriptable.put("d", scriptable, document);
        if (document != null)
        {
          scriptable.put("o", scriptable, document);
        }
        else
        {
          scriptable.put("o", scriptable, _case);
        }
        Object result = cx.evaluateString(scriptable,
          activationCondition, "<expr>", 1, null);
        apply = Boolean.TRUE.equals(result);
      }
      catch (Exception ex)
      {        
        log(ex.toString(), Project.MSG_WARN);
        apply = false;
      }
      finally
      {
        org.mozilla.javascript.Context.exit();
      }
    }
    return apply;
  }

  protected WeightedPolicy getPolicy(String policyId)
  {
    PolicyManagerPort port = getPolicyManagerPort();
    return new WeightedPolicy(port.loadPolicy(policyId), 100000);
  }

  protected void loadPolicyStates()
  {
    PolicyState[] states = PolicyState.values();
    for (PolicyState state : states)
    {
      setVariable(state.toString(), state);
    }
  }

  protected ClassificationManagerPort getClassificationManagerPort()
  {
    WSEndpoint endpoint = getEndpoint(ClassificationManagerService.class);
    return endpoint.getPort(ClassificationManagerPort.class,
      getUsername(), getPassword());
  }

  protected PolicyManagerPort getPolicyManagerPort()
  {
    WSEndpoint endpoint = getEndpoint(PolicyManagerService.class);
    return endpoint.getPort(PolicyManagerPort.class,
      getUsername(), getPassword());
  }

}
