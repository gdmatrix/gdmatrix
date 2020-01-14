<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:outputText value="#{newDetailsBean.emailSharingScripts}" escape="false" />  
  
  <f:loadBundle basename="org.santfeliu.news.web.resources.NewsBundle" var="newsBundle" />
  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" var="objectBundle" />

  <t:saveState value="#{newDetailsBean}"/>
  
  <t:div styleClass="newView">

    <h:messages rendered="#{userSessionBean.facesMessagesQueued}" 
              showSummary="true"
              infoClass="infoMessage"
              warnClass="warnMessage"
              errorClass="errorMessage"
              fatalClass="fatalMessage" />
    
    <t:div styleClass="actions">
      <h:commandButton onclick="javascript:printGroup('printNew');"
        rendered="#{newDetailsBean.printEnabled and userSessionBean.menuModel.browserType == 'desktop'}"
        image="#{userSessionBean.icons.print}"
        alt="#{webBundle.buttonPrint}"
        title="#{webBundle.buttonPrint}"
        styleClass="buttonLink" />
    </t:div>

    <sf:printableGroup name="printNew">
      <t:div styleClass="newPrintView">
        <t:div styleClass="dateLayer">
          <h:outputText value="#{newDetailsBean.humanStartDay}"
                        styleClass="startDay"/>
        </t:div>
        <t:div rendered="#{newDetailsBean.readingCountRender}"
                  styleClass="readingCountLayer">
          <h:panelGroup>
            <h:outputText value="#{newsBundle.new_details_readings}: "
              styleClass="readingCount" />
            <h:outputText value="#{newDetailsBean.readingCount}"
              styleClass="readingCount"/>
          </h:panelGroup>
        </t:div>
        <t:div styleClass="headlineLayer">
          <sf:outputText value="#{newDetailsBean.newObject.headline}"
            styleClass="headline"
            translator="#{userSessionBean.translator}"
            translationGroup="#{newDetailsBean.translationGroup}"/>
        </t:div>
        <t:div styleClass="textLayer">
          <h:graphicImage url="#{newDetailsBean.imageURL}"
                          styleClass="image"
                          height="#{newDetailsBean.imageHeight}"
                          width="#{newDetailsBean.imageWidth}"
                          rendered="#{newDetailsBean.renderImage}" />
          <t:div styleClass="text">
            <sf:outputText value="#{newDetailsBean.shownText}"
              escape="false"
              translator="#{userSessionBean.translator}"
              translationGroup="#{newDetailsBean.translationGroup}" />
          </t:div>
        </t:div>
      </t:div>
    </sf:printableGroup>
    
    <t:div styleClass="documentListLayer" rendered="#{newDetailsBean.documentListRender}">
      <h:outputText value="#{newsBundle.new_details_relatedDocuments}:"
        styleClass="header" />        
      <t:dataList var="row" value="#{newDetailsBean.documentList}"
                   styleClass="documentList"                   
                   layout="unorderedList">
        <t:div id="documentListColumn">
          <h:panelGroup>
            <h:graphicImage url="#{newDetailsBean.mimeTypePath}"/>
            <sf:outputLink value="#{newDetailsBean.documentUrl}"
                           styleClass="documentLink" target="_blank" 
                           ariaLabel="#{newDetailsBean.documentAriaLabel}"
                           translator="#{userSessionBean.translator}"
                           translationGroup="#{newDetailsBean.translationGroup}">
              <h:outputText id="documentListTitle" value="#{row.title}" 
                            styleClass="title"/>
            </sf:outputLink>
          </h:panelGroup>
        </t:div>
      </t:dataList>
    </t:div>
    <t:div rendered="#{newDetailsBean.sharingEnabled}" styleClass="shareLayer">
      <h:panelGroup>
        <sf:outputText value="#{userSessionBean.selectedMenuItem.properties['details.shareText']}"
          styleClass="shareText"
          translator="#{userSessionBean.translator}"
          translationGroup="#{newDetailsBean.translationGroup}"
          rendered="#{userSessionBean.selectedMenuItem.properties['details.shareText'] != null}"/>
        <t:dataList value="#{newDetailsBean.shareLinkList}" styleClass="shareTable" var="item">          
          <sf:outputLink rendered="#{item.shareURL != 'EMAIL'}" target="_blank" value="#{item.shareURL}" styleClass="shareLink"
                        title="#{item.description}"
                        translator="#{userSessionBean.translator}"
                        translationGroup="#{userSessionBean.translationGroup}">
            <sf:graphicImage url="#{item.iconURL}" styleClass="shareImage" 
                            alt="#{item.description}"                              
                            translator="#{userSessionBean.translator}"
                            translationGroup="#{userSessionBean.translationGroup}" />
          </sf:outputLink>
          <sf:outputLink rendered="#{item.shareURL == 'EMAIL' and userSessionBean.menuModel.browserType == 'desktop'}" 
                        onclick="switchSendEmailDiv(event);return false;" value="#" styleClass="shareLink"
                        title="#{item.description}"
                        translator="#{userSessionBean.translator}"
                        translationGroup="#{userSessionBean.translationGroup}">
            <sf:graphicImage url="#{item.iconURL}"  
                           alt="#{item.description}"                             
                           translator="#{userSessionBean.translator}"
                           translationGroup="#{userSessionBean.translationGroup}" />                            
          </sf:outputLink>
        </t:dataList>
      </h:panelGroup>
    </t:div>
    <t:div rendered="#{newDetailsBean.editLinkRender and userSessionBean.menuModel.browserType == 'desktop'}" styleClass="objectForm">
      <h:commandButton value="#{objectBundle.edit}"
                       image="#{userSessionBean.icons.detail}"
                       alt="#{objectBundle.edit}" 
                       title="#{objectBundle.edit}"
                       styleClass="editButton"
                       style="vertical-align:middle"
                       immediate="true" action="#{newDetailsBean.editNew}" />
    </t:div>    
  </t:div>
 
  <t:div styleClass="sendEmailDiv" forceId="true" id="sendEmailDiv">
    <t:div styleClass="titleDiv">
      <h:outputText value="#{webBundle.sendEmailTitle}"/>
      <h:outputLink onclick="switchSendEmailDiv(event);return false;" value="#" 
                    styleClass="closeButton"
                    title="#{webBundle.sendEmailCancel}">
        <h:graphicImage url="/images/sn/send_email_close.png"
                        alt="#{webBundle.sendEmailCancel}" />
      </h:outputLink>
    </t:div>    
    <t:div styleClass="labelDiv">
      <h:outputText value="* #{webBundle.sendEmailRequiredFields}"/>
    </t:div>
    <t:div styleClass="labelDiv">
      <h:outputLabel value="#{webBundle.sendEmailName}:" for="sendEmailName" />
    </t:div>
    <t:div styleClass="inputDiv">
      <t:inputText forceId="true" id="sendEmailName" value="#{newDetailsBean.emailName}"/>
    </t:div>
    <t:div styleClass="labelDiv">
      <h:outputLabel value="#{webBundle.sendEmailFrom}&lt;abbr title='#{webBundle.requiredField}'&gt;*&lt;/abbr&gt;:" for="sendEmailFrom" />      
    </t:div>
    <t:div styleClass="inputDiv">
      <t:inputText forceId="true" id="sendEmailFrom" value="#{newDetailsBean.emailFrom}" />
    </t:div>
    <t:div styleClass="labelDiv">      
      <h:outputLabel value="#{webBundle.sendEmailTo}&lt;abbr title='#{webBundle.requiredField}'&gt;*&lt;/abbr&gt;:" for="sendEmailTo" />      
    </t:div>
    <t:div styleClass="inputDiv">
      <t:inputText forceId="true" id="sendEmailTo" value="#{newDetailsBean.emailTo}"/>
    </t:div>
    <t:div styleClass="labelDiv">
      <h:outputLabel value="#{webBundle.sendEmailSubject}:" for="sendEmailSubject" />
    </t:div>
    <t:div styleClass="inputDiv">
      <t:inputText forceId="true" id="sendEmailSubject" value="#{newDetailsBean.emailSubject}"/>
    </t:div>
    <t:div styleClass="labelDiv">
      <h:outputLabel value="#{webBundle.sendEmailText}&lt;abbr title='#{webBundle.requiredField}'&gt;*&lt;/abbr&gt;:" for="sendEmailText" />      
    </t:div>
    <t:div styleClass="inputDiv">
      <t:inputTextarea forceId="true" id="sendEmailText" value="#{newDetailsBean.emailText}"/>
    </t:div>    
    <t:div styleClass="buttonDiv">      
      <t:div styleClass="errorMessageDiv">
        <t:outputText value="#{webBundle.sendEmailInvalidForm}" forceId="true" id="sendEmailError" style="display:inline;visibility:hidden;" />
      </t:div>    
      <h:outputLink onclick="sendEmail();return false;" value="#" styleClass="sendLink"> 
        <t:outputText value="#{webBundle.sendEmailButton}" />      
      </h:outputLink>
    </t:div>      
  </t:div>
  
  <t:commandButton style="visibility:hidden" forceId="true" id="sendEmailButton" action="#{newDetailsBean.sendEmail}" styleClass="hiddenButton" />   

</jsp:root>
