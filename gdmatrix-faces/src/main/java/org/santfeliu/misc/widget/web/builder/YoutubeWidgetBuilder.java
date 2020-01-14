package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.faces.component.HtmlObject;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.HttpUtils;

/**
 *
 * @author realor
 */
public class YoutubeWidgetBuilder extends WidgetBuilder
{
  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    Map properties = widgetDef.getProperties();
    Map mutableProperties = new HashMap();
    mutableProperties.putAll(properties);

    String forceHttps = (String)mutableProperties.get("forceHttps");
    if (forceHttps != null && forceHttps.equalsIgnoreCase("true"))
    {
      HttpServletRequest request = 
        (HttpServletRequest)context.getExternalContext().getRequest();
      if (HttpUtils.isSecure(request))
      {
        String code = (String)mutableProperties.get("code");
        code = code.replaceAll("http://", "https://");
        mutableProperties.put("code", code);
      }
    }

    HtmlObject component = new HtmlObject();
    component.setValue(getValue(mutableProperties));
    component.setHttpsDisableOnAgent((String)mutableProperties.get("httpsDisableOnAgent"));
    String disabledMessage = (String)mutableProperties.get("disabledMessage");
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

  private Object getValue(Map properties)
  {
    try
    {
      InputStream is = getClass().getResourceAsStream("youtube.jsp");
      WebTemplate template = WebTemplate.create(new InputStreamReader(is));
      return template.merge(properties);
    }
    catch (IOException ex)
    {
      return null;
    }
  }
}
