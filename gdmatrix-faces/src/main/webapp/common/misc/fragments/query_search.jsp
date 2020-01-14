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
   
  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    globalOnly="true"
    layout="table"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />
  
  <t:div>
    <t:outputLabel value="#{queryBundle.query_scope}:" for="filter_scope" />
    <t:selectOneMenu value="#{querySearchBean.filterByScope}" styleClass="input_text">
      <f:selectItem itemLabel=" " itemValue="" />
      <f:selectItems value="#{querySearchBean.queryScopeSelectItems}" />                     
    </t:selectOneMenu>
  </t:div>  
  <t:div>
    <t:outputLabel value="#{queryBundle.query_object}:" for="filter_object" />
    <t:selectOneMenu value="#{querySearchBean.filterByObject}" styleClass="input_text">
      <f:selectItem itemLabel=" " itemValue="" />      
      <f:selectItems value="#{querySearchBean.queryObjectSelectItems}" />                     
    </t:selectOneMenu>
  </t:div>  
  <t:div>
    <t:outputLabel value="#{queryBundle.query_type}:" for="filter_type" />
    <t:selectOneMenu value="#{querySearchBean.filterByType}" styleClass="input_text">
      <f:selectItem itemLabel=" " itemValue="" />      
      <f:selectItems value="#{querySearchBean.queryTypeSelectItems}" />                     
    </t:selectOneMenu>
  </t:div>  
  <t:div>
    <t:outputLabel value="#{queryBundle.name}:" for="filter_name" />  
    <t:inputText id="filter_name" value="#{querySearchBean.filterByName}" styleClass="input_text" />

    <t:outputLabel value="#{queryBundle.title}:" for="filter_title" />  
    <t:inputText id="filter_title" value="#{querySearchBean.filterByTitle}" styleClass="input_text" />      
    
    <t:commandButton action="#{querySearchBean.search}" value="#{objectBundle.search}" styleClass="big_button" />                
  </t:div>

  <t:div>
    <t:dataList value="#{querySearchBean.documents}" var="document" layout="unorderedList" 
      styleClass="query_list">
      <t:div styleClass="left #{querySearchBean.queryUpdate ? 'update' : 'select'}">
        <t:outputText value="#{querySearchBean.queryName}" styleClass="query_name" />
        <t:outputText value="#{document.title}" styleClass="query_title" />
        <t:outputText value="#{querySearchBean.queryDescription}" styleClass="query_description" />
      </t:div>
      <t:div styleClass="right">
        <t:commandButton action="#{querySearchBean.showQuery}" value="#{objectBundle.show}" styleClass="big_button" />
      </t:div>
    </t:dataList>
  </t:div>
  <t:commandButton action="#{queryBean.createQuery}" value="#{objectBundle.create}" styleClass="big_button" />        
 
</jsp:root>
