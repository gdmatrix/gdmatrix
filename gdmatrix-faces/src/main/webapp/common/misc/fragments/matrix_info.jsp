<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
          
  <t:div styleClass="matrixInfoLayer">

    <t:div styleClass="titleLayer">
      <h:outputText value="Matrix Info" />
    </t:div>

    <t:div styleClass="tableLayer">
      <h:panelGrid columns="2" border="1" cellpadding="10" cellspacing="0"
                   columnClasses="col1,col2" width="100%">
        <h:outputText value="Version" />
        <h:outputText value="#{matrixInfoBean.version}" />
        <h:outputText value="License" />
        <h:outputText value="#{matrixInfoBean.license}" />
        <h:outputText value="Team" />
        <t:dataList value="#{matrixInfoBean.team}" var="row"
                    layout="unorderedList">
          <h:outputText value="#{row}" />
        </t:dataList>
        <h:outputText value="Properties" />
        <t:dataList value="#{matrixInfoBean.matrixInfoProperties}" var="row"
                    layout="unorderedList">
          <h:outputText value="#{row}" />
        </t:dataList>
      </h:panelGrid>
    </t:div>

  </t:div>

  <t:div styleClass="matrixInfoLayer">

    <t:div styleClass="titleLayer">
      <h:outputText value="JVM Info" />
    </t:div>

    <t:div styleClass="tableLayer">
      <h:panelGrid columns="2" border="1" cellpadding="10" cellspacing="0"
                   columnClasses="col1,col2" width="100%">
        <h:outputText value="Up Time" />
        <h:outputText value="#{matrixInfoBean.upTime} (from #{matrixInfoBean.startDateTime})" />
        <h:outputText value="JVM Name" />
        <h:outputText value="#{matrixInfoBean.runtimeBean.name}" />
        <h:outputText value="Input arguments" />
        <t:dataList value="#{matrixInfoBean.runtimeBean.inputArguments}" var="row"
                    layout="unorderedList">
          <h:outputText value="#{row}" />
        </t:dataList>
        <h:outputText value="JVM Specification"/>
        <h:panelGrid columns="1">
          <h:outputText value="Name: #{matrixInfoBean.runtimeBean.specName}" />
          <h:outputText value="Vendor: #{matrixInfoBean.runtimeBean.specVendor}" />
          <h:outputText value="Version: #{matrixInfoBean.runtimeBean.specVersion}" />
        </h:panelGrid>
        <h:outputText value="JVM Implementation"/>
        <h:panelGrid columns="1">
          <h:outputText value="Name: #{matrixInfoBean.runtimeBean.vmName}" />
          <h:outputText value="Vendor: #{matrixInfoBean.runtimeBean.vmVendor}" />
          <h:outputText value="Version: #{matrixInfoBean.runtimeBean.vmVersion}" />
        </h:panelGrid>
        <h:outputText value="System properties" />
        <t:dataList value="#{matrixInfoBean.systemProperties}" var="row"
                    layout="unorderedList">
          <h:outputText value="#{row}" />
        </t:dataList>
        <h:outputText value="Management spec version" />
        <h:outputText value="#{matrixInfoBean.runtimeBean.managementSpecVersion}" />
        <h:outputText value="Boot classpath" />
        <t:dataList value="#{matrixInfoBean.bootClassPath}" var="row"
                    layout="unorderedList">
          <h:outputText value="#{row}" />
        </t:dataList>
        <h:outputText value="Boot classpath supported" />
        <h:outputText value="#{matrixInfoBean.runtimeBean.bootClassPathSupported}" />
        <h:outputText value="Classpath" />
        <t:dataList value="#{matrixInfoBean.classPath}" var="row"
                    layout="unorderedList">
          <h:outputText value="#{row}" />
        </t:dataList>
        <h:outputText value="Library path" />
        <t:dataList value="#{matrixInfoBean.libraryPath}" var="row"
                    layout="unorderedList">
          <h:outputText value="#{row}" />
        </t:dataList>
      </h:panelGrid>
    </t:div>

  </t:div>

</jsp:root>
