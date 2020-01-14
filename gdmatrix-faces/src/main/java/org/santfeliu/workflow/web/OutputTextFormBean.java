package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


public class OutputTextFormBean extends FormBean implements Serializable
{
  private String message;
  private boolean html;

  public OutputTextFormBean()
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

  public void setHtml(boolean html)
  {
    this.html = html;
  }

  public boolean isHtml()
  {
    return html;
  }
  
  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    Object value;
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("html");
    if (value != null) html = "true".equalsIgnoreCase(String.valueOf(value));

    return "output_text_form";
  }

  public Map submit()
  {
    HashMap variables = new HashMap();
    return variables;
  }
}
