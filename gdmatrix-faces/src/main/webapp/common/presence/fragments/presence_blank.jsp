<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <t:commandLink id="enter_link" forceId="true" 
                 action="worker_id" value="enter" style="display:none" />

  <f:verbatim>
    <script type="text/javascript">
      function enterLogin()
      {
        var button = document.getElementById("enter_link");
        console.info(button);
        button.click();
      }
      setTimeout(enterLogin, 100);
    </script>
  </f:verbatim>
    
</jsp:root>

