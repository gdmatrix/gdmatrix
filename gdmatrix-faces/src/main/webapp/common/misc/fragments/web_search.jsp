<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />  

  <f:loadBundle basename="org.santfeliu.misc.websearch.web.resources.WebSearchBundle"
    var="webSearchBundle" />

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    globalOnly="true"
    layout="table"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />

  <t:div styleClass="webSearch">

    <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties['header.docId']}"
       rendered="#{userSessionBean.selectedMenuItem.properties['header.docId'] != null}"
       translator="#{userSessionBean.translator}"
       translationGroup="webSearch" />

    <t:div styleClass="webSearchPanel">
      <t:outputLabel for="webSearchInput" 
                     value="#{webSearchBundle.words}:" 
                     styleClass="label" />
      <h:inputText id="webSearchInput" value="#{webSearchBean.words}" styleClass="words" />
      <t:commandButton id="default_button" 
        value="#{webSearchBundle.search}"
        action="#{webSearchBean.search}" styleClass="searchButton" />
    </t:div>

    <t:dataList id="results" value="#{webSearchBean.results}" styleClass="resultList"
      var="result" rows="#{webSearchBean.pageSize}" layout="unorderedList" 
      rendered="#{webSearchBean.results != null and webSearchBean.resultCount > 0}">
      <t:div styleClass="resultDiv">
        <t:div>
          <h:outputLink value="#{result.menuItem.actionURL}"
            target="#{result.menuItem.target}" styleClass="resultLink">
            <sf:outputText value="#{result.menuItem.label}"
              translator="#{userSessionBean.translator}"
              translationGroup="webSearch" />
            <h:outputText value=" (#{result.score})" 
              rendered="#{userSessionBean.administrator}" />
          </h:outputLink>
        </t:div>
        <t:dataList value="#{result.menuItem.cursorPath}" var="menuItem">
          <h:panelGroup rendered="#{not menuItem.root and menuItem.mid != result.nodeId}"
            styleClass="itemPath">
            <h:outputText value="/" />
            <sf:outputText value=" #{menuItem.label} "
              translator="#{userSessionBean.translator}"
              translationGroup="webSearch"/>
          </h:panelGroup>
        </t:dataList>
      </t:div>
    </t:dataList>

    <t:dataScroller for="results"
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

    <sf:outputText value="#{webSearchBean.linksMessage}"
      styleClass="linksMessage"
      translator="#{userSessionBean.translator}"
      translationGroup="webSearch"
      rendered="#{not empty webSearchBean.links}" />

    <t:dataList id="links" value="#{webSearchBean.links}" var="link"
      styleClass="linkList" layout="unorderedList" 
      rendered="#{webSearchBean.links != null and webSearchBean.linkCount > 0}">
      <t:div styleClass="linkDiv">
        <h:outputLink value="#{link.url}" styleClass="link" target="_blank">
          <sf:outputText value="#{link.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="webSearch" />
        </h:outputLink>
      </t:div>
    </t:dataList>

    <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties['footer.docId']}"
       rendered="#{userSessionBean.selectedMenuItem.properties['footer.docId'] != null}" />

  </t:div>

</jsp:root>
