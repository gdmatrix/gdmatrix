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
import java.util.Date;
import java.util.List;
import org.matrix.classif.Class;
import org.matrix.classif.ClassFilter;
import org.matrix.classif.ClassificationManagerPort;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;
import org.santfeliu.ws.WSExceptionFactory;

/**
 *
 * @author realor
 */
public class ClassMainBean extends DynamicTypifiedPageBean
{
  private Class classObject;

  public ClassMainBean()
  {
    super("Class", "CLASSIF_ADMIN");
  }

  @Override
  public String show()
  {
    return "class_main";
  }

  public Class getClassObject()
  {
    return classObject;
  }

  public void setClassObject(Class classObject)
  {
    this.classObject = classObject;
  }

  public String showType()
  {
    return getControllerBean().showObject("Type", getCurrentTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getCurrentTypeId() != null && getCurrentTypeId().trim().length() > 0;
  }
  
  public List<org.santfeliu.classif.Class> getSuperClasses()
  {
    String superClassId = classObject.getSuperClassId();
    if (superClassId == null || superClassId.trim().length() == 0) return null;

    String pathDateTime = getInternalPathDateTime();
    ClassCache cache = ClassCache.getInstance(pathDateTime);
    org.santfeliu.classif.Class c = cache.getClass(superClassId);
    List<org.santfeliu.classif.Class> superClasses;
    if (c == null)
    {
      superClasses = new ArrayList<org.santfeliu.classif.Class>();
    }
    else
    {
      superClasses = c.getSuperClasses();
      superClasses.add(c);
    }
    return superClasses;
  }

  public String getIndent()
  {
    Class row = (Class)getValue("#{classItem}");
    String pathDateTime = getInternalPathDateTime();
    ClassCache cache = ClassCache.getInstance(pathDateTime);
    org.santfeliu.classif.Class rowClass = cache.getClass(row.getClassId());
    return (rowClass == null) ? "0" :
      String.valueOf((rowClass.getClassLevel() - 1) * 6);
  }

  public Date getPathDateTime()
  {
    String pathDateTime = getInternalPathDateTime();
    return TextUtils.parseInternalDate(pathDateTime);
  }

  public String showClassFromPath()
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    Class row = (Class)getValue("#{classItem}");
    String objectId = classBean.getObjectId(row);
    return getControllerBean().showObject("Class", objectId);
  }

  public Date getCreationDateTime()
  {
    return TextUtils.parseInternalDate(classObject.getCreationDateTime());
  }

  public Date getChangeDateTime()
  {
    return TextUtils.parseInternalDate(classObject.getChangeDateTime());
  }

  public boolean isRootClass()
  {
    String superClassId = classObject.getSuperClassId();
    return superClassId == null || superClassId.trim().length() == 0;
  }

  @Override
  public String store()
  {
    try
    {
      ClassificationManagerPort port = ClassificationConfigBean.getPort();
      if (isNew())
      {
        String classId = classObject.getClassId();
        if (classId != null && classId.trim().length() > 0)
        {
          ClassFilter filter = new ClassFilter();
          filter.setClassId(classId);
          filter.setFirstResult(0);
          filter.setMaxResults(1);
          if (port.findClasses(filter).size() > 0)
          {
            error("CLASS_ALREADY_EXISTS");
            return null;
          }
        }
      }

      String superClassId = classObject.getSuperClassId();
      if (superClassId != null && superClassId.trim().length() == 0)
      {
        classObject.setSuperClassId(null);
      }
      // store      
      classObject.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        classObject.getProperty().addAll(properties);

      System.out.println(">>>>>>>>> currentTypeId:" + getCurrentTypeId());
      classObject.setClassTypeId(getCurrentTypeId());

      classObject = port.storeClass(classObject);

      setFormDataFromProperties(classObject.getProperty());

      ClassBean classBean = (ClassBean)getBean("classBean");
      setObjectId(classBean.getObjectId(classObject));

      // force history reload
      ClassHistoryBean classHistoryBean =
      (ClassHistoryBean)getBean("classHistoryBean");
      classHistoryBean.getRows().clearCache();
      classHistoryBean.setFirstRowIndex(0);
    }
    catch (Exception ex)
    {
      error(ex);
      List<String> details = WSExceptionFactory.getDetails(ex);
      if (details.size() > 0) error(details);
    }
    return null;
  }

  protected String getInternalPathDateTime()
  {
    String pathDateTime;
    String defaultDateTime = ClassificationConfigBean.getDefaultDateTime();
    String startDateTime = classObject.getStartDateTime();
    String endDateTime = classObject.getEndDateTime();
    if (startDateTime.compareTo(defaultDateTime) <= 0 &&
      defaultDateTime.compareTo(endDateTime) < 0)
    {
      // defaultDateTime in class period
      pathDateTime = defaultDateTime;
    }
    else
    {
      // take startDateTime in class period
      pathDateTime = startDateTime;
    }
    return pathDateTime;
  }

  protected void load()
  {
    if (isNew())
    {
      classObject = new Class();
    }
    else
    {
      try
      {
        ClassificationManagerPort port = ClassificationConfigBean.getPort();
        ClassBean classBean = (ClassBean)getBean("classBean");
        String classId = classBean.getClassId();
        String startDateTime = classBean.getStartDateTime();
        classObject = port.loadClass(classId, startDateTime);
        setCurrentTypeId(classObject.getClassTypeId());
        setFormDataFromProperties(classObject.getProperty());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        classObject = new Class();
      }
    }
  }
}
