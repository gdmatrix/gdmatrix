package org.santfeliu.faces.tree;


import java.util.Iterator;

import java.util.Map;

import org.apache.myfaces.custom.tree2.TreeNodeBase;

import org.santfeliu.util.Table;


public class TreeModelUtils
{
  public TreeModelUtils()
  {
  }

  /**
   * table that contains node information sorted by pathColumn
   * @param table
   * @param pathColumn
   * @return
   */
  public static TreeNodeBase createTree(Table table, 
    String pathSeparator, String pathColumn) 
    throws Exception
  {
    ExtendedTreeNode rootNode = 
      new ExtendedTreeNode("document", "root", true);
    for (int i = 0; i < table.getRowCount(); i++)
    {
      String path = (String)table.getElementAt(i, pathColumn);
      Table.Row row = table.getRow(i);
      String[] pathArray = path.split(pathSeparator);
      addNode(rootNode, pathArray, row);
    }
    return rootNode;
  }

  private static ExtendedTreeNode createNode(
    String type, String desc, String info,
    Map properties) throws Exception
  {
    ExtendedTreeNode node = new ExtendedTreeNode();
    node.setDescription(desc);
    node.setType(type);
    if (properties != null) node.getProperties().putAll(properties);
    node.getProperties().put("info", info);
    return node;
  }

  /* private methods */
  private static void addNode(
    TreeNodeBase rootNode, String[] pathArray, Map row)
    throws Exception
  {
    TreeNodeBase currentNode = rootNode;
    for (int i = 0; i < pathArray.length; i++)
    {
      String pathElem = pathArray[i];
      String[] name = splitName(pathElem);
      String desc = name[0];
      String info = name[1];
      
      TreeNodeBase childNode = findChildNode(currentNode, desc);
      if (childNode == null)
      {
        boolean isLeaf = (i == pathArray.length - 1);
        if (isLeaf)
        {
          childNode = createNode("document", desc, info, row);
        }
        else // create inner tree node
        {
          childNode = createNode("folder", desc, info, null);
        }
        currentNode.getChildren().add(childNode);
      }
      currentNode.setType("folder");
      currentNode.setLeaf(false);
      currentNode = childNode;
    }
  }
  
  private static TreeNodeBase findChildNode(TreeNodeBase node, String pathElem)
  {
    TreeNodeBase childNode = null;
    boolean found = false;
    Iterator iter = node.getChildren().iterator();
    while (!found && iter.hasNext())
    {
      childNode = (TreeNodeBase)iter.next();
      String nodeDesc = childNode.getDescription();
      found = nodeDesc.equals(pathElem);
    }
    return found ? childNode : null;
  }
  
  private static String[] splitName(String name)
  {
    int index = name.indexOf("=");
    if (index == -1) return new String[]{name, null};
    else
    {
      String folderDesc = name.substring(0, index);
      String folderInfo = name.substring(index + 1);
      return new String[]{folderDesc, folderInfo};
    }
  }
}
