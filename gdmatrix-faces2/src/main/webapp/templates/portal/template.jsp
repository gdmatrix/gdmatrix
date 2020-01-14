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
               footerClass="footer" 
               width="100%">

    <f:facet name="header">
      <h:panelGrid columns="2" styleClass="header" 
        cellspacing="0" cellpadding="0" rowClasses="row1,row2"
        columnClasses="col1,col2" headerClass="header" summary="">
        
        <f:facet name="header">
          <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.pantoneUrl == null ? 
            '#' : userSessionBean.selectedMenuItem.properties.pantoneUrl}"
            title="#{userSessionBean.selectedMenuItem.properties.pantoneTitle == null ? 
            '' : userSessionBean.selectedMenuItem.properties.pantoneTitle}">
            <h:graphicImage url="#{userSessionBean.selectedMenuItem.properties.pantone}" width="100%" style="border:none" alt=""/>
          </h:outputLink>
        </f:facet>

        <h:panelGroup>
          <h:outputText value="#{webBundle.language}:" />
          <sf:languageSelector locales="#{userSessionBean.supportedLocales}" />
        </h:panelGroup>

        <sf:navigationMenu id="topmenu"
                           var="item"
                           value="main"
                           orientation="horizontal"
                           baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.mid}"
                           styleClass="topmenu"
                           selectedStyleClass="topmenu_selected" 
                           unselectedStyleClass="topmenu_unselected">
           <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
             style="vertical-align:middle"
             translator="#{userSessionBean.translator}"
             translationGroup="#{userSessionBean.translationGroup}" />
        </sf:navigationMenu>

      </h:panelGrid>
    </f:facet>

    <h:panelGrid id="menuCol" columns="1" cellspacing="0" cellpadding="0" 
      width="100%" summary="" styleClass="menuTable"
      rowClasses="row1,row2,row3,row4">
<!--
      <h:outputText value="#{userSessionBean.systemTime}" />
-->      

      <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.topwebTitle != null ?
                userSessionBean.selectedMenuItem.properties.topwebTitle : 
                'Els nostres webs:'}" styleClass="websText"
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}" />

      <sf:navigationMenu id="websmenu"
                         value="main"
                         var="item"
                         baseMid="#{userSessionBean.selectedMenuItem.path[0]}"
                         mode="passive"
                         orientation="vertical"
                         style="width:100%"
                         selectedStyleClass="websmenu_item" 
                         unselectedStyleClass="websmenu_item">
        <h:panelGrid columns="2" cellpadding="0" cellspacing="0" summary=""
          rendered="#{item.properties.topweb == 'true' and item.rendered}">
          <h:graphicImage url="#{item.properties.iconURL}" 
                          width="32" height="32" alt=""
                          style="border-style:none;margin-right:4px;" />
          <h:outputLink value="#{item.actionURL}" 
            onclick="#{item.onclick}" target="#{item.target}" 
            styleClass="weblink">
            <sf:outputText value="#{item.properties.description}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </h:outputLink>
        </h:panelGrid>
      </sf:navigationMenu>

      <h:panelGrid columns="1" summary="" cellpadding="2"
                   rendered="#{userSessionBean.anonymousUser}"
                   styleClass="loginContainer">
        <h:outputText style="font-weight: bold" styleClass="userBar"
          value="#{webBundle.outputMessageNotConnected}" />
        <h:outputText value="#{webBundle.outputUsername}:" />
        <h:inputText binding="#{loginBean.usernameInputText}"
                     styleClass="loginBox" immediate="true"
                     onkeypress="login(event)"
                     tabindex="1" size="10" />
        <h:outputText value="#{webBundle.outputPassword}:" />
        <h:inputSecret binding="#{loginBean.passwordInputSecret}" 
                       styleClass="loginBox" immediate="true"
                       size="10" tabindex="2" 
                       onkeypress="login(event)" />
        <sf:secureCommandLink action="#{loginBean.login}" 
            styleClass="loginButton" function="login" immediate="true"
            port="#{applicationBean.serverSecurePort}">
          <h:outputText value="#{webBundle.buttonSignin}" />
        </sf:secureCommandLink>

        <h:outputText value="#{loginBean.loginMessage}"
                      rendered="#{loginBean.loginMessage != null}"
                      styleClass="errorMessage" />

        <h:panelGroup style="line-height:8pt">
          <h:outputText
            style="font-weight: bold" styleClass="userBar" 
            value="#{webBundle.outputMessageInfo} " />
          <h:commandLink action="register" styleClass="buttonLink"
                         style="font-size: 7pt">
            <h:outputText value="#{webBundle.outputCreateAccount}" />
          </h:commandLink>
        </h:panelGroup>
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
          <h:outputText value="#{webBundle.buttonSignout}" />
        </h:commandLink>

        <h:commandLink action="password" styleClass="buttonLink">
          <h:outputText value="#{webBundle.outputChangePassword}" />
        </h:commandLink>
      </h:panelGrid>

      <h:outputText value="#{webBundle.themes}:"
        rendered="#{userSessionBean.themeSelectionEnabled}" />
      <sf:commandMenu value="#{userSessionBean.theme}"
        rendered="#{userSessionBean.themeSelectionEnabled}">
        <f:selectItems value="#{userSessionBean.themes}" />
      </sf:commandMenu>

    </h:panelGrid>

    <h:panelGrid id="contCol" styleClass="contTable" width="100%"
                 cellpadding="0" cellspacing="0" summary=""
                 columns="1" rowClasses="emptyRow,tabsMenuRow,contRow">

      <h:outputText value="" />

      <sf:navigationMenu id="tabsmenu"
                         var="item"
                         value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].firstChild.mid}"
                         orientation="horizontal"
                         styleClass="tabsmenu"
                         selectedStyleClass="tabsmenu_selected" 
                         unselectedStyleClass="tabsmenu_unselected">
         <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
           style="vertical-align:middle"
           translator="#{userSessionBean.translator}"
           translationGroup="#{userSessionBean.translationGroup}" />
      </sf:navigationMenu>

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
                             baseMid="#{userSessionBean.selectedMenuItem.properties.rightMenuMid}"
                             orientation="vertical"
                             styleClass="rmenu"
                             mode="passive">
             <h:panelGrid columns="4" width="100%" cellpadding="0" cellspacing="0"
               styleClass="rmenu_item" rowClasses="row1,row2,row3"
               columnClasses="col1,col2,col3,col4" summary="">
               <h:graphicImage value="/templates/portal/images/round_nw.gif" alt="" />
               <h:panelGroup />
               <h:panelGroup />
               <h:graphicImage value="/templates/portal/images/round_ne.gif" alt="" />

               <h:panelGroup />
               <h:panelGroup>
                 <h:graphicImage value="#{item.properties.iconURL}" alt=""
                   styleClass="icon" width="32px" height="32px"
                   rendered="#{item.properties.iconURL != null}" />
               </h:panelGroup>
               <h:outputLink value="#{item.actionURL}" 
                  onclick="#{item.onclick}"
                  target="#{item.target}">
                 <sf:outputText value="#{item.label}" styleClass="text"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" />
               </h:outputLink>
               <h:panelGroup />

               <h:graphicImage value="/templates/portal/images/round_sw.gif" alt="" />
               <h:panelGroup />
               <h:panelGroup />
               <h:graphicImage value="/templates/portal/images/round_se.gif" alt="" />
             </h:panelGrid>
          </sf:navigationMenu>
        </h:panelGrid>
      </h:panelGrid>
    </h:panelGrid>

  </h:panelGrid>
</jsp:root>
