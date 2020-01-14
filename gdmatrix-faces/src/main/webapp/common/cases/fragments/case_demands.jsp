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

  <t:buffer into="#{table}">
    <t:dataTable id="data" value="#{caseDemandsBean.rows}" var="row"
                 rowClasses="row1,row2" headerClass="header" footerClass="footer"
                 rowStyleClass="#{caseDemandsBean.editingDemand != null
                                  and row.demandId == caseDemandsBean.editingDemand.demandId ? 'selectedRow' : null}"
                 styleClass="resultList" style="width:100%"
                 bodyStyle="#{empty caseDemandsBean.rows ? 'display:none' : ''}"
                 rows="#{caseDemandsBean.pageSize}">

      <t:column style="width:15%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.caseDemands_id}:" />
        </f:facet>
        <h:outputText value="#{row.demandId}" />
      </t:column>

      <t:column style="width:50%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.caseDemands_type}:" />
        </f:facet>
        <h:outputText value="#{row.demandTypeId}" rendered="#{row.demandTypeId != null}"/>
      </t:column>

      <t:column style="width:25%" styleClass="actionsColumn">
        <h:panelGroup>
          <h:commandButton action="#{caseDemandsBean.showRowType}"
                           rendered="#{row.demandTypeId != null}"
                           value="#{objectBundle.show}"
                           image="#{userSessionBean.icons.show}"
                           alt="#{objectBundle.show}" title="#{objectBundle.show}"
                           styleClass="showButton" />
          <h:commandButton action="#{caseDemandsBean.editDemand}"
                           rendered="#{row.demandId != null}"
                           styleClass="editButton" value="#{objectBundle.edit}"
                           image="#{userSessionBean.icons.detail}"
                           alt="#{objectBundle.edit}" title="#{objectBundle.edit}" />
          <h:commandButton value="#{objectBundle.delete}"
                           image="#{userSessionBean.icons.delete}"
                           alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
                           action="#{caseDemandsBean.removeDemand}"
                           rendered="#{row.demandId != null}"
                           disabled="#{!caseBean.editable}"
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

  <t:div styleClass="resultBar" rendered="#{caseDemandsBean.rowCount > 0}">
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
                     action="#{caseDemandsBean.createDemand}"
                     rendered="#{row.demandId == null}"
                     disabled="#{!caseBean.editable or caseDemandsBean.editingDemand != null}"
                     styleClass="addButton"  />
  </t:div>

  <t:div rendered="#{caseDemandsBean.editingDemand != null}"
            styleClass="editingPanel">

    <t:div>
      <h:outputText value="#{caseBundle.caseDemands_type}:" 
                    style="width:15%" styleClass="textBox"/>
      <t:selectOneMenu value="#{caseDemandsBean.editingDemand.demandTypeId}"
                       styleClass="selectBox"
                       disabled="#{!caseBean.editable}">
        <f:selectItems value="#{caseDemandsBean.allTypeItems}" />
      </t:selectOneMenu>
      <h:commandButton action="#{caseDemandsBean.showEditType}"
                       rendered="#{caseDemandsBean.renderShowEditTypeButton}"
                       value="#{objectBundle.show}"
                       image="#{userSessionBean.icons.show}"
                       alt="#{objectBundle.show}" title="#{objectBundle.show}"
                       styleClass="showButton" />
    </t:div>

    <t:div>
      <h:outputText value="#{caseBundle.caseDemands_comments}: " 
                    style="width:15%;vertical-align:top" styleClass="textBox"/>
      <h:inputTextarea value="#{caseDemandsBean.editingDemand.comments}"
                       styleClass="inputBox" style="width:80%"
                       onkeypress="checkMaxLength(this, #{caseDemandsBean.propertySize.comments})"
                       readonly="#{!caseBean.editable}"/>
    </t:div>

    <t:div styleClass="actionsRow">
      <h:commandButton action="#{caseDemandsBean.storeDemand}" 
                       styleClass="addButton" value="#{objectBundle.store}"
                       disabled="#{!caseBean.editable}"
                       onclick="showOverlay()"/>
      <h:commandButton action="#{caseDemandsBean.cancelDemand}"
                       styleClass="cancelButton" value="#{objectBundle.cancel}" 
                       immediate="true"/>
    </t:div>
  </t:div>

</jsp:root>
