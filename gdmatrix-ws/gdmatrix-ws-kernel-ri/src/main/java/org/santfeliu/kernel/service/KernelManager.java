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
package org.santfeliu.kernel.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressDocument;
import org.matrix.kernel.AddressDocumentFilter;
import org.matrix.kernel.AddressDocumentView;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.City;
import org.matrix.kernel.CityFilter;
import org.matrix.kernel.Contact;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.Country;
import org.matrix.kernel.CountryFilter;
import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.KernelList;
import org.matrix.kernel.KernelListItem;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelMetaData;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.matrix.kernel.PersonDocument;
import org.matrix.kernel.PersonDocumentFilter;
import org.matrix.kernel.PersonDocumentView;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonPerson;
import org.matrix.kernel.PersonPersonFilter;
import org.matrix.kernel.PersonPersonView;
import org.matrix.kernel.PersonRepresentant;
import org.matrix.kernel.PersonRepresentantFilter;
import org.matrix.kernel.PersonRepresentantView;
import org.matrix.kernel.PersonView;
import org.matrix.kernel.Province;
import org.matrix.kernel.ProvinceFilter;
import org.matrix.kernel.Room;
import org.matrix.kernel.RoomFilter;
import org.matrix.kernel.RoomView;
import org.matrix.kernel.Street;
import org.matrix.kernel.StreetFilter;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.InternalValueConverter;
import org.santfeliu.dic.util.WSTypeValidator;
import org.santfeliu.jpa.JPA;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.audit.Auditor;
import org.santfeliu.ws.WSUtils;


