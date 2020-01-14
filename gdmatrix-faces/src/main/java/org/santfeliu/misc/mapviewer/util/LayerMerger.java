package org.santfeliu.misc.mapviewer.util;

import java.util.HashSet;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.mapviewer.Map;

/**
 *
 * @author realor
 */
public class LayerMerger
{
  private static final int MAX_LENGTH = 100;
  private String serviceUrl;
  private StringBuilder layerNames = new StringBuilder();
  private StringBuilder styleNames = new StringBuilder();
  private StringBuilder cqlFilter = new StringBuilder();
  private String sld;
  private HashSet<String> layerSet = new HashSet<String>();
  private int maxLength = MAX_LENGTH;

  public int getMaxLength()
  {
    return maxLength;
  }

  public void setMaxLength(int maxLength)
  {
    this.maxLength = maxLength;
  }
  
  public boolean merge(Map.Layer layer)
  {
    boolean canMerge =
     (!layer.isBaseLayer() || layerNames.length() == 0) &&
     (serviceUrl == null || serviceUrl.equals(layer.getService().getUrl())) &&
     (sld == null || sld.equals(layer.getSld())) &&
     (layerNames.length() + styleNames.length() + cqlFilter.length() < maxLength);
    
    if (!canMerge) return false;

    if (StringUtils.isBlank(layer.getCqlFilter()))
    {
      canMerge = cqlFilter.length() == 0;
    }
    else // with filter
    {
      canMerge = layerNames.length() == 0 ||
        layerNames.toString().equals(layer.getNamesString());
    }

    if (!canMerge) return false;

    // merge is possible
    
    serviceUrl = layer.getService().getUrl();
    sld = layer.getSld() == null ? "" : layer.getSld();

    String namesString = layer.getNamesString();
    if (!layerSet.contains(namesString))
    {
      // add layer
      if (!layerSet.isEmpty()) layerNames.append(",");
      layerNames.append(namesString);
    
      // add style
      if (!layerSet.isEmpty()) styleNames.append(",");
      styleNames.append(layer.getStylesString());
      
      layerSet.add(namesString);
    }

    if (!StringUtils.isBlank(layer.getCqlFilter()))
    {
      if (cqlFilter.length() > 0) cqlFilter.append(" or ");
      cqlFilter.append("(");
      cqlFilter.append(layer.getCqlFilter());
      cqlFilter.append(")");
    }
    return canMerge;
  }

  public String getServiceUrl()
  {
    return serviceUrl;
  }

  public String getLayerNames()
  {
    return layerNames.toString();
  }

  public String getStyleNames()
  {
    return styleNames.toString();
  }

  public String getCqlFilter()
  {
    return cqlFilter.toString();
  }

  public String getSld()
  {
    return sld;
  }

  public void reset()
  {
    serviceUrl = null;
    sld = null;
    layerNames.setLength(0);
    styleNames.setLength(0);
    cqlFilter.setLength(0);
    layerSet.clear();
  }
}
