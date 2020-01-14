package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;


import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

public class StartInstanceBean extends FacesBean implements Serializable
{
  private String url;
  private Map values = new HashMap();

  public StartInstanceBean()
  {
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public void setValues(Map values)
  {
    this.values = values;
  }

  public Map getValues()
  {
    return values;
  }

  // action methods
  public String loadForm()
  {
    try
    {
      MenuItemCursor cursor = 
        UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();

      url = (String)cursor.getProperties().get("url");
      if (url == null)
      {
        String doccod = (String)cursor.getProperties().get("doccod");
        if (doccod != null)
        {
          url = getContextURL() + "/documents/" + doccod;
        }
      }
      return url == null ? "blank" : "start_instance";
    }
    catch (Exception ex)
    {
      error(ex.getLocalizedMessage());
      ex.printStackTrace();
    }
    return null;
  }

  public String startInstance()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      MenuItemCursor cursor = userSessionBean.getMenuModel().getSelectedMenuItem();
      String workflowName = (String)cursor.getProperties().get("workflowName");
      if (workflowName == null)
        throw new Exception("UNDEFINED_WORKFLOWNAME_NODE_PROPERTY");
  
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      return instanceBean.createInstance(workflowName, workflowName, true, values);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return null;
  }
}
