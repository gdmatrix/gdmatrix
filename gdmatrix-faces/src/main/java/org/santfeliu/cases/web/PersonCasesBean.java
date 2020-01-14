package org.santfeliu.cases.web;

import java.util.List;

import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.santfeliu.util.TextUtils;

import org.santfeliu.web.obj.PageBean;


public class PersonCasesBean extends PageBean
{
  private List<CasePersonView> rows;
    
  public PersonCasesBean()
  {
    load();
  }

  public String show()
  {
    return "person_cases";
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        CasePersonFilter filter = new CasePersonFilter();
        filter.setPersonId(getObjectId());
        rows = 
          CaseConfigBean.getPort().findCasePersonViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public String showCase()
  {
    return getControllerBean().showObject("Case",
      (String)getValue("#{row.caseObject.caseId}"));
  }  
  
  public void setRows(List<CasePersonView> rows)
  {
    this.rows = rows;
  }

  public List<CasePersonView> getRows()
  {
    return rows;
  }
  
  public String getTypeDescription()
  {
    CasePersonView row = (CasePersonView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String type = row.getCaseObject().getCaseTypeId();
    
    CaseConfigBean caseConfigBean = (CaseConfigBean)getBean("caseConfigBean");
    String typeDescription = null;    
    try
    {
      typeDescription = caseConfigBean.getCaseTypeDescription(type);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return typeDescription;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }
  
  public String getViewStartDate()
  {
    String date = "";
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      date = row.getStartDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }

  public String getViewEndDate()
  {
    String date = "";
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      date = row.getEndDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }  

}
