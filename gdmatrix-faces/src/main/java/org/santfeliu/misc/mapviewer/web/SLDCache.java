package org.santfeliu.misc.mapviewer.web;

import java.io.Serializable;
import java.util.HashMap;
import org.santfeliu.misc.mapviewer.SLDStore;

/**
 *
 * @author realor
 */
public class SLDCache implements Serializable
{
  private HashMap<String, String> map = new HashMap<String, String>();

  public String getSldUrl(String sldName)
  {
    String url = null;
    try
    {
      url = map.get(sldName);
      if (url == null)
      {
        url = SLDStore.getSldURL(sldName);
        if (url != null) map.put(sldName, url);
      }
    }
    catch (Exception ex)
    {
    }
    return url;
  }

  public void clear()
  {
    map.clear();
  }
}
