<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

  <t:div styleClass="casesPanel">

    <t:buffer into="#{table}">
      <t:dataTable id="casesTable" value="#{panel.caseCases}" var="row"  
                   summary="#{panel.tableSummary}"
                   first="#{panel.first}"
                   rowClasses="row1,row2" footerClass="footer" headerClass="header"
                   bodyStyle="#{empty panel.caseCases ? 'display:none' : ''}"
                   styleClass="resultList" style="width:100%"
                   rows="#{panel.pageSize}">

        <t:columns value="#{panel.resultsManager.columnNames}" var="column"
                   style="#{panel.resultsManager.columnStyle}"
                   styleClass="#{panel.resultsManager.columnStyleClass}">
          <f:facet name="header">
            <t:commandSortHeader columnName="#{panel.resultsManager.localizedColumnName}" arrow="true" immediate="false"
                                 action="#{panel.sort}" >
              <h:outputText value="#{panel.resultsManager.columnName}" />
            </t:commandSortHeader>
          </f:facet>

          <h:panelGroup rendered="#{(panel.resultsManager.columnName == 'relCase.title' or panel.resultsManager.columnName == 'mainCase.title')
                                    and panel.resultsManager.customColumn}">
            <t:div styleClass="title">
              <h:outputLink value="#{panel.showCaseUrl}" >
                <h:outputText value="#{panel.resultsManager.columnValue}" />
              </h:outputLink>
            </t:div>
          </h:panelGroup>

          <h:panelGroup rendered="#{!panel.resultsManager.customColumn}">
            <!-- render as link -->
            <h:outputLink target="_blank" value="#{panel.resultsManager.columnValue}"
                          rendered="#{panel.resultsManager.linkColumn}">
              <h:outputText value="#{panel.resultsManager.columnValue}"/>
            </h:outputLink>

            <!-- render as image -->
            <h:graphicImage value="#{panel.resultsManager.columnValue}"
              rendered="#{panel.resultsManager.imageColumn and panel.resultsManager.columnValue != null}"/>

            <!-- render as text -->
            <h:outputText value="#{panel.resultsManager.columnValue}"
              rendered="#{not panel.resultsManager.imageColumn and not panel.resultsManager.linkColumn}"/>
          </h:panelGroup>
        </t:columns>
            
        <f:facet name="footer">
          <t:dataScroller for="casesTable"
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
  
    <h:outputText value="#{table}" escape="false"/>          
</t:div>


</jsp:root>