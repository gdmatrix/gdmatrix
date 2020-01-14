<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.classif.web.resources.ClassificationBundle"
    var="classificationBundle" />

  <h:panelGrid columns="2" styleClass="filterPanel" summary=""
    columnClasses="column1, column2"
    headerClass="header" footerClass="footer">
    <f:facet name="header">
      <h:outputText />
    </f:facet>

    <h:outputText value="#{classificationBundle.class_classId}:" />
    <h:inputText value="#{classSearchBean.filter.classId}"
      styleClass="inputBox" style="font-family:Courier New;width:20%"/>

    <h:outputText value="#{classificationBundle.class_superClassId}:" />
    <h:panelGroup>
      <h:inputText value="#{classSearchBean.filter.superClassId}"
        styleClass="inputBox" style="font-family:Courier New;width:20%"/>
    </h:panelGroup>

    <h:outputText value="#{classificationBundle.class_title}:" />
    <h:inputText value="#{classSearchBean.filter.title}"
      styleClass="inputBox" style="width:80%"/>

    <h:outputText value="#{classificationBundle.class_description}:" />
    <h:inputText value="#{classSearchBean.filter.description}"
      styleClass="inputBox" style="width:80%"/>

    <h:outputText value="#{classificationBundle.class_date}:" />
    <h:panelGroup>
      <sf:calendar value="#{classSearchBean.filter.startDateTime}"
      styleClass="calendarBox" buttonStyleClass="calendarButton" style="width:80px"
      internalFormat="yyyyMMddHHmmss" externalFormat="dd/MM/yyyy|HH:mm:ss" />
    </h:panelGroup>

    <f:facet name="footer">
      <t:commandButton id="default_button" value="#{objectBundle.search}" 
        onclick="showOverlay(); return true;"
        action="#{classSearchBean.search}" styleClass="searchButton" />
    </f:facet>
  </h:panelGrid>

  <t:div rendered="#{classSearchBean.navigationEnabled}" styleClass="resultBar">
    <h:outputText value="#{classificationBundle.classSearch_subclassesFrom}: " />
    <t:dataList value="#{classSearchBean.superClasses}" var="class">
      <h:panelGroup style="display:block;padding:2px 2px 2px #{classSearchBean.indent}px;">
        <h:graphicImage value="/common/classif/images/next.gif" alt="" />
        <h:commandLink action="#{classSearchBean.findSubClassesFromPath}"
          style="text-decoration:none;color:black">
          <h:outputText value="#{class.classId}"
          style="font-family:Courier new;font-weight:bold;
            background:#FFFFD0;padding:0px;border:orange 1px solid;" />
          <h:outputText value=" : #{class.title}" />
        </h:commandLink>
      </h:panelGroup>
    </t:dataList>
  </t:div>

  <t:div styleClass="resultBar" rendered="#{classSearchBean.rows != null}">
    <t:dataScroller for="data"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      rendered="#{classSearchBean.rowCount > 0}">
      <h:outputFormat value="#{objectBundle.resultRange}"
        style="margin-top:10px;display:block">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
    <h:outputText value="#{objectBundle.no_results_found}"
      rendered="#{classSearchBean.rowCount == 0}" />
  </t:div>

  <t:dataTable id="data" value="#{classSearchBean.rows}" var="row"
    rows="#{classSearchBean.pageSize}"
    first="#{classSearchBean.firstRowIndex}"
    rendered="#{classSearchBean.rowCount > 0}"
    rowStyleClass="#{classSearchBean.currentClass ? 'selectedRow' : null}"
    styleClass="resultList"
    rowClasses="row1,row2" headerClass="header" footerClass="footer">
    <t:column style="width:12%" >
      <f:facet name="header">
        <h:outputText value="#{classificationBundle.class_class}" />
      </f:facet>
      <h:panelGroup>
        <h:outputText value="#{row.classId}"
          style="font-family:Courier New;font-weight:bold;"
          styleClass="#{classSearchBean.rowStyle}" />
        <h:commandLink action="#{classSearchBean.showInTree}"
          rendered="#{classSearchBean.showInTreeEnabled}">
          <h:graphicImage url="/common/classif/images/tree.gif"
            alt="#{classificationBundle.class_show_in_tree}"
            style="border:none;vertical-align:middle;margin-left:4px" />
        </h:commandLink>
        <h:commandLink action="#{classSearchBean.findSubClasses}"
          rendered="#{not classSearchBean.leafClass}">
          <h:graphicImage url="/common/dic/images/down.gif"
            alt="#{classificationBundle.class_show_subclasses}"
            style="border:none;vertical-align:middle;margin-left:4px" />
        </h:commandLink>
      </h:panelGroup>
    </t:column>
    <t:column style="width:45%">
      <f:facet name="header">
        <h:outputText value="#{classificationBundle.class_title}" />
      </f:facet>
      <h:outputText value="#{row.title}"
        styleClass="#{classSearchBean.rowStyle}" />
    </t:column>
    <t:column style="width:21%">
      <f:facet name="header">
        <h:outputText value="#{classificationBundle.class_startDate}" />
      </f:facet>
      <h:outputText value="#{classSearchBean.rowStartDateTime}"
        styleClass="#{classSearchBean.rowStyle}">
          <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
      </h:outputText>
    </t:column>
    <t:column style="width:21%" styleClass="actionsColumn">
      <h:panelGroup>
      <h:commandButton value="#{objectBundle.select}"
        image="#{userSessionBean.icons.back}"
        alt="#{objectBundle.select}" title="#{objectBundle.select}"
        rendered="#{controllerBean.selectableNode}"
        styleClass="selectButton" immediate="true"
        action="#{classSearchBean.selectClass}" />
      <h:commandButton value="#{objectBundle.show}"           
        image="#{userSessionBean.icons.show}"
        alt="#{objectBundle.show}" title="#{objectBundle.show}"
        styleClass="showButton" immediate="true"
        action="#{classSearchBean.showClass}" />
      </h:panelGroup>
    </t:column>

    <f:facet name="footer">
      <t:dataScroller for="data"
        fastStep="100"
        paginator="true"
        paginatorMaxPages="9"
        immediate="true"
        styleClass="scrollBar"
        paginatorColumnClass="page"
        paginatorActiveColumnClass="activePage"
        nextStyleClass="nextButton"
        previousStyleClass="previousButton"
        firstStyleClass="firstButton"
        lastStyleClass="lastButton"
        fastfStyleClass="fastForwardButton"
        fastrStyleClass="fastRewindButton"
        renderFacetsIfSinglePage="false">
        <f:facet name="first">
          <t:div title="#{objectBundle.first}"></t:div>
        </f:facet>
        <f:facet name="last">
          <t:div title="#{objectBundle.last}"></t:div>
        </f:facet>
        <f:facet name="previous">
          <t:div title="#{objectBundle.previous}"></t:div>
        </f:facet>
        <f:facet name="next">
          <t:div title="#{objectBundle.next}"></t:div>
        </f:facet>
        <f:facet name="fastrewind">
          <t:div title="#{objectBundle.fastRewind}"></t:div>
        </f:facet>
        <f:facet name="fastforward">
          <t:div title="#{objectBundle.fastForward}"></t:div>
        </f:facet>
      </t:dataScroller>
    </f:facet>
  </t:dataTable>

  <t:div styleClass="actionsBar">
     <h:commandButton value="#{objectBundle.current}" image="#{userSessionBean.icons.current}" alt="#{objectBundle.current}" title="#{objectBundle.current}"
       action="#{classBean.show}" immediate="true"
       styleClass="currentButton" />
     <h:commandButton value="#{objectBundle.create}"        image="#{userSessionBean.icons.new}"        alt="#{objectBundle.create}" title="#{objectBundle.create}"
       action="#{classBean.create}" immediate="true"
       styleClass="createButton" />
  </t:div>
  
</jsp:root>
