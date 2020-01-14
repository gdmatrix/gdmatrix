<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.agenda.web.resources.AgendaBundle" var="agendaBundle"/>

<h:panelGrid columns="1" styleClass="agendaEventPage"
              headerClass="eventType"
              rowClasses="eventTitle,description,timetable,places"
              footerClass="footer" width="100%"
              cellpadding="5">

    <h:messages rendered="#{userSessionBean.facesMessagesQueued and agendaBean.showMessages}" 
                errorClass="errorMessage" warnClass="warnMessage"
                infoClass="infoClass" fatalClass="fatalClass"
                showSummary="true" />

  <f:facet name="header">
    <sf:outputText value="#{agendaBean.currentEventTypeDescription}"
      styleClass="eventType"
      translator="#{userSessionBean.translator}"
      translationGroup="#{agendaBean.eventTranslationGroup}" />
  </f:facet>

  <sf:outputText value="#{agendaBean.currentEvent.summary}"
    translator="#{userSessionBean.translator}"
    translationGroup="#{agendaBean.eventTranslationGroup}" />

  <sf:outputText value="#{agendaBean.currentEvent.description}" escape="false"
    translator="#{userSessionBean.translator}"
    translationGroup="#{agendaBean.eventTranslationGroup}" />

  <h:panelGrid columns="2"  rowClasses="row1, row2"
    columnClasses="column1,column2" width="100%">
    <h:graphicImage url="/common/agenda/images/calendari.gif" alt="#{agendaBundle.outputFirstDay}" />
    <h:outputText value="#{agendaBean.formatedInitialDate}">
      <f:convertDateTime pattern="EEEE, dd MMMM yyyy"/>
    </h:outputText>
    <h:graphicImage url="/common/agenda/images/horari.gif" alt="#{agendaBundle.outputTimetableTitle}"/>
    <h:panelGroup>
     <h:outputText value="#{agendaBundle.outputTimetableFrom} "/>
     <h:outputText value="#{agendaBean.formatedInitialHour}">
      <f:convertDateTime pattern="HH:mm"/>
     </h:outputText>
     <h:outputText value=" #{agendaBundle.outputTimetableTo} "/>
     <h:outputText value="#{agendaBean.formatedFinalHour}">
      <f:convertDateTime pattern="HH:mm"/>
     </h:outputText>
    </h:panelGroup>
    <h:graphicImage url="/common/agenda/images/on.gif" alt="#{agendaBundle.outputPlaces}"/>
    <h:dataTable value="#{agendaBean.eventPlaces}" var="row" cellpadding="3"
      cellspacing="0"
      rowClasses="row1, row2" width="100%">
     <h:column>
      <h:panelGrid columns="1" width="100%" cellpadding="0"
      cellspacing="0">
       <h:outputText value="#{row.roomView.description}"/>
        <h:outputText value="#{row.roomView.addressView.description} - #{row.roomView.addressView.city}"
                      rendered="#{row.roomView.addressView != null}"/>
        <h:outputText value="#{row.addressView.description} - #{row.addressView.city}"
                      rendered="#{row.roomView.addressView == null}"/>
      </h:panelGrid>
     </h:column>
    </h:dataTable>
  </h:panelGrid>

<!--
  <f:facet name="footer">
   <h:commandLink value=" #{webBundle.commandLinkReturn}"
    action="#{agendaBean.nextEvents}" styleClass="buttonLink"/>

    <h:outputLink value="javascript:history.go(-1);" styleClass="buttonLink"
                  style="text-decoration:none;">
      <h:outputText value="#{webBundle.commandLinkReturn}" />
   </h:outputLink>
  </f:facet>
-->

 <t:saveState value="#{agendaBean.filter}" />
 <t:saveState value="#{agendaBean.selectedDay}" />
 <t:saveState value="#{agendaBean.currentFirstPage}" />
 <t:saveState value="#{agendaBean.eventContent}" />
 <t:saveState value="#{agendaBean.eventType}" />
 <t:saveState value="#{agendaBean.currentEvent}" />
 
 </h:panelGrid>



</jsp:root>