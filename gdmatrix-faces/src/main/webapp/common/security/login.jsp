<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html;charset=UTF-8"/>
  
  <jsp:output omit-xml-declaration="true" 
              doctype-root-element="html" 
              doctype-system="about:legacy-compat" />  
  
  <f:view>
    <f:loadBundle basename="org.santfeliu.web.resources.WebBundle" var="webBundle"/>
    <f:loadBundle basename="org.santfeliu.security.web.resources.SecurityBundle" var="securityBundle"/>
    <html lang="${userSessionBean.viewLanguage}">
      <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width" />
        <title>${loginBean.loginTitle}</title>
        <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/common/security/css/security.css?v=${applicationBean.resourcesVersion}" />
      </head>
      <body class="loginPage">
        <sf:heading level="1" styleClass="element-invisible">
          <sf:outputText value="#{loginBean.loginTitle}"
            translator="#{userSessionBean.translator}"
            translationGroup="login" />        
        </sf:heading>        
        <h:form id="mainform" enctype="multipart/form-data">
          <h:panelGroup styleClass="loginForm">
            <h:panelGroup styleClass="header">
              <h:panelGroup styleClass="loginImage"
                rendered="#{loginBean.loginImage != null}">
                <h:graphicImage value="#{loginBean.loginImage}" alt="" title="" />
              </h:panelGroup>
              <h:panelGroup styleClass="loginTitle">
                <sf:outputText value="#{loginBean.loginTitle}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="login" />
              </h:panelGroup>
            </h:panelGroup>

            <h:panelGroup styleClass="message">
              <h:outputText value="#{loginBean.loginMessage}"
                styleClass="loginMessage" />
            </h:panelGroup>

            <h:panelGroup styleClass="body">

              <h:panelGroup rendered="#{not loginBean.loginCertificateFailed}">

                <!-- User/password -->
                <h:panelGroup rendered="#{loginBean.requestedAuthenticationLevel lt 2 and loginBean.requestedSignatureLevel lt 2}">
                  <h:outputLabel value="#{webBundle.outputUsername}:" for="username" />
                  <h:inputText binding="#{loginBean.usernameInputText}" id="username" styleClass="fixed" />

                  <h:outputLabel value="#{webBundle.outputPassword}:" for="password" />
                  <h:inputSecret binding="#{loginBean.passwordInputSecret}" id="password" styleClass="fixed" />

                  <h:commandButton value="#{webBundle.buttonSignin}"
                    action="#{loginBean.loginRedirect}" 
                    styleClass="button password fixed" style="margin-top:14px;" />
                </h:panelGroup>

                <!-- Certificate -->
                <h:outputLink styleClass="button certificate fixed"
                  value="#{loginBean.loginCertificateURL}">
                  <h:outputText value="#{webBundle.certificateAuthentication}"/>
                </h:outputLink>

                <!-- VALid -->
                <h:outputLink value="#{validBean.loginURL}" styleClass="button valid fixed"
                  rendered="#{loginBean.requestedAuthenticationLevel lt 3 and loginBean.requestedSignatureLevel lt 3 and 
                             (userSessionBean.intranetUser or userSessionBean.selectedMenuItem.properties.login_valid_enabled == 'true')}">
                  <h:outputText value="VALid" />
                </h:outputLink>

                <!-- MobileID -->
                <h:outputLink value="#{mobileIdBean.loginURL}" styleClass="button mobileid fixed"
                  rendered="#{loginBean.requestedAuthenticationLevel lt 3 and loginBean.requestedSignatureLevel lt 3 and 
                             (userSessionBean.intranetUser or userSessionBean.selectedMenuItem.properties.login_mobileid_enabled == 'true')}">
                  <h:outputText value="MobileID" />
                </h:outputLink>              
              </h:panelGroup>

              <h:outputLink value="#{loginBean.cancelURL}" styleClass="button cancel fixed">
                <h:outputText value="#{securityBundle.cancel}"/>
              </h:outputLink>

            </h:panelGroup>

            <h:panelGroup styleClass="footer">
              <h:outputLink target="_blank" value="/accessibilitat">
                <h:outputText value="#{webBundle.accessibility}" />                
              </h:outputLink>
            </h:panelGroup>
              
          </h:panelGroup>
          <t:saveState value="#{loginBean}" />
        </h:form>
      </body>
    </html>
  </f:view>
</jsp:root>

