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
  
  <t:div styleClass="groupal_view">
    <t:div styleClass="toolbar">
      <t:commandLink action="#{groupalViewBean.previousYear}" styleClass="button img_left"
        title="#{presenceBundle.previousYearInfo}">
        <t:outputText value="#{presenceBundle.year}" styleClass="left" />
      </t:commandLink>
      <t:commandLink action="#{groupalViewBean.previousMonth}" styleClass="button img_left"
        title="#{presenceBundle.previousMonthInfo}">
        <t:outputText value="#{presenceBundle.month}" styleClass="left" />
      </t:commandLink>
      <t:commandLink action="#{groupalViewBean.previousWeek}" styleClass="button img_left"
        title="#{presenceBundle.previousWeekInfo}">
        <t:outputText value="#{presenceBundle.week}" styleClass="left" />
      </t:commandLink>
      <t:commandLink action="#{groupalViewBean.goToday}" value="#{presenceBundle.today}" styleClass="button" 
        title="#{presenceBundle.todayInfo}" />
      <t:commandLink action="#{groupalViewBean.nextWeek}" styleClass="button img_right"
        title="#{presenceBundle.nextWeekInfo}">
        <t:outputText value="#{presenceBundle.week}" styleClass="right" />
      </t:commandLink>
      <t:commandLink action="#{groupalViewBean.nextMonth}" styleClass="button img_right"
        title="#{presenceBundle.nextMonthInfo}">
        <t:outputText value="#{presenceBundle.month}" styleClass="right" />
      </t:commandLink>
      <t:commandLink action="#{groupalViewBean.nextYear}" styleClass="button img_right"
        title="#{presenceBundle.nextYearInfo}">
        <t:outputText value="#{presenceBundle.year}" styleClass="right" />
      </t:commandLink>
    </t:div>
    <t:div styleClass="toolbar" style="margin-left:6px">
      <t:commandLink action="#{groupalViewBean.saveView}" styleClass="button img_left"
        title="#{presenceBundle.saveView}">
        <t:outputText value="#{presenceBundle.saveView}" styleClass="save_view" />
      </t:commandLink>
    </t:div>
    <t:div styleClass="toolbar" style="margin-left:6px">
      <h:outputLink onclick="javascript:printGroup('groupal_view');return false;" styleClass="button img_left">
        <t:outputText value="#{webBundle.buttonPrint}" styleClass="print" />
      </h:outputLink>
    </t:div>
    <sf:printableGroup name="groupal_view">
      <t:div styleClass="weeks_header">
        <t:div styleClass="toggler_panel">
          <t:outputText value="Selecciona-ho tot" onclick="setSelection(true);" styleClass="toggler" />
          <t:outputText value="Deselecciona-ho tot" onclick="setSelection(false);" styleClass="toggler" />
        </t:div>
        <t:div styleClass="days">
          <t:outputText value="#{groupalViewBean.monthYear}" styleClass="month_year"/>
          <t:dataList value="#{groupalViewBean.days}" var="day" rowIndexVar="index">
            <t:commandLink value="#{day.label}" styleClass="day#{groupalViewBean.today ? ' today' : ''}#{day.selected ? ' selected' : ''} col#{index % 7}" 
                          title="#{day.holiday == null ? null : day.holiday.description}"
                          disabled="#{not presenceMainBean.presenceAdministrator}"
                          style="#{day.style}" action="#{groupalViewBean.captureDate}" />
          </t:dataList>
        </t:div>
      </t:div>
      <t:div styleClass="scroll" forceId="true" id="scroll_div">
        <t:dataList value="#{groupalViewBean.workerSchedule}" var="schedule" rowIndexVar="workerIndex">
          <t:div styleClass="worker#{groupalViewBean.selection[workerIndex] ? ' selected' : ''}">
            <t:selectBooleanCheckbox id="worker_selector" onclick="updateWorkerClass(this)"
              value="#{groupalViewBean.selection[workerIndex]}" styleClass="selector" />
            <t:outputLabel for="worker_selector" value="#{groupalViewBean.worker.fullName}" styleClass="worker_name" 
                           title="#{groupalViewBean.worker.fullName}" />
            <t:div styleClass="days">
              <t:dataList value="#{schedule.dayTypeId}" var="dayTypeId" rowIndexVar="index">
                <t:commandLink styleClass="day col#{index % 7}#{groupalViewBean.daySelected ? ' selected' : ''}" 
                  action="#{groupalViewBean.captureDateForWorker}" 
                  disabled="#{not presenceMainBean.presenceAdministrator}">
                  <t:outputText value="#{groupalViewBean.dayTypeCode}"
                    rendered="#{dayTypeId != null}" styleClass="work" 
                    style="background-color:#{groupalViewBean.dayTypeColor}"
                    title="#{groupalViewBean.dayTypeLabel}" />
                  <t:div rendered="#{groupalViewBean.absenceDay != null}" styleClass="absence"
                    title="#{groupalViewBean.absenceDayInfo}">
                    <t:outputText value="#{groupalViewBean.absenceDayStatus}" styleClass="status" 
                      title="#{groupalViewBean.absenceDayStatusLabel}" />
                  </t:div>
                </t:commandLink>
              </t:dataList>
              <h:commandButton action="#{groupalViewBean.showWorkerWeek}" image="/common/presence/images/show.png" 
                rendered="#{groupalViewBean.workerVisible}" styleClass="show_worker_button"
                alt="#{objectBundle.show}" title="#{objectBundle.show}" />
            </t:div>
          </t:div>
        </t:dataList>
      </t:div>
    </sf:printableGroup>
    <t:panelTabbedPane styleClass="tabbed_panel footer">
      <t:panelTab label="#{presenceBundle.workerSelection}">
        <t:div styleClass="row">
          <t:outputText value="#{presenceBundle.selectedWorkers}:" styleClass="output_text" />
          <t:outputText value="" id="selection_count" forceId="true" styleClass="selection_counter" />
          <t:panelGroup styleClass="toolbar" id="selection_toolbar" forceId="true">
            <t:commandButton value="#{presenceBundle.removeSelectedWorkers}" 
              action="#{groupalViewBean.removeSelectedWorkers}" styleClass="button first" onclick="showOverlay()" />
            <t:commandButton value="#{presenceBundle.addTeamMates}" 
              action="#{groupalViewBean.addTeamMates}" styleClass="button" onclick="showOverlay()" />        
            <t:commandButton value="#{presenceBundle.addValidatedWorkers}" 
              action="#{groupalViewBean.addValidatedWorkers}" styleClass="button" onclick="showOverlay()" />        
            <t:commandButton value="#{presenceBundle.addAllValidatedWorkers}" 
              action="#{groupalViewBean.addAllValidatedWorkers}" styleClass="button" onclick="showOverlay()" />
          </t:panelGroup>
        </t:div>
        <t:div styleClass="row toolbar">
          <t:outputLabel value="#{presenceBundle.addWorker}:" for="worker_name" styleClass="output_text" />
          <t:inputText id="worker_name" value="#{groupalViewBean.workerToAdd}" styleClass="input_text" />
          <t:outputLabel value="#{presenceBundle.addTeam}:" for="team_name" styleClass="output_text" />
          <t:inputText id="team_name" value="#{groupalViewBean.teamToAdd}" styleClass="input_text" />
          <t:commandButton value="#{presenceBundle.apply}"
            action="#{groupalViewBean.addWorker}" styleClass="button first" onclick="showOverlay()" />
        </t:div>
      </t:panelTab>
      <t:panelTab label="#{presenceBundle.scheduleAssignment}" disabled="#{not presenceMainBean.presenceAdministrator}">
        <t:outputText rendered="#{not presenceMainBean.presenceAdministrator}"
          value="#{presenceBundle.notPresenceAdministrator}" />
        <h:panelGroup rendered="#{presenceMainBean.presenceAdministrator}">
          <t:div styleClass="row" rendered="#{groupalViewBean.periodStartDate == null}">
            <t:outputText value="#{presenceBundle.selectDate}" />
          </t:div>
          <t:div styleClass="row" rendered="#{groupalViewBean.periodStartDate != null}">
            <t:outputText value="#{presenceBundle.sinceDate} #{groupalViewBean.periodStartDateFormatted}" styleClass="words" />
            <t:outputText value="#{presenceBundle.toDate} #{groupalViewBean.periodEndDateFormatted}" styleClass="words" 
              rendered="#{groupalViewBean.periodEndDate != null}" />
            <t:outputText value="#{presenceBundle.assignWeekType}" styleClass="words" />
            <t:selectOneMenu value="#{groupalViewBean.weekTypeId}" styleClass="input_text">
              <f:selectItem itemLabel=" " itemValue="" />
              <f:selectItems value="#{presenceConfigBean.weekTypeSelectItems}" />
            </t:selectOneMenu>
            <t:commandButton value="#{presenceBundle.apply}" action="#{groupalViewBean.assignWeekType}" 
              styleClass="button" onclick="showOverlay()" />
          </t:div>
          <t:div styleClass="row" rendered="#{groupalViewBean.periodStartDate != null}">
            <t:outputText value="#{presenceBundle.onDate} #{groupalViewBean.periodStartDateFormatted}" styleClass="words" />
            <t:outputText value="#{presenceBundle.assignDayType}" styleClass="words" />
            <t:selectOneMenu value="#{groupalViewBean.dayTypeId}">
              <f:selectItem itemLabel=" " itemValue="" />
              <f:selectItem itemLabel="#{presenceBundle.weekDefault}" itemValue="WEEK_DEFAULT" />
              <f:selectItems value="#{presenceConfigBean.dayTypeSelectItems}" />          
            </t:selectOneMenu>
            <t:commandButton value="#{presenceBundle.apply}" action="#{groupalViewBean.assignDayType}" 
              styleClass="button" onclick="showOverlay()" />
            <t:commandButton value="#{presenceBundle.cancelPeriod}" action="#{groupalViewBean.cancelPeriod}" 
              styleClass="button" style="float:right" />
          </t:div>
        </h:panelGroup>
      </t:panelTab>
      <t:panelTab label="#{presenceBundle.countersManagement}" disabled="#{not presenceMainBean.presenceAdministrator}">
        <t:outputText rendered="#{not presenceMainBean.presenceAdministrator}"
          value="#{presenceBundle.notPresenceAdministrator}" />
        <h:panelGroup rendered="#{presenceMainBean.presenceAdministrator}">
          <t:div styleClass="row toolbar">            
            <t:outputText value="#{presenceBundle.year} #{groupalViewBean.year}: " />
            <t:commandButton value="#{presenceBundle.createAbsenceCounters}" 
              action="#{groupalViewBean.createAbsenceCounters}" styleClass="button" 
              onclick="showOverlay()"/>
            <t:commandButton value="#{presenceBundle.copyAbsenceCounters}" 
              action="#{groupalViewBean.copyAbsenceCounters}" styleClass="button" 
              onclick="showOverlay()"/>
          </t:div>
        </h:panelGroup>
      </t:panelTab>
    </t:panelTabbedPane>
  </t:div>
  
  <t:inputHidden value="#{workerWeekBean.scroll}" forceId="true" id="scroll_input" />
  
  <f:verbatim>
    <script type="text/javascript">

      var scrollDiv = document.getElementById("scroll_div");
      var scrollInput = document.getElementById("scroll_input");

      scrollDiv.addEventListener("scroll", function(event)
      {
        scrollInput.value = scrollDiv.scrollTop;
      }, true);

      scrollDiv.scrollTop = parseFloat(scrollInput.value);

      function setSelection(value)
      {
        var elems = document.getElementsByClassName("selector");
        for (var i = 0; i &lt; elems.length; i++)
        {
          var elem = elems[i];
          elem.checked = value;
          updateWorkerClass(elem);
        }
      }
      
      function updateWorkerClass(elem)
      {
        var value = elem.checked;
        elem.parentNode.className = value ? 'worker selected' : 'worker';
        updateSelectionCounter();
      }
      
      function updateSelectionCounter()
      {
        var counter = 0;
        var elems = document.getElementsByClassName("selector");
        for (var i = 0; i &lt; elems.length; i++)
        {
          var elem = elems[i];
          if (elem.checked) counter++;          
        }
        var counterElem = document.getElementById("selection_count");
        counterElem.innerHTML = counter;
        var selectionToolbarElem = document.getElementById("selection_toolbar");
        selectionToolbarElem.style.visibility = counter == 0 ? 'hidden' : 'visible';
      }      
      updateSelectionCounter();
    </script>
  </f:verbatim>
  
</jsp:root>
