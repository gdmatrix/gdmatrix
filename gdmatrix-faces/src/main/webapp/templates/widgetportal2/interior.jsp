<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core">

  <!-- CONTENT -->

  <t:div styleClass="interiorHeaderBackground" rendered="#{userSessionBean.selectedMenuItem.properties.interiorHeaderRender == 'true'}" />
    
  <t:div styleClass="interiorLayout">
    
    <t:div styleClass="navPathLayer"
           rendered="#{userSessionBean.selectedMenuItem.properties.renderNavPath == 'true'}">
      <h:outputText value="&lt;nav id='interior_nav_path' aria-label='#{webBundle.navigationPath}'&gt;" escape="false"/>            
      <sf:heading level="2" styleClass="element-invisible">
        <h:outputText value="#{webBundle.navigationPath}" />
      </sf:heading>
      <sf:navigationPath id="navPath"
                       var="item"
                       value="main"
                       baseMid="#{userSessionBean.selectedMenuItem.path[1]}"
                       maxDepth="#{userSessionBean.selectedMenuItem.properties.maxNavPathDepth}"
                       styleClass="navPath"
                       renderMode="list">
        <f:facet name="menuitem">
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </f:facet>        
      </sf:navigationPath> 
      <h:outputText value="&lt;/nav&gt;" escape="false"/>           
    </t:div>
    
    <t:div styleClass="interiorHeader" rendered="#{userSessionBean.selectedMenuItem.properties.interiorHeaderRender == 'true'}">
      <h:outputText value="&lt;header id='interior_header' aria-label='#{webBundle.interiorHeader}'&gt;" escape="false"/>        
      <t:div styleClass="image">
        <h:graphicImage url="#{userSessionBean.selectedMenuItem.properties.interiorHeaderImageURL}" />
      </t:div>
      <t:div styleClass="headLine">
        <sf:outputText escape="false" value="#{userSessionBean.selectedMenuItem.properties.interiorHeaderHeadLine}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />                          
      </t:div>
      <t:div styleClass="summary">
        <sf:outputText escape="false" value="#{userSessionBean.selectedMenuItem.properties.interiorHeaderSummary}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />                                    
      </t:div>
      <h:outputText value="&lt;/header&gt;" escape="false"/>
    </t:div>   
    
    <t:div styleClass="ilcol1" rendered="#{userSessionBean.selectedMenuItem.properties.interiorHeaderRender == 'true' or userSessionBean.selectedMenuItem.properties.interiorContentWidth == 'normal'}">
      <h:outputText value="&lt;nav id='interior_nav_left' aria-label='#{webBundle.interiorSideMenu}'&gt;" escape="false"/>        
      <sf:heading level="2" styleClass="element-invisible">
        <sf:outputText value="Menú lateral #{userSessionBean.selectedMenuItem.cursorPath[userSessionBean.selectedMenuItem.properties.interiorLeftMenuRootDepth - 1].label}"               
           translator="#{userSessionBean.translator}"
           translationGroup="#{userSessionBean.translationGroup}" />
      </sf:heading>        
      <t:div styleClass="leftMenu #{userSessionBean.selectedMenuItem.properties.interiorHeaderRender == 'true' ? 'big' : 'normal'}">
        <t:div styleClass="sectionTitle" rendered="#{userSessionBean.selectedMenuItem.properties.interiorHeaderRender != 'true'}">
          <sf:outputText value="#{userSessionBean.selectedMenuItem.cursorPath[userSessionBean.selectedMenuItem.properties.interiorLeftMenuRootDepth - 1].label}"
             styleClass="text"
             translator="#{userSessionBean.translator}"
             translationGroup="#{userSessionBean.translationGroup}" />
        </t:div>
        <sf:treeMenu var="item" styleClass="sectionChildren"
          baseMid="#{userSessionBean.selectedMenuItem.path[userSessionBean.selectedMenuItem.properties.interiorLeftMenuRootDepth - 1]}"
          expandDepth="6" expandSelected="true" headingsRender="false" headingsBaseLevel="2">
          <f:facet name="data">
            <t:div rendered="#{item.rendered}" 
                   styleClass="#{item.selectionContained ? 'selected' : ''}">
              <sf:outputLink value="#{item.actionURL}"
                onclick="#{item.onclick}" target="#{item.target}"
                ariaLabel="#{item.directProperties.ariaLabel}"
                ariaHidden="#{item.directProperties.ariaHidden == 'true'}"                
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}">
                <sf:outputText value="#{item.label}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" />
              </sf:outputLink>            
            </t:div>
          </f:facet>
        </sf:treeMenu>
      </t:div>
      <h:outputText value="&lt;/nav&gt;" escape="false"/>      
    </t:div>

    <t:div styleClass="ilcol2 #{userSessionBean.selectedMenuItem.properties.interiorContentWidth}#{(userSessionBean.selectedMenuItem.action == 'blank') ? ' blank' : ''}">
      <h:outputText value="&lt;main id='sf_main_content' tabindex='-1'&gt;" escape="false"/>      
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
                         userSessionBean.selectedMenuItem.properties.interiorTopWidgetLayout != null}">
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
      <sf:widget id="widget_body" styleClass="widget">
        <h:panelGroup>
          <c:set var="_customBar" scope="request" value="/templates/widgetportal2/custombar.jsp" />          
          <jsp:include page="${requestScope['_body']}"/>          
        </h:panelGroup>
      </sf:widget>      
      <t:div rendered="#{widgetBean.interiorBottomWidgetRender and 
                         userSessionBean.selectedMenuItem.properties.interiorBottomWidgetLayout != null}">
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
    </t:div>
    
    <t:div rendered="#{widgetBean.interiorWidgetRender and 
                       userSessionBean.selectedMenuItem.properties.interiorWidgetLayout != null}">
      <h:outputText value="&lt;nav id='interior_nav_wcont' aria-label='#{webBundle.interiorWidgetsMenu}'&gt;" escape="false"/>      
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
        styleClass="widget_container #{userSessionBean.selectedMenuItem.properties.interiorWidgetPosition == 'bottom' ? 'bottom' : 'left'}" />      
      <h:outputText value="&lt;/nav&gt;" escape="false"/>            
    </t:div>

    <h:outputText value="#{widgetBean.scripts}" escape="false" />
    
  </t:div>
  
</jsp:root>
