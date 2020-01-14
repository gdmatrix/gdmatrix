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
package org.santfeliu.test.control;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 *
 * @author blanquepa
 */
public class ViewController implements PhaseListener
{
  public void afterPhase(PhaseEvent pe)
  {
    System.out.println("After Phase: " + pe.getPhaseId().toString());
    if (pe.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
    {
      UIViewRoot uiViewRoot = FacesContext.getCurrentInstance().getViewRoot();
      if (uiViewRoot != null)
        System.out.println(uiViewRoot.getId() + " " + uiViewRoot);
//      renderTree(uiViewRoot, "");
    }
  }

  public void beforePhase(PhaseEvent pe)
  {
    System.out.println("Before Phase: " + pe.getPhaseId().toString());
    if (pe.getPhaseId().equals(PhaseId.RESTORE_VIEW)
      || pe.getPhaseId().equals(PhaseId.RENDER_RESPONSE))
    {
      UIViewRoot uiViewRoot = FacesContext.getCurrentInstance().getViewRoot();
//      renderTree(uiViewRoot, "");
      if (uiViewRoot != null)
        System.out.println(uiViewRoot.getId() + " " + uiViewRoot);
    }
  }

  public PhaseId getPhaseId()
  {
    return PhaseId.ANY_PHASE;
  }

  private void renderTree(UIComponent comp, String indent)
  {
    if (comp == null) return;
    System.out.println(indent + comp.getId() + " " + comp);
    List list = comp.getChildren();
    Iterator iter = list.iterator();
    while (iter.hasNext())
    {
      UIComponent child = (UIComponent)iter.next();
      renderTree(child, indent + " ");
    }
    iter = comp.getFacets().entrySet().iterator();
    if (iter.hasNext())
    {
      while (iter.hasNext())
      {
        Map.Entry entry = (Map.Entry)iter.next();
        System.out.println(indent + "FACET " + entry.getKey() + ":");
        UIComponent facet = (UIComponent)entry.getValue();
        renderTree(facet, indent + " ");
      }
    }
  }
}
