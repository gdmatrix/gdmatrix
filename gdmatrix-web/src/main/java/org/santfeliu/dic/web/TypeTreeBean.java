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
package org.santfeliu.dic.web;

import java.io.Serializable;
import java.util.List;
import org.apache.myfaces.custom.tree2.TreeModel;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeStateBase;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class TypeTreeBean extends PageBean
{
  @CMSProperty
  public static final String TYPE_TREE_MID_PROPERTY = "typeTreeMid";
  @CMSProperty
  public static final String NODES_PER_PAGE_PROPERTY = "nodesPerPage";

  private Filter filter = new Filter();
  private TreeModel treeModel;
  private TreeStateBase treeState = new TreeStateBase();

  public Filter getFilter()
  {
    return filter;
  }

  public void setFilter(Filter filter)
  {
    this.filter = filter;
  }

  public TreeModel getTreeModel()
  {
    return treeModel;
  }

  public boolean isSelectedType()
  {
    String typeId = (String)getValue("#{node.identifier}");
    TypeBean typeBean  = (TypeBean)getBean("typeBean");
    return typeId.equals(typeBean.getObjectId());
  }

  public static int getNodesPerPage()
  {
    int nodesPerPage = 10; // default;
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String value = userSessionBean.getSelectedMenuItem().
      getProperty(NODES_PER_PAGE_PROPERTY);
    if (value != null)
    {
      try
      {
        nodesPerPage = Integer.parseInt(value);
      }
      catch (Exception ex)
      {
        // ignore
      }
    }
    return nodesPerPage;
  }

  // actions

  public String expandType(String typeId)
  {
    TypeCache typeCache = TypeCache.getInstance();
    Type type = typeCache.getType(typeId);
    if (type != null)
    {
      String rootTypeId = type.getRootTypeId();
      if (!rootTypeId.equals(filter.getRootTypeId()))
      {
        filter.setRootTypeId(rootTypeId);
        // create new treeState if root changes
        treeState = new TreeStateBase();
      }
      // create treeModel
      TypeTreeNode root = new TypeTreeNode(rootTypeId);
      treeModel = new TreeModelBase(root);
      treeModel.setTreeState(treeState);
      System.out.println("expanding root");
      String nodePath = "0";
      if (!treeState.isNodeExpanded(nodePath))
      {
        treeState.toggleExpanded(nodePath);
      }
      List<String> typePathList = type.getTypePathList();      
      TypeTreeNode typeTreeNode = root;
      int pathIndex = 1;
      int pageIndex = 0;
      while (pathIndex < typePathList.size() && pageIndex >= 0)
      {
        String currentTypeId = typePathList.get(pathIndex);
        pageIndex = typeTreeNode.moveFirstIndexTo(currentTypeId);
        if (pageIndex >= 0)
        {
          if (pathIndex + 1 < typePathList.size()) // do not expand typeId
          {
            nodePath = nodePath + ":" + pageIndex;
            if (!treeState.isNodeExpanded(nodePath))
            {
              treeState.toggleExpanded(nodePath);
            }
          }
          typeTreeNode = (TypeTreeNode)treeModel.getNodeById(nodePath);
          pathIndex++;
        }
      }
    }
    String mid = getTypeTreeMid();
    ControllerBean controllerBean = ControllerBean.getCurrentInstance();
    return controllerBean.search(mid);
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      (String)getValue("#{node.identifier}"));
  }
  
  @Override
  @CMSAction
  public String show()
  {
    return "type_tree";
  }

  public String search()
  {
    TypeCache typeCache = TypeCache.getInstance();
    if (typeCache.getType(filter.getRootTypeId()) != null)
    {
      String rootTypeId = filter.getRootTypeId();
      if (rootTypeId != null && rootTypeId.trim().length() > 0)
      {
        TypeTreeNode root = new TypeTreeNode(rootTypeId);
        treeModel = new TreeModelBase(root);
        treeState = new TreeStateBase();
        treeModel.setTreeState(treeState);
        treeState.toggleExpanded("0");
      }
    }
    else
    {
      treeModel = null;
      error("dic:TYPE_NOT_FOUND");
    }
    return show();
  }

  public boolean isShowInTreeEnabled()
  {
    return getTypeTreeMid() != null;
  }

  public class Filter implements Serializable
  {
    private String rootTypeId;
    private boolean showTypeId;

    public String getRootTypeId()
    {
      return rootTypeId;
    }

    public void setRootTypeId(String rootTypeId)
    {
      this.rootTypeId = rootTypeId;
    }

    public boolean isShowTypeId()
    {
      return showTypeId;
    }

    public void setShowTypeId(boolean showTypeId)
    {
      this.showTypeId = showTypeId;
    }
  }

  private String getTypeTreeMid()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    return userSessionBean.getMenuModel().
      getSelectedMenuItem().getProperty(TYPE_TREE_MID_PROPERTY);
  }
}
