<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <!-- CONTENT -->

  <t:div>
     <h:outputText value="&lt;main id='sf_main_content' tabindex='-1'&gt;" escape="false"/>          
     <jsp:include page="${requestScope['_body']}"/>
     <h:outputText value="&lt;/main&gt;" escape="false"/>    
  </t:div>
      
</jsp:root>
