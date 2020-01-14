<%@page import="java.util.Enumeration"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<html>
  <head>
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="Expires" CONTENT="-1">
    <SCRIPT src="${pageContext.request.contextPath}/plugins/working/work.js"
      type="text/javascript"></SCRIPT>
  </head>
  <body>
    <%
      String action = (String)request.getAttribute("action");
      if (action != null)
      {
    %>
    <form action="<%= action %>" method="<%= request.getMethod() %>">
      <%
         Enumeration enu = request.getParameterNames();
         while (enu.hasMoreElements())
         {
           String paramName = (String)enu.nextElement();
           String paramValue = request.getParameter(paramName);
           if (!"action".equals(paramName))
           {
      %>
      <INPUT type="hidden" name="<%= paramName %>" value="<%= paramValue %>" />
      <%
           }
         }
      %>
    </form>
    <script type="text/javascript">
      submitWork('${pageContext.request.contextPath}');
    </script>
    <% } %>
  </body>
</html>