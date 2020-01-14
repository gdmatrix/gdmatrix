<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle" var="workflowBundle" />

<t:div styleClass="final_form">
    <h:graphicImage url="#{instanceBean.finalIcon}" 
      style="vertical-align:middle" alt="" />
    <sf:outputText value="#{instanceBean.finalMessage}" 
      translator="#{instanceBean.translationEnabled ? applicationBean.translator : null}" 
      translationGroup="#{instanceBean.workflowName}"
      styleClass="workflowMessage" escape="false" />
  </t:div>

</jsp:root>
