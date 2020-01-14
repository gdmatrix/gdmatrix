<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.feed.web.resources.FeedBundle"
    var="feedBundle" />

  <h:panelGrid columns="2" styleClass="filterPanel" summary=""
    columnClasses="column1, column2"
    headerClass="header" footerClass="footer">

    <f:facet name="header">
      <h:outputText />
    </f:facet>

    <h:outputText value="#{feedBundle.folderId}:" />
    <h:inputText value="#{folderSearchBean.folderIdInput}"
      styleClass="inputBox" style="width:15%" />

    <h:outputText value="#{feedBundle.name}:" />
    <h:inputText value="#{folderSearchBean.filter.name}"
      styleClass="inputBox" style="width:70%"/>

    <f:facet name="footer">
      <h:commandButton id="default_button" value="#{objectBundle.search}"
        styleClass="searchButton"
        action="#{folderSearchBean.search}" />
    </f:facet>
  </h:panelGrid>

  <t:div styleClass="resultBar" rendered="#{folderSearchBean.rows != null}">
    <t:dataScroller for="data"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      rendered="#{folderSearchBean.rowCount > 0}">
      <h:outputFormat value="#{objectBundle.resultRange}"
        style="margin-top:10px;display:block">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
    <h:outputText value="#{objectBundle.no_results_found}"
      rendered="#{folderSearchBean.rowCount == 0}" />
  </t:div>

  <t:dataTable rows="#{folderSearchBean.pageSize}" id="data"
    first="#{folderSearchBean.firstRowIndex}"
    value="#{folderSearchBean.rows}" var="row"
    rendered="#{folderSearchBean.rowCount > 0}"
    rowStyleClass="#{row.folderId == folderBean.objectId ? 'selectedRow' : null}"
    styleClass="resultList"
    rowClasses="row1,row2" headerClass="header" footerClass="footer">
    <t:column style="width:12%">
      <f:facet name="header">
        <h:outputText value="#{feedBundle.folder}" />
      </f:facet>
      <h:outputText value="#{row.folderId}"
        styleClass="#{row.folderId == folderBean.objectId ? 'selected' : ''}"/>
    </t:column>
    <t:column style="width:78%">
      <f:facet name="header">
        <h:outputText value="#{feedBundle.name}" />
      </f:facet>
      <h:outputText value="#{row.name}"
        styleClass="#{row.folderId == folderBean.objectId ? 'selected' : ''}"/>
    </t:column>
    <t:column style="width:10%" styleClass="actionsColumn">
      <h:panelGroup>
        <h:commandButton value="#{objectBundle.select}"
          image="#{userSessionBean.icons.back}"
          alt="#{objectBundle.select}" title="#{objectBundle.select}"
          rendered="#{controllerBean.selectableNode}"
          styleClass="selectButton" immediate="true"
          action="#{folderSearchBean.selectFolder}" />
        <h:commandButton value="#{objectBundle.show}"           
          image="#{userSessionBean.icons.show}"
          alt="#{objectBundle.show}" title="#{objectBundle.show}"
          styleClass="showButton" immediate="true"
          action="#{folderSearchBean.showFolder}" />
      </h:panelGroup>
    </t:column>
    <f:facet name="footer">
      <t:dataScroller for="data"
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

  <t:div styleClass="actionsBar">
     <h:commandButton value="#{objectBundle.current}"
                      image="#{userSessionBean.icons.current}"
                      alt="#{objectBundle.current}"
                      title="#{objectBundle.current}"
                      action="#{folderBean.show}"
                      immediate="true"
                      styleClass="currentButton" />
     <h:commandButton value="#{objectBundle.create}"
                      image="#{userSessionBean.icons.new}"
                      alt="#{objectBundle.create}"
                      title="#{objectBundle.create}"
                      action="#{folderBean.create}"
                      immediate="true"
                      styleClass="createButton" />
  </t:div>

</jsp:root>
