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
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
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
import org.matrix.kernel.Contact;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.primefaces.PrimeFaces;
import org.santfeliu.webapp.DataTableRowExportable;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.GroupableRowsHelper;
import org.santfeliu.webapp.helpers.RowsExportHelper;
import org.santfeliu.webapp.helpers.RowsFilterHelper;
import org.santfeliu.webapp.helpers.TablePropertyHelper;
import org.santfeliu.webapp.modules.dic.TypeTypeBean;
import org.santfeliu.webapp.modules.kernel.KernelModuleBean;
import org.santfeliu.webapp.modules.kernel.PersonTypeBean;
import org.santfeliu.webapp.setup.TableProperty;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.DataTableRow;
import org.santfeliu.webapp.util.DataTableRow.Value;
import org.santfeliu.webapp.util.DataTableRowComparator;
import org.santfeliu.webapp.util.DateTimeRowStyleClassGenerator;
import org.santfeliu.webapp.util.RowStyleClassGenerator;
import org.santfeliu.webapp.util.WebUtils;
import static org.santfeliu.webapp.setup.Action.POST_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_STORE_ACTION;
import static org.santfeliu.webapp.setup.Action.POST_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_EDIT_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_LOAD_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_REMOVE_ACTION;
import static org.santfeliu.webapp.setup.Action.PRE_TAB_STORE_ACTION;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class CasePersonsTabBean extends TabBean  
  implements DataTableRowExportable
{
  private Map<String, TabInstance> tabInstances = new HashMap<>();
  private CasePerson editing;
  private boolean importAddresses;
  private Map<String, ContactView> personContacts = new HashMap();
  private Map<String, ContactView> representantContacts = new HashMap();
  private String contactValue;
  private String contactTypeId;
  private String representantContactValue;
  private String representantContactTypeId;
  private int tabIndex;
  private final TabInstance EMPTY_TAB_INSTANCE = new TabInstance();
  private List<SelectItem> contactTypeSelectItems;
  private GroupableRowsHelper groupableRowsHelper;

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CasePersonsDataTableRow> rows;
    int firstRow = 0;
    RowsFilterHelper rowsFilterHelper =
      new RowsFilterHelper<CasePersonsDataTableRow>()
    {
      @Override
      public ObjectBean getObjectBean() 
      {
        return CasePersonsTabBean.this.getObjectBean();
      }
      
      @Override
      public List<CasePersonsDataTableRow> getRows()
      {
        return rows;
      }

      @Override
      public boolean isGroupedViewEnabled()
      {
        return CasePersonsTabBean.this.getGroupableRowsHelper().
          isGroupedViewEnabled();
      }

      @Override
      public void resetFirstRow()
      {
        firstRow = 0;
      }

      @Override
      public List<TableProperty> getColumns() 
      {
        return CasePersonsTabBean.this.getColumns();        
      }

      @Override
      public String getFixedColumnValue(CasePersonsDataTableRow row, 
        String columnName) 
      {
        return null; //No fixed columns        
      }

      @Override
      public String getRowTypeId(CasePersonsDataTableRow row) 
      {
        return row.getTypeId();               
      }
    };

    public RowsFilterHelper getRowsFilterHelper()
    {
      return rowsFilterHelper;
    }
  }

  @Inject
  PersonTypeBean personTypeBean;

  @Inject
  TypeTypeBean typeTypeBean;

  @Inject
  CaseObjectBean caseObjectBean;

  @PostConstruct
  public void init()
  {
    groupableRowsHelper = new GroupableRowsHelper()
    {
      @Override
      public ObjectBean getObjectBean()
      {
        return CasePersonsTabBean.this.getObjectBean();
      }

      @Override
      public List<TableProperty> getColumns()
      {
        return CasePersonsTabBean.this.getColumns();
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
        return "casePersonTypeId";
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

  @Override
  public ObjectBean getObjectBean()
  {
    return caseObjectBean;
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

  public List<CasePersonsDataTableRow> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CasePersonsDataTableRow> rows)
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

  public CasePerson getEditing()
  {
    return editing;
  }

  public void setEditing(CasePerson casePerson)
  {
    editing = casePerson;
  }

  public String getContactValue()
  {
    return contactValue;
  }

  public void setContactValue(String contactValue)
  {
    this.contactValue = contactValue;
  }

  public String getContactTypeId()
  {
    return contactTypeId;
  }

  public void setContactTypeId(String contactTypeId)
  {
    this.contactTypeId = contactTypeId;
  }

  public String getRepresentantContactValue()
  {
    return representantContactValue;
  }

  public void setRepresentantContactValue(String representantContactValue)
  {
    this.representantContactValue = representantContactValue;
  }

  public String getRepresentantContactTypeId()
  {
    return representantContactTypeId;
  }

  public void setRepresentantContactTypeId(String representantContactTypeId)
  {
    this.representantContactTypeId = representantContactTypeId;
  }

  public int getTabIndex()
  {
    return tabIndex;
  }

  public void setTabIndex(int tabIndex)
  {
    this.tabIndex = tabIndex;
  }

  public boolean isImportAddresses()
  {
    return importAddresses;
  }

  public void setImportAddresses(boolean importAddresses)
  {
    this.importAddresses = importAddresses;
  }

  public String getPersonId()
  {
    return editing.getPersonId();
  }

  public void setPersonId(String personId)
  {
    if (!StringUtils.defaultString(personId).equals(
      StringUtils.defaultString(editing.getPersonId())))
    {
      onPersonSelect(personId);
    }
    editing.setPersonId(personId);
  }

  public void setCasePersonTypeId(String casePersonTypeId)
  {
    if (editing != null)
      editing.setCasePersonTypeId(casePersonTypeId);
  }

  public String getCasePersonTypeId()
  {
    return editing == null ? NEW_OBJECT_ID : editing.getCasePersonTypeId();
  }

  public String getRepresentantPersonId()
  {
    return editing.getRepresentantPersonId();
  }

  public void setRepresentantPersonId(String personId)
  {
    if (!StringUtils.defaultString(personId).equals(
      StringUtils.defaultString(editing.getRepresentantPersonId())))
    {
      onRepresentantSelect(personId);
    }
    editing.setRepresentantPersonId(personId);
  }

  public List<SelectItem> getContactTypeSelectItems()
  {
    if (contactTypeSelectItems == null)
    {
      contactTypeSelectItems = typeTypeBean.getSelectItems("Contact");
    }
    return contactTypeSelectItems;
  }

  public void onPersonSelect()
  {
    onPersonSelect(editing.getPersonId());
  }

  public void onRepresentantSelect()
  {
    onRepresentantSelect(editing.getRepresentantPersonId());
  }

  public List<ContactView> getPersonContacts()
  {
    if (editing == null)
      return Collections.emptyList();

    List<ContactView> results = new ArrayList<>();
    for (String contactId : personContacts.keySet())
    {
      if (!editing.getContactId().contains(contactId))
        results.add(personContacts.get(contactId));
    }
    return results;
  }

  public List<ContactView> getRepresentantContacts()
  {
    if (editing == null)
      return Collections.emptyList();

    List<ContactView> results = new ArrayList<>();
    for (String contactId : representantContacts.keySet())
    {
      if (!editing.getRepresentantContactId().contains(contactId))
        results.add(representantContacts.get(contactId));
    }
    return results;
  }

  public List<ContactView> getSelectedContacts()
  {
    if (editing == null)
      return Collections.emptyList();

    List<ContactView> results = new ArrayList<>();
    for (String contactId : editing.getContactId())
    {
      ContactView contactView = personContacts.get(contactId);
      if (contactView != null)
        results.add(contactView);
    }

    return results;
  }

  public List<ContactView> getSelectedRepresentantContacts()
  {
    if (editing == null)
      return Collections.emptyList();

    List<ContactView> results = new ArrayList<>();
    for (String contactId : editing.getRepresentantContactId())
    {
      ContactView contactView = representantContacts.get(contactId);
      if (contactView != null)
        results.add(contactView);
    }

    return results;
  }

  public void selectContact(ContactView contact)
  {
    if (editing.getContactId().size() < 3)
      editing.getContactId().add(contact.getContactId());
  }

  public void unselectContact(ContactView contact)
  {
    if (!editing.getContactId().isEmpty())
      editing.getContactId().remove(contact.getContactId());
  }

  public void selectRepresentantContact(ContactView contact)
  {
    if (editing.getRepresentantContactId().size() < 3)
      editing.getRepresentantContactId().add(contact.getContactId());
  }

  public void unselectRepresentantContact(ContactView contact)
  {
    if (!editing.getRepresentantContactId().isEmpty())
      editing.getRepresentantContactId().remove(contact.getContactId());
  }

  public void moveContactDown(Integer index)
  {
    moveContactDown(editing.getContactId(), index);
  }

  public void moveRepresentantContactDown(Integer index)
  {
    moveContactDown(editing.getRepresentantContactId(), index);
  }

  private void moveContactDown(List<String> contacts, Integer index)
  {
    if (index >= 0 && index <= 2)
    {
      int index2 = (index + 1) % contacts.size();
      String contactId = contacts.get(index);
      String contactId2 = contacts.get(index2);
      contacts.set(index2, contactId);
      contacts.set(index, contactId2);
    }
  }

  public String getPersonDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return personTypeBean.getDescription(editing.getPersonId());
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

  @Override
  public List<TableProperty> getTableProperties()
  {
    EditTab activeEditTab = caseObjectBean.getActiveEditTab();
    if (activeEditTab != null)
      return activeEditTab.getTableProperties();
    else
      return Collections.EMPTY_LIST;
  }

  @Override
  public List<TableProperty> getColumns()
  {
    return TablePropertyHelper.getColumnTableProperties(getTableProperties());
  }

  @Override
  public List<? extends DataTableRow> getExportableRows() 
  {
    return getCurrentTabInstance().rowsFilterHelper.getFilteredRows();    
  }  

  @Override
  public int getRowExportLimit()
  {
    return RowsExportHelper.getActiveEditTabRowExportLimit(caseObjectBean);
  }
  
  @Override
  public boolean isExportable()
  {
    return RowsExportHelper.isActiveEditTabExportable(caseObjectBean);
  } 
  
  public void create()
  {
    executeTabAction(PRE_TAB_EDIT_ACTION, null);
    editing = new CasePerson();
    editing.setCasePersonTypeId(getCreationTypeId());
    tabIndex = 0;
    executeTabAction(POST_TAB_EDIT_ACTION, editing);
  }

  @Override
  public void load()
  {
    executeTabAction(PRE_TAB_LOAD_ACTION, null);
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CasePersonFilter filter = new CasePersonFilter();
        filter.setCaseId(getObjectId());

        String typeId = getTabBaseTypeId();
        EditTab tab = caseObjectBean.getActiveEditTab();
        if (tab.isShowAllTypes())
          typeId = DictionaryConstants.CASE_PERSON_TYPE;
        if (typeId != null)
          filter.setCasePersonTypeId(typeId);

        List<CasePersonView> casePersons =
          CasesModuleBean.getPort(false).findCasePersonViews(filter);

        List<CasePersonsDataTableRow> auxList = toDataTableRows(casePersons);
        if (getOrderBy() != null)
        {
          Collections.sort(auxList,
            new DataTableRowComparator(getColumns(), getOrderBy()));
        }
        setRows(auxList);
        getCurrentTabInstance().rowsFilterHelper.load();
        executeTabAction(POST_TAB_LOAD_ACTION, null);
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
      getCurrentTabInstance().rowsFilterHelper.load();
      tabInstance.firstRow = 0;
    }
  }

  public void edit(DataTableRow row)
  {
    if (row != null)
    {
      try
      {
        executeTabAction(PRE_TAB_EDIT_ACTION, row);
        editing = CasesModuleBean.getPort(false).loadCasePerson(row.getRowId());
        personContacts =
          getPersonContacts(editing.getPersonId());
        representantContacts =
          getPersonContacts(editing.getRepresentantPersonId());
        tabIndex = 0;
        executeTabAction(POST_TAB_EDIT_ACTION, editing);
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
  public void edit(String rowId)
  {
    List<? extends DataTableRow> rows = getRows();
    for (DataTableRow row : rows)
    {
      if (row.getRowId().equals(rowId))
      {
        edit(row);
        PrimeFaces.current().executeScript("PF('casePersonsDialog').show()");
        return;
      }
    }
  }  

  @Override
  public void store()
  {
    try
    {
      editing.setCaseId(getObjectId());

      if (editing.getCasePersonTypeId() == null)
        editing.setCasePersonTypeId(DictionaryConstants.CASE_PERSON_TYPE);
      editing = (CasePerson) executeTabAction(PRE_TAB_STORE_ACTION, editing);
      editing = CasesModuleBean.getPort(false).storeCasePerson(editing);
      if (importAddresses)
      {
        importAddressesFromEditingPerson();
        refreshCaseAddressesTabInstances();
        importAddresses = false;
      }
      executeTabAction(POST_TAB_STORE_ACTION, editing);
      refreshHiddenTabInstances();
      load();
      editing = null;
      growl("STORE_OBJECT");
    }
    catch (Exception ex)
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
        row = (DataTableRow)executeTabAction(PRE_TAB_REMOVE_ACTION, row);
        CasesModuleBean.getPort(false).removeCasePerson(row.getRowId());
        executeTabAction(POST_TAB_REMOVE_ACTION, row);
        refreshHiddenTabInstances();
        load();
        growl("REMOVE_OBJECT");
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

  public void addNewContact()
  {
    if (!StringUtils.isBlank(contactTypeId)
      && !StringUtils.isBlank(contactValue)
      && editing.getContactId().size() < 3)
    {
      Contact newContact = new Contact();
      newContact.setPersonId(editing.getPersonId());
      newContact.setContactTypeId(contactTypeId);
      newContact.setValue(contactValue);

      try
      {
        newContact = storeNewContact(newContact);
        personContacts = getPersonContacts(editing.getPersonId());
        editing.getContactId().add(newContact.getContactId());

        contactTypeId = null;
        contactValue = null;
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  public void addNewRepresentantContact()
  {
    if (!StringUtils.isBlank(representantContactTypeId)
      && !StringUtils.isBlank(representantContactValue)
      && editing.getRepresentantContactId().size() < 3)
    {
      Contact newContact = new Contact();
      newContact.setPersonId(editing.getRepresentantPersonId());
      newContact.setContactTypeId(representantContactTypeId);
      newContact.setValue(representantContactValue);

      try
      {
        newContact = storeNewContact(newContact);
        representantContacts =
          getPersonContacts(editing.getRepresentantPersonId());
        editing.getRepresentantContactId().add(newContact.getContactId());

        representantContactTypeId = null;
        representantContactValue = null;
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }

  private Contact storeNewContact(Contact contact)
    throws Exception
  {
    Contact newContact =
      KernelModuleBean.getPort(true).storeContact(contact);

    contact.setContactTypeId(null);
    contact.setPersonId(null);
    contact.setValue(null);

    return newContact;
  }

  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing, tabIndex };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (CasePerson)stateArray[0];
      tabIndex = (int)stateArray[1];

      load();

      if (editing != null)
      {
        personContacts =
          getPersonContacts(editing.getPersonId());
        representantContacts =
          getPersonContacts(editing.getRepresentantPersonId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private List<CasePersonsDataTableRow> toDataTableRows(
    List<CasePersonView> casePersons) throws Exception
  {
    List<CasePersonsDataTableRow> convertedRows = new ArrayList<>();
    for (CasePersonView row : casePersons)
    {
      CasePersonsDataTableRow dataTableRow = new CasePersonsDataTableRow(row);
      dataTableRow.setValues(this, row, getTableProperties());
      dataTableRow.setStyleClass(getRowStyleClass(row));
      convertedRows.add(dataTableRow);
    }
    return convertedRows;
  }

  private boolean isNew(CasePerson casePerson)
  {
    return (casePerson != null && casePerson.getCasePersonId() == null);
  }

  private void onPersonSelect(String personId)
  {
    try
    {
      personContacts = getPersonContacts(personId);
      editing.getContactId().clear();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void onRepresentantSelect(String personId)
  {
    try
    {
      representantContacts = getPersonContacts(personId);
      editing.getRepresentantContactId().clear();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private Map<String, ContactView> getPersonContacts(String personId) throws Exception
  {
    if (personId == null)
      return Collections.emptyMap();

    ContactFilter filter = new ContactFilter();
    filter.setPersonId(personId);

    List<ContactView> contacts =
      KernelModuleBean.getPort(true).findContactViews(filter);

    Map<String, ContactView> results = new TreeMap();
    for (ContactView contact : contacts)
    {
      results.put(contact.getContactId(), contact);
    }

    return results;
  }

  private void importAddressesFromEditingPerson() throws Exception
  {
    if (editing != null)
    {
      String personId = editing.getPersonId();
      if (personId != null)
      {
        String caseId = editing.getCaseId();
        String typeId = caseObjectBean.getActiveEditTab().getProperties()
          .getString("importAddressesTypeId");
        if (typeId == null)
          typeId = DictionaryConstants.CASE_ADDRESS_TYPE;
        List<String> currentAddressIds = getCurrentAddresses(caseId, typeId);
        List<PersonAddressView> personAddresses =
          getPersonAddresses(personId);
        for (PersonAddressView personAddressView : personAddresses)
        {
          String addressId = personAddressView.getAddress().getAddressId();
          if (!currentAddressIds.contains(addressId))
          {
            CaseAddress caseAddress = new CaseAddress();
            caseAddress.setCaseId(caseId);
            caseAddress.setAddressId(addressId);
            caseAddress.setCaseAddressTypeId(typeId);
            CasesModuleBean.getPort(false).storeCaseAddress(caseAddress);
          }
        }
      }
    }
  }

  private List<PersonAddressView> getPersonAddresses(String personId)
    throws Exception
  {
    PersonAddressFilter filter = new PersonAddressFilter();
    filter.setPersonId(personId);
    return KernelModuleBean.getPort(true).findPersonAddressViews(filter);
  }

  private List<String> getCurrentAddresses(String caseId, String importTypeId)
    throws Exception
  {
    List<String> results = new ArrayList();

    CaseAddressFilter filter = new CaseAddressFilter();
    filter.setCaseId(caseId);
    List<CaseAddressView> caseAddresses =
      CasesModuleBean.getPort(true).findCaseAddressViews(filter);

    for(CaseAddressView caseAddressView : caseAddresses)
    {
      if (caseAddressView.getCaseAddressTypeId().equals(importTypeId))
        results.add(caseAddressView.getAddressView().getAddressId());
    }

    return results;
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

  private void refreshCaseAddressesTabInstances()
  {
    CaseAddressesTabBean caseAddressesTabBean =
      WebUtils.getBean("caseAddressesTabBean");
    Collection<CaseAddressesTabBean.TabInstance> caTabInstances =
      caseAddressesTabBean.getTabInstances().values();
    for (CaseAddressesTabBean.TabInstance tabInstance : caTabInstances)
    {
      tabInstance.objectId = NEW_OBJECT_ID;
    }
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

  public class CasePersonsDataTableRow extends DataTableRow
  {
    private String personId;
    private String personTypeId;
    private String personName;
    private String personIdent;

    public CasePersonsDataTableRow(CasePersonView row)
    {
      super(row.getCasePersonId(), row.getCasePersonTypeId());
      if (row.getPersonView() != null)
      {
        personId = row.getPersonView().getPersonId();
        personTypeId = row.getPersonView().getPersonTypeId();
        personName = row.getPersonView().getFullName();
        personIdent = row.getPersonView().getNif() != null ?
          row.getPersonView().getNif() :
          row.getPersonView().getPassport();
      }
    }

    public String getPersonId()
    {
      return personId;
    }

    public void setPersonId(String personId)
    {
      this.personId = personId;
    }

    public String getPersonTypeId()
    {
      return personTypeId;
    }

    public void setPersonTypeId(String personTypeId)
    {
      this.personTypeId = personTypeId;
    }

    public String getPersonName()
    {
      return personName;
    }

    public void setPersonName(String personName)
    {
      this.personName = personName;
    }

    public String getPersonIdent()
    {
      return personIdent;
    }

    public void setPersonIdent(String personIdent)
    {
      this.personIdent = personIdent;
    }

    @Override
    protected Value getDefaultValue(String columnName)
    {
      if (columnName != null)
      {
        switch (columnName)
        {
          case "personId":
            return new NumericValue(getPersonId());
          case "personTypeId":
            return new TypeValue(getPersonTypeId());
          case "personName":
            return new DefaultValue(getPersonName());
          case "personIdent":
            return new DefaultValue(getPersonIdent());
          default:
            break;
        }
      }
      return super.getDefaultValue(columnName);
    }
  }

}
