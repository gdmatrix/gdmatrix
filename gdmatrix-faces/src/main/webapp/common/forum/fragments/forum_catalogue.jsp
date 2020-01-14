<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.forum.web.resources.ForumBundle" var="forumBundle" />
<f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" var="objectBundle" />

  <t:saveState value="#{forumCatalogueBean}" />

  <t:div styleClass="objectSearch">

  <t:div rendered="#{userSessionBean.selectedMenuItem.properties.forumsHeaderDocId!=null}"
               styleClass="headerDocument">
    <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.forumsHeaderDocId}"
      port="#{applicationBean.defaultPort}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}"
      rendered="#{userSessionBean.selectedMenuItem.properties.forumsHeaderDocId!=null}"/>
  </t:div>

  <h:panelGrid columns="2" width="100%" styleClass="filterPanel"
    columnClasses="column1, column2"
    headerClass="header" footerClass="footer" rendered="#{forumCatalogueBean.editorUser}">

    <h:outputText value="#{objectBundle.object_id}:"/>
    <h:inputText value="#{forumCatalogueBean.forumIdFilter}"
      styleClass="inputBox" style="width:10%"
      valueChangeListener="#{forumCatalogueBean.processForumFilterValueChange}"/>
    <h:outputText value="#{forumBundle.name}:"/>
    <h:inputText value="#{forumCatalogueBean.forumFilter.name}"
      styleClass="inputBox" style="width:90%"
      valueChangeListener="#{forumCatalogueBean.processForumFilterValueChange}"/>
    <h:outputText value="#{forumBundle.description}:"/>
    <h:inputText value="#{forumCatalogueBean.forumFilter.description}"
      styleClass="inputBox" style="width:90%"
      valueChangeListener="#{forumCatalogueBean.processForumFilterValueChange}"/>
    <h:outputText value="#{forumBundle.group}:"
      rendered="#{userSessionBean.selectedMenuItem.properties.forumGroup == null}"/>
    <h:inputText value="#{forumCatalogueBean.forumFilter.group}"
      styleClass="inputBox" style="width:40%"
      valueChangeListener="#{forumCatalogueBean.processForumFilterValueChange}"
      rendered="#{userSessionBean.selectedMenuItem.properties.forumGroup == null}"/>
    <f:facet name="footer">
      <h:commandButton id="default_button"  value="#{objectBundle.search}"
        styleClass="searchButton" action="#{forumCatalogueBean.searchForums}"/>
    </f:facet>
  </h:panelGrid>

  <t:buffer into="#{table}">
    <t:dataTable rows="#{forumCatalogueBean.forumsData.pageSize}" id="data"
      first="#{forumCatalogueBean.forumsData.firstRowIndex}"
      value="#{forumCatalogueBean.forumsData.rows}" var="row"
      rendered="#{forumCatalogueBean.forumsData.rowCount > 0}"
      styleClass="resultList" summary="results"
      rowClasses="row1,row2" headerClass="header"
      footerClass="footer">
      <t:column style="border-right:0px;vertical-align:top" width="3%"
                rendered="#{forumCatalogueBean.renderForumTypeIcon}">
        <h:graphicImage value="/common/forum/images/forum.png" alt="forum" style="margin-top:2px"
                          rendered="#{row.forum.type == 'NORMAL'}"/>
        <h:graphicImage value="/common/forum/images/interview.png" alt="interview"
                          rendered="#{row.forum.type == 'INTERVIEW'}"/>
      </t:column>
      <t:column style="width:80%">
        <f:facet name="header">
          <sf:outputText value="#{forumCatalogueBean.forumNameLabel}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </f:facet>
        <t:div styleClass="forumName">
          <h:outputText value="#{row.forum.name} " />
          <h:graphicImage value="#{userSessionBean.icons.lock}"
             style="border:none;vertical-align:top;margin-top:1px" 
             alt="#{row.status == 'CLOSED_BEFORE' ? forumBundle.closedBefore : forumBundle.closed}"
             title="#{row.status == 'CLOSED_BEFORE' ? forumBundle.closedBefore : forumBundle.closed}"
             rendered="#{row.status == 'CLOSED' or row.status == 'CLOSED_BEFORE'}"/>
        </t:div>
        <t:div styleClass="forumDescription" >
          <h:outputText value=" #{row.forum.description}"/>
        </t:div>
      </t:column>
      <t:column style="width:5%;text-align:center" >
        <f:facet name="header">
          <h:outputLabel value="#{forumBundle.visibleQuestionCount}" />
        </f:facet>
        <t:div>
          <h:outputText value="#{row.visibleQuestionCount}"/>
        </t:div>
      </t:column>
      <t:column style="width:5%;text-align:center">
        <f:facet name="header">
          <h:outputLabel value="#{forumBundle.visibleAnswerCount}" />
        </f:facet>
        <t:div>
          <h:outputText value=" #{row.visibleAnswerCount}"/>
        </t:div>
      </t:column>
      <t:column style="width:10%" styleClass="actionsColumn">
        <sf:commandButton value="#{forumCatalogueBean.participateButtonLabel}"
          title="#{forumCatalogueBean.participateButtonLabel} #{row.forum.name}"
          ariaLabel="#{forumCatalogueBean.readButtonLabel} #{row.forum.name}"          
          styleClass="showButton" immediate="true"
          action="#{forumCatalogueBean.showForum}"
          rendered="#{forumCatalogueBean.participantUser and 
                    (row.status == 'OPEN' or row.status == 'OPEN_BEFORE')}"/>
        <sf:commandButton value="#{forumCatalogueBean.readButtonLabel}"
          title="#{forumCatalogueBean.readButtonLabel} #{row.forum.name}"
          ariaLabel="#{forumCatalogueBean.readButtonLabel} #{row.forum.name}"
          styleClass="showButton" immediate="true"
          action="#{forumCatalogueBean.showForum}"
          rendered="#{not forumCatalogueBean.participantUser or 
                      (row.status == 'CLOSED' or row.status == 'CLOSED_BEFORE')}"/>
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
  </t:buffer>

  <t:div styleClass="resultBar" rendered="#{forumCatalogueBean.forumsData.rows != null
         and forumCatalogueBean.editorUser}">
    <t:dataScroller for="data"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      rendered="#{forumCatalogueBean.forumsData.rowCount > 0}">
      <h:outputFormat value="#{objectBundle.resultRange}"
        style="margin-top:10px;display:block">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
    <h:outputText value="#{objectBundle.no_results_found}"
      rendered="#{forumCatalogueBean.forumsData.rowCount == 0}" />
  </t:div>

  <h:outputText value="#{table}" escape="false" />

  <t:div styleClass="actionsBar" >
     <h:commandButton value="#{objectBundle.create}" rendered="#{forumCatalogueBean.editorUser}"
       action="#{forumCatalogueBean.createForum}" immediate="true"
       styleClass="createButton"
       alt="#{objectBundle.create}" title="#{objectBundle.create}"  />
  </t:div>


  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />

</t:div>

</jsp:root>
