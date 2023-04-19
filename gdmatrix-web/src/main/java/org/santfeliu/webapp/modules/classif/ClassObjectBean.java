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
package org.santfeliu.webapp.modules.classif;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import static org.santfeliu.webapp.modules.classif.ClassifModuleBean.getPort;
import org.matrix.classif.Class;
import org.matrix.classif.ClassFilter;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class ClassObjectBean extends ObjectBean
{
  private Class classObject = new Class();
  private transient List<Class> history;
  private String formSelector;

  @Inject
  ClassTypeBean classTypeBean;

  @Inject
  ClassFinderBean classFinderBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CLASS_TYPE;
  }

  @Override
  public Class getObject()
  {
    return isNew() ? null : classObject;
  }

  @Override
  public ClassTypeBean getTypeBean()
  {
    return classTypeBean;
  }

  @Override
  public ClassFinderBean getFinderBean()
  {
    return classFinderBean;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : classObject.getTitle();
  }

  @Override
  public int getEditModeSelector()
  {
    return 2;
  }

  public Class getClassObject()
  {
    return classObject;
  }

  public void setClassObject(Class classObject)
  {
    this.classObject = classObject;
  }

  @Override
  public void loadObject() throws Exception
  {
    history = null;
    formSelector = null;

    if (!NEW_OBJECT_ID.equals(objectId))
    {
      String dateTime = WebUtils.getValue("#{classFinderBean.filterDateTime}");
        classObject = getPort(false).loadClass(objectId, dateTime);
    }
    else
    {
      classObject = new Class();
    }
  }

  @Override
  public void storeObject() throws Exception
  {
    if (StringUtils.isBlank(classObject.getSuperClassId()))
    {
      classObject.setSuperClassId(null);
    }

    classObject = getPort(false).storeClass(classObject);
    if (StringUtils.isBlank(classObject.getTitle()))
    {
      // WebService returns Class object with classId when period is removed
      setObjectId(NEW_OBJECT_ID);
      classObject = new Class();
    }
    else
    {
      setObjectId(classObject.getClassId());
    }

    classFinderBean.outdate();

    history = null;
  }

  @Override
  public void removeObject() throws Exception
  {
    getPort(false).removeClass(classObject.getClassId());

    classFinderBean.outdate();
  }

  public void loadHistory()
  {
    try
    {
      if (isNew())
      {
        history = Collections.EMPTY_LIST;
      }
      else
      {
        ClassFilter filter = new ClassFilter();
        filter.setClassId(objectId);
        history = getPort(false).findClasses(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void showHistory()
  {
    if (history == null)
    {
      loadHistory();
    }
  }

  public List<Class> getHistory()
  {
    return history;
  }

  public void loadClassHistory(String startDateTime)
  {
    if (!NEW_OBJECT_ID.equals(objectId) &&
      !startDateTime.equals(classObject.getStartDateTime()))
    {
      try
      {
        classObject = getPort(false).loadClass(objectId, startDateTime);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public String getPathDateTime()
  {
    return StringUtils.isBlank(classObject.getStartDateTime()) ?
      TextUtils.formatDate(new Date(), "yyyyMMddHHmmss") :
      classObject.getStartDateTime();
  }

  public List<org.santfeliu.classif.Class> getSuperClasses()
  {
    String superClassId = classObject.getSuperClassId();
    if (StringUtils.isBlank(superClassId)) return Collections.EMPTY_LIST;

    ClassCache cache = ClassCache.getInstance(getPathDateTime());
    org.santfeliu.classif.Class superClass = cache.getClass(superClassId);
    List<org.santfeliu.classif.Class> superClasses;
    if (superClass == null)
    {
      superClasses = Collections.EMPTY_LIST;
    }
    else
    {
      superClasses = superClass.getSuperClasses();
      superClasses.add(superClass);
    }
    return superClasses;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[] { classObject, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.classObject = (Class) array[0];
    this.formSelector = (String)array[1];
  }

}
