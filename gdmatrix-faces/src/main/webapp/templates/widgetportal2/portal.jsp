<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core"
          xmlns:t="http://myfaces.apache.org/tomahawk">

  <t:div styleClass="portalLayout">

    <t:div styleClass="navPathLayer" 
           rendered="#{userSessionBean.selectedMenuItem.properties.renderNavPath == 'true'}">
      <h:outputText value="&lt;nav id='portal_nav_path' aria-label='#{webBundle.navigationPath}'&gt;" escape="false"/> 
      <sf:heading level="2" styleClass="element-invisible">
        <h:outputText value="#{webBundle.navigationPath}" />
      </sf:heading>
      <sf:navigationPath id="navPath"
                       var="item"
                       value="main"
                       baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                       maxDepth="#{userSessionBean.selectedMenuItem.properties.maxNavPathDepth}"
                       styleClass="navPath">
        <f:facet name="menuitem">
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </f:facet>        
      </sf:navigationPath> 
      <h:outputText value="&lt;/nav&gt;" escape="false"/>     
    </t:div>
    <!-- CONTENT -->
    <c:set var="_customBar" scope="request" value="/templates/widgetportal2/custombar.jsp" />
    <h:outputText value="&lt;main id='sf_main_content' tabindex='-1'&gt;" escape="false"/>
    <sf:heading level="1" styleClass="element-invisible">
      <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.ariaLabel == null ? 
                      userSessionBean.selectedMenuItem.label : 
                      userSessionBean.selectedMenuItem.properties.ariaLabel}" 
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />                     
    </sf:heading>    
    <jsp:include page="${requestScope['_body']}"/>
    <h:outputText value="&lt;/main&gt;" escape="false"/>      
    
  </t:div>

</jsp:root>
