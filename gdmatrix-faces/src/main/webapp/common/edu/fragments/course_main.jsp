<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.edu.web.resources.EducationBundle" 
    var="educationBundle" />

  <t:div>
    <h:outputText value="#{educationBundle.course_code}:" 
      styleClass="textBox" style="width:18%" />
    <h:inputText value="#{courseMainBean.course.code}" 
      styleClass="inputBox" />
  </t:div>

  <t:div>
    <h:outputText value="#{educationBundle.school_name}:" 
      styleClass="textBox" style="width:18%" />
    <t:selectOneMenu value="#{courseMainBean.course.schoolId}" 
      styleClass="selectBox">
      <f:selectItems value="#{courseMainBean.schoolSelectItems}" />
    </t:selectOneMenu>
  </t:div>

  <t:div>
    <h:outputText value="#{educationBundle.course_name}:" 
      styleClass="textBox" style="width:18%" />
    <h:inputText value="#{courseMainBean.course.name}" 
      styleClass="inputBox" />
  </t:div>

</jsp:root>