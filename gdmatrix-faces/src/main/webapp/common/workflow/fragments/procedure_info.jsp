<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle" var="workflowBundle" />
<f:loadBundle basename="org.santfeliu.doc.web.resources.DocumentBundle" var="documentBundle" />
<f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" var="objectBundle" />

  <t:saveState value="#{procedureInfoBean}" />

  <t:div style="width:100%;text-align:right" rendered="#{procedureInfoBean.editorUser}">
    <h:commandLink value="#{objectBundle.edit}"
      action="#{procedureInfoBean.editProcedureInfo}"
      styleClass="workflowCommandLink"
      rendered="#{!procedureInfoBean.editing}"/>
    <h:commandLink value="#{objectBundle.store}"
      action="#{procedureInfoBean.storeProcedureInfo}"
      styleClass="workflowCommandLink"
      rendered="#{procedureInfoBean.editing}"/>
    <h:commandLink value="#{objectBundle.cancel}"
      action="#{procedureInfoBean.closeProcedureInfo}"
      styleClass="workflowCommandLink"
      rendered="#{procedureInfoBean.editing}"/>
  </t:div>
  
  <t:div>
    <h:graphicImage url="/common/doc/images/security-lock.png" height="16" width="16"
      style="vertical-align:middle" alt="#{documentBundle.lock}"
      rendered="#{procedureInfoBean.lockUserId != null
       and procedureInfoBean.lockUserId != userSessionBean.username}"/>
    <h:graphicImage
      url="/common/doc/images/security-lock-open.png" height="16" width="16"
      style="vertical-align:middle" alt="#{documentBundle.lock}"
      rendered="#{procedureInfoBean.lockUserId != null
       and procedureInfoBean.lockUserId == userSessionBean.username}"/>
    <h:outputText value="#{documentBundle.lockedBy} #{procedureInfoBean.lockUserId} : #{procedureInfoBean.editor.document.docId} (#{procedureInfoBean.editor.document.language})"
      rendered="#{procedureInfoBean.lockUserId != null}"
      style="margin-left:2px;display:inline-block; font-weight:bold; vertical-align:middle"/>
  </t:div>
  <sf:editor toolbarSet="CustomDocuments"
             rendered="#{procedureInfoBean.editing}"
    height="600px" width="100%"
    configProperties="#{procedureInfoBean.editor.configProperties}"
    value="#{procedureInfoBean.editor.documentData}" />

  <sf:printableGroup name="printDocument" rendered="#{!procedureInfoBean.editing}">
    <sf:browser url="#{procedureInfoBean.documentURL}"
      port="#{applicationBean.defaultPort}"
      translator="#{userSessionBean.translator}"
      translationGroup="doc:#{procedureInfoBean.docId}" />
  </sf:printableGroup>

  <t:div styleClass="procedureActionBar">
    <sf:secureCommandLink
      value="#{workflowBundle.upperProcess}"
      rendered="#{procedureInfoBean.transactEnabled and
      procedureInfoBean.certificateRequired and !procedureInfoBean.editing}"
      action="#{procedureInfoBean.certStartProcedure}"
      scheme="https" port="#{applicationBean.clientSecurePort}"
      styleClass="workflowCommandLink">
    </sf:secureCommandLink>
    <sf:secureCommandLink
      value="#{workflowBundle.upperSimulate}"
      rendered="#{procedureInfoBean.simulateEnabled and
      procedureInfoBean.certificateRequired and !procedureInfoBean.editing}"
      action="#{procedureInfoBean.certSimulateProcedure}"
      scheme="https" port="#{applicationBean.clientSecurePort}"
      styleClass="workflowCommandLink">
    </sf:secureCommandLink>
    <sf:secureCommandLink
      value="#{workflowBundle.upperProcess}"
      rendered="#{procedureInfoBean.transactEnabled and
      not procedureInfoBean.certificateRequired and !procedureInfoBean.editing}"
      action="#{procedureInfoBean.startProcedure}"
      scheme="https" port="#{applicationBean.serverSecurePort}"
      styleClass="workflowCommandLink">
    </sf:secureCommandLink>
    <sf:secureCommandLink
      value="#{workflowBundle.upperSimulate}"
      rendered="#{procedureInfoBean.simulateEnabled and
      not procedureInfoBean.certificateRequired and !procedureInfoBean.editing}"
      action="#{procedureInfoBean.simulateProcedure}"
      scheme="https" port="#{applicationBean.serverSecurePort}"
      styleClass="workflowCommandLink">
    </sf:secureCommandLink>
    <h:outputLink value="javascript:printGroup('printDocument');" 
      styleClass="workflowCommandLink"
      rendered="#{!procedureInfoBean.editing and userSessionBean.menuModel.browserType == 'desktop'}">
      <h:outputText value="#{webBundle.buttonPrint}" />
    </h:outputLink>
    <h:commandLink value="#{workflowBundle.upperCatalogue}" 
      action="#{procedureCatalogueBean.showCatalogue}" 
      styleClass="workflowCommandLink"
      rendered="#{!procedureInfoBean.editing}"/>
  </t:div>

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true" 
    warnClass="warnMessage"
    errorClass="errorMessage" 
    fatalClass="fatalMessage" />

</jsp:root>