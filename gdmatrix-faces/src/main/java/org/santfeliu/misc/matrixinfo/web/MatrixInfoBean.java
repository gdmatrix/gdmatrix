package org.santfeliu.misc.matrixinfo.web;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.matrix.MatrixInfo;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;

/**
 *
 * @author lopezrj
 */
@CMSManagedBean
public class MatrixInfoBean extends FacesBean
{
  public static RuntimeMXBean RUNTIME_MX_BEAN = getRuntimeMXBean();

  //Matrix Info Properties

  public String getVersion() 
  {
    return MatrixInfo.getFullVersion();
  }

  public String getLicense()
  {
    return MatrixInfo.getLicense();
  }

  public List<String> getTeam()
  {
    List<String> result = new ArrayList<String>();
    for (String[] teamMate : MatrixInfo.getTeam())
    {
      result.add(teamMate[0] + " ("+ teamMate[1] + ")");
    }
    return result;
  }

  public List<String> getMatrixInfoProperties()
  {
    List<String> result = new ArrayList<String>();
    Properties properties = MatrixInfo.getProperties();
    for (Object key : properties.keySet())
    {
      String sKey = (String)key;
      String value = properties.getProperty(sKey);
      result.add(sKey + " = "+ (value != null ? value : "null"));
    }
    return result;
  }

  //JVM Properties

  public String getUpTime()
  {    
    long ms = RUNTIME_MX_BEAN.getUptime();
    int secs = (int)(ms / 1000);
    int d = (int)(secs / (24 * 60 * 60));
    secs = secs - d * (24 * 60 * 60);
    int h = (int)(secs / (60 * 60));
    secs = secs - h * (60 * 60);
    int m = (int)(secs / 60);
    secs = secs - m * 60;
    int s = secs;
    return (d > 0 ? d + "d " : "") +
      (h > 0 ? h + "h " : "") +
      (m > 0 ? m + "m " : "") +
      s + "s";
  }

  public String getStartDateTime()
  {    
    Date date = new Date(RUNTIME_MX_BEAN.getStartTime());
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    return format.format(date);
  }

  public List<String> getSystemProperties()
  {
    List<String> result = new ArrayList<String>();
    Map<String, String> properties = RUNTIME_MX_BEAN.getSystemProperties();
    for (String sKey : properties.keySet())
    {
      String value = properties.get(sKey);
      result.add(sKey + " = "+ (value != null ? value : "null"));
    }
    return result;
  }

  public List<String> getLibraryPath()
  {
    String s = RUNTIME_MX_BEAN.getLibraryPath();
    List<String> result = TextUtils.stringToList(s, ";");
    if (result == null) result = new ArrayList<String>();
    return result;
  }

  public List<String> getBootClassPath()
  {
    String s = RUNTIME_MX_BEAN.getBootClassPath();
    List<String> result = TextUtils.stringToList(s, ";");
    if (result == null) result = new ArrayList<String>();
    return result;
  }

  public List<String> getClassPath()
  {
    String s = RUNTIME_MX_BEAN.getClassPath();
    List<String> result = TextUtils.stringToList(s, ";");
    if (result == null) result = new ArrayList<String>();
    return result;
  }
  
  public RuntimeMXBean getRuntimeBean()
  {
    return RUNTIME_MX_BEAN;
  }
  
  @CMSAction
  public String show()
  {
    return "matrix_info";
  }

  private static RuntimeMXBean getRuntimeMXBean()
  {
    return ManagementFactory.getRuntimeMXBean();
  }
  
}
