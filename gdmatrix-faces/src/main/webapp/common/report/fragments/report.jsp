<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.report.web.resources.ReportBundle" 
    var="reportBundle"/>

  <t:div styleClass="reportPanel">
   <jsp:include page="/common/report/templates/${userSessionBean.selectedMenuItem.properties.reportTemplate != null ? userSessionBean.selectedMenuItem.properties.reportTemplate : 'default'}/template.jsp" /> 
  </t:div>
</jsp:root>
