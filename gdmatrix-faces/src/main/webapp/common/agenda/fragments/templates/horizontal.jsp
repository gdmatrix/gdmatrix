<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  <jsp:directive.page contentType="text/html;charset=UTF-8"/>

  <h:panelGrid columns="2" columnClasses="ilcol1,ilcol2" width="100%"
               styleClass="interiorLayout" summary="" title="">

    <t:div styleClass="leftMenu">
      <jsp:include page="/common/agenda/fragments/filter/${eventSearchBean.filterName}.jsp"/>
    </t:div>

    <t:div styleClass="eventView">
      <jsp:include page="/common/agenda/fragments/view/${eventSearchBean.viewName}.jsp"/>
    </t:div>
    
  </h:panelGrid>
</jsp:root>
