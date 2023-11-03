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
package org.santfeliu.webapp;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@SessionScoped
public class ContextTreeBean implements Serializable
{
  private final HashMap<String, TreeNode> rootNodes = new HashMap<>();
  private TreeNode selectedNode;

  @Inject
  NavigatorBean navigatorBean;

  public void clear()
  {
    selectedNode = null;
    rootNodes.clear();
  }

  public void update()
  {
    selectedNode = null;
    String scriptName = getScriptName();
    if (scriptName != null)
    {
      rootNodes.remove(scriptName);
    }
  }

  public TreeNode getTreeNode()
  {
    String scriptName = getScriptName();
    if (scriptName == null) return null;

    TreeNode rootNode = rootNodes.get(scriptName);
    if (rootNode == null)
    {
      rootNode = new DefaultTreeNode("", null);
      rootNodes.put(scriptName, rootNode);

      try
      {
        ScriptClient client = new ScriptClient();
        client.refreshCache();
        List<ObjectData> datas = (List<ObjectData>)client
          .executeScript(scriptName, "getRoots()");

        for (ObjectData data : datas)
        {
          ObjectTreeNode childNode = new ObjectTreeNode(data);
          rootNode.getChildren().add(childNode);
        }
      }
      catch (Exception ex)
      {
        System.out.println(ex);
      }
    }
    return rootNode;
  }

  public boolean isCurrentBaseType(TreeNode node)
  {
    ObjectData data = (ObjectData)node.getData();
    String typeId = data.getTypeId();
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
    if (baseTypeInfo == null) return false;
    return typeId.equals(baseTypeInfo.getBaseTypeId());
  }

  public boolean isSelectedNode(TreeNode node)
  {
    BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
    if (baseTypeInfo == null) return false;
    ObjectData data = (ObjectData)node.getData();
    return data.getObjectId().equals(baseTypeInfo.getObjectId());
  }

