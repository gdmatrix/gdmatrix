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
package org.santfeliu.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.matrix.cms.Property;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.cms.CNode;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj-sf
 */
public class JSONUtils
{
  private static final String PROPERTY_SEPARATOR = "::";

  public static JSONObject getJSON(String nodeId)
  {
    return getJSON(nodeId, "");
  }

  public static JSONObject getJSON(String nodeId, String basePath)
  {
    try
    {
      CNode node = getNode(nodeId);
      if (node != null)
      {
        TreeNode<String> rootTreeNode = getTreeNode(node, basePath);
        return getJSON(rootTreeNode);
      }
    }
    catch (Exception ex)
    {
    }
    return null;
  }

  public static String getJSONString(String nodeId)
  {
    return getJSONString(nodeId, "");
  }

  public static String getJSONString(String nodeId, String basePath)
  {
    Gson gson = new GsonBuilder()
      .setPrettyPrinting().disableHtmlEscaping().create();
    return gson.toJson(getJSON(nodeId, basePath));
  }

  public static List<Property> getProperties(String jsonString)
  {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Map<String, Object> map = gson.fromJson(jsonString, HashMap.class);
    return getPropertiesFromJsonMap(map, "");
  }

  /* PRIVATE METHODS */

  private static TreeNode<String> getTreeNode(CNode node, String basePath)
  {
    TreeNode<String> rootTreeNode = new DefaultTreeNode("Root", null, null);
    List<Property> propertyList = node.getProperties(true);
    for (Property property : propertyList)
    {
      if (property.getName().startsWith(basePath))
      {
        TreeNode<String> parentNode = rootTreeNode;
        String[] path = property.getName().split(PROPERTY_SEPARATOR);
        for (String pathItem : path)
        {
          TreeNode<String> auxNode = getDescendant(parentNode, pathItem);
          if (auxNode == null)
          {
            auxNode = new DefaultTreeNode("Node", pathItem, parentNode);
          }
          else
          {
            //nothing to do
          }
          parentNode = auxNode;
        }
        parentNode.setType("Property");
        for (String value : property.getValue())
        {
          new DefaultTreeNode("Value", value, parentNode);
        }
      }
    }
    return rootTreeNode;
  }

  private static TreeNode<String> getDescendant(TreeNode<String> treeNode,
    String search)
  {
    for (TreeNode<String> child : treeNode.getChildren())
    {
      if (search.equals(child.getData())) return child;
    }
    return null;
  }

  private static boolean allDescendantsAreInteger(TreeNode<String> treeNode)
  {
    if (treeNode.getChildren().isEmpty()) return false;

    for (TreeNode<String> child : treeNode.getChildren())
    {
      try
      {
        Integer.parseInt(child.getData());
      }
      catch (NumberFormatException ex)
      {
        return false;
      }
    }
    return true;
  }

  private static JSONObject getJSON(TreeNode<String> treeNode)
  {
    JSONObject jsonObject = new JSONObject();
    for (TreeNode<String> child : treeNode.getChildren())
    {
      addToJSON(jsonObject, child);
    }
    return jsonObject;
  }

  private static void addToJSON(JSONObject jsonObject,
    TreeNode<String> treeNode)
  {
    if (treeNode.getType().equals("Node"))
    {
      if (allDescendantsAreInteger(treeNode))
      {
        JSONArray jsonArray = new JSONArray();
        jsonObject.put(treeNode.getData(), jsonArray);
        List<TreeNode<String>> children = new ArrayList(treeNode.getChildren());
        Collections.sort(children, new Comparator<TreeNode<String>>()
          {
            @Override
            public int compare(TreeNode<String> n1, TreeNode<String> n2)
            {
              return (Integer.parseInt(n1.getData()) -
                Integer.parseInt(n2.getData()));
            }
          }
        );
        for (TreeNode<String> child : children)
        {
          jsonArray.add(getJSON(child));
        }
      }
      else
      {
        JSONObject json = new JSONObject();
        jsonObject.put(treeNode.getData(), json);
        for (TreeNode<String> child : treeNode.getChildren())
        {
          addToJSON(json, child);
        }
      }
    }
    else if (treeNode.getType().equals("Property"))
    {
      String propertyName = (String)treeNode.getData();
      if (treeNode.getChildCount() > 1)
      {
        JSONArray values = new JSONArray();
        for (TreeNode<String> child : treeNode.getChildren())
        {
          Object jsonValue = getJsonValue(child.getData());
          values.add(jsonValue);
        }
        jsonObject.put(propertyName, values);
      }
      else
      {
        Object jsonValue =
          getJsonValue(treeNode.getChildren().get(0).getData());
        jsonObject.put(propertyName, jsonValue);
      }
    }
  }

  private static Object getJsonValue(String stringValue)
  {
    if (NumberUtils.isNumber(stringValue))
    {
      try
      {
        return Long.parseLong(stringValue);
      }
      catch (NumberFormatException ex)
      {
        try
        {
          return Double.parseDouble(stringValue);
        }
        catch (NumberFormatException ex2)
        {
          return stringValue;
        }
      }
    }
    else
    {
      if ("true".equals(stringValue) || "false".equals(stringValue)) //Boolean
      {
        return Boolean.parseBoolean(stringValue);
      }
      else //String
      {
        return stringValue;
      }
    }
  }

  private static String getStringValue(Object jsonValue)
  {
    if (jsonValue instanceof Double && (Double)jsonValue % 1 == 0) //integer
    {
      return String.valueOf(Math.round((Double)jsonValue));
    }
    else //double, boolean or string
    {
      return String.valueOf(jsonValue);
    }
  }

  private static CNode getNode(String nodeId) throws Exception
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    return (nodeId == null ? null : menuModel.getCWorkspace().getNode(nodeId));
  }

  private static List<Property> getPropertiesFromJsonMap(
    Map<String, Object> map, String prefix)
  {
    List<Property> result = new ArrayList();
    for (String name : map.keySet())
    {
      Object value = map.get(name);
      if (value instanceof Double || value instanceof Boolean ||
        value instanceof String)
      {
        Property prop = new Property();
        prop.setName(prefix + name);
        prop.getValue().add(getStringValue(value));
        result.add(prop);
      }
      else if (value instanceof Map)
      {
        result.addAll(getPropertiesFromJsonMap((Map)value,
          prefix + name + PROPERTY_SEPARATOR));
      }
      else if (value instanceof List)
      {
        List values = (List)value;
        if (values.size() > 0)
        {
          if (values.get(0) instanceof Map) //list of maps
          {
            for (int i = 0; i < values.size(); i++)
            {
              result.addAll(getPropertiesFromJsonMap((Map)values.get(i),
                prefix + name + PROPERTY_SEPARATOR + i + PROPERTY_SEPARATOR));
            }
          }
          else //list of single values
          {
            Property prop = new Property();
            prop.setName(prefix + name);
            for (Object jsonValue : values)
            {
              prop.getValue().add(getStringValue(jsonValue));
            }
            result.add(prop);
          }
        }
      }
    }
    return result;
  }

}
