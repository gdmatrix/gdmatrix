<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:outputText value="&lt;nav id='frame_nav_skipTop' aria-label='#{webBundle.skipToContent}'&gt;" escape="false"/> 
  <t:div id="templateSkipTop" forceId="true" styleClass="item skipDiv">
    <h:outputLink value="#sf_main_content" rendered="#{userSessionBean.selectedMenuItem.properties.skipToContentIconURL != null}">
      <t:graphicImage url="#{userSessionBean.selectedMenuItem.properties.skipToContentIconURL}"
        alt="#{webBundle.skipToContent}" />          
    </h:outputLink>
  </t:div>
  <h:outputText value="&lt;/nav&gt;" escape="false"/>            

  <t:div styleClass="centralPage">
    <t:div styleClass="headerPanel">
      <h:outputText value="&lt;header id='frame_header' aria-label='#{webBundle.mainHeader}'&gt;" escape="false"/>      
        <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.headerDocId}"
          rendered="#{userSessionBean.selectedMenuItem.properties.headerDocId != null}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      <h:outputText value="&lt;/header&gt;" escape="false"/>      
    </t:div>

    <t:div>
      <jsp:include page="/templates/${userSessionBean.template}/template.jsp" />
    </t:div>

    <t:div styleClass="footerPanel">
      <h:outputText value="&lt;footer id='frame_footer' aria-label='#{webBundle.mainFooter}'&gt;" escape="false"/>      
        <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.footerDocId}"
          rendered="#{userSessionBean.selectedMenuItem.properties.footerDocId != null}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      <h:outputText value="&lt;/footer&gt;" escape="false"/>      
    </t:div>
  </t:div>
          
  <h:outputText value="&lt;nav id='frame_nav_skipBottom' aria-label='#{webBundle.skipToTop}'&gt;" escape="false"/>  
  <t:div id="templateSkipBottom" forceId="true" styleClass="skipDiv hide">
    <h:outputLink value="#"
                  onclick="skipToTop(); return false;"
                  rendered="#{userSessionBean.selectedMenuItem.properties.skipToTopIconURL != null}">
      <t:graphicImage url="#{userSessionBean.selectedMenuItem.properties.skipToTopIconURL}"
        alt="#{webBundle.skipToTop}" />          
    </h:outputLink>
  </t:div>
  <h:outputText value="&lt;/nav&gt;" escape="false"/>            

  <f:verbatim>
    <script type="text/javascript">      
      window.addEventListener('scroll', checkSkipBottomLink);
      window.addEventListener('load', checkSkipBottomLink);
    </script>
  </f:verbatim>
  
</jsp:root>

