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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.cms.Node;
import org.matrix.pf.web.WebBacking;
import org.matrix.security.RoleFilter;
import org.matrix.security.Role;
import org.santfeliu.cms.CNode;
import org.santfeliu.cms.web.NodeEditBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.security.web.SecurityConfigBean;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBeanIntrospector;
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
  public static final String DESCRIPTION = "description";
  @CMSProperty
  public static final String ACTION = "action";  
  @CMSProperty
  public static final String FRAME = "frame";   
  @CMSProperty
  public static final String TEMPLATE = "template";
  @CMSProperty
  public static final String SECTION = "section";
  @CMSProperty
  public static final String TOPWEB = "topWeb";
  @CMSProperty
  public static final String ROLES_SELECT = "roles.select";  
  @CMSProperty
  public static final String ROLES_UPDATE = "roles.update";  
  @CMSProperty
  public static final String BEAN_NAME = "beanName";  
  @CMSProperty
  public static final String BEAN_ACTION = "beanAction"; 
  @CMSProperty
  public static final String RENDERED = "rendered";
  @CMSProperty
  public static final String ENABLED = "enabled";
  @CMSProperty
  public static final String TOPIC = "topic";
  @CMSProperty
  public static final String URL = "url";
  @CMSProperty
  public static final String TARGET = "target";
  @CMSProperty
  public static final String CERTIFICATE_REQUIRED = "certificateRequired";

  private final CMSConfigHelper configHelper;
  
  public SystemConfigBacking()
  {
    configHelper = new CMSConfigHelper();
  }
  
  //LABEL
  
  public String getLabel()
  {
    return getDirectProperty(LABEL);
  }
  
  public void setLabel(String label)
  {
    configHelper.setProperty(LABEL, label);
  }

  //DESCRIPTION  
  
  public String getDescription()
  {
    return getDirectProperty(DESCRIPTION);
  }
  
  public void setDescription(String description)
  {
    configHelper.setProperty(DESCRIPTION, description);
  }  
  
  //ACTION
  
  public String getAction()
  {
    return getDirectProperty(ACTION);
  }
  
  public void setAction(String action)
  {
    configHelper.setProperty(ACTION, action);
    if (action != null)
    {
      if (action.startsWith("#{") && action.endsWith("}") && 
        action.contains("."))
      {
        String auxAction = action.replace("#{", "").replace("}", "");
        configHelper.setProperty(BEAN_NAME, auxAction.split("\\.")[0]);
        configHelper.setProperty(BEAN_ACTION, auxAction.split("\\.")[1]);
      }
      else
      {
        configHelper.setProperty(BEAN_NAME, null);
        configHelper.setProperty(BEAN_ACTION, null);        
      }
    }
    else
    {
      configHelper.setProperty(BEAN_NAME, null);
      configHelper.setProperty(BEAN_ACTION, null);
    }
  }

  public List<SelectItem> getActions()
  {
    try
    {
      List<SelectItem> result = new ArrayList();
      CMSManagedBeanIntrospector introspector =
        new CMSManagedBeanIntrospector();
      List<String> beanNameList = introspector.getBeanNames();
      Collections.sort(beanNameList);
      SelectItem undefinedItem = new SelectItem();
      undefinedItem.setLabel("");
      undefinedItem.setValue(null);
      result.add(undefinedItem);
      for (String beanName : beanNameList)
      {
        List<String> beanActions = getBeanActions(introspector, beanName);
        for (String beanAction : beanActions)
        {
          String action = "#{" + beanName + "." + beanAction + "}";
          SelectItem item = new SelectItem();
          item.setLabel(action);
          item.setValue(action);
          result.add(item);
        }
      }
      return result;
    }
    catch (Exception ex)
    {
      return new ArrayList();
    }
  }
  
  private List<String> getBeanActions(CMSManagedBeanIntrospector introspector, 
    String beanName)
  {
    try
    {
      List<String> result = new ArrayList();
      Class c = null;
      Object obj = getBean(beanName);
      if (obj != null) c = obj.getClass();
      if (c != null && introspector.getBeanClasses().contains(c))
      {
        Map<String, CMSAction> actionMap = introspector.getActions(c);
        result.addAll(actionMap.keySet());
        Collections.sort(result);
      }
      return result;      
    }
    catch (Exception ex)
    {
      return new ArrayList();     
    }
  }  
  
  //FRAME
  
  public String getFrame()
  {
    return getProperty(FRAME, true);
  }
  
  public void setFrame(String frame)
  {
    configHelper.setProperty(FRAME, frame);
  }

  public String getInheritedFrame()
  {
    return getProperty(FRAME, false);
  }

  //TEMPLATE
  
  public String getTemplate()
  {
    String template = getProperty(TEMPLATE, true);
    if (template == null)
    {
      template = getProperty(SECTION, true);
    }
    return template;
  }
  
  public void setTemplate(String template)
  {
    configHelper.setProperty(TEMPLATE, template);
  }
  
  public String getInheritedTemplate()
  {
    String template = getProperty(TEMPLATE, false);
    if (template == null)
    {
      template = getProperty(SECTION, false);
    }
    return template;
  }

  //ROLES_SELECT
  
  public List<String> getRolesSelect()
  {
    return getMultivaluedProperty(ROLES_SELECT, true);
  }
  
  public void setRolesSelect(List<String> rolesSelect)
  {
    configHelper.setMultivaluedProperty(ROLES_SELECT, rolesSelect);
  }    

  public List<String> getInheritedRolesSelect()
  {
    return getMultivaluedProperty(ROLES_SELECT, false);
  }
  
  //ROLES_UPDATE
  
  public List<String> getRolesUpdate()
  {
    return getMultivaluedProperty(ROLES_UPDATE, true);
  }
  
  public void setRolesUpdate(List<String> rolesUpdate)
  {
    configHelper.setMultivaluedProperty(ROLES_UPDATE, rolesUpdate);
  }    

  public List<String> getInheritedRolesUpdate()
  {
    return getMultivaluedProperty(ROLES_UPDATE, false);
  }
  
  //COMPLETE ROLE
  
  public List<String> completeRole(String query) 
  {
    List<String> result = new ArrayList();
    String queryLowerCase = query.toLowerCase();
    List<Role> roles = getAllRoles();
    for (Role role : roles)
    {
      if (role.getRoleId().toLowerCase().contains(queryLowerCase))
      {
        result.add(role.getRoleId());
      }
    }
    return result;
  }
  
  //TOPWEB
  
  public String getTopWeb()
  {
    return getBooleanProperty(TOPWEB, false);
  }
  
  public void setTopWeb(String topWeb)
  {
    setBooleanProperty(TOPWEB, topWeb, false);
  }

  //RENDERED
  
  public String getRendered()
  {
    return getBooleanProperty(RENDERED, true);
  }
  
  public void setRendered(String rendered)
  {
    setBooleanProperty(RENDERED, rendered, true);
  }
  
  //TOPIC
  
  public String getTopic()
  {
    return getDirectProperty(TOPIC);
  }
  
  public void setTopic(String topic)
  {
    configHelper.setProperty(TOPIC, topic);
  }

  //URL
  
  public String getUrl()
  {
    return getProperty(URL, true);
  }
  
  public void setUrl(String url)
  {
    configHelper.setProperty(URL, url);
  }

  public String getInheritedUrl()
  {
    return getProperty(URL, false);
  }
  
  //TARGET
  
  public String getTarget()
  {
    return getDirectProperty(TARGET);
  }
  
  public void setTarget(String target)
  {
    configHelper.setProperty(TARGET, target);
  }
  
  public List<SelectItem> getTargets()
  {
    List<SelectItem> result = new ArrayList();
    result.add(new SelectItem("_blank", "_blank"));
    result.add(new SelectItem("_self", "_self"));
    result.add(new SelectItem("_parent", "_parent"));
    result.add(new SelectItem("_top", "_top"));
    return result;
  }
  
  //ENABLED
    
  public String getEnabled()
  {
    return getProperty(ENABLED, true);
  }
  
  public void setEnabled(String enabled)
  {
    configHelper.setProperty(ENABLED, enabled);
  }

  public String getInheritedEnabled()
  {
    return getProperty(ENABLED, false);
  }  

  //CERTIFICATE_REQUIRED
    
  public String getCertificateRequired()
  {
    return getProperty(CERTIFICATE_REQUIRED, true);
  }
  
  public void setCertificateRequired(String certificateRequired)
  {
    configHelper.setProperty(CERTIFICATE_REQUIRED, certificateRequired);
  }

  public String getInheritedCertificateRequired()
  {
    return getProperty(CERTIFICATE_REQUIRED, false);
  }  

  public List<SelectItem> getRequiredCertificates()
  {
    List<SelectItem> result = new ArrayList();
    result.add(new SelectItem(MenuModel.CLIENT_CERTIFICATE, 
      MenuModel.CLIENT_CERTIFICATE));
    result.add(new SelectItem(MenuModel.SERVER_CERTIFICATE, 
      MenuModel.SERVER_CERTIFICATE));
    return result;
  }
  
  //OTHER METHODS
  
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
      
      NodeEditBean nodeEditBean = getNodeEditBean();
      CNode selectedCNode = configHelper.getSelectedCNode();
      Node selectedNode = selectedCNode.getNode();      
      if (selectedCNode.isRoot())
      {
        nodeEditBean.resetRootSelectionPanel();
      }
      configHelper.updateCache();
      nodeEditBean.resetTree();
      if (!configHelper.nodeIsVisible(selectedNode.getNodeId()))
      {
        String newVisibleNodeId = configHelper.getNewVisibleNodeId();
        nodeEditBean.goToNode(newVisibleNodeId);
      }
      else
      {
        nodeEditBean.resetTopPanel();       
        nodeEditBean.resetPropertiesPanel();
        nodeEditBean.resetCssPanel();        
        nodeEditBean.resetSyncPanel();        
      }
      info("NODE_SAVED");      
    }
    catch (Exception ex)
    {
      error(ex);
      Logger.getLogger(SystemConfigBacking.class.getName()).
        log(Level.SEVERE, null, ex);
    }
  }
  
  public void revertProperties()
  {
    try
    {
      info("NODE_REVERTED");      
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void goToParentNode(String propertyNames)
  {
    String nodeId = null;
    boolean found = false;
    List<String> propertyNameList = Arrays.asList(propertyNames.split(","));
    MenuItemCursor cursor = 
      UserSessionBean.getCurrentInstance().getSelectedMenuItem();
    while (cursor.moveParent() && !found)
    {
      for (String propertyName : propertyNameList)
      {
        if (cursor.getDirectProperty(propertyName) != null)
        {
          nodeId = cursor.getMid();
          found = true;
        }
      }
    }
    if (nodeId != null)
    {    
      getNodeEditBean().goToNode(nodeId);
    }
  }
  
  private NodeEditBean getNodeEditBean()
  {
    return (NodeEditBean)getBean("nodeEditBean"); 
  }
  
  private List<Role> getAllRoles()
  {
    try
    {
      RoleFilter filter = new RoleFilter();
      return SecurityConfigBean.getPort().findRoles(filter);
    }
    catch (Exception ex)
    {
      return new ArrayList();      
    }
  }
  
  private String getBooleanProperty(String name, boolean defaultValue)
  {
    String value = getDirectProperty(name);
    return (value != null ? value : Boolean.toString(defaultValue));
  }
  
  private void setBooleanProperty(String name, String value, 
    boolean defaultValue)
  {
    configHelper.setProperty(name, 
      (defaultValue != Boolean.parseBoolean(value) ? value : null));    
  }

}
