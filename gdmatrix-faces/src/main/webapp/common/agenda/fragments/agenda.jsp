<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.agenda.web.resources.AgendaBundle" var="agendaBundle"/>

  <h:panelGrid columns="1" style="width:100%" styleClass="agendaListPage">
    <h:messages rendered="#{userSessionBean.facesMessagesQueued and agendaBean.showMessages}" 
                errorClass="errorMessage" warnClass="warnMessage"
                infoClass="infoClass" fatalClass="fatalClass"
                showSummary="true" />

    <h:panelGrid id="header" columns="1" style="width:100%"
      rendered="#{agendaBean.headerBrowser!=null}"
      styleClass="headerDocument">
      <sf:browser binding="#{agendaBean.headerBrowser}"
        port="#{applicationBean.defaultPort}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </h:panelGrid>

    <h:panelGrid id="filterPanel" columns="2" styleClass="filterPanel"
                 rendered="#{agendaBean.renderFilterPanel}"
                 columnClasses="fp_column1,fp_column2"
                 headerClass="fp_header">
      <f:facet name="header">
        <h:outputText value="#{agendaBundle.headerSearchEvent}"/>
      </f:facet>
      <h:panelGrid columns="2" styleClass="filterSubPanel"
                   columnClasses="fsp_column1,fsp_column2" >
        <h:outputText value="#{agendaBundle.outputFirstDay}"
                      rendered="#{agendaBean.renderStartDateFilter}"/>
        <sf:calendar value="#{agendaBean.selectedDay}"
          styleClass="calendarBox"
          style="width:35%;vertical-align:top;margin-right:2px;"
          rendered="#{agendaBean.renderStartDateFilter}"/>

        <h:outputText value="#{agendaBundle.outputEventType}" rendered="#{agendaBean.renderEventTypeFilter}"/>
        <h:selectOneListbox id="eventypes" size="1"
                            value="#{agendaBean.eventType}"  rendered="#{agendaBean.renderEventTypeFilter}">
          <f:selectItem itemLabel=" " itemValue=""/>
          <f:selectItems value="#{agendaBean.eventTypes}"/>
        </h:selectOneListbox>

        <h:outputText value="#{agendaBundle.outputContain}" rendered="#{agendaBean.renderContentFilter}"/>
        <h:inputText value="#{agendaBean.eventContent}"
                     styleClass="inputText" rendered="#{agendaBean.renderContentFilter}" />
      </h:panelGrid>
      <sf:commandButton id="default_button" action="#{agendaBean.searchEvents}" value="#{webBundle.buttonFind}"
                        styleClass="searchButton" renderBox="true">
      </sf:commandButton>

<!--
      <h:commandButton action="#{agendaBean.searchEvents}" value="#{webBundle.buttonFind}"
                       styleClass="button">
      </h:commandButton>
-->
    </h:panelGrid>


    <h:dataTable id="agendaTable" var="row" binding="#{agendaBean.eventsTable}"
                 rowClasses="row1,row2" styleClass="resultList"
                 footerClass="footer"
                 columnClasses="column1"
                 rows="#{agendaBean.rowsPerPage}">
      <h:column id="agendaTable_column1">
          <h:panelGrid id="agendaTableContainer" columns="1" style="width:100%"
            styleClass="body"
            rowClasses="eventHeader,eventName,eventDescription">

            <h:panelGrid columns="5"
              columnClasses="column1,column2,column3,column4,column5">
            <h:graphicImage url="/common/agenda/images/calendari.gif" alt="#{agendaBundle.outputFirstDay}" />

            <h:outputText value="#{agendaBean.formatedInitialDate}" id="InitDateColumn">
              <f:convertDateTime pattern="E dd/MM/yyyy"/>
            </h:outputText>

            <h:graphicImage url="/common/agenda/images/horari.gif" alt="#{agendaBundle.outputTimetableTitle}"/>
            
            <h:panelGroup>
              <h:outputText value="#{agendaBean.formatedInitialHour}" id="InitHourColumn">
                <f:convertDateTime pattern="HH:mm"/>
              </h:outputText>
              <h:outputText value="-"
                            rendered="#{agendaBean.formatedFinalHour != null}">
              </h:outputText>
              <h:outputText value="#{agendaBean.formatedFinalHour}" id="FinalHourColumn"
                            rendered="#{agendaBean.formatedFinalHour != null}">
                <f:convertDateTime pattern="HH:mm"/>
              </h:outputText>
            </h:panelGroup>
