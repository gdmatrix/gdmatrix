package org.santfeliu.faces.dynamicform;

import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.el.SimpleActionMethodBinding;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author realor
 */
public class DynamicFormTag extends UIComponentTag
{
  String form;
  String value;
  String rendererTypes;
  String translator;
  String translationGroup;
  String action;
  String actionListener;

  @Override
  public String getComponentType()
  {
    return "DynamicForm";
  }

  @Override
  public String getRendererType()
  {
    return "HtmlFormRenderer";
  }

  public String getForm()
  {
    return form;
  }

  public void setForm(String form)
  {
    this.form = form;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getRendererTypes()
  {
    return rendererTypes;
  }

  public void setRendererTypes(String rendererTypes)
  {
    this.rendererTypes = rendererTypes;
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

  public String getAction()
  {
    return action;
  }

  public void setAction(String action)
  {
    this.action = action;
  }

  public String getActionListener()
  {
    return actionListener;
  }

  public void setActionListener(String actionListener)
  {
    this.actionListener = actionListener;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);

      UIComponentTagUtils.setStringProperty(context, component,
        "rendererTypes", rendererTypes);
      UIComponentTagUtils.setStringProperty(context, component,
        "translationGroup", translationGroup);
      if (form != null)
      {
        if (isValueReference(form))
        {
          ValueBinding vb = context.getApplication().createValueBinding(form);
          component.setValueBinding("form", vb);
        }
      }
      UIComponentTagUtils.setValueProperty(context, component, value);
      if (translator != null)
      {
        if (isValueReference(translator))
        {
          ValueBinding vb = context.getApplication().
            createValueBinding(translator);
          component.setValueBinding("translator", vb);
        }
      }
      if (action != null)
      {
        MethodBinding mb;
        if (isValueReference(action))
        {
          mb = context.getApplication().createMethodBinding(action, new Class[0]);
        }
        else
        {
          mb = new SimpleActionMethodBinding(action);
        }
        ((ActionSource)component).setAction(mb);
      }
      if (actionListener != null)
      {
        MethodBinding mb = context.getApplication().createMethodBinding(
          actionListener, new Class[]{javax.faces.event.ActionEvent.class});
        ((ActionSource)component).setActionListener(mb);
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
    form = null;
    rendererTypes = null;
    value = null;
    translator = null;
    translationGroup = null;
    action = null;
    actionListener = null;
  }
}
