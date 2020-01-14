package org.santfeliu.feed.web;

import java.text.SimpleDateFormat;
import java.util.List;
import org.matrix.feed.Entry;
import org.matrix.feed.EntryFilter;
import org.matrix.feed.Feed;
import org.santfeliu.web.obj.PageBean;

public class FeedEntriesBean extends PageBean
{
  private List<Entry> rows;

  private int firstRowIndex;

  public FeedEntriesBean()
  {
    load();
  }

  public int getFirstRowIndex()
  {
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  public List<Entry> getRows()
  {
    return rows;
  }

  public void setRows(List<Entry> rows)
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String show()
  {
    return "feed_entries";
  }

  public String switchEntryVisibility()  
  {
    try
    {
      Entry row = (Entry)getExternalContext().getRequestMap().get("row");
      row.setVisible(!row.isVisible());
      FeedConfigBean.getPort().storeEntry(row);
      load();      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getRowDate() throws Exception
  {
    try
    {
      Entry row = (Entry)getExternalContext().getRequestMap().get("row");
      SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      SimpleDateFormat humanFormat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      return humanFormat.format(sysFormat.parse(row.getPubDateTime()));
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "";
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        EntryFilter filter = new EntryFilter();
        filter.getFeedId().add(getFeed().getFeedId());
        rows = FeedConfigBean.getPort().findEntriesFromCache(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private Feed getFeed()
  {
    FeedMainBean feedMainBean = (FeedMainBean)getBean("feedMainBean");
    return feedMainBean.getFeed();
  }

}
