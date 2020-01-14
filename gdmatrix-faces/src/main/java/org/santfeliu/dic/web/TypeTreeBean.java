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
