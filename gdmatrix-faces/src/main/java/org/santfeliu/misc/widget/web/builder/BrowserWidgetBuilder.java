package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.santfeliu.faces.browser.HtmlBrowser;

/**
 *
 * @author realor
 */
public class BrowserWidgetBuilder extends WidgetBuilder
{
  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    Map properties = widgetDef.getProperties();

    String url = (String)properties.get("url");
    HtmlBrowser browser = new HtmlBrowser();
    browser.setUrl(url);

    setTranslationProperties(browser, properties, 
      getStrictTranslationGroup(widgetDef, "browser"), context);

    return browser;
  }  
}
