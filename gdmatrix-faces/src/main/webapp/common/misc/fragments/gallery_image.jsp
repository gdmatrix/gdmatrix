<?xml version='1.0' encoding='UTF-8'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
    var="objectBundle" />

  <t:div styleClass="galleryImage">

    <t:div styleClass="topScrollerBar">
      <h:commandButton action="#{galleryBean.showThumbnails}"
        styleClass="menuButton" image="/images/gallery.png"
        title="#{objectBundle.back}" />
      <t:dataScroller for="data" styleClass="rowCountBar"
        rowsCountVar="rowCount"
        pageIndexVar="pageIndex"
        immediate="true"
        renderFacetsIfSinglePage="false">
        <sf:outputText value="#{pageIndex} / #{rowCount}" styleClass="pageNumber" />
      </t:dataScroller>
    </t:div>


    <t:dataScroller for="data"
      rowsCountVar="rowCount"
      pageIndexVar="pageIndex"
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
    </t:dataScroller>

    <t:dataList id="data" value="#{galleryBean.items}" var="item" 
                rows="1" first="#{galleryBean.currentIndex}">
        <t:div styleClass="imageTitle"
             rendered="#{userSessionBean.selectedMenuItem.properties.renderImageTitle == 'true'}">
          <sf:outputText value="#{item.view.document.title}"
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{userSessionBean.translationGroup}" />
        </t:div>
        <sf:graphicImage url="#{item.imageUrl}" styleClass="image" 
                         alt="#{galleryBean.itemDescription != null ? galleryBean.itemDescription : ''}"
                         title="#{galleryBean.itemDescription}" 
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{userSessionBean.translationGroup}" />
        <t:div styleClass="comments">
          <sf:outputText value="#{galleryBean.itemDescription}"
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{userSessionBean.translationGroup}" />
        </t:div>
        
    </t:dataList>


    <t:dataScroller for="data"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      pageIndexVar="pageIndex"
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
    </t:dataScroller>

    <t:div styleClass="bottomScrollerBar">
      <t:dataScroller for="data"
        rowsCountVar="rowCount"
        pageIndexVar="pageIndex"
        immediate="true"
        styleClass="page"
        renderFacetsIfSinglePage="false">
        <sf:outputText value="#{pageIndex} / #{rowCount}" styleClass="page" />
      </t:dataScroller>
    </t:div>
    
  </t:div>

  <t:saveState value="#{galleryBean}"  />
</jsp:root>
