<%@ page contentType="text/html;charset=UTF-8" import="org.santfeliu.web.HttpUtils"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%
  if (request.getParameter("validate") != null)
  {
    String sigId = request.getParameter("sigId").toUpperCase();
    response.sendRedirect(HttpUtils.getContextURL(request) + "/common/signature/cms.jsp?sigId=" + sigId);
  }
  else
  {
%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Validaci&oacute; de signatures</title>
    <link href="css/cms.css" type="text/css" rel="stylesheet"/>
  </head>
  <body>
    <form action="validate.jsp" method="get">
      <div class="page">
        <div class="block">
          <jsp:include page="/common/signature/fragments/header.jsp" />
        </div>
        <div class="title">
           Servei de validaci&oacute; de documents electr&ograve;nics
        </div>
        <div class="block">
            Els documents que podeu verificar amb aquesta opci&oacute; porten
            signatura electr&ograve;nica reconeguda, &eacute;s a dir, signatura
            electr&ograve;nica avan&ccedil;ada basada en un certificat reconegut
            em&egrave;s per l&rsquo;Ag&egrave;ncia Catalana de Certificaci&oacute;
            i generada emprant un dispositiu segur, per la qual cosa,
            d&rsquo;acord amb el que estableix l&rsquo;article 3.4 de la Llei
            59/2003, de 19 de desembre, s&rsquo;equipara a la signatura
            manuscrita en relaci&oacute; amb les dades consignades en paper.
        </div>
        <div class="block">
           Introdueix la refer&egrave;ncia electr&ograve;nica del document:
        </div>
        <div class="block">
           <input class="sigId" type="text" name="sigId" maxlength="36" size="36" />
           <input class="button" type="submit" name="validate" value="VALIDA"/>
        </div>
        <div class="block">
           <jsp:include page="/common/signature/fragments/footer.jsp" />
        </div>
      </div>
    </form>
  </body>
</html>
<%
  }
%>
