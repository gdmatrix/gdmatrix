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

import java.io.Serializable;
import java.util.List;
import org.apache.myfaces.custom.tree2.TreeModel;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeStateBase;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.classif.Class;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class ClassTreeBean extends PageBean
{
  private Filter _filter = new Filter();
  private Filter filter = new Filter("0000");
  private transient TreeModel treeModel;
  private TreeStateBase treeState = new TreeStateBase();
  private org.matrix.classif.Class cutClass;

  public Filter getFilter()
  {
    return filter;
  }

  public void setFilter(Filter filter)
  {
    this.filter = filter;
  }

  public org.matrix.classif.Class getCutClass()
  {
    return cutClass;
  }

  public TreeModel getTreeModel()
  {
    if (treeModel == null)
    {
      String rootClassId = _filter.getRootClassId();
      String dateTime = _filter.getDateTime();
      if (rootClassId != null && rootClassId.trim().length() > 0)
      {
        ClassTreeNode root = new ClassTreeNode(rootClassId, dateTime);
        treeModel = new TreeModelBase(root);
        treeModel.setTreeState(treeState);
      }
    }
    return treeModel;
  }

  public boolean isSelectedClass()
  {
    String classId = (String)getValue("#{node.identifier}");
    ClassBean classBean  = (ClassBean)getBean("classBean");
    return classId.equals(classBean.getClassId());
  }

  public boolean isCutSubClass()
  {
    if (cutClass == null) return false;
    org.santfeliu.classif.Class nodeClass = 
      (org.santfeliu.classif.Class)getValue("#{node.properties}");
    return cutClass.getClassId().equals(nodeClass.getClassId()) ||
      nodeClass.getSuperClassIds().contains(cutClass.getClassId());
  }

  // actions

  public String selectClass()
  {  
    org.santfeliu.classif.Class nodeClass =
      (org.santfeliu.classif.Class)getValue("#{node.properties}");
    ClassBean classBean = (ClassBean)getBean("classBean");
    String objectId = classBean.getObjectId(nodeClass);
    classBean.setObjectId(objectId);
    return null;
  }

  public String showClass()
  {
    org.santfeliu.classif.Class nodeClass =
      (org.santfeliu.classif.Class)getValue("#{node.properties}");
    ClassBean classBean = (ClassBean)getBean("classBean");
    String objectId = classBean.getObjectId(nodeClass);
    return getControllerBean().showObject("Class", objectId);
  }

  public String addClass()
  {
    String superClassId = (String)getValue("#{node.identifier}");
    String outcome = getControllerBean().createObject("Class");
    ClassMainBean classMainBean = (ClassMainBean)getBean("classMainBean");
    classMainBean.getClassObject().setSuperClassId(superClassId);
    return outcome;
  }

  public String cutClass()
  {
    Class nodeClass = (Class)getValue("#{node.properties}");
    // nodeClass is not Serializable
    cutClass = new org.matrix.classif.Class();
    cutClass.setClassId(nodeClass.getClassId());
    cutClass.setStartDateTime(nodeClass.getStartDateTime());
    return null;
  }

  public String pasteClass()
  {
    String outcome = null;
    if (cutClass != null)
    {
      String superClassId = (String)getValue("#{node.identifier}");

      ClassBean classBean = (ClassBean)getBean("classBean");
      String objectId = classBean.getObjectId(cutClass);

      outcome = getControllerBean().showObject("Class", objectId);
      ClassMainBean classMainBean = (ClassMainBean)getBean("classMainBean");
      classMainBean.getClassObject().setSuperClassId(superClassId);
      classMainBean.getClassObject().setChangeReason(null);
      cutClass = null;
    }
    return outcome;
  }

  public String search()
  {
    cutClass = null;
    treeModel = null;
    treeState = new TreeStateBase();
    treeState.toggleExpanded("0");

    String rootClassId = filter.getRootClassId();
    String dateTime = filter.getDateTime();
    ClassCache classCache = ClassCache.getInstance(dateTime);
    if (classCache.getClass(rootClassId) != null)
    {
      filter.copyTo(_filter);
    }
    else
    {
      _filter.setRootClassId(null);
      error("classif:CLASS_NOT_FOUND");
    }
    return show();
  }

  @Override
  @CMSAction
  public String show()
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    if (!classBean.isNew())
    {
      expandClass(classBean.getClassId(), _filter.getDateTime(), false);
    }
    return "class_tree";
  }

  // expand class at specific dateTime (change filter)
  public void expandClass(String classId, String dateTime, boolean changeFilter)
  {
    ClassCache classCache = ClassCache.getInstance(dateTime);
    Class classObject = classCache.getClass(classId);
    if (classObject != null)
    {
      if (changeFilter)
      {
        String rootClassId = classObject.getRootClassId();
        if (!rootClassId.equals(_filter.getRootClassId()))
        {
          filter.setRootClassId(rootClassId);
          _filter.setRootClassId(rootClassId);
        }
        if (!dateTime.equals(_filter.getDateTime()))
        {
          filter.setDateTime(dateTime);
          _filter.setDateTime(dateTime);
        }
      }
      List<String> superClassIds = classObject.getSuperClassIds();
      superClassIds.add(classId);
      Class currentClass = classObject.getRootClass();
      String nodePath = "0";
      System.out.println("expanding 0");
      if (!treeState.isNodeExpanded(nodePath))
      {
        treeState.toggleExpanded(nodePath);
      }
      for (int i = 1; i < superClassIds.size() - 1; i++)
      {
        String currentClassId = superClassIds.get(i);
        List<String> subClassIds = currentClass.getSubClassIds();
        int index = subClassIds.indexOf(currentClassId);
        nodePath = nodePath + ":" + index;
        System.out.println("expanding " + nodePath);
        if (!treeState.isNodeExpanded(nodePath))
        {
          treeState.toggleExpanded(nodePath);
        }
        currentClass =  classCache.getClass(currentClassId);
      }
    }
  }

  public class Filter implements Serializable
  {
    private String rootClassId;
    private String dateTime;

    public Filter()
    {
    }

    public Filter(String rootClassId)
    {
      this.rootClassId = rootClassId;
      this.dateTime = ClassificationConfigBean.getDefaultDateTime();
    }

    public Filter(String rootClassId, String dateTime)
    {
      this.rootClassId = rootClassId;
      this.dateTime = dateTime;
    }

    public String getRootClassId()
    {
      return rootClassId;
    }

    public void setRootClassId(String rootClassId)
    {
      this.rootClassId = rootClassId;
    }

    public String getDateTime()
    {
      return dateTime;
    }

    public void setDateTime(String dateTime)
    {
      this.dateTime = dateTime;
    }

    private void copyTo(Filter _filter)
    {
      _filter.rootClassId = rootClassId;
      _filter.dateTime = dateTime;
    }
  }
}
