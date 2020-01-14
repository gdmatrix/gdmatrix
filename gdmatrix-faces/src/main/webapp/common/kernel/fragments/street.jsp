<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:div styleClass="header">
    <h:outputText value="#{kernelBundle.street} #{countryToStreetBean.street.streetId}:" />
  </t:div>
  <t:div styleClass="tabs" style="height:10px" />

  <t:div styleClass="sheet"
    style="padding-top:10px;padding-bottom:20px">
    <t:div>
      <h:outputText value="#{kernelBundle.street_type}:"
        styleClass="textBox" style="width:20%" />
      <h:inputText value="#{countryToStreetBean.street.streetTypeId}" 
        styleClass="inputBox" />
    </t:div>
    <t:div>
      <h:outputText value="#{kernelBundle.street_name}:"
        styleClass="textBox" style="width:20%" />
      <h:inputText value="#{countryToStreetBean.street.particle}" 
        styleClass="inputBox" style="width:10%" />
      <h:inputText value="#{countryToStreetBean.street.name}" 
        styleClass="inputBox" style="width:50%;margin-left:4px;" />
    </t:div>
  </t:div>
    
  <t:div styleClass="footer">
    <h:commandButton value="#{objectBundle.store}"
      action="#{countryToStreetBean.storeStreet}" 
      styleClass="storeButton" />
    <h:commandButton value="#{objectBundle.cancel}"
      action="#{countryToStreetBean.cancel}" 
      immediate="true"
      styleClass="cancelButton" />
  </t:div>
</jsp:root>