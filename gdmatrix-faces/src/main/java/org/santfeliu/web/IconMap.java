package org.santfeliu.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author blanquepa
 */
public class IconMap implements Map
{
  private String iconsPath;
  private String[] extensions = new String[]{"png", "gif", "jpg"};

  public IconMap(String iconsPath)
  {
    this.iconsPath = iconsPath;
  }

  public String[] getExtensions()
  {
    return extensions;
  }

  public void setExtensions(String[] extensions)
  {
    this.extensions = extensions;
  }

  public int size()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isEmpty()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsKey(Object key)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsValue(Object value)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object get(Object key)
  {
    String result = null;
    String iconPath = iconsPath + key;
    ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
    if (extensions == null)
      extensions = new String[]{"png", "gif", "jpg"};

    for (int i = 0; i < extensions.length; i++)
    {
      try
      {
        URL iconUrl = ctx.getResource(iconPath + "." + extensions[i]);
        if (iconUrl != null)
          return iconPath + "." + extensions[i];
      }
      catch (MalformedURLException ex)
      {
      }
    }

    return result;
  }

  public Object put(Object key, Object value)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object remove(Object key)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void putAll(Map m)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void clear()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Set keySet()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Collection values()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Set entrySet()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
