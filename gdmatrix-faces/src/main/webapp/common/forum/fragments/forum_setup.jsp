<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.forum.web.resources.ForumBundle" var="forumBundle" />
<f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" var="objectBundle" />

  <t:saveState value="#{forumCatalogueBean}" />

  <t:div styleClass="objectForm">
    <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
      showSummary="true"
      warnClass="warnMessage"
      errorClass="errorMessage"
      fatalClass="fatalMessage" />

    <t:div styleClass="sheet">

      <t:div rendered="#{forumCatalogueBean.currentForumView.forum.forumId != null}">
        <h:outputText value="#{forumBundle.forumId}:"
          style="width:18%" styleClass="textBox"/>
        <h:outputText value="#{forumCatalogueBean.currentForumView.forum.forumId}"
          styleClass="outputBox" style="width:10%" />
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.name}:"
          style="width:18%" styleClass="textBox"/>
        <h:inputText value="#{forumCatalogueBean.currentForumView.forum.name}"
          styleClass="inputBox" style="width:78%" />
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.description}:"
          style="width:18%" styleClass="textBox"/>
        <h:inputText value="#{forumCatalogueBean.currentForumView.forum.description}"
          styleClass="inputBox" style="width:78%" />
      </t:div>


      <t:div>
        <h:outputText value="#{forumBundle.type}:" styleClass="textBox"
          style="width:18%" />
        <t:selectOneMenu styleClass="selectBox"
          value="#{forumCatalogueBean.currentForumView.forum.type}">
          <f:selectItems value="#{forumCatalogueBean.forumTypeItems}" />
          <f:converter converterId="EnumConverter" />
          <f:attribute name="enum" value="org.matrix.forum.ForumType" />
        </t:selectOneMenu>
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.startDate}:"
          style="width:18%"
          styleClass="textBox"/>

        <sf:calendar value="#{forumCatalogueBean.currentForumView.forum.startDateTime}"
          styleClass="calendarBox"
          externalFormat="dd/MM/yyyy|HH:mm:ss"
          internalFormat="yyyyMMddHHmmss"
          buttonStyleClass="calendarButton"
          style="width:14%;"/>
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.endDate}:"
          styleClass="textBox"
          style="width:18%"/>

        <sf:calendar value="#{forumCatalogueBean.currentForumView.forum.endDateTime}"
          styleClass="calendarBox"
          externalFormat="dd/MM/yyyy|HH:mm:ss"
          internalFormat="yyyyMMddHHmmss"
          buttonStyleClass="calendarButton"
          style="width:14%;"/>
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.adminRole}:" styleClass="textBox"
          style="width:18%" />
        <h:inputText value="#{forumCatalogueBean.currentForumView.forum.adminRoleId}"
          styleClass="inputBox" />
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.emailFrom}:" styleClass="textBox"
          style="width:18%" />
        <h:inputText value="#{forumCatalogueBean.currentForumView.forum.emailFrom}"
          styleClass="inputBox" style="width:50%" />
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.emailTo}:" styleClass="textBox"
          style="width:18%" />
        <h:inputText value="#{forumCatalogueBean.currentForumView.forum.emailTo}"
          styleClass="inputBox" style="width:50%"/>
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.maxQuestions}:"
          style="width:18%" styleClass="textBox"/>
        <h:inputText value="#{forumCatalogueBean.currentForumView.forum.maxQuestions}"
          styleClass="inputBox" style="width:10%" />
      </t:div>

      <t:div>
        <h:outputText value="#{forumBundle.group}:"
          style="width:18%" styleClass="textBox"/>
        <h:inputText value="#{forumCatalogueBean.currentForumView.forum.group}"
          styleClass="inputBox" style="width:50%" />
      </t:div>
    </t:div>

    <t:div styleClass="footer">
      <h:commandButton action="#{forumCatalogueBean.storeForum}"
        value="#{objectBundle.store}" styleClass="storeButton"/>
      <h:commandButton action="#{forumCatalogueBean.removeForum}"
        value="#{objectBundle.delete}" immediate="true"
        onclick="return confirm('#{objectBundle.confirm_remove}')"
        styleClass="removeButton" />
      <h:commandButton action="#{forumCatalogueBean.cancelForum}"
        value="#{objectBundle.cancel}" styleClass="cancelButton"
        immediate="true" />
    </t:div>

  </t:div>

</jsp:root>
