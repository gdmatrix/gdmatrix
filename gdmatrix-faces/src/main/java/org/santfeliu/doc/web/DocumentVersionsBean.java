package org.santfeliu.doc.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.doc.State;
import org.santfeliu.web.obj.PageBean;
import org.santfeliu.web.obj.PageHistory;

public class DocumentVersionsBean extends PageBean
{
  private List<Document> rows;
  
  public DocumentVersionsBean()
  {
    //load();
  }

  public List<Document> getRows()
  {
    return rows;
  }

  public void setRows(List<Document> rows)
  {
    this.rows = rows;
  }

  public Date getRowCaptureDateTime()
  {
    Document row = (Document)getExternalContext().getRequestMap().get("row");
    if (row.getCaptureDateTime() != null)
    {
      try
      {
        SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return sysFormat.parse(row.getCaptureDateTime());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return null;
  }

  public Date getRowChangeDateTime()
  {
    Document row = (Document)getExternalContext().getRequestMap().get("row");
    if (row.getChangeDateTime() != null)
    {
      try
      {
        SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return sysFormat.parse(row.getChangeDateTime());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return null;
  }

  public String show()
  {
    load();
    return "document_versions";
  }

  public String purgeDocument()
  {
    try
    {
      String[] objectIdArray = DocumentConfigBean.fromObjectId(getObjectId());
      String objectDocId = objectIdArray[0];
      DocumentConfigBean.getClient().removeDocument(objectDocId, -3);
      DocumentBean documentBean = (DocumentBean)getBean("document2Bean");
      //We remove the purged elements from the history list
      Set<String> purgeObjects = new HashSet<String>();
      for (String historyObjectId : documentBean.getObjectHistory())
      {
        String[] historyObjectIdArray =
          DocumentConfigBean.fromObjectId(historyObjectId);
        String historyDocId = historyObjectIdArray[0];
        if (objectDocId.equalsIgnoreCase(historyDocId))
        {
          purgeObjects.add(historyObjectId);
        }
      }
      for (String objectId : purgeObjects)
      {
        documentBean.getObjectHistory().removeObject(objectId);
      }
      load();
      String outcome = showLastVersion();
      updatePageHistory(objectDocId);
      return outcome;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private void updatePageHistory(String docId) throws Exception
  {
    Set<PageHistory.Entry> entrySet = new HashSet<PageHistory.Entry>();
    for (Object entryObj : getControllerBean().getPageHistory())
    {
      PageHistory.Entry entry = (PageHistory.Entry)entryObj;
      if (entry.getObjectId() != null && entry.getObjectId().contains("-"))
      {
        String[] entryObjectIdArray =
          DocumentConfigBean.fromObjectId(entry.getObjectId());
        String entryDocId = entryObjectIdArray[0];
        if (docId.equals(entryDocId))
        {
          entrySet.add(entry);
        }
      }
    }
    for (PageHistory.Entry entry : entrySet)
    {
      entry.close();
    }
  }

  private void removeFromPageHistory(String objectId) throws Exception
  {
    for (Object entryObj : getControllerBean().getPageHistory())
    {
      PageHistory.Entry entry = (PageHistory.Entry)entryObj;
      if (objectId.equals(entry.getObjectId()))
      {
        entry.close();
        return;
      }
    }
  }

  public String showDocument()
  {
    Document row = (Document)getRequestMap().get("row");
    String docId = row.getDocId();
    int version = row.getVersion();
    return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
      DocumentConfigBean.toObjectId(docId, version));
  }

  public boolean isCurrentVersion()
  {
    try
    {
      String[] objectIdArray = DocumentConfigBean.fromObjectId(getObjectId());
      String docId = objectIdArray[0];
      int version = Integer.valueOf(objectIdArray[1]);
      Document row = (Document)getRequestMap().get("row");
      return ((docId.equalsIgnoreCase(row.getDocId())) &&
        (version == row.getVersion()));
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return false;
  }

  public String removeDocument()
  {
    try
    {
      boolean removingCurrent = false;
      String[] objectIdArray = DocumentConfigBean.fromObjectId(getObjectId());
      String currentDocId = objectIdArray[0];
      String currentVersion = objectIdArray[1];
      Document row = (Document)getExternalContext().getRequestMap().get("row");
      if ((row.getDocId().equalsIgnoreCase(currentDocId)) && 
        (String.valueOf(row.getVersion()).equalsIgnoreCase(currentVersion)))
      {
        removingCurrent = true;
      }
      DocumentConfigBean.getClient().removeDocument(row.getDocId(),
        row.getVersion());
      DocumentBean documentBean = (DocumentBean)getBean("document2Bean");
      String objectId = DocumentConfigBean.toObjectId(
        row.getDocId(), row.getVersion());
      documentBean.getObjectHistory().removeObject(objectId);

      String outcome = null;
      if (rows.size() <= 1)
      {
        outcome = documentBean.create();
      }
      else
      {
        load();
        if (removingCurrent) outcome = showLastVersion();
      }
      removeFromPageHistory(objectId);
      return outcome;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        String[] objectId = DocumentConfigBean.fromObjectId(getObjectId());
        DocumentFilter filter = new DocumentFilter();
        filter.getDocId().add(objectId[0]);
        filter.setVersion(-1);
        filter.setIncludeContentMetadata(false);
        filter.getStates().add(State.DRAFT);
        filter.getStates().add(State.COMPLETE);
        filter.getStates().add(State.RECORD);
        filter.getStates().add(State.DELETED);
        OrderByProperty order = new OrderByProperty();
        order.setName(DocumentConstants.VERSION);
        order.setDescending(false);
        filter.getOrderByProperty().add(order);
        rows = DocumentConfigBean.getClient().findDocuments(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private String showLastVersion()
  {
    if (rows.size() > 0)
    {
      Document lastDocument = rows.get(rows.size() - 1);
      String lastObjectId = DocumentConfigBean.toObjectId(
        lastDocument.getDocId(), lastDocument.getVersion());
      return getControllerBean().showObject(
        DictionaryConstants.DOCUMENT_TYPE, lastObjectId);
    }
    return null;
  }

}
