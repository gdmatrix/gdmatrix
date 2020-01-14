package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.web.WebBean;

/**
 *
 * @author blanquepa
 */

public class ParametersManager extends WebBean implements Serializable
{
  private ArrayList<ParametersProcessor> processors;
  
  public ParametersManager()
  {
    this.processors = new ArrayList();
  }
  
  public void addProcessor(ParametersProcessor processor)
  {
    this.processors.add(processor);
  }
    
  public String processParameters()
  {
    String outcome = null;
    Map parameters = getRequestParameters();
    for (ParametersProcessor processor : this.processors)
    {
      outcome = processor.processParameters(parameters);
      if (outcome != null) return outcome;
    }
    return outcome;
  }
    
  private Map getRequestParameters()
  {
    // Discard POST parameters
    HashMap qsMap = new HashMap();
    
    Map requestMap = getExternalContext().getRequestParameterMap();
    HttpServletRequest request = 
      (HttpServletRequest)getExternalContext().getRequest();
    String qs = request.getQueryString();

    if (qs != null)
    {
      for (Object key : requestMap.keySet())
      {
        if (qs.contains("?" + String.valueOf(key) + "=") || 
            qs.contains("&" + String.valueOf(key) + "="))
          qsMap.put(key, requestMap.get(key));
      }
    }    
    return qsMap;
  } 
}
