<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.presence.web.resources.PresenceBundle"
    var="presenceBundle" />
  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
    var="objectBundle" />

  <t:inputHidden immediate="true" value="#{presenceMainBean.sessionTrack}" />
  
  <jsp:include page="/common/presence/fragments/header.jsp"/>
    
  <t:div styleClass="absences">
    <t:panelGrid columns="7" columnClasses="c1,c2,c3,c4,c5,c6,c7" styleClass="absence_filter">
      <t:outputLabel value="#{presenceBundle.id}" for="absence_filter_id" />
      <t:outputLabel value="#{presenceBundle.worker}" for="absence_filter_worker" />
      <t:outputLabel value="#{presenceBundle.type}" for="absence_filter_type" />
      <t:outputLabel value="#{presenceBundle.startDate}" for="absence_filter_start_date" />
      <t:outputLabel value="#{presenceBundle.endDate}" for="absence_filter_end_date" />
      <t:outputLabel value="#{presenceBundle.status}" for="absence_filter_status" />
      <t:outputText value="" />
      
      <t:inputText id="absence_filter_id" value="#{absencesBean.filter.absenceId}" />
      <t:inputText id="absence_filter_worker" value="#{absencesBean.workerName}" />
      <t:selectOneMenu id="absence_filter_type" value="#{absencesBean.filter.absenceTypeId}" 
        styleClass="absence_type">
        <f:selectItem itemLabel=" " itemValue="" />
        <f:selectItems value="#{presenceConfigBean.absenceTypeSelectItems}" />
      </t:selectOneMenu>
      <sf:calendar id="absence_filter_start_date" value="#{absencesBean.filter.startDateTime}" styleClass="date" />
      <sf:calendar id="absence_filter_end_date" value="#{absencesBean.filter.endDateTime}" styleClass="date" />
      <t:selectOneMenu id="absence_filter_status" value="#{absencesBean.filter.status}">
        <f:selectItem itemLabel=" " itemValue="" />
        <f:selectItems value="#{presenceConfigBean.absenceStatusSelectItems}" />
      </t:selectOneMenu>
      
      <t:commandLink action="#{absencesBean.search}" styleClass="button img_left" onclick="showOverlay()">
        <t:outputText value="#{objectBundle.search}" styleClass="search" />
      </t:commandLink>
    </t:panelGrid>
  
    <t:div styleClass="toolbar">
      <t:commandButton action="#{absencesBean.statusView}" value="#{presenceBundle.statusView}" 
        styleClass="button#{absencesBean.view == 'status' ? ' selected' : ''}" />
      <t:commandButton action="#{absencesBean.timesView}" value="#{presenceBundle.timesView}" 
        styleClass="button#{absencesBean.view == 'times' ? ' selected' : ''}" />
    </t:div>
    
    <t:dataTable 
      value="#{absencesBean.absences}" var="absenceView" preserveDataModel="false"
      sortColumn="#{absencesBean.sortColumn}" preserveSort="true"
      rowStyleClass="status_row #{absenceView.absence.status}"
      bodyStyleClass="#{empty absencesBean.absences ? 'empty' : null}"
      styleClass="presence_table">
      <t:column sortable="true" styleClass="center" style="width:5%">
        <f:facet name="header">
          <h:outputText value="Id" />
        </f:facet>
        <h:outputText value="#{absenceView.absence.absenceId}" />  
      </t:column>
      <t:column sortable="true" style="width:21%">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.worker}" />
        </f:facet>
        <h:outputText value="#{absenceView.worker.fullName}" />
      </t:column>
      <t:column sortable="true" style="width:21%">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.type}" />
        </f:facet>
        <h:outputText value="#{absenceView.absenceType.label}" />     
      </t:column>
      <t:column sortable="true" styleClass="center" style="width:11%">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.startDate}" />
        </f:facet>
        <h:outputText value="#{absenceView.absence.startDateTime}">
          <f:converter converterId="DateTimeConverter" />
          <f:attribute name="userFormat" value="dd/MM/yyyy H:mm" />
          <f:attribute name="internalFormat" value="yyyyMMddHHmmss" />
        </h:outputText>
      </t:column>
      <t:column sortable="true" styleClass="center" style="width:11%">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.endDate}" />
        </f:facet>
        <h:outputText value="#{absenceView.absence.endDateTime}">
          <f:converter converterId="DateTimeConverter" />
          <f:attribute name="userFormat" value="dd/MM/yyyy H:mm" />
          <f:attribute name="internalFormat" value="yyyyMMddHHmmss" />
        </h:outputText>
      </t:column>
      <t:column sortable="true" styleClass="center" style="width:6%" rendered="#{absencesBean.view == 'times'}">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.days}" />
        </f:facet>
        <h:outputText value="#{absenceView.absence.requestedDays}">
          <f:convertNumber type="number" groupingUsed="true" pattern="#0.##"/>
        </h:outputText>
      </t:column>
      <t:column sortable="true" styleClass="center" style="width:8%" rendered="#{absencesBean.view == 'times'}">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.requestedTime}" />
        </f:facet>
        <h:outputText value="#{absenceView.absence.requestedTime}">
          <f:converter converterId="IntervalConverter" />          
        </h:outputText>
      </t:column>
      <t:column sortable="true" styleClass="center" style="width:8%" rendered="#{absencesBean.view == 'times'}">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.consolidatedTime}" />
        </f:facet>
        <h:outputText value="#{absenceView.absence.consolidatedTime}">
          <f:converter converterId="IntervalConverter" />          
        </h:outputText>
      </t:column>    
      <t:column sortable="true" styleClass="status_cell center" 
        style="width:22%" rendered="#{absencesBean.view == 'status'}" sortPropertyName="status">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.status}" />
        </f:facet>
        <h:outputText value="#{absencesBean.absenceStatusTitle}" styleClass="status" title="#{absenceView.absence.instanceId}" />     
        <h:outputText value="#{absenceView.absence.statusDetail}" styleClass="status_detail" />
      </t:column>
      <t:column sortable="true" styleClass="center">
        <t:div styleClass="toolbar">
          <h:commandButton action="#{absencesBean.showAbsence}" image="/common/presence/images/show.png" 
            styleClass="button" title="#{objectBundle.show}" alt="#{objectBundle.show}" />
          <h:commandButton action="#{absencesBean.processAbsence}" image="/common/presence/images/process.png"
            styleClass="button" rendered="#{absencesBean.processAbsenceEnabled}"
            title="#{presenceBundle.processAbsence}" alt="#{presenceBundle.processAbsence}" />
        </t:div>
      </t:column>    
    </t:dataTable>
    <t:div styleClass="footer">
      <t:outputText value="#{presenceBundle.absenceCount}: #{absencesBean.absenceCount}" />
      <h:panelGroup rendered="#{not empty absencesBean.absences}">
        <t:outputText value="#{presenceBundle.days}: " styleClass="total" />
        <t:outputText value="#{absencesBean.totalDays}">
          <f:convertNumber maxFractionDigits="2" />
        </t:outputText>        
        <t:outputText value="#{presenceBundle.requestedTime}: " styleClass="total" />
        <t:outputText value="#{absencesBean.totalRequestedTime}">
          <f:converter converterId="IntervalConverter" />
        </t:outputText>
        <t:outputText value="#{presenceBundle.consolidatedTime}: " styleClass="total" />
        <t:outputText value="#{absencesBean.totalConsolidatedTime}">
          <f:converter converterId="IntervalConverter" />
        </t:outputText>
      </h:panelGroup>
    </t:div>
  </t:div>
  
</jsp:root>

