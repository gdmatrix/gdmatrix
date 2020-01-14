<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:saveState value="#{surveyBean}" />

  <f:loadBundle basename="org.santfeliu.survey.web.resources.SurveyBundle"
                var="surveyBundle"/>

  <t:div id="answersPanel" rendered="#{!surveyBean.viewResults}">
    <h:outputText value="#{surveyBean.survey.text}" id="surveyTitle" />
    <h:selectOneRadio value="#{surveyBean.answerSelected}" layout="pageDirection" >
      <f:selectItems value="#{surveyBean.answerItemList}" />
    </h:selectOneRadio>
    <h:panelGroup id="surveyButtonPanel">
      <h:commandButton value="#{surveyBundle.vote}" action="#{surveyBean.vote}" id="surveyVote"/>
      <h:commandButton value="#{surveyBundle.view}" action="#{surveyBean.view}" id="surveyView"/>
    </h:panelGroup>
  </t:div>

  <t:div id="resultsPanel" rendered="#{surveyBean.viewResults}">
    <h:outputText value="#{surveyBean.survey.text}" id="surveyTitle2"/>
    <t:dataTable rowClasses="row1,row2" columnClasses="col1,col2"
                 value="#{surveyBean.answerList}" var="answer"
                 id="resultsTable">
      <t:column>
        <h:panelGrid columns="1">
          <h:panelGroup>
            <h:graphicImage url="/common/survey/images/#{surveyBean.barImage}" width="#{surveyBean.answerWidth}px" height="#{surveyBean.barHeight}px" />
            <h:graphicImage url="/common/survey/images/#{surveyBean.remainImage}" width="#{surveyBean.remainWidth}px" height="#{surveyBean.barHeight}px" />
          </h:panelGroup>
          <h:outputText value="#{answer.text}" id="answerText2" />
        </h:panelGrid>
      </t:column>
      <t:column>
        <h:panelGrid columns="1">
          <h:outputText value="#{answer.voteCount} #{surveyBundle.votes}" id="answerVotes" />
          <h:outputText value="#{surveyBean.answerPercent} %" id="answerPercent" />
        </h:panelGrid>
      </t:column>
    </t:dataTable>
    <h:commandButton value="#{surveyBundle.back}" action="#{surveyBean.back}" id="surveyBack"/>
  </t:div>

</jsp:root>