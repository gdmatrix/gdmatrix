package org.santfeliu.news.web;

import org.matrix.news.New;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.web.obj.ObjectBean;

public class NewBean extends ObjectBean
{
  private static final int HEADLINE_MAX_LENGTH = 40;

  public NewBean()
  {
  }

  public String getObjectTypeId()
  {
    return "New";
  }

  public String getDescription()
  {
    NewMainBean newMainBean = (NewMainBean)getBean("newMainBean");
    New newObject = newMainBean.getNewObject();
    return getDescription(newObject);
  }

  public String getDescription(String objectId)
  {
    try
    {
      New newObject = NewsConfigBean.getPort().loadNewFromCache(objectId);
      return getDescription(newObject);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }

  public String getDescription(New newObject)
  {
    StringBuffer buffer = new StringBuffer();
    if (newObject == null) return "";
    else
    {
      String headline = newObject.getHeadline();
      if (headline != null)
      {
        if (headline.length() > HEADLINE_MAX_LENGTH)
        {
          headline = headline.substring(0, HEADLINE_MAX_LENGTH) + "...";
        }
        buffer.append(headline + " ");
      }
      buffer.append("(");
      buffer.append(newObject.getNewId());
      buffer.append(")");
    }
    return buffer.toString();
  }

/*
  public String createRedirected()
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    return super.getControllerBean().create(mic.getProperty(PARAM_EDIT_NODE));
  }
*/
  
  @Override
  public String cancel()  
  {
    NewsManagerClient.getCache().clear();
    return super.cancel();
  }  
  
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        NewsConfigBean.getPort().removeNew(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }
  
}
