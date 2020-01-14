<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.sign.web.resources.SignatureBundle"
                var="signatureBundle" />

  <sf:browser id="headerBrowser"
            url="#{signatureValidatorBean.headerUrl}"
            port="#{applicationBean.defaultPort}"
            rendered="#{signatureValidatorBean.headerRender}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
  
  <t:div styleClass="validatorPanel">
  
    <t:div styleClass="referencePanel">
      <h:outputLabel for="sigIdInput" value="#{signatureBundle.reference}: " 
        styleClass="referenceLabel" />
      <h:inputText id="sigIdInput" value="#{signatureValidatorBean.sigId}" size="50"
        styleClass="referenceInput" />
      <h:commandButton id="defaultButton" action="#{signatureValidatorBean.validate}"
        value="#{signatureBundle.validate}" styleClass="validateButton"
        onclick="this.className='validateButtonOn'" />
    </t:div>

    <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
                errorClass="errorMessage" warnClass="warnMessage" />

    <t:div rendered="#{signatureValidatorBean.document != null}"
      styleClass="metadataPanel">

      <t:div styleClass="metadataPanel">
        <t:panelGrid columns="2" columnClasses="col1,col2">
          <h:outputText value="#{signatureBundle.docId}:" />
          <h:outputText value="#{signatureValidatorBean.document.docId}" />

          <h:outputText value="#{signatureBundle.title}:" />
          <h:outputText value="#{signatureValidatorBean.document.title}" />

          <h:outputText value="#{signatureBundle.contentId}:" />
          <h:outputText value="#{signatureValidatorBean.document.content.contentId}" />

          <h:outputText value="#{signatureBundle.contentType}:" />
          <h:outputText value="#{signatureValidatorBean.document.content.contentType}" />

          <h:outputText value="#{signatureBundle.language}:" />
          <h:outputText value="#{signatureValidatorBean.language}" />

          <h:outputText value="#{signatureBundle.size}:" />
          <h:outputText value="#{signatureValidatorBean.size}" />
        </t:panelGrid>
      </t:div>

      <t:div styleClass="buttonPanel">
        <h:outputLink value="#{signatureValidatorBean.downloadUrl}"
          styleClass="downloadButton">
          <h:outputText value="#{signatureBundle.download}" />
        </h:outputLink>

        <h:outputLink value="#{signatureValidatorBean.viewUrl}"
          styleClass="viewButton"
          rendered="#{signatureValidatorBean.viewUrl != null}" target="_blank">
          <h:outputText value="#{signatureBundle.view}" />
        </h:outputLink>
      </t:div>

      <t:div rendered="#{signatureValidatorBean.signatures != null}"
        styleClass="signaturePanel">
        <h:outputText value="#{signatureBundle.signatures}:"
          styleClass="signatureHeader" />
        <h:outputText value="#{signatureValidatorBean.signatures}"
          styleClass="signatureInfo" escape="false" />
      </t:div>
    </t:div>
  </t:div>

  <sf:browser id="footerBrowser"
            url="#{signatureValidatorBean.footerUrl}"
            port="#{applicationBean.defaultPort}"
            rendered="#{signatureValidatorBean.footerRender}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />

</jsp:root>