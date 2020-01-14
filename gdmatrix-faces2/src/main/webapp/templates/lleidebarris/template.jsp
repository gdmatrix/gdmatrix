<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:panelGrid id="mainLayout" columns="2" 
               styleClass="main" 
               rowClasses="contRow"
               cellspacing="0"
               cellpadding="0"
               columnClasses="menuCol,contCol" 
               headerClass="header"
               footerClass="footer" 
               width="100%">

    <f:facet name="header">
      <h:panelGrid columns="3" style="width:100%" 
        cellspacing="0" cellpadding="0"
        rowClasses="row1"
        columnClasses="column1,column2,column3"
        footerClass="footer"
        summary="">

        <h:panelGroup rendered="#{userSessionBean.anonymousUser}">
          <h:outputText value="#{webBundle.outputUsername}:"
            style="margin-right:4px" />
          <h:inputText binding="#{loginBean.usernameInputText}"
                       styleClass="loginBox" immediate="true"
                       tabindex="1" size="10" />
          <h:outputText value="#{webBundle.outputPassword}:"
                        style="margin-left:4px; margin-right:4px" />
          <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                         styleClass="loginBox" immediate="true"
                         tabindex="2" size="10" onkeypress="login(event)" />
          <sf:secureCommandLink style="margin-left:4px" 
            action="#{loginBean.login}" function="login" immediate="true"
            port="#{applicationBean.serverSecurePort}" scheme="https">
            <h:graphicImage url="/templates/lleidebarris/images/button.gif" alt="" 
              style="border-style:none;vertical-align:middle" />
          </sf:secureCommandLink>
        </h:panelGroup>

        <h:panelGroup rendered="#{not userSessionBean.anonymousUser}">
          <h:outputText value="#{webBundle.outputUsername}:"
            style="margin-right:4px" />
          <h:outputText value="#{userSessionBean.displayName}" styleClass="displayNameText" />
          <h:commandLink action="#{loginBean.logout}" style="margin-left:4px">
            <h:graphicImage url="/templates/lleidebarris/images/button.gif" alt="" 
              style="border-style:none;vertical-align:middle" />
          </h:commandLink>
        </h:panelGroup>

        <h:panelGroup>
          <!--
          <h:outputText value="#{webBundle.search}: " />
          <h:inputText size="14" styleClass="loginBox" />
          <h:commandLink style="margin-left:4px" 
                         action="">
            <h:graphicImage url="/lleidebarris/images/button.gif" 
              style="border-style:none;vertical-align:middle" />
          </h:commandLink>
          -->
        </h:panelGroup>

        <h:outputText value="" />

        <f:facet name="footer">
          <h:panelGroup>
            <h:outputText value="#{loginBean.loginMessage}"
                          rendered="#{loginBean.loginMessage != null}"
                          styleClass="errorMessage" />
            <h:commandLink action="register" styleClass="loginCommand" 
                           rendered="#{userSessionBean.anonymousUser}">
              <h:outputText value="#{webBundle.outputCreateAccount}" />
            </h:commandLink>
            <h:commandLink action="password" styleClass="loginCommand" 
                           rendered="#{!userSessionBean.anonymousUser}">
              <h:outputText value="#{webBundle.outputChangePassword}" />
            </h:commandLink>
          </h:panelGroup>
        </f:facet>
      </h:panelGrid>
    </f:facet>

    <h:panelGrid id="menuCol" columns="1" cellspacing="0" cellpadding="0" 
      width="90%" summary="">
      <h:graphicImage url="/templates/lleidebarris/images/lleidebarris.gif" width="100%" alt="" />

      <sf:navigationMenu id="vmenu"
                         value="main"
                         var="item"
                         baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
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

      <h:outputText value="#{webBundle.language}:" styleClass="langSel" />
      <sf:languageSelector locales="#{userSessionBean.supportedLocales}" styleClass="langSel"/>
    </h:panelGrid>

    <h:panelGrid id="contCol"
                 cellpadding="0" cellspacing="0" summary="" width="100%"
                 columns="1" rowClasses="navPathRow, hmenuRow, contRow">
      <sf:navigationPath id="navPath"
                         value="main" 
                         var="item"
                         baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                         styleClass="navPath">
        <f:facet name="separator">
          <h:outputText value=" || " />
        </f:facet>
        <f:facet name="menuitem">
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </f:facet>
      </sf:navigationPath>

      <h:panelGrid cellspacing="0" cellpadding="4" 
        style="width:100%;border-style:solid;border-top-width:1px;border-bottom-width:1px;border-left-width:0px;border-right-width:0px;border-color:red">
        <sf:navigationMenu id="hmenu"
                           value="main" 
                           var="item"    
                           orientation="horizontal"
                           rendered="#{userSessionBean.selectedMenuItem.depth > 3 || (userSessionBean.selectedMenuItem.childCount > 0 and userSessionBean.selectedMenuItem.depth == 3)}"
                           styleClass="hmenu"
                           selectedStyleClass="hmenu_selected" 
                           unselectedStyleClass="hmenu_unselected">
              <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
                style="vertical-align:middle"
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}" />
        </sf:navigationMenu>
      </h:panelGrid>
    
      <h:panelGroup>
        <jsp:include page="${requestScope['_body']}"/>
      </h:panelGroup>
    </h:panelGrid>
         
  </h:panelGrid>
</jsp:root>
