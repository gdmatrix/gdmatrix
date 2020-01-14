package org.santfeliu.misc.widget.web.builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.div.Div;
import org.santfeliu.doc.faces.HtmlDocuments;
import org.santfeliu.misc.widget.web.WidgetDefinition;
import org.santfeliu.web.obj.util.ResultsManager;

/**
 *
 * @author blanquepa
 */
public class DocumentsWidgetBuilder extends WidgetBuilder
{
  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef, FacesContext context)
  {
    ResultsManager resultsManager = new ResultsManager(
      "org.santfeliu.doc.web.resources.DocumentBundle", "");
    resultsManager.setColumns(widgetDef.getMid());

    Div containerDiv = new Div();

    Map properties = widgetDef.getProperties();
    if (properties != null)
    {
      String widgetId = widgetDef.getWidgetId();
      String docTypeId = (String)properties.get("docTypeId");
      String propertyName = (String)properties.get("propertyName");
      String propertyValue = (String)properties.get("propertyValue");
      String searchMid = (String)properties.get("documentSearchMid");
      String directDownload = (String)properties.get("directDownload");
      String style = (String)properties.get("style");
      String styleClass = (String)properties.get("styleClass");
      if (styleClass == null) styleClass = "documentList";
      String pageSize = (String)properties.get("pageSize");
      int size = (pageSize != null ? Integer.parseInt(pageSize) : 9);
      List orderBy = widgetDef.getMultivaluedProperty("orderBy");

      HtmlDocuments component = new HtmlDocuments();
      component.setResultsManager(resultsManager);
      component.setRows(size);

      if (docTypeId != null)
        component.setDocTypeId(docTypeId);
      if (propertyName != null)
        component.setPropertyName(propertyName);
      if (propertyValue != null)
        component.setPropertyValues(Collections.singletonList(propertyValue));
      if (style != null)
        component.setStyle(style);
      if (styleClass != null)
        component.setStyleClass(styleClass);
      if (orderBy != null && !orderBy.isEmpty())
        component.setOrderBy(orderBy);
      if (directDownload != null)
        component.setDirectDownload(Boolean.valueOf(directDownload));

      return component;
    }

    return containerDiv;
  }
}
