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
package org.santfeliu.workflow;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;

import java.beans.BeanDescriptor;


/**
 *
 * @author unknown
 */
public class WorkflowBeanInfo extends BaseBeanInfo
{
  public WorkflowBeanInfo()
  {
    this(Workflow.class);
  }

  public WorkflowBeanInfo(Class beanClass)
  {
    super(beanClass);
    ExtendedPropertyDescriptor pd;
    pd = addProperty("name");
    pd.setShortDescription("Workflow name");
    
    pd = addProperty("description");
    pd.setShortDescription("Workflow description");

    pd = addProperty("firstNodeId");
    pd.setShortDescription("First node identifier");

    pd = addProperty("undoable");
    pd.setShortDescription("Store events?");

    pd = addProperty("nodesCount");
    pd.setShortDescription("Number of nodes");

    pd = addProperty("gridSize");
    pd.setShortDescription("Grid size in pixels");

    pd = addProperty("nodeWidth");
    pd.setShortDescription("Default node width in pixels");

    pd = addProperty("nodeHeight");
    pd.setShortDescription("Default node height in pixels");

    pd = addProperty("gridVisible");
    pd.setShortDescription("Grid visible");

    pd = addProperty("gridEnabled");
    pd.setShortDescription("Snap vertices to grid");
  }

  @Override
  public BeanDescriptor getBeanDescriptor()
  {
    return null;
  }
}
