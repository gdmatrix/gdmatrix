<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  
  <f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle" var="workflowBundle" />  

  <t:saveState value="#{mobileIdFormBean}" />

  <sf:outputText value="#{mobileIdFormBean.message}"
    translator="#{instanceBean.translationEnabled ? 
      applicationBean.translator : null}" 
    translationGroup="wf:#{instanceBean.workflowName}"
    styleClass="workflowMessage" />
  <h:commandButton value="#{workflowBundle.continue}"
     alt="#{workflowBundle.continue}" title="#{workflowBundle.continue}"
     action="#{mobileIdFormBean.check}"
     styleClass="workflowButton"          
     style="display:block;text-align:right"/>   
</jsp:root>
