package org.santfeliu.feed.web;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import org.matrix.feed.Feed;
import org.santfeliu.web.obj.PageBean;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

public class FeedMainBean extends PageBean
{
  private static final String ENTRY_LIFE_SPAN = "365";
  private static final String REFRESH_INTERVAL = "1";
  
  private static String TAG_RSS = "rss";
  private static String TAG_FEED = "feed";

  private Feed feed;

  private String auxFeedType;
  private String auxFeedInternalUrl;

  public FeedMainBean()
  {
    load();
  }

  public Feed getFeed()
  {
    return feed;
  }

  public void setFeed(Feed feed)
  {
    this.feed = feed;
  }
  
  public String show()
  {
    return "feed_main";
  }

  @Override
  public String store()
  {
    try
    {      
      if (feed.getEntryLifeSpan() == null ||
        feed.getEntryLifeSpan().trim().isEmpty())
      {
        feed.setEntryLifeSpan(ENTRY_LIFE_SPAN);
      }
      if (feed.getRefreshInterval() == null ||
        feed.getRefreshInterval().trim().isEmpty())
      {
        feed.setRefreshInterval(REFRESH_INTERVAL);
      }

      if (feed.getInternalUrl() == null ||
        feed.getInternalUrl().trim().isEmpty())
      {
        discoverFeed();
        feed.setInternalUrl(auxFeedInternalUrl);
        feed.setType(auxFeedType);
      }
      feed = FeedConfigBean.getPort().storeFeed(feed);
      setObjectId(feed.getFeedId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }

  public String getLastRefreshDate()
  {
    try
    {
      if (feed.getRefreshDateTime() != null)
      {
        SimpleDateFormat humanFormat =
          new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return humanFormat.format(sysFormat.parse(feed.getRefreshDateTime()));
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "";
  }

  private void load()
  {
    if (isNew())
    {
      feed = new Feed();
    }
    else
    {
      try
      {
        feed = FeedConfigBean.getPort().loadFeedFromCache(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        feed = new Feed();
      }
    }
  }

  private void discoverFeed() throws Exception
  {
    InputStream is = null;
    try
    {
      Tidy tidy = new Tidy();
      tidy.setOnlyErrors(true);
      tidy.setShowWarnings(false);
      tidy.setInputEncoding("utf-8");
      tidy.setXmlTags(true);

      URL url = new URL(feed.getUrl());
      is = new BufferedInputStream(url.openStream());

      Document doc = tidy.parseDOM(is, null);
      NodeList nodeList = doc.getElementsByTagName(TAG_RSS);
      if (nodeList.getLength() > 0)
      {
        auxFeedInternalUrl = feed.getUrl();
        auxFeedType = "rss";
      }
      else
      {
        nodeList = doc.getElementsByTagName(TAG_FEED);
        if (nodeList.getLength() > 0)
        {
          auxFeedInternalUrl = feed.getUrl();
          auxFeedType = "atom";
        }
        else
        {
          discoverFeedByHtml(doc);
        }
      }
    }
    catch (Exception ex)
    {
      throw ex;
    }
    finally
    {
      try
      {
        if (is != null) is.close();
      }
      catch (Exception ex2) { }
    }
  }

  private void discoverFeedByHtml(Document doc) throws Exception
  {
    NodeList nodeList = doc.getElementsByTagName("link");
    for (int i = 0; i < nodeList.getLength(); i++)
    {
      Node item = nodeList.item(i);
      NamedNodeMap map = item.getAttributes();
      if (map.getNamedItem("rel") != null)
      {
        if ("alternate".equals(map.getNamedItem("rel").getNodeValue()))
        {
          if (map.getNamedItem("type") != null && map.getNamedItem("href") != null)
          {
            String typeValue = map.getNamedItem("type").getNodeValue();
            if ("application/atom+xml".equals(typeValue))
            {
              auxFeedInternalUrl = map.getNamedItem("href").getNodeValue();
              auxFeedType = "atom";
              return;
            }
            else if ("application/rss+xml".equals(typeValue))
            {
              auxFeedInternalUrl = map.getNamedItem("href").getNodeValue();
              auxFeedType = "rss";
              return;
            }
          }
        }
      }
    }
    //Feed not found
    throw new Exception("Unknown feed type");
  }

}
