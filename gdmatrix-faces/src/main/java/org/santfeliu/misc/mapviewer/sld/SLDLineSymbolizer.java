package org.santfeliu.misc.mapviewer.sld;

/**
 *
 * @author real
 */
public class SLDLineSymbolizer extends SLDSymbolizer
{
  public SLDLineSymbolizer()
  {
  }

  public SLDLineSymbolizer(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getSymbolizerType()
  {
    return "Line";
  }

  public SLDStroke getStroke()
  {
    return getNode("Stroke", SLDStroke.class);
  }
}
