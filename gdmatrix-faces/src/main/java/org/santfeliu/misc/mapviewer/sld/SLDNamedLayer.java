package org.santfeliu.misc.mapviewer.sld;

import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author real
 */
public class SLDNamedLayer extends SLDNode
{
  public SLDNamedLayer()
  {
  }

  public SLDNamedLayer(String prefix, String name)
  {
    super(prefix, name);
  }
 
  public String getLayerName()
  {
    return getElementText("Name");
  }

  public void setLayerName(String name)
  {
    int index = findNode("Name", 0);
    if (index != -1)
    {
      SLDNode child = getChild(index);
      child.setTextValue(name);
    }
    else
    {
      SLDNode node = new SLDNode(null, "Name");
      node.setTextValue(name);
      insertChild(node, 0);
    }
  }
  
  public List<SLDUserStyle> getUserStyles()
  {
    return findNodes(SLDUserStyle.class);
  }

  public SLDUserStyle addUserStyle()
  {
    SLDUserStyle userStyle = new SLDUserStyle(null, "UserStyle");
    addChild(userStyle);
    return userStyle;
  }

  public SLDUserStyle getUserStyle(String name)
  {
    boolean found = false;
    SLDUserStyle userStyle = null;
    int i = 0;
    while (!found && i < getChildCount())
    {
      SLDNode child = getChild(i);
      if (child instanceof SLDUserStyle)
      {
        userStyle = (SLDUserStyle)child;
        String styleName = userStyle.getStyleName();
        if (StringUtils.isBlank(name) && StringUtils.isBlank(styleName))
          found = true;
        else if (name != null && name.equals(styleName))
          found = true;
      }
      i++;
    }
    return found ? userStyle : null;
  }
}
