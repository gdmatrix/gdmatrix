package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.security.service.DBGrupUsuari;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.Property;
import org.santfeliu.jpa.JPAQuery;
import com.audifilm.matrix.security.service.SecurityManager;
import java.util.Iterator;
import javax.persistence.Query;
import org.santfeliu.jpa.QueryBuilder;

/**
 *
 * @author blanquepa
 */
public class FindCasesQueryBuilder extends QueryBuilder
{
  private static final String CASESTATE_CLASS = 
    "com.audifilm.matrix.cases.service.DBCaseState";
  private static final String SITUACIO_CLASS = 
    "com.audifilm.matrix.cases.service.DBSituacio";
  
  protected static final Logger log
    = Logger.getLogger(FindCasesQueryBuilder.class.getName());

  private List<String> userRoles;
  private String userId;
  private boolean counterQuery;
  private boolean caseAdmin;
  private CaseFilter caseFilter;
  private SecurityManager securityManager;

  final static private String FIND_CASES_WHERE
    = " WHERE "
    + "  trim(ap.aplId) = 'SDE' "
    + "  AND trim(ap.docorigen) = 'EXPED' "
    + "  AND e.caseTypeId = ap.docId ";

  private final StringBuilder select
    = new StringBuilder("SELECT ");
  private final StringBuilder from
    = new StringBuilder(" FROM Case e, AplDocument ap ");
  private final StringBuilder where
    = new StringBuilder(FIND_CASES_WHERE);
  private final StringBuilder orderBy
    = new StringBuilder(" ORDER BY e.caseId ");

  public FindCasesQueryBuilder(CaseFilter caseFilter,
    SecurityManager securityManager, org.santfeliu.security.User user)
  {
    this.caseFilter = caseFilter;
    this.securityManager = securityManager;
    this.userId = user.getUserId();
    this.userRoles = user.getRolesList();
  }

  public List<Property> getPropertiesFilter()
  {
    return caseFilter.getProperty();
  }

  public boolean isCounterQuery()
  {
    return counterQuery;
  }

  public void setCounterQuery(boolean counterQuery)
  {
    this.counterQuery = counterQuery;
  }

  public boolean isCaseAdmin()
  {
    return caseAdmin;
  }

  public void setCaseAdmin(boolean caseAdmin)
  {
    this.caseAdmin = caseAdmin;
  }

  public List<String> getUserRoles()
  {
    return userRoles;
  }

  public void setUserRoles(List<String> userRoles)
  {
    this.userRoles = userRoles;
  }

  public String getFilterTypeId()
  {
    return caseFilter.getCaseTypeId();
  }

  public CaseFilter getCaseFilter()
  {
    return caseFilter;
  }

  public JPAQuery getFilterCasesQuery(EntityManager em) throws Exception
  {
    StringBuilder buffer = new StringBuilder();

    appendSelect();
    appendWhereCaseId();
    appendWhereTitle();
    appendWhereDescription();
    appendWhereDates();
    if (!appendWhereTypeIds())
      return null;

    appendWhereClassId();
    appendWhereProperties();
    appendAditionalSearchExpression();

    buffer.append(select);
    buffer.append(from);
    buffer.append(where);
    if (!isCounterQuery())
      buffer.append(orderBy);

    System.out.println("FilterCasesQuery: SQL " + buffer);
    System.out.println("FilterCasesQuery: SQL " + parameters.toString());

    JPAQuery query = new JPAQuery(em.createQuery(buffer.toString()));
    try
    {
      setCaseFilterParameters(query);
    }
    catch (Exception ex)
    {
      Logger.getLogger(CaseManager.class.getName()).log(Level.SEVERE, null, ex);
    }

    return query;
  }

  private void appendSelect()
  {
    if (counterQuery)
    {
      select.append(" count(e) ");
    }
    else
    {
      select.append(" e, ap.classId ");
    }
  }

