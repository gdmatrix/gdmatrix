<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:commandButton id="hiddenSubmitButton" style="display: none;" />      

  <h:panelGrid id="mainLayout" columns="2" 
               styleClass="main" 
               rowClasses="mainRow"
               columnClasses="menuCol,contCol" 
               headerClass="header"
               footerClass="footer"
               summary="">

    <f:facet name="header">
      <h:panelGrid columns="1" styleClass="headerTable">
        <h:graphicImage style="border-style:none;margin:0px;padding:0px" width="100%"
          url="/documents/#{userSessionBean.selectedMenuItem.properties.headerImageId}"/>      
      </h:panelGrid>        
    </f:facet>
    
    <t:div>  
    <h:outputText value="&lt;nav id='interior_nav_left' aria-label='#{webBundle.leftMenu}'&gt;" escape="false"/>      
      
    <h:panelGrid id="menuCol" columns="1" summary="" styleClass="menuColTable">
     <h:graphicImage 
       url="/documents/#{userSessionBean.selectedMenuItem.properties.logoImageId}" 
       width="100%" 
       rendered="#{userSessionBean.selectedMenuItem.properties.logoImageId != null}"/>   

      <sf:navigationMenu id="vmenu"
                         var="item"
                         value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                         orientation="vertical"
                         styleClass="vmenu#{userSessionBean.selectedMenuItem.properties.vMenuClassSuffix}"
                         selectedStyleClass="vmenu_selected#{userSessionBean.selectedMenuItem.properties.vMenuClassSuffix}" 
                         unselectedStyleClass="vmenu_unselected#{userSessionBean.selectedMenuItem.properties.vMenuClassSuffix}"
                         layout="LIST">  
        
        <h:graphicImage url="/documents/#{item.properties.vMenuImageId}" alt="#{item.label}" 
          rendered="#{item.rendered 
                  and item.properties.vMenuImageId != null
                  and (item.mid != userSessionBean.selectedMenuItem.cursorPath[2] 
                   or item.properties.vMenuSelectedImageId == null)}"/>
        <h:graphicImage url="/documents/#{item.properties.vMenuSelectedImageId}" alt="#{item.label}" 
          rendered="#{item.rendered 
                  and item.properties.vMenuSelectedImageId != null
                  and item.mid == userSessionBean.selectedMenuItem.cursorPath[2]}"/>

        <h:panelGroup rendered="#{item.properties.vMenuImageId == null 
          and item.rendered}">
          <h:graphicImage value="/documents/#{item.properties.vMenuIconId}" alt=""
             styleClass="icon" width="32px" height="32px" 
             rendered="#{item.properties.vMenuImageId == null 
                     and item.properties.vMenuIconId != null}"
              />
          <sf:outputText value="#{item.label}" styleClass="text"
            rendered="#{item.properties.vMenuImageId == null
                  and item.rendered}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:panelGroup>
          
      </sf:navigationMenu>
      
        <h:panelGrid columns="1" 
                     rendered="#{userSessionBean.anonymousUser}"
                     styleClass="loginContainer">
          <h:outputText style="font-weight: bold" styleClass="userBar"
            value="#{webBundle.outputMessageNotConnected}" />
          <h:outputLabel for="inputUsername" value="#{webBundle.outputUsername}:" />
          <h:inputText id="inputUsername" binding="#{loginBean.usernameInputText}"
                       styleClass="loginBox" immediate="true"
                       size="10" />
          <h:outputLabel for="inputPassword" value="#{webBundle.outputPassword}:" />
          <h:inputSecret id="inputPassword" binding="#{loginBean.passwordInputSecret}"
                         styleClass="loginBox" size="10" 
                         onkeypress="login(event)" immediate="true" />
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

      <h:outputLabel for="langSelector" value="#{webBundle.language}:" />
      <sf:languageSelector id="langSelector" locales="#{userSessionBean.supportedLocales}" />
        
    </h:panelGrid>   
      
    <h:outputText value="&lt;/nav&gt;" escape="false"/>      
    </t:div>       
      
    <h:panelGrid id="contCol" styleClass="contTable" summary=""
                 columns="1" 
                 rowClasses="topmenuRow, navPathRow#{userSessionBean.selectedMenuItem.properties.navPathClassSuffix}, hmenuRow, contRow">
      
      <t:div>  
      <h:outputText value="&lt;nav id='interior_nav_hmenu' aria-label='#{webBundle.topMenu}'&gt;" escape="false"/>                          
      
      <sf:navigationMenu id="topmenu"
                         var="item"
                         value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].firstChild.mid}"
                         orientation="horizontal"
                         styleClass="topmenu#{userSessionBean.selectedMenuItem.properties.topMenuClassSuffix}"
                         selectedStyleClass="topmenu_selected#{userSessionBean.selectedMenuItem.properties.topMenuClassSuffix}" 
                         unselectedStyleClass="topmenu_unselected#{userSessionBean.selectedMenuItem.properties.topMenuClassSuffix}"
                         layout="LIST">
         <h:graphicImage url="/documents/#{item.properties.topMenuImageId}"
          alt="#{item.label}" title="#{item.label}"
          rendered="#{item.properties.topMenuImageId != null and item.rendered}"
          onmouseover="javascript:this.src='/documents/#{(item.properties.topHoverMenuImageId != null ? item.properties.topHoverMenuImageId :item.properties.topMenuImageId)}'" 
          onmouseout="javascript:this.src='/documents/#{item.properties.topMenuImageId}'"/>
         
         <h:panelGroup rendered="#{item.properties.topMenuImageId == null and item.rendered}">
          <h:graphicImage value="/documents/#{item.properties.topMenuIconId}" alt=""
             styleClass="icon" width="32px" height="32px"
             rendered="#{item.properties.topMenuImageId == null 
                     and item.properties.topMenuIconId != null}"
              />
          <sf:outputText value="#{item.label}" styleClass="text"
            rendered="#{item.properties.topMenuImageId == null}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:panelGroup>
         
      </sf:navigationMenu>
      
      <h:outputText value="&lt;/nav&gt;" escape="false"/>                          
      </t:div>                  
        
      <t:div>  
      <h:outputText value="&lt;nav id='interior_nav_path' aria-label='#{webBundle.navigationPath}'&gt;" escape="false"/>                
        
      <h:panelGroup>
        <sf:navigationPath id="navPath"
                         var="item"
                         value="main"
                         baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                         styleClass="navPath#{userSessionBean.selectedMenuItem.properties.navPathClassSuffix}"
                         rendered="#{userSessionBean.selectedMenuItem.properties.navPathClassSuffix != 'false'}">
          <f:facet name="separator">
            <h:outputText value=" | " />
          </f:facet>
          <f:facet name="menuitem">
            <sf:outputText value="#{item.label}" rendered="#{item.depth != 3 or item.childIndex > 1}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </f:facet>
        </sf:navigationPath>  
      </h:panelGroup>

      <h:outputText value="&lt;/nav&gt;" escape="false"/>                          
      </t:div>                            
         
      <t:div>  
      <h:outputText value="&lt;nav id='interior_nav_hmenu_2' aria-label='#{webBundle.topMenu} 2'&gt;" escape="false"/>                                    
          
      <h:panelGroup>
        <sf:navigationMenu id="hmenu"
                           var="item"
                           value="main"
                           orientation="horizontal"
                           rendered="#{userSessionBean.selectedMenuItem.depth > 3 || 
                           (userSessionBean.selectedMenuItem.childCount > 0 and 
                           userSessionBean.selectedMenuItem.depth == 3)}"
                           styleClass="hmenu#{userSessionBean.selectedMenuItem.properties.hMenuClassSuffix}"
                           selectedStyleClass="hmenu_selected#{userSessionBean.selectedMenuItem.properties.hMenuClassSuffix}" 
                           unselectedStyleClass="hmenu_unselected#{userSessionBean.selectedMenuItem.properties.hMenuClassSuffix}"
                           layout="LIST">
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
                         and item.properties.hMenuIconId != null}"
                  />
             <sf:outputText value="#{item.label}" styleClass="text"
                rendered="#{item.properties.hMenuImageId == null 
                      and item.rendered}"
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}" />
           </h:panelGroup>                
        </sf:navigationMenu>
      </h:panelGroup>

      <h:outputText value="&lt;/nav&gt;" escape="false"/>                          
      </t:div>            
           
      <h:panelGrid columns="2" summary=""
        styleClass="content"
        columnClasses="ccol1, ccol2">

        <t:div>  
        <h:outputText value="&lt;main id='sf_main_content' tabindex='-1'&gt;" escape="false"/>                
        
        <h:panelGroup>
          <jsp:include page="${requestScope['_body']}"/>
        </h:panelGroup>
        
        <h:outputText value="&lt;/main&gt;" escape="false"/>          
        </t:div>                
        
        <t:div>  
        <h:outputText value="&lt;nav id='interior_nav_right' aria-label='#{webBundle.rightMenu}'&gt;" escape="false"/>                
        
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
                             unselectedStyleClass="rmenu_item#{userSessionBean.selectedMenuItem.properties.rMenuClassSuffix}"
                             layout="LIST">
            <h:graphicImage url="/documents/#{item.properties.rMenuImageId}" alt="#{item.label}" 
              rendered="#{item.properties.rMenuImageId != null and item.rendered}"/>
    
            <h:panelGroup rendered="#{item.properties.rMenuImageId == null and item.rendered}">
              <h:graphicImage value="/documents/#{item.properties.rMenuIconId}" alt=""
                 styleClass="icon" width="32px" height="32px" 
                 rendered="#{item.properties.rMenuImageId == null 
                         and item.properties.rMenuIconId != null}"
                  />
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
          
        <h:outputText value="&lt;/nav&gt;" escape="false"/>        
        </t:div>            
          
      </h:panelGrid>
    </h:panelGrid>         
          
  </h:panelGrid>             
</jsp:root>
