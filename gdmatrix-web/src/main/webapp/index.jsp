<%@ page import="org.santfeliu.web.ApplicationBean, org.santfeliu.web.UserAgentDetector, org.santfeliu.web.HttpUtils" %>
<%
  boolean isMobile = UserAgentDetector.isMobile(request);
  String mid = (isMobile ? 
    request.getParameter("mobileMid") : 
    request.getParameter("desktopMid"));
  
  if (mid == null)
  {
    ServletContext context = getServletContext();
    mid = ApplicationBean.getInstance(context).getStartMid(isMobile);
  }
  
  if (mid != null)
  {
    response.sendRedirect(HttpUtils.getContextURL(request) + "/go.faces?xmid=" + mid);
  }
  else
  {
%>
<html>
<body>
<h1>GDMatrix-3.0</h1>
<p>WARNING: Start mid property is not defined</p>
</body>
</html>
<%
  }  
%>
