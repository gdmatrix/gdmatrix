<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.grx.web.resources.GrxBundle"
                var="grxBundle"/>

  <t:saveState value="#{viewerBean.context}" />

  <t:div>
    <h:panelGrid columns="2"
      styleClass="layerSelection"
      columnClasses="col1,col2" headerClass="header" footerClass="footer">
      <f:facet name="header">
        <h:outputText value="#{viewerBean.layersConfigLabel == null ?
          grxBundle.layersConfigLabel : viewerBean.layersConfigLabel}:" />
      </f:facet>

      <t:dataTable var="category" value="#{viewerBean.categories}"
        styleClass="categoryList" headerClass="catHeader" width="100%"
        rowClasses="arow">
        <t:column>
          <f:facet name="header">
            <h:outputText value="#{viewerBean.categoriesLabel == null ?
              grxBundle.categoriesLabel : viewerBean.categoriesLabel}:" />
          </f:facet>
          <h:commandLink action="#{viewerBean.showCategoryLayers}"
            styleClass="#{viewerBean.category == category ? 'selected' : 'unselected'}">
            <sf:outputText value="#{category}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </h:commandLink>
        </t:column>
      </t:dataTable>

      <t:dataTable var="layer" value="#{viewerBean.categoryLayers}"
        styleClass="layerList" headerClass="header" width="100%"
        rowClasses="arow1, arow2">
        <t:column style="text-align:right">
          <h:graphicImage url="#{layer.iconURL}"
            style="vertical-align:middle"
            rendered="#{layer.iconURL != null}" />
        </t:column>
        <t:column style="text-align:left">
          <h:selectBooleanCheckbox value="#{viewerBean.layerVisible}" />
          <sf:outputText value="#{layer.displayName}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </t:column>
      </t:dataTable>

      <f:facet name="footer">
        <h:commandButton value="#{grxBundle.applyLabel}"
          action="#{viewerBean.applyLayers}" styleClass="grxButton" />
      </f:facet>
    </h:panelGrid>
  </t:div>

</jsp:root>
