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
package org.santfeliu.swing.undo;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.undo.AbstractUndoableEdit;


/**
 *
 * @author realor
 */
public class BeanUndoableEdit extends AbstractUndoableEdit
{
  private final Object object;
  private Map stateBefore;
  private Map stateAfter;

  public BeanUndoableEdit(Object object)
  {
    this.object = object;
  }

  public void beforeChange()
  {
    stateBefore = readProperties();
  }

  public void afterChange()
  {
    stateAfter = readProperties();
    purgeMaps();
  }

  public void propertyChange(String propertyName,
    Object oldValue, Object newValue) throws Exception
  {
    stateBefore = new HashMap();
    stateAfter = new HashMap();

    BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
    PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
    boolean found = false;
    int i = 0;
    PropertyDescriptor pd = null;
    while (!found && i < pds.length)
    {
      pd = pds[i];
      String name = pd.getName();
      if (name.equals(propertyName)) found = true;
      else i++;
    }
    if (found)
    {
      stateBefore.put(pd, oldValue);
      stateAfter.put(pd, newValue);
    }
  }

  public void undo()
  {
    super.undo();
    writeProperties(stateBefore);
  }

  public void redo()
  {
    super.redo();
    writeProperties(stateAfter);
  }

  private Map readProperties()
  {
    Map map = new HashMap();
    try
    {
      BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
      PropertyDescriptor pds[] = beanInfo.getPropertyDescriptors();
      for (PropertyDescriptor pd : pds)
      {
        if (pd.getWriteMethod() != null)
        {
          Method method = pd.getReadMethod();
          Object value = method.invoke(object, new Object[0]);
          map.put(pd, value);
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return map;
  }

  private void writeProperties(Map map)
  {
    try
    {
      Iterator iter = map.entrySet().iterator();
      while (iter.hasNext())
      {
        Map.Entry entry = (Map.Entry)iter.next();
        PropertyDescriptor pd = (PropertyDescriptor)entry.getKey();
        Object value = entry.getValue();
        Method method = pd.getWriteMethod();
        method.invoke(object, new Object[]{value});
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void purgeMaps()
  {
  }
}
