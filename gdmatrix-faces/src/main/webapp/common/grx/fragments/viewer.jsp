<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.grx.web.resources.GrxBundle"
                var="grxBundle"/>
 
  <t:saveState value="#{viewerBean.context}" />

  <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.headerDocId}"
    port="#{applicationBean.defaultPort}"
    translator="#{userSessionBean.translator}"
    rendered="#{userSessionBean.selectedMenuItem.properties.headerDocId != null}"
    translationGroup="#{userSessionBean.translationGroup}" />

  <h:panelGrid columns="3" styleClass="grxWindow"
    columnClasses="col1,col2,col3"
    headerClass="header" 
    style="margin-left:auto;margin-right:auto">

    <f:facet name="header">
      <t:div style="margin-left:auto;margin-right:auto;"
        styleClass="toolbar">
        <h:selectOneRadio value="#{viewerBean.context.selcmd}"
          style="margin-left:auto;margin-right:auto;">
          <f:selectItem itemLabel="#{grxBundle.zoomin}" itemValue="zoomin"/>
          <f:selectItem itemLabel="#{grxBundle.zoomout}" itemValue="zoomout"/>
          <f:selectItem itemLabel="#{grxBundle.center}" itemValue="center"/>
          <f:selectItem itemLabel="#{viewerBean.infoLabel == null ? grxBundle.infoLabel : viewerBean.infoLabel}" itemValue="info" />
        </h:selectOneRadio>
        <h:commandLink action="#{viewerBean.zoomAll}"
          style="text-decoration:none;margin-left:4px">
          <h:graphicImage url="/common/grx/images/zoomall.gif"
           style="border:none;vertical-align:middle;" />
          <h:outputText value="#{grxBundle.extension}" style="margin-left:4px" />
        </h:commandLink>
        <h:outputLink value="/pdfprint/#{viewerBean.context.sd.encoded}"
          target="_blank" style="text-decoration:none;margin-left:4px">
          <h:graphicImage url="/common/grx/images/print.gif"
           style="border:none;vertical-align:middle;" />
          <h:outputText value="#{grxBundle.print}" style="margin-left:4px" />
        </h:outputLink>
      </t:div>
    </f:facet>

    <h:commandLink action="#{viewerBean.moveNW}">
      <h:graphicImage url="/common/grx/images/arrow_nw.gif" alt=""
       style="border:none" />
    </h:commandLink>

    <h:commandLink action="#{viewerBean.moveN}">
      <h:graphicImage url="/common/grx/images/arrow_n.gif" alt=""
       style="border:none" />
    </h:commandLink>

    <h:commandLink action="#{viewerBean.moveNE}">
      <h:graphicImage url="/common/grx/images/arrow_ne.gif" alt=""
       style="border:none" />
    </h:commandLink>

    <h:commandLink action="#{viewerBean.moveW}">
      <h:graphicImage url="/common/grx/images/arrow_w.gif" alt=""
       style="border:none" />
    </h:commandLink>

    <h:panelGrid columns="2" styleClass="renderCanvas"
      rowClasses="crow" columnClasses="ccol1, ccol2" summary=""
      width="#{viewerBean.width}px" cellpadding="0" cellspacing="0">
      <h:outputText value="#{viewerBean.context.currentAddress}" 
        styleClass="title" />
      <h:panelGroup styleClass="zoomButtons">
        <h:outputText value="#{grxBundle.scaleLabel}:" />
        <h:commandLink action="#{viewerBean.zoom0}">
          <h:graphicImage url="/common/grx/images/zoom0.gif" />
        </h:commandLink>
        <h:commandLink action="#{viewerBean.zoom1}">
          <h:graphicImage url="/common/grx/images/zoom1.gif" />
        </h:commandLink>
        <h:commandLink action="#{viewerBean.zoom2}">
          <h:graphicImage url="/common/grx/images/zoom2.gif" />
        </h:commandLink>
        <h:commandLink action="#{viewerBean.zoom3}">
          <h:graphicImage url="/common/grx/images/zoom3.gif" />
        </h:commandLink>
      </h:panelGroup>
      <f:facet name="footer">
        <sf:renderCanvas
          definition="#{viewerBean.context.sd}"
          point="#{viewerBean.context.point}"
          action="#{viewerBean.mousePressed}"
          width="#{viewerBean.width}px" height="#{viewerBean.height}px"
          style="cursor:crosshair" />
      </f:facet>
    </h:panelGrid>

    <h:commandLink action="#{viewerBean.moveE}">
      <h:graphicImage url="/common/grx/images/arrow_e.gif" alt=""
       style="border:none" />
    </h:commandLink>

    <h:commandLink action="#{viewerBean.moveSW}">
      <h:graphicImage url="/common/grx/images/arrow_sw.gif" alt=""
       style="border:none" />
    </h:commandLink>

    <h:commandLink action="#{viewerBean.moveS}">
      <h:graphicImage url="/common/grx/images/arrow_s.gif" alt=""
       style="border:none"/>
    </h:commandLink>

    <h:commandLink action="#{viewerBean.moveSE}">
      <h:graphicImage url="/common/grx/images/arrow_se.gif" alt=""
      style="border:none"/>
    </h:commandLink>

    <f:facet name="footer">
      <h:panelGrid columns="1" width="100%">
      
        <h:outputText value="#{viewerBean.context.message}" />
        
        <t:panelTabbedPane 
          binding="#{viewerBean.tabbedPane}"
          activeTabStyleClass="CSSClass"
          inactiveTabStyleClass="CSSClass"
          disabledTabStyleClass="CSSClass"
          activeSubStyleClass="CSSClass"
          inactiveSubStyleClass="CSSClass"
          tabContentStyleClass="CSSClass"
          width="100%">
          <t:panelTab label="#{viewerBean.searchAddressLabel == null ? grxBundle.searchAddressLabel : viewerBean.searchAddressLabel}">
            <h:panelGrid columns="2" style="width:100%" 
              styleClass="addressLocator" columnClasses="alcol1,alcol2">
              <h:outputText value="#{grxBundle.streetNameLabel}:" styleClass="outputBox" />
              <h:inputText value="#{viewerBean.context.street}" 
                styleClass="inputBox" style="width:90%" />
              <h:outputText value="#{grxBundle.numberLabel}:" styleClass="outputBox" />
              <h:panelGroup>
                <h:inputText value="#{viewerBean.context.number}" 
                  styleClass="inputBox" />
                <h:commandButton value="#{grxBundle.searchButtonLabel}" 
                  action="#{viewerBean.searchAddress}"
                  styleClass="grxButton" />
              </h:panelGroup>
            </h:panelGrid>
          </t:panelTab>
          <t:panelTab label="#{viewerBean.layersLabel == null ? grxBundle.layersLabel : viewerBean.layersLabel}"
             style="text-align:center;margin-left:auto;margin-right:auto;">
             <t:dataTable value="#{viewerBean.visibleLayers}"
               footerClass="legendFooter"
               width="100%" var="layer" newspaperColumns="2">
               <t:column style="text-align:right">
                 <h:graphicImage url="#{layer.iconURL}"
                   style="vertical-align:middle" 
                   rendered="#{layer.iconURL != null}" />
               </t:column>
               <t:column style="text-align:left">
                 <sf:outputText value="#{layer.displayName}"
                   translator="#{userSessionBean.translator}"
                   translationGroup="#{userSessionBean.translationGroup}" />
               </t:column>
               <f:facet name="footer">
                 <sf:commandButton 
                   value="#{viewerBean.layersConfigLabel == null ? grxBundle.layersConfigLabel : viewerBean.layersConfigLabel}"
                   action="layer_selection" styleClass="grxButton"
                   translator="#{userSessionBean.translator}"
                   translationGroup="#{userSessionBean.translationGroup}" />
               </f:facet>
             </t:dataTable>
          </t:panelTab>
          <t:panelTab label="#{viewerBean.infoLabel == null ? grxBundle.infoLabel : viewerBean.infoLabel}" 
            style="text-align:left;margin-left:auto;margin-right:auto;">
            <h:outputText value="#{viewerBean.context.info}" escape="false" />
          </t:panelTab>
          <t:panelTab label="Admin" rendered="#{viewerBean.adminUser}">
            <h:outputText value="#{grxBundle.commandLabel}: " styleClass="outputBox" />
            <h:inputText value="#{viewerBean.context.inputCommand}" 
              styleClass="inputBox" style="width:65%"/>
            <h:commandButton value="#{webBundle.buttonExecute}"
              action="#{viewerBean.executeCommand}" 
              styleClass="grxButton" />
          </t:panelTab>
        </t:panelTabbedPane>

        <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
          showSummary="true" 
          layout="table"
          globalOnly="true"
          infoClass="infoMessage"
          warnClass="warnMessage"
          errorClass="errorMessage" 
          fatalClass="fatalMessage" />

      </h:panelGrid>
    </f:facet>
  </h:panelGrid>
</jsp:root>
