<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  <f:loadBundle basename="org.santfeliu.news.web.resources.NewsBundle"
                var="newsBundle"/>

  <t:div styleClass="newInaccessible">
    <t:div styleClass="errorMessage">
      <h:outputText value="#{newsBundle.new_inaccessible_error}"
                    styleClass="text" />    
    </t:div>
  </t:div>

</jsp:root>
