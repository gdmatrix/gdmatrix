package org.santfeliu.misc.widget.web.builder;

import java.util.List;
import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.faces.imagescarousel.HtmlImagesCarousel;

/**
 *
 * @author lopezrj
 */
public class ImagesCarouselWidgetBuilder extends WidgetBuilder
{
  private static final int DEFAULT_THUMBNAIL_COUNT = 10;
  private static final int DEFAULT_THUMBNAIL_WINDOW = 4;  
  private static final int DEFAULT_TRANSITION_TIME = 0; //no transition
  private static final int DEFAULT_CONTINUE_TIME = 0; //no continue
  private static final String DEFAULT_THUMBNAIL_SHIFT_MODE = "thumbnail";  
  private static final String DEFAULT_THUMBNAIL_HOVER_MODE = "select";  
  private static final String DEFAULT_THUMBNAIL_CLICK_MODE = "selectAndOpen";  
  private static final String DEFAULT_THUMBNAIL_PREV_LABEL = 
    "Mostrar miniatura anterior";
  private static final String DEFAULT_THUMBNAIL_NEXT_LABEL = 
    "Mostrar miniatura següent";  
  private static final String DEFAULT_MAIN_IMAGE_CLICK_MODE = "open";  
  private static final String DEFAULT_MAIN_IMAGE_WIDTH = "0";
  private static final String DEFAULT_MAIN_IMAGE_HEIGHT = "0";
  private static final String DEFAULT_MAIN_IMAGE_CROP = "auto";
  private static final String DEFAULT_THUMBNAIL_WIDTH = "0";
  private static final String DEFAULT_THUMBNAIL_HEIGHT = "0";
  private static final String DEFAULT_THUMBNAIL_CROP = "auto";
  
  private static final String DEFAULT_STYLE_CLASS = "imagesCarousel";
  //private static final String DEFAULT_VAR = "i";

  public ImagesCarouselWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    HtmlImagesCarousel component = new HtmlImagesCarousel();
    
    component.getAttributes().put("nodeId", widgetDef.getMid());        
    
