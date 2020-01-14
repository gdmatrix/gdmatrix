<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.classif.web.resources.ClassificationBundle"
    var="classificationBundle" />

  <sf:saveScroll />
  
  <h:panelGrid columns="2" styleClass="filterPanel" summary=""
    columnClasses="column1, column2"
    headerClass="header" footerClass="footer">
    <f:facet name="header">
      <h:outputText />
    </f:facet>

    <h:outputText value="#{classificationBundle.class_rootClassId}:" />
    <h:inputText value="#{classTreeBean.filter.rootClassId}" required="true"
      styleClass="inputBox" style="font-family:Courier New;width:14%"/>

    <h:outputText value="#{classificationBundle.class_date}:" />
    <h:panelGroup>
      <sf:calendar value="#{classTreeBean.filter.dateTime}" required="true"
        styleClass="calendarBox" buttonStyleClass="calendarButton" style="width:80px"
        internalFormat="yyyyMMddHHmmss" externalFormat="dd/MM/yyyy|HH:mm:ss" />
    </h:panelGroup>

    <f:facet name="footer">
      <t:commandButton id="default_button" value="#{objectBundle.search}"
        onclick="showOverlay(); return true;"
        action="#{classTreeBean.search}" styleClass="searchButton" />
    </f:facet>
  </h:panelGrid>

  <t:div styleClass="inputBox" style="margin-top:12px"
    rendered="#{classTreeBean.treeModel != null}">
    <h:panelGroup styleClass="documentTree">
      <t:tree2 value="#{classTreeBean.treeModel}"
        var="node" varNodeToggler="t" clientSideToggle="false"
        preserveToggle="false" showRootNode="true">

        <f:facet name="folder">
          <t:panelGroup styleClass="nodeFolder">
            <t:graphicImage value="/common/doc/images/yellow-folder-open.png"
              rendered="#{t.nodeExpanded}" border="0" alt=""
              style="vertical-align:middle; margin-right:4px;" />
            <t:graphicImage value="/common/doc/images/yellow-folder-closed.png"
              rendered="#{!t.nodeExpanded}" border="0" alt=""
              style="vertical-align:middle; margin-right:4px;" />
              <h:commandLink action="#{classTreeBean.showClass}"
                style="text-decoration:none">
                <h:outputText value="#{node.identifier}: #{node.description}"
                  styleClass="#{classTreeBean.cutSubClass ? 'cutClass' :
                  classTreeBean.selectedClass ? 'selectedClass' : 'unselectedClass'}" />
              </h:commandLink>
              <h:panelGroup rendered="#{classTreeBean.selectedClass and classBean.editable}"
                styleClass="treeActions">
                <h:commandLink action="#{classTreeBean.addClass}" title="add">
                  <t:graphicImage value="/images/new.gif" alt="" />
                </h:commandLink>
                <h:commandLink action="#{classTreeBean.cutClass}" title="cut"
                  rendered="#{node.properties != classTreeBean.cutClass}">
                  <t:graphicImage value="/images/cut.gif" alt="" />
                </h:commandLink>
                <h:commandLink action="#{classTreeBean.pasteClass}" title="paste"
                  rendered="#{classTreeBean.cutClass != null and !classTreeBean.cutSubClass}">
                  <t:graphicImage value="/images/paste.gif" alt="" />
                </h:commandLink>
              </h:panelGroup>
              <h:commandLink action="#{classTreeBean.selectClass}"
                rendered="#{not classTreeBean.selectedClass and classBean.editable}"
                styleClass="treeActions">
                <t:graphicImage value="/images/select.gif" alt="" />
              </h:commandLink>
         </t:panelGroup>
        </f:facet>
      </t:tree2>
    </h:panelGroup>
  </t:div>

  <t:div styleClass="actionsBar">
    <h:commandButton value="#{objectBundle.current}" image="#{userSessionBean.icons.current}" alt="#{objectBundle.current}" title="#{objectBundle.current}"
      action="#{classBean.show}" immediate="true"
      styleClass="currentButton" />
    <h:commandButton value="#{objectBundle.create}"        image="#{userSessionBean.icons.new}"        alt="#{objectBundle.create}" title="#{objectBundle.create}"
      action="#{classBean.create}" immediate="true"
      styleClass="createButton" />
  </t:div>
</jsp:root>
