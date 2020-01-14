package org.santfeliu.faces.matrixclient.model;

/**
 *
 * @author blanquepa
 */
public class ScanMatrixClientModel extends ServletMatrixClientModel
{
  public ScanMatrixClientModel() 
  {
    super();
    putParameter("scanServletUrl", getServletUrl());
  }

  @Override
  protected String getServletName()
  {
    return "scanner";
  }
  
  public Object parseResult() throws Exception
  {
    if (result != null)
    {
      String exception = (String)result.get(EXCEPTION);
      if (exception != null)
      {
        if (exception.contains("INVALID_SCAN"))
          throw new Exception("INVALID_SCAN");
        else
          throw new Exception(exception);
      }
      return result.get(RESULT);
    }
    else
      return null;
  }   
}
        

