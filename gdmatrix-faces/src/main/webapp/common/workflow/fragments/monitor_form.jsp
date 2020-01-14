<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <f:loadBundle basename="org.santfeliu.web.obj.resources.ObjectBundle"
    var="objectBundle" />

  <t:saveState value="#{monitorFormBean}" />

  <f:verbatim>
    <script src="${pageContext.request.contextPath}/plugins/working/work.js"
      type="text/javascript">a=0</script>
  </f:verbatim>

  <t:div style="text-align:center">
    <sf:outputText value="#{monitorFormBean.message}"
      translator="#{instanceBean.translationEnabled ?
        applicationBean.translator : null}"
      translationGroup="wf:#{instanceBean.workflowName}"
      styleClass="workflowMessage" />
  </t:div>

  <t:div style="text-align:center;height:30px">
    <h:outputText value="#{monitorFormBean.progress}"
      styleClass="workflowMessage" />
  </t:div>

  <t:div forceId="true" id="workWheel" style="text-align:center;height:100px">
  </t:div>

  <t:div style="text-align:center"
    rendered="#{monitorFormBean.cancelButtonRendered}">
    <t:commandButton value="#{objectBundle.cancel}" action="#{monitorFormBean.cancel}" />
  </t:div>

  <t:commandButton forceId="true" id="monitorRefresh"
    value="Refresh" action="#{monitorFormBean.refresh}" style="display:none" />

  <f:verbatim>
    <script type="text/javascript">
      function updateMonitor()
      {
        elem = document.getElementById("monitorRefresh");
        elem.click();
      }
      showWork(document.getElementById("workWheel"), '${pageContext.request.contextPath}');
      setTimeout(updateMonitor, ${monitorFormBean.refreshTime});
    </script>
  </f:verbatim>

</jsp:root>
