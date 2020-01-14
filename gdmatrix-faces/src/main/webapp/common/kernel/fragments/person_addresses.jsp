<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.kernel.web.resources.KernelBundle" 
    var="kernelBundle" />  

  <t:buffer into="#{table}">
    <t:dataTable id ="data" value="#{personAddressesBean.rows}" var="row"
      rowClasses="row1,row2" headerClass="header" footerClass="footer"
      styleClass="resultList" style="width:100%"
      bodyStyle="#{empty personAddressesBean.rows ? 'display:none' : ''}"
      rows="#{personAddressesBean.pageSize}">
      <t:column style="width:10%">
        <f:facet name="header">
          <h:outputText value="Id:" />
        </f:facet>
        <h:outputText value="#{row.address.addressId}" />
      </t:column>

      <t:column style="width:70%">
        <f:facet name="header">
          <h:outputText value="#{kernelBundle.address}:" />
        </f:facet>
        <h:outputText value="#{row.address.description} - #{row.address.city} (#{row.address.province})" />
      </t:column>

      <t:column style="width:20%" styleClass="actionsColumn">
        <h:panelGroup>
          <h:commandButton value="#{objectBundle.show}"
            image="#{userSessionBean.icons.show}"
            alt="#{objectBundle.show}" title="#{objectBundle.show}"
            action="#{personAddressesBean.showAddress}"
            styleClass="showButton" />
          <h:commandButton value="#{objectBundle.delete}"
            image="#{userSessionBean.icons.delete}"
            alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
            action="#{personAddressesBean.removeAddress}"
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

  <t:div styleClass="resultBar" rendered="#{personAddressesBean.rowCount > 0}">
    <t:dataScroller for="data" styleClass="resultBar"
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
    <h:outputText value="#{kernelBundle.address}: " />
    <h:panelGroup>
      <t:selectOneMenu value="#{personAddressesBean.addressId}" 
        style="width:400px" styleClass="selectBox">
        <f:selectItems value="#{personAddressesBean.addressSelectItems}" />
      </t:selectOneMenu>
      <h:commandButton value="#{objectBundle.add}"        
        image="#{userSessionBean.icons.add}"
        alt="#{objectBundle.add}" title="#{objectBundle.add}"
        action="#{personAddressesBean.addAddress}" 
        styleClass="addButton" />
      <h:commandButton value="#{objectBundle.search}"         
        image="#{userSessionBean.icons.search}"
        alt="#{objectBundle.search}" title="#{objectBundle.search}"
        action="#{personAddressesBean.searchAddress}" 
        styleClass="searchButton" />
    </h:panelGroup>
  </t:div>

</jsp:root>
