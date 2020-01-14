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
      <h:panelGrid columns="1" style="width:100%" 
        cellspacing="0" cellpadding="0"
        rowClasses="hmenuRow"
        columnClasses="column1,column2,column3"
        footerClass="footer"
        summary="">

        <f:facet name="header">
          <h:graphicImage url="/templates/default/images/pantone.jpg" width="100%" />
        </f:facet>

        <sf:navigationMenu id="hmenu"
                           var="item"
                           value="main"
                           orientation="horizontal"
                           baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
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

    <h:panelGrid id="menuCol" columns="1" cellspacing="0" cellpadding="0"
      width="100%" summary="">

      <sf:navigationMenu id="vmenu"
                         var="item"
                         value="main"
                         orientation="vertical"
                         styleClass="vmenu"
                         rendered="#{userSessionBean.selectedMenuItem.depth > 2}"
                         selectedStyleClass="vmenu_selected" 
                         unselectedStyleClass="vmenu_unselected">        
        <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
          style="vertical-align:middle"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </sf:navigationMenu>

      <h:panelGrid columns="1" summary="" cellpadding="2"
                   rendered="#{userSessionBean.anonymousUser}"
                   styleClass="loginContainer">
        <h:outputText
          style="font-weight: bold" styleClass="userBar" 
          value="Usuario:" />                                           
        <h:outputText value="#{webBundle.outputUsername}" />
        <h:inputText binding="#{loginBean.usernameInputText}"
                     styleClass="loginBox" immediate="true"
                     tabindex="1" size="10" />
        <h:outputText value="#{webBundle.outputPassword}" />
        <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                       styleClass="loginBox" size="10" immediate="true"
                       tabindex="2"/>
        <sf:secureCommandLink action="#{loginBean.login}" 
            styleClass="loginButton" immediate="true"
            port="#{applicationBean.serverSecurePort}" scheme="https">
          <h:outputText value="#{webBundle.buttonSignin}" />
        </sf:secureCommandLink>

        <h:outputText value="#{loginBean.loginMessage}"
                      rendered="#{loginBean.loginMessage != null}"
                      styleClass="errorMessage" />
      </h:panelGrid>

      <h:panelGrid rendered="#{not userSessionBean.anonymousUser}"
                   cellpadding="2" columns="1" 
                   styleClass="loginContainer" summary="">
        <h:outputText value="#{webBundle.outputUsername}:"
            style="text-align:left" />
        <h:outputText value="#{userSessionBean.displayName}"
                      styleClass="displayName"/>

        <h:commandLink action="#{loginBean.logout}"
          styleClass="loginButton" immediate="true">
          <h:outputText value="[#{webBundle.buttonSignout}]" />
        </h:commandLink>

        <h:commandLink action="password" styleClass="buttonLink">
          <h:outputText value="#{webBundle.outputChangePassword}" />
        </h:commandLink>
      </h:panelGrid>

      <h:panelGroup styleClass="languageContainer">
        <h:outputText value="Idioma:" 
        style="margin-right:4px" />
        <sf:languageSelector styleClass="langSel" />
      </h:panelGroup>
    </h:panelGrid>

    <h:panelGrid id="contCol" styleClass="contTable" width="100%"
                 cellpadding="0" cellspacing="0" summary=""
                 columns="1" rowClasses="hmenuRow, contRow" 
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

      <h:panelGroup>
        <jsp:include page="${requestScope['_body']}"/>
      </h:panelGroup>

    </h:panelGrid>
  </h:panelGrid>
</jsp:root>
