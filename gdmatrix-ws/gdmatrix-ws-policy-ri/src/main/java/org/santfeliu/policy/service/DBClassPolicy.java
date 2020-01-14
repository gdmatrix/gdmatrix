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
package org.santfeliu.policy.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.matrix.policy.ClassPolicy;
import org.santfeliu.jpa.JPAUtils;

/**
 *
 * @author unknown
 */
public class DBClassPolicy extends ClassPolicy
{
  public void copyTo(ClassPolicy classPolicy)
  {
    JPAUtils.copy(this, classPolicy);
  }

  public void copyFrom(ClassPolicy classPolicy)
  {
    classPolicyId = classPolicy.getClassPolicyId();
    classId = classPolicy.getClassId();
    policyId = classPolicy.getPolicyId();
    dispHoldId = classPolicy.getDispHoldId();
    reason = classPolicy.getReason();
    startDate = classPolicy.getStartDate();
    endDate = classPolicy.getEndDate();
  }

  public boolean isActiveAt(Date date)
  {
    boolean active = true;
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    String nowDate = df.format(date);
    if (startDate != null)
    {
      active = startDate.compareTo(nowDate) <= 0;
    }
    if (active && endDate != null)
    {
      active = endDate.compareTo(nowDate) >= 0;
    }
    return active;
  }
}