  public String getScriptName()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.getSelectedMenuItem().getProperty("contextTree.script");
  }

  public void setSelectedNode(TreeNode treeNode)
  {
    this.selectedNode = treeNode;
  }

  public TreeNode getSelectedNode()
  {
    return selectedNode;
  }

  public String show(TreeNode treeNode)
  {
    ObjectTreeNode objectTreeNode = (ObjectTreeNode)treeNode;
    ObjectData data = objectTreeNode.getData();
    this.selectedNode = objectTreeNode;
    return navigatorBean.show(data.getTypeId(), data.getObjectId());
  }

  public void view(TreeNode treeNode)
  {
    ObjectTreeNode objectTreeNode = (ObjectTreeNode)treeNode;
    ObjectData data = objectTreeNode.getData();
    this.selectedNode = objectTreeNode;
    navigatorBean.view(data.getObjectId(), 0, false);
  }

  public List<ContextAction> getContextActions()
  {
    Map requestMap = FacesContext.getCurrentInstance()
      .getExternalContext().getRequestMap();
    List actions = (List)requestMap.get("contextActions");
    if (actions == null && selectedNode != null)
    {
      try
      {
        ScriptClient client = new ScriptClient();
        ObjectData data = (ObjectData)selectedNode.getData();
        client.put("data", data);
        Object result = client.executeScript(getScriptName(),
          "getContextActions()");
        if (result instanceof List)
        {
          actions = (List)result;
          requestMap.put("contextActions", actions);
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return actions == null ? Collections.EMPTY_LIST : actions;
  }

  public String doContextAction(ContextAction action)
  {
    try
    {
      ObjectTreeNode objectTreeNode = (ObjectTreeNode)selectedNode;
      ObjectData data = objectTreeNode.getData();
      System.out.println("ACTION: " + action);
      System.out.println("selectedNode: " + data.getDescription());

      ScriptClient client = new ScriptClient();
      client.put("data", data);
      Object result = client.executeScript(getScriptName(),
        action.getMethodName() + "()");

      System.out.println("Result: " + result);

      String actionType = action.getType();
      if (ContextAction.ADD.equals(actionType))
      {
        selectedNode.setExpanded(true);

        if (result instanceof ObjectData)
        {
          // add this ObjectData
          ObjectData newData = (ObjectData)result;
          ObjectTreeNode newNode = new ObjectTreeNode(newData);

          selectedNode.getChildren().add(newNode);
          this.selectedNode = newNode;
          
          return navigatorBean.show(newData.typeId, newData.objectId);
        }
        else
        {
          objectTreeNode.update();
        }
      }
      else if (ContextAction.UPDATE.equals(actionType))
      {
        objectTreeNode.update();
      }
      else if (ContextAction.REMOVE.equals(actionType))
      {
        if (selectedNode.getParent() instanceof ObjectTreeNode)
        {
          navigatorBean.remove(data.typeId, data.objectId);
          ((ObjectTreeNode)objectTreeNode.getParent()).update();
        }
        else
        {
          clear();
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return null;
  }

  public static class ObjectTreeNode extends DefaultTreeNode<ObjectData>
  {
    protected boolean loaded;

    public ObjectTreeNode(ObjectData data)
    {
      super(data);
    }

    @Override
    public boolean isLeaf()
    {
      load();

      return super.isLeaf();
    }

    @Override
    public boolean isSelected()
    {
      ContextTreeBean contextTreeBean = WebUtils.getBean("contextTreeBean");
      return this == contextTreeBean.getSelectedNode();
    }

    void load()
    {
      if (!loaded)
      {
        loaded = true;

        List<ObjectData> childDatas = getChildObjects(getData());

        for (ObjectData childData : childDatas)
        {
          ObjectTreeNode childNode = new ObjectTreeNode(childData);
          getChildren().add(childNode);
        }
      }
    }

    public void update()
    {
      loaded = false;
      getChildren().clear();
    }

    List<ObjectData> getChildObjects(ObjectData data)
    {
      try
      {
        ContextTreeBean contextTreeBean = WebUtils.getBean("contextTreeBean");

        ScriptClient client = new ScriptClient();
        client.put("data", data);
        List<ObjectData> datas = (List<ObjectData>)client
          .executeScript(contextTreeBean.getScriptName(), "getChildren()");

        return datas;
      }
      catch (Exception ex)
      {
        System.out.println(ex);
      }
      return Collections.EMPTY_LIST;
    }
  }

  public static class ObjectData implements Serializable
  {
    String typeId;
    String objectId;

    public ObjectData(String typeId, String objectId)
    {
      this.typeId = typeId;
      this.objectId = objectId;
    }

    public String getTypeId()
    {
      return typeId;
    }

    public String getObjectId()
    {
      return objectId;
    }

    public String getDescription()
    {
      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(typeId);
      if (baseTypeInfo == null)
      {
        return typeId + "@" + objectId;
      }
      TypeBean typeBean = TypeBean.getInstance(baseTypeInfo.getBaseTypeId());
      return typeBean.getDescription(objectId);
    }

    public String getIcon()
    {
      NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
      BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(typeId);
      if (baseTypeInfo == null)
      {
        return "pi pi-folder";
      }
      return baseTypeInfo.getIcon();
    }

    @Override
    public String toString()
    {
      return typeId + "@" + objectId;
    }
  }

  public static class ContextAction implements Serializable
  {
    public static final String NOP = "NOP";
    public static final String ADD = "ADD";
    public static final String UPDATE = "UPDATE";
    public static final String REMOVE = "REMOVE";

    String type = NOP;
    String methodName;
    String methodParams;
    String label;

    public ContextAction()
    {
    }

    public ContextAction(String type, String methodName, String label)
    {
      this(type, methodName, "", label);
    }
    
    public ContextAction(String type, String methodName, String methodParams, 
      String label)
    {
      this.type = type;
      this.methodName = methodName;
      this.methodParams = methodParams;
      this.label = label;
    }    
    
    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public String getMethodName()
    {
      return methodName;
    }

    public void setMethodName(String methodName)
    {
      this.methodName = methodName;
    }

    public String getMethodParams()
    {
      return methodParams;
    }

    public void setMethodParams(String methodParams)
    {
      this.methodParams = methodParams;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    @Override
    public String toString()
    {
      return type + "/" + methodName;
    }
  }
}
