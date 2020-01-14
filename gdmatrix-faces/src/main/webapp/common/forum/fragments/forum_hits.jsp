<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

<f:loadBundle basename="org.santfeliu.forum.web.resources.ForumBundle" var="forumBundle" />
<f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" var="objectBundle" />

  <t:saveState value="#{forumCatalogueBean}" />

  <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
    showSummary="true"
    warnClass="warnMessage"
    errorClass="errorMessage"
    fatalClass="fatalMessage" />

  <t:div styleClass="objectSearch">

  <t:div rendered="#{userSessionBean.selectedMenuItem.properties.questionsHeaderDocId!=null}"
               styleClass="headerDocument">
    <sf:browser url="/documents/#{userSessionBean.selectedMenuItem.properties.questionsHeaderDocId}"
      port="#{applicationBean.defaultPort}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}"
      rendered="#{userSessionBean.selectedMenuItem.properties.questionsHeaderDocId!=null}"/>
  </t:div>

  <t:div styleClass="forumName" style="float:left">
    <h:outputText value="#{forumCatalogueBean.currentForumView.forum.name}" />
  </t:div>
  <t:div styleClass="topButtons">
    <h:commandButton value="#{objectBundle.close}"
      styleClass="searchButton" action="#{forumCatalogueBean.cancelForumHits}"
      rendered="#{not forumCatalogueBean.singleForum}"/>
  </t:div>

  <t:dataTable id="data"
    value="#{forumCatalogueBean.forumHitList}" var="row"
    styleClass="resultList" summary="results"
    rowClasses="row1,row2" headerClass="header"
    bodyStyle="#{empty forumCatalogueBean.forumHitList ? 'display:none' : ''}"
    footerClass="footer">
    <t:column style="border-right:0px;vertical-align:top" width="3%">
      <h:graphicImage value="/common/forum/images/user.png" alt="user" style="margin-top:2px"/>
    </t:column>
    <t:column style="width:80%">
      <f:facet name="header">
        <h:outputLabel value="#{forumBundle.user}" />
      </f:facet>
      <t:div styleClass="userName">
        <h:outputText value="#{row.userId} " style="font-weight: bold"/>
      </t:div>
      <t:div styleClass="time" >
        <h:outputText value="Connectat fa #{row.timeGap} segons" />
      </t:div>
    </t:column>
  </t:dataTable>



</t:div>

</jsp:root>
