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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.matrix.pf.web.WebBacking;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
@Named("systemConfigBacking")
public class SystemConfigBacking extends WebBacking
{
  @CMSProperty
  public static final String LABEL = "label";
  @CMSProperty
  public static final String ACTION = "action";  
  @CMSProperty
  public static final String FRAME = "frame";   
  @CMSProperty
  public static final String TEMPLATE = "template";     
  @CMSProperty
  public static final String TOPWEB = "topWeb";  
  @CMSProperty
  public static final String ROLES_SELECT = "roles.select";  
  @CMSProperty
  public static final String ROLES_UPDATE = "roles.update";  
   
  private final CMSConfigHelper configHelper;
  
  public SystemConfigBacking()
  {
    configHelper = new CMSConfigHelper();
  }
  
  public String getLabel()
  {
    return getDirectProperty(LABEL);
  }
  
  public void setLabel(String label)
  {
    configHelper.setProperty(LABEL, label);
  }

  public String getAction()
  {
    return getDirectProperty(ACTION);
  }
  
  public void setAction(String action)
  {
    configHelper.setProperty(ACTION, action);
  }  

  public String getFrame()
  {
    return getDirectProperty(FRAME);
  }
  
  public void setFrame(String frame)
  {
    configHelper.setProperty(FRAME, frame);
  }  

  public String getTemplate()
  {
    return getDirectProperty(TEMPLATE);
  }
  
  public void setTemplate(String template)
  {
    configHelper.setProperty(TEMPLATE, template);
  } 
  
  public List<String> getRolesSelect()
  {
    return getDirectMultivaluedProperty(ROLES_SELECT);
  }
  
  public void setRolesSelect(List<String> rolesSelect)
  {
    configHelper.setMultivaluedProperty(ROLES_SELECT, rolesSelect);
  }    
  
  public List<String> getRolesUpdate()
  {
    return getDirectMultivaluedProperty(ROLES_UPDATE);
  }
  
  public void setRolesUpdate(List<String> rolesUpdate)
  {
    configHelper.setMultivaluedProperty(ROLES_UPDATE, rolesUpdate);
  }    

  public String getTopWeb()
  {
    return getDirectProperty(TOPWEB);
  }
  
  public void setTopWeb(String topWeb)
  {
    configHelper.setProperty(TOPWEB, topWeb);
  }
  
  public List<Path> getFolders(String path)
  {
    List<Path> folders = null;
    String absoluteWebPath = getExternalContext().getRealPath("/");
    Path parent = Paths.get(absoluteWebPath + path);
    try
    {  
      folders = Files.list(parent).filter(Files::isDirectory)
        .collect(Collectors.toList());
    }
    catch (IOException ex)
    {
      error(ex);
      Logger.getLogger(SystemConfigBacking.class.getName()).
        log(Level.SEVERE, null, ex);
    }
    return folders;
  }
  
  public void store()
  {
    try
    {
      configHelper.saveProperties();
    }
    catch (Exception ex)
    {
      error(ex);
      Logger.getLogger(SystemConfigBacking.class.getName()).
        log(Level.SEVERE, null, ex);
    }
  }
  
}
