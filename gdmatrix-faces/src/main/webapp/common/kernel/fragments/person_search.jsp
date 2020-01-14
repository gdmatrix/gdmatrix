<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.kernel.web.resources.KernelBundle" 
    var="kernelBundle" />

  <h:panelGrid columns="2" styleClass="filterPanel" summary=""
    columnClasses="column1, column2"
    headerClass="header" footerClass="footer">

    <f:facet name="header">
      <h:outputText />
    </f:facet>

    <h:outputText value="#{objectBundle.object_id}:" />
    <h:inputText value="#{personSearchBean.personId}"
      styleClass="inputBox" style="width:14%" />

    <h:outputText value="#{kernelBundle.person_full_name}:" />
    <h:inputText value="#{personSearchBean.filter.fullName}"
      styleClass="inputBox" style="width:80%" />

    <h:outputText value="#{kernelBundle.person_name}:" />
    <h:inputText value="#{personSearchBean.filter.name}"
      styleClass="inputBox" />

    <h:outputText value="#{kernelBundle.person_surname1}:" />
    <h:inputText value="#{personSearchBean.filter.firstSurname}"
      styleClass="inputBox" />

    <h:outputText value="#{kernelBundle.person_surname2}:" />
    <h:inputText value="#{personSearchBean.filter.secondSurname}"
      styleClass="inputBox" />

    <h:outputText value="#{kernelBundle.person_nif}:" />
    <h:inputText value="#{personSearchBean.filter.nif}"
      styleClass="inputBox" />

    <f:facet name="footer">
      <h:panelGroup>
        <h:commandButton value="#{objectBundle.clear}"
                       styleClass="searchButton"
                       action="#{personSearchBean.clearFilter}"/>      
        <h:commandButton id="default_button" value="#{objectBundle.search}"
          action="#{personSearchBean.search}" styleClass="searchButton"
          onclick="showOverlay()"/>
      </h:panelGroup>
    </f:facet>
  </h:panelGrid>

  <t:div styleClass="actionsBar top" rendered="#{personSearchBean.rowCount > 2}">
     <h:commandButton value="#{objectBundle.current}" 
       image="#{userSessionBean.icons.current}"
       alt="#{objectBundle.current}" title="#{objectBundle.current}"
       action="#{personBean.show}" immediate="true"
       styleClass="currentButton" />
     <h:commandButton value="#{objectBundle.create}"
       image="#{userSessionBean.icons.new}"
       alt="#{objectBundle.create}" title="#{objectBundle.create}"
       action="#{personBean.create}" immediate="true"
       styleClass="createButton" />
  </t:div>
  
  <t:div styleClass="resultBar" rendered="#{personSearchBean.rows != null}">
    <t:dataScroller for="data"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      rendered="#{personSearchBean.rowCount > 0}">
      <h:outputFormat value="#{objectBundle.resultRange}"
        style="margin-top:10px;display:block">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
    <h:outputText value="#{objectBundle.no_results_found}"
      rendered="#{personSearchBean.rowCount == 0}" />
  </t:div>

  <t:dataTable id="data" value="#{personSearchBean.rows}" 
    var="row" rows="#{personSearchBean.pageSize}"
    first="#{personSearchBean.firstRowIndex}"
    rendered="#{personSearchBean.rowCount > 0}"
    rowStyleClass="#{row.personId == personBean.objectId ? 'selectedRow' : null}"
    styleClass="resultList" summary="results"
    rowClasses="row1,row2" headerClass="header" footerClass="footer">
    <t:column style="width:10%">
      <f:facet name="header">
        <h:outputText value="Id" />
      </f:facet>
      <h:outputText value="#{row.personId}" />
    </t:column>
    <t:column style="width:60%">
      <f:facet name="header">
        <h:outputText value="#{kernelBundle.person_full_name}" />
      </f:facet>
      <h:outputText value="#{row.fullName}" />
    </t:column>
    <t:column style="width:10%">
      <f:facet name="header">
        <h:outputText value="#{kernelBundle.person_nif}" />
      </f:facet>
      <h:outputText value="#{row.nif}" />
    </t:column>
    <t:column style="width:20%" styleClass="actionsColumn">
      <h:commandButton value="#{objectBundle.select}"
        image="#{userSessionBean.icons.back}"
        alt="#{objectBundle.select}" title="#{objectBundle.select}"
        rendered="#{controllerBean.selectableNode}"
        styleClass="selectButton" immediate="true"
        action="#{personSearchBean.selectPerson}" />
      <h:commandButton value="#{objectBundle.show}"
        image="#{userSessionBean.icons.show}"
        alt="#{objectBundle.show}" title="#{objectBundle.show}"
        styleClass="showButton" immediate="true"
        action="#{personSearchBean.showPerson}"/>
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
     <h:commandButton value="#{objectBundle.current}" 
       image="#{userSessionBean.icons.current}"
       alt="#{objectBundle.current}" title="#{objectBundle.current}"
       action="#{personBean.show}" immediate="true"
       styleClass="currentButton" />
     <h:commandButton value="#{objectBundle.create}"
       image="#{userSessionBean.icons.new}"
       alt="#{objectBundle.create}" title="#{objectBundle.create}"
       action="#{personBean.create}" immediate="true"
       styleClass="createButton" />
  </t:div>

</jsp:root>
