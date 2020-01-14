<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <!-- CONTENT -->

  <t:div styleClass="interiorLayout noMenu">

    <t:div styleClass="navPathLayer"
           rendered="#{userSessionBean.selectedMenuItem.properties.renderNavPath == 'true'}">
      <h:outputText value="&lt;nav id='nomenu_nav_path'&gt;" aria-label='#{webBundle.navigationPath}' escape="false"/>            

      <sf:heading level="2" styleClass="element-invisible">
        <h:outputText value="#{webBundle.navigationPath}" />
      </sf:heading>
    
      <sf:navigationPath id="navPath"
                       var="item"
                       value="main"
                       baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                       styleClass="navPath">
        <f:facet name="menuitem">
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </f:facet>
      </sf:navigationPath>
      
      <h:outputText value="&lt;/nav&gt;" escape="false"/>            
    </t:div>  
      
    <sf:heading level="2" styleClass="element-invisible">
      <h:outputText escape="false" value="#{webBundle.pageContent}" />                                    
    </sf:heading>    
    <t:div styleClass="ilcol2">
      <sf:widget id="widget_body" styleClass="widget">
        <t:div>
          <h:outputText value="&lt;main id='sf_main_content' tabindex='-1'&gt;" escape="false"/>          
          <jsp:include page="${requestScope['_body']}"/>
          <h:outputText value="&lt;/main&gt;" escape="false"/>
        </t:div>
      </sf:widget>    
    </t:div>

  </t:div>      
      
</jsp:root>
