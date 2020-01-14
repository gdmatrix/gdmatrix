package org.santfeliu.misc.mapviewer.sld;

/**
 *
 * @author real
 */
public class SLDPointSymbolizer extends SLDSymbolizer
{
  public SLDPointSymbolizer()
  {
  }

  public SLDPointSymbolizer(String prefix, String name)
  {
    super(prefix, name);
  }

  public SLDGraphic getGraphic()
  {
    return getNode("Graphic", SLDGraphic.class);
  }

  public String getSymbolizerType()
  {
    return "Point";
  }
}
