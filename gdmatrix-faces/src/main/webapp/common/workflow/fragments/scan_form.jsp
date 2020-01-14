<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  
  <f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle" var="workflowBundle" />  

  <t:saveState value="#{scanDocumentFormBean}" />

  <sf:outputText value="#{scanDocumentFormBean.message}"
    translator="#{instanceBean.translationEnabled ? 
      applicationBean.translator : null}" 
    translationGroup="wf:#{instanceBean.workflowName}"
    styleClass="workflowMessage" />
 
  <t:div styleClass="buttonBar">
    <h:commandButton value="#{workflowBundle.scan}"
       alt="#{workflowBundle.scan}" title="#{workflowBundle.scan}"
       styleClass="workflowButton"
       onclick="javascript:scanDocument();return false"
       rendered="#{userSessionBean.matrixClientEnabled}" />   

    <sf:matrixclient command="org.santfeliu.matrix.client.cmd.doc.ScanDocumentCommand"
      action="#{scanDocumentFormBean.documentScanned}"
      model="#{scanDocumentFormBean.model}"
      function="scanDocument"
      rendered="#{userSessionBean.matrixClientEnabled}"
      helpUrl="#{matrixClientBean.helpUrl}"/>
  </t:div>
  
</jsp:root>
