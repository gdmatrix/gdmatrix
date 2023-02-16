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
package org.santfeliu.faces.menu.model;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;
import org.santfeliu.cms.CNode;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.menu.util.MenuUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.util.json.JSONUtils;

/**
 *
 * @author unknown
 */
public class MenuItemCursor
{
  private MenuModel menuModel;
  private String mid;
  private PropertiesMap properties = new PropertiesMap();

  MenuItemCursor(MenuModel menuModel, String mid)
  {
    this.menuModel = menuModel;
    this.mid = mid;
  }

  public MenuModel getMenuModel()
  {
    return menuModel;
  }

  public String getMid()
  {
    return mid;
  }

  public Map getProperties()
  {
    return properties; // localized properties with inheritance
  }

  public Map getTranslatedProperties()
  {
    return new TranslatedPropertiesMap(properties);
  }

  public Map getDirectProperties() // no inheritance
  {
    try
    {
      CNode node = getNode(mid);
      if (node != null) return node.getPropertiesMap();
    }
    catch (Exception ex)
    {
    }
    return Collections.EMPTY_MAP;
  }

  public String getProperty(String propertyName)
  {
    return (String)properties.get(propertyName);   // with inheritance
  }

  public List<String> getMultiValuedProperty(String propertyName)
  {
    try
    {
      return properties.getMultiValued(propertyName);  // with inheritance
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  public String getDirectProperty(String propertyName)
  {
    return (String)getDirectProperties().get(propertyName); // no inheritance
  }

  public List<String> getDirectMultiValuedProperty(String propertyName)
  {
    try
    {
      return properties.getDirectMultiValued(propertyName);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  public JSONObject getJSON()
  {
    return getJSON("");
  }

  public JSONObject getJSON(String basePath)
  {
    return JSONUtils.getJSON(mid, basePath);
  }

  public String getJSONString()
  {
    return getJSONString("");
  }

  public String getJSONString(String basePath)
  {
    return JSONUtils.getJSONString(mid, basePath);
  }

  public String getLabel()
  {
    String labelValue = getBrowserSensitiveProperty(MenuModel.LABEL);
    return evalExpression(labelValue);
  }

  public String getTopic()
  {
    return (String)getDirectProperties().get(MenuModel.TOPIC);
  }

  public String getAction()
  {
    return getBrowserSensitiveProperty(MenuModel.ACTION, false);
  }

  public String getBrowserSensitiveProperty(String propertyName)
  {
    return getBrowserSensitiveProperty(propertyName, true);
  }

  public String getBrowserSensitiveProperty(String propertyName,
    boolean inherit)
  {
    Map propertyMap = (inherit ? getProperties() : getDirectProperties());
    String value =
      (String)propertyMap.get(propertyName + "." + menuModel.getBrowserType());
    if (value == null)
    {
      value = (String)propertyMap.get(propertyName);
    }
    return value;
  }

  public List getBrowserSensitiveMultiValuedProperty(String propertyName)
  {
    return getBrowserSensitiveMultiValuedProperty(propertyName, true);
  }

  public List getBrowserSensitiveMultiValuedProperty(String propertyName,
    boolean inherit)
  {
    List result;
    String browserPropertyName = propertyName + "." + menuModel.getBrowserType();
    result = (inherit ?
      getMultiValuedProperty(browserPropertyName) :
      getDirectMultiValuedProperty(browserPropertyName));
    if (result == Collections.EMPTY_LIST)
    {
      result = (inherit ?
        getMultiValuedProperty(propertyName) :
        getDirectMultiValuedProperty(propertyName));
    }
    return result;
  }

  public String getURL()
  {
    return evalExpression(getProperty(MenuModel.URL));
  }

  public String getTarget()
  {
    return (String)getDirectProperties().get(MenuModel.TARGET);
  }

  public String getActionURL()
  {
    return MenuUtils.getActionURL(this);
  }

  public String getOnclick()
  {
    return MenuUtils.getOnclick(this);
  }

  public List<String> getViewRoles()
  {
    return getMultiValuedProperty(MenuModel.VIEW_ROLES);
  }

  public List<String> getAccessRoles()
  {
    List<String> accessRoles = getMultiValuedProperty(MenuModel.ACCESS_ROLES);
    if (accessRoles.isEmpty()) accessRoles = getViewRoles();
    return accessRoles;
  }

  public List<String> getEditRoles()
  {
    return getMultiValuedProperty(MenuModel.EDIT_ROLES);
  }

  public String getCertificateRequired()
  {
    return getProperty(MenuModel.CERTIFICATE_REQUIRED);
  }

  public boolean isNull()
  {
    return mid == null;
  }

  public int getChildIndex() // 1 based. Invisible nodes are not counted.
  {
    int index = 0;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        index++;
        node = getPreviousVisibleNode(node.getPreviousSibling());
        while (node != null)
        {
          index++;
          node = getPreviousVisibleNode(node.getPreviousSibling());
        }
      }
    }
    catch (Exception ex)
    {
    }
    return index;
  }

  public int getDepth()
  {
    int depth = 0;
    try
    {
      CNode node = getNode(mid);
      if (node != null) depth = node.getDepth();
    }
    catch (Exception ex)
    {
    }
    return depth;
  }

  public MenuItemCursor[] getCursorPath()
  {
    MenuItemCursor[] ancestors = null;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        String[] path = node.getNodeIdPath();
        ancestors = new MenuItemCursor[path.length];
        for (int i = 0; i < path.length; i++)
        {
          ancestors[i] = new MenuItemCursor(menuModel, path[i]);
        }
      }
    }
    catch (Exception ex)
    {
    }
    return ancestors;
  }

