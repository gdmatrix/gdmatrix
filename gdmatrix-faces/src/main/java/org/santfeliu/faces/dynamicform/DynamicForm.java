package org.santfeliu.faces.dynamicform;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.ActionSource;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import org.santfeliu.faces.Translator;
import org.santfeliu.form.Form;
import org.santfeliu.faces.dynamicform.render.FormRenderer;

/**
 *
 * @author realor
 */
public class DynamicForm extends UIInput implements ActionSource
{
  public static final String INVALID_VALUE = "dform.INVALID_VALUE";
  public static final String REQUIRED_VALUE = "dform.REQUIRED_VALUE";

  private Form _form;
  private String _rendererTypes;
  private Translator _translator;
  private String _translationGroup;
  private MethodBinding _action;
  private MethodBinding _actionListener;
  private Form renderedForm;

  private static final String FAMILY = "DynamicForm";
  private static final String RENDERER_TYPE_SELECTED =
   "DynamicForm.rendererTypeSelected";

  @Override
  public String getFamily()
  {
    return FAMILY;
  }

  public MethodBinding getAction()
  {
    return _action;
  }

  public void setAction(MethodBinding mb)
  {
    this._action = mb;
  }

  public MethodBinding getActionListener()
  {
    return _actionListener;
  }

  public void setActionListener(MethodBinding mb)
  {
    this._actionListener = mb;
  }

  public void addActionListener(ActionListener listener)
  {
    addFacesListener(listener);
  }

  public void removeActionListener(ActionListener listener)
  {
    removeFacesListener(listener);
  }

  public ActionListener[] getActionListeners()
  {
    return (ActionListener[])getFacesListeners(ActionListener.class);
  }

  @Override
  public void broadcast(FacesEvent event) throws AbortProcessingException
  {
    super.broadcast(event);
    if (event instanceof ActionEvent)
    {
      FacesContext context = getFacesContext();      
      MethodBinding actionListener = getActionListener();
      if (actionListener != null)
      {
        actionListener.invoke(context, new Object[]{event});
      }
      ActionListener defaultActionListener =
        context.getApplication().getActionListener();
      if (defaultActionListener != null)
      {
        defaultActionListener.processAction((ActionEvent)event);
      }
    }
  }

  @Override
  public String getRendererType()
  {
    FacesContext facesContext = getFacesContext();
    if (facesContext.getRenderResponse())
    {
      Map requestMap = facesContext.getExternalContext().getRequestMap();
      if (requestMap.get(RENDERER_TYPE_SELECTED + "." + getId()) == null)
      {
        requestMap.put(RENDERER_TYPE_SELECTED + "." + getId(), "true");
        String rendererTypes = getRendererTypes();
        if (rendererTypes != null)
        {
          setRendererType(chooseBestRendererType(rendererTypes));          
        }
      }
    }
    return super.getRendererType();
  }

  @Override
  public void validate(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    Object submittedValue = getSubmittedValue();
    if (submittedValue == null) return;

    // conversion delegated to FormRenderer
    Object convertedValue = getConvertedValue(context, submittedValue);

    if (!isValid()) return;

    // validate values via Form
    validateForm(context, convertedValue);

    if (!isValid()) return;

    // check custom validators
    validateValue(context, convertedValue);

    if (!isValid()) return;

    Object previousValue = getValue();
    // set local value
    setValue(convertedValue);
    // clear submited value
    setSubmittedValue(null);

    // fire change event
    if (compareValues(previousValue, convertedValue))
    {
      queueEvent(new ValueChangeEvent(this, previousValue, convertedValue));
    }
  }

  public void setForm(Form form)
  {
    this._form = form;
  }

  public Form getForm()
  {
    if (_form != null) return _form;

    if (getFacesContext().getRenderResponse())
    {
      Map requestMap = getFacesContext().getExternalContext().getRequestMap();
      //Avoid using the same renderedForm for multiple instances 
      Object formEncoded = requestMap.get("formEncoded");
      if (formEncoded != null && (Boolean)formEncoded)
        renderedForm = null;
      
      if (renderedForm == null)
      {
        ValueBinding vb = getValueBinding("form");
        renderedForm = vb != null ? (Form)vb.getValue(getFacesContext()) : null;
      }
      return renderedForm;
    }
    else
    {
      renderedForm = null;
      ValueBinding vb = getValueBinding("form");
      return vb != null ? (Form)vb.getValue(getFacesContext()) : null;
    }
  }
  
  public void setRendererTypes(String rendererTypes)
  {
    this._rendererTypes = rendererTypes;
  }

  public String getRendererTypes()
  {
    if (_rendererTypes != null) return _rendererTypes;
    ValueBinding vb = getValueBinding("rendererTypes");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public Translator getTranslator()
  {
    if (_translator != null) return _translator;
    ValueBinding vb = getValueBinding("translator");
    return vb != null ? (Translator)vb.getValue(getFacesContext()) : null;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null) return _translationGroup;
    ValueBinding vb = getValueBinding("translationGroup");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[5];
    values[0] = super.saveState(context);
    values[1] = _rendererTypes;
    values[2] = _translationGroup;
    values[3] = saveAttachedState(context, _action);
    values[4] = saveAttachedState(context, _actionListener);
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _rendererTypes = (String)values[1];
    _translationGroup = (String)values[2];
    _action = (MethodBinding)restoreAttachedState(context, values[3]);
    _actionListener = (MethodBinding)restoreAttachedState(context, values[4]);
  }

  // internal methods

  protected String chooseBestRendererType(String rendererTypes)
  {
    Form form = getForm();
    String array[] = rendererTypes.split(",");
    FacesContext facesContext = getFacesContext();
    RenderKit renderKit = facesContext.getRenderKit();

    String bestRendererType = null;
    int bestSuitability = 0;
    for (String rendererType : array)
    {
      rendererType = rendererType.trim();
      Renderer renderer = renderKit.getRenderer(FAMILY, rendererType);
      int suitability;
      if (renderer instanceof FormRenderer)
      {
        FormRenderer formRenderer = (FormRenderer)renderer;
        suitability = formRenderer.getSuitability(form, facesContext);
      }
      else
      {
        suitability = 1; // default suitability
      }
      if (suitability > bestSuitability)
      {
        bestSuitability = suitability;
        bestRendererType = rendererType;
      }
    }
    return bestRendererType;
  }

  protected void validateForm(FacesContext context, Object convertedValue)
  {
    Map data = (Map)convertedValue;
    Form form = getForm();
    ArrayList errors = new ArrayList();
    Locale locale = context.getViewRoot().getLocale();
    boolean valid = form.validate(data, errors, locale);
    System.out.println(">>> Errors: " + errors);
    if (!valid)
    {
      setValid(false);
      for (Object error : errors)
      {
        String errorMessage = error.toString();
        FacesMessage message = 
          new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, null);
        context.addMessage(getClientId(context), message);
      }
    }
  }

  @Override
  protected boolean compareValues(Object previousValue, Object convertedValue)
  {
    Map map1 = (Map)previousValue;
    Map map2 = (Map)convertedValue;
    // TODO:
    return (map1 == null && map2 != null) || !map1.equals(map2);
  }
}
