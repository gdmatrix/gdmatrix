<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:t="http://myfaces.apache.org/tomahawk">

  <f:loadBundle basename="org.santfeliu.security.web.resources.SecurityBundle" var="securityBundle"/>
  
  <t:div styleClass="register form">
    
    <t:div styleClass="header">
      <h:outputText value="#{securityBundle.headerRegistre}" />
    </t:div>
        
    <h:panelGroup>
      <sf:browser binding="#{registerBean.browser}"
        port="#{applicationBean.defaultPort}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </h:panelGroup>
    
    <t:div>
      <t:div styleClass="column1">
        <h:outputLabel for="username" value="#{securityBundle.outputUserName}*" />
      </t:div>
      <t:div styleClass="column2">
        <h:inputText id="username" value="#{registerBean.username}" maxlength="20" styleClass="inputText"/>
      </t:div>

      <t:div styleClass="column1">
        <h:outputLabel for="displayName" value="#{securityBundle.outputFullName}" />
      </t:div> 
      <t:div styleClass="column2">      
        <h:inputText id="displayName" value="#{registerBean.displayName}" maxlength="20" styleClass="inputText"/>
      </t:div>
      
      <t:div styleClass="column1">     
        <h:outputLabel for="password" value="#{securityBundle.outputPassword}*" />
      </t:div>    
      <t:div styleClass="column2">      
        <h:inputSecret id="password" value="#{registerBean.password1}" styleClass="inputSecret"/>
      </t:div>
      
      <t:div styleClass="column1">      
        <h:outputLabel for="passwordTwice" value="#{securityBundle.outputPasswordTwice}*" />
      </t:div>  
      <t:div styleClass="column2"> 
        <h:inputSecret id="passwordTwice" value="#{registerBean.password2}" styleClass="inputSecret"/>
      </t:div> 
    </t:div>
    
    <t:div styleClass="footer">
      <h:panelGrid columns="1">
        <h:panelGroup>
          <h:commandButton value="#{webBundle.buttonSave}" styleClass="button"
             action="#{registerBean.register}" style="margin-right:5.0px;"/>
        </h:panelGroup>
        <h:messages rendered="#{userSessionBean.facesMessagesQueued and registerBean.showMessages}" 
                    showSummary="true"                     
                    infoClass="infoMessage"
                    errorClass="errorMessage" 
                    fatalClass="fatalClass" />
      </h:panelGrid>
    </t:div>
  </t:div>
</jsp:root>
