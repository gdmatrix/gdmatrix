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
package org.matrix.pf.script;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.matrix.pf.web.WebBacking;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.event.UnselectEvent;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author blanquepa
 * @param <T> extends ScriptPage
 */
public class ScriptHelper<T extends ScriptPage> extends WebBacking 
  implements Serializable
{
  private static final String FORM_SERVLET = "/form/";
  private static final String FORM_RENDERER_CLASS = 
    "org.santfeliu.web.servlet.form.EchoFormRenderer"; 
  
  public static final String SCRIPT_BACKING_NAME = "scriptBackingName";  
  
  protected T backing;
  
  protected Map data = new HashMap<>(); //Data persisted as dynamic properties
  protected Map outdata = new HashMap<>(); //Not persistent data  
  
  public ScriptHelper()
  {
  }
  
  protected ScriptHelper(T backing)
  {
    this.backing = backing;
  }

  protected T getBacking()
  {
    return backing;
  }

  protected void setBacking(T backing)
  {
    this.backing = backing;
  }
  
  public Map getData()
  {
    return data;
  }

  public void setData(Map data)
  {
    this.data = data;
  }

  public Map getOutdata()
  {
    return outdata;
  }

  public void setOutdata(Map outdata)
  {
    this.outdata = outdata;
  }  
    
  public String getXhtmlFormUrl(String pageName)
  {    
    HttpServletRequest request = 
      (HttpServletRequest)getExternalContext().getRequest();
    String contextPath = request.getContextPath();
    String serverName = HttpUtils.getServerName(request);
    String protocol = "localhost".equals(serverName) ? "http" : 
      HttpUtils.getScheme(request);
    String defaultPort = 
      MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
    String port = !"80".equals(defaultPort) ? ":" + defaultPort : "";     
    
    //Avoid caching
    String nocache = getMenuItemProperty("nocache");
    String caching = nocache == null || nocache.equalsIgnoreCase("true") ? 
      "&nocache=true" : "";

    return protocol + "://" + serverName + port + contextPath + FORM_SERVLET 
      + "?selector=xhtml:" + pageName
      + "&renderer=" + FORM_RENDERER_CLASS 
      + caching;       
  }   
    
  public Object call(String methodName, Object... args) 
    throws Exception
  {
    Object result = null;
    String scriptBackingName = getScriptBackingName(); 
    if (scriptBackingName != null)
    {
      try
      {
        ScriptBean scriptBean = new ScriptBean(scriptBackingName);
        scriptBean.inject("data", data);
        scriptBean.inject("outdata", outdata);
        scriptBean.initScope();
        if (scriptBean.containsKey(methodName))
          result = scriptBean.call(methodName, args);     
      }
      catch (IOException ex)
      {      
        Logger.getLogger(ScriptFormHelper.class.getName()).
          log(Level.WARNING, "Page without backing bean", ex.getMessage());
      }
    }
    return result;
  } 
  
  public void show() throws Exception
  {
    call("show"); //TODO: "load" or "populate".
  }  
  
  //Component methods
  public List<SelectItem> complete(String query)
  {
    List<SelectItem> result = new ArrayList();

    try
    {
      FacesContext context = FacesContext.getCurrentInstance();   
      UIComponent component = UIComponent.getCurrentComponent(context);
      String methodName = 
        (String) component.getAttributes().get("completeMethodName");      
      result = (List<SelectItem>) call(methodName, query);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return result;
  }
    
  public void onItemSelect(SelectEvent event)
  {
    createListener("itemSelectMethodName", event); 
  }
  
  public void onItemUnselect(UnselectEvent event)
  {
    createListener("itemUnselectMethodName", event); 
  } 
    
  public void onClear()
  {
   createListener("clearMethodName");     
  }
  
  public void onRowToggle(ToggleEvent event)
  {
    createListener("rowToggleMethodName", event);
  }
  
  public void onTabChange(TabChangeEvent event)
  {
    createListener("tabChangeMethodName", event);
  } 
  protected void createListener(String listenerName, AjaxBehaviorEvent event)
  {  
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();   
      UIComponent component = UIComponent.getCurrentComponent(context);
      String methodName = 
        (String) component.getAttributes().get(listenerName); 
      if (methodName != null)
      {
        if (event != null)
          outdata.put("_event", event);
        
        call(methodName);
        
        if (event != null)
          outdata.remove("_event");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }    
  }
  
  protected void createListener(String listenerName)
  {
    createListener(listenerName, null);
  }   

  protected String getScriptBackingName()
  {
    
    //1. Try if backing name is set as show() parameter.
    String backingName = backing.getScriptBackingName();    
    
    //2. Try if backing name is set as dictionary or node property.
    if (backingName == null)
      backingName = backing.getProperty(SCRIPT_BACKING_NAME);
    
    //3. Try if backing name is set as component attribute
    if (backingName == null)
    {
      FacesContext context = FacesContext.getCurrentInstance();   
      UIComponent component = UIComponent.getCurrentComponent(context);
      if (component != null)
        backingName = (String) component.getAttributes().get(SCRIPT_BACKING_NAME);    
    }
    
    //4. Try default name (from current page name)
    if (backingName == null)
    {
      StringBuilder sb = null;
      String scriptPageName = getScriptPageName();

      if (scriptPageName != null)
      {
        String[] parts = scriptPageName.split("\\_");
        for (String part : parts)
        {
          if (sb == null)
            sb = new StringBuilder();
          else
            part = StringUtils.capitalize(part);

          sb.append(part);          
        }

        if (sb != null)
        {
          sb.append("Backing");
          backingName = sb.toString();
        }
      }
    }
    
    return backingName;
  }
  
  protected String getScriptPageName()
  {
    return backing.getScriptPageName();
  }  
}


