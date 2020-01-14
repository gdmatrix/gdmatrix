package org.santfeliu.faces.matrixclient.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author blanquepa
 */
public class DefaultMatrixClientModel implements Serializable, HtmlMatrixClientModel
{
  protected static final String EXCEPTION = "exception";
  protected static final String RESULT = "result";
  
  Map<String,Object> parameters;
  Map<String,Object> result;

  public DefaultMatrixClientModel()
  {
    parameters = new HashMap();
    result = new HashMap();
  }

  public Map getParameters()
  {
    return parameters;
  }
  
  public Object getParameter(String name)
  {
    return parameters.get(name);
  }
  
  public void putParameter(String name, Object value)
  {
    if (parameters == null)
      parameters = new HashMap();
    
    parameters.put(name, value);
  }
  
  public void putParameters(Map parameters)
  {
    if (parameters == null)
      parameters = new HashMap();
    
    this.parameters.putAll(parameters);
  }

  public Map getResult()
  {
    return result;
  }

  public void setResult(Map result)
  {
    this.result = result;
  }
  
  public Object parseResult() throws Exception
  {
    if (result != null)
    {
      String exception = (String)result.get(EXCEPTION);
      if (exception != null)
        throw new Exception(exception);
      return result.get(RESULT);
    }
    else
      return null;
  }   
  
  public void reset()
  {
    parameters.clear();
    result.clear();
  }
}
