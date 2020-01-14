<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
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
      <h:graphicImage alt="" style="border-style:none;margin:0px;padding:0px" width="100%"
        url="/documents/#{userSessionBean.selectedMenuItem.properties.headerImageId}"
        rendered="#{userSessionBean.selectedMenuItem.properties.headerImageRendered != 'false'}" />
    </f:facet>

    <h:panelGrid id="menuCol" columns="1" cellspacing="0" cellpadding="0"
      width="100%" summary="">

    <t:div styleClass="languageContainer">
      <h:panelGroup>
        <h:outputText value="#{webBundle.language}:" />
        <sf:languageSelector locales="#{userSessionBean.supportedLocales}" />
      </h:panelGroup>
    </t:div>

     <h:graphicImage alt=""
       url="/documents/#{userSessionBean.selectedMenuItem.properties.logoImageId}" 
       width="100%" 
       rendered="#{userSessionBean.selectedMenuItem.properties.logoImageId != null}"/>   


      <sf:navigationMenu id="vmenu"
                         var="item"
                         value="main"
                         mode="passive"
                         baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].firstChild.mid}"
                         orientation="vertical"
                         styleClass="vmenu#{userSessionBean.selectedMenuItem.properties.vMenuClassSuffix}"
                         selectedStyleClass="vmenu_selected#{userSessionBean.selectedMenuItem.properties.vMenuClassSuffix}" 
                         unselectedStyleClass="vmenu_unselected#{userSessionBean.selectedMenuItem.properties.vMenuClassSuffix}"> 
        
        <h:outputLink value="#{item.actionURL}" onclick="#{item.onclick}" target="#{item.target}"
           rendered="#{item.rendered and item.properties.vMenuImageId != null
                  and (item.mid != userSessionBean.selectedMenuItem.cursorPath[2] 
                  or item.properties.vMenuSelectedImageId == null)}">
          <h:graphicImage url="/documents/#{item.properties.vMenuImageId}" alt="#{item.label}" />
        </h:outputLink>

        <h:outputLink value="#{item.actionURL}" onclick="#{item.onclick}" target="#{item.target}"
          rendered="#{item.rendered and item.properties.vMenuSelectedImageId != null
                  and item.mid == userSessionBean.selectedMenuItem.cursorPath[2]}" >
          <h:graphicImage url="/documents/#{item.properties.vMenuSelectedImageId}" alt="#{item.label}" />
        </h:outputLink>

        <h:outputLink value="#{item.actionURL}" onclick="#{item.onclick}" target="#{item.target}"
           rendered="#{item.properties.vMenuImageId == null and item.rendered}">
          <h:graphicImage value="/documents/#{item.properties.vMenuIconId}" alt=""
             styleClass="icon" width="32px" height="32px" 
             rendered="#{item.properties.vMenuImageId == null 
                    and item.properties.vMenuIconId != null}" />
          <sf:outputText value="#{item.label}" styleClass="text"
            rendered="#{item.properties.vMenuImageId == null and item.rendered}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:outputLink>

        
        <sf:navigationMenu id="vsubmenu"
                           var="subitem"
                           value="main"
                           rendered="#{item.cursorPath[3].mid == userSessionBean.selectedMenuItem.cursorPath[3].mid}"
                           baseMid="#{item.cursorPath[3].mid}"
                           orientation="vertical"
                           style="width:100%"
                           styleClass="vsubmenu#{userSessionBean.selectedMenuItem.properties.vSubMenuClassSuffix}"
                           selectedStyleClass="vsubmenu_selected#{userSessionBean.selectedMenuItem.properties.vSubMenuClassSuffix}" 
                           unselectedStyleClass="vsubmenu_unselected#{userSessionBean.selectedMenuItem.properties.vSubMenuClassSuffix}">  

          <sf:outputText value="#{subitem.label}" rendered="#{subitem.rendered}"
            styleClass="text"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </sf:navigationMenu>          
          
      </sf:navigationMenu>
      
        <h:panelGrid columns="1" cellpadding="3"
                     rendered="#{userSessionBean.anonymousUser}"
                     styleClass="loginContainer">
          <h:outputText style="font-weight: bold" styleClass="userBar"
            value="#{webBundle.outputMessageNotConnected}" />
          <h:outputText value="#{webBundle.outputUsername}:" />
          <h:inputText binding="#{loginBean.usernameInputText}"
                       styleClass="loginBox" immediate="true"
                       tabindex="1" size="10" />
          <h:outputText value="#{webBundle.outputPassword}:" />
          <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                         styleClass="loginBox" size="10" 
                         onkeypress="login(event)" immediate="true"
                         tabindex="2"/>
          <sf:secureCommandLink action="#{loginBean.login}" 
            styleClass="loginButton" function="login" immediate="true"
            port="#{applicationBean.serverSecurePort}" scheme="https">
            <h:outputText value="#{webBundle.buttonSignin}" />
          </sf:secureCommandLink>

          <h:outputText value="#{loginBean.loginMessage}"
                        rendered="#{loginBean.loginMessage != null}"
                        styleClass="errorMessage" />

          <h:panelGroup style="line-height:8pt">
          <h:outputText rendered="#{userSessionBean.anonymousUser}"
            style="font-weight: bold" styleClass="userBar" 
            value="#{webBundle.outputMessageInfo}" />
            <h:commandLink action="register" styleClass="buttonLink"
                           style="font-size: 7pt">
              <h:outputText value="#{webBundle.outputCreateAccount}" />
            </h:commandLink>              
          </h:panelGroup>
        </h:panelGrid>
        
        <h:panelGrid rendered="#{!userSessionBean.anonymousUser}"
                     columns="1" styleClass="loginContainer" cellpadding="3">
                     
          <h:outputText value="#{webBundle.outputUsername}:"
                        style="text-align: left " />
          <h:outputText value="#{userSessionBean.displayName}"
                        style="text-align: right; font-weight: bold; color: red"/>

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
<!--
      <h:outputText value="#{webBundle.language}:" />
      <sf:languageSelector locales="#{userSessionBean.supportedLocales}" />
