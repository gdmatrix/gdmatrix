<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf"
          xmlns:c="http://java.sun.com/jsp/jstl/core" >

    <t:div styleClass="propertyValuesPanel">
      <t:dataTable value="#{panel.values}" var="row"  
                   summary="#{panel.tableSummary}"
                   rowClasses="row1,row2" footerClass="footer" headerClass="header"
                   bodyStyle="#{empty panel.values ? 'display:none' : ''}"
                   styleClass="resultList" style="width:100%">
        <t:column>
          <h:outputText value="#{row}"
            styleClass="#{panel.intersectedValue ? 'intersectedValue' : 'value'}"/>
        </t:column>
      </t:dataTable>
    </t:div>

</jsp:root>