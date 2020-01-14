package org.santfeliu.faces.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import static javax.faces.webapp.UIComponentTag.isValueReference;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author lopezrj
 */
public class HtmlGraphicImageTag extends UIComponentTag
{  
  private String value;
  private String url;
  private String style;
  private String styleClass;
  private String alt;
  private String title;
  private String height;
  private String width;    
  private String translator;
  private String translationGroup;   
  private String onclick;
  private String onmouseover;  

  public String getComponentType()
  {
    return "GraphicImage";
  }
  
  public String getRendererType()
  {
    return null;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }  
  
  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
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
  
  public String getAlt()
  {
    return alt;
  }

  public void setAlt(String alt)
  {
    this.alt = alt;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }
  
  public String getHeight()
  {
    return height;
  }

  public void setHeight(String height)
  {
    this.height = height;
  }

  public String getWidth()
  {
    return width;
  }

  public void setWidth(String width)
  {
    this.width = width;
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

  public String getOnclick()
  {
    return onclick;
  }

  public void setOnclick(String onclick)
  {
    this.onclick = onclick;
  }

  public String getOnmouseover()
  {
    return onmouseover;
  }

  public void setOnmouseover(String onmouseover)
  {
    this.onmouseover = onmouseover;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component, "value", value);
      UIComponentTagUtils.setStringProperty(context, component, "url", url);
      UIComponentTagUtils.setStringProperty(context, component, "style", style);
      UIComponentTagUtils.setStringProperty(context, component, "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(context, component, "alt", alt);
      UIComponentTagUtils.setStringProperty(context, component, "title", title);
      UIComponentTagUtils.setStringProperty(context, component, "height", height);
      UIComponentTagUtils.setStringProperty(context, component, "width", width);          
      UIComponentTagUtils.setStringProperty(context, component, "translationGroup", translationGroup);   
      UIComponentTagUtils.setStringProperty(context, component, "onclick", onclick);
      UIComponentTagUtils.setStringProperty(context, component, "onmouseover", onmouseover);  
      
      if (translator != null)
      {
        if (isValueReference(translator))
        {
          ValueBinding vb = context.getApplication().
            createValueBinding(translator);
          component.setValueBinding("translator", vb);
        }
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
    value = null;
    url = null;
    translator = null;
    translationGroup = null;   
    alt = null;
    title = null;
    style = null;
    styleClass = null;
    height = null;
    width = null;  
    onclick = null;
    onmouseover = null;
  }  
  
}
