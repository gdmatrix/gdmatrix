<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />

  <f:loadBundle basename="org.santfeliu.kernel.web.resources.KernelBundle" 
    var="kernelBundle" />

  <jsp:include page="/common/obj/page_history.jsp" />
  
  <t:div styleClass="objectSearch">
    <t:div styleClass="header">
      <h:panelGroup style="width:80%;display:inline-block;">
        <h:graphicImage rendered="#{controllerBean.objectBean.renderObjectTypeIcon}"
          value="#{controllerBean.objectBean.objectTypeIconPath}"
          style="border:none;" />
        <sf:outputText value="#{controllerBean.searchBean.title}:"
          translator="#{applicationBean.translator}"
          translationGroup="jsp:object_search"
          style="text-align:left" />
      </h:panelGroup>
      <h:panelGroup style="width:20%;display:inline-block;text-align:right">
        <h:commandButton action="#{controllerBean.close}"
          image="#{userSessionBean.icons.close}"
          alt="#{objectBundle.close}" title="#{objectBundle.close}"
          value="#{objectBundle.close}" immediate="true"
          styleClass="closeButton" />
      </h:panelGroup>
    </t:div>
  
    <t:div styleClass="filterPanel">
      <t:div style="margin-left:6px">
        <h:outputText value="#{kernelBundle.country}:" />
      </t:div>

      <t:div style="margin-left:6px">
        <sf:commandMenu value="#{countryBean.objectId}" 
          style="width:300px" styleClass="selectBox"
          action="#{countryToStreetBean.showCountry}">
          <f:selectItems value="#{countryToStreetBean.countrySelectItems}" />
        </sf:commandMenu>
        <h:commandButton value="#{objectBundle.add}"
          image="#{userSessionBean.icons.add}"
          alt="#{objectBundle.add}" title="#{objectBundle.add}"
          action="#{countryToStreetBean.addCountry}"
          styleClass="addButton" />
        <h:commandButton value="#{objectBundle.edit}"
          image="#{userSessionBean.icons.detail}"
          alt="#{objectBundle.edit}" title="#{objectBundle.edit}"
          action="#{countryToStreetBean.editCountry}"
          rendered="#{countryToStreetBean.editableCountry}"
          styleClass="editButton" />
        <h:commandButton value="#{objectBundle.delete}"           
          image="#{userSessionBean.icons.delete}"
          alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
          action="#{countryToStreetBean.removeCountry}"
          rendered="#{countryToStreetBean.editableCountry}"
          onclick="return confirm('#{objectBundle.confirm_remove}')"
          styleClass="removeButton" />
        <h:commandButton value="#{objectBundle.select}"
          image="#{userSessionBean.icons.back}"
          alt="#{objectBundle.select}" title="#{objectBundle.select}"
          action="#{controllerBean.select}"
          rendered="#{countryToStreetBean.selectableCountry}"
          styleClass="selectButton" />
      </t:div>
    
      <t:div style="margin-left:6px">
        <h:outputText value="#{kernelBundle.province}:" />
      </t:div>
      
      <t:div style="margin-left:6px">
        <sf:commandMenu value="#{provinceBean.objectId}" 
          style="width:300px" styleClass="selectBox"
          action="#{countryToStreetBean.showProvince}">
          <f:selectItems value="#{countryToStreetBean.provinceSelectItems}" />
        </sf:commandMenu>
        <h:commandButton value="#{objectBundle.add}"        image="#{userSessionBean.icons.add}"        alt="#{objectBundle.add}" title="#{objectBundle.add}"
          action="#{countryToStreetBean.addProvince}"
          rendered="#{countryToStreetBean.editableCountry}"
          styleClass="addButton" />
        <h:commandButton value="#{objectBundle.edit}"
          image="#{userSessionBean.icons.detail}"
          alt="#{objectBundle.edit}" title="#{objectBundle.edit}"
          action="#{countryToStreetBean.editProvince}"
          rendered="#{countryToStreetBean.editableProvince}"
          styleClass="editButton" />
        <h:commandButton value="#{objectBundle.delete}"           
          image="#{userSessionBean.icons.delete}"
          alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
          action="#{countryToStreetBean.removeProvince}"
          rendered="#{countryToStreetBean.editableProvince}"
          onclick="return confirm('#{objectBundle.confirm_remove}')"
          styleClass="removeButton" />
        <h:commandButton value="#{objectBundle.select}" 
          image="#{userSessionBean.icons.back}"
          alt="#{objectBundle.select}" title="#{objectBundle.select}"
          action="#{controllerBean.select}"
          rendered="#{countryToStreetBean.selectableProvince}"
          styleClass="selectButton" />
      </t:div>
    
      <t:div style="margin-left:6px">
        <h:outputText value="#{kernelBundle.city}:" />
      </t:div>
      
      <t:div style="margin-left:6px">
        <sf:commandMenu value="#{cityBean.objectId}" 
          style="width:300px" styleClass="selectBox"
          action="#{countryToStreetBean.showCity}">
          <f:selectItems value="#{countryToStreetBean.citySelectItems}" />
        </sf:commandMenu>
        <h:commandButton value="#{objectBundle.add}"        image="#{userSessionBean.icons.add}"        alt="#{objectBundle.add}" title="#{objectBundle.add}"
          action="#{countryToStreetBean.addCity}"
          rendered="#{countryToStreetBean.editableProvince}"
          styleClass="addButton" />
        <h:commandButton value="#{objectBundle.edit}"           image="#{userSessionBean.icons.detail}"           alt="#{objectBundle.edit}" title="#{objectBundle.edit}" 
          action="#{countryToStreetBean.editCity}"
          rendered="#{countryToStreetBean.editableCity}"
          styleClass="editButton" />
        <h:commandButton value="#{objectBundle.delete}"           image="#{userSessionBean.icons.delete}"           alt="#{objectBundle.delete}" title="#{objectBundle.delete}" 
          action="#{countryToStreetBean.removeCity}"
          onclick="return confirm('#{objectBundle.confirm_remove}')"
          rendered="#{countryToStreetBean.editableCity}"
          styleClass="removeButton" />
        <h:commandButton value="#{objectBundle.select}"
          image="#{userSessionBean.icons.back}"
          alt="#{objectBundle.select}" title="#{objectBundle.select}"
          action="#{controllerBean.select}"
          rendered="#{countryToStreetBean.selectableCity}"
          styleClass="selectButton" />      
      </t:div>
  
      <t:div style="margin-left:6px">
        <h:outputText value="#{kernelBundle.street}:" />
      </t:div>
      
      <t:div style="margin-left:6px;margin-bottom:20px">
        <sf:commandMenu value="#{streetBean.objectId}" 
          style="width:300px" styleClass="selectBox"
          action="#{countryToStreetBean.showStreet}">
          <f:selectItems value="#{countryToStreetBean.streetSelectItems}" />
        </sf:commandMenu>
        <h:commandButton value="#{objectBundle.add}"        image="#{userSessionBean.icons.add}"        alt="#{objectBundle.add}" title="#{objectBundle.add}"
          action="#{countryToStreetBean.addStreet}"
          rendered="#{countryToStreetBean.editableCity}"
          styleClass="addButton" />
        <h:commandButton value="#{objectBundle.edit}"           image="#{userSessionBean.icons.detail}"           alt="#{objectBundle.edit}" title="#{objectBundle.edit}"
          action="#{countryToStreetBean.editStreet}"
          rendered="#{countryToStreetBean.editableStreet}"
          styleClass="editButton" />
        <h:commandButton value="#{objectBundle.delete}"           image="#{userSessionBean.icons.delete}"           alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
          action="#{countryToStreetBean.removeStreet}"
          rendered="#{countryToStreetBean.editableStreet}"
          onclick="return confirm('#{objectBundle.confirm_remove}')"
          styleClass="removeButton" />
        <h:commandButton value="#{objectBundle.select}"
          image="#{userSessionBean.icons.back}"
          alt="#{objectBundle.select}" title="#{objectBundle.select}"
          action="#{controllerBean.select}"
          rendered="#{countryToStreetBean.selectableStreet}"
          styleClass="selectButton" />
      </t:div>
    </t:div>
  </t:div>

  <t:div styleClass="objectForm" style="margin-top:10px"
    rendered="#{countryToStreetBean.editing}">
    <jsp:include page="${requestScope['_object']}" />
  </t:div>

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    errorClass="errorMessage" warnClass="warnMessage" 
    infoClass="infoMessage"/>

</jsp:root>
