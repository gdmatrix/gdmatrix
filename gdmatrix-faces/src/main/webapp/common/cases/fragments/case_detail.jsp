<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

<f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" var="objectBundle"/>  
<f:loadBundle basename="org.santfeliu.cases.web.resources.CaseBundle" var="caseBundle"/>

<sf:saveScroll />  

<t:div styleClass="caseDetail" >

  <t:div id="shortcutURL" styleClass="shortcutURL"
     style="display:none">
    <h:outputText value="#{caseBundle.caseDetail_shortcutURL}:"
      styleClass="label"/>
    <h:outputText value="#{caseDetailBean.shortcutURL}"
      styleClass="url"/>
  </t:div>

  <!-- action buttons -->
  <t:div style="width:100%;text-align:right">
    <h:graphicImage value="#{userSessionBean.icons.link}"
      onclick="javascript:toggleVisibility('mainform:shortcutURL')"
      styleClass="closeButton"
      title="#{caseBundle.caseDetail_shortcutURL}: #{caseDetailBean.shortcutURL}"
      alt="#{caseBundle.caseDetail_shortcutURL}: #{caseDetailBean.shortcutURL}"
      rendered="#{caseDetailBean.renderShortcutURL}"/>
    <t:commandButton value="#{objectBundle.edit}"
      action="#{caseDetailBean.editCase}"
      image="#{userSessionBean.icons.edit}"
      rendered="#{caseSearchBean.editorUser and caseDetailBean.editable and userSessionBean.menuModel.browserType == 'desktop'}"
      alt="#{objectBundle.edit}" title="#{objectBundle.edit}"
      styleClass="editButton"/>
    <t:commandButton onclick="javascript:printGroup('printCase');"
      styleClass="editButton"
      image="#{userSessionBean.icons.print}"
      rendered="#{userSessionBean.selectedMenuItem.properties.printEnabled=='true' and userSessionBean.menuModel.browserType == 'desktop'}"
      alt="#{objectBundle.print_new_window}" title="#{objectBundle.print_new_window}"/>      
    <t:commandButton action="#{controllerBean.close}"
      value="#{objectBundle.close}" immediate="true"
      styleClass="closeButton"
      image="#{userSessionBean.icons.close}"
      alt="#{objectBundle.close}" title="#{objectBundle.close}"
      rendered="#{caseDetailBean.renderCloseButton and userSessionBean.menuModel.browserType == 'desktop'}"/>
  </t:div>


  <!-- List view -->
  <sf:printableGroup name="printCase">
  <h:panelGroup rendered="#{!caseDetailBean.tabbedView}">
    <t:dataList id="panels" value="#{caseDetailBean.panels}" var="panel">
      <sf:div styleClass="#{panel.styleClass}" style="overflow:hidden"
             rendered="#{caseDetailBean.renderPanelContent}"
             ariaHidden="#{panel.ariaHidden}">
        <t:div styleClass="header" rendered="#{panel.headingLevel == null and panel.renderContent}">  
          <sf:outputText value="#{panel.title}" escape="false"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}"/>
        </t:div>          
        <sf:heading level="#{panel.headingLevel}" styleClass="header" rendered="#{panel.headingLevel != null and panel.renderContent}">
          <sf:outputText value="#{panel.title}" escape="false"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}"/>
        </sf:heading>
        <t:div rendered="#{panel.headerBrowserUrl!=null}"
          styleClass="headerDocument">
          <sf:browser url="#{panel.headerBrowserUrl}"
            port="#{applicationBean.defaultPort}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </t:div>

        <t:div styleClass="content">
          <t:panelGroup rendered="#{panel.type == 'main' }">
            <jsp:include page="/common/cases/fragments/panels/main.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'browser'}">
            <jsp:include page="/common/cases/fragments/panels/browser.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'documents'}">
            <jsp:include page="/common/cases/fragments/panels/documents.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'cases'}">
            <jsp:include page="/common/cases/fragments/panels/cases.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'gallery'}">
            <jsp:include page="/common/cases/fragments/panels/gallery.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'dynamicform'}">
            <jsp:include page="/common/cases/fragments/panels/dynamicform.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'workflows'}">
            <jsp:include page="/common/cases/fragments/panels/workflows.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'persons'}">
            <jsp:include page="/common/cases/fragments/panels/persons.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'addresses'}">
            <jsp:include page="/common/cases/fragments/panels/addresses.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'contacts'}">
            <jsp:include page="/common/cases/fragments/panels/contacts.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'property_values'}">
            <jsp:include page="/common/cases/fragments/panels/property_values.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'addresses_agenda'}">
            <jsp:include page="/common/cases/fragments/panels/addresses_agenda.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'persons_agenda'}">
            <jsp:include page="/common/cases/fragments/panels/persons_agenda.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'agenda'}">
            <jsp:include page="/common/cases/fragments/panels/agenda.jsp" />
          </t:panelGroup>
          <t:panelGroup rendered="#{panel.type == 'report'}">
            <jsp:include page="/common/cases/fragments/panels/report.jsp" />
          </t:panelGroup>          
        </t:div>

        <t:div rendered="#{panel.footerBrowserUrl!=null}"
          styleClass="headerDocument">
          <sf:browser url="#{panel.footerBrowserUrl}"
            port="#{applicationBean.defaultPort}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </t:div>

        <t:div styleClass="footer">
        </t:div>
      </sf:div>
    </t:dataList>
  </h:panelGroup>
  </sf:printableGroup>
</t:div>

</jsp:root>