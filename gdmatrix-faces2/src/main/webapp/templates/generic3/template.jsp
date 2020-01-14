<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:panelGrid id="mainLayout" columns="2" 
               styleClass="main" 
               rowClasses="mainRow"
               cellspacing="0"
               cellpadding="0"
               columnClasses="menuCol,contCol" 
               headerClass="header"
               footerClass="footer" 
               width="100%" 
               summary="">

    <f:facet name="header">
      <h:panelGrid columns="3" style="width:100%" 
        cellspacing="0" cellpadding="0"
        rowClasses="row1"
        columnClasses="column1,column2,column3"
        footerClass="footer"
        summary="">

        <h:panelGroup rendered="#{userSessionBean.anonymousUser}">
          <h:outputText value="#{webBundle.outputUsername}:"
            style="margin-right:4px" styleClass="loginText" />
          <h:inputText binding="#{loginBean.usernameInputText}"
                       tabindex="1" size="10" immediate="true" 
                       onkeypress="login(event)"
                       styleClass="loginBox" />
          <h:outputText value="#{webBundle.outputPassword}:"
                        style="margin-left:4px; margin-right:4px" 
                        styleClass="loginText" />
          <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                         tabindex="2" size="10" immediate="true" 
                         onkeypress="login(event)"
                         styleClass="loginBox" />
          <sf:secureCommandLink id="loginbutton" style="margin-left:4px" 
            action="#{loginBean.login}" function="login" immediate="true"
            port="#{applicationBean.serverSecurePort}" scheme="https">
            <h:graphicImage url="/templates/generic1/images/button.gif" alt="login"
              style="border-style:none;vertical-align:middle" />
          </sf:secureCommandLink>
        </h:panelGroup>

        <h:panelGroup rendered="#{not userSessionBean.anonymousUser}">
          <h:outputText value="#{webBundle.outputUsername}:"
            styleClass="loginText" />
          <h:outputText value="#{userSessionBean.displayName}" 
            styleClass="displayNameText" style="margin-left:4px" />
        </h:panelGroup>

        <h:outputText value="#{userSessionBean.systemTime}" />

        <h:outputText value="" />

        <f:facet name="footer">
          <h:panelGrid columns="2" width="100%" summary=""
            cellpadding="0" cellspacing="0" columnClasses="fcol1, fcol2">
            <h:panelGroup>

              <h:outputText value="#{loginBean.loginMessage}"
                rendered="#{loginBean.loginMessage != null}"
                styleClass="errorMessage" />

              <h:commandLink action="#{loginBean.logout}"
                rendered="#{not userSessionBean.anonymousUser}"
                styleClass="loginCommand" immediate="true">
                <h:outputText value="[#{webBundle.buttonSignout}]" />
              </h:commandLink>

              <sf:secureCommandLink
                styleClass="loginCommand" immediate="true"
                rendered="#{not userSessionBean.certificateUser}"
                action="#{loginBean.loginCertificate}" scheme="https"
                port="#{applicationBean.clientSecurePort}">
                <h:outputText value="[#{webBundle.certificateAuthentication}]" />
              </sf:secureCommandLink>

              <h:commandLink action="register" styleClass="loginCommand" 
                             rendered="#{userSessionBean.anonymousUser}"
                             immediate="true">
                <h:outputText value="[#{webBundle.outputCreateAccount}]" />
              </h:commandLink>

              <h:commandLink action="password" styleClass="loginCommand" 
                             rendered="#{not userSessionBean.anonymousUser and 
                             not userSessionBean.certificateUser}"
                             immediate="true">
                <h:outputText value="[#{webBundle.outputChangePassword}]" />
              </h:commandLink>
            </h:panelGroup>

            <h:panelGroup/>

          </h:panelGrid>
        </f:facet>
      </h:panelGrid>
    </f:facet>

    <h:panelGrid id="menuCol" columns="1" cellspacing="0" cellpadding="0"
      width="100%" summary="">

      <h:graphicImage alt=""
        url="/documents/#{userSessionBean.selectedMenuItem.properties.imageId}"
        width="100%" />

      <sf:navigationMenu id="vmenu"
                         var="item"
                         value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                         orientation="vertical"
                         styleClass="vmenu"
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

    <h:panelGrid id="contCol" styleClass="contTable" width="100%"
                 cellpadding="0" cellspacing="0" summary=""
                 columns="1" rowClasses="contRow" 
                 headerClass="navPathRow">
      <f:facet name="header">
        <sf:navigationPath id="navPath"
                           var="item"
                           value="main"
                           baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                           styleClass="navPath">
          <f:facet name="separator">
            <h:outputText value=" / " />
          </f:facet>
          <f:facet name="menuitem">
            <sf:outputText value="#{item.label}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </f:facet>
        </sf:navigationPath>
      </f:facet>

      <h:panelGrid columns="2" width="100%" summary=""
        styleClass="content"
        columnClasses="ccol1, ccol2">

        <h:panelGroup>
          <jsp:include page="${requestScope['_body']}"/>
        </h:panelGroup>
        
        <h:panelGrid columns="1" summary="" styleClass="rightMenuPanel"
          rowClasses="title,menu"
          rendered="#{userSessionBean.selectedMenuItem.properties.rightMenuMid != null and
            userSessionBean.selectedMenuItem.properties.rightMenuMid != 'disabled'}">
          <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.rightMenuTitle}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
          <sf:navigationMenu id="rmenu"
                             var="item"
                             value="main"
                             orientation="vertical"
                             styleClass="rmenu"
                             baseMid="#{userSessionBean.selectedMenuItem.properties.rightMenuMid}"
                             selectedStyleClass="rmenu_item" 
                             unselectedStyleClass="rmenu_item">
             <h:graphicImage value="/templates/generic1/images/item.gif" 
               rendered="#{item.rendered}" styleClass="icon" alt="" />
             <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
               styleClass="text"
               translator="#{userSessionBean.translator}"
               translationGroup="#{userSessionBean.translationGroup}" />
          </sf:navigationMenu>
        </h:panelGrid>
      </h:panelGrid>
    </h:panelGrid>

  </h:panelGrid>
</jsp:root>
