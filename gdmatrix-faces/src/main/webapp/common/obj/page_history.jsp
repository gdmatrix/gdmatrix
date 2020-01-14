<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf ="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />

  <t:div rendered="#{userSessionBean.selectedMenuItem.properties.showHistory == 'true'}">
    <h:commandLink action="#{controllerBean.showPageHistory}"
      rendered="#{controllerBean.pageHistoryVisibility &lt; 1}">
      <h:graphicImage value="/common/obj/images/show_pages.gif" style="border:none" />
    </h:commandLink>
    <h:commandLink action="#{controllerBean.hidePageHistory}"
      rendered="#{controllerBean.pageHistoryVisibility > 0}">
      <h:graphicImage value="/common/obj/images/hide_pages.gif" style="border:none" />
    </h:commandLink>
  </t:div>

  <h:dataTable value="#{controllerBean.recentPageHistory}"
    rendered="#{controllerBean.pageHistoryVisibility > 0}" summary=""
    var="row" styleClass="pageHistory" columnClasses="col1,col2">
    <h:column>
      <h:panelGroup>
        <h:graphicImage rendered="#{row.renderObjectTypeIcon}"
          value="#{row.objectTypeIconPath}" style="border:none;" />
        <h:commandLink action="#{row.show}">
          <sf:outputText value="#{row.title}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
          <h:outputText value=": #{row.description}" />
        </h:commandLink>
      </h:panelGroup>
    </h:column>
    <h:column>
      <h:commandButton action="#{row.close}"
        value="#{objectBundle.close}" styleClass="closeButton"
        image="#{userSessionBean.icons.close}"
        alt="#{objectBundle.close}" title="#{objectBundle.close}" />
    </h:column>
  </h:dataTable>

</jsp:root>
