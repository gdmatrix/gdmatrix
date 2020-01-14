<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  <jsp:output omit-xml-declaration="true" doctype-root-element="HTML"
             doctype-system="http://www.w3.org/TR/html4/loose.dtd"
             doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"/>

          <h:panelGrid id="generalLayout" 
                       columns="1"
                       styleClass="general_main" 
                       rowClasses="general_body"
                       headerClass="general_header"
                       cellspacing="0"
                       cellpadding="0"
                       style="margin-top:1px"
                       summary="">
            <t:div>
              <jsp:include page="/templates/${userSessionBean.template}/template.jsp" />              
            </t:div>
          </h:panelGrid>

</jsp:root>

