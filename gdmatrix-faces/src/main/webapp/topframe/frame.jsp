<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core">
  
  <jsp:output omit-xml-declaration="true" 
              doctype-root-element="html" 
              doctype-system="about:legacy-compat" />
  <f:view>
    <f:loadBundle basename="org.santfeliu.web.resources.WebBundle" var="webBundle"/>
    <html lang="${userSessionBean.viewLanguage}">
      <head>
        <meta charset="utf-8" />
        <c:if test="${requestScope.showBaseTag == 'true'}">
          <base href="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}" />
        </c:if>
        <c:if test="${userSessionBean.selectedMenuItem.properties.mobile == 'true' or 
          userSessionBean.menuModel.browserType == 'mobile'}">
          <meta name="viewport" content="width=device-width" />
        </c:if>
        <title><sf:outputText value="#{userSessionBean.selectedMenuItem.properties.pageTitlePrefix} 
          #{userSessionBean.selectedMenuItem.properties.pageTitle == null ? 
          userSessionBean.selectedMenuItem.properties.label : 
          userSessionBean.selectedMenuItem.properties.pageTitle}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" /></title>
        <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/topframe/css/topframe.css?v=${applicationBean.resourcesVersion}" />
        <c:if test="${userSessionBean.viewMode == 'RENDER'}">
          <link rel="stylesheet" type="text/css"
            href="${pageContext.request.contextPath}/frames/${userSessionBean.frame}/css/frame.css?v=${applicationBean.resourcesVersion}" />
          <link rel="stylesheet" type="text/css"
             href="${pageContext.request.contextPath}/templates/${userSessionBean.template}/css/template.css?v=${applicationBean.resourcesVersion}" />
        </c:if>
        <c:if test="${requestScope['_css'] != null}">
          <link rel="stylesheet" type="text/css"
                href="${pageContext.request.contextPath}${requestScope['_css']}?v=${applicationBean.resourcesVersion}" />
        </c:if>
        <c:if test="${userSessionBean.viewMode == 'RENDER'}">
          <link rel="stylesheet" type="text/css"
                href="${pageContext.request.contextPath}/themes/${userSessionBean.theme}/theme.css?v=${applicationBean.resourcesVersion}" />
          <c:forEach var="nodeCSS" items="${userSessionBean.nodeCSS}">
            <link rel="stylesheet" type="text/css"
                  href="${pageContext.request.contextPath}${nodeCSS}" />
          </c:forEach>
        </c:if>
        <c:if test="${userSessionBean.selectedMenuItem.properties.faviconURL != null}">
          <link rel="shortcut icon" href="${userSessionBean.selectedMenuItem.properties.faviconURL}" />
        </c:if>
        <c:if test="${userSessionBean.selectedMenuItem.properties.touchIconURL != null}">
          <link rel="apple-touch-icon" href="${userSessionBean.selectedMenuItem.properties.touchIconURL}" />
        </c:if>
        <script type="text/javascript" src="${pageContext.request.contextPath}/topframe/js/common_script.js">
          /* general script */
        </script>
      </head>
      <body>
        <h:form id="mainform" enctype="multipart/form-data" onsubmit="return onSubmit();"
          styleClass="#{userSessionBean.administrator ? 'adminview' : null}">
          <h:outputText value="&lt;header id='topframe_header' aria-label='#{webBundle.pageTitle}'&gt;" escape="false"/>
          <sf:heading level="1" styleClass="element-invisible">
            <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.pageTitle == null ?
              userSessionBean.selectedMenuItem.properties.description :
              userSessionBean.selectedMenuItem.properties.pageTitle}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </sf:heading>
          <t:div style="display:none">
            <t:inputHidden id="hiddenjumpcommand" value="#{userSessionBean.jumpCommand}" forceId="true" />
            <t:commandLink id="jumptoobjectaction" action="#{userSessionBean.jumpToObject}" value="Link to Object" forceId="true" />
            <t:inputHidden id="hiddenaction" value="#{userSessionBean.actionToExecute}" forceId="true" />
            <t:commandLink id="doactionexecution" action="#{userSessionBean.executeAction}" value="Execute action" forceId="true" />
          </t:div>            
          <h:outputText value="&lt;/header&gt;" escape="false"/>
          <jsp:include page="/topframe/toolbar.jsp" />
          <c:if test="${requestScope['_page'] == null}">
            <jsp:include page="/frames/${userSessionBean.frame}/frame.jsp" />
          </c:if>
          <c:if test="${requestScope['_page'] != null}">
            <jsp:include page="${requestScope['_page']}" />
          </c:if>
          <f:verbatim>
            <input type="hidden" name="_userid_" value="${userSessionBean.userId}" />
          </f:verbatim>
        </h:form>    
      </body>
    </html>
    <sf:overlay />
    <sf:beanSaver />
    <t:saveState value="#{userSessionBean.workspaceId}" />
    <t:saveState value="#{userSessionBean.selectedMid}" />
    <t:saveState value="#{userSessionBean.viewMode}" />
  </f:view>
</jsp:root>

