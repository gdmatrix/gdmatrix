package org.santfeliu.misc.mapviewer.sld;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class SLDCssNode extends SLDNode
{
  public SLDCssNode()
  {
  }

  public SLDCssNode(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getCssParameter(String parameter)
  {
    String value = null;
    boolean found = false;
    int index = 0;
    while (!found && index < getChildCount())
    {
      SLDNode child = getChild(index);
      String paramName = child.getAttributes().get("name");
      if (parameter.equals(paramName))
      {
        found = true;
        value = child.getTextValue();
      }
      else index++;
    }
    return found ? value : null;
  }

  public void setCssParameter(String parameter, String value)
  {
    if (StringUtils.isBlank(value)) value = null;

    boolean found = false;
    int index = 0;
    SLDNode child = null;
    while (!found && index < getChildCount())
    {
      child = getChild(index);
      String paramName = child.getAttributes().get("name");
      if (parameter.equals(paramName)) found = true;
      else index++;
    }
    if (value == null)
    {
      if (found)
      {
        removeChild(index);
      }
    }
    else // value != null
    {
      if (found)
      {
        child.setTextValue(value);
      }
      else
      {
        SLDNode cssNode = new SLDNode(null, "CssParameter");
        cssNode.getAttributes().put("name", parameter);
        cssNode.setTextValue(value);
        addChild(cssNode);
      }
    }
  }
}
