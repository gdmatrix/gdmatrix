package org.santfeliu.cases.web.detail;

import java.util.ArrayList;
import java.util.List;
import org.matrix.cases.Case;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.util.ResultsManager;

/**
 *
 * @author blanquepa
 */
public class CasesDetailPanel extends TabulatedDetailPanel
{
  public static final String NORMAL_DIRECTION = "normal";
  public static final String REVERSE_DIRECTION = "reverse";
  public static final String BOTH_DIRECTION = "both";

  public static final String ALLOWED_TYPEIDS_PROPERTY = "allowedTypeIds";
  public static final String FORBIDDEN_TYPEIDS_PROPERTY = "forbiddenTypeIds";
  public static final String CASE_SEARCH_MID_PROPERTY = "caseSearchMid";
  public static final String DIRECTION_PROPERTY = "direction";
  public static final String PAGE_SIZE = "pageSize";

  private List<CaseCaseView> caseCases;
  private ResultsManager resultsManager;

  public CasesDetailPanel()
  {
    resultsManager =
      new ResultsManager(
        "org.santfeliu.cases.web.resources.CaseBundle", "caseCases_");
    resultsManager.addDefaultColumn("relCase.caseId");
    resultsManager.addDefaultColumn("relCase.title");
  }
  
  @Override
  public void loadData(DetailBean detailBean)
  {
    caseCases = new ArrayList();    
    resultsManager.setColumns(getMid());
    try
    {
      String caseId = ((CaseDetailBean) detailBean).getCaseId();
      CaseCaseFilter filter = new CaseCaseFilter();
      filter.setCaseId(caseId);

      String direction = getProperty(DIRECTION_PROPERTY);

      //Normal direction
      if (direction == null || direction.equals(NORMAL_DIRECTION) ||
          direction.equals(BOTH_DIRECTION))
      {
        List<CaseCaseView> rows =
          CaseConfigBean.getPort().findCaseCaseViews(filter);
        for (CaseCaseView row : rows)
        {
          String caseCaseTypeId = row.getCaseCaseTypeId();
          String relCaseTypeId = row.getRelCase().getCaseTypeId();
          if (isAllowedTypeId(caseCaseTypeId) || isAllowedTypeId(relCaseTypeId))
          {
            List<String> columnNames = resultsManager.getColumnNames();
            for (String columnName : columnNames)
            {
              if (columnName.contains("relCase.property[") && columnName.contains("]"))
              {
                String id = row.getRelCase().getCaseId();
                Case c =
                  CaseConfigBean.getPort().loadCase(id);
                row.setRelCase(c);
              }
            }
            caseCases.add(row);
          }
        }
      }

      //Reverse direction
      if (direction != null &&
        (direction.equals(REVERSE_DIRECTION) || direction.equals(BOTH_DIRECTION)))
      {
        List<CaseCaseView> revRows = new ArrayList();
        filter = new CaseCaseFilter();
        filter.setRelCaseId(caseId);
        revRows = CaseConfigBean.getPort().findCaseCaseViews(filter);
        for (CaseCaseView row : revRows)
        {
          String caseCaseTypeId = row.getCaseCaseTypeId();
          String mainCaseTypeId = row.getMainCase().getCaseTypeId();
          if (isAllowedTypeId(caseCaseTypeId) || isAllowedTypeId(mainCaseTypeId))
          {
            if (direction.equals(BOTH_DIRECTION))
            {
              CaseCaseView aux = new CaseCaseView();
              aux.setMainCase(row.getRelCase());
              aux.setRelCase(row.getMainCase());
              row = aux;
            }

            List<String> columnNames = resultsManager.getColumnNames();
            for (String columnName : columnNames)
            {
              if (columnName.contains("mainCase.property[") && columnName.contains("]"))
              {
                String id = row.getMainCase().getCaseId();
                Case c = CaseConfigBean.getPort().loadCase(id);
                row.setMainCase(c);
              }
            }

            caseCases.add(row);
          }
        }
      }

      List<String> orderBy = getMultivaluedProperty(ResultsManager.ORDERBY);
      if (orderBy != null && !orderBy.isEmpty())
        resultsManager.sort(caseCases, orderBy);
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public List<CaseCaseView> getCaseCases()
  {
    return caseCases;
  }

  public void setCaseCases(List<CaseCaseView> caseCases)
  {
    this.caseCases = caseCases;
  }

  @Override
  public boolean isRenderContent()
  {
    return (caseCases != null && !caseCases.isEmpty());
  }

  @Override
  public String getType()
  {
    return "cases";
  }
  
  public Integer getPageSize()
  {
    return Integer.valueOf(getProperty(PAGE_SIZE, "10"));
  }

  private List<String> getAllowedDocumentTypeIds()
  {
    return getMultivaluedProperty(ALLOWED_TYPEIDS_PROPERTY);
  }

  private List<String> getForbiddenDocumentTypeIds()
  {
    return getMultivaluedProperty(FORBIDDEN_TYPEIDS_PROPERTY);
  }

  private boolean isAllowedTypeId(String typeId)
  {
    return (getAllowedDocumentTypeIds().isEmpty() || isDerivedFrom(getAllowedDocumentTypeIds(), typeId)) &&
      (getForbiddenDocumentTypeIds().isEmpty() || !isDerivedFrom(getForbiddenDocumentTypeIds(), typeId));
  }
  
  private boolean isDerivedFrom(List<String> typeIds, String typeId)
  {
    if (typeId == null)
      return false;
    
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      for (String allowedTypeId : typeIds)
      {
        if (type.isDerivedFrom(allowedTypeId))
          return true;
      }
    }    
    return false;
  }

  private boolean isBidirectional()
  {
    String direction = getProperty(DIRECTION_PROPERTY);
    return direction != null && direction.equalsIgnoreCase(BOTH_DIRECTION);
  }

    private boolean isReverse()
  {
    String direction = getProperty(DIRECTION_PROPERTY);
    return direction != null && direction.equalsIgnoreCase(REVERSE_DIRECTION);
  }

  public ResultsManager getResultsManager()
  {
    return resultsManager;
  }

  public void setResultsManager(ResultsManager resultsManager)
  {
    this.resultsManager = resultsManager;
  }

  public String sort()
  {
    resultsManager.sort(caseCases);
    return null;
  }

  public String getShowCaseUrl()
  {
    String vb = 
      isReverse() ? "#{row.mainCase.caseId}" : "#{row.relCase.caseId}";
    String caseId = (String)getValue(vb);
    String searchMid = getProperty(CASE_SEARCH_MID_PROPERTY);
    return "/go.faces?xmid=" + searchMid + "&caseid=" + caseId;
  }
}
