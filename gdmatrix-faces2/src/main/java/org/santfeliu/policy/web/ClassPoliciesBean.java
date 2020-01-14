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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.policy.ClassPolicy;
import org.matrix.policy.ClassPolicyFilter;
import org.matrix.policy.ClassPolicyView;
import org.matrix.policy.PolicyManagerPort;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.classif.web.ClassBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author realor
 */
public class ClassPoliciesBean extends PageBean
{
  private ClassPolicy editingClassPolicy;
  private List<ClassPolicyView> rows;
  private boolean renderInheritedPolicies = false;

  public ClassPoliciesBean()
  {
    load();
  }

  public ClassPolicy getEditingClassPolicy()
  {
    return editingClassPolicy;
  }

  public void setEditingClassPolicy(ClassPolicy editingClassPolicy)
  {
    this.editingClassPolicy = editingClassPolicy;
  }

  public List<ClassPolicyView> getRows()
  {
    return rows;
  }

  public void setRows(List<ClassPolicyView> rows)
  {
    this.rows = rows;
  }

  public Date getRowStartDate()
  {
    Date result = null;
    ClassPolicyView ClassPolicyView = (ClassPolicyView) getValue("#{row}");
    if (ClassPolicyView != null && ClassPolicyView.getClassPolicy() != null)
    {
      result = TextUtils.parseInternalDate(
        ClassPolicyView.getClassPolicy().getStartDate());
    }
    return result;
  }

  public Date getRowEndDate()
  {
    Date result = null;
    ClassPolicyView ClassPolicyView = (ClassPolicyView) getValue("#{row}");
    if (ClassPolicyView != null && ClassPolicyView.getClassPolicy() != null)
    {
      result = TextUtils.parseInternalDate(
        ClassPolicyView.getClassPolicy().getEndDate());
    }
    return result;
  }

  public Date getEditingCreationDateTime()
  {
    Date result = null;
    if (editingClassPolicy != null)
    {
      result = TextUtils.parseInternalDate(
        editingClassPolicy.getCreationDateTime());
    }
    return result;
  }

  public Date getEditingChangeDateTime()
  {
    Date result = null;
    if (editingClassPolicy != null)
    {
      result = TextUtils.parseInternalDate(
        editingClassPolicy.getChangeDateTime());
    }
    return result;
  }

  @Override
  public String show()
  {
    return "class_policies";
  }

  public String showClass()
  {
    org.santfeliu.classif.Class clazz =
      (org.santfeliu.classif.Class)getValue("#{class}");
    return getControllerBean().showObject("Class", clazz.getClassId());
  }

  @Override
  public String store()
  {
    if (editingClassPolicy != null)
    {
      storeClassPolicy();
    }
    else
    {
      load();
    }
    return show();
  }

  public String showPolicy()
  {
    return getControllerBean().showObject("Policy",
      (String)getValue("#{row.policy.policyId}"));
  }

  public String searchPolicy()
  {
    return getControllerBean().searchObject("Policy",
      "#{classPoliciesBean.editingClassPolicy.policyId}");
  }

  public String removeClassPolicy()
  {
    try
    {
      ClassPolicyView row = (ClassPolicyView)getRequestMap().get("row");
      PolicyManagerPort port = PolicyConfigBean.getPort();
      port.removeClassPolicy(row.getClassPolicy().getClassPolicyId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeClassPolicy()
  {
    try
    {
      ClassBean classBean = (ClassBean)getBean("classBean");
      String classId = classBean.getClassId();
      editingClassPolicy.setClassId(classId);

      PolicyManagerPort port = PolicyConfigBean.getPort();
      port.storeClassPolicy(editingClassPolicy);
      editingClassPolicy = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancelClassPolicy()
  {
    editingClassPolicy = null;
    return null;
  }

  public String createClassPolicy()
  {
    editingClassPolicy = new ClassPolicy();
    return null;
  }

  public String editClassPolicy()
  {
    try
    {
      ClassPolicyView row = (ClassPolicyView)getExternalContext().
        getRequestMap().get("row");

      ClassPolicy ClassPolicy = row.getClassPolicy();

      if (ClassPolicy != null)
        editingClassPolicy = ClassPolicy;
      else
        editingClassPolicy = new ClassPolicy();
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public List<SelectItem> getPolicySelectItems()
  {
    PolicyBean policyBean = (PolicyBean)getBean("policyBean");
    return policyBean.getSelectItems(editingClassPolicy.getPolicyId());
  }

  @Override
  public Type getSelectedType()
  {
    return TypeCache.getInstance().getType(DictionaryConstants.CLASS_POLICY_TYPE);
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public List<org.santfeliu.classif.Class> getSuperClasses()
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    String classId = classBean.getClassId();
    org.santfeliu.classif.Class clazz = ClassCache.getInstance().getClass(classId);
    return clazz.getSuperClasses();
  }

  public List<ClassPolicyView> getSuperClassPolicyViews()
  {
    List<ClassPolicyView> result = new ArrayList();
    try
    {
      org.santfeliu.classif.Class clazz =
        (org.santfeliu.classif.Class)getValue("#{class}");
      result = load(clazz.getClassId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return result;
  }

  public boolean isInheritedPoliciesRendered()
  {
    return renderInheritedPolicies;
  }

  public void setInheritedPoliciesRendered(boolean renderInheritedPolicies)
  {
    this.renderInheritedPolicies = renderInheritedPolicies;
  }

  public String changeRenderInheritedPolicies()
  {
    this.renderInheritedPolicies = !this.renderInheritedPolicies;
    return null;
  }

  private List<ClassPolicyView> load(String classId) throws Exception
  {
    ClassPolicyFilter filter = new ClassPolicyFilter();
    filter.setClassId(classId);
    return PolicyConfigBean.getPort().findClassPolicyViews(filter);
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        ClassPolicyFilter filter = new ClassPolicyFilter();
        ClassBean classBean = (ClassBean)getBean("classBean");
        rows = load(classBean.getClassId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
