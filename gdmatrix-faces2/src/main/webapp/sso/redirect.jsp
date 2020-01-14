<%
   String url = request.getParameter("url");   
%>
<html>
<body>
<form method="post" action="<%= url %>">
<input type="hidden" name="username" value="${userSessionBean.userId == 'intranet' ? '' : userSessionBean.userId}" />
<input type="hidden" name="password" value="${userSessionBean.userId == 'intranet' ? '' : userSessionBean.password}" />
</form>
<script type="text/javascript">
  document.forms[0].submit();
</script>
</body>
</html>
