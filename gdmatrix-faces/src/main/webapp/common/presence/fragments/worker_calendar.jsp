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

  <t:div>
    <t:div styleClass="toolbar">
      <h:commandLink action="#{workerCalendarBean.previousYear}" styleClass="button img_left"
        title="#{presenceBundle.previousYearInfo}">
        <t:outputText value="#{objectBundle.previous}" styleClass="left" />
      </h:commandLink>    
      <h:commandLink action="#{workerCalendarBean.currentYear}" 
        title="#{presenceBundle.currentYearInfo}"
        value="#{workerCalendarBean.year}" styleClass="button" />
      <h:commandLink action="#{workerCalendarBean.nextYear}" styleClass="button img_right"
        title="#{presenceBundle.nextYearInfo}">
        <t:outputText value="#{objectBundle.next}" styleClass="right" />
      </h:commandLink>
    </t:div>
    
    <h:outputText value="#{presenceBundle.absenceCount}:" styleClass="absence_counter_label" />
    <h:outputText value="#{workerCalendarBean.absenceCount}" 
      styleClass="absence_counter">
    </h:outputText>

    <h:outputText value="#{presenceBundle.requestedTime}:" styleClass="absence_counter_label" />
    <h:outputText value="#{workerCalendarBean.absenceRequestedTime}" 
      styleClass="absence_counter">
      <f:converter converterId="IntervalConverter" />
    </h:outputText>
    
    <h:outputText value="#{presenceBundle.consolidatedTime}:" styleClass="absence_counter_label" /> 
    <h:outputText value="#{workerCalendarBean.absenceConsolidatedTime}" 
      styleClass="absence_counter">
      <f:converter converterId="IntervalConverter" />
    </h:outputText>    
  </t:div>
  
  <t:div styleClass="worker_calendar">
    <t:dataList value="#{workerCalendarBean.months}" var="month" >
      <t:div styleClass="month">
        <h:outputText value="#{month.name}" styleClass="month_name" />
        <h:outputText value="ST" styleClass="week_of_year" />
        <t:dataList value="#{presenceConfigBean.daysOfWeek}" var="dayOfWeek" rowIndexVar="headerIndex">
          <h:outputText value="#{dayOfWeek}" styleClass="day_header col#{headerIndex % 7}" />
        </t:dataList>
        <t:dataList value="#{month.weeks}" var="week">
          <t:div styleClass="week">
            <h:outputText value="#{week.weekOfYear}" styleClass="week_of_year" />
            <t:dataList value="#{week.days}" var="day" rowIndexVar="index">
              <t:commandLink value="#{day.dayOfMonth}" 
                styleClass="day #{day.visible ? 'visible' : 'hidden'}#{day.visible and workerCalendarBean.today ? ' today' : ''} col#{index}" 
                style="#{day.style}" title="#{day.title}"
                action="#{day.showWeek}" />
            </t:dataList>
          </t:div>
        </t:dataList>
      </t:div>
    </t:dataList>
  </t:div>
  
  <t:div styleClass="absence_counters_bars" rendered="#{not empty workerCalendarBean.absenceCounters}">
    <t:panelGroup styleClass="counter">
      <h:outputText value="#{presenceBundle.availableTime} (#{workerCalendarBean.absencesYear}):" />
    </t:panelGroup>
    <t:dataList value="#{workerCalendarBean.absenceCounters}" var="counter">
      <t:panelGroup styleClass="counter">
        <h:outputText value="#{counter.absenceType.label}" styleClass="counterType" />
        <t:div styleClass="progressBar">
          <t:div style="width:#{100 * counter.absenceCounter.remainingTime / counter.absenceCounter.totalTime}%"></t:div>
        </t:div>
        <t:div styleClass="days">
          <h:outputText value="#{counter.absenceCounter.remainingTime}">
            <f:convertNumber type="number" groupingUsed="true" pattern="#0.##"/>
          </h:outputText>
          <h:outputText value=" / " />
          <h:outputText value="#{counter.absenceCounter.totalTime}">
            <f:convertNumber type="number" groupingUsed="true" pattern="#0.##"/>
          </h:outputText>
          <h:outputText value=" #{workerCalendarBean.counterCounting}" />
        </t:div>
      </t:panelGroup>
    </t:dataList>
  </t:div>            
  
</jsp:root>

