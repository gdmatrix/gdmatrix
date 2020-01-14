<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
                var="objectBundle"/>
  <f:loadBundle basename="org.santfeliu.doc.web.resources.DocumentBundle"
                var="documentBundle"/>
  <f:loadBundle basename="org.santfeliu.grx.web.resources.GrxBundle"
                var="grxBundle"/>

  <t:div styleClass="mapBrowser">

    <h:panelGrid styleClass="filterPanel" columns="2" style="width:100%"
                 columnClasses="col1, col2">
      <h:panelGroup>
        <h:inputText value="#{browserBean.filter.title}" styleClass="inputBox"
                     style="vertical-align:middle" />
        <h:commandButton value="#{objectBundle.search}" styleClass="searchButton"
                         style="vertical-align:middle" action="#{browserBean.search}"/>
      </h:panelGroup>
      <h:panelGroup>
        <h:outputLink value="#{browserBean.javaWebStartURL}/jdesign-studio.jnlp"
                      styleClass="viewerLink">
          <h:graphicImage url="/common/grx/images/viewer.gif" style="border:none;vertical-align:middle;margin-right:2px" />
          <h:outputText value="#{grxBundle.startJDS}" />
        </h:outputLink>
        <h:outputLink value="#{userSessionBean.selectedMenuItem.properties.guideURL}"
                      styleClass="guideLink" target="_blank"
                      rendered="#{userSessionBean.selectedMenuItem.properties.guideURL != null}">
          <h:graphicImage url="/common/grx/images/guide.gif" style="border:none;vertical-align:middle;margin-right:2px" />
          <h:outputText value="#{grxBundle.guideJDS}" />
        </h:outputLink>
      </h:panelGroup>
    </h:panelGrid>

    <t:div styleClass="resultBar" rendered="#{browserBean.rows != null}">
      <t:dataScroller for="data"
        firstRowIndexVar="firstRow"
        lastRowIndexVar="lastRow"
        rowsCountVar="rowCount"
        rendered="#{browserBean.rowCount > 0}">
        <h:outputFormat value="#{objectBundle.resultRange}"
          style="margin-top:10px;display:block">
          <f:param value="#{firstRow}" />
          <f:param value="#{lastRow}" />
          <f:param value="#{rowCount}" />
        </h:outputFormat>
      </t:dataScroller>
      <h:outputText value="#{objectBundle.no_results_found}"
        rendered="#{browserBean.rowCount == 0}" />
    </t:div>

    <t:dataTable id="data" value="#{browserBean.rows}" var="row" width="100%"
                 first="#{browserBean.firstRowIndex}"
                 rows="#{browserBean.pageSize}"
                 rendered="#{browserBean.rowCount > 0}"
                 newspaperColumns="2"
                 rowStyleClass="#{row.docId == objectBean.objectId ? 'selectedRow' : null}"
                 styleClass="resultList" rowClasses="row1,row2"
                 headerClass="header" footerClass="footer">

      <t:column>
        <t:div style="text-align:center;padding:10px;">
          <h:outputText value="#{browserBean.title}" styleClass="mapTitle" />
        </t:div>
        <t:div style="text-align:center">
          <h:graphicImage url="#{browserBean.imageURL}" alt=""
                          style="border-style:solid;border-color:black;border-width:1px"
                          width="300px" height="240px"/>
        </t:div>
        <t:div style="text-align:center;padding:10px;">
          <h:outputLink value="#{browserBean.javaWebStartURL}/jds-launcher.jnlp?id=#{row.content.contentId}"
                        style="border-style:none">
            <h:graphicImage url="/common/grx/images/webstart.gif" style="border-style:none" />
          </h:outputLink>
        </t:div>
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
  </t:div>
</jsp:root>