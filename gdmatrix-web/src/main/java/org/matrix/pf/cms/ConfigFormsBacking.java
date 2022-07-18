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
package org.matrix.pf.cms;

import java.net.MalformedURLException;
import java.net.URL;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.matrix.pf.web.WebBacking;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
@Named("configFormsBacking")
public class ConfigFormsBacking extends WebBacking
{ 
  private static final String FRAMES_PATH = "/frames/";
  private static final String TEMPLATES_PATH = "/templates/"; 
  private static final String CONFIG_FILENAME = "/config.xhtml";
  private static final String NOT_FOUND = "/common/util/not_found.xhtml";
  
  public ConfigFormsBacking()
  {
  }
  
  public String storeForms()
  {
    return null;
  }
  
  public String getFrameForm()
  {
    return getForm(getFrameFormPath());
  }
  
  public String getTemplateForm()
  {
    return getForm(getTemplateFormPath());
  }  
  
  public boolean isRenderFrameForm()
  {
    return isAvaliable(getFrameFormPath());
  }
  
  public boolean isRenderTemplateForm()
  {

    return isAvaliable(getTemplateFormPath());
  } 
  
  private String getForm(String path)
  {
    if (isAvaliable(path))
      return path;
    else
      return NOT_FOUND;
  }  
  
  private String getFrameFormPath()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return FRAMES_PATH + userSessionBean.getFrame() + CONFIG_FILENAME;
  }
  
  private String getTemplateFormPath()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return TEMPLATES_PATH + userSessionBean.getTemplate() + CONFIG_FILENAME;    
  }   
  
  private boolean isAvaliable(String fullPath)
  {
    try
    {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      URL resource = facesContext.getExternalContext().getResource(fullPath);   
      return (resource != null);
    }
    catch (MalformedURLException ex)
    {
      return false;
    }
  }
  
  public class Form
  {
    private String name;
    private String outcome;

    public Form(String name, String outcome)
    {
      this.name = name;
      this.outcome = outcome;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getOutcome()
    {
      return outcome;
    }

    public void setOutcome(String outcome)
    {
      this.outcome = outcome;
    }
    
    
  }
}
