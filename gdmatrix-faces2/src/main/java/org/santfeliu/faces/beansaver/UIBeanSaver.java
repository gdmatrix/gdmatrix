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
package org.santfeliu.faces.beansaver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.faces.component.FacesComponent;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

/**
 *
 * @author unknown
 */
@FacesComponent(value = "UIBeanSaver")
public class UIBeanSaver extends UIParameter
{
  public static final String COMPONENT_TYPE = "BeanSaver";
  public static final String COMPONENT_FAMILY = "javax.faces.Parameter";

  public UIBeanSaver()
  {
  }

  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }

  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[2];
    values[0] = super.saveState(context);
    Map requestMap = context.getExternalContext().getRequestMap();
    values[1] = saveRequestBeans(requestMap);
    return values;
  }

  public void restoreState(FacesContext context, Object state)
  {
    Object values[] = (Object[])state;
    super.restoreState(context, values[0]);
    Map requestMap = context.getExternalContext().getRequestMap();
    restoreRequestBeans(requestMap, values[1]);
  }

  private Object saveRequestBeans(Map requestMap)
  {
    ArrayList beanList = new ArrayList();
    Iterator iter = requestMap.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      Object value = entry.getValue();
      if (value instanceof Savable)
      {
        String name = String.valueOf(entry.getKey());
        beanList.add(name);
        beanList.add(value);
      }
    }
    return beanList.toArray();
  }

  private void restoreRequestBeans(Map requestMap, Object state)
  {
    Object[] beans = (Object[])state;
    for (int i = 0; i < beans.length; i += 2)
    {      
      Object name = beans[i];
      Object bean = beans[i + 1];
      requestMap.put(name, bean);
    }
  }
}
