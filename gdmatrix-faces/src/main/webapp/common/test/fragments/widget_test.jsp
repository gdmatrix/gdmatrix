<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
<!--
  <h:panelGrid columns="1" binding="#{widgetTestBean.panel}" border="1" />

  <h:commandButton action="#{widgetTestBean.outcomeNull}" value="NULL" />
  <h:commandButton action="#{widgetTestBean.outcome}" value="NOT NULL" />
  <h:commandButton action="#{widgetTestBean.addInputText}" value="ADD" />
-->

<h:panelGrid columns="1" border="1" >
  <sf:agenda theme="#{widgetTestBean.theme}"
             var="e"
             url="/common/agenda/esdeveniment.faces?xmid=#{widgetTestBean.agendaMid}&amp;eventid=#{e.eventId}"
             rows="4"
             translator="#{userSessionBean.translator}"
             translationGroup="event"
             styleClass="eventsContainer"
             dateStyleClass="eventDate"
             nameStyleClass="eventName"
             commentsStyleClass="eventComments"/>

  <sf:news section="#{widgetTestBean.section}"
           var="n"
           url="/go.faces?xmid=#{widgetTestBean.section}&amp;newid=#{n.newId}"
           rows="4"
           translator="#{userSessionBean.translator}"
           translationGroup="new"
           styleClass="container"
           dateStyleClass="date"
           headLineStyleClass="headLine"
           imageStyleClass="image"
           summaryStyleClass="summary"/>
</h:panelGrid>


</jsp:root>
