package org.santfeliu.web.obj;

import java.io.Serializable;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.collections.LRUMap;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;

/**
 *
 * @author realor
 */
public class ObjectDescriptionCache implements Serializable
{
  private static final int MAX_SIZE = 200;
  private static final ObjectDescriptionCache defaultInstance = 
    new ObjectDescriptionCache();

  private final LRUMap map;

  public ObjectDescriptionCache()
  {
    map = new LRUMap(MAX_SIZE);
    try
    {
      JMXUtils.registerMBean("ObjectDescriptionCache", new MBean());
    }
    catch (Exception ex)
    {
    }
  }

  public static ObjectDescriptionCache getInstance()
  {
    return defaultInstance;
  }

  public String getDescription(ObjectBean objectBean, String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId))
    {
      return "";
    }
    else
    {
      String typeId = objectBean.getObjectTypeId();
      String key = getKey(typeId, objectId);
      synchronized (map)
      {
        String description = (String)map.get(key);
        if (description == null)
        {
          description = loadDescription(objectBean, objectId);
          map.put(key, description);
        }
        return description;
      }
    }
  }

  public void clearDescription(ObjectBean objectBean, String objectId)
  {
    String typeId = objectBean.getObjectTypeId();
    String key = getKey(typeId, objectId);
    synchronized (map)
    {
      map.remove(key);
    }
  }

  // private methods

  private String getKey(String typeId, String objectId)
  {
    StringBuilder builder = new StringBuilder(typeId);
    builder.append(":");
    builder.append(objectId);
    return builder.toString();
  }

  private String loadDescription(ObjectBean objectBean, String objectId)
  {
    String description = objectId;
    String currentObjectId = objectBean.getObjectId();
    if (currentObjectId != null && currentObjectId.equals(objectId))
    {
      description = objectBean.getDescription();
    }
    else
    {
      description = objectBean.getDescription(objectId);
    }
    return description;
  }

  public class MBean extends StandardMBean implements CacheMBean
  {
    public MBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "ObjectDescriptionCache";
    }

    public long getMaxSize()
    {
      return map.getMaximumSize();
    }

    public long getSize()
    {
      return map.size();
    }

    public String getDetails()
    {
      return "ObjectDescriptionCache: " +
        map.size() + "/" + map.getMaximumSize();
    }

    public void clear()
    {
      synchronized (map)
      {
        map.clear();
      }
    }

    public void update()
    {
      synchronized (map)
      {
        map.clear();
      }
    }
  }
}
