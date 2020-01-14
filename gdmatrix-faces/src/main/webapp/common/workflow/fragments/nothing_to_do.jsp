<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk">

<f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle" var="workflowBundle" />

  <t:div styleClass="paragraph">
    <h:outputText value="#{workflowBundle.nothingToDo}" />
  </t:div>

  <t:div rendered="#{instanceBean.instanceUserState != null}" styleClass="paragraph">
    <h:outputText value="#{workflowBundle.currentState}: #{instanceBean.instanceUserState}" />
  </t:div>

</jsp:root>
