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
package org.santfeliu.classif.web;

import java.util.ArrayList;
import java.util.List;
import org.santfeliu.classif.Class;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.santfeliu.classif.ClassCache;

/**
 *
 * @author realor
 */
public class ClassTreeNode implements TreeNode
{
  // Class identifier
  private String classId;
  // dateTime snapshot
  private String dateTime;
  // Link to Class to reduce the number of calls to TypeCache during tree rendering
  private transient Class classObject;
  // list of children nodes
  private transient List<ClassTreeNode> children;

  public ClassTreeNode(String classId, String dateTime)
  {
    this.classId = classId;
    this.dateTime = dateTime;
  }

  public String getType()
  {
    return "folder";
  }

  public void setType(String type)
  {
  }

  public String getDescription()
  {
    String description = null;
    linkClass();
    if (classObject != null)
    {
      description = classObject.getTitle();
    }
    return description;
  }

  public void setDescription(String description)
  {
  }

  public void setIdentifier(String identifier)
  {
    this.classId = identifier;
  }

  public String getIdentifier()
  {
    return classId;
  }

  public int getChildCount()
  {
    linkClass();
    if (classObject != null)
    {
      return classObject.getSubClassesCount();
    }
    return 0;
  }

  public boolean isLeaf()
  {
    linkClass();
    if (classObject != null)
    {
      return classObject.isLeaf();
    }
    return true;
  }

  public void setLeaf(boolean arg0)
  {
  }

  public List getChildren()
  {
    if (children == null)
    {
      children = new ArrayList<ClassTreeNode>();
      linkClass();
      if (classObject != null)
      {
        List<String> subClassIds = classObject.getSubClassIds();
        for (String subClassId : subClassIds)
        {
          children.add(new ClassTreeNode(subClassId, dateTime));
        }
      }
    }
    return children;
  }

  public boolean isOpen()
  {
    linkClass();
    if (classObject != null)
    {
      return classObject.isOpen();
    }
    return true;
  }

  public Class getProperties()
  {
    linkClass();
    return classObject;
  }

  private void linkClass()
  {
    if (classObject == null)
    {
      ClassCache classCache = ClassCache.getInstance(dateTime);
      classObject = classCache.getClass(classId);
    }
  }
}
