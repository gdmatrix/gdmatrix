<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
           xmlns:f="http://java.sun.com/jsf/core"
           xmlns:h="http://java.sun.com/jsf/html"
           xmlns:sf ="http://www.santfeliu.org/jsf">

  <h:panelGrid columns="1" id="mainLayout" width="100%"
               styleClass="main">

    <h:panelGrid columns="2" width="100%" id="topSection"
    styleClass="topSection" columnClasses="column1,column2">
      <h:graphicImage url="/documents/#{userSessionBean.selectedMenuItem.properties['topSection.logo.docid']}" 
                      width="#{userSessionBean.selectedMenuItem.properties['topSection.logo.width']}%"/>
      <h:graphicImage url="/documents/#{userSessionBean.selectedMenuItem.properties['topSection.banner.docid']}" 
                      width="#{userSessionBean.selectedMenuItem.properties['topSection.banner.width']}%" />      
    </h:panelGrid>

<!--    
    <f:verbatim>
      <object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" type="application/x-shockwave-flash"                               
		 data="ticker324.swf" width="755" height="40" id="ticker324" align="middle">
          <param name="FlashVars" value="urlconfig=stc/config.xml"/>
          <param name="allowScriptAccess" value="sameDomain" />
          <param name="movie" value="ticker324.swf" />
          <param name="quality" value="high" />
          <param name="bgcolor" value="#000000" />
          Flash Noticies 3/24 
      </object>
    </f:verbatim>
