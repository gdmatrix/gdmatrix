package org.santfeliu.misc.mapviewer.sld;

import java.util.List;
import org.santfeliu.misc.mapviewer.util.ConversionUtils;

/**
 *
 * @author real
 */
public class SLDRule extends SLDNode
{
  public SLDRule()
  {
  }

  public SLDRule(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getTitle()
  {
    return getElementText("Title");
  }

  public void setTitle(String name)
  {
    int index = findNode("Title", 0);
    if (index != -1)
    {
      SLDNode child = getChild(index);
      child.setTextValue(name);
    }
    else
    {
      SLDNode node = new SLDNode("Title");
      node.setTextValue(name);
      insertChild(node, 0);
    }
  }

  public String getMinScaleDenominator()
  {
    return getElementText("MinScaleDenominator");
  }

  public void setMinScaleDenominator(String minScale)
  {
    SLDNode node = getNode("MinScaleDenominator", SLDNode.class);
    node.setTextValue(minScale);
  }

  public String getMaxScaleDenominator()
  {
    return getElementText("MaxScaleDenominator");
  }

  public void setMaxScaleDenominator(String maxScale)
  {
    SLDNode node = getNode("MaxScaleDenominator", SLDNode.class);
    node.setTextValue(maxScale);
  }

  public String getFilterAsXml()
  {
    return getInnerElements("Filter");
  }

  public void setFilterAsXml(String filter)
  {
    setInnerElements("ogc", "Filter", filter); // TODO take right prefix
  }

  public String getFilterAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("Filter"));
  }

  public void setFilterAsCql(String cqlFilter)
  {
    // TODO take right prefix
    setInnerElements("ogc", "Filter", ConversionUtils.cqlToXml(cqlFilter)); 
  }

  public List<SLDSymbolizer> getSymbolizers()
  {
    return findNodes(SLDSymbolizer.class);
  }

  public SLDPointSymbolizer addPointSymbolizer()
  {
    SLDPointSymbolizer symbolizer =
      new SLDPointSymbolizer(null, "PointSymbolizer");
    addChild(symbolizer);
    return symbolizer;
  }

  public SLDLineSymbolizer addLineSymbolizer()
  {
    SLDLineSymbolizer symbolizer =
      new SLDLineSymbolizer(null, "LineSymbolizer");
    addChild(symbolizer);
    return symbolizer;
  }

  public SLDPolygonSymbolizer addPolygonSymbolizer()
  {
    SLDPolygonSymbolizer symbolizer =
      new SLDPolygonSymbolizer(null, "PolygonSymbolizer");
    addChild(symbolizer);
    return symbolizer;
  }

  public SLDTextSymbolizer addTextSymbolizer()
  {
    SLDTextSymbolizer symbolizer =
      new SLDTextSymbolizer(null, "TextSymbolizer");
    addChild(symbolizer);
    return symbolizer;
  }
}
