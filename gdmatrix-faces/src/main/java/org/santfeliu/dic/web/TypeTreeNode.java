package org.santfeliu.dic.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

/**
 *
 * @author realor
 */
public class TypeTreeNode implements TreeNode, Serializable
{
  // Type identifier
  private String typeId;
  // index to first derivedTypeId
  private int firstIndex;
  // list of children nodes
  private List<TreeNode> children;

  // transient objects to reduce serialization size and to get updated data
  private transient Type _type;
  private transient List<TreeNode> _children;

  public TypeTreeNode(String typeId)
  {
    this.typeId = typeId;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public String getType()
  {
    return "type";
  }

  public void setType(String type)
  {
  }

  public String getDescription()
  {
    String description = null;
    linkType();
    if (_type != null)
    {
      description = _type.getDescription();
    }
    return description;
  }

  public void setDescription(String description)
  {
  }

  public void setIdentifier(String identifier)
  {
    this.typeId = identifier;
  }

  public String getIdentifier()
  {
    return typeId;
  }

  public int getChildCount()
  {
    return getChildren().size();
  }

  public int getDerivedTypeCount()
  {
    linkType();
    if (_type != null)
    {
      return _type.getDerivedTypeCount();
    }
    return 0;
  }

  public boolean isLeaf()
  {
    linkType();
    if (_type != null)
    {
      return _type.isLeaf();
    }
    return true;
  }

  public void setLeaf(boolean arg0)
  {
  }

  public List getChildren()
  {
    if (_children == null)
    {
      _children = new ArrayList<TreeNode>();
      linkType();
      if (_type != null)
      {
        List<String> derivedTypeIds = _type.getDerivedTypeIds();
        if (firstIndex > 0)
        {
          _children.add(new PreviousTypeTreeNode(this));
        }
        int count = 0;
        int i;
        int nodesPerPage = TypeTreeBean.getNodesPerPage();
        for (i = firstIndex; i >= 0 && i < derivedTypeIds.size() &&
          count < nodesPerPage; i++)
        {
          String derivedTypeId = derivedTypeIds.get(i);
          // try to preserve old node to mantain pagination
          TypeTreeNode node = findOldTypeTreeNode(derivedTypeId);
          if (node == null) node = new TypeTreeNode(derivedTypeId);
          _children.add(node);
          count++;
        }
        if (i < derivedTypeIds.size())
        {
          _children.add(new NextTypeTreeNode(this));
        }
        children = _children;
      }
    }
    return _children;
  }

  public int getFirstIndex()
  {
    return firstIndex;
  }

  public void setFirstIndex(int firstIndex)
  {
    this.firstIndex = firstIndex;
    children = null; // refresh children
    _children = null;
  }

  public int moveFirstIndexTo(String derivedTypeId)
  {
    linkType();
    if (_type != null)
    {
      int index = _type.getDerivedTypeIds().indexOf(derivedTypeId);
      if (index != -1)
      {
        int nodesPerPage = TypeTreeBean.getNodesPerPage();
        firstIndex = (index / nodesPerPage) * nodesPerPage;
        return index % nodesPerPage + (firstIndex > 0 ? 1 : 0);
      }
    }
    return -1;
  }

  public Type getProperties()
  {
    linkType();
    return _type;
  }

  private void linkType()
  {
    if (_type == null)
    {
      TypeCache typeCache = TypeCache.getInstance();
      _type = typeCache.getType(typeId);
    }
  }

  private TypeTreeNode findOldTypeTreeNode(String derivedTypeId)
  {
    TypeTreeNode node = null;
    if (children != null)
    {
      int i = 0;
      while (node == null && i < children.size())
      {
        TreeNode currentNode = children.get(i);
        if (currentNode instanceof TypeTreeNode)
        {
          String currentTypeId = ((TypeTreeNode)currentNode).getTypeId();
          if (derivedTypeId.equals(currentTypeId))
          {
            node = (TypeTreeNode)currentNode;
          }
        }
        i++;
      }
    }
    return node;
  }
}
