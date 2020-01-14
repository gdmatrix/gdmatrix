<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
                var="objectBundle" />

  <f:loadBundle basename="org.santfeliu.security.web.resources.SecurityBundle"
                var="securityBundle" />
  
  <t:buffer into="#{table}">
    <t:dataTable id="data" value="#{userRolesBean.rows}" var="row"
                 rowStyleClass="#{userRolesBean.editingRole == null ? null :
                   (userRolesBean.editingRole.roleId == row.role.roleId ? 'selectedRow' : null)}"
                 rowClasses="row1,row2" headerClass="header" footerClass="footer"
                 styleClass="resultList" style="width:100%"
                 bodyStyle="#{empty userRolesBean.rows ? 'display:none' : ''}"
                 rendered="#{!userRolesBean.new}"
                 rows="#{userRolesBean.pageSize}"
                 first="#{userRolesBean.firstRowIndex}">
    <t:column style="width:20%">
      <f:facet name="header">
        <h:outputText value="#{securityBundle.role_role}:" />
      </f:facet>
      <h:outputText styleClass="#{userRolesBean.userInRoleStyleClass}"
                    value="#{row.role.roleId}" />
    </t:column>
    <t:column style="width:50%">
      <f:facet name="header">
        <h:outputText value="#{securityBundle.role_name}:" />
      </f:facet>
      <h:outputText styleClass="#{userRolesBean.userInRoleStyleClass}"
                    value="#{row.role.name}" />
    </t:column>
    <t:column style="width:30%;text-align:right;">
      <h:panelGroup>
        <h:commandButton value="#{objectBundle.show}"
          image="#{userSessionBean.icons.show}"
          alt="#{objectBundle.show}" title="#{objectBundle.show}"
          action="#{userRolesBean.showUserInRole}"
          styleClass="showButton"  />
        <h:commandButton value="#{objectBundle.edit}"
          image="#{userSessionBean.icons.detail}"
          alt="#{objectBundle.edit}" title="#{objectBundle.edit}"
          action="#{userRolesBean.editUserInRole}"
          styleClass="addButton"  />
        <h:commandButton value="#{objectBundle.delete}"
          image="#{userSessionBean.icons.delete}"
          alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
          action="#{userRolesBean.removeUserInRole}"
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

  <t:div styleClass="resultBar" rendered="#{userRolesBean.rowCount > 0}">
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

  <t:div style="width:100%;text-align:right">
    <h:commandButton action="#{userRolesBean.createUserInRole}"
                     disabled="#{userRolesBean.editingRole != null}"
                     styleClass="addButton" value="#{objectBundle.add}"
                     image="#{userSessionBean.icons.add}"
                     alt="#{objectBundle.add}" title="#{objectBundle.add}"      />
  </t:div>

  <t:div styleClass="editingPanel" rendered="#{userRolesBean.editingRole != null}">

    <t:div>
      <h:outputText value="#{securityBundle.role_role}:" styleClass="textBox"
                    style="width:18%" />
      <h:panelGroup>
        <h:inputText value="#{userRolesBean.editingRole.roleId}"
          style="width:200px" styleClass="selectBox" />
        <h:commandButton action="#{userRolesBean.searchRole}"
          styleClass="searchButton" value="#{objectBundle.search}"
          image="#{userSessionBean.icons.search}"
          alt="#{objectBundle.search}" title="#{objectBundle.search}"/>
      </h:panelGroup>
    </t:div>

    <t:div>
      <h:outputText value="#{securityBundle.userRoles_comments}:"
        styleClass="textBox" style="width:18%;vertical-align:top" />
      <h:inputTextarea value="#{userRolesBean.editingRole.comments}"
        styleClass="inputBox" style="width:79%" rows="3"
        onkeypress="checkMaxLength(this, 1000)" />
    </t:div>

    <t:div>
      <h:outputText value="#{securityBundle.userRoles_startDate}:"
        styleClass="textBox" style="width:18%" />
      <sf:calendar value="#{userRolesBean.editingRole.startDate}"
        style="width:80px" styleClass="calendarBox" buttonStyleClass="calendarButton" />
    </t:div>

    <t:div>
      <h:outputText value="#{securityBundle.userRoles_endDate}:"
        styleClass="textBox" style="width:18%" />
      <sf:calendar value="#{userRolesBean.editingRole.endDate}"
        style="width:80px" styleClass="calendarBox" buttonStyleClass="calendarButton" />
    </t:div>

    <t:div styleClass="actionsRow">
      <h:commandButton action="#{userRolesBean.storeUserInRole}"
                       styleClass="addButton" value="#{objectBundle.store}" />
      <h:commandButton action="#{userRolesBean.cancelUserInRole}"
                       styleClass="cancelButton" value="#{objectBundle.cancel}" />
    </t:div>

  </t:div>

</jsp:root>
