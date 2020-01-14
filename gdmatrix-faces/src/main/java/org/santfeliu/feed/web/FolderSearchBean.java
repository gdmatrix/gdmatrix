package org.santfeliu.feed.web;

import java.util.List;
import org.matrix.feed.Folder;
import org.matrix.feed.FolderFilter;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

@CMSManagedBean
public class FolderSearchBean extends BasicSearchBean
{
  private String folderIdInput;
  private FolderFilter filter;
  
  public FolderSearchBean()
  {
    filter = new FolderFilter();
  }

  public String getFolderIdInput()
  {
    return folderIdInput;
  }

  public void setFolderIdInput(String folderIdInput)
  {
    this.folderIdInput = folderIdInput;
  }

  public FolderFilter getFilter()
  {
    return filter;
  }

  public void setFilter(FolderFilter filter)
  {
    this.filter = filter;
  }

  public int countResults()
  {
    try
    {
      setFilterFolderId();
      return FeedConfigBean.getPort().countFoldersFromCache(filter);
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
      setFilterFolderId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return FeedConfigBean.getPort().findFoldersFromCache(filter);
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
    return "folder_search";
  }

  public String selectFolder()
  {
    Folder row = (Folder)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String folderId = row.getFolderId();
    return getControllerBean().select(folderId);
  }

  public String showFolder()
  {
    return getControllerBean().showObject("Folder",
      (String)getValue("#{row.folderId}"));
  }

  private void setFilterFolderId()
  {
    filter.getFolderId().clear();
    if (folderIdInput != null)
    {
      for (String folderId : folderIdInput.split(";"))
      {
        if (!folderId.isEmpty()) filter.getFolderId().add(folderId);
      }
    }
  }
  
}
