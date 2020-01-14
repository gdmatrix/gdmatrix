package org.santfeliu.faces.imagescarousel;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import static javax.faces.webapp.UIComponentTag.isValueReference;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author lopezrj
 */
public class HtmlImagesCarouselTag extends UIComponentTag
{
  private String caseId;
  private String newId;
  private String docId;
  private String thumbnailCount;
  private String thumbnailWindow;
  private String transitionTime;
  private String continueTime;
  private String translator;
  private String translationGroup;
  private String style;
  private String styleClass;
  private String var;
  private String mainImageWidth;
  private String mainImageHeight;
  private String mainImageCrop;
  private String thumbnailWidth;
  private String thumbnailHeight;
  private String thumbnailCrop;
  private String renderMainImage;
  private String renderThumbnails;
  private String renderNavLinks;
  private String thumbnailShiftMode;
  private String thumbnailHoverMode;
  private String thumbnailClickMode;
  private String mainImageClickMode;
  private String imageId;

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getNewId()
  {
    return newId;
  }

  public void setNewId(String newId)
  {
    this.newId = newId;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }
  
  public String getThumbnailCount()
  {
    return thumbnailCount;
  }

  public void setThumbnailCount(String thumbnailCount)
  {
    this.thumbnailCount = thumbnailCount;
  }

  public String getThumbnailWindow()
  {
    return thumbnailWindow;
  }

  public void setThumbnailWindow(String thumbnailWindow)
  {
    this.thumbnailWindow = thumbnailWindow;
  }

  public String getTransitionTime()
  {
    return transitionTime;
  }

  public void setTransitionTime(String transitionTime)
  {
    this.transitionTime = transitionTime;
  }

  public String getContinueTime()
  {
    return continueTime;
  }

  public void setContinueTime(String continueTime)
  {
    this.continueTime = continueTime;
  }

  public String getTranslator()
  {
    return translator;
  }

