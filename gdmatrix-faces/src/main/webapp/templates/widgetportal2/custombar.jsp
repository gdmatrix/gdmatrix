<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <sf:heading level="1" styleClass="element-invisible" 
              rendered="#{userSessionBean.selectedMenuItem.properties.widgetSelectorTitle != null}">
    <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.widgetSelectorTitle}"                   
                   translator="#{userSessionBean.translator}"
                   translationGroup="#{userSessionBean.translationGroup}" />
  </sf:heading>
  
  <t:div styleClass="widgetSelector">
    <t:div id="widgetSelectorLeftLink" forceId="true" style="visibility:hidden" styleClass="leftLink">
      <h:outputLink value="#" onclick="widgetSelectorLeft(); return false;" style="display: block; height: 100%;">
        <sf:graphicImage id="widgetSelectorLeftImg"
          url="/templates/widgetportal2/images/widget_selector_left_on.png"
          alt="#{userSessionBean.selectedMenuItem.properties.widgetSelectorPrevBlockText != null ? 
                 userSessionBean.selectedMenuItem.properties.widgetSelectorPrevBlockText : 'Mostrar bloc anterior del selector de widgets'}"                                          
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </h:outputLink>
    </t:div>
    <t:div styleClass="itemArea">
      <t:div styleClass="item visible" id="widgetSelectorItem0" forceId="true">
        <sf:commandButton action="#{widgetBean.setup}"
          value="#{userSessionBean.selectedMenuItem.properties.setupButtonText}"
          title="#{userSessionBean.selectedMenuItem.properties.setupButtonTitle}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />                    
      </t:div>
      <h:outputText escape="false" value="#{widgetBean.widgetSelectorItemsHtml}" />
    </t:div>
    <t:div id="widgetSelectorRightLink" forceId="true" style="visibility:hidden" styleClass="rightLink">
      <h:outputLink value="#" onclick="widgetSelectorRight(); return false;" style="display: block; height: 100%;">
        <sf:graphicImage id="widgetSelectorRightImg"
          url="/templates/widgetportal2/images/widget_selector_right_on.png"
          alt="#{userSessionBean.selectedMenuItem.properties.widgetSelectorNextBlockText != null ? 
                 userSessionBean.selectedMenuItem.properties.widgetSelectorNextBlockText : 'Mostrar bloc següent del selector de widgets'}"                                          
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </h:outputLink>
    </t:div>
  </t:div>  
 
  <h:outputText escape="false" value="#{widgetBean.widgetSelectorScripts}" /> 
  
</jsp:root>