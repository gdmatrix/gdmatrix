package org.santfeliu.web.obj;

import java.io.Serializable;

import java.util.ArrayList;


public class ObjectHistory extends ArrayList<String> implements Serializable
{
  private static final int DEFAULT_MAX_ENTRIES = 5;
  private int maxEntries;
  
  public ObjectHistory()
  {
    this(DEFAULT_MAX_ENTRIES);
  }

  public ObjectHistory(int size)
  {
    super(size + 1);
    this.maxEntries = size;
  }

  public void setObject(String objectId)
  {
    if (!ControllerBean.NEW_OBJECT_ID.equals(objectId))
    {
      remove(objectId);
      add(0, objectId);
      if (size() > maxEntries)
      {
        remove(maxEntries); // remove last
      }
    }
  }
  
  public void removeObject(String objectId)
  {
    if (!ControllerBean.NEW_OBJECT_ID.equals(objectId))
    {
      remove(objectId);
    }
  }
  
  public boolean containsObject(String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId)) return false;
    return indexOf(objectId) != -1;
  }
}

