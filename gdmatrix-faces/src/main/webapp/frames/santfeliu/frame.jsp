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
  
  <h:panelGrid id="generalLayout" 
               columns="1"
               styleClass="general_main" 
               rowClasses="general_body"
               headerClass="general_header"
               footerClass="general_footer" 
               summary="">
    <f:facet name="header">
      <t:div>
      <h:outputText value="&lt;header id='frame_header' aria-label='#{webBundle.mainHeader}'&gt;" escape="false"/>      
      
      <h:panelGrid columns="3"
                   style="width:100%"
                   styleClass="headerTable"
                   rowClasses="general_bar"
                   columnClasses="column1, column2, column3"
                   summary="">

        <sf:outputLink value="#" onclick="goMid('#{userSessionBean.menuModel.rootMid}')"
                      accesskey="h"
                      title="#{userSessionBean.selectedMenuItem.properties.headerLogoTitle}"
                      translator="#{userSessionBean.translator}"
                      translationGroup="#{userSessionBean.translationGroup}">
          <sf:graphicImage url="/frames/santfeliu/images/header_text.gif" 
             style="border-style:none;height:41px" 
            alt="#{userSessionBean.selectedMenuItem.properties.headerLogoTitle}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" 
            />        
        </sf:outputLink>

        <t:div style="position:relative;z-index:10">

          <h:graphicImage url="/frames/santfeliu/images/header_back.gif"
                          alt="Oh!SantFeliu" style="height:41px;width:100%"/>

          <t:div style="position:absolute; top:38px; left:-4px; width:170px; z-index:1;"
            rendered="#{userSessionBean.selectedMenuItem.properties.renderWebsMenu == 'true'}">
            <h:panelGrid columns="2" style="width:100%">
              <h:outputLink value="#" onclick="push('mainform:menubar')"
                            accesskey="w">
                <h:graphicImage url="/frames/santfeliu/images/webs.gif" 
                  alt="#{userSessionBean.selectedMenuItem.properties.topwebTitle != null ? 
                userSessionBean.selectedMenuItem.properties.topwebTitle : 
                'Els nostres webs'}" style="border-style:none" />
              </h:outputLink>
              <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.topwebTitle != null ?
                userSessionBean.selectedMenuItem.properties.topwebTitle : 
                'Els nostres webs'}" styleClass="websText"
                translator="#{userSessionBean.translator}"
                translationGroup="frame:santfeliu" />
            </h:panelGrid>
            <f:verbatim>
              <script type="text/javascript">opaqueLayer()</script>
            </f:verbatim>
            <t:div id="menubar" styleClass="menubar" style="visibility:hidden">
              <sf:navigationMenu id="mbar"
                                 value="main"
                                 var="item"
                                 baseMid="#{userSessionBean.selectedMenuItem.path[0]}"
                                 orientation="vertical"
                                 style="margin-top:0px;width:100%;border-style:none"
                                 selectedStyleClass="mbar_item"
                                 unselectedStyleClass="mbar_item">
                  <h:graphicImage url="#{item.properties.iconURL}"
                                  alt=""
                                  style="margin-right:4px;vertical-align:middle;border:0;width:32px;height:32px"
                                  rendered="#{item.properties.topweb == 'true'}" />
                  <sf:outputText value="#{item.properties.description}"
                                rendered="#{item.properties.topweb == 'true'}"
                                translator="#{userSessionBean.translator}"
                                translationGroup="frame:santfeliu" />
              </sf:navigationMenu>
            </t:div>
          </t:div>

        </t:div>

        <h:graphicImage url="/frames/santfeliu/images/header_round.gif"
          alt="" style="width:7px;height:41px" />

      </h:panelGrid>

      <h:outputText value="&lt;/header&gt;" escape="false"/>      
      </t:div>                 
    </f:facet>

    <t:div>
      <jsp:include page="/templates/${userSessionBean.template}/template.jsp" />              
    </t:div>

    <f:facet name="footer">
      <t:div>
      <h:outputText value="&lt;footer id='frame_footer' aria-label='#{webBundle.mainFooter}'&gt;" escape="false"/>      
      
      <h:panelGrid columns="2" styleClass="footerTable" style="height:20px" columnClasses="column1, column2">
        <h:panelGroup>
          <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.footerInfo}"
            translator="#{userSessionBean.translator}"
            translationGroup="frame:santfeliu" />
          
          <h:panelGroup rendered="#{userSessionBean.selectedMenuItem.properties.legalNoticeMid != null}">
            <h:outputText value=" - " />                  
            <h:outputLink value="javascript:goMid('#{userSessionBean.selectedMenuItem.properties.legalNoticeMid}')"
              styleClass="bottomLink">
              <h:outputText value="#{webBundle.legalNotice}" />
            </h:outputLink>
          </h:panelGroup>
          
          <h:panelGroup rendered="#{userSessionBean.selectedMenuItem.properties.accessibilityMid != null}">
            <h:outputText value=" - " />
            <h:outputLink value="javascript:goMid('#{userSessionBean.selectedMenuItem.properties.accessibilityMid}')"
              styleClass="bottomLink">
              <h:outputText value="#{webBundle.accessibility}" />
            </h:outputLink>
          </h:panelGroup>

        </h:panelGroup>
        <h:outputText value="#{userSessionBean.selectedMenuItem.properties.footerEmail}" />
      </h:panelGrid>
      
      <h:outputText value="&lt;/footer&gt;" escape="false"/>      
      </t:div>
    </f:facet>

  </h:panelGrid>
  <f:verbatim>
    <script type="text/javascript">
      function push_Outside()
      {
        pushOutside('mainform:menubar');
      }
      document.onclick = push_Outside;
    </script>
  </f:verbatim>
        
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

