package org.santfeliu.misc.mapviewer.sld;

/**
 *
 * @author realor
 */
public class SLDFill extends SLDCssNode
{
  public SLDFill()
  {
  }

  public SLDFill(String prefix, String name)
  {
    super(prefix, name);
  }

  public void setFillColor(String color)
  {
    setCssParameter("fill", color);
  }

  public String getFillColor()
  {
    return getCssParameter("fill");
  }

  public void setFillOpacity(String opacity)
  {
    setCssParameter("fill-opacity", opacity);
  }

  public String getFillOpacity()
  {
    return getCssParameter("fill-opacity");
  }
}
