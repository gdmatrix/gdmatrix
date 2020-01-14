<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
          
  <f:loadBundle basename="org.santfeliu.misc.userprofile.web.resources.UserProfileBundle" var="miscBundle"/>
  <f:loadBundle basename="org.santfeliu.web.resources.WebBundle" var="webBundle"/>

  <t:saveState value="#{userProfileBean}" />

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
              errorClass="errorMessage" warnClass="warnMessage"
              infoClass="infoMessage" fatalClass="fatalMessage"
              showSummary="true" />
  
  <t:div styleClass="userProfilePanel">

    <t:div styleClass="configPanel">
      <t:div styleClass="title">
        <sf:outputText value="#{miscBundle.settings}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </t:div>
      <t:div styleClass="inputRow">
        <t:div styleClass="leftColumn">
          <sf:outputText value="#{miscBundle.defaultLanguage}:"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </t:div>
        <t:div styleClass="rightColumn">
          <t:selectOneMenu value="#{userProfileBean.defaultLanguage}" styleClass="selectBox">
            <f:selectItems value="#{userProfileBean.defaultLanguageList}" />
          </t:selectOneMenu>
        </t:div>
      </t:div>
      <t:div styleClass="inputRow">
        <t:div styleClass="leftColumn">
          <sf:outputText value="#{miscBundle.defaultTheme}:"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </t:div>
        <t:div styleClass="rightColumn">
          <t:selectOneMenu value="#{userProfileBean.defaultTheme}" styleClass="selectBox">
            <f:selectItems value="#{userProfileBean.defaultThemeList}" />
          </t:selectOneMenu>
        </t:div>
      </t:div>
      <t:div styleClass="inputRow">
        <t:div styleClass="leftColumn">
          <sf:outputText value="#{miscBundle.recentPagesSize}:"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </t:div>
        <t:div styleClass="rightColumn">
          <h:inputText value="#{userProfileBean.recentPagesSize}" size="2"/>
        </t:div>
      </t:div>
    </t:div>

    <t:div styleClass="actionsPanel">
      <sf:commandButton value="#{webBundle.buttonSave}"
        action="#{userProfileBean.store}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </t:div>

  </t:div>

</jsp:root>
