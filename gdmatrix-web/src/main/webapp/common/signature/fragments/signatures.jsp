<%@ page contentType="text/html;charset=UTF-8"
   import="java.util.*,org.santfeliu.signature.cms.*"%>
<%
try
{
  String contentId = (String)request.getAttribute("contentId");
  CMSLoader loader = new CMSLoader();
  CMSData data = loader.loadCMSByContentId(contentId);
  List<CMSSignature> signatures =  data.getSignatures();
  for (CMSSignature signature : signatures)
  {
%>
<div class="signatureBox">
<% if (signature.isValid()) { %>
  <img src="/common/signature/images/certificate.gif" alt="" style="border: none">
<% } else { %>
  <img src="/common/signature/images/error.gif" alt="" style="border: none">
<% } %>
  <div>
    <%= signature.getCertificateProperties().get("CN") %>
  </div>
  <div>
    <%= signature.getCertificateProperties().get("SN") == null ?
      "" : signature.getCertificateProperties().get("SN") %>
  </div>
<% if (signature.getSigningDate() != null) { %>
  <div>
    <span class="field">Data de signatura:</span> <%= signature.getSigningTime() %>
  </div>
<% } %>
<% if (signature.getFilename() != null) { %>
  <div>
    <span class="field">Document:</span> <%= signature.getFilename() %>
  </div>
<% } %>
<% if (signature.getDecretNumber() != null &&
   signature.getDecretNumber().trim().length() > 0) { %>
  <div>
     <span class="field">Decret:</span> <%= signature.getDecretNumber() %>
  </div>
<% } %>
<% if (signature.getTimeStampDate() != null) { %>
  <div>
     <span class="field">Segell de temps:</span> <%= signature.getTimeStamp() %>
  </div>
<% } %>
</div>
<%
  }
}
catch(Exception ex)
{
}
%>

