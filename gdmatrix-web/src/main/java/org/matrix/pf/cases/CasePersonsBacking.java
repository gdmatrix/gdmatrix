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
package org.matrix.pf.cases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Contact;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;
import org.matrix.pf.kernel.KernelUtils;
import org.matrix.pf.kernel.PersonBacking;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.kernel.web.AddressBean;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.kernel.web.PersonContactsBean;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
@Named
public class CasePersonsBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{
  private static final String PERSON_BACKING = "personBacking";
  private static final String CASE_BACKING = "caseBacking";
  
  private static final String OUTCOME = "pf_case_persons";
  
  private static final String GROUPBY_PROPERTY = "groupBy";
  
  private CaseBacking caseBacking;
  
  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<CasePersonView> resultListHelper;
  private TabHelper tabHelper;
  
  private CasePerson editing;
  
  private SelectItem personSelectItem;
  private SelectItem representantSelectItem;
  
  private List<SelectItem> personAddresses;
  private List<SelectItem> representantAddresses;
  
  private List<ContactView> contacts;
  private List<ContactView> representantContacts; 
  
  private List<ContactView> selectedContacts;
  private List<ContactView> selectedRepresentantContacts; 
  private String contactTypeId;
  private String contactValue; 
  private String representantContactTypeId;
  private String representantContactValue;     
    
  private boolean importAddresses = false;
  private List<SelectItem> contactTypeSelectItems;


  public CasePersonsBacking()
  { 
  }
  
  @PostConstruct
  public void init()
  {
    caseBacking = WebUtils.getBacking(CASE_BACKING);   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
  }
  
  public CasePerson getEditing()
  {
    return editing;
  }

  public void setEditing(CasePerson editing)
  {
    this.editing = editing;
  } 
  
  public boolean isNew()
  {
    return isNew(editing);
  }

  @Override
  public CaseBacking getObjectBacking()
  {
    return caseBacking;
  }
  
