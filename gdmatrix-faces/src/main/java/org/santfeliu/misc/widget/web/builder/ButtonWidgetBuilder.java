package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import org.santfeliu.faces.component.HtmlOutputLink;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.div.Div;
import org.santfeliu.faces.component.HtmlOutputText;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class ButtonWidgetBuilder extends WidgetBuilder
{
  public ButtonWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext facesContext)
  {
    Map properties = widgetDef.getProperties();

    String label = (String)properties.get("label");
    if (label == null) label = (String)properties.get("description");
    String ariaLabel = (String)properties.get("ariaLabel");
    String detail = (String)properties.get("detail");

    Application application = facesContext.getApplication();
    
    Div div = new Div();
    String divStyleClass = (String)properties.get("divStyleClass");
    div.setStyleClass(divStyleClass);

    HtmlOutputLink outputLink = new HtmlOutputLink();
    String url = (String)properties.get("url");
    String target = (String)properties.get("target");
    outputLink.setId(widgetDef.getWidgetId() + "_link");
    outputLink.setValue(url);
    outputLink.setTarget(target);
    if (ariaLabel != null || label != null)
    {
      String auxLabel = (ariaLabel != null ? ariaLabel.trim() : label.trim());
      if ("_blank".equals(target))
      {
        String openNewWindowLabel = 
          MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
        auxLabel += " (" + openNewWindowLabel + ")";
      }
      outputLink.setAriaLabel(auxLabel);
      outputLink.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      outputLink.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));
    }
    div.getChildren().add(outputLink);

    HtmlPanelGroup iconGroup = new HtmlPanelGroup();
    String iconStyle = (String)properties.get("iconStyle");  
    iconGroup.setId(widgetDef.getWidgetId() + "_icon");
    iconGroup.setStyle(iconStyle);
    iconGroup.setStyleClass("icon");
    outputLink.getChildren().add(iconGroup);

    if (label != null)
    {  
      HtmlOutputText labelOutputText = new HtmlOutputText();
      labelOutputText.setId(widgetDef.getWidgetId() + "_label");
      labelOutputText.setStyleClass("label");
      labelOutputText.setValue(label);
      labelOutputText.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      labelOutputText.setValueBinding("translationGroup",     
        application.createValueBinding("#{userSessionBean.translationGroup}"));
      outputLink.getChildren().add(labelOutputText);
    }

    if (detail != null)
    {
      HtmlOutputText detailOutputText = new HtmlOutputText();
      detailOutputText.setId(widgetDef.getWidgetId() + "_detail");
      detailOutputText.setStyleClass("detail");
      detailOutputText.setValue(detail);
      detailOutputText.setEscape(false);
      detailOutputText.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      detailOutputText.setValueBinding("translationGroup",     
        application.createValueBinding("#{userSessionBean.translationGroup}"));
      div.getChildren().add(detailOutputText);
    }
    return div;
  }
}
