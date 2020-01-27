<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.forum.web.resources.ForumBundle" var="forumBundle" />
  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" var="objectBundle" />

  <sf:saveScroll value="#{forumCatalogueBean.scroll}" resetIfError="true" />
  <t:saveState value="#{forumCatalogueBean}" />
  <t:div style="margin:5px">
    <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
      showSummary="true"
      infoClass="infoMessage"
      warnClass="warnMessage"
      errorClass="errorMessage"
      fatalClass="fatalMessage" />
  </t:div>

  <sf:commandTimer action="#{forumCatalogueBean.searchQuestions}"
    time="#{forumCatalogueBean.refreshTime}"
    rendered="#{forumCatalogueBean.currentForumView.status == 'OPEN'}"
    enabled="document.forms[0]['mainform:questionText'] == null ? true :
      document.forms[0]['mainform:questionText'].value.length == 0" />

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
    <h:commandButton value="#{forumCatalogueBean.setupButtonLabel}"
      styleClass="searchButton" action="#{forumCatalogueBean.setupCurrentForum}"
      rendered="#{forumCatalogueBean.editorUser}"/>
    <h:commandButton value="#{forumCatalogueBean.otherForumsLabel}"
      styleClass="searchButton" action="#{forumCatalogueBean.show}"
      rendered="#{not forumCatalogueBean.singleForum}"/>
  </t:div>

  <t:div>
    <h:commandLink action="#{forumCatalogueBean.showForumHits}"
      styleClass="Link" 
      rendered="#{forumCatalogueBean.editorUser}">
      <h:outputText value="#{forumBundle.userConnected}: #{forumCatalogueBean.forumHits}" />
    </h:commandLink>
    <h:outputText value="#{forumBundle.userConnected}: #{forumCatalogueBean.forumHits}"
      rendered="#{not forumCatalogueBean.editorUser}"/>
  </t:div>
    
  <t:div styleClass="#{forumCatalogueBean.readOnly ? 'closedStatusMessage' : 'openStatusMessage'}">
    <sf:outputText value="#{forumCatalogueBean.statusLabel}"
      translator="#{userSessionBean.translator}"
      translationGroup="#{userSessionBean.translationGroup}"/>
  </t:div>

  <h:panelGrid columns="1" styleClass="filterPanel" style="margin-top:3px;width:100%;"
    columnClasses="column"
    headerClass="header" footerClass="footer">
    <t:div>
      <h:outputLabel value="#{forumBundle.show} " for="showAnsweredSelect" />
      <t:selectOneMenu id="showAnsweredSelect" styleClass="selectBox"
        value="#{forumCatalogueBean.questionFilter.showAnswered}"
        valueChangeListener="#{forumCatalogueBean.processQuestionFilterValueChange}">
        <f:selectItem itemValue="" itemLabel="#{forumBundle.showAnsweredAll}" />
        <f:selectItems value="#{forumCatalogueBean.showAnsweredItems}" />
        <f:converter converterId="EnumConverter" />
        <f:attribute name="enum" value="org.matrix.forum.ShowAnswered" />
      </t:selectOneMenu>

      <t:selectOneMenu styleClass="selectBox"
        value="#{forumCatalogueBean.questionFilter.showVisible}"
        rendered="#{forumCatalogueBean.editorUser}"
        valueChangeListener="#{forumCatalogueBean.processQuestionFilterValueChange}">
        <f:selectItem itemValue="" itemLabel="#{forumBundle.showVisibleAll}" />
        <f:selectItems value="#{forumCatalogueBean.showVisibleItems}" />
        <f:converter converterId="EnumConverter" />
        <f:attribute name="enum" value="org.matrix.forum.ShowVisible" />
      </t:selectOneMenu>

      <h:outputText value="#{forumBundle.orderedBy} " rendered="#{forumCatalogueBean.editorUser}" />
      <t:selectOneMenu style="vertical-align:middle;" styleClass="selectBox"
        value="#{forumCatalogueBean.questionFilter.orderBy}"
        valueChangeListener="#{forumCatalogueBean.processQuestionFilterValueChange}"
        rendered="#{forumCatalogueBean.editorUser}">
        <f:selectItems value="#{forumCatalogueBean.orderByItems}" />
        <f:converter converterId="EnumConverter" />
        <f:attribute name="enum" value="org.matrix.forum.OrderBy" />
      </t:selectOneMenu>
    </t:div>    
    <f:facet name="footer">
      <h:commandButton value="#{forumBundle.refresh}"
        styleClass="searchButton" action="#{forumCatalogueBean.searchQuestions}"/>
    </f:facet>
  </h:panelGrid>

  <t:buffer into="#{table}">
    <t:dataTable rows="#{forumCatalogueBean.questionsData.pageSize}" id="data"
      first="#{forumCatalogueBean.questionsData.firstRowIndex}"
      value="#{forumCatalogueBean.questionsData.rows}" var="row"
      rendered="#{forumCatalogueBean.questionsData.rowCount > 0}"
      styleClass="resultList" summary="results"
      rowClasses="row1,row2" headerClass="header"
      footerClass="footer">
      <t:column styleClass="interviewQuestionsColumn">
        <t:div styleClass="#{forumCatalogueBean.questionStatus}">
          <t:div styleClass="infoBar" rendered="#{forumCatalogueBean.editorUser}"
                 style="font-size:10px">
          </t:div>

          <t:div styleClass="questionInfo">
            <t:div styleClass="dateInfo">
              <h:outputText value="#{row.question.outputIndex == 0 ? '?' : row.question.outputIndex}."
                            styleClass="#{not forumCatalogueBean.inputIndexOrderBy or not forumCatalogueBean.editorUser ? 'boldOutputIndex' : 'outputIndex'}"/>
               <h:outputText value=" ##{row.question.inputIndex}"
                             rendered="#{forumCatalogueBean.editorUser and forumCatalogueBean.censoredInterviewType}"
                            styleClass="#{forumCatalogueBean.inputIndexOrderBy ? 'boldInputIndex' : 'inputIndex'}"/>

              <h:outputText value="#{forumCatalogueBean.questionDateTime}" styleClass="dateTime"
                style="margin-left:5px">
                <f:convertDateTime pattern="dd/MM/yyyy HH:mm:ss" />
              </h:outputText>
            </t:div>
          </t:div>

          <t:div styleClass="questionBody">
            <sf:outputText value="#{forumCatalogueBean.questionLabel}" styleClass="user"
               rendered="#{forumCatalogueBean.renderQuestionUser}"
               translator="#{userSessionBean.translator}"
               translationGroup="#{userSessionBean.translationGroup}"/>
            <h:outputText value=" (#{row.question.creationUserId})"
              styleClass="user"
              rendered="#{row.question.text != null and (forumCatalogueBean.editorUser or userSessionBean.selectedMenuItem.properties.showUser == 'true')}"/>
            <h:outputText value=": " styleClass="user" rendered="#{forumCatalogueBean.renderQuestionUser}"/>
            <h:graphicImage value="/common/forum/images/user.png" alt="user"
              rendered="#{row.question.text != null and forumCatalogueBean.editorUser and forumCatalogueBean.userConnected}"
              styleClass="userImage"/>
          </t:div>
          <t:div styleClass="questionBody" rendered="#{row.question.text != null}">
            <h:outputText value="#{forumCatalogueBean.textWithLinks}"
                          styleClass="text" escape="false" />
          </t:div>
          <t:div styleClass="answerBody">
            <h:outputText value="#{forumCatalogueBean.firstAnswerUserId}: "
                          styleClass="user"/>
          </t:div>
          <t:div styleClass="answerBody">
            <sf:outputRichText value="#{forumCatalogueBean.firstAnswer.text}"
                               styleClass="text"/>
          </t:div>
          <t:div styleClass="answerBody" rendered="#{forumCatalogueBean.editorUser and forumCatalogueBean.firstAnswer.comments != null}" >
            <h:outputText value="#{forumBundle.comments}: "
                          styleClass="user"/>
          </t:div>
          <t:div styleClass="answerBody" rendered="#{forumCatalogueBean.editorUser and forumCatalogueBean.firstAnswer.comments != null}">
            <sf:outputRichText value="#{forumCatalogueBean.firstAnswer.comments}"
                               styleClass="comments"/>
          </t:div>
          <t:div styleClass="buttonsBar" rendered="#{forumCatalogueBean.editorUser}">
            <h:commandButton value="#{forumBundle.questionVisible}"
                rendered="#{forumCatalogueBean.editorUser and not row.question.visible}"
                alt="#{forumBundle.questionVisible}"
                title="#{forumBundle.questionVisible}"
                styleClass="editButton" immediate="true"
                onclick="return confirm('#{forumBundle.visibleAlert}')"
                action="#{forumCatalogueBean.setSelectedQuestionVisible}"
                />
            <h:commandButton value="#{objectBundle.edit}"
                rendered="#{forumCatalogueBean.editorUser}"
                alt="#{objectBundle.edit}"
                title="#{objectBundle.edit}"
                styleClass="editButton" immediate="true"
                action="#{forumCatalogueBean.editQuestion}"/>
            <h:commandButton value="#{objectBundle.delete}"
                rendered="#{forumCatalogueBean.editorUser}"
                alt="#{objectBundle.delete}"
                title="#{objectBundle.delete}"
                styleClass="editButton" immediate="true"
                action="#{forumCatalogueBean.removeQuestion}"
                onclick="return confirm('#{objectBundle.confirm_remove}')"/>
          </t:div>
        </t:div>
      </t:column>

      <f:facet name="footer">
        <t:dataScroller
          fastStep="100"
          paginator="true"
          paginatorMaxPages="20"
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
        </t:dataScroller>
      </f:facet>
    </t:dataTable>
  </t:buffer>

  <t:div styleClass="resultBar"
            rendered="#{forumCatalogueBean.questionsData.rows != null}"
            style="float:left;border:0">
    <t:dataScroller for="data"
      firstRowIndexVar="firstRow"
      lastRowIndexVar="lastRow"
      rowsCountVar="rowCount"
      rendered="#{forumCatalogueBean.questionsData.rowCount > 0}">
      <h:outputFormat value="#{forumBundle.questionsResultRange}"
        style="margin-top:10px">
        <f:param value="#{firstRow}" />
        <f:param value="#{lastRow}" />
        <f:param value="#{rowCount}" />
      </h:outputFormat>
    </t:dataScroller>
  </t:div>

    <t:div styleClass="forumTotals" style="width:100%"
    rendered="#{forumCatalogueBean.editorUser}">
    <h:outputText value="#{forumBundle.answers}: #{forumCatalogueBean.currentForumView.visibleAnswerCount} / #{forumCatalogueBean.currentForumView.visibleQuestionCount} " />
  </t:div>

  <h:outputText value="#{table}" escape="false"/>

  <t:div rendered = "#{!forumCatalogueBean.userAwaitingResponse or forumCatalogueBean.editorUser}">

  <t:div rendered="#{(!forumCatalogueBean.readOnly and forumCatalogueBean.participantUser) or 
    forumCatalogueBean.editorUser}">
    <t:div style="margin-top:10px" styleClass="fieldName">
      <sf:outputText value=" #{forumCatalogueBean.askQuestionLabel}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}"/>
      <h:outputText value=" (#{forumCatalogueBean.questionMaxLength} #{forumBundle.characters})" />
    </t:div>

    <t:div styleClass="editingPanel">
      <t:div>
        <t:inputTextarea title="#{forumBundle.question}" id="questionText" value="#{forumCatalogueBean.currentQuestion.text}"
          styleClass="inputBox" style="width:99%" rows="4"
          onmouseout="checkMaxLength(this, #{forumCatalogueBean.questionMaxLength})"
          onmousemove="checkMaxLength(this, #{forumCatalogueBean.questionMaxLength})"
          onkeypress="checkMaxLength(this, #{forumCatalogueBean.questionMaxLength})"
          onkeyup="checkMaxLength(this, #{forumCatalogueBean.questionMaxLength})" />
      </t:div>
      <t:div styleClass="actionsBar">
        <h:panelGroup rendered="#{forumCatalogueBean.maxFileSize gt 0}">
          <t:inputFileUpload id="inputFile" size="70" storage="file"
            value="#{forumCatalogueBean.uploadedFile}" styleClass="fileUploadInput" 
            onchange="showOverlay(); document.getElementById('uploadFileButton').click();" />
          <t:outputLabel for="inputFile" value="#{forumBundle.linkDocument}" styleClass="createButton" />
          <t:commandButton id="uploadFileButton" forceId="true"
            style="display:none" action="#{forumCatalogueBean.uploadFile}" />
        </h:panelGroup>
        <h:commandButton value="#{objectBundle.send}"
          alt="#{objectBundle.send}" title="#{objectBundle.send}"
          styleClass="createButton" onclick="showOverlay(); return true;"
          action="#{forumCatalogueBean.createQuestion}" />
        <t:inputHidden value="#{forumCatalogueBean.questionHash}" />
      </t:div>
    </t:div>
  </t:div>

 </t:div>
 </t:div>

</jsp:root>
