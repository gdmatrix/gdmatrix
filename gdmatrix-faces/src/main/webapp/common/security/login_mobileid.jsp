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
    <f:loadBundle basename="org.santfeliu.security.web.resources.MobileIdBundle" var="mobileIdBundle"/>

    <html lang="${userSessionBean.viewLanguage}">
      <head>
        <meta content="text/html;charset=UTF-8" />
        <meta name="viewport" content="width=device-width" />
        <title>${loginBean.loginTitle}</title>
        <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/common/security/css/security.css?v=${applicationBean.resourcesVersion}" />
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/common/security/css/mobileid.css?v=${applicationBean.resourcesVersion}" />
      </head>
      <body class="loginPage">
        <sf:heading level="1" styleClass="element-invisible">
          <h:outputText value="MobileID" />        
        </sf:heading>        
        
        <h:form id="mainform" enctype="multipart/form-data">

          <t:div styleClass="loginMobile">

            <t:div rendered="#{userSessionBean.selectedMenuItem.properties.login_mobileid_headerDocId != null}">
              <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.login_mobileid_headerDocId}" 
                          port="#{applicationBean.defaultPort}"
                          translator="#{userSessionBean.translator}"
                          translationGroup="mobileId" />
            </t:div>

            <t:panelGroup rendered="#{not mobileIdBean.waitingForPin}" styleClass="panel enter_id_number">
              <h:outputLabel value="#{mobileIdBundle.docType}:" for="docType" />
              <t:selectOneMenu id="docType" value="#{mobileIdBean.docType}" styleClass="selectBox">
                <f:selectItem itemValue="0" itemLabel="#{mobileIdBundle.NIF}" />
                <f:selectItem itemValue="1" itemLabel="#{mobileIdBundle.CIF}" />
                <f:selectItem itemValue="2" itemLabel="#{mobileIdBundle.others}" />
              </t:selectOneMenu>
              <h:outputLabel value="#{mobileIdBundle.docNum}:" for="docNum" />
              <h:inputText id="docNum" value="#{mobileIdBean.docNum}" styleClass="inputBox" />

              <t:messages styleClass="messages" errorClass="error" infoClass="info" />
              <t:div styleClass="toolbar">
                <h:commandButton value="#{mobileIdBundle.login}" action="#{mobileIdBean.beginLogin}" styleClass="button" />
                <h:outputLink value="#{loginBean.cancelURL}" styleClass="button fixed">
                  <h:outputText value="#{securityBundle.cancel}"/>
                </h:outputLink>
              </t:div>
            </t:panelGroup>

            <t:panelGroup rendered="#{mobileIdBean.waitingForPin}" styleClass="panel wait_for_pin">
              <t:outputText value="#{mobileIdBundle.enterPIN}" />
              <t:messages styleClass="messages" errorClass="error" infoClass="info" />
              <t:div styleClass="toolbar">
                <h:commandButton value="#{mobileIdBundle.pinEntered}" action="#{mobileIdBean.endLogin}" styleClass="button" />
                <h:outputLink value="#{loginBean.cancelURL}" styleClass="button fixed">
                  <h:outputText value="#{securityBundle.cancel}"/>
                </h:outputLink>
              </t:div>
            </t:panelGroup>
                
            <t:div styleClass="footer">
              <h:outputLink target="_blank" value="/accessibilitat">
                <h:outputText value="#{webBundle.accessibility}" />                
              </h:outputLink>
            </t:div>
                
          </t:div>
          <t:saveState value="#{mobileIdBean}" />
        </h:form>
      </body>
    </html>
  </f:view>
</jsp:root>
