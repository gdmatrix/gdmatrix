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

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyFilter;
import org.matrix.policy.PolicyManagerPort;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.dic.web.TypeSearchBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class PolicySearchBean extends BasicSearchBean
{
  private PolicyFilter filter = new PolicyFilter();
  private transient List<SelectItem> typeSelectItems;

  public PolicyFilter getFilter()
  {
    return filter;
  }

  public void setFilter(PolicyFilter filter)
  {
    this.filter = filter;
  }

  public List<SelectItem> getActionSelectItems()
  {
    return PolicyConfigBean.getActionSelectItems();
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      if (typeSelectItems == null)
      {
        TypeBean typeBean = (TypeBean)getBean("typeBean");
        typeSelectItems =
          typeBean.getSelectItems(
          DictionaryConstants.POLICY_TYPE, filter.getPolicyTypeId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  }

  public String getTypeDescription()
  {
    Policy row = (Policy)getExternalContext().getRequestMap().get("row");
    if (row != null)
    {
      String rowDocTypeId = row.getPolicyTypeId();

      TypeCache typeCache = TypeCache.getInstance();
      Type type = typeCache.getType(rowDocTypeId);

      if (type != null)
        return type.getDescription();
      else
        return rowDocTypeId;
    }
    else
      return "";
  }

  @Override
  public int countResults()
  {
    try
    {
      PolicyManagerPort port = PolicyConfigBean.getPort();
      return port.countPolicies(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      PolicyManagerPort port = PolicyConfigBean.getPort();
      return port.findPolicies(filter);
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
    return "policy_search";
  }

  public String showPolicy()
  {
    return getControllerBean().showObject("Policy",
      (String)getValue("#{row.policyId}"));
  }

  public String selectPolicy()
  {
    Policy row = (Policy)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String policyId = row.getPolicyId();
    return getControllerBean().select(policyId);
  }

  public String searchType()
  {
    TypeSearchBean typeSearchBean = (TypeSearchBean)getBean("typeSearchBean");
    if (typeSearchBean == null)
      typeSearchBean = new TypeSearchBean();

    typeSearchBean.setRootTypeId(DictionaryConstants.POLICY_TYPE);
    typeSearchBean.setFilter(new TypeFilter());
    typeSearchBean.search();

    return getControllerBean().searchObject("Type",
      "#{policySearchBean.filter.policyTypeId}");
  }
}