  private boolean appendWhereSecurityFilter()
  {

    if (isCaseAdmin())
      return true;

    StringBuilder whereSecurity = new StringBuilder();
    List<DBGrupUsuari> grups
      = securityManager.findGrupsUsuari(userId, null, null, null);

    if (!grups.isEmpty())
    {
      StringBuilder areaGrupDeps = new StringBuilder();
      boolean primer = true;
      for (DBGrupUsuari grup : grups)
      {

        areaGrupDeps.append(primer ? "'" : ", '");
        areaGrupDeps.append(grup.getAreaId().trim());
        areaGrupDeps.append(grup.getDepartamentId().trim());
        areaGrupDeps.append(grup.getGrupId().trim());
        areaGrupDeps.append("'");

        primer = false;
      }

      whereSecurity.append("e.caseId IN (");

      whereSecurity.append("SELECT distinct cs.caseId FROM CaseState cs ");
      whereSecurity.append(" WHERE cs.caseId = e.caseId ")
        .append("AND concat(trim(cs.areaId),concat(trim(cs.departamentId),")
        .append("trim(cs.grupId))) IN (")
        .append(areaGrupDeps).append(")");
      whereSecurity.append(")");

      where.append(" AND (").append(whereSecurity).append(") ");
      return true;
    }
    else
    {
      where.append(" AND (2=3)");
      return false;
    }

  }

  private boolean appendWhereTypeIds()
  {
    String filterTypeId = caseFilter.getCaseTypeId();
    if (filterTypeId != null && filterTypeId.equals("SDE"))
    {
      filterTypeId = null;
    }

    if (isCaseAdmin())
    {
      if (filterTypeId != null)
      {
        where.append("  AND (trim(e.caseTypeId) = :caseTypeId ")
          .append("OR :caseTypeId IS NULL)");
        parameters.put("caseTypeId", filterTypeId);
      }
    }
    else
    {

      Set<String> typeIds = getTypeIds();
      int i = 0;
      if (filterTypeId != null && filterTypeId.trim().length() > 0)
      {
        if (typeIds != null && typeIds.contains(filterTypeId))
        {
          where.append(" AND trim(e.caseTypeId) = :caseTypeId" + i);
          parameters.put("caseTypeId" + i, filterTypeId);
        }
        else
        {
          where.append(" AND 1=0");
          return false;
        }
      }
      else
      {
        if (typeIds != null && typeIds.size() > 0)
        {
          where.append(" AND (");

          Iterator it = typeIds.iterator();
          while (it.hasNext())
          {
            if (i != 0)
              where.append(" OR ");

            where.append(" trim(e.caseTypeId) = :caseTypeId" + i);
            parameters.put("caseTypeId" + i, (String) it.next());
            i++;
          }
          where.append(")");
        }
        else
        {
          where.append(" AND 1=0");
          return false;
        }
      }
    }
    return true;

  }

  private void appendWhereCaseId()
  {
    List<String> caseIdList = caseFilter.getCaseId();
    if (caseIdList != null && !caseIdList.isEmpty())
    {      
      appendOperator(where, "AND");
      appendOperator(where, "e.caseId", ":", "caseId", caseIdList);      
    }
  }

  private void appendWhereDates()
  {
    if (caseFilter.getDateComparator() == null
      || caseFilter.getDateComparator().equals("0"))
    {
      return;
    }

    if (caseFilter.getDateComparator().equals("1"))
    {
      if (caseFilter.getFromDate() != null)
      {
        where.append("AND (e.registryDate>=:registryFromDate)");
        parameters.put("registryFromDate", caseFilter.getFromDate());
      }
      if (caseFilter.getToDate() != null)
      {
        where.append("AND (e.registryDate<=:registryToDate)");
        parameters.put("registryToDate", caseFilter.getToDate());
      }
    }

    if (caseFilter.getDateComparator().equals("2"))
    {
      //DATA TANCAMENT
      where.append("AND (EXISTS (SELECT csd.endDate FROM CaseState csd ")
        .append("WHERE csd.caseId = e.caseId AND csd.stateId='9999' ");

      if (caseFilter.getFromDate() != null)
      {
        where.append(" AND csd.endDate >= :registryFromDate ");
        parameters.put("registryFromDate", caseFilter.getFromDate());
      }

      if (caseFilter.getToDate() != null)
      {
        where.append(" AND csd.endDate <= :registryToDate ");
        parameters.put("registryToDate", caseFilter.getToDate());
      }
      where.append("))");
    }

    if (caseFilter.getDateComparator().equals("3"))
    {
      //PERIODE ACTIU
      //DataObertura <= ToDate && DataTancament >= FromDate
      if (caseFilter.getFromDate() != null)
      {
        where.append("AND (");
        where.append("NOT EXISTS (SELECT csd.endDate FROM CaseState csd ")
          .append("WHERE csd.caseId = e.caseId and csd.stateId='9999' ")
          .append("and csd.endDate<:registryFromDate)")
          .append(")");
        parameters.put("registryFromDate", caseFilter.getFromDate());
      }

      if (caseFilter.getToDate() != null)
      {
        where.append("AND (e.registryDate<=:registryToDate)");
        parameters.put("registryToDate", caseFilter.getToDate());
      }
    }

  }

