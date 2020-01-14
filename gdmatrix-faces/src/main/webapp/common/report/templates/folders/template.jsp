<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  
  <f:verbatim>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js">0;</script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js">0;</script>
    <script src="/plugins/jquery/jquery-1.10.2.js">0;</script>
    <script src="/plugins/jquery/ui/1.11.4/jquery-ui.js">0;</script>
    <script src="/plugins/jquery/datepicker/datepicker-ca.js">0;</script>
    <script src="/plugins/jquery/datepicker/datepicker-es.js">0;</script>
    <script src="/common/report/templates/folders/template.js">0;</script>
    <link rel="stylesheet" type="text/css" href="https:////code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css" />
  </f:verbatim>    

    <t:div styleClass="page-header">
      <t:div styleClass="icona-carpetes" style="background: url('#{userSessionBean.selectedMenuItem.properties.iconURL}') no-repeat 0 0; background-size: 60px"></t:div>
      <sf:heading level="#{userSessionBean.selectedMenuItem.properties.headingLevel}" styleClass="title-carpetes">
        <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.label}" 
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </sf:heading> 
    </t:div>  
      
    <jsp:include  page="/common/report/templates/default/template.jsp" /> 

</jsp:root>
