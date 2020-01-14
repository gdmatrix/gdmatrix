<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

    <t:div styleClass="reportPanel">
      <sf:browser url="#{panel.url}"
        port="#{applicationBean.defaultPort}"
        iframe="#{panel.showInIFrame}" width="100%" height="500px"
        translator="#{not panel.showInIFrame ? userSessionBean.translator : null}"
        translationGroup="#{userSessionBean.translationGroup}"
        allowedHtmlTags="#{panel.allowedHtmlTags}" />
    </t:div>

</jsp:root>