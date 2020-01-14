package org.santfeliu.news.web;

import java.util.HashSet;
import java.util.Set;
import org.apache.myfaces.custom.tree2.TreeNodeChecked;
import org.matrix.news.NewSection;

public class NewSectionTreeNode extends TreeNodeChecked
{
  private String mid;
  private Set<String> updateRoles = new HashSet<String>();
  private NewSection newSection;
  private boolean sticky;
  
  public NewSectionTreeNode()
  {
  }

  public void setMid(String mid)
  {
    this.mid = mid;
  }

  public String getMid()
  {
    return mid;
  }

  public void setUpdateRoles(Set<String> updateRoles)
  {
    this.updateRoles = updateRoles;
  }

  public Set<String> getUpdateRoles()
  {
    return updateRoles;
  }

  public void setNewSection(NewSection newSection)
  {
    this.newSection = newSection;
  }

  public NewSection getNewSection()
  {
    return newSection;
  }

  public boolean isSticky()
  {
    return sticky;
  }

  public void setSticky(boolean sticky)
  {
    this.sticky = sticky;
  }
  
}