  @Override
  public String getPageObjectId()
  {
    if (editing != null)
      return editing.getCasePersonId();
    else
      return null;
  }
  
  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);
      return getDescription(personBacking, editing.getPersonId());
    }
    return null;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CASE_PERSON_TYPE;
  }
  
  @Override
  public String getTypeId()
  {
    return caseBacking.getTabTypeId();
  }
  
  @Override
  public ResultListHelper<CasePersonView> getResultListHelper()
  {
    return resultListHelper;
  }  
  
  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }  

  public TabHelper getTabHelper()
  {
    return tabHelper;
  }
  
  public List<CasePersonView> getRows()
  {
    return resultListHelper.getRows();
  }
  
  public String getViewStartDate()
  {
    String date = "";
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      date = row.getStartDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }

  public String getViewEndDate()
  {
    String date = "";
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      date = row.getEndDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }   
  
  public String getCasePersonTypeId()
  {
    String typeId = null;
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
    {
      typeId = row.getCasePersonTypeId();
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        return type.getDescription();
      }
    }
    return typeId;    
  }
  
  public Date getStartDate()
  {
    if (editing != null && editing.getStartDate() != null)
      return TextUtils.parseInternalDate(editing.getStartDate());
    else
      return null;
  }
  
  public Date getEndDate()
  {
    if (editing != null && editing.getEndDate() != null)
      return TextUtils.parseInternalDate(editing.getEndDate());
    else
      return null;
  }  
    
  public void setStartDate(Date date)
  {
    if (date != null && editing != null)
      editing.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
  }
  
  public void setEndDate(Date date)
  {
    if (date != null && editing != null)
      editing.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
  }   
  
  public Date getCreationDateTime()
  {
    if (editing != null && editing.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(editing.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (editing != null && editing.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(editing.getChangeDateTime());
    else
      return null;
  }  
  
  public boolean isRenderGroupedResults()
  {
    return getProperty(GROUPBY_PROPERTY) != null;
  }  
  
  //Person selection
  public SelectItem getPersonSelectItem()
  {
    return personSelectItem;
  }
  
  public void setPersonSelectItem(SelectItem item)
  {
    personSelectItem = item;
  }
  
  public void onPersonSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String personId = (String) item.getValue();
    setSelectedPerson(personId);
  }  
  
  public void setSelectedPerson(String personId)
  {
    editing.setPersonId(personId);    
    if (personSelectItem == null || 
      !personId.equals(personSelectItem.getValue()))
    {
      personSelectItem = newPersonSelectItem(personId);
    }
    contacts = getContacts(personId);
    personAddresses = getAddresses(editing.getAddressId(), personId);
    selectedContacts = null;
    editing.getContactId().clear();
    editing.setAddressId(null);      
    showDialog();    
  }
  
  public List<SelectItem> completePerson(String query)
  {
    return completePerson(query, editing.getPersonId());
  }  
  
  //Representant selection
  public SelectItem getRepresentantSelectItem()
  {
    return representantSelectItem;
  }

  public void setRepresentantSelectItem(SelectItem representantSelectItem)
  {
    this.representantSelectItem = representantSelectItem;
  }
  
  public void onRepresentantSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String personId = (String) item.getValue();
    setSelectedRepresentant(personId);
  } 
  
  public void setSelectedRepresentant(String personId)
  {
    editing.setRepresentantPersonId(personId);
    if (!personId.equals(personSelectItem.getValue()))
      representantSelectItem = newPersonSelectItem(personId);    
    representantContacts = getContacts(personId);
    representantAddresses = getAddresses(editing.getRepresentantAddressId(),
      editing.getRepresentantPersonId());
    selectedRepresentantContacts = null;
    editing.getRepresentantContactId().clear();
    editing.setRepresentantAddressId(null);
    showDialog();
  }  

  public void onRepresentantClear()
  {
    editing.setRepresentantPersonId(null);
    editing.getRepresentantContactId().clear();
    editing.setRepresentantAddressId(null);     
    representantContacts = null;
    representantAddresses = null;
    selectedRepresentantContacts = null;
  }

  public List<SelectItem> completeRepresentant(String query)
  {
    return completePerson(query, editing.getRepresentantPersonId());    
  }  

  public List<SelectItem> getFavorites()
  {
    PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);
    return personBacking.getFavorites();     
  }      

  //Adresses
  public List<SelectItem> getPersonAddressSelectItems()
  {
    return personAddresses;
  }  

  public List<SelectItem> getRepresentantAddressSelectItems()
  {
    return representantAddresses;
  }
  
  public void setSelectedAddress(String addressId)
  {
    if (editing != null)
    {
      editing.setAddressId(addressId);
      showDialog();
    }
  }
  
  public void setSelectedRepresentantAddress(String addressId)
  {
    if (editing != null)
    {
      editing.setRepresentantAddressId(addressId);
      showDialog();
    }
  }  

  //Contacts
  
  /**
   * 
   * @return the selected personal contacts.
   */
  public List<ContactView> getSelectedContacts()
  {
    return selectedContacts;
  }
  
  public List<ContactView> getSelectedRepresentantContacts()
  {
    return selectedRepresentantContacts;
  }    
    
  public void removeContact(String contactId)
  {
    ContactView remove = null;
    for (ContactView contact : selectedContacts)
    {
      if (contactId != null && contactId.equals(contact.getContactId()))
      {
        remove = contact;
        break;
      }
    }
    
    if (remove != null)
    {
      selectedContacts.remove(remove);
      editing.getContactId().remove(contactId);
    }
  }
  
  public void removeRepresentantContact(String contactId)
  {
    ContactView remove = null;
    for (ContactView contact : selectedRepresentantContacts)
    {
      if (contactId != null && contactId.equals(contact.getContactId()))
      {
        remove = contact;
        break;
      }
    }
    
    if (remove != null)
    {
      selectedRepresentantContacts.remove(remove);
      editing.getRepresentantContactId().remove(contactId);
    }
  }  
  
  public void setSelectedContact(ContactView selectedContact)
  {
    //Do nothing. Don't want to keep it.
  }
    
  public ContactView getSelectedContact()
  {
    return null;
  }
     
  public List<ContactView> completeContact(String query)
  {
    return completeContact(query, contacts);
  }
  
  public List<ContactView> completeRepresentantContact(String query)
  {
    return completeContact(query, representantContacts);
  }  
  
  private List<SelectItem> completePerson(String query, String personId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);
    
    //Add current item
    if (!isNew(editing))
    {
      String description = "";
      if (personId != null)
        description = personBacking.getDescription(personId);
      items.add(new SelectItem(personId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      PersonFilter filter = new PersonFilter();
      filter.setFullName(query);
      filter.setMaxResults(10);
      List<PersonView> persons =
        KernelConfigBean.getPort().findPersonViews(filter);
      
      if (persons != null)
      {       
        for (PersonView person : persons)
        {
          String description = personBacking.getDescription(person);
          SelectItem item = new SelectItem(person.getPersonId(), description);
          items.add(item);
        }
      }
    }
    else
    {
      //Add favorites
      items.addAll(personBacking.getFavorites()); 
    }
    
    return items;
  }  
    
  public String getContactIcon(ContactView contact)
  {
    if (contact != null)
      return KernelUtils.getContactTypeIconName(contact.getContactTypeId());
    else
      return "";
  }

  public boolean isImportAddresses()
  {
    return importAddresses;
  }

  public void setImportAddresses(boolean importAddresses)
  {
    this.importAddresses = importAddresses;
  }
  
  
  public void addNewContact()
  {
    if (contactTypeId != null && contactValue != null 
      && !isContactsMaxSize(selectedContacts))
    {
      Contact newContact = new Contact();
      newContact.setPersonId(editing.getPersonId());
      newContact.setContactTypeId(contactTypeId);
      newContact.setValue(contactValue);
      addNewContact(newContact, editing.getPersonId(), editing.getContactId(), 
        contacts);
      loadPersonContacts(); 
      loadSelectedContacts();
      contactTypeId = null;
      contactValue = null;
    }
  }
  
  public void addNewRepresentantContact()
  {
    if (representantContactTypeId != null && representantContactValue != null 
      && !isContactsMaxSize(selectedRepresentantContacts))
    {
      String personId = editing.getRepresentantPersonId();
      Contact newContact = new Contact();
      newContact.setPersonId(personId);
      newContact.setContactTypeId(representantContactTypeId);
      newContact.setValue(representantContactValue);
      addNewContact(newContact, personId, editing.getRepresentantContactId(), 
        contacts);
      loadPersonContacts(); 
      loadSelectedContacts();
      representantContactTypeId = null;
      representantContactValue = null;
    }
  }  

  public String getContactValue()
  {
    return contactValue;
  }

  public void setContactValue(String newContactValue)
  {
    this.contactValue = newContactValue;
  }

  public String getContactTypeId()
  {
    return contactTypeId;
  }

  public void setContactTypeId(String typeId)
  {
    this.contactTypeId = typeId;
  }

  public String getRepresentantContactTypeId()
  {
    return representantContactTypeId;
  }

  public void setRepresentantContactTypeId(String representantContactTypeId)
  {
    this.representantContactTypeId = representantContactTypeId;
  }

  public String getRepresentantContactValue()
  {
    return representantContactValue;
  }

  public void setRepresentantContactValue(String representantContactValue)
  {
    this.representantContactValue = representantContactValue;
  }
   
  @Override
  public String show(String pageObjectId)
  {
    editPerson(pageObjectId);
    showDialog();
    return (isEditing(pageObjectId) ? OUTCOME : show());
  }  
  
  @Override
  public String show()
  {    
    populate();
    return OUTCOME;
  }
  
  public String editPerson(CasePersonView row)
  {
//    reset();
    String casePersonId = null;
    if (row != null)
      casePersonId = row.getCasePersonId();

    return editPerson(casePersonId);
  } 
  
  public String createPerson()
  {
    reset();    
    editing = new CasePerson();
    return null;
  }  
  
  public String removePerson(CasePersonView row)
  {
    try
    {
      if (row == null)
        throw new Exception("PERSON_MUST_BE_SELECTED");
      
      String rowCasePersonId = row.getCasePersonId();
      
      if (editing != null && rowCasePersonId.equals(editing.getCasePersonId()))
        editing = null;
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCasePerson(rowCasePersonId);
      
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  public String storePerson()
  {
    try
    {
      if (editing == null)
        return null;
      
      //Person must be selected
      if (editing.getPersonId() == null || 
        editing.getPersonId().isEmpty())
      {
        throw new Exception("PERSON_MUST_BE_SELECTED"); 
      }
      
      //Representant deletion when null
      if (editing.getRepresentantPersonId() != null &&
        representantSelectItem == null)
      {
        editing.setRepresentantPersonId(null);
        editing.setRepresentantAddressId(null);
        editing.getRepresentantContactId().clear();
      }
                      
      String caseId = caseBacking.getObjectId();
      editing.setCaseId(caseId);
      
      if (editing.getCasePersonTypeId() == null)
      {
        editing.setCasePersonTypeId(typedHelper.getTypeId());
      }
                  
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeCasePerson(editing);
      
      if (importAddresses)
      {
        importAddressesFromEditingPerson();
        importAddresses = false;
      }      

      cancel();
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
    


  @Override
  public List<CasePersonView> getResults(int firstResult, int maxResults)
  {
    try
    {
      CasePersonFilter filter = new CasePersonFilter();
      filter.setCaseId(caseBacking.getObjectId());        
      filter.setCasePersonTypeId(getTypeId());
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return CaseConfigBean.getPort().findCasePersonViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String store()
  {
    return storePerson();
  }
  
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new CasePerson();
  }
  
  @Override
  public String cancel()
  {
    editing = null;
    contacts = null;
    representantContacts = null;
    personAddresses = null;
    representantAddresses = null;
    personSelectItem = null;
    representantSelectItem = null;
    selectedContacts = null;
    representantContacts = null;
    importAddresses = false;
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
  }
  
  public List<SelectItem> getContactTypeSelectItems()
  {
    if (contactTypeSelectItems == null)
    {
      //TODO: Implement backing      
      PersonContactsBean personContactsBean = 
        WebUtils.getBacking("personContactsBean");
      contactTypeSelectItems = personContactsBean.getAllTypeItems();
      contactTypeSelectItems.add(0, new SelectItem("", " ", ""));
    }
    return contactTypeSelectItems;
  }  
  
  public void onContactSelect(SelectEvent<ContactView> event)
  {
    ContactView contactView = event.getObject();
    if (!isContactsMaxSize(selectedContacts))
      addSelectedPersonContact(contactView);
  }
  
  public void onRepresentantContactSelect(SelectEvent<ContactView> event)
  {
    ContactView contactView = event.getObject();
    if (!isContactsMaxSize(selectedRepresentantContacts))
      addSelectedRepresentantContact(contactView);    
  }
        
  private boolean isNew(CasePerson casePerson)
  {
    return (casePerson != null && casePerson.getCasePersonId() == null);
  }  
    
  private String editPerson(String casePersonId)
  {
    try
    {
      if (casePersonId != null && !isEditing(casePersonId))
      {
        editing = CaseConfigBean.getPort().loadCasePerson(casePersonId);
        loadPersonSelectItem();
        loadPersonContacts();
        loadSelectedContacts();
        loadAddresses();        
      }
      else if (casePersonId == null)
      {
        editing = new CasePerson();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  private void loadPersonSelectItem()
  {
    if (editing != null)
    {
      String personId = editing.getPersonId();
      String representantPersonId = editing.getRepresentantPersonId();
      
      if (personId != null)
        personSelectItem = newPersonSelectItem(personId);
      
      if (representantPersonId != null)
        representantSelectItem = newPersonSelectItem(representantPersonId);
    }
  }  
  
  private SelectItem newPersonSelectItem(String personId)
  {
    PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);    
    
    String description = 
      personBacking.getDescription(personId);
    return new SelectItem(personId, description);    
  }
  
  private void loadPersonContacts()
  {
    if (editing != null)
    {
      contacts = getContacts(editing.getPersonId());
      representantContacts = getContacts(editing.getRepresentantPersonId()); 
    }
  }
  
  private List<ContactView> getContacts(String personId)
  {
    if (personId == null)
      return Collections.emptyList();  

    ContactFilter filter = new ContactFilter();
    filter.setPersonId(personId);

    return KernelConfigBean.getPortAsAdmin().findContactViews(filter);      
  }
  
  private void loadSelectedContacts()
  {
    if (editing != null)
    {
      List<String> ids = editing.getContactId();
      selectedContacts = findContactViews(ids, contacts);
        
      ids = editing.getRepresentantContactId();
      selectedRepresentantContacts = 
        findContactViews(ids, representantContacts);
    }
  }
  
  public List<ContactView> findContactViews(List<String> ids, 
    List<ContactView> list)
  {
    List<ContactView> result = new ArrayList<>();
    for (String id : ids)
    {
      ContactView contactView = findContactViewById(id, list);
      result.add(contactView);        
    } 
    return result;
  }  
  
  public ContactView findContactViewById(String id, List<ContactView> list)
  {
    boolean found = false;
    ContactView entity = null;
    Iterator<ContactView> iter = list.iterator();
    while (!found && iter.hasNext())
    {
      entity = iter.next();
      if (entity.getContactId().equals(id)) found = true;
    }
    return found ? entity : null;
  }     
  
  private List<SelectItem> getAddresses(String addressId, String personId)
  {
    //TODO: AddressBacking
    AddressBean addressBean = WebUtils.getBacking("addressBean");
    return addressBean.getSelectItems(addressId, personId);     
  }
  
  private void loadAddresses()
  {
    if (editing != null)
    {
      personAddresses = getAddresses(
        editing.getAddressId(), 
        editing.getPersonId()); 
      representantAddresses = getAddresses(
        editing.getRepresentantAddressId(), 
        editing.getRepresentantPersonId());
    }
  } 
  
  private void importAddressesFromEditingPerson()
  {
    if (editing != null)
    {
      String personId = editing.getPersonId();
      if (personId != null)
      {
        try
        {
          List<String> addressIdList = getCurrentCaseAddressesList();
          List<PersonAddressView> personAddressViewList =
            getPersonAddressViewList(personId);
          for (PersonAddressView personAddressView : personAddressViewList)
          {
            String addressId = personAddressView.getAddress().getAddressId();
            if (!addressIdList.contains(addressId))
            {
              CaseAddress caseAddress = new CaseAddress();
              caseAddress.setCaseId(caseBacking.getObjectId());
              caseAddress.setAddressId(addressId);
              CaseConfigBean.getPort().storeCaseAddress(caseAddress);
            }
          }
        }
        catch(Exception ex)
        {
          error(ex);
        }
      }
    }
  }  
      
  private List<ContactView> completeContact(String query, 
    List<ContactView> contacts) 
  {
    if (StringUtils.isBlank(query))
      return contacts;
   
    List<ContactView> result = new ArrayList<>();
    
    String queryLowerCase = query.toLowerCase();
    if (contacts != null)
    {
      for (ContactView contact : contacts)
      {
        String contactValue = contact.getValue();
        if (contactValue.toLowerCase().contains(queryLowerCase))
          result.add(contact);
      }
    }
    
    return result;
  }   
  
  private List<String> getCurrentCaseAddressesList()
    throws Exception
  {
    CaseAddressFilter caseAddressFilter = new CaseAddressFilter();
    caseAddressFilter.setCaseId(caseBacking.getObjectId());
    List<CaseAddressView> caseAddressViewList = 
      CaseConfigBean.getPort().findCaseAddressViews(caseAddressFilter);
    List<String> addressIdList = new ArrayList();
    for(CaseAddressView caseAddressView : caseAddressViewList)
    {
      addressIdList.add(caseAddressView.getAddressView().getAddressId());
    }    
    return addressIdList;
  }  
  
  private List<PersonAddressView> getPersonAddressViewList(String personId)
    throws Exception
  {
    PersonAddressFilter filter = new PersonAddressFilter();
    filter.setPersonId(personId);
    return KernelConfigBean.getPortAsAdmin().findPersonAddressViews(filter);
  } 
  
  
  //Add contact
  private boolean sameValue(String value1, String value2)
  {
    String nValue1 = value1.replaceAll(" ", "");
    String nValue2 = value2.replaceAll(" ", "");
    return nValue1.equals(nValue2);
  }  
  
  private ContactView findContactByValue(String contactTypeId, String value,
    List<ContactView> contacts)
  {
    boolean found = false;
    ContactView contact = null;
    Iterator<ContactView> iter = contacts.iterator();
    while (!found && iter.hasNext())
    {
      contact = iter.next();
      if (contact.getContactTypeId().equals(contactTypeId) &&
        sameValue(contact.getValue(), value)) found = true;
    }
    return found ? contact : null;
  }  
  
  private String addNewContact(Contact contact, String personId,
    List<String> contactIds, List<ContactView> contacts)
  {
    String contactId = null;
    try
    {
      String contactTypeId = contact.getContactTypeId();
      String value = contact.getValue().trim();

      if (contactTypeId != null && contactTypeId.length() > 0 &&
          value != null && value.length() > 0 &&
          personId != null && personId.length() > 0)
      {
        ContactView contactView = findContactByValue(contactTypeId, value,
          contacts);

        if (contactView == null)
        {
          // contact do not exists in kernel
          contact.setPersonId(personId);
          Contact newContact = KernelConfigBean.getPortAsAdmin().storeContact(contact);
          contactId = newContact.getContactId();
        }
        else
        {
          // contact already exists in kernel
          contactId = contactView.getContactId();
        }
        
        if (!contactIds.contains(contactId))
        {
          contactIds.add(contactId);
        }
        contact.setContactTypeId(null);
        contact.setPersonId(null);
        contact.setValue(null);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return contactId;
  }    
  
  private void addSelectedPersonContact(ContactView selected)
  {    
    if (selectedContacts == null)
      selectedContacts = new ArrayList<>();
    selectedContacts.add(selected);
    editing.getContactId().add(selected.getContactId());
  }
  
  private void addSelectedRepresentantContact(ContactView selected)
  {    
    if (selectedRepresentantContacts == null)
      selectedRepresentantContacts = new ArrayList<>();
    selectedRepresentantContacts.add(selected);
    editing.getRepresentantContactId().add(selected.getContactId());
  }
    
  private boolean isContactsMaxSize(List<ContactView> contacts)
  {
    return contacts != null && contacts.size() >= 3;
  }  
    
}
