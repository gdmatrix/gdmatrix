package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


public class InputNumberFormBean extends FormBean implements Serializable
{
  private String message;
  private Double number;
  private String varName;

  public InputNumberFormBean()
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

  public void setNumber(Double number)
  {
    this.number = number;
  }

  public Double getNumber()
  {
    return number;
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
    if (value != null)
    {
      try
      {
        number = new Double(value.toString());
      }
      catch (NumberFormatException ex)
      {
        number = null;
      }
    }
    else
    {
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      value = instanceBean.getVariables().get(varName);
      if (value instanceof Double)
      {
        number = (Double)value;
      }
      else number = null;
    }
    return "input_number_form";
  }

  public Map submit()
  {
    HashMap variables = new HashMap();
    variables.put(this.varName, number);
    return variables;
  }
}
