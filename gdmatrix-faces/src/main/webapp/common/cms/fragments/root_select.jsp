<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.cms.web.resources.CMSBundle" var="cmsBundle" />

  <t:saveState value="#{nodeEditBean}" />
  <t:saveState value="#{cmsToolbarBean}" />

  <h:panelGrid columns="1" styleClass="cmsRootSelection">
    <h:panelGroup styleClass="subtoolbar">
      <h:commandLink action="#{nodeEditBean.createRootNode}" styleClass="imageButton">
        <t:graphicImage url="/common/cms/images/add.png" title="#{cmsBundle.createRoot}"
                        alt="#{cmsBundle.createRoot}" />
      </h:commandLink>
    </h:panelGroup>
    <h:outputText value="#{cmsBundle.selectRoot}" styleClass="title" />
    <h:dataTable value="#{nodeEditBean.rootNodeList}" var="node" styleClass="rootTable" columnClasses="col1,col2" >
      <h:column>
        <h:outputText value="#{node.nodeId}" styleClass="midBox" />
      </h:column>
      <h:column>
        <h:commandLink action="#{nodeEditBean.changeRootNode}" styleClass="rootLink">
          <h:outputText value="#{nodeEditBean.nodeLabel}" styleClass="rootLabel" />
        </h:commandLink>
      </h:column>
    </h:dataTable>
  </h:panelGrid>

</jsp:root>

