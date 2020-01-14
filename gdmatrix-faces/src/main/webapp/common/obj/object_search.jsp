<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf ="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />

  <t:buffer into="#{buffer}">
    <jsp:include page="/common/obj/page_history.jsp" />

    <t:div styleClass="objectSearch">
      <t:div styleClass="header"
        rendered="#{userSessionBean.selectedMenuItem.properties.showHeader == 'true'}">
        <h:panelGroup style="width:80%;display:inline-block;">

          <h:graphicImage rendered="#{controllerBean.objectBean != null and controllerBean.objectBean.renderObjectTypeIcon}"
            value="#{controllerBean.objectBean.rootObjectTypeIconPath}"
            style="border:none" />

          <sf:outputText value="#{controllerBean.searchBean.title}:"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}"
            style="text-align:left" />
        </h:panelGroup>
        <h:panelGroup style="width:20%;display:inline-block;text-align:right">
          <h:commandButton action="#{controllerBean.close}"
            value="#{objectBundle.close}" immediate="true"
            styleClass="closeButton"
            image="#{userSessionBean.icons.close}"
            alt="#{objectBundle.close}" title="#{objectBundle.close}" />
        </h:panelGroup>
      </t:div>
      <jsp:include page="${requestScope['_filterlist']}" />
    </t:div>
  </t:buffer>

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    errorClass="errorMessage" warnClass="warnMessage_line"
    infoClass="infoMessage_line"/>

  <h:outputText value="#{buffer}" escape="false"/>

</jsp:root>
