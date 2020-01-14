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
package org.santfeliu.classif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class Class extends org.matrix.classif.Class
{
  private ClassCache classCache;

  protected Class(ClassCache classCache, org.matrix.classif.Class classObject)
  {
    this.classCache = classCache;
    this.classId = classObject.getClassId();
    this.superClassId = classObject.getSuperClassId();
    this.creationDateTime = classObject.getCreationDateTime();
    this.creationUserId = classObject.getCreationUserId();
    this.startDateTime = classObject.getStartDateTime();
    this.endDateTime = classObject.getEndDateTime();
    this.classTypeId = classObject.getClassTypeId();
    this.title = classObject.getTitle();
    this.description = classObject.getDescription();
    this.changeReason = classObject.getChangeReason();
  }

  public ClassCache getClassCache()
  {
    return classCache;
  }

  public synchronized Class getSuperClass()
  {
    return (getSuperClassId() == null) ?
      null : classCache.getClass(getSuperClassId());
  }

  public synchronized List<Class> getSuperClasses()
  {
    ArrayList<Class> path = new ArrayList<Class>();
    String curClassId = getSuperClassId();
    while (curClassId != null)
    {
      Class classObject = classCache.getClass(curClassId);
      if (classObject == null) curClassId = null;
      else
      {
        path.add(classObject);
        curClassId = classObject.superClassId;
      }
    }
    Collections.reverse(path);
    return path;
  }

  public synchronized List<Class> getSubClasses()
  {
    return getSubClasses(false);
  }

  public synchronized List<Class> getSubClasses(boolean recursive)
  {
    List<String> subClassIds = classCache.getSubClassIds(classId);

    ArrayList<Class> subClasses = new ArrayList<Class>();
    Iterator<String> iter = subClassIds.iterator();
    while (iter.hasNext())
    {
      String subClassId = iter.next();
      Class subClass = classCache.getClass(subClassId);
      if (subClass != null)
      {
        subClasses.add(subClass);
        if (recursive)
        {
          subClasses.addAll(subClass.getSubClasses(true));
        }
      }
    }
    return subClasses;
  }

  public synchronized List<String> getSubClassIds()
  {
    return classCache.getSubClassIds(classId);
  }

  public synchronized int getSubClassesCount()
  {
    return getSubClassIds().size();
  }

  public synchronized boolean isLeaf()
  {
    return getSubClassesCount() == 0;
  }

  public List<String> getSuperClassIds()
  {
    ArrayList<String> path = new ArrayList<String>();
    String curClassId = getSuperClassId();
    while (curClassId != null)
    {
      Class classObject = classCache.getClass(curClassId);
      if (classObject == null) curClassId = null;
      else
      {
        path.add(curClassId);
        curClassId = classObject.superClassId;
      }
    }
    Collections.reverse(path);
    return path;
  }

  public synchronized String getRootClassId()
  {
    String rootClassId;
    if (superClassId == null) // class is root
    {
      rootClassId = classId;
    }
    else
    {
      rootClassId = getSuperClassIds().get(0);
    }
    return rootClassId;
  }

  public synchronized Class getRootClass()
  {
    String rootClassId = getRootClassId();
    return classCache.getClass(rootClassId);
  }

  public synchronized String formatClassPath(
    boolean includeClassId, boolean includeTitle, String separator)
  {
    StringBuilder buffer = new StringBuilder();
    List<Class> path = getSuperClasses();
    path.add(this);
    for (Class classObject : path)
    {
      if (includeClassId)
      {
        buffer.append(classObject.getClassId());
      }
      if (includeTitle)
      {
        if (includeClassId) buffer.append(": ");
        buffer.append(classObject.getTitle());
      }
      buffer.append(separator);
    }
    return buffer.toString();
  }

  public synchronized int getClassLevel()
  {
    List<Class> path = getSuperClasses();
    return path.size() + 1;
  }

  public boolean isOpen() // this period now
  {
    Date now = new Date();
    Date date1 = TextUtils.parseInternalDate(startDateTime);
    Date date2 = TextUtils.parseInternalDate(endDateTime);
    return date1.before(now) || date2.after(now);
  }
}
