package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


public class SelectMenuFormBean extends FormBean implements Serializable
{
  private String varName;
  private String message;
  private List options;
  private String selectedCode;

  public SelectMenuFormBean()
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

  public void setOptions(List options)
  {
    this.options = options;
  }

  public List getOptions()
  {
    return options;
  }

  public String show(Form form)
  {
    Properties parameters = form.getParameters();
  
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setForwardEnabled(false);

    Object value;
    value = parameters.get("var");
    if (value != null) varName = String.valueOf(value);
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);

    options = new ArrayList();
    int i = 0;
    Object code = parameters.get("code" + i);
    while (code != null)
    {
      String scode = code.toString();
      String label = String.valueOf(parameters.get("label" + i));
      Map option = new HashMap();
      option.put("code", scode);
      option.put("label", label);
      options.add(option);
      i++;
      code = parameters.get("code" + i);
    }
    return "select_menu_form";
  }
  
  public Map submit()
  {
    HashMap variables = new HashMap();
    variables.put(varName, selectedCode);
    return variables;
  }
  
  public String selectOption()
  {
    Map option = (Map)getRequestMap().get("option");
    selectedCode = String.valueOf(option.get("code"));

    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    return instanceBean.forward();
  }
}
