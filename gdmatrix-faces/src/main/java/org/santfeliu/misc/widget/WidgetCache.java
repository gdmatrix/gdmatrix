package org.santfeliu.misc.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.santfeliu.jmx.CacheMBean;
import org.santfeliu.jmx.JMXUtils;

/**
 *
 * @author lopezrj
 */
public class WidgetCache
{  
  private static final long CLEAR_TIME = 10 * 60 * 1000; // 10 minutes  
  
  private static WidgetCache cache; 
  
  //nodeId;username -> objects
  private Map<String, List> map = new HashMap<String, List>();   
  private long lastClearMillis;
  
  public static synchronized WidgetCache getInstance()
  {
    if (cache == null)
    {
      cache = new WidgetCache();
      JMXUtils.registerMBean("WidgetCache", cache.getCacheMBean());      
    }
    return cache;
  }
  
  public List getWidgetObjects(String workspaceId, String nodeId)
  {
    return getWidgetObjects(workspaceId, nodeId, null);
  }

  public List getWidgetObjects(String workspaceId, String nodeId, String username)
  {
    if (mustClear())
    {
      clear();
    }
    String key = getKey(workspaceId, nodeId, username);
    return map.get(key);
  }

  public void setWidgetObjects(String workspaceId, String nodeId, List objectList)
  {
    setWidgetObjects(workspaceId, nodeId, null, objectList);
  }
  
  public void setWidgetObjects(String workspaceId, String nodeId, String username, List objectList)
  {
    String key = getKey(workspaceId, nodeId, username);
    map.put(key, objectList);
  }

  public void clear()
  {
    map.clear();
    lastClearMillis = System.currentTimeMillis();
  }  
  
  private String getKey(String workspaceId, String nodeId, String username)
  {
    if (username == null) username = "";
    return workspaceId + ";" + nodeId + ";" + username;
  }
  
  private boolean mustClear()
  {
    return (System.currentTimeMillis() > lastClearMillis + CLEAR_TIME);
  }

  private String getNextCleaningInfo()
  {    
    try
    {
      SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      Date d = new Date(lastClearMillis + CLEAR_TIME);
      return format.format(d);
    }
    catch (Exception ex)
    {
      return "";
    }    
  }
    
  /* Cache Info */  
  
  WidgetCacheMBean getCacheMBean()
  {
    try
    {
      return new WidgetCacheMBean();
    }
    catch (NotCompliantMBeanException ex)
    {
      return null;
    }
  }

  public class WidgetCacheMBean extends StandardMBean implements CacheMBean
  {
    public WidgetCacheMBean() throws NotCompliantMBeanException
    {
      super(CacheMBean.class);
    }

    public String getName()
    {
      return "WidgetCache";
    }

    public long getMaxSize()
    {
      return Long.MAX_VALUE;
    }

    public long getSize()
    {
      return map.size();
    }

    public String getDetails()
    {
      return "Map size: " + getSize() + " / " + "Next clear: " + 
        getNextCleaningInfo();
    }

    public void clear()
    {
      map.clear();
    }

    public void update()
    {
      map.clear();
    }

  }
  
}
