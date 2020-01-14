<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.workflow.web.resources.WorkflowBundle"
              var="workflowBundle" />
<f:loadBundle basename="org.santfeliu.doc.web.resources.DocumentBundle"
              var="documentBundle" />
<f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
              var="objectBundle" />

  <t:saveState value="#{uploadDocumentsFormBean}" />
  
  <h:panelGrid columns="1" width="100%">
    <sf:outputText value="#{uploadDocumentsFormBean.message}" 
      translator="#{instanceBean.translationEnabled ? 
        applicationBean.translator : null}" 
      translationGroup="wf:#{instanceBean.workflowName}"
      styleClass="workflowMessage" />

    <t:dataTable value="#{uploadDocumentsFormBean.documentsTable}" 
                 var="document" 
                 width="100%" 
                 styleClass="uploadFilesList"
                 rowClasses="row1,row2"
                 columnClasses="col1,col2,col3"
                 headerClass="header"
                 renderedIfEmpty="false" cellpadding="2" cellspacing="0">
      <t:column width="70%">
        <f:facet name="header">
          <h:outputText value="#{workflowBundle.description}" />
        </f:facet>
        <h:outputLink value="#{uploadDocumentsFormBean.documentURL}" 
          target="_blank" styleClass="documentLink" rendered="#{document.uuid != null}">
          <h:graphicImage value="#{document.mimetype}" style="vertical-align:middle"
                          rendered="#{document.mimetype != null}"/>
          <h:outputText value="#{document.description}" style="margin-left:4px" />
        </h:outputLink>


      </t:column>
      <t:column width="15%" style="text-align:right">
        <f:facet name="header">
          <h:outputText value="#{workflowBundle.size}" />
        </f:facet>
        <h:outputText value="#{document.size}" />
      </t:column>
      <t:column width="15%" style="text-align:center">
        <f:facet name="header">
          <h:outputText value="" />
        </f:facet>

        <h:commandLink
          styleClass="workflowCommandLink"
          rendered="#{document.uuid != null}"          
          action="#{uploadDocumentsFormBean.deleteDocument}">
          <h:outputText value="#{workflowBundle.delete}" />
        </h:commandLink>      
      </t:column>
    </t:dataTable>

    <t:div rendered="#{!uploadDocumentsFormBean.renderDocumentForm}">
      <t:outputText value="#{workflowBundle.attach}: " styleClass="workflowMessage" />
      <t:inputFileUpload id="fileinput" styleClass="fileUploadButton" size="0"
        value="#{uploadDocumentsFormBean.uploadedFile}" storage="file"
        valueChangeListener="#{uploadDocumentsFormBean.uploadFile}"
        onchange="showOverlay();submit();" />
    </t:div>

    <t:div styleClass="uploadDocumentForm" rendered="#{document.uuid == null and
          uploadDocumentsFormBean.renderDocumentForm}">

      <t:div>
        <t:div>
          <h:outputText value="#{workflowBundle.attachMessage1} " />
          <h:outputText value="#{uploadDocumentsFormBean.uploadFileManager.fileName} (#{uploadDocumentsFormBean.uploadFileManager.fileSize}). " style="font-weight:bold"/>
        </t:div>
        <t:div>
          <h:outputText value="#{workflowBundle.attachMessage2} :" />
        </t:div>
      </t:div>

      <t:div rendered="#{uploadDocumentsFormBean.uploadFileManager.renderTitle}">
        <h:outputText value="#{workflowBundle.documentTitle}: "
          styleClass="textBox" style="width:14%" />
        <h:inputText value="#{uploadDocumentsFormBean.uploadFileManager.docTitle}"
          styleClass="inputBox" style="width:83%" />
      </t:div>

      <t:div rendered="#{uploadDocumentsFormBean.uploadFileManager.renderDocLanguage}">
        <h:outputLabel value="#{workflowBundle.documentLanguage}: "
          styleClass="textBox" style="width:14%"/>
        <t:selectOneMenu
          value="#{uploadDocumentsFormBean.uploadFileManager.docLanguage}"
          styleClass="selectBox">
          <f:selectItem itemValue="%%" itemLabel="universal"/>
          <f:selectItem itemValue="ca" itemLabel="#{documentBundle.selectItemCA}"/>
          <f:selectItem itemValue="es" itemLabel="#{documentBundle.selectItemES}"/>
          <f:selectItem itemValue="en" itemLabel="#{documentBundle.selectItemEN}"/>
          <f:selectItem itemValue="fr" itemLabel="#{documentBundle.selectItemFR}"/>
          <f:selectItem itemValue="it" itemLabel="#{documentBundle.selectItemIT}"/>
          <f:selectItem itemValue="de" itemLabel="#{documentBundle.selectItemDE}"/>
        </t:selectOneMenu>
      </t:div>

      <t:div style="text-align: right;width:100%">
        <h:commandButton value="#{objectBundle.store}" styleClass="workflowButton"
          action="#{uploadDocumentsFormBean.storeFile}"
          image="#{userSessionBean.icons.store}"
          alt="#{objectBundle.store}" title="#{objectBundle.store}"/>
        <h:commandButton value="#{objectBundle.cancel}" styleClass="workflowButton"
          action="#{uploadDocumentsFormBean.cancelFile}"
          image="#{userSessionBean.icons.cancel}"
          alt="#{objectBundle.cancel}" title="#{objectBundle.cancel}"/>
      </t:div>

   </t:div>

  </h:panelGrid>

</jsp:root>