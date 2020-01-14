<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:sf="http://www.santfeliu.org/jsf">

  <h:panelGrid id="mainLayout" columns="1" 
               styleClass="main" 
               rowClasses="mainRow"
               cellspacing="0"
               cellpadding="0"
               columnClasses="contCol" 
               headerClass="header"
               footerClass="footer" 
               width="100%" 
               summary="">

    <h:panelGrid id="contCol" styleClass="contTable" width="100%"
                 cellpadding="1" cellspacing="1" summary=""
                 columns="1" rowClasses="contRow" 
                 headerClass="navPathRow">

      <h:panelGroup>
        <jsp:include page="${requestScope['_body']}"/>
      </h:panelGroup>

    </h:panelGrid>
  </h:panelGrid>
</jsp:root>
