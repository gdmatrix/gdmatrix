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
    <t:dataTable id="data" value="#{feedEntriesBean.rows}" var="row"
                 rowClasses="row1,row2" headerClass="header"
                 footerClass="footer"
                 styleClass="resultList" style="width:100%"
                 rendered="#{!feedEntriesBean.new}"
                 rows="#{feedEntriesBean.pageSize}"
                 first="#{feedEntriesBean.firstRowIndex}">
      <t:column style="width:10%">
        <f:facet name="header">
          <h:outputText value="#{feedBundle.entryId}:" />
        </f:facet>
        <h:outputText value="#{row.entryId}" />
      </t:column>
      <t:column style="width:80%">
        <f:facet name="header">
          <h:outputText value="#{feedBundle.entry}:" />
        </f:facet>
        <h:panelGrid columns="1">
          <h:outputText value="#{feedEntriesBean.rowDate}" />
          <h:outputLink value="#{row.url}">
            <h:outputText value="#{row.title}" />
          </h:outputLink>          
        </h:panelGrid>
      </t:column>
      <t:column style="width:10%;text-align:center;">
        <f:facet name="header">
          <h:outputText value="#{feedBundle.visible}:" />
        </f:facet>        
        <h:commandButton value="#{feedBundle.switchVisibility}"
                         image="#{row.visible ? '/images/yes.png' : '/images/no.png'}"
                         alt="#{feedBundle.switchVisibility}"
                         title="#{feedBundle.switchVisibility}"
                         action="#{feedEntriesBean.switchEntryVisibility}"                         
                         onclick="return confirm('#{feedBundle.confirmSwitchVisibility}');" />
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

  <t:div styleClass="resultBar" rendered="#{feedEntriesBean.rowCount > 0}">
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

</jsp:root>
