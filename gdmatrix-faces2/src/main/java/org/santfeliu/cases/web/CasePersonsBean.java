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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.cases.Case;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.kernel.Contact;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.kernel.web.AddressBean;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.GroupablePageBean;
import org.santfeliu.web.obj.TypifiedPageBean;
import org.santfeliu.web.obj.ExternalEditable;

/**
 *
 * @author unknown
 */
public class CasePersonsBean extends TypifiedPageBean implements ExternalEditable
{
  //Node properties
  @CMSProperty
  public static final String MAX_PERSON_CONTACTS_PROPERTY = "maxPersonContacts";
  @CMSProperty
  public static final String MAX_REPRESENTANT_CONTACTS_PROPERTY = "maxRepresentantContacts";
  
  //Dic Case properties
  public static final String ALL_TYPES_VISIBLE_PROPERTY = "_personsAllTypesVisible";
  public static final String ROW_TYPE_ID_PROPERTY = "_personsRowTypeId";
  public static final String ROOT_TYPE_ID_PROPERTY = "_personRootTypeId";
  public static final String ORDERBY_PROPERTY = "_personsOrderBy";
  public static final String GROUPBY_PROPERTY = "_personsGroupBy";
  public static final String GROUP_SELECTION_MODE_PROPERTY = "_personsGroupSelectionMode";  
  public static final String SHOW_PERSON_DATES_PROPERTY = "_showPersonDates";
  public static final String SHOW_PERSON_COMMENTS_PROPERTY = "_showPersonComments";
  
  private CasePerson editingPerson;
  private List<CasePersonView> rows;
  private boolean importAddresses = false;

  private Contact newPersonContact = new Contact();
  private Contact newRepresentantContact = new Contact();
  private List<ContactView> personContacts;
  private List<ContactView> representantContacts;
  private transient List<SelectItem> contactTypeSelectItems;

  private String lastCasePersonMid = null;
  private int objectPageScroll;  
  
  public CasePersonsBean()
  {
    super(DictionaryConstants.CASE_PERSON_TYPE, "CASE_ADMIN");    
    load();
    lastCasePersonMid = UserSessionBean.getCurrentInstance().getSelectedMid();
  }
  
  public CasePerson getEditingPerson()
  {
    return editingPerson;
  }

  public Contact getNewPersonContact()
  {
    return newPersonContact;
  }

  public Contact getNewRepresentantContact()
  {
    return newRepresentantContact;
  }

  public List<ContactView> getPersonContacts()
  {
    if (personContacts == null)
    {
      String personId = editingPerson.getPersonId();
      List<String> contactIds = editingPerson.getContactId();
      personContacts = findContacts(personId, contactIds);
    }
    return personContacts;
  }

  public List<ContactView> getRepresentantContacts()
  {
    if (representantContacts == null)
    {
      String personId = editingPerson.getRepresentantPersonId();
      List<String> contactIds = editingPerson.getRepresentantContactId();
      representantContacts = findContacts(personId, contactIds);
    }
    return representantContacts;
  }

  public int getNumPersonContacts()
  {
    return editingPerson.getContactId().size();
  }

  public int getMaxPersonContacts()
  {
    return getMaxContacts(MAX_PERSON_CONTACTS_PROPERTY);
  }

  public int getNumRepresentantContacts()
  {
    return editingPerson.getRepresentantContactId().size();
  }

  public int getMaxRepresentantContacts()
  {
    return getMaxContacts(MAX_REPRESENTANT_CONTACTS_PROPERTY);
  }

  public boolean isPersonSelected()
  {
    String personId = editingPerson.getPersonId();
    return personId != null && personId.length() > 0;
  }

  public boolean isRepresentantSelected()
  {
    String personId = editingPerson.getRepresentantPersonId();
    return personId != null && personId.length() > 0;
  }

  public boolean isSelectedRow()
  {
    CasePersonView casePerson = (CasePersonView)getValue("#{row}");
    return editingPerson != null &&
      casePerson.getCasePersonId().equals(editingPerson.getCasePersonId());
  }

  public String getContactDescription()
  {
    ContactView contact = (ContactView)getValue("#{contact}");
    return getContactDescription(contact);
  }

  public String getPersonContactIdDescription()
  {
    String contactId = (String)getValue("#{contactId}");
    ContactView contact = findContactById(contactId, getPersonContacts());
    return getContactDescription(contact);
  }

