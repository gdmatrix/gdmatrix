<%@page language="java" import="org.santfeliu.util.*,org.santfeliu.web.*" contentType="text/html" pageEncoding="utf-8"%>
<jsp:useBean id="userSessionBean" class="org.santfeliu.web.UserSessionBean" scope="session" />
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>Ajuda MatrixClient</title>
    <style>
      body{font-family: Arial, Helvetica, sans-serif}
      a{text-decoration: none;} 
      a:hover{color:gray;}
      .title{font-weight:bold;margin-bottom:3px}
    </style> 
  </head>
  <body>
    <%
      String clientAppPath = MatrixConfig.getProperty("MATRIX_CLIENT_APP_PATH");
      if (clientAppPath == null)
        clientAppPath = "/documents/97853/MatrixClient.zip";
      
      String cause = request.getParameter("cause");
      if ("not_configured".equals(cause))
      {
    %>  
      <p class="title">El client de Matrix no està configurat!</p>
    <%  
      }      
      else if ("not_started".equals(cause))
      {
    %>
      <p class="title">El client de Matrix no està en execució.</p>
    <%      
      }
      boolean isIntranetUser = userSessionBean.isIntranetUser();
      if (!isIntranetUser)
      {
    %>    
    <p>
      Per realitzar aquesta acció és necessari tenir el client de Matrix 
      instal·lat i executant-se. 
    </p>
    <p>
      Pots descarregar-te el client de Matrix 
      <a href="<%=clientAppPath%>" target="_blank">clicant aquí</a>.
    </p>
    <p>
      Un cop instal·lat a la teva màquina, el primer cop que l'executis, hauràs
      d'enregistrar el client en el navegador, pitjant el botó "Configura". Un cop fet
      això podràs reintentar l'execució de l'acció.
    </p>    
    <%
      }
      else
      {
    %>
    <p>
      Arrenca l'aplicació instal·lada a c:\matrix\dist\client\MatrixClient.exe
    </p>
    <%
      }
    %>    
  </body>
</html>