-->
    </h:panelGrid>

    <h:panelGrid id="contCol" styleClass="contTable" width="100%"
                 cellpadding="0" cellspacing="0" summary=""
                 columns="1" 
                 rowClasses="topmenuRow, navPathRow#{userSessionBean.selectedMenuItem.properties.navPathClassSuffix}, hmenuRow, contRow">

      <sf:navigationMenu id="topmenu"
                         var="item"
                         value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.properties.topMenuMid}"
                         rendered="#{userSessionBean.selectedMenuItem.properties.topMenuMid != null and
                           userSessionBean.selectedMenuItem.properties.topMenuMid != 'disabled'}"
                         orientation="horizontal"
                         styleClass="topmenu#{userSessionBean.selectedMenuItem.properties.topMenuClassSuffix}"
                         selectedStyleClass="topmenu_selected#{userSessionBean.selectedMenuItem.properties.topMenuClassSuffix}" 
                         unselectedStyleClass="topmenu_unselected#{userSessionBean.selectedMenuItem.properties.topMenuClassSuffix}">
         <h:graphicImage url="/documents/#{item.properties.topMenuImageId}" alt="#{item.label}" title="#{item.label}"
          rendered="#{item.rendered and item.properties.topMenuImageId != null}"
          onmouseover="javascript:this.src='/documents/#{(item.properties.topHoverMenuImageId != null ? item.properties.topHoverMenuImageId :item.properties.topMenuImageId)}'" 
          onmouseout="javascript:this.src='/documents/#{item.properties.topMenuImageId}'"/>
         
         <h:panelGroup rendered="#{item.rendered and item.properties.topMenuImageId == null}">
          <h:graphicImage value="/documents/#{item.properties.topMenuIconId}" alt=""
             styleClass="icon" width="32px" height="32px"
             rendered="#{item.properties.topMenuImageId == null 
                     and item.properties.topMenuIconId != null}" />
          <sf:outputText value="#{item.label}" styleClass="text"
            rendered="#{item.properties.topMenuImageId == null}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:panelGroup>         
      </sf:navigationMenu>
      
      <h:panelGroup id="topmenu_unrendered"
        rendered="#{userSessionBean.selectedMenuItem.properties.topMenuMid == null or
         userSessionBean.selectedMenuItem.properties.topMenuMid == 'disabled'}">
      </h:panelGroup>
      
      <h:panelGroup>
        <sf:navigationPath id="navPath"
                         var="item"
                         value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                         styleClass="navPath#{userSessionBean.selectedMenuItem.properties.navPathClassSuffix}"
                         rendered="#{userSessionBean.selectedMenuItem.properties.navPathClassSuffix != 'false'}">
          <f:facet name="separator">
            <h:outputText value="#{userSessionBean.selectedMenuItem.properties.separator == null ? 
              ' / ' : userSessionBean.selectedMenuItem.properties.separator}" rendered="#{item.depth != 3}" />
          </f:facet>
          <f:facet name="menuitem">
            <sf:outputText value="#{item.label}" rendered="#{item.depth != 3}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </f:facet>
        </sf:navigationPath>  
      </h:panelGroup>

      <h:panelGroup id="navPath_unrendered"
        rendered="#{userSessionBean.selectedMenuItem.properties.navPathClassSuffix == 'false'}">
      </h:panelGroup>

      <sf:navigationMenu id="hmenu"
                         var="item"
                         value="main"
                         orientation="horizontal"
                         layout="list"
                         rendered="#{(userSessionBean.selectedMenuItem.depth == 5 and
                           not userSessionBean.selectedMenuItem.leaf)
                           or userSessionBean.selectedMenuItem.depth >= 6}"
                         styleClass="hmenu#{userSessionBean.selectedMenuItem.properties.hMenuClassSuffix}"
                         selectedStyleClass="hmenu_selected#{userSessionBean.selectedMenuItem.properties.hMenuClassSuffix}" 
                         unselectedStyleClass="hmenu_unselected#{userSessionBean.selectedMenuItem.properties.hMenuClassSuffix}">
        <h:graphicImage url="/documents/#{item.properties.hMenuImageId}" alt="#{item.label}" 
          rendered="#{(item.directProperties['oc.objectBean'] != null or
                item.directProperties['oc.pageBean'] == null) and 
                item.rendered and 
                item.properties.hMenuImageId != null and
                (item.mid != userSessionBean.selectedMenuItem or item.properties.hMenuSelectedImageId == null)}"/>
        <h:graphicImage url="/documents/#{item.properties.hMenuSelectedImageId}" alt="#{item.label}" 
          rendered="#{(item.directProperties['oc.objectBean'] != null or
                item.directProperties['oc.pageBean'] == null) and 
                item.rendered and 
                item.properties.hMenuSelectedImageId != null and
                item.mid == userSessionBean.selectedMenuItem}"/>
                  
        <h:panelGroup rendered="#{(item.directProperties['oc.objectBean'] != null or
          item.directProperties['oc.pageBean'] == null) and
          item.rendered and item.properties.hMenuImageId == null}">
          <h:graphicImage value="/documents/#{item.properties.hMenuIconId}" alt=""
            styleClass="icon" width="32px" height="32px"
            rendered="#{item.properties.hMenuImageId == null
                     and item.properties.hMenuIconId != null}" />
          <sf:outputText value="#{item.label}" styleClass="text"
            rendered="#{item.properties.hMenuImageId == null}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:panelGroup>
      </sf:navigationMenu>

      <h:panelGroup id="hmenu_unrendered"
        rendered="#{not ((userSessionBean.selectedMenuItem.depth == 5 and
                           not userSessionBean.selectedMenuItem.leaf)
                           or userSessionBean.selectedMenuItem.depth >= 6)}">
      </h:panelGroup>

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
                             styleClass="rmenu#{userSessionBean.selectedMenuItem.properties.rMenuClassSuffix}"
                             baseMid="#{userSessionBean.selectedMenuItem.properties.rightMenuMid}"
                             selectedStyleClass="rmenu_item#{userSessionBean.selectedMenuItem.properties.rMenuClassSuffix}" 
                             unselectedStyleClass="rmenu_item#{userSessionBean.selectedMenuItem.properties.rMenuClassSuffix}">
            <h:graphicImage url="/documents/#{item.properties.rMenuImageId}" alt="#{item.label}" 
              rendered="#{item.properties.rMenuImageId != null and item.rendered}" />
    
            <h:panelGroup rendered="#{item.properties.rMenuImageId == null and item.rendered}">
              <h:graphicImage value="/documents/#{item.properties.rMenuIconId}" alt=""
                 styleClass="icon" width="32px" height="32px" 
                 rendered="#{item.properties.rMenuImageId == null 
                         and item.properties.rMenuIconId != null}" />
              <sf:outputText value="#{item.label}" styleClass="text"
                rendered="#{item.properties.rMenuImageId == null}"
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}" />
            </h:panelGroup>
                             
<!--             <h:graphicImage value="/templates/generic4/images/item.gif" 
               styleClass="icon" />
             <h:outputText value="#{item.label}" styleClass="text" /> -->
          </sf:navigationMenu>
        </h:panelGrid>
      </h:panelGrid>
    </h:panelGrid>

  </h:panelGrid>
</jsp:root>
