package org.santfeliu.faces.matrixclient.model;

import java.util.Map;

/**
 *
 * @author blanquepa
 */
public interface HtmlMatrixClientModel 
{
  public Map getResult();
  
  public void setResult(Map result);
  
  public Map getParameters();
  
  public Object getParameter(String name);
  
  public void putParameter(String name, Object value);
  
  public void putParameters(Map parameters);

  public Object parseResult() throws Exception;
}
