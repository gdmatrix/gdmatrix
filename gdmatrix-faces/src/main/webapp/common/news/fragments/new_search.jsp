<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.news.web.resources.NewsBundle"
                var="newsBundle"/>
  <sf:browser id="nsHeaderBrowser" 
              url="#{newSearchBean.headerUrl}"
              port="#{applicationBean.defaultPort}"
              rendered="#{newSearchBean.headerRender}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />

  <t:div id="filterPanel" styleClass="filterPanel"
    rendered="#{newSearchBean.filterRender}">
    
    <t:div styleClass="header">
      <h:outputText />
    </t:div>
    
 
    <t:div styleClass="column1">
      <h:outputLabel for="contentInputText" id="contentLabel" styleClass="textBox"
        value="#{newsBundle.new_search_content}:" />
    </t:div>
    <t:div styleClass="column2">
      <h:inputText id="contentInputText" value="#{newSearchBean.searchContent}"
        styleClass="inputBox" style="width:80%" />
    </t:div>
    
    <sf:div styleClass="column1" ariaHidden="true">    
      <h:outputLabel id="startDayLabel" styleClass="textBox"
                    value="#{newsBundle.new_search_startDay}:" />
    </sf:div>
    <t:div styleClass="column2">
      <sf:calendar id="startInputCalendar"
        externalFormat="dd/MM/yyyy|HH:mm"
        internalFormat="yyyyMMddHHmmss"
        value="#{newSearchBean.filter.startDateTime}"
        styleClass="calendarBox"
        buttonStyleClass="calendarButton"
        style="width:20%;"
        dayLabel="#{newsBundle.new_search_startDay}"
        hourLabel="#{newsBundle.new_search_startHour}"/>
    </t:div>
    
    <sf:div styleClass="column1" ariaHidden="true">    
      <h:outputLabel id="endDayLabel" styleClass="textBox"
                    value="#{newsBundle.new_search_endDay}:" />
    </sf:div>
    <t:div styleClass="column2">
      <sf:calendar id="endInputCalendar"
        externalFormat="dd/MM/yyyy|HH:mm"
        internalFormat="yyyyMMddHHmmss"
        value="#{newSearchBean.filter.endDateTime}"
        styleClass="calendarBox"
        buttonStyleClass="calendarButton"
        style="width:20%;"        
        dayLabel="#{newsBundle.new_search_endDay}"
        hourLabel="#{newsBundle.new_search_endHour}"/>
    </t:div>
    
    <t:div styleClass="column1" rendered="#{newSearchBean.filterUserRender}">    
      <h:outputLabel for="userIdInputText" id="userIdLabel" styleClass="textBox"
                    value="#{newsBundle.new_search_user}:" />
    </t:div>
    <t:div styleClass="column2" rendered="#{newSearchBean.filterUserRender}">
      <h:inputText id="userIdInputText" value="#{newSearchBean.filter.userId}"
                   styleClass="inputBox"
                   style="width:20%" />
    </t:div>
    
    <t:div styleClass="footer">
      <h:panelGroup id="actionsBar" styleClass="actionsBar">
        <h:commandButton value="#{objectBundle.search}" styleClass="searchButton"
                         action="#{newSearchBean.search}" id="default_button"/>
      </h:panelGroup>
    </t:div>
  </t:div>

  <t:div styleClass="actionsBar top" rendered="#{newSearchBean.editMode and newSearchBean.rowCount > 2}">
    <h:commandButton value="#{objectBundle.current}" 
     image="#{userSessionBean.icons.current}" alt="#{objectBundle.current}" title="#{objectBundle.current}"
     action="#{newBean.show}" immediate="true"
     styleClass="currentButton" />
    <h:commandButton value="#{objectBundle.create}" 
      image="#{userSessionBean.icons.new}" alt="#{objectBundle.create}" title="#{objectBundle.create}"
    action="#{newBean.create}" immediate="true"
    styleClass="createButton" />
  </t:div>
  
  <t:div styleClass="resultBar" rendered="#{newSearchBean.rows != null and newSearchBean.resultBarRender}">
    <t:dataScroller for="newSearchRootList"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      rendered="#{newSearchBean.rowCount > 0}">
      <h:outputFormat value="#{objectBundle.resultRange}"
        style="margin-top:10px;display:block">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
    <h:outputText value="#{objectBundle.no_results_found}"
      rendered="#{newSearchBean.rowCount == 0}" />
  </t:div>

  <t:dataTable id="newSearchRootList"
               value="#{newSearchBean.rows}" var="row"
               rendered="#{newSearchBean.rowCount > 0}"
               styleClass="resultList" summary="results"
               rowClasses="row1,row2" headerClass="header"
               footerClass="footer"
               rows="#{newSearchBean.pageSize}"
               first="#{newSearchBean.firstRowIndex}">
    <t:column style="width:10%" rendered="#{newSearchBean.editMode}" >
      <f:facet name="header">
        <h:outputText value="#{newsBundle.new_search_id}"
                      rendered="#{newSearchBean.colHeadersRender}"/>
      </f:facet>
      <h:outputText value="#{row.newId}" />
    </t:column>
    <t:column style="width:12%">
      <f:facet name="header">
        <h:outputText value="#{newsBundle.new_search_date}"
                      rendered="#{newSearchBean.colHeadersRender}"/>
      </f:facet>
      <h:outputText value="#{newSearchBean.newDate}" />
    </t:column>
    <t:column style="width:68%">
      <f:facet name="header">
        <h:outputText value="#{newsBundle.new_search_headline}"
                      rendered="#{newSearchBean.colHeadersRender}" />
      </f:facet>
      <h:graphicImage value="/common/news/images/#{row.draft ? 'draft.gif' : 'final.gif'}"
        alt="#{row.draft ? 'draft' : 'final'}"
        style="vertical-align:middle;margin-right:3px;"
        rendered="#{newSearchBean.editMode}" />
      <sf:outputText value="#{row.headline}" rendered="#{newSearchBean.editMode}"
                  translator="#{userSessionBean.translator}"
                  translationGroup="#{newSearchBean.translationGroup}" />            
      <h:outputLink id="headlineLink" styleClass="headlineLink"
                     value="#{newSearchBean.newLink}"
                     rendered="#{!newSearchBean.editMode}">
        <sf:outputText value="#{row.headline}" styleClass="headlineText"
                      translator="#{userSessionBean.translator}"
                      translationGroup="#{newSearchBean.translationGroup}" />
      </h:outputLink>
    </t:column>
    <t:column style="width:10%" styleClass="actionsColumn"
      rendered="#{newSearchBean.editMode}">
      <h:commandButton id="rootNewShowButton" styleClass="showButton"
               immediate="true" action="#{newSearchBean.showNew}"
               value="#{objectBundle.show}"
              image="#{userSessionBean.icons.show}"
              alt="#{objectBundle.show}" title="#{objectBundle.show}"/>
    </t:column>

  </t:dataTable>
    
  <t:dataScroller for="newSearchRootList"
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
    renderFacetsIfSinglePage="false"
    rendered="#{newSearchBean.paginatorRender}">
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
    

  <t:div styleClass="actionsBar" rendered="#{newSearchBean.editMode}">
    <h:commandButton value="#{objectBundle.current}" 
     image="#{userSessionBean.icons.current}" alt="#{objectBundle.current}" title="#{objectBundle.current}"
     action="#{newBean.show}" immediate="true"
     styleClass="currentButton" />
    <h:commandButton value="#{objectBundle.create}" 
      image="#{userSessionBean.icons.new}" alt="#{objectBundle.create}" title="#{objectBundle.create}"
    action="#{newBean.create}" immediate="true"
    styleClass="createButton" />
  </t:div>

  <sf:browser id="nsFooterBrowser" 
              url="#{newSearchBean.footerUrl}"
              port="#{applicationBean.defaultPort}"
              rendered="#{newSearchBean.footerRender}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />

</jsp:root>
