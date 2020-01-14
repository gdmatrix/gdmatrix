<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.cms.web.resources.CMSBundle"
                var="cmsBundle" />
  
  <t:saveState value="#{nodeEditBean}" />
  <t:saveState value="#{cmsToolbarBean}" />

  <t:div style="margin-top: 8px; margin-left: 8px;">
    <h:panelGrid columns="2">
        <h:outputLabel value="#{cmsBundle.workspaceId}:" />
        <h:outputText value="#{workspaceEditBean.workspace.workspaceId}" />
        <h:outputLabel value="#{cmsBundle.name}:" />
        <h:inputText value="#{workspaceEditBean.workspace.name}" style="width:300px;"/>
        <h:outputLabel value="#{cmsBundle.description}:" />
        <h:inputText value="#{workspaceEditBean.workspace.description}" style="width:600px;"/>
        <h:outputLabel value="#{cmsBundle.referenceWorkspace}:" />
        <t:selectOneMenu value="#{workspaceEditBean.workspace.refWorkspaceId}">
          <f:selectItems value="#{workspaceEditBean.refWorkspaceItems}" />
        </t:selectOneMenu>
    </h:panelGrid>
  </t:div>
  <t:div style="margin-top: 8px; margin-left: 8px;">
    <h:panelGroup>
      <h:commandButton value="#{cmsBundle.save}" action="#{workspaceEditBean.save}" />
      <h:commandButton value="#{cmsBundle.cancel}" action="#{workspaceEditBean.cancel}" />
    </h:panelGroup>
  </t:div>

</jsp:root>

