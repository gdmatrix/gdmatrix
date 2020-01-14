<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  
  <f:loadBundle basename="org.santfeliu.kernel.web.resources.KernelBundle" 
    var="kernelBundle" />

  <t:buffer into="#{table}">
  <t:dataTable id="data" value="#{personContactsBean.rows}" var="row"
    rowStyleClass="#{personContactsBean.editingRow == row ? 'selectedRow' : null}"
    rowClasses="row1,row2" headerClass="header" footerClass="footer"
    styleClass="resultList" style="width:100%"
    bodyStyle="#{empty personContactsBean.rows ? 'display:none' : ''}"
    rows="#{personContactsBean.pageSize}">
    <t:column style="width:30%">
      <f:facet name="header">
        <h:outputText value="#{kernelBundle.contact_type}:" />
      </f:facet>
      <h:outputText value="#{row.contactTypeLabel}"
        rendered="#{personContactsBean.editingRow != row}" />
      <t:selectOneMenu value="#{row.contactTypeId}"
        rendered="#{personContactsBean.editingRow == row}"
        styleClass="selectBox" style="width:95%">
        <f:selectItems value="#{personContactsBean.allTypeItems}" />
      </t:selectOneMenu>
    </t:column>

    <t:column style="width:25%">
      <f:facet name="header">
        <h:outputText value="#{kernelBundle.contact_value}:" />
      </f:facet>
      <h:outputText value="#{row.value}"
        rendered="#{personContactsBean.editingRow != row}" />
      <h:inputText value="#{row.value}"
        rendered="#{personContactsBean.editingRow == row}"
        styleClass="inputBox" style="width:95%"
        maxlength="#{personContactsBean.propertySize.value}"/>
    </t:column>

    <t:column style="width:25%">
      <f:facet name="header">
        <h:outputText value="#{kernelBundle.contact_comments}:" />
      </f:facet>
      <h:outputText value="#{row.comments}"
        rendered="#{personContactsBean.editingRow != row}" />
      <h:inputText value="#{row.comments}"
        rendered="#{personContactsBean.editingRow == row}"
        styleClass="inputBox" style="width:95%"
        maxlength="#{personContactsBean.propertySize.comments}"/>
    </t:column>

    <t:column style="width:20%" styleClass="actionsColumn">
      <!-- non editing row -->
      <h:panelGroup rendered="#{personContactsBean.editingRow != row}">
        <h:commandButton value="#{objectBundle.edit}"
          image="#{userSessionBean.icons.detail}"
          alt="#{objectBundle.edit}" title="#{objectBundle.edit}"
          action="#{personContactsBean.editContact}"
          rendered="#{row.contactId != null}"
          disabled="#{personContactsBean.editingRow != null}"
          styleClass="editButton"  />
        <h:commandButton value="#{objectBundle.delete}"           image="#{userSessionBean.icons.delete}"           alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
          action="#{personContactsBean.removeContact}"
          rendered="#{row.contactId != null}"
          disabled="#{personContactsBean.editingRow != null}"
          styleClass="removeButton"
          onclick="return confirm('#{objectBundle.confirm_remove}');" />
      </h:panelGroup>

      <!-- editing row -->
      <h:panelGroup rendered="#{personContactsBean.editingRow == row}">
        <h:commandButton value="#{objectBundle.store}"
          action="#{personContactsBean.storeContact}"
          styleClass="storeButton" />
        <h:commandButton value="#{objectBundle.cancel}"
          immediate="true"
          action="#{personContactsBean.cancelContact}"
          styleClass="cancelButton" />
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

  <t:div styleClass="resultBar" rendered="#{personContactsBean.rowCount > 0}">
    <t:dataScroller for="data"
      rowsCountVar="rowCount">
      <h:outputFormat value="#{objectBundle.shortResultRange}">
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
  </t:div>

  <h:outputText value="#{table}" escape="false"/>

  <t:div style="width:100%;text-align:right">
    <h:commandButton value="#{objectBundle.add}"
      image="#{userSessionBean.icons.add}"
      alt="#{objectBundle.add}" title="#{objectBundle.add}"
      action="#{personContactsBean.addContact}"
      disabled="#{personContactsBean.editingRow != null}"
      styleClass="addButton"  />
  </t:div>

</jsp:root>
