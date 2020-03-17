<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:c="http://java.sun.com/jsp/jstl/core">
  
  <script type="text/javascript">
    <![CDATA[  

      var showSiblingsMenu = false;
      var showChildrenMenu = false;
      
      function switchTopDiv(divId)
      {
        var cookieValue = getCookieValue('mobile2_' + divId);
        if (cookieValue == "true")
        {
          hideTopDiv(divId);
        }
        else //show
        {
          showTopDiv(divId);
        }        
      }
      
      function showTopDiv(divId)
      {        
        document.getElementById(divId).style.display='block';
        if (divId == 'profileDiv')
        {
          document.getElementById('showProfileLinkBg').className = 'showProfileLinkBg selected';
          document.getElementsByClassName('showProfileLink')[0].title = '${webBundle.hideProfilePanel}';             
          hideTopDiv('searchDiv');
        }
        else if (divId == 'searchDiv')
        {
          document.getElementsByClassName('showSearchLink')[0].className = 'showSearchLink selected';
          document.getElementsByClassName('showSearchLink')[0].title = '${webBundle.hideGlobalSearch}';             
          hideTopDiv('profileDiv');
        }
        setCookieValue('mobile2_' + divId, 'true');
      }
      
      function hideTopDiv(divId)
      {
        document.getElementById(divId).style.display='none';
        if (divId == 'profileDiv')
        {
          document.getElementById('showProfileLinkBg').className='showProfileLinkBg';        
          document.getElementsByClassName('showProfileLink')[0].title = '${webBundle.showProfilePanel}';             
        }
        else if (divId == 'searchDiv')
        {
          document.getElementsByClassName('showSearchLink')[0].className='showSearchLink';        
          document.getElementsByClassName('showSearchLink')[0].title = '${webBundle.showGlobalSearch}';             
        }
        setCookieValue('mobile2_' + divId, 'false');
      }
            
      function initTopDivs()
      {
        initTopDiv('profileDiv');
        //initTopDiv('searchDiv');
        hideTopDiv('searchDiv'); // Always hide the search div. Ignore cookie.
      }
      
      function initTopDiv(divId)
      {
        var cookieValue = getCookieValue('mobile2_' + divId);
        if (cookieValue == null || cookieValue == '')
        {
          setCookieValue('mobile2_' + divId, 'false');
          cookieValue = "false";
        }        
        if (cookieValue == "true")
        {
          showTopDiv(divId);
        }
        else
        {
          hideTopDiv(divId);
        }        
      }

      function switchSiblingsMenuDiv()
      {        
        if (showSiblingsMenu == true) //hide
        {
          hideSiblingsMenuDiv();          
        }
        else //show
        {
          showSiblingsMenuDiv();          
        }
      }
  
      function showSiblingsMenuDiv()
      {
        hideChildrenMenuDiv();
        if (document.getElementById('siblingsMenuDiv') != null)
        {
          document.getElementById('siblingsMenuDiv').style.display='block';
          if (document.getElementsByClassName('showSiblingsMenuLink').
            length > 0)
          {
            document.getElementsByClassName('showSiblingsMenuLink')[0].title = 
              '${webBundle.hideSiblingsMenu}';
          }
        }
        if (document.getElementById('interiorContainerDiv') != null)
        {
          document.getElementById('interiorContainerDiv').style.display='block';
        }
        document.getElementById('titleBar').className='titleBar open';
        showSiblingsMenu = true;
      }
      
      function hideSiblingsMenuDiv()
      {
        if (document.getElementById('siblingsMenuDiv') != null)
        {      
          document.getElementById('siblingsMenuDiv').style.display='none';
          if (document.getElementsByClassName('showSiblingsMenuLink').
            length > 0)
          {
            document.getElementsByClassName('showSiblingsMenuLink')[0].title = 
              '${webBundle.showSiblingsMenu}';
          }
        }
        if (document.getElementById('interiorContainerDiv') != null)
        {
          document.getElementById('interiorContainerDiv').style.display='none';
        }
        document.getElementById('titleBar').className='titleBar';
        showSiblingsMenu = false;
      }

      function switchChildrenMenuDiv()
      {        
        if (showChildrenMenu == true) //hide
        {
          hideChildrenMenuDiv();          
        }
        else //show
        {
          showChildrenMenuDiv();          
        }
      }
  
      function showChildrenMenuDiv()
      {
        hideSiblingsMenuDiv();
        if (document.getElementById('childrenMenuDiv') != null)
        {        
          document.getElementById('childrenMenuDiv').style.display='block';
          if (document.getElementsByClassName('showChildrenMenuLink').
            length > 0)
          {          
            document.getElementsByClassName('showChildrenMenuLink')[0].title = 
              '${webBundle.hideChildrenMenu}';
          }
        }
        if (document.getElementById('interiorContainerDiv') != null)
        {        
          document.getElementById('interiorContainerDiv').style.display='block';
        }
        if (document.getElementById('showChildrenMenuDiv'))
        {
          document.getElementById('showChildrenMenuDiv').className='showChildrenMenuDiv open';
        }
        document.getElementById('titleBar').className='titleBar open';
        showChildrenMenu = true;
      }
      
      function hideChildrenMenuDiv()
      {
        if (document.getElementById('childrenMenuDiv') != null)
        {
          document.getElementById('childrenMenuDiv').style.display='none';
          if (document.getElementsByClassName('showChildrenMenuLink').
            length > 0)
          {
            document.getElementsByClassName('showChildrenMenuLink')[0].title = 
              '${webBundle.showChildrenMenu}';                              
          }
        }
        if (document.getElementById('interiorContainerDiv') != null)
        {        
          document.getElementById('interiorContainerDiv').style.display='none';
        }
        if (document.getElementById('showChildrenMenuDiv'))
        {
          document.getElementById('showChildrenMenuDiv').className='showChildrenMenuDiv';
        }        
        document.getElementById('titleBar').className='titleBar';
        showChildrenMenu = false;
      }
      
      initTopDivs();
      
    ]]>
  </script>

  <t:div styleClass="mobile">
    
    <h:outputText value="&lt;header id='wp2_mobile_header' aria-label='#{webBundle.mainHeader}'&gt;" escape="false"/> 
    
    <t:div styleClass="topBar">    
      <t:div styleClass="logoDiv">
        <sf:outputLink styleClass="logoLink"
          value="#{userSessionBean.selectedMenuItem.properties['mobile2.pantoneUrl']}"
          title="#{userSessionBean.selectedMenuItem.properties['mobile2.pantoneTitle'] == null ?
            'Inici' : userSessionBean.selectedMenuItem.properties['mobile2.pantoneTitle']}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}">          
        </sf:outputLink>
      </t:div>
      <t:div id="showProfileLinkBg" forceId="true" styleClass="showProfileLinkBg">
        <h:outputLink id="showProfileLink" title="#{webBundle.showProfilePanel}"
                       styleClass="showProfileLink#{userSessionBean.anonymousUser ? '' : ' logged'}"
                       onclick="switchTopDiv('profileDiv')" value="#">
        </h:outputLink>
      </t:div>
      <h:outputLink id="showSearchLink" title="#{webBundle.showGlobalSearch}" styleClass="showSearchLink"
                     onclick="switchTopDiv('searchDiv')" value="#">              
      </h:outputLink>
    </t:div>

    <t:div id="profileDiv" forceId="true" styleClass="profileDiv" style="display:none;">    
      <sf:heading level="2" styleClass="element-invisible">
        <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.identificationMessage}"
                       translator="#{userSessionBean.translator}"
                       translationGroup="#{userSessionBean.translationGroup}" />
      </sf:heading>      
      <t:div rendered="#{userSessionBean.anonymousUser}" styleClass="loginArea">

        <t:div styleClass="loginUserDiv">
          <t:div styleClass="labelDiv">
            <h:outputLabel for="userBox"
                        value="#{webBundle.outputUsername}" />            
          </t:div>
          <t:div styleClass="inputTextDiv">
            <h:inputText binding="#{loginBean.usernameInputText}"
                        tabindex="1" size="10" immediate="true"
                        onkeypress="login(event)"                      
                        id="userBox" />
          </t:div>
        </t:div>

        <t:div styleClass="loginPasswordDiv">         
          <t:div styleClass="labelDiv">
            <h:outputLabel for="passwordBox"
                           value="#{webBundle.outputPassword}" />
          </t:div>
          <t:div styleClass="inputTextDiv">
            <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                          tabindex="2" size="10" immediate="true"
                          onkeypress="login(event)"                        
                          id="passwordBox" />
          </t:div>
        </t:div>
                
        <t:div styleClass="loginButtonDiv password">
          <sf:secureCommandLink id="loginbutton" tabindex="3" 
            styleClass="loginButton"
            action="#{loginBean.login}" function="login" immediate="true"
            port="#{applicationBean.serverSecurePort}" scheme="https">
            <h:outputText value="#{webBundle.buttonSignin}" />
          </sf:secureCommandLink>
        </t:div>

        <t:div styleClass="loginButtonDiv certificate">
          <sf:secureCommandLink
            action="#{loginBean.loginCertificate}" immediate="true" tabindex="4"
            scheme="https" port="#{applicationBean.clientSecurePort}" styleClass="loginButton">
            <h:outputText value="#{webBundle.certificateAuthentication}"  />
          </sf:secureCommandLink>          
        </t:div>

        <t:div styleClass="loginButtonDiv valid"
          rendered="#{(userSessionBean.intranetUser or userSessionBean.selectedMenuItem.properties.login_valid_enabled == 'true')}">
          <h:outputLink value="#{validBean.loginURL}" tabindex="5" styleClass="loginButton">
            <h:outputText value="VALid"  />
          </h:outputLink>   
        </t:div>

        <t:div styleClass="loginButtonDiv mobileid"
          rendered="#{(userSessionBean.intranetUser or userSessionBean.selectedMenuItem.properties.login_mobileid_enabled == 'true')}">
          <h:outputLink value="#{mobileIdBean.loginURL}" tabindex="5" styleClass="loginButton">
            <h:outputText value="MobileID"  />
          </h:outputLink>
        </t:div>
                
        <t:div rendered="#{loginBean.loginMessage != null}" styleClass="errorMessageDiv" id="errorMessageDiv" forceId="true">
          <h:outputText value="#{loginBean.loginMessage}" />
        </t:div>        
      </t:div>
      <t:div rendered="#{not userSessionBean.anonymousUser}" styleClass="logoutArea">
        <t:div styleClass="welcomeMessageDiv">
          <h:outputText value="#{webBundle.welcome}, #{userSessionBean.displayName}" />
        </t:div>
        <t:div styleClass="logoutButtonDiv">
          <h:commandLink action="#{loginBean.logout}" title="#{webBundle.buttonSignout}"
             immediate="true" styleClass="logoutButton">
            <h:outputText value="#{webBundle.buttonSignout}" />
          </h:commandLink>
        </t:div>        
        <t:div styleClass="intranetButtonDiv" rendered="#{userSessionBean.intranetUser}">
          <h:outputLink value="/intranet" styleClass="intranetButton">
            <h:outputText value="#{webBundle.intranet}" />
          </h:outputLink>          
        </t:div>
      </t:div>      
    </t:div>
    
    <t:div id="searchDiv" forceId="true" styleClass="searchDiv" style="display:none;">
      <sf:heading level="2" styleClass="element-invisible">
        <h:outputText value="#{webBundle.globalSearch}" />          
      </sf:heading>          
      <t:div>
        <t:inputText id="checkedTextField" title="#{webBundle.globalSearch}" 
                     forceId="true" styleClass="searchText"
                     onkeypress="inputTextKeyCheck(event, 'mainform:templateSearchButton');"
                     value="#{webSearchBean.outerWords}" />                
      </t:div>
      <sf:commandButton id="templateSearchButton" styleClass="searchButton"
         value=""                          
         action="#{webSearchBean.outerSearch}"
         translator="#{userSessionBean.translator}"
         translationGroup="#{userSessionBean.translationGroup}" />
    </t:div>

    <t:div id="titleBar" styleClass="titleBar" forceId="true">      
      <t:div styleClass="moveUpDiv">
        <t:div id="moveUpMenuLink" styleClass="titleMenuLink" forceId="true"             
             rendered="#{(not (userSessionBean.selectedMenuItem.parentWithAction.mid == null and 
                         userSessionBean.selectedMenuItem.directProperties['mobile2.moveUpMid'] == null))}">
          <h:outputLink value="/go.faces?xmid=#{userSessionBean.selectedMenuItem.directProperties['mobile2.moveUpMid'] == null ? 
                                              userSessionBean.selectedMenuItem.parentWithAction.mid : 
                                                userSessionBean.selectedMenuItem.directProperties['mobile2.moveUpMid']}"
                         title="#{webBundle.moveUp}">
          </h:outputLink>
        </t:div>
      </t:div>
      <t:div styleClass="titleDiv">
        <h:outputLink id="showSiblingsMenuLink" value="#"
                       styleClass="showSiblingsMenuLink"
               onclick="switchSiblingsMenuDiv()"
               rendered="#{userSessionBean.selectedMenuItem.rendered and ((userSessionBean.selectedMenuItem.properties.interiorWidgetRender == 'true' and userSessionBean.selectedMenuItem.properties.interiorWidgetLayout != null) or 
                (userSessionBean.selectedMenuItem.anyRenderedSibling))}"
               title="#{webBundle.showSiblingsMenu}">
          <t:div styleClass="showSiblingsIcon"></t:div>
          <sf:outputText value="#{userSessionBean.selectedMenuItem.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:outputLink>
        <sf:outputText value="#{userSessionBean.selectedMenuItem.label}"      
                       translator="#{userSessionBean.translator}"
                       translationGroup="#{userSessionBean.translationGroup}" 
                       rendered="#{not (userSessionBean.selectedMenuItem.rendered and ((userSessionBean.selectedMenuItem.properties.interiorWidgetRender == 'true' and userSessionBean.selectedMenuItem.properties.interiorWidgetLayout != null) or 
                                         (userSessionBean.selectedMenuItem.anyRenderedSibling)))}" />
      </t:div>
      <t:div id="showChildrenMenuDiv" forceId="true" styleClass="showChildrenMenuDiv">        
        <h:outputLink value="#" id="showChildrenMenuLink" styleClass="showChildrenMenuLink titleMenuLink"
               onclick="switchChildrenMenuDiv()"
               rendered="#{userSessionBean.selectedMenuItem.anyRenderedChildren}"
               title="#{webBundle.showChildrenMenu}">
        </h:outputLink>
      </t:div>
      <t:div style="clear:both;" />
    </t:div>
    <h:outputText value="&lt;/header&gt;" escape="false"/>
       
    <t:div id="siblingsMenuDiv" forceId="true" styleClass="titleMenuDiv" style="display:none" 
           rendered="#{userSessionBean.selectedMenuItem.anyRenderedSibling}">
      <h:outputText value="&lt;nav id='wp2_mobile_nav_siblings' aria-label='#{webBundle.mobileSiblingsMenu}'&gt;" escape="false"/> 
      <sf:heading level="2" styleClass="element-invisible">
        <h:outputText value="#{webBundle.mobileSiblingsMenu}" />
      </sf:heading>                    
      <sf:treeMenu styleClass="menu" var="item"
        baseMid="#{userSessionBean.selectedMenuItem.parent.mid}"
        expandDepth="1" headingsRender="true" headingsBaseLevel="3">
        <f:facet name="data">          
          <t:div styleClass="#{(item.mid == userSessionBean.selectedMid) ? 'menuItemDiv selected' : 'menuItemDiv'}" 
                 rendered="#{item.rendered}">
            <t:div styleClass="iconDiv" rendered="#{item.properties.mobileIcon != null}">
              <h:graphicImage url="#{item.properties.mobileIcon}" alt="#{item.label}" />              
            </t:div>
            <t:div styleClass="textDiv">
              <sf:outputLink value="#{item.actionURL}"
                onclick="#{item.onclick}" target="#{item.target}"
                ariaLabel="#{item.directProperties.ariaLabel}"
                ariaHidden="#{item.directProperties.ariaHidden == 'true'}"                
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}">                
                <sf:outputText value="#{item.label}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" 
                  styleClass="title" />
                <sf:outputText value="#{item.directProperties.mobileDescr}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" 
                  styleClass="description" />              
              </sf:outputLink>
            </t:div>
          </t:div>
        </f:facet>
      </sf:treeMenu>
      <h:outputText value="&lt;/nav&gt;" escape="false"/>                    
    </t:div>      

    <t:div id="childrenMenuDiv" forceId="true" styleClass="titleMenuDiv" style="display:none" 
           rendered="#{userSessionBean.selectedMenuItem.anyRenderedChildren}">
      <h:outputText value="&lt;nav id='wp2_mobile_nav_children' aria-label='#{webBundle.mobileChildrenMenu}'&gt;" escape="false"/>       
      <sf:heading level="2" styleClass="element-invisible">
        <h:outputText value="#{webBundle.mobileChildrenMenu}" />
      </sf:heading>                          
      <sf:treeMenu styleClass="menu" var="item"
        baseMid="#{userSessionBean.selectedMid}"
        expandDepth="1" headingsRender="true" headingsBaseLevel="3">
        <f:facet name="data">          
          <t:div styleClass="menuItemDiv" rendered="#{item.rendered}">
            <t:div styleClass="iconDiv" rendered="#{item.properties.mobileIcon != null}">
              <h:graphicImage url="#{item.properties.mobileIcon}" alt="#{item.label}" />              
            </t:div>
            <t:div styleClass="textDiv">
              <sf:outputLink value="#{item.actionURL}"
                onclick="#{item.onclick}" target="#{item.target}"
                ariaLabel="#{item.directProperties.ariaLabel}"
                ariaHidden="#{item.directProperties.ariaHidden == 'true'}"                
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}">                
                <sf:outputText value="#{item.label}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" 
                  styleClass="title" />
                <sf:outputText value="#{item.directProperties.mobileDescr}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" 
                  styleClass="description" />              
              </sf:outputLink>
            </t:div>
          </t:div>
        </f:facet>
      </sf:treeMenu>
      <h:outputText value="&lt;/nav&gt;" escape="false"/>                  
    </t:div>      
                  
    <t:div id="interiorContainerDiv" forceId="true"
           rendered="#{widgetBean.interiorWidgetRender and 
                       userSessionBean.selectedMenuItem.properties.interiorWidgetLayout != null}"
           styleClass="widgetContainerLayer" style="display:none">
      <h:outputText value="&lt;nav id='wp2_mobile_wcont' aria-label='#{webBundle.interiorWidgetsMenu}'&gt;" escape="false"/>      
      <sf:heading level="2" styleClass="element-invisible">
        <h:outputText escape="false" value="#{webBundle.interiorWidgetsMenu}" />                                    
      </sf:heading>
      <sf:widgetContainer
        id="interior_container"
        binding="#{widgetBean.interiorContainer}"
        columns="#{widgetBean.interiorColumns}"
        columnClasses="#{widgetBean.interiorColumnClasses}"
        columnTitles="#{widgetBean.interiorColumnTitles}"
        columnRenderAsList="#{widgetBean.interiorColumnRenderAsList}"
        layout="#{widgetBean.interiorLayout}"
        dynamic="false"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}"                  
        title="#{widgetBean.interiorTitle}"
        headerDocId="#{widgetBean.interiorHeaderDocId}"
        footerDocId="#{widgetBean.interiorFooterDocId}"
        styleClass="widget_container" /> 
      <h:outputText value="&lt;/nav&gt;" escape="false"/>      
    </t:div>      
                  
    <h:outputText value="&lt;main id='wp2_mobile_main' tabindex='-1' style='outline: 0;'&gt;" escape="false"/>        

    <c:if test="${userSessionBean.selectedMenuItem.properties.action != '#{documentBean.loadDocument}'}">
      <sf:heading rendered="#{userSessionBean.selectedMenuItem.properties.interiorHeaderRender == 'true'}" 
                  level="1" styleClass="element-invisible">
        <sf:outputText escape="false" value="#{userSessionBean.selectedMenuItem.properties.interiorHeaderHeadLine}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}"
          rendered="#{userSessionBean.selectedMenuItem.properties.interiorHeaderHeadLine != null}" />
        <sf:outputText escape="false" value=" - #{userSessionBean.selectedMenuItem.properties.interiorHeaderSummary}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}"
          rendered="#{userSessionBean.selectedMenuItem.properties.interiorHeaderSummary != null}" />
      </sf:heading>
      <sf:heading rendered="#{userSessionBean.selectedMenuItem.properties.interiorHeaderRender != 'true'}" 
                  level="1" styleClass="element-invisible">
        <sf:outputText escape="false" value="#{userSessionBean.selectedMenuItem.properties.label}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </sf:heading>        
    </c:if>

    <t:div rendered="#{widgetBean.interiorTopWidgetRender and 
                       userSessionBean.selectedMenuItem.properties.interiorTopWidgetLayout != null}" 
           styleClass="widgetContainerLayer">
      <sf:widgetContainer
        id="interior_top_container"
        binding="#{widgetBean.interiorTopContainer}"
        columns="#{widgetBean.interiorTopColumns}"
        columnClasses="#{widgetBean.interiorTopColumnClasses}"
        columnTitles="#{widgetBean.interiorTopColumnTitles}"
        columnRenderAsList="#{widgetBean.interiorTopColumnRenderAsList}"        
        layout="#{widgetBean.interiorTopLayout}"
        dynamic="false"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}"
        title="#{widgetBean.interiorTopTitle}"
        headerDocId="#{widgetBean.interiorTopHeaderDocId}"
        footerDocId="#{widgetBean.interiorTopFooterDocId}"
        styleClass="widget_container" />
    </t:div>
      
    <t:div styleClass="bodyDiv">
      <h:panelGroup>
        <jsp:include page="${requestScope['_body']}"/>
      </h:panelGroup>
    </t:div>
      
    <t:div rendered="#{widgetBean.interiorBottomWidgetRender and 
                       userSessionBean.selectedMenuItem.properties.interiorBottomWidgetLayout != null}"
           styleClass="widgetContainerLayer">
      <sf:widgetContainer
        id="interior_bottom_container"
        binding="#{widgetBean.interiorBottomContainer}"
        columns="#{widgetBean.interiorBottomColumns}"
        columnClasses="#{widgetBean.interiorBottomColumnClasses}"
        columnTitles="#{widgetBean.interiorBottomColumnTitles}"
        columnRenderAsList="#{widgetBean.interiorBottomColumnRenderAsList}"        
        layout="#{widgetBean.interiorBottomLayout}"
        dynamic="false"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}"                  
        title="#{widgetBean.interiorBottomTitle}"
        headerDocId="#{widgetBean.interiorBottomHeaderDocId}"
        footerDocId="#{widgetBean.interiorBottomFooterDocId}"
        styleClass="widget_container" />
    </t:div>
    
    <h:outputText value="&lt;/main&gt;" escape="false"/>            
        
    <t:div styleClass="separator" />
    
    <t:div styleClass="bottomBar">
      <h:outputText value="&lt;footer id='wp2_mobile_footer' aria-label='#{webBundle.mainFooter}'&gt;" escape="false"/>      
      <sf:heading level="1" styleClass="element-invisible">
        <h:outputText value="#{webBundle.mainFooter}" />
      </sf:heading>
      <sf:heading level="2" styleClass="element-invisible">
        <sf:outputText value="#{webBundle.browserType}" />
      </sf:heading>        
      <t:div styleClass="navModeDiv">
        <sf:outputLink value="/go.faces?xmid=#{userSessionBean.selectedMenuItem.mid}&amp;btype=desktop"
                       styleClass="desktopLink" ariaHidden="true">
          <sf:outputText value="#{userSessionBean.selectedMenuItem.properties['mobile2.desktopVersionText']}"
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{userSessionBean.translationGroup}" />
        </sf:outputLink>        
      </t:div>
      <sf:heading level="2" styleClass="element-invisible">
        <sf:outputText value="#{webBundle.language}" />
      </sf:heading>        
      <t:div styleClass="langDiv">
        <h:outputLink value="/go.faces?language=ca" styleClass="langLink"
                       lang="ca" title="català">
          <sf:outputText value="ca" />          
        </h:outputLink>
        <sf:outputText value="|" />          
        <h:outputLink value="/go.faces?language=es" styleClass="langLink"
                       lang="es" title="castellano">
          <sf:outputText value="es" />          
        </h:outputLink>
        <sf:outputText value="|" />          
        <h:outputLink value="/go.faces?language=en" styleClass="langLink"
                       lang="en" title="english">
          <sf:outputText value="en" />          
        </h:outputLink>
      </t:div>
      <sf:heading level="2" styleClass="element-invisible">
        <h:outputText value="#{webBundle.bottomMenu}" />
      </sf:heading>
      <sf:treeMenu styleClass="headerMenuDiv" var="item"
        baseMid="#{userSessionBean.selectedMenuItem.properties.headerMenuMid}"
        expandDepth="1" headingsRender="true" headingsBaseLevel="3">
        <f:facet name="data">
          <sf:outputLink value="#{item.actionURL}"
            onclick="#{item.onclick}" target="#{item.target}"
            rendered="#{item.rendered}"
            ariaLabel="#{item.directProperties.ariaLabel}"
            ariaHidden="#{item.directProperties.ariaHidden == 'true'}" 
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}">
            <h:graphicImage alt="" 
                            url="/documents/#{item.properties.iconDocId}" 
                            rendered="#{item.properties.iconDocId != null}"/>
            <sf:outputText style="display: block;" value="#{item.label}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </sf:outputLink>
        </f:facet>
      </sf:treeMenu> 
      <h:outputText value="&lt;/footer&gt;" escape="false"/>          
    </t:div>

    <h:outputText value="#{widgetBean.scripts}" escape="false" />

  </t:div>
    
</jsp:root>
