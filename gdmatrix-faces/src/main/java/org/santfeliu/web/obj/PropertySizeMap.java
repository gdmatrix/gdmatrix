package org.santfeliu.web.obj;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;

/**
 *
 * @author blanquepa
 */
class PropertySizeMap implements Map
{
  private static final int MAX_SIZE = 4000;
  Type type;

  PropertySizeMap(Type type)
  {
    this.type = type;
  }

  public Object get(Object property)
  {
    if (type == null)
      return MAX_SIZE;
    
    PropertyDefinition pd = type.getPropertyDefinition((String)property);
    if (pd == null)
      return MAX_SIZE;

    int size = pd.getSize();
    return size == 0 ? MAX_SIZE : size;
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