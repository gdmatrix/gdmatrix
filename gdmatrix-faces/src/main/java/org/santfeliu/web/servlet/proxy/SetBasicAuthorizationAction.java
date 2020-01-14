package org.santfeliu.web.servlet.proxy;

import java.net.URLConnection;
import javax.servlet.http.HttpServletRequest;
import org.matrix.security.SecurityConstants;
import org.santfeliu.security.User;
import org.santfeliu.security.util.BasicAuthorization;

/**
 *
 * @author realor
 */
public class SetBasicAuthorizationAction extends ProxyAction
{
  private String userId;
  private String password;

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  @Override
  public void execute(URLConnection conn, HttpServletRequest req, User user)
  {
    BasicAuthorization basic = new BasicAuthorization();
    basic.setUserId(userId == null ? user.getUserId() : userId);
    basic.setPassword(password == null ? user.getPassword() : password);
    if (!SecurityConstants.ANONYMOUS.equals(basic.getUserId()))
    {
      conn.setRequestProperty("Authorization", basic.toString());
    }
  }

  @Override
  public String toString()
  {
    return "SetBasicAuthorization " + userId + " " + password;
  }
}
