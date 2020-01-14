package org.santfeliu.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author realor
 */
public class SessionCreator
{
  private String sessionCookieName = "JSESSIONID";
  private String protocol;
  private String host;
  private int port;
  private String contextPath;

  public SessionCreator(String protocol, String host, int port)
  {
    this(protocol, host, port, "");
  }

  public SessionCreator(String protocol, String host,
     int port, String contextPath)
  {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.contextPath = contextPath;
  }

  public String getSessionCookieName()
  {
    return sessionCookieName;
  }

  public void setSessionCookieName(String sessionCookieName)
  {
    this.sessionCookieName = sessionCookieName;
  }

  public String createSession(String userId, String password)
    throws IOException
  {
    String sessionId = null;
    if (contextPath == null) contextPath = "";

    URL url = new URL(protocol, host, port,
      contextPath + "/login.faces?userid=" + userId + "&password=" + password);

    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setRequestMethod("GET");
    conn.setInstanceFollowRedirects(false);
    String location = conn.getHeaderField("Location");
    if (location != null && location.indexOf("/login.faces") == -1)
    {
      sessionId = getSessionId(conn);
    }
    return sessionId;
  }

  public boolean isValidSession(String sessionId) throws IOException
  {
    if (sessionId == null) return false;
    URL url = new URL(protocol, host, port,
      contextPath + "/go.faces?sessionid=check");
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setRequestMethod("GET");    
    conn.addRequestProperty("Cookie", sessionCookieName + "=" + sessionId);
    conn.setInstanceFollowRedirects(false);
    return sessionId.equals(getSessionId(conn));
  }

  private String getSessionId(HttpURLConnection conn)
  {
    String sessionId = null;
    String cookie = conn.getHeaderField("Set-Cookie");
    if (cookie != null)
    {
      int index1 = cookie.indexOf(sessionCookieName + "=");
      if (index1 != -1)
      {
        int index2 = cookie.indexOf(";", index1);
        sessionId = cookie.substring(
          index1 + sessionCookieName.length() + 1, index2);
      }
    }
    return sessionId;
  }

  public static void main(String[] args)
  {
    try
    {
      SessionCreator sc = new SessionCreator("http", "localhost", 80, "");
      if (true)
      {
        String sessionId = sc.createSession("realor", "******");
        System.out.println(sessionId);
        System.out.println("valid: " + sc.isValidSession(sessionId));
      }
      else
      {
        System.out.println("valid:" +
          sc.isValidSession("7F0E1EA677D3B9500A9BCFA120B29A73"));
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
