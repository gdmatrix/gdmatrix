package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import org.santfeliu.faces.component.HtmlGraphicImage;
import org.santfeliu.faces.component.HtmlOutputLink;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.div.Div;
import org.santfeliu.faces.component.HtmlOutputText;

/**
 *
 * @author realor
 */
public class FooterWidgetBuilder extends WidgetBuilder
{
  private static final String FOOTER_MORE_INFO_ARIA_LABEL = 
    "Ampliació d'informació del widget";
  private static final String FOOTER_RSS_ARIA_LABEL = 
    "Accedir al canal RSS del widget";
  
  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    Application application = context.getApplication();
    
    Map properties = widgetDef.getProperties();

    String widgetId = widgetDef.getWidgetId();
    Div div = new Div();
    String footerStyleClass = (String)properties.get("footerStyleClass");
    if (footerStyleClass != null)
    {
      div.setStyleClass(footerStyleClass);
    }
    
    String widgetDescription = widgetDef.getAriaDescription();

    String showMoreURL = (String)properties.get("showMoreURL");
    if (showMoreURL != null)
    {
      HtmlOutputLink showMoreOutputLink = new HtmlOutputLink();
      showMoreOutputLink.setId(widgetId + "_showMore");
      showMoreOutputLink.setValue(showMoreURL);
      showMoreOutputLink.setStyleClass("showMoreLink");
      String showMoreTarget = (String)properties.get("showMoreTarget");
      if (showMoreTarget != null)
      {
        showMoreOutputLink.setTarget(showMoreTarget);
      }
      String showMoreText = (String)properties.get("showMoreText");
      if (showMoreText == null) showMoreText = "INFO";
      
      showMoreOutputLink.setTitle(FOOTER_MORE_INFO_ARIA_LABEL + ": " + 
        widgetDescription);
      showMoreOutputLink.setAriaLabel(FOOTER_MORE_INFO_ARIA_LABEL + ": " + 
        widgetDescription);
      showMoreOutputLink.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      showMoreOutputLink.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));
      
      HtmlOutputText showMoreOutputText = new HtmlOutputText();
      showMoreOutputText.setId(widgetId + "_showMoreText");
      showMoreOutputText.setValue(showMoreText);
      setTranslationProperties(showMoreOutputText, properties, "widget", 
        context);

      showMoreOutputLink.getChildren().add(showMoreOutputText);
      div.getChildren().add(showMoreOutputLink);
    }

    String rssURL = (String)properties.get("rssURL");
    String rssImageURL = (String)properties.get("rssImageURL");
    if (rssURL != null && rssImageURL != null)
    {
      HtmlOutputLink rssOutputLink = new HtmlOutputLink();
      rssOutputLink.setId(widgetId + "_rssLink");
      rssOutputLink.setValue(rssURL);
      rssOutputLink.setStyleClass("rssLink");

      String ariaLabel = FOOTER_RSS_ARIA_LABEL + ": " + widgetDescription;

      rssOutputLink.setTitle(ariaLabel);
      rssOutputLink.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      rssOutputLink.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));      
      
      HtmlGraphicImage image = new HtmlGraphicImage();
      image.setId(widgetId + "_rssImg");      
      image.setAlt(ariaLabel);
      image.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      image.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));      
      image.setUrl(rssImageURL);
      rssOutputLink.getChildren().add(image);
      div.getChildren().add(rssOutputLink);
    }
        
    if ("footer".equals(widgetDef.getCloseLinkPosition()))
    {
      String closeImageURL = (String)properties.get("closeImageURL");    
      if (closeImageURL != null && !"none".equals(closeImageURL))
      {
        HtmlOutputLink closeLink = new HtmlOutputLink();
        closeLink.setId(widgetId + "_close");        
        closeLink.setStyleClass("widget_close");
        closeLink.setValue("#");
        closeLink.setOnclick("removeWidget('" + widgetId + "');return false;");

        String closeText = (String)properties.get("closeText");
        if (closeText == null)
        {
          closeText = "Close widget";
        }

        closeLink.setTitle(closeText + ": " + widgetDescription);
        closeLink.setValueBinding("translator",
          application.createValueBinding("#{userSessionBean.translator}"));
        closeLink.setValueBinding("translationGroup",
          application.createValueBinding("#{userSessionBean.translationGroup}"));
        
        HtmlGraphicImage image = new HtmlGraphicImage();
        image.setId(widgetId + "_closeImg");
        image.setUrl(closeImageURL);        
        image.setAlt(closeText + ": " + widgetDescription);
        image.setValueBinding("translator",
          application.createValueBinding("#{userSessionBean.translator}"));
        image.setValueBinding("translationGroup",
          application.createValueBinding("#{userSessionBean.translationGroup}"));

        closeLink.getChildren().add(image);
        div.getChildren().add(closeLink);
      }
    }    
    if (isFolded())
    {
      div.setStyle("display: none;");
    }
    else
    {
      div.setStyle("display: block;");
    }                
    return div;
  }
}
