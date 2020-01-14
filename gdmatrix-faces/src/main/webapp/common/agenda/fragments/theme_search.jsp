<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.agenda.web.resources.AgendaBundle"
    var="agendaBundle" />

  <h:panelGrid columns="2" styleClass="filterPanel" summary=""
    columnClasses="column1, column2"
    headerClass="header" footerClass="footer">
    <f:facet name="header">
      <h:outputText />
    </f:facet>

    <h:outputText value="ThemeId:" />
    <h:inputText value="#{themeSearchBean.filter.themeId}"
                 styleClass="inputBox" style="width:20%" />

    <h:outputText value="#{agendaBundle.theme_description}:" />
    <h:inputText value="#{themeSearchBean.filter.description}"
      styleClass="inputBox" style="width:90%"/>

    <f:facet name="footer">
      <h:commandButton id="default_button" value="#{objectBundle.search}"
        styleClass="searchButton"
        action="#{themeSearchBean.search}" />
    </f:facet>

  </h:panelGrid>

  <t:div styleClass="resultBar" rendered="#{themeSearchBean.rows != null}">
    <t:dataScroller for="data"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      rendered="#{themeSearchBean.rowCount > 0}">
      <h:outputFormat value="#{objectBundle.resultRange}"
        style="margin-top:10px;display:block">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
    <h:outputText value="#{objectBundle.no_results_found}"
      rendered="#{themeSearchBean.rowCount == 0}" />
  </t:div>

  <t:dataTable rows="#{themeSearchBean.pageSize}"
    id="data"
    first="#{themeSearchBean.firstRowIndex}"
    value="#{themeSearchBean.rows}" var="row"
    rendered="#{themeSearchBean.rowCount > 0}"
    rowStyleClass="#{row.themeId == themeBean.objectId ? 'selectedRow' : null}"
    styleClass="resultList"
    rowClasses="row1,row2" headerClass="header" footerClass="footer">
    <t:column style="width:10%" >
      <f:facet name="header">
        <h:outputText value="#{agendaBundle.theme_id}" />
      </f:facet>
      <h:outputText value="#{row.themeId}"
        styleClass="#{row.themeId == themeBean.objectId ? 'selected' : ''}"
        rendered="#{themeSearchBean.rowCount > 0}"/>
    </t:column>

    <t:column style="width:70%">
      <f:facet name="header">
        <h:outputText value="#{agendaBundle.theme_description}" />
      </f:facet>
      <h:outputText value="#{row.description}" style="font-weight:bold"
        styleClass="#{row.themeId == themeBean.objectId ? 'selected' : ''}"
        />
    </t:column>
    <t:column style="width:20%" styleClass="actionsColumn">
      <h:panelGroup>
      <h:commandButton value="#{objectBundle.select}"
        image="#{userSessionBean.icons.back}"
        alt="#{objectBundle.select}" title="#{objectBundle.select}"
        rendered="#{controllerBean.selectableNode}"
        styleClass="selectButton" immediate="true"
        action="#{themeSearchBean.selectTheme}" />
      <h:commandButton value="#{objectBundle.show}"
        image="#{userSessionBean.icons.show}"
        alt="#{objectBundle.show}" title="#{objectBundle.show}"
        styleClass="showButton" immediate="true"
        action="#{themeSearchBean.showTheme}"/>
      </h:panelGroup>
    </t:column>

    <f:facet name="footer">
      <t:dataScroller for="data"
        fastStep="100"
        paginator="true"
        paginatorMaxPages="9"
        immediate="true"
        rendered="#{themeSearchBean.rows != null}"
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
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/first.png" alt="#{objectBundle.first}" title="#{objectBundle.first}"/>
        </f:facet>
        <f:facet name="last">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/last.png" alt="#{objectBundle.last}" title="#{objectBundle.last}"/>
        </f:facet>
        <f:facet name="previous">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/previous.png" alt="#{objectBundle.previous}" title="#{objectBundle.previous}"/>
        </f:facet>
        <f:facet name="next">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/next.png" alt="#{objectBundle.next}" title="#{objectBundle.next}"/>
        </f:facet>
        <f:facet name="fastrewind">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/fastrewind.png" alt="#{objectBundle.fastRewind}" title="#{objectBundle.fastRewind}"/>
        </f:facet>
        <f:facet name="fastforward">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/fastforward.png" alt="#{objectBundle.fastForward}" title="#{objectBundle.fastForward}"/>
        </f:facet>
      </t:dataScroller>
    </f:facet>
  </t:dataTable>

  <t:div styleClass="actionsBar">
     <h:commandButton value="#{objectBundle.current}"
       image="#{userSessionBean.icons.current}"
       alt="#{objectBundle.current}" title="#{objectBundle.current}"
       action="#{themeBean.show}" immediate="true"
       styleClass="currentButton" />
     <h:commandButton value="#{objectBundle.create}"        
       image="#{userSessionBean.icons.new}"
       alt="#{objectBundle.create}" title="#{objectBundle.create}"
       action="#{themeBean.create}" immediate="true"
       styleClass="createButton" />
  </t:div>

</jsp:root>
