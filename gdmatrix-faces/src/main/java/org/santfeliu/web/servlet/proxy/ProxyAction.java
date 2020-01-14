package org.santfeliu.web.servlet.proxy;

import java.net.URLConnection;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.security.User;

/**
 *
 * @author realor
 */
public abstract class ProxyAction
{
  public abstract void execute(URLConnection conn,
    HttpServletRequest req, User user);
}
