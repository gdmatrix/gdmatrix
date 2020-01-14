package org.santfeliu.dic.web;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.apache.myfaces.custom.tree2.TreeNode;

/**
 *
 * @author realor
 */
public class PreviousTypeTreeNode implements TreeNode, Serializable
{
  private TypeTreeNode typeTreeNode;

  public PreviousTypeTreeNode(TypeTreeNode typeTreeNode)
  {
    this.typeTreeNode = typeTreeNode;
  }

  public TypeTreeNode getTypeTreeNode()
  {
    return typeTreeNode;
  }

  public boolean isLeaf()
  {
    return true;
  }

  public void setLeaf(boolean leaf)
  {
  }

  public List getChildren()
  {
    return Collections.EMPTY_LIST;
  }

  public String getType()
  {
    return "previous";
  }

  public void setType(String type)
  {
  }

  public String getDescription()
  {
    return "previous";
  }

  public void setDescription(String description)
  {
  }

  public void setIdentifier(String id)
  {
  }

  public String getIdentifier()
  {
    return typeTreeNode.getTypeId() + "-previous";
  }

  public int getChildCount()
  {
    return 0;
  }

  public int getNextChildCount()
  {
    return 0;
  }

  public int getPreviousChildCount()
  {
    return typeTreeNode.getFirstIndex();
  }

  public String previousPage()
  {
    int newFirstIndex = typeTreeNode.getFirstIndex();
      newFirstIndex -= TypeTreeBean.getNodesPerPage();
    typeTreeNode.setFirstIndex(newFirstIndex);
    return null;
  }
}
