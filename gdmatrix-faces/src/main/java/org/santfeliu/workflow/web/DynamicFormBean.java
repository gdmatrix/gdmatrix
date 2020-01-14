package org.santfeliu.workflow.web;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;

/**
 *
 * @author realor
 */
public class DynamicFormBean extends FormBean implements Serializable
{
  private String selector;
  private boolean useCache = false;
  private Map data = new HashMap();

  public org.santfeliu.form.Form getForm()
  {
    try
    {
      // in post back always use cache
      org.santfeliu.form.FormFactory formFactory =
        org.santfeliu.form.FormFactory.getInstance();

      if (!useCache && getFacesContext().getRenderResponse())
      {
        formFactory.clearForm(selector);
      }
      return formFactory.getForm(selector, data);
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public Map getData()
  {
    return data;
  }

  public void setData(Map data)
  {
    this.data = data;
  }

  // actions
  public String show(Form form)
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    data.clear();
    data.putAll(instanceBean.getVariables());
    Properties parameters = form.getParameters();
    selector = (String)parameters.getProperty("selector");
    if (parameters.containsKey("useCache"))
    {
      useCache = (Boolean)parameters.getProperty("useCache");
    }
    return "dynamic_form";
  }

  public Map submit()
  {
    // remove unchanged variables
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    Map variables = instanceBean.getVariables();
    for (Object key : variables.keySet())
    {
      String name = key.toString();
      Object oldValue = variables.get(name);
      Object newValue = data.get(name);
      boolean changed = (oldValue == null && newValue != null) ||
        !oldValue.equals(newValue);
      if (!changed) data.remove(name);
    }
    return data;
  }

  public String buttonPressed()
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    return instanceBean.forward();
  }
}
