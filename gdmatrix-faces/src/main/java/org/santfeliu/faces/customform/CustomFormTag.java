package org.santfeliu.faces.customform;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

public class CustomFormTag extends UIComponentTag
{
  private String url;
  private String values;
  private String newValues;
  private String translator;
  private String translationGroup;

  public CustomFormTag()
  {
  }
  
  public String getComponentType()
  {
    return "CustomForm";
  }

  public String getRendererType()
  {
    return null;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public void setValues(String values)
  {
    this.values = values;
  }

  public String getValues()
  {
    return values;
  }

  public void setNewValues(String newValues)
  {
    this.newValues = newValues;
  }

  public String getNewValues()
  {
    return newValues;
  }

  public void setTranslator(String translator)
  {
    this.translator = translator;
  }

  public String getTranslator()
  {
    return translator;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this.translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component, "url", url);
      UIComponentTagUtils.setStringProperty(context, component, 
        "translationGroup", translationGroup);
      if (values != null)
      {
        if (isValueReference(values))
        {
          ValueBinding vb = context.getApplication().createValueBinding(values);
          component.setValueBinding("values", vb);
        }
      }
      if (newValues != null)
      {
        if (isValueReference(newValues))
        {
          ValueBinding vb = context.getApplication().
            createValueBinding(newValues);
          component.setValueBinding("newValues", vb);
        }
      }
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
    }
  }

  @Override
  public void release()
  {
    super.release();
    url = null;
    values = null;
    newValues = null;
    translator = null;
    translationGroup = null;
  }
}
