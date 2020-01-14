package org.santfeliu.feed.web;

import java.net.URL;
import org.santfeliu.feed.client.FeedManagerClient;
import org.santfeliu.web.UserSessionBean;

public class FeedConfigBean
{
  public static FeedManagerClient getPort() throws Exception
  {
    return new FeedManagerClient(
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static FeedManagerClient getPort(String userId, String password) 
    throws Exception
  {
    return new FeedManagerClient(userId, password);
  }

  public static FeedManagerClient getPort(URL wsDirectoryURL, String userId, 
    String password) throws Exception
  {
    return new FeedManagerClient(wsDirectoryURL, userId, password);
  }

}
