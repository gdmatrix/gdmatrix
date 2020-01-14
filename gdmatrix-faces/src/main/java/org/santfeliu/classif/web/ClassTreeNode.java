package org.santfeliu.classif.web;

import java.util.ArrayList;
import java.util.List;
import org.santfeliu.classif.Class;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.santfeliu.classif.ClassCache;

/**
 *
 * @author realor
 */
public class ClassTreeNode implements TreeNode
{
  // Class identifier
  private String classId;
  // dateTime snapshot
  private String dateTime;
  // Link to Class to reduce the number of calls to TypeCache during tree rendering
  private transient Class classObject;
  // list of children nodes
  private transient List<ClassTreeNode> children;

  public ClassTreeNode(String classId, String dateTime)
  {
    this.classId = classId;
    this.dateTime = dateTime;
  }

  public String getType()
  {
    return "folder";
  }

  public void setType(String type)
  {
  }

  public String getDescription()
  {
    String description = null;
    linkClass();
    if (classObject != null)
    {
      description = classObject.getTitle();
    }
    return description;
  }

  public void setDescription(String description)
  {
  }

  public void setIdentifier(String identifier)
  {
    this.classId = identifier;
  }

  public String getIdentifier()
  {
    return classId;
  }

  public int getChildCount()
  {
    linkClass();
    if (classObject != null)
    {
      return classObject.getSubClassesCount();
    }
    return 0;
  }

  public boolean isLeaf()
  {
    linkClass();
    if (classObject != null)
    {
      return classObject.isLeaf();
    }
    return true;
  }

  public void setLeaf(boolean arg0)
  {
  }

  public List getChildren()
  {
    if (children == null)
    {
      children = new ArrayList<ClassTreeNode>();
      linkClass();
      if (classObject != null)
      {
        List<String> subClassIds = classObject.getSubClassIds();
        for (String subClassId : subClassIds)
        {
          children.add(new ClassTreeNode(subClassId, dateTime));
        }
      }
    }
    return children;
  }

  public boolean isOpen()
  {
    linkClass();
    if (classObject != null)
    {
      return classObject.isOpen();
    }
    return true;
  }

  public Class getProperties()
  {
    linkClass();
    return classObject;
  }

  private void linkClass()
  {
    if (classObject == null)
    {
      ClassCache classCache = ClassCache.getInstance(dateTime);
      classObject = classCache.getClass(classId);
    }
  }
}
