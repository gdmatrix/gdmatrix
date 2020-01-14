<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.agenda.web.resources.AgendaBundle"
    var="agendaBundle" />

  <t:div style="width:100%"  
    styleClass="headerDocument"
    rendered="#{eventSearchBean.headerBrowser != null}">
    <sf:browser binding="#{eventSearchBean.headerBrowser}"
      port="#{applicationBean.defaultPort}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}" />
  </t:div>

  <t:div style="width:100%" rendered="#{eventSearchBean.renderFilterInfo}">
    <t:div style="width:100%"
      styleClass="infoDocument"
      rendered="#{eventSearchBean.infoBrowser != null}" >
      <sf:browser binding="#{eventSearchBean.infoBrowser}"
        port="#{applicationBean.defaultPort}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </t:div>
  </t:div>

  <t:div styleClass="confirmPickup"
    rendered="#{eventSearchBean.selectedEvent != null and
      eventSearchBean.renderPickUpButton}" >
    <t:div styleClass="header">
      <h:outputText value="#{agendaBundle.confirmAddSelectedEvent}" />
    </t:div>
    <t:div styleClass="body">
      <h:outputText value="#{eventSearchBean.selectedEvent.summary}" />
    </t:div>
    <t:div styleClass="footer">
      <h:commandButton value="#{agendaBundle.confirmYes}"
        action="#{eventSearchBean.addSelectedEvent}" immediate="true" />
      <h:commandButton value="#{agendaBundle.confirmNo}"
        action="#{eventSearchBean.discardSelectedEvent}" immediate="true" />
    </t:div>
  </t:div>

  <jsp:include page="${requestScope['_subtemplate']}" />

  <t:div styleClass="actionsBar" rendered="#{eventSearchBean.renderActionsBar}">
     <h:commandButton value="#{objectBundle.search}"
        styleClass="searchButton"
        action="#{eventSearchBean.pickUpEvent}"
        image="#{userSessionBean.icons.search}"
        alt="#{objectBundle.search}" title="#{objectBundle.search}"
        rendered="#{eventSearchBean.renderPickUpButton}"/>
    <h:commandButton value="#{objectBundle.current}"
       image="#{userSessionBean.icons.current}"
       alt="#{objectBundle.current}" title="#{objectBundle.current}"
       action="#{eventBean.show}" immediate="true"
       styleClass="currentButton"
       rendered="#{eventSearchBean.editorUser and userSessionBean.menuModel.browserType == 'desktop'}"/>
     <h:commandButton value="#{objectBundle.create}"
       image="#{userSessionBean.icons.new}"
       alt="#{objectBundle.create}" title="#{objectBundle.create}"
       action="#{eventBean.create}" immediate="true"
       styleClass="createButton"
       rendered="#{eventSearchBean.editorUser and userSessionBean.menuModel.browserType == 'desktop'}"/>
  </t:div>

  <t:div style="width:100%"
    rendered="#{eventSearchBean.footerBrowser!=null}"
    styleClass="footerDocument">
    <sf:browser binding="#{eventSearchBean.footerBrowser}"
      port="#{applicationBean.defaultPort}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}" />
  </t:div>


</jsp:root>
