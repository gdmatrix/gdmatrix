<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:panelGrid id="mainLayout" columns="2" 
               styleClass="main" 
               rowClasses="row"
               cellspacing="0"
               cellpadding="0"
               columnClasses="contCol,menuCol" 
               headerClass="header"
               footerClass="footer" 
               width="100%">

    <f:facet name="header">
      <h:panelGrid columns="1" style="width:100%" 
        cellspacing="0" cellpadding="0"
        rowClasses="row1, row2"
        columnClasses="column1"
        summary="">

        <h:graphicImage url="/templates/tramitacio/images/tramitacio.jpg" width="100%" height="50px" alt=""/>

        <sf:navigationMenu id="hmenu"
                           value="main" 
                           var="item"
                           baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].firstChild.mid}"
                           orientation="horizontal"
                           styleClass="hmenu"
                           selectedStyleClass="hmenu_selected" 
                           unselectedStyleClass="hmenu_unselected">
              <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
                style="vertical-align:middle"
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}" />
        </sf:navigationMenu>

      </h:panelGrid>
    </f:facet>

    <h:panelGrid id="contCol"
                 cellpadding="0" cellspacing="0" summary=""
                 columns="1" width="100%">
                 
        <jsp:include page="${requestScope['_body']}"/>

    </h:panelGrid>

    <sf:navigationMenu id="vmenu"
                       value="main"
                       var="item"
                       baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.mid}"
                       orientation="vertical"
                       style="width:100%"
                       selectedStyleClass="vmenu_selected" 
                       unselectedStyleClass="vmenu_unselected">
      <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
        style="vertical-align:middle"
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
</jsp:root>
