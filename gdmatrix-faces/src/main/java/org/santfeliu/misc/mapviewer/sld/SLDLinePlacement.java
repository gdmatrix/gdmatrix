package org.santfeliu.misc.mapviewer.sld;

import org.santfeliu.misc.mapviewer.util.ConversionUtils;

/**
 *
 * @author realor
 */
public class SLDLinePlacement extends SLDNode
{
  public SLDLinePlacement()
  {
  }

  public SLDLinePlacement(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getPerpendicularOffsetAsXml()
  {
    return getInnerElements("PerpendicularOffset");
  }

  public void setPerpendicularOffsetAsXml(String offset)
  {
    setInnerElements("PerpendicularOffset", offset);
  }

  public String getPerpendicularOffsetAsCql()
  {
    return ConversionUtils.xmlToCql(getInnerElements("PerpendicularOffset"));
  }

  public void setPerpendicularOffsetAsCql(String offset)
  {
    setInnerElements("PerpendicularOffset", ConversionUtils.cqlToXml(offset));
  }
}
