<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.dic.web.resources.DictionaryBundle"
    var="dictionaryBundle" />

  <t:div rendered="#{!typeBean.new}">
    <t:dataList value="#{typeMainBean.superTypes}" var="type">      
      <h:panelGroup style="display:block;padding:0">
        <h:outputText styleClass="textBox" style="width:18%" />
        <h:commandLink action="#{typeMainBean.showTypeFromPath}"
          style="text-decoration:none;color:black;text-decoration:none}">
          <h:outputText value="#{type.typeId}"
            style="font-family:Courier new;font-weight:bold" />
          <h:outputText value=" : #{type.description}" />
        </h:commandLink>
      </h:panelGroup>
      <h:panelGroup style="padding:0">
        <h:outputText styleClass="textBox" style="width:19%" />
        <h:graphicImage url="/common/dic/images/up.gif" style="border:none;" />
      </h:panelGroup>
    </t:dataList>
  </t:div>

  <t:div>
    <h:outputText value="#{dictionaryBundle.type_typeId}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{typeMainBean.type.typeId}" styleClass="inputBox" required="true"
      style="font-family:Courier New;width:50%" readonly="#{!typeBean.new}" />
  </t:div>

  <t:div>
    <h:outputText value="#{dictionaryBundle.type_superTypeId}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{typeMainBean.type.superTypeId}" styleClass="inputBox"
      style="font-family:Courier New;width:50%" />
  </t:div>

  <t:div>
    <h:outputText value="#{dictionaryBundle.type_description}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{typeMainBean.type.description}" styleClass="inputBox"
      style="width:78%" />
  </t:div>
  
  <t:div>
    <h:outputText value="#{dictionaryBundle.type_detail}:" styleClass="textBox"
      style="width:18%;vertical-align:top" />
    <h:inputTextarea value="#{typeMainBean.type.detail}" styleClass="inputBox"
      style="width:78%" rows="6" onkeyup="checkMaxLength(this, 1500)" />
  </t:div>
  
  <t:div>
    <h:outputText value="#{dictionaryBundle.typeMain_instantiable}:" styleClass="textBox"
      style="width:18%" />
    <h:selectBooleanCheckbox value="#{typeMainBean.type.instantiable}" />
  </t:div>

  <t:div>
    <h:outputText value="#{dictionaryBundle.typeMain_restricted}:" styleClass="textBox"
      style="width:18%" />
    <h:selectBooleanCheckbox value="#{typeMainBean.type.restricted}" />
  </t:div>

  <t:div>
    <h:outputText value="#{dictionaryBundle.typeMain_creation}:" styleClass="textBox"
      style="width:18%" />
    <h:outputText value="#{typeMainBean.creationDateTime}"
      styleClass="outputBox" style="width:120px" >
        <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
    <h:outputText value="#{dictionaryBundle.typeMain_by}" styleClass="textBox" />
    <h:outputText value="#{typeMainBean.type.creationUserId}"
      styleClass="outputBox" style="width:100px" />
  </t:div>

  <t:div>
    <h:outputText value="#{dictionaryBundle.typeMain_lastChange}:" styleClass="textBox"
      style="width:18%" />
    <h:outputText value="#{typeMainBean.changeDateTime}"
      styleClass="outputBox" style="width:120px" >
        <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
    <h:outputText value="#{dictionaryBundle.typeMain_by}" styleClass="textBox" />
    <h:outputText value="#{typeMainBean.type.changeUserId}"
      styleClass="outputBox" style="width:100px" />
  </t:div>

</jsp:root>
