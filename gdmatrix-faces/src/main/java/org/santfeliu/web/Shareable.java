package org.santfeliu.web;

import java.util.List;

/**
 *
 * @author lopezrj
 */
public interface Shareable
{  
  public boolean isSharingEnabled();  
  public List<ShareLink> getShareLinkList(); 
  
  public class ShareLink
  {
    private String shareURL;
    private String iconURL;
    private String description;

    public ShareLink(String shareURL, String iconURL, String description)
    {
      this.shareURL = shareURL;
      this.iconURL = iconURL;
      this.description = description;
    }
    
    public String getShareURL()
    {
      return shareURL;
    }

    public void setShareURL(String shareURL)
    {
      this.shareURL = shareURL;
    }

    public String getIconURL()
    {
      return iconURL;
    }

    public void setIconURL(String iconURL)
    {
      this.iconURL = iconURL;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }
  }
}
