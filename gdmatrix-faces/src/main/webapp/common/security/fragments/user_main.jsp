<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.security.web.resources.SecurityBundle"
    var="securityBundle" />

  <t:div>
    <h:outputText value="#{securityBundle.user_user}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{userMainBean.user.userId}"
                 styleClass="inputBox"
                 readonly="#{!userBean.new}" />
  </t:div>

  <t:div>
    <h:outputText value="#{securityBundle.user_displayName}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{userMainBean.user.displayName}"
                 styleClass="inputBox" style="width:40%" />
  </t:div>

  <t:div>
    <h:outputText value="#{securityBundle.user_person}:"
                  styleClass="textBox" style="width:25%" />
    <h:panelGroup>
      <t:selectOneMenu value="#{userMainBean.user.personId}"
                       styleClass="selectBox" style="width:50%">
        <f:selectItems value="#{userMainBean.personSelectItems}" />
      </t:selectOneMenu>
      <h:commandButton value="#{objectBundle.search}"
        image="#{userSessionBean.icons.search}"
        alt="#{objectBundle.search}" title="#{objectBundle.search}"
        action="#{userMainBean.searchPerson}"
        styleClass="searchButton" />
      <h:commandButton action="#{userMainBean.showPerson}"
        value="#{objectBundle.show}"
        image="#{userSessionBean.icons.show}"
        alt="#{objectBundle.show}" title="#{objectBundle.show}"
        styleClass="showButton"
        rendered="#{userMainBean.renderShowPersonButton}" />
    </h:panelGroup>
  </t:div>

  <t:div>
    <h:outputText value="#{securityBundle.user_password}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputSecret value="#{userMainBean.passwordInput}" redisplay="true"
                   styleClass="inputBox" />
  </t:div>

  <t:div>
    <h:outputText value="#{securityBundle.user_locked}:"
                  styleClass="textBox" style="width:25%" />
    <h:selectBooleanCheckbox value="#{userMainBean.locked}" />
  </t:div>

  <t:div rendered="#{userMainBean.user.creationUserId != null}">
    <h:outputText value="#{securityBundle.user_creationUserId}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{userMainBean.user.creationUserId}"
      styleClass="outputBox" style="width:22%">
    </h:outputText>
  </t:div>

  <t:div rendered="#{userMainBean.user.creationDateTime != null}">
    <h:outputText value="#{securityBundle.user_creationDateTime}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{userMainBean.user.creationDateTime}"
      styleClass="outputBox" style="width:22%">
      <f:converter converterId="DateTimeConverter" />
      <f:attribute name="userFormat" value="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
  </t:div>

  <t:div rendered="#{userMainBean.user.changeUserId != null}">
    <h:outputText value="#{securityBundle.user_changeUserId}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{userMainBean.user.changeUserId}"
      styleClass="outputBox" style="width:22%">
    </h:outputText>
  </t:div>

  <t:div rendered="#{userMainBean.user.changeDateTime != null}">
    <h:outputText value="#{securityBundle.user_changeDateTime}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{userMainBean.user.changeDateTime}"
      styleClass="outputBox" style="width:22%">
      <f:converter converterId="DateTimeConverter" />
      <f:attribute name="userFormat" value="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
  </t:div>

</jsp:root>
