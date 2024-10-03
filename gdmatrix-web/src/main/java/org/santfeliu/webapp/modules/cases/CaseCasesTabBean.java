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
package org.santfeliu.webapp.modules.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.helpers.TypeSelectHelper;
import org.santfeliu.webapp.modules.kernel.PersonTypeBean;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRowComparator;
import org.santfeliu.webapp.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.webapp.util.RowStyleClassGenerator;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class CaseCasesTabBean extends TabBean
{
  private static final String TYPEID1_PROPERTY = "typeId1";
  private static final String TYPEID2_PROPERTY = "typeId2";

  private static final String SOURCE_TYPEID = "_sourceTypeId";
  private static final String TARGET_TYPEID = "_targetTypeId";

  Map<String, TabInstance> tabInstances = new HashMap<>();
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private GroupableRowsHelper groupableRowsHelper;  
  
  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseCasesDataTableRow> rows;
    int firstRow = 0;
    boolean relatedByPerson = false;
    TypeSelectHelper typeSelectHelper = new TypeSelectHelper<CaseCasesDataTableRow>()
    {
      @Override
      public List<CaseCasesDataTableRow> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return CaseCasesTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public String getBaseTypeId()
      {
        return CaseCasesTabBean.this.getTabBaseTypeId();        
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }      

      @Override
      public String getRowTypeId(CaseCasesDataTableRow row)
      {
        return row.getTypeId();        
      }
    };
    
    public TypeSelectHelper getTypeSelectHelper()
    {
      return typeSelectHelper;
    }
  }    

  private CaseCase editing;
  private String formSelector;

  @Inject
  CaseObjectBean caseObjectBean;

  @Inject
  CaseTypeBean caseTypeBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return CaseCasesTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return CaseCasesTabBean.this.getColumns();
      }

      @Override
      public void sortRows()
      {
        if (getOrderBy() != null)
        {        
          Collections.sort(getCurrentTabInstance().rows, 
            new DataTableRowComparator(getColumns(), getOrderBy()));            
        }
      }

      @Override
      public String getRowTypeColumnName()
      {
        return "caseCaseTypeId";
      }

      @Override
      public String getFixedColumnValue(Object row, String columnName)
      {
        return null; //No fixed columns
      }      
    };
  }  

  public GroupableRowsHelper getGroupableRowsHelper()
  {
    return groupableRowsHelper;
  }

  public void setGroupableRowsHelper(GroupableRowsHelper groupableRowsHelper)
  {
    this.groupableRowsHelper = groupableRowsHelper;
  }  

  public TabInstance getCurrentTabInstance()
  {
    EditTab tab = caseObjectBean.getActiveEditTab();
    if (WebUtils.getBeanName(this).equals(tab.getBeanName()))
    {
      TabInstance tabInstance = tabInstances.get(tab.getSubviewId());
      if (tabInstance == null)
      {
        tabInstance = new TabInstance();
        tabInstances.put(tab.getSubviewId(), tabInstance);
      }
      return tabInstance;
    }
    else
      return EMPTY_TAB_INSTANCE;
  }

  public Map<String, TabInstance> getTabInstances()
  {
    return tabInstances;
  }  
  
  @Override
  public String getObjectId()
  {
    return getCurrentTabInstance().objectId;
  }

  @Override
  public void setObjectId(String objectId)
  {
    getCurrentTabInstance().objectId = objectId;
  }

  @Override
  public boolean isNew()
  {
    return NEW_OBJECT_ID.equals(getCurrentTabInstance().objectId);
  }

  public List<CaseCasesDataTableRow> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseCasesDataTableRow> rows)
  {
    getCurrentTabInstance().rows = rows;
  }

  public boolean isRelatedByPerson()
  {
    return getCurrentTabInstance().relatedByPerson;
  }

  public List<String> getOrderBy()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getOrderBy();
    else
      return Collections.EMPTY_LIST;
  }  
  
  public List<TableProperty> getTableProperties()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getTableProperties();
    else
      return Collections.EMPTY_LIST;
  }
  
  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
  }
  
  public CaseCase getEditing()
  {
    return editing;
  }

  public void setEditing(CaseCase editing)
  {
    this.editing = editing;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }

  public void setCaseCaseTypeId(String caseCaseTypeId)
  {
    if (editing != null)
      editing.setCaseCaseTypeId(caseCaseTypeId);
  }

  public String getCaseCaseTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCaseCaseTypeId();
  }

  public void setRefCaseId(String caseId)
  {
    if (editing != null)
    {
      if (isDirectRelation())
        editing.setRelCaseId(caseId);
      else
        editing.setCaseId(caseId);
    }
  }

  public String getRefCaseId()
  {
    if (editing == null) return NEW_OBJECT_ID;
    else
    {
      if (isDirectRelation())
        return editing.getRelCaseId();
      else
        return editing.getCaseId();
    }
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public void create()
  {
    executeTabAction("preTabEdit", null);    
    editing = new CaseCase();
    editing.setCaseCaseTypeId(getCreationTypeId());
    if (isDirectRelation())
    {
      editing.setCaseId(getObjectId());
    }
    else
    {
      editing.setRelCaseId(getObjectId());
    }
    formSelector = null;
    executeTabAction("postTabEdit", null);    
  }

  public String getCaseDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return caseTypeBean.getDescription(getRefCaseId());
    }
    return "";
  }

  public String getRefCaseTypeId()
  {
    String typeIdPropertyName =
      isDirectRelation() ? TARGET_TYPEID : SOURCE_TYPEID;
    TypeCache typeCache = TypeCache.getInstance();
    Type selectedType = typeCache.getType(getCaseCaseTypeId());
    if (selectedType != null)
    {
      PropertyDefinition pd =
        selectedType.getPropertyDefinition(typeIdPropertyName);
      if (pd != null) return pd.getValue().get(0);
    }
    return null;
  }

  @Override
  public void load() throws Exception
  {
    executeTabAction("preTabLoad", null);    
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        EditTab editTab = caseObjectBean.getActiveEditTab();
        if (editTab != null)
        {
          String typeId1 =
            editTab.getProperties().getString(TYPEID1_PROPERTY);
          String typeId2 =
            editTab.getProperties().getString(TYPEID2_PROPERTY);

          if (typeId1 != null || typeId2 != null)
          {
            TabInstance tabInstance = getCurrentTabInstance();
            tabInstance.rows = getResultsByPersons(typeId1, typeId2);
            tabInstance.relatedByPerson = true;
          }
          else
          {             
            String typeId = editTab.getBaseTypeId();
            typeId = editTab.isShowAllTypes() ? 
              DictionaryConstants.CASE_CASE_TYPE : typeId;
            getCurrentTabInstance().rows = getResultsByDefault(typeId);
          }
          getCurrentTabInstance().typeSelectHelper.load();
          executeTabAction("postTabLoad", null); 
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      TabInstance tabInstance = getCurrentTabInstance();
      tabInstance.objectId = NEW_OBJECT_ID;
      tabInstance.rows = Collections.EMPTY_LIST;
      getCurrentTabInstance().typeSelectHelper.load();      
      tabInstance.firstRow = 0;
    }    
  }

  public void edit(DataTableRow row)
  {
    String caseCaseId = null;
    if (row != null)
      caseCaseId = row.getRowId();

    try
    {
      if (caseCaseId != null)
      {
        executeTabAction("preTabEdit", row);
        editing = CasesModuleBean.getPort(false).loadCaseCase(caseCaseId);
        executeTabAction("postTabEdit", row);
      }
      else
      {
        create();
      }
      formSelector = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public void store() throws Exception
  {
    try
    {
      if (editing != null)
      {
        String refCaseId = getRefCaseId();
        if (refCaseId == null || refCaseId.isEmpty())
          throw new Exception("CASE_MUST_BE_SELECTED");
        editing = (CaseCase) executeTabAction("preTabStore", editing);
        editing = CasesModuleBean.getPort(false).storeCaseCase(editing);
        executeTabAction("postTabStore", editing);        
        refreshHiddenTabInstances();
        load();
        editing = null;
        growl("STORE_OBJECT");
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }  
  
  public void remove(DataTableRow row)
  {
    try
    {
      if (row != null)
      {
        row = (DataTableRow) executeTabAction("preTabRemove", row);        
        String caseCaseId = row.getRowId();      
        CasesModuleBean.getPort(false).removeCaseCase(caseCaseId);
        executeTabAction("postTabRemove", row);        
        refreshHiddenTabInstances();
        load();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
    
  @Override
  public void clear()
  {
    tabInstances.clear();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CaseCase)stateArray[0];
      formSelector = (String)stateArray[1];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(CaseCase caseCase)
  {
    return (caseCase != null && caseCase.getCaseCaseId() == null);
  }

  private List<CaseCasesDataTableRow> getResultsByDefault(String caseCaseTypeId)
    throws Exception
  {
    CaseCaseFilter filter = new CaseCaseFilter();
    filter.setCaseId(getObjectId());
    filter.setCaseCaseTypeId(caseCaseTypeId);
    List<CaseCaseView> results
      = CasesModuleBean.getPort(false).findCaseCaseViews(filter);

    //Reverse cases
    filter = new CaseCaseFilter();
    filter.setRelCaseId(getObjectId());
    filter.setCaseCaseTypeId(caseCaseTypeId);
    List<CaseCaseView> revResults
      = CasesModuleBean.getPort(false).findCaseCaseViews(filter);

    if (revResults != null && !revResults.isEmpty())
    {
      int maxResults = results.size() + revResults.size();
      int revCount = revResults.size();

      for (int i = 0; i < revCount && results.size() <= maxResults; i++)
      {
        results.add(revResults.get(i));
      }
    }
    
    List<CaseCasesDataTableRow> auxList = toDataTableRows(results);
    if (getOrderBy() != null)
    {            
      Collections.sort(auxList, 
        new DataTableRowComparator(getColumns(), getOrderBy()));
    }
    return auxList;
  }

  private List<CaseCasesDataTableRow> getResultsByPersons(String cpTypeId1,
    String cpTypeId2) throws Exception
  {
    CaseCaseMatcher matcher = new CaseCaseMatcher(caseObjectBean.getCase());    
    List<CaseCasesDataTableRow> auxList = 
      toDataTableRows(matcher.matchByPersons(cpTypeId1, cpTypeId2));
    if (getOrderBy() != null)
    {            
      Collections.sort(auxList, 
        new DataTableRowComparator(getColumns(), getOrderBy()));
    }
    return auxList;
  }

  private List<CaseCasesDataTableRow> toDataTableRows(List<CaseCaseView>
    caseCaseViews) throws Exception
  {
    List<CaseCasesDataTableRow> convertedRows = new ArrayList<>();
    for (CaseCaseView row : caseCaseViews)
    {
      CaseCasesDataTableRow dataTableRow = new CaseCasesDataTableRow(row);
      dataTableRow.setValues(this, row, getTableProperties());
      dataTableRow.setStyleClass(getRowStyleClass(row));
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
  }

  private void refreshHiddenTabInstances()
  {
    for (TabInstance tabInstance : tabInstances.values())
    {
      if (tabInstance != getCurrentTabInstance())
      {
        tabInstance.objectId = NEW_OBJECT_ID;
      }
    }
  }

  private boolean isDirectRelation()
  {
    String caseCaseTypeId = editing.getCaseCaseTypeId();
    if (caseCaseTypeId != null)
    {
      TypeCache typeCache = TypeCache.getInstance();
      Type caseCaseType = typeCache.getType(caseCaseTypeId);
      if (caseCaseType != null)
      {
        Type caseType =
          typeCache.getType(caseObjectBean.getCase().getCaseTypeId());
        PropertyDefinition pd =
          caseCaseType.getPropertyDefinition(SOURCE_TYPEID);
        if (pd != null)
        {
          String sourceTypeId = pd.getValue().get(0);
          return caseType.isDerivedFrom(sourceTypeId);
        }
      }
    }
    return true;
  }
  
  private RowStyleClassGenerator getRowStyleClassGenerator()
  {
    return new DateTimeRowStyleClassGenerator("startDate", "endDate", null);
  }
  
  private String getRowStyleClass(Object row)
  {
    RowStyleClassGenerator styleClassGenerator = 
      getRowStyleClassGenerator();
    return styleClassGenerator.getStyleClass(row);    
  }  
  
  public class CaseCasesDataTableRow extends DataTableRow
  {
    private boolean reverseRelation;
    private String caseId;
    private String caseTypeId;
    private String caseTitle;
    private String caseIniDateTime;
    private String caseEndDateTime;
    private String personId; //If is related by person

    public CaseCasesDataTableRow(CaseCaseView row)
    {
      super(row.getCaseCaseId(), row.getCaseCaseTypeId());

      Case mainCase = row.getMainCase();
      Case relCase = row.getRelCase();

      String objectId = getObjectId();
      reverseRelation = objectId.equals(relCase.getCaseId())
        && !objectId.equals(mainCase.getCaseId());

      Case cas = reverseRelation ? mainCase : relCase;
      caseId = cas.getCaseId();
      caseTypeId = cas.getCaseTypeId();
      caseTitle = cas.getTitle();
      if (cas.getStartDate() != null)
      {
        caseIniDateTime = cas.getStartDate() + 
          (cas.getStartTime() != null ? cas.getStartTime() : "000000");
      }
      if (cas.getEndDate() != null)
      {
        caseEndDateTime = cas.getEndDate() + 
          (cas.getEndTime() != null ? cas.getEndTime() : "000000");
      }
      personId =
        DictionaryUtils.getPropertyValue(row.getProperty(), "personId");
    }

    public boolean isReverseRelation()
    {
      return reverseRelation;
    }

    public String getCaseTypeId()
    {
      return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId)
    {
      this.caseTypeId = caseTypeId;
    }

    public String getCaseTitle()
    {
      return caseTitle;
    }

    public void setCaseTitle(String caseTitle)
    {
      this.caseTitle = caseTitle;
    }

    public String getCaseId()
    {
      return caseId;
    }

    public void setCaseId(String caseId)
    {
      this.caseId = caseId;
    }

    public String getCaseIniDateTime()
    {
      return caseIniDateTime;
    }

    public void setCaseIniDateTime(String caseIniDateTime)
    {
      this.caseIniDateTime = caseIniDateTime;
    }

    public String getCaseEndDateTime()
    {
      return caseEndDateTime;
    }

    public void setCaseEndDateTime(String caseEndDateTime)
    {
      this.caseEndDateTime = caseEndDateTime;
    }
    
    public String getCaseIniDate()
    {
      return (caseIniDateTime == null ? null : caseIniDateTime.substring(0, 8));      
    }
    
    public String getCaseEndDate()
    {
      return (caseEndDateTime == null ? null : caseEndDateTime.substring(0, 8));      
    }

    public String getPersonId()
    {
      return personId;
    }

    public String getPerson()
    {
      PersonTypeBean personTypeBean = WebUtils.getBean("personTypeBean");
      return personTypeBean.getDescription(personId);
    }

    @Override
    protected Value getDefaultValue(String columnName)
    {
      if (columnName != null)
      {
        switch (columnName)
        {
          case "caseId":
            return new NumericValue(getCaseId());
          case "caseTitle":
            return new DefaultValue(getCaseTitle());
          case "caseTypeId":
            return new TypeValue(getCaseTypeId());
          case "caseIniDate":
            return new DateValue(getCaseIniDate());
          case "caseIniDateTime":
            return new DateValue(getCaseIniDateTime());
          case "caseEndDate":
            return new DateValue(getCaseEndDate());
          case "caseEndDateTime":
            return new DateValue(getCaseEndDateTime());
          case "person":
            return new DefaultValue(getPerson());
          default:
            break;
        }
      }
      return super.getDefaultValue(columnName);
    }
  }

}