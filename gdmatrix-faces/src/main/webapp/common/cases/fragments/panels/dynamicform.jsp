<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >


  <t:div styleClass="dynamicFormPanel" >
    <sf:dynamicForm
      form="#{panel.form}"
      rendererTypes="#{panel.rendererTypes}"
      value="#{panel.data}"
      rendered="#{not panel.typeUndefined and panel.selector != null}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}_#{caseDetailBean.caseId}"/>
  </t:div>

</jsp:root>