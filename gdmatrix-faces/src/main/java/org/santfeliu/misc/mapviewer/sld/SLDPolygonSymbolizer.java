package org.santfeliu.misc.mapviewer.sld;

/**
 *
 * @author real
 */
public class SLDPolygonSymbolizer extends SLDSymbolizer
{
  public SLDPolygonSymbolizer()
  {
  }

  public SLDPolygonSymbolizer(String prefix, String name)
  {
    super(prefix, name);
  }

  public SLDStroke getStroke()
  {
    return getNode("Stroke", SLDStroke.class);
  }

  public SLDFill getFill()
  {
    return getNode("Fill", SLDFill.class);
  }

  @Override
  public String getSymbolizerType()
  {
    return "Polygon";
  }
}