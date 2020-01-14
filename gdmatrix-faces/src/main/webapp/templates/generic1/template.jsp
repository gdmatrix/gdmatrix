<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:commandButton id="hiddenSubmitButton" style="display: none;" />

  <t:div id="mainLayout" styleClass="main">

    <t:div styleClass="header">  
      <h:outputText value="&lt;header id='gen1_header' aria-label='#{webBundle.interiorHeader}'&gt;" escape="false"/>              
      <t:div styleClass="headerTable">
        <t:div styleClass="row1">
          <t:div styleClass="column1" rendered="#{userSessionBean.anonymousUser}">
            <h:outputLabel for="inputUsername" value="#{webBundle.outputUsername}:"
                           style="margin-right:4px" styleClass="loginText" />
            <h:inputText id="inputUsername" binding="#{loginBean.usernameInputText}"
                         size="10" immediate="true" 
                         onkeypress="login(event)"
                         styleClass="loginBox" />
            <h:outputLabel for="inputPassword" value="#{webBundle.outputPassword}:"
                           style="margin-left:4px; margin-right:4px" 
                           styleClass="loginText" />
            <h:inputSecret id="inputPassword" binding="#{loginBean.passwordInputSecret}"
                           size="10" immediate="true" 
                           onkeypress="login(event)"
                           styleClass="loginBox" />
            <sf:secureCommandLink id="loginbutton" style="margin-left:4px"
                                  action="#{loginBean.login}" function="login" immediate="true"
                                  port="#{applicationBean.serverSecurePort}" scheme="https">
              <h:graphicImage url="/templates/generic1/images/button.gif" alt="login"
                              style="border-style:none;vertical-align:middle" />
            </sf:secureCommandLink>
          </t:div>

          <t:div styleClass="column2" rendered="#{not userSessionBean.anonymousUser}">
            <h:outputText value="#{webBundle.outputUsername}:"
                          styleClass="loginText" />
            <h:outputText value="#{userSessionBean.displayName}" 
                          styleClass="displayNameText" style="margin-left:4px" />
          </t:div>

          <t:div styleClass="column3">   
            <sf:clock />
          </t:div>
        </t:div>
        <t:div styleClass="footer">
          <t:div styleClass="footerTable">
            <t:div styleClass="fcol1">
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
            </t:div>
            <t:div styleClass="fcol2"></t:div>
          </t:div>
        </t:div>
      </t:div>        
      <h:outputText value="&lt;/header&gt;" escape="false"/>              
    </t:div>                
    <t:div styleClass="mainRow">
      <t:div styleClass="menuCol">
        <h:outputText value="&lt;nav id='interior_nav_left' aria-label='#{webBundle.leftMenu}'&gt;" escape="false"/>        
        <t:div styleClass="menuColTable" id="menuCol">
          <h:graphicImage alt="" 
                          styleClass="logo"
                          url="/documents/#{userSessionBean.selectedMenuItem.properties.imageId}" />
          <sf:navigationMenu id="vmenu"
                             var="item"
                             value="main"
                             baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                             orientation="vertical"
                             styleClass="vmenu"
                             selectedStyleClass="vmenu_selected" 
                             unselectedStyleClass="vmenu_unselected"
                             layout="LIST">        
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

          <t:div styleClass="langSelect">
            <h:outputLabel for="langSelector" value="#{webBundle.language}:" />
            <sf:languageSelector id="langSelector" locales="#{userSessionBean.supportedLocales}" />
          </t:div>
        </t:div>
        <h:outputText value="&lt;/nav&gt;" escape="false"/>      
      </t:div>  
      <t:div styleClass="contCol">
        <t:div id="contCol" styleClass="contTable">
          <t:div styleClass="navPathRow">
            <t:div>  
              <h:outputText value="&lt;nav id='interior_nav_path' aria-label='#{webBundle.navigationPath}'&gt;" escape="false"/>                
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
              <h:outputText value="&lt;/nav&gt;" escape="false"/>                
            </t:div>            
          </t:div>

          <t:div styleClass="hmenuRow">  
            <h:outputText value="&lt;nav id='interior_nav_hmenu' aria-label='#{webBundle.topMenu}'&gt;" escape="false"/>                          
            <h:panelGroup>
              <sf:navigationMenu id="hmenu"
                                 var="item"
                                 value="main"
                                 orientation="horizontal"
                                 rendered="#{
                                 (userSessionBean.selectedMenuItem.depth > 3 || 
                                   (userSessionBean.selectedMenuItem.childCount > 0 and 
                                   userSessionBean.selectedMenuItem.depth == 3)) and
                                   userSessionBean.selectedMenuItem.parent.mid !=
                                   userSessionBean.selectedMenuItem.properties.rightMenuMid and
                                   userSessionBean.selectedMenuItem.mid !=
                                   userSessionBean.selectedMenuItem.properties.rightMenuMid}"
                                 styleClass="hmenu"
                                 selectedStyleClass="hmenu_selected" 
                                 unselectedStyleClass="hmenu_unselected"
                                 layout="LIST">
                <sf:outputText value="#{item.label}" style="vertical-align:middle"
                               rendered="#{(item.directProperties['oc.objectBean'] != null or
                                           item.directProperties['oc.pageBean'] == null) and 
                                           item.rendered}"
                               translator="#{userSessionBean.translator}"
                               translationGroup="#{userSessionBean.translationGroup}" />
              </sf:navigationMenu>
            </h:panelGroup>
            <h:outputText value="&lt;/nav&gt;" escape="false"/>                          
          </t:div>          

          <t:div styleClass="contRow">
            <t:div styleClass="content">
              <t:div styleClass="ccol1">  
                <h:outputText value="&lt;main id='sf_main_content' tabindex='-1'&gt;" escape="false"/>                
                <h:panelGroup>
                  <jsp:include page="${requestScope['_body']}"/>
                </h:panelGroup>        
                <h:outputText value="&lt;/main&gt;" escape="false"/>          
              </t:div>        
              <t:div styleClass="ccol2">  
                <h:outputText value="&lt;nav id='interior_nav_right' aria-label='#{webBundle.rightMenu}'&gt;" escape="false"/>        
                <t:div styleClass="rightMenuPanel"  
                       rendered="#{userSessionBean.selectedMenuItem.properties.rightMenuMid != null and
                                   userSessionBean.selectedMenuItem.properties.rightMenuMid != 'disabled'}">
                  <t:div styleClass="title">
                    <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.rightMenuTitle}"
                                   translator="#{userSessionBean.translator}"
                                   translationGroup="#{userSessionBean.translationGroup}" />
                  </t:div>
                  <t:div styleClass="menu">
                    <sf:navigationMenu id="rmenu"
                                       var="item"
                                       value="main"
                                       orientation="vertical"
                                       styleClass="rmenu"
                                       baseMid="#{userSessionBean.selectedMenuItem.properties.rightMenuMid}"
                                       selectedStyleClass="rmenu_item" 
                                       unselectedStyleClass="rmenu_item"
                                       layout="LIST">
                      <h:graphicImage alt="" value="/templates/generic1/images/item.gif" 
                                      rendered="#{item.rendered}" styleClass="icon" />
                      <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
                                     styleClass="text"
                                     translator="#{userSessionBean.translator}"
                                     translationGroup="#{userSessionBean.translationGroup}" />
                    </sf:navigationMenu>
                  </t:div>
                </t:div>
                <h:outputText value="&lt;/nav&gt;" escape="false"/>        
              </t:div>            

            </t:div>
          </t:div>
        </t:div>
      </t:div>
    </t:div>
  </t:div>

</jsp:root>
