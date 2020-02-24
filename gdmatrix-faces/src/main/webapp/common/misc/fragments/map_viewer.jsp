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

  <t:saveState value="#{mapBean.map}" />

  <sf:heading level="1" styleClass="hidden">
    <sf:outputText value="#{mapViewerBundle.mapUpper}:" />
    <sf:outputText value="#{mapBean.map.title}"
      translator="#{userSessionBean.translator}"
      translationGroup="map:#{mapBean.map.name}" />      
  </sf:heading>
  
  <t:panelGroup id="mapViewer" forceId="true">
    
    <t:div id="subHeader" forceId="true">
      <t:div id="screenTitle" forceId="true">
        <sf:outputText value="#{mapBean.map.title}"
          translator="#{userSessionBean.translator}"
          translationGroup="map:#{mapBean.map.name}" />
      </t:div>
      <t:div id="buttonsBar" forceId="true">
        <t:commandLink value="#{mapViewerBundle.refreshForms}"
          action="#{mapViewerBean.refreshForms}" styleClass="barButton"
          rendered="#{mapViewerBean.editionEnabled}" />

        <t:commandLink value="#{mapViewerBundle.editMap}"
          action="#{mapEditorBean.editMap}" styleClass="barViewButton"
          rendered="#{mapViewerBean.editionEnabled}" />
        <t:commandLink value="#{mapViewerBundle.editStyles}"
          action="#{sldEditorBean.show}" styleClass="barViewButton"
          rendered="#{mapViewerBean.editionEnabled and sldEditorBean.sldName != null}" />
        <t:commandLink value="#{mapViewerBundle.catalogue}"
          action="#{mapViewerBean.showCatalogue}" styleClass="barViewButton"
          rendered="#{mapViewerBean.catalogueVisible}" />
      </t:div>
    </t:div>

    <t:div id="screenBody" forceId="true">
      <f:verbatim><h2 class="hidden">${mapViewerBundle.searchPanel}</h2></f:verbatim>
      <h:outputText value="#{mapViewerBundle.searchPanel}" styleClass="element-invisible"/>
      <t:div id="leftPanel" forceId="true">
      </t:div>
      
      <sf:div id="map" forceId="true" ariaHidden="true">
        <f:verbatim><h2 class="hidden">${mapViewerBundle.mapUpper}</h2></f:verbatim>       
        <h:outputLink id="toggleLeftPanel" styleClass="leftPanelButton" onclick="toggleLeftPanel(); return false"
           title="#{mapViewerBundle.searchPanelLabel}">
          <t:outputText id="toggleLeftPanelText" forceId="true"
            value="#{mapViewerBundle.search}"/>
          <t:graphicImage id="toggleLeftPanelButton" forceId="true"
            alt=""
            url="/plugins/mapviewer/img/left-panel-maximize.png" />
        </h:outputLink>
        <h:outputLink id="toggleRightPanel" styleClass="rightPanelButton" onclick="toggleRightPanel(); return false" 
           title="#{mapViewerBundle.infoPanelLabel}">
          <t:graphicImage id="toggleRightPanelButton" forceId="true"
            alt=""
            url="/plugins/mapviewer/img/right-panel-maximize.png" />
          <t:outputText id="toggleRightPanelText" forceId="true"
            value="#{mapViewerBundle.info}"/>
        </h:outputLink>
        <t:div id="wrapper" forceId="true">
          <t:div id="location" forceId="true" />
          <t:outputText id="srs" forceId="true" value="#{mapBean.map.srs}" />
          <t:div id="scale" forceId="true"
            rendered="#{mapViewerBean.editionEnabled}"/>
        </t:div>
      </sf:div>

      <t:div id="rightPanel" forceId="true">
        <f:verbatim><h2 class="hidden">${mapViewerBundle.info}</h2></f:verbatim>
        <t:div id="info" forceId="true">
          <t:div id="infoContent" styleClass="mapDescription" forceId="true">
            <sf:outputText value="#{mapBean.description}"
               escape="#{not mapBean.map.descriptionFormatted}"
               translator="#{userSessionBean.translator}"
               translationGroup="map:#{mapBean.map.name}" />
          </t:div>
          <h:outputLink value="#" onclick="infoDividerUp()" 
                        styleClass="infoDividerUpButton"
                        title="#{mapViewerBundle.upPanelLabel}">
            <h:graphicImage alt="#{mapViewerBundle.upPanelLabel}"
              url="/plugins/mapviewer/img/north-mini.png" />
          </h:outputLink>
        </t:div>
        <f:verbatim><h2 class="hidden">${mapViewerBundle.legend}</h2></f:verbatim>        
        <t:div id="legend" forceId="true">
          <h:outputLink value="#" styleClass="infoDividerDownButton"
            onclick="infoDividerDown()" title="#{mapViewerBundle.downPanelLabel}">
            <h:graphicImage alt="#{mapViewerBundle.downPanelLabel}"
              url="/plugins/mapviewer/img/south-mini.png" />              
          </h:outputLink>
          <t:div id="legendContent" forceId="true">
            <h:outputText value="#{mapViewerBundle.legend}:"
              styleClass="legendHeader" />
            <h:outputText value="#{mapViewerBean.legend}" escape="false" />
          </t:div>
        </t:div>
      </t:div>
    </t:div>

  </t:panelGroup>
  
  <h:outputText escape="false" value="#{mapViewerBean.scripts}" />

</jsp:root>

