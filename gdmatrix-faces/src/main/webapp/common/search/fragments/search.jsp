<?xml version='1.0' encoding='windows-1252'?>

<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.search.web.resources.SearchBundle"
                var="searchBundle"/>
  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
    var="objectBundle" />

  <t:saveState value="#{searchBean}" />

  <t:div styleClass="globalSearch">

    <t:div styleClass="filterLayer">
      
      <t:div styleClass="module">
        <h:outputLabel value="#{searchBundle.moduleToSearch}:" styleClass="moduleLabel" />
        <t:selectOneMenu value="#{searchBean.selectedModule}"
                         styleClass="moduleSelector">
          <f:selectItem itemLabel="#{searchBundle.agendaUpper}"
                        itemValue="AGENDA" />
          <f:selectItem itemLabel="#{searchBundle.newsUpper}"
                        itemValue="NEWS" />
          <f:selectItem itemLabel="#{searchBundle.documentsUpper}"
                        itemValue="DOC" />
          <f:selectItem itemLabel="#{searchBundle.webUpper}"
                        itemValue="WEB" />
        </t:selectOneMenu>
      </t:div>

      <t:div styleClass="keywords">
        <h:outputLabel value="#{searchBundle.textToSearch}:" styleClass="textLabel" />
        <h:inputText value="#{searchBean.inputText}"
          styleClass="inputText" />
        <sf:commandButton id="default_button" action="#{searchBean.search}"
          value="#{searchBundle.search}" styleClass="searchButton" renderBox="true" />
      </t:div>

      <t:div styleClass="help"
        rendered="#{userSessionBean.selectedMenuItem.properties.searchHelp != null}">
        <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.searchHelp}"
          styleClass="helpLabel"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </t:div>
    </t:div>


    <h:panelGroup styleClass="centralLayer" rendered="#{searchBean.searchDone}">

      <t:div styleClass="resultSummary">
        <t:dataScroller for="#{searchBean.dataTableId}"
                        rendered="#{!searchBean.noResultsFound}"
                        firstRowIndexVar="firstRow"
                        lastRowIndexVar="lastRow"
                        rowsCountVar="rowCount">
          <h:outputFormat value="#{objectBundle.resultRange}">
            <f:param value="#{firstRow}" />
            <f:param value="#{lastRow}" />
            <f:param value="#{rowCount}" />
          </h:outputFormat>
        </t:dataScroller>
        <h:outputText value="#{searchBundle.noResultsFound}"
           rendered="#{searchBean.noResultsFound}"/>
      </t:div>

      <t:div styleClass="agendaSearch" rendered="#{searchBean.agendaSearchSelected}">
        <t:div styleClass="results">
          <h:dataTable id="agendaDataTable"
                       value="#{searchBean.agendaItemList}" var="item"
                       rowClasses="row1,row2"
                       first="#{searchBean.firstRowIndex}"
                       rows="#{searchBean.maxRowsPerPage}">
            <h:column>
              <t:div styleClass="item">
                <t:div rendered="#{searchBean.agendaRenderId}" styleClass="id">
                  <h:outputText value="#{item.id}" />
                </t:div>
                <t:div rendered="#{searchBean.agendaRenderDate}" styleClass="date">
                  <h:outputText value="#{searchBean.itemDate}" />
                </t:div>
                <t:div rendered="#{searchBean.agendaRenderName}" styleClass="name">
                  <h:outputLink rendered="#{searchBean.eventRenderAsLink}"
                                value="#{searchBean.agendaLink}">
                    <sf:outputText value="#{item.info1}"
                                   translator="#{userSessionBean.translator}"
                                   translationGroup="#{searchBean.agendaTranslationGroup}" />
                  </h:outputLink>
                  <sf:outputText rendered="#{!searchBean.eventRenderAsLink}"
                                 value="#{item.info1}"
                                 translator="#{userSessionBean.translator}"
                                 translationGroup="#{searchBean.agendaTranslationGroup}" />
                </t:div>
                <t:div rendered="#{searchBean.agendaRenderObserv}" styleClass="observ">
                  <sf:outputText value="#{item.info2}" escape="false"
                                 translator="#{userSessionBean.translator}"
                                 translationGroup="#{searchBean.agendaTranslationGroup}" />
                </t:div>
                <t:div rendered="#{searchBean.agendaRenderScore}" styleClass="score">
                  <h:outputText value="#{item.score}" />
                </t:div>
              </t:div>
            </h:column>
          </h:dataTable>
        </t:div>
      </t:div>

      <t:div styleClass="newsSearch" rendered="#{searchBean.newsSearchSelected}">
        <t:div styleClass="results">
          <h:dataTable id="newsDataTable"
                       value="#{searchBean.newsItemList}" var="item"
                       rowClasses="row1,row2"
                       first="#{searchBean.firstRowIndex}"
                       rows="#{searchBean.maxRowsPerPage}">
            <h:column>
              <t:div styleClass="item">
                <t:div rendered="#{searchBean.newsRenderId}" styleClass="id">
                  <h:outputText value="#{item.id}" />
                </t:div>
                <t:div rendered="#{searchBean.newsRenderDate}" styleClass="date">
                  <h:outputText value="#{searchBean.itemDate}" />
                </t:div>
                <t:div rendered="#{searchBean.newsRenderHeadline}" styleClass="headline">                  
                  <h:outputLink rendered="#{searchBean.newRenderAsLink}" 
                                value="#{searchBean.newLink}">
                    <sf:outputText value="#{item.info1}"
                                   translator="#{userSessionBean.translator}"
                                   translationGroup="#{searchBean.newsTranslationGroup}" />
                  </h:outputLink>
                  <sf:outputText rendered="#{!searchBean.newRenderAsLink}"
                                 value="#{item.info1}"
                                 translator="#{userSessionBean.translator}"
                                 translationGroup="#{searchBean.newsTranslationGroup}" />
                </t:div>
                <t:div rendered="#{searchBean.newsRenderSummary}" styleClass="summary">
                  <sf:outputText value="#{item.info2}" escape="false"
                                 translator="#{userSessionBean.translator}"
                                 translationGroup="#{searchBean.newsTranslationGroup}" />
                </t:div>
                <t:div rendered="#{searchBean.newsRenderScore}" styleClass="score">
                  <h:outputText value="#{item.score}" />
                </t:div>
              </t:div>
            </h:column>
          </h:dataTable>
        </t:div>
      </t:div>

      <t:div styleClass="docSearch" rendered="#{searchBean.docSearchSelected}">
        <t:div styleClass="results">
          <h:dataTable id="docDataTable"
                       value="#{searchBean.docItemList}" var="item"
                       rowClasses="row1,row2"
                       first="#{searchBean.firstRowIndex}"
                       rows="#{searchBean.maxRowsPerPage}">
            <h:column>
              <t:div styleClass="item">
                <t:div rendered="#{searchBean.docRenderId}" styleClass="id">
                  <h:outputText value="#{item.id}" />
                </t:div>
                <t:div rendered="#{searchBean.docRenderDate}" styleClass="date">
                  <h:outputText value="#{searchBean.itemDate}" />
                </t:div>
                <t:div rendered="#{searchBean.docRenderMimeType}" styleClass="mimeType">
                  <h:graphicImage url="#{searchBean.docFileTypeImage}" />
                </t:div>
                <t:div rendered="#{searchBean.docRenderName}" styleClass="name">
                  <h:outputLink value="#{searchBean.docLink}">
                    <h:outputText value="#{item.info2}" />
                  </h:outputLink>
                </t:div>
                <t:div rendered="#{searchBean.docRenderScore}" styleClass="score">
                  <h:outputText value="#{item.score}" />
                </t:div>
              </t:div>
            </h:column>
          </h:dataTable>
        </t:div>
      </t:div>

      <t:div styleClass="webSearch" rendered="#{searchBean.webSearchSelected}">
        <t:div styleClass="results">
          <h:dataTable id="webDataTable"
                       value="#{searchBean.webItemList}" var="item"
                       rowClasses="row1,row2"
                       first="#{searchBean.firstRowIndex}"
                       rows="#{searchBean.maxRowsPerPage}">
            <h:column>
              <t:div styleClass="item">
                <t:div rendered="#{searchBean.webRenderId}" styleClass="id">
                  <h:outputText value="#{item.id}" />
                </t:div>
                <t:div rendered="#{searchBean.webRenderDate}" styleClass="date">
                  <h:outputText value="#{searchBean.itemDate}" />
                </t:div>
                <t:div rendered="#{searchBean.webRenderLabel}" styleClass="label">
                  <h:outputLink value="#{searchBean.webLink}">
                    <sf:outputText value="#{item.info1}"
                                   translator="#{userSessionBean.translator}"
                                   translationGroup="#{searchBean.webTranslationGroup}" />
                  </h:outputLink>
                </t:div>
                <t:div rendered="#{searchBean.webRenderScore}" styleClass="score">
                  <h:outputText value="#{item.score}" />
                </t:div>
              </t:div>
            </h:column>
          </h:dataTable>
        </t:div>
      </t:div>

      <t:div styleClass="scroller" rendered="#{!searchBean.noResultsFound}">
        <t:dataScroller for="#{searchBean.dataTableId}"
          fastStep="100"
          paginator="true"
          paginatorMaxPages="#{searchBean.maxPages}"
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
      </t:div>

    </h:panelGroup>

    <t:div styleClass="messagesLayer">
      <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
                  showSummary="true" infoClass="infoMessage"
                  warnClass="warnMessage" errorClass="errorMessage"
                  fatalClass="fatalMessage" />
    </t:div>

  </t:div>

</jsp:root>