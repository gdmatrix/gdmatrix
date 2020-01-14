package org.santfeliu.feed.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.feed.Folder;
import org.santfeliu.web.obj.PageBean;

public class FolderMainBean extends PageBean
{
  private static final String DEFAULT_ENTRY_COUNT = "100";
  
  private Folder folder;

  public FolderMainBean()
  {
    load();
  }

  public Folder getFolder()
  {
    return folder;
  }

  public void setFolder(Folder folder)
  {
    this.folder = folder;
  }
  
  public String show()
  {
    return "folder_main";
  }

  @Override
  public String store()
  {
    try
    {      
      if (folder.getDefaultEntryCount() == null ||
        folder.getDefaultEntryCount().trim().isEmpty())
      {
        folder.setDefaultEntryCount(DEFAULT_ENTRY_COUNT);
      }
      folder = FeedConfigBean.getPort().storeFolder(folder);
      setObjectId(folder.getFolderId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public List<SelectItem> getFolderItems()
  {
    FolderBean folderBean = (FolderBean)getBean("folderBean");
    return folderBean.getSelectItems(folder.getParentFolderId());
  }

  public String searchParentFolder()
  {
    return getControllerBean().searchObject("Folder",
      "#{folderMainBean.folder.parentFolderId}");
  } 
  
  public String showParentFolder()
  {
    return getControllerBean().showObject("Folder", 
      getFolder().getParentFolderId());
  }

  public boolean isRenderShowParentFolder()
  {
    return !isNew() && (getFolder().getParentFolderId() != null) && 
      (!getFolder().getParentFolderId().isEmpty());
  }  
  
  private void load()
  {
    if (isNew())
    {
      folder = new Folder();
    }
    else
    {
      try
      {
        folder = FeedConfigBean.getPort().loadFolderFromCache(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        folder = new Folder();
      }
    }
  }

}