    Map properties = widgetDef.getProperties();
    if (properties != null)
    {
      //caseId
      String caseId = (String)properties.get("caseId");
      if (isValueReference(caseId))
        UIComponentTagUtils.setValueBinding(context, component, "caseId",
          caseId);
      else
        component.setCaseId(caseId);
      
      //newId
      String newId = (String)properties.get("newId");
      if (isValueReference(newId))
        UIComponentTagUtils.setValueBinding(context, component, "newId",
          newId);
      else
        component.setNewId(newId);

      //docId
      String docId = 
        (String)properties.get("docId");
      if (isValueReference((String)properties.get("docId")))
        UIComponentTagUtils.setValueBinding(context, component, 
          "docId", docId);
      else
      {
        List<String> docIdList = 
          widgetDef.getMultivaluedProperty("docId");
        component.setDocId(docIdList);
      }
      
      //thumbnail count
      String thumbnailCount = (String)properties.get("thumbnailCount");
      if (thumbnailCount != null)
        component.setThumbnailCount(Integer.valueOf(thumbnailCount).intValue());
      else
        component.setThumbnailCount(DEFAULT_THUMBNAIL_COUNT);

      //thumbnailWindow
      String thumbnailWindow = (String)properties.get("thumbnailWindow");
      if (thumbnailWindow != null)
        component.setThumbnailWindow(Integer.valueOf(thumbnailWindow).intValue());
      else
        component.setThumbnailWindow(DEFAULT_THUMBNAIL_WINDOW);

      //transitionTime
      String transitionTime = (String)properties.get("transitionTime");
      if (transitionTime != null)
        component.setTransitionTime(Integer.valueOf(transitionTime).intValue());
      else
        component.setTransitionTime(DEFAULT_TRANSITION_TIME);

      //continueTime
      String continueTime = (String)properties.get("continueTime");
      if (continueTime != null)
        component.setContinueTime(Integer.valueOf(continueTime).intValue());
      else
        component.setContinueTime(DEFAULT_CONTINUE_TIME);
      
      //Translation properties
      setTranslationProperties(component, properties, "imagesCarousel", context); 

      //style
      component.setStyle((String)properties.get("imagesCarouselStyle"));

      //style class
      String styleClass = (String)properties.get("imagesCarouselStyleClass");
      if (styleClass == null) styleClass = DEFAULT_STYLE_CLASS;
      component.setStyleClass(styleClass);      

      //var
      String var = (String)properties.get("var");
      //if (var == null) var = DEFAULT_VAR;
      component.setVar(var);

      //image properties
      String mainImageWidth = (String)properties.get("mainImageWidth");
      if (mainImageWidth == null) mainImageWidth = DEFAULT_MAIN_IMAGE_WIDTH;
      component.setMainImageWidth(mainImageWidth);
      String mainImageHeight = (String)properties.get("mainImageHeight");
      if (mainImageHeight == null) mainImageHeight = DEFAULT_MAIN_IMAGE_HEIGHT;
      component.setMainImageHeight(mainImageHeight);
      String mainImageCrop = (String)properties.get("mainImageCrop");
      if (mainImageCrop == null) mainImageCrop = DEFAULT_MAIN_IMAGE_CROP;
      component.setMainImageCrop(mainImageCrop);
      
      //thumbnails properties
      String thumbnailWidth = (String)properties.get("thumbnailWidth");
      if (thumbnailWidth == null) thumbnailWidth = DEFAULT_THUMBNAIL_WIDTH;
      component.setThumbnailWidth(thumbnailWidth);
      String thumbnailHeight = (String)properties.get("thumbnailHeight");
      if (thumbnailHeight == null) thumbnailHeight = DEFAULT_THUMBNAIL_HEIGHT;
      component.setThumbnailHeight(thumbnailHeight);
      String thumbnailCrop = (String)properties.get("thumbnailCrop");
      if (thumbnailCrop == null) thumbnailCrop = DEFAULT_THUMBNAIL_CROP;
      component.setThumbnailCrop(thumbnailCrop);
      String thumbnailPrevLabel = (String)properties.get("thumbnailPrevLabel");
      if (thumbnailPrevLabel == null) 
        thumbnailPrevLabel = DEFAULT_THUMBNAIL_PREV_LABEL;
      component.setThumbnailPrevLabel(thumbnailPrevLabel);
      String thumbnailNextLabel = (String)properties.get("thumbnailNextLabel");
      if (thumbnailNextLabel == null) 
        thumbnailNextLabel = DEFAULT_THUMBNAIL_NEXT_LABEL;
      component.setThumbnailNextLabel(thumbnailNextLabel);
      String thumbnailPrevIconUrl = 
        (String)properties.get("thumbnailPrevIconUrl");
      component.setThumbnailPrevIconUrl(thumbnailPrevIconUrl);
      String thumbnailNextIconUrl = 
        (String)properties.get("thumbnailNextIconUrl");
      component.setThumbnailNextIconUrl(thumbnailNextIconUrl);
      
      //render properties
      String renderMainImage = (String)properties.get("renderMainImage");
      if (renderMainImage == null) renderMainImage = "true";
      component.setRenderMainImage(Boolean.valueOf(renderMainImage));      
      
      String renderThumbnails = (String)properties.get("renderThumbnails");
      if (renderThumbnails == null) renderThumbnails = "true";
      component.setRenderThumbnails(Boolean.valueOf(renderThumbnails));
      
      String renderNavLinks = (String)properties.get("renderNavLinks");
      if (renderNavLinks == null) renderNavLinks = "true";
      component.setRenderNavLinks(Boolean.valueOf(renderNavLinks));

      //Behavior
      String thumbnailShiftMode = (String)properties.get("thumbnailShiftMode");
      if (thumbnailShiftMode == null) thumbnailShiftMode = DEFAULT_THUMBNAIL_SHIFT_MODE;
      component.setThumbnailShiftMode(thumbnailShiftMode);      
      
      String thumbnailHoverMode = (String)properties.get("thumbnailHoverMode");
      if (thumbnailHoverMode == null) thumbnailHoverMode = DEFAULT_THUMBNAIL_HOVER_MODE;
      component.setThumbnailHoverMode(thumbnailHoverMode);      

      String thumbnailClickMode = (String)properties.get("thumbnailClickMode");
      if (thumbnailClickMode == null) thumbnailClickMode = DEFAULT_THUMBNAIL_CLICK_MODE;
      component.setThumbnailClickMode(thumbnailClickMode);      

      String mainImageClickMode = (String)properties.get("mainImageClickMode");
      if (mainImageClickMode == null) mainImageClickMode = DEFAULT_MAIN_IMAGE_CLICK_MODE;
      component.setMainImageClickMode(mainImageClickMode);      
    }
    return component;
  }
}
