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

import java.util.Date;
import java.util.List;
import org.matrix.classif.Class;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.classif.ClassFilter;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.BasicSearchBean;
import org.santfeliu.web.obj.ControllerBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class ClassSearchBean extends BasicSearchBean
{
  @CMSProperty
  public static final String CLASS_TREE_MID_PROPERTY = "classTreeMid";

  private ClassFilter filter = new ClassFilter();

  public ClassSearchBean()
  {
    filter.setStartDateTime(ClassificationConfigBean.getDefaultDateTime());
  }

  public ClassFilter getFilter()
  {
    return filter;
  }

  public void setFilter(ClassFilter filter)
  {
    this.filter = filter;
  }

  @Override
  public int countResults()
  {
    try
    {
      filter.setEndDateTime(filter.getStartDateTime());
      ClassificationManagerPort port = ClassificationConfigBean.getPort();
      return port.countClasses(filter);
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
      filter.setEndDateTime(filter.getStartDateTime());
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);

      ClassificationManagerPort port = ClassificationConfigBean.getPort();
      return port.findClasses(filter);
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
    return "class_search";
  }

  public String findSubClasses()
  {
    Class row = (Class)getValue("#{row}");
    String classId = row.getClassId();
    filter.setSuperClassId(classId);
    filter.setClassId(null);
    filter.setTitle(null);
    filter.setDescription(null);
    return search();
  }

  public boolean isShowInTreeEnabled()
  {
    return getClassTreeMid() != null && isInstantSearch();
  }

  public String showInTree()
  {
    Class row = (Class)getValue("#{row}");
    ClassBean classBean = (ClassBean)getBean("classBean");
    String objectId = classBean.getObjectId(row);
    classBean.setObjectId(objectId);

    ClassTreeBean classTreeBean = (ClassTreeBean)getBean("classTreeBean");
    classTreeBean.expandClass(classBean.getClassId(), 
      filter.getStartDateTime(), true);

    ControllerBean controllerBean = ControllerBean.getCurrentInstance();
    return controllerBean.search(getClassTreeMid());
  }

  public boolean isLeafClass()
  {
    if (isInstantSearch())
    {
      Class row = (Class)getValue("#{row}");

      ClassCache classCache = ClassCache.getInstance(filter.getStartDateTime());
      org.santfeliu.classif.Class classObject = 
        classCache.getClass(row.getClassId());
      if (classObject != null) return classObject.isLeaf();
    }
    return false;
  }

  public String findSubClassesFromPath()
  {
    Class classObject = (Class)getValue("#{classItem}");
    filter.setSuperClassId(classObject.getClassId());
    filter.setClassId(null);
    filter.setTitle(null);
    filter.setDescription(null);
    return search();
  }

  public boolean isInstantSearch()
  {
    String startDateTime = filter.getStartDateTime();
    return startDateTime != null;
  }

  public boolean isSuperClassSearch()
  {
    String superClassId = filter.getSuperClassId();
    return superClassId != null && superClassId.trim().length() > 0;
  }

  public boolean isNavigationEnabled()
  {
    return isInstantSearch() && isSuperClassSearch();
  }

  public String getIndent()
  {
    Class row = (Class)getValue("#{classItem}");
    ClassCache cache = ClassCache.getInstance(filter.getStartDateTime());
    org.santfeliu.classif.Class rowClass = cache.getClass(row.getClassId());
    return rowClass == null ?
      "0" : String.valueOf(rowClass.getClassLevel() * 6);
  }

  public Date getRowStartDateTime()
  {
    String startDateTime = (String)getValue("#{row.startDateTime}");
    return TextUtils.parseInternalDate(startDateTime);
  }

  public List<org.santfeliu.classif.Class> getSuperClasses()
  {
    ClassCache cache = ClassCache.getInstance(filter.getStartDateTime());
    org.santfeliu.classif.Class classObject =
      cache.getClass(filter.getSuperClassId());
    if (classObject != null)
    {
      List<org.santfeliu.classif.Class> classPath = 
        classObject.getSuperClasses();
      classPath.add(classObject);
      return classPath;
    }
    return null;
  }

  public boolean isCurrentClass()
  {
    Class row = (Class)getValue("#{row}");    
    ClassBean classBean = (ClassBean)getBean("classBean");
    String objectId = classBean.getObjectId();
    String rowObjectId = classBean.getObjectId(row);
    return rowObjectId.equals(objectId);
  }

  public String getRowStyle()
  {
    Class row = (Class)getValue("#{row}");
    return ClassificationConfigBean.getClassStyle(row);
  }

  public String showClass()
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    Class row = (Class)getValue("#{row}");
    String objectId = classBean.getObjectId(row);
    return getControllerBean().showObject("Class", objectId);
  }

  public String selectClass()
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    Class row = (Class)getValue("#{row}");
    String objectId = classBean.getObjectId(row);
    return getControllerBean().select(objectId);
  }

  // private

  private String getClassTreeMid()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.getMenuModel().
      getSelectedMenuItem().getProperty(CLASS_TREE_MID_PROPERTY);
  }
}
