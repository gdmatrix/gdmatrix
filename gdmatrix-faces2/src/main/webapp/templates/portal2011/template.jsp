<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core">

  <!-- TOP BAR -->
  <t:div styleClass="topBar">
    <t:div styleClass="infoBar">
      <sf:languageSelector locales="#{userSessionBean.supportedLocales}"
        styleClass="langSelector" />
      <sf:clock styleClass="clock" />
    </t:div>

    <sf:treeMenu id="topMenu" styleClass="topMenu" var="item"
      baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.previous.previous.mid}"
      expandDepth="1">
      <f:facet name="data">
        <h:outputLink value="#{item.actionURL}"
          onclick="#{item.onclick}" target="#{item.target}"
          rendered="#{item.rendered}">
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:outputLink>
      </f:facet>
    </sf:treeMenu>

    <t:div styleClass="loginBar">
      <h:panelGroup rendered="#{userSessionBean.anonymousUser}">
        <h:outputText value="#{webBundle.outputUsername}:"
          styleClass="loginText" />
        <h:inputText binding="#{loginBean.usernameInputText}"
                     tabindex="1" size="10" immediate="true"
                     onkeypress="login(event)"
                     styleClass="loginBox" />
        <h:outputText value="#{webBundle.outputPassword}:"
                      styleClass="loginText" />
        <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                       tabindex="2" size="10" immediate="true"
                       onkeypress="login(event)"
                       styleClass="loginBox" />
        <sf:secureCommandLink id="loginbutton"
          action="#{loginBean.login}" function="login" immediate="true"
          port="#{applicationBean.serverSecurePort}" scheme="https"
          styleClass="logButton">
          <h:graphicImage url="/templates/portal2011/images/login.png"
            title="#{webBundle.buttonSignin}" alt="" />
        </sf:secureCommandLink>
        <sf:secureCommandLink
          action="#{loginBean.loginCertificate}" immediate="true"
          rendered="#{not userSessionBean.certificateUser}" scheme="https"
          port="#{applicationBean.clientSecurePort}" styleClass="loginCert">
          <h:outputText value="#{webBundle.certificateAuthentication}"  />
        </sf:secureCommandLink>
      </h:panelGroup>

      <h:panelGroup rendered="#{not userSessionBean.anonymousUser}">
        <h:outputText value="#{webBundle.outputUsername}:"
          styleClass="loginText" />
        <h:outputText value="#{userSessionBean.displayName}"
          styleClass="displayNameText" />
        <h:commandLink action="#{loginBean.logout}"
          rendered="#{not userSessionBean.anonymousUser}"
          styleClass="logButton" immediate="true">
          <h:graphicImage url="/templates/portal2011/images/login.png"
            title="#{webBundle.buttonSignout}" alt="" />
        </h:commandLink>
      </h:panelGroup>
    </t:div>

    <h:outputText value="#{loginBean.loginMessage}"
      rendered="#{loginBean.loginMessage != null}"
      styleClass="loginError" />
  </t:div>

  <!-- MENU -->
  <t:div styleClass="headerBar">
    <t:div styleClass="central_panel">
      <t:div styleClass="search_panel">
        <t:div styleClass="searchBox">
          <h:graphicImage url="/images/empty.png" title="" alt="" />
          <t:inputText id="checkedTextField" forceId="true"
            onkeypress="inputTextKeyCheck(event, 'mainform:templateSearchButton');"
            styleClass="searchWords" value="#{searchBean.remoteInputText}" />
          <h:panelGroup styleClass="searchButton">
            <sf:commandButton id="templateSearchButton"
               value="#{userSessionBean.selectedMenuItem.properties.searchButtonText}"
               action="#{searchBean.remoteSearch}"
               translator="#{userSessionBean.translator}"
               translationGroup="#{userSessionBean.translationGroup}" />
          </h:panelGroup>
        </t:div>
      </t:div>
      <t:div styleClass="back_left">
        <t:div styleClass="back_right">          
          <sf:treeMenu id="mainMenu" styleClass="menu" var="item"
            baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].mid}"
            menuStyleClass="#{item.properties.menuStyleClass}"
            expandDepth="3">
            <f:facet name="data">
              <h:outputLink value="#{item.actionURL}"
                onclick="#{item.onclick}" target="#{item.target}"
                rendered="#{item.rendered}">
                <sf:outputText value="#{item.label}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" />
              </h:outputLink>
            </f:facet>
          </sf:treeMenu>
        </t:div>
      </t:div>
    </t:div>
  </t:div>

  <!-- CENTRAL PANEL -->
  <t:div styleClass="page_body">
    <t:div styleClass="central_panel">

      <!-- CONTENT -->
      <c:if test="${userSessionBean.layout != null}">
        <jsp:include page="${userSessionBean.layout}.jsp" />
      </c:if>

    </t:div>
  </t:div>

  <!-- BOTTOM BAR -->
  <t:div styleClass="footerBar">
    <t:div styleClass="central_panel">
      <t:div styleClass="social_networks">
        <t:div styleClass="sn_left">
          <t:div styleClass="sn_right">

            <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.bloggerURL}"
              rendered="#{userSessionBean.selectedMenuItem.properties.bloggerURL != null and
                userSessionBean.selectedMenuItem.properties.bloggerURL != 'none'}"
              target="blank">
              <h:graphicImage title="#{userSessionBean.selectedMenuItem.properties.bloggerTitle}"
                url="/images/sn/blogger.png" alt="" />
            </h:outputLink>

            <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.facebookURL}"
              rendered="#{userSessionBean.selectedMenuItem.properties.facebookURL != null and
                userSessionBean.selectedMenuItem.properties.facebookURL != 'none'}"
              target="blank">
              <h:graphicImage title="#{userSessionBean.selectedMenuItem.properties.facebookTitle}"
                url="/images/sn/facebook.png" alt="" />
            </h:outputLink>

            <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.twitterURL}"
              rendered="#{userSessionBean.selectedMenuItem.properties.twitterURL != null  and
                userSessionBean.selectedMenuItem.properties.twitterURL != 'none'}"
              target="blank">
              <h:graphicImage title="#{userSessionBean.selectedMenuItem.properties.twitterTitle}"
                url="/images/sn/twitter.png" alt="" />
            </h:outputLink>

            <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.youtubeURL}"
              rendered="#{userSessionBean.selectedMenuItem.properties.youtubeURL != null and
                userSessionBean.selectedMenuItem.properties.youtubeURL != 'none'}"
              target="blank">
              <h:graphicImage title="#{userSessionBean.selectedMenuItem.properties.youtubeTitle}"
                url="/images/sn/youtube.png" alt="" />
            </h:outputLink>

            <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.googleURL}"
              rendered="#{userSessionBean.selectedMenuItem.properties.googleURL != null and
                userSessionBean.selectedMenuItem.properties.googleURL != 'none'}"
              target="blank">
              <h:graphicImage title="#{userSessionBean.selectedMenuItem.properties.googleTitle}"
                url="/images/sn/google.png" alt="" />
            </h:outputLink>
          </t:div>
        </t:div>
      </t:div>
      <sf:treeMenu id="footerMenu" styleClass="footerMenu" var="item"
        baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.previous.mid}"
        expandDepth="1">
        <f:facet name="data">
          <h:outputLink value="#{item.actionURL}"
            onclick="#{item.onclick}" target="#{item.target}"
            rendered="#{item.rendered}">
            <sf:outputText value="#{item.label}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </h:outputLink>
        </f:facet>
      </sf:treeMenu>
      <sf:treeMenu id="contactsMenu" styleClass="contacts" var="item"
        baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.mid}"
        expandDepth="1">
        <f:facet name="data">
            <h:outputText value="#{item.label}" />
        </f:facet>
      </sf:treeMenu>
    </t:div>
  </t:div>

  <t:div styleClass="bottomBar">
  </t:div>

</jsp:root>
