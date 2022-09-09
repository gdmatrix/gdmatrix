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
import javax.faces.component.UIComponent;
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
import org.matrix.pf.web.SelectEntity;
import org.matrix.pf.web.SelectEntityList;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.primefaces.PrimeFaces;
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
@Named("casePersonsBacking")
public class CasePersonsBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{
  private static final String GROUPBY_PROPERTY = "groupBy";
  private static final String CONTACTID = "contactId";
  
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
  private SelectEntityList personContacts;
  private SelectEntityList representantContacts;  
  private SelectEntityList selectedPersonContacts;
  private SelectEntityList selectedRepresentantContacts;  
  private boolean importAddresses = false;
  private List<SelectItem> contactTypeSelectItems;
  private String newContactValue;
  private boolean isRepresentantNewContact;
  private SelectItem newContactType;
  
  public CasePersonsBacking()
  { 
  }
  
  @PostConstruct
  public void init()
  {
    caseBacking = WebUtils.getBacking("caseBacking");   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
    populate();
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

  @Override
  public String getRootTypeId()
  {
    return "CasePerson";
  }
  
  @Override
  public String getTypeId()
  {
    return caseBacking.getPageTypeId();
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
    if (!personId.equals(personSelectItem.getValue()))
      personSelectItem = newPersonSelectItem(personId);
    List<ContactView> contacts = getContacts(personId);
    personContacts = new SelectEntityList(contacts, CONTACTID);
    personAddresses = getAddresses(editing.getAddressId(), personId);
    selectedPersonContacts = null;
    editing.getContactId().clear();
    editing.setAddressId(null);       
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
    List<ContactView> contacts = getContacts(personId);
    representantContacts = new SelectEntityList(contacts, CONTACTID);
    representantAddresses = getAddresses(editing.getRepresentantAddressId(),
      editing.getRepresentantPersonId());
    selectedRepresentantContacts = null;
    editing.getRepresentantContactId().clear();
    editing.setRepresentantAddressId(null);
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
    PersonBacking personBacking = WebUtils.getBacking("personBacking");
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

  //Contacts
  /**
   * @return the full list of all personal contacts, every contact wrapper in a 
   * SelectEntity.
   */
  public List<SelectEntity<ContactView>> getPersonContacts()
  {
    List<SelectEntity<ContactView>> result = personContacts.getEntities();
    Collections.sort(result, (Object o1, Object o2) ->
    {
      if (o1 == null)
        return -1;
      if (o2 == null)
        return 1;
      
      SelectEntity c1 = (SelectEntity) o1;
      SelectEntity c2 = (SelectEntity) o2;
      
      String id1 = c1.getId();
      String id2 = c2.getId();
      
      if (editing != null)
      {
        List<String> ids = editing.getContactId();
        if (ids.contains(id1) && !ids.contains(id2))
          return -1;
        else if (!ids.contains(id1) && ids.contains(id2))
          return 1;              
      }
      
      return id1.compareTo(id2);
    });   
    return result;
  }
 
  /**
   * 
   * @return the selected personal contacts.
   */
  public List<SelectEntity<ContactView>> getSelectedPersonContacts()
  {
    List result = null;
    if (selectedPersonContacts != null)
      result = selectedPersonContacts.getEntities();

    return result;
  }
  
  public List<SelectEntity<ContactView>> getSelectedRepresentantContacts()
  {
    List result = null;    
    if (selectedRepresentantContacts != null)
      result = selectedRepresentantContacts.getEntities();

    return result;
  }    
  
  //SelectMenu setter
  public void setSelectedPersonContacts(List<SelectEntity> selected)
  {
    selectedPersonContacts = new SelectEntityList(selected, CONTACTID);
    editing.getContactId().clear();
    editing.getContactId().addAll(selectedPersonContacts.getIds());
  }
   
  //Autocomplete setter
  public void setSelectedRepresentantContacts(List<SelectEntity> selected)
  {
    selectedRepresentantContacts = new SelectEntityList(selected, CONTACTID);
    editing.getRepresentantContactId().clear();
    editing.getRepresentantContactId().addAll(
      selectedRepresentantContacts.getIds());
  }  
     
  public List<SelectEntity> completePersonContact(String query)
  {
    return completeContact(query, personContacts);
  }

  public  List<SelectEntity> completeRepresentantContact(String query)
  {
    return completeContact(query, representantContacts);
  }
  
  public void onContactValueChange(SelectEvent event)
  {
    Object obj = event.getObject();
    if (!(obj instanceof ContactView))
    {
      String value = String.valueOf(obj);          
    }
    System.out.println(obj + " " + obj.getClass());
  }
    
  public boolean isRenderGroupedResults()
  {
    return tabHelper.getProperty(GROUPBY_PROPERTY) != null;
  }
  
  private List<SelectItem> completePerson(String query, String personId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    PersonBacking personBacking = WebUtils.getBacking("personBacking");
    
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
    return KernelUtils.getContactTypeIconName(contact.getContactTypeId());
  }

  public boolean isImportAddresses()
  {
    return importAddresses;
  }

  public void setImportAddresses(boolean importAddresses)
  {
    this.importAddresses = importAddresses;
  }

  public String getNewContactValue()
  {
    return newContactValue;
  }

  public void setNewContactValue(String newContactValue)
  {
    this.newContactValue = newContactValue;
  }

  public SelectItem getNewContactType()
  {
    return newContactType;
  }

  public void setNewContactType(SelectItem newContactType)
  {
    this.newContactType = newContactType;
  }
    
  
  @Override
  public String show(String pageId)
  {
    editPerson(pageId);
    return show();
  }  
  
  @Override
  public String show()
  {    
    populate();
    return "pf_case_persons";
  }
  
  public String editPerson(CasePersonView row)
  {
    String casePersonId = null;
    if (row != null)
      casePersonId = row.getCasePersonId();

    return editPerson(casePersonId);
  } 
  
  public String createPerson()
  {
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
    personContacts = null;
    representantContacts = null;
    personAddresses = null;
    representantAddresses = null;
    personSelectItem = null;
    representantSelectItem = null;
    selectedPersonContacts = null;
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
  
  public void onContactSelect(SelectEvent<SelectEntity> event)
  {
    Object object = event.getObject();
    if (object instanceof String)
    {
      UIComponent component = event.getComponent();
      String isRepresentantContact = 
        (String) component.getAttributes().get("isRepresentantContact");
      isRepresentantNewContact = Boolean.valueOf(isRepresentantContact);
      
      newContactValue = (String) object;
      PrimeFaces pf = PrimeFaces.current();
      pf.executeScript("PF('contactTypePicker').show();");    
      pf.ajax().update(":mainform:contactTypePicker");    
    }     
  }
  
  public void onContactTypeSelect(SelectEvent<SelectItem> event)
  {
    SelectItem item = (SelectItem) event.getObject();
    if (newContactValue != null)
    {
      String newContactTypeId = (String) item.getValue();
      Contact contact = new Contact();
      contact.setContactTypeId(newContactTypeId);
      contact.setValue(newContactValue);
      
      String personId = isRepresentantNewContact ?
        editing.getRepresentantPersonId() : editing.getPersonId();
      List<String> contactIds = isRepresentantNewContact ? 
        editing.getRepresentantContactId() : editing.getContactId();
      List<ContactView> contacts = isRepresentantNewContact ?
        representantContacts.getOriginals() : personContacts.getOriginals();
      
      String contactId = addNewContact(contact, personId, contactIds, contacts);
      this.loadPersonContacts();
            
      if (isRepresentantNewContact)
      {
        SelectEntity cv = representantContacts.findEntityById(contactId);
        if (selectedRepresentantContacts == null)
          selectedRepresentantContacts = new SelectEntityList(CONTACTID);
        selectedRepresentantContacts.add(CONTACTID, cv.getOriginal());
      }
      else
      {
        SelectEntity cv = personContacts.findEntityById(contactId);
        if (selectedPersonContacts == null)
          selectedPersonContacts = new SelectEntityList(CONTACTID);
        selectedPersonContacts.add(CONTACTID, cv.getOriginal());
      }
    }
    newContactValue = null;
    isRepresentantNewContact = false;
  }  
    
  private boolean isNew(CasePerson casePerson)
  {
    return (casePerson != null && casePerson.getCasePersonId() == null);
  }  
    
  private String editPerson(String casePersonId)
  {
    try
    {
      if (casePersonId != null)
      {
        editing = CaseConfigBean.getPort().loadCasePerson(casePersonId);
        loadPersonSelectItem();
        loadPersonContacts();
        loadSelectedContacts();
        loadAddresses();        
      }
      else
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
    PersonBacking personBacking = WebUtils.getBacking("personBacking");    
    
    String description = 
      personBacking.getDescription(personId);
    return new SelectItem(personId, description);    
  }
  
  private void loadPersonContacts()
  {
    if (editing != null)
    {
      List<ContactView> contacts = getContacts(editing.getPersonId());
      personContacts = new SelectEntityList(contacts, CONTACTID);
      contacts = getContacts(editing.getRepresentantPersonId());
      representantContacts = new SelectEntityList(contacts, CONTACTID);     
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
      selectedPersonContacts = 
        personContacts.findEntities(ids);
        
      ids = editing.getRepresentantContactId();
      selectedRepresentantContacts = 
        representantContacts.findEntities(ids);
    }
  }
  
  private List<SelectItem> getAddresses(String addressId, String personId)
  {
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
      
  private List<SelectEntity> completeContact(String query, 
    SelectEntityList personalContacts) 
  {
    List<SelectEntity> contacts = null;
    if (personalContacts != null)
      contacts = personalContacts.getEntities();
    
    if (StringUtils.isBlank(query))
      return contacts;
   
    List<SelectEntity> result = new ArrayList<>();
    
    String queryLowerCase = query.toLowerCase();
    if (personalContacts != null && contacts != null)
    {
      for (SelectEntity<ContactView> contact : contacts)
      {
        String contactValue = contact.getOriginal().getValue();
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
    
}
