package org.santfeliu.feed.web;

import org.matrix.feed.Feed;
import org.santfeliu.feed.client.FeedManagerClient;
import org.santfeliu.web.obj.ObjectBean;

public class FeedBean extends ObjectBean
{
  public FeedBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Feed";
  }

  @Override
  public String cancel()  
  {
    FeedManagerClient.getCache().clear();
    return super.cancel();
  }
  
  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        FeedConfigBean.getPort().removeFeed(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return getControllerBean().show();
  }
  
  @Override
  public String getDescription()
  {
    FeedMainBean feedMainBean = (FeedMainBean)getBean("feedMainBean");
    Feed feed = feedMainBean.getFeed();
    return getFeedDescription(feed);
  }

  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      Feed feed = FeedConfigBean.getPort().loadFeedFromCache(oid);
      description = getFeedDescription(feed);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  private String getFeedDescription(Feed feed)
  {
    StringBuilder sb = new StringBuilder();  
    if (feed.getName() != null)
    {
      sb.append(feed.getName());
      sb.append(" ");
    }
    sb.append("(");
    sb.append(feed.getFeedId());
    sb.append(")");
    return sb.toString();
  }  
  
}
