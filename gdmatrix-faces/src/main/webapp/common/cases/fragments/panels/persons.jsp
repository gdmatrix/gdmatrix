<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

    <t:div styleClass="personsPanel">
      <t:dataTable value="#{panel.casePersons}" var="row"
                   summary="#{panel.tableSummary}"
                   rowClasses="row1,row2" footerClass="footer" headerClass="header"
                   bodyStyle="#{empty panel.casePersons ? 'display:none' : ''}"
                   styleClass="resultList" style="width:100%">
        <t:columns value="#{panel.resultsManager.columnNames}" var="column"
                   style="#{panel.resultsManager.columnStyle}"
                   styleClass="#{panel.resultsManager.columnStyleClass}"
                   sortPropertyName="casePersonTypeId" sortable="#{panel.resultsManager.groupBy}">
          <f:facet name="header">
            <t:commandSortHeader columnName="#{panel.resultsManager.localizedColumnName}"
               arrow="true" immediate="false" action="#{panel.sort}" >
                <h:outputText value="#{panel.resultsManager.localizedColumnName}" />
            </t:commandSortHeader>
          </f:facet>
          <h:outputText value="#{panel.resultsManager.columnValue}"
            styleClass="#{panel.formerPerson ? 'formerPerson' : null}"/>
        </t:columns>
      </t:dataTable>
    </t:div>

</jsp:root>