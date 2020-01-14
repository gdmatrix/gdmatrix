<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf ="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    errorClass="errorMessage_line" warnClass="warnMessage_line"
    infoClass="infoMessage_line"/>

  <jsp:include page="/common/obj/page_history.jsp" />

  <t:div styleClass="objectForm">

    <h:panelGrid columns="4" styleClass="navBar" title=""
      columnClasses="col1,col2,col3,col4" width="100%">

      <h:panelGroup>
        <h:graphicImage rendered="#{controllerBean.objectBean.renderObjectTypeIcon}"
          value="#{controllerBean.objectBean.objectTypeIconPath}"
          style="border:none" />
      </h:panelGroup>
      
      <sf:outputText value="#{controllerBean.objectBean.title}: "
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
      
      <sf:commandMenu binding="#{controllerBean.objectBean.commandMenu}"
        value="#{controllerBean.objectBean.objectId}"
        styleClass="selectBox" style="width:100%"
        action="#{controllerBean.objectBean.changeObject}" immediate="true">
        <f:selectItems value="#{controllerBean.objectBean.selectItems}" />
      </sf:commandMenu>
            
      <h:panelGrid columns="5" cellpadding="0" cellspacing="0">
        <h:panelGroup rendered="#{!controllerBean.objectBean.new}">
          <h:commandButton image="#{userSessionBean.icons.nofavorite}"
            rendered="#{!controllerBean.objectBean.favorite}"
            action="#{controllerBean.objectBean.markFavorite}"
            value="#{objectBundle.favorite}"
            alt="#{objectBundle.favorite}" title="#{objectBundle.favorite}"
            immediate="true" styleClass="searchButton" />
          <h:commandButton image="#{userSessionBean.icons.favorite}"
            rendered="#{controllerBean.objectBean.favorite}"
            action="#{controllerBean.objectBean.unmarkFavorite}"
            value="#{objectBundle.noFavorite}"
            alt="#{objectBundle.noFavorite}" title="#{objectBundle.noFavorite}"
            immediate="true" styleClass="searchButton" />
        </h:panelGroup>
        <h:commandButton action="#{controllerBean.searchObject}"
          value="#{objectBundle.search}" immediate="true" styleClass="searchButton"
          image="#{userSessionBean.icons.search}"
          alt="#{objectBundle.search}" title="#{objectBundle.search}" />
        <h:commandButton action="#{controllerBean.create}"
          value="#{objectBundle.create}" immediate="true" styleClass="createButton"
          image="#{userSessionBean.icons.new}"
          alt="#{objectBundle.create}" title="#{objectBundle.create}" />
        <h:commandButton action="#{controllerBean.objectBean.select}" 
          value="#{objectBundle.select}" immediate="true" styleClass="selectButton"
          image="#{userSessionBean.icons.back}"
          alt="#{objectBundle.select}" title="#{objectBundle.select}"
          rendered="#{controllerBean.selectableObject}" />
        <t:popup closePopupOnExitingElement="false"
                 closePopupOnExitingPopup="true"
                 displayAtDistanceX="-100"
                 displayAtDistanceY="-5"
                 styleClass="actionsPopup"
                 rendered="#{not empty objectActionsBean.objectActions}">
          <h:commandButton action="#{controllerBean.close}"
            value="#{objectBundle.actions}" immediate="true" styleClass="closeButton"
            image="#{userSessionBean.icons.gear}"
            alt="#{objectBundle.actions}" title="#{objectBundle.actions}" />
          <f:facet name="popup">
            <h:panelGroup>
              <h:outputText value="#{objectBundle.actions}:"
                 styleClass="popupHeader" />
                  <t:dataList value="#{objectActionsBean.objectActions}" var="action"
                    layout="unorderedList" itemStyleClass="objectAction"
                    styleClass="objectActions">
                    <h:graphicImage value="/common/obj/images/action.gif" />
                    <h:panelGroup rendered="#{action.expression != null}">
                      <h:graphicImage value="#{action.image}" alt="" title=""
                        rendered="#{action.image != null}" />
                      <t:commandLink action="#{objectActionsBean.executeAction}" onclick="showOverlay()">
                        <h:outputText value="#{action.description}" />
                      </t:commandLink>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{action.url != null}">
                      <h:graphicImage value="#{action.image}" alt="" title=""
                        rendered="#{action.image != null}" />
                      <h:outputLink value="#{action.url}" target="#{action.target}">
                        <h:outputText value="#{action.description}"/>
                      </h:outputLink>
                    </h:panelGroup>
                  </t:dataList>
            </h:panelGroup>
          </f:facet>
        </t:popup>

        <h:commandButton action="#{controllerBean.close}"
          value="#{objectBundle.close}" immediate="true" styleClass="closeButton"
          image="#{userSessionBean.icons.close}"
          alt="#{objectBundle.close}" title="#{objectBundle.close}" />
      </h:panelGrid>
      
    </h:panelGrid>

    <t:div styleClass="tabs">
      <sf:navigationMenu var="item" value="main" id="tabs"
        layout="list" mode="passive" maxVisibleMenuItems="7"
        selectedStyleClass="selected" unselectedStyleClass="unselected">
        <h:commandLink action="#{controllerBean.objectBean.showTab}"
          rendered="#{item.rendered}">
          <f:param name="tabmid" value="#{item.mid}" />
          <sf:outputText value="#{item.label}"
            translator="#{userSessionBean.translator}"
            translationGroup="#{userSessionBean.translationGroup}" />
        </h:commandLink>
      </sf:navigationMenu>
    </t:div>

    <t:div styleClass="sheet">
      <jsp:include page="${requestScope['_tab']}" />
    </t:div>

    <t:div styleClass="footer">
      <h:commandButton action="#{controllerBean.objectBean.store}" 
        value="#{objectBundle.store}" styleClass="storeButton"
        disabled="#{!controllerBean.objectBean.editable or controllerBean.objectBean.objectActionsDisabled}"
        onclick="showOverlay()"/>
      <h:commandButton action="#{controllerBean.objectBean.remove}" 
        value="#{objectBundle.delete}" immediate="true"
        onclick="return confirm('#{objectBundle.confirm_full_remove}');"
        disabled="#{!controllerBean.objectBean.editable or controllerBean.objectBean.objectActionsDisabled}"
        styleClass="removeButton"
        />
      <h:commandButton action="#{controllerBean.objectBean.cancel}" 
        value="#{objectBundle.cancel}" styleClass="cancelButton"
        immediate="true"
        disabled="#{controllerBean.objectBean.objectActionsDisabled}"/>
    </t:div>
  </t:div>

</jsp:root>
