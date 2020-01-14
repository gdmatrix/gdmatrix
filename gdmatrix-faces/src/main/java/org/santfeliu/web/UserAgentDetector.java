package org.santfeliu.web;

import javax.servlet.http.HttpServletRequest;

/**
 * @author realor
 * @author lopezrj
 */
public class UserAgentDetector
{
  private static final String[] MOBILE_AGENT_LIST =
  {
    "iphone",
    "android",
    "blackberry",
    "nokia",
    "opera mini",
    "opera mobi",
    "series60",
    "symbian",
    "iemobile",
    "smartphone",
    "ppc",
    "mib",
    "semc",
    "mobile safari",
    "blazer",
    "bolt",
    "fennec",
    "minimo",
    "netfront",
    "skyfire",
    "teashark",
    "teleca",
    "uzard"
  };  
    
  public static boolean isMobile(HttpServletRequest request)
  {
    if (request.getHeader("user-agent") != null)
    {
      String userAgent = request.getHeader("user-agent").toLowerCase();    
      for (String agent : MOBILE_AGENT_LIST)
      {
        if (userAgent.contains(agent)) return true;
      }      
    }
    return false;
  }
  
  public static boolean isUserAgent(HttpServletRequest request, String agent)
  {
    String userAgent = null;
    if (request.getHeader("user-agent") != null)
    {
      userAgent = request.getHeader("user-agent").toLowerCase();
    }
    return (userAgent != null ? userAgent.contains(agent) : false);
  }
  
}
