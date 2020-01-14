<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.dic.web.resources.DictionaryBundle"
                var="dictionaryBundle"/>

  <sf:saveScroll />
  <h:panelGrid columns="2" styleClass="filterPanel" summary=""
    columnClasses="column1, column2"
    headerClass="header" footerClass="footer">
    <f:facet name="header">
      <h:outputText />
    </f:facet>

    <h:outputText value="#{dictionaryBundle.type_rootTypeId}:" />
    <t:selectOneMenu value="#{typeTreeBean.filter.rootTypeId}"
      styleClass="selectBox" style="font-family:Courier New;width:40%">
        <f:selectItems value="#{typeBean.rootTypeIdSelectItems}" />
    </t:selectOneMenu>

    <h:outputText value="#{dictionaryBundle.type_showTypeId}:" />
    <h:selectBooleanCheckbox value="#{typeTreeBean.filter.showTypeId}" />

    <f:facet name="footer">
      <h:commandButton id="default_button" value="#{objectBundle.search}"
        action="#{typeTreeBean.search}" styleClass="searchButton" />
    </f:facet>
  </h:panelGrid>

  <t:div styleClass="inputBox" style="margin-top:12px"
    rendered="#{typeTreeBean.treeModel != null}">
    <h:panelGroup styleClass="documentTree">
      <t:tree2 value="#{typeTreeBean.treeModel}"
        var="node" varNodeToggler="t" clientSideToggle="false"
        preserveToggle="false" showRootNode="true">
    
        <f:facet name="type">
          <t:panelGroup styleClass="nodeFolder">
            <t:graphicImage value="/common/dic/images/type_open.png"
              rendered="#{t.nodeExpanded}" border="0" alt=""
              style="vertical-align:middle; margin-right:4px;" />
            <t:graphicImage value="/common/dic/images/type_closed.png"
              rendered="#{!t.nodeExpanded}" border="0" alt=""
              style="vertical-align:middle; margin-right:4px;" />
            <h:commandLink action="#{typeTreeBean.showType}"
              style="text-decoration:none;color:black">
              <h:outputText value="#{node.description}"
                style="#{typeTreeBean.selectedType ? 'font-weight:bold' : ''}" />
              <h:outputText value="(#{node.derivedTypeCount})"
                rendered="#{node.derivedTypeCount > 0}"
                style="margin-left:4px" />
              <h:outputText value=" (#{node.identifier})"
                rendered="#{typeTreeBean.filter.showTypeId}"
                style="font-family:Courier New;color:gray" />
            </h:commandLink>
          </t:panelGroup>
        </f:facet>
        
        <f:facet name="previous">
          <h:commandLink action="#{node.previousPage}"
            styleClass="treePaginator">
            <t:graphicImage value="/common/dic/images/up.gif" border="0" alt=""
              style="vertical-align:middle; margin-right:4px;" />
            <h:outputText value="#{dictionaryBundle.typeTree_previous} (#{node.previousChildCount})" />
          </h:commandLink>
        </f:facet>

        <f:facet name="next">
          <h:commandLink action="#{node.nextPage}"
            styleClass="treePaginator">
            <t:graphicImage value="/common/dic/images/down.gif" border="0" alt=""
              style="vertical-align:middle; margin-right:4px;" />
            <h:outputText value="#{dictionaryBundle.typeTree_next} (#{node.nextChildCount})" />
          </h:commandLink>
        </f:facet>

      </t:tree2>
    </h:panelGroup>
  </t:div>

  <t:div styleClass="actionsBar">
     <h:commandButton value="#{objectBundle.current}" image="#{userSessionBean.icons.current}" alt="#{objectBundle.current}" title="#{objectBundle.current}"
       action="#{typeBean.show}" immediate="true"
       styleClass="currentButton" />
     <h:commandButton value="#{objectBundle.create}"        image="#{userSessionBean.icons.new}"        alt="#{objectBundle.create}" title="#{objectBundle.create}"
       action="#{typeBean.create}" immediate="true"
       styleClass="createButton" />
  </t:div>

</jsp:root>

