package org.santfeliu.misc.iarxiu.ant;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author blanquepa
 */
public abstract class ClientPITCommand extends PITCommand
{
  protected String ticketVar;
  protected String resultVar;
  protected String configFilePath;
  private static Map<String, FileSystemXmlApplicationContext> appContexts =
    new HashMap<String, FileSystemXmlApplicationContext>();

  public String getConfigFilePath()
  {
    return configFilePath;
  }

  public void setConfigFilePath(String configFilePath)
  {
    this.configFilePath = configFilePath;
  }

  public String getResultVar()
  {
    return resultVar;
  }

  public void setResultVar(String resultVar)
  {
    this.resultVar = resultVar;
  }

  public String getTicketVar()
  {
    return ticketVar;
  }

  public void setTicketVar(String ticketVar)
  {
    this.ticketVar = ticketVar;
  }

  protected FileSystemXmlApplicationContext getApplicationContext()
  {
    FileSystemXmlApplicationContext appContext = getAppContext(configFilePath);
    if (appContext == null)
    {
      appContext = new FileSystemXmlApplicationContext(configFilePath);
      setAppContext(configFilePath, appContext);
    }

    return appContext;
  }

  private FileSystemXmlApplicationContext getAppContext(String configFilePath)
  {
    return appContexts.get(configFilePath);
  }

  private void setAppContext(String configFilePath,
    FileSystemXmlApplicationContext appContext)
  {
    appContexts.put(configFilePath, appContext);
  }
}
