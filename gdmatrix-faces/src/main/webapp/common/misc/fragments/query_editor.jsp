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
 
  <t:stylesheet path="/plugins/codemirror/codemirror.css" />
  
  <sf:saveScroll resetIfError="true" value="#{queryEditorBean.scroll}"  />
  
  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    globalOnly="true"
    layout="table"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />
    
  <t:div styleClass="button_bar">
    <h:commandButton value="#{queryBundle.list}" action="#{querySearchBean.show}" styleClass="big_button" />
    <h:commandButton value="#{objectBundle.show}" action="query_instance" styleClass="big_button" />
    <h:commandButton value="#{queryBundle.reload}" action="#{queryEditorBean.reloadQuery}" styleClass="big_button" 
                     rendered="#{queryBean.persistent}" immediate="true" />
    <h:commandButton value="#{queryBundle.copy}" action="#{queryEditorBean.copyQuery}" styleClass="big_button" 
                     rendered="#{queryBean.persistent}" immediate="true" />
    <h:commandButton value="#{objectBundle.store}" action="#{queryEditorBean.saveQuery}" styleClass="big_button" rendered="#{queryBean.saveEnabled}" />
    <h:commandButton value="#{objectBundle.delete}" action="#{queryEditorBean.removeQuery}" styleClass="big_button" rendered="#{queryBean.persistent}" 
                     onclick="return confirm('#{objectBundle.confirm_full_remove}')" />
    <t:popup closePopupOnExitingPopup="true">
      <t:outputText value="#{queryBundle.variables}" styleClass="big_button" />
      <f:facet name="popup">
        <t:div styleClass="vars_popup">
          <t:outputText value="_FILTER" styleClass="var" />
          <t:outputText value="_OUTPUT" styleClass="var" />
          <t:outputText value="_OUTPUT_NAMES" styleClass="var" />
          <t:outputText value="_OUTPUT_LABELS" styleClass="var" />
          <t:outputText value="_USERID" styleClass="var" />
          <t:outputText value="_DATE" styleClass="var" />    
          <t:outputText value="_DATETIME" styleClass="var" />      
        </t:div>
      </f:facet>
    </t:popup>
  </t:div>

  <t:div>
    <t:outputLabel for="query_name" value="#{queryBundle.name}:" styleClass="output_label top" />
    <t:inputText id="query_name" forceId="true" readonly="#{queryBean.persistent}"
      value="#{queryBean.query.name}" styleClass="input_text" />
    <t:outputLabel for="query_base" value="#{queryBundle.base}:" styleClass="output_label" />
    <t:inputText id="query_base" forceId="true"
      value="#{queryBean.query.base}" styleClass="input_text" />
  </t:div>

  <t:div>
    <t:outputLabel for="query_title" value="#{queryBundle.title}:" styleClass="output_label top" />
    <t:inputText id="query_title" forceId="true" 
      value="#{queryBean.query.title}" styleClass="input_text" />
  </t:div>
  
  <t:outputLabel for="query_description" value="#{queryBundle.description}:" />
  <t:div>
    <t:inputTextarea id="query_description" forceId="true" 
      value="#{queryBean.query.description}" styleClass="input_text" />
  </t:div>
  
  <t:outputLabel for="query_label" value="#{queryBundle.label}:" />
  <t:div>
    <t:inputTextarea id="query_label" forceId="true"
      value="#{queryBean.query.label}" styleClass="input_text" />
  </t:div>
  
  <t:outputLabel for="query_sql" value="#{queryBundle.sql_statement}:" />
  <t:div>
    <t:inputTextarea id="query_sql" forceId="true"
      value="#{queryBean.query.sql}" />
  </t:div>
  
  <t:panelTabbedPane styleClass="tabbed_panel">
    <t:panelTab label="#{queryBundle.classification}">
      <t:div id="classification_panel" forceId="true">
        <t:div>
          <t:outputLabel value="#{queryBundle.query_scope}:" styleClass="output_label" for="query_scope" />
          <t:selectOneMenu id="query_scope" value="#{queryBean.queryScope}"
            styleClass="select_box" forceId="true">
            <f:selectItem itemValue="" itemLabel=" " />
            <f:selectItems value="#{queryEditorBean.queryScopeSelectItems}" />            
          </t:selectOneMenu>
        </t:div>
        <t:div>
          <t:outputLabel value="#{queryBundle.query_object}:" styleClass="output_label" for="query_object" />
          <t:selectOneMenu id="query_object" value="#{queryBean.queryObject}"
            styleClass="select_box" forceId="true">
            <f:selectItem itemValue="" itemLabel=" " />
            <f:selectItems value="#{queryEditorBean.queryObjectSelectItems}" />            
          </t:selectOneMenu>
        </t:div>
        <t:div>
          <t:outputLabel value="#{queryBundle.query_type}:" styleClass="output_label" for="query_type" />
          <t:selectOneMenu id="query_type" value="#{queryBean.queryType}"
            styleClass="select_box" forceId="true">
            <f:selectItem itemValue="" itemLabel=" " />
            <f:selectItems value="#{queryEditorBean.queryTypeSelectItems}" />            
          </t:selectOneMenu>
        </t:div>
        <t:div>
          <t:outputLabel value="#{queryBundle.query_code}:" styleClass="output_label" for="query_code" />
          <t:inputText id="query_code" value="#{queryBean.queryCode}"
            styleClass="input_text" forceId="true" />
        </t:div>
        <t:div>
          <t:commandButton value="#{queryBundle.set_query_name}" action="#{queryBean.setQueryName}"
            styleClass="big_button" forceId="true" />
        </t:div>
      </t:div>
    </t:panelTab>

    <t:panelTab label="#{queryBundle.connection}">
      <t:div id="connection_panel" forceId="true">
        <t:div>
          <t:outputLabel value="#{queryBundle.driver}:" styleClass="output_label" for="conn_driver" />
          <t:inputText id="conn_driver" value="#{queryBean.query.connection.driver}" 
            styleClass="input_text" forceId="true" />
        </t:div>
        <t:div>
          <t:outputLabel value="#{queryBundle.url}:" styleClass="output_label" for="conn_url" />
          <t:inputText id="conn_url" value="#{queryBean.query.connection.url}" 
            styleClass="input_text" forceId="true" />
        </t:div>
        <t:div>
          <t:outputLabel value="#{queryBundle.username}:" styleClass="output_label" for="conn_username" />
          <t:inputText id="conn_username" value="#{queryBean.query.connection.username}" 
            styleClass="input_text" forceId="true" />
        </t:div>
        <t:div>
          <t:outputLabel value="#{queryBundle.password}:" styleClass="output_label" for="conn_password" />
          <t:inputSecret id="conn_password" value="#{queryBean.query.connection.password}" 
            styleClass="input_text" forceId="true" redisplay="true" />
        </t:div>
      </t:div>
    </t:panelTab>

    <t:panelTab label="#{queryBundle.parameters}" onclick="refreshEditor(parameterEditor);">
      <t:div styleClass="edit_panel">
        <t:div styleClass="left">
          <t:dataList
            value="#{queryBean.query.parameters}" var="parameter">
              <t:commandLink value="#{parameter.name} (#{parameter.description})" 
                 action="#{queryEditorBean.editParameter}" styleClass="list_entry #{parameter == queryEditorBean.editingParameter ? 'selected' : ''}" />
          </t:dataList>
        </t:div>
        <t:div styleClass="right">
          <t:div>
            <t:outputLabel value="#{queryBundle.name}:" styleClass="output_label" for="parameter_name" />
            <t:inputText id="parameter_name" value="#{queryEditorBean.editingParameter.name}" 
              styleClass="input_text" readonly="#{queryEditorBean.editingParameter == null}" forceId="true" />
          </t:div>
          <t:div>
            <t:outputLabel value="#{queryBundle.description}:" styleClass="output_label" for="parameter_description" />
            <t:inputText id="parameter_description" value="#{queryEditorBean.editingParameter.description}" 
              styleClass="input_text" readonly="#{queryEditorBean.editingParameter == null}" forceId="true" />
          </t:div>
          <t:div>
            <t:outputLabel value="#{queryBundle.format}:" styleClass="output_label" for="parameter_format" />
            <t:selectOneMenu id="parameter_format" value="#{queryEditorBean.editingParameter.format}" 
              styleClass="select_box" rendered="#{queryEditorBean.editingParameter != null}" forceId="true">
              <f:selectItems value="#{queryEditorBean.formatSelectItems}" />
            </t:selectOneMenu>
          </t:div>
          <t:div>
            <t:outputLabel value="#{queryBundle.size}:" styleClass="output_label" for="parameter_size" />
            <t:inputText id="parameter_size" value="#{queryEditorBean.editingParameter.size}" 
              styleClass="input_text" readonly="#{queryEditorBean.editingParameter == null}" forceId="true" />
          </t:div>
          <t:div>
            <t:outputLabel value="#{queryBundle.default_value}:" styleClass="output_label" for="parameter_value" />
            <t:inputText id="parameter_value" value="#{queryEditorBean.editingParameter.defaultValue}" 
              styleClass="input_text" readonly="#{queryEditorBean.editingParameter == null}" forceId="true" />
          </t:div>
          <t:div>
            <t:outputLabel value="#{queryBundle.sql_statement}:" styleClass="output_label" for="parameter_sql" />
          </t:div>
          <t:inputTextarea id="parameter_sql" value="#{queryEditorBean.editingParameter.sql}" 
            styleClass="input_sql" forceId="true" readonly="#{queryEditorBean.editingParameter == null}"/>
        </t:div>
      </t:div>
      <t:commandButton image="/common/misc/images/add.png" 
        action="#{queryEditorBean.addParameter}" styleClass="small_button" alt="#{objectBundle.add}" title="#{objectBundle.add}"  />
      <t:commandButton image="/common/misc/images/remove.png" 
        action="#{queryEditorBean.removeParameter}" styleClass="small_button" alt="#{objectBundle.remove}" title="#{objectBundle.remove}" />
      <t:commandButton image="/common/misc/images/sort.png" 
        action="#{queryBean.query.sortParameters}" styleClass="small_button" alt="#{queryBundle.sort}" title="#{queryBundle.sort}" />
    </t:panelTab>
    
    <t:panelTab label="#{queryBundle.predicates}" onclick="refreshEditor(predicateEditor);">
      <t:div styleClass="edit_panel">
        <t:div styleClass="left">
          <t:dataList
            value="#{queryBean.query.predicates}" var="predicate">
              <t:commandLink value="#{predicate.name}" 
                 action="#{queryEditorBean.editPredicate}" styleClass="list_entry #{predicate == queryEditorBean.editingPredicate ? 'selected' : ''}" />
          </t:dataList>
        </t:div>
        <t:div styleClass="right">
          <t:div>
            <t:outputLabel value="#{queryBundle.name}:" styleClass="output_label" for="predicate_name" />
            <t:inputText id="predicate_name" value="#{queryEditorBean.editingPredicate.name}" 
              styleClass="input_text" readonly="#{queryEditorBean.editingPredicate == null}" forceId="true" />
          </t:div>
          <t:div>
            <t:outputLabel value="#{queryBundle.label}:" styleClass="output_label" for="predicate_label" />
          </t:div>
          <t:inputTextarea id="predicate_label" value="#{queryEditorBean.editingPredicate.label}" 
            styleClass="input_text" readonly="#{queryEditorBean.editingPredicate == null}" forceId="true" />
          <t:div>
            <t:outputLabel value="#{queryBundle.sql_statement}:" styleClass="output_label" for="predicate_sql" />
          </t:div>
          <t:inputTextarea id="predicate_sql" value="#{queryEditorBean.editingPredicate.sql}"
            styleClass="input_sql" forceId="true" readonly="#{queryEditorBean.editingPredicate == null}" />
        </t:div>
      </t:div>
      <t:commandButton image="/common/misc/images/add.png" 
        action="#{queryEditorBean.addPredicate}" styleClass="small_button" alt="#{objectBundle.add}" title="#{objectBundle.add}" />
      <t:commandButton image="/common/misc/images/remove.png" 
        action="#{queryEditorBean.removePredicate}" styleClass="small_button" alt="#{objectBundle.remove}" title="#{objectBundle.remove}" />
      <t:commandButton image="/common/misc/images/up.png" 
        action="#{queryEditorBean.movePredicateUp}" styleClass="small_button" alt="#{queryBundle.move_up}" title="#{queryBundle.move_up}" />
      <t:commandButton image="/common/misc/images/down.png" 
        action="#{queryEditorBean.movePredicateDown}" styleClass="small_button" alt="#{queryBundle.move_down}" title="#{queryBundle.move_down}" />
      <t:commandButton image="/common/misc/images/sort.png" 
        action="#{queryBean.query.sortPredicates}" styleClass="small_button" alt="#{queryBundle.sort}" title="#{queryBundle.sort}" />
    </t:panelTab>
    
    <t:panelTab label="#{queryBundle.outputs}" onclick="refreshEditor(outputEditor);">
      <t:div styleClass="edit_panel">
        <t:div styleClass="left">
          <t:dataList 
            value="#{queryBean.query.outputs}" var="output">
              <t:commandLink value="#{output.name}" 
                 action="#{queryEditorBean.editOutput}" styleClass="list_entry #{output == queryEditorBean.editingOutput ? 'selected' : ''}" />
          </t:dataList>
        </t:div>
        <t:div styleClass="right">
          <t:div>
            <t:outputLabel value="#{queryBundle.name}:" styleClass="output_label" for="output_name" />
            <t:inputText id="output_name" value="#{queryEditorBean.editingOutput.name}" 
              styleClass="input_text" readonly="#{queryEditorBean.editingOutput == null}" forceId="true" />
          </t:div>
          <t:div>
            <t:outputLabel value="#{queryBundle.label}:" styleClass="output_label" for="output_label" />
          </t:div>
          <t:inputTextarea id="output_label" value="#{queryEditorBean.editingOutput.label}" 
            styleClass="input_text" readonly="#{queryEditorBean.editingOutput == null}" forceId="true" />
          <t:div>
            <t:outputLabel value="#{queryBundle.sql_statement}:" styleClass="output_label" for="output_sql" />
          </t:div>
          <t:inputTextarea id="output_sql" value="#{queryEditorBean.editingOutput.sql}"
            styleClass="input_sql" forceId="true" readonly="#{queryEditorBean.editingOutput == null}" />
        </t:div>
      </t:div>
      <t:commandButton image="/common/misc/images/add.png" 
        action="#{queryEditorBean.addOutput}" styleClass="small_button" alt="#{objectBundle.add}" title="#{objectBundle.add}" />
      <t:commandButton image="/common/misc/images/remove.png" 
        action="#{queryEditorBean.removeOutput}" styleClass="small_button" alt="#{objectBundle.remove}" title="#{objectBundle.remove}" />
      <t:commandButton image="/common/misc/images/up.png" 
        action="#{queryEditorBean.moveOutputUp}" styleClass="small_button" alt="#{queryBundle.move_up}" title="#{queryBundle.move_up}" />
      <t:commandButton image="/common/misc/images/down.png" 
        action="#{queryEditorBean.moveOutputDown}" styleClass="small_button" alt="#{queryBundle.move_down}" title="#{queryBundle.move_down}" />
      <t:commandButton image="/common/misc/images/sort.png" 
        action="#{queryBean.query.sortOutputs}" styleClass="small_button" alt="#{queryBundle.sort}" title="#{queryBundle.sort}" />
    </t:panelTab>

    <t:panelTab label="#{queryBundle.roles}">
      <t:div>
        <t:outputLabel value="#{queryBundle.read_roles}:" styleClass="output_label" for="read_roles" />
      </t:div>
      <t:inputTextarea id="read_roles" value="#{queryBean.readRolesString}" rows="4"
        styleClass="input_text roles" />
      <t:div>
        <t:outputLabel value="#{queryBundle.write_roles}:" styleClass="output_label" for="write_roles" />
      </t:div>
      <t:inputTextarea id="write_roles" value="#{queryBean.writeRolesString}" rows="4" 
        styleClass="input_text roles" />
    </t:panelTab>

  </t:panelTabbedPane>

  <f:verbatim>
    <script type="text/javascript" src="/plugins/codemirror/codemirror.js">
    0;
    </script>
    <script type="text/javascript" src="/plugins/codemirror/sql.js">
    0;
    </script>
    <script type="text/javascript" src="/plugins/codemirror/runmode.js">
    0;
    </script>
    <script type="text/javascript" src="/plugins/codemirror/matchbrackets.js">
    0;
    </script>
    <script type="text/javascript">
      var elem = document.getElementById("query_sql");
      var bodyEditor = CodeMirror.fromTextArea(elem, 
        {mode: 'text/x-sql', lineNumbers: false, matchBrackets: true, lineWrapping: true});

      elem = document.getElementById("parameter_sql");
      var parameterEditor = CodeMirror.fromTextArea(elem, 
        {mode: 'text/x-sql', lineNumbers: false, matchBrackets: true, lineWrapping: true});
      parameterEditor.setSize(null, 78);

      elem = document.getElementById("predicate_sql");
      var predicateEditor = CodeMirror.fromTextArea(elem, 
        {mode: 'text/x-sql', lineNumbers: false, matchBrackets: true, lineWrapping: true});    
      predicateEditor.setSize(null, 90);

      elem = document.getElementById("output_sql");
      var outputEditor = CodeMirror.fromTextArea(elem, 
        {mode: 'text/x-sql', lineNumbers: false, matchBrackets: true, lineWrapping: true});
      outputEditor.setSize(null, 90);

      var elems = document.getElementsByClassName("selected");
      for (var i = 0; i &lt; elems.length; i++)
      {
        elem = elems[i];
        var topPos = elem.offsetTop;
        elem.parentNode.scrollTop = topPos;
      }
      
      function refreshEditor(editor)
      {
        setTimeout(function() { editor.refresh(); }, 0);
      }
    </script>
  </f:verbatim>

</jsp:root>
