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
package org.santfeliu.dic.web;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.apache.myfaces.custom.tree2.TreeNode;

/**
 *
 * @author realor
 */
public class NextTypeTreeNode implements TreeNode, Serializable
{
  private TypeTreeNode typeTreeNode;

  public NextTypeTreeNode(TypeTreeNode typeTreeNode)
  {
    this.typeTreeNode = typeTreeNode;
  }

  public TypeTreeNode getTypeTreeNode()
  {
    return typeTreeNode;
  }

  public boolean isLeaf()
  {
    return true;
  }

  public void setLeaf(boolean leaf)
  {
  }

  public List getChildren()
  {
    return Collections.EMPTY_LIST;
  }

  public String getType()
  {
    return "next";
  }

  public void setType(String type)
  {
  }

  public String getDescription()
  {
    return "next";
  }

  public void setDescription(String description)
  {
  }

  public void setIdentifier(String id)
  {
  }

  public String getIdentifier()
  {
    return typeTreeNode.getTypeId() + "-next";
  }

  public int getChildCount()
  {
    return 0;
  }

  public int getNextChildCount()
  {
    int derivedTypeCount = typeTreeNode.getDerivedTypeCount();
    int firstIndex = typeTreeNode.getFirstIndex();
    return derivedTypeCount - (firstIndex + TypeTreeBean.getNodesPerPage());
  }

  public String nextPage()
  {
    int newFirstIndex = typeTreeNode.getFirstIndex();
      newFirstIndex += TypeTreeBean.getNodesPerPage();
    typeTreeNode.setFirstIndex(newFirstIndex);
    return null;
  }
}
