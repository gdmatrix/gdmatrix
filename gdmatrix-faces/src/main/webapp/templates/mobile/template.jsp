<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:t="http://myfaces.apache.org/tomahawk">

  <t:div styleClass="main">
    <t:div styleClass="frame">
      <sf:languageSelector locales="#{userSessionBean.supportedLocales}"
        styleClass="languages" />
      <h:panelGroup styleClass="desktop">
        <h:outputLink value="/go.faces?xmid=#{userSessionBean.selectedMenuItem.properties.desktopNodeMid}"
                      styleClass="link">
          <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.desktopVersionText}"
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{userSessionBean.translationGroup}" />
        </h:outputLink>
      </h:panelGroup>
    </t:div>
    <t:div styleClass="pantone">
      <h:outputLink 
        value="#{userSessionBean.selectedMenuItem.properties.pantoneUrl}"
        title="#{userSessionBean.selectedMenuItem.properties.pantoneTitle == null ?
        '' : userSessionBean.selectedMenuItem.properties.pantoneTitle}">
        <t:div styleClass="logo"></t:div>
      </h:outputLink>
    </t:div>
    <t:div styleClass="navPathRow">
      <sf:navigationPath id="navPath"
                         var="item"
                         value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                         styleClass="navPath">
        <f:facet name="separator">
          <h:outputText value=" &gt; " rendered="#{item.rendered}" />
        </f:facet>
        <f:facet name="menuitem">
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}"
            rendered="#{item.rendered}" />
        </f:facet>
      </sf:navigationPath>
    </t:div>

    <t:div styleClass="body">
      <sf:navigationMenu id="mobileMenu" value="main"
        baseMid="#{userSessionBean.selectedMid}" var="item"
        orientation="vertical" styleClass="mobileMenu"
        rendered="#{userSessionBean.selectedMenuItem.action == 'blank'}">
        <h:panelGrid columns="2" styleClass="menuitem" columnClasses="col1, col2"
          cellpadding="0" cellspacing="0" rendered="#{item.rendered}">
          <t:graphicImage value="#{item.properties.icon}"
            alt="#{item.properties.title}" title="#{item.properties.title}"
            rendered="#{item.properties.icon != null}" />
          <h:panelGroup styleClass="text">
            <sf:outputText value="#{item.label}"
              styleClass="title"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
            <sf:outputText value="#{item.properties.info}"
              styleClass="info"
              rendered="#{item.properties.info != null}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </h:panelGroup>
        </h:panelGrid>
      </sf:navigationMenu>

      <h:panelGroup>
        <jsp:include page="${requestScope['_body']}"/>
      </h:panelGroup>
    </t:div>    
  </t:div>
  
</jsp:root>
