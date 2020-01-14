<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:verbatim>
    <style type="text/css">
      .selector_panel {vertical-align: middle;}
      .selector_panel .text {vertical-align: middle;margin: 2px;}
      .selector_panel .find_forms {vertical-align: middle;margin: 2px; width:auto; overflow:visible;padding:0 2px 0 2px;}
      .selector_panel select {vertical-align: middle;margin: 2px;}
      .selector_panel .selector {vertical-align: middle;margin: 2px; width:auto; overflow:visible;width:250px;background:#F0E8E8;}
      .selector_panel .forms_menu {width:480px; background:#F0E8E8;}

      .form_panel {border:solid 1px #808080; margin-top: 20px; background:#E8E8E8;
         font-family: Verdana; font-size:11px;}

      .control_panel {text-align: center;}
      .control_panel .button {margin:0 2px 0 2px;vertical-align: middle;
        font-family: Verdana; font-size:11px; background: #D0D080; border:solid 1px red;
        width:auto; overflow:visible; padding:0 2px 0 2px;}

      .field_panel {background:#E0E0FF; padding:4px;border:1px solid red;
         font-family: Verdana; font-size:11px; margin-top:20px;}
      .field_panel .title {font-size:14px; font-family: "Arial";padding:4px;}
      .fields {background:#F4F4FF;border:solid 1px red;margin-top:6px;border-collapse:collapse;width:100%;}
      .fields .row1 {font-family:"Courier new";font-size:12px; background:white;}
      .fields .row2 {font-family:"Courier new";font-size:12px; background:#F0F0F0;}
      .fields .row1 td {border-left:solid 1px #A0A0A0;padding:2px;}
      .fields .row2 td {border-left:solid 1px #A0A0A0;padding:2px;}
      .fields .row1:hover {background: #EFEF40;}
      .fields .row2:hover {background: #EFEF40;}
      .fields .ref_col {width:20%;}
      .fields .label_col {width:35%;}
      .fields .type_col {width:20%;}
      .fields .ocurrs_col {width:10%;}
      .fields .read_only_col {width:15%;}
      .fields .read_only {color:gray;}

      .data_panel {background:#E0E060; padding:4px;border:1px solid red;
         font-family: Verdana; font-size:11px; margin-top:20px;}
      .data_panel .title {font-size:14px; font-family: "Arial";padding:4px;}
      .data_panel .button {margin:0 2px 0 14px;vertical-align: middle;
        font-family: Verdana; font-size:11px; background: #D0D080; border:solid 1px red;
        width:auto; overflow:visible; padding:0 2px 0 2px;}
      .data_panel .box {margin:2px;vertical-align: middle;font-size:11px;}
      
      .data {background:#F8F080;border:solid 1px red;margin-top:10px;border-collapse:collapse;width:100%;}
      .data .row1 {font-family:"Courier new";font-size:12px; background:white;}
      .data .row2 {font-family:"Courier new";font-size:12px; background:#F0F0F0;}
      .data .row1 td {border-left:solid 1px #A0A0A0;padding:2px;}
      .data .row2 td {border-left:solid 1px #A0A0A0;padding:2px;}
      .data .row1:hover {background: #EFEF40;}
      .data .row2:hover {background: #EFEF40;}
      .data .name_col {width:20%;}
      .data .class_col {width:30%;}
      .data .value_col {width:50%;}

      .factory_panel {border:1px solid gray; background:#D0F0D0;margin-top:20px;padding:4px;
         font-family: Verdana; font-size:11px;}
      .factory_panel .title {font-size:14px; font-family: "Arial";padding:4px;}
      .factory_panel .button {margin-left:10px;vertical-align: middle;
        font-family:Verdana; font-size:11px; background: #D0D080; border:solid 1px red;
        width:auto; overflow:visible; padding:0 2px 0 2px;}
      .form_list {list-style-type:none;margin: 0px;margin-top:8px;padding: 0px;}
      .form_entry {background:#F0F0F0;margin:6px;border:1px solid #404040;padding:4px;
         border-collapse: collapse;display:block;margin: 1px;padding: 0px;}
      .form_entry:hover {background:#EFEF40;}
      .form_entry .col1 {color:black;}
      .form_entry .col2 {font-family: "Courier New"; font-size: 12px;color:#404040;}
      .form_entry TR TD {border-bottom: 1px solid #A0A0A0; padding:2px;}
      .dump {display:block;width:600px;height:400px;border:solid 1px gray; background: #F0F0F0;
        font-family: "Courier New"; font-size: 12px; overflow:scroll;
        margin-left:auto; margin-right: auto; padding:0px;}
    </style>
  </f:verbatim>

  <t:saveState value="#{formTestBean}" />

  <t:div>
    <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
      errorClass="errorMessage_line" warnClass="warnMessage_line"
      infoClass="infoMessage_line"/>
  </t:div>

  <t:div styleClass="selector_panel">
    <t:div>
      <h:outputText value="Selector:" styleClass="text" />
      <h:inputText binding="#{formTestBean.selectorInputText}" styleClass="selector" immediate="true" />
      <h:commandButton value="Find forms" action="#{formTestBean.findForms}" styleClass="find_forms" immediate="true" />
    </t:div>
    <t:div rendered="#{formTestBean.formCount > 0}">
      <h:outputText value="#{formTestBean.formCount} forms found:" styleClass="text" />
      <sf:commandMenu value="#{formTestBean.selector}"
        styleClass="forms_menu">
        <f:selectItems value="#{formTestBean.formSelectItems}" />
      </sf:commandMenu>
    </t:div>
    <t:div rendered="#{formTestBean.formCount == 0}">
      <h:outputText value="No forms found." styleClass="text" />
    </t:div>
  </t:div>

  <t:div styleClass="form_panel">
    <sf:dynamicForm
      form="#{formTestBean.form}"
      rendererTypes="HtmlFormRenderer,GenericFormRenderer"
      value="#{formTestBean.data}" />
  </t:div>

  <t:div styleClass="control_panel">
    <h:commandButton value="Submit" action="#{formTestBean.submit}"
      styleClass="button" />
  </t:div>
 
  <t:div styleClass="field_panel">
    <h:outputText value="Form fields" styleClass="title" />
    <t:dataTable value="#{formTestBean.formFields}" var="field"
      rowClasses="row1,row2" styleClass="fields">
      <t:column styleClass="ref_col">
        <f:facet name="header">
          <h:outputText value="Reference" />
        </f:facet>
        <h:outputText value="#{field.reference}"
          styleClass="#{field.readOnly ? 'read_only' : null}" />
      </t:column>
      <t:column styleClass="label_col">
        <f:facet name="header">
          <h:outputText value="Label" />
        </f:facet>
        <h:outputText value="#{field.label}"
          styleClass="#{field.readOnly ? 'read_only' : null}" />
      </t:column>
      <t:column styleClass="type_col">
        <f:facet name="header">
          <h:outputText value="Type" />
        </f:facet>
        <h:outputText value="#{field.type}"
           styleClass="#{field.readOnly ? 'read_only' : null}"/>
      </t:column>
      <t:column styleClass="occurs_col">
        <f:facet name="header">
          <h:outputText value="Occurs" />
        </f:facet>
        <h:outputText value="#{field.minOccurs}..#{field.maxOccurs}"
          styleClass="#{field.readOnly ? 'read_only' : null}" />
      </t:column>
      <t:column styleClass="read_only_col">
        <f:facet name="header">
          <h:outputText value="Read only" />
        </f:facet>
        <h:outputText value="#{field.readOnly}" 
          styleClass="#{field.readOnly ? 'read_only' : null}" />
      </t:column>
    </t:dataTable>
  </t:div>
  
  <t:div styleClass="data_panel">
    <h:outputText value="Form data" styleClass="title" />
    <h:commandButton value="Clear data" action="#{formTestBean.clearData}"
      styleClass="button" />
    <h:outputText value="Field:" styleClass="box" />
    <h:inputText value="#{formTestBean.fieldName}" size="12"
      styleClass="box" />
    <h:outputText value="Value:" styleClass="box" />
    <h:inputText value="#{formTestBean.fieldValue}" size="12"
      styleClass="box" />
    <h:commandButton value="Set" styleClass="button"
      action="#{formTestBean.assignFieldValue}" />
    
    <t:dataTable value="#{formTestBean.dataEntries}" var="row"
      rowClasses="row1,row2" styleClass="data">
      <t:column styleClass="name_col">
        <f:facet name="header">
          <h:outputText value="Name" />
        </f:facet>
        <h:outputText value="#{row[0]}" />
      </t:column>
      <t:column styleClass="class_col">
        <f:facet name="header">
          <h:outputText value="Class" />
        </f:facet>
        <h:outputText value="#{row[2]}" />
      </t:column>
      <t:column styleClass="value_col">
        <f:facet name="header">
          <h:outputText value="Value" />
        </f:facet>
        <h:outputText value="#{row[1]}" />
      </t:column>
    </t:dataTable>
  </t:div>

  <t:div styleClass="factory_panel">
    <h:outputText value="Form factory" styleClass="title" />
    <h:commandButton value="Clear forms" action="#{formTestBean.clearForms}"
      styleClass="button" immediate="true" />
    
    <t:dataList value="#{formTestBean.formFactoryEntries}" var="row"
      layout="unorderedList" styleClass="form_list">
      <h:panelGrid styleClass="form_entry" columns="2"
         columnClasses="col1,col2">
        <h:outputText value="Form id:"/>
        <t:div>
          <h:outputText value="#{row.form.id}" />
          <h:commandButton action="#{formTestBean.dumpForm}" value="Dump"
            styleClass="button" immediate="true" />
        </t:div>

        <h:outputText value="Selector:" />
        <h:outputText value="#{row.selector}" />

        <h:outputText value="Class:"/>
        <h:outputText value="#{formTestBean.formEntryClass}" />

        <h:outputText value="Context:" />
        <h:outputText value="#{row.form.context}" />
      </h:panelGrid>
    </t:dataList>

    <t:div rendered="#{formTestBean.dumpFormId != null}" styleClass="dumpBar">
      <h:outputText value="#{formTestBean.dumpFormContent}"
        styleClass="dump" escape="false" />
    </t:div>
  </t:div>
  
</jsp:root>