  public String getRepresentantContactIdDescription()
  {
    String contactId = (String)getValue("#{contactId}");
    ContactView contact = findContactById(contactId, getRepresentantContacts());
    return getContactDescription(contact);
  }

  public void setSelectedPerson(String personId)
  {
    editingPerson.setPersonId(personId);
    selectPerson();
  }

  public void setSelectedRepresentant(String personId)
  {
    editingPerson.setRepresentantPersonId(personId);
    selectRepresentant();
  }

  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(editingPerson.getPersonId());
  }

  public List<SelectItem> getRepresentantPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(editingPerson.getRepresentantPersonId());
  }

  public List<SelectItem> getAddressSelectItems()
  {
    AddressBean addressBean = (AddressBean)getBean("addressBean");
    return addressBean.getSelectItems(
     editingPerson.getAddressId(),
     editingPerson.getPersonId());
  }

  public List<SelectItem> getRepresentantAddressSelectItems()
  {
    AddressBean addressBean = (AddressBean)getBean("addressBean");
    return addressBean.getSelectItems(
      editingPerson.getRepresentantAddressId(),
      editingPerson.getRepresentantPersonId());
  }

  public void setEditingPerson(CasePerson editingPerson)
  {
    this.editingPerson = editingPerson;
  }

  public void setImportAddresses(boolean importAddresses)
  {
    this.importAddresses = importAddresses;
  }

  public boolean isImportAddresses()
  {
    return importAddresses;
  }

  public int getObjectPageScroll()
  {
    return objectPageScroll;
  }

  public void setObjectPageScroll(int objectPageScroll)
  {
    this.objectPageScroll = objectPageScroll;
  }
  
  public boolean isRenderDateFields()
  {    
    String typeId = getObjectBean().getActualTypeId();
    TypeCache typeCache = TypeCache.getInstance();
    Type type = typeCache.getType(typeId);
    String value = getIndexedDicProperty(type, SHOW_PERSON_DATES_PROPERTY, "true");
    return "true".equals(value);
  }

  public boolean isRenderComments()
  {    
    String typeId = getObjectBean().getActualTypeId();
    TypeCache typeCache = TypeCache.getInstance();
    Type type = typeCache.getType(typeId);
    String value = getIndexedDicProperty(type, SHOW_PERSON_COMMENTS_PROPERTY, "false");
    return "true".equals(value);
  }
  
  public Date getCreationDateTime()
  {
    if (editingPerson != null && editingPerson.getCreationDateTime() != null)
      return TextUtils.parseInternalDate(editingPerson.getCreationDateTime());
    else
      return null;
  }

  public Date getChangeDateTime()
  {
    if (editingPerson != null && editingPerson.getChangeDateTime() != null)
      return TextUtils.parseInternalDate(editingPerson.getChangeDateTime());
    else
      return null;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public List<SelectItem> getContactTypeSelectItems()
  {
    if (contactTypeSelectItems == null)
    {
      contactTypeSelectItems =
        (List<SelectItem>)getValue("#{personContactsBean.allTypeItems}");
      contactTypeSelectItems.add(0, new SelectItem("", " ", ""));
    }
    return contactTypeSelectItems;
  }

  public int getContactMaxLength()
  {
    int maxLength = 4000;
    Type contactType =
      TypeCache.getInstance().getType(DictionaryConstants.CONTACT_TYPE);
    if (contactType != null)
    {
      PropertyDefinition pd = contactType.getPropertyDefinition("value");
      if (pd != null) maxLength = pd.getSize();
    }
    return maxLength;
  }
  
  public String getRowTypeDescription()
  {
    CasePersonView casePersonView = (CasePersonView)getValue("#{row}");
    if (casePersonView != null && casePersonView.getCasePersonId() != null)
    {
      String typeId = casePersonView.getCasePersonTypeId();
      if (typeId != null)
      {  
        TypeCache typeCache = TypeCache.getInstance();
        try
        {
          Type type = typeCache.getType(typeId);
          if (type != null)
            return type.getDescription();
        }
        catch (Exception ex)
        {          
        }
      }  
    }
    return "";
  }

  // ***** Actions *****

  public String show()
  {
    objectPageScroll = 0;
    String mid = UserSessionBean.getCurrentInstance().getSelectedMid();
    if (!mid.equals(lastCasePersonMid))
    {
      load();
    }
    lastCasePersonMid = mid;
    return "case_persons";    
  }

  @Override
  public String store()
  {
    if (editingPerson != null)
    {
      storePerson();
    }
    else
    {
      load();
    }
    return show();
  }

  public String addPersonContact()
  {
    addContact(editingPerson.getContactId());
    return null;
  }

  public String removePersonContact()
  {
    String contactId = (String)getValue("#{contactId}");
    editingPerson.getContactId().remove(contactId);
    return null;
  }

  public String moveDownPersonContact()
  {
    moveDownContact(editingPerson.getContactId());
    return null;
  }
  
  public String addRepresentantContact()
  {
    addContact(editingPerson.getRepresentantContactId());
    return null;
  }

  public String removeRepresentantContact()
  {
    String contactId = (String)getValue("#{contactId}");
    editingPerson.getRepresentantContactId().remove(contactId);
    return null;
  }

  public String moveDownRepresentantContact()
  {
    moveDownContact(editingPerson.getRepresentantContactId());
    return null;
  }

  public String showPerson()
  {
    return getControllerBean().showObject("Person",
      (String)getValue("#{row.personView.personId}"));
  }

  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{casePersonsBean.selectedPerson}");
  }

  public String searchRepresentantPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{casePersonsBean.selectedRepresentant}");
  }

  public String searchAddress()
  {
    return getControllerBean().searchObject("Address",
      "#{casePersonsBean.editingPerson.addressId}");
  }

  public String searchRepresentantAddress()
  {
    return getControllerBean().searchObject("Address",
      "#{casePersonsBean.editingPerson.representantAddressId}");
  }

  public String removePerson()
  {
    try
    {
      CasePersonView row = (CasePersonView)getRequestMap().get("row");
      preRemove();
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCasePerson(row.getCasePersonId());
      getControllerBean().clearBean("personCasesBean"); 
      postRemove();
      load();
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
      if (editingPerson.getPersonId() == null || 
        editingPerson.getPersonId().isEmpty())
      {
        throw new Exception("PERSON_MUST_BE_SELECTED");
      }
      
      objectPageScroll = 0;      
      preStore();                
      
      String caseId = getObjectId();
      editingPerson.setCaseId(caseId);
      
      addPersonAddressToKernel(editingPerson.getPersonId(),
        editingPerson.getAddressId());
      addPersonAddressToKernel(editingPerson.getRepresentantPersonId(),
        editingPerson.getRepresentantAddressId());
      
      if (editingPerson.getCasePersonTypeId() == null)
      {
        editingPerson.setCasePersonTypeId(getRootTypeId());
      }
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeCasePerson(editingPerson);
      if (importAddresses)
      {
        importAddressesFromEditingPerson();
        importAddresses = false;
      }
      
      postStore();

      editingPerson = null;      
      getControllerBean().clearBean("personCasesBean");      
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelPerson()
  {
    objectPageScroll = 0;
    editingPerson = null;
    return null;
  }
  
  @Override
  public boolean isModified()
  {
    return editingPerson != null;
  }    

  public String addNewPersonContact()
  {
    addNewContact(newPersonContact,
      editingPerson.getPersonId(),
      editingPerson.getContactId(),
      personContacts);
    personContacts = null;
    return null;
  }

  public String addNewRepresentantContact()
  {
    addNewContact(newRepresentantContact,
      editingPerson.getRepresentantPersonId(), 
      editingPerson.getRepresentantContactId(),
      representantContacts);
    representantContacts = null;
    return null;
  }

  public String selectPerson()
  {
    editingPerson.setAddressId(null);
    editingPerson.getContactId().clear();
    editingPerson.setRepresentantPersonId(null);
    personContacts = null;
    representantContacts = null;
    return null;
  }

  public String selectRepresentant()
  {
    editingPerson.setRepresentantAddressId(null);
    editingPerson.getRepresentantContactId().clear();
    representantContacts = null;
    return null;
  }

  public String createPerson()
  {
    editingPerson = new CasePerson();
    return null;
  }
  
  public String editObject(String objectId)
  {
    return editPerson(objectId);
  }
  
  public String editPerson()
  {
    String casePersonId = null;
    CasePersonView row = (CasePersonView)getValue("#{row}");
    if (row != null)
      casePersonId = row.getCasePersonId();

    return editPerson(casePersonId);
  }
  
  public String editPerson(String casePersonId)
  {
    try
    {
      if (casePersonId != null)
      {
        editingPerson = CaseConfigBean.getPort().loadCasePerson(casePersonId);
      }
      else
      {
        editingPerson = new CasePerson();
      }

      personContacts = null;
      representantContacts = null;
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
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

  // ***** private methods *****

  private void importAddressesFromEditingPerson()
  {
    if (editingPerson != null)
    {
      String personId = editingPerson.getPersonId();
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
              caseAddress.setCaseId(getObjectId());
              caseAddress.setAddressId(addressId);
              CaseConfigBean.getPort().storeCaseAddress(caseAddress);
            }
          }
          getControllerBean().clearBean("caseAddressesBean");
        }
        catch(Exception ex)
        {
          error(ex);
        }
      }
    }
  }

  private List<PersonAddressView> getPersonAddressViewList(String personId)
    throws Exception
  {
    PersonAddressFilter filter = new PersonAddressFilter();
    filter.setPersonId(personId);
    return KernelConfigBean.getPort().findPersonAddressViews(filter);
  }
  
  private List<String> getCurrentCaseAddressesList()
    throws Exception
  {
    CaseAddressFilter caseAddressFilter = new CaseAddressFilter();
    caseAddressFilter.setCaseId(getObjectId());
    List<CaseAddressView> caseAddressViewList = 
      CaseConfigBean.getPort().findCaseAddressViews(caseAddressFilter);
    List<String> addressIdList = new ArrayList();
    for(CaseAddressView caseAddressView : caseAddressViewList)
    {
      addressIdList.add(caseAddressView.getAddressView().getAddressId());
    }    
    return addressIdList;
  }

  protected void load()
  {
    try
    {
      editingPerson = null;
      if (!isNew())
      {        
        CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
        Case cas = caseMainBean.getCase();
        Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
        if (caseType != null)
        {
          loadPropertyDefinitions(caseType);
        }
                
        CasePersonFilter filter = new CasePersonFilter();
        filter.setCaseId(getObjectId());
        filter.setCasePersonTypeId(rowTypeId);
        rows = CaseConfigBean.getPort().findCasePersonViews(filter);
        setGroups(rows, getGroupExtractor());

        Collections.sort(rows, new PropertyComparator());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
  
  @Override
  protected String getRowTypeId(Object row)
  {
    CasePersonView cpRow = (CasePersonView)row;
    return cpRow.getCasePersonTypeId();
  }

  private List<ContactView> findContacts(String personId,
    List<String> contactIds)
  {
    if (personId == null) return Collections.EMPTY_LIST;

    try
    {
      ContactFilter filter = new ContactFilter();
      filter.setPersonId(personId);

      List<ContactView> contacts =
        KernelConfigBean.getPort().findContactViews(filter);

      // look for contactIds in contacts list
      for (String contactId : contactIds)
      {
        ContactView contactView = findContactById(contactId, contacts);
        if (contactView == null)
        {
          Contact contact = null;
          try
          {
            contact = KernelConfigBean.getPort().loadContact(contactId);
          }
          catch (Exception ex)
          {
            // not found
          }
          contactView = new ContactView();
          contactView.setContactId(contactId);
          if (contact == null)
          {
            contactView.setContactTypeId(DictionaryConstants.CONTACT_TYPE);
            contactView.setValue("Id " + contactId);            
          }
          else
          {
            contactView.setContactTypeId(contact.getContactTypeId());
            contactView.setValue(contact.getValue());
          }
          contacts.add(contactView);
        }
      }
      return contacts;
    }
    catch (Exception ex)
    {
      error(ex);
      return Collections.EMPTY_LIST;
    }
  }

  private ContactView findContactById(String contactId,
    List<ContactView> contacts)
  {
    boolean found = false;
    ContactView contact = null;
    Iterator<ContactView> iter = contacts.iterator();
    while (!found && iter.hasNext())
    {
      contact = iter.next();
      if (contact.getContactId().equals(contactId)) found = true;
    }
    return found ? contact : null;
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

  private String getContactDescription(ContactView contact)
  {
    if (contact == null) return null;
    String typeId = contact.getContactTypeId();
    if (typeId == null) return contact.getValue();
    Type type = TypeCache.getInstance().getType(typeId);
    if (type == null) return typeId + ": " + contact.getValue();
    return type.getDescription() + ": " + contact.getValue();
  }

  private void moveDownContact(List<String> contactIds)
  {
    if (contactIds.size() > 1)
    {
      int index1 = (Integer)getValue("#{rowIndex}");
      int index2 = (index1 + 1) % contactIds.size();
      String contactId1 = contactIds.get(index1);
      String contactId2 = contactIds.get(index2);
      contactIds.set(index2, contactId1);
      contactIds.set(index1, contactId2);
    }
  }

  private int getMaxContacts(String property)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor menuItem = userSessionBean.getSelectedMenuItem();
    String value = menuItem.getProperty(property);
    if (value != null)
    {
      try
      {
        return Integer.parseInt(value);
      }
      catch (NumberFormatException ex)
      {
      }
    }
    return Integer.MAX_VALUE;
  }

  private void addContact(List<String> contactIds)
  {
    ContactView contact = (ContactView)getValue("#{contact}");
    String contactId = contact.getContactId();
    if (!contactIds.contains(contactId))
    {
      contactIds.add(contactId);
    }
  }

  private void addPersonAddressToKernel(String personId, String addressId)
  {
    if (personId != null && personId.length() > 0 &&
        addressId != null && addressId.length() > 0)
    {
      KernelManagerPort port = KernelConfigBean.getPort();
      PersonAddressFilter filter = new PersonAddressFilter();
      filter.setPersonId(personId);
      filter.setAddressId(addressId);
      if (port.countPersonAddresses(filter) == 0)
      {
        PersonAddress personAddress = new PersonAddress();
        personAddress.setPersonId(personId);
        personAddress.setAddressId(addressId);
        port.storePersonAddress(personAddress);
      }
    }
  }

  private void addNewContact(Contact contact, String personId,
    List<String> contactIds, List<ContactView> contacts)
  {
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
        String contactId = null;
        if (contactView == null)
        {
          // contact do not exists in kernel
          contact.setPersonId(personId);
          Contact newContact = KernelConfigBean.getPort().storeContact(contact);
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
  }

  private boolean sameValue(String value1, String value2)
  {
    String nValue1 = value1.replaceAll(" ", "");
    String nValue2 = value2.replaceAll(" ", "");
    return nValue1.equals(nValue2);
  }

  private void loadPropertyDefinitions(Type caseType)
  {
    if (caseType != null)
    {
      rootTypeId = getIndexedDicProperty(caseType, ROOT_TYPE_ID_PROPERTY, rootTypeId);
      
      rowTypeId = getIndexedDicProperty(caseType, ROW_TYPE_ID_PROPERTY, null);
      
      String allTypesVisibleString = getIndexedDicProperty(caseType, ALL_TYPES_VISIBLE_PROPERTY, null);
      if (allTypesVisibleString != null)
      {
        allTypesVisible = Boolean.parseBoolean(allTypesVisibleString);
      }
      
      groupBy = getIndexedDicProperty(caseType, GROUPBY_PROPERTY, null);
      
      groupSelectionMode = getIndexedDicProperty(caseType, GROUP_SELECTION_MODE_PROPERTY, null);
      
      String orderByString = getIndexedDicProperty(caseType, ORDERBY_PROPERTY, null);
      if (orderByString != null)
      {
        String[] array = orderByString.split(",");
        if (array != null)
          orderBy = Arrays.asList(array);
      }
    }
  }
  
  public class PropertyComparator extends GroupablePageBean.PropertyComparator
  {
    @Override
    public int compare(Object o1, Object o2)
    {
      int result = 0;
      if (o1 != null && o2 != null)
      {
        CasePersonView cp1 = (CasePersonView)o1;
        CasePersonView cp2 = (CasePersonView)o2;

        result = compareEndDate(cp1.getEndDate(), cp2.getEndDate());
        if (result == 0)
        {
          if (orderBy != null && !orderBy.isEmpty())
            super.compare(cp1, cp2);
        }
      }
      return result;
    }

    private int compareEndDate(String date1, String date2)
    {
      String today = TextUtils.formatDate(new Date(), "yyyyMMdd") + "000000";
      if (date1 != null && date1.compareTo(today) <= 0 && date2 == null)
        return 1;
      else if (date1 == null && date2 != null && date2.compareTo(today) <= 0)
        return -1;
      else
        return 0;
    }
  }
}
