<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />  

  <f:loadBundle basename="org.santfeliu.misc.query.web.resources.QueryBundle" 
    var="queryBundle" />
 
  <sf:saveScroll resetIfError="true" value="#{queryInstanceBean.scroll}"  />
  
  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    globalOnly="false"
    layout="table"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />
   
  <t:div styleClass="button_bar">
    <h:commandButton value="#{queryBundle.list}" action="#{querySearchBean.show}" styleClass="big_button" />
    <h:commandButton value="#{objectBundle.edit}" action="query_editor" styleClass="big_button" rendered="#{queryBean.editionEnabled}"/>
    <h:commandButton value="#{queryBundle.reload}" action="#{queryInstanceBean.reloadQuery}" styleClass="big_button" 
                     rendered="#{queryBean.persistent}" immediate="true" />
    <h:commandButton value="#{objectBundle.store}" action="#{queryInstanceBean.saveQuery}" styleClass="big_button" rendered="#{queryBean.saveEnabled}" />
  </t:div>

  <t:div styleClass="query_header">
    <h:outputText value="#{queryInstanceBean.query.title}" styleClass="title" />
    <h:outputText value="#{queryInstanceBean.query.description}" styleClass="description" />
  </t:div>
  
  <t:div rendered="#{queryInstanceBean.selectedInstanceDescription == null}">
    <sf:commandMenu value="#{queryInstanceBean.selectedInstanceName}" 
      action="#{queryInstanceBean.selectInstance}" styleClass="instance_description">
      <f:selectItems value="#{queryInstanceBean.instanceSelectItems}" />
    </sf:commandMenu>
    <h:commandButton value="#{queryBundle.add_instance}" action="#{queryInstanceBean.addInstance}" styleClass="big_button" />
    <h:commandButton value="#{queryBundle.remove_instance}" action="#{queryInstanceBean.removeInstance}" styleClass="big_button" immediate="true" />
    <h:commandButton value="#{queryBundle.rename_instance}" action="#{queryInstanceBean.renameInstance}" styleClass="big_button" />
  </t:div>
  
  <t:div rendered="#{queryInstanceBean.selectedInstanceDescription != null}">
    <t:inputText value="#{queryInstanceBean.selectedInstanceDescription}" styleClass="instance_description" />
    <h:commandButton value="#{objectBundle.accept}" action="#{queryInstanceBean.acceptRename}" styleClass="big_button" />
    <h:commandButton value="#{objectBundle.cancel}" action="#{queryInstanceBean.cancelRename}" styleClass="big_button" />
  </t:div>
  
  <t:panelGroup binding="#{queryInstanceBean.expressionComponents}">
  </t:panelGroup>

  <t:div id="output_selector" forceId="true" 
    rendered="#{queryInstanceBean.query.outputCount != 0}">
    <t:outputLabel id="output_list_label" for="output_list" 
      value="#{queryBundle.available_fields}:" forceId="true" />
    <t:outputLabel id="output_selection_label" for="output_selection"
      value="#{queryBundle.selected_fields}:" forceId="true" />
    <t:div id="output_panel" forceId="true">
      <t:selectManyListbox id="output_list" forceId="true" 
        value="#{queryInstanceBean.outputsToAdd}">
        <f:selectItems value="#{queryInstanceBean.availableOutputSelectItems}" />
      </t:selectManyListbox>
      <t:commandButton value=""
        id="add_output" forceId="true" alt="add" 
        action="#{queryInstanceBean.addOutputs}" />
      <t:commandButton value=""
        id="remove_output" forceId="true" alt="remove" 
        action="#{queryInstanceBean.removeOutputs}" />
      <t:selectManyListbox id="output_selection" forceId="true" 
        value="#{queryInstanceBean.outputsToRemove}">
        <f:selectItems value="#{queryInstanceBean.selectedOutputSelectItems}" />
      </t:selectManyListbox>
    </t:div>
  </t:div>

  <t:commandButton value="#{queryBundle.execute_query}" action="#{queryInstanceBean.execute}" 
    styleClass="big_button" onclick="changeTarget();"  />
  
</jsp:root>
