package org.santfeliu.doc.web;

import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.dic.DictionaryConstants;
import org.santfeliu.cases.web.CaseBean;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.web.obj.PageBean;

public class DocumentCasesBean extends PageBean
{
  private List<CaseDocumentView> rows;
  private String editingCaseId;

  public DocumentCasesBean()
  {
    load();
  }

  public List<CaseDocumentView> getRows()
  {
    return rows;
  }

  public void setRows(List<CaseDocumentView> rows)
  {
    this.rows = rows;
  }

  public String getEditingCaseId()
  {
    return editingCaseId;
  }

  public void setEditingCaseId(String editingCaseId)
  {
    this.editingCaseId = editingCaseId;
  }

  public String show()
  {
    return "document_cases";
  }

  public String showDocumentCase()
  {
    return getControllerBean().showObject("Case",
      (String)getValue("#{row.caseObject.caseId}"));
  }

  public String searchCase()
  {
    return getControllerBean().searchObject("Case",
      "#{documentCasesBean.editingCaseId}");
  }

  public String storeDocumentCase()
  {
    try
    {
      String[] objectIdArray = DocumentConfigBean.fromObjectId(getObjectId());
      String docId = objectIdArray[0];
      CaseDocument caseDocument = new CaseDocument();
      caseDocument.setCaseId(editingCaseId);
      caseDocument.setDocId(docId);
      caseDocument.setCaseDocTypeId(DictionaryConstants.CASE_DOCUMENT_TYPE);
      CaseConfigBean.getPort().storeCaseDocument(caseDocument);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      editingCaseId = null;
      load();
    }
    return null;
  }

  public String removeDocumentCase()
  {
    try
    {
      CaseDocumentView row =
        (CaseDocumentView)getExternalContext().getRequestMap().get("row");
      CaseConfigBean.getPort().removeCaseDocument(row.getCaseDocId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public List<SelectItem> getDocumentCaseSelectItems()
  {
    CaseBean caseBean = (CaseBean)getBean("caseBean");
    return caseBean.getSelectItems(getEditingCaseId());
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
        String[] objectIdArray = DocumentConfigBean.fromObjectId(getObjectId());
        String docId = objectIdArray[0];
        CaseDocumentFilter filter = new CaseDocumentFilter();
        filter.setDocId(docId);
        rows = CaseConfigBean.getPort().findCaseDocumentViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
