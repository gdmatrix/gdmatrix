<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" xmlns:c="http://java.sun.com/jsp/jstl/core">
   <!-- CONTENT -->
   <c:set var="_customBar" scope="request" value="/templates/widgetportal/custombar.jsp" />
   <jsp:include page="${requestScope['_body']}"/>
</jsp:root>
