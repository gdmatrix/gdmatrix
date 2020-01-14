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

  <t:div styleClass="workerPanel">
    <t:div styleClass="header">
      <h:outputText value="#{presenceBundle.workerInfo}"
        styleClass="workerInfo" />
      <h:outputText value="#{presenceBean.workerProfile.displayName}"
        styleClass="displayName" />
    </t:div>

    <t:panelGrid columns="2" styleClass="properties" columnClasses="col1,col2">

      <h:outputLabel value="#{presenceBundle.userId}:" />
      <h:outputText value="#{presenceBean.workerProfile.userId}" />

      <h:outputLabel value="#{presenceBundle.personId}:" />
      <h:outputText value="#{presenceBean.workerProfile.personId}" />

      <h:outputLabel value="#{presenceBundle.caseId}:" />
      <h:outputText value="#{presenceBean.workerProfile.caseId}" 
        rendered="#{presenceBean.workerProfile.caseId != null}" />
      <h:outputText value="#{presenceBundle.notFound}" styleClass="warning"
        rendered="#{presenceBean.workerProfile.caseId == null}" />

      <h:outputLabel value="#{presenceBundle.workingTimeInWeek}:" />
      <h:outputText value="#{presenceBean.workerWorkingTimeFormatted}" 
        rendered="#{presenceBean.workerProfile.caseId != null}" />
      <h:outputText value="#{presenceBundle.unknown}" styleClass="warning"
        rendered="#{presenceBean.workerProfile.caseId == null}" />

      <h:outputLabel value="#{presenceBundle.bonusTimeInDay}:" />
      <h:outputText value="#{presenceBean.workerBonusTimeFormatted}" />

    </t:panelGrid>

    <t:div styleClass="buttonsPanel">
      <t:commandButton action="#{presenceBean.back}"
        id="backEntry" forceId="true" onclick="showOverlay()"
        onfocus="javascript:focused(this);" onblur="javascript:unfocused(this);"
        value="#{presenceBundle.back}" styleClass="button" />
    </t:div>
  </t:div>

  <t:inputHidden immediate="true" value="#{presenceBean.sessionTrack}" />

  <f:verbatim>
    <script type="text/javascript">
      document.getElementById("backEntry").focus();

      function focused(elem)
      {
        elem.style.border = "1px solid black";
      }

      function unfocused(elem)
      {
        elem.style.border = "none";
      }
    </script>
  </f:verbatim>
</jsp:root>
