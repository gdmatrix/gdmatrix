package org.santfeliu.cases.web;

import java.util.List;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author blanquepa
 */
public class AddressCasesBean extends PageBean
{
  private List<CaseAddressView> rows;
    
  public AddressCasesBean()
  {
    load();
  }

  public String show()
  {
    return "address_cases";
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        CaseAddressFilter filter = new CaseAddressFilter();
        filter.setAddressId(getObjectId());
        rows = 
          CaseConfigBean.getPort().findCaseAddressViews(filter);
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
  
  public void setRows(List<CaseAddressView> rows)
  {
    this.rows = rows;
  }

  public List<CaseAddressView> getRows()
  {
    return rows;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String getTypeDescription()
  {
    CaseAddressView row = (CaseAddressView)getFacesContext().getExternalContext().
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
}