-->
    <h:panelGrid columns="3" width="100%" 
    styleClass="centerSection" columnClasses="leftArea,centerArea,rightArea" 
    id="centerSection">
    
      <h:panelGrid columns="1" id="leftSidePanel" rowClasses="row0,row1,row2,row3,row4" width="100%">
        <h:panelGroup>
          <h:outputText value="#{webBundle.language}:" />
          <sf:languageSelector locales="#{userSessionBean.supportedLocales}" />
        </h:panelGroup>
        <h:panelGrid columns="1" rendered="#{userSessionBean.anonymousUser}" 
        styleClass="anonymousUser" cellpadding="0" cellspacing="5" width="100%">
          <sf:outputText value="#{loginBean.loginMessage}"
                        rendered="#{loginBean.loginMessage != null}" 
                        style="color:rgb(255,0,0);"/>
          <sf:outputText value="Benvingut!" rendered="#{loginBean.loginMessage == null}" 
                        style="color:rgb(0,128,0);"/>     
          <h:panelGrid columns="2" cellpadding="0" cellspacing="0" width="100%">
            <sf:outputText value="Usuari"/>
            <h:inputText binding="#{loginBean.usernameInputText}"
                         immediate="true" tabindex="1" size="10"/>
            <sf:outputText value="Password"/>
            <h:inputSecret binding="#{loginBean.passwordInputSecret}"
                           immediate="true" tabindex="2" size="10" />
            <sf:secureCommandLink action="#{loginBean.login}" immediate="true"
              port="#{applicationBean.serverSecurePort}" scheme="https">
              <sf:outputText value="Connecta"/>
            </sf:secureCommandLink>
            <h:commandLink action="register">
              <sf:outputText value="Enregistra't"/>
            </h:commandLink>

          </h:panelGrid>          
        </h:panelGrid>        
        
        <h:panelGrid rendered="#{!userSessionBean.anonymousUser}"
                       columns="1" styleClass="nonAnonymousUser" 
                       cellpadding="0" cellspacing="5" width="100%">                       
          <sf:outputText value="Hola, #{userSessionBean.displayName}"
                        style="color:rgb(0,128,0);"/>    
          <h:commandLink action="#{loginBean.logout}" immediate="true">
            <h:outputText value="#{webBundle.buttonSignout}" />
          </h:commandLink>
          <h:commandLink action="password">
            <sf:outputText value="Canvia paraula de pas"/>
          </h:commandLink>
        </h:panelGrid>
                
        <h:panelGrid columns="1" rowClasses="title,notitle" width="100%">
          <sf:outputText value="SANT FELIU" id="leftSideMenuTitle1"/> 
          <sf:navigationMenu id="leftSideMenu1" value="main" var="item"
                           baseMid="#{userSessionBean.selectedMenuItem.properties['leftArea.menu1.mid']}"
                           orientation="vertical">
            <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </sf:navigationMenu>             
        </h:panelGrid>
        
        <h:panelGrid columns="1" rowClasses="title,notitle" width="100%">
          <sf:outputText value="LINKS" id="leftSideMenuTitle2"/> 
          <sf:navigationMenu id="leftSideMenu2" value="main" var="item"
                           baseMid="#{userSessionBean.selectedMenuItem.properties['leftArea.menu2.mid']}"
                           orientation="vertical">
            <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </sf:navigationMenu>             
        </h:panelGrid>
        
        <h:panelGrid columns="1" rowClasses="title,notitle" width="100%">
          <sf:outputText value="DESCARREGUES" id="leftSideMenuTitle3" /> 
          <sf:navigationMenu id="leftSideMenu3" value="main" var="item"
                           baseMid="#{userSessionBean.selectedMenuItem.properties['leftArea.menu3.mid']}"
                           orientation="vertical">
            <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </sf:navigationMenu>             
        </h:panelGrid>
      </h:panelGrid>
      
      <h:panelGrid columns="1" width="100%">
        <jsp:include page="${requestScope['_body']}" />                         
      </h:panelGrid>
      
      <h:panelGrid columns="1" id="rightSidePanel" 
        rowClasses="row1,row2,row3" cellpadding="0" cellspacing="8">
        
        <h:panelGrid columns="1" id="searchPanel" width="100%" styleClass="blackBorder">
          <h:inputText id="searchText" />
          <h:commandButton value="Buscar" id="searchTextButton" />
        </h:panelGrid>
        
        <h:panelGrid styleClass="blackBorder" width="100%" columns="1" rendered="#{!surveyBean.viewResults}" id="surveyQuestionPanel">
          <h:panelGrid columns="2" width="100%" id="surveyTitlePanel">
            <sf:outputText value="ENQUESTA" styleClass="title"/>
            <sf:navigationMenu id="surveyAdminMenu" value="main" var="item"
                             baseMid="#{userSessionBean.selectedMenuItem.properties['survey.admin.mid']}"
                             orientation="vertical" rendered="#{surveyBean.renderAdminLink}">
              <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
                translator="#{userSessionBean.translator}"
                translationGroup="#{userSessionBean.translationGroup}" />
            </sf:navigationMenu>                         
          </h:panelGrid>
          <sf:outputText value="#{surveyBean.surveyText}" id="surveyTitle" />
          <h:selectOneRadio value="#{surveyBean.answerSelected}" layout="pageDirection" id="surveyAnswers" >
            <f:selectItems value="#{surveyBean.answersItemList}" id="surveyAnswersList" />
          </h:selectOneRadio>          
          <h:panelGrid columns="2" id="surveyButtonPanel">
            <h:commandButton value="Votar" action="#{surveyBean.vote}" id="surveyVote"/>
            <h:commandButton value="Mostrar" action="#{surveyBean.view}" id="surveyView"/>
          </h:panelGrid>
        </h:panelGrid>      
        
        <h:panelGrid styleClass="blackBorder" columns="1" width="100%" id="surveyResultsPanel" rendered="#{surveyBean.viewResults}">
          <sf:outputText value="ENQUESTA" styleClass="title"/>      
          <sf:outputText value="#{surveyBean.surveyText}" id="surveyResultsTitle"/>
          <h:dataTable binding="#{surveyBean.answersDataTable}" var="row" 
            id="surveyResultsTable">
            <h:column>
              <h:panelGrid columns="1" id="answerTextGrid">
                <h:panelGrid columns="2" id="answerBar" cellspacing="0" cellpadding="0">
                  <h:graphicImage url="/common/survey/images/#{surveyBean.barImage}" width="#{row.answerPixels}" height="#{surveyBean.barHeight}" id="surveyColor1" />
                  <h:graphicImage url="/common/survey/images/#{surveyBean.remainImage}" width="#{row.remainPixels}" height="#{surveyBean.barHeight}" id="surveyColor2" />                  
                </h:panelGrid>                
                <sf:outputText value="#{row.answerText}" id="surveyAnswerText2" />
              </h:panelGrid>
            </h:column>
            <h:column>
              <h:panelGrid columns="1" id="surveyAnswerVotesGrid">
                <h:outputText value="#{row.answerVotes} votos" id="surveyAnswerVotes" />
                <h:outputText value="#{row.answerPercent} %" id="surveyAnswerPercent" />
              </h:panelGrid>              
            </h:column>
          </h:dataTable>
          <h:commandButton value="Tornar" action="#{surveyBean.back}" id="surveyBack"/>
        </h:panelGrid>
        
        <h:panelGrid columns="1" id="lastAnswersPanel" width="100%" styleClass="blackBorder">
          <sf:navigationMenu id="forumsMenu" value="main" var="item"
                           baseMid="#{userSessionBean.selectedMenuItem.properties['forum.menu.mid']}"
                           orientation="vertical">
            <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
              translator="#{userSessionBean.translator}"
              translationGroup="#{userSessionBean.translationGroup}" />
          </sf:navigationMenu>                         
          <h:dataTable binding="#{forumBean.lastAnswersDataTable}" var="row">
            <h:column>
              <h:panelGrid columns="1">
                <h:outputText value="#{row.texte}" styleClass="questionTitle" />
                <h:outputText value="#{row.dataalta} a les #{row.horaalta}" />
                <h:outputText value=" per #{row.usercod}" />          
              </h:panelGrid>
            </h:column>
          </h:dataTable>
        </h:panelGrid>
        
      </h:panelGrid>
    </h:panelGrid>
    <h:panelGroup styleClass="bottomSection" id="bottomSection">
      <sf:navigationMenu id="hfootermenu" value="main" var="item"
                       baseMid="#{userSessionBean.selectedMenuItem.properties['bottomSection.menu.mid']}"
                       orientation="horizontal" styleClass="hmenu" >
        <sf:outputText value="#{item.label}" rendered="#{item.rendered}"
          translator="#{userSessionBean.translator}"
          translationGroup="#{userSessionBean.translationGroup}" />
        <h:outputText styleClass="hmenu_separator" value="|"
                      rendered="#{item.rendered}"/>
      </sf:navigationMenu>             
    </h:panelGroup> 
  </h:panelGrid>  
      
</jsp:root>    
