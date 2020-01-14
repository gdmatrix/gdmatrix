<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.agenda.web.resources.AgendaBundle"
                var="agendaBundle" />

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
                var="objectBundle" />

  <t:div styleClass="eventRecurrences">
    <t:div styleClass="collapsiblePanel">
      <t:collapsiblePanel titleStyleClass="textBox"
                          var="currentCollapsed"
                          value="#{eventRecurrencesBean.currentCollapsed}" >
          <f:facet name="header">
            <t:div styleClass="title">
              <t:headerLink immediate="true">
                <h:graphicImage value="/images/expand.gif" rendered="#{currentCollapsed}"/>
                <h:graphicImage value="/images/collapse.gif" rendered="#{!currentCollapsed}"/>
              </t:headerLink>
              <h:outputText value=" #{agendaBundle.eventRecurrences_currentRecurrences} " />
            </t:div>
          </f:facet>

        <t:div>
          <t:buffer into="#{table}">
            <t:dataTable id="data" value="#{eventRecurrencesBean.rows}" var="row"
                         rowClasses="row1,row2" headerClass="header" footerClass="footer"
                         bodyStyle="#{empty eventRecurrencesBean.rows ? 'display:none' : ''}"
                         styleClass="resultList" style="width:100%"
                         rows="#{eventRecurrencesBean.pageSize}">
              <t:column style="width:5%" styleClass="#{row.eventId == eventBean.objectId ? 'selected' : ''}"
                        rendered="#{row.eventId != eventBean.objectId}">
                <f:facet name="header">
                  <h:outputText value="#{agendaBundle.event_id}:" />
                </f:facet>
                <h:outputText value="#{row.eventId}" />
              </t:column>

              <t:column style="width:60%" styleClass="#{row.eventId == eventBean.objectId ? 'selected' : ''}"
                        rendered="#{row.eventId != eventBean.objectId}">
                <f:facet name="header">
                  <h:outputText value="#{agendaBundle.event_summary}:" />
                </f:facet>
                <t:div>
                  <h:outputText value="#{eventRecurrencesBean.startDateTime}" style="font-size:12px"
                    styleClass="#{row.eventId == eventBean.objectId ? 'selected' : ''}">
                    <f:convertDateTime pattern="dd/MM/yyyy (HH:mm)"/>
                  </h:outputText>
                  <h:outputLabel value=" - " />
                  <h:outputText value="#{eventRecurrencesBean.endDateTime}" style="font-size:12px"
                    styleClass="#{row.eventId == eventBean.objectId ? 'selected' : ''}">
                    <f:convertDateTime pattern="dd/MM/yyyy (HH:mm)"/>
                  </h:outputText>
                </t:div>
                <t:div>
                  <h:outputText value="#{row.summary}"
                    />
                </t:div>
              </t:column>

              <t:column style="width:20%" styleClass="actionsColumn"
                        rendered="#{row.eventId != eventBean.objectId}">
                <h:panelGroup>
                  <h:commandButton action="#{eventSearchBean.showEvent}"
                     rendered="#{row.eventId != null and row.eventId != eventBean.objectId}"
                     styleClass="showButton" value="#{objectBundle.show}"
                     image="#{userSessionBean.icons.show}"
                     alt="#{objectBundle.show}" title="#{objectBundle.show}" />
                </h:panelGroup>
              </t:column>

              <f:facet name="footer">
                <t:dataScroller
                  fastStep="100"
                  paginator="true"
                  paginatorMaxPages="9"
                  immediate="true"
                  styleClass="scrollBar"
                  paginatorColumnClass="page"
                  paginatorActiveColumnClass="activePage"
                  nextStyleClass="nextButton"
                  previousStyleClass="previousButton"
                  firstStyleClass="firstButton"
                  lastStyleClass="lastButton"
                  fastfStyleClass="fastForwardButton"
                  fastrStyleClass="fastRewindButton"
                  renderFacetsIfSinglePage="false">
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
                  <f:facet name="fastrewind">
                    <t:div title="#{objectBundle.fastRewind}"></t:div>
                  </f:facet>
                  <f:facet name="fastforward">
                    <t:div title="#{objectBundle.fastForward}"></t:div>
                  </f:facet>
                </t:dataScroller>
              </f:facet>
            </t:dataTable>

          </t:buffer>
        <!--
          <t:div styleClass="resultBar" rendered="#{eventRecurrencesBean.rowCount > 0}">
            <t:dataScroller for="data"
                            firstRowIndexVar="firstRow"
                            lastRowIndexVar="lastRow"
                            rowsCountVar="rowCount">
              <h:outputFormat value="#{objectBundle.resultRange}">
                <f:param value="#{firstRow}" />
                <f:param value="#{lastRow}" />
                <f:param value="#{rowCount}" />
              </h:outputFormat>
            </t:dataScroller>
          </t:div>
        -->
          <h:outputText value="#{table}" escape="false"/>

        </t:div>
        <t:div styleClass="actionBar">
          <h:commandButton action="#{eventRecurrencesBean.deleteAllRecurrences}"
             alt="#{objectBundle.eventRecurrences_deleteAllRecurrences}"
             title="#{agendaBundle.eventRecurrences_deleteAllRecurrences}"
             value="#{agendaBundle.eventRecurrences_deleteAllRecurrences}"
             rendered="#{!empty eventRecurrencesBean.rows}"
             disabled="#{!eventBean.editable}"
             styleClass="removeButton"
             onclick="return confirm('#{agendaBundle.eventRecurrences_confirmDeleteAllRecurrences}');" />
        </t:div>
      </t:collapsiblePanel>

    </t:div>

    <t:div styleClass="collapsiblePanel" style="margin-top:5px"
           rendered="#{!eventBean.new and eventBean.editable}">
      <t:collapsiblePanel titleStyleClass="textBox" 
                          var="newCollapsed"
                          value="#{eventRecurrencesBean.newCollapsed}">
        <f:facet name="header">
          <t:div styleClass="title" >
            <t:headerLink immediate="true" >
              <h:graphicImage value="/images/expand.gif" rendered="#{newCollapsed}"/>
              <h:graphicImage value="/images/collapse.gif" rendered="#{!newCollapsed}"/>
            </t:headerLink>
            <h:outputText value=" #{agendaBundle.eventRecurrences_newRecurrences} "  />
          </t:div>

        </f:facet>
        <t:div>
          <jsp:include page="/common/agenda/fragments/event_copy.jsp"/>
        </t:div>

        <t:div styleClass="actionBar" >
          <h:commandButton action="#{eventRecurrencesBean.copyRecurrences}" 
           value="#{agendaBundle.eventRecurrences_copyRecurrences}"
           rendered="#{eventCopyBean.rowCount > 0}"
           styleClass="addButton"
           onclick="showOverlay()"/>
        </t:div>
      </t:collapsiblePanel>
    </t:div>



  </t:div>



</jsp:root>
