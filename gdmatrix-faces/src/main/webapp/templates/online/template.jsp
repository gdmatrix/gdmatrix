<?xml version='1.0' encoding='windows-1252'?>
 <jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
           xmlns:f="http://java.sun.com/jsf/core"
           xmlns:h="http://java.sun.com/jsf/html"
           xmlns:sf ="http://www.santfeliu.org/jsf">

    <h:panelGrid columns="2" id="mainLayout" styleClass="main"
                 columnClasses="menuCol,contCol"
                 rowClasses="row1"
                 headerClass="header"
                 cellspacing="0" cellpadding="0">

      <f:facet name="header">
        <h:panelGrid columns="2" cellspacing="0" cellpadding="0" 
          width="100%" summary="" columnClasses="column1,column2" footerClass="footer">
           <h:graphicImage style="border-style:none;margin:0px;padding:0px" 
                          url="/templates/online/images/logo_sfol.jpg" alt="" />
           <h:graphicImage style="border-style:none;margin:0px;padding:0px"
                          url="#{userSessionBean.selectedMenuItem.properties.pantone}" alt="" />                          
           <f:facet name="footer">
		     
           <h:panelGrid columns="1" cellpadding="0" cellspacing="0" style="width:100%"
             rowClasses="topmenuRow, capmenuRow">
             <sf:navigationMenu id="topmenu" var="item" value="main"
                                orientation="horizontal"
                                styleClass="topmenu"
                                selectedStyleClass="topmenu_selected"
                                unselectedStyleClass="topmenu_unselected"
                                baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].firstChild.mid}">
                <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
                  style="padding:4px"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" />
             </sf:navigationMenu>
    
             <sf:navigationMenu id="capmenu" var="item" value="main"
                                orientation="horizontal"
                                styleClass="capmenu"
                                selectedStyleClass="capmenu_selected"
                                unselectedStyleClass="capmenu_unselected"
                                baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].firstChild.next.mid}" style="width:100%">
                <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" />
             </sf:navigationMenu>
           </h:panelGrid>
			 
           </f:facet>
        </h:panelGrid>
      </f:facet>

      <h:panelGrid id="menuCol" columns="1" width="100%"
        cellpadding="0" cellspacing="0" summary="">
        
        <sf:navigationMenu id="vmenu" value="main" var="item"
                           baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                           orientation="vertical" styleClass="vmenu"
                           selectedStyleClass="vmenu_selected"
                           unselectedStyleClass="vmenu_unselected">
          <sf:outputText rendered="#{item.childIndex > 3 and item.rendered}"
                        value="#{item.label}"
                        translator="#{userSessionBean.translator}"
                        translationGroup="#{userSessionBean.translationGroup}" />
        </sf:navigationMenu>

        <h:panelGrid columns="1" summary="" cellpadding="2"
                     rendered="#{userSessionBean.anonymousUser}"
                     styleClass="loginContainer">
          <h:outputText
            style="font-weight: bold" styleClass="userBar" 
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
              port="#{applicationBean.serverSecurePort}" scheme="https">
            <h:outputText value="#{webBundle.buttonSignin}" />
          </sf:secureCommandLink>

          <h:outputText value="#{loginBean.loginMessage}"
                        rendered="#{loginBean.loginMessage != null}"
                        styleClass="errorMessage" />

          <h:panelGroup style="line-height:8pt">
            <h:outputText
              style="font-weight: bold" styleClass="userBar" 
              value="#{webBundle.outputMessageInfo}" />
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

        <h:panelGroup styleClass="languageContainer">
          <h:outputText value="#{webBundle.themes}:"
            rendered="#{userSessionBean.themeSelectionEnabled}" styleClass="langSel" />
          <sf:commandMenu value="#{userSessionBean.theme}"
            rendered="#{userSessionBean.themeSelectionEnabled}" styleClass="langSel">
            <f:selectItems value="#{userSessionBean.themes}" />
          </sf:commandMenu>
    
          <h:outputText value="#{webBundle.language}:" style="margin-right:4px" />
          <sf:languageSelector locales="#{userSessionBean.supportedLocales}" styleClass="langSel"/>          
        </h:panelGroup>              
      </h:panelGrid>

      <h:panelGrid id="contCol" columns="1" summary="" width="100%"
           cellpadding="0" cellspacing="3" rowClasses="navPathRow, hmenuRow, contRow">

        <h:panelGroup>
          <sf:navigationPath id="navPath"
                             value="main" 
                             var="item"
                             baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                             styleClass="navPath">
            <f:facet name="separator">
              <h:outputText value=" | " rendered="#{item.depth != 3 or item.childIndex > 3}" />
            </f:facet>
            <f:facet name="menuitem">
              <sf:outputText value="#{item.label}" rendered="#{item.depth != 3 or item.childIndex > 3}"
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}" />
            </f:facet>
          </sf:navigationPath>        
        </h:panelGroup>
        
        <sf:navigationMenu id="hmenu" var="item" value="main"
                           orientation="horizontal"
                           rendered="#{userSessionBean.selectedMenuItem.depth > 2 
                            and !(userSessionBean.selectedMenuItem.cursorPath[2].childIndex == 1 
                            and userSessionBean.selectedMenuItem.depth == 4 
                            and userSessionBean.selectedMenuItem.childCount == 0) 
                            and !(userSessionBean.selectedMenuItem.cursorPath[2].childIndex > 1 
                            and userSessionBean.selectedMenuItem.depth lt 4 
                            and userSessionBean.selectedMenuItem.childCount == 0)
                            and !(userSessionBean.selectedMenuItem.parent.cursorPath[2].childIndex == 3
                            and userSessionBean.selectedMenuItem.childCount == 0
                            and userSessionBean.selectedMenuItem.depth lt 5)}"
                           styleClass="hmenu"
                           selectedStyleClass="hmenu_selected"
                           unselectedStyleClass="hmenu_unselected">
          <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
                        translator="#{userSessionBean.translator}"
                        translationGroup="#{userSessionBean.translationGroup}" />
        </sf:navigationMenu>

        <h:panelGrid columns="2" styleClass="content" width="100%"
          columnClasses="ccol1, ccol2">
        
          <h:panelGroup>
            <jsp:include page="${requestScope['_body']}" />
          </h:panelGroup>
          
          <h:panelGrid columns="1" summary="" styleClass="rightMenuPanel"
            rowClasses="title, menu" width="100%"
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
                               selectedStyleClass="rmenu_selected" 
                               unselectedStyleClass="rmenu_unselected">
             <h:panelGrid columns="4" width="100%" cellpadding="0" cellspacing="0"
               styleClass="rmenu_item" rowClasses="row1,row2,row3"
               columnClasses="col1,col2,col3,col4" summary="">
               <h:graphicImage value="/templates/online/images/round_nw.gif" />
               <h:panelGroup />
               <h:panelGroup />
               <h:graphicImage value="/templates/online/images/round_ne.gif" />

               <h:panelGroup />
               <h:panelGroup>
                 <h:graphicImage value="#{item.properties.iconURL}" alt=""
                   styleClass="icon" width="32px" height="32px"
                   rendered="#{item.properties.iconURL != null}" />
               </h:panelGroup>
               <h:outputLink value="#{item.actionURL}" target="#{item.properties.target}">
                 <sf:outputText value="#{item.label}" styleClass="text"
                    translator="#{userSessionBean.translator}"
                    translationGroup="#{userSessionBean.translationGroup}" />
               </h:outputLink>
               <h:panelGroup />

               <h:graphicImage value="/templates/online/images/round_sw.gif" />
               <h:panelGroup />
               <h:panelGroup />
               <h:graphicImage value="/templates/online/images/round_se.gif" />
             </h:panelGrid>
            </sf:navigationMenu>
          </h:panelGrid>
        </h:panelGrid>
        
      </h:panelGrid>
    </h:panelGrid>
</jsp:root>