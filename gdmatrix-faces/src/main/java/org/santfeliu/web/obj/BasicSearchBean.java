package org.santfeliu.web.obj;

import java.util.List;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.BigList;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author realor
 */
public abstract class BasicSearchBean extends PageBean
{
  @CMSProperty
  public static final String SEARCH_TITLE_PROPERTY = "oc.searchTitle";
  private static final int CACHE_SIZE = 15;  
  private int firstRowIndex;
  private BigList rows; // when rows == null, result table is not shown

  public BigList getRows()
  {
    return rows;
  }

  // force data refresh, page is unchanged
  public String refresh()
  {
    initRows(); 
    return null;
  }

  // force data refresh, move to first page
  public String search()
  {
    firstRowIndex = 0; // reset index
    initRows(); // force rows population
    return null;
  }

  // new search, clear data, result table is not shown
  public String reset()
  {
    rows = null;
    firstRowIndex = 0;
    return null;
  }

  public abstract int countResults();

  public abstract List getResults(int firstResult, int maxResults);

  @Override
  public String getTitle(MenuItemCursor cursor)
  {
    String title = cursor.getProperty(SEARCH_TITLE_PROPERTY);
    if (title == null)
    {
      title = cursor.getLabel();
    }
    return title;
  }

  public int getFirstRowIndex()
  {
    int size = getRowCount();
    if (size == 0)
    {
      firstRowIndex = 0;
    }
    else if (firstRowIndex >= size)
    {
      int pageSize = getPageSize();
      firstRowIndex = pageSize * ((size - 1) / pageSize);
    }
    return firstRowIndex;
  }

  public void setFirstRowIndex(int firstRowIndex)
  {
    this.firstRowIndex = firstRowIndex;
  }

  public int getRowCount()
  {
    return rows == null ? 0 : rows.size();
  }

  public int getCacheSize()
  {
    if (getPageSize() != PAGE_SIZE)
      return getPageSize() + 5;
    else
      return CACHE_SIZE;
  }

  public String createObject()
  {
    return getObjectBean().create();
  }

  public String showObject(String typeId, String objectId)
  {
    return getControllerBean().showObject(typeId, objectId);
  }

  // force new seach
  private void initRows()
  {
    if (rows != null && (getPageSize() == rows.getBlockSize()))
    {
      // clear rows cache, reuse rows
      rows.clearCache();
    }
    else
    {
      // create new BigList
      rows = new BigList(getCacheSize(), getPageSize())
      {
        @Override
        public int getElementCount()
        {
          return countResults();
        }

        @Override
        public List getElements(int firstResult, int maxResults)
        {
          return getResults(firstResult, maxResults);
        }
      };
    }
  }
}
