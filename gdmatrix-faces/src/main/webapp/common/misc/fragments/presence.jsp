<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />  

  <f:loadBundle basename="org.santfeliu.misc.presence.web.resources.PresenceBundle"
    var="presenceBundle" />

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    globalOnly="true"
    layout="table"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />

  <h:panelGrid styleClass="presencePanel"
    columns="2" width="100%" columnClasses="col1,col2" headerClass="header">
    <f:facet name="header">
      <h:panelGroup>
        <h:panelGroup styleClass="date">
          <h:commandLink action="#{presenceBean.previousDay}" immediate="true"
            styleClass="arrow">
            <t:graphicImage value="/images/previous.gif" alt="" title="" />
          </h:commandLink>
          <h:outputText value="#{presenceBean.currentDate}" 
            styleClass="#{presenceBean.today ? 'today' : 'other'}">
            <f:convertDateTime pattern="EEEE, dd MMMM yyyy" />
          </h:outputText>
          <h:commandLink action="#{presenceBean.nextDay}" immediate="true"
            styleClass="arrow" rendered="#{not presenceBean.today}">
            <t:graphicImage value="/images/next.gif" alt="" title="" />
          </h:commandLink>
          <h:commandButton value="#{presenceBundle.today}" immediate="true"
            action="#{presenceBean.goToday}" onclick="showOverlay()"
            styleClass="#{presenceBean.today ? 'todayButton' : 'notTodayButton'}" />
        </h:panelGroup>
        <t:commandLink action="#{presenceBean.showWorkerProfile}" immediate="true"
          styleClass="workerInfoLink" onclick="showOverlay();">
          <h:outputText value="#{presenceBean.workerProfile.displayName}"
             styleClass="displayName" />
        </t:commandLink>
      </h:panelGroup>
    </f:facet>

    <h:panelGroup>
      <t:dataList layout="list" value="#{presenceBean.entries}" var="entry"
        styleClass="entryList" rowIndexVar="rowIndex" rendered="#{not empty presenceBean.entries}">
          <h:commandLink action="#{presenceBean.editEntry}"
            styleClass="#{entry.manipulated ? 'manipulated' : 'entry'}">
            <t:graphicImage
              value="/common/misc/images/#{presenceBean.workEntry ? 'presence_work' : 'presence_nowork'}.gif"
              alt="" title="" styleClass="entryIcon" />
            <h:outputText value="#{entry.date}" styleClass="entryTime">
              <f:convertDateTime pattern="HH:mm:ss" />
            </h:outputText>
            <h:outputText value="#{presenceBean.entryLabel}"
              styleClass="entryType" />
            <h:graphicImage value="/common/misc/images/bonus.gif"
              rendered="#{entry.bonified}" styleClass="bonusIcon" 
              alt="#{presenceBundle.bonusEntry}"
              title="#{presenceBundle.bonusEntry}"/>
          </h:commandLink>
          <t:div styleClass="#{entry.manipulated ? 'manipulatedReason' : 'entryReason'}"
            rendered="#{entry.reason != null}">
            <h:outputText value="#{entry.reason}" />
          </t:div>
          <t:div styleClass="#{presenceBean.workEntry ? 'workInterval' : 'noworkInterval'}"
            rendered="#{presenceBean.entryDuration gt 0}">
            <t:graphicImage url="/common/misc/images/interval.png"
              alt="" title="" />
            <h:outputText value="#{presenceBundle.interval}: #{presenceBean.entryDurationFormatted}"
              title="#{entry.duration == 0 ? presenceBundle.lastIntervalInfo : presenceBundle.intervalInfo}" />
          </t:div>
      </t:dataList>

      <t:panelGrid columns="4" styleClass="timeSummary" headerClass="header"
        columnClasses="first,second,third,fourth">
        <h:outputText value="" />
        <h:outputText value="#{presenceBundle.workDay}:" styleClass="time_header" />
        <h:outputText value="#{presenceBundle.workWeek}:" styleClass="time_header" />
        <h:outputText value="#{presenceBundle.workYear} (*):" styleClass="time_header" />
        
        <h:outputText value="#{presenceBundle.workedTime}:" styleClass="time_header" />
        <h:outputText value="#{presenceBean.workedTimeInDayFormatted}" />
        <h:outputText value="#{presenceBean.workedTimeInWeekFormatted}" />
        <h:outputText value="#{presenceBean.workedTimeInYearFormatted}" />

        <h:outputText value="#{presenceBundle.bonusTime}:" styleClass="time_header" />
        <h:outputText value="#{presenceBean.bonusTimeInDayFormatted}" />
        <h:outputText value="#{presenceBean.bonusTimeInWeekFormatted}" />
        <h:outputText value="#{presenceBean.bonusTimeInYearFormatted}" />

        <h:outputText value="#{presenceBundle.totalTime}:" styleClass="time_header" />
        <h:outputText value="#{presenceBean.totalTimeInDayFormatted}" />
        <h:outputText value="#{presenceBean.totalTimeInWeekFormatted}" />
        <h:outputText value="#{presenceBean.totalTimeInYearFormatted}" />
      </t:panelGrid>
      <h:outputText value="(*) #{presenceBundle.yearTimesHelp}" style="line-height:20px; font-size:12px" />    
    </h:panelGroup>
      
    <h:panelGroup>
      <h:commandLink action="#{presenceBean.editTime}" styleClass="editTime"
         rendered="#{not presenceBean.editingTime}">
        <sf:clock styleClass="clock" format="time" />
      </h:commandLink>
      <t:panelGroup
         rendered="#{presenceBean.editingTime}"
         styleClass="editTimePanel">
        <h:outputText value="#{presenceBundle.hourPattern}:" styleClass="hourLabel" />
        <t:inputText id="timeInput" forceId="true"
          value="#{presenceBean.time}" styleClass="clock" />
        <h:outputText value="#{presenceBundle.reason}:" styleClass="reasonLabel" />
        <h:inputTextarea rows="3" value="#{presenceBean.reason}"
          styleClass="reasonText" onkeypress="checkMaxLength(this,100)"/>
      </t:panelGroup>

      <t:dataList layout="list" id="entryTypes" forceId="true"
        value="#{presenceBean.entryTypes}" var="entryType"        
        styleClass="incidencesPanel">
        <h:panelGroup rendered="#{presenceBean.entryTypeEnabled or presenceBean.editingTime}"
          styleClass="markButton">
          <t:commandButton action="#{presenceBean.mark}" onclick="showOverlay();"
            value="#{entryType.label}" accesskey="#{entryType.key}" />
          <h:outputText value="#{entryType.key}" styleClass="buttonKey" />
        </h:panelGroup>
      </t:dataList>
      <h:panelGroup rendered="#{presenceBean.editingTime and presenceBean.today}"
        styleClass="specialButton">
        <t:commandButton id="cancelButton" forceId="true"
          action="#{presenceBean.cancel}" onclick="showOverlay();"
          value="#{presenceBundle.cancel}" accesskey="*" />
        <h:outputText value="*" styleClass="buttonKey" />
      </h:panelGroup>
      <h:panelGroup rendered="#{not empty presenceBean.entries and not presenceBean.editingTime}"
        styleClass="specialButton">
        <t:commandButton id="editButton" forceId="true"
           action="#{presenceBean.editLastEntry}" onclick="showOverlay();"
           value="#{presenceBundle.editLast}" accesskey="-" />
        <h:outputText value="-" styleClass="buttonKey" />
      </h:panelGroup>
      <h:panelGroup styleClass="specialButton">
        <t:commandButton id="closeButton" forceId="true" 
          action="#{presenceBean.close}" onclick="showOverlay();" accesskey="."
          value="#{presenceBundle.close}" immediate="true" />
        <h:outputText value="." styleClass="buttonKey" />
      </h:panelGroup>

      <h:outputText value="#{presenceBundle.option}:" />
      <t:inputText id="option" forceId="true" maxlength="1" size="1"
        styleClass="option" onkeyup="presenceKeyTyped(this.value);" />
      <t:commandButton id="selectOption" forceId="true" value="OK"
        action="#{presenceBean.selectOption}" styleClass="selectOption" />
    </h:panelGroup>
  </h:panelGrid>

  <t:inputHidden immediate="true" value="#{presenceBean.sessionTrack}" />

  <f:verbatim>
    <script type="text/javascript">      
      function presenceKeyTyped(ch)
      {
        if (buttonPressed) return;
        document.getElementById("option").value = '';
        var elem = null;
        if (ch == '-') elem = document.getElementById("editButton");
        else if (ch == '*') elem = document.getElementById("cancelButton");
        else if (ch == '.') elem = document.getElementById("closeButton");
        if (elem != null) // special buttons
        {
          pressButton(elem);
        }
        else
        {
          elem = document.getElementById("entryTypes");
          var buttons = elem.childNodes;
          var i = 0;
          for (i = 0; i != buttons.length; i++)
          {
            var button = buttons[i];
            if (button.nodeName == "SPAN")
            {
              var elems = button.childNodes;
              var inputb = null;
              var j = 0;
              while (j != elems.length)
              {
                inputb = elems[j++];
                if (inputb.nodeName == 'INPUT') break;
              }
              if (inputb.nodeName == 'INPUT')
              {
                var key = inputb.accessKey;
                if (key == ch)
                {
                  pressButton(inputb);
                }
              }
            }
          }
        }
      }

      function pressButton(button)
      {
        buttonPressed = true;
        button.click();
      }

      function editEntry(message)
      {
        if (confirm(message)) return true;
        buttonPressed = false;
        return false;
      }

      function setFocus()
      {
        if (document.getElementById("timeInput") == null)
        {
          document.getElementById("option").focus();
          setTimeout('setFocus()', 500);
        }
        else
        {
          document.getElementById("timeInput").focus();
        }
      }
      var buttonPressed = false;
      setFocus();
      document.getElementById("option").value = '';
    </script>
  </f:verbatim>

</jsp:root>
