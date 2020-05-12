<%@ page contentType="text/html;charset=UTF-8"
         import="org.santfeliu.signature.cms.*"%>
<%
String contentId = request.getParameter("contentId");
String mode = request.getParameter("disposition");
if (mode == null || mode.equals(""))
{
  mode = "attachment";
}
try
{
  CMSLoader loader = new CMSLoader();
  CMSData data = loader.loadCMSByContentId(contentId);
  response.setContentType("application/msword");
  response.setHeader("Content-Disposition", mode + "; filename=signature.doc");
  java.io.OutputStream os = response.getOutputStream();
  data.writeSignedContent(os);
  os.flush();
  os.close();
}
catch (Exception ex)
{
}
%>

