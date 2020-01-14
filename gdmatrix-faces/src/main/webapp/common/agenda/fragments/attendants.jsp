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
    <t:dataTable id="data" value="#{attendantsBean.rows}" var="row"
                 rowClasses="row1,row2" headerClass="header" footerClass="footer"
                 bodyStyle="#{empty attendantsBean.rows ? 'display:none' : ''}"
                 rowStyleClass="#{attendantsBean.attendant != null and row.attendantId == attendantsBean.attendant.attendantId ? 'selectedRow' :
                  attendantsBean.attendantAvailable ? null : 'unavailableAttendant'}"
                 styleClass="resultList" style="width:100%"
                 rows="#{attendantsBean.pageSize}">
      <t:column style="width:10%">
        <f:facet name="header">
          <h:outputText value="#{agendaBundle.attendants_id}:" />
        </f:facet>
        <h:outputText value="#{row.personView.personId}" />
      </t:column>

      <t:column style="width:45%">
        <f:facet name="header">
          <h:outputText value="#{agendaBundle.attendants_person}:" />
        </f:facet>
        <h:outputText value="#{row.personView.fullName}"
                      />
      </t:column>

      <t:column style="width:15%">
        <f:facet name="header">
          <h:outputText value="#{agendaBundle.attendants_type}:" />
        </f:facet>
        <h:outputText value="#{attendantsBean.rowTypeDescription}" />
      </t:column>

      <t:column style="width:10%">
        <f:facet name="header">
          <h:outputText value="#{agendaBundle.attendants_attendedShort}:" />
        </f:facet>
        <h:outputText value="#{attendantsBean.attendedLabel}" />
      </t:column>
      
      <t:column style="width:20%" styleClass="actionsColumn">
        <h:panelGroup>
          <h:commandButton action="#{attendantsBean.showPerson}"
                           rendered="#{row.attendantId != null}"
                           styleClass="showButton" value="#{objectBundle.show}"
                           image="#{userSessionBean.icons.show}"
                           alt="#{objectBundle.show}" title="#{objectBundle.show}"/>
          <h:commandButton action="#{attendantsBean.editAttendant}"
                           rendered="#{row.attendantId != null}"
                           styleClass="editButton" value="#{objectBundle.edit}"
                           image="#{userSessionBean.icons.detail}"
                           alt="#{objectBundle.edit}" title="#{objectBundle.edit}" />
          <h:commandButton value="#{objectBundle.delete}"
                           image="#{userSessionBean.icons.delete}"
                           alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
                           action="#{attendantsBean.removePerson}"
                           rendered="#{row.attendantId != null}"
                           disabled="#{!eventBean.editable or attendantsBean.attendant != null}"
                           styleClass="removeButton"
                           onclick="return confirm('#{objectBundle.confirm_remove}');"/>
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

  <t:div styleClass="resultBar" rendered="#{attendantsBean.rowCount > 0}">
    <t:dataScroller for="data"
                    rowsCountVar="rowCount">
      <h:outputFormat value="#{objectBundle.shortResultRange}">
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
  </t:div>

  <h:outputText value="#{table}" escape="false"/>

  <t:div style="width:100%;text-align:right">
    <h:commandButton value="#{objectBundle.add}"
                     image="#{userSessionBean.icons.add}"
                     alt="#{objectBundle.add}" title="#{objectBundle.add}"
                     action="#{attendantsBean.createAttendant}"
                     rendered="#{row.attendantId == null}" disabled="#{!eventBean.editable}"
                     styleClass="addButton"  />
  </t:div>

  <t:div rendered="#{attendantsBean.attendant != null}"
            styleClass="editingPanel">
    <t:div>
      <h:outputText value="#{agendaBundle.attendants_type}:"
                    style="width:15%" styleClass="textBox"/>
       <sf:commandMenu value="#{attendantsBean.attendant.attendantTypeId}"
                       styleClass="selectBox"
                       disabled="#{not eventBean.editable}">
        <f:selectItems value="#{attendantsBean.allTypeItems}" />
      </sf:commandMenu>
    </t:div>

    <t:div>
      <h:outputText value="#{agendaBundle.attendants_person}: "
                    style="width:15%" styleClass="textBox"/>
      <h:panelGroup>
        <t:selectOneMenu value="#{attendantsBean.attendant.personId}"
                         styleClass="selectBox" style="width:70%"                         
                         disabled="#{not eventBean.editable}">
          <f:selectItems value="#{attendantsBean.personSelectItems}" />
          <f:selectItems value="#{attendantsBean.groupSelectItems}" />
        </t:selectOneMenu>
        <h:commandButton action="#{attendantsBean.searchPerson}"
                         styleClass="searchButton" value="#{objectBundle.search}"
                         image="#{userSessionBean.icons.search}"
                         alt="#{objectBundle.search}" title="#{objectBundle.search}" />
      </h:panelGroup>
    </t:div>

    <t:div>
      <h:outputText value="#{agendaBundle.attendants_hidden}:" styleClass="textBox"
        style="width:15%; vertical-align:top"/>
      <t:selectBooleanCheckbox value="#{attendantsBean.hidden}"
        style="vertical-align:middle;"
        disabled="#{not eventBean.editable}"/>
    </t:div>

    <t:div>
      <h:outputText value="#{agendaBundle.attendants_attended}:" styleClass="textBox"
        style="width:15%; vertical-align:top"/>
      <t:selectOneMenu value="#{attendantsBean.attendant.attended}"
                       styleClass="selectBox" 
                       disabled="#{not eventBean.editable}">
        <f:selectItem itemLabel=" " itemValue="" />
        <f:selectItem itemLabel="NO" itemValue="N" />
        <f:selectItem itemLabel="SI" itemValue="S" />
        <f:selectItem itemLabel="FJ" itemValue="J" />
      </t:selectOneMenu>
    </t:div>

    <t:div>
      <h:outputText value="#{agendaBundle.attendants_comments}: "
                    style="width:15%;vertical-align:top" styleClass="textBox" />
      <h:inputTextarea value="#{attendantsBean.attendant.comments}"
                       styleClass="inputBox" style="width:70%"
                       onkeypress="checkMaxLength(this, #{attendantsBean.propertySize.comments})"
                       readonly="#{not eventBean.editable}"/>
    </t:div>
    <t:div styleClass="actionsRow">
      <h:commandButton action="#{attendantsBean.storeAttendant}"
                       styleClass="addButton" value="#{objectBundle.store}"
                       disabled="#{!eventBean.editable}"
                       onclick="showOverlay()"/>
      <h:commandButton action="#{attendantsBean.cancelAttendant}"
                       styleClass="cancelButton" value="#{objectBundle.cancel}" />
    </t:div>

  </t:div>

</jsp:root>
