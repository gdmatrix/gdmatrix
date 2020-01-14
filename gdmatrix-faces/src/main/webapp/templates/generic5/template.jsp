<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  <h:panelGrid columns="2" cellspacing="0" cellpadding="0" width="100%"
    styleClass="header" columnClasses="col1,col2">

    <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.title}"
      styleClass="title"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}" />
    
    <h:panelGroup rendered="#{userSessionBean.anonymousUser}">
      <h:outputText value="#{webBundle.outputUsername}:" style="margin-right:4px"
                    styleClass="loginText" />
      <h:inputText binding="#{loginBean.usernameInputText}" tabindex="1"
                   size="10" immediate="true" onkeypress="login(event)"
                   styleClass="loginBox"/>
      <h:outputText value="#{webBundle.outputPassword}:"
                    style="margin-left:4px; margin-right:4px"
                    styleClass="loginText" />
      <h:inputSecret binding="#{loginBean.passwordInputSecret}" tabindex="2"
                     size="10" immediate="true" onkeypress="login(event)"
                     styleClass="loginBox"/>
      <sf:secureCommandLink id="loginbutton" style="margin-left:4px"
                            action="#{loginBean.login}" function="login"
                            immediate="true" styleClass="loginButton"
                            port="#{applicationBean.serverSecurePort}"
                            scheme="https">
          <h:outputText value="#{webBundle.buttonSignin}" />
      </sf:secureCommandLink>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{not userSessionBean.anonymousUser}">
      <h:outputText value="#{webBundle.outputUsername}:" styleClass="loginText" />
      <h:outputText value="#{userSessionBean.displayName} "
                    styleClass="displayNameText" style="margin-left:4px"/>
      <h:commandLink action="#{loginBean.logout}" styleClass="loginButton" 
        rendered="#{not userSessionBean.anonymousUser}" immediate="true">
        <h:outputText value="#{webBundle.buttonSignout}" />
      </h:commandLink>
    </h:panelGroup>
  </h:panelGrid>
  
  <t:div>
    <jsp:include page="${requestScope['_body']}"/>
  </t:div>
</jsp:root>
