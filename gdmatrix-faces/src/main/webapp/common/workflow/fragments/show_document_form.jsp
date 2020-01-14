<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle"
    var="workflowBundle" />

  <t:saveState value="#{showDocumentFormBean}" />

  <sf:outputText value="#{showDocumentFormBean.message}" 
    translator="#{instanceBean.translationEnabled ? 
      applicationBean.translator : null}" 
    translationGroup="wf:#{instanceBean.workflowName}"
    styleClass="workflowMessage" />

  <sf:printableGroup name="printDocument">
    <sf:browser binding="#{showDocumentFormBean.browser}"
      port="#{applicationBean.defaultPort}"
      iframe="#{showDocumentFormBean.IFrame}"
      translator="#{instanceBean.translationEnabled ? 
        applicationBean.translator : null}" 
      translationGroup="wf:#{instanceBean.workflowName}"
      width="100%" height="400px"/>
  </sf:printableGroup>

  <h:panelGrid columns="1" width="100%" summary=""
    columnClasses="workflowPrintFooter" 
    style="margin-top:8px;margin-bottom:8px;"
    rendered="#{showDocumentFormBean.showPrintButton and
      not showDocumentFormBean.IFrame}">
    <h:outputLink value="javascript:printGroup('printDocument');"
      styleClass="docPrintButton">
      <h:graphicImage url="/common/workflow/images/print.gif" 
        style="vertical-align:middle;margin-right:6px;" /> 
      <h:outputText value="#{workflowBundle.printDocument}" />
    </h:outputLink>
  </h:panelGrid>

</jsp:root>

