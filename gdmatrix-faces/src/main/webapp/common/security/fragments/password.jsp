<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf">
          
  <f:loadBundle basename="org.santfeliu.security.web.resources.SecurityBundle" var="securityBundle"/>

  <h:panelGrid columns="1" styleClass="form"
    headerClass="header" footerClass="footer"
    rendered="#{not userSessionBean.anonymousUser and not userSessionBean.certificateUser}">
    <f:facet name="header">
      <h:outputText value="#{securityBundle.headerChangePassword}" />
    </f:facet>
    <h:panelGrid columns="1">
        <sf:browser binding="#{passwordBean.browser}"
          port="#{applicationBean.defaultPort}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
    </h:panelGrid>
    <h:panelGrid columns="2" >
      <h:outputText value="#{securityBundle.outputOldPassword}" />
      <h:inputSecret value="#{passwordBean.oldPassword}" 
                     styleClass="inputSecret" />
      <h:outputText value="#{securityBundle.outputNewPassword}" />
      <h:inputSecret value="#{passwordBean.newPassword1}" 
                     styleClass="inputSecret" />
      <h:outputText value="#{securityBundle.outputNewPasswordTwice}" />
      <h:inputSecret value="#{passwordBean.newPassword2}" 
                     styleClass="inputSecret" />
    </h:panelGrid>
    <f:facet name="footer">
      <h:panelGrid columns="1">
        <h:panelGroup>
          <h:commandButton value="#{webBundle.buttonSave}" 
                           action="#{passwordBean.changePassword}" 
                           styleClass="button"
                           style="margin-right:5.0px;"/>
        </h:panelGroup>
        <h:messages rendered="#{userSessionBean.facesMessagesQueued and passwordBean.showMessages}" 
                    showSummary="true"
                    infoClass="infoMessage"
                    errorClass="errorMessage" 
                    fatalClass="fatalMessage" />
      </h:panelGrid>
    </f:facet>
  </h:panelGrid>
</jsp:root>
