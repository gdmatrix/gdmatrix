<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

    <sf:browser url="#{reportBean.headerURL}"
      port="#{applicationBean.defaultPort}"
      rendered="#{reportBean.headerURL != null}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}" />

    <t:div rendered="#{reportBean.formRendered}" styleClass="filterPanel">
      <sf:customForm url="#{reportBean.formURL}"
        values="#{reportBean.parameters}"
        newValues="#{reportBean.parameters}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
      <t:div styleClass="footer" rendered="#{reportBean.executeButtonRendered}">
        <h:commandButton id="default_button" value="#{reportBean.executeButtonLabel == null ?
          webBundle.buttonExecute : reportBean.executeButtonLabel}"
          action="#{reportBean.executeReport}" styleClass="executeButton" />
      </t:div>
    </t:div>

    <t:div rendered="#{reportBean.reportRendered and reportBean.printButtonRendered}">
      <h:outputLink value="#{reportBean.printURL}" target="blank" styleClass="printButton">
        <h:graphicImage url="/common/report/images/print.gif" />
        <sf:outputText value="#{reportBean.printButtonLabel}"
          rendered="#{reportBean.printButtonLabel != null}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
      </h:outputLink>
    </t:div>

    <t:div styleClass="body">
      <sf:browser url="#{reportBean.reportURL}"
        port="#{applicationBean.defaultPort}"
        iframe="#{reportBean.showInIFrame}" width="100%" height="500px"
        translator="#{not reportBean.showInIFrame ? userSessionBean.translator : null}"
        translationGroup="#{userSessionBean.translationGroup}" 
        allowedHtmlTags="#{reportBean.allowedHtmlTags}"
        readTimeout="#{reportBean.readTimeout}"/>
    </t:div>

    <sf:browser url="#{reportBean.footerURL}"
      port="#{applicationBean.defaultPort}"
      rendered="#{reportBean.footerURL != null}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}" />
</jsp:root>
