package org.santfeliu.misc.mapviewer.sld;

import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author real
 */
public class SLDRoot extends SLDNode
{
  public SLDRoot(String prefix, String name)
  {
    super(prefix, name);
  }
  
  public List<SLDNamedLayer> getNamedLayers()
  {
    return findNodes(SLDNamedLayer.class);
  }

  public SLDNamedLayer addNamedLayer()
  {
    SLDNamedLayer namedLayer = new SLDNamedLayer(null, "NamedLayer");
    addChild(namedLayer);
    return namedLayer;
  }

  public SLDNamedLayer getNamedLayer(String name)
  {
    if (StringUtils.isBlank(name)) return null;
    boolean found = false;
    SLDNamedLayer namedLayer = null;
    int i = 0;
    while (!found && i < getChildCount())
    {
      SLDNode child = getChild(i);
      if (child instanceof SLDNamedLayer)
      {
        namedLayer = (SLDNamedLayer)child;
        if (name.equals(namedLayer.getLayerName())) found = true;
      }
      i++;
    }
    return found ? namedLayer : null;
  }

  public void addNamedLayers(List<String> layers, List<String> styles)
  {
    for (int i = 0; i < layers.size(); i++)
    {
      String layerName = layers.get(i);
      String styleName = null;
      if (i < styles.size()) styleName = styles.get(i);

      SLDNamedLayer namedLayer = getNamedLayer(layerName);
      if (namedLayer == null)
      {
        namedLayer = addNamedLayer();
        namedLayer.setLayerName(layerName);
      }
      
      SLDUserStyle userStyle = namedLayer.getUserStyle(styleName);
      if (userStyle == null)
      {
        userStyle = namedLayer.addUserStyle();
      }
      if (!StringUtils.isBlank(styleName))
      {
        userStyle.setStyleName(styleName);
        userStyle.setDefault(false);
      }
      else
      {
        userStyle.setDefault(true);
      }
    }
  }
}

