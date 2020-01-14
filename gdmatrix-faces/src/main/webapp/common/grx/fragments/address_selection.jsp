<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.grx.web.resources.GrxBundle"
                var="grxBundle"/>

  <t:saveState value="#{viewerBean.context}" />

  <t:div>
    <t:dataTable var="address" value="#{viewerBean.context.addressList}"
      styleClass="addressList" headerClass="header"
      rowClasses="arow1, arow2">
      <t:column>
        <f:facet name="header">
          <h:outputText value="#{grxBundle.selectAddressLabel}:" />
        </f:facet>
        <h:commandLink action="#{viewerBean.locateAddress}"
          styleClass="addressEntry">
          <h:outputText value="#{address.name}" />
        </h:commandLink>
      </t:column>
    </t:dataTable>
  </t:div>

</jsp:root>