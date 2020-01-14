<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

    <t:div styleClass="addressesPanel">
      <t:dataTable value="#{panel.caseAddresses}" var="row" summary="#{panel.tableSummary}"
                   rowClasses="row1,row2" footerClass="footer" headerClass="header"
                   bodyStyle="#{empty panel.caseAddresses ? 'display:none' : ''}"
                   styleClass="resultList">

       <t:columns value="#{panel.resultsManager.columnNames}" var="column"
                   style="#{panel.resultsManager.columnStyle}"
                   styleClass="#{panel.resultsManager.columnStyleClass}">
        <f:facet name="header">
          <t:commandSortHeader columnName="#{panel.resultsManager.localizedColumnName}" arrow="true" immediate="false"
                               action="#{panel.sort}" >
              <h:outputText value="#{panel.resultsManager.columnName}" />
          </t:commandSortHeader>
        </f:facet>

        <h:panelGroup rendered="#{panel.resultsManager.customColumn and
          panel.resultsManager.columnName == 'description'}">
          <h:outputText value="#{row.addressView.description} " />
          <h:outputText value="(#{row.addressView.city}) " />
        </h:panelGroup>

        <h:panelGroup rendered="#{panel.resultsManager.customColumn and
          panel.resultsManager.columnName == 'linktomap'}">
          <h:outputLink value="#{panel.mapCode}"
                        target="_blank"
                        styleClass="showButton" style="margin-left:5px">
            <h:graphicImage value="/common/agenda/images/directions.png" style="vertical-align:middle"/>
          </h:outputLink>
        </h:panelGroup>

        <h:panelGroup rendered="#{panel.resultsManager.customColumn and
          panel.resultsManager.columnName == 'maponly'}">
          <t:div styleClass="map">
            <h:outputText value="#{panel.mapCode}" escape="false"/>
          </t:div>
        </h:panelGroup>

        <h:panelGroup rendered="#{panel.resultsManager.customColumn and
          panel.resultsManager.columnName == 'descandmap'}">
          <t:div styleClass="description">
            <h:outputText value="#{row.addressView.description} " />
            <h:outputText value="(#{row.addressView.city}) " />
          </t:div>
          <t:div styleClass="map" rendered="#{panel.localAddress}">
            <h:outputText value="#{panel.mapCode}" escape="false"/>
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
            rendered="#{panel.resultsManager.imageColumn and
                        panel.resultsManager.columnValue != null}"/>

          <!-- render as text -->
          <h:outputText value="#{panel.resultsManager.columnValue}"
            rendered="#{not panel.resultsManager.imageColumn and
                        not panel.resultsManager.linkColumn}"/>
        </h:panelGroup>
       </t:columns>

      </t:dataTable>
    </t:div>

</jsp:root>