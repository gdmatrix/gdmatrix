package org.santfeliu.cases.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.Property;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.FilterUtils;
import org.santfeliu.web.obj.util.DynamicFormFilter;
import org.santfeliu.web.obj.util.FormFilter;

/**
 *
 * @author blanquepa
 */
public class CaseFormFilter extends DynamicFormFilter
  implements Serializable
{
  private String caseIdInput;
  private String classIdInput;
  private String titleInput;
  private String descriptionInput;
  private String stateInput;
  private String personIdInput;
  private String propertyNameInput1 = null;
  private String propertyValueInput1 = null;
  private String propertyNameInput2 = null;
  private String propertyValueInput2 = null;
  private String searchExpressionInput = null;
  private List<String> orderBy = new ArrayList();

  private CaseFilter caseFilter;

  public CaseFormFilter()
  {
    caseFilter = new CaseFilter();
  }

  //Accessors
  public String getCaseId()
  {
    return caseIdInput;
  }

  public void setCaseId(String caseIdInput)
  {
    this.caseIdInput = caseIdInput;
  }

  public String getClassId()
  {
    return classIdInput;
  }

  public void setClassId(String classIdInput)
  {
    this.classIdInput = classIdInput;
  }

  public String getDescription()
  {
    return descriptionInput;
  }

  public void setDescription(String descriptionInput)
  {
    this.descriptionInput = descriptionInput;
  }
  
  public String getPersonId()
  {
    return personIdInput;
  }
  
  public void setPersonId(String personId)
  {
    this.personIdInput = personId;
  }

  public String getPropertyName1()
  {
    return propertyNameInput1;
  }

  public void setPropertyName1(String propertyNameInput1)
  {
    this.propertyNameInput1 = propertyNameInput1;
  }

  public String getPropertyName2()
  {
    return propertyNameInput2;
  }

  public void setPropertyName2(String propertyNameInput2)
  {
    this.propertyNameInput2 = propertyNameInput2;
  }

  public String getPropertyValue1()
  {
    return propertyValueInput1;
  }

  public void setPropertyValue1(String propertyValueInput1)
  {
    this.propertyValueInput1 = propertyValueInput1;
  }

  public String getPropertyValue2()
  {
    return propertyValueInput2;
  }

  public void setPropertyValue2(String propertyValueInput2)
  {
    this.propertyValueInput2 = propertyValueInput2;
  }

  public String getState()
  {
    return stateInput;
  }

  public void setState(String state)
  {
    this.stateInput = state;
  }

  public String getTitle()
  {
    return titleInput;
  }

  public void setTitle(String titleInput)
  {
    this.titleInput = titleInput;
  }

  public String getSearchExpression()
  {
    return searchExpressionInput;
  }

  public void setSearchExpression(String searchExpressionInput)
  {
    this.searchExpressionInput = searchExpressionInput;
  }

  public CaseFilter getCaseFilter()
  {
    return caseFilter;
  }

  public void setCaseFilter(CaseFilter caseFilter)
  {
    this.caseFilter = new CaseFilter();
    if (caseFilter != null)
    {
      this.caseFilter.setCaseTypeId(caseFilter.getCaseTypeId());
      this.caseFilter.setDateComparator(caseFilter.getDateComparator());
      this.caseFilter.setDescription(caseFilter.getDescription());
      this.caseFilter.setFirstResult(caseFilter.getFirstResult());
      this.caseFilter.setFromDate(caseFilter.getFromDate());
      this.caseFilter.setMaxResults(caseFilter.getMaxResults());
      this.caseFilter.setSearchExpression(caseFilter.getSearchExpression());
      this.caseFilter.setState(caseFilter.getState());
      this.caseFilter.setTitle(caseFilter.getTitle());
      this.caseFilter.setToDate(caseFilter.getToDate());
      this.caseFilter.setPersonId(caseFilter.getPersonId());
      this.caseFilter.setPersonFlag(caseFilter.getPersonFlag());
      this.caseFilter.getCaseId().clear();
      this.caseFilter.getCaseId().addAll(caseFilter.getCaseId());
      this.caseFilter.getClassId().clear();
      this.caseFilter.getClassId().addAll(caseFilter.getClassId());
      this.caseFilter.getProperty().clear();
      this.caseFilter.getProperty().addAll(caseFilter.getProperty());
    }
  }

  public void setCaseTypeId(String caseTypeId)
  {
    this.caseFilter.setCaseTypeId(caseTypeId);
  }

  public void setDefaultDateFilter(String fromDate, String toDate,
    String dateComparator)
  {
    caseFilter.setFromDate(fromDate);
    caseFilter.setToDate(toDate);
    caseFilter.setDateComparator(dateComparator);
  }

  @Override
  public void setFirstResult(int value)
  {
    this.caseFilter.setFirstResult(value);
  }

  @Override
  public void setMaxResults(int value)
  {
    this.caseFilter.setMaxResults(value);
  }


  //Actions
  public void clearLists()
  {
    caseFilter.getCaseId().clear();
    caseFilter.getClassId().clear();
    caseFilter.getProperty().clear();
    if (orderBy != null) orderBy.clear();
  }

  /**
   * This method copies and transforms input properties to inner object filter
   * @param formProperties
   */
  public void setInputProperties(List<Property> formProperties)
  {
    //caseId
    if (!StringUtils.isBlank(caseIdInput))
      caseFilter.getCaseId().add(caseIdInput.trim());
    //classId
    if (!StringUtils.isBlank(classIdInput))
      setClassIdFilter();
    //title with wildcards
    if (!StringUtils.isBlank(titleInput))
      caseFilter.setTitle(FilterUtils.addWildcards(titleInput));
    else if (DictionaryUtils.getPropertyByName(formProperties, "title") != null)
      caseFilter.setTitle(FilterUtils.addWildcards(caseFilter.getTitle()));
    else
      caseFilter.setTitle(null);
    //description with wildcards
    if (!StringUtils.isBlank(descriptionInput))
      caseFilter.setDescription(FilterUtils.addWildcards(descriptionInput));
    else if (DictionaryUtils.getPropertyByName(formProperties, "description") != null)
      caseFilter.setDescription(FilterUtils.addWildcards(caseFilter.getDescription()));
    else
      caseFilter.setDescription(null);
    //state with wildcards
    if (!StringUtils.isBlank(stateInput))
      caseFilter.setState(FilterUtils.addWildcards(stateInput));
    else if (DictionaryUtils.getPropertyByName(formProperties, "state") != null)
      caseFilter.setState(FilterUtils.addWildcards(caseFilter.getState()));
    else
      caseFilter.setState(null);
    //personId
    if (!StringUtils.isBlank(personIdInput))
      caseFilter.setPersonId(personIdInput);
    else if (DictionaryUtils.getPropertyByName(formProperties, "personId") == null)
      caseFilter.setPersonId(null);

    //searchExpression && orderBy
    String searchExpression = null;
    if (!StringUtils.isBlank(searchExpressionInput))
      searchExpression = searchExpressionInput;
    else if (DictionaryUtils.getPropertyByName(formProperties, "searchExpression") != null)
      searchExpression = caseFilter.getSearchExpression();

    if (orderBy != null && !orderBy.isEmpty())
    {
      StringBuilder buffer = new StringBuilder(" ORDER BY ");
      boolean firstColumn = true;
      for (String column : orderBy)
      {
        if (column.startsWith("property[") && column.endsWith("]"))  
          column = column.replaceAll("property\\[|\\]", "");
        
        if (!firstColumn)
          buffer.append(", ");
        else
          firstColumn = false;

        String[] parts = column.split(":");
        buffer.append(parts[0]).append(" ");
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
          buffer.append(" desc ");
      }
      
      String expression = null;
      if (!StringUtils.isBlank(expression))
      {
        if (!searchExpression.toUpperCase().contains("ORDER BY"))
          buffer.insert(0, searchExpression);
        else
        {
          String[] parts = searchExpression.toUpperCase().split("ORDER BY");
          buffer.insert(0, parts[0]);
          buffer.append(parts[1]);
        }
      }

      caseFilter.setSearchExpression(buffer.toString());
    }
    else
      caseFilter.setSearchExpression(searchExpression);
  }

  public void setOrderBy(List<String> orderBy)
  {
    this.orderBy = new ArrayList();
    this.orderBy.addAll(orderBy);
  }

  public boolean isOrderBySet()
  {
    return orderBy != null && !orderBy.isEmpty();
  }

  @Override
  protected void copy(FormFilter src, FormFilter dst)
  {
    CaseFormFilter dff = new CaseFormFilter();
    if (src instanceof CaseFormFilter && dst instanceof CaseFormFilter)
    {
      CaseFormFilter sff = (CaseFormFilter)src;
      dff.setCaseFilter(sff.getCaseFilter());
      dff.setCaseId(sff.getCaseId());
      dff.setClassId(sff.getClassId());
      dff.setDescription(sff.getDescription());
      dff.setPropertyName1(sff.getPropertyName1());
      dff.setPropertyName2(sff.getPropertyName2());
      dff.setPropertyValue1(sff.getPropertyValue1());
      dff.setPropertyValue2(sff.getPropertyValue2());
      dff.setState(sff.getState());
      dff.setTitle(sff.getTitle());
      dff.setPersonId(sff.getPersonId());
      dst = dff;
    }
  }

  @Override
  protected Object getObjectFilter()
  {
    return caseFilter;
  }

  @Override
  protected void clearAll()
  {
    caseIdInput = null;
    classIdInput = null;
    titleInput = null;
    descriptionInput = null;
    stateInput = null;
    personIdInput = null;
    propertyNameInput1 = null;
    propertyValueInput1 = null;
    propertyNameInput2 = null;
    propertyValueInput2 = null;
    searchExpressionInput = null;
    caseFilter = new CaseFilter();
    orderBy = null;
  }

  private void setClassIdFilter()
  {
    org.santfeliu.classif.Class classObject =
      ClassCache.getInstance().getClass(classIdInput);
    if (classObject != null)
    {
      caseFilter.getClassId().add(classObject.getClassId());
      for (org.santfeliu.classif.Class subClass :
        classObject.getSubClasses(true))
      {
        caseFilter.getClassId().add(subClass.getClassId());
      }
    }
    else
      caseFilter.getClassId().add(classIdInput);
  }

  @Override
  public boolean isEmpty()
  {
    return (StringUtils.isBlank(getCaseId()) 
      && StringUtils.isBlank(getTitle())
      && StringUtils.isBlank(getClassId())
      && StringUtils.isBlank(getPropertyName1())
      && StringUtils.isBlank(getPropertyName2())
      && StringUtils.isBlank(getCaseFilter().getFromDate())
      && StringUtils.isBlank(getCaseFilter().getToDate())
      && StringUtils.isBlank(getCaseFilter().getCaseTypeId())
      && StringUtils.isBlank(getCaseFilter().getPersonId())
      && getCaseFilter().getProperty().isEmpty()
      ) || (!StringUtils.isBlank(getCaseFilter().getPersonId()) && StringUtils.isBlank(getCaseFilter().getCaseTypeId()));
  }
  
  public List<Property> getProperty()
  {
    if (caseFilter == null)
      caseFilter = new CaseFilter();
    return caseFilter.getProperty();
  }

}
