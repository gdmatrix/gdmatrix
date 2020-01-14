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
  
  <t:div styleClass="presence_stats">

    <t:panelGrid columns="4" columnClasses="c1,c2,c3,c4" styleClass="stats_filter">
      <t:outputLabel value="#{presenceBundle.period}" for="stats_filter_period" />
      <t:outputLabel value="#{presenceBundle.startDate}" for="stats_filter_start_date" />
      <t:outputLabel value="#{presenceBundle.endDate}" for="stats_filter_end_date" />
      <t:outputText value="" />

      <sf:commandMenu id="stats_filter_period" 
        value="#{presenceStatsBean.period}"
        action="#{presenceStatsBean.search}">
        <f:selectItems value="#{presenceStatsBean.periodSelectItems}" />
        <f:selectItem itemValue="" itemLabel="Entre dates" /> 
      </sf:commandMenu>
      <sf:calendar id="stats_filter_start_date" value="#{presenceStatsBean.startDate}" styleClass="date" 
         disabled="#{not presenceStatsBean.datesEnabled}" />
      <sf:calendar id="stats_filter_end_date" value="#{presenceStatsBean.endDate}" styleClass="date" 
         disabled="#{not presenceStatsBean.datesEnabled}" />
      <t:commandLink action="#{presenceStatsBean.search}" styleClass="button img_left" onclick="showOverlay()">
        <t:outputText value="#{objectBundle.update}" styleClass="refresh" />
      </t:commandLink>
    </t:panelGrid>

    <t:div rendered="#{presenceStatsBean.workerStatistics != null}" styleClass="stats_panel">
      <t:div styleClass="row ok" title="#{presenceBundle.daysToWorkInfo}">
        <h:outputText value="#{presenceBundle.daysToWork}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.daysToWork}" />
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.timeToWorkInfo}">
        <h:outputText value="#{presenceBundle.timeToWork}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.timeToWork}">
          <f:converter converterId="IntervalConverter" />
        </h:outputText>
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.instantTimeToWorkInfo}">
        <h:outputText value="#{presenceBundle.instantTimeToWork}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.instantTimeToWork}">
          <f:converter converterId="IntervalConverter" />
        </h:outputText>
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.workedDaysInfo}">
        <h:outputText value="#{presenceBundle.workedDays}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.workedDays}" />
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.notWorkedDays gt 0 ? ' warning' : ' ok'}"
             title="#{presenceBundle.notWorkedDaysInfo}">
        <h:outputText value="#{presenceBundle.notWorkedDays}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.notWorkedDays}" />
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.workedTimeInfo}">
        <h:outputText value="#{presenceBundle.workedTime}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.workedTime}">
          <f:converter converterId="IntervalConverter" />
        </h:outputText>
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.realWorkedTimeInfo}">
        <h:outputText value="#{presenceBundle.realWorkedTime}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.realWorkedTime}">
          <f:converter converterId="IntervalConverter" />
        </h:outputText>
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.absenceTimeInfo}">
        <h:outputText value="#{presenceBundle.absenceTime}:" styleClass="label" /> 
        <h:outputText value="#{presenceStatsBean.workerStatistics.absenceTime}">
          <f:converter converterId="IntervalConverter" />
        </h:outputText>
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.scheduleFaultTime gt 0 ? ' warning' : ' ok'}"
        title="#{presenceBundle.scheduleFaultTimeInfo}">
        <h:outputText value="#{presenceBundle.scheduleFaultTime}:" styleClass="label" /> 
        <h:outputText value="#{presenceStatsBean.workerStatistics.scheduleFaultTime}">
          <f:converter converterId="IntervalConverter" />
        </h:outputText>
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.scheduleFaults gt 0 ? ' warning' : ' ok'}"
        title="#{presenceBundle.scheduleFaultsInfo}">
        <h:outputText value="#{presenceBundle.scheduleFaults}:" styleClass="label" /> 
        <h:outputText value="#{presenceStatsBean.workerStatistics.scheduleFaults}" />
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.entryCountInfo}">
        <h:outputText value="#{presenceBundle.entryCount}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.entryCount}" />
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.manipulatedEntryCount gt 0 ? ' warning' : ' ok'}"
        title="#{presenceBundle.manipulatedEntryCountInfo}">
        <h:outputText value="#{presenceBundle.manipulatedEntryCount}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.manipulatedEntryCount}" />
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.suspiciousEntryCount gt 0 ? ' warning' : ' ok'}"
        title="#{presenceBundle.suspiciousEntryCountInfo}">
        <h:outputText value="#{presenceBundle.suspiciousEntryCount}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.suspiciousEntryCount}" />
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.compensatedEntryCountInfo}" 
             rendered="#{presenceStatsBean.workerStatistics.compensatedEntryCount gt 0}">
        <h:outputText value="#{presenceBundle.compensatedEntryCount}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.compensatedEntryCount}" />
      </t:div>
      <t:div styleClass="row ok" title="#{presenceBundle.compensationTimeInfo}" 
        rendered="#{presenceStatsBean.workerStatistics.compensationTime gt 0}">
        <h:outputText value="#{presenceBundle.compensationTime}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.compensationTime}">
          <f:converter converterId="IntervalConverter" />
        </h:outputText>
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.workedTimeDifference lt 0 ? ' warning' : ' ok'}"
        title="#{presenceBundle.workedTimeDifferenceInfo}">
        <h:outputText value="#{presenceBundle.workedTimeDifference}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.workedTimeDifference}">
          <f:converter converterId="IntervalConverter" />
          <f:attribute name="positiveSignum" value="true" />
        </h:outputText>
      </t:div>
      <t:div styleClass="row ok"
        title="#{presenceBundle.absenceDegreeInfo}">
        <h:outputText value="#{presenceBundle.absenceDegree}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.absenceDegree}">
          <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
        </h:outputText>
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.complianceDegree lt 100 ? ' warning' : ' ok'}"
        title="#{presenceBundle.complianceDegreeInfo}">
        <h:outputText value="#{presenceBundle.complianceDegree}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.complianceDegree}">
          <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
        </h:outputText>
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.punctualityDegree lt 100 ? ' warning' : ' ok'}"
        title="#{presenceBundle.punctualityDegreeInfo}">
        <h:outputText value="#{presenceBundle.punctualityDegree}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.punctualityDegree}">
          <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
        </h:outputText>
      </t:div>
      <t:div styleClass="row#{presenceStatsBean.workerStatistics.veracityDegree lt 90 ? ' warning' : ' ok'}"
        title="#{presenceBundle.veracityDegreeInfo}">
        <h:outputText value="#{presenceBundle.veracityDegree}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.veracityDegree}">
          <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
        </h:outputText>
      </t:div>
      <t:div styleClass="row ok"
        title="#{presenceBundle.presenceDegreeInfo}">
        <h:outputText value="#{presenceBundle.presenceDegree}:" styleClass="label" />
        <h:outputText value="#{presenceStatsBean.workerStatistics.presenceDegree}">
         <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
        </h:outputText>
      </t:div>
    </t:div>

    <t:panelTabbedPane serverSideTabSwitch="true" 
      selectedIndex="#{presenceStatsBean.selectedTabIndex}" 
      styleClass="tabbed_panel stats">
      <t:panelTab label="#{presenceBundle.mainIndicators}"> 
        <t:div styleClass="main_indicators">
          <t:div styleClass="differential">
            <sf:outputText value="#{presenceStatsBean.currentPeriod ? 
              presenceStatsBean.currentDifferentialMessage : presenceStatsBean.previousDifferentialMessage} " styleClass="diff_message"
              translator="#{userSessionBean.translator}"
              translationGroup="presence" />
            <t:outputText value="#{presenceStatsBean.workerStatistics.workedTimeDifference}" 
              styleClass="diff_value #{presenceStatsBean.workerStatistics.workedTimeDifference lt 0 ? 'ko' : 'ok'}">
              <f:converter converterId="IntervalConverter" />
              <f:attribute name="positiveSignum" value="true" />
            </t:outputText>
          </t:div>
          <t:div id="compliance_gauge" forceId="true" styleClass="gauge" 
            title="#{presenceBundle.complianceDegreeInfo}">
            <t:outputText value="#{presenceBundle.complianceDegree}" styleClass="label" />
            <h:outputText value="#{presenceStatsBean.workerStatistics.complianceDegree}" styleClass="value">
              <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
            </h:outputText>
          </t:div>
          <t:div id="punctuality_gauge" forceId="true" styleClass="gauge" 
            title="#{presenceBundle.punctualityDegreeInfo}">
            <t:outputText value="#{presenceBundle.punctualityDegree}" styleClass="label" />
            <h:outputText value="#{presenceStatsBean.workerStatistics.punctualityDegree}" styleClass="value">
             <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
            </h:outputText>
          </t:div>
          <t:div id="veracity_gauge" forceId="true" styleClass="gauge" 
            title="#{presenceBundle.veracityDegreeInfo}">
            <t:outputText value="#{presenceBundle.veracityDegree}" styleClass="label" />
            <h:outputText value="#{presenceStatsBean.workerStatistics.veracityDegree}" styleClass="value">
              <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
            </h:outputText>            
          </t:div>
          <t:div id="presence_gauge" forceId="true" styleClass="gauge"
            title="#{presenceBundle.presenceDegreeInfo}">
            <t:outputText value="#{presenceBundle.presenceDegree}" styleClass="label" />
            <h:outputText value="#{presenceStatsBean.workerStatistics.presenceDegree}" styleClass="value">
             <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00'%'"/>
            </h:outputText>
          </t:div>
        </t:div>
        <h:outputText value="#{presenceStatsBean.gaugeScript}" escape="false" />
      </t:panelTab>
      <t:panelTab label="#{presenceBundle.entriesByType}">
        <t:div styleClass="entries_by_type">
          <t:dataTable styleClass="presence_table"
            bodyStyleClass="#{empty presenceStatsBean.entryTypeStatistics ? 'empty' : null}"
            value="#{presenceStatsBean.entryTypeStatistics}" var="entryTypeStats">
            <t:column sortable="true">
              <f:facet name="header">
                <t:outputText value="#{presenceBundle.presenceEntryType}" />
              </f:facet>
              <t:outputText value="#{entryTypeStats.label}" />
            </t:column>
            <t:column sortable="true">
              <f:facet name="header">
                <t:outputText value="#{presenceBundle.entryCount}" />
              </f:facet>
              <t:outputText value="#{entryTypeStats.count}" />
            </t:column>
            <t:column sortable="true">
              <f:facet name="header">
                <t:outputText value="#{presenceBundle.total_time}" />
              </f:facet>
              <t:outputText value="#{entryTypeStats.duration}">
                <f:converter converterId="IntervalConverter" />
              </t:outputText>
            </t:column>
            <t:column sortable="true">
              <f:facet name="header">
                <t:outputText value="#{presenceBundle.entriesPerDay}" />
              </f:facet>
              <t:outputText value="#{entryTypeStats.entriesPerDay}">
                <f:convertNumber type="number" groupingUsed="true" minFractionDigits="2" pattern="#0.00"/>
              </t:outputText>
            </t:column>
            <t:column sortable="true">
              <f:facet name="header">
                <t:outputText value="#{presenceBundle.durationPerEntry}" />
              </f:facet>
              <t:outputText value="#{entryTypeStats.durationPerEntry}">
                <f:converter converterId="IntervalConverter" />
              </t:outputText>
            </t:column>
          </t:dataTable>
        </t:div>
      </t:panelTab>
      <t:panelTab label="#{presenceBundle.evaluation}" rendered="#{not presenceStatsBean.currentPeriod}">
        <t:div id="face_panel" forceId="true" title="" >
          <t:div styleClass="arrow_box">
            <sf:outputText value="#{presenceStatsBean.previousDifferentialMessage} " styleClass="diff_message"
              translator="#{userSessionBean.translator}"
              translationGroup="presence" />
            <t:outputText value="#{presenceStatsBean.workerStatistics.workedTimeDifference}" 
              styleClass="diff_value #{presenceStatsBean.workerStatistics.workedTimeDifference lt 0 ? 'ko' : 'ok'}">
              <f:converter converterId="IntervalConverter" />
              <f:attribute name="positiveSignum" value="true" />
            </t:outputText>
          </t:div>
        </t:div>
        <sf:outputText value="#{presenceStatsBean.faceMessage} "
          translator="#{userSessionBean.translator}" styleClass="face_info bold"
          translationGroup="presence" />
        <t:outputText value="#{presenceBundle.faceInfo}" styleClass="face_info" />
        <h:outputText value="#{presenceStatsBean.faceScript}" escape="false" />
      </t:panelTab>
    </t:panelTabbedPane>
    
  </t:div>
  
</jsp:root>

