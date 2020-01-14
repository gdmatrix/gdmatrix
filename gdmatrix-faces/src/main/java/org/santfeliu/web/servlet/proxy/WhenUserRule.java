package org.santfeliu.web.servlet.proxy;

import java.net.URLConnection;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.security.User;

/**
 *
 * @author realor
 */
public class WhenUserRule extends ProxyRule
{
  private String userId;

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  @Override
  public boolean evaluate(URLConnection conn, HttpServletRequest req, User user)
  {
    if (userId != null && userId.equals(user.getUserId())) return true;
    return false;
  }

  @Override
  public String toString()
  {
    return "WhenUser " + userId + " stop:" + stop;
  }
}
