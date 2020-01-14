<%@ page contentType="text/html;charset=UTF-8"
  import="org.santfeliu.signature.cms.*, org.santfeliu.web.HttpUtils" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%
// detect property id
String propertyName = null;
String id = request.getParameter("uuid"); // == docId for historical reasons
if (id != null)
{
  propertyName = "docId";
}
else
{
  id = request.getParameter("contentId");
  {
    if (id != null)
    {
      propertyName = "contentId";
    }
    else
    {
      id = request.getParameter("docId");
      if (id != null)
      {
        propertyName = "docId";
      }
      else
      {
        id = request.getParameter("sigId");
        if (id != null)
        {
          propertyName = "sigId";
        }
      }
    }
  }
}

// get contentId from id/propertyName
String contentId = null;
if (id != null)
{
  try
  {
    CMSLoader loader = new CMSLoader();
    contentId = loader.getCMSContentId(id, propertyName);
  }
  catch (Exception ex)
  {
  }
}

if (contentId == null)
{
  response.sendRedirect(HttpUtils.getContextURL(request) + "/common/signature/not_found.jsp");
}
else
{
  request.setAttribute("contentId", contentId);
%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Validaci&oacute; de signatures</title>
    <link href="css/cms.css" type="text/css" rel="stylesheet"/>
  </head>
  <body>
    <div class="page">
      <div class="block">
        <jsp:include page="/common/signature/fragments/header.jsp" />
      </div>
      <div class="block">
        <ul>
          <li>Enlla&ccedil; al contingut del document (format Microsoft Word .doc)
            <a href="/common/signature/fragments/document.jsp?contentId=<%=contentId%>" target="_blank">[aqu&iacute;]</a>
          </li>
          <li>Enlla&ccedil; al document amb signatures digitals (format PKCS7 .p7s)
            <a href="/documents/<%=contentId%>/cms.p7s" target="_blank">[aqu&iacute;]</a>
          </li>
        </ul>
      </div>
      <div class="block">
        Signatures del document:
      </div>
      <div class="block">
        <jsp:include page="/common/signature/fragments/signatures.jsp" />
      </div>
      <div class="block">
        <jsp:include page="/common/signature/fragments/footer.jsp" />
      </div>
    </div>
  </body>
</html>
<%
}
%>