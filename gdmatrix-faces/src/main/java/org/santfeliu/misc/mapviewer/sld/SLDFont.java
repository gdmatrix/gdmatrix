package org.santfeliu.misc.mapviewer.sld;

/**
 *
 * @author realor
 */
public class SLDFont extends SLDCssNode
{
  public SLDFont()
  {
  }

  public SLDFont(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getFontFamily()
  {
    return getCssParameter("font-family");
  }

  public void setFontFamily(String family)
  {
    setCssParameter("font-family", family);
  }

  public String getFontSize()
  {
    return getCssParameter("font-size");
  }

  public void setFontSize(String size)
  {
    setCssParameter("font-size", size);
  }

  public String getFontStyle()
  {
    return getCssParameter("font-style");
  }

  public void setFontStyle(String style)
  {
    setCssParameter("font-style", style);
  }

  public String getFontWeight()
  {
    return getCssParameter("font-weight");
  }

  public void setFontWeight(String weight)
  {
    setCssParameter("font-weight", weight);
  }
}
