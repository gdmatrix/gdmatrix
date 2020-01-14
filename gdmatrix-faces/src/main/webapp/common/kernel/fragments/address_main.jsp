<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.kernel.web.resources.KernelBundle" 
    var="kernelBundle" />

  <t:div rendered="#{!addressBean.new}">
    <h:outputText value="#{objectBundle.object_id}:" styleClass="textBox"
      style="width:16%" />
    <h:outputText value="#{addressMainBean.address.addressId}" styleClass="outputBox"
      style="width:10%" />
  </t:div>
  
  <t:div>
    <h:outputText value="#{kernelBundle.address_type}:" styleClass="textBox" style="width:16%" />
    <t:selectOneMenu value="#{addressMainBean.address.addressTypeId}" 
      styleClass="selectBox">
      <f:selectItem itemLabel=" " itemValue="" />
      <f:selectItems value="#{addressMainBean.allTypeItems}" />
    </t:selectOneMenu>
    <h:commandButton action="#{addressMainBean.showType}"
      value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"
      rendered="#{addressMainBean.renderShowTypeButton}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.street}:" styleClass="textBox" style="width:16%" />
    <t:selectOneMenu value="#{addressMainBean.address.streetId}" 
      styleClass="selectBox" style="width:395px">
      <f:selectItems value="#{addressMainBean.streetSelectItems}" />
    </t:selectOneMenu>
    <h:commandButton value="#{objectBundle.search}" 
      image="#{userSessionBean.icons.search}" 
      alt="#{objectBundle.search}" title="#{objectBundle.search}" 
      action="#{addressMainBean.searchStreet}" 
      styleClass="searchButton" />
    <h:commandButton value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}" 
      alt="#{objectBundle.show}" title="#{objectBundle.show}" 
      action="#{addressMainBean.showStreet}" 
      styleClass="showButton" />
  </t:div>

  <t:div>
    <h:outputText value="Km:" styleClass="textBox" style="width:16%" />
    <h:inputText value="#{addressMainBean.address.km}" 
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.km}"/>
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.address_number} 1:" styleClass="textBox"
      style="width:16%" />
    <h:inputText value="#{addressMainBean.address.number1}" 
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.number1}"/>
    <h:outputText value="#{kernelBundle.address_bis}:" 
      styleClass="textBox" style="width:8%" />
    <h:inputText value="#{addressMainBean.address.bis1}" 
      styleClass="inputBox" style="width:3%"
      maxlength="#{addressMainBean.propertySize.bis1}"  />
  </t:div>
      
  <t:div>
    <h:outputText value="#{kernelBundle.address_number} 2:"
      styleClass="textBox" style="width:16%" />
    <h:inputText value="#{addressMainBean.address.number2}" 
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.number2}"/>
    <h:outputText value="#{kernelBundle.address_bis}:"
      styleClass="textBox" style="width:8%" />
    <h:inputText value="#{addressMainBean.address.bis2}" 
      styleClass="inputBox" style="width:3%"
      maxlength="#{addressMainBean.propertySize.bis2}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.address_block}:"
      styleClass="textBox" style="width:16%" />
    <h:inputText value="#{addressMainBean.address.block}"
      styleClass="inputBox" style="width:8%" 
      maxlength="#{addressMainBean.propertySize.block}"/>
    <h:outputText value="#{kernelBundle.address_entrance_hall}:"
      styleClass="textBox" style="width:8%" />
    <h:inputText value="#{addressMainBean.address.entranceHall}"
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.entranceHall}"/>
    <h:outputText value="#{kernelBundle.address_stair}:"
      styleClass="textBox" style="width:10%"/>
    <h:inputText value="#{addressMainBean.address.stair}"
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.stair}"/>
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.address_floor}:"
      styleClass="textBox" style="width:16%" />
    <h:inputText value="#{addressMainBean.address.floor}"
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.floor}"/>
    <h:outputText value="#{kernelBundle.address_door}:" 
      styleClass="textBox" style="width:8%" />
    <h:inputText value="#{addressMainBean.address.door}"
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.door}"/>
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.address_postal_code}:" 
      styleClass="textBox" style="width:16%" />
    <h:inputText value="#{addressMainBean.address.postalCode}"
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.postalCode}"/>
    <h:outputText value="#{kernelBundle.address_post_office_box}:" 
      styleClass="textBox" style="width:19%" />
    <h:inputText value="#{addressMainBean.address.postOfficeBox}"
      styleClass="inputBox" style="width:8%"
      maxlength="#{addressMainBean.propertySize.postOfficeBox}"/>
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.address_gis_reference}:"
      styleClass="textBox" style="width:16%" />
    <h:inputText value="#{addressMainBean.address.gisReference}"
      styleClass="inputBox" style="width:16%;font-family:courier"
      maxlength="#{addressMainBean.propertySize.gisReference}" />
  </t:div>

  <t:div>
    <h:outputText value="#{kernelBundle.address_comments}:"
      styleClass="textBox"  style="width:16%" />
    <h:inputText value="#{addressMainBean.address.comments}"
      styleClass="inputBox" style="width:80%"
      maxlength="#{addressMainBean.propertySize.comments}" />
  </t:div>

</jsp:root>
