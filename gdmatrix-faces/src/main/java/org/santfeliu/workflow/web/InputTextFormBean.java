package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


public class InputTextFormBean extends FormBean implements Serializable
{
  private String message;
  private String text;
  private String varName;

  public InputTextFormBean()
  {
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public String getText()
  {
    return text;
  }

  public void validateText(FacesContext context, UIComponent component, 
    Object value)
  {
    String s = value.toString();
    if (s.replaceAll("\n", "").trim().length() == 0)
    {
      throw new ValidatorException(
        new FacesMessage("Has d'escriure alguna cosa"));
    }
  }

  public String show(Form form)
  {
    Properties parameters = form.getParameters();
    
    Object value;
    value = parameters.get("var");
    if (value != null) varName = String.valueOf(value);
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("value");
    if (value instanceof String)
    {
      text = (String)value;
    }
    else
    {
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      value = instanceBean.getVariables().get(varName);
      if (value instanceof String)
      {
        text = (String)value;
      }
      else text = null;
    }
    return "input_text_form";
  }
  
  public Map submit()
  {
    HashMap variables = new HashMap();
    variables.put(this.varName, text);
    return variables;
  }
}
