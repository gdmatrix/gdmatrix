package org.santfeliu.feed.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.feed.Folder;
import org.matrix.feed.FolderFilter;
import org.santfeliu.web.obj.PageBean;

public class FolderFoldersBean extends PageBean
{
  private List<Folder> rows;  
  private String editingFolderId;

  public FolderFoldersBean()
  {
    load();
  }

  public String getEditingFolderId() 
  {
    return editingFolderId;
  }

  public void setEditingFolderId(String editingFolderId) 
  {
    this.editingFolderId = editingFolderId;
  }

  public List<Folder> getRows() 
  {
    return rows;
  }

  public void setRows(List<Folder> rows) 
  {
    this.rows = rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }
  
  public String show()
  {
    return "folder_folders";
  }

  public String showChildFolder()
  {
    return getControllerBean().showObject("Folder",
      (String)getValue("#{row.folderId}"));
  }

  public String storeChildFolder()
  {
    try
    {
      if (editingFolderId == null || editingFolderId.trim().length() == 0)
      {
        throw new Exception("VALUE_IS_MANDATORY");
      }
      else
      {
        Folder editingFolder = 
          FeedConfigBean.getPort().loadFolderFromCache(editingFolderId);
        editingFolder.setParentFolderId(getFolder().getFolderId());
        FeedConfigBean.getPort().storeFolder(editingFolder);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      editingFolderId = null;
      load();
    }
    return null;
  }
  
  public String removeChildFolder()
  {
    try
    {
      Folder row = (Folder)getExternalContext().getRequestMap().get("row");
      row.setParentFolderId(null);
      FeedConfigBean.getPort().storeFolder(row);
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String searchFolder()
  {
    return getControllerBean().searchObject("Folder",
      "#{folderFoldersBean.editingFolderId}");
  }

  public List<SelectItem> getFolderItems()
  {
    FolderBean folderBean = (FolderBean)getBean("folderBean");
    return folderBean.getSelectItems(editingFolderId);
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        FolderFilter filter = new FolderFilter();
        filter.setParentFolderId(getFolder().getFolderId());
        rows = FeedConfigBean.getPort().findFoldersFromCache(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private Folder getFolder()
  {
    FolderMainBean folderMainBean = (FolderMainBean)getBean("folderMainBean");
    return folderMainBean.getFolder();
  }

}
