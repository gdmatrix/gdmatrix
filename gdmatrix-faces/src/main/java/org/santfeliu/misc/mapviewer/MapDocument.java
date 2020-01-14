package org.santfeliu.misc.mapviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author realor
 */
public class MapDocument extends Map
{
  private String docId;
  private String creationDate;
  private String captureUserId;
  private String captureDateTime;
  private String changeUserId;
  private String changeDateTime;
  private final HashMap metadata = new HashMap();
  private final List<String> readRoles = new ArrayList<String>();
  private final List<String> writeRoles = new ArrayList<String>();
  private String thumbnailDocId;

  public MapDocument()
  {
  }

  public MapDocument(Map map)
  {
    setTo(map);
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getCreationDate()
  {
    return creationDate;
  }

  public void setCreationDate(String creationDate)
  {
    this.creationDate = creationDate;
  }

  public String getChangeUserId()
  {
    return changeUserId;
  }

  public void setChangeUserId(String changeUserId)
  {
    this.changeUserId = changeUserId;
  }

  public String getChangeDateTime()
  {
    return changeDateTime;
  }

  public void setChangeDateTime(String changeDateTime)
  {
    this.changeDateTime = changeDateTime;
  }

  public String getCaptureUserId()
  {
    return captureUserId;
  }

  public void setCaptureUserId(String captureUserId)
  {
    this.captureUserId = captureUserId;
  }

  public String getCaptureDateTime()
  {
    return captureDateTime;
  }

  public void setCaptureDateTime(String captureDateTime)
  {
    this.captureDateTime = captureDateTime;
  }
  
  public void setMetadata(java.util.Map metadata)
  {
    if (this.metadata != metadata)
    {
      this.metadata.clear();
      this.metadata.putAll(metadata);
    }
  }
  
  public java.util.Map getMetadata() // save as Document.Property
  {
    return metadata;
  }
  
  public List<String> getReadRoles()
  {
    return readRoles;
  }

  public List<String> getWriteRoles()
  {
    return writeRoles;
  }

  public String getThumbnailDocId()
  {
    return thumbnailDocId;
  }

  public void setThumbnailDocId(String thumbnailDocId)
  {
    this.thumbnailDocId = thumbnailDocId;
  }
}
