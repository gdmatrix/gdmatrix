<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.cases.web.resources.CaseBundle"
                var="caseBundle"/>

  <sf:saveScroll value="#{caseTreeBean.scroll}" />
  <t:div styleClass="caseTree">

    <h:panelGrid columns="2" styleClass="filterPanel" summary=""
      columnClasses="column1, column2"
      headerClass="header" footerClass="footer">
      <f:facet name="header">
        <h:outputText />
      </f:facet>

      <h:outputText value="#{caseBundle.case_title}:" />
      <h:inputText value="#{caseTreeBean.inputSearchText}" style="width:85%"
                   styleClass="inputBox" />

      <h:outputText value="#{caseBundle.caseSearch_date}:" />
      <sf:calendar id="caseTreeCalendar"
                   value="#{caseTreeBean.inputDate}"
                   binding="#{caseTreeBean.htmlCalendar}"
        styleClass="calendarBox"
        buttonStyleClass="calendarButton"
        style="width:15%;margin-left:2px"/>

      <f:facet name="footer">
        <h:commandButton id="default_button" action="#{caseTreeBean.search}"
                         value="#{objectBundle.search}" styleClass="searchButton"
                         onclick="showOverlay()" />
      </f:facet>
    </h:panelGrid>  

    <t:div styleClass="inputBox" style="margin-top:12px">
      <h:panelGroup>
        <t:tree2 value="#{caseTreeBean.treeModel}" var="node" varNodeToggler="t"
                 clientSideToggle="false" preserveToggle="false">
          <f:facet name="case">
            <h:panelGroup styleClass="node">
              <t:graphicImage styleClass="icon" alt="" rendered="#{node.icon != null}" value="#{node.icon}" border="0" />
              <h:commandLink rendered="#{not node.renderPropertiesAsPopUp}"
                             styleClass="#{node.found ? 'foundCaseLink' : 'caseLink'}"
                             action="#{caseTreeBean.showCase}">
                <h:outputText styleClass="#{node.styleClass}"
                              style="#{node.selected ? 'font-weight:bold' : ''}"
                              value="#{node.description}" />
              </h:commandLink>

              <t:popup closePopupOnExitingElement="true"
                closePopupOnExitingPopup="true"
                displayAtDistanceX="20"
                displayAtDistanceY="-5"
                styleClass="propertiesPopUp"
                rendered="#{node.renderPropertiesAsPopUp}">
                <h:commandLink styleClass="#{node.found ? 'foundCaseLink' : 'caseLink'}" action="#{caseTreeBean.showCase}">
                  <h:outputText styleClass="#{node.styleClass}"
                                style="#{node.selected ? 'font-weight:bold' : ''}"
                                value="#{node.description}" />
                </h:commandLink>
                <f:facet name="popup">
                  <t:dataList value="#{node.renderedProperties}"
                              var="property">
                    <t:div rendered="#{property.icon == null}" styleClass="propertyText">
                      <h:outputText rendered="#{property.label != null}" 
                                    value="#{property.label}: #{property.value}" />
                      <h:outputText rendered="#{property.label == null}" 
                                    value="#{property.value}" />                    
                    </t:div>
                    <t:div rendered="#{property.icon != null}" styleClass="propertyIcon">
                      <h:graphicImage url="#{property.icon}" />
                    </t:div>             
                  </t:dataList>                  
                </f:facet>
              </t:popup>

              <t:div styleClass="propertyList" 
                     rendered="#{node.renderPropertiesAsList}">
                <t:dataList value="#{node.renderedProperties}"
                            var="property">
                  <t:div rendered="#{property.icon == null}" styleClass="propertyText">
                    <h:outputText rendered="#{property.label != null}" 
                                  value="#{property.label}: #{property.value}" />
                    <h:outputText rendered="#{property.label == null}" 
                                  value="#{property.value}" />                    
                  </t:div>
                  <t:div rendered="#{property.icon != null}" styleClass="propertyIcon">
                    <h:graphicImage url="#{property.icon}" />
                  </t:div>             
                </t:dataList>
              </t:div>
                  
            </h:panelGroup>
          </f:facet>
        </t:tree2>
      </h:panelGroup>
    </t:div>

    <t:div styleClass="actionsBar">
       <h:commandButton value="#{objectBundle.update}" image="#{userSessionBean.icons.update}" alt="#{objectBundle.update}" title="#{objectBundle.update}"
         action="#{caseTreeBean.update}" immediate="true"
         styleClass="currentButton" rendered="#{caseTreeBean.renderUpdateButton}" />
       <h:commandButton value="#{objectBundle.current}" image="#{userSessionBean.icons.current}" alt="#{objectBundle.current}" title="#{objectBundle.current}"
         action="#{caseBean.show}" immediate="true"
         styleClass="currentButton" />
       <h:commandButton value="#{objectBundle.create}" image="#{userSessionBean.icons.new}" alt="#{objectBundle.create}" title="#{objectBundle.create}"
         action="#{caseBean.create}" immediate="true"
         styleClass="createButton" />
    </t:div>

  </t:div>
</jsp:root>
