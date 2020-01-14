package org.santfeliu.doc.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;
import org.santfeliu.doc.DocumentCache;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.web.obj.PageBean;

public class DocumentRelatedBean extends PageBean
{
  private List<RelatedDocument> rows;
  private String editingRowId;
  private RelatedDocument editingRow;
  private String previousName;
  
  public DocumentRelatedBean()
  {
    load();
  }

  public List<RelatedDocument> getRows()
  {
    return rows;
  }

  public void setRows(List<RelatedDocument> rows)
  {
    this.rows = rows;
  }

  public RelatedDocument getEditingRow()
  {
    return editingRow;
  }

  public void setEditingRow(RelatedDocument editingRow)
  {
    this.editingRow = editingRow;
  }

  public void setEditingRowId(String editingRowId)
  {
    this.editingRowId = editingRowId;
  }

  public String getEditingRowId()
  {
    return editingRowId;
  }

  public Date getEditingRowCaptureDateTime()
  {
    if (editingRow.getCaptureDateTime() != null)
    {
      try
      {
        SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return sysFormat.parse(editingRow.getCaptureDateTime());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return null;
  }

  public Date getEditingRowChangeDateTime()
  {
    if (editingRow.getChangeDateTime() != null)
    {
      try
      {
        SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return sysFormat.parse(editingRow.getChangeDateTime());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return null;
  }

  public SelectItem[] getRelationTypeSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.doc.web.resources.DocumentBundle", getLocale());
    return FacesUtils.getEnumSelectItems(RelationType.class, bundle);
  }

  public String show()
  {
    return "document_related";
  }

  public String store()
  {
    if (editingRow != null)
    {
      storeRelatedDocument();
    }
    else
    {
      load();
    }
    return show();
  }

  public String showRelatedDocument()
  {
    RelatedDocument row = (RelatedDocument)getRequestMap().get("row");
    String docId = row.getDocId();
    int version = row.getVersion();
    return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
      DocumentConfigBean.toObjectId(docId, version));
  }

  public String editRelatedDocument()
  {
    RelatedDocument row = (RelatedDocument)getRequestMap().get("row");
    editingRow = row;
    editingRowId = DocumentConfigBean.toObjectId(
      editingRow.getDocId(), editingRow.getVersion());
    previousName = row.getName();
    return null;
  }

  public String addRelatedDocument()
  {
    editingRow = new RelatedDocument();
    editingRowId = null;
    return null;
  }

  public String removeRelatedDocument()
  {
    try
    {
      RelatedDocument row = (RelatedDocument)getRequestMap().get("row");
      Document document = getDocument();
      if (((DocumentBean)getObjectBean()).isCreateNewVersion())
        document.setVersion(DocumentConstants.NEW_VERSION);
      removeRelatedDocument(document, row);
      Document storedDocument =
        DocumentConfigBean.getClient().storeDocument(document);
      DocumentCache.reset(row.getDocId(), row.getName());
      editingRow = null;
      editingRowId = null;
      if (((DocumentBean)getObjectBean()).isCreateNewVersion())
      {
        String objectId =
          DocumentConfigBean.toObjectId(storedDocument.getDocId(), storedDocument.getVersion());
        getControllerBean().show(getSelectedMenuItem().getMid(), objectId);
      }

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getRelatedDocumentTitle()
  {
    RelatedDocument row = (RelatedDocument)getRequestMap().get("row");
    if (row.getDocId() == null) return "";
    
    DocumentBean documentBean = (DocumentBean)getBean("document2Bean");
    return documentBean.getDescription(
      DocumentConfigBean.toObjectId(row.getDocId(), row.getVersion()));
    //return DocumentConfigBean.toObjectId(row.getDocId(), row.getVersion());
  }

  public List<SelectItem> getRelatedDocumentSelectItems()
  {
    DocumentBean documentBean = (DocumentBean)getBean("document2Bean");
    return documentBean.getSelectItems(getEditingRowId());
  }

  public String searchDocument()
  {
    return getControllerBean().searchObject(DictionaryConstants.DOCUMENT_TYPE,
      "#{documentRelatedBean.editingRowId}");
  }

  public String storeRelatedDocument()
  {
    try
    {
      Document document = getDocument();
      if (!document.getRelatedDocument().contains(editingRow))
      {
        document.getRelatedDocument().add(editingRow);
      }

      String[] relatedDocId = DocumentConfigBean.fromObjectId(editingRowId);
      editingRow.setDocId(relatedDocId[0]);
      editingRow.setVersion(Integer.valueOf(relatedDocId[1]));
      if (((DocumentBean)getObjectBean()).isCreateNewVersion())
        document.setVersion(DocumentConstants.NEW_VERSION);
      Document storedDocument = DocumentConfigBean.getClient().storeDocument(document);
      DocumentCache.reset(document.getDocId(), previousName);
      String objectId =
        DocumentConfigBean.toObjectId(storedDocument.getDocId(), storedDocument.getVersion());
      getControllerBean().show(getSelectedMenuItem().getMid(), objectId);
      reloadDocument();
      load();
      editingRow = null;
      editingRowId = null;
    }
    catch (Exception ex)
    {
      reloadDocument();
      getDocument().getRelatedDocument().remove(editingRow);
      load();
      error(ex);
    }
    return null;
  }

  public String cancelRelatedDocument()
  {
    editingRow = null;
    editingRowId = null;
    load();
    return null;
  }

  public boolean isRenderDateFields()
  {
    if (editingRow != null)
    {
      return getDocument().getRelatedDocument().contains(editingRow);
      //String docId = editingRow.getDocId();
      //return !(docId == null || docId.length() == 0);
    }
    return false;
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
        Document document = getDocument();
        rows = new ArrayList<RelatedDocument>();
        rows.addAll(document.getRelatedDocument());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private Document getDocument()
  {
    DocumentMainBean documentMainBean =
      (DocumentMainBean)getBean("documentMainBean");
    return documentMainBean.getDocument();
  }

  private void reloadDocument()
  {
    DocumentMainBean documentMainBean =
      (DocumentMainBean)getBean("documentMainBean");
    boolean createNewVersion =
      ((DocumentBean)getObjectBean()).isCreateNewVersion();
    documentMainBean.reload(createNewVersion);
  }

  private void removeRelatedDocument(Document document, RelatedDocument row)
  {
    List<RelatedDocument> relDocs = new ArrayList();
    for (RelatedDocument relDoc : document.getRelatedDocument())
    {
      if (!row.getDocId().equals(relDoc.getDocId()) ||
          row.getVersion() != relDoc.getVersion() ||
        !row.getRelationType().equals(relDoc.getRelationType()))
      {
        relDocs.add(relDoc);
      }
    }
    document.getRelatedDocument().clear();
    document.getRelatedDocument().addAll(relDocs);
  }

}
