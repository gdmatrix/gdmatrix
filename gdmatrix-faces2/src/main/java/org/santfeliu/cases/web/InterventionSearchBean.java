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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.faces.convert.TypeIdConverter;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.DynamicTypifiedSearchBean;
import org.santfeliu.web.obj.util.ColumnDefinition;
import org.santfeliu.web.obj.util.SetObjectManager;
import org.santfeliu.web.obj.util.JumpManager;

/**
 *
 * @author blanquepa
 */
@CMSManagedBean
public class InterventionSearchBean extends DynamicTypifiedSearchBean
{
  @CMSProperty
  public static final String SEARCH_TYPE_PROPERTY = "searchType";
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
  public static final String RENDER_INTID_PROPERTY = "renderIntId";
  @CMSProperty
  public static final String RENDER_CASEID_PROPERTY = "renderCaseId";
  @CMSProperty
  public static final String RENDER_COMMENTS_PROPERTY = "renderComments";
  @CMSProperty
  public static final String RENDER_DATE_PROPERTY = "renderDate";
  @CMSProperty
  public static final String RENDER_SEARCH_EXPRESSION_PROPERTY = "renderSearchExpression";
  @CMSProperty
  public static final String RENDER_TYPE_PROPERTY = "renderType";
  @CMSProperty
  public static final String RENDER_PERSON_PROPERTY = "renderPerson";  
  @CMSProperty
  public static final String RENDER_DYNAMIC_FORM_PROPERTY = "renderDynamicForm";
  @CMSProperty
  public static final String RENDER_FILTER_PANEL = "renderFilterPanel";
  @CMSProperty
  public static final String RENDER_PROPERTY_VALUE_FILTER = "renderPropertyValueFilter";
  @CMSProperty
  public static final String RENDER_CLEAR_BUTTON = "renderClearButton";
  @CMSProperty
  public static final String SHOW_MODE = "showMode";
  @CMSProperty
  public static final String INTERVENTION_TAB_MID = "interventionTabMid";
  
  public static final String DOC_SERVLET_URL = "/documents/";

  private transient List<SelectItem> typeSelectItems;
  private transient List<SelectItem> personSelectItems;
  private transient List<SelectItem> caseSelectItems;

  private String headerBrowserUrl;
  private String footerBrowserUrl;

  private SetObjectManager setObjectManager;
  private InterventionFormFilter filter;
  

  public InterventionSearchBean()
  {
    super("org.santfeliu.cases.web.resources.CaseBundle", "caseInterventions_", 
      "intTypeId");
//    parametersManager = new ParametersManager();
    jumpManager = new JumpManager(this, "caseid", "intid", 
      DictionaryConstants.INTERVENTION_TYPE);
    
    setObjectManager = new SetObjectManager(this);
    
    typeSelectItems = null;
    personSelectItems = null;
    caseSelectItems = null;
    filter = new InterventionFormFilter();  
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

  public InterventionFormFilter getFilter()
  {
    return filter;
  }

  public void setFilter(InterventionFormFilter filter)
  {
    this.filter = filter;
  }
  
//Actions  

  @Override
  public int countResults()
  {
    try
    {
      filter.clearLists();

      //apply node filters
      String intTypeId = getProperty(SEARCH_TYPE_PROPERTY);
      if (intTypeId != null && (!isRenderIntType() || StringUtils.isBlank(getCurrentTypeId())))
        setCurrentTypeId(intTypeId);

      setSearchDynamicProperties(filter);

      //apply dynamic properties filters
      List<Property> properties = getFormDataAsProperties();

      filter.setFormProperties(properties);
      //apply input properties
      filter.setInputProperties(properties);
      //set additional values
      filter.setIntTypeId(getCurrentTypeId());

      if (!filter.isEmpty())
        return CaseConfigBean.getPort().countInterventions(filter.getInterventionFilter());
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
      String intTypeId = getProperty(SEARCH_TYPE_PROPERTY);
      if (intTypeId != null && (!isRenderIntType() || StringUtils.isBlank(getCurrentTypeId())))
        setCurrentTypeId(intTypeId);

      setSearchDynamicProperties(filter);

      //apply dynamic form properties
      List<Property> properties = getFormDataAsProperties();
      filter.setFormProperties(properties);
      //apply filter lists (set from xInput properties)
      filter.setInputProperties(properties);
      //set additional values
      filter.setIntTypeId(getCurrentTypeId());
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      if (!filter.isEmpty())
      {
        List<InterventionView> rows = 
          CaseConfigBean.getPort().findInterventionViews(filter.getInterventionFilter());
        if (rows != null && !rows.isEmpty())
        {
          HashMap<String, List<InterventionView>> caseIds = new HashMap();
          for (InterventionView row :rows)
          {
            List<InterventionView> views = caseIds.get(row.getCaseId());
            if (views == null)
              views = new ArrayList();
            views.add(row);            
            caseIds.put(row.getCaseId(), views);
          }
          CaseFilter caseFilter = new CaseFilter();
          caseFilter.getCaseId().addAll(caseIds.keySet());
          List<Case> cases = CaseConfigBean.getPort().findCases(caseFilter);
          if (cases != null && !cases.isEmpty())
          {
            for (Case cas : cases)
            {
              List<InterventionView> views = caseIds.get(cas.getCaseId());
              for (InterventionView view : views)
              {
                DictionaryUtils.setProperty(view, "title", cas.getTitle());
              }
            }
          }
        }
        return rows;
      }
      else
        error("FILTER_IS_EMPTY");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String show()
  {
    setHeaderBrowserUrl(null);
    setFooterBrowserUrl(null);

    String outcome = executeParametersManagers(jumpManager, 
      setObjectManager, "INVALID_INTERVENTION");
    if (outcome != null)
        return outcome;

    configureColumns();

    String intTypeId = getProperty(SEARCH_TYPE_PROPERTY);
    if (intTypeId != null)
      setCurrentTypeId(intTypeId);

    if (hasFirstLoadProperty())
      search();

    return "intervention_search";
  }
  
  public String showIntervention()
  {
    InterventionView intView = (InterventionView)getValue("#{row}");
    String caseId = intView.getCaseId();
    String intTabMid = getProperty(INTERVENTION_TAB_MID);

    String outcome = getControllerBean().show(intTabMid, caseId);
    
    CaseInterventionsBean caseInterventionsBean = 
      (CaseInterventionsBean)getBean("caseInterventionsBean");
    caseInterventionsBean.editIntervention();

    return outcome;
  }
  
  @Override
  public String showObject(String typeId, String objectId)
  {
    String intTabMid = getProperty(INTERVENTION_TAB_MID);
    String outcome = getControllerBean().show(intTabMid, objectId);
    
    CaseInterventionsBean caseInterventionsBean = 
      (CaseInterventionsBean)getBean("caseInterventionsBean");
    caseInterventionsBean.editIntervention(jumpManager.getTabObjectId());
    
    return outcome;
  }
  
  public String selectIntervention()
  {
    InterventionView row = (InterventionView)getExternalContext().getRequestMap().get("row");
    String intId = row.getIntId();
    return getControllerBean().select(intId);
  }  
  
  public String searchType()
  {
    return searchType(DictionaryConstants.INTERVENTION_TYPE,
      "#{interventionSearchBean.currentTypeId}");
  }  
  
  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{interventionSearchBean.selectedPerson}");
  }  
  
  public String searchCase()
  {
    return getControllerBean().searchObject("Case",
      "#{interventionSearchBean.selectedCase}");
  }  
  
  @Override
  public void setCurrentTypeId(String typeId)
  {
    this.typeSelectItems = null;
    super.setCurrentTypeId(typeId);
  } 

  public void setSelectedPerson(String personId)
  {
    this.personSelectItems = null;
    filter.setPersonId(personId);
  }
  
  public void setSelectedCase(String caseId)  
  {
    this.caseSelectItems = null;
    filter.setCaseId(caseId);
  }
  
  public boolean isRenderIntType()
  {
    return render(RENDER_TYPE_PROPERTY, true) &&
      getTypeSelectItems() != null;
  }  
  
  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      if (typeSelectItems == null)
      {
        TypeBean typeBean = (TypeBean)getBean("typeBean");
        if (StringUtils.isBlank(getProperty(SEARCH_TYPE_PROPERTY)))
        {
          typeSelectItems = typeBean.getSelectItems(
            DictionaryConstants.INTERVENTION_TYPE, filter.getInterventionFilter().getIntTypeId());
        }
        else
        {
          typeSelectItems = new ArrayList();
          typeSelectItems.add(new SelectItem(""," "));
          typeSelectItems.addAll(typeBean.getAllSelectItems(
            getProperty(SEARCH_TYPE_PROPERTY), CaseConstants.CASE_ADMIN_ROLE,
            new String[]{DictionaryConstants.READ_ACTION}, false));
        }
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
    try
    {
      if (personSelectItems == null)
      {
        PersonBean personBean = (PersonBean)getBean("personBean");
        personSelectItems = personBean.getSelectItems(filter.getPersonId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return personSelectItems;
  }

  public void setPersonSelectItems(List<SelectItem> personSelectItems)
  {
    this.personSelectItems = personSelectItems;
  }  
  
  public List<SelectItem> getCaseSelectItems()
  {
    try
    {
      if (caseSelectItems == null)
      {
        CaseBean caseBean = (CaseBean)getBean("caseBean");
        caseSelectItems = caseBean.getSelectItems(filter.getInterventionFilter().getCaseId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return caseSelectItems;
  }

  public void setCaseSelectItems(List<SelectItem> caseSelectItems)
  {
    this.caseSelectItems = caseSelectItems;
  }  
  
  public String getIntId()
  {
    return filter.getIntId();
  }

  public void setIntId(String intId)
  {
    filter.setIntId(intId);
  }
  
  public String getCaseId()
  {
    return filter.getInterventionFilter().getCaseId();
  }

  public void setCaseId(String caseId)
  {
    filter.getInterventionFilter().setCaseId(caseId);
  }
  
  public String getComments()
  {
    return filter.getComments();
  }
  
  public void setComments(String comments)
  {
    filter.setComments(comments);
  }
  
  public String getPersonId()
  {
    return filter.getPersonId();
  }
  
  public void setPersonId(String personId)
  {
    filter.setPersonId(personId);
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
    return filter.getInterventionFilter().getDateComparator();
  }

  public void setDateComparator(String value)
  {
    filter.getInterventionFilter().setDateComparator(value);
  }

  public String getFromDate()
  {
    return filter.getInterventionFilter().getFromDate();
  }

  public void setFromDate(String value)
  {
    filter.getInterventionFilter().setFromDate(value);
  }

  public String getToDate()
  {
    return filter.getInterventionFilter().getToDate();
  }

  public void setToDate(String value)
  {
    filter.getInterventionFilter().setToDate(value);
  }  
  
//Rendering
  public boolean isRenderIntId()
  {
    return render(RENDER_INTID_PROPERTY);
  }
  
  public boolean isRenderCaseId()
  {
    return render(RENDER_CASEID_PROPERTY);
  }

  public boolean isRenderComments()
  {
    return render(RENDER_COMMENTS_PROPERTY);
  }

  public boolean isRenderDate()
  {
    return render(RENDER_DATE_PROPERTY);
  }
  
  public boolean isRenderPerson()
  {
    return render(RENDER_PERSON_PROPERTY);
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
    return isRenderCaseId() || isRenderCaseType() || 
      isRenderDate() || isRenderSearchExpression() ||
      isRenderComments();
  }

  public boolean isRenderClearButton()
  {
    return render(RENDER_CLEAR_BUTTON, false);
  }  
  
  @Override
  public String getRowStyleClass()
  {
    String defaultStyleClass = null;
    InterventionView row = (InterventionView)getValue("#{row}");
    if (row != null && row.getIntId().equals(getObjectId()))
      defaultStyleClass = "selectedRow";

    return getRowStyleClass(defaultStyleClass);
  }  
  
  /*
   * Checks if the Case satisfy the filter type and filter search properties.
   */
  @Override
  protected boolean checkSuitability(String intId)
  {
    InterventionFormFilter formFilter = new InterventionFormFilter();
    setSearchDynamicProperties(formFilter);
    
    InterventionFilter intFilter = formFilter.getInterventionFilter();
    intFilter.getIntId().add(intId);
    String intTypeId = getProperty(SEARCH_TYPE_PROPERTY);
    if (intTypeId != null)
      intFilter.setIntTypeId(intTypeId);

    try
    {
      int counter =
        CaseConfigBean.getPort().countInterventions(intFilter);
      return (counter > 0);
    }
    catch (Exception ex)
    {
      return false;
    }
  }  
  
  private void configureColumns()
  {
    resultsManager.getDefaultColumnNames().clear();

    //Default "caseId", "caseTypeId", "title", "actions"
    ColumnDefinition intIdColDef = new ColumnDefinition("intId");
    intIdColDef.setStyle("width:10%");
    resultsManager.addDefaultColumn(intIdColDef);

    ColumnDefinition intTypeIdColDef = new ColumnDefinition("intTypeId");
    intTypeIdColDef.setConverter(new TypeIdConverter());
    intTypeIdColDef.setStyle("width:20%");
    resultsManager.addDefaultColumn(intTypeIdColDef);

    ColumnDefinition commentsColDef = new ColumnDefinition("comments");
    commentsColDef.setStyle("width:50%");
    resultsManager.addDefaultColumn(commentsColDef);

    ColumnDefinition actionsColDef = new ColumnDefinition("actions");
    actionsColDef.setType(ColumnDefinition.CUSTOM_TYPE);
    actionsColDef.setAlias("");
    actionsColDef.setStyleClass("actionsColumn");
    actionsColDef.setStyle("width:20%");
    resultsManager.addDefaultColumn(actionsColDef);
  }  
  
  private boolean hasFirstLoadProperty()
  {
    MenuItemCursor cursor = getSelectedMenuItem();
    String firstLoad = cursor.getProperty(FIRSTLOAD_PROPERTY);
    return "true".equals(firstLoad);
  }  

  @Override
  public String getAdminRole()
  {
    return CaseConstants.CASE_ADMIN_ROLE;
  }
  
 
}
