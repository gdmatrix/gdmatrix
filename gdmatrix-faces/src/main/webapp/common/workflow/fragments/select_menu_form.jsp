<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:saveState value="#{selectMenuFormBean}" />

  <sf:outputText value="#{selectMenuFormBean.message}" 
    translator="#{instanceBean.translationEnabled ? 
      applicationBean.translator : null}" 
    translationGroup="wf:#{instanceBean.workflowName}"
    styleClass="workflowMessage" />

  <t:dataTable value="#{selectMenuFormBean.options}" 
    var="option" width="100%">
    <t:column>
      <h:commandLink action="#{selectMenuFormBean.selectOption}" 
        styleClass="selectMenuLink">
        <h:graphicImage alt="" url="/common/workflow/images/redbutton.gif" 
          style="vertical-align:middle; border-style:none; margin-right:6px"/>
        <sf:outputText value="#{option.label}"
          translator="#{instanceBean.translationEnabled ?
            applicationBean.translator : null}"
          translationGroup="wf:#{instanceBean.workflowName}" />
      </h:commandLink>
    </t:column>
  </t:dataTable>

</jsp:root>
