<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.feed.web.resources.FeedBundle"
    var="feedBundle" />

  <t:div>
    <h:outputText value="#{feedBundle.folderId}:"
                  styleClass="textBox" style="width:20%" />
    <h:outputText value="#{folderMainBean.folder.folderId}"
                 styleClass="textBox" />
  </t:div>
  <t:div>
    <h:outputText value="#{feedBundle.parentFolder}:"
                  styleClass="textBox" style="width:20%" />
    <t:selectOneMenu value="#{folderMainBean.folder.parentFolderId}"
                     styleClass="selectBox">
      <f:selectItems value="#{folderMainBean.folderItems}" />
    </t:selectOneMenu>
    <h:commandButton value="#{objectBundle.search}"
      image="#{userSessionBean.icons.search}"
      alt="#{objectBundle.search}" title="#{objectBundle.search}"
      styleClass="searchButton"
      action="#{folderMainBean.searchParentFolder}" />  
    <h:commandButton action="#{folderMainBean.showParentFolder}"
      value="#{objectBundle.show}"
      image="#{userSessionBean.icons.show}"
      alt="#{objectBundle.show}" title="#{objectBundle.show}"
      styleClass="showButton"      
      rendered="#{folderMainBean.renderShowParentFolder}" />    
  </t:div>  
  <t:div>
    <h:outputText value="#{feedBundle.name}:"
                  styleClass="textBox" style="width:20%" />
    <h:inputText value="#{folderMainBean.folder.name}"
                 styleClass="inputBox" style="width:75%"/>
  </t:div>
  <t:div>
    <h:outputText value="#{feedBundle.description}:"
                  styleClass="textBox" style="width:20%" />
    <h:inputText value="#{folderMainBean.folder.description}"
                 styleClass="inputBox" style="width:75%"/>
  </t:div>
  <t:div>
    <h:outputText value="#{feedBundle.iconDocId}:"
                  styleClass="textBox" style="width:20%" />
    <h:inputText value="#{folderMainBean.folder.iconDocId}"
                 styleClass="inputBox" style="width:10%"/>
  </t:div>
  <t:div>
    <h:outputText value="#{feedBundle.alias}:"
                  styleClass="textBox" style="width:20%" />
    <h:inputText value="#{folderMainBean.folder.alias}"
                 styleClass="inputBox" style="width:30%"/>
  </t:div>
  <t:div>
    <h:outputText value="#{feedBundle.defaultEntryCount}:"
                  styleClass="textBox" style="width:20%" />
    <h:inputText value="#{folderMainBean.folder.defaultEntryCount}"
                 styleClass="inputBox" style="width:10%"/>
  </t:div>
</jsp:root>
