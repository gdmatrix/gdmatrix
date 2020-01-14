package org.santfeliu.web.servlet.proxy;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.security.User;

/**
 *
 * @author realor
 */
public abstract class ProxyRule
{
  protected boolean stop;
  private ArrayList<ProxyAction> actions = new ArrayList<ProxyAction>();

  public boolean isStop()
  {
    return stop;
  }

  public void setStop(boolean stop)
  {
    this.stop = stop;
  }

  public abstract boolean evaluate(URLConnection conn, 
    HttpServletRequest req, User user);

  public List<ProxyAction> getActions()
  {
    return actions;
  }
}
