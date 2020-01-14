<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:div styleClass="centralSheet">
    <t:div styleClass="topbar">
      <h:panelGroup styleClass="languagePanel">
        <h:outputText value="#{webBundle.language}:" />
        <sf:languageSelector locales="#{userSessionBean.supportedLocales}"
          styleClass="languageSelector" />
      </h:panelGroup>

      <h:panelGroup styleClass="loginPanel" rendered="#{userSessionBean.anonymousUser}">
        <h:outputText value="#{webBundle.outputUsername}:"
          styleClass="username" />
        <h:inputText binding="#{loginBean.usernameInputText}"
                     tabindex="1" size="10" immediate="true"
                     onkeypress="login(event)"
                     styleClass="loginBox" />
        <h:outputText value="#{webBundle.outputPassword}:"
                      styleClass="password" />
        <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                       tabindex="2" size="10" immediate="true"
                       onkeypress="login(event)"
                       styleClass="loginBox" />
        <sf:secureCommandLink id="loginbutton"
          action="#{loginBean.login}" function="login" immediate="true"
          port="#{applicationBean.serverSecurePort}" scheme="https"
          styleClass="loginButton">
          <h:outputText value="#{webBundle.buttonSignin}" />
        </sf:secureCommandLink>
      </h:panelGroup>

      <h:panelGroup rendered="#{!userSessionBean.anonymousUser}"
                   styleClass="loginPanel">
        <h:outputText value="#{webBundle.outputUsername}:"
                      styleClass="username" />
        <h:outputText value="#{userSessionBean.displayName}"
                      styleClass="displayName"/>
        <h:commandLink action="#{loginBean.logout}" 
          styleClass="loginButton" immediate="true">
          <h:outputText value="#{webBundle.buttonSignout}" />
        </h:commandLink>
        <h:commandLink action="password" styleClass="loginButton">
          <h:outputText value="#{webBundle.outputChangePassword}" />
        </h:commandLink>
      </h:panelGroup>
    </t:div>

    <t:div styleClass="headerPanel">
      <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.headerDocId}"
        rendered="#{userSessionBean.selectedMenuItem.properties.headerDocId != null}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </t:div>

    <t:panelGrid styleClass="centralPanel" columns="2" columnClasses="cpcol1,cpcol2">
      <sf:treeMenu id="leftMenu" var="item" styleClass="menuPanel"
        baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
        expandDepth="2" expandSelected="false">
        <f:facet name="data">
          <h:outputLink value="#{item.actionURL}"
            onclick="#{item.onclick}" target="#{item.target}"
            styleClass="#{item.selectionContained ? 'selected' : ''}"
            rendered="#{item.rendered}">
            <sf:outputText value="#{item.label}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </h:outputLink>
        </f:facet>
      </sf:treeMenu>

      <h:panelGroup styleClass="contentPanel">
          <jsp:include page="${requestScope['_body']}"/>
      </h:panelGroup>
    </t:panelGrid>

    <t:div styleClass="footerPanel">
      <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.footerDocId}"
        rendered="#{userSessionBean.selectedMenuItem.properties.footerDocId != null}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </t:div>
  </t:div>
  
</jsp:root>
