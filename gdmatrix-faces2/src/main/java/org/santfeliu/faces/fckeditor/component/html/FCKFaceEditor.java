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
package org.santfeliu.faces.fckeditor.component.html;

import java.io.IOException;

import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;


import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.context.FacesContext;

import javax.faces.el.ValueBinding;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;


import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author srecinto
 *
 */
@FacesComponent(value = "CKEditor")
public class FCKFaceEditor extends HtmlInputTextarea
{
  public static final String COMPONENT_TYPE = "org.fckfaces.Editor";
  public static final String COMPONENT_FAMILY = "org.fckfaces.FCKFacesFamily";
  private static final String DEFAULT_RENDERER_TYPE = "org.ckfaces.EditorRenderer";
  private String toolbarSet;
  private String height;
  private String width;
  private Map configProperties;

  public String getComponentType()
  {
    return COMPONENT_TYPE;
  }

  public String getRendererType()
  {
    return DEFAULT_RENDERER_TYPE;
  }
  
  public void encodeBegin(FacesContext context)
    throws IOException
  {
    super.encodeBegin(context);
  }

  /**
    * Moved to encode end so that the inline java script will run after the textArea was rendered before this script is run
    * @param context
    * @throws IOException
    */
  public void encodeEnd(FacesContext context)
    throws IOException
  {
    String editorRenderer = DEFAULT_RENDERER_TYPE;
    
    try
    {
      UserPreferences userPreferences = UserSessionBean.getCurrentInstance().getUserPreferences();      
      editorRenderer = userPreferences.getPreference("ckEditor");
    }
    catch (Exception ex)
    {
    }
    
    if (editorRenderer == null)
      editorRenderer = DEFAULT_RENDERER_TYPE;

    RenderKit renderKit = context.getRenderKit();
    Renderer renderer = renderKit.getRenderer(getFamily(), editorRenderer);
    if (renderer != null) 
      renderer.encodeEnd(context, this);
    else
      getRenderer(context).encodeEnd(context, this);
  }

  /**
	 *
	 * @return
	 */
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }

  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[6];
    values[0] = super.saveState(context);
    values[1] = toolbarSet;
    values[2] = width;
    values[3] = height;
    values[4] = configProperties;

    return values;
  }

  public void restoreState(FacesContext context, Object state)
  {
    Object values[] = (Object[]) state;
    super.restoreState(context, values[0]);
    this.toolbarSet = (String) values[1];
    this.width = (String)values[2];
    this.height = (String)values[3];
    this.configProperties = (Map)values[4];
  }

  public String getToolbarSet()
  {
    return toolbarSet;
  }

  public void setToolbarSet(String toolbarSet)
  {
    this.toolbarSet = toolbarSet;
  }

  public String getHeight()
  {
    return height;
  }

  public void setHeight(String height)
  {
    System.out.println("compsetheight=" + height);
    this.height = height;
  }

  public String getWidth()
  {
    return width;
  }

  public void setWidth(String width)
  {
    this.width = width;
  }

  public void setConfigProperties(Map configProperties)
  {
    this.configProperties = configProperties;
  }

  public Map getConfigProperties()
  {
    if (configProperties != null) return configProperties;
    ValueExpression ve = getValueExpression("configProperties");
    return ve != null ? (Map)ve.getValue(getFacesContext().getELContext()) : null;
  }
}
