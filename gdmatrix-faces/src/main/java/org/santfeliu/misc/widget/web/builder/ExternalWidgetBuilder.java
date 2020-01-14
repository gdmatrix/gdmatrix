package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.faces.component.HtmlObject;

/**
 *
 * @author blanquepa
 */
public class ExternalWidgetBuilder extends WidgetBuilder
{
  public ExternalWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    Map properties = widgetDef.getProperties();

    HtmlObject component = new HtmlObject();
    component.setValue(properties.get("value"));
    component.setHttpsDisableOnAgent((String)properties.get("httpsDisableOnAgent"));
    String disabledMessage = (String)properties.get("disabledMessage");
    if (disabledMessage != null)
    {
      if (isValueReference(disabledMessage))
        UIComponentTagUtils.setValueBinding(context, component, "disabledMessage",
          disabledMessage);
      else
        component.setDisabledMessage(disabledMessage);
    }
    return component;
  }
}
