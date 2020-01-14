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
package org.santfeliu.news.web;

import java.util.HashSet;
import java.util.Set;
import org.apache.myfaces.custom.tree2.TreeNodeChecked;
import org.matrix.news.NewSection;

/**
 *
 * @author unknown
 */
public class NewSectionTreeNode extends TreeNodeChecked
{
  private String mid;
  private Set<String> updateRoles = new HashSet<String>();
  private NewSection newSection;
  private boolean sticky;
  
  public NewSectionTreeNode()
  {
  }

  public void setMid(String mid)
  {
    this.mid = mid;
  }

  public String getMid()
  {
    return mid;
  }

  public void setUpdateRoles(Set<String> updateRoles)
  {
    this.updateRoles = updateRoles;
  }

  public Set<String> getUpdateRoles()
  {
    return updateRoles;
  }

  public void setNewSection(NewSection newSection)
  {
    this.newSection = newSection;
  }

  public NewSection getNewSection()
  {
    return newSection;
  }

  public boolean isSticky()
  {
    return sticky;
  }

  public void setSticky(boolean sticky)
  {
    this.sticky = sticky;
  }
  
}
