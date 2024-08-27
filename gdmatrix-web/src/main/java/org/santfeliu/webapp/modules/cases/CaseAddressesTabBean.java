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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.helpers.TypeSelectHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.kernel.AddressTypeBean;
import org.santfeliu.webapp.modules.kernel.KernelModuleBean;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.EditTab;
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
public class CaseAddressesTabBean extends TabBean
{
  private CaseAddress editing;
  Map<String, TabInstance> tabInstances = new HashMap();
  private boolean importPersons;
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private GroupableRowsHelper groupableRowsHelper;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CaseAddressesDataTableRow> rows;
    int firstRow = 0;
    TypeSelectHelper typeSelectHelper = 
      new TypeSelectHelper<CaseAddressesDataTableRow>()
    {
      @Override
      public List<CaseAddressesDataTableRow> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return CaseAddressesTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public String getBaseTypeId()
      {
        return CaseAddressesTabBean.this.getTabBaseTypeId();        
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public String getRowTypeId(CaseAddressesDataTableRow row)
      {
        return row.getTypeId();
      }
    };

    public TypeSelectHelper getTypeSelectHelper()
    {
      return typeSelectHelper;
    }
  }    

  @Inject
  CaseObjectBean caseObjectBean;

  @Inject
  AddressTypeBean addressTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return CaseAddressesTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return CaseAddressesTabBean.this.getColumns();
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
        return "caseAddressTypeId";
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

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
  }

  public CaseAddress getEditing()
  {
    return editing;
  }

  public void setEditing(CaseAddress editing)
  {
    this.editing = editing;
  }

  public boolean isImportPersons()
  {
    return importPersons;
  }

  public void setImportPersons(boolean importPersons)
  {
    this.importPersons = importPersons;
  }

  public void setCaseAddressTypeId(String caseAddressTypeId)
  {
    if (editing != null)
      editing.setCaseAddressTypeId(caseAddressTypeId);
  }

  public String getCaseAddressTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCaseAddressTypeId();
  }

  public List<CaseAddressesDataTableRow> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CaseAddressesDataTableRow> rows)
  {
    getCurrentTabInstance().rows = rows;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }
  
  public String getAddressDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return addressTypeBean.getDescription(editing.getAddressId());
    }
    return "";
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
  
  public void edit(DataTableRow row)
  {
    if (row != null)
    {
      try
      {
        editing = 
          CasesModuleBean.getPort(false).loadCaseAddress(row.getRowId());
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      create();
    }
  }

  @Override
  public void load() throws Exception
  {
    executeTabAction("preTabLoad", null);
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CaseAddressFilter filter = new CaseAddressFilter();
        filter.setCaseId(caseObjectBean.getObjectId());

        List<CaseAddressView> auxList = 
          CasesModuleBean.getPort(false).findCaseAddressViews(filter);
        
        List<CaseAddressView> result;        
        String typeId = getTabBaseTypeId();
        if (typeId == null)
        {
          result = auxList;          
        }
        else  
        {
          result = new ArrayList();
          for (CaseAddressView item : auxList)
          {
            String caseAddressTypeId = item.getCaseAddressTypeId();
            if (caseAddressTypeId == null)
              caseAddressTypeId = DictionaryConstants.CASE_ADDRESS_TYPE;

            Type caseAddressType =
              TypeCache.getInstance().getType(caseAddressTypeId);
            if (caseAddressType.isDerivedFrom(typeId))
            {
              result.add(item);
            }
          }
        }        
        List<CaseAddressesDataTableRow> auxList2 = toDataTableRows(result);
        if (getOrderBy() != null)
        {                
          Collections.sort(auxList2, 
            new DataTableRowComparator(getColumns(), getOrderBy()));
        }
        setRows(auxList2);
        getCurrentTabInstance().typeSelectHelper.load();        
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
    executeTabAction("postTabLoad", null);    
  }

  public void create()
  {
    editing = new CaseAddress();
    editing.setCaseAddressTypeId(getCreationTypeId());
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        String caseId = caseObjectBean.getObjectId();
        editing.setCaseId(caseId);
        editing = (CaseAddress) executeTabAction("preTabStore", editing);
        editing = CasesModuleBean.getPort(false).storeCaseAddress(editing);
        if (importPersons)
        {
          importPersonsFromEditingAddress();
          refreshCasePersonsTabInstances();
          importPersons = false;
        }
        executeTabAction("postTabStore", editing);        
        refreshHiddenTabInstances();
        load();
        editing = null;
        growl("STORE_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void remove(DataTableRow row)
  {
    if (row != null)
    {
      try
      {
        row = (DataTableRow)executeTabAction("preTabRemove", row);
        CasesModuleBean.getPort(false).removeCaseAddress(row.getRowId());
        executeTabAction("postTabRemove", row);
        refreshHiddenTabInstances();
        load();
        growl("REMOVE_OBJECT");
      }
      catch (Exception ex)
      {
        error(ex);
      }
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
 
  @Override
  public void clear()
  {
    tabInstances.clear();
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CaseAddress)stateArray[0];
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(CaseAddress caseAddress)
  {
    return (caseAddress != null && caseAddress.getCaseAddressId() == null);
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

  private void refreshCasePersonsTabInstances()
  {
    CasePersonsTabBean casePersonsTabBean =
      WebUtils.getBean("casePersonsTabBean");
    Collection<CasePersonsTabBean.TabInstance> cpTabInstances =
      casePersonsTabBean.getTabInstances().values();
    for (CasePersonsTabBean.TabInstance tabInstance : cpTabInstances)
    {
      tabInstance.objectId = NEW_OBJECT_ID;
    }
  }

  private void importPersonsFromEditingAddress() throws Exception
  {    
    if (editing != null)
    {
      String addressId = editing.getAddressId();
      if (addressId != null)
      {
        String typeId = caseObjectBean.getActiveEditTab().getProperties()
            .getString("importPersonsTypeId"); 
        if (typeId == null)
          typeId = DictionaryConstants.CASE_PERSON_TYPE;
        List<String> personIds = getCurrentPersons(editing.getCaseId(), typeId);
        List<PersonAddressView> personAddresses = getPersonAddresses(addressId);
        for (PersonAddressView personAddress : personAddresses)
        {
          String personId = personAddress.getPerson().getPersonId();
          if (!personIds.contains(personId))
          {
            CasePerson casePerson = new CasePerson();
            casePerson.setCasePersonTypeId(typeId);
            casePerson.setCaseId(editing.getCaseId());
            casePerson.setPersonId(personId);
            CasesModuleBean.getPort(false).storeCasePerson(casePerson);
          }
        }
      }
    }
  }
  
  private List<String> getCurrentPersons(String caseId, String importTypeId)
    throws Exception
  {
    List<String> results = new ArrayList();

    CasePersonFilter filter = new CasePersonFilter();
    filter.setCaseId(caseId);
    List<CasePersonView> casePersonViews =
      CasesModuleBean.getPort(true).findCasePersonViews(filter);

    for(CasePersonView casePersonView : casePersonViews)
    {
      if (casePersonView.getCasePersonTypeId().equals(importTypeId))
        results.add(casePersonView.getPersonView().getPersonId());
    }

    return results;
  }

  private List<PersonAddressView> getPersonAddresses(String addressId)
    throws Exception
  {
    PersonAddressFilter filter = new PersonAddressFilter();
    filter.setAddressId(addressId);
    return KernelModuleBean.getPort(true).findPersonAddressViews(filter);
  }
  
  private List<CaseAddressesDataTableRow> toDataTableRows(
    List<CaseAddressView> caseAddresses) throws Exception
  {
    List<CaseAddressesDataTableRow> convertedRows = new ArrayList<>();
    for (CaseAddressView row : caseAddresses)
    {
      CaseAddressesDataTableRow dataTableRow = 
        new CaseAddressesDataTableRow(row);
      dataTableRow.setValues(this, row, getTableProperties());      
      dataTableRow.setStyleClass(getRowStyleClass(row));
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
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
  
  public class CaseAddressesDataTableRow extends DataTableRow
  {      
    private String addressId;
    private String addressTypeId;
    private String addressDescription;
    private String addressFullDescription;
    private String addressCity;
    private String addressProvince;
    private String addressCountry;

    public CaseAddressesDataTableRow(CaseAddressView row)
    {
      super(row.getCaseAddressId(), row.getCaseAddressTypeId());
      if (row.getAddressView() != null)
      {
        addressId = row.getAddressView().getAddressId();
        addressTypeId = row.getAddressView().getAddressTypeId();
        addressDescription = row.getAddressView().getDescription();
        addressCity = row.getAddressView().getCity();
        addressProvince = row.getAddressView().getProvince();
        addressCountry = row.getAddressView().getCountry();
        addressFullDescription = addressDescription + 
          (!StringUtils.isBlank(addressCity) ? " (" + addressCity + ")" : "");
      }
    }

    public String getAddressId()
    {
      return addressId;
    }

    public void setAddressId(String addressId)
    {
      this.addressId = addressId;
    }

    public String getAddressTypeId()
    {
      return addressTypeId;
    }

    public void setAddressTypeId(String addressTypeId)
    {
      this.addressTypeId = addressTypeId;
    }

    public String getAddressDescription()
    {
      return addressDescription;
    }

    public void setAddressDescription(String addressDescription)
    {
      this.addressDescription = addressDescription;
    }

    public String getAddressFullDescription()
    {
      return addressFullDescription;
    }

    public void setAddressFullDescription(String addressFullDescription)
    {
      this.addressFullDescription = addressFullDescription;
    }

    public String getAddressCity()
    {
      return addressCity;
    }

    public void setAddressCity(String addressCity)
    {
      this.addressCity = addressCity;
    }

    public String getAddressProvince()
    {
      return addressProvince;
    }

    public void setAddressProvince(String addressProvince)
    {
      this.addressProvince = addressProvince;
    }

    public String getAddressCountry()
    {
      return addressCountry;
    }

    public void setAddressCountry(String addressCountry)
    {
      this.addressCountry = addressCountry;
    }
    
    @Override
    protected DataTableRow.Value getDefaultValue(String columnName)
    {
      if (columnName != null)
      {
        switch (columnName)
        {
          case "addressId":
            return new NumericValue(getAddressId());
          case "addressTypeId":
            return new TypeValue(getAddressTypeId());
          case "addressDescription":
            return new DefaultValue(getAddressDescription());
          case "addressFullDescription":
            return new DefaultValue(getAddressFullDescription());
          case "addressCity":
            return new DefaultValue(getAddressCity());
          case "addressProvince":
            return new DefaultValue(getAddressProvince());
          case "addressCountry":
            return new DefaultValue(getAddressCountry());
          default:
            break;
        }
      }
      return super.getDefaultValue(columnName);
    }
  }
  
  
}
