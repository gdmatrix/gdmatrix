<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <!-- CONTENT -->

  <t:panelGrid columns="2" columnClasses="ilcol1,ilcol2" width="100%"
               styleClass="interiorLayout" summary="" title="">
    <t:div styleClass="leftMenu">
      <t:div styleClass="web_capture"
        rendered="#{userSessionBean.selectedMenuItem.properties.captureEnabled == 'true'}">
        <h:outputLink value="javascript:capture()" styleClass="capture_url">
          <h:graphicImage value="/documents/#{userSessionBean.selectedMenuItem.properties.captureImageDocId}" 
            rendered="#{userSessionBean.selectedMenuItem.properties.captureImageDocId != null}"
            alt="" title="" />
          <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.captureLabel}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:outputLink>
        <h:outputLink value="/go.faces?xmid=#{userSessionBean.selectedMenuItem.properties.captureHelpMid}"
          rendered="#{userSessionBean.selectedMenuItem.properties.captureHelpMid != null}"
          styleClass="capture_help">
          <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.captureHelpLabel}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:outputLink>
      </t:div>
    </t:div>

    <t:div>
      <sf:navigationPath id="navPath"
                       var="item"
                       value="main"
                       baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                       styleClass="navPath">
        <f:facet name="menuitem">
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </f:facet>
      </sf:navigationPath>
    </t:div>
  </t:panelGrid>

  <sf:widget id="widget_body" styleClass="widget">
  <h:panelGroup>
    <jsp:include page="${requestScope['_body']}"/>
  </h:panelGroup>
  </sf:widget>
</jsp:root>
