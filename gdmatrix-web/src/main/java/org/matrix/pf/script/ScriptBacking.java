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

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ControllerBacking;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.TabPage;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;

/**
 * @author blanquepa
 */
@CMSContent(typeId = "Script")
@Named("scriptBacking")
public class ScriptBacking extends PageBacking 
  implements TabPage
{
  private static final String OUTCOME = "pf_script"; 
  private static final String FORM_SERVLET = "/form/";
  private static final String FORM_RENDERER_CLASS = 
    "org.santfeliu.web.servlet.form.EchoFormRenderer"; 
  
  protected String pageName;
  protected String label;  
  protected String beanName; //Name of the scriptBean 
  
  //Form URL construction
  private String contextPath;
  private String serverName;
  private String protocol;
  private String port;   
  
  private ObjectBacking objectBacking;
  
  public ScriptBacking()
  {
//    init();
  }
  
  @PostConstruct
  public final void init()
  {
    HttpServletRequest request = 
      (HttpServletRequest)getExternalContext().getRequest();
    contextPath = request.getContextPath();
    serverName = HttpUtils.getServerName(request);
    protocol = "localhost".equals(serverName) ? "http" : 
      HttpUtils.getScheme(request);
    String defaultPort = 
      MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
    port = !"80".equals(defaultPort) ? ":" + defaultPort : ""; 
  }

  public String getXhtmlFormUrl()
  {
    //Avoid caching
    String nocache = getProperty("nocache");
    String caching = nocache != null && !nocache.equalsIgnoreCase("false") ? 
      "&nocache=true" : "";

    return protocol + "://" + serverName + port + contextPath + FORM_SERVLET 
      + "?selector=xhtml:" + pageName
      + "&renderer=" + FORM_RENDERER_CLASS 
      + caching;       
  }  
  
  public String show(String pageName, String beanName)
  {
    this.objectBacking = 
      ControllerBacking.getCurrentInstance().getObjectBacking();
    this.pageName = pageName;
    this.label = getSelectedMenuItem().getLabel(); 
    this.beanName = beanName != null ? beanName : null;
    populate();        
    return OUTCOME;    
  }  

  @Override
  public ObjectBacking getObjectBacking()
  {
    return objectBacking;
  }
  
  @Override
  public String show(String page)
  {
    return show(page, null);
  }
  
  @Override
  public String show()
  {
    return show(null);
  }  
  
  @Override
  public void create()
  {
    call("create"); 
  }

  @Override
  public void load()
  {
    call("populate");
  }

  @Override
  public String store()
  {
    return (String) call("store");
  }

  @Override
  public void reset()
  {
    call("reset");
  }  
  
  @Override
  public String cancel()
  {
    return (String) call("cancel");
  }   

  @Override
  public String getPageObjectId()
  {
    return (String) call("getObjectId"); 
  } 
  
  protected Object call(String method, Object... args)
  {
    if (method != null && beanName != null)
    {
      ScriptBean scriptBean = getScriptBean(beanName);
      
      if (scriptBean.containsKey(method))
      {
        try
        {
          return scriptBean.call(method);
        }
        catch (Exception ex)
        {
          error(ex);
        }
      }
    }
    return null;
  } 
  
  private ScriptBean getScriptBean(String name)
  {
    return (ScriptBean) 
      UserSessionBean.getCurrentInstance().getSb().get(name);
  }     
 
}
