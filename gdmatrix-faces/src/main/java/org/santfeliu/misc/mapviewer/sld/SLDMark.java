package org.santfeliu.misc.mapviewer.sld;

/**
 *
 * @author realor
 */
public class SLDMark extends SLDNode
{
  public SLDMark()
  {
  }

  public SLDMark(String prefix, String name)
  {
    super(prefix, name);
  }

  public String getWellKnownName()
  {
    return getElementText("WellKnownName");
  }

  public void setWellKnownName(String wellKnownName)
  {
    SLDNode node = getNode("WellKnownName", SLDNode.class);
    node.setTextValue(wellKnownName);
  }

  public SLDStroke getStroke()
  {
    return getNode("Stroke", SLDStroke.class);
  }

  public SLDFill getFill()
  {
    return getNode("Fill", SLDFill.class);
  }
}