/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.kernel.KernelManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class KernelManager implements KernelManagerPort
{
  @Resource
  WebServiceContext wsContext;

  @PersistenceContext
  public EntityManager entityManager;

  private WSEndpoint endpoint;

  private static final String PERSON_CLAUPREF = "PERS    ";
  private static final String PERSON_CLAUCOD = "PERS    ";
  private static final String PERSON_CLAUORIGEN = "NCL ";
  private static final String PERSON_CLAUDESC = "CONTADOR DE PERSONAS";
  
  private static final String ADDRESS_CLAUPREF = "DOM     ";
  private static final String ADDRESS_CLAUCOD = "DOM     ";
  private static final String ADDRESS_CLAUORIGEN = "NCL "; 
  private static final String ADDRESS_CLAUDESC = "CONTADOR DE DIRECCIONES";

  static final String PK_SEPARATOR = ";";

  public KernelMetaData getKernelMetaData()
  {
    KernelMetaData metaData = new KernelMetaData();
    return metaData;
  }

  @Override
  public Person loadPerson(String personId)
  {
    DBPerson dbPerson = entityManager.find(DBPerson.class, personId);
    if (dbPerson == null)
      throw new WebServiceException("kernel:PERSON_NOT_FOUND");
    Person person = new Person();
    dbPerson.copyTo(person);
    return person;
  }

  @Override
  public Person storePerson(Person person)
  {
    person = getWSEndpoint().toLocal(Person.class, person);

    validatePerson(person);
    checkStorePerson(person, KernelConstants.KERNEL_ADMIN_ROLE);
    checkDuplicatedNif(person);
    DBPerson dbPerson = null;
    if (person.getPersonId() == null) // insert new person
    {
      dbPerson = new DBPerson();
      dbPerson.copyFrom(person);
      String personId = String.valueOf(getNextCounterValue(
        PERSON_CLAUPREF,
        PERSON_CLAUCOD,
        PERSON_CLAUORIGEN, 
        PERSON_CLAUDESC));
      dbPerson.setPersonId(personId);
      auditCreation(dbPerson);
      entityManager.persist(dbPerson);
    }
    else // update person
    {
      dbPerson = entityManager.find(DBPerson.class, person.getPersonId());
      dbPerson.copyFrom(person);
      auditUpdate(dbPerson);
      dbPerson = entityManager.merge(dbPerson);
    }
    dbPerson.copyTo(person);

    return getWSEndpoint().toGlobal(Person.class, person);
  }

  @Override
  public boolean removePerson(String personId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
    DBPerson dbPerson = entityManager.getReference(DBPerson.class, personId);
    entityManager.remove(dbPerson);
    return true;
  }

  @Override
  public List<Person> findPersons(PersonFilter filter)
  {
    validatePersonFilter(filter);
    
    Query query = entityManager.createNamedQuery("findPersons");
    setPersonFilterParameters(query, filter);    
    List<DBPerson> dbPersons = query.getResultList();
    List<Person> persons = new ArrayList<Person>();
    for (DBPerson dbPerson : dbPersons)
    {
      Person person = new Person();
      person.setPersonId(dbPerson.getPersonId());
      person.setName(dbPerson.getName());
      person.setFirstParticle(dbPerson.getFirstParticle());
      person.setFirstSurname(dbPerson.getFirstSurname());
      person.setSecondParticle(dbPerson.getSecondParticle());
      person.setSecondSurname(dbPerson.getSecondSurname());
      person.setNif(dbPerson.getNif());
      person.setPassport(dbPerson.getPassport());
      persons.add(person);
    }
    return persons;
  }

  @Override
  public List<PersonView> findPersonViews(PersonFilter filter)
  {
    validatePersonFilter(filter);
    
    Query query = entityManager.createNamedQuery("findPersons");
    setPersonFilterParameters(query, filter);
    List<DBPerson> dbPersons = query.getResultList();
    List<PersonView> personViews = new ArrayList<PersonView>();
    for (DBPerson dbPerson : dbPersons)
    {
      PersonView personView = new PersonView();
      personView.setPersonId(dbPerson.getPersonId());
      personView.setFullName(dbPerson.getFullName());
      personView.setNif(dbPerson.getNif());
      personView.setPassport(dbPerson.getPassport());
      personViews.add(personView);
    }
    return personViews;
  }

  @Override
  public int countPersons(PersonFilter filter)
  {
    Query query = entityManager.createNamedQuery("countPersons");
    setPersonFilterParameters(query, filter);
    query.setFirstResult(0);
    query.setMaxResults(1);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  /* Contact */
  @Override
  public Contact loadContact(String contactId)
  {
    if (contactId.startsWith("1" + PK_SEPARATOR)) // person contact: teleco1
    {
      DBTelecoPK telecoPK = new DBTelecoPK(contactId);
      DBTeleco dbTeleco = entityManager.find(DBTeleco.class, telecoPK);
      if (dbTeleco == null)
        throw new WebServiceException("kernel:CONTACT_NOT_FOUND");
      Contact contact = new Contact();
      dbTeleco.copyTo(contact);
      return contact;
    }
    else
    {
      // teleco2
      return null;
    }
  }

  @Override
  public Contact storeContact(Contact contact)
  {
    validateContact(contact);
    if (contact.getContactId() == null) // create new contact
    {
      if (contact.getAddressId() == null) // teleco1
      {
        String personId = contact.getPersonId();
        Query query =
          entityManager.createNamedQuery("incrementPersonContactCounter");
        query.setParameter("personId", personId);
        query.executeUpdate();
        query = entityManager.createNamedQuery("readPersonContactCounter");
        query.setParameter("personId", personId);
        int contvnum = ((Number)query.getSingleResult()).intValue();
        
        DBTeleco dbTeleco = new DBTeleco();
        dbTeleco.copyFrom(contact);
        dbTeleco.setContactNumber(contvnum);
        auditCreation(dbTeleco);
        entityManager.persist(dbTeleco);
        contact.setContactId("1" + PK_SEPARATOR + personId + 
          PK_SEPARATOR + contvnum);
      }
      else
      {
        // teleco2
      }
    }
    else
    {
      String contactId = contact.getContactId();
      if (contactId.startsWith("1" + PK_SEPARATOR)) // teleco1
      {
        DBTelecoPK dbTelecoPK = new DBTelecoPK(contactId);
        DBTeleco dbTeleco = entityManager.find(DBTeleco.class, dbTelecoPK);
        dbTeleco.copyFrom(contact);
        auditUpdate(dbTeleco);
        entityManager.merge(dbTeleco);
      }
      else
      {
        // teleco2
      }
    }
    return contact;
  }

  @Override
  public boolean removeContact(String contactId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
    if (contactId.startsWith("1" + PK_SEPARATOR)) // teleco1
    {
      DBTelecoPK dbTelecoPK = new DBTelecoPK(contactId);
      DBTeleco dbTeleco =
        entityManager.getReference(DBTeleco.class, dbTelecoPK);
      entityManager.remove(dbTeleco);
    }
    else
    {
      //teleco2
    }
    return true;
  }

  @Override
  public int countContacts(ContactFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<ContactView> findContactViews(ContactFilter filter)
  {
    List<ContactView> contactViews = new ArrayList<ContactView>();
    String personId = filter.getPersonId();
    if ("".equals(personId)) personId = null;
    if (personId != null) // teleco1
    {
      Query query = entityManager.createNamedQuery("findTelecos");
      query.setParameter("personId", personId);
      query.setParameter("contactTypeId", filter.getContactTypeId());

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);

      List<DBTeleco> resultList = query.getResultList();
      for (DBTeleco dbTeleco : resultList)
      {
        ContactView contactView = new ContactView();
        contactView.setContactId("1" + PK_SEPARATOR + dbTeleco.getPersonId() + 
          PK_SEPARATOR + dbTeleco.getContactNumber());
        InternalValueConverter typeIdConverter =
          new InternalValueConverter(DictionaryConstants.CONTACT_TYPE);
        String typeId = typeIdConverter.getTypeId(dbTeleco.getContactTypeId());
        Type type = TypeCache.getInstance().getType(typeId);
        contactView.setContactTypeId(typeId);
        String contactTypeLabel = type != null ? type.getDescription() : typeId;
        contactView.setContactTypeLabel(contactTypeLabel);
        contactView.setValue(dbTeleco.getValue());
        contactView.setComments(dbTeleco.getComments());
        contactViews.add(contactView);
      }
    }
    else
    {
      // teleco2
    }
    return contactViews;
  }


  /* Address */
  @Override
  public Address loadAddress(String addressId)
  {
    DBAddress dbAddress = entityManager.find(DBAddress.class, addressId);
    if (dbAddress == null)
      throw new WebServiceException("kernel:ADDRESS_NOT_FOUND");
    Address address = new Address();
    dbAddress.copyTo(address);
    return address;
  }

  @Override
  public Address storeAddress(Address address)
  {
    address = getWSEndpoint().toLocal(Address.class, address);

    validateAddress(address);
    DBAddress dbAddress = null;
    if (address.getAddressId() == null) // insert new address
    {
      dbAddress = new DBAddress();
      dbAddress.copyFrom(address);
      String addressId = String.valueOf(getNextCounterValue(
        ADDRESS_CLAUPREF,
        ADDRESS_CLAUCOD,
        ADDRESS_CLAUORIGEN, 
        ADDRESS_CLAUDESC));
      dbAddress.setAddressId(addressId);
      auditCreation(dbAddress);
      entityManager.persist(dbAddress);
    }
    else // update address
    {
      dbAddress = entityManager.find(DBAddress.class, address.getAddressId());
      dbAddress.copyFrom(address);
      auditUpdate(dbAddress);
      dbAddress = entityManager.merge(dbAddress);
    }
    dbAddress.copyTo(address);
//    return address;
    return getWSEndpoint().toGlobal(Address.class, address);    
  }

  @Override
  public boolean removeAddress(String addressId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);  
    DBAddress dbAddress = 
      entityManager.getReference(DBAddress.class, addressId);
    entityManager.remove(dbAddress);
    return true;
  }

  @Override
  public List<Address> findAddresses(AddressFilter filter)
  {
    return null;
  }

  @Override
  public List<AddressView> findAddressViews(AddressFilter filter)
  {
    if (filter.getAddressIdList().isEmpty() &&
      StringUtils.isBlank(filter.getAddressTypeId()) &&
      StringUtils.isBlank(filter.getStreetTypeId())&&
      StringUtils.isBlank(filter.getCityName()) &&
      StringUtils.isBlank(filter.getStreetName()) &&
      StringUtils.isBlank(filter.getCountryName()) &&
      StringUtils.isBlank(filter.getDescription()) &&
      StringUtils.isBlank(filter.getNumber()) &&
      StringUtils.isBlank(filter.getGisReference()) &&
      filter.getMaxResults() == 0)
      throw new WebServiceException("FILTER_NOT_ALLOWED");
    
    List<AddressView> addressViews = new ArrayList<AddressView>();
    Query query = entityManager.createNamedQuery("findAddresses");

    setAddressFilterParameters(query, filter);
    
    List<DBAddress> resultList = query.getResultList();
    for (DBAddress dbAddress : resultList)
    {
//        DBAddress dbAddress = (DBAddress) row[0];
      DBStreet dbStreet = dbAddress.getStreet();
      DBCity dbCity = dbStreet.getCity();
      DBProvince dbProvince = dbCity.getProvince();
      DBCountry dbCountry = dbProvince.getCountry();
      
      AddressView addressView = new AddressView();
      addressView.setAddressId(dbAddress.getAddressId());
      String description = describeAddress(dbAddress, dbStreet); 
      addressView.setDescription(description);
      addressView.setCity(dbCity.getName());
      addressView.setProvince(dbProvince.getName());
      addressView.setCountry(dbCountry.getName());
      addressViews.add(addressView);
    }
    return addressViews;
  }

  @Override
  public int countAddresses(AddressFilter filter)
  {
    Query query = entityManager.createNamedQuery("countAddresses");
    setAddressFilterParameters(query, filter);

    query.setFirstResult(0);
    query.setMaxResults(1);

    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }


  /* PersonAddress */
  @Override
  public PersonAddress loadPersonAddress(String personAddressId)
  {
    DBPersonAddressPK pk = new DBPersonAddressPK(personAddressId);
    DBPersonAddress dbPersonAddress = 
      entityManager.find(DBPersonAddress.class, pk);
    if (dbPersonAddress == null)
      throw new WebServiceException("kernel:PERSON_ADDRESS_NOT_FOUND");
    PersonAddress personAddress = new PersonAddress();
    dbPersonAddress.copyTo(personAddress);
    return personAddress;
  }

  @Override
  public PersonAddress storePersonAddress(PersonAddress personAddress)
  {
    validatePersonAddress(personAddress);
    if (personAddress.getPersonAddressId() == null) // insert new
    {
      String personId = personAddress.getPersonId();
      Query query = 
        entityManager.createNamedQuery("incrementPersonAddressCounter");
      query.setParameter("personId", personId);
      query.executeUpdate();
      query = entityManager.createNamedQuery("readPersonAddressCounter");
      query.setParameter("personId", personId);
      int persvnum = ((Number)query.getSingleResult()).intValue();

      DBPersonAddress dbPersonAddress = new DBPersonAddress();
      dbPersonAddress.copyFrom(personAddress);
      dbPersonAddress.setPersnd(persvnum);
      auditCreation(dbPersonAddress);
      entityManager.persist(dbPersonAddress);
      String personAddressId = personId + KernelManager.PK_SEPARATOR + 
        personAddress.getAddressId();
      personAddress.setPersonAddressId(personAddressId);
    }
    else // update
    {
      // TODO: implement update. Updates are unusual
      throw new WebServiceException("NOT_IMPLEMENTED");
    }
    return personAddress;
  }

  @Override
  public boolean removePersonAddress(String personAddressId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
    DBPersonAddressPK pk = new DBPersonAddressPK(personAddressId);
    DBPersonAddress dbPersonAddress = 
      entityManager.getReference(DBPersonAddress.class, pk);
    entityManager.remove(dbPersonAddress);
    return true;
  }

  @Override
  public int countPersonAddresses(PersonAddressFilter filter)
  {
    Query query = entityManager.createNamedQuery("countPersonAddresses");
    query.setParameter("personId", filter.getPersonId());
    query.setParameter("addressId", filter.getAddressId());
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<PersonAddressView> findPersonAddressViews(
    PersonAddressFilter filter)
  {
    if (StringUtils.isBlank(filter.getAddressId()) &&
        StringUtils.isBlank(filter.getPersonId()))
      throw new WebServiceException("FILTER_NOT_ALLOWED");
    
    List<PersonAddressView> personAddressViews = 
      new ArrayList<PersonAddressView>();
    String personId = filter.getPersonId();
    if (personId != null)
    {
      Query query = entityManager.createNamedQuery("findPersonAddresses");
      query.setParameter("id", personId);

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);
      
      List<DBPersonAddress> resultList = query.getResultList();
      for (DBPersonAddress dbPersonAddress : resultList)
      {
        DBAddress dbAddress = dbPersonAddress.getAddress();
        DBStreet dbStreet = dbAddress.getStreet();
        DBCity dbCity = dbStreet.getCity();
        DBProvince dbProvince = dbCity.getProvince();
        DBCountry dbCountry = dbProvince.getCountry();
        
        PersonAddressView personAddressView = new PersonAddressView();
        personAddressView.setPersonAddressId(
          personId + PK_SEPARATOR + dbAddress.getAddressId());
        AddressView addressView = new AddressView();
        String description = describeAddress(dbAddress, dbStreet); 
        addressView.setAddressId(dbAddress.getAddressId());
        addressView.setDescription(description);
        addressView.setCity(dbCity.getName());
        addressView.setProvince(dbProvince.getName());
        addressView.setCountry(dbCountry.getName());
        personAddressView.setAddress(addressView);
        personAddressViews.add(personAddressView);
      }
    }
    else
    {
      String addressId = filter.getAddressId();
      Query query = entityManager.createNamedQuery("findAddressPersons");
      query.setParameter("id", addressId);
      List<DBPerson> resultList = query.getResultList();
      for (DBPerson dbPerson : resultList)
      {
        personId = dbPerson.getPersonId();
        PersonAddressView personAddressView = new PersonAddressView();
        personAddressView.setPersonAddressId(
          personId + PK_SEPARATOR + addressId);
        PersonView personView = new PersonView();
        personView.setPersonId(personId);
        personView.setFullName(dbPerson.getFullName());
        personView.setNif(dbPerson.getNif());
        personView.setPassport(dbPerson.getPassport());
        personAddressView.setPerson(personView);
        personAddressViews.add(personAddressView);
      }
    }
    return personAddressViews;
  }


  /* PersonRepresentant */
  @Override
  public PersonRepresentant loadPersonRepresentant(
    String personRepresentantId)
  {
    DBPersonRepresentantPK pk = 
      new DBPersonRepresentantPK(personRepresentantId);
    DBPersonRepresentant dbPersonRepresentant = 
      entityManager.find(DBPersonRepresentant.class, pk);
    if (dbPersonRepresentant == null)
      throw new WebServiceException("kernel:REPRESENTANT_NOT_FOUND");
    PersonRepresentant personRepresentant = new PersonRepresentant();
    dbPersonRepresentant.copyTo(personRepresentant);
    return personRepresentant;
  }

  @Override
  public PersonRepresentant storePersonRepresentant(
    PersonRepresentant personRepresentant)
  {
    personRepresentant = getWSEndpoint()
      .toLocal(PersonRepresentant.class, personRepresentant);

    validatePersonRepresentant(personRepresentant);
    String personRepresentantId = personRepresentant.getPersonRepresentantId();
    DBPersonRepresentant dbPersonRepresentant = null;
    if (personRepresentantId == null) // new
    {
      dbPersonRepresentant = new DBPersonRepresentant();
      dbPersonRepresentant.copyFrom(personRepresentant);
      auditCreation(dbPersonRepresentant);
      entityManager.persist(dbPersonRepresentant);
    }
    else // update
    {
      dbPersonRepresentant = entityManager.find(DBPersonRepresentant.class, 
        new DBPersonRepresentantPK(personRepresentantId));

      String ids[] = personRepresentantId.split(KernelManager.PK_SEPARATOR);
      if (!personRepresentant.getPersonId().equals(ids[0]) ||
        !personRepresentant.getRepresentantId().equals(ids[1]))
      {
        // primaryKey was updated, reinsert personRepresentant
        DBPersonRepresentant dbPersonRepresentant2 = new DBPersonRepresentant();
        dbPersonRepresentant2.setStddgr(dbPersonRepresentant.getStddgr());
        dbPersonRepresentant2.setStdhgr(dbPersonRepresentant.getStdhgr());
        dbPersonRepresentant2.setStdugr(dbPersonRepresentant.getStdugr());
        dbPersonRepresentant2.copyFrom(personRepresentant);
        auditUpdate(dbPersonRepresentant);
        entityManager.remove(dbPersonRepresentant);
        entityManager.persist(dbPersonRepresentant2);
      }
      else
      {
        dbPersonRepresentant.copyFrom(personRepresentant);
        auditUpdate(dbPersonRepresentant);
        entityManager.merge(dbPersonRepresentant);
      }
    }
    personRepresentant.setPersonRepresentantId(
      personRepresentant.getPersonId() + KernelManager.PK_SEPARATOR + 
      personRepresentant.getRepresentantId());
    
    return getWSEndpoint()
      .toGlobal(PersonRepresentant.class, personRepresentant);
  }

  @Override
  public boolean removePersonRepresentant(String personRepresentantId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);  
    DBPersonRepresentantPK pk = 
      new DBPersonRepresentantPK(personRepresentantId);
    DBPersonRepresentant dbPersonRepresentant = 
      entityManager.getReference(DBPersonRepresentant.class, pk);
    entityManager.remove(dbPersonRepresentant);
    return true;
  }

  @Override
  public List<PersonRepresentantView> findPersonRepresentantViews(
    PersonRepresentantFilter filter)
  {
    List<PersonRepresentantView> personRepresentantViews = 
      new ArrayList<PersonRepresentantView>();
    String personId = filter.getPersonId();
    if (personId != null)
    {
      Query query = entityManager.createNamedQuery("findPersonRepresentants");
      query.setParameter("id", personId);

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);

      List<Object[]> results = query.getResultList();
      for (Object[] row : results)
      {
        DBPersonRepresentant dbPersonRepresentant = 
          (DBPersonRepresentant)row[0];
        DBPerson dbPerson = (DBPerson)row[1];
        String representantId = dbPersonRepresentant.getRepresentantId();

        PersonRepresentantView personRepresentantView =
          new PersonRepresentantView();

        PersonView personView = new PersonView();
        personView.setPersonId(representantId);
        personView.setFullName(dbPerson.getFullName());
        personView.setNif(dbPerson.getNif());
        personView.setPassport(dbPerson.getPassport());

        personRepresentantView.setPersonRepresentantId(
          personId + KernelManager.PK_SEPARATOR + representantId);
        personRepresentantView.setRepresentationTypeId("");
        personRepresentantView.setRepresentant(personView);
        personRepresentantViews.add(personRepresentantView);
      }
    }
    else
    {
    }
    return personRepresentantViews;
  }

  @Override
  public PersonPerson loadPersonPerson(String personPersonId)
  {
    PersonPerson personPerson = null;

    if (personPersonId != null)
    {
      DBPersonPerson dbPersonPerson =
        entityManager.find(DBPersonPerson.class, personPersonId);

      if (dbPersonPerson != null)
      {
        personPerson = new PersonPerson();
        dbPersonPerson.copyTo(personPerson);
        personPerson.setPersonPersonId(personPersonId);
        if (personPerson.getPersonPersonTypeId() == null)
          personPerson.setPersonPersonTypeId(DictionaryConstants.PERSON_PERSON_TYPE);
      }
    }

    return personPerson;
  }

  @Override
  public PersonPerson storePersonPerson(PersonPerson personPerson)
  {
    String personPersonId = personPerson.getPersonPersonId();

    validatePersonPerson(personPerson);

    DBPersonPerson dbPersonPerson = null;
    if (personPersonId == null) //insert
    {
      dbPersonPerson = new DBPersonPerson(personPerson);
      entityManager.persist(dbPersonPerson);
    }
    else //update
    {
      dbPersonPerson = entityManager.merge(new DBPersonPerson(personPerson));
    }

    return dbPersonPerson;
  }

  @Override
  public boolean removePersonPerson(String personPersonId)
  {
    DBPersonPerson dbPersonPerson =
      entityManager.find(DBPersonPerson.class, personPersonId);

    if (dbPersonPerson == null)
      return false;
    entityManager.remove(dbPersonPerson);

    return true;
  }

  @Override
  public List<PersonPersonView> findPersonPersonViews(PersonPersonFilter filter)
  {
    List<PersonPersonView> personPersonViewList = new ArrayList<PersonPersonView>();

    if (filter.getPersonId() != null || filter.getRelPersonId() != null)
    {
      Query query = entityManager.createNamedQuery("findPersonPersons");
      query.setParameter("personId", filter.getPersonId());
      query.setParameter("relPersonId", filter.getRelPersonId());
      List<Object[]> dbList = query.getResultList();
      for (Object[] row : dbList)
      {
        DBPersonPerson dbPersonPerson = (DBPersonPerson)row[0];
        DBPerson dbMainPerson = (DBPerson)row[1];
        DBPerson dbRelPerson = (DBPerson)row[2];

        PersonView mainPersonView = new PersonView();
        mainPersonView.setFullName(dbMainPerson.getFullName());
        mainPersonView.setNif(dbMainPerson.getNif());
        mainPersonView.setPassport(dbMainPerson.getPassport());
        mainPersonView.setPersonId(dbMainPerson.getPersonId());

        PersonView relPersonView = new PersonView();
        relPersonView.setFullName(dbRelPerson.getFullName());
        relPersonView.setNif(dbRelPerson.getNif());
        relPersonView.setPassport(dbRelPerson.getPassport());
        relPersonView.setPersonId(dbRelPerson.getPersonId());

        PersonPersonView personPersonView = new PersonPersonView();
        personPersonView.setPersonView(mainPersonView);
        personPersonView.setRelPersonView(relPersonView);
        personPersonView.setPersonPersonId(dbPersonPerson.getPersonPersonId());
        String personPersonTypeId = dbPersonPerson.getPersonPersonTypeId();
        personPersonView.setPersonPersonTypeId(personPersonTypeId != null ? personPersonTypeId :
          DictionaryConstants.PERSON_PERSON_TYPE);

        personPersonViewList.add(personPersonView);
      }
    }
    return personPersonViewList;
  }

  public int countPersonPersons(PersonPersonFilter filter)
  {
    if (filter.getPersonId() == null && filter.getRelPersonId() == null)
      return 0;
      
    Query query = entityManager.createNamedQuery("countPersonPersons");
    query.setParameter("personId", filter.getPersonId());
    query.setParameter("relPersonId", filter.getRelPersonId());

    query.setFirstResult(0);
    query.setMaxResults(1);

    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  /* Room */
  @Override
  public Room loadRoom(String roomId)
  {
    roomId = getWSEndpoint().toLocalId(Room.class, roomId);

    DBRoomPK dbRoomPK = new DBRoomPK(roomId);
    DBRoom dbRoom = entityManager.find(DBRoom.class, dbRoomPK);
    if (dbRoom == null)
      throw new WebServiceException("kernel:ROOM_NOT_FOUND");
    Room room = new Room();
    dbRoom.copyTo(room);

    return getWSEndpoint().toGlobal(Room.class, room);
  }

  @Override
  public Room storeRoom(Room room)
  {
    room = getWSEndpoint().toLocal(Room.class, room);

    validateRoom(room);

    DBRoom dbRoom = null;
    if (room.getRoomId() == null) // insert new room
    {
      dbRoom = new DBRoom();
      dbRoom.copyFrom(room);

      Query query = entityManager.createNamedQuery("getLastRoomId");
      query.setParameter("addressId", room.getAddressId());
      query.setFirstResult(0);
      query.setMaxResults(1);
      int roomId = 1;
      try
      {
        Number maxRoomId  = (Number)query.getSingleResult();
        roomId = maxRoomId.intValue() + 1;
      }
      catch (Exception ex)
      {
      }
      dbRoom.setRoomId(String.valueOf(roomId));
      entityManager.persist(dbRoom);
    }
    else // update room 
    {
      DBRoomPK roomPK = new DBRoomPK(room.getRoomId());
      if (!roomPK.getAddressId().equals(room.getAddressId()))
        throw new WebServiceException("ADDRESS_UPDATE_NOT_SUPPORTED");
      dbRoom = entityManager.find(DBRoom.class, roomPK);
      dbRoom.copyFrom(room);
      dbRoom = entityManager.merge(dbRoom);
    }
    dbRoom.copyTo(room);

    return getWSEndpoint().toGlobal(Room.class, room);
  }

  @Override
  public boolean removeRoom(String roomId)
  {
    roomId = getWSEndpoint().toLocalId(Room.class, roomId);

    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
    DBRoomPK roomPK = new DBRoomPK(roomId);
    DBRoom dbRoom =
      entityManager.getReference(DBRoom.class, roomPK);
    entityManager.remove(dbRoom);
    return true;
  }

  @Override
  public int countRooms(RoomFilter globalFilter)
  {
    RoomFilter filter = getWSEndpoint().toLocal(RoomFilter.class, globalFilter);
    
    Query query = entityManager.createNamedQuery("countRooms");
    setRoomFilterParameters(query, filter);

    query.setFirstResult(0);
    query.setMaxResults(1);

    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<Room> findRooms(RoomFilter globalFilter)
  {
    RoomFilter filter = getWSEndpoint().toLocal(RoomFilter.class, globalFilter);

    List<Room> rooms = new ArrayList();
    Query query = entityManager.createNamedQuery("findRooms");

    setRoomFilterParameters(query, filter);

    List<DBRoom> dbRooms = query.getResultList();
    if (dbRooms != null && dbRooms.size() > 0)
    {
      for (DBRoom dbRoom : dbRooms)
      {
        Room room = new Room();
        dbRoom.copyTo(room);
        rooms.add(getWSEndpoint().toGlobal(Room.class, room));
      }
    }
    return rooms;
  }

  @Override
  public List<RoomView> findRoomViews(RoomFilter globalFilter)
  {
    RoomFilter filter = getWSEndpoint().toLocal(RoomFilter.class, globalFilter);
    
    List<RoomView> roomViews = new ArrayList();
    Query query = entityManager.createNamedQuery("findRooms");
    setRoomFilterParameters(query, filter);

    List<DBRoom> dbRooms = query.getResultList();
    for (DBRoom dbRoom : dbRooms)
    {
      RoomView roomView = new RoomView();
      dbRoom.copyTo(roomView);
      roomViews.add(getWSEndpoint().toGlobal(RoomView.class, roomView));
    }
    return roomViews;
  }

  /* Country */
  @Override
  public Country loadCountry(String countryId)
  {
    DBCountry dbCountry = entityManager.find(DBCountry.class, countryId);
    if (dbCountry == null)
      throw new WebServiceException("kernel:COUNTRY_NOT_FOUND");
    Country country = new Country();
    dbCountry.copyTo(country);
    return country;
  }

  @Override
  public Country storeCountry(Country country)
  {
    String countryId = country.getCountryId();
    if (countryId == null) // new country
    {
      // TODO:
    }
    else // update
    {
      DBCountry dbCountry = entityManager.find(DBCountry.class, countryId);
      dbCountry.copyFrom(country);
      auditUpdate(dbCountry);
      entityManager.merge(dbCountry);
    }
    return country;
  }

  @Override
  public boolean removeCountry(String countryId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
    DBCountry dbCountry =
      entityManager.getReference(DBCountry.class, countryId);
    entityManager.remove(dbCountry);
    return true;
  }

  @Override
  public int countCountries(CountryFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Country> findCountries(CountryFilter filter)
  {
    List<Country> countries = new ArrayList<Country>();
    Query query = entityManager.createNamedQuery("findCountries");
    String countryName = filter.getCountryName();
    if (countryName != null)
    {
      countryName = countryName.toLowerCase();
    }
    query.setParameter("name", countryName);

    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);

    List<DBCountry> dbCountries = query.getResultList();
    for (DBCountry dbCountry : dbCountries)
    {
      Country country = new Country();
      dbCountry.copyTo(country);
      countries.add(country);
    }
    return countries;
  }


  /* Provinces */
  @Override
  public Province loadProvince(String provinceId)
  {
    DBProvince dbProvince = entityManager.find(DBProvince.class, 
      new DBProvincePK(provinceId));
    if (dbProvince == null)
      throw new WebServiceException("kernel:PROVINCE_NOT_FOUND");
    Province province = new Province();
    dbProvince.copyTo(province);
    return province;
  }

  @Override
  public Province storeProvince(Province province)
  {
    String provinceId = province.getProvinceId();
    if (provinceId == null) // new province
    {
      String countryId = province.getCountryId();
      Query query = entityManager.createNamedQuery("incrementProvinceCounter");
      query.setParameter("countryId", countryId);
      query.executeUpdate();
      query = entityManager.createNamedQuery("readProvinceCounter");
      query.setParameter("countryId", countryId);
      int paisvnum = ((Number)query.getSingleResult()).intValue();
      
      provinceId = province.getCountryId() + KernelManager.PK_SEPARATOR + 
        paisvnum;
      province.setProvinceId(provinceId);
      DBProvince dbProvince = new DBProvince();
      dbProvince.copyFrom(province);
      auditCreation(dbProvince);
      entityManager.persist(dbProvince);
    }
    else // update
    {
      DBProvince dbProvince = entityManager.find(DBProvince.class, 
        new DBProvincePK(provinceId));
      dbProvince.copyFrom(province);
      auditUpdate(dbProvince);
      entityManager.merge(dbProvince);
    }
    return province;
  }

  @Override
  public boolean removeProvince(String provinceId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
    DBProvince dbProvince = entityManager.getReference(DBProvince.class, 
      new DBProvincePK(provinceId));
    entityManager.remove(dbProvince);
    return true;
  }

  @Override
  public int countProvinces(ProvinceFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Province> findProvinces(ProvinceFilter filter)
  {
    List<Province> provinces = new ArrayList<Province>();
    Query query = entityManager.createNamedQuery("findProvinces");
    query.setParameter("countryId", filter.getCountryId());
    String provinceName = filter.getProvinceName();
    if (provinceName != null)
    {
      provinceName = provinceName.toLowerCase();
    }
    query.setParameter("name", provinceName);

    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);

    List<DBProvince> dbProvinces = query.getResultList();
    for (DBProvince dbProvince : dbProvinces)
    {
      Province province = new Province();
      dbProvince.copyTo(province);
      provinces.add(province);
    }
    return provinces;
  }


  /* City */
  @Override
  public City loadCity(String cityId)
  {
    DBCity dbCity = entityManager.find(DBCity.class, 
      new DBCityPK(cityId));
    if (dbCity == null)
      throw new WebServiceException("kernel:CITY_NOT_FOUND");
    City city = new City();
    dbCity.copyTo(city);
    return city;
  }

  @Override
  public City storeCity(City city)
  {
    String cityId = city.getCityId();
    if (cityId == null) // new city
    {
      String provinceId = city.getProvinceId();
      DBProvincePK provincePK = new DBProvincePK(provinceId);

      Query query = entityManager.createNamedQuery("incrementCityCounter");
      query.setParameter("countryId", provincePK.getCountryId());
      query.setParameter("provinceId", provincePK.getProvinceId());
      query.executeUpdate();      
      query = entityManager.createNamedQuery("readCityCounter");
      query.setParameter("countryId", provincePK.getCountryId());
      query.setParameter("provinceId", provincePK.getProvinceId());
      int provvnum = ((Number)query.getSingleResult()).intValue();
      
      cityId = provincePK.getCountryId() + KernelManager.PK_SEPARATOR + 
        provincePK.getProvinceId() + KernelManager.PK_SEPARATOR + 
        provvnum;
      city.setCityId(cityId);
      DBCity dbCity = new DBCity();
      dbCity.copyFrom(city);
      auditCreation(dbCity);
      entityManager.persist(dbCity);
    }
    else // update
    {
      DBCity dbCity = entityManager.find(DBCity.class, 
        new DBCityPK(cityId));
      dbCity.copyFrom(city);
      auditUpdate(dbCity);
      entityManager.merge(dbCity);
    }
    return city;
  }

  @Override
  public boolean removeCity(String cityId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
    DBCity dbCity = entityManager.getReference(DBCity.class, 
      new DBCityPK(cityId));
    entityManager.remove(dbCity);
    return true;
  }

  @Override
  public int countCities(CityFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<City> findCities(CityFilter filter)
  {
    List<City> cities = new ArrayList<City>();
    Query query = entityManager.createNamedQuery("findCities");
    String countryId = filter.getCountryId();
    String provinceId = filter.getProvinceId();
    if (provinceId != null)
    {
      String ids[] = provinceId.split(KernelManager.PK_SEPARATOR);
      countryId = ids[0];
      provinceId = ids[1];
    }
    String cityName = filter.getCityName();
    if (cityName != null)
    {
      cityName = cityName.toLowerCase();
    }
    query.setParameter("countryId", countryId);
    query.setParameter("provinceId", provinceId);
    query.setParameter("name", cityName);
    List<DBCity> dbCities = query.getResultList();
    for (DBCity dbCity : dbCities)
    {
      City city = new City();
      dbCity.copyTo(city);
      cities.add(city);
    }
    return cities;
  }


  /* Street */
  @Override
  public Street loadStreet(String streetId)
  {
    DBStreet dbStreet = entityManager.find(DBStreet.class, 
      new DBStreetPK(streetId));
    if (dbStreet == null)
      throw new WebServiceException("kernel:STREET_NOT_FOUND");
    Street street = new Street();
    dbStreet.copyTo(street);
    return street;
  }

  @Override
  public Street storeStreet(Street street)
  {
    String streetId = street.getStreetId();
    if (streetId == null) // new street
    {
      String cityId = street.getCityId();
      DBCityPK cityPK = new DBCityPK(cityId);
      Query query = entityManager.createNamedQuery("incrementStreetCounter");
      query.setParameter("countryId", cityPK.getCountryId());
      query.setParameter("provinceId", cityPK.getProvinceId());
      query.setParameter("cityId", cityPK.getCityId());
      query.executeUpdate();      
      query = entityManager.createNamedQuery("readStreetCounter");
      query.setParameter("countryId", cityPK.getCountryId());
      query.setParameter("provinceId", cityPK.getProvinceId());
      query.setParameter("cityId", cityPK.getCityId());
      int munivnum = ((Number)query.getSingleResult()).intValue();
      
      streetId = cityPK.getCountryId() + KernelManager.PK_SEPARATOR + 
        cityPK.getProvinceId() + KernelManager.PK_SEPARATOR + 
        cityPK.getCityId() + KernelManager.PK_SEPARATOR + munivnum;
      street.setStreetId(streetId);
      DBStreet dbStreet = new DBStreet();
      dbStreet.copyFrom(street);
      auditCreation(dbStreet);
      entityManager.persist(dbStreet);
    }
    else // update
    {
      DBStreet dbStreet = entityManager.find(DBStreet.class, 
        new DBStreetPK(streetId));
      dbStreet.copyFrom(street);
      auditUpdate(dbStreet);
      entityManager.merge(dbStreet);
    }
    return street;
  }

  @Override
  public boolean removeStreet(String streetId)
  {
    checkUserInRole(KernelConstants.KERNEL_ADMIN_ROLE);
    DBStreet dbStreet = entityManager.getReference(DBStreet.class, 
      new DBStreetPK(streetId));
    entityManager.remove(dbStreet);
    return true;
  }

  @Override
  public int countStreets(StreetFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<Street> findStreets(StreetFilter filter)
  {
    List<Street> streets = new ArrayList<Street>();
    Query query = entityManager.createNamedQuery("findStreets");
    String countryId = filter.getCountryId();
    String provinceId = filter.getProvinceId();
    if (provinceId != null)
    {
      String ids[] = provinceId.split(KernelManager.PK_SEPARATOR);
      countryId = ids[0];
      provinceId = ids[1];
    }
    String cityId = filter.getCityId();
    if (cityId != null)
    {
      String ids[] = cityId.split(KernelManager.PK_SEPARATOR);
      countryId = ids[0];
      provinceId = ids[1];
      cityId = ids[2];
    }
    query.setParameter("countryId", countryId);
    query.setParameter("provinceId", provinceId);
    query.setParameter("cityId", cityId);
    String streetName = filter.getStreetName();
    if (streetName != null)
    {
      streetName = streetName.toLowerCase();
    }
    query.setParameter("name", streetName);

    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);

    List<DBStreet> dbStreets = query.getResultList();
    for (DBStreet dbStreet : dbStreets)
    {
      Street street = new Street();
      dbStreet.copyTo(street);
      streets.add(street);
    }
    return streets;
  }


  /* KernelList */
  @Override
  public KernelListItem loadKernelListItem(KernelList list, 
                                           String itemId)
  {
    String listId = getKernelListId(list);
    DBKernelListItemPK pk = new DBKernelListItemPK(listId, itemId);
    DBKernelListItem dbItem = entityManager.find(DBKernelListItem.class, pk);
    if (dbItem == null) return null;
    KernelListItem kernelListItem = new KernelListItem();
    dbItem.copyTo(kernelListItem);
    return kernelListItem;
  }

  @Override
  public KernelListItem storeKernelListItem(KernelList list,
                                            KernelListItem kernelListItem)
  {
    if (kernelListItem.getItemId() == null) // new item
    {      
    }
    else // update
    {
      String listId = getKernelListId(list);
      String itemId = kernelListItem.getItemId();
      DBKernelListItemPK pk = new DBKernelListItemPK(listId, itemId);
      DBKernelListItem dbItem = entityManager.find(DBKernelListItem.class, pk);
      dbItem.copyFrom(kernelListItem);
      auditUpdate(dbItem);
      entityManager.merge(dbItem);
    }
    return kernelListItem;
  }

  @Override
  public boolean removeKernelListItem(KernelList list, String itemId)
  {
    String listId = getKernelListId(list);
    DBKernelListItem dbItem = entityManager.getReference(
      DBKernelListItem.class, new DBKernelListItemPK(listId, itemId));
    entityManager.remove(dbItem);
    return true;
  }

  @Override
  public List<KernelListItem> listKernelListItems(KernelList list)
  {
    List<KernelListItem> kernelListItems = new ArrayList<KernelListItem>();

    String listId = getKernelListId(list);
    int length = getKernelListSize(list);
    Query query = entityManager.createNamedQuery("findKernelListItems");
    query.setParameter("listId", listId);
    List<DBKernelListItem> dbKernelListItems = query.getResultList();
    for (DBKernelListItem dbKernelListItem : dbKernelListItems)
    {
      String itemId = dbKernelListItem.getItemId();
      KernelListItem kernelListItem = new KernelListItem();
      if (itemId.length() > length)
        itemId = itemId.substring(0, length);
      kernelListItem.setItemId(itemId);
      kernelListItem.setLabel(dbKernelListItem.getLabel());
      kernelListItem.setDescription(dbKernelListItem.getDescription());
      kernelListItems.add(kernelListItem);
    }

    return kernelListItems;
  }

  @Override
  public PersonDocument loadPersonDocument(String personDocId)
  {
    DBPersonDocument dbPersonDocument =
      entityManager.find(DBPersonDocument.class, personDocId);

    PersonDocument personDocument = new PersonDocument();
    dbPersonDocument.copyTo(personDocument);

    if (personDocument == null)
      throw new WebServiceException("kernel:PERSON_DOCUMENT_NOT_FOUND");

    return personDocument;
  }

  @Override
  public PersonDocument storePersonDocument(PersonDocument personDocument)
  {
    String userId = getUserId();

    validatePersonDocument(personDocument);

    if (personDocument.getPersonDocId() == null)
      return createPersonDocument(personDocument, userId);

    //Needs to load audit properties
    DBPersonDocument dbPersonDocument =
      entityManager.getReference(DBPersonDocument.class, personDocument.getPersonDocId());
    dbPersonDocument.copyFrom(personDocument);

    Auditor.auditChange(dbPersonDocument, userId);
    entityManager.merge(dbPersonDocument);

    return dbPersonDocument;
  }

  @Override
  public boolean removePersonDocument(String personDocId)
  {
    DBPersonDocument dbDocPerson =
      entityManager.getReference(DBPersonDocument.class, personDocId);
    if (dbDocPerson == null)
      return false;

    entityManager.remove(dbDocPerson);
    return true;
  }

  @Override
  public List<PersonDocumentView> findPersonDocumentViews(PersonDocumentFilter filter)
  {
    if (StringUtils.isBlank(filter.getDocId()) &&
        StringUtils.isBlank(filter.getPersonId()))
      throw new WebServiceException("FILTER_NOT_ALLOWED");
    
    List<PersonDocumentView> personDocViewList = new ArrayList();
    Query query = entityManager.createNamedQuery("findPersonDocuments");
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    query.setParameter("docId", filter.getDocId());
    query.setParameter("version", filter.getVersion());
    query.setParameter("personId", filter.getPersonId());

    List<DBPersonDocument> dbPersonDocList = query.getResultList();
    if (dbPersonDocList != null && !dbPersonDocList.isEmpty())
    {
      HashMap<String, List<PersonDocumentView>> personIdMap = new HashMap();
      for (DBPersonDocument dbPersonDoc : dbPersonDocList)
      {
        PersonDocumentView docPersView = new PersonDocumentView();

        docPersView.setPersonDocId(dbPersonDoc.getPersonDocId());
        docPersView.setPersonDocTypeId(dbPersonDoc.getPersonDocTypeId());

        String personId = dbPersonDoc.getPersonId();
        List<PersonDocumentView> dpvList = personIdMap.get(personId);
        if (dpvList == null)
          dpvList = new ArrayList();
        dpvList.add(docPersView);
        personIdMap.put(personId, dpvList);

        personDocViewList.add(docPersView);
      }

      PersonFilter personFilter = new PersonFilter();
      personFilter.getPersonId().addAll(personIdMap.keySet());
      personFilter.setFirstResult(0);
      personFilter.setMaxResults(0);
      List<PersonView> personViewList = findPersonViews(personFilter);

      for (PersonView personView : personViewList)
      {
        List<PersonDocumentView> dpvList = personIdMap.get(personView.getPersonId());
        for (PersonDocumentView dpv : dpvList)
        {
          if (dpv != null)
            dpv.setPersonView(personView);
        }
      }
    }

    return personDocViewList;
  }

  @Override
  public List<AddressDocumentView> findAddressDocumentViews(AddressDocumentFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public AddressDocument loadAddressDocument(String addressDocId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean removeAddressDocument(String addressDocId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public AddressDocument storeAddressDocument(AddressDocument addressDocument)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /* private methods */
  private WSEndpoint getWSEndpoint()
  {
    if (endpoint == null)
    {
      String endpointName = WSUtils.getServletAdapter(wsContext).getName();
      endpoint = WSDirectory.getInstance().getEndpoint(endpointName);
    }
    return endpoint;
  }

  private String getKernelListId(KernelList list)
  {
    String listId = null;
    switch (list)
    {
      case PERSON_PARTICLE: listId = "PCOG"; break;
      case ADDRESS_PARTICLE: listId = "PCOG"; break;
    }
    return listId;
  }

  private int getKernelListSize(KernelList list)
  {
    int size = 8;
    switch (list)
    {
      case PERSON_PARTICLE: size = 6; break;
      case ADDRESS_PARTICLE: size = 6; break;
    }
    return size;
  }
  
  private String getUserId()
  {
    try
    {
      User user = UserCache.getUser(wsContext);
      return user.getUserId();
    }
    catch (Exception ex)
    {
      throw new WebServiceException(ex);
    }
  }

  private String getDateString(Date date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    return sdf.format(date);
  }
  
  private String getTimeString(Date date)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
    return sdf.format(date);
  }
  
  private void checkStorePerson(Person person, String role)
  {
    Set roles = UserCache.getUser(wsContext).getRoles();
    if (!roles.contains(role))
    {
      String personId = person.getPersonId();
      if (personId != null) // is an update
      {
        String queryString = MatrixConfig.getClassProperty(KernelManager.class,
          "checkStorePersonQuery");
        if (queryString != null && queryString.length() > 0)
        {
          Query query = entityManager.createNativeQuery(queryString);
          query.setParameter(1, personId);
          if (query.getResultList().size() > 0)
          {
            throw new WebServiceException("NOT_AUTHORIZED");
          }
        }
      }
    }
  }

  private void checkDuplicatedNif(Person person)
  {
    String nif = person.getNif();
    if (nif != null)
    {
      Query query = entityManager.createNamedQuery("countPersons");
      PersonFilter filter = new PersonFilter();
      filter.setNif(person.getNif());
      setPersonFilterParameters(query, filter);
      int count = ((Number)query.getSingleResult()).intValue();
      if (count > 0) throw new WebServiceException("kernel:DUPLICATED_NIF");
    }
  }
  
  private void checkUserInRole(String role)
  {
    Set roles = UserCache.getUser(wsContext).getRoles();
    if (!roles.contains(role)) // is not in role
    {
      throw new WebServiceException("NOT_AUTHORIZED");
    }
  }

  private PersonDocument createPersonDocument(PersonDocument personDocument,
    String userId) 
  {
    DBPersonDocument dbPersonDocument = new DBPersonDocument(personDocument);
    Auditor.auditCreation(dbPersonDocument, userId);
    entityManager.persist(dbPersonDocument);

    return dbPersonDocument;
  }
  
  private void checkUserInRoleOrAuthor(String role, DBEntityBase entity)
  {
    User user = UserCache.getUser(wsContext);
    Set<String> roles = user.getRoles();
    if (!roles.contains(role)) // is not in role
    {
      String userId = user.getUserId().trim();
      String lastUserId = entity.getStdumod();
      if (lastUserId != null) lastUserId = lastUserId.trim();
      if (!userId.equals(lastUserId))
      {
        throw new WebServiceException("NOT_AUTHORIZED");
      }
    }
  }
  
  private String addPercent(String text)
  {
    if (text == null) return null;
    if ("".equals(text)) return "";
    return "%" + text.toLowerCase() + "%";
  }

  private String likePattern(String pattern)
  {
    if (pattern.length() == 0) return null;
    StringBuffer buffer = new StringBuffer("% ");
    if (pattern.startsWith("\"") && pattern.endsWith("\""))
    {
      pattern = pattern.substring(1);
      pattern = pattern.substring(0, pattern.length() - 1);
      buffer.append(pattern);
    }
    else
    {
      char[] carray = pattern.toCharArray();
      for (int i = 0; i < carray.length; i++)
      {
        char c = Character.toLowerCase(carray[i]);
        if (c == 'a' || c == 'à' || c == 'á' || 
          c == 'e' || c == 'è' || c == 'é' || 
          c == 'i' || c == 'ì' || c == 'í' || 
          c == 'o' || c == 'ò' || c == 'ó' ||
          c == 'u' || c == 'ù' || c == 'ú' || 
          c == 'ü' || c == 'ï' || c == 'ç')
          {
            c = '_';
          }
        carray[i] = c;
      }
      buffer.append(carray);
    }
    buffer.append(" %");
    return buffer.toString();
  }

  private void auditCreation(DBEntityBase base)
  {
    String userId = getUserId();
    base.setStdugr(userId);
    base.setStdumod(userId);

    Date now = new Date();
    String nowDate = getDateString(now);
    String nowTime = getTimeString(now);
    
    base.setStddgr(nowDate);
    base.setStdhgr(nowTime);
    base.setStddmod(nowDate);
    base.setStdhmod(nowTime);
  }

  private void auditUpdate(DBEntityBase base)
  {
    base.setStdumod(getUserId());
    Date now = new Date();
    base.setStddmod(getDateString(now));
    base.setStdhmod(getTimeString(now));
  }
  
  private String describeAddress(DBAddress dbAddress, DBStreet dbStreet)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(dbStreet.getStreetTypeId());
    buffer.append(" ");
    buffer.append(dbStreet.getName());
    if (dbAddress.getNumber1() != null)
    {
      buffer.append(" ");
      buffer.append(dbAddress.getNumber1());
    }
    if (dbAddress.getBis1() != null)
    {
      buffer.append(" ");
      buffer.append(dbAddress.getBis1());
    }
    if (dbAddress.getBlock() != null)
    {
      buffer.append(" ");
      buffer.append(dbAddress.getBlock());
    }
    if (dbAddress.getStair() != null)
    {
      buffer.append(" ");
      buffer.append(dbAddress.getStair());
    }
    if (dbAddress.getFloor() != null)
    {
      buffer.append(" ");
      buffer.append(dbAddress.getFloor());
    }
    if (dbAddress.getDoor() != null)
    {
      buffer.append(" ");
      buffer.append(dbAddress.getDoor());
    }
    return buffer.toString();
  }
  
  private String getStringFromList(List<String> idList)
  {
    if (idList == null) return null;
    if (idList.isEmpty()) return null;
    StringBuffer buffer = new StringBuffer(" ");
    for (String id : idList)
    {
      buffer.append(id);
      buffer.append(" ");
    }
    return buffer.toString();
  }

  private int getNextCounterValue(String claupref, String claucod, 
    String clauorigen, String claudesc)
  {
    int counter;

    // update counter
    Date now = new Date();
    Query query = entityManager.createNamedQuery("incrementGenericCounter");
    query.setParameter("claupref", claupref);
    query.setParameter("claucod", claucod);
    query.setParameter("clauorigen", clauorigen);
    query.setParameter("userId", getUserId());
    query.setParameter("day", getDateString(now));
    query.setParameter("hour", getTimeString(now));
    int numUpdated = query.executeUpdate();
    
    if (numUpdated == 1)
    {
      query = entityManager.createNamedQuery("readGenericCounter");
      query.setParameter("claupref", claupref);
      query.setParameter("claucod", claucod);
      query.setParameter("clauorigen", clauorigen);
      Number value = (Number)query.getSingleResult();
      counter = value.intValue();
    }
    else // counter row do not exists, then create it.
    {
      counter = 0; // starting value
      DBCounter dbCounter = new DBCounter();
      dbCounter.setClaupref(claupref);
      dbCounter.setClaucod(claucod);
      dbCounter.setClauorigen(clauorigen);
      dbCounter.setClaudesc(claudesc);
      dbCounter.setCounter(counter);
      auditCreation(dbCounter);
      entityManager.persist(dbCounter);
    }
    return counter;
  }

  private void setPersonFilterParameters(Query query, PersonFilter filter)
  {
    List<String> personIdList = filter.getPersonId();
    query.setParameter("idList", getStringFromList(personIdList));
    query.setParameter("name", addPercent(filter.getName()));
    query.setParameter("firstSurname", addPercent(filter.getFirstSurname()));
    query.setParameter("secondSurname", addPercent(filter.getSecondSurname()));
    query.setParameter("fullName" , addPercent(filter.getFullName()));
    query.setParameter("nif", addPercent(filter.getNif()));
    query.setParameter("passport", filter.getPassport() != null ? 
      filter.getPassport().toLowerCase().trim() : null);
    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);
  }

  private void setAddressFilterParameters(Query query, AddressFilter filter)
  {
    List<String> addressIdList = filter.getAddressIdList();
    query.setParameter("idList", getStringFromList(addressIdList));

    String countryName = filter.getCountryName();
    if (countryName != null) countryName =  likePattern(countryName);
    query.setParameter("countryName", countryName);

    String cityName = filter.getCityName();
    if (cityName != null) cityName =  likePattern(cityName);
    query.setParameter("cityName", cityName);

    String streetName = filter.getStreetName();
    if (streetName != null) streetName = likePattern(streetName);
    query.setParameter("streetName", streetName);

    query.setParameter("number", 
      TextUtils.lowerLeftPadding(filter.getNumber(), "0000"));

    query.setParameter("floor", 
      TextUtils.lowerLeftPadding(filter.getFloor(), "00"));

    query.setParameter("door",
      TextUtils.lowerLeftPadding(filter.getDoor(), "00"));

    String gisReference = filter.getGisReference();
    query.setParameter("gisReference", gisReference);

    String comments = filter.getComments();
    if (comments != null) comments = likePattern(comments);
    query.setParameter("comments", comments);
    
    String addressTypeId = filter.getAddressTypeId();
    if (addressTypeId != null)
    {
      InternalValueConverter typeIdConverter = 
        new InternalValueConverter(DictionaryConstants.ADDRESS_TYPE);
      addressTypeId = typeIdConverter.fromTypeId(addressTypeId);
    }
    query.setParameter("addressTypeId", addressTypeId); 
    
    String streetTypeId = filter.getStreetTypeId();
    if (streetTypeId != null)
    {
      streetTypeId = streetTypeId.toLowerCase().trim();
    }
    query.setParameter("streetTypeId", streetTypeId);    

    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);
  }

  private void setRoomFilterParameters(Query query, RoomFilter filter)
  {
    InternalValueConverter typeIdConverter =
      new InternalValueConverter(DictionaryConstants.ROOM_TYPE);

    query.setParameter("addressId", filter.getAddressId());
    query.setParameter("roomName", addPercent(filter.getRoomName()));
    String roomTypeId = 
      (filter.getRoomTypeId() == null || filter.getRoomTypeId().trim().length() == 0) ?
        DictionaryConstants.ROOM_TYPE : filter.getRoomTypeId();
    String internalValue = typeIdConverter.fromTypeId(roomTypeId);
    query.setParameter("roomTypeId",
      DictionaryConstants.ROOM_TYPE.equals(internalValue) ? null : internalValue);
    query.setParameter("capacity", filter.getCapacity());
    query.setParameter("comments" , addPercent(filter.getComments()));
    query.setParameter("spaceId", filter.getSpaceId());
    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);
  }

  private void validatePerson(Person person)
  {
    //Dictionary properties validation
    String personTypeId = person.getPersonTypeId();
    if (personTypeId == null || personTypeId.trim().length() == 0)
      throw new WebServiceException("kernel:TYPEID_IS_MANDATORY");
    personTypeId = getWSEndpoint().toGlobalId(Type.class, personTypeId);
    Type type = TypeCache.getInstance().getType(personTypeId);

    HashSet unvalidable = new HashSet();
    unvalidable.add("personId");

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(person, unvalidable);
  }

  private void validatePersonAddress(PersonAddress personAddress)
  {
    if (personAddress.getPersonId() == null ||
      personAddress.getPersonId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }
    if (personAddress.getAddressId() == null ||
      personAddress.getAddressId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }

    //Dictionary properties validation
    String personAddressTypeId = personAddress.getPersonAddressTypeId();
    personAddressTypeId =
      getWSEndpoint().toGlobalId(Type.class, personAddressTypeId);
    Type type = TypeCache.getInstance().getType(personAddressTypeId);

    HashSet unvalidable = new HashSet();
    unvalidable.add("personAddressId");

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(personAddress, unvalidable);
  }

  private void validatePersonDocument(PersonDocument personDocument)
  {
    if (personDocument.getPersonDocTypeId() == null ||
        personDocument.getPersonDocTypeId().equals(""))
      throw new WebServiceException("doc:INVALID_PERSON_DOCUMENT_TYPE");

    String personDocTypeId =
      getWSEndpoint().toGlobalId(Type.class, personDocument.getPersonDocTypeId());
    org.santfeliu.dic.Type type =
      TypeCache.getInstance().getType(personDocTypeId);

    HashSet unvalidable = new HashSet();
    unvalidable.add("personDocId");

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(personDocument, unvalidable);
  }

  private void validatePersonRepresentant(PersonRepresentant personRepresentant)
  {
    if (personRepresentant.getPersonId() == null ||
      personRepresentant.getPersonId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }
    if (personRepresentant.getRepresentantId() == null ||
      personRepresentant.getRepresentantId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }

    String repTypeId =
      getWSEndpoint().toGlobalId(Type.class, 
      personRepresentant.getRepresentationTypeId());

    org.santfeliu.dic.Type type = TypeCache.getInstance().getType(repTypeId);

    HashSet unvalidable = new HashSet();
    unvalidable.add("personRepresentatId");

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(personRepresentant, unvalidable);

  }

  private void validateContact(Contact contact)
  {
    String contactTypeId =
      getWSEndpoint().toGlobalId(Type.class, contact.getContactTypeId());

    org.santfeliu.dic.Type type =
      TypeCache.getInstance().getType(contactTypeId);

    HashSet unvalidable = new HashSet();
    unvalidable.add("contactId");

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(contact, unvalidable);
  }

  private void validateAddress(Address address)
  {
    if (address.getStreetId() == null ||
      address.getStreetId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }

    if (address.getPostalCode() != null &&
      address.getPostalCode().length() > 0)
    {
      try
      {
        Long.valueOf(address.getPostalCode());
      }
      catch (NumberFormatException ex)
      {
        throw new WebServiceException("VALUE_MUST_BE_NUMBER");
      }      
    }
    if (address.getPostOfficeBox() != null &&
      address.getPostOfficeBox().length() > 0)
    {
      try
      {
        Long.valueOf(address.getPostOfficeBox());
      }
      catch (NumberFormatException ex)
      {
        throw new WebServiceException("VALUE_MUST_BE_NUMBER");
      }
    }

    String addressTypeId = address.getAddressTypeId();
    if (addressTypeId == null ||
      addressTypeId.trim().length() == 0)
    {
      throw new WebServiceException("kernel:TYPEID_IS_MANDATORY");
    }

    addressTypeId = getWSEndpoint().toGlobalId(Type.class, addressTypeId);
    org.santfeliu.dic.Type type = TypeCache.getInstance().getType(addressTypeId);

    HashSet unvalidable = new HashSet();
    unvalidable.add("addressId");

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(address, unvalidable);
  }

  public void validateRoom(Room room)
  {
    if (room.getAddressId() == null ||
      room.getAddressId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }

    String roomTypeId = room.getRoomTypeId();
    if (roomTypeId == null || roomTypeId.trim().length() == 0)
    {
      throw new WebServiceException("kernel:TYPEID_IS_MANDATORY");
    }

    roomTypeId = getWSEndpoint().toGlobalId(Type.class, roomTypeId);
    org.santfeliu.dic.Type type = TypeCache.getInstance().getType(roomTypeId);

    HashSet unvalidable = new HashSet();
    unvalidable.add("roomId");

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(room, unvalidable);
  }



  private void validatePersonPerson(PersonPerson personPerson)
  {
    if (personPerson.getRelPersonId() == null || personPerson.getRelPersonId().equals(""))
      throw new WebServiceException("persons:INVALID_PERSON_PERSON");
    if (personPerson.getPersonId() == null || personPerson.getPersonId().equals(""))
      throw new WebServiceException("persons:INVALID_PERSON_PERSON");
    if (personPerson.getPersonPersonTypeId() == null ||
        personPerson.getPersonPersonTypeId().trim().length() == 0)
      throw new WebServiceException("persons:INVALID_PERSON_PERSON_TYPE");

    String typeId = personPerson.getPersonPersonTypeId();
    typeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, typeId);
    Type type = TypeCache.getInstance().getType(typeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(personPerson, "personPersonId");
  }

  private void validatePersonFilter(PersonFilter filter)
  {
    if (filter.getPersonId().isEmpty() &&
        StringUtils.isBlank(filter.getNif()) &&
        StringUtils.isBlank(filter.getName()) &&
        StringUtils.isBlank(filter.getFirstSurname()) &&
        StringUtils.isBlank(filter.getSecondSurname()) &&
        StringUtils.isBlank(filter.getFullName()) &&
        StringUtils.isBlank(filter.getPassport()) &&
        filter.getMaxResults() == 0)
      throw new WebServiceException("FILTER_NOT_ALLOWED");
  }

}
