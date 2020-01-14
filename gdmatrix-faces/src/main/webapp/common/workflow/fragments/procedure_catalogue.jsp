<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle" var="workflowBundle" />

  <t:saveState value="#{procedureCatalogueBean}" />
  <sf:saveScroll />

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" style="margin-bottom:6px;" />

  <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.catalogueIntro}"
    translator="#{applicationBean.translator}"
    styleClass="workflowMessage" style="display:block" />

  <h:panelGroup styleClass="catalogue">
    <sf:treeMenu id="tree" var="item"
                 baseMid="#{procedureCatalogueBean.catalogueMid}"
                 expandedMenuItems="#{procedureCatalogueBean.expandedMenuItems}"
                 expandImageUrl="/images/expand.gif"
                 collapseImageUrl="/images/collapse.gif">
      <f:facet name="data">
        <h:panelGroup>
          <t:graphicImage value="/images/item.gif" alt=""
            styleClass="itemImage" title="" rendered="#{item.leaf}" />
          <sf:outputText value="#{item.label} "
            styleClass="#{item.leaf ? 'procedureText' : 'procedureGroupText'}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />

            <h:graphicImage url="/common/workflow/images/certificate.gif"
              styleClass="procedureIcon"
              title="Autenticaci&#243; amb certificat"
              rendered="#{procedureCatalogueBean.certificateRequired}" />
            <h:graphicImage url="/common/workflow/images/signature.gif"
              styleClass="procedureIcon"
              title="Amb signatura electr&#242;nica"
              rendered="#{procedureCatalogueBean.signatureRequired}" />
            <!-- certificate required -->
            <sf:secureCommandLink
              rendered="#{procedureCatalogueBean.transactEnabled and
              procedureCatalogueBean.certificateRequired}"
              action="#{procedureCatalogueBean.certStartProcedure}"
              scheme="https" port="#{applicationBean.clientSecurePort}"
              styleClass="procedureButton">
              <h:outputText value="#{workflowBundle.catalogueProcess}" />
            </sf:secureCommandLink>
            <sf:secureCommandLink
              rendered="#{procedureCatalogueBean.simulateEnabled and
              procedureCatalogueBean.certificateRequired}"
              action="#{procedureCatalogueBean.certSimulateProcedure}"
              scheme="https" port="#{applicationBean.clientSecurePort}"
              styleClass="procedureButton">
              <h:outputText value="#{workflowBundle.catalogueSimulate}" />
            </sf:secureCommandLink>
            <!-- no certificate required -->
            <sf:secureCommandLink
              rendered="#{procedureCatalogueBean.transactEnabled and
              not procedureCatalogueBean.certificateRequired}"
              action="#{procedureCatalogueBean.startProcedure}"
              scheme="https" port="#{applicationBean.serverSecurePort}"
              styleClass="procedureButton">
              <h:outputText value="#{workflowBundle.catalogueProcess}" />
            </sf:secureCommandLink>
            <sf:secureCommandLink
              rendered="#{procedureCatalogueBean.simulateEnabled and
              not procedureCatalogueBean.certificateRequired}"
              action="#{procedureCatalogueBean.simulateProcedure}"
              scheme="https" port="#{applicationBean.serverSecurePort}"
              styleClass="procedureButton">
              <h:outputText value="#{workflowBundle.catalogueSimulate}" />
            </sf:secureCommandLink>
            <!-- external procedures -->
            <h:outputLink value="#{procedureCatalogueBean.externalURL}"
              rendered="#{procedureCatalogueBean.externalURL != null}"
              target="_blank" styleClass="procedureButton">
              <h:outputText value="#{workflowBundle.catalogueExternal}" />
            </h:outputLink>
            <!-- documentation -->
            <h:outputLink
              value="#{procedureCatalogueBean.procedureInfoURL}"
              rendered="#{procedureCatalogueBean.documentEnabled}"
              styleClass="consultButton">
              <h:outputText value="#{workflowBundle.catalogueConsult}" />
            </h:outputLink>
        </h:panelGroup>
      </f:facet>
    </sf:treeMenu>
  </h:panelGroup>

</jsp:root>

