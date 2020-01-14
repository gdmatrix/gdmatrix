<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.doc.web.resources.DocumentBundle"
                var="documentBundle"/>

  <h:panelGrid columns="1" style="width:100%" styleClass="documentsTreePage">
  
    <h:panelGrid id="header" columns="1" style="width:100%" 
      rendered="#{documentTreeBean.headerBrowser != null}" 
      styleClass="headerDocument">
      <sf:browser binding="#{documentTreeBean.headerBrowser}"
        port="#{applicationBean.defaultPort}"/>
    </h:panelGrid>

    <h:panelGroup styleClass="documentTree">
      <t:tree2 id="clientTree" value="#{documentTreeBean.treeData}" 
        var="node" varNodeToggler="t" clientSideToggle="true" 
        preserveToggle="false" showRootNode="false">
  
  <!--
          <f:facet name="folder">
            <t:panelGrid columns="2" cellpadding="4" cellspacing="0" summary=""
             styleClass="node" columnClasses="icon, text">
              <t:graphicImage value="/common/doc/images/yellow-folder-open.png" 
                alt="" border="0" />
              <h:panelGroup>
                <h:outputText value="#{node.description}" styleClass="nodeFolder"/>
                <h:outputText value=" (#{node.childCount})" styleClass="childCount"
                  rendered="#{!empty node.children}"/>
              </h:panelGroup>
           </t:panelGrid>
          </f:facet>
  -->
  
        <f:facet name="folder">
          <t:panelGroup styleClass="nodeFolder">
            <f:facet name="expand">
              <t:graphicImage value="/common/doc/images/yellow-folder-open.png" 
                rendered="#{t.nodeExpanded}" border="0" alt=""
                style="vertical-align:middle; margin-right:4px;" />
            </f:facet>
            <f:facet name="collapse">
              <t:graphicImage value="/common/doc/images/yellow-folder-closed.png" 
                rendered="#{!t.nodeExpanded}" border="0" alt=""
                style="vertical-align:middle; margin-right:4px;" />
            </f:facet>
            <h:outputText value="#{node.description}" />
            <h:outputText value=": #{node.properties.info}" 
              rendered="#{node.properties.info != null}" />
         </t:panelGroup>
        </f:facet>
  
        <f:facet name="document">
          <h:panelGroup>
            <t:graphicImage value="#{node.properties.icon}" border="0" alt=""
              style="vertical-align:middle; margin-right:4px;" />
              <h:outputLink value="/documents/#{node.properties.uuid}/#{node.properties.filename}" target="_blank"
                styleClass="#{t.nodeSelected ? 'documentSelected':'document'}" >
                <h:outputText value="#{node.description}"/>
                <h:outputText value=": #{node.properties.info}" 
                  rendered="#{node.properties.info != null}" />
              </h:outputLink>
            <h:outputText value=" (#{node.properties.size})"/>
          </h:panelGroup>
        </f:facet>
      </t:tree2>
    </h:panelGroup>

    <h:panelGrid id="footer" columns="1" style="width:100%"
      rendered="#{documentTreeBean.footerBrowser != null}" 
      styleClass="footerDocument">
      <sf:browser binding="#{documentTreeBean.footerBrowser}"
        port="#{applicationBean.defaultPort}"/>
    </h:panelGrid>

  </h:panelGrid>
</jsp:root>
