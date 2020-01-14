<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.security.web.resources.SecurityBundle"
    var="securityBundle" />

  <t:div>
    <h:outputText value="#{securityBundle.role_role}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{roleMainBean.role.roleId}"
                 styleClass="inputBox"
                 readonly="#{!roleBean.new}" />
  </t:div>
  <t:div>
    <h:outputText value="#{securityBundle.role_name}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{roleMainBean.role.name}"
                   styleClass="inputBox" style="width:60%" />
  </t:div>

  <t:div>
    <h:outputText value="#{securityBundle.role_type}:"
      style="width:25%" styleClass="textBox"/>
    <t:selectOneMenu value="#{roleMainBean.role.roleTypeId}"
      styleClass="selectBox">
      <f:selectItem itemLabel=" " itemValue="" />
      <f:selectItems value="#{roleMainBean.allTypeItems}" />
    </t:selectOneMenu>
<!--
    <h:commandButton value="#{objectBundle.search}"
      image="#{userSessionBean.icons.search}"
      alt="#{objectBundle.search}" title="#{objectBundle.search}"
      styleClass="searchButton"
      action="#{roleMainBean.searchType}"/>
-->
    <h:commandButton action="#{roleMainBean.showType}"
      value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"
      rendered="#{roleMainBean.renderShowTypeButton}" />
  </t:div>

  <t:div>
    <h:outputText value="#{securityBundle.role_description}:"
                  styleClass="textBox" style="width:25%;vertical-align:top;" />
    <h:inputTextarea rows="5"
                     value="#{roleMainBean.role.description}"
                     styleClass="inputBox" style="width:60%" />
  </t:div>
                     
  <t:div rendered="#{roleMainBean.role.creationUserId != null}">
    <h:outputText value="#{securityBundle.role_creationUserId}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{roleMainBean.role.creationUserId}"
      styleClass="outputBox" style="width:22%">
    </h:outputText>
  </t:div>

  <t:div rendered="#{roleMainBean.role.creationDateTime != null}">
    <h:outputText value="#{securityBundle.role_creationDateTime}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{roleMainBean.role.creationDateTime}"
      styleClass="outputBox" style="width:22%">
      <f:converter converterId="DateTimeConverter" />
      <f:attribute name="userFormat" value="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
  </t:div>

  <t:div rendered="#{roleMainBean.role.changeUserId != null}">
    <h:outputText value="#{securityBundle.role_changeUserId}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{roleMainBean.role.changeUserId}"
      styleClass="outputBox" style="width:22%">
    </h:outputText>
  </t:div>

  <t:div rendered="#{roleMainBean.role.changeDateTime != null}">
    <h:outputText value="#{securityBundle.role_changeDateTime}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{roleMainBean.role.changeDateTime}"
      styleClass="outputBox" style="width:22%">
      <f:converter converterId="DateTimeConverter" />
      <f:attribute name="userFormat" value="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
  </t:div>

</jsp:root>
