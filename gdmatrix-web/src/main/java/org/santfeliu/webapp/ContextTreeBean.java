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
import org.mozilla.javascript.Callable;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.santfeliu.util.script.ScriptClient;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.webapp.NavigatorBean.BaseTypeInfo;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
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
  private ObjectTreeNode editingNode; 
  private ScriptClient scriptClient;
  private String scriptName;

  @Inject
  NavigatorBean navigatorBean;

  public void clear()
  {
    selectedNode = null;
    editingNode = null;
    scriptClient = null;
    scriptName = null;
    rootNodes.clear();
  }

  public void update()
  {
    selectedNode = null;
    editingNode = null;
    scriptClient = null;
    if (scriptName != null)
    {
      rootNodes.remove(scriptName);
    }
    scriptName = null;
  }

  public TreeNode getTreeNode()
  {
    if (scriptName == null || !scriptName.equals(getScriptName()))
    {
      scriptName = getScriptName();
      if (scriptName == null) return null;      
      scriptClient = new ScriptClient();
    }
    
    TreeNode rootNode = rootNodes.get(scriptName);
    if (rootNode == null)
    {
      rootNode = new DefaultTreeNode("", null);
      rootNodes.put(scriptName, rootNode);

      try
      {
        scriptClient.refreshCache();
        List<ObjectData> datas = (List<ObjectData>)scriptClient
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

  public boolean isCurrentBaseType(ObjectTreeNode node)
  {
    if (node != null && node.isObjectNode())
    {
      ObjectData data = (ObjectData)node.getData();
      String typeId = data.getTypeId();
      BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
      if (baseTypeInfo == null) return false;
      return typeId.equals(baseTypeInfo.getBaseTypeId());
    }
    return false;
  }

  public boolean isSelectedNode(ObjectTreeNode node)
  {
    if (node != null && node.isObjectNode())
    {
      BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo();
      if (baseTypeInfo == null) return false;
      ObjectData data = (ObjectData)node.getData();
      return data.getObjectId().equals(baseTypeInfo.getObjectId());
    }
    return false;
  }
  
  public boolean isEditingNode(String objectId)
  {
    if (editingNode == null)
      return false;
    
    return editingNode.getData().getObjectId().equals(objectId);
  }  

  public String getScriptName()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.getSelectedMenuItem().getProperty("contextTree.script");
  }

  public void setSelectedNode(TreeNode treeNode)
  {
    expandNode(treeNode);
    this.selectedNode = treeNode;
  }
  
  public void setSelectedNode(String objectId)
  {
    TreeNode node = getTreeNode(objectId);
    if (node != null)
      setSelectedNode(node);
  }
  
  public TreeNode getSelectedNode()
  {
    return selectedNode;
  }
  
  public void onNodeSelect(NodeSelectEvent event) 
  {
    ObjectTreeNode treeNode = (ObjectTreeNode) event.getTreeNode();

    if (treeNode.isObjectNode())
    {     
      if (isCurrentBaseType(treeNode))
        view(treeNode);
      else
        show(treeNode);
    }
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
        if (scriptName == null || !scriptName.equals(getScriptName()))
        {
          scriptClient = new ScriptClient();
          scriptName = getScriptName();
        }
        ObjectData data = (ObjectData)selectedNode.getData();
        scriptClient.put("data", data);
        Object result = scriptClient.executeScript(scriptName,
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

      if (scriptName == null || !scriptName.equals(getScriptName()))
      {
        scriptClient = new ScriptClient();
        scriptName = getScriptName();
      }
      scriptClient.put("data", data);
      scriptClient.executeScript(scriptName);
      Callable callable = (Callable) scriptClient.get(action.getMethodName());
      Object result = scriptClient.execute(callable, action.getMethodParams());

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
      else if (ContextAction.EDIT.equals(actionType))
      {
        if (result instanceof ObjectData)
          editingNode = objectTreeNode;
        else
        {
          if (editingNode.getParent() instanceof ObjectTreeNode)
          {
            ObjectTreeNode parentNode = 
              ((ObjectTreeNode)editingNode.getParent());
            parentNode.setExpanded(true);          
            parentNode.update();            
          }
          editingNode = null;
        }
               
        objectTreeNode.update();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return null;
  }
  
  private void expandNode(TreeNode node)
  {
    if (node != null)
    {
      node.setExpanded(true);
      if (node.getParent() != null)
        expandNode(node.getParent());
    }
  }
  
  private TreeNode getTreeNode(String objectId)
  {
    return getTreeNode(objectId, getTreeNode());
  }
  
  private TreeNode getTreeNode(String objectId, TreeNode node)
  {
    if (objectId == null || node == null)
      return null;
    
    if (node instanceof ObjectTreeNode)
    {
      String nodeId = ((ObjectTreeNode)node).getData().getObjectId();
      if (objectId.equals(nodeId))
        return node;
    }
    
    if (!node.isLeaf())
    {
      List<TreeNode> children = node.getChildren();
      for (int i = 0; i < children.size(); i++)
      {
        TreeNode result = getTreeNode(objectId, children.get(i));
        if (result != null)
          return result;
      } 
    }    
    return null;
  }
  

  public class ObjectTreeNode extends DefaultTreeNode<ObjectData>
  {
    static final String GROUP_TYPE = "GROUP";
    static final String OBJECT_TYPE = "OBJECT";
    
    protected boolean loaded;

    public ObjectTreeNode(ObjectData data)
    {
      super(data);
      if (data.getTypeId() == null)
        setType(GROUP_TYPE);
      else
        setType(OBJECT_TYPE);
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
    
    public boolean isGroupNode()
    {
      return getType().equals(GROUP_TYPE);
    }
    
    public boolean isObjectNode()
    {
      return getType().equals(OBJECT_TYPE);
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
          if (selectedNode != null 
            && childNode.getRowKey().equals(selectedNode.getRowKey()))
          {
            setSelectedNode(childNode);
          }        
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

        if (scriptName == null || !scriptName.equals(getScriptName()))
        {
          scriptClient = new ScriptClient();
          scriptName = getScriptName();
        }
        scriptClient.put("data", data);
        List<ObjectData> datas = (List<ObjectData>)scriptClient
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
    String description;
    String icon;
    
    public ObjectData()
    {
      this(null, NEW_OBJECT_ID);
    }
    
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
    
    public void setDescription(String description)
    {
      this.description = description;
    }

    public String getDescription()
    {
      if (description != null)
        return description;
      else
      {
        NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
        if (typeId != null)
        {
          BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(typeId);
          if (baseTypeInfo == null)
            return typeId + "@" + objectId;
        
          TypeBean typeBean = TypeBean.getInstance(baseTypeInfo.getBaseTypeId());
          return typeBean.getDescription(objectId);
        }
        else
          return objectId;
      }
    }

    public void setIcon(String icon)
    {
      this.icon = icon;
    }

    public String getIcon()
    {
      if (icon != null)
        return icon;
      else
      {
        NavigatorBean navigatorBean = WebUtils.getBean("navigatorBean");
        if (typeId != null)
        {
          BaseTypeInfo baseTypeInfo = navigatorBean.getBaseTypeInfo(typeId);
          if (baseTypeInfo != null)
            return baseTypeInfo.getIcon();
        }
        return "pi pi-folder";
      }
    }

    @Override
    public String toString()
    {
      if (typeId != null)
        return typeId + "@" + objectId;
      else if (description != null)
        return description;
      else
        return objectId;
    }
  }
  
  public static class ContextAction implements Serializable
  {
    public static final String NOP = "NOP";
    public static final String ADD = "ADD";
    public static final String UPDATE = "UPDATE";
    public static final String REMOVE = "REMOVE";
    public static final String EDIT = "EDIT";
   
    String type = NOP;
    String methodName;
    Object[] methodParams;
    String label;
    String icon;

    public ContextAction()
    {
    }

    public ContextAction(String type, String methodName, String label)
    {
      this(type, methodName, null, label, null);
    }
    
    public ContextAction(String type, String methodName, String label, 
      String icon)
    {
      this(type, methodName, null, label, icon);
    }    
    
    public ContextAction(String type, String methodName, Object[] methodParams, 
      String label, String icon)
    {
      this.type = type;
      this.methodName = methodName;
      this.methodParams = methodParams;
      this.label = label;
      this.icon = icon;
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

    public Object[] getMethodParams()
    {
      return methodParams;
    }

    public void setMethodParams(Object[] methodParams)
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

    public String getIcon()
    {
      return icon;
    }

    public void setIcon(String icon)
    {
      this.icon = icon;
    }

    @Override
    public String toString()
    {
      return type + "/" + methodName;
    }
  }
}
