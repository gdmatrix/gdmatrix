package org.santfeliu.misc.mapviewer.sld;

import org.santfeliu.misc.mapviewer.util.ConversionUtils;

/**
 *
 * @author realor
 */
public class SLDHalo extends SLDNode
{
  public SLDHalo()
  {
  }

  public SLDHalo(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getRadiusAsXml()
  {
    return getInnerElements("Radius");
  }

  public void setRadiusAsXml(String radius)
  {
    setInnerElements("Radius", radius);
  }

  public String getRadiusAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Radius"));
  }

  public void setRadiusAsCql(String radius)
  {
    setInnerElements("Radius", ConversionUtils.cqlToXml(radius));
  }

  public SLDFill getFill()
  {
    return getNode("Fill", SLDFill.class);
  }
}
