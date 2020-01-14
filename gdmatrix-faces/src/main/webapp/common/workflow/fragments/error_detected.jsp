<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk">

<f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle" var="workflowBundle" />

  <t:saveState value="#{instanceBean}" />

  <h:panelGrid columns="2" styleClass="workflowErrorDialog"
    headerClass="header" footerClass="footer" columnClasses="col1, col2">
    <f:facet name="header">
      <h:outputText value="#{workflowBundle.processingErrorDetected}" />
    </f:facet>

    <h:graphicImage alt="" url="/common/workflow/images/warning.gif" />

    <t:messages showSummary="true"
       globalOnly="true"
       layout="table"
       warnClass="warnMessage"
       errorClass="errorMessage" 
       fatalClass="fatalMessage" />

    <f:facet name="footer">
      <h:commandLink action="#{instanceBean.updateInstance}" 
        value="#{workflowBundle.continue}" 
        styleClass="workflowCommandLink" />
    </f:facet>
  </h:panelGrid>

</jsp:root>
