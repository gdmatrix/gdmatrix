<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:div rendered="#{!mobileWidgetBean.topWidgetListEmpty}">
    <t:dataTable cellpadding="0" cellspacing="0" 
                 columnClasses="imageColumn,textColumn"
                 value="#{mobileWidgetBean.renderedTopWidgetList}" var="widget"
                 styleClass="widgetList"
                 rendered="#{mobileWidgetBean.topWidgetListOrientation == 'vertical'}"
                 id="verticalTopWidgetList">
      <t:column>
        <t:div rendered="#{!mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.widgetUrl}">
            <h:graphicImage url="#{widget.iconUrl}"
                            styleClass="image"
                            rendered="#{widget.iconUrl != null}" />
          </h:outputLink>
        </t:div>
        <t:div rendered="#{mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.bannerWidgetUrl}">
            <h:graphicImage url="#{widget.iconUrl}"
                            styleClass="image"
                            rendered="#{widget.iconUrl != null}" />
          </h:outputLink>
        </t:div>
      </t:column>
      <t:column>
        <t:div styleClass="textLayer" rendered="#{!mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.widgetUrl}" styleClass="link">
            <sf:outputText value="#{widget.title}"
                           styleClass="title"
                           translator="#{userSessionBean.translator}"
                           translationGroup="#{mobileWidgetBean.translationGroup}" />
          </h:outputLink>
          <sf:outputText value="#{widget.info}"
                        styleClass="info"
                        translator="#{userSessionBean.translator}"
                        translationGroup="#{mobileWidgetBean.translationGroup}" />
        </t:div>
        <t:div styleClass="textLayer" rendered="#{mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.bannerWidgetUrl}" styleClass="link">
            <sf:outputText value="#{widget.title}"
                           styleClass="title"
                           translator="#{userSessionBean.translator}"
                           translationGroup="#{mobileWidgetBean.translationGroup}" />
          </h:outputLink>
          <sf:outputText value="#{widget.info}"
                         styleClass="info"
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{mobileWidgetBean.translationGroup}" />
        </t:div>
      </t:column>
    </t:dataTable>

    <t:dataTable newspaperOrientation="horizontal"
                 newspaperColumns="#{mobileWidgetBean.topWidgetListColumnCount}"
                 cellpadding="0" cellspacing="0"                 
                 value="#{mobileWidgetBean.renderedTopWidgetList}" var="widget"
                 styleClass="widgetList"
                 rendered="#{mobileWidgetBean.topWidgetListOrientation == 'horizontal'}"
                 style="width:100%"
                 id="horizontalTopWidgetList">
      <t:column style="width:#{mobileWidgetBean.topWidgetListColumnWidth}%;text-align:center">
        <t:div rendered="#{!mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.widgetUrl}">
            <h:graphicImage url="#{widget.iconUrl}"
                            styleClass="image"
                            rendered="#{widget.iconUrl != null}" />
          </h:outputLink>
        </t:div>
        <t:div rendered="#{mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.bannerWidgetUrl}">
            <h:graphicImage url="#{widget.iconUrl}"
                            styleClass="image"
                            rendered="#{widget.iconUrl != null}" />
          </h:outputLink>
        </t:div>
        <t:div rendered="#{widget.visible}">
          <sf:outputText value="#{widget.title}"
                         styleClass="title"
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{mobileWidgetBean.translationGroup}" />
        </t:div>
      </t:column>
    </t:dataTable>

  </t:div>

  <t:div rendered="#{!mobileWidgetBean.centerWidgetListEmpty}">
    <t:div id="separator1" styleClass="separator" />
    <sf:widgetContainer
      id="frontendContainer"
      binding="#{mobileWidgetBean.centerWidgetContainer}"
      dynamic="false"
      layout="#{mobileWidgetBean.centerLayout}"
      columns="1"/>
  </t:div>

  <t:div rendered="#{!mobileWidgetBean.bottomWidgetListEmpty}">
    <t:div id="separator2" styleClass="separator" />

    <t:dataTable cellpadding="0" cellspacing="0"
                 columnClasses="imageColumn,textColumn"
                 value="#{mobileWidgetBean.renderedBottomWidgetList}" var="widget"
                 styleClass="widgetList"
                 rendered="#{mobileWidgetBean.bottomWidgetListOrientation == 'vertical'}"
                 id="verticalBottomWidgetList">
      <t:column>
        <t:div rendered="#{!mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.widgetUrl}">
            <h:graphicImage url="#{widget.iconUrl}"
                            styleClass="image"
                            rendered="#{widget.iconUrl != null}" />
          </h:outputLink>
        </t:div>
        <t:div rendered="#{mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.bannerWidgetUrl}">
            <h:graphicImage url="#{widget.iconUrl}"
                            styleClass="image"
                            rendered="#{widget.iconUrl != null}" />
          </h:outputLink>
        </t:div>
      </t:column>
      <t:column>
        <t:div styleClass="textLayer" rendered="#{!mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.widgetUrl}" styleClass="link">
            <sf:outputText value="#{widget.title}"
                           styleClass="title"
                           translator="#{userSessionBean.translator}"
                           translationGroup="#{mobileWidgetBean.translationGroup}" />
          </h:outputLink>
          <sf:outputText value="#{widget.info}"
                        styleClass="info"
                        translator="#{userSessionBean.translator}"
                        translationGroup="#{mobileWidgetBean.translationGroup}" />
        </t:div>
        <t:div styleClass="textLayer" rendered="#{mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.bannerWidgetUrl}" styleClass="link">
            <sf:outputText value="#{widget.title}"
                           styleClass="title"
                           translator="#{userSessionBean.translator}"
                           translationGroup="#{mobileWidgetBean.translationGroup}" />
          </h:outputLink>
          <sf:outputText value="#{widget.info}"
                         styleClass="info"
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{mobileWidgetBean.translationGroup}" />
        </t:div>
      </t:column>
    </t:dataTable>

    <t:dataTable newspaperOrientation="horizontal"
                 newspaperColumns="#{mobileWidgetBean.bottomWidgetListColumnCount}"
                 cellpadding="0" cellspacing="0"
                 value="#{mobileWidgetBean.renderedBottomWidgetList}" var="widget"
                 rendered="#{mobileWidgetBean.bottomWidgetListOrientation == 'horizontal'}"
                 styleClass="widgetList" style="width:100%"
                 id="horizontalBottomWidgetList">
      <t:column style="width:#{mobileWidgetBean.bottomWidgetListColumnWidth}%;text-align:center">
        <t:div rendered="#{!mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.widgetUrl}">
            <h:graphicImage url="#{widget.iconUrl}"
                            styleClass="image"
                            rendered="#{widget.iconUrl != null}" />
          </h:outputLink>
        </t:div>
        <t:div rendered="#{mobileWidgetBean.bannerWidget and widget.visible}">
          <h:outputLink value="#{mobileWidgetBean.bannerWidgetUrl}">
            <h:graphicImage url="#{widget.iconUrl}"
                            styleClass="image"
                            rendered="#{widget.iconUrl != null}" />
          </h:outputLink>
        </t:div>
        <t:div rendered="#{widget.visible}">
          <sf:outputText value="#{widget.title}"
                         styleClass="title"
                         translator="#{userSessionBean.translator}"
                         translationGroup="#{mobileWidgetBean.translationGroup}" />
        </t:div>
      </t:column>
    </t:dataTable>

  </t:div>

</jsp:root>
