package org.santfeliu.faces.menu.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import org.santfeliu.cms.CNode;
import org.santfeliu.cms.CWorkspace;
import org.santfeliu.web.UserSessionBean;


public class MenuModel
{
  public static final String LABEL = "label";
  public static final String TOPIC = "topic";
  public static final String ACTION = "action";
  public static final String URL = "url";
  public static final String TARGET = "target";
  public static final String ENABLED = "enabled";
  public static final String RENDERED = "rendered";  
  public static final String VIEW_ROLES = "roles.select";
  public static final String ACCESS_ROLES = "roles.access";
  public static final String EDIT_ROLES = "roles.update";
  public static final String CERTIFICATE_REQUIRED = "certificateRequired";

  public static final String SERVER_CERTIFICATE = "server";
  public static final String CLIENT_CERTIFICATE = "client";

  private CWorkspace cWorkspace;
  private String rootMid;
  private String selectedMid;
  private Set roles;
  private String ipAddress;
  private boolean allVisible;

  public MenuModel()
  {
  }

  public CWorkspace getCWorkspace()
  {
    return cWorkspace;
  }

  public void setCWorkspace(CWorkspace cWorkspace)
  {
    this.cWorkspace = cWorkspace;
  }
  
  public void setRoles(Set roles)
  {
    this.roles = roles;
  }

  public Set getRoles()
  {
    return roles;
  }

  public String getBrowserType()
  {
    return UserSessionBean.getCurrentInstance().getBrowserType();
  }
  
  public String getSelectedMid()
  {
    return selectedMid;
  }

  public boolean isAllVisible()
  {
    return allVisible;
  }

  public void setAllVisible(boolean allVisible)
  {
    this.allVisible = allVisible;
  }

  public void setIpAddress(String ipAddress)
  {
    this.ipAddress = ipAddress;
  }

  public String getIpAddress()
  {
    return ipAddress;
  }
  
  public void setSelectedMid(String selectedMid)
  {
    this.selectedMid = selectedMid;
  }
  
  public String getRootMid()
  {
    return rootMid;
  }

  public void setRootMid(String rootMid)
  {
    this.rootMid = rootMid;
  }

  public List<MenuItemCursor> getMenuItemsByMid(List<String> midList)
  {
    List<MenuItemCursor> result = new ArrayList<MenuItemCursor>();
    Map<String, CNode> nodeMap = cWorkspace.getNodes(midList);
    for (String mid : midList)
    {
      if (nodeMap.get(mid) != null) //existing node
      {
        try
        {
          MenuItemCursor mic = getMenuItemByMid(mid);
          result.add(mic); //node is visible
        }
        catch (Exception ex)
        {
        }
      }
    }
    return result;
  }

  public MenuItemCursor getMenuItemByMid(String mid) throws Exception
  {
    CNode node = cWorkspace.getNode(mid);
    if (node == null)
    {
      throw new MenuItemNotFoundException(mid);
    }
    else
    {
      if (!isVisibleNode(node)) // protected node
      {
        throw new ProtectedMenuItemException(mid);
      }
    }
    return new MenuItemCursor(this, mid);
  }

  public MenuItemCursor getMenuItemByTopic(String topic) throws Exception
  {
    CNode node = cWorkspace.findNodeByTopic(topic);
    if (node == null) throw new TopicNotFoundException(topic);
    return getMenuItemByMid(node.getNodeId());
  }

  // secure getMenuItem method. It never returns null
  public MenuItemCursor getMenuItem(String mid)
  {
    if (mid == null) return new MenuItemCursor(this, null);
    try
    {
      return getMenuItemByMid(mid);
    }
    catch (Exception ex)
    {
      return new MenuItemCursor(this, null);
    }
  }

  public MenuItemCursor getSelectedMenuItem()
  {
    MenuItemCursor menuItem;
    if (selectedMid == null)
    {
      menuItem = new MenuItemCursor(this, null);
    }
    else
    {
      menuItem = getMenuItem(selectedMid);
    }
    return menuItem;
  }

  public MenuItemCursor getRootMenuItem()
  {
    return getMenuItem(rootMid);
  }

  /**
   * @deprecated
   * @return
   */
  public MenuItemCursor getRoot()
  {
    return getRootMenuItem();
  }

  public Map getItems()
  {
    return new MenuItemsMap();
  }

  // package methods

  boolean isVisibleNode(CNode node) throws Exception
  {
    if (roles != null && roles.contains("CMS_ADMIN")) return true;
    
    String enabledExpression = 
      getSingleValuedProperty(node, ENABLED + "." + getBrowserType());
    if (enabledExpression == null)
    {
      enabledExpression = getSingleValuedProperty(node, ENABLED);
    }
    if (!isEnabled(enabledExpression)) return false;

    if (allVisible) return true;
  
    List<String> viewRoles = getMultiValuedProperties(node, VIEW_ROLES);
    if (isUserInRole(viewRoles)) return true;
    
    return false;
  }
  
  boolean isEnabled(String enabledExpression)
  {
    boolean enabled;
    if (enabledExpression == null) enabled = true;
    else if ("true".equals(enabledExpression)) enabled = true;
    else if ("false".equals(enabledExpression)) enabled = false;
    else // eval expression
    {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Application application = facesContext.getApplication();
      try
      {
        ValueBinding vb = application.createValueBinding(enabledExpression);
        Object result = vb.getValue(facesContext);
        if (result instanceof Boolean)
        {
          enabled = ((Boolean)result).booleanValue();
        }
        else enabled = false;
      }
      catch (Exception ex)
      {
        enabled = false;
      }
    }
    return enabled;
  }
  
  boolean isEditableNode(CNode node) throws Exception
  {
    List<String> editRoles = getMultiValuedProperties(node, EDIT_ROLES);
    return isUserInRole(editRoles);
  }

  boolean isUserInRole(List<String> roleList)
  {
    boolean isInRole = roleList.isEmpty() || roles == null;
    if (!isInRole)
    {
      for (int i = 0; i < roleList.size() && !isInRole; i++)
      {
        isInRole = roles.contains(roleList.get(i));
      }
    }
    return isInRole;
  }

  // returns single valued property with inheritance
  String getSingleValuedProperty(CNode node, String propertyName) 
    throws Exception
  {    
    while (node != null)
    {
      String value = node.getSinglePropertyValue(propertyName);
      if (value != null)
      {
        return value;
      }
      node = node.getParent();
    }
    return null;
  }
  
// returns multi valued properties with inheritance
  List<String> getMultiValuedProperties(CNode node, String propertyName)
    throws Exception
  {
    List<String> values = null;
    boolean found = false;
    while (!found && node != null)
    {
      values = node.getMultiPropertyValue(propertyName);
      if (values != null)
      {
        found = true;
      }
      else
      {
        node = node.getParent();
      }
    }
    return values == null ? Collections.EMPTY_LIST : values;
  }

  @Override
  public String toString()
  {
    return "[" + rootMid + "]: " + cWorkspace.getWorkspace().getWorkspaceId();
  }

  class MenuItemsMap implements Map
  {
    public int size()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEmpty()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsKey(Object key)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsValue(Object value)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object get(Object key)
    {
      return getMenuItem(String.valueOf(key));
    }

    public Object put(Object key, Object value)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object remove(Object key)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void putAll(Map m)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set keySet()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection values()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set entrySet()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
