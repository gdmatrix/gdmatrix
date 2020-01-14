<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.cases.web.resources.CaseBundle"
    var="caseBundle" />

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
    var="objectBundle" />

  <t:div rendered="#{caseACLBean.renderTypeRows}">
    <t:div>
      <h:outputText value="#{caseBundle.caseACL_inheritedRoles}"
              styleClass="textBox" style="font-weight:bold;" />
    </t:div>

    <t:dataTable value="#{caseACLBean.typeRows}" var="typeRow"
      rowClasses="row1,row2" headerClass="header" footerClass="footer"
      bodyStyle="#{empty caseACLBean.typeRows ? 'display:none' : ''}"
      styleClass="resultList" style="width:100%">

      <t:column style="width:30%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.caseACL_roleId}:" />
        </f:facet>
        <h:outputText value="#{typeRow.roleId}" style="font-family:Courier New" />
      </t:column>

      <t:column style="width:70%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.caseACL_action}:" />
        </f:facet>
        <h:outputText value="#{caseACLBean.typeRowAction}"
          style="font-family:Courier New" />
      </t:column>

    </t:dataTable>
  </t:div>

  <t:div>
    <t:div>
      <h:outputText value="#{caseBundle.caseACL_ownRoles}"
              styleClass="textBox" style="font-weight:bold;" />
    </t:div>

    <t:dataTable value="#{caseACLBean.rows}" var="row"
      rowClasses="row1,row2" headerClass="header" footerClass="footer"
      rowStyleClass="#{(caseACLBean.editingAccessControlItem != null and
                       row.accessControl.roleId == caseACLBean.editingAccessControlItem.accessControl.roleId and
                       row.accessControl.action == caseACLBean.editingAccessControlItem.accessControl.action) ?
        'selectedRow' : null}"
      styleClass="resultList" style="width:100%"
      bodyStyle="#{empty caseACLBean.rows ? 'display:none' : ''}"
      rows="#{caseACLBean.pageSize}">
      <t:column style="width:30%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.caseACL_roleId}:" />
        </f:facet>
        <h:outputText value="#{row.accessControl.roleId}" style="font-family:Courier New"
                      styleClass="#{caseACLBean.accessControlStyleClass}" />
      </t:column>

      <t:column style="width:40%">
        <f:facet name="header">
          <h:outputText value="#{caseBundle.caseACL_action}:" />
        </f:facet>
        <h:outputText value="#{caseACLBean.rowAction}"
          style="font-family:Courier New"
          styleClass="#{caseACLBean.accessControlStyleClass}" />
      </t:column>

      <t:column style="width:30%" styleClass="actionsColumn">
        <h:panelGroup>
          <h:commandButton value="#{objectBundle.edit}"
            image="#{userSessionBean.icons.detail}"
            alt="#{objectBundle.edit}" title="#{objectBundle.edit}"
            action="#{caseACLBean.editAccessControl}"
            rendered="#{row.accessControl.roleId != null}"
            disabled="#{!caseBean.editable or caseACLBean.editingAccessControlItem != null}"
            styleClass="editButton"  />
          <h:commandButton value="#{objectBundle.remove}"
            image="#{userSessionBean.icons.remove}"
            alt="#{objectBundle.remove}" title="#{objectBundle.remove}"
            action="#{caseACLBean.removeAccessControl}"
            rendered="#{row.accessControl.roleId != null}"
            disabled="#{!caseBean.editable or caseACLBean.editingAccessControlItem != null}"
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
  </t:div>

  <t:div style="text-align:right">
    <h:commandButton value="#{objectBundle.add}"
      image="#{userSessionBean.icons.add}"
      alt="#{objectBundle.add}" title="#{objectBundle.add}"
      action="#{caseACLBean.addAccessControl}"
      disabled="#{!caseBean.editable or caseACLBean.editingAccessControlItem != null}"
      styleClass="addButton"  />
  </t:div>

  <t:div rendered="#{caseACLBean.editingAccessControlItem != null}"
    styleClass="editingPanel">

    <t:div>
      <h:outputText value="#{caseBundle.caseACL_roleId}:"
        style="width:15%" styleClass="textBox" />
      <h:inputText value="#{caseACLBean.editingAccessControlItem.accessControl.roleId}"
        style="width:40%;font-family:Courier New"
        styleClass="inputBox"
        readonly="#{!caseBean.editable}"/>
      <h:commandButton action="#{caseACLBean.searchRole}"
        styleClass="searchButton" value="#{objectBundle.search}"
        image="#{userSessionBean.icons.search}"
        alt="#{objectBundle.search}" title="#{objectBundle.search}"
        disabled="#{!caseBean.editable}"/>
    </t:div>

    <t:div>
      <h:outputText value="#{caseBundle.caseACL_action}:"
        style="width:15%" styleClass="textBox" />
      <t:selectOneMenu value="#{caseACLBean.editingAccessControlItem.accessControl.action}"
        style="width:20%;font-family:Courier New" styleClass="selectBox"
        disabled="#{!caseBean.editable}">
        <f:selectItems value="#{caseACLBean.actions}" />
      </t:selectOneMenu>
    </t:div>

    <t:div styleClass="actionsRow">
      <h:commandButton action="#{caseACLBean.storeAccessControl}"
        styleClass="addButton" value="#{objectBundle.accept}"
        disabled="#{!caseBean.editable}"/>
      <h:commandButton action="#{caseACLBean.cancelAccessControl}"
        styleClass="cancelButton" immediate="true" value="#{objectBundle.cancel}" />
    </t:div>
  </t:div>

</jsp:root>
