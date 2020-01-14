package org.santfeliu.kernel.web;

import org.santfeliu.doc.web.*;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.PersonDocument;
import org.matrix.kernel.PersonDocumentView;
import org.matrix.kernel.PersonDocumentFilter;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.TypifiedPageBean;
import org.santfeliu.ws.WSExceptionFactory;

public class PersonDocumentsBean extends TypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_personRootTypeId";

  private List<PersonDocumentView> rows;
  private PersonDocument editingPersonDocument;

  public PersonDocumentsBean()
  {
    super(DictionaryConstants.PERSON_DOCUMENT_TYPE, "DOC_ADMIN");
    load();
  }

  public PersonDocument getEditingPersonDocument()
  {
    return editingPersonDocument;
  }

  public void setEditingPersonDocument(PersonDocument editingDocPerson)
  {
    this.editingPersonDocument = editingDocPerson;
  }

  @Override
  public List<PersonDocumentView> getRows()
  {
    return rows;
  }

  public void setRows(List<PersonDocumentView> rows)
  {
    this.rows = rows;
  }

  public String show()
  {
    return "person_documents";
  }

  @Override
  public String store()
  {
    if (editingPersonDocument == null)
    {
      load();
    }
    return show();
  }

  public String showDocument()
  {
    return getControllerBean().showObject("Document",
      (String)getValue("#{row.document.docId}"));
  }

  public String searchDocument()
  {
    return getControllerBean().searchObject("Document",
      "#{personDocumentsBean.editingPersonDocument.docId}");
  }

  public String createPersonDocument()
  {
    editingPersonDocument = new PersonDocument();
    return null;
  }

  public String editPersonDocument()
  {
    try
    {
      PersonDocumentView row = (PersonDocumentView)getExternalContext().
        getRequestMap().get("row");
      String personDocId = row.getPersonDocId();
      if (personDocId != null)
        editingPersonDocument =
          KernelConfigBean.getPort().loadPersonDocument(personDocId);
    }
    catch (Exception e)
    {
      error(e);
    }
    return null;
  }

  public String cancelPersonDocument()
  {
    editingPersonDocument = null;
    return null;
  }

  public String storePersonDocument()
  {
    try
    {
      if (editingPersonDocument != null)
      {
        String objectId = editingPersonDocument.getDocId();
        String[] split = DocumentConfigBean.fromObjectId(objectId);
        editingPersonDocument.setDocId(split[0]);
        editingPersonDocument.setPersonId(getObjectId());
        KernelConfigBean.getPort().storePersonDocument(editingPersonDocument);
        editingPersonDocument = null;
      }
    }
    catch (Exception ex)
    {
      error(ex);
      List<String> details = WSExceptionFactory.getDetails(ex);
      if (details.size() > 0) error(details);
    }
    finally
    {
      editingPersonDocument = null;
      load();
    }
    return null;
  }

  public String removePersonDocument()
  {
    try
    {
      PersonDocumentView row = (PersonDocumentView)getRequestMap().get("row");
      KernelConfigBean.getPort().removePersonDocument(row.getPersonDocId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public List<SelectItem> getPersonDocumentSelectItems()
  {
    DocumentBean documentBean = (DocumentBean)getBean("document2Bean");
    if (editingPersonDocument != null)
      return documentBean.getSelectItems(editingPersonDocument.getDocId());
    else
      return documentBean.getSelectItems(ControllerBean.NEW_OBJECT_ID);
  }

  public Date getCreationDateTime()
  {
    if (editingPersonDocument != null && editingPersonDocument.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(editingPersonDocument.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (editingPersonDocument != null && editingPersonDocument.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(editingPersonDocument.getChangeDateTime());
    else
      return null;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      getEditingPersonDocument().getPersonDocTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getEditingPersonDocument().getPersonDocTypeId() != null &&
      getEditingPersonDocument().getPersonDocTypeId().trim().length() > 0;
  }

  @Override
  protected org.santfeliu.dic.Type getSelectedType()
  {
    return TypeCache.getInstance().getType(DictionaryConstants.PERSON_DOCUMENT_TYPE);
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        PersonDocumentFilter filter = new PersonDocumentFilter();
        filter.setPersonId(getObjectId());
        rows = KernelConfigBean.getPort().findPersonDocumentViews(filter);

        setRowsTypeLabels();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void setRowsTypeLabels()
  {
    for (PersonDocumentView personDocView : rows)
    {
      if (personDocView.getPersonDocId() != null)
      {
        String typeId = personDocView.getPersonDocTypeId();
        TypeCache typeCache = TypeCache.getInstance();
        try
        {
          Type type = typeCache.getType(typeId);
          if (type != null)
            personDocView.setPersonDocTypeId(type.getDescription());
        }
        catch (Exception ex)
        {
          warn(ex.getMessage());
        }
      }
    }
  }

}
