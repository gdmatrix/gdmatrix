package org.santfeliu.misc.mapviewer;

import java.io.Serializable;

/**
 *
 * @author realor
 */
public class MapView implements Serializable
{
  private String name;
  private String title;
  private String creationDate;
  private String description;
  private String thumbnailDocId;
  private String thumbnailContentId;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getCreationDate()
  {
    return creationDate;
  }

  public void setCreationDate(String creationDate)
  {
    this.creationDate = creationDate;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getThumbnailDocId()
  {
    return thumbnailDocId;
  }

  public void setThumbnailDocId(String thumbnailDocId)
  {
    this.thumbnailDocId = thumbnailDocId;
  }

  public String getThumbnailContentId()
  {
    return thumbnailContentId;
  }

  public void setThumbnailContentId(String thumbnailContentId)
  {
    this.thumbnailContentId = thumbnailContentId;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }
}
