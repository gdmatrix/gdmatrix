<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >


  <t:div styleClass="mainPanel" style="width:100%">
    <t:div styleClass="propertyRow">
      <h:outputText value="#{caseBundle.case_type}: "
                    styleClass="name" style="width:15%"/>
      <sf:outputText value="#{panel.caseType}"
        styleClass="value"
        translator="#{userSessionBean.translator}"
        translationGroup="case:#{panel.case.caseId}"/>
    </t:div>

    <t:div styleClass="propertyRow">
      <h:outputText value="#{caseBundle.case_title}: "
                    styleClass="name" style="width:15%"/>
      <sf:outputText value="#{panel.case.title}"
        styleClass="value"
        translator="#{userSessionBean.translator}"
        translationGroup="case:#{panel.case.caseId}"/>
    </t:div>

    <t:div styleClass="propertyRow">
      <h:outputText value="#{caseBundle.case_startDate}: "
                    styleClass="name" style="width:15%"/>
      <h:outputText value="#{panel.startDate}"
        styleClass="value" />
      <h:outputText value="#{panel.startTime}"
        styleClass="value" style="margin-left:5px"
        rendered="#{panel.startTime != null}"/>
    </t:div>

    <t:div styleClass="propertyRow">
      <h:outputText value="#{caseBundle.case_endDate}: "
                    styleClass="name" style="width:15%"/>
      <h:outputText value="#{panel.endDate}"
        styleClass="value" />
      <h:outputText value="#{panel.endTime}"
        styleClass="value" style="margin-left:5px"
        rendered="#{panel.endTime != null}"/>
    </t:div>

    <t:div styleClass="propertyRow">
      <h:outputText value="#{caseBundle.case_description}: "
                    styleClass="name" style="width:15%"/>
      <sf:outputText value="#{panel.case.description}"
        styleClass="value"
        translator="#{userSessionBean.translator}"
        translationGroup="case: #{panel.case.caseId}"/>
    </t:div>
  </t:div>

</jsp:root>