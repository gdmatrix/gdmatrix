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

  <t:stylesheet path="/plugins/codemirror/codemirror.css" />
  
  <t:saveState value="#{mapBean.map}" />
  
  <t:panelGroup id="sldEditor" forceId="true">

    <t:div id="subHeader" forceId="true">
      <t:div id="screenTitle" forceId="true">
        <h:outputText value="#{mapViewerBundle.sldEditor}: " />
        <h:outputText value="#{sldEditorBean.sldName}" styleClass="sldName" />
      </t:div>
      <t:div id="buttonsBar" forceId="true">
        <t:commandLink value="#{mapViewerBundle.saveSLD}"
          action="#{sldEditorBean.saveSLD}"
          styleClass="barButton" />
        <t:commandLink value="#{mapViewerBundle.reloadSLD}"
          action="#{sldEditorBean.reloadSLD}" immediate="true"
          onclick="if (!confirm('#{mapViewerBundle.discard_changes}')) return false"
          styleClass="barButton" />

        <t:commandLink value="#{mapViewerBundle.showMap}"
          action="#{mapEditorBean.showMap}" styleClass="barViewButton"
          rendered="#{mapBean.map.complete}" />
        <t:commandLink value="#{mapViewerBundle.editMap}"
          action="#{sldEditorBean.editMap}" styleClass="barViewButton"
          rendered="#{mapViewerBean.editionEnabled}" />
        <t:commandLink value="#{mapViewerBundle.catalogue}"
          action="#{mapViewerBean.showCatalogue}" styleClass="barViewButton"
          onclick="if (!confirm('#{mapViewerBundle.discard_changes}')) return false"
          rendered="#{mapViewerBean.catalogueVisible}" />
      </t:div>
    </t:div>

    <t:div id="screenBody" forceId="true">
    
      <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
        showSummary="true"
        infoClass="infoMessage"
        warnClass="warnMessage"
        errorClass="errorMessage"
        fatalClass="fatalMessage" />

      <t:div styleClass="tabs">
        <h:outputText value="Editor:" styleClass="header" />
        <h:commandButton value="Basic" action="#{sldEditorBean.exitAdvancedMode}"
          disabled="#{not sldEditorBean.advancedMode}" styleClass="button" />
        <h:commandButton value="Advanced" action="#{sldEditorBean.enterAdvancedMode}"
          disabled="#{sldEditorBean.advancedMode}" styleClass="button" />
      </t:div>

      <t:div rendered="#{not sldEditorBean.advancedMode}">
        <h:outputText value="Named layers:" styleClass="header" />
        <h:outputText value="#{sldEditorBean.namedLayerCount}" styleClass="layerCount"/>
        <h:commandButton value="Show all layers"
          action="#{sldEditorBean.showAllNamedLayers}" styleClass="smallButton" />
        
        <t:dataList value="#{sldEditorBean.sld.namedLayers}" var="namedLayer" layout="unorderedList"
          itemStyleClass="#{sldEditorBean.namedLayerVisible ? 'visible' : 'hidden'} #{sldEditorBean.namedLayerExpanded ? 'expanded' : 'collapsed'}">
          <t:div>
            <h:commandButton image="/common/misc/images/expand.png"
              action="#{sldEditorBean.expandNamedLayer}" styleClass="ecButton"
              rendered="#{not sldEditorBean.namedLayerExpanded}" />
            <h:commandButton image="/common/misc/images/collapse.png" styleClass="ecButton"
              action="#{sldEditorBean.collapseNamedLayer}"
              rendered="#{sldEditorBean.namedLayerExpanded}" />
            <h:outputText value="NamedLayer: " styleClass="header" />
            <t:inputText id="namedLayer" forceId="true"
              value="#{namedLayer.layerName}" styleClass="codeBox" style="width:200px" />
            <h:commandButton image="/common/misc/images/focus.png" title="Focus on this layer"
              action="#{sldEditorBean.focusNamedLayer}" styleClass="smallButton" />
            <h:commandButton image="/common/misc/images/remove.png" title="Remove named layer"
              action="#{namedLayer.remove}" styleClass="smallButton" />
          </t:div>

          <t:dataList value="#{namedLayer.userStyles}" var="userStyle" layout="unorderedList"
            itemStyleClass="#{sldEditorBean.userStyleExpanded ? 'expanded' : 'collapsed'}">
            <t:div>
              <h:commandButton image="/common/misc/images/expand.png"
                action="#{sldEditorBean.expandUserStyle}" styleClass="ecButton"
                rendered="#{not sldEditorBean.userStyleExpanded}" />
              <h:commandButton image="/common/misc/images/collapse.png" styleClass="ecButton"
                action="#{sldEditorBean.collapseUserStyle}"
                rendered="#{sldEditorBean.userStyleExpanded}" />
              <h:outputText value="UserStyle: " styleClass="header" />
              <h:inputText value="#{userStyle.styleName}" styleClass="codeBox" style="width:200px" />
              <h:commandButton image="/common/misc/images/remove.png" title="Remove user style"
                action="#{userStyle.remove}" styleClass="smallButton" />
            </t:div>
            <t:div rendered="#{sldEditorBean.userStyleExpanded}">
              <t:div>
                <h:outputText value="IsDefault:" style="vertical-align:middle" />
                <h:selectBooleanCheckbox value="#{userStyle.default}" style="vertical-align:middle" />
              </t:div>
            </t:div>

            <t:dataList value="#{userStyle.rules}" var="rule" layout="unorderedList"
              itemStyleClass="#{sldEditorBean.ruleExpanded ? 'expanded' : 'collapsed'}">
              <t:div>
                <h:commandButton image="/common/misc/images/expand.png"
                  action="#{sldEditorBean.expandRule}" styleClass="ecButton"
                  rendered="#{not sldEditorBean.ruleExpanded}" />
                <h:commandButton image="/common/misc/images/collapse.png" styleClass="ecButton"
                  action="#{sldEditorBean.collapseRule}"
                  rendered="#{sldEditorBean.ruleExpanded}" />
                <h:outputText value="Rule: " styleClass="header" />
                <h:inputText value="#{rule.title}" styleClass="inputBox" style="width:200px" />
                <h:commandButton image="/common/misc/images/remove.png" title="Remove rule"
                  action="#{rule.remove}" styleClass="smallButton" />
                <h:outputText value="#{sldEditorBean.ruleSummary}" styleClass="code" 
                  rendered="#{not sldEditorBean.ruleExpanded}" />
              </t:div>
              <t:div rendered="#{sldEditorBean.ruleExpanded}">
                <t:div>
                  <h:outputText value="MinScaleDenominator: " />
                  <h:inputText value="#{rule.minScaleDenominator}" styleClass="codeBox" style="width:60px" />
                </t:div>
                <t:div>
                  <h:outputText value="MaxScaleDenominator: " />
                  <h:inputText value="#{rule.maxScaleDenominator}" styleClass="codeBox" style="width:60px" />
                </t:div>
                <t:div>
                  <h:outputText value="Filter: " />
                  <h:inputTextarea value="#{rule.filterAsCql}" styleClass="codeBox" rows="2" style="display:block;width:100%"
                     onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                     onblur="hideCQLAssistant()" />
                </t:div>
              </t:div>

              <t:dataList value="#{rule.symbolizers}" var="symbolizer" layout="unorderedList"
                itemStyleClass="#{sldEditorBean.symbolizerExpanded ? 'expanded' : 'collapsed'}">
                <t:div rendered="#{symbolizer.symbolizerType == 'Point'}">
                  <h:commandButton image="/common/misc/images/expand.png"
                    action="#{sldEditorBean.expandSymbolizer}" styleClass="ecButton"
                    rendered="#{not sldEditorBean.symbolizerExpanded}" />
                  <h:commandButton image="/common/misc/images/collapse.png" styleClass="ecButton"
                    action="#{sldEditorBean.collapseSymbolizer}"
                    rendered="#{sldEditorBean.symbolizerExpanded}" />
                  <h:outputText value="PointSymbolizer" styleClass="symbolizer" />
                  <h:commandButton image="/common/misc/images/remove.png" title="Remove rule"
                    action="#{symbolizer.remove}" styleClass="smallButton" />
                  <h:outputText value="#{sldEditorBean.pointSymbolizerSummary}" styleClass="code" 
                    rendered="#{not sldEditorBean.symbolizerExpanded}" escape="false" />
                  <t:div rendered="#{sldEditorBean.symbolizerExpanded}">
                    <t:div>
                      <h:outputText value="Geometry: " />
                      <h:inputText value="#{symbolizer.geometryAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="ExternalGraphicURL: " />
                      <h:inputText value="#{symbolizer.graphic.externalGraphic.onlineResource}"
                        styleClass="codeBox" style="width:300px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="ExternalGraphicFormat: " />
                      <t:selectOneMenu value="#{symbolizer.graphic.externalGraphic.format}" styleClass="selectBox">
                        <f:selectItem itemLabel=" " itemValue="" />
                        <f:selectItem itemLabel="image/png" itemValue="image/png" />
                        <f:selectItem itemLabel="image/jpeg" itemValue="image/jpeg" />
                        <f:selectItem itemLabel="image/gif" itemValue="image/gif" />
                        <f:selectItem itemLabel="image/svg+xml" itemValue="image/svg+xml" />
                      </t:selectOneMenu>
                    </t:div>
                    <t:div>
                      <h:outputText value="MarkSymbol: " />
                      <t:selectOneMenu value="#{symbolizer.graphic.mark.wellKnownName}" styleClass="selectBox">
                        <f:selectItem itemLabel=" " itemValue="" />
                        <f:selectItem itemLabel="circle" itemValue="circle" />
                        <f:selectItem itemLabel="cross" itemValue="cross" />
                        <f:selectItem itemLabel="square" itemValue="square" />
                        <f:selectItem itemLabel="star" itemValue="star" />
                        <f:selectItem itemLabel="triangle" itemValue="triangle" />
                        <f:selectItem itemLabel="x" itemValue="x" />
                      </t:selectOneMenu>
                    </t:div>
                    <t:div>
                      <h:outputText value="MarkStrokeColor: " />
                      <h:inputText value="#{symbolizer.graphic.mark.stroke.strokeColor}" styleClass="color {hash:true, required:false}" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="MarkStrokeWidth: " />
                      <h:inputText value="#{symbolizer.graphic.mark.stroke.strokeWidth}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="MarkFillColor: " />
                      <h:inputText value="#{symbolizer.graphic.mark.fill.fillColor}" styleClass="color {hash:true, required:false}" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="Opacity: " />
                      <h:inputText value="#{symbolizer.graphic.opacityAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="Size: " />
                      <h:inputText value="#{symbolizer.graphic.sizeAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="Rotation: " />
                      <h:inputText value="#{symbolizer.graphic.rotationAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                  </t:div>
                </t:div>

                <t:div rendered="#{symbolizer.symbolizerType == 'Line'}">
                  <h:commandButton image="/common/misc/images/expand.png"
                    action="#{sldEditorBean.expandSymbolizer}" styleClass="ecButton"
                    rendered="#{not sldEditorBean.symbolizerExpanded}" />
                  <h:commandButton image="/common/misc/images/collapse.png" styleClass="ecButton"
                    action="#{sldEditorBean.collapseSymbolizer}"
                    rendered="#{sldEditorBean.symbolizerExpanded}" />
                  <h:outputText value="LineSymbolizer" styleClass="symbolizer" />
                  <h:commandButton image="/common/misc/images/remove.png" title="Remove symbolizer"
                     action="#{symbolizer.remove}" styleClass="smallButton" />
                  <h:outputText value="#{sldEditorBean.lineSymbolizerSummary}" styleClass="code"
                    rendered="#{not sldEditorBean.symbolizerExpanded}" escape="false" />
                  <t:div rendered="#{sldEditorBean.symbolizerExpanded}">
                    <t:div>
                      <h:outputText value="Geometry: " />
                      <h:inputText value="#{symbolizer.geometryAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeColor: " />
                      <h:inputText value="#{symbolizer.stroke.strokeColor}" styleClass="color {hash:true, required:false}" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeWidth: " />
                      <h:inputText value="#{symbolizer.stroke.strokeWidth}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeOpacity: " />
                      <h:inputText value="#{symbolizer.stroke.strokeOpacity}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeLineJoin: " />
                      <t:selectOneMenu value="#{symbolizer.stroke.strokeLineJoin}" styleClass="selectBox">
                        <f:selectItem itemLabel=" " itemValue="" />
                        <f:selectItem itemLabel="mitre" itemValue="mitre" />
                        <f:selectItem itemLabel="round" itemValue="round" />
                        <f:selectItem itemLabel="bevel" itemValue="bevel" />
                      </t:selectOneMenu>
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeLineCap: " />
                      <t:selectOneMenu value="#{symbolizer.stroke.strokeLineCap}" styleClass="selectBox">
                        <f:selectItem itemLabel=" " itemValue="" />
                        <f:selectItem itemLabel="butt" itemValue="butt" />
                        <f:selectItem itemLabel="round" itemValue="round" />
                        <f:selectItem itemLabel="square" itemValue="square" />
                      </t:selectOneMenu>
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeDashArray: " />
                      <h:inputText value="#{symbolizer.stroke.strokeDashArray}" styleClass="codeBox" style="width:100px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeDashOffset: " />
                      <h:inputText value="#{symbolizer.stroke.strokeDashOffset}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                  </t:div>
                </t:div>

                <t:div rendered="#{symbolizer.symbolizerType == 'Polygon'}">
                  <h:commandButton image="/common/misc/images/expand.png"
                    action="#{sldEditorBean.expandSymbolizer}" styleClass="ecButton"
                    rendered="#{not sldEditorBean.symbolizerExpanded}" />
                  <h:commandButton image="/common/misc/images/collapse.png" styleClass="ecButton"
                    action="#{sldEditorBean.collapseSymbolizer}"
                    rendered="#{sldEditorBean.symbolizerExpanded}" />
                  <h:outputText value="PolygonSymbolizer" styleClass="symbolizer" />
                  <h:commandButton image="/common/misc/images/remove.png" title="Remove symbolizer"
                     action="#{symbolizer.remove}" styleClass="smallButton" />
                  <h:outputText value="#{sldEditorBean.polygonSymbolizerSummary}" styleClass="code"
                    rendered="#{not sldEditorBean.symbolizerExpanded}" escape="false" />
                  <t:div rendered="#{sldEditorBean.symbolizerExpanded}">
                    <t:div>
                      <h:outputText value="Geometry: " />
                      <h:inputText value="#{symbolizer.geometryAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeColor: " />
                      <h:inputText value="#{symbolizer.stroke.strokeColor}" styleClass="color {hash:true, required:false}" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeWidth: " />
                      <h:inputText value="#{symbolizer.stroke.strokeWidth}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeOpacity: " />
                      <h:inputText value="#{symbolizer.stroke.strokeOpacity}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeLineJoin: " />
                      <t:selectOneMenu value="#{symbolizer.stroke.strokeLineJoin}" styleClass="selectBox">
                        <f:selectItem itemLabel=" " itemValue="" />
                        <f:selectItem itemLabel="mitre" itemValue="mitre" />
                        <f:selectItem itemLabel="round" itemValue="round" />
                        <f:selectItem itemLabel="bevel" itemValue="bevel" />
                      </t:selectOneMenu>
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeLineCap: " />
                      <t:selectOneMenu value="#{symbolizer.stroke.strokeLineCap}" styleClass="selectBox">
                        <f:selectItem itemLabel=" " itemValue="" />
                        <f:selectItem itemLabel="butt" itemValue="butt" />
                        <f:selectItem itemLabel="round" itemValue="round" />
                        <f:selectItem itemLabel="square" itemValue="square" />
                      </t:selectOneMenu>
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeDashArray: " />
                      <h:inputText value="#{symbolizer.stroke.strokeDashArray}" styleClass="codeBox" style="width:100px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="StrokeDashOffset: " />
                      <h:inputText value="#{symbolizer.stroke.strokeDashOffset}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="FillColor: " />
                      <h:inputText value="#{symbolizer.fill.fillColor}" styleClass="color {hash:true, required:false}" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="FillOpacity: " />
                      <h:inputText value="#{symbolizer.fill.fillOpacity}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                  </t:div>
                </t:div>

                <t:div rendered="#{symbolizer.symbolizerType == 'Text'}">
                  <h:commandButton image="/common/misc/images/expand.png"
                    action="#{sldEditorBean.expandSymbolizer}" styleClass="ecButton"
                    rendered="#{not sldEditorBean.symbolizerExpanded}" />
                  <h:commandButton image="/common/misc/images/collapse.png" styleClass="ecButton"
                    action="#{sldEditorBean.collapseSymbolizer}"
                    rendered="#{sldEditorBean.symbolizerExpanded}" />
                  <h:outputText value="TextSymbolizer" styleClass="symbolizer" />
                  <h:commandButton image="/common/misc/images/remove.png" title="Remove symbolizer"
                     action="#{symbolizer.remove}" styleClass="smallButton" />                  
                  <h:outputText value="#{sldEditorBean.textSymbolizerSummary}" styleClass="code"
                    rendered="#{not sldEditorBean.symbolizerExpanded}" escape="false" />
                  <t:div rendered="#{sldEditorBean.symbolizerExpanded}">
                    <t:div>
                      <h:outputText value="Geometry: " />
                      <h:inputText value="#{symbolizer.geometryAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="Label: " />
                      <h:inputText value="#{symbolizer.labelAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="FontFamily: " />
                      <h:inputText value="#{symbolizer.font.fontFamily}" styleClass="inputBox" style="width:200px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="FontSize: " />
                      <h:inputText value="#{symbolizer.font.fontSize}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="FontStyle: " />
                      <t:selectOneMenu value="#{symbolizer.font.fontStyle}" styleClass="selectBox">
                        <f:selectItem itemLabel=" " itemValue="" />
                        <f:selectItem itemLabel="normal" itemValue="normal" />
                        <f:selectItem itemLabel="italic" itemValue="italic" />
                        <f:selectItem itemLabel="oblique" itemValue="oblique" />
                      </t:selectOneMenu>
                    </t:div>
                    <t:div>
                      <h:outputText value="FontWeight: " />
                      <t:selectOneMenu value="#{symbolizer.font.fontWeight}" styleClass="selectBox">
                        <f:selectItem itemLabel=" " itemValue="" />
                        <f:selectItem itemLabel="normal" itemValue="normal" />
                        <f:selectItem itemLabel="bold" itemValue="bold" />
                      </t:selectOneMenu>
                    </t:div>
                    <t:div>
                      <h:outputText value="FillColor: " />
                      <h:inputText value="#{symbolizer.fill.fillColor}" styleClass="color {hash:true, required:false}" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="FillOpacity: " />
                      <h:inputText value="#{symbolizer.fill.fillOpacity}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="AnchorPointX: " />
                      <h:inputText value="#{symbolizer.pointPlacement.anchorPointXAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="AnchorPointY: " />
                      <h:inputText value="#{symbolizer.pointPlacement.anchorPointYAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="DisplacementX: " />
                      <h:inputText value="#{symbolizer.pointPlacement.displacementXAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="DisplacementY: " />
                      <h:inputText value="#{symbolizer.pointPlacement.displacementYAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="Rotation: " />
                      <h:inputText value="#{symbolizer.pointPlacement.rotationAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="PerpendicularOffset: " />
                      <h:inputText value="#{symbolizer.linePlacement.perpendicularOffsetAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="HaloRadius: " />
                      <h:inputText value="#{symbolizer.halo.radiusAsCql}" styleClass="codeBox" style="width:300px"
                        onfocus="showCQLAssistant('namedLayer[#{sldEditorBean.namedLayerIndex}]', this)"
                        onblur="hideCQLAssistant()" />
                    </t:div>
                    <t:div>
                      <h:outputText value="HaloFillColor: " />
                      <h:inputText value="#{symbolizer.halo.fill.fillColor}" styleClass="color {hash:true, required:false}" style="width:60px" />
                    </t:div>
                    <t:div>
                      <h:outputText value="HaloFillOpacity: " />
                      <h:inputText value="#{symbolizer.halo.fill.fillOpacity}" styleClass="codeBox" style="width:60px" />
                    </t:div>
                  </t:div>
                </t:div>
              </t:dataList>
              <t:div rendered="#{sldEditorBean.ruleExpanded}">
                <h:commandButton value="+PointSymbolizer" action="#{sldEditorBean.addPointSymbolizer}" styleClass="smallButton" />
                <h:commandButton value="+LineSymbolizer" action="#{sldEditorBean.addLineSymbolizer}" styleClass="smallButton" />
                <h:commandButton value="+PolygonSymbolizer" action="#{sldEditorBean.addPolygonSymbolizer}" styleClass="smallButton" />
                <h:commandButton value="+TextSymbolizer" action="#{sldEditorBean.addTextSymbolizer}" styleClass="smallButton" />
              </t:div>
            </t:dataList>
            <h:commandButton value="+Rule" action="#{sldEditorBean.addRule}" styleClass="smallButton"
              rendered="#{sldEditorBean.userStyleExpanded}" />
          </t:dataList>
          <h:commandButton value="+UserStyle" action="#{sldEditorBean.addUserStyle}" styleClass="smallButton"
            rendered="#{sldEditorBean.namedLayerExpanded}" />
        </t:dataList>
        <h:commandButton value="+NamedLayer" action="#{sldEditorBean.addNamedLayer}" styleClass="smallButton"
          rendered="#{not sldEditorBean.focusingNamedLayer}" />
        <h:outputText value="#{sldEditorBean.basicModeScripts}" escape="false" />
      </t:div>

      <t:div rendered="#{sldEditorBean.advancedMode}">
        <t:inputTextarea id="sld_editor" forceId="true"
          value="#{sldEditorBean.source}" />
        <h:outputText value="#{sldEditorBean.advancedModeScripts}" escape="false" />
      </t:div>

    </t:div>

  </t:panelGroup>

  <sf:saveScroll />
  
</jsp:root>

