package org.santfeliu.misc.mapviewer.sld;

/**
 *
 * @author realor
 */
public class SLDStroke extends SLDCssNode
{
  public SLDStroke()
  {
  }
  
  public SLDStroke(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getStrokeColor()
  {
    return getCssParameter("stroke");
  }

  public void setStrokeColor(String color)
  {
    setCssParameter("stroke", color);
  }

  public String getStrokeWidth()
  {
    return getCssParameter("stroke-width");
  }

  public void setStrokeWidth(String width)
  {
    setCssParameter("stroke-width", width);
  }

  public String getStrokeOpacity()
  {
    return getCssParameter("stroke-opacity");
  }

  public void setStrokeOpacity(String opacity)
  {
    setCssParameter("stroke-opacity", opacity);
  }

  public String getStrokeLineJoin()
  {
    return getCssParameter("stroke-linejoin");
  }

  public void setStrokeLineJoin(String value)
  {
    setCssParameter("stroke-linejoin", value);
  }

  public String getStrokeLineCap()
  {
    return getCssParameter("stroke-linecap");
  }

  public void setStrokeLineCap(String value)
  {
    setCssParameter("stroke-linecap", value);
  }

  public String getStrokeDashArray()
  {
    return getCssParameter("stroke-dasharray");
  }

  public void setStrokeDashArray(String value)
  {
    setCssParameter("stroke-dasharray", value);
  }

  public String getStrokeDashOffset()
  {
    return getCssParameter("stroke-dashoffset");
  }

  public void setStrokeDashOffset(String value)
  {
    setCssParameter("stroke-dashoffset", value);
  }
}
