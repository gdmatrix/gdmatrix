<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf ="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />

  <t:dataList value="#{objectActionsBean.objectActions}" var="action"
    layout="unorderedList" itemStyleClass="objectAction"
    styleClass="objectActions">
    <h:graphicImage value="/common/obj/images/action.gif" />
    <h:panelGroup rendered="#{action.expression != null}">
      <h:graphicImage value="#{action.image}" alt="" title=""
        rendered="#{action.image != null}" />
      <t:commandLink action="#{objectActionsBean.executeAction}" onclick="showOverlay()">
        <h:outputText value="#{action.description}" />
      </t:commandLink>
    </h:panelGroup>
    <h:panelGroup rendered="#{action.url != null}">
      <h:graphicImage value="#{action.image}" alt="" title="" 
        rendered="#{action.image != null}" />
      <h:outputLink value="#{action.url}" target="#{action.target}">
        <h:outputText value="#{action.description}" />
      </h:outputLink>
    </h:panelGroup>
  </t:dataList>

</jsp:root>