  public void setTranslator(String translator)
  {
    this.translator = translator;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this.translationGroup = translationGroup;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getVar()
  {
    return var;
  }

  public void setVar(String var)
  {
    this.var = var;
  }

  public String getMainImageWidth()
  {
    return mainImageWidth;
  }

  public void setMainImageWidth(String mainImageWidth)
  {
    this.mainImageWidth = mainImageWidth;
  }

  public String getMainImageHeight()
  {
    return mainImageHeight;
  }

  public void setMainImageHeight(String mainImageHeight)
  {
    this.mainImageHeight = mainImageHeight;
  }

  public String getMainImageCrop()
  {
    return mainImageCrop;
  }

  public void setMainImageCrop(String mainImageCrop)
  {
    this.mainImageCrop = mainImageCrop;
  }

  public String getThumbnailWidth()
  {
    return thumbnailWidth;
  }

  public void setThumbnailWidth(String thumbnailWidth)
  {
    this.thumbnailWidth = thumbnailWidth;
  }

  public String getThumbnailHeight()
  {
    return thumbnailHeight;
  }

  public void setThumbnailHeight(String thumbnailHeight)
  {
    this.thumbnailHeight = thumbnailHeight;
  }

  public String getThumbnailCrop()
  {
    return thumbnailCrop;
  }

  public void setThumbnailCrop(String thumbnailCrop)
  {
    this.thumbnailCrop = thumbnailCrop;
  }

  public String getRenderMainImage()
  {
    return renderMainImage;
  }

  public void setRenderMainImage(String renderMainImage)
  {
    this.renderMainImage = renderMainImage;
  }

  public String getRenderThumbnails()
  {
    return renderThumbnails;
  }

  public void setRenderThumbnails(String renderThumbnails)
  {
    this.renderThumbnails = renderThumbnails;
  }

  public String getRenderNavLinks()
  {
    return renderNavLinks;
  }

  public void setRenderNavLinks(String renderNavLinks)
  {
    this.renderNavLinks = renderNavLinks;
  }

  public String getThumbnailShiftMode()
  {
    return thumbnailShiftMode;
  }

  public void setThumbnailShiftMode(String thumbnailShiftMode)
  {
    this.thumbnailShiftMode = thumbnailShiftMode;
  }

  public String getThumbnailHoverMode()
  {
    return thumbnailHoverMode;
  }

  public void setThumbnailHoverMode(String thumbnailHoverMode)
  {
    this.thumbnailHoverMode = thumbnailHoverMode;
  }

  public String getThumbnailClickMode()
  {
    return thumbnailClickMode;
  }

  public void setThumbnailClickMode(String thumbnailClickMode)
  {
    this.thumbnailClickMode = thumbnailClickMode;
  }

  public String getMainImageClickMode()
  {
    return mainImageClickMode;
  }

  public void setMainImageClickMode(String mainImageClickMode)
  {
    this.mainImageClickMode = mainImageClickMode;
  }

  public String getImageId()
  {
    return imageId;
  }

  public void setImageId(String imageId)
  {
    this.imageId = imageId;
  }
  
  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      if (caseId != null)
      {
        if (isValueReference(caseId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(caseId);
          component.setValueBinding("caseId", vb);
        }
        else
        {
          UIComponentTagUtils.setStringProperty(
            context, component, "caseId", caseId);
        }
      }
      if (newId != null)
      {
        if (isValueReference(newId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(newId);
          component.setValueBinding("newId", vb);
        }
        else
        {
          UIComponentTagUtils.setStringProperty(
            context, component, "newId", newId);
        }
      }
      if (docId != null)
      {
        if (isValueReference(docId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(docId);
          component.setValueBinding("docId", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(docId, ",");
          component.getAttributes().put("docId", list);
        }
      } 
      if (imageId != null)
      {
        if (isValueReference(imageId))
        {
          ValueBinding vb = context.getApplication().createValueBinding(imageId);
          component.setValueBinding("imageId", vb);
        }
        else
        {
          List<String> list = TextUtils.stringToList(imageId, ",");
          component.getAttributes().put("imageId", list);
        }
      }
      if (thumbnailCount != null)
      {
        if (isValueReference(thumbnailCount))
        {
          ValueBinding vb = context.getApplication().createValueBinding(thumbnailCount);
          component.setValueBinding("thumbnailCount", vb);
        }
        else
          UIComponentTagUtils.setIntegerProperty(
            context, component, "thumbnailCount", thumbnailCount);      
      }            
      if (thumbnailWindow != null)
      {
        if (isValueReference(thumbnailWindow))
        {
          ValueBinding vb = context.getApplication().createValueBinding(thumbnailWindow);
          component.setValueBinding("thumbnailWindow", vb);
        }
        else
          UIComponentTagUtils.setIntegerProperty(
            context, component, "thumbnailWindow", thumbnailWindow);      
      }            
      if (transitionTime != null)
      {
        if (isValueReference(transitionTime))
        {
          ValueBinding vb = context.getApplication().createValueBinding(transitionTime);
          component.setValueBinding("transitionTime", vb);
        }
        else
          UIComponentTagUtils.setIntegerProperty(
            context, component, "transitionTime", transitionTime);      
      }            
      if (continueTime != null)
      {
        if (isValueReference(continueTime))
        {
          ValueBinding vb = context.getApplication().createValueBinding(continueTime);
          component.setValueBinding("continueTime", vb);
        }
        else
          UIComponentTagUtils.setIntegerProperty(
            context, component, "continueTime", continueTime);      
      }            
      if (translator != null)
      {
        if (isValueReference(translator))
        {
          ValueBinding vb = context.getApplication().createValueBinding(translator);
          component.setValueBinding("translator", vb);
        }
      }
      if (translationGroup != null)
      {
        if (isValueReference(translationGroup))
        {
          ValueBinding vb = context.getApplication().createValueBinding(translationGroup);
          component.setValueBinding("translationGroup", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "translationGroup", translationGroup);      
      }                        
      if (style != null)
      {
        if (isValueReference(style))
        {
          ValueBinding vb = context.getApplication().createValueBinding(style);
          component.setValueBinding("style", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "style", style);      
      }                  
      if (styleClass != null)
      {
        if (isValueReference(styleClass))
        {
          ValueBinding vb = context.getApplication().createValueBinding(styleClass);
          component.setValueBinding("styleClass", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "styleClass", styleClass);      
      }                  
      UIComponentTagUtils.setStringProperty(
        context, component, "var", var);
      if (mainImageWidth != null)
      {
        if (isValueReference(mainImageWidth))
        {
          ValueBinding vb = context.getApplication().createValueBinding(mainImageWidth);
          component.setValueBinding("mainImageWidth", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "mainImageWidth", mainImageWidth);      
      }                        
      if (mainImageHeight != null)
      {
        if (isValueReference(mainImageHeight))
        {
          ValueBinding vb = context.getApplication().createValueBinding(mainImageHeight);
          component.setValueBinding("mainImageHeight", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "mainImageHeight", mainImageHeight);      
      }                        
      if (mainImageCrop != null)
      {
        if (isValueReference(mainImageCrop))
        {
          ValueBinding vb = context.getApplication().createValueBinding(mainImageCrop);
          component.setValueBinding("mainImageCrop", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "mainImageCrop", mainImageCrop);      
      }                         
      if (thumbnailWidth != null)
      {
        if (isValueReference(thumbnailWidth))
        {
          ValueBinding vb = context.getApplication().createValueBinding(thumbnailWidth);
          component.setValueBinding("thumbnailWidth", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "thumbnailWidth", thumbnailWidth);      
      }                        
      if (thumbnailHeight != null)
      {
        if (isValueReference(thumbnailHeight))
        {
          ValueBinding vb = context.getApplication().createValueBinding(thumbnailHeight);
          component.setValueBinding("thumbnailHeight", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "thumbnailHeight", thumbnailHeight);      
      }                        
      if (thumbnailCrop != null)
      {
        if (isValueReference(thumbnailCrop))
        {
          ValueBinding vb = context.getApplication().createValueBinding(thumbnailCrop);
          component.setValueBinding("thumbnailCrop", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "thumbnailCrop", thumbnailCrop);      
      }                             
      if (renderMainImage != null)
      {
        if (isValueReference(renderMainImage))
        {
          ValueBinding vb = context.getApplication().createValueBinding(renderMainImage);
          component.setValueBinding("renderMainImage", vb);
        }
        else
          UIComponentTagUtils.setBooleanProperty(
            context, component, "renderMainImage", renderMainImage);      
      }                        
      if (renderThumbnails != null)
      {
        if (isValueReference(renderThumbnails))
        {
          ValueBinding vb = context.getApplication().createValueBinding(renderThumbnails);
          component.setValueBinding("renderThumbnails", vb);
        }
        else
          UIComponentTagUtils.setBooleanProperty(
            context, component, "renderThumbnails", renderThumbnails);      
      }                        
      if (renderNavLinks != null)
      {
        if (isValueReference(renderNavLinks))
        {
          ValueBinding vb = context.getApplication().createValueBinding(renderNavLinks);
          component.setValueBinding("renderNavLinks", vb);
        }
        else
          UIComponentTagUtils.setBooleanProperty(
            context, component, "renderNavLinks", renderNavLinks);      
      }                        
      if (thumbnailShiftMode != null)
      {
        if (isValueReference(thumbnailShiftMode))
        {
          ValueBinding vb = context.getApplication().createValueBinding(thumbnailShiftMode);
          component.setValueBinding("thumbnailShiftMode", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "thumbnailShiftMode", thumbnailShiftMode);      
      }                        
      if (thumbnailHoverMode != null)
      {
        if (isValueReference(thumbnailHoverMode))
        {
          ValueBinding vb = context.getApplication().createValueBinding(thumbnailHoverMode);
          component.setValueBinding("thumbnailHoverMode", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "thumbnailHoverMode", thumbnailHoverMode);      
      }                        
      if (thumbnailClickMode != null)
      {
        if (isValueReference(thumbnailClickMode))
        {
          ValueBinding vb = context.getApplication().createValueBinding(thumbnailClickMode);
          component.setValueBinding("thumbnailClickMode", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "thumbnailClickMode", thumbnailClickMode);      
      }                        
      if (mainImageClickMode != null)
      {
        if (isValueReference(mainImageClickMode))
        {
          ValueBinding vb = context.getApplication().createValueBinding(mainImageClickMode);
          component.setValueBinding("mainImageClickMode", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "mainImageClickMode", mainImageClickMode);      
      }                        
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void release()
  {
    super.release();
    caseId = null;
    newId = null;
    docId = null;
    thumbnailCount = null;
    thumbnailWindow = null;
    transitionTime = null;
    continueTime = null;
    translator = null;
    translationGroup = null;
    style = null;
    styleClass = null;
    var = null;
    mainImageWidth = null;
    mainImageHeight = null;
    mainImageCrop = null;
    thumbnailWidth = null;
    thumbnailHeight = null;
    thumbnailCrop = null;
    renderMainImage = null;
    renderThumbnails = null;
    renderNavLinks = null;
    thumbnailShiftMode = null;
    thumbnailHoverMode = null;
    thumbnailClickMode = null;
    mainImageClickMode = null;
    imageId = null;
  }

  @Override
  public String getComponentType()
  {
    return "ImagesCarousel";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }
}
