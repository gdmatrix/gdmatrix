<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
                var="objectBundle" />

  <f:loadBundle basename="org.santfeliu.feed.web.resources.FeedBundle"
                var="feedBundle" />

  <t:buffer into="#{table}">
    <t:dataTable id="data" value="#{feedFoldersBean.rows}" var="row"
                 rowStyleClass="#{feedFoldersBean.editingFolder == null ? null :
                 (feedFoldersBean.editingFolder.folderId == row.folderId ?
                 'selectedRow' : null)}"
                 rowClasses="row1,row2" headerClass="header"
                 footerClass="footer"
                 styleClass="resultList" style="width:100%"
                 rendered="#{!feedFoldersBean.new}"
                 rows="#{feedFoldersBean.pageSize}"
                 first="#{feedFoldersBean.firstRowIndex}">
      <t:column style="width:12%">
        <f:facet name="header">
          <h:outputText value="#{feedBundle.folder}:" />
        </f:facet>
        <h:outputText value="#{row.folderId}" />
      </t:column>
      <t:column style="width:68%">
        <f:facet name="header">
          <h:outputText value="#{feedBundle.name}:" />
        </f:facet>
        <h:outputText value="#{row.folderName}" />
      </t:column>
      <t:column style="width:20%;text-align:right;">
        <h:panelGroup>
          <h:commandButton value="#{objectBundle.show}"
                           image="#{userSessionBean.icons.show}"
                           alt="#{objectBundle.show}"
                           title="#{objectBundle.show}"
                           action="#{feedFoldersBean.showFolder}"
                           styleClass="showButton"  />
          <h:commandButton value="#{objectBundle.edit}"
                           image="#{userSessionBean.icons.detail}"
                           alt="#{objectBundle.edit}"
                           title="#{objectBundle.edit}"
                           action="#{feedFoldersBean.editFeedFolder}"
                           styleClass="addButton"  />
          <h:commandButton value="#{objectBundle.delete}"
                           image="#{userSessionBean.icons.delete}"
                           alt="#{objectBundle.delete}"
                           title="#{objectBundle.delete}"
                           action="#{feedFoldersBean.removeFeedFolder}"
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

  <t:div styleClass="resultBar" rendered="#{feedFoldersBean.rowCount > 0}">
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

  <t:div style="width:100%;text-align:right">
    <h:commandButton action="#{feedFoldersBean.createFeedFolder}"
                     disabled="#{feedFoldersBean.editingFolder != null}"
                     styleClass="addButton" value="#{objectBundle.add}"
                     image="#{userSessionBean.icons.add}"
                     alt="#{objectBundle.add}" title="#{objectBundle.add}" />
  </t:div>

  <t:div styleClass="editingPanel"
            rendered="#{feedFoldersBean.editingFolder != null}">

    <t:div>
      <h:outputText value="#{feedBundle.folder}:" styleClass="textBox"
                    style="width:12%" />
      <h:panelGroup>
        <t:selectOneMenu value="#{feedFoldersBean.editingFolder.folderId}"
                         style="width:66%; height:20px" styleClass="selectBox">
          <f:selectItems value="#{feedFoldersBean.folderItems}" />
        </t:selectOneMenu>
        <h:commandButton value="#{objectBundle.search}"
                         image="#{userSessionBean.icons.search}"
                         alt="#{objectBundle.search}"
                         title="#{objectBundle.search}"
                         action="#{feedFoldersBean.searchFolder}"
                         styleClass="searchButton" />
      </h:panelGroup>
    </t:div>

    <t:div styleClass="actionsRow">
      <h:commandButton action="#{feedFoldersBean.storeFeedFolder}"
                       styleClass="addButton" value="#{objectBundle.store}" />
      <h:commandButton action="#{feedFoldersBean.cancelFeedFolder}"
                       styleClass="cancelButton"
                       value="#{objectBundle.cancel}" />
    </t:div>
    
  </t:div>

</jsp:root>
