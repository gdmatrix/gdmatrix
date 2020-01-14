<?xml version='1.0' encoding='UTF-8'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
                var="objectBundle" />

  <sf:browser binding="#{galleryBean.headerBrowser}"
    port="#{applicationBean.defaultPort}"
    translator="#{userSessionBean.translator}"
    translationGroup="#{userSessionBean.translationGroup}"
    rendered="#{galleryBean.headerBrowser != null}"/>

  <sf:div styleClass="galleryThumbnails" ariaHidden="true">
    <t:dataList id="data" value="#{galleryBean.items}" var="item" 
                rows="#{galleryBean.pageSize}">
      
      <h:commandLink action="#{galleryBean.showImage}" styleClass="thumbnail">
        <sf:graphicImage url="#{item.thumbnailUrl}" styleClass="image" 
                         alt="#{galleryBean.itemDescription != null ? galleryBean.itemDescription : galleryBean.itemDefaultDescription}"
                         title="#{galleryBean.itemDescription}" 
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{userSessionBean.translationGroup}" />
      </h:commandLink>      
  
    </t:dataList>

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
  </sf:div>

  <sf:browser binding="#{galleryBean.footerBrowser}"
    port="#{applicationBean.defaultPort}"
    translator="#{userSessionBean.translator}"
    translationGroup="#{userSessionBean.translationGroup}"
    rendered="#{galleryBean.footerBrowser != null}"/>

  <t:saveState value="#{galleryBean}"  />

</jsp:root>
