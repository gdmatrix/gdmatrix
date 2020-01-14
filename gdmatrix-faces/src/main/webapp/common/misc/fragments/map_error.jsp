<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
                var="objectBundle"/>
  <f:loadBundle basename="org.santfeliu.misc.mapviewer.web.resources.MapViewerBundle"
                var="mapViewerBundle"/>

  <t:panelGroup id="mapViewer" forceId="true">

    <t:div id="subHeader" forceId="true">
      <t:div id="screenTitle" forceId="true">
        <t:outputText value="ERROR" />
      </t:div>
      <t:div id="buttonsBar" forceId="true">
        <t:commandLink value="#{mapViewerBundle.catalogue}"
          action="#{mapViewerBean.showCatalogue}" styleClass="barViewButton"
          rendered="#{mapViewerBean.catalogueVisible}" />
      </t:div>
    </t:div>

    <t:div id="screenBody" forceId="true">
      <t:div id="map" forceId="true">
        <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
          showSummary="true"
          infoClass="infoMessage"
          warnClass="warnMessage"
          errorClass="errorMessage"
          fatalClass="fatalMessage" />
      </t:div>
    </t:div>
  </t:panelGroup>

</jsp:root>

