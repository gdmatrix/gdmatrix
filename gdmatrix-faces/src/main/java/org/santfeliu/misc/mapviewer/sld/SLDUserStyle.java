package org.santfeliu.misc.mapviewer.sld;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author real
 */
public class SLDUserStyle extends SLDNode
{
  public SLDUserStyle()
  {
  }

  public SLDUserStyle(String prefix, String name)
  {
    super(prefix, name);
  }
  
  public String getStyleName()
  {
    return getElementText("Name");
  }  

  public void setStyleName(String name)
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

  public boolean isDefault()
  {
    String value = getElementText("IsDefault");
    return "1".equals(value);
  }

  public void setDefault(boolean def)
  {
    SLDNode node = getNode("IsDefault", SLDNode.class);
    node.setTextValue(def ? "1" : "0");
  }
  
  public List<SLDRule> getRules()
  {
    int index = findNode("FeatureTypeStyle", 0);
    if (index == -1) return Collections.EMPTY_LIST;
    SLDNode featureStyle = (SLDNode)getChild(index);
    return featureStyle.findNodes(SLDRule.class);
  }

  public SLDRule addRule()
  {
    SLDNode node = getNode("FeatureTypeStyle", SLDNode.class);
    SLDRule rule = new SLDRule(null, "Rule");
    node.addChild(rule);
    return rule;
  }
}
