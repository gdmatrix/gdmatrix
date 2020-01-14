<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:panelGrid columns="2" id="mainLayout" styleClass="main"
               columnClasses="column1,column2" rowClasses="row1,row2"
               headerClass="header" footerClass="footer"
               cellspacing="0" cellpadding="0">
    <f:facet name="header">
      <h:graphicImage alt="Promocio Economica" style="margin:0px;border-style:none"
                      url="/templates/promocioeconomica/images/fonsindex.gif"/>
    </f:facet>
    <h:outputText value="" />
    <h:panelGrid id="topMenu" styleClass="topmenu" columns="1"
                 rowClasses="row1" cellspacing="0" cellpadding="0">
      <sf:navigationPath id="navPath" var="item" value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.path[1]}">
        <f:facet name="separator">
          <h:outputText value=" #{webBundle.facetSeparator}" />
        </f:facet>
        <f:facet name="menuitem">
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </f:facet>
      </sf:navigationPath>
      <sf:navigationMenu id="hmenu" value="main" var="item"
                         orientation="horizontal"
                         rendered="#{userSessionBean.selectedMenuItem.depth > 2}"
                         styleClass="hmenu" selectedStyleClass="hmenu_selected"
                         unselectedStyleClass="hmenu_unselected">
        <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </sf:navigationMenu>
    </h:panelGrid>
    <h:panelGroup id="vBar">
      <h:panelGrid columns="1">
        <sf:navigationMenu id="vmenu" value="main" var="item"
                           baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                           orientation="vertical" styleClass="vmenu"
                           selectedStyleClass="vmenu_selected "
                           unselectedStyleClass="vmenu_unselected">
          <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </sf:navigationMenu>
        <h:outputText value="#{webBundle.themes}:"
          rendered="#{userSessionBean.themeSelectionEnabled}" />
        <sf:commandMenu value="#{userSessionBean.theme}"
          rendered="#{userSessionBean.themeSelectionEnabled}">
          <f:selectItems value="#{userSessionBean.themes}" />
        </sf:commandMenu>
  
        <h:outputText value="#{webBundle.language}:" />
        <sf:languageSelector locales="#{userSessionBean.supportedLocales}" />
      </h:panelGrid>
    </h:panelGroup>
    <h:panelGroup id="pageBody">
      <jsp:include page="${requestScope['_body']}"/>
    </h:panelGroup>
  </h:panelGrid>
</jsp:root>