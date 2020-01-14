<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.policy.web.resources.PolicyBundle"
    var="policyBundle" />

  <t:stylesheet path="/plugins/codemirror/codemirror.css" />
  
  <t:div>
    <h:outputText value="#{policyBundle.policyMain_description}:" styleClass="textBox"
      style="width:100%;font-weight:bold;display:block;background:#E8E8E8;padding:1px 0px 1px 1px;" />
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_title}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{policyMainBean.policy.title}" styleClass="inputBox"
      style="width:78%"
      maxlength="#{policyMainBean.propertySize.title}"/>
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_type}:"
      style="width:18%" styleClass="textBox"/>
    <sf:commandMenu value="#{policyMainBean.currentTypeId}"
      styleClass="selectBox">
      <f:selectItems value="#{policyMainBean.selectedTypeItems}" />
    </sf:commandMenu>
    <h:commandButton value="#{objectBundle.search}"
      image="#{userSessionBean.icons.search}"
      alt="#{objectBundle.search}" title="#{objectBundle.search}"
      styleClass="searchButton"
      action="#{policyMainBean.searchType}"/>
    <h:commandButton action="#{policyMainBean.showType}"
      value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"      
      rendered="#{policyMainBean.renderShowTypeButton}" />
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_description}:" styleClass="textBox"
      style="width:18%;vertical-align:top" />
    <h:inputTextarea value="#{policyMainBean.policy.description}"
      styleClass="inputBox" style="width:78%" rows="3"
      onkeypress="checkMaxLength(this, #{policyMainBean.propertySize.description})"/>
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_mandate}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{policyMainBean.policy.mandate}"
      styleClass="inputBox" style="width:60%"
      maxlength="#{policyMainBean.propertySize.mandate}"      />
    <h:outputText value="#{policyBundle.policyMain_evaluationCode}:" styleClass="textBox" />
    <h:inputText value="#{policyMainBean.policy.evaluationCode}"
      styleClass="inputBox" style="width:10%"
      maxlength="#{policyMainBean.propertySize.evaluationCode}"/>
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_creation}:" styleClass="textBox"
      style="width:18%" />
    <h:outputText value="#{policyMainBean.creationDateTime}"
      styleClass="outputBox" style="width:120px" >
        <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
    <h:outputText value="#{policyBundle.by}" styleClass="textBox" />
    <h:outputText value="#{policyMainBean.policy.creationUserId}"
      styleClass="outputBox" style="width:100px" />
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_lastModify}:" styleClass="textBox"
      style="width:18%" />
    <h:outputText value="#{policyMainBean.changeDateTime}"
      styleClass="outputBox" style="width:120px" >
        <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
    <h:outputText value="#{policyBundle.by}" styleClass="textBox" />
    <h:outputText value="#{policyMainBean.policy.changeUserId}"
      styleClass="outputBox" style="width:100px" />
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_activation}:" styleClass="textBox"
      style="width:100%;font-weight:bold;display:block;background:#E0E0E0;margin-top:10px;padding:1px 0px 1px 1px;" />
  </t:div>
      
  <t:div>
    <h:outputText value="#{policyBundle.policyMain_date}:" styleClass="textBox"
      style="vertical-align:top" />
    <h:outputLink value="/common/policy/help.jsp" target="_blank"
      accesskey="h" style="margin-left:4px">
      <h:graphicImage value="/images/help.png"
        style="border:none" alt="help" />
    </h:outputLink>
    <t:inputTextarea id="activationDateExpression" forceId="true"
        value="#{policyMainBean.policy.activationDateExpression}"
        styleClass="inputBox cmFixed" rows="3"
        onkeypress="checkMaxLength(this, #{policyMainBean.propertySize.activationDateExpression})"/>
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_condition}:" styleClass="textBox"
      style="vertical-align:top" />
    <h:outputLink value="/common/policy/help.jsp" target="_blank"
      accesskey="h" style="margin-left:4px">
      <h:graphicImage value="/images/help.png"
        style="border:none" alt="help" />
    </h:outputLink>
    <t:inputTextarea id="activationCondition" forceId="true"
      value="#{policyMainBean.policy.activationCondition}"
      styleClass="inputBox cmFixed" rows="3"
      onkeypress="checkMaxLength(this, #{policyMainBean.propertySize.activationCondition})"/>
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_execution}:" styleClass="textBox"
      style="width:100%;font-weight:bold;display:block;background:#E0E0E0;margin-top:10px;padding:1px 0px 1px 1px;" />
  </t:div>

  <t:div>
    <h:outputText value="#{policyBundle.policyMain_automatic}:"
      styleClass="textBox" style="width:18%" />
    <h:selectBooleanCheckbox value="#{policyMainBean.policy.automaticExecution}"
      style="vertical-align:middle;" />
  </t:div>

  <t:div rendered="#{not policyMainBean.typeUndefined and
    policyMainBean.selector != null}">
    <h:outputText value="#{policyBundle.policyMain_form}:"
      styleClass="textBox" style="width:18%" />
    <sf:commandMenu value="#{policyMainBean.selector}"
      styleClass="selectBox" style="width:72%">
      <f:selectItems value="#{policyMainBean.formSelectItems}" />
    </sf:commandMenu>
    <h:commandButton value="#{objectBundle.update}"
      image="#{userSessionBean.icons.update}" immediate="true"
      alt="#{objectBundle.update}" title="#{objectBundle.update}"
      styleClass="showButton"
      rendered="#{not policyMainBean.propertyEditorVisible}"
      action="#{policyMainBean.updateForm}" />
  </t:div>

  <t:div rendered="#{not policyMainBean.typeUndefined and
    policyMainBean.selector != null}">
    <sf:dynamicForm form="#{policyMainBean.form}"
      value="#{policyMainBean.data}"
      rendererTypes="HtmlFormRenderer,GenericFormRenderer"
      rendered="#{not policyMainBean.propertyEditorVisible}" />
    <h:inputTextarea value="#{policyMainBean.propertyEditorString}"
      rendered="#{policyMainBean.propertyEditorVisible}"
      validator="#{policyMainBean.validatePropertyEditorString}"
      style="width:98%;height:100px; font-family:Courier New"
      styleClass="inputBox" />
  </t:div>

  <h:outputText value="#{policyMainBean.scripts}" escape="false" />          
      
</jsp:root>
