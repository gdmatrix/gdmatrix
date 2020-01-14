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

  <sf:saveScroll resetIfError="true" value="#{caseCasesBean.objectPageScroll}" />

  <t:div styleClass="resultBar" >
    <t:dataScroller for="data"
                    firstRowIndexVar="firstRow"
                    lastRowIndexVar="lastRow"
                    rowsCountVar="rowCount">
      <h:outputFormat value="#{objectBundle.resultRange}" rendered="#{rowCount > caseCasesBean.pageSize}">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
  </t:div>

  <t:div rendered="#{caseCasesBean.renderGroupButtonMode}"
         styleClass="buttonGroupSelection">
    <t:dataList value="#{caseCasesBean.groups}" var="group"
                rows="#{caseCasesBean.groupsPageSize}"
                rendered="#{caseCasesBean.renderGroupSelection}">
      <h:commandButton value="#{group.description}"
         styleClass="#{caseCasesBean.renderGroup ? 'buttonEnabled' : 'buttonDisabled'}"
         action="#{caseCasesBean.showGroup}" />
    </t:dataList>
  </t:div>

  <t:dataList id="data" value="#{caseCasesBean.groups}" var="group" rows="#{caseCasesBean.groupsPageSize}">

    <t:buffer into="#{table}">

      <t:div rendered="#{caseCasesBean.renderGroupBarMode}"
          styleClass="barGroupSelection">
        <h:commandLink action="#{caseCasesBean.showGroup}" style="color:black">
           <h:graphicImage value="/images/expand.gif" rendered="#{not caseCasesBean.renderGroup}"/>
           <h:graphicImage value="/images/collapse.gif" rendered="#{caseCasesBean.renderGroup}"/>
           <h:outputText value="#{group.description}" style="margin-left:2px"/>
        </h:commandLink>
      </t:div>
      
      <t:dataTable id="dataCases" value="#{caseCasesBean.rows}" var="row"
          first="#{caseCasesBean.firstRowIndex}"
          rowClasses="row1,row2" footerClass="footer"
          bodyStyle="#{caseCasesBean.rowsEmpty ? 'display:none' : ''}"
          rowIndexVar="index"
          rowStyleClass="#{caseCasesBean.editingCase != null and row.caseCaseId == caseCasesBean.editingCase.caseCaseId ? 'selectedRow' : null} #{caseCasesBean.rowStyleClass} row#{(index % 2) + 1}"
          styleClass="resultList" style="width:100%"
          rows="#{caseCasesBean.pageSize}"
          rendered="#{caseCasesBean.renderGroup}">
        <f:facet name="header">
          <t:div styleClass="theader">
            <h:outputText value="#{group.description != '' ? (caseCasesBean.renderGroupSelection ? ' ' : group.description) : userSessionBean.selectedMenuItem.label}"
                          styleClass="textBox" style="width:85%"/>
            <t:dataScroller for="dataCases"
                            rowsCountVar="rowCount" >
              <h:outputFormat styleClass="textBox" value="#{objectBundle.shortResultRange}">
                <f:param value="#{rowCount}" />
              </h:outputFormat>
            </t:dataScroller>
          </t:div>
        </f:facet>

        <t:column style="width:3%" headerstyleClass="header">
          <h:graphicImage value="#{caseCasesBean.reverseRelation ? '/common/cases/images/left.png' : '/common/cases/images/right.png'}"/>
        </t:column>

        <t:column style="width:12%" headerstyleClass="header">
          <f:facet name="header">
            <h:outputText value="#{caseBundle.caseCases_id}:" />
          </f:facet>
          <h:outputText 
            value="#{caseCasesBean.reverseRelation ? row.mainCase.caseId : row.relCase.caseId}"
            styleClass="#{caseCasesBean.rowStyleClass}" />
        </t:column>

        <t:column style="width:65%" headerstyleClass="header">
          <f:facet name="header">
            <h:outputText value="#{caseBundle.caseCases_case}:" />
          </f:facet>

          <t:div styleClass="mainProperties">
            <h:panelGrid columns="1" styleClass="#{caseCasesBean.rowStyleClass}">              
              <h:outputFormat style="font-size:11px; font-weight:normal;"
                              value="#{caseBundle.caseCases_closedDateInterval}"
                              rendered="#{caseCasesBean.viewStartDate != null
                                          and caseCasesBean.viewEndDate != null}">
                <f:param value="#{caseCasesBean.viewStartDate}" />
                <f:param value="#{caseCasesBean.viewEndDate}" />
              </h:outputFormat>
              <h:outputFormat style="font-size:11px; font-weight:normal;"
                              value="#{caseBundle.caseCases_openToDateInterval}"
                              rendered="#{caseCasesBean.viewStartDate == null
                                          and caseCasesBean.viewEndDate != null}">
                <f:param value="#{caseCasesBean.viewEndDate}" />
              </h:outputFormat>
              <h:outputFormat style="font-size:11px; font-weight:normal;"
                              value="#{caseBundle.caseCases_openFromDateInterval}"
                              rendered="#{caseCasesBean.viewStartDate != null
                                          and caseCasesBean.viewEndDate == null}">
                <f:param value="#{caseCasesBean.viewStartDate}" />
              </h:outputFormat>
              <h:outputText value="#{caseCasesBean.reverseRelation ?
                row.mainCase.title : row.relCase.title}" />
            </h:panelGrid>
          </t:div>

          <t:dataList value="#{caseCasesBean.viewProperties}" var="property">
            <t:div styleClass="propertyRow" rendered="#{caseCasesBean.propertyVisible and property.value != null and property.value != ''}">
              <t:div styleClass="propName">
                <h:outputText value="#{property.description}:" />
              </t:div>
              <t:div styleClass="propValue">
                <h:outputText escape="false" value="#{property.value}" />
              </t:div>              
            </t:div>
          </t:dataList>

        </t:column>

        <t:column style="width:20%" styleClass="actionsColumn" headerstyleClass="header">
          <h:panelGroup>
            <h:commandButton action="#{caseCasesBean.showCase}"
                             rendered="#{row.caseCaseId != null}"
                             styleClass="showButton" value="#{objectBundle.show}"
                             image="#{userSessionBean.icons.show}"
                             alt="#{objectBundle.show}" title="#{objectBundle.show}" />
            <h:commandButton action="#{caseCasesBean.editCase}"
                             rendered="#{row.caseCaseId != null and 
                                         caseCasesBean.renderEditButton}"
                             disabled="#{!caseCasesBean.rowEditable}"
                             styleClass="editButton" value="#{objectBundle.edit}"
                             image="#{userSessionBean.icons.detail}"
                             alt="#{objectBundle.edit}" title="#{objectBundle.edit}"/>
            <h:commandButton value="#{objectBundle.delete}"
                             image="#{userSessionBean.icons.delete}"
                             alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
                             action="#{caseCasesBean.removeCase}"
                             rendered="#{row.caseCaseId != null}"
                             disabled="#{!caseBean.editable or caseCasesBean.editingCase != null or !caseCasesBean.rowRemovable}"
                             styleClass="removeButton"
                             onclick="return confirm('#{objectBundle.confirm_remove}');"/>
          </h:panelGroup>
        </t:column>
        <f:facet name="footer">          
          <t:dataScroller for="dataCases"
                    fastStep="5"
                    paginator="true"
                    paginatorMaxPages="5"
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
                    renderFacetsIfSinglePage="false"
                    firstRowIndexVar="firstRow">
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

    <t:outputText value="#{table}" escape="false" />

  </t:dataList>

  <t:dataScroller for="data"
                  fastStep="5"
                  paginator="true"
                  paginatorMaxPages="5"
                  immediate="true"
                  rendered="#{!caseCasesBean.singleGroup and ((groupIndex + 1) mod caseCasesBean.groupsPageSize == 0 or (groupIndex + 1 == groupCount))}"
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

  <t:div style="width:100%;text-align:right">
    <h:commandButton action="#{caseCasesBean.createCase}"
                     image="#{userSessionBean.icons.add}"
                     alt="#{objectBundle.add}" title="#{objectBundle.add}"
                     disabled="#{not caseBean.editable or caseCasesBean.editingCase != null}"
                     styleClass="addButton" value="#{objectBundle.add}" />
  </t:div>

  <t:div rendered="#{caseCasesBean.editingCase != null}"
         styleClass="editingPanel">
    <t:div styleClass="caseCasesHelp" rendered="#{caseCasesBean.caseCasesHelp != null}">
      <sf:outputText escape="false" value="#{caseCasesBean.caseCasesHelp}" 
                    translator="#{userSessionBean.translator}"
                    translationGroup="#{userSessionBean.translationGroup}" />
    </t:div>
    
    <t:div>
      <h:outputText value="#{caseBundle.caseCases_case}: "
                    style="width:15%" styleClass="textBox"/>
      <h:panelGroup>        
        <sf:commandMenu action="#{caseCasesBean.changeCase}"                          
                        value="#{caseCasesBean.selectedCaseId}"
                        styleClass="selectBox" style="width:70%"
                        disabled="#{!caseBean.editable}">
          <f:selectItems value="#{caseCasesBean.caseSelectItems}" />
        </sf:commandMenu>
        <h:commandButton action="#{caseCasesBean.searchCase}"
                         styleClass="searchButton" value="#{objectBundle.search}"
                         image="#{userSessionBean.icons.search}"
                         alt="#{objectBundle.search}" title="#{objectBundle.search}"
                         disabled="#{!caseBean.editable}"/>
      </h:panelGroup>
    </t:div>

    <t:div rendered="#{caseCasesBean.renderTypeSelector}">
      <h:outputText value="#{caseBundle.caseCases_type}:"
                    style="width:15%" styleClass="textBox"/>
      <sf:commandMenu action="#{caseCasesBean.changeType}" 
                      value="#{caseCasesBean.selectedTypeId}"                                           
                      styleClass="selectBox" disabled="#{!caseBean.editable}">
        <f:selectItems value="#{caseCasesBean.allTypeItems}" />
      </sf:commandMenu>
    </t:div>

    <t:div>
      <h:outputText value="#{caseBundle.caseCases_startDate}: "
        style="width:15%" styleClass="textBox"/>
      <sf:calendar value="#{caseCasesBean.editingCase.startDate}"
                   styleClass="calendarBox"
                   externalFormat="dd/MM/yyyy"
                   internalFormat="yyyyMMdd"
                   buttonStyleClass="calendarButton"
                   style="width:14%"
                   disabled="#{!caseBean.editable}"/>
    </t:div>

    <t:div>
      <h:outputText value="#{caseBundle.caseCases_endDate}: "
         style="width:15%" styleClass="textBox"/>
      <sf:calendar value="#{caseCasesBean.editingCase.endDate}"
                   styleClass="calendarBox"
                   externalFormat="dd/MM/yyyy"
                   internalFormat="yyyyMMdd"
                   buttonStyleClass="calendarButton"
                   style="width:14%"
                   disabled="#{!caseBean.editable}"/>
    </t:div>

    <t:div>
      <h:outputText value="#{caseBundle.caseCases_comments}: "
                    style="width:15%" styleClass="textBox" />
      <h:inputTextarea value="#{caseCasesBean.editingCase.comments}"
                       styleClass="inputBox" style="width:70%;vertical-align:top"
                       onkeypress="checkMaxLength(this, #{caseCasesBean.propertySize.comments})"
                       readonly="#{!caseBean.editable}"/>
    </t:div>
   
    <t:div rendered="#{caseCasesBean.renderTypeSelector and 
                       not caseCasesBean.typeUndefined and
                       caseCasesBean.selector != null}">
      <t:div style="margin-top:20px">
        <h:outputText value="#{caseBundle.caseCases_otherProperties}: "
          styleClass="blockHeader" />        
      </t:div>

      <t:div rendered="#{caseCasesBean.propertyEditorVisible || caseCasesBean.renderFormSelector}">
        <h:outputText value="#{caseBundle.caseCases_form}:"
                      styleClass="textBox" style="width:16%" />
        <sf:commandMenu value="#{caseCasesBean.selector}"
                        styleClass="selectBox" style="width:77%">
          <f:selectItems value="#{caseCasesBean.formSelectItems}" />
        </sf:commandMenu>
        <h:commandButton value="#{objectBundle.update}"
                         image="#{userSessionBean.icons.update}"
                         alt="#{objectBundle.update}" title="#{objectBundle.update}"
                         styleClass="showButton"
                         rendered="#{not caseCasesBean.propertyEditorVisible}"
                         action="#{caseCasesBean.updateForm}" />
      </t:div>
      
      <t:div>
        <sf:dynamicForm 
          form="#{caseCasesBean.form}"
          rendererTypes="#{caseBean.editable ? 'HtmlFormRenderer,GenericFormRenderer' : 'DisabledHtmlFormRenderer'}"
          value="#{caseCasesBean.data}" 
          rendered="#{not caseCasesBean.propertyEditorVisible}" />
        <h:inputTextarea value="#{caseCasesBean.propertyEditorString}"
                         rendered="#{caseCasesBean.propertyEditorVisible}"
                         validator="#{caseCasesBean.validatePropertyEditorString}"
                         style="width:98%;height:100px; font-family:Courier New"
                         styleClass="inputBox"
                         readonly="#{!caseBean.editable}"/>
      </t:div>            
    </t:div>
    
    <t:div styleClass="actionsRow">
      <h:commandButton action="#{caseCasesBean.storeCase}"
                       styleClass="addButton" value="#{objectBundle.store}"
                       disabled="#{!caseBean.editable or !caseCasesBean.enableStoreCaseButton}"
                       onclick="showOverlay()"/>
      <h:commandButton action="#{caseCasesBean.cancelCase}"
                       styleClass="cancelButton" value="#{objectBundle.cancel}" 
                       immediate="true"/>
    </t:div>
    
  </t:div>

</jsp:root>
