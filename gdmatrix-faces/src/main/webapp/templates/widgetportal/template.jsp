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
      <sf:heading level="2" styleClass="element-invisible">
        <sf:outputText value="#{webBundle.language}" />
      </sf:heading>
      <sf:languageSelector locales="#{userSessionBean.supportedLocales}"
        styleClass="langSelector" />
      <sf:clock styleClass="clock" />
    </t:div>

    <sf:heading level="2" styleClass="element-invisible">
      <sf:outputText value="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.previous.previous.label}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </sf:heading>
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

    <sf:heading level="2" styleClass="element-invisible">
      <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.identificationMessage}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </sf:heading>
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
          <h:graphicImage url="/templates/widgetportal/images/login.png"
            title="#{webBundle.buttonSignin}" />
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
          <h:graphicImage url="/templates/widgetportal/images/login.png"
            title="#{webBundle.buttonSignout}" />
        </h:commandLink>
      </h:panelGroup>
    </t:div>

    <h:outputText value="#{loginBean.loginMessage}"
      rendered="#{loginBean.loginMessage != null}"
      styleClass="loginError" />
  </t:div>

  <!-- HEADER BAR -->
  <t:div styleClass="headerBar">
    <sf:heading level="2" styleClass="element-invisible">
      <sf:outputText value="header"/>
    </sf:heading>
    <t:div styleClass="central_panel">
      <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.homeURL}">
        <t:div styleClass="homeLink"
          title="#{userSessionBean.selectedMenuItem.properties.homeTitle}">
        </t:div>
      </h:outputLink>
      <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.hilightURL}"        
        rendered="#{userSessionBean.selectedMenuItem.properties.hilightURL != null}"
        target="_blank">
        <t:div styleClass="hilightLink"
          title="#{userSessionBean.selectedMenuItem.properties.hilightTitle}">
        </t:div>
      </h:outputLink>
      <t:div styleClass="search_panel">
        <sf:heading level="2" styleClass="element-invisible">
          <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.searchButtonText}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </sf:heading>
        <t:div styleClass="searchBox">
          <h:graphicImage url="/images/empty.png" title="" />
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
          <sf:heading level="2" styleClass="element-invisible">
            <sf:outputText value="menu"/>
          </sf:heading>
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
      <sf:heading level="2" styleClass="element-invisible">
        <sf:outputText value="#{userSessionBean.selectedMenuItem.label}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </sf:heading>
      <c:if test="${userSessionBean.layout != null}">
        <jsp:include page="${userSessionBean.layout}.jsp" />
      </c:if>

    </t:div>
  </t:div>

  <!-- FOOTER BAR -->
  <t:div styleClass="footerBar">
    <t:panelGrid columns="2" summary="" title="" styleClass="table"
      columnClasses="col1,col2">
      <h:panelGroup>
        <sf:heading level="2" styleClass="element-invisible">
          <sf:outputText value="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.previous.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </sf:heading>
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
        <sf:heading level="2" styleClass="element-invisible">
          <sf:outputText value="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </sf:heading>
        <sf:treeMenu id="contactsMenu" styleClass="contacts" var="item"
          baseMid="#{userSessionBean.selectedMenuItem.cursorPath[1].lastChild.mid}"
          expandDepth="1">
          <f:facet name="data">
              <h:outputText value="#{item.label}" />
          </f:facet>
        </sf:treeMenu>
      </h:panelGroup>

      <t:div styleClass="social_networks">
        <sf:heading level="2" styleClass="element-invisible">
          <sf:outputText value="Xarxes Socials"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </sf:heading>
        <t:div styleClass="sn_left">
          <t:div styleClass="sn_right">
            <sf:navigationMenu styleClass="snMenu" var="item"
              value="main" mode="passive"
              baseMid="#{userSessionBean.selectedMenuItem.properties.snMid}"
              orientation="horizontal">
              <h:outputLink value="#{item.properties.actionURL}"
                  title="#{item.properties.title}"
                  target="#{item.target == null ? 'blank' : item.target}">
                <h:graphicImage alt="" title="#{item.properties.title}"
                  url="#{item.properties.imageURL}" />
              </h:outputLink>
            </sf:navigationMenu>
          </t:div>
        </t:div>
      </t:div>
    </t:panelGrid>
  </t:div>

  <!-- BOTTOM BAR -->
  <t:div styleClass="bottomBar">
  </t:div>

</jsp:root>