  public String[] getPath()
  {
    String[] path = null;
    try
    {
      CNode node = getNode(mid);
      if (node != null) path = node.getNodeIdPath();
    }
    catch (Exception ex)
    {
    }
    return path;
  }

  public boolean isRoot()
  {
    boolean root = false;
    try
    {
      CNode node = getNode(mid);
      if (node != null) root = node.isRoot();
    }
    catch (Exception ex)
    {
    }
    return root;
  }

  public boolean isLeaf()
  {
    boolean leaf = true;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode firstChildNode = node.getFirstChild();
        if (firstChildNode != null)
        {
          leaf = (getNextVisibleNode(firstChildNode) == null);
        }
      }
    }
    catch (Exception ex)
    {
    }
    return leaf;
  }

  public boolean isRendered()
  {
    String renderedValue =
      getBrowserSensitiveProperty(MenuModel.RENDERED, false);
    renderedValue = TextUtils.replaceReservedWords(renderedValue);
    String value = evalExpression(renderedValue);
    return (value == null || "true".equals(value));
  }

  public boolean isAnyRenderedSibling()
  {
    MenuItemCursor parent = getParent();
    if (parent != null && !parent.isNull())
    {
      MenuItemCursor menuItem = parent.getFirstChild();
      while (menuItem != null && !menuItem.isNull())
      {
        if (menuItem.isRendered() && !menuItem.getMid().equals(mid)) return true;
        menuItem = menuItem.getNext();
      }
    }
    return false;
  }

  public boolean isAnyRenderedChildren()
  {
    MenuItemCursor menuItem = getFirstChild();
    while (menuItem != null && !menuItem.isNull())
    {
      if (menuItem.isRendered()) return true;
      menuItem = menuItem.getNext();
    }
    return false;
  }

  public int getChildCount()
  {
    int childCount = 0;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        node = getNextVisibleNode(node.getFirstChild());
        while (node != null)
        {
          childCount++;
          node = getNextVisibleNode(node.getNextSibling());
        }
      }
    }
    catch (Exception ex)
    {
    }
    return childCount;
  }

  public boolean hasChildren()
  {
    return !isLeaf();
  }

  public boolean isSelected()
  {
    if (mid == null) return false;
    if (mid.equals(menuModel.getSelectedMid())) return true;
    return false;
  }

  public boolean isDescendantOf(String parentMid)
  {
    boolean descendant = false;
    if (mid == null) return false;
    try
    {
      CNode node = getNode(mid);
      node = node.getParent();

      while (node != null && !descendant)
      {
        if (parentMid.equals(node.getNodeId()))
        {
          descendant = true;
        }
        else
        {
          node = node.getParent();
        }
      }
    }
    catch (Exception ex)
    {
    }
    return descendant;
  }

  public boolean isSelectionContained()
  {
    return containsSelection();
  }

  public boolean containsSelection()
  {
    if (mid == null) return false;
    String selectedMid = menuModel.getSelectedMid();
    if (selectedMid == null) return false;
    boolean selected = false;
    try
    {
      CNode node = getNode(selectedMid);
      while (node != null && !selected)
      {
        if (mid.equals(node.getNodeId()))
        {
          selected = true;
        }
        else
        {
          node = node.getParent();
        }
      }
    }
    catch (Exception ex)
    {
    }
    return selected;
  }

  public MenuItemCursor getParent()
  {
    String parentMid = null;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        parentMid = node.getParentNodeId();
      }
    }
    catch (Exception ex)
    {
    }
    return new MenuItemCursor(menuModel, parentMid);
  }

  public MenuItemCursor getParentWithAction()
  {
    try
    {
      MenuItemCursor parent = getParent();
      while (parent != null && !parent.isNull())
      {
        if (parent.getAction() != null) return parent;
        else
        {
          parent = parent.getParent();
        }
      }
    }
    catch (Exception ex)
    {
    }
    return new MenuItemCursor(menuModel, null); //not found
  }

  public MenuItemCursor getFirstChild()
  {
    String firstChildMid = null;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode nextNode = getNextVisibleNode(node.getFirstChild());
        if (nextNode != null) firstChildMid = nextNode.getNodeId();
      }
    }
    catch (Exception ex)
    {
    }
    return new MenuItemCursor(menuModel, firstChildMid);
  }

  public MenuItemCursor getLastChild()
  {
    String lastChildMid = null;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode previousNode = getPreviousVisibleNode(node.getLastChild());
        if (previousNode != null) lastChildMid = previousNode.getNodeId();
      }
    }
    catch (Exception ex)
    {
    }
    return new MenuItemCursor(menuModel, lastChildMid);
  }

  public MenuItemCursor getNext()
  {
    String nextMid = null;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode nextNode = getNextVisibleNode(node.getNextSibling());
        if (nextNode != null) nextMid = nextNode.getNodeId();
      }
    }
    catch (Exception ex)
    {
    }
    return new MenuItemCursor(menuModel, nextMid);
  }

  public MenuItemCursor getPrevious()
  {
    String previousMid = null;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode previousNode = getPreviousVisibleNode(node.getPreviousSibling());
        if (previousNode != null) previousMid = previousNode.getNodeId();
      }
    }
    catch (Exception ex)
    {
    }
    return new MenuItemCursor(menuModel, previousMid);
  }

  public MenuItemCursor getClone()
  {
    return new MenuItemCursor(menuModel, mid);
  }

  public MenuItemCursor getRoot()
  {
    String rootMid = null;
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode rootNode = node.getRoot();
        if (rootNode != null) rootMid = rootNode.getNodeId();
      }
    }
    catch (Exception ex)
    {
    }
    return new MenuItemCursor(menuModel, rootMid);
  }

  public boolean select()
  {
    if (mid != null)
    {
      menuModel.setSelectedMid(mid);
      return true;
    }
    return false;
  }

  public boolean moveNext()
  {
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode nextNode = getNextVisibleNode(node.getNextSibling());
        if (nextNode != null)
        {
          mid = nextNode.getNodeId();
          return true;
        }
      }
    }
    catch (Exception ex)
    {
    }
    mid = null;
    return false;
  }

  public boolean movePrevious()
  {
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode previousNode = getPreviousVisibleNode(node.getPreviousSibling());
        if (previousNode != null)
        {
          mid = previousNode.getNodeId();
          return true;
        }
      }
    }
    catch (Exception ex)
    {
    }
    mid = null;
    return false;
  }

  public boolean moveParent()
  {
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode parentNode = node.getParent();
        if (parentNode != null)
        {
          mid = parentNode.getNodeId();
          return true;
        }
      }
    }
    catch (Exception ex)
    {
    }
    return false;
  }

  public boolean moveFirstChild()
  {
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode nextNode = getNextVisibleNode(node.getFirstChild());
        if (nextNode != null)
        {
          mid = nextNode.getNodeId();
          return true;
        }
      }
    }
    catch (Exception ex)
    {
    }
    mid = null;
    return false;
  }

  public boolean moveLastChild()
  {
    try
    {
      CNode node = getNode(mid);
      if (node != null)
      {
        CNode previousNode = getPreviousVisibleNode(node.getLastChild());
        if (previousNode != null)
        {
          mid = previousNode.getNodeId();
          return true;
        }
      }
    }
    catch (Exception ex)
    {
    }
    mid = null;
    return false;
  }

  @Override
  public boolean equals(Object o)
  {
    if (o instanceof MenuItemCursor)
    {
      MenuItemCursor cursor = (MenuItemCursor)o;
      if (menuModel.getCWorkspace() == cursor.menuModel.getCWorkspace())
      {
        if (mid == null && cursor.mid == null) return true;
        else if (mid.equals(cursor.mid)) return true;
      }
    }
    return false;
  }

  @Override
  public String toString()
  {
    return "[" + mid + "]";
  }

  // ****** private methods *****

  private String evalExpression(String expression)
  {
    return ApplicationBean.getCurrentInstance().evalExpression(expression);
  }

  private CNode getNode(String mid) throws Exception
  {
    return (mid == null) ? null : menuModel.getCWorkspace().getNode(mid);
  }

  private CNode getNextVisibleNode(CNode node) throws Exception
  {
    while (node != null && !menuModel.isVisibleNode(node))
    {
      node = node.getNextSibling();
    }
    return node;
  }

  private CNode getPreviousVisibleNode(CNode node) throws Exception
  {
    while (node != null && !menuModel.isVisibleNode(node))
    {
      node = node.getPreviousSibling();
    }
    return node;
  }

  // ****** class that implements inheritance and localization ******

  class PropertiesMap implements Map
  {
    public int size()
    {
      return getProperties(mid).size();
    }

    public boolean isEmpty()
    {
      return getProperties(mid).isEmpty();
    }

    public boolean containsKey(Object key)
    {
      boolean containsKey = false;
      try
      {
        CNode node = getNode(mid);
        while (!containsKey && node != null)
        {
          Map properties = node.getPropertiesMap();
          if (properties.containsKey(key))
          {
            containsKey = true;
          }
          else
          {
            node = node.getParent();
          }
        }
      }
      catch (Exception ex)
      {
      }
      return containsKey;
    }

    public boolean containsValue(Object value)
    {
      boolean containsValue = false;
      try
      {
        CNode node = getNode(mid);
        while (!containsValue && node != null)
        {
          Map properties = node.getPropertiesMap();
          if (properties.containsValue(value))
          {
            containsValue = true;
          }
          else
          {
            node = node.getParent();
          }
        }
      }
      catch (Exception ex)
      {
      }
      return containsValue;
    }

    public Object get(Object key)
    {
      Object value = null;
      try
      {
        boolean found = false;
        CNode node = getNode(mid);
        while (!found && node != null)
        {
          Map properties = node.getPropertiesMap();
          if (properties.containsKey(key))
          {
            value = properties.get(key);
            found = true;
          }
          else
          {
            node = node.getParent();
          }
        }
      }
      catch (Exception ex)
      {
      }
      return value;
    }

    // always returns an ArrayList. Stops ascending when propertyName is found.
    public List<String> getMultiValued(String propertyName)
      throws Exception
    {
      CNode node = getNode(mid);
      return menuModel.getMultiValuedProperties(node, propertyName);
    }

    public List<String> getDirectMultiValued(String propertyName)
      throws Exception
    {
      CNode node = getNode(mid);

      List<String> values = node.getMultiPropertyValue(propertyName);
      return values == null ? Collections.EMPTY_LIST : values;
    }

    public Object put(Object key, Object value)
    {
      // not implemented
      return null;
    }

    public Object remove(Object key)
    {
      // not implemented
      return null;
    }

    public void putAll(Map t)
    {
      // not implemented
    }

    public void clear()
    {
      // not implemented
    }

    public Set keySet()
    {
      return getProperties(mid).keySet();
    }

    public Collection values()
    {
      return getProperties(mid).values();
    }

    public Set entrySet()
    {
      return getProperties(mid).entrySet();
    }

    public boolean equals(Object o)
    {
      return getProperties(mid).equals(o);
    }

    public int hashCode()
    {
      return getProperties(mid).hashCode();
    }

    private Map getProperties(String mid)
    {
      try
      {
        CNode node = getNode(mid);
        if (node != null)
        {
          return node.getPropertiesMap();
        }
      }
      catch (Exception ex)
      {
      }
      return Collections.EMPTY_MAP;
    }
  }

  //PropertiesMap Decorator (Wrapper) class to add translation functionallity
  class TranslatedPropertiesMap extends PropertiesMap
  {
    private PropertiesMap propertiesMap;

    public TranslatedPropertiesMap(PropertiesMap propertiesMap)
    {
      this.propertiesMap = propertiesMap;
    }

    @Override
    public Object get(Object key)
    {
      Object value = propertiesMap.get(key);

      if (value != null)
      {
        Translator translator =
          ApplicationBean.getCurrentInstance().getTranslator();

        if (translator != null)
        {
          try
          {
            String userLanguage = FacesUtils.getViewLanguage();
            StringWriter sw = new StringWriter();
            translator.translate(new StringReader(String.valueOf(value)), sw,
              "text/plain", userLanguage, "jsp:" + mid);
            return sw.toString();
          }
          catch (Exception ex)
          {
          }
        }
      }
      return value;
    }
  }
}
