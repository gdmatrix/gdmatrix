<%@page contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>JSP Page</title>
  </head>
  <body>
    <h1>MatrixClient test page</h1>
    <script type="text/javascript" src="/plugins/client/matrix-client.js">
    </script>
    <button onclick="echo('Hello ÁÉÍÓÚ<%= "ÁÉÍÓÚáéíóú" %>', function(result){alert(JSON.stringify(result));})">Echo</button>
    <button onclick="editDocument('1635087', 'http://www.santfeliu.cat/wsdirectory', 'anonymous', 'anonymous', function(result){alert(JSON.stringify(result));})">Edita</button>
    <button onclick="signDocument('97813-82ce1c26-2e25-4372-87e4-3958bf33d460', function(result){alert(JSON.stringify(result));})">Sign</button>
  </body>
</html>
