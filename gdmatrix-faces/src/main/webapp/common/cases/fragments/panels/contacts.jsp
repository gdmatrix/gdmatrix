<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

    <t:div styleClass="contactsPanel">
      <t:dataList value="#{panel.casePersons}" var="cp"
                   rowClasses="row1,row2" footerClass="footer" headerClass="header"
                   styleClass="personsList" style="width:100%"
                   rendered="#{panel.groupByPerson}">
        <h:outputText value="#{cp.personView.fullName}"
           rendered="#{!empty panel.contacts and panel.renderPersonNames}" />

        <t:dataTable value="#{panel.contacts}" var="row" summary="#{panel.tableSummary}"
            rowClasses="row1,row2" footerClass="footer" headerClass="header"
            styleClass="resultList"
            bodyStyle="#{empty panel.contacts ? 'display:none' : ''}">
          <t:columns value="#{panel.resultsManager.columnNames}" var="column"
                     style="#{panel.resultsManager.columnStyle}"
                     styleClass="#{panel.resultsManager.columnStyleClass}"
                     sortPropertyName="casePersonTypeId"
                     sortable="#{panel.resultsManager.groupBy}">
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
                            rendered="#{not panel.resultsManager.imageColumn and not panel.resultsManager.linkColumn}"
                            escape="false"/>
          </h:panelGroup>      

          </t:columns>
        </t:dataTable>

      </t:dataList>

      <t:dataTable value="#{panel.contacts}" var="row" 
          summary="#{panel.tableSummary}"
          rowClasses="row1,row2" footerClass="footer" headerClass="header"
          styleClass="resultList"
          bodyStyle="#{empty panel.contacts ? 'display:none' : ''}"
          rendered="#{!panel.groupByPerson}">
        <t:columns value="#{panel.resultsManager.columnNames}" var="column"
                   style="#{panel.resultsManager.columnStyle}"
                   styleClass="#{panel.resultsManager.columnStyleClass}"
                   sortPropertyName="casePersonTypeId" sortable="#{panel.resultsManager.groupBy}">

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
                          rendered="#{not panel.resultsManager.imageColumn and not panel.resultsManager.linkColumn}"
                          escape="false"/>
          </h:panelGroup>
        </t:columns>
      </t:dataTable>

    </t:div>

</jsp:root>