<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.edu.web.resources.EducationBundle" 
    var="educationBundle" />

  <t:div>
    <h:outputText value="#{educationBundle.inscription_code}:" 
      styleClass="textBox" style="width:18%" />
    <h:inputText value="#{inscriptionMainBean.inscription.code}" 
      styleClass="inputBox" />
  </t:div>

  <t:div>
    <h:outputText value="#{educationBundle.inscription_person}:" 
      styleClass="textBox" style="width:18%" />
    <t:selectOneMenu value="#{inscriptionMainBean.inscription.personId}" 
      styleClass="selectBox">
      <f:selectItems value="#{inscriptionMainBean.personSelectItems}" />
    </t:selectOneMenu>
    <h:commandButton action="#{inscriptionMainBean.searchPerson}" 
      value="#{objectBundle.search}" styleClass="searchButton"
      immediate="true"/>
    <h:commandButton action="#{inscriptionMainBean.showPerson}" 
      value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"
      immediate="true"/>
  </t:div>

  <t:div>
    <h:outputText value="#{educationBundle.inscription_course}:" 
      styleClass="textBox" style="width:18%" />
    <t:selectOneMenu value="#{inscriptionMainBean.inscription.courseId}" 
      styleClass="selectBox">
      <f:selectItems value="#{inscriptionMainBean.courseSelectItems}" />
    </t:selectOneMenu>
    <h:commandButton action="#{inscriptionMainBean.searchCourse}" 
      value="#{objectBundle.search}" styleClass="searchButton"
      immediate="true"/>
    <h:commandButton action="#{inscriptionMainBean.showCourse}" 
      value="#{objectBundle.show}" 
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"
      immediate="true"/>
  </t:div>

  <t:div>
    <h:outputText value="#{educationBundle.inscription_start_date}:" 
      styleClass="textBox" style="width:18%" />
    <sf:calendar value="#{inscriptionMainBean.inscription.startDate}" 
      styleClass="calendarBox"
      buttonStyleClass="calendarButton" />
  </t:div>

  <t:div>
    <h:outputText value="#{educationBundle.inscription_end_date}:" 
      styleClass="textBox" style="width:18%" />
    <sf:calendar value="#{inscriptionMainBean.inscription.endDate}" 
      styleClass="calendarBox"
      buttonStyleClass="calendarButton" />
  </t:div>

  <sf:customForm url="#{inscriptionMainBean.formUrl}"
    values="#{inscriptionMainBean.properties}" 
    newValues="#{inscriptionMainBean.properties}" />
</jsp:root>