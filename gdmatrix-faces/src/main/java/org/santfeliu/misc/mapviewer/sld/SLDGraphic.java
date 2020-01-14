package org.santfeliu.misc.mapviewer.sld;

import org.santfeliu.misc.mapviewer.util.ConversionUtils;

/**
 *
 * @author realor
 */
public class SLDGraphic extends SLDNode
{
  public SLDGraphic()
  {
  }

  public SLDGraphic(String prefix, String name)
  {
    super(prefix, name);
  }

  public SLDExternalGraphic getExternalGraphic()
  {
    return getNode("ExternalGraphic", SLDExternalGraphic.class);
  }

  public SLDMark getMark()
  {
    return getNode("Mark", SLDMark.class);
  }

  public String getOpacityAsXml()
  {
    return getInnerElements("Opacity");
  }
  
  public void setOpacityAsXml(String opacity)
  {
    setInnerElements("Opacity", opacity);
  }

  public String getOpacityAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Opacity"));
  }

  public void setOpacityAsCql(String opacity)
  {
    setInnerElements("Opacity", ConversionUtils.cqlToXml(opacity));
  }

  public String getSizeAsXml()
  {
    return getInnerElements("Size");
  }

  public void setSizeAsXml(String size)
  {
    setInnerElements("Size", size);
  }

  public String getSizeAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Size"));
  }

  public void setSizeAsCql(String size)
  {
    setInnerElements("Size", ConversionUtils.cqlToXml(size));
  }

  public String getRotationAsXml()
  {
    return getInnerElements("Rotation");
  }

  public void setRotationAsXml(String rotation)
  {
    setInnerElements("Rotation", rotation);
  }

  public String getRotationAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Rotation"));
  }

  public void setRotationAsCql(String rotation)
  {
    setInnerElements("Rotation", ConversionUtils.cqlToXml(rotation));
  }
}

