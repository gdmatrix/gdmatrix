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
import org.matrix.kernel.Contact;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.primefaces.PrimeFaces;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.modules.kernel.KernelModuleBean;
import org.santfeliu.webapp.modules.kernel.PersonTypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author realor
 */
@Named
@ViewScoped
public class CasePersonsTabBean extends TabBean
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

  public class TabInstance
  {
    String objectId = NEW_OBJECT_ID;
    List<CasePersonView> rows;
    int firstRow = 0;
    boolean groupedView = true;   
  }

  @Inject
  PersonTypeBean personTypeBean;

  @Inject
  CaseObjectBean caseObjectBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
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

  public List<CasePersonView> getRows()
  {
    return getCurrentTabInstance().rows;
  }

  public void setRows(List<CasePersonView> casePersonViews)
  {
    getCurrentTabInstance().rows = casePersonViews;
  }

  public int getFirstRow()
  {
    return getCurrentTabInstance().firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    getCurrentTabInstance().firstRow = firstRow;
  }
  
  public boolean isGroupedView()
  {
    return isGroupedViewEnabled() && getCurrentTabInstance().groupedView;
  }

  public void setGroupedView(boolean groupedView)
  {
    getCurrentTabInstance().groupedView = groupedView;
  }  
  
  public boolean isGroupedViewEnabled()
  {
    return Boolean.parseBoolean(caseObjectBean.getActiveEditTab().
      getProperties().getString("groupedViewEnabled"));
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
  
  public Map<String, TabInstance> getTabInstances()
  {
    return tabInstances;
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
    showDialog();
  }
    
  public void setCasePersonTypeId(String casePersonTypeId)
  {
    if (editing != null)
      editing.setCasePersonTypeId(casePersonTypeId);

    showDialog();
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
    showDialog();
  }  
  
  public void onPersonSelect()
  {
    onPersonSelect(editing.getPersonId());
  }
  
  public void onRepresentantSelect()
  {
    onRepresentantSelect(editing.getRepresentantPersonId());
  }  
  
  public String getAddressId()
  {
    return editing.getAddressId();
  }
  
  public void setAddressId(String personId)
  {
    editing.setAddressId(personId);
    showDialog();
  }  
  
  public String getRepresentantAddressId()
  {
    return editing.getRepresentantAddressId();
  }
  
  public void setRepresentantAddressId(String personId)
  {
    editing.setRepresentantAddressId(personId);
    showDialog();
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

  public void switchView()
  {
    getCurrentTabInstance().groupedView = !getCurrentTabInstance().groupedView;
  }  
  
  public String getPersonDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return personTypeBean.getDescription(editing.getPersonId());
    }
    return "";
  }  
  
  public void create()
  {
    editing = new CasePerson(); 
    editing.setCasePersonTypeId(getCreationTypeId());
  }
  
  @Override
  public void load()
  {
    System.out.println("load casePersons:" + getObjectId());
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        CasePersonFilter filter = new CasePersonFilter();
        filter.setCaseId(getObjectId());
        
        String typeId = getTabBaseTypeId();
        if (typeId != null)
          filter.setCasePersonTypeId(typeId);        
        
        getCurrentTabInstance().rows = 
          CasesModuleBean.getPort(false).findCasePersonViews(filter);
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
      tabInstance.firstRow = 0;      
    }
  }
  
  public void edit(CasePersonView casePersonView)
  {
    if (casePersonView != null)
    {
      try
      {
        editing = CasesModuleBean.getPort(false)
          .loadCasePerson(casePersonView.getCasePersonId());
        personContacts = 
          getPersonContacts(editing.getPersonId());
        representantContacts = 
          getPersonContacts(editing.getRepresentantPersonId());
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
  public void store()
  {
    try
    { 
      editing.setCaseId(getObjectId());
      
      if (editing.getCasePersonTypeId() == null)
        editing.setCasePersonTypeId(DictionaryConstants.CASE_PERSON_TYPE);
  
      CasesModuleBean.getPort(false).storeCasePerson(editing);
      if (importAddresses)
      {
        importAddressesFromEditingPerson();
        refreshCaseAddressesTabInstances();
        importAddresses = false;
      }      
      
      refreshHiddenTabInstances();
      load();
      editing = null;
      info("STORE_OBJECT");
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

  public void remove(CasePersonView row)
  {
    try
    {
      if (row != null)
      {
        String casePersonId = row.getCasePersonId();
        CasesModuleBean.getPort(false).removeCasePerson(casePersonId);
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

  private boolean isNew(CasePerson casePerson)
  {
    return (casePerson != null && casePerson.getCasePersonId() == null);
  }

  private void showDialog()
  {
    try
    {
      PrimeFaces current = PrimeFaces.current();
      current.executeScript("PF('casePersonsDialog').show();");
    }
    catch (Exception ex)
    {
      error(ex);
    }
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
        List<String> currentAddressIds = getCurrentAddresses(caseId);
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
            caseAddress.setCaseAddressTypeId(
              DictionaryConstants.CASE_PERSON_TYPE);            
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
  
  private List<String> getCurrentAddresses(String caseId)
    throws Exception
  {
    List<String> results = new ArrayList();
    
    CaseAddressFilter filter = new CaseAddressFilter();
    filter.setCaseId(caseId);
    List<CaseAddressView> caseAddresses = 
      CasesModuleBean.getPort(true).findCaseAddressViews(filter);
    
    for(CaseAddressView caseAddressView : caseAddresses)
    {
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
  
}
