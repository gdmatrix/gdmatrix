package org.santfeliu.web.servlet.proxy;

import java.net.URLConnection;
import javax.servlet.http.HttpServletRequest;
import org.santfeliu.security.User;

/**
 *
 * @author realor
 */
public class WhenRoleRule extends ProxyRule
{
  private String roleId;

  public String getRoleId()
  {
    return roleId;
  }

  public void setRoleId(String roleId)
  {
    this.roleId = roleId;
  }

  @Override
  public boolean evaluate(URLConnection conn, HttpServletRequest req, User user)
  {
    if (roleId != null && user.getRoles().contains(roleId)) return true;
    return false;
  }

  @Override
  public String toString()
  {
    return "WhenRole " + roleId + " stop:" + stop;
  }
}
