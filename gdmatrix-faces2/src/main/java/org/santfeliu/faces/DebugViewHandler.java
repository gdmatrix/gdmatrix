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
package org.santfeliu.faces;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.santfeliu.web.UserSessionBean;


/**
 *
 * @author unknown
 */
public class DebugViewHandler extends ViewHandler
{
  private ViewHandler _viewHandler;
  private boolean debug = false;
  
  public DebugViewHandler(ViewHandler viewHandler)
  {
    _viewHandler = viewHandler;
  }
  
  public UIViewRoot createView(FacesContext context, String viewId)
  {    
    Map sessionMap = context.getExternalContext().getSessionMap();
    UIViewRoot uiRoot = _viewHandler.createView(context, viewId);

    System.out.println(sessionMap);

    System.out.println("createView " + viewId + ": " + uiRoot + 
      " [" + uiRoot.getLocale() + "]");
    renderTree(uiRoot, "");

    return uiRoot;
  }

  public UIViewRoot restoreView(FacesContext context, String viewId)
  {
    Map sessionMap = context.getExternalContext().getSessionMap();
    UIViewRoot uiRoot = _viewHandler.restoreView(context, viewId);

    System.out.println(sessionMap);
    
    System.out.println("restoreView " + viewId + ": " + uiRoot);
    return uiRoot;
  }
  
  public void renderView(FacesContext context, UIViewRoot uiRoot)
    throws IOException
  {
    String template = 
      UserSessionBean.getCurrentInstance().getFrame() + "." +
      UserSessionBean.getCurrentInstance().getTemplate();
    System.out.println("renderView " + template + uiRoot.getViewId() + 
      ": " + uiRoot);
    _viewHandler.renderView(context, uiRoot);
    uiRoot = context.getViewRoot();
    if (debug) renderTree(uiRoot, "");
  }

  public Locale calculateLocale(FacesContext context)
  {
    return _viewHandler.calculateLocale(context);
  }

  public String calculateRenderKitId(FacesContext context)
  {
    return _viewHandler.calculateRenderKitId(context);
  }

  public String getActionURL(FacesContext context, String viewId)
  {
    return _viewHandler.getActionURL(context, viewId);
  }

  public String getResourceURL(FacesContext context, String viewId)
  {
    return _viewHandler.getResourceURL(context, viewId);
  }

  public void writeState(FacesContext context) throws IOException
  {
    _viewHandler.writeState(context);
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
