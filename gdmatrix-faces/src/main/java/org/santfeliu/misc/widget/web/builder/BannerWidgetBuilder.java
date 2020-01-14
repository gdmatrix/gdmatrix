package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import org.santfeliu.faces.component.HtmlOutputLink;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.div.Div;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.faces.component.HtmlGraphicImage;

/**
 *
 * @author realor
 */
public class BannerWidgetBuilder extends WidgetBuilder
{
  public BannerWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext facesContext)
  {
    Application application = facesContext.getApplication();
    
    Map properties = widgetDef.getProperties();

    String widgetId = widgetDef.getWidgetId();
    String url = (String)properties.get("url");
    String target = (String)properties.get("target");
    String height = (String)properties.get("height");
    String imageUrl = (String)properties.get("imageUrl");    
    String label = widgetDef.getLabel();
    
    String widgetDescription = widgetDef.getAriaDescription();    

    Div bannerDiv = new Div();
    bannerDiv.setStyleClass("bannerDiv");
            
    if ("content".equals(widgetDef.getDragAreaPosition()))
    {
      Div dragAreaDiv = new Div();
      dragAreaDiv.setId("dragArea_" + widgetId);
      dragAreaDiv.setForceId(true);
      dragAreaDiv.setStyleClass("dragArea");
      bannerDiv.getChildren().add(dragAreaDiv);
    }
    
    if ("content".equals(widgetDef.getCloseLinkPosition()))
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
        bannerDiv.getChildren().add(closeLink);
      }
    }    
    
    HtmlOutputLink outputLink = new HtmlOutputLink();
    outputLink.setValue(url);
    String title = "#{userSessionBean.menuModel.items['" + widgetDef.getMid() + "'].translatedProperties.label}";
    UIComponentTagUtils.setValueBinding(facesContext, outputLink, "title", title);    
    outputLink.setTarget(target);
    String style = "display:block;width:100%;";
    if (height != null) style += "height:" + height;
    outputLink.setStyle(style);
    outputLink.setStyleClass("bannerLink");

    if (imageUrl != null)
    {
      HtmlGraphicImage image = new HtmlGraphicImage();
      image.setId(widgetId + "_image");
      image.setAlt(label);
      image.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      image.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));
      image.setUrl(imageUrl);
      outputLink.getChildren().add(image);      
    }
    bannerDiv.getChildren().add(outputLink);     
    
    return bannerDiv;
  }
}
