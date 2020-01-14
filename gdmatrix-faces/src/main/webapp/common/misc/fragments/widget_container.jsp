<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />
  <f:loadBundle basename="org.santfeliu.misc.widget.web.resources.WidgetBundle"
    var="miscBundle" />  
  
  <sf:saveScroll value="#{widgetBean.scroll}" />    

  <t:div styleClass="portalLayer">
  
    <sf:widgetContainer
      id="header_container"
      binding="#{widgetBean.headerContainer}"
      columns="#{widgetBean.headerColumns}"
      columnClasses="#{widgetBean.headerColumnClasses}"
      columnTitles="#{widgetBean.headerColumnTitles}"
      columnRenderAsList="#{widgetBean.headerColumnRenderAsList}"
      layout="#{widgetBean.headerLayout}"
      dynamic="false"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}"                
      styleClass="widget_container" />
    
    <sf:widgetContainer
      id="left_container"
      binding="#{widgetBean.leftContainer}"
      columns="#{widgetBean.leftColumns}"
      columnClasses="#{widgetBean.leftColumnClasses}"
      columnTitles="#{widgetBean.leftColumnTitles}"      
      columnRenderAsList="#{widgetBean.leftColumnRenderAsList}"
      layout="#{widgetBean.leftLayout}"
      dynamic="false"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}"                      
      styleClass="widget_container" />
    
    <t:div styleClass="mainColumn">
      
      <sf:widgetContainer
        id="main_container"
        binding="#{widgetBean.mainContainer}"
        columns="#{widgetBean.mainColumns}"
        columnClasses="#{widgetBean.mainColumnClasses}"
        columnTitles="#{widgetBean.mainColumnTitles}"   
        columnRenderAsList="#{widgetBean.mainColumnRenderAsList}"
        layout="#{widgetBean.mainLayout}"
        dynamic="false"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}"                        
        styleClass="widget_container" />

      <jsp:include page="${requestScope['_customBar']}"/>        

      <sf:widgetContainer
        id="custom_container"
        binding="#{widgetBean.customContainer}"
        columns="#{widgetBean.customColumns}"
        columnClasses="#{widgetBean.customColumnClasses}"
        columnTitles="#{widgetBean.customColumnTitles}"  
        columnRenderAsList="#{widgetBean.customColumnRenderAsList}"        
        layout="#{widgetBean.customLayout}"
        styleClass="widget_container"
        dynamic="true"
        draggableWidgets="#{widgetBean.customDraggableWidgets}"        
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}"                        
        updateCallback="onUpdate" />  

      <sf:widgetContainer
        id="footer_container"
        binding="#{widgetBean.footerContainer}"
        columns="#{widgetBean.footerColumns}"
        columnClasses="#{widgetBean.footerColumnClasses}"
        columnTitles="#{widgetBean.footerColumnTitles}"    
        columnRenderAsList="#{widgetBean.footerColumnRenderAsList}"
        layout="#{widgetBean.footerLayout}"
        dynamic="false"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}"                        
        styleClass="widget_container" />
      
    </t:div>

    <h:outputText value="#{widgetBean.scripts}" escape="false" />

    <h:commandButton id="refreshWidgetContainer" value="Refresh" action="#{widgetBean.show}" style="display:none;" />
    
  </t:div>
  
</jsp:root>
