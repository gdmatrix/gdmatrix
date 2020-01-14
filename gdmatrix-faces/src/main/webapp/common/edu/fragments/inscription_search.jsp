<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.edu.web.resources.EducationBundle" 
    var="educationBundle" />

  <h:panelGrid columns="2" styleClass="filterPanel" summary=""
    columnClasses="column1, column2"
    headerClass="header" footerClass="footer">

    <f:facet name="header">
      <h:outputText />
    </f:facet>

    <h:outputText value="#{educationBundle.inscription_code}:" />
    <h:inputText value="#{inscriptionSearchBean.filter.code}"
      styleClass="inputBox" />

    <h:outputText value="#{educationBundle.person_name}:" />
    <h:inputText value="#{inscriptionSearchBean.filter.personName}"
      styleClass="inputBox" style="width:80%" />

    <f:facet name="footer">
      <h:commandButton id="default_button" value="#{objectBundle.search}"
        action="#{inscriptionSearchBean.search}" styleClass="searchButton" />
    </f:facet>
  </h:panelGrid>

  <t:div styleClass="resultBar" rendered="#{inscriptionSearchBean.rows != null}">
    <t:dataScroller for="data"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      rendered="#{inscriptionSearchBean.rowCount > 0}">
      <h:outputFormat value="#{objectBundle.resultRange}"
        style="margin-top:10px;display:block">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
    <h:outputText value="#{objectBundle.no_results_found}"
      rendered="#{inscriptionSearchBean.rowCount == 0}" />
  </t:div>

  <t:dataTable rows="#{inscriptionSearchBean.pageSize}" id="data"
    first="#{inscriptionSearchBean.firstRowIndex}"
    value="#{inscriptionSearchBean.rows}" var="row"
    rendered="#{inscriptionSearchBean.rowCount > 0}"
    rowStyleClass="#{row.inscriptionId == inscriptionBean.objectId ? 'selectedRow' : null}"
    styleClass="resultList" summary="results"
    rowClasses="row1,row2" headerClass="header" footerClass="footer">
    <t:column style="width:6%">
      <f:facet name="header">
        <h:outputText value="Id" />
      </f:facet>
      <h:outputText value="#{row.inscriptionId}" />
    </t:column>
    <t:column style="width:34%">
      <f:facet name="header">
        <h:outputText value="#{educationBundle.person_name}" />
      </f:facet>
      <h:outputText value="#{row.personView.fullName}" />
    </t:column>
    <t:column style="width:30%">
      <f:facet name="header">
        <h:outputText value="#{educationBundle.school_name}" />
      </f:facet>
      <h:outputText value="#{row.schoolName}" />
    </t:column>
    <t:column style="width:10%">
      <f:facet name="header">
        <h:outputText value="#{educationBundle.course_name}" />
      </f:facet>
      <h:outputText value="#{row.courseName}" />
    </t:column>
    <t:column style="width:20%" styleClass="actionsColumn">
      <h:commandButton value="#{objectBundle.select}"
        image="#{userSessionBean.icons.back}"
        alt="#{objectBundle.select}" title="#{objectBundle.select}"
        rendered="#{controllerBean.selectableNode}"
        styleClass="selectButton" immediate="true"
        action="#{inscriptionSearchBean.selectInscription}" />
      <h:commandButton value="#{objectBundle.show}"           
        image="#{userSessionBean.icons.show}"
        alt="#{objectBundle.show}" title="#{objectBundle.show}"
        styleClass="showButton" immediate="true"
        action="#{inscriptionSearchBean.showInscription}" />
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
       action="#{inscriptionBean.show}" immediate="true"
       styleClass="currentButton" />
     <h:commandButton value="#{objectBundle.create}"        image="#{userSessionBean.icons.new}"        alt="#{objectBundle.create}" title="#{objectBundle.create}"
       action="#{inscriptionBean.create}" immediate="true"
       styleClass="createButton" />
  </t:div>

</jsp:root>
