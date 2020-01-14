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
package org.santfeliu.workflow.node;

import java.beans.BeanInfo;

import org.santfeliu.workflow.WorkflowNodeBeanInfo;

/**
 *
 * @author unknown
 */
public class FormNodeBeanInfo extends WorkflowNodeBeanInfo
{
  public FormNodeBeanInfo()
  {
    this(FormNode.class);
  }

  public FormNodeBeanInfo(Class beanClass)
  {
    super(beanClass);
    addProperty("formType").setCategory(SPECIFIC_CATEGORY);
    addProperty("group").setCategory(SPECIFIC_CATEGORY);
    addProperty("forwardEnabled").setCategory(SPECIFIC_CATEGORY);
    addProperty("backwardEnabled").setCategory(SPECIFIC_CATEGORY);
    addProperty("parameters").setCategory(SPECIFIC_CATEGORY);
    addProperty("viewRoles").setCategory(SPECIFIC_CATEGORY);
    addProperty("editRoles").setCategory(SPECIFIC_CATEGORY);
    addProperty("readVariables").setCategory(SPECIFIC_CATEGORY);
    addProperty("writeVariables").setCategory(SPECIFIC_CATEGORY);
    addProperty("checkExpression").setCategory(SPECIFIC_CATEGORY);
    addProperty("cancelExpression").setCategory(SPECIFIC_CATEGORY);
    addProperty("outcomeExpression").setCategory(SPECIFIC_CATEGORY);
    addProperty("actorVariable").setCategory(SPECIFIC_CATEGORY);
  }
  
  @Override
  public java.awt.Image getIcon(int iconKind)
  {
    if (iconKind == BeanInfo.ICON_COLOR_16x16)
    {
      return loadImage("/org/santfeliu/workflow/swing/resources/icon/form.gif");
    }
    return null;
  }
}
