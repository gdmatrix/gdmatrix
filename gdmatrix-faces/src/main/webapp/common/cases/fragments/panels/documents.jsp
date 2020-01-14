<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

    <t:div styleClass="documentsPanel">
      <t:dataTable value="#{panel.caseDocuments}" var="row"  
                   summary="#{panel.tableSummary}"
                   rowClasses="row1,row2" footerClass="footer" headerClass="header"
                   bodyStyle="#{empty panel.caseDocuments ? 'display:none' : ''}"
                   styleClass="resultList" style="width:100%">

       <t:columns value="#{panel.resultsManager.columnNames}" var="column"
                   style="#{panel.resultsManager.columnStyle}"
                   styleClass="#{panel.resultsManager.columnStyleClass}">
        <f:facet name="header">
          <t:commandSortHeader columnName="#{panel.resultsManager.localizedColumnName}" arrow="true" immediate="false"
                               action="#{panel.sort}" >
              <h:outputText value="#{panel.resultsManager.localizedColumnName}" />
          </t:commandSortHeader>
        </f:facet>

        <h:panelGroup rendered="#{panel.resultsManager.customColumn and panel.resultsManager.columnName == 'document.title'}">
          <h:graphicImage url="#{panel.fileTypeImage}" height="16" width="16"
                          style="vertical-align:middle;margin-right:5px"
                          rendered="#{row.document != null and row.document.docId != null}"/>
          <h:outputLink value="#{caseDocumentsBean.documentUrl}" target="_blank"
                        rendered="#{row.document != null
                          and row.document.content != null and row.document.content.contentId != null}"
                        style="margin-left:2px" styleClass="documentLink">
            <h:outputText value="#{row.document.title}" />
            <h:outputText value=" (#{panel.extension})"
                          styleClass="#{row.document.state == 'DELETED' ? 'deletedDocument' : null}"
                          rendered="#{panel.extensionRender 
                            and panel.extension != null}" />             
          </h:outputLink>

          <h:outputText value="#{row.document.title != null ? row.document.title : caseBundle.caseDocuments_notAvaliable}" rendered="#{row.document.content == null}" />
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
        
      </t:dataTable>
    </t:div>

</jsp:root>