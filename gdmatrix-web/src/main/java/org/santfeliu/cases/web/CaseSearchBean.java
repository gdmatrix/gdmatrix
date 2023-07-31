/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.cases.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.cases.CaseFilter;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.cases.web.detail.CaseDetailBean;
import org.santfeliu.classif.web.ClassBean;
import org.santfeliu.classif.web.ClassSearchBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.DynamicTypifiedSearchBean;
import org.santfeliu.util.keywords.KeywordsManager;
import org.santfeliu.faces.convert.TypeIdConverter;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.kernel.web.PersonSearchBean;
import org.santfeliu.util.FilterUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DefaultDetailBean;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.util.ColumnDefinition;
import org.santfeliu.web.obj.util.JumpData;
import org.santfeliu.web.obj.util.SetObjectManager;
import org.santfeliu.web.obj.util.ParametersManager;
import org.santfeliu.web.obj.util.CheckJumpSuitability;

/**
 *
 * @author blanquepa
 */
@CMSManagedBean
public class CaseSearchBean extends DynamicTypifiedSearchBean 
  implements CheckJumpSuitability
{
  @CMSProperty
  public static final String SEARCH_CASE_TYPE_PROPERTY = "searchCaseType";
  @CMSProperty
  public static final String SEARCH_CASE_PROPERTY_NAME = 
    "searchCasePropertyName";
  @CMSProperty
  public static final String SEARCH_CASE_PROPERTY_VALUE = 
    "searchCasePropertyValue";
  @CMSProperty
  public static final String ORDERBY_PROPERTY = "orderBy";
  @CMSProperty
  public static final String HEADER_DOCUMENT_PROPERTY = "header.docId";
  @CMSProperty
  public static final String HEADER_RENDER_PROPERTY = "header.render";
  @CMSProperty
  public static final String FOOTER_DOCUMENT_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String FOOTER_RENDER_PROPERTY = "footer.render";
  @CMSProperty
  public static final String FIRSTLOAD_PROPERTY =
    "resultList.showOnFirstRequest";
  @CMSProperty
  public static final String FIRSTLOAD_FILTER_BY_DATE_PROPERTY =
    "resultList.showOnFirstRequest.filterByDate";
  @CMSProperty
  public static final String RENDER_CASEID_PROPERTY = "renderCaseId";
  @CMSProperty
  public static final String RENDER_TITLE_PROPERTY = "renderTitle";
  @CMSProperty
  public static final String RENDER_DESCRIPTION_PROPERTY = "renderDescription";
  @CMSProperty
  public static final String RENDER_DATE_PROPERTY = "renderDate";
  @CMSProperty
  public static final String RENDER_CLASSID_PROPERTY = "renderClassId";
  @CMSProperty
  public static final String RENDER_PERSONID_PROPERTY = "renderPersonId";  
  @CMSProperty
  public static final String RENDER_SEARCH_EXPRESSION_PROPERTY =
    "renderSearchExpression";
  @CMSProperty
  public static final String RENDER_TYPE_PROPERTY = "renderType";
  @CMSProperty
  public static final String RENDER_DYNAMIC_FORM_PROPERTY = "renderDynamicForm";
  @CMSProperty
  public static final String RENDER_FILTER_PANEL = "renderFilterPanel";
  @CMSProperty
  public static final String RENDER_PROPERTY_VALUE_FILTER = 
    "renderPropertyValueFilter";
  @CMSProperty
  public static final String RENDER_CLEAR_BUTTON = "renderClearButton";
  @CMSProperty
  public static final String LOAD_METADATA_PROPERTY = "loadMetadata";
  @CMSProperty
  public static final String FORM_WILDCARD_PROPERTIES = 
    "formWildcardProperties";  
  @CMSProperty
  public static final String RENDER_COLLAPSIBLE_PANEL_COLLAPSED = 
    "renderCollapsiblePanelCollapsed";  
  @CMSProperty
  public static final String FIND_AS_ADMIN_FOR_PROPERTY = "findAsAdminFor";
  @CMSProperty
  public static final String ENABLE_TRANSLATION_PROPERTY = 
    "searchCaseEnableTranslation";
  @CMSProperty
  public static final String RESULTS_LAYOUT_PROPERTY = "resultsLayout";
  public static final String RESULTS_LAYOUT_LIST = "list";
  public static final String RESULTS_LAYOUT_TABLE = "table";
  
  public static final String ALL_USERS = "%";
  
  //Dictionary properties
  public static final String PERSON_SEARCH_ENABLED = "_personSearchEnabled";  

  public static final String SHOW_DETAIL_MODE = "detail";
  public static final String SHOW_EDIT_MODE = "edit";
  public static final String DOC_SERVLET_URL = "/documents/";

  private transient List<SelectItem> typeSelectItems;

  private String headerBrowserUrl;
  private String footerBrowserUrl;

  private KeywordsManager keywordsManager;
  private SetObjectManager setObjectManager;
  
  private CaseFormFilter filter;
  
  private boolean collapsePanel = true; //Collapsed by default


  public CaseSearchBean()
  {
    super("org.santfeliu.cases.web.resources.CaseBundle", "case_", "caseTypeId");
    typeSelectItems = null;
    filter = new CaseFormFilter();
    setObjectManager = new SetObjectManager(filter);
  }
  
  public boolean isCollapsePanel()
  {
    return collapsePanel;
  }

  //Getters & Setters
  public void setCollapsePanel(boolean collapsePanel)  
  {
    this.collapsePanel = collapsePanel;
  }

  public void setFilter(CaseFormFilter filter)
  {
    this.filter = filter;
  }

  public CaseFormFilter getFilter()
  {
    return filter;
  }

  public String getDescription()
  {
    return filter.getDescription();
  }

  public void setDescription(String description)
  {
    filter.setDescription(description);
  }

  public String getState()
  {
    return filter.getState();
  }

  public void setState(String state)
  {
    filter.setState(state);
  }

  public String getTitleInput()
  {
    return filter.getTitle();
  }

  public void setTitleInput(String title)
  {
    filter.setTitle(title);
  }

  public String getClassId()
  {
    return filter.getClassId();
  }

  public void setClassId(String objectId)
  {
    ClassBean classBean = (ClassBean)getBean("classBean");
    filter.setClassId(classBean.getClassId(objectId));
  }
  
  public String getPersonId()
  {
    return filter.getPersonId();
  }
  
  public void setPersonId(String personId)
  {
    filter.setPersonId(personId);
  }

  public String getCaseId()
  {
    return filter.getCaseId();
  }

  public void setCaseId(String caseId)
  {
    filter.setCaseId(caseId);
  }

  public String getPropertyName1()
  {
    return filter.getPropertyName1();
  }

  public void setPropertyName1(String propertyNameFilter1)
  {
    filter.setPropertyName1(propertyNameFilter1);
  }

  public String getPropertyName2()
  {
    return filter.getPropertyName2();
  }

  public void setPropertyName2(String propertyNameFilter2)
  {
    filter.setPropertyName2(propertyNameFilter2);
  }

  public String getPropertyValue1()
  {
    return filter.getPropertyValue1();
  }

  public void setPropertyValue1(String propertyValueFilter1)
  {
    filter.setPropertyValue1(propertyValueFilter1);
  }

  public String getPropertyValue2()
  {
    return filter.getPropertyValue2();
  }

  public void setPropertyValue2(String propertyValueFilter2)
  {
    filter.setPropertyValue2(propertyValueFilter2);
  }

  public String getDateComparator()
  {
    return filter.getCaseFilter().getDateComparator();
  }

  public void setDateComparator(String value)
  {
    filter.getCaseFilter().setDateComparator(value);
  }
  
  public String getPersonFlag()
  {
    return filter.getCaseFilter().getPersonFlag();
  }
  
  public void setPersonFlag(String value)
  {
    filter.getCaseFilter().setPersonFlag(value);
  }

  public String getFromDate()
  {
    return filter.getCaseFilter().getFromDate();
  }

  public void setFromDate(String value)
  {
    filter.getCaseFilter().setFromDate(value);
  }

  public String getToDate()
  {
    return filter.getCaseFilter().getToDate();
  }

  public void setToDate(String value)
  {
    filter.getCaseFilter().setToDate(value);
  }
  
  public String getSearchExpression()
  {
    return filter.getSearchExpression();
  }
  
  public void setSearchExpression(String value)
  {
    filter.setSearchExpression(value);
  }

  public String getFooterBrowserUrl()
  {
    String docId = getProperty(FOOTER_DOCUMENT_PROPERTY);
    if (docId != null)
      footerBrowserUrl = getContextPath() + DOC_SERVLET_URL + docId;

    return footerBrowserUrl;
  }

  public void setFooterBrowserUrl(String footerBrowserUrl)
  {
    this.footerBrowserUrl = footerBrowserUrl;
  }

  public String getHeaderBrowserUrl()
  {
    String docId = getProperty(HEADER_DOCUMENT_PROPERTY);
    if (docId != null)
      headerBrowserUrl = getContextPath() + DOC_SERVLET_URL + docId;

    return headerBrowserUrl;
  }

  public void setHeaderBrowserUrl(String headerBrowserUrl)
  {
    this.headerBrowserUrl = headerBrowserUrl;
  }

  public boolean isHeaderRender()
  {
    String value = getProperty(HEADER_RENDER_PROPERTY);
    if (value == null) return false;
    else return "true".equalsIgnoreCase(value);
  }

  public boolean isFooterRender()
  {
    String value = getProperty(FOOTER_RENDER_PROPERTY);
    if (value == null) return false;
    else return "true".equalsIgnoreCase(value);
  }

  public boolean isFirstLoadFilterByDate()
  {
    String value = getProperty(FIRSTLOAD_FILTER_BY_DATE_PROPERTY);
    if (value == null) return true;
    else return "true".equalsIgnoreCase(value);
  }

  //User actions
  @Override
  public int countResults()
  {
    try
    {
      filter.clearLists();

      //apply node filters
      String caseTypeId = getProperty(SEARCH_CASE_TYPE_PROPERTY);
      if (caseTypeId != null && !isRenderCaseType()) 
        setCurrentTypeId(caseTypeId);

      setSearchDynamicProperties(filter);

      //apply dynamic properties filters
      List<Property> properties = getFormDataAsProperties();
      
      keywordsManager = KeywordsManager.newInstance(properties);
      filter.setFormProperties(addWildcards(properties));
      
      //apply input properties
      filter.setInputProperties(properties);
      //set additional values
      filter.setCaseTypeId(getCurrentTypeId());

      if (!filter.isEmpty())
        return callCountResults(filter);
      else
        error("FILTER_IS_EMPTY");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.clearLists();
      //apply node filters
      String caseTypeId = getProperty(SEARCH_CASE_TYPE_PROPERTY);
      if (caseTypeId != null && !isRenderCaseType())
        setCurrentTypeId(caseTypeId);

      setSearchDynamicProperties(filter);

      List<String> orderBy =
        getSelectedMenuItem().getMultiValuedProperty(ORDERBY_PROPERTY);
      if (orderBy != null && !orderBy.isEmpty())
        filter.setOrderBy(orderBy);

      //apply dynamic form properties
      List<Property> properties = getFormDataAsProperties();
      keywordsManager = KeywordsManager.newInstance(properties);
      filter.setFormProperties(addWildcards(properties));
      //apply filter lists (set from xInput properties)
      filter.setInputProperties(properties);
      //set additional values
      if (!isRenderCaseType())
        filter.setCaseTypeId(getCurrentTypeId());
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      if (!filter.isEmpty())
        return callFindResults(filter);
      else
        error("FILTER_IS_EMPTY");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @CMSAction
  @Override
  public String show()
  {
    setHeaderBrowserUrl(null);
    setFooterBrowserUrl(null);
    
    ParametersManager[] managers = {jumpManager, setObjectManager};
    String outcome = executeParametersManagers(managers);
    if (outcome != null)
      return outcome;

    configureColumns();

    String caseTypeId = getProperty(SEARCH_CASE_TYPE_PROPERTY);
    if (caseTypeId != null)
      setCurrentTypeId(caseTypeId);
    
    if (hasFirstLoadProperty())
      search();

    return "case_search";
  }

  /**
   * Called only from action node (if defined). Other objects don't call never 
   * showFirst(). It allows to reset filter and results if necessary.
   * @return outcome
   */
  @CMSAction
  public String showFirst()
  {
    filter.clearAll();

    setHeaderBrowserUrl(null);
    setFooterBrowserUrl(null);
    
    if (hasFirstLoadProperty())
    {
      String now = new SimpleDateFormat("yyyyMMdd").format(new Date());
      if (isFirstLoadFilterByDate())
      {
        filter.setDefaultDateFilter(now, now, "3");        
      }
    }
    
    ParametersManager[] managers = {jumpManager, setObjectManager};
    String outcome = executeParametersManagers(managers);
    if (outcome != null)
      return outcome;

    configureColumns();

    String caseTypeId = getProperty(SEARCH_CASE_TYPE_PROPERTY);
    if (caseTypeId != null)
      setCurrentTypeId(caseTypeId);
    
    String collapsed = getProperty(RENDER_COLLAPSIBLE_PANEL_COLLAPSED);
    collapsePanel = (collapsed == null || collapsed.equalsIgnoreCase("true"));

    if (hasFirstLoadProperty())
    {
      //String now = new SimpleDateFormat("yyyyMMdd").format(new Date());
      //filter.setDefaultDateFilter(now, now, "3");

      dynamicFormsManager.setFormDataFromProperties(
        setObjectManager.getProcessedParameters(), null);

      search();
    }
    else
      reset();

    return "case_search";
  }

  public String clearFilter()
  {
    //Clear formFilter
    if (filter != null)
      filter.clearAll();

    //Clear dynamicForm data
    if (dynamicFormsManager != null)
      dynamicFormsManager.getData().clear();
    
    //Set default values
    setCurrentTypeId(getProperty(SEARCH_CASE_TYPE_PROPERTY));
    if (hasFirstLoadProperty())
    {      
      String now = new SimpleDateFormat("yyyyMMdd").format(new Date());
      if (isFirstLoadFilterByDate())
      {
        filter.setDefaultDateFilter(now, now, "3");        
      }
    }

    //Clear results
    reset();

    return "case_search";
  }
  
  @Override
  public String showObject(String typeId, String caseId)
  {
    String showMode = getProperty(SHOW_MODE);
    return showCase(caseId, showMode);
  }

  public String showCase()
  {
    String caseId = (String)getValue("#{row.caseId}");
    return showObject(DictionaryConstants.CASE_TYPE, caseId);
  }

  public String showCase(String caseId, String showMode)
  {
    if (showMode != null && showMode.equals(SHOW_DETAIL_MODE))
    {
      //detail mode
      if (getProperty(DefaultDetailBean.DETAIL_PANELS_MID) == null)
      {
        return getControllerBean().showObject(
          DictionaryConstants.CASE_TYPE, caseId);
      }
      else
        return showDetail(caseId);
    }
    else
    {
      //edit mode
      return getControllerBean().showObject(
        DictionaryConstants.CASE_TYPE, caseId);
    }
  }

  public String getShowLinkUrl()
  {
    String caseId = (String)getValue("#{row.caseId}");
    return getShowLinkUrl("caseid", caseId);
  }
  
  public String selectCase()
  {
    Case row = (Case)getExternalContext().getRequestMap().get("row");
    String caseId = row.getCaseId();
    return getControllerBean().select(caseId);
  }

  @Override
  public String searchType()
  {
    return searchType(DictionaryConstants.CASE_TYPE ,
      "#{caseSearchBean.currentTypeId}");
  }
  
  @Override
  public void setCurrentTypeId(String typeId)
  {
    this.typeSelectItems = null;
    super.setCurrentTypeId(typeId);
  }

  public String searchClass()
  {
    ClassSearchBean classSearchBean = 
      (ClassSearchBean)getBean("classSearchBean");
    if (classSearchBean == null)
      classSearchBean = new ClassSearchBean();

    classSearchBean.search();

    return getControllerBean().searchObject("Class",
      "#{caseSearchBean.classId}");
  }
  
  public String searchPerson()
  {
    PersonSearchBean personSearchBean = 
      (PersonSearchBean)getBean("personSearchBean");
    if (personSearchBean == null)
      personSearchBean = new PersonSearchBean();

    personSearchBean.search();

    return getControllerBean().searchObject("Person",
      "#{caseSearchBean.personId}");
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      if (typeSelectItems == null)
      {
        TypeBean typeBean = (TypeBean)getBean("typeBean");
        if (StringUtils.isBlank(getProperty(SEARCH_CASE_TYPE_PROPERTY)))
        {
          typeSelectItems = typeBean.getSelectItems(
            DictionaryConstants.CASE_TYPE, 
            filter.getCaseFilter().getCaseTypeId());
        }
        else
          typeSelectItems = typeBean.getAllSelectItems(
            getProperty(SEARCH_CASE_TYPE_PROPERTY), 
            CaseConstants.CASE_ADMIN_ROLE,
            new String[]{DictionaryConstants.READ_ACTION}, false);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  }
  
  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(getPersonId());
  }  

  //Rendering
  public boolean isRenderCaseId()
  {
    return render(RENDER_CASEID_PROPERTY);
  }

  public boolean isRenderTitle()
  {
    return render(RENDER_TITLE_PROPERTY);
  }

  public boolean isRenderDescription()
  {
    return render(RENDER_DESCRIPTION_PROPERTY);
  }

  public boolean isRenderDate()
  {
    return render(RENDER_DATE_PROPERTY);
  }

  public boolean isRenderClassId()
  {
    return render(RENDER_CLASSID_PROPERTY);
  }
  
  public boolean isRenderPersonId()
  {
    boolean render = false;
    String typeId = getCurrentTypeId();
    if (!StringUtils.isBlank(typeId))
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {        
        PropertyDefinition pd = 
          type.getPropertyDefinition(PERSON_SEARCH_ENABLED);
        if (pd != null && !pd.getValue().isEmpty() && 
          pd.getValue().get(0).equalsIgnoreCase("true"))
        {
          render = render(RENDER_PERSONID_PROPERTY);
        }
      }
    }
    if (!render)
      filter.setPersonId(null);
    return render;
  }  

  public boolean isRenderSearchExpression()
  {
    return render(RENDER_SEARCH_EXPRESSION_PROPERTY);
  }

  public boolean isRenderDynamicForm()
  {
    return render(RENDER_DYNAMIC_FORM_PROPERTY) && getSelector() != null;
  }
  
  public boolean isRenderPropertyValueFilter()
  {
    return render(RENDER_PROPERTY_VALUE_FILTER, false);
  }
  
  public boolean isRenderCaseType()
  {
    return render(RENDER_TYPE_PROPERTY, true) &&
      getTypeSelectItems() != null;
  }

  public boolean isRenderFilterPanel()
  {
    return render(RENDER_FILTER_PANEL);
  }

  public boolean isRenderCollapsiblePanel()
  {
    return isRenderCaseId() || isRenderCaseType() || isRenderClassId() ||
      isRenderDate() || isRenderDescription() || isRenderSearchExpression() ||
      isRenderTitle();
  }

  public boolean isRenderClearButton()
  {
    return render(RENDER_CLEAR_BUTTON, false);
  }

  public String getTypeDescription()
  {
    Case row = (Case)getExternalContext().getRequestMap().get("row");
    if (row != null)
    {
      String rowDocTypeId = row.getCaseTypeId();

      TypeCache typeCache = TypeCache.getInstance();
      Type type = typeCache.getType(rowDocTypeId);

      if (type != null)
        return type.getDescription();
      else
        return rowDocTypeId;
    }
    else
      return "";
  }

  @Override
  public String getRowStyleClass()
  {
    String defaultStyleClass = null;
    Case row = (Case)getValue("#{row}");
    if (row != null && row.getCaseId().equals(getObjectId()))
      defaultStyleClass = "selectedRow";

    return getRowStyleClass(defaultStyleClass);
  }
  
  private boolean hasFirstLoadProperty()
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    String firstLoad = cursor.getProperty(FIRSTLOAD_PROPERTY);
    return "true".equals(firstLoad);
  }
  
  @Override
  public String getSearchPropertyName()
  {
    return SEARCH_CASE_PROPERTY_NAME;    
  }
  
  @Override
  public String getSearchPropertyValue()
  {
    return SEARCH_CASE_PROPERTY_VALUE;
  }
  
  public Translator getTranslator()
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    String translate = cursor.getProperty(ENABLE_TRANSLATION_PROPERTY);
    if ("true".equals(translate))
    {
      return UserSessionBean.getCurrentInstance().getTranslator();
    }
    return null;
  }
  
  @Override
  public String getAdminRole()
  {
    return CaseConstants.CASE_ADMIN_ROLE;
  }
  
  public String getResultsLayout()
  {
    String layout = getProperty(RESULTS_LAYOUT_PROPERTY);
    if (layout != null)
      return layout;
    else
      return RESULTS_LAYOUT_TABLE;
  }

  //keywords
  private int callCountResults(CaseFormFilter filter) throws Exception
  {
    CaseFilter caseFilter = filter.getCaseFilter();
    int result = 0;
    
    if (keywordsManager != null)
    {
      List<Property> joinKeywords = keywordsManager.getJointKeywords();
      caseFilter.getProperty().addAll(joinKeywords);
      result = CaseConfigBean.getPort().countCases(caseFilter);
      if (result == 0)
      {
        caseFilter.getProperty().removeAll(joinKeywords);
        Property disjointKeywords = keywordsManager.getDisjointKeywords();
        caseFilter.getProperty().add(disjointKeywords);
        result = CaseConfigBean.getPort().countCases(caseFilter);
      }
    }
    else
      result = CaseConfigBean.getPort().countCases(caseFilter);

    return result;
  }

  private List callFindResults(CaseFormFilter filter) throws Exception
  {
    List<Case> result = new ArrayList();

    CaseFilter caseFilter = filter.getCaseFilter();
    if (keywordsManager != null)
    {
      List<Property> joinKeywords = keywordsManager.getJointKeywords();
        caseFilter.getProperty().addAll(joinKeywords);
        result = CaseConfigBean.getPort().findCases(caseFilter);
      if (result == null || result.isEmpty())
      {
        caseFilter.getProperty().removeAll(joinKeywords);
        Property disjointKeywords = keywordsManager.getDisjointKeywords();
        caseFilter.getProperty().add(disjointKeywords);
        result.addAll(CaseConfigBean.getPort().findCases(caseFilter));
      }
    }
    else
      result = CaseConfigBean.getPort().findCases(caseFilter);

    boolean loadMetadata = false;
    //Looking for 'loadMetadata' property
    loadMetadata = (getProperty(LOAD_METADATA_PROPERTY) != null
      && getProperty(LOAD_METADATA_PROPERTY).equalsIgnoreCase("true"));
    
    //Looking for 'property' columnName
    if (!loadMetadata)
    {
      List<String> columnNames = getColumnNames();
      if (columnNames != null && !columnNames.isEmpty())
      {
        for (String column : columnNames)
        {
          if (!loadMetadata)
            loadMetadata = column.contains("property[") && column.contains("]");
        }
      }
    }
    if (loadMetadata)
    {
      for (int i = 0; i < result.size(); i++)
      {
        Case cas = CaseConfigBean.getPort().loadCase(result.get(i).getCaseId());
        result.set(i, cas);
      }
    }

    if (keywordsManager != null && !filter.isOrderBySet())
      keywordsManager.sortResults(result);

    return result;
  }

  /*
   * Checks if the Case satisfy the filter type and filter search properties.
   * Set objectId to prevent non prefix 
   */
  @Override
  public void checkJumpSuitability(JumpData jumpData)
  {   
    CaseFormFilter formFilter = new CaseFormFilter();
    setSearchDynamicProperties(formFilter);
    CaseFilter caseFilter = formFilter.getCaseFilter();
    caseFilter.getCaseId().add(jumpData.getObjectId());
    String caseTypeId = getProperty(SEARCH_CASE_TYPE_PROPERTY);
    if (caseTypeId != null)
      caseFilter.setCaseTypeId(caseTypeId);

    try
    {
      List<Case> cases = CaseConfigBean.getPort().findCases(caseFilter);
      if (cases != null && !cases.isEmpty())
      {
        jumpData.setSuitable(true);
        Case cas = cases.get(0);
        if (cas != null)
          jumpData.setObjectId(cas.getCaseId());
      }
      else
        jumpData.setSuitable(false);
    }
    catch (Exception ex)
    {
      jumpData.setSuitable(false);
    }
  }
  
  @Override
  public String getNotSuitableMessage()
  {
    return "INVALID_CASE";
  }  

  private boolean isOrderPreserved(String title, List<String> keywords)
  {
    int lastIndexOf = 0;
    for (String keyword : keywords)
    {
      int indexOf = title.indexOf(keyword);
      if (indexOf >= 0 && indexOf < lastIndexOf)
        return false;
      lastIndexOf = indexOf;
    }

    return true;
  }

  private void configureColumns()
  {
    resultsManager.getDefaultColumnNames().clear();

    //Default "caseId", "caseTypeId", "title", "actions"
    ColumnDefinition caseIdColDef = new ColumnDefinition("caseId");
    caseIdColDef.setStyle("width:10%");
    resultsManager.addDefaultColumn(caseIdColDef);

    ColumnDefinition caseTypeIdColDef = new ColumnDefinition("caseTypeId");
    caseTypeIdColDef.setConverter(new TypeIdConverter());
    caseTypeIdColDef.setStyle("width:20%");
    resultsManager.addDefaultColumn(caseTypeIdColDef);

    ColumnDefinition titleColDef = new ColumnDefinition("title");
    titleColDef.setStyle("width:50%");
    resultsManager.addDefaultColumn(titleColDef);

    ColumnDefinition actionsColDef = new ColumnDefinition("actions");
    actionsColDef.setType(ColumnDefinition.CUSTOM_TYPE);
    actionsColDef.setAlias("");
    actionsColDef.setStyleClass("actionsColumn");
    actionsColDef.setStyle("width:20%");
    resultsManager.addDefaultColumn(actionsColDef);
  }

  private Set<String> getFormWildcardProperties()
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    return new HashSet(cursor.getMultiValuedProperty(FORM_WILDCARD_PROPERTIES));
  }
  
  private List<Property> addWildcards(List<Property> propertyList)
  {
    Set<String> formWildcardProperties = getFormWildcardProperties();
    if (formWildcardProperties.isEmpty())
    {
      return propertyList;
    }
    else
    {
      List<Property> result = new ArrayList();    
      if (propertyList != null)
      {
        for (Property property : propertyList)
        {      
          Property auxProperty = new Property();
          auxProperty.setName(property.getName());
          for (String value : property.getValue())
          {
            if (formWildcardProperties.contains(property.getName()))
            {                        
              auxProperty.getValue().add("low(" + 
                FilterUtils.addWildcards(normalize(value)) + ")");
            }
            else
            {
              auxProperty.getValue().add(value);
            }
          }
          result.add(auxProperty);
        }
      }
      return result;
    }
  }
  
  private String normalize(String text)
  {
    char[] cArray = text.toCharArray();
    for (int i = 0; i < cArray.length; i++)
    {
      char c = cArray[i];
      if ((c == 'à') || (c == 'ä') || (c == 'á')) cArray[i] = 'a';
      else if ((c == 'è') || (c == 'ë') || (c == 'é')) cArray[i] = 'e';
      else if ((c == 'ì') || (c == 'ï') || (c == 'í')) cArray[i] = 'i'; 
      else if ((c == 'ò') || (c == 'ö') || (c == 'ó')) cArray[i] = 'o'; 
      else if ((c == 'ù') || (c == 'ü') || (c == 'ú')) cArray[i] = 'u';     
      else if ((c == 'À') || (c == 'Ä') || (c == 'Á')) cArray[i] = 'A'; 
      else if ((c == 'È') || (c == 'Ë') || (c == 'É')) cArray[i] = 'E';
      else if ((c == 'Ì') || (c == 'Ï') || (c == 'Í')) cArray[i] = 'I'; 
      else if ((c == 'Ò') || (c == 'Ö') || (c == 'Ó')) cArray[i] = 'O'; 
      else if ((c == 'Ù') || (c == 'Ü') || (c == 'Ú')) cArray[i] = 'U';     
    }
    return new String(cArray);
  }  

  @Override
  public DetailBean getDetailBean()
  {
    return new CaseDetailBean();
  }
}