<!--
            <h:outputLink id="infoLinkText"
              value="/go.faces?xmid=#{userSessionBean.selectedMid}&amp;eventid=#{row.eventId}">
              <sf:outputText value="#{agendaBean.moreInfoLabel}" styleClass="selectLink"
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}" />
            </h:outputLink>
-->
            </h:panelGrid>
            <h:outputLink value="/go.faces?xmid=#{userSessionBean.selectedMid}&amp;eventid=#{row.eventId}">
              <sf:outputText id="eventName" value="#{row.summary}"
                translator="#{userSessionBean.translator}"
                translationGroup="#{agendaBean.translationGroup}"/>
              <sf:outputText id="eventType" value="#{agendaBean.rowType}"
                translator="#{userSessionBean.translator}"
                translationGroup="#{agendaBean.translationGroup}"
                rendered="#{row.summary == null}"/>
            </h:outputLink>

            <sf:outputText id="eventDescription" value="#{row.description}" escape="false"
              translator="#{userSessionBean.translator}"
              translationGroup="#{agendaBean.translationGroup}" />
         </h:panelGrid>
      </h:column>

      <f:facet name="footer">
        <h:panelGrid columns="2" style="width:100%; text-align:center">
          <t:dataScroller id="dataScroller"
                          for="agendaTable"
                          paginator="true"
                          paginatorMaxPages="5" style="text-align:center"
                          styleClass="scrollBar"
                          paginatorActiveColumnClass="activePage"
                          paginatorTableClass="dataScrollerPaginatorTable"
                          paginatorColumnClass="dataScrollerPaginatorColumn"
                          nextStyleClass="nextButton"
                          previousStyleClass="previousButton"
                          firstStyleClass="firstButton"
                          lastStyleClass="lastButton"
                          fastfStyleClass="fastForwardButton"
                          fastrStyleClass="fastRewindButton">
            <f:facet name="first">
              <t:div title="#{objectBundle.first}"></t:div>
            </f:facet>
            <f:facet name="last">
              <t:div title="#{objectBundle.last}"></t:div>
            </f:facet>
            <f:facet name="previous">
              <t:div title="#{objectBundle.previous}"></t:div>
            </f:facet>
            <f:facet name="next">
              <t:div title="#{objectBundle.next}"></t:div>
            </f:facet>
          </t:dataScroller>

          <t:dataScroller id="dataScrollerText" for="agendaTable"
                          pageCountVar="pageCount" pageIndexVar="pageIndex"
                          rowsCountVar="rowCount">
            <h:panelGroup>
              <h:outputText value="#{pageIndex}/#{pageCount} [#{rowCount} " />
              <h:outputText value="#{webBundle.scrollerOutputFound}" />
              <h:outputText value="]" />
            </h:panelGroup>
          </t:dataScroller>
        </h:panelGrid>
      </f:facet>
    </h:dataTable>

    <h:panelGrid id="footer" columns="1" style="width:100%"
      rendered="#{agendaBean.footerBrowser!=null}"
      styleClass="footerDocument">
      <sf:browser binding="#{agendaBean.footerBrowser}"
        port="#{applicationBean.defaultPort}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </h:panelGrid>

    <t:saveState value="#{agendaBean.filter}" />
    <t:saveState value="#{agendaBean.selectedDay}" />
    <t:saveState value="#{agendaBean.eventContent}" />
    <t:saveState value="#{agendaBean.eventType}" />

  </h:panelGrid>
</jsp:root>