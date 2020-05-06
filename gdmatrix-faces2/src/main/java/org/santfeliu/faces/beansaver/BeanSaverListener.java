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
import java.util.List;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public class BeanSaverListener implements PhaseListener
{
  public static final String BEAN_SAVING_METHOD = 
    "org.matrix.BEAN_SAVING_METHOD";
  public static final String SESSION_METHOD = "session";
  public static final String SAVABLE_BEANS = "savableBeans";

  @Override
  public void afterPhase(PhaseEvent event)
  {
  }

  @Override
  public void beforePhase(PhaseEvent event)
  {
    PhaseId phaseId = event.getPhaseId();
    if (isSessionSavingMethod())
    {
      if (PhaseId.RESTORE_VIEW.equals(phaseId))   
        restoreRequestBeans();
      else if (PhaseId.RENDER_RESPONSE.equals(phaseId))
        saveRequestBeans();      
    }
  }

  @Override
  public PhaseId getPhaseId()
  {
    return PhaseId.ANY_PHASE;
  }
  
  private void saveRequestBeans()
  {
    ExternalContext ctx = 
      FacesContext.getCurrentInstance().getExternalContext();
    Map requestMap = ctx.getRequestMap();
      
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
    
    ctx.getSessionMap().put(SAVABLE_BEANS, beanList);
  }  
  
  private void restoreRequestBeans()
  {
    ExternalContext ctx = 
      FacesContext.getCurrentInstance().getExternalContext();
    List beanList = (List) ctx.getSessionMap().get(SAVABLE_BEANS);
    if (beanList != null && !beanList.isEmpty())
    {
      Map requestMap = ctx.getRequestMap();
      for (int i = 0; i < beanList.size(); i += 2)
      {      
        Object name = beanList.get(i);
        Object bean = beanList.get(i + 1);
        requestMap.put(name, bean);
      }
    }
  }  
  
  private boolean isSessionSavingMethod()
  {
    String savingMethod = MatrixConfig.getProperty(BEAN_SAVING_METHOD);     
    return (SESSION_METHOD.equalsIgnoreCase(savingMethod));    
  }  
}
