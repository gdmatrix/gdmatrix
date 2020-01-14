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
    <t:dataTable id="data" value="#{folderFoldersBean.rows}" var="row"
                 rowClasses="row1,row2" headerClass="header" footerClass="footer"
                 styleClass="resultList" style="width:100%"
                 bodyStyle="#{empty folderFoldersBean.rows ? 'display:none' : ''}"
                 rendered="#{!folderFoldersBean.new}"
                 rows="#{folderFoldersBean.pageSize}">
      <t:column style="width:12%">
        <f:facet name="header">
          <h:outputText value="#{feedBundle.folder}:" />
        </f:facet>
        <h:outputText value="#{row.folderId}" />
      </t:column>
      <t:column style="width:73%">
        <f:facet name="header">
          <h:outputText value="#{feedBundle.name}:" />
        </f:facet>
        <h:outputText value="#{row.name}" />
      </t:column>
      <t:column style="width:15%;text-align:right;">
        <h:panelGroup>
          <h:commandButton value="#{objectBundle.show}"
            image="#{userSessionBean.icons.show}"
            alt="#{objectBundle.show}" title="#{objectBundle.show}"
            action="#{folderFoldersBean.showChildFolder}"
            styleClass="showButton"  />
          <h:commandButton value="#{objectBundle.delete}"
            image="#{userSessionBean.icons.delete}"
            alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
            action="#{folderFoldersBean.removeChildFolder}"
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

  <t:div styleClass="resultBar" rendered="#{folderFoldersBean.rowCount > 0}"
         style="margin-top:10px">
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

  <t:div styleClass="editingPanel" rendered="#{!folderFoldersBean.new}" style="margin-bottom:15px">
    <h:outputText value="#{feedBundle.folder}:" styleClass="textBox"
                  style="width:15%" />
    <h:panelGroup>
      <t:selectOneMenu value="#{folderFoldersBean.editingFolderId}"
                       style="width:350px" styleClass="selectBox">
        <f:selectItems value="#{folderFoldersBean.folderItems}" />
      </t:selectOneMenu>
      <h:commandButton value="#{objectBundle.add}"        
                       image="#{userSessionBean.icons.add}"
                       alt="#{objectBundle.add}" title="#{objectBundle.add}"
        action="#{folderFoldersBean.storeChildFolder}"
        styleClass="addButton" />
      <h:commandButton action="#{folderFoldersBean.searchFolder}"
        styleClass="searchButton" value="#{objectBundle.search}" />
    </h:panelGroup>
  </t:div>

</jsp:root>
