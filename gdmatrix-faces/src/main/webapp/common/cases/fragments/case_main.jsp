<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.cases.web.resources.CaseBundle" 
    var="caseBundle" />

  <t:div rendered="#{!caseBean.new}">
    <h:outputText value="#{objectBundle.object_id}:" styleClass="textBox"
      style="width:18%" />
    <h:outputText value="#{caseMainBean.case.caseId}" styleClass="outputBox"
      style="width:20%" />
    <h:outputLink rendered="#{caseMainBean.showInTreeEnabled}"
                  value="/go.faces?xmid=#{caseMainBean.caseTreeMid}"
                  onclick="return goMid('#{caseMainBean.caseTreeMid}')">
      <h:graphicImage url="/common/cases/images/tree.gif"
        alt="#{caseBundle.case_show_in_tree}"
        style="border:none;vertical-align:middle;margin-left:4px" />
    </h:outputLink>
  </t:div>
  
  <t:div>
    <h:outputText value="#{caseBundle.case_classId}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{caseMainBean.classIdString}"
      style="width:20%;color:#{caseMainBean.validClassId ? 'black' : 'red'}"
      styleClass="inputBox" valueChangeListener="#{caseMainBean.valueChanged}"
      maxlength="#{caseMainBean.propertySize.classId}"
      readonly="#{not caseBean.editable}"/>
    <h:commandButton value="#{objectBundle.search}"
      image="#{userSessionBean.icons.search}"
      alt="#{objectBundle.search}" title="#{objectBundle.search}"
      styleClass="searchButton"
      action="#{caseMainBean.searchClass}"
      disabled="#{not caseBean.editable}"/>
    <h:outputText rendered="#{caseMainBean.classTitle != null}"
      value="#{caseMainBean.classTitle}" styleClass="outputBox"
      style="margin-left:5px;width:48%"/>
    <h:commandButton rendered="#{caseMainBean.classTitle != null}" 
      value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"
      action="#{caseMainBean.showClass}" />
  </t:div>  

  <t:div>
    <h:outputText value="#{caseBundle.case_type}:"
      style="width:18%" styleClass="textBox"/>
    <sf:commandMenu value="#{caseMainBean.currentTypeId}"
      styleClass="selectBox"
      disabled="#{not caseBean.editable}">
      <f:selectItems value="#{caseMainBean.selectedTypeItems}" />
    </sf:commandMenu>
    <h:commandButton value="#{objectBundle.search}"
      image="#{userSessionBean.icons.search}"
      alt="#{objectBundle.search}" title="#{objectBundle.search}"
      styleClass="searchButton"
      action="#{caseMainBean.searchType}" disabled="#{not caseBean.editable}"/>
    <h:commandButton action="#{caseMainBean.showType}"
      value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"      
      rendered="#{caseMainBean.renderShowTypeButton}"
      />
  </t:div>

  <t:div>
    <h:outputText value="#{caseBundle.case_title}:" styleClass="textBox"
      style="width:18%" />
    <h:inputText value="#{caseMainBean.case.title}" styleClass="inputBox"
      style="width:78%" valueChangeListener="#{caseMainBean.valueChanged}"
      maxlength="#{caseMainBean.propertySize.title}"
      readonly="#{not caseBean.editable}" />
  </t:div>

  <t:div>
    <h:outputText value="#{caseBundle.case_startDate}:" 
      style="width:18%"
      styleClass="textBox"/>

    <sf:calendar value="#{caseMainBean.startDateTime}"
      styleClass="calendarBox"
      externalFormat="dd/MM/yyyy|HH:mm:ss"
      internalFormat="yyyyMMddHHmmss"
      buttonStyleClass="calendarButton"
      style="width:14%;"
      disabled="#{not caseBean.editable}"/>

  </t:div>
  
  <t:div>
    <h:outputText value="#{caseBundle.case_endDate}:" 
      styleClass="textBox"
      style="width:18%"/>

    <sf:calendar value="#{caseMainBean.endDateTime}"
      styleClass="calendarBox"
      externalFormat="dd/MM/yyyy|HH:mm:ss"
      internalFormat="yyyyMMddHHmmss"
      buttonStyleClass="calendarButton"
      style="width:14%;"
      disabled="#{not caseBean.editable}"/>
  </t:div>

  <t:div rendered="#{caseMainBean.showAuditProperties and caseMainBean.creationDateTime != null}">
    <h:outputText value="#{caseBundle.case_creationDateTime}:"
      styleClass="textBox" style="width:18%" />
      <h:outputText value="#{caseMainBean.creationDateTime}"
        styleClass="outputBox" style="width:22%">
        <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
    <h:outputText value="#{caseBundle.case_createdBy}:"
      styleClass="textBox" style="width:5%"
      rendered="#{caseMainBean.case.creationUserId != null}"/>
    <h:outputText value="#{caseMainBean.case.creationUserId}"
      styleClass="outputBox" style="width:22%" />
  </t:div>

  <t:div rendered="#{caseMainBean.showAuditProperties and caseMainBean.changeDateTime != null}">
    <h:outputText value="#{caseBundle.case_changeDateTime}:"
      styleClass="textBox" style="width:18%" />
    <h:outputText value="#{caseMainBean.changeDateTime}"
    styleClass="outputBox" style="width:22%">
        <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
    </h:outputText>
    <h:outputText value="#{caseBundle.case_changedBy}:"
      styleClass="textBox" style="width:5%"
      rendered="#{caseMainBean.case.changeUserId != null}"/>
    <h:outputText value="#{caseMainBean.case.changeUserId}"
      styleClass="outputBox" style="width:22%" />
  </t:div>

  <t:div>
    <h:outputText value="#{caseBundle.case_description}:" styleClass="textBox"
      style="width:18%; vertical-align:top"/>
    <h:inputTextarea value="#{caseMainBean.case.description}" 
      style="width:79%" rows="4"
      styleClass="inputBox"
      valueChangeListener="#{caseMainBean.valueChanged}"
      onkeypress="checkMaxLength(this, #{caseMainBean.propertySize.description})"
      readonly="#{not caseBean.editable}"/>
  </t:div>
  
  <t:div rendered="#{caseMainBean.propertyEditorVisible || caseMainBean.renderFormSelector}">
    <h:outputText value="#{caseBundle.case_form}:"
      styleClass="textBox" style="width:18%" />
    <sf:commandMenu value="#{caseMainBean.selector}"
      styleClass="selectBox">
      <f:selectItems value="#{caseMainBean.formSelectItems}" />
    </sf:commandMenu>
    <h:commandButton value="#{objectBundle.update}"
      image="#{userSessionBean.icons.update}" immediate="true"
      alt="#{objectBundle.update}" title="#{objectBundle.update}"
      styleClass="showButton"
      rendered="#{not caseMainBean.propertyEditorVisible}"
      action="#{caseMainBean.updateForm}" />
  </t:div>

  <t:div rendered="#{caseMainBean.renderForm}" style="height:100%">
    <sf:dynamicForm
      form="#{caseMainBean.form}"
      rendererTypes="#{caseBean.editable ? 'HtmlFormRenderer,GenericFormRenderer' : 'DisabledHtmlFormRenderer'}"
      value="#{caseMainBean.data}" 
      rendered="#{not caseMainBean.propertyEditorVisible}" />
    <h:inputTextarea value="#{caseMainBean.propertyEditorString}"
      rendered="#{caseMainBean.propertyEditorVisible}"
      validator="#{caseMainBean.validatePropertyEditorString}"
      style="width:98%;height:100px; font-family:Courier New"
      styleClass="inputBox"
      readonly="#{not caseBean.editable}"/>
  </t:div>
  
</jsp:root>
