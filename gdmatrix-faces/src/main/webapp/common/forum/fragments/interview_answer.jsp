<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.forum.web.resources.ForumBundle" var="forumBundle" />
<f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" var="objectBundle" />

  <t:saveState value="#{forumCatalogueBean}" />

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />

  <t:div styleClass="objectForm">

  <t:div rendered="#{userSessionBean.selectedMenuItem.properties.answerHeaderDocId!=null}"
               styleClass="headerDocument">
    <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.answerHeaderDocId}"
      port="#{applicationBean.defaultPort}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}"
      rendered="#{userSessionBean.selectedMenuItem.properties.answerHeaderDocId!=null}"/>
  </t:div>

    <t:div styleClass="interviewAnswer">
      <t:div styleClass="sheet">

      <t:div styleClass="questionInfo" >
        <h:outputText value="##{forumCatalogueBean.currentQuestion.inputIndex} " styleClass="inputIndex"/>
        <h:outputText value="#{forumCatalogueBean.currentQuestionDateTime}" styleClass="dateTime">
          <f:convertDateTime pattern="EEEE, dd/MM/yyyy HH:mm:ss" />
        </h:outputText>
      </t:div>

        <t:div styleClass="topButtons">
        <h:commandButton action="#{forumCatalogueBean.cancelQuestion}"
          value="#{objectBundle.close}" styleClass="cancelButton"
          immediate="true" />
      </t:div>

        <t:div styleClass="questionBody">
        <h:outputText value="#{forumCatalogueBean.questionLabel} "
          styleClass="user" rendered="#{forumCatalogueBean.currentQuestion.text != null}"/>
        <h:outputText value="(#{forumCatalogueBean.currentQuestion.creationUserId}) "
          styleClass="user"
          rendered="#{forumCatalogueBean.currentQuestion.text != null and forumCatalogueBean.editorUser}"/>
        <h:outputText value=": " styleClass="user" rendered="#{forumCatalogueBean.currentQuestion.text != null}"/>
        <t:div>
          <h:outputText
            value="#{forumCatalogueBean.currentQuestion.text}"
            style="width:99%"/>
        </t:div>
      </t:div>

      <t:div styleClass="answerBody"
        rendered="#{forumCatalogueBean.currentAnswer != null}">
        <h:outputText value=" #{userSessionBean.selectedMenuItem.properties.answererAlias != null ?
          userSessionBean.selectedMenuItem.properties.answererAlias : forumBundle.answer}:"
          styleClass="boxLabel" />
        <h:inputTextarea value="#{forumCatalogueBean.currentAnswer.text}"
          styleClass="inputBox" style="width:100%"
          onkeypress="checkMaxLength(this, 4000)"
          rows="10"/>
        <h:outputText value="#{forumBundle.comments}:" styleClass="boxLabel" />
        <h:inputTextarea value="#{forumCatalogueBean.currentAnswer.comments}"
          styleClass="inputBox" style="width:100%"
          onkeypress="checkMaxLength(this, 4000)"
          rows="10"/>
      </t:div>
    </t:div>
  </t:div>
    <t:div styleClass="footer">
      <h:commandButton action="#{forumCatalogueBean.storeInterviewAnswer}"
                       value="#{objectBundle.store}" styleClass="storeButton"
                       rendered="#{forumCatalogueBean.editorUser}"/>
      <h:commandButton action="#{forumCatalogueBean.setQuestionVisible}"
        value="#{forumBundle.questionVisible}" styleClass="cancelButton"
        rendered="#{not forumCatalogueBean.currentQuestion.visible}"
        onclick="return confirm('#{forumBundle.visibleAlert}')"/>
    </t:div>
</t:div>

</jsp:root>
