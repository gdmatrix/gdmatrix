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
import org.matrix.classif.Class;
import org.santfeliu.cases.web.CaseSearchBean;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.doc.web.executeParametersManagers;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author realor
 */
public class ClassBean extends ObjectBean
{
  @Override
  public String getObjectTypeId()
  {
    return "Class";
  }

  @Override
  public String getDescription(String objectId)
  {
    String description;
    String classId = getClassId(objectId);
    String startDateTime = getStartDateTime(objectId);

    ClassCache classCache = ClassCache.getInstance(startDateTime);
    Class classObject = classCache.getClass(classId);
    if (classObject == null)
    {
      description = classId;
    }
    else if (startDateTime == null)
    {
      description = classId + ": " + classObject.getTitle();
    }
    else
    {
      Date date = TextUtils.parseInternalDate(startDateTime);
      description = classId + 
        " (" + TextUtils.formatDate(date, "dd/MM/yyyy HH:mm:ss") + "): " +
        classObject.getTitle();
    }
    return description;
  }

  public String getClassId()
  {
    String classId = null;
    if (!isNew())
    {
      classId = getClassId(objectId);
    }
    return classId;
  }

  public String getStartDateTime()
  {
    String startDateTime = null;
    if (!isNew())
    {
      startDateTime = getStartDateTime(objectId);
    }
    return startDateTime;
  }

  public String getClassId(String objectId)
  {
    if (objectId == null) return ControllerBean.NEW_OBJECT_ID;
    String classId;
    int index = objectId.indexOf(":");
    if (index != -1)
    {
      classId = objectId.substring(0, index);
    }
    else classId = objectId;
    return classId;
  }

  public String getStartDateTime(String objectId)
  {
    String startDateTime;
    int index = objectId.indexOf(":");
    if (index != -1)
    {
      startDateTime = objectId.substring(index + 1);
    }
    else startDateTime = null;
    return startDateTime;
  }

  public String getObjectId(Class classObject)
  {
    if (classObject.getStartDateTime() == null) return classObject.getClassId();
    return classObject.getClassId() + ":" + classObject.getStartDateTime();
  }

  public String searchCases()
  {
    CaseSearchBean caseSearchBean = (CaseSearchBean)getBean("caseSearchBean");
    caseSearchBean.reset();
    caseSearchBean.setClassId(getClassId());
    return ControllerBean.getCurrentInstance().searchObject("Case");
  }

  public String searchDocuments()
  {
    executeParametersManagers documentSearchBean =
      (executeParametersManagers)getBean("documentSearchBean");
    documentSearchBean.reset();
    documentSearchBean.setClassId(getClassId());
    return ControllerBean.getCurrentInstance().searchObject("Document");
  }

  @Override
  public void postStore()
  {
    // sync classes
    ClassCache.syncInstances();
  }
  
  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        ClassificationConfigBean.getPort().removeClass(getClassId());
        removed();
        // sync classes
        ClassCache.syncInstances();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }
  
  @Override
  public boolean isEditable()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.isUserInRole("CLASSIF_ADMIN");
  }
}
