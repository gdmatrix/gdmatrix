<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
                var="objectBundle"/>
  <f:loadBundle basename="org.santfeliu.translation.web.resources.TranslationBundle"
                var="translationBundle"/>
  
  <t:div styleClass="objectForm">
    <t:div styleClass="header">
      <h:outputText
        value="#{translationBundle.editor_title} " />
    </t:div>

    <t:div styleClass="tabs" style="height:10px">
    </t:div>
      
    <t:div styleClass="sheet">
      <t:div>
        <h:outputText value="#{translationBundle.text}:"
          styleClass="textBox" />
      </t:div>
  
      <t:div>
        <h:outputText value="#{translationBean.translation.text}" 
           styleClass="inputBox"
           style="font-family:courier new,courier;width:99%;display:-moz-inline-block;display:inline-block" />
      </t:div> 
  
      <t:div rendered="#{translationBean.creationDateTime != null}">
        <h:outputText value="#{translationBundle.editor_creation_date}:" style="width:20%" styleClass="textBox" />
        <h:outputText value="#{translationBean.creationDateTime}" styleClass="textBox">
          <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
        </h:outputText>
      </t:div>

      <t:div>
        <h:outputText value="#{translationBundle.editor_last_read}:" style="width:20%" styleClass="textBox" />
        <h:outputText value="#{translationBean.readDateTime}" styleClass="textBox">
           <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
        </h:outputText>
      </t:div>

      <t:div rendered="#{translationBean.modifyDateTime != null}">
        <h:outputText value="#{translationBundle.editor_last_mod}:" style="width:20%" styleClass="textBox" />
        <h:outputText value="#{translationBean.modifyDateTime}" styleClass="textBox">
          <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
        </h:outputText>
      </t:div>

      <t:div rendered="#{translationBean.translation.modifyUserId != null}">
        <h:outputText value="#{translationBundle.editor_translated_by}:" style="width:20%" styleClass="textBox" />
        <h:outputText value="#{translationBean.translation.modifyUserId}" styleClass="textBox" />
      </t:div>
  
      <t:div>
        <h:outputText value="#{translationBundle.editor_translate_to}:"
           styleClass="textBox" style="width:20%;vertical-align:middle" />
        <h:outputText value="#{translationBean.displayLanguage}"
           styleClass="textBox" style="vertical-align:middle;" />
        <h:graphicImage value="/common/translation/images/flags/#{translationBean.translation.language}.gif"
          alt="" style="margin-left:3px;vertical-align:middle;" />
      </t:div>
  
      <t:div>
        <h:inputTextarea value="#{translationBean.translation.translation}"
                         rows="10" 
                         style="width:99%;font-family:courier new, courier"
                         styleClass="inputBox" />
      </t:div>
 
      <t:div>
        <h:selectBooleanCheckbox value="#{translationBean.translationClosed}"
          style="vertical-align:middle;"/>
        <h:outputText styleClass="textBox" 
          value="#{translationBundle.editor_closed}" />
      </t:div>
    </t:div>
    
    <t:div styleClass="footer">
      <h:commandButton value="#{translationBundle.close}"
                       rendered="#{translationBean.filter.state == 'DRAFT'}"
                       styleClass="storeButton"
                       action="#{translationBean.closeTranslation}"/>
      <h:commandButton value="#{objectBundle.store}"
                       styleClass="storeButton"
                       action="#{translationBean.storeTranslation}"/>
      <h:commandButton value="#{objectBundle.remove}"
                       styleClass="removeButton" immediate="true"
                       action="#{translationBean.removeTranslation}"/>
      <h:commandButton value="#{objectBundle.cancel}"
                       styleClass="cancelButton" immediate="true"
                       action="#{translationBean.cancel}"/>
    </t:div>
  </t:div>
  
  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    errorClass="errorMessage" warnClass="warnMessage" 
    infoClass="infoMessage"/>

</jsp:root>











