<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.dic.web.resources.DictionaryBundle"
    var="dictionaryBundle" />

  <t:div>
    <h:outputText value="#{dictionaryBundle.enumType_enumTypeId}:"
                  styleClass="textBox" style="width:20%" />
    <h:inputText value="#{enumTypeMainBean.enumType.enumTypeId}"
                 styleClass="inputBox" style="width:70%;font-family:Courier new"
                 readonly="#{!enumTypeBean.new}" />
  </t:div>
  <t:div>
    <h:outputText value="#{dictionaryBundle.enumTypeMain_superEnumTypeId}:"
                  styleClass="textBox" style="width:20%" />
    <h:inputText value="#{enumTypeMainBean.enumType.superEnumTypeId}"
                 styleClass="inputBox" style="width:70%;font-family:Courier new" />
  </t:div>
  <t:div>
    <h:outputText value="#{dictionaryBundle.enumType_name}:"
                  styleClass="textBox" style="width:20%" />
    <h:inputText value="#{enumTypeMainBean.enumType.name}"
                 styleClass="inputBox" style="width:70%" />
  </t:div>
  <t:div>
    <h:outputText value="#{dictionaryBundle.enumTypeMain_itemType}:"
                  styleClass="textBox" style="width:20%" />
    <t:selectOneMenu value="#{enumTypeMainBean.itemTypeInput}"
                     styleClass="selectBox">
      <f:selectItem itemLabel="TEXT" itemValue="T" />
      <f:selectItem itemLabel="NUMERIC" itemValue="N" />
      <f:selectItem itemLabel="DATE" itemValue="D" />
      <f:selectItem itemLabel="BOOLEAN" itemValue="B" />
    </t:selectOneMenu>
  </t:div>
  <t:div>
    <h:outputText value="#{dictionaryBundle.enumTypeMain_sorted}:"
                  styleClass="textBox" style="width:20%" />
    <h:selectBooleanCheckbox value="#{enumTypeMainBean.enumType.sorted}" />
  </t:div>

  <t:div>
    <h:outputText value="#{dictionaryBundle.enumTypeMain_creation}:" 
                  styleClass="textBox" style="width:20%" />
    <h:outputText value="#{enumTypeMainBean.creationDateTime}"
      styleClass="outputBox" style="width:120px" >
        <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
    <h:outputText value="#{dictionaryBundle.enumTypeMain_by}" styleClass="textBox" />
    <h:outputText value="#{enumTypeMainBean.enumType.creationUserId}"
      styleClass="outputBox" style="width:100px" />
  </t:div>

  <t:div>
    <h:outputText value="#{dictionaryBundle.enumTypeMain_lastChange}:" 
                  styleClass="textBox" style="width:20%" />
    <h:outputText value="#{enumTypeMainBean.changeDateTime}"
      styleClass="outputBox" style="width:120px" >
        <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
    <h:outputText value="#{dictionaryBundle.enumTypeMain_by}" styleClass="textBox" />
    <h:outputText value="#{enumTypeMainBean.enumType.changeUserId}"
      styleClass="outputBox" style="width:100px" />
  </t:div>

</jsp:root>
