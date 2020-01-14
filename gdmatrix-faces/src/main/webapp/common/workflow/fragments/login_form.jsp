<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:saveState value="#{loginFormBean}" />

  <sf:outputText value="#{loginFormBean.message}"
    translator="#{instanceBean.translationEnabled ?
      applicationBean.translator : null}"
    translationGroup="wf:#{instanceBean.workflowName}"
    styleClass="workflowMessage" />

  <t:div rendered="#{loginFormBean.loginByPassword}" 
    style="margin-top:20px;margin-bottom:20px;text-align: center">
    <h:outputLabel for="intUserId" value="#{webBundle.outputUsername}:" 
      style="vertical-align:middle;margin:4px" />
    <h:inputText id="intUserId" value="#{loginFormBean.userId}"
      style="vertical-align:middle" onkeypress="enterlogin(event)" />
    <h:outputLabel for="intPassword" value="#{webBundle.outputPassword}:"
      style="vertical-align:middle;margin:4px" />
    <h:inputSecret id="intPassword" value="#{loginFormBean.password}"
      style="vertical-align:middle" onkeypress="enterlogin(event)" />
    <sf:secureCommandLink action="#{loginFormBean.login}"
      scheme="https" function="enterlogin" port="#{applicationBean.serverSecurePort}"
      styleClass="workflowCommandLink" style="vertical-align:middle">
      <sf:outputText value="#{webBundle.loginByPassword}"
        styleClass="workflowMessage" />
    </sf:secureCommandLink>
  </t:div>

  <t:div style="margin-top:20px;margin-bottom:20px;text-align: center;">
    <sf:secureCommandLink action="#{loginFormBean.loginCertificate}"
     rendered="#{loginFormBean.loginByCertificate}"
     scheme="https" port="#{applicationBean.clientSecurePort}"
     styleClass="workflowCommandLink" style="min-width:150px">
      <sf:outputText value="#{webBundle.certificateAuthentication}"
        styleClass="workflowMessage" />
    </sf:secureCommandLink>
  </t:div>

  <t:div style="margin-top:20px;margin-bottom:20px;text-align: center;">
    <h:outputLink value="#{loginFormBean.validUrl}" 
                rendered="#{loginFormBean.loginByValid}"
                styleClass="workflowCommandLink" style="min-width:150px">
    <sf:outputText value="VALid"
      styleClass="workflowMessage" />
    </h:outputLink>
  </t:div>

  <t:div style="margin-top:20px;margin-bottom:20px;text-align: center;">                
    <h:outputLink value="#{loginFormBean.mobileidUrl}" 
                  rendered="#{loginFormBean.loginByMobileid}"
                  styleClass="workflowCommandLink" style="min-width:150px">
      <sf:outputText value="MobileId"
        styleClass="workflowMessage" />
    </h:outputLink>
  </t:div>

</jsp:root>
