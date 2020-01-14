<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf">
          
  <f:loadBundle basename="org.santfeliu.security.web.resources.SecurityBundle" var="securityBundle"/>
  
  <h:panelGrid columns="1" headerClass="header" styleClass="form">
    <f:facet name="header">
      <h:outputText value="#{securityBundle.headerChangePassword}" />
    </f:facet>
    <sf:browser binding="#{passwordBean.browserOk}"
      port="#{applicationBean.defaultPort}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}" />
  </h:panelGrid>
</jsp:root>
