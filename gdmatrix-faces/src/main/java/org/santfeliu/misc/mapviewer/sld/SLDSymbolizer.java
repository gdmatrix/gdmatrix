package org.santfeliu.misc.mapviewer.sld;

import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.mapviewer.util.ConversionUtils;

/**
 *
 * @author real
 */
public abstract class SLDSymbolizer extends SLDNode
{
  public SLDSymbolizer()
  {
  }

  public SLDSymbolizer(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getGeometryAsXml()
  {
    return getInnerElements("Geometry");
  }

  public void setGeometryAsXml(String geometry)
  {
    setInnerElements("Geometry", geometry);
  }

  public String getGeometryAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Geometry"));
  }

  public void setGeometryAsCql(String geometry)
  {
    setInnerElements("Geometry", ConversionUtils.cqlToXml(geometry));
  }

  public String getCssParameter(SLDNode tagNode, String parameter)
  {
    String value = null;
    boolean found = false;
    int index = 0;
    while (!found && index < tagNode.getChildCount())
    {
      SLDNode child = tagNode.getChild(index);
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

  public void setCssParameter(SLDNode tagNode, String parameter, String value)
  {
    if (StringUtils.isBlank(value)) value = null;

    boolean found = false;
    int index = 0;
    SLDNode child = null;
    while (!found && index < tagNode.getChildCount())
    {
      child = tagNode.getChild(index);
      String paramName = child.getAttributes().get("name");
      if (parameter.equals(paramName)) found = true;
      else index++;
    }
    if (value == null)
    {
      if (found)
      {
        tagNode.removeChild(index);
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
        tagNode.addChild(cssNode);
      }
    }
  }

  public String getCssParameter(String tag, String parameter)
  {
    int tagIndex = findNode(tag, 0);
    if (tagIndex == -1) return null;

    SLDNode tagNode = getChild(tagIndex);
    return getCssParameter(tagNode, parameter);
  }

  public void setCssParameter(String tag, String parameter, String value)
  {
    if (StringUtils.isBlank(value)) value = null;

    SLDNode tagNode = null;
    int tagIndex = findNode(tag, 0);
    if (tagIndex == -1)
    {
      tagNode = new SLDNode(null, tag);
      addChild(tagNode);
    }
    else tagNode = getChild(tagIndex);
    setCssParameter(tagNode, parameter, value);
  }

  public abstract String getSymbolizerType();
}

