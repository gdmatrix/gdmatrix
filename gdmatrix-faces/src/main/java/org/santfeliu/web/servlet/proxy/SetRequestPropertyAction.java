package org.santfeliu.web.servlet.proxy;

import java.net.URLConnection;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.security.User;

/**
 *
 * @author realor
 */
public class SetRequestPropertyAction extends ProxyAction
{
  private String name;
  private String value;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  @Override
  public void execute(URLConnection conn, HttpServletRequest req, User user)
  {
    conn.setRequestProperty(name, value);
  }

  @Override
  public String toString()
  {
    return "SetRequestProperty " + name + " " + value;
  }
}
