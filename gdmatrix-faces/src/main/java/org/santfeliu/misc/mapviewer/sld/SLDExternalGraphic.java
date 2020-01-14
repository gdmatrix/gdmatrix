package org.santfeliu.misc.mapviewer.sld;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author realor
 */
public class SLDExternalGraphic extends SLDNode
{
  public SLDExternalGraphic()
  {
  }

  public SLDExternalGraphic(String prefix, String name)
  {
    super(prefix, name);
  }

  public void setOnlineResource(String resource)
  {
    if (StringUtils.isBlank(resource))
    {
      int index = findNode("OnlineResource", 0);
      if (index != -1)
      {
        removeChild(index);
      }
    }
    else
    {
      SLDNode node = getNode("OnlineResource", SLDNode.class);
      node.getAttributes().put("xlink:href", resource);
      node.getAttributes().put("xlink:type", "simple");
    }
  }

  public String getOnlineResource()
  {
    int index = findNode("OnlineResource", 0);
    if (index != -1)
    {
      SLDNode node = getChild(index);
      return node.getAttributes().get("xlink:href");
    }
    return null;
  }

  public String getFormat()
  {
    return getElementText("Format");
  }

  public void setFormat(String format)
  {
    SLDNode node = getNode("Format", SLDNode.class);
    node.setTextValue(format);
  }
}
