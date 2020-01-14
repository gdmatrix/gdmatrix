package org.santfeliu.cases.web;

import java.util.List;
import org.matrix.cases.Case;

import org.matrix.cases.Demand;
import org.matrix.cases.CaseManagerPort;

import org.matrix.cases.DemandFilter;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.obj.TypifiedPageBean;


public class CaseDemandsBean extends TypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_demandRootTypeId";

  private List<Demand> rows;
  private Demand editingDemand;
  
  public CaseDemandsBean()
  {
    super(DictionaryConstants.DEMAND_TYPE, "CASE_ADMIN");

    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();
    Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
    if (caseType != null)
    {
      PropertyDefinition pd =
        caseType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        setRootTypeId(pd.getValue().get(0));
    }

    load();    
  }
  
  public String show()
  {
    return "case_demands";
  }
  
  public String store()
  {
    if (editingDemand != null)
    {
      storeDemand();
    }
    else
    {
      load();
    }
    return show();
  }

  public String createDemand()
  {
    editingDemand = new Demand();
    return null;
  }
  
  public String editDemand()
  {
    try
    {
      Demand row = (Demand)getExternalContext().
        getRequestMap().get("row");   
      String demandId = row.getDemandId();
      if (demandId != null)
        editingDemand =
          CaseConfigBean.getPort().loadDemand(demandId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String storeDemand()
  {
    try
    {
      String caseId = getObjectId();
      editingDemand.setCaseId(caseId);
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeDemand(editingDemand);
      editingDemand = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }    
  
  public String removeDemand()
  {
    try
    {
      Demand row = (Demand)getRequestMap().get("row");
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeDemand(row.getDemandId());
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelDemand()
  {
    editingDemand = null;
    return null;
  }  
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        DemandFilter filter = new DemandFilter();
        filter.setCaseId(getObjectId());
        rows = CaseConfigBean.getPort().findDemands(filter);
        
        setRowsTypeLabels();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void setRows(List<Demand> rows)
  {
    this.rows = rows;
  }

  public List<Demand> getRows()
  {
    return rows;
  }

  public void setEditingDemand(Demand editingDemand)
  {
    this.editingDemand = editingDemand;
  }

  public Demand getEditingDemand()
  {
    return editingDemand;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String showEditType()
  {
    return getControllerBean().showObject("Type", 
      getEditingDemand().getDemandTypeId());
  }

  public boolean isRenderShowEditTypeButton()
  {
    return getEditingDemand().getDemandTypeId() != null &&
      getEditingDemand().getDemandTypeId().trim().length() > 0;
  }

  public String showRowType()
  {
    return getControllerBean().showObject("Type", getRowTypeId());
  }

  private String getRowTypeId()
  {
    try
    {
      Demand row = (Demand)getExternalContext().getRequestMap().get("row");
      String demandId = row.getDemandId();
      if (demandId != null)
      {
        return CaseConfigBean.getPort().loadDemand(demandId).getDemandTypeId();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  private void setRowsTypeLabels()
  {
    for (Demand demand : rows)
    {
      if (demand.getDemandId() != null)
      {
        String typeId = demand.getDemandTypeId();
        TypeCache typeCache = TypeCache.getInstance();
        try
        {
          Type type = typeCache.getType(typeId);
          if (type != null)
            demand.setDemandTypeId(type.formatTypePath(
              false, true, false, getRootTypeId()));
        }
        catch (Exception ex)
        {
          warn(ex.getMessage());
        }
      }
    }
  }  
}
