package org.santfeliu.web.servlet.proxy;

import java.net.URLConnection;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.security.User;

/**
 *
 * @author realor
 */
public class AlwaysRule extends ProxyRule
{
  @Override
  public boolean evaluate(URLConnection conn, HttpServletRequest req, User user)
  {
    return true;
  }

  @Override
  public String toString()
  {
    return "Always stop:" + stop;
  }
}
