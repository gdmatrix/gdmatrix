<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.feed.web.resources.FeedBundle"
    var="feedBundle" />

  <t:div>
    <h:outputText value="#{feedBundle.feed}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{feedMainBean.feed.feedId}"
                 styleClass="textBox" />
  </t:div>
  <t:div>
    <h:outputText value="*#{feedBundle.type}:"
                  styleClass="textBox" style="width:25%" />

    <t:selectOneMenu value="#{feedMainBean.feed.type}" styleClass="selectBox">
      <f:selectItem itemLabel="RSS" itemValue="rss" />
      <f:selectItem itemLabel="ATOM" itemValue="atom" />
    </t:selectOneMenu>
  </t:div>

  <t:div>
    <h:outputText value="#{feedBundle.name}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{feedMainBean.feed.name}"
                   styleClass="inputBox" style="width:70%" />
  </t:div>
  <t:div>
    <h:outputText value="#{feedBundle.url}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{feedMainBean.feed.url}"
                   styleClass="inputBox" style="width:70%" />
  </t:div>
  <t:div>
    <h:outputText value="*#{feedBundle.internalUrl}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{feedMainBean.feed.internalUrl}"
                   styleClass="inputBox" style="width:70%" />
  </t:div>
  <t:div>
    <h:outputText value="#{feedBundle.iconUrl}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{feedMainBean.feed.iconUrl}"
                   styleClass="inputBox" style="width:70%" />
  </t:div>  
  <t:div>
    <h:outputText value="#{feedBundle.refreshInterval}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{feedMainBean.feed.refreshInterval}"
                   styleClass="inputBox" style="width:10%" />
    <h:outputText value=" (#{feedBundle.minutes})" />
  </t:div>
  <t:div>
    <h:outputText value="#{feedBundle.entryLifeSpan}:"
                  styleClass="textBox" style="width:25%" />
    <h:inputText value="#{feedMainBean.feed.entryLifeSpan}"
                   styleClass="inputBox" style="width:10%" />
    <h:outputText value=" (#{feedBundle.days})" />
  </t:div>  
  <t:div>
    <h:outputText value="#{feedBundle.refreshDateTime}:"
                  styleClass="textBox" style="width:25%" />
    <h:outputText value="#{feedMainBean.lastRefreshDate}"
                   styleClass="textBox">
      <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
  </t:div>
  <t:div>
    <h:outputText value="* #{feedBundle.autoFields}"
                  styleClass="textBox" style="width:100%" />
  </t:div>

</jsp:root>