  private void appendWhereTitle()
  {
    if (caseFilter.getTitle() != null && !caseFilter.getTitle().equals(""))
    {
      String whereTitle = 
        CaseManager.evalFieldExpression("CaseFilter.title", this, 
          "UPPER(e.caseTypeNum) like :title", "");
      where.append(" AND (" + whereTitle + ")");
      parameters.put("title", caseFilter.getTitle().toUpperCase());
    }
  }

  private void appendWhereDescription()
  {
    if (caseFilter.getDescription() != null 
      && !caseFilter.getDescription().equals(""))
    {
      String whereTitle = 
        CaseManager.evalFieldExpression("CaseFilter.description", this, 
          "UPPER(e.sdetext) like :description", "");
      where.append(" AND (" + whereTitle + ")");
      parameters.put("description", caseFilter.getDescription().toUpperCase());
    }
  }

  private Set<String> getTypeIds()
  {
    Set<String> typeIds = new HashSet<String>();
    for (String role : userRoles)
    {
      List<String> types = RoleTypes.getTypes(role);
      if (types != null)
      {
        typeIds.addAll(types);
      }
    }

    return typeIds;
  }

  private void appendWhereProperties()
  {
    boolean filterByCaseState = false;
    boolean filterBySituacio = false;
    int filterByVariableCount = 0;

    for (Property prop : caseFilter.getProperty())
    {

      String name = prop.getName();
      String value = (prop.getValue() != null) ? prop.getValue().get(0) : null;

      DBCaseProperty cp = null;
      try
      {
        cp = DBCaseProperty.valueOf(name);
      }
      catch (IllegalArgumentException ex)
      {
      };
      if (cp != null)
      {
        where.append(" AND (").append(cp.filterJPAObjectAlias).append(".")
          .append(cp.fieldName).append(" =:").append(name)
          .append(" OR :").append(name).append(" IS NULL)");
        parameters.put(name, value);
        
        if (cp.className.equals(CASESTATE_CLASS))
          filterByCaseState = true;
        else if (cp.className.equals(SITUACIO_CLASS))
          filterBySituacio = true;

      }
      else if (name.startsWith("VAR"))
      {
        filterByVariableCount++;
        from.append(", CaseVariable cv").append(filterByVariableCount);

        where.append(" AND (").append(" cv")
          .append(filterByVariableCount)
          .append(".caseId=e.caseId").append(" AND cv")
          .append(filterByVariableCount)
          .append(".variableId=:variableId")
          .append(filterByVariableCount)
          .append(" AND UPPER(cv")
          .append(filterByVariableCount)
          .append(".value) LIKE :varvalue")
          .append(filterByVariableCount)
          .append(" )");

        parameters.put("variableId" + filterByVariableCount, 
          name.substring(3));
        parameters.put("varvalue" + filterByVariableCount, 
          value == null ? null : value.toUpperCase());
      }
    }
    if (filterByCaseState)
    {
      from.append(", CaseState cs");
      where.append(" AND cs.caseId = c.caseId");
    }
    if (filterBySituacio)
    {
      from.append(", Situacio s");
      where.append(" AND (e.caseId = s.caseId ")
        .append("AND UPPER(s.propertyValue) LIKE :situacio")
        .append(")");
    }

  }

  private void appendAditionalSearchExpression()
  {
    String searchExpression = caseFilter.getSearchExpression();
    if (searchExpression != null && !searchExpression.equals("")
      && !searchExpression.toUpperCase().contains("ORDER BY"))
    {
      where.append(" AND (").append(searchExpression).append(")");
    }
  }

  private void appendWhereClassId()
  {
    List<String> classIds = caseFilter.getClassId();
    if (classIds != null && !classIds.isEmpty())
    {
      appendOperator(where, "AND");
      appendOperator(where, "ap.classId", ":", "classId", classIds);
    }
  }

  private void setCaseFilterParameters(JPAQuery query) throws Exception
  {
    Set<Entry<String, Object>> paramSet = parameters.entrySet();
    for (Entry<String, Object> param : paramSet)
    {
      String name = param.getKey();
      Object value = param.getValue();
      query.setParameter(name, value);
    }
  }

  @Override
  public Query getQuery(EntityManager em) throws Exception
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
