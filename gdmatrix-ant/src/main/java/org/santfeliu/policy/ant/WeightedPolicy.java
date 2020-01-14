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

import org.matrix.policy.Policy;

/**
 *
 * @author realor
 * 
 * WeightedPolicy adds a weight to a Policy
 * @author realor
 */
public class WeightedPolicy extends Policy
{
  private int weight;

  public WeightedPolicy(Policy policy)
  {
    this.setPolicyId(policy.getPolicyId());
    this.setPolicyTypeId(policy.getPolicyTypeId());
    this.setTitle(policy.getTitle());
    this.setDescription(policy.getDescription());
    this.setEvaluationCode(policy.getEvaluationCode());
    this.setMandate(policy.getMandate());   
    this.setActivationCondition(policy.getActivationCondition());
    this.setActivationDateExpression(policy.getActivationDateExpression());
    this.setAutomaticExecution(policy.isAutomaticExecution());
    this.setCreationUserId(policy.getCreationUserId());
    this.setCreationDateTime(policy.getCreationDateTime());
    this.setChangeUserId(policy.getChangeUserId());
    this.setChangeDateTime(policy.getChangeDateTime());
    this.getProperty().addAll(policy.getProperty());
  }

  public WeightedPolicy(Policy policy, int weight)
  {
    this(policy);
    this.weight = weight;
  }

  public int getWeight()
  {
    return weight;
  }

  public void setWeight(int weight)
  {
    this.weight = weight;
  }
}
