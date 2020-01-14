package org.santfeliu.misc.mapviewer.sld;

import org.santfeliu.misc.mapviewer.util.ConversionUtils;

/**
 *
 * @author real
 */
public class SLDTextSymbolizer extends SLDSymbolizer
{
  public SLDTextSymbolizer()
  {
  }

  public SLDTextSymbolizer(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getLabelAsXml()
  {
    return getInnerElements("Label");
  }

  public void setLabelAsXml(String elements)
  {
    setInnerElements("Label", elements);
  }

  public String getLabelAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Label"));
  }

  public void setLabelAsCql(String cql)
  {
    setInnerElements("Label", ConversionUtils.cqlToXml(cql));
  }

  public SLDFont getFont()
  {
    return getNode("Font", SLDFont.class);
  }

  public SLDFill getFill()
  {
    return getNode("Fill", SLDFill.class);
  }

  public SLDHalo getHalo()
  {
    return getNode("Halo", SLDHalo.class);
  }

  public SLDPointPlacement getPointPlacement()
  {
    SLDNode placement = getNode("LabelPlacement", SLDNode.class);
    return placement.getNode("PointPlacement", SLDPointPlacement.class);
  }

  public SLDLinePlacement getLinePlacement()
  {
    SLDNode placement = getNode("LabelPlacement", SLDNode.class);
    return placement.getNode("LinePlacement", SLDLinePlacement.class);
  }
  
  public String getSymbolizerType()
  {
    return "Text";
  }
}
