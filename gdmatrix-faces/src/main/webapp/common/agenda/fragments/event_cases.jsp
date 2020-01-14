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

  <t:div rendered="#{eventCasesBean.renderGroupButtonMode}"
         styleClass="buttonGroupSelection">
    <t:dataList value="#{eventCasesBean.groups}" var="group"
                rows="#{eventCasesBean.groupsPageSize}"
                rendered="#{eventCasesBean.renderGroupSelection}">
      <h:commandButton value="#{group.description}"
                       styleClass="#{eventCasesBean.renderGroup ? 'buttonEnabled' : 'buttonDisabled'}"
                       action="#{eventCasesBean.showGroup}" />
    </t:dataList>
  </t:div>  
  
  <t:dataList id="dataList" value="#{eventCasesBean.groups}" 
              var="group" rows="#{eventCasesBean.groupsPageSize}" 
              rowIndexVar="groupIndex" rowCountVar="groupCount">

    <t:div rendered="#{eventCasesBean.renderGroupBarMode}"
           styleClass="barGroupSelection">
      <h:commandLink action="#{eventCasesBean.showGroup}" style="color:black">
        <h:graphicImage value="/images/expand.gif" rendered="#{not eventCasesBean.renderGroup}"/>
        <h:graphicImage value="/images/collapse.gif" rendered="#{eventCasesBean.renderGroup}"/>
        <h:outputText value="#{group.description}" style="margin-left:2px"/>
      </h:commandLink>
    </t:div>      
  
    <t:buffer into="#{table}">
      
      <t:dataTable id="data" value="#{eventCasesBean.rows}" var="row"
                 rowClasses="row1,row2" headerClass="header" footerClass="footer"
                 rowStyleClass="#{eventCasesBean.editingCase != null and
                                  row.caseEventId == eventCasesBean.editingCase.caseEventId ? 'selectedRow' : null}"
                 styleClass="resultList caseEvents" style="width:100%"
                 bodyStyle="#{empty eventCasesBean.rows ? 'display:none' : ''}"
                 rows="#{eventCasesBean.pageSize}"
                 rendered="#{eventCasesBean.renderGroup}">
      
        <f:facet name="header">
          <t:div styleClass="theader">
            <h:outputText value="#{group.description != '' ? (eventCasesBean.renderGroupSelection ? ' ' : group.description) : userSessionBean.selectedMenuItem.label}"
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
          <h:outputText value="#{agendaBundle.eventCases_id}:" />
          </f:facet>
          <h:outputText value="#{row.caseObject.caseId}" />
        </t:column>

        <t:column styleClass="caseColumn">
          <f:facet name="header">
            <h:outputText value="#{agendaBundle.eventCases_case}:" />
          </f:facet>
        <t:div styleClass="caseDiv">          
          <h:outputText value="#{row.caseObject.title}" styleClass="caseTitle" />
          </t:div>        
        </t:column>

        <t:column styleClass="actionsColumn">
          <h:panelGroup>
            <h:commandButton action="#{eventCasesBean.showCase}"
                             rendered="#{row.caseEventId != null}"
                             styleClass="showButton" value="#{objectBundle.show}"
                             image="#{userSessionBean.icons.show}"
                             alt="#{objectBundle.show}" title="#{objectBundle.show}" />
            <h:commandButton action="#{eventCasesBean.editCase}"
                             rendered="#{row.caseEventId != null}"
                             disabled="#{!eventCasesBean.rowEditable}"
                             styleClass="editButton" value="#{objectBundle.edit}"
                             image="#{userSessionBean.icons.detail}"
                             alt="#{objectBundle.edit}" title="#{objectBundle.edit}"/>                        
            <h:commandButton value="#{objectBundle.delete}" 
                             image="#{userSessionBean.icons.delete}"
                             alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
                             action="#{eventCasesBean.removeCase}"
                             rendered="#{row.caseEventId != null}"
                             disabled="#{!caseBean.editable or !eventCasesBean.rowRemovable}"
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
                    rendered="#{!eventCasesBean.singleGroup and ((groupIndex + 1) mod eventCasesBean.groupsPageSize == 0 or (groupIndex + 1 == groupCount))}"
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
                     action="#{eventCasesBean.createCase}"
                     rendered="#{row.caseEventId == null}"
                     disabled="#{!caseBean.editable or eventCasesBean.editingCase != null}"
                     styleClass="addButton"  />
  </t:div>

  <t:div rendered="#{eventCasesBean.editingCase != null}"
            styleClass="editingPanel">
    <t:div styleClass="eventCasesHelp" rendered="#{eventCasesBean.casesHelp != null}">
      <sf:outputText value="#{eventCasesBean.casesHelp}" 
                    translator="#{userSessionBean.translator}"
                    translationGroup="#{userSessionBean.translationGroup}" />
    </t:div>    
    <t:div>
      <h:outputText value="#{agendaBundle.eventCases_case}: " 
                    style="width:15%" styleClass="textBox"/>
      <h:panelGroup>
        <t:selectOneMenu value="#{eventCasesBean.editingCase.caseId}"
                         styleClass="selectBox" style="width:70%">
          <f:selectItems value="#{eventCasesBean.caseSelectItems}" />
        </t:selectOneMenu>
        <h:commandButton action="#{eventCasesBean.searchCase}" 
                         value="#{objectBundle.search}"
                         image="#{userSessionBean.icons.search}"
                         alt="#{objectBundle.search}" title="#{objectBundle.search}"
                         styleClass="searchButton" />
      </h:panelGroup>
    </t:div>
    
    <t:div>
      <h:outputText value="#{agendaBundle.eventCases_type}:"
                    style="width:15%" styleClass="textBox"/>
      <sf:commandMenu value="#{eventCasesBean.currentTypeId}"
                       styleClass="selectBox"
                       action="#{eventCasesBean.show}" >        
        <f:selectItem itemLabel=" " itemValue="" />
          <f:selectItems value="#{eventCasesBean.allTypeItems}" />
      </sf:commandMenu>
    </t:div>
    
    <t:div>
      <h:outputText value="#{agendaBundle.eventCases_comments}: "
                    style="width:15%" styleClass="textBox" />
      <h:inputTextarea value="#{eventCasesBean.editingCase.comments}"
                       styleClass="inputBox" style="width:70%;vertical-align:top"
                       onkeypress="checkMaxLength(this, #{eventCasesBean.propertySize.comments})"
                       readonly="#{!caseBean.editable}"/>
    </t:div>
    
    <t:div rendered="#{not eventCasesBean.typeUndefined and
                       eventCasesBean.selector != null}">
      <t:div style="margin-top:20px">
        <h:outputText value="#{agendaBundle.eventCases_otherProperties}: "
          styleClass="blockHeader" />        
      </t:div>

      <t:div>
        <h:outputText value="#{agendaBundle.eventCases_form}:"
                      styleClass="textBox" style="width:16%" />
        <sf:commandMenu value="#{eventCasesBean.selector}"
                        styleClass="selectBox" style="width:77%">
          <f:selectItems value="#{eventCasesBean.formSelectItems}" />
        </sf:commandMenu>
        <h:commandButton value="#{objectBundle.update}"
                         image="#{userSessionBean.icons.update}"
                         alt="#{objectBundle.update}" title="#{objectBundle.update}"
                         styleClass="showButton"
                         rendered="#{not eventCasesBean.propertyEditorVisible}"
                         action="#{eventCasesBean.updateForm}" />
      </t:div>
      
      <t:div>
        <sf:dynamicForm 
          form="#{eventCasesBean.form}"
          rendererTypes="#{caseBean.editable ? 'HtmlFormRenderer,GenericFormRenderer' : 'DisabledHtmlFormRenderer'}"
          value="#{eventCasesBean.data}" 
          rendered="#{not eventCasesBean.propertyEditorVisible}" />
        <h:inputTextarea value="#{eventCasesBean.propertyEditorString}"
                         rendered="#{eventCasesBean.propertyEditorVisible}"
                         validator="#{eventCasesBean.validatePropertyEditorString}"
                         style="width:98%;height:100px; font-family:Courier New"
                         styleClass="inputBox"
                         readonly="#{!caseBean.editable}"/>
      </t:div>
    </t:div>    
    
    <t:div styleClass="actionsRow">
      <h:commandButton action="#{eventCasesBean.storeCase}" 
                       styleClass="addButton" value="#{objectBundle.store}" />                       
      <h:commandButton action="#{eventCasesBean.cancelCase}" 
                       styleClass="cancelButton" value="#{objectBundle.cancel}" />
    </t:div>
  </t:div>

</jsp:root>
