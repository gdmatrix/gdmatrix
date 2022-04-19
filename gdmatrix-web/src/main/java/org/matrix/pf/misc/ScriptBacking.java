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
package org.matrix.pf.misc;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.matrix.pf.web.PageBacking;
import org.matrix.web.Describable;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
@Named("scriptBacking")
public class ScriptBacking extends PageBacking implements Describable
{
  private static final String FORM_SERVLET = "/form/";
  private static final String FORM_RENDERER_CLASS = 
    "org.santfeliu.web.servlet.form.EchoFormRenderer";
  
  private static final String OUTCOME = "pf_script";

  protected String pageName;
  protected String scriptName;
  protected String label;
       
  public String getXhtmlFormUrl()
  { 
    if (pageName != null)
    {
      HttpServletRequest request = 
        (HttpServletRequest)getExternalContext().getRequest();
      String contextPath = request.getContextPath();
      String serverName = HttpUtils.getServerName(request);
      String protocol = "localhost".equals(serverName) ? "http" : 
        HttpUtils.getScheme(request);
      String port = MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
      port = !"80".equals(port) ? ":" + port : ""; 
      
      //Avoid caching
      String nocache = getProperty("nocache");
      String caching = nocache != null && !nocache.equalsIgnoreCase("false") ? 
        "&nocache=true" : "";
      
      return protocol + "://" + serverName + port + contextPath + FORM_SERVLET 
        + "?selector=xhtml:" + pageName 
        + "&renderer=" + FORM_RENDERER_CLASS 
        + caching;    
    }
    return null;
  }
  
  @Override
  public String show()
  {
    return show(null);
  }  
  
  @Override
  public String show(String pageName)
  {
    return show(pageName, null);
  }    
    
  public String show(String pageName, String scriptName)
  {
    this.pageName = pageName != null ? pageName : getProperty("xhtml");
    this.scriptName = scriptName != null ? scriptName : null;
    this.label = getSelectedMenuItem().getLabel();
    return OUTCOME;    
  }
  
  @Override
  public String getPageObjectId()
  {
    return null;
  }
  
  protected Object call(String method)
  {
    if (method != null && scriptName != null)
    {
      ScriptBean scriptBean = getScriptBean(scriptName);
      
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
  
  private ScriptBean getScriptBean(String scriptName)
  {
    return (ScriptBean) 
      UserSessionBean.getCurrentInstance().getSb().get(scriptName);
  }
  
  @Override
  public String getObjectTypeId()
  {
    return "Script";
  }

  @Override
  public String getObjectId()
  {
    return pageName;
  }

  @Override
  public String getDescription()
  {
    return label;
  }

  @Override
  public String getDescription(String objectId)
  {
    return label;
  }
}
