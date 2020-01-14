<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf ="http://www.santfeliu.org/jsf">

 <h:panelGrid columns="1"  width ="100%" styleClass="tabs">
   <h:panelGrid columns="2" width="100%" styleClass="tabs_header" columnClasses="c1,c2">
     <h:panelGroup>
        <h:outputText   value="#{objectController.objectName}" 
                    styleClass="objectName"/>

        <sf:selectobject  
                      size = "1" styleClass="selectObject"
                      action="#{objectController.change}"
                      submitOnchange="true"
                      immediate="true"
                      store="#{objectController.changedObject}"
                      itemValue="#{objectController.firstElement}"
                      listValues = "#{objectController.currentObjectList}" 
        />
     </h:panelGroup>
     <h:panelGroup>
          <h:commandLink styleClass="commandLink" 
                       action="#{objectController.search}" immediate="true">
                <h:graphicImage url="/common/object/images/buscar.gif" />
          </h:commandLink>
  
          <h:commandLink  styleClass="commandLink"
                        action="#{objectController.reset}" immediate="true"
                        rendered="#{objectController.rolesOwner}">
              <h:graphicImage url="/common/object/images/afegir.gif"  />
          </h:commandLink>
      
          <h:commandLink styleClass="commandLink"
                       action="#{objectController.back}" immediate="true"
                       rendered="#{objectController.backRendered}">
              <h:graphicImage url="/common/object/images/tornar.gif" />
          </h:commandLink>
     </h:panelGroup>    
  </h:panelGrid>
   <h:panelGrid cellspacing="0" cellpadding="4"  styleClass="tabs_hmenuRow">
             
       <sf:navigationMenu  id="tabsmenu"
                           value="main" 
                           var="item"    
                           orientation="horizontal"
                           rendered="#{userSessionBean.selectedMenuItem.parent.childCount > 1 and 
                                       userSessionBean.selectedMenuItem.depth > 3 || 
                                       (userSessionBean.selectedMenuItem.childCount > 0  
                                       and userSessionBean.selectedMenuItem.depth == 3)}"
                           styleClass="tabs_hmenu"
                           style="width:100%"
                           selectedStyleClass="tabs_hmenu_selected" 
                           unselectedStyleClass="tabs_hmenu_unselected">
          <h:outputText value="#{item.label}"/>
       </sf:navigationMenu>
   </h:panelGrid>
  

   <h:panelGrid id="tabbody" columns="1" width="100%">
    <jsp:include page="${requestScope['_tab']}"/>
  </h:panelGrid>  
   <t:saveState value="#{objectController.stack}" />
   <t:saveState value="#{objectController.backMap}" />
   <t:saveState value="#{objectController.history}" />
   <t:saveState value="#{objectController.changedObject}" />
 </h:panelGrid>

    
</jsp:root>