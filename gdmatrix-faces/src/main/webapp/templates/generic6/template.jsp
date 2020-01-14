<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:panelGrid id="mainLayout" columns="2"
               styleClass="main" 
               rowClasses="prow1"
               columnClasses="pcolumn1,pcolumn2"
               cellspacing="0"
               cellpadding="0"
               headerClass="pantone"
               footerClass="footer" 
               width="100%">

    <f:facet name="header">
      <h:graphicImage url="#{userSessionBean.selectedMenuItem.properties.pantone}" 
        width="100%" />
    </f:facet>

    <h:panelGroup>
      <jsp:include page="${requestScope['_body']}"/>
    </h:panelGroup>

    <t:div styleClass="rightMenu">
      <h:outputText value="#{webBundle.language}:" />
      <sf:languageSelector locales="#{userSessionBean.supportedLocales}" />

      <sf:navigationMenu id="vmenu"
                         value="main" 
                         var="item"
                         orientation="vertical"                         
                         styleClass="vmenu"
                         selectedStyleClass="vmenu_selected" 
                         unselectedStyleClass="vmenu_unselected">
          <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
            style="vertical-align:middle"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
      </sf:navigationMenu>

      <h:outputText value="#{webBundle.themes}:"
        rendered="#{userSessionBean.themeSelectionEnabled}" />
      <sf:commandMenu value="#{userSessionBean.theme}"
        rendered="#{userSessionBean.themeSelectionEnabled}">
        <f:selectItems value="#{userSessionBean.themes}" />
      </sf:commandMenu>     

      <h:panelGrid columns="1" summary="" cellpadding="2"
                   rendered="#{userSessionBean.anonymousUser}"
                   styleClass="loginContainer">
        <h:outputText value="#{webBundle.outputUsername}:" />
        <h:inputText binding="#{loginBean.usernameInputText}"
                     styleClass="loginBox" immediate="true"
                     onkeypress="login(event)"
                     tabindex="1" size="10" />
        <h:outputText value="#{webBundle.outputPassword}:" />
        <h:inputSecret binding="#{loginBean.passwordInputSecret}" 
                       styleClass="loginBox" immediate="true"
                       size="10" tabindex="2" 
                       onkeypress="login(event)" />
        <sf:secureCommandLink action="#{loginBean.login}" 
            styleClass="loginButton" function="login" immediate="true"
            port="#{applicationBean.serverSecurePort}" scheme="https">
          <h:outputText value="#{webBundle.buttonSignin}" />
        </sf:secureCommandLink>

        <h:outputText value="#{loginBean.loginMessage}"
                      rendered="#{loginBean.loginMessage != null}"
                      styleClass="errorMessage" />
      </h:panelGrid>

      <h:panelGrid rendered="#{not userSessionBean.anonymousUser}"
                   cellpadding="2" columns="1" 
                   styleClass="loginContainer" summary="">
        <h:outputText value="#{webBundle.outputUsername}:"
                      style="text-align:left" />
        <h:outputText value="#{userSessionBean.displayName}"
                      styleClass="displayName"/>

        <h:commandLink action="#{loginBean.logout}" 
          styleClass="loginButton" immediate="true">
          <h:outputText value="#{webBundle.buttonSignout}" />
        </h:commandLink>
      </h:panelGrid>

    </t:div>
    
  </h:panelGrid>
</jsp:root>
