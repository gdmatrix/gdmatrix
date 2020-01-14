<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk">

  <f:loadBundle basename="org.santfeliu.survey.web.resources.SurveyBundle"
                var="surveyBundle"/>

  <h:panelGrid columns="1" width="100%" id="surveyAdmin_mainPanel" 
  cellpadding="0" cellspacing="0">
    <h:commandButton value="#{surveyBundle.newSurvey}" action="#{adminSurveyBean.newSurvey}" id="surveyAdmin_newButton"/>
    <h:dataTable binding="#{adminSurveyBean.surveysDataTable}" id="surveyAdmin_surveysDT" var="row">
      <h:column>
        <f:facet name="header">
          <h:outputText value="#{surveyBundle.idUpper}" id="surveyAdmin_idHeader"/>
        </f:facet>
        <h:outputText value="#{row.survid}" id="surveyAdmin_survid"/>
      </h:column>
      <h:column>
        <f:facet name="header">
          <h:outputText value="#{surveyBundle.textUpper}" id="surveyAdmin_textHeader"/>
        </f:facet>
        <h:outputText value="#{row.text}" id="surveyAdmin_surveyText"/>
      </h:column>
      <h:column>
        <f:facet name="header">
          <h:outputText value="#{surveyBundle.votesUpper}" id="surveyAdmin_votesHeader"/>
        </f:facet>
        <h:outputText value="#{row.votes}" id="surveyAdmin_votes"/>
      </h:column>
      <h:column>
        <f:facet name="header">
          <h:outputText value="#{surveyBundle.dateUpper}" id="surveyAdmin_dateHeader" />
        </f:facet>
        <h:outputText value="#{row.startdate}" id="surveyAdmin_startDate"/>
      </h:column>
      <h:column>
        <f:facet name="header">
          <h:outputText value="#{surveyBundle.openUpper}" id="surveyAdmin_openHeader" />
        </f:facet>
        <h:graphicImage url="/common/survey/images/open.gif" rendered="#{row.open == 'Y'}" id="surveyAdmin_openIcon" />
        <h:graphicImage url="/common/survey/images/closed.gif" rendered="#{row.open == 'N'}" id="surveyAdmin_closedIcon" />
      </h:column>
      <h:column>
        <h:commandLink action="#{adminSurveyBean.openSurvey}" 
        rendered="#{row.open == 'N'}" id="surveyAdmin_openCommandLink" >
          <h:outputText value="#{surveyBundle.open}" id="surveyAdmin_openString"/>
        </h:commandLink>
        <h:commandLink action="#{adminSurveyBean.closeSurvey}" 
        rendered="#{row.open == 'Y'}" id="surveyAdmin_closeCommandLink" >
          <h:outputText value="#{surveyBundle.close}" id="surveyAdmin_closeString"/>
        </h:commandLink>
      </h:column>            
    </h:dataTable>
    <h:panelGrid columns="1" width="100%" id="surveyAdmin_creationPanel"
    rendered="#{adminSurveyBean.renderCreationPanel}" cellpadding="0" cellspacing="0">
      <h:outputText value="#{surveyBundle.newSurvey}" id="surveyAdmin_newSurveyText" />
      <h:panelGrid columns="1" width="100%" id="surveyAdmin_newSurveyPanel">
        <h:outputText value="#{surveyBundle.text}" id="surveyAdmin_newSurveyTextString"/>
        <h:inputText value="#{adminSurveyBean.inputSurveyText}" id="surveyAdmin_inputSurveyText" size="70"/>
        <h:outputText value="#{surveyBundle.answers}" id="surveyAdmin_newSurveyAnswersString" />
        <h:commandButton action="#{adminSurveyBean.addNewAnswer}" value="#{surveyBundle.add}" id="surveyAdmin_addButton" />
        <h:dataTable binding="#{adminSurveyBean.newAnswersDataTable}" var="row" id="surveyAdmin_newAnswersDT"> 
          <h:column>
            <h:inputText value="#{row.text}" id="surveyAdmin_inputNewAnswerText"/>
          </h:column>
          <h:column>
            <h:commandButton action="#{adminSurveyBean.removeNewAnswer}" value="#{surveyBundle.remove}" id="surveyAdmin_delButton"/>
          </h:column>
        </h:dataTable>
        <h:panelGrid columns="2" id="surveyAdmin_saveCancelPanel" 
        cellpadding="0" cellspacing="0">
          <h:commandButton action="#{adminSurveyBean.saveNewSurvey}" value="#{surveyBundle.save}" id="surveyAdmin_saveButton"/>
          <h:commandButton action="#{adminSurveyBean.cancelNewSurvey}" value="#{surveyBundle.cancel}" id="surveyAdmin_cancelButton" />
        </h:panelGrid>
      </h:panelGrid>
    </h:panelGrid>
  </h:panelGrid>
  
  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    errorClass="errorMessage" warnClass="warnMessage"
    infoClass="infoMessage"/>

  <t:saveState value="#{adminSurveyBean.renderCreationPanel}" />
  
</jsp:root>