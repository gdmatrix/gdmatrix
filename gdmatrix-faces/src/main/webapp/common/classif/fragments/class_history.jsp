<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.classif.web.resources.ClassificationBundle"
                var="classificationBundle" />

  <t:buffer into="#{table}">
    <t:dataTable id="data"
                 value="#{classHistoryBean.rows}"
                 first="#{classHistoryBean.firstRowIndex}"
                 rows="#{classHistoryBean.pageSize}"
                 var="row"
                 headerClass="header" footerClass="footer"
                 styleClass="resultList" style="width:100%"
                 rowStyleClass="#{row.classId == null ? 'closed' : 'open'}">

      <t:column style="width:25%">
        <f:facet name="header">
          <h:outputText value="#{classificationBundle.classMain_startDate}" />
        </f:facet>
        <h:outputText value="#{classHistoryBean.rowStartDate}"
                      styleClass="#{classHistoryBean.rowStyle}">
          <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
        </h:outputText>
      </t:column>

      <t:column style="width:25%">
        <f:facet name="header">
          <h:outputText value="#{classificationBundle.classMain_endDate}" />
        </f:facet>
        <h:outputText value="#{classHistoryBean.rowEndDate}"
                      styleClass="#{classHistoryBean.rowStyle}">
          <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
        </h:outputText>
      </t:column>

      <t:column style="width:40%">
        <f:facet name="header">
          <h:outputText value="#{classificationBundle.classMain_changeReason}" />
        </f:facet>
        <h:outputText value="#{row.changeReason}"
                      styleClass="#{classHistoryBean.rowStyle}"/>
      </t:column>

      <t:column style="width:10%" styleClass="actionsColumn">
        <h:commandButton value="#{objectBundle.show}"
                         image="#{userSessionBean.icons.show}"
                         alt="#{objectBundle.show}" title="#{objectBundle.show}"
                         styleClass="showButton" immediate="true"
                         action="#{classHistoryBean.showClass}"
                         rendered="#{row.classId != null}"/>
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

  <t:div styleClass="resultBar" rendered="#{classHistoryBean.rowCount > 0}">
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
