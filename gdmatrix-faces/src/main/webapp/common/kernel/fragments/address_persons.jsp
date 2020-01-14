<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.kernel.web.resources.KernelBundle" 
                var="kernelBundle" />

  <t:buffer into="#{table}">
    <t:dataTable id="data" value="#{addressPersonsBean.rows}" var="row"
                 rowClasses="row1,row2" headerClass="header" footerClass="footer"
                 styleClass="resultList" style="width:100%"
                 bodyStyle="#{empty addressPersonsBean.rows ? 'display:none' : ''}"
                 rows="#{addressPersonsBean.pageSize}">
      <t:column style="width:10%">
        <f:facet name="header">
          <h:outputText value="#{kernelBundle.address_id}:" />
        </f:facet>
        <h:outputText value="#{row.person.personId}" />
      </t:column>

      <t:column style="width:50%">
        <f:facet name="header">
          <h:outputText value="#{kernelBundle.person_full_name}:" />
        </f:facet>
        <h:outputText value="#{row.person.fullName}" />
      </t:column>

      <t:column style="width:20%">
        <f:facet name="header">
          <h:outputText value="NIF:" />
        </f:facet>
        <h:outputText value="#{row.person.nif}" />
      </t:column>

      <t:column style="width:20%" styleClass="actionsColumn">
        <h:panelGroup>
          <h:commandButton value="#{objectBundle.show}"
                           image="#{userSessionBean.icons.show}"
                           alt="#{objectBundle.show}" title="#{objectBundle.show}"
                           action="#{addressPersonsBean.showPerson}"
                           styleClass="showButton"  />
          <h:commandButton value="#{objectBundle.delete}"
                           image="#{userSessionBean.icons.delete}"
                           alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
                           action="#{addressPersonsBean.removePerson}"
                           styleClass="removeButton"
                           onclick="return confirm('#{objectBundle.confirm_remove}');" />
        </h:panelGroup>
      </t:column>

      <f:facet name="footer">
        <t:dataScroller
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

  </t:buffer>

  <t:div styleClass="resultBar" rendered="#{addressPersonsBean.rowCount > 0}">
    <t:dataScroller for="data"
                    firstRowIndexVar="firstRow"
                    lastRowIndexVar="lastRow"
                    rowsCountVar="rowCount">
      <h:outputFormat value="#{objectBundle.resultRange}">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
  </t:div>

  <h:outputText value="#{table}" escape="false"/>

  <t:div>
    <h:outputText value="#{kernelBundle.person}: " />
    <h:panelGroup>
      <t:selectOneMenu value="#{addressPersonsBean.personId}"
                       style="width:300px" styleClass="selectBox">
        <f:selectItems value="#{addressPersonsBean.personSelectItems}" />
      </t:selectOneMenu>
      <h:commandButton action="#{addressPersonsBean.addPerson}" 
                       styleClass="addButton" value="#{objectBundle.add}"
                       image="#{userSessionBean.icons.add}"
                       alt="#{objectBundle.add}" title="#{objectBundle.add}"
                       />
      <h:commandButton action="#{addressPersonsBean.searchPerson}" 
                       styleClass="searchButton" value="#{objectBundle.search}"
                       image="#{userSessionBean.icons.search}"
                       alt="#{objectBundle.search}" title="#{objectBundle.search}"
                       />
    </h:panelGroup>
  </t:div>

</jsp:root>
