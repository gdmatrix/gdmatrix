<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:div id="customBar" styleClass="customBar">
    <t:div styleClass="widgetInfo">
      <t:div>
        <t:div>
          <h:graphicImage title="" value="/images/empty.png" />
          <sf:outputText
            value="#{userSessionBean.selectedMenuItem.properties.widgetInfo}"
            styleClass="info"
            translator="#{userSessionBean.translator}"
            translationGroup="#{agendaBean.eventTranslationGroup}" />
        </t:div>
      </t:div>
    </t:div>
    <t:div styleClass="setupButtons">
      <sf:commandButton action="#{widgetBean.setup}"
        value="#{userSessionBean.selectedMenuItem.properties.setupButton}"
        title="#{userSessionBean.selectedMenuItem.properties.setupButtonTitle}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}">
        <h:graphicImage title="" value="/images/empty.png" />
      </sf:commandButton>

      <sf:commandButton action="#{widgetBean.sort}"
        value="#{userSessionBean.selectedMenuItem.properties.sortButton}"
        title="#{userSessionBean.selectedMenuItem.properties.sortButtonTitle}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}">
        <h:graphicImage title="" value="/images/empty.png" />
      </sf:commandButton>

      <sf:commandButton action="#{widgetBean.reset}"
        value="#{userSessionBean.selectedMenuItem.properties.resetButton}"
        title="#{userSessionBean.selectedMenuItem.properties.resetButtonTitle}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}">
        <h:graphicImage title="" value="/images/empty.png" />
      </sf:commandButton>
      </t:div>
  </t:div>
</jsp:root>