<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.presence.web.resources.PresenceBundle"
    var="presenceBundle" />
  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle" 
    var="objectBundle" />

  <h:panelGrid columns="2" styleClass="worker_id" width="100%"
    columnClasses="col1, col2" headerClass="header">
    <f:facet name="header">
      <sf:outputText value="#{userSessionBean.selectedMenuItem.properties.title}"
        translator="#{userSessionBean.translator}"
        translationGroup="#{userSessionBean.translationGroup}" />
    </f:facet>

    <h:panelGrid columns="3">
      <t:div onclick="javascript:typeKey('7');" styleClass="key">
        <h:outputText value="7" />
      </t:div>
      <t:div onclick="javascript:typeKey('8');" styleClass="key">
        <h:outputText value="8" />
      </t:div>
      <t:div onclick="javascript:typeKey('9');" styleClass="key">
        <h:outputText value="9" />
      </t:div>
      <t:div onclick="javascript:typeKey('4');" styleClass="key">
        <h:outputText value="4" />
      </t:div>
      <t:div onclick="javascript:typeKey('5');" styleClass="key">
        <h:outputText value="5" />
      </t:div>
      <t:div onclick="javascript:typeKey('6');" styleClass="key">
        <h:outputText value="6" />
      </t:div>
      <t:div onclick="javascript:typeKey('1');" styleClass="key">
        <h:outputText value="1" />
      </t:div>
      <t:div onclick="javascript:typeKey('2');" styleClass="key">
        <h:outputText value="2" />
      </t:div>
      <t:div onclick="javascript:typeKey('3');" styleClass="key">
        <h:outputText value="3" />
      </t:div>
      <t:div onclick="javascript:typeKey('0');" styleClass="key">
        <h:outputText value="0" />
      </t:div>
      <t:div onclick="javascript:backspace();" styleClass="key">
        <h:graphicImage value="/common/misc/images/backspace.png" />
      </t:div>
      <t:div onclick="javascript:clearIdCode();" styleClass="key">
        <h:graphicImage value="/common/misc/images/clear.png" />
      </t:div>
    </h:panelGrid>

    <h:panelGroup>
      <t:inputSecret id="idCode" forceId="true" value="#{presenceMainBean.idCode}"
        styleClass="idCode"
        onkeyup="checkCode(this);" />
      <t:commandButton id="sendIdCode" forceId="true"
        action="#{presenceMainBean.identify}"
        value="ENTER" style="visibility:hidden" />
      <t:graphicImage id="waitIcon" forceId="true"
        value="/images/wait.png" style="visibility:hidden"
        alt="" title="" styleClass="waitIcon" />
      <t:messages 
        id="message_list" forceId="true" 
        styleClass="messages"
        globalOnly="false"
        layout="list"
        infoClass="info"
        warnClass="warn"
        errorClass="error"
        fatalClass="fatal" />
    </h:panelGroup>

  </h:panelGrid>

  <f:verbatim>
    <script type="text/javascript">  
      var idCodeLength = 5;
      var isMobile =
      {
        Android: function()
        {
          return navigator.userAgent.match(/Android/i) ? true : false;
        },
        BlackBerry: function()
        {
          return navigator.userAgent.match(/BlackBerry/i) ? true : false;
        },
        iOS: function()
        {
          return navigator.userAgent.match(/iPhone|iPad|iPod/i) ? true : false;
        },
        Windows: function()
        {
          return navigator.userAgent.match(/IEMobile/i) ? true : false;
        },
        any: function()
        {
          return (isMobile.Android() || isMobile.BlackBerry() || 
            isMobile.iOS() || isMobile.Windows());
        }
      };

      function typeKey(ch)
      {
        focusInput();
        var elem = document.getElementById("idCode");
        var value = elem.value;
        if (value == null || value == '') elem.value = ch;
        else if (value.length != idCodeLength)
        {
          elem.value += ch;
          if (elem.value.length == idCodeLength) sendIdCode();
        }
      }

      function checkCode(elem)
      {
        var value = elem.value;
        if (value != null)
        {
          if (value.length == idCodeLength)
          {
            sendIdCode();
          }
        }
      }

      function sendIdCode()
      {
        var iconElem = document.getElementById("waitIcon");
        iconElem.style.visibility = "visible";
        var elem = document.getElementById("sendIdCode");
        elem.click();
      }

      function backspace()
      {
        var elem = document.getElementById("idCode");
        var value = elem.value;
        if (value.length > 0) value = value.substring(1);
        elem.value = value;
        focusInput();
      }

      function clearIdCode()
      {
        var elem = document.getElementById("idCode");
        elem.value = "";
        focusInput();
      }

      function focusInput()
      {
        var elem = document.getElementById("idCode");
        if (isMobile.any())
        {
          elem.style.border = "none";
        }
        else
        {
          elem.focus();
        }
      }

      focusInput();
    </script>
  </f:verbatim>

</jsp:root>
