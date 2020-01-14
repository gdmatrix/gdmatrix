<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.kernel.web.resources.KernelBundle" 
    var="kernelBundle" />

  <t:div rendered="#{!roomBean.new}">
    <h:outputText value="#{objectBundle.object_id}:" styleClass="textBox"
      style="width:18%" />
    <h:outputText value="#{roomMainBean.room.roomId}" styleClass="outputBox"
      style="width:16%" />
  </t:div>
  
  <t:div>
    <h:outputText value="#{kernelBundle.address}:" styleClass="textBox" style="width:18%" />
    <t:selectOneMenu value="#{roomMainBean.room.addressId}"
                     styleClass="selectBox" 
                     style="width:70%">
      <f:selectItems value="#{roomMainBean.addressSelectItems}" /> 
    </t:selectOneMenu>
    <h:commandButton value="#{objectBundle.search}"
      image="#{userSessionBean.icons.search}"
      alt="#{objectBundle.search}" title="#{objectBundle.search}"
      action="#{roomMainBean.searchAddress}"
      styleClass="searchButton" rendered="#{roomMainBean.new}" />
    <h:commandButton value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      action="#{roomMainBean.showAddress}"
      styleClass="showButton" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.room_type}:"
      style="width:18%" styleClass="textBox"/>
    <t:selectOneMenu value="#{roomMainBean.room.roomTypeId}"
      styleClass="selectBox">
      <f:selectItem itemLabel=" " itemValue="" />
      <f:selectItems value="#{roomMainBean.allTypeItems}" />
    </t:selectOneMenu>
<!--
    <h:commandButton value="#{objectBundle.search}"
      image="#{userSessionBean.icons.search}"
      alt="#{objectBundle.search}" title="#{objectBundle.search}"
      styleClass="searchButton"
      action="#{roomMainBean.searchType}"/>
-->
    <h:commandButton action="#{roomMainBean.showType}"
      value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"
      rendered="#{roomMainBean.renderShowTypeButton}" />

  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.room_name}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{roomMainBean.room.name}" styleClass="inputBox"
      style="width:78%" valueChangeListener="#{roomMainBean.valueChanged}"
      maxlength="#{roomMainBean.propertySize.name}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.room_capacity}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{roomMainBean.room.capacity}" styleClass="inputBox"
      style="width:10%" valueChangeListener="#{roomMainBean.valueChanged}"
      maxlength="#{roomMainBean.propertySize.capacity}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.room_area}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{roomMainBean.room.area}" styleClass="inputBox"
      style="width:10%" valueChangeListener="#{roomMainBean.valueChanged}"
      maxlength="#{roomMainBean.propertySize.area}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.room_phone}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{roomMainBean.room.phone}" styleClass="inputBox"
      style="width:20%" valueChangeListener="#{roomMainBean.valueChanged}"
      maxlength="#{roomMainBean.propertySize.phone}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.room_fax}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{roomMainBean.room.fax}" styleClass="inputBox"
      style="width:20%" valueChangeListener="#{roomMainBean.valueChanged}"
      maxlength="#{roomMainBean.propertySize.fax}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.room_email}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{roomMainBean.room.email}" styleClass="inputBox"
      style="width:40%" valueChangeListener="#{roomMainBean.valueChanged}"
      maxlength="#{roomMainBean.propertySize.email}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.room_schedule}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{roomMainBean.room.schedule}" styleClass="inputBox"
      style="width:78%" valueChangeListener="#{roomMainBean.valueChanged}"
      maxlength="#{roomMainBean.propertySize.schedule}" />
  </t:div>
  
  <t:div>
    <h:outputText value="#{kernelBundle.room_spaceId}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{roomMainBean.room.spaceId}" styleClass="inputBox"
      style="width:10%" valueChangeListener="#{roomMainBean.valueChanged}"
      maxlength="#{roomMainBean.propertySize.spaceId}" />
  </t:div>    

  <t:div>
    <h:outputText value="#{kernelBundle.room_comments}:" styleClass="textBox"
      style="width:18%" />
    <h:inputTextarea value="#{roomMainBean.room.comments}"
                     valueChangeListener="#{roomMainBean.valueChanged}"
                     styleClass="inputBox" style="width:78%"
                     onkeypress="checkMaxLength(this, #{roomMainBean.propertySize.comments})" />
  </t:div>
  
</jsp:root>
