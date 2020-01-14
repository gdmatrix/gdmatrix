<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:div styleClass="pageHeader"
    rendered="#{userSessionBean.selectedMenuItem.properties.pageHeaderRendered == 'true' or
      userSessionBean.selectedMenuItem.properties.pageHeaderRendered == null}">
    <t:div styleClass="topPanel">
      <h:panelGroup>
        <sf:clock styleClass="clock" />
        <sf:languageSelector styleClass="langSel" />
      </h:panelGroup>
    </t:div>
    <t:div styleClass="logoPanel"
      rendered="#{userSessionBean.selectedMenuItem.properties.logoUrl != null}">
      <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.linkUrl == null ?
        '#' : userSessionBean.selectedMenuItem.properties.linkUrl}">
        <t:graphicImage url="#{userSessionBean.selectedMenuItem.properties.logoUrl}"
          title="#{userSessionBean.selectedMenuItem.properties.logoTitle}"
          alt="#{userSessionBean.selectedMenuItem.properties.logoTitle}" />
      </h:outputLink>
    </t:div>
    <t:div styleClass="loginPanel">
      <h:panelGroup rendered="#{userSessionBean.anonymousUser}">
        <h:outputText value="#{webBundle.outputUsername}:"
          style="margin-right:4px" styleClass="loginText" />
        <h:inputText binding="#{loginBean.usernameInputText}"
                     tabindex="1" size="10" immediate="true"
                     onkeypress="login(event)"
                     styleClass="loginBox" />
        <h:outputText value="#{webBundle.outputPassword}:"
                      style="margin-left:4px; margin-right:4px"
                      styleClass="loginText" />
        <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                       tabindex="2" size="10" immediate="true"
                       onkeypress="login(event)"
                       styleClass="loginBox" />
        <sf:secureCommandLink styleClass="loginCommand"
          action="#{loginBean.login}" function="login" immediate="true"
          port="#{applicationBean.serverSecurePort}" scheme="https">
          <h:outputText value="#{webBundle.buttonSignin}" />
        </sf:secureCommandLink>
      </h:panelGroup>

      <h:panelGroup rendered="#{not userSessionBean.anonymousUser}">
        <h:outputText value="#{webBundle.outputUsername}:"
          styleClass="loginText" />
        <h:outputText value="#{userSessionBean.displayName}"
          styleClass="displayNameText" style="margin-left:4px" />
      </h:panelGroup>

      <h:outputText value="#{loginBean.loginMessage}"
        rendered="#{loginBean.loginMessage != null}"
        styleClass="loginMessage" />

      <h:commandLink action="#{loginBean.logout}"
        rendered="#{not userSessionBean.anonymousUser}"
        styleClass="loginCommand" immediate="true">
        <h:outputText value="#{webBundle.buttonSignout}" />
      </h:commandLink>
    </t:div>

    <sf:navigationPath id="navPath"
                     var="item"
                     value="main"
                     baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                     styleClass="navPath"
                     rendered="#{userSessionBean.selectedMenuItem.properties.navPathRendered != 'false'}">
      <f:facet name="menuitem">
        <sf:outputText value="#{item.label}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </f:facet>
    </sf:navigationPath>
  </t:div>
  
  <h:panelGroup styleClass="pageBody">
    <jsp:include page="${requestScope['_body']}"/>
  </h:panelGroup>

 </jsp:root>
