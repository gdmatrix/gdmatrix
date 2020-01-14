<?xml version='1.0' encoding='windows-1252'?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:t="http://myfaces.apache.org/tomahawk"
          xmlns:sf="http://www.santfeliu.org/jsf">
  <f:loadBundle basename="org.santfeliu.news.web.resources.NewsBundle"
                var="newsBundle"/>
  <t:div>
    <h:commandButton value="#{newsBundle.new_sections_expandAll}" 
      action="#{newSectionsBean.expandAll}" 
      styleClass="showButton" />
    <h:commandButton value="#{newsBundle.new_sections_collapseAll}" 
      action="#{newSectionsBean.collapseAll}" 
      styleClass="showButton" />
  </t:div>
  <t:div styleClass="inputBox newSectionsTree">
    <t:tree2 value="#{newSectionsBean.treeModel}" var="node"
             binding="#{newSectionsBean.tree}">
      <f:facet name="NoModule">
        <h:panelGroup id="rootNodePanel">
          <h:outputText id="rootDesc" value="#{node.description}"
                        styleClass="textBox"/>
        </h:panelGroup>
      </f:facet>
      <f:facet name="Module">
        <h:panelGroup id="moduleNodePanel" style="vertical-align:middle" >
          <h:selectBooleanCheckbox id="moduleChecked" value="#{node.checked}"
                                   disabled="#{!newSectionsBean.enabledNode}" 
                                   onclick="submit();" 
                                   style="vertical-align:middle"/>
          <h:outputText id="moduleDesc" value="#{node.description}"
                        styleClass="textBox"
                        style="vertical-align:middle;margin-left:4pt"/>
          <h:outputLink id="toSectionLink" onclick="return goMid('#{node.mid}')"
                        rendered="#{node.checked}" value="/go.faces?xmid=#{node.mid}"
                        style="text-decoration:none;margin-left:4pt"
                        styleClass="showButton">
            <h:outputText id="toSectionText"
                          value="#{objectBundle.show}"
                          style="vertical-align:middle"/>
          </h:outputLink>
          <h:commandLink styleClass="stickyLink" style="margin-left: 4px;" rendered="#{node.checked}" action="#{newSectionsBean.switchSticky}">
            <h:graphicImage style="border: 0; vertical-align: middle;" alt="#{newsBundle.new_sections_sticky_on}" url="/common/news/images/sticky_on.png" rendered="#{node.sticky}" />
            <h:graphicImage style="border: 0; vertical-align: middle;" alt="#{newsBundle.new_sections_sticky_off}" url="/common/news/images/sticky_off.png" rendered="#{!node.sticky}" />
          </h:commandLink> 
        </h:panelGroup>
      </f:facet>
    </t:tree2>
  </t:div>
</jsp:root>
