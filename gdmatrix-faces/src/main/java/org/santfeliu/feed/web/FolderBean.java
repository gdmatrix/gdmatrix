package org.santfeliu.feed.web;

import org.matrix.feed.Folder;
import org.santfeliu.web.obj.ObjectBean;

public class FolderBean extends ObjectBean
{
  public FolderBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Folder";
  }

  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        FeedConfigBean.getPort().removeFolder(getObjectId());
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
    FolderMainBean folderMainBean = (FolderMainBean)getBean("folderMainBean");
    Folder folder = folderMainBean.getFolder();
    return getFolderDescription(folder);
  }   
  
  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      Folder folder = FeedConfigBean.getPort().loadFolderFromCache(oid);
      description = getFolderDescription(folder);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  private String getFolderDescription(Folder folder)
  {
    StringBuilder sb = new StringBuilder();  
    if (folder.getName() != null)
    {
      sb.append(folder.getName());
      sb.append(" ");
    }
    sb.append("(");
    sb.append(folder.getFolderId());
    sb.append(")");
    return sb.toString();
  }
  
}
