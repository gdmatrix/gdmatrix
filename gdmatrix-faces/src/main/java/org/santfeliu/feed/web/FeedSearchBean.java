package org.santfeliu.feed.web;

import java.util.List;
import org.matrix.feed.Feed;
import org.matrix.feed.FeedFilter;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

@CMSManagedBean
public class FeedSearchBean extends BasicSearchBean
{
  private String feedIdInput;
  private FeedFilter filter;
  
  public FeedSearchBean()
  {
    filter = new FeedFilter();
  }

  public String getFeedIdInput()
  {
    return feedIdInput;
  }

  public void setFeedIdInput(String feedIdInput)
  {
    this.feedIdInput = feedIdInput;
  }

  public FeedFilter getFilter()
  {
    return filter;
  }

  public void setFilter(FeedFilter filter)
  {
    this.filter = filter;
  }

  public int countResults()
  {
    try
    {
      setFilterFeedId();
      return FeedConfigBean.getPort().countFeedsFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {      
      setFilterFeedId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return FeedConfigBean.getPort().findFeedsFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  @CMSAction
  public String show()
  {
    return "feed_search";
  }

  public String selectFeed()
  {
    Feed row = (Feed)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String feedId = row.getFeedId();
    return getControllerBean().select(feedId);
  }

  public String showFeed()
  {
    return getControllerBean().showObject("Feed",
      (String)getValue("#{row.feedId}"));
  }

  private void setFilterFeedId()
  {
    filter.getFeedId().clear();
    if (feedIdInput != null)
    {
      for (String feedId : feedIdInput.split(";"))
      {
        if (!feedId.isEmpty()) filter.getFeedId().add(feedId);
      }
    }
  }

}
