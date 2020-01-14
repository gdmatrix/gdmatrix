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
  
  <sf:saveScroll />
  
  <jsp:include page="/common/presence/fragments/header.jsp"/>
    
  <t:div styleClass="absence_counters">  
    <t:div styleClass="toolbar">
      <h:commandLink action="#{absenceCountersBean.previousYear}" styleClass="button img_left"
        title="#{presenceBundle.previousYearInfo}">
        <t:outputText value="#{objectBundle.previous}" styleClass="left" />
      </h:commandLink>    
      <h:commandLink action="#{absenceCountersBean.currentYear}" 
        value="#{absenceCountersBean.year}" styleClass="button" 
        title="#{presenceBundle.currentYearInfo}" />
      <h:commandLink action="#{absenceCountersBean.nextYear}" styleClass="button img_right"
        title="#{presenceBundle.previousYearInfo}">
        <t:outputText value="#{objectBundle.next}" styleClass="right" />
      </h:commandLink>
    </t:div>
    
    <t:dataTable 
      value="#{absenceCountersBean.absenceCounterViews}" 
      var="absenceCounterView" preserveDataModel="false"
      preserveSort="true"
      bodyStyleClass="#{empty absenceCountersBean.absenceCounterViews ? 'empty' : null}"
      styleClass="presence_table">
      <t:column sortable="true" style="width:5%" styleClass="center">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.id}" />
        </f:facet>
        <h:outputText value="#{absenceCounterView.absenceType.absenceTypeId}" />
      </t:column>
      <t:column sortable="true" style="width:41%">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.absenceTypes}" />
        </f:facet>
        <h:outputText value="#{absenceCounterView.absenceType.label}" />
      </t:column>
      <t:column sortable="true" style="width:16%" styleClass="center">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.totalPresenceCounters}" />
        </f:facet>
        <h:outputText value="#{absenceCounterView.absenceCounter.totalTime}" 
                      rendered="#{absenceCounterView.absenceCounter != null and 
                                  absenceCountersBean.editingAbsenceCounterView != absenceCounterView}" />
        <t:inputText value="#{absenceCounterView.absenceCounter.totalTime}" styleClass="input_text"
                     rendered="#{absenceCountersBean.editingAbsenceCounterView == absenceCounterView}" />
      </t:column>
      <t:column sortable="true" style="width:16%" styleClass="center">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.remainingPresenceCounters}" />
        </f:facet>
        <h:outputText value="#{absenceCounterView.absenceCounter.remainingTime}" 
                      rendered="#{absenceCounterView.absenceCounter != null and 
                                  absenceCountersBean.editingAbsenceCounterView != absenceCounterView}" 
                      style="color:#{absenceCounterView.absenceCounter.remainingTime == 0 ? 'red' : 'black'}" />
        <t:inputText value="#{absenceCounterView.absenceCounter.remainingTime}" styleClass="input_text"
                     rendered="#{absenceCountersBean.editingAbsenceCounterView == absenceCounterView}" />
      </t:column>
      <t:column sortable="true" style="width:8%" styleClass="center">
        <f:facet name="header">
          <h:outputText value="#{presenceBundle.count}" />
        </f:facet>
        <h:outputText value="#{absenceCountersBean.counterCounting}" />
      </t:column>
      <t:column sortable="true" styleClass="center" style="width:14%">
        <t:div styleClass="toolbar">
          <h:commandButton action="#{absenceCountersBean.editAbsenceCounter}" image="/common/presence/images/edit.png" 
            styleClass="button" title="#{objectBundle.edit}" alt="#{objectBundle.edit}" 
            rendered="#{absenceCountersBean.editingAbsenceCounterView == null}" />
          <h:commandButton action="#{absenceCountersBean.removeAbsenceCounter}" image="/common/presence/images/remove.png"
            styleClass="button" title="#{objectBundle.remove}" alt="#{objectBundle.remove}" 
            onclick="return confirm('#{presenceBundle.confirmRemoveAbsenceCounter}');" 
            rendered="#{absenceCounterView.absenceCounter != null and absenceCountersBean.editingAbsenceCounterView == null}" />
          <h:commandButton action="#{absenceCountersBean.moveAbsenceCounter}" image="/common/presence/images/copy_next_year.png" 
            rendered="#{absenceCounterView.absenceCounter != null and absenceCountersBean.editingAbsenceCounterView == null}" styleClass="button" 
            alt="#{presenceBundle.moveNextYear}" title="#{presenceBundle.moveNextYear}" />
          <h:commandButton action="#{absenceCountersBean.storeAbsenceCounter}" image="/common/presence/images/accept.png" 
            rendered="#{absenceCountersBean.editingAbsenceCounterView == absenceCounterView}" styleClass="button" 
            alt="#{objectBundle.accept}" title="#{objectBundle.accept}" />
          <h:commandButton action="#{absenceCountersBean.cancelAbsenceCounter}" image="/common/presence/images/cancel.png" 
            rendered="#{absenceCountersBean.editingAbsenceCounterView == absenceCounterView}" styleClass="button" 
            alt="#{objectBundle.cancel}" immediate="true" title="#{objectBundle.cancel}" />
        </t:div>
      </t:column>
    </t:dataTable>
    <t:div>
      <t:outputText value="#{presenceBundle.absenceTypes}:" style="font-size:13px"/>
      <t:selectOneMenu value="#{absenceCountersBean.absenceTypeId}">
        <f:selectItem itemValue="" itemLabel=" " />
        <f:selectItems value="#{absenceCountersBean.absenceTypeSelectItems}" />
      </t:selectOneMenu>
      <t:div styleClass="toolbar" style="margin-left:4px">
        <t:commandButton value="#{presenceBundle.createAbsenceCounter}" 
          action="#{absenceCountersBean.createAbsenceCounter}" onclick="showOverlay()"
          styleClass="button" />
        <t:commandButton value="#{presenceBundle.createAbsenceCounters}" 
          action="#{absenceCountersBean.createAbsenceCounters}" onclick="showOverlay()"
          styleClass="button" />
        <t:commandButton value="#{presenceBundle.createAllAbsenceCounters}" 
          action="#{absenceCountersBean.createAllAbsenceCounters}" onclick="showOverlay()"
          styleClass="button" />
      </t:div>
    </t:div>
  </t:div>
  
</jsp:root>

