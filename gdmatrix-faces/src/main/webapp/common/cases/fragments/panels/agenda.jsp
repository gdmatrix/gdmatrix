<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

  <t:div styleClass="agendaPanel">
     <sf:agenda caseId="#{panel.caseId}" nameStyleClass="eventName" rows="#{panel.pageSize}"
                themes="#{panel.themes}" commentsStyleClass="eventComments" styleClass="agenda"
        dateStyleClass="eventDate" target="_blank" dateFormat="#{panel.dateFormat}"
        url="go.faces?xmid=#{panel.agendaSearchMid}&amp;eventid=#{e.eventId}"
        var="e" />
    <h:outputLink value="go.faces?xmid=#{panel.agendaSearchMid}"
                    styleClass="showMoreLink" rendered="#{panel.showMoreText != null}">
      <h:outputText value="#{panel.showMoreText}" styleClass="showMoreText" />
    </h:outputLink>
  </t:div>

</jsp:root>