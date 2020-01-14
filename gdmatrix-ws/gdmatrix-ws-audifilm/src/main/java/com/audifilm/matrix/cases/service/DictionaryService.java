package com.audifilm.matrix.cases.service;

import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class DictionaryService
{

  DictionaryManagerPort dictionaryPort = null;
  WSEndpoint endpoint = null;

  public DictionaryService(String username, String password)
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      this.endpoint = wsDirectory.getEndpoint(DictionaryManagerService.class);
      this.dictionaryPort = this.endpoint.getPort( DictionaryManagerPort.class, username, password);

      //this.dictionaryPort = endpoint.getPort(DictionaryManagerPort.class, username, password);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }

  }


  protected WSEndpoint getEndpoint()
  {
    return endpoint;
  }

  protected DictionaryManagerPort getDictionaryPort()
  {
    return dictionaryPort;
  }
}
