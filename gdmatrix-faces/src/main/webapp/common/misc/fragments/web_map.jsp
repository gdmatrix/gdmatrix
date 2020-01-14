<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:saveState value="#{webMapBean.expandedMenuItems}" />
  <sf:saveScroll/>

  <sf:browser binding="#{webMapBean.headerBrowser}"
    port="#{applicationBean.defaultPort}"
    rendered="#{webMapBean.headerBrowser != null}"
    translator="#{userSessionBean.translator}"
    translationGroup="#{userSessionBean.translationGroup}" />

  <t:div styleClass="webmap">
    <sf:treeMenu var="item"
                 baseMid="#{webMapBean.baseMid}"
                 expandedMenuItems="#{webMapBean.expandedMenuItems}"
                 expandImageUrl="/images/expand.gif"
                 collapseImageUrl="/images/collapse.gif">
      <f:facet name="data">
        <sf:outputLink value="#{item.actionURL}"
          onclick="#{item.onclick}" target="#{item.target}"
          styleClass="#{item.leaf ? 'webMapLeaf' : 'webMapGroup'}"
          rendered="#{item.rendered}"
          ariaLabel="#{item.directProperties.ariaLabel}"
          ariaHidden="#{item.directProperties.ariaHidden == 'true'}"                
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}">
          <t:graphicImage value="/images/item.gif" alt=""
            styleClass="itemImage" title="" rendered="#{item.leaf}" />
          <sf:outputText value="#{item.label} "
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </sf:outputLink>
      </f:facet>
    </sf:treeMenu>
  </t:div>

  <sf:browser binding="#{webMapBean.footerBrowser}"
    port="#{applicationBean.defaultPort}"
    rendered="#{webMapBean.footerBrowser != null}"
    translator="#{userSessionBean.translator}"
    translationGroup="#{userSessionBean.translationGroup}" />

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />

</jsp:root>

