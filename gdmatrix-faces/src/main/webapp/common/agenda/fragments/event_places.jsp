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

  <t:buffer into="#{table}">
    <t:dataTable id="data" value="#{eventPlacesBean.rows}" var="row"
                 rowClasses="row1,row2" headerClass="header" footerClass="footer"
                 bodyStyle="#{empty eventPlacesBean.rows ? 'display:none' : ''}"
                 rowStyleClass="#{eventPlacesBean.editingEventPlace != null and
                                  row.eventPlaceId == eventPlacesBean.editingEventPlace.eventPlaceId ? 'selectedRow' : null}"
                 styleClass="resultList" style="width:100%"
                 rows="#{eventPlacesBean.pageSize}">
      <t:column style="width:5%">
        <f:facet name="header">
          <h:outputText value="#{agendaBundle.eventPlaces_id}:" />
        </f:facet>
        <h:outputText value="#{row.eventPlaceId}" />
      </t:column>

      <t:column style="width:60%">
        <f:facet name="header">
          <h:outputText value="#{agendaBundle.eventPlaces_place}:" />
        </f:facet>
        <t:div rendered="#{!row.address}">
          <h:outputText value="#{row.description}" />
        </t:div>
        <t:div rendered="#{row.address or row.room}">
          <h:outputText value="#{row.addressView.description} - #{row.addressView.city}" />
        </t:div>
      </t:column>

      <t:column style="width:20%" styleClass="actionsColumn">
        <h:panelGroup>
          <h:commandButton action="#{eventPlacesBean.showPlace}"
                           rendered="#{row.eventPlaceId != null and (row.room or row.address)}"
                           styleClass="showButton" value="#{objectBundle.show}"
                           image="#{userSessionBean.icons.show}"
                           alt="#{objectBundle.show}" title="#{objectBundle.show}" />
          <h:commandButton action="#{eventPlacesBean.editEventPlace}"
                           rendered="#{row.eventPlaceId != null}"
                           styleClass="editButton" value="#{objectBundle.edit}"
                           image="#{userSessionBean.icons.detail}"
                           alt="#{objectBundle.edit}" title="#{objectBundle.edit}" />
          <h:commandButton value="#{objectBundle.delete}"           
                           image="#{userSessionBean.icons.delete}"
                           alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
                           action="#{eventPlacesBean.removeEventPlace}"
                           rendered="#{row.eventPlaceId != null}"
                           disabled="#{!eventBean.editable}"
                           styleClass="removeButton"
                           onclick="return confirm('#{objectBundle.confirm_remove}');" />
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

  <t:div styleClass="resultBar" rendered="#{eventPlacesBean.rowCount > 0}">
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

  <h:outputText value="#{table}" escape="false"/>

  <t:div style="width:100%;text-align:right">
    <h:commandButton value="#{objectBundle.add}"        
     image="#{userSessionBean.icons.add}"
     alt="#{objectBundle.add}" title="#{objectBundle.add}"
     action="#{eventPlacesBean.createEventPlace}"
     rendered="#{row.eventPlaceId == null}"
     disabled="#{!eventBean.editable or eventPlacesBean.editingEventPlace != null}"
     styleClass="addButton"  />
  </t:div>

  <t:div rendered="#{eventPlacesBean.editingEventPlace != null}"
            styleClass="editingPanel">
    <t:div>
      <h:outputText value="#{agendaBundle.eventPlaces_address}: "
                    style="width:15%" styleClass="textBox"/>
      <h:panelGroup>
        <t:selectOneMenu value="#{eventPlacesBean.editingEventPlace.addressId}"
                         styleClass="selectBox" style="width:70%"
                         disabled="#{not eventBean.editable}">
          <f:selectItems value="#{eventPlacesBean.addressSelectItems}" />
        </t:selectOneMenu>
        <h:commandButton action="#{eventPlacesBean.searchAddress}"
                         value="#{objectBundle.search}"
                         image="#{userSessionBean.icons.search}"
                         alt="#{objectBundle.search}" title="#{objectBundle.search}"
                         styleClass="searchButton"
                         disabled="#{not eventBean.editable}"/>
      </h:panelGroup>
    </t:div>
    <t:div>
      <h:outputText value="#{agendaBundle.eventPlaces_room}: "
                    style="width:15%" styleClass="textBox"/>
      <h:panelGroup>
        <sf:commandMenu action="#{eventPlacesBean.show}" styleClass="selectBox" style="width:70%"
                        value="#{eventPlacesBean.editingRoomId}"
                        disabled="#{not eventBean.editable}">
          <f:selectItems value="#{eventPlacesBean.roomSelectItems}" />
        </sf:commandMenu>
        <h:commandButton action="#{eventPlacesBean.searchRoom}"
                         value="#{objectBundle.search}"
                         image="#{userSessionBean.icons.search}"
                         alt="#{objectBundle.search}" title="#{objectBundle.search}"
                         styleClass="searchButton"
                         disabled="#{not eventBean.editable}"/>
      </h:panelGroup>
    </t:div>
    <t:div>
      <h:outputText value="#{agendaBundle.eventPlaces_comments}: "
                    style="width:15%;vertical-align:top" styleClass="textBox"/>
      <h:inputTextarea value="#{eventPlacesBean.editingEventPlace.comments}"
                       styleClass="inputBox" style="width:70%"
                       onkeypress="checkMaxLength(this, #{eventPlacesBean.propertySize.comments})"
                       readonly="#{not eventBean.editable}"/>
    </t:div>
    <t:div styleClass="actionsRow">
      <h:commandButton action="#{eventPlacesBean.storeEventPlace}"
                       styleClass="addButton" value="#{objectBundle.store}"
                       disabled="#{!eventBean.editable}"
                       onclick="showOverlay()"/>
      <h:commandButton action="#{eventPlacesBean.cancelEventPlace}"
                       styleClass="cancelButton" value="#{objectBundle.cancel}" />
    </t:div>
  </t:div>

</jsp:root>
