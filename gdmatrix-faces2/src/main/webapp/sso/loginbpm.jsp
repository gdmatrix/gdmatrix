<%-- 
    Document   : loginbpm
    Created on : 03-jun-2013
    Author     : Cecilia Comas Forns
--%>

<%@page import="org.santfeliu.dbf.DBConnection"%>
<%@page import="org.santfeliu.dbf.DBRepository"%>
<%@page import="org.santfeliu.web.UserSessionBean"%>
<%@page import="org.santfeliu.security.util.Credentials"%>
<%@page contentType="text/html" pageEncoding="windows-1252"%>
<%
    //URL del BPM
    String server = "https://bpm.santfeliu.cat";
    String path = "/OAC/SegExpStandalone.jsp?idioma=ca";
    String url = server + path;

    //Obtenir usuari logat a Matrix
    Credentials credentials =  UserSessionBean.getCredentials(request);
    if (credentials != null &&
        !credentials.getUserId().equals("anonymous") &&
        !credentials.getUserId().equals("intranet"))
        {
        //Si l'usuari està logat llavors logar a BPM amb el mateix usuari
        //si no s'obrirà el BPM sense login
        String username = credentials.getUserId();
        
        DBRepository repository = new DBRepository();
        DBConnection dbConn = repository.getConnection("jdbc/genesys5");
        try
        {
          //Crear una clau de hash aleatoria  
          String hash = Double.toString(Math.random() * 99999999);
          
          //Insertar la clau de hash a la taula "G5BPM.NCL_PARAM"
          dbConn.executeUpdate("INSERT INTO G5BPM.NCL_PARAM values ('LOGIN',' ','" + hash + "','" + username + "',0,' ',null)" , null);
          dbConn.commit();
          
          //Afegir la clau de hash a la url del BPM
          url = url + "&hh=" + hash;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dbConn.close();
        }
    }
    //Redireccionar al BPM
    response.sendRedirect(url);
        
%>