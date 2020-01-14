<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:saveState value="#{inputTextFormBean}" />

  <h:panelGrid columns="1" width="100%">
    <sf:outputText value="#{inputTextFormBean.message}" 
      translator="#{instanceBean.translationEnabled ? 
        applicationBean.translator : null}" 
      translationGroup="wf:#{instanceBean.workflowName}"
      styleClass="workflowMessage" />

    <h:inputTextarea id="text" value="#{inputTextFormBean.text}" 
      style="width:99%" rows="8" required="true" 
      validator="#{inputTextFormBean.validateText}" 
      onkeypress="checkMaxLength(this, 2000);" />
  </h:panelGrid>

  <h:message for="text" warnClass="warnMessage" 
    errorClass="errorMessage" fatalClass="fatalMessage" showDetail="true"/>        

</jsp:root>
