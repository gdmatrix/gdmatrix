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
    <f:loadBundle basename="org.santfeliu.security.web.resources.ValidBundle" var="validBundle"/>
    <html lang="${userSessionBean.viewLanguage}">
      <head>
        <meta content="text/html;charset=UTF-8" />
        <meta name="viewport" content="width=device-width" />
        <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/common/security/css/security.css?v=${applicationBean.resourcesVersion}" />
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/common/security/css/valid.css?v=${applicationBean.resourcesVersion}" />
        <title>
          <t:outputText value="#{validBean.title}" />
        </title>        
      </head>
      <body class="loginPage">
        <h:form id="mainform" enctype="multipart/form-data">
          <sf:heading level="1" styleClass="element-invisible">
            <t:outputText value="#{validBean.title}" />
          </sf:heading>            
          <t:div styleClass="validBox">
            <t:div styleClass="logo">
              <t:outputText value="#{validBean.title}" />
            </t:div>
            <t:div styleClass="message">
              <t:messages         
                styleClass="messages"
                globalOnly="false"
                infoClass="info"
                warnClass="warn"
                errorClass="error"
                fatalClass="fatal" />
            </t:div>
            <t:div styleClass="footer">
              <h:outputLink value="#{loginBean.cancelURL}" styleClass="button">
                <h:outputText value="#{securityBundle.cancel}"/>
              </h:outputLink>
              <h:outputLink target="_blank" value="/accessibilitat" 
                            styleClass="accLink">
                <h:outputText value="#{webBundle.accessibility}" />                
              </h:outputLink>              
            </t:div>
          </t:div>
        </h:form>
      </body>
    </html>
  </f:view>
</jsp:root>

