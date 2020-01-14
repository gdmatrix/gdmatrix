<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:saveState value="#{outputTextFormBean}" />

  <sf:outputText value="#{outputTextFormBean.message}" 
    translator="#{instanceBean.translationEnabled ? 
      applicationBean.translator : null}" 
    translationGroup="wf:#{instanceBean.workflowName}"
    styleClass="workflowMessage" 
    escape="#{!outputTextFormBean.html}" />

</jsp:root>
