<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
          
  <f:loadBundle basename="org.santfeliu.agenda.web.resources.AgendaBundle" var="agendaBundle"/>
  <f:loadBundle basename="org.santfeliu.web.resources.WebBundle" var="webBundle"/>
  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />

  <t:saveState value="#{meetingFinderBean}" />
  
  <sf:saveScroll />    

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
              errorClass="errorMessage" warnClass="warnMessage"
              infoClass="infoMessage" fatalClass="fatalMessage"
              showSummary="true" />
  <t:div styleClass="objectSearch">
    <t:div styleClass="header">
      <sf:outputText value="#{controllerBean.searchBean.title}:"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}"
        style="text-align:left" />    
    </t:div>
    <t:div styleClass="meetingFinderPanel">
      <t:div styleClass="filterPanel">
      
        <t:div styleClass="panel">
          <t:div styleClass="panelTitle">
            <h:graphicImage value="/common/agenda/images/person.png"/>                                                        
            <h:outputText value=" #{agendaBundle.meetingFinder_attendants} " />
          </t:div>

          <t:div styleClass="panelBody">
            <h:outputLabel for="personId" value="#{agendaBundle.attendants_person}: "
                          style="width:15%" styleClass="textBox"/>
            <h:panelGroup>
              <sf:commandMenu id="personId" value="#{meetingFinderBean.selectedAttendant.personId}"
                              styleClass="selectBox" style="width:79%"
                              action="#{meetingFinderBean.addAttendant}">
                <f:selectItems value="#{meetingFinderBean.personSelectItems}" />
              </sf:commandMenu>                                    
              <h:commandButton action="#{meetingFinderBean.searchPerson}"
                               styleClass="searchButton" value="#{objectBundle.search}"
                               image="#{userSessionBean.icons.search}"
                               alt="#{objectBundle.search}" title="#{objectBundle.search}" />
            </h:panelGroup>

            <t:buffer into="#{table}">
              <t:dataTable id="attData" value="#{meetingFinderBean.attendants}" var="att"
                           rowClasses="row1,row2" headerClass="header" footerClass="footer"
                           bodyStyle="#{empty meetingFinderBean.attendants ? 'display:none' : ''}"
                           styleClass="resultList attList" style="width:100%"
                           rendered="#{not empty meetingFinderBean.attendants}" >

                <t:column>
                  <f:facet name="header">
                    <h:outputText value="#{agendaBundle.attendants_person}" />
                  </f:facet>
                  <h:outputText value="#{att.fullName}"  />
                </t:column>

                <t:column styleClass="actionsColumn">
                  <h:commandButton value="#{objectBundle.delete}"
                                   image="#{userSessionBean.icons.delete}"
                                   alt="#{objectBundle.delete}" title="#{objectBundle.delete}"
                                   action="#{meetingFinderBean.removeAttendant}"
                                   rendered="#{att.personId != null}"
                                   styleClass="removeButton"
                                   onclick="return confirm('#{objectBundle.confirm_remove}');"/>
                </t:column>
              </t:dataTable>
            </t:buffer>

            <h:outputText value="#{table}" escape="false"/>
          </t:div>           
        </t:div>

        <t:div styleClass="panel">
            <t:div styleClass="panelTitle">
              <h:graphicImage value="/common/agenda/images/calendar.png" />                                                  
              <h:outputText value=" #{agendaBundle.meetingFinder_period} " />

            </t:div>

            <t:div styleClass="panelBody">
              <t:div style="width:100%;display:inline-block" >
                <t:div style="width:100%;float:left">
                  <t:div>
                    <h:outputLabel for="startsOn" value="#{agendaBundle.meetingFinder_startsOn}" />
                    <sf:calendar id="startsOn" value="#{meetingFinderBean.rangeStartDateTime}"
                      styleClass="calendarBox"
                      externalFormat="dd/MM/yyyy"
                      internalFormat="yyyyMMddHHmmss"
                      buttonStyleClass="calendarButton"
                      style="width:70px" />
                    <h:outputLabel for="endsOn" value="#{agendaBundle.meetingFinder_endsOn}" />                  
                    <sf:calendar id="endsOn" value="#{meetingFinderBean.rangeEndDateTime}"
                      styleClass="calendarBox"
                      externalFormat="dd/MM/yyyy"
                      internalFormat="yyyyMMddHHmmss"
                      buttonStyleClass="calendarButton"
                      style="width:70px"  />
                  </t:div>
                </t:div>
              </t:div>

              <t:div style="width:100%;display:inline-block" >
                <t:div style="width:100%;float:left;padding:2px">
                  <h:outputLabel for="startHour" value="#{agendaBundle.meetingFinder_startHour} " />
                  <h:inputText id="startHour" value="#{meetingFinderBean.startHour}" styleClass="inputBox"
                               style="width:35px;text-align:right">
                    <t:validateRegExpr pattern="([01]?[0-9]|2[0-3])(:[0-5][0-9])?" />
                  </h:inputText>
                  <h:outputLabel for="endHour" value=" #{agendaBundle.meetingFinder_endHour} " />
                  <h:inputText id="endHour" value="#{meetingFinderBean.endHour}"  styleClass="inputBox"
                               style="width:35px;text-align:right">
                    <t:validateRegExpr pattern="([01]?[0-9]|2[0-3])(:[0-5][0-9])?" />
                  </h:inputText>
                </t:div>
                <t:div style="width:100%;float:left;padding:2px">
                  <h:outputLabel for="duration" value="#{agendaBundle.meetingFinder_duration}: " />
                  <h:inputText id="duration" value="#{meetingFinderBean.duration}"  styleClass="inputBox"
                    style="width:30px;text-align:right">
                    <f:validateLongRange minimum="1" />
                  </h:inputText>
                  <t:selectOneMenu value="#{meetingFinderBean.durationSelector}"
                    style="vertical-align:middle" styleClass="selectBox" title="#{agendaBundle.meetingFinder_durationSelector}">
                    <f:selectItem itemValue="m" itemLabel="minuts" />
                    <f:selectItem itemValue="h" itemLabel="hores" />
                  </t:selectOneMenu>
                </t:div>
                <t:div style="width:100%;float:left;padding:2px">
                  <h:outputLabel for="steps" value="#{agendaBundle.meetingFinder_step}: " />
                  <h:inputText id="steps" value="#{meetingFinderBean.steps}"  styleClass="inputBox"
                    style="width:30px;text-align:right">
                    <f:validateLongRange minimum="1" />
                  </h:inputText>
                  <h:outputText value="#{agendaBundle.meetingFinder_stepMinutes} " />                  
                </t:div>
                <t:div style="width:100%;float:left;padding:2px">
                  <h:outputLabel for="timePadding" value="#{agendaBundle.meetingFinder_timePadding}: " />
                  <h:inputText id="timePadding" value="#{meetingFinderBean.timePadding}"  styleClass="inputBox"
                    style="width:30px;text-align:right">
                    <f:validateLongRange minimum="0" />
                  </h:inputText>
                  <h:outputText value="#{agendaBundle.meetingFinder_stepMinutes} " />                  
                </t:div>                
              </t:div>
            </t:div>          
        </t:div> 

        <t:div styleClass="panel">
          <t:div styleClass="panelTitle">
            <h:graphicImage value="/common/agenda/images/place.png" />                                                                      
            
            <h:outputText value=" #{agendaBundle.meetingFinder_rooms} " />
          </t:div>
          <t:div styleClass="panelBody">
            <t:buffer into="#{table2}">
              <t:div style="width:100%; text-align: right">
              <h:commandButton action="#{meetingFinderBean.searchRoom}"
                               styleClass="searchButton" value="#{objectBundle.search}"
                               image="#{userSessionBean.icons.search}"
                               alt="#{objectBundle.search}" title="#{objectBundle.search}" 
                               />              
              </t:div>

              <t:dataTable id="roomData" value="#{meetingFinderBean.rooms}" var="room"
                           rowClasses="row1,row2" headerClass="header" footerClass="footer"
                           columnClasses="col1,col2"
                           bodyStyle="#{empty meetingFinderBean.rooms ? 'display:none' : ''}"
                           styleClass="resultList roomList" style="width:100%"
                           rendered="#{not empty meetingFinderBean.rooms}">



                <t:column>
                  <f:facet name="header">
                    <h:outputText value="#{agendaBundle.meetingFinder_roomDescription}" />
                  </f:facet>
                  <h:outputLabel for="roomSelection" value="#{room.description}" />
                </t:column>

                <t:column>
                    <f:facet name="header">
                      <h:outputText value="#{agendaBundle.meetingFinder_checkRoom}" />
                    </f:facet>
                    <h:selectBooleanCheckbox id="roomSelection" value="#{room.selected}" />
                </t:column>
              </t:dataTable>
            </t:buffer>

            <h:outputText value="#{table2}" escape="false"/>  
          
          </t:div>
        </t:div>                   

        <t:div styleClass="footer">
          <h:commandButton value="#{objectBundle.search}"
                           alt="#{objectBundle.search}" title="#{objectBundle.search}"
                           action="#{meetingFinderBean.findFreeSlots}"
                           styleClass="searchButton"
                           onclick="var r = confirm('#{agendaBundle.confirmFindFreeSlots}');if (r) showOverlay();return r"/>
        </t:div>  
      </t:div> 
      
 

      <t:dataTable id="slotsData" value="#{meetingFinderBean.freeSlots}" var="slot"
                    rowClasses="row1,row2" headerClass="header" footerClass="footer"
                    columnClasses="col1,col2,col3,col4"
                    bodyStyle="#{empty meetingFinderBean.freeSlots ? 'display:none' : ''}"
                    styleClass="resultList" style="width:100%" 
                    rows="#{meetingFinderBean.pageSize}" first="#{meetingFinderBean.firstRowIndex}"
                    rendered="#{not empty meetingFinderBean.freeSlots}">
         <t:column>
           <f:facet name="header">
             <h:outputText value="#{agendaBundle.event_startDate}" />
           </f:facet>
           <h:outputText value="#{slot.startDateTime}" />
         </t:column>

         <t:column>
           <f:facet name="header">
             <h:outputText value="#{agendaBundle.event_endDate}" />
           </f:facet>
           <h:outputText value="#{slot.endDateTime}" />
         </t:column>       
         
        <t:column>
          <f:facet name="header">
            <h:outputText value="#{agendaBundle.meetingFinder_roomAvailibility}" />
          </f:facet>
          <h:graphicImage value="/common/agenda/images/cross.png"
            rendered="#{empty slot.availableRooms and (meetingFinderBean.editingSlot == null or meetingFinderBean.editingSlot != slot)}"
            alt="#{agendaBundle.meetingFinder_roomNotAvailable}" title="#{agendaBundle.meetingFinder_roomNotAvailable}"/>

          <t:popup closePopupOnExitingElement="false"
                   closePopupOnExitingPopup="true"
                   displayAtDistanceX="-50"
                   displayAtDistanceY="-5" 
                   styleClass="actionsPopup"
                   rendered="#{meetingFinderBean.editingSlot == null or meetingFinderBean.editingSlot != slot}">
            <h:graphicImage value="/common/agenda/images/accept.png"
                            rendered="#{not empty slot.availableRooms}"/> 
            <f:facet name="popup">
              <h:panelGroup>
                <t:dataList value="#{slot.availableRooms}" var="oe" layout="unorderedList" style="text-align: left">
                  <h:outputText value="#{oe}" />
                </t:dataList>
              </h:panelGroup>
            </f:facet>
          </t:popup>

          <t:selectOneMenu value="#{meetingFinderBean.selectedRoom}"
            rendered="#{meetingFinderBean.editingSlot != null and meetingFinderBean.editingSlot == slot}"
            styleClass="selectBox" style="width:95%">
            <f:selectItems value="#{slot.availableRooms}" />
          </t:selectOneMenu>           

        </t:column>  
              
        <t:column>
          <f:facet name="header">
            <h:outputText value="#{objectBundle_actions}" />
          </f:facet>
          <h:commandButton action="#{meetingFinderBean.createEvent}"
           rendered="#{meetingFinderBean.editingSlot == null or meetingFinderBean.editingSlot != slot}"
           styleClass="addButton" value="#{objectBundle.create}"
           alt="#{objectBundle.create}"
           onclick="var r = confirm('#{agendaBundle.confirmStoreEvent}');if (r) showOverlay();return r" />           
          <h:commandButton action="#{meetingFinderBean.createEvent}"
           rendered="#{meetingFinderBean.editingSlot != null and meetingFinderBean.editingSlot == slot}"                           
           styleClass="addButton" value="#{objectBundle.store}"
           alt="#{objectBundle.create}"
           onclick="var r = confirm('#{agendaBundle.confirmStoreEvent}');if (r) showOverlay();return r" />            
          <h:commandButton action="#{meetingFinderBean.cancelEvent}"
           rendered="#{meetingFinderBean.editingSlot != null and meetingFinderBean.editingSlot == slot}"
           styleClass="addButton" value="#{objectBundle.cancel}"
           alt="#{objectBundle.cancel}"
           onclick="showOverlay()"/>            
        </t:column>                 

      </t:dataTable>
      
     <t:div styleClass="resultBar" rendered="#{meetingFinderBean.freeSlots != null}">
      
     <sf:outputText value="#{objectBundle.no_results_found}"
                  rendered="#{empty meetingFinderBean.freeSlots}" 
                  translator="#{caseSearchBean.translator}"
                  translationGroup="#{userSessionBean.translationGroup}" /> 
     </t:div>
     
      <t:dataScroller for="slotsData"
                      fastStep="100"
                      paginator="true"
                      paginatorMaxPages="9"
                      immediate="true"
                      rendered="#{meetingFinderBean.freeSlots != null and !empty meetingFinderBean.freeSlots}"
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
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/first.png" alt="#{objectBundle.first}" title="#{objectBundle.first}"/>
        </f:facet>
        <f:facet name="last">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/last.png" alt="#{objectBundle.last}" title="#{objectBundle.last}"/>
        </f:facet>
        <f:facet name="previous">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/previous.png" alt="#{objectBundle.previous}" title="#{objectBundle.previous}"/>
        </f:facet>
        <f:facet name="next">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/next.png" alt="#{objectBundle.next}" title="#{objectBundle.next}"/>
        </f:facet>
        <f:facet name="fastrewind">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/fastrewind.png" alt="#{objectBundle.fastRewind}" title="#{objectBundle.fastRewind}"/>
        </f:facet>
        <f:facet name="fastforward">
          <h:graphicImage value="/themes/#{userSessionBean.theme}/images/fastforward.png" alt="#{objectBundle.fastForward}" title="#{objectBundle.fastForward}"/>
        </f:facet>
      </t:dataScroller>       

    </t:div>
  </t:div>
</jsp:root>
