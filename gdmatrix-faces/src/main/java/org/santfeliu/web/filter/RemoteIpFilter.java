package org.santfeliu.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author realor
 */
public class RemoteIpFilter implements Filter
{
  public static final String X_FORWARDED_FOR_HEADER  = "X-Forwarded-For";
  public static final String X_FORWARDED_PROTO_HEADER  = "X-Forwarded-Proto";
  public static final String X_FORWARDED_PORT_HEADER  = "X-Forwarded-Port";
  
  public void init(FilterConfig fc) throws ServletException
  {
  }

  public void doFilter(ServletRequest request, ServletResponse response, 
    FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest httpRequest = (HttpServletRequest)request;
    HttpServletResponse httpResponse = (HttpServletResponse)response;

    if (httpRequest.getHeader(X_FORWARDED_FOR_HEADER) == null)
    {
      chain.doFilter(httpRequest, httpResponse);
    }
    else
    {
      ForwardedHttpServletRequest forwardedRequest = 
        new ForwardedHttpServletRequest(httpRequest);
      chain.doFilter(forwardedRequest, httpResponse);
    }
  }

  public void destroy()
  {
  }
  
  public class ForwardedHttpServletRequest extends HttpServletRequestWrapper
  {
    private final String scheme;
    private final int port;
    private final boolean secure;
    private final String remoteAddr;
    
    public ForwardedHttpServletRequest(HttpServletRequest httpRequest)
    {
      super(httpRequest);
      
      String xForwardedFor = httpRequest.getHeader(X_FORWARDED_FOR_HEADER);
      if (xForwardedFor == null)
      {
        remoteAddr = httpRequest.getRemoteAddr();
      }
      else
      {
        remoteAddr = xForwardedFor;
      }      
      
      String xForwardedProto = httpRequest.getHeader(X_FORWARDED_PROTO_HEADER);
      if (xForwardedProto == null)
      {
        scheme = httpRequest.getScheme();
      }
      else
      {
        scheme = xForwardedProto;
      }      
      secure = "https".equals(scheme);
      
      String xForwardedPort = httpRequest.getHeader(X_FORWARDED_PORT_HEADER);
      if (xForwardedPort == null)
      {
        port = httpRequest.getServerPort();        
      }
      else
      {
        int portNum;
        try
        {
          portNum = Integer.parseInt(xForwardedPort);
        }
        catch (NumberFormatException ex)
        {
          portNum = httpRequest.getServerPort();
        }
        port = portNum;
      }      
    }

    @Override
    public int getServerPort()
    {
      return port;
    }

    @Override
    public String getScheme()
    {
      return scheme;
    }

    @Override
    public boolean isSecure()
    {
      return secure;
    }
    
    @Override
    public String getRemoteAddr()
    {
      return remoteAddr;
    }
    
    @Override
    public StringBuffer getRequestURL()
    {
      String scheme = getScheme();             // http
      String serverName = getServerName();     // hostname.com
      int serverPort = getServerPort();        // 80
      String contextPath = getContextPath();   // /mywebapp
      String servletPath = getServletPath();   // /servlet/MyServlet
      String pathInfo = getPathInfo();         // /a/b;c=123

      StringBuffer url =  new StringBuffer();
      url.append(scheme).append("://").append(serverName);

      if (serverPort != -1 && serverPort != 80 && serverPort != 443) 
      {
        url.append(":").append(serverPort);
      }
      url.append(contextPath).append(servletPath);

      if (pathInfo != null) 
      {
        url.append(pathInfo);
      }
      return url;
    }
  }
}
