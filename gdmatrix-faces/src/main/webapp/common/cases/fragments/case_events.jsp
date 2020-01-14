<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.cases.web.resources.CaseBundle" 
                var="caseBundle" />

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
                var="objectBundle" />

  <t:div rendered="#{caseEventsBean.renderGroupButtonMode}"
         styleClass="buttonGroupSelection">
    <t:dataList value="#{caseEventsBean.groups}" var="group"
                rows="#{caseEventsBean.groupsPageSize}"
                rendered="#{caseEventsBean.renderGroupSelection}">
      <h:commandButton value="#{group.description}"
                       styleClass="#{caseEventsBean.renderGroup ? 'buttonEnabled' : 'buttonDisabled'}"
                       action="#{caseEventsBean.showGroup}" />
    </t:dataList>
  </t:div>  
  
  <t:dataList id="dataList" value="#{caseEventsBean.groups}" 
              var="group" rows="#{caseEventsBean.groupsPageSize}" 
              rowIndexVar="groupIndex" rowCountVar="groupCount">

    <t:div rendered="#{caseEventsBean.renderGroupBarMode}"
           styleClass="barGroupSelection">
      <h:commandLink action="#{caseEventsBean.showGroup}" style="color:black">
        <h:graphicImage value="/images/expand.gif" rendered="#{not caseEventsBean.renderGroup}"/>
        <h:graphicImage value="/images/collapse.gif" rendered="#{caseEventsBean.renderGroup}"/>
        <h:outputText value="#{group.description}" style="margin-left:2px"/>
      </h:commandLink>
    </t:div>      
  
    <t:buffer into="#{table}">
      
      <t:dataTable id="data" value="#{caseEventsBean.rows}" var="row"
        first="#{caseEventsBean.firstRowIndex}"  
        rowClasses="row1,row2" headerClass="header" footerClass="footer"
        rowStyleClass="#{caseEventsBean.editingEvent != null and
                         row.caseEventId == caseEventsBean.editingEvent.caseEventId ? 'selectedRow' : null}"
        styleClass="resultList caseEvents" style="width:100%"
        bodyStyle="#{empty caseEventsBean.rows ? 'display:none' : ''}"
        rows="#{caseEventsBean.pageSize}"
        rendered="#{caseEventsBean.renderGroup}">
      
        <f:facet name="header">
          <t:div styleClass="theader">
            <h:outputText value="#{group.description != '' ? (caseEventsBean.renderGroupSelection ? ' ' : group.description) : userSessionBean.selectedMenuItem.label}"
                          styleClass="textBox" style="width:85%"/>
            <t:dataScroller for="data"
                            rowsCountVar="rowCount">
              <h:outputFormat styleClass="textBox" value="#{objectBundle.shortResultRange}">
                <f:param value="#{rowCount}" />
              </h:outputFormat>
            </t:dataScroller>
          </t:div>
        </f:facet>
        
        <t:column styleClass="idColumn">
          <f:facet name="header">
            <h:outputText value="#{caseBundle.caseEvents_id}:" />
          </f:facet>
          <h:outputText value="#{row.event.eventId}" />
        </t:column>

        <t:column styleClass="eventColumn">
          <f:facet name="header">
            <h:outputText value="#{caseBundle.caseEvents_event}:" />
          </f:facet>
          <t:div styleClass="eventDiv">
            <h:outputText value="#{row.event.summary}" styleClass="eventSummary" />
            <t:dataList value="#{caseEventsBean.viewProperties}" var="property">
              <t:div styleClass="propertyRow" rendered="#{caseEventsBean.propertyVisible and property.value != null and property.value != ''}">
                <t:div styleClass="propName">
                  <h:outputText value="#{property.description}:" />
                </t:div>        
                <t:div styleClass="propValue">
                  <h:outputText escape="false" value="#{property.value}" />
                </t:div>              
              </t:div>
            </t:dataList>            
          </t:div>        
        </t:column>

        <t:column styleClass="dateColumn">
          <f:facet name="header">
            <h:outputText value="#{caseBundle.caseEvents_date}:" />
          </f:facet>
          <t:div styleClass="eventDiv">
            <h:outputText value="#{caseEventsBean.eventDate}" styleClass="eventDate" />
          </t:div>        
        </t:column>
        
        <t:column styleClass="actionsColumn">
          <h:panelGroup>
            <h:commandButton action="#{caseEventsBean.showEvent}"
                             rendered="#{row.caseEventId != null}"
                             styleClass="showButton" value="#{objectBundle.show}"
                             image="#{userSessionBean.icons.show}"
                             alt="#{objectBundle.show}" title="#{objectBundle.show}" />
            <h:commandButton action="#{caseEventsBean.editEvent}"
                             rendered="#{row.caseEventId != null}"
                             disabled="#{!caseEventsBean.rowEditable}"
                             styleClass="editButton" value="#{objectBundle.edit}"
                             image="#{userSessionBean.icons.detail}"
                             alt="#{objectBundle.edit}" title="#{objectBundle.edit}"/>            
            <h:commandButton value="#{objectBundle.delete}" 
                             image="#{userSessionBean.icons.delete}"
                             alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
                             action="#{caseEventsBean.removeEvent}"
                             rendered="#{row.caseEventId != null}"
                             disabled="#{!caseBean.editable or !caseEventsBean.rowRemovable}"
                             styleClass="removeButton"
                             onclick="return confirm('#{objectBundle.confirm_remove}');" />
          </h:panelGroup>
        </t:column>

        <f:facet name="footer">
          <t:dataScroller
            for="data"
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

    <h:outputText value="#{table}" escape="false"/>

    <t:dataScroller for="dataList"
                    fastStep="5"
                    paginator="true"
                    paginatorMaxPages="5"
                    immediate="true"
                    rendered="#{!caseEventsBean.singleGroup and ((groupIndex + 1) mod caseEventsBean.groupsPageSize == 0 or (groupIndex + 1 == groupCount))}"
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

  </t:dataList>

  <t:div style="width:100%;text-align:right">
    <h:commandButton value="#{objectBundle.add}"        
                     image="#{userSessionBean.icons.add}"
                     alt="#{objectBundle.add}" title="#{objectBundle.add}"
                     action="#{caseEventsBean.createEvent}"
                     rendered="#{row.caseEventId == null}"
                     disabled="#{!caseBean.editable or caseEventsBean.editingEvent != null}"
                     styleClass="addButton"  />
  </t:div>

  <t:div rendered="#{caseEventsBean.editingEvent != null}"
            styleClass="editingPanel">
    <t:div>
      <h:outputText value="#{caseBundle.caseEvents_event}: " 
                    style="width:15%" styleClass="textBox"/>
      <h:panelGroup>
        <t:selectOneMenu value="#{caseEventsBean.editingEvent.eventId}"
                         styleClass="selectBox" style="width:70%">
          <f:selectItems value="#{caseEventsBean.eventSelectItems}" />
        </t:selectOneMenu>
        <h:commandButton action="#{caseEventsBean.searchEvent}" 
                         value="#{objectBundle.search}"
                         image="#{userSessionBean.icons.search}"
                         alt="#{objectBundle.search}" title="#{objectBundle.search}"
                         styleClass="searchButton" />
      </h:panelGroup>
    </t:div>
    
    <t:div>
      <h:outputText value="#{caseBundle.caseEvents_type}:"
                    style="width:15%" styleClass="textBox"/>
      <sf:commandMenu value="#{caseEventsBean.currentTypeId}"
                      action="#{caseEventsBean.show}" styleClass="selectBox"
                      disabled="#{!caseBean.editable}">        
        <f:selectItem itemLabel=" " itemValue="" />
        <f:selectItems value="#{caseEventsBean.allTypeItems}" />
      </sf:commandMenu>
    </t:div>
    
    <t:div>
      <h:outputText value="#{caseBundle.caseEvents_comments}: "
                    style="width:15%" styleClass="textBox" />
      <h:inputTextarea value="#{caseEventsBean.editingEvent.comments}"
                       styleClass="inputBox" style="width:70%;vertical-align:top"
                       onkeypress="checkMaxLength(this, #{caseEventsBean.propertySize.comments})"
                       readonly="#{!caseBean.editable}"/>
    </t:div>
   
    <t:div rendered="#{not caseEventsBean.typeUndefined and
                       caseEventsBean.selector != null}">
      <t:div style="margin-top:20px">
        <h:outputText value="#{caseBundle.caseEvents_otherProperties}: "
          styleClass="blockHeader" />        
      </t:div>

      <t:div rendered="#{caseEventsBean.propertyEditorVisible || caseEventsBean.renderFormSelector}">
        <h:outputText value="#{caseBundle.caseEvents_form}:"
                      styleClass="textBox" style="width:16%" />
        <sf:commandMenu value="#{caseEventsBean.selector}"
                        styleClass="selectBox" style="width:77%">
          <f:selectItems value="#{caseEventsBean.formSelectItems}" />
        </sf:commandMenu>
        <h:commandButton value="#{objectBundle.update}"
                         image="#{userSessionBean.icons.update}"
                         alt="#{objectBundle.update}" title="#{objectBundle.update}"
                         styleClass="showButton"
                         rendered="#{not caseEventsBean.propertyEditorVisible}"
                         action="#{caseEventsBean.updateForm}" />
      </t:div>
      
      <t:div>
        <sf:dynamicForm 
          form="#{caseEventsBean.form}"
          rendererTypes="#{caseBean.editable ? 'HtmlFormRenderer,GenericFormRenderer' : 'DisabledHtmlFormRenderer'}"
          value="#{caseEventsBean.data}" 
          rendered="#{not caseEventsBean.propertyEditorVisible}" />
        <h:inputTextarea value="#{caseEventsBean.propertyEditorString}"
                         rendered="#{caseEventsBean.propertyEditorVisible}"
                         validator="#{caseEventsBean.validatePropertyEditorString}"
                         style="width:98%;height:100px; font-family:Courier New"
                         styleClass="inputBox"
                         readonly="#{!caseBean.editable}"/>
      </t:div>
    </t:div>
    
    <t:div styleClass="actionsRow">
      <h:commandButton action="#{caseEventsBean.storeEvent}" 
                       styleClass="addButton" value="#{objectBundle.store}" />                       
      <h:commandButton action="#{caseEventsBean.cancelEvent}" 
                       styleClass="cancelButton" value="#{objectBundle.cancel}" />
    </t:div>
  </t:div>

</jsp:root>
