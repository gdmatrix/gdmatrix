package org.santfeliu.faces.tree;

import java.util.HashMap;

import org.apache.myfaces.custom.tree2.TreeNodeBase;

public class ExtendedTreeNode extends TreeNodeBase
{
  private HashMap properties = new HashMap();

  public ExtendedTreeNode()
  {
  }

  public ExtendedTreeNode(String type, String desc, boolean isLeaf)
  {
    super(type, desc, isLeaf);
  }

  public void setProperties(HashMap properties)
  {
    this.properties = properties;
  }

  public HashMap getProperties()
  {
    return properties;
  }
}
