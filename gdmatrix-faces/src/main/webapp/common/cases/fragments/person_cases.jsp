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
    <t:dataTable id="data" value="#{personCasesBean.rows}" var="row"
                 rowClasses="row1,row2" headerClass="header" footerClass="footer"
                 rowIndexVar="index"
                 rowStyleClass="#{personCasesBean.rowStyleClass} row#{(index % 2) + 1}"
                 styleClass="resultList" style="width:100%"
                 bodyStyle="#{empty personCasesBean.rows ? 'display:none' : ''}"
                 rows="#{personCasesBean.pageSize}">
      <t:column style="width:10%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.personCases_id}:" />
        </f:facet>
        <h:outputText value="#{row.caseObject.caseId}" />
      </t:column>

      <t:column style="width:35%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.personCases_title}:" />
        </f:facet>
        
        <h:panelGrid columns="1" styleClass="#{personCasesBean.rowStyleClass}">
          <h:outputFormat style="font-size:11px; font-weight:normal;"
                          value="#{caseBundle.casePersons_closedDateInterval}"
                          rendered="#{personCasesBean.viewStartDate != null
                                      and personCasesBean.viewEndDate != null}">
            <f:param value="#{personCasesBean.viewStartDate}" />
            <f:param value="#{personCasesBean.viewEndDate}" />
          </h:outputFormat>

          <h:outputFormat style="font-size:11px; font-weight:normal;"
                          value="#{caseBundle.casePersons_openToDateInterval}"
                          rendered="#{personCasesBean.viewStartDate == null
                                      and personCasesBean.viewEndDate != null}">
            <f:param value="#{personCasesBean.viewEndDate}" />
          </h:outputFormat>

          <h:outputFormat style="font-size:11px; font-weight:normal;"
                          value="#{caseBundle.casePersons_openFromDateInterval}"
                          rendered="#{personCasesBean.viewStartDate != null
                                      and personCasesBean.viewEndDate == null}">
            <f:param value="#{personCasesBean.viewStartDate}" />
          </h:outputFormat>        

          <h:outputText value="#{row.caseObject.title}" />
        </h:panelGrid>
        
        
      </t:column>

      <t:column style="width:20%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.personCases_type}:" />
        </f:facet>
        <h:outputText value="#{personCasesBean.typeDescription}" />
      </t:column>

      <t:column style="width:25%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.personCases_comments}:" />
        </f:facet>
        <h:outputText value="#{row.comments}" />
      </t:column>

      <t:column style="width:10%" styleClass="actionsColumn">
        <h:panelGroup>
          <h:commandButton action="#{personCasesBean.showCase}"
                           rendered="#{row.casePersonId != null}"
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

  <t:div styleClass="resultBar" rendered="#{personCasesBean.rowCount > 0}">
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

</jsp:root>
