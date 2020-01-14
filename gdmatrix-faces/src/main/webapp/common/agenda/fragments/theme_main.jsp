<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.agenda.web.resources.AgendaBundle"
    var="agendaBundle" />

  <t:div>
    <h:outputText value="ThemeId:" styleClass="textBox"
      style="width:18%" />
    <h:outputText value="#{themeMainBean.theme.themeId}" styleClass="outputBox"
      style="width:20%" />
  </t:div>

  <t:div>
    <h:outputText value="#{agendaBundle.theme_description}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{themeMainBean.theme.description}" styleClass="inputBox"
      style="width:78%" 
      maxlength="#{themeMainBean.propertySize.description}" />
  </t:div>

</jsp:root>
