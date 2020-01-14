<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle" var="workflowBundle" />
  <f:loadBundle basename="org.santfeliu.security.web.resources.MobileIdBundle" var="mobileIdBundle" />

  <t:saveState value="#{signatureFormBean}" />

  <sf:outputText value="#{signatureFormBean.message}"
    translator="#{instanceBean.translationEnabled ?
      applicationBean.translator : null}"
    translationGroup="wf:#{instanceBean.workflowName}"
    styleClass="workflowMessage" />

  <sf:browser binding="#{signatureFormBean.browser}"
     port="#{applicationBean.defaultPort}"
     translator="#{instanceBean.translationEnabled ?
       applicationBean.translator : null}"
     translationGroup="wf:#{instanceBean.workflowName}"
     iframe="#{signatureFormBean.IFrame}"
     width="100%" height="400px" />

  <!-- signature with VALid -->
  <t:div rendered="#{userSessionBean.loginMethod == 'VALID'}">
    <h:graphicImage url="/common/workflow/images/valid.png" alt="VALID" title="VALID" 
      style="margin:8px;display:block;" />
    <t:div styleClass="buttonBar">
      <h:commandButton value="#{workflowBundle.sign}"
        action="#{signatureFormBean.signValid}"
        alt="#{workflowBundle.sign}" title="#{workflowBundle.sign}"
        onclick="javascript:showOverlay(); return true"
        styleClass="workflowButton" />
      <h:commandButton value="#{workflowBundle.cancel}"
        action="#{signatureFormBean.cancelSignature}"
        alt="#{workflowBundle.cancel}" title="#{workflowBundle.cancel}"
        onclick="javascript:showOverlay(); return true"
        styleClass="workflowButton" /> 
    </t:div>
  </t:div>

  <!-- signature with MobileId -->
  <h:panelGroup rendered="#{userSessionBean.loginMethod == 'MOBILEID'}">
    <h:graphicImage url="/common/workflow/images/mobileid.png" alt="MOBILEID" title="MOBILEID" 
      style="margin:8px;display:block;" />
    <h:panelGroup rendered="#{signatureFormBean.mobileidState == 0}">
      <t:div styleClass="buttonBar">
        <h:commandButton value="#{workflowBundle.sign}"
          action="#{signatureFormBean.signMobileid}"
          alt="#{workflowBundle.sign}" title="#{workflowBundle.sign}"
          onclick="javascript:showOverlay(); return true"
          styleClass="workflowButton" />
        <h:commandButton value="#{workflowBundle.cancel}"
          action="#{signatureFormBean.cancelSignature}"
          alt="#{workflowBundle.cancel}" title="#{workflowBundle.cancel}"
          onclick="javascript:showOverlay(); return true"
          styleClass="workflowButton" />
      </t:div>
    </h:panelGroup>
    <h:panelGroup rendered="#{signatureFormBean.mobileidState == 1}">
      <h:outputText value="#{mobileIdBundle.enterPIN}" styleClass="mobileidEnterPIN"/>
      <t:div styleClass="buttonBar">
        <h:commandButton value="#{mobileIdBundle.pinEntered}"
          action="#{signatureFormBean.pinEntered}"
          alt="#{mobileIdBundle.pinEntered}" title="#{mobileIdBundle.pinEntered}"
          onclick="javascript:showOverlay(); return true"
          styleClass="workflowButton" />
        <h:commandButton value="#{mobileIdBundle.cancel}"
          action="#{signatureFormBean.pinCancelled}"
          alt="#{mobileIdBundle.cancel}" title="#{mobileIdBundle.cancel}"
          onclick="javascript:showOverlay(); return true"
          styleClass="workflowButton" />
      </t:div>
    </h:panelGroup>
  </h:panelGroup>

  <!-- signature with certificate -->  
  <t:div rendered="#{userSessionBean.loginMethod != 'VALID' and 
                     userSessionBean.loginMethod != 'MOBILEID'}" styleClass="buttonBar">
    
    <!-- signature with matrix client -->
    <h:panelGroup rendered="#{userSessionBean.matrixClientEnabled}">
      <h:commandButton value="#{workflowBundle.sign}"
         alt="#{workflowBundle.sign}" title="#{workflowBundle.sign}"
         styleClass="workflowButton"
         onclick="javascript:signDocument({sigId:'#{signatureFormBean.document}'});return false" />
      <h:commandButton value="#{workflowBundle.cancel}"
         action="#{signatureFormBean.cancelSignature}"
         alt="#{workflowBundle.cancel}" title="#{workflowBundle.cancel}"
         onclick="javascript:showOverlay(); return true;"
         styleClass="workflowButton" />
      <sf:matrixclient command="org.santfeliu.matrix.client.cmd.SignDocumentCommand"
        action="#{signatureFormBean.documentSigned}"
        model="#{signatureFormBean.model}"
        function="signDocument"
        helpUrl="#{matrixClientBean.helpUrl}"/>
    </h:panelGroup>

    <!-- signature with applets -->
    <h:panelGroup rendered="#{not userSessionBean.matrixClientEnabled}">
      <sf:signer action="#{signatureFormBean.sign}"
        document="#{signatureFormBean.document}"
        result="#{signatureFormBean.result}"
        port="#{applicationBean.serverSecurePort}"/>
    </h:panelGroup>
  </t:div>

</jsp:root>
