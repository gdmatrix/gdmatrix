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
package org.santfeliu.faces.signer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;


/**
 *
 * @author unknown
 */
public class HtmlSignerTag extends UIComponentTag
{
  // main properties
  private String document;
  private String result;
  private String port;
  
  // UICommand attributes
  private String action;
  private String immediate;
  private String actionListener;

  // style properties
  private String style;
  private String styleClass;

  public HtmlSignerTag()
  {
  }
  
  public String getComponentType()
  {
    return "Signer";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public void setDocument(String document)
  {
    this.document = document;
  }

  public String getDocument()
  {
    return document;
  }

  public void setResult(String result)
  {
    this.result = result;
  }

  public String getResult()
  {
    return result;
  }

  public String getPort()
  {
    return port;
  }

  public void setPort(String port)
  {
    this.port = port;
  }

  public void setAction(String action)
  {
    this.action = action;
  }

  public String getAction()
  {
    return action;
  }

  public void setImmediate(String immediate)
  {
    this.immediate = immediate;
  }

  public String getImmediate()
  {
    return immediate;
  }

  public void setActionListener(String actionListener)
  {
    this.actionListener = actionListener;
  }

  public String getActionListener()
  {
    return actionListener;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component, "document", document);
      UIComponentTagUtils.setStringProperty(context, component, "result", result);
      UIComponentTagUtils.setStringProperty(context, component, "port", port);

      UIComponentTagUtils.setActionProperty(context, component, action);
      UIComponentTagUtils.setActionListenerProperty(context, component, actionListener);
      UIComponentTagUtils.setActionListenerProperty(getFacesContext(), component, actionListener);

      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void release()
  {
    super.release();
    style = null;
    styleClass = null;
    document = null;
    result = null;
    port = null;
    action = null;
    immediate = null;
    actionListener = null;
  }
}
