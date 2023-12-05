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
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.santfeliu.webapp.ObjectBean;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyConstants;
import org.matrix.policy.PolicyManagerPort;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.UserSessionBean;
import static org.santfeliu.webapp.modules.policy.PolicyModuleBean.getPort;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class PolicyObjectBean extends ObjectBean
{
  private Policy policy = new Policy();
  private String formSelector;

  @Inject
  PolicyTypeBean policyTypeBean;

  @Inject
  PolicyFinderBean policyFinderBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.POLICY_TYPE;
  }

  @Override
  public Policy getObject()
  {
    return isNew() ? null : policy;
  }

  @Override
  public PolicyTypeBean getTypeBean()
  {
    return policyTypeBean;
  }

  @Override
  public PolicyFinderBean getFinderBean()
  {
    return policyFinderBean;
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
    return isNew() ? "" : policy.getTitle();
  }

  public Policy getPolicy()
  {
    return policy;
  }

  public void setPolicy(Policy policy)
  {
    this.policy = policy;
  }

  @Override
  public void loadObject() throws Exception
  {
    formSelector = null;

    if (!NEW_OBJECT_ID.equals(objectId))
    {
      policy = getPort(false).loadPolicy(objectId);
    }
    else
    {
      policy = new Policy();
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    policy = getPort(false).storePolicy(policy);
    setObjectId(policy.getPolicyId());
    policyFinderBean.outdate();
  }

  @Override
  public void removeObject() throws Exception
  {
    PolicyManagerPort port = getPort(false);
    port.removePolicy(policy.getPolicyId());

    policyFinderBean.outdate();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[] { policy, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.policy = (Policy) array[0];
    this.formSelector = (String)array[1];
  }
  
  @Override
  public boolean isEditable()
  {
    if (UserSessionBean.getCurrentInstance().isUserInRole(
      PolicyConstants.POLICY_ADMIN_ROLE))
      return true;
    
    if (!super.isEditable()) return false; //tab protection

    if (policy == null || policy.getPolicyId() == null || 
      policy.getPolicyTypeId() == null)
      return true;
        
    Type currentType =
      TypeCache.getInstance().getType(policy.getPolicyTypeId());
    return currentType == null;
  }

}
