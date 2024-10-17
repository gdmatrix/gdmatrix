package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.PKUtil;
import com.audifilm.matrix.common.service.VersionIdentifier;
import com.audifilm.matrix.common.service.VersionType;
import com.audifilm.matrix.dic.service.address.AddressType;
import com.audifilm.matrix.dic.service.contact.ContactType;
import com.audifilm.matrix.dic.service.person.PersonType;
import static com.audifilm.matrix.dic.service.person.PersonType.Types.E;
import static com.audifilm.matrix.dic.service.person.PersonType.Types.F;
import static com.audifilm.matrix.dic.service.person.PersonType.Types.J;
import static com.audifilm.matrix.dic.service.person.PersonType.Types.R;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import com.audifilm.matrix.util.DateFormat;
import java.text.DecimalFormat;
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
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
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
import org.matrix.util.Entity;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.WSTypeValidator;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.kernel.util.KernelUtils;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.ws.WSUtils;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.MultiInstance;
import static org.matrix.kernel.KernelConstants.KERNEL_ADMIN_ROLE;
import org.santfeliu.ws.annotations.State;

@WebService(endpointInterface = "org.matrix.kernel.KernelManagerPort")
@HandlerChain(file = "handlers.xml")
@MultiInstance
public class KernelManager implements KernelManagerPort
{
  @Resource
  WebServiceContext wsContext;
  @PersistenceContext(unitName = "kernel_g5")
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
  static final String DOC_TYPEID_SEPARATOR = "_";
  private static final String UNKNOWN_DATE = "19691231";
  private static final String NOT_AUTHORIZED = "NOT_AUTHORIZED";

  @State
  private String readRole;
  @State
  private String writeRole;

  @Initializer
  public void initialize(String endpointName)
  {
    readRole =
      MatrixConfig.getProperty("com.audifilm.matrix.kernel.readRole");
    writeRole =
      MatrixConfig.getProperty("com.audifilm.matrix.kernel.writeRole");
  }

  public WSEndpoint getEndpoint()
  {
    if (endpoint == null)
    {
      String endpointName = WSUtils.getServletAdapter(wsContext).getName();
      endpoint = WSDirectory.getInstance().getEndpoint(endpointName);
    }
    return endpoint;
  }

  @Override
  public KernelMetaData getKernelMetaData()
  {
    KernelMetaData metaData = new KernelMetaData();
    return metaData;
  }

  @Override
  public Person loadPerson(String globalPersonId)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    Person person = new Person();

    WSEndpoint ep = getEndpoint();
    VersionIdentifier versionPersonId =
      new VersionIdentifier(ep.getEntity(Person.class), globalPersonId);

    if (!versionPersonId.hasVersion())
    {
      String personId = versionPersonId.getId();
      DBPerson dbPerson = entityManager.find(DBPerson.class, personId);
      if (dbPerson == null)
      {
        throw new WebServiceException("kernel:PERSON_NOT_FOUND");
      }

      dbPerson.copyTo(ep, person);
    }
    else
    {
      String[] ids = versionPersonId.getIds();
      String[] versionId = versionPersonId.getVersionId();

      String caseId = null;
      String personId = null;
      String representantId = null;

      if (versionId == null || versionId.length == 0)
      {
        personId = (ids.length > 0) ? ids[0] : null;
      }
      else
      {
        caseId = versionId[0];
        if (versionId.length == 1)
        {
          personId = (ids.length > 0) ? ids[0] : null;
        }
        else
        {
          personId = versionId[1];
          representantId = (ids.length > 0) ? ids[0] : null;
        }
      }

      String queryName =
        (representantId == null) ? "findFotoPersona" : "findFotoRepresentant";

      Query query = entityManager.createNamedQuery(queryName + "Expedient");
      query.setParameter("caseId", caseId);
      query.setParameter("personId", personId);
      query.setParameter("addressId", null);
      if (representantId != null)
      {
        query.setParameter("representantId", representantId);
      }
      query.setFirstResult(0);
      query.setMaxResults(1);

      List<Object[]> dbPersonList = query.getResultList();
      Object[] dbPerson = (dbPersonList == null || dbPersonList.size() < 1) ?
        null : dbPersonList.get(0);
      if (dbPersonList == null || dbPersonList.size() < 1)
      {
        query = entityManager.createNamedQuery(queryName + "Interessat");
        query.setParameter("caseId", caseId);
        query.setParameter("personId", personId);
        query.setParameter("addressId", null);
        if (representantId != null)
        {
          query.setParameter("representantId", representantId);
        }
        query.setFirstResult(0);
        query.setMaxResults(1);

        dbPersonList = query.getResultList();
        dbPerson = (dbPersonList == null || dbPersonList.size() < 1) ?
          null : dbPersonList.get(0);
      }
      if (dbPerson == null)
      {
        throw new WebServiceException("kernel:PERSON_NOT_FOUND");
      }

      DBFotoPersona foto =
        new DBFotoPersona(versionPersonId.getLocalVersionedId(), dbPerson);

      foto.copyTo(getEndpoint(), person);
    }
    return getEndpoint().toGlobal(Person.class, person);
  }

  @Override
  public Person storePerson(Person globalPerson)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    Person person = getEndpoint().toLocal(Person.class, globalPerson);
    person.setNationalityId(getEndpoint().
      toLocalId(Country.class, globalPerson.getNationalityId()));

    validatePerson(person);
    checkStorePerson(person, KernelConstants.KERNEL_ADMIN_ROLE);

    DBPerson dbPerson;

    if (person.getPersonId() == null) // insert new person
    {
      checkDuplicatedNif(person);

      dbPerson = new DBPerson();
      dbPerson.copyFrom(getEndpoint(),person);
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
      VersionIdentifier personIdVersion = new VersionIdentifier(getEndpoint().
        getEntity(Person.class), person.getPersonId());
      if (personIdVersion.hasVersion())
      {
        // can not modify a version
        return null;
      }

      dbPerson = entityManager.find(DBPerson.class, person.getPersonId());
      if (!StringUtils.equals(dbPerson.getNif(), person.getNif()))
      {
        checkDuplicatedNif(person);
      }

      dbPerson.copyFrom(getEndpoint(), person);
      auditUpdate(dbPerson);
      dbPerson = entityManager.merge(dbPerson);
    }

    storePersonHistoric(dbPerson, dbPerson.getStddmod(), dbPerson.getStdhmod());

    dbPerson.copyTo(getEndpoint(), person);

    return getEndpoint().toGlobal(Person.class, person);
  }

  private void storePersonHistoric(DBPerson dbPerson, String stddmod,
    String stdhmod)
  {
    DBPersonHistoric dbPersonHistoricOld;

    Query query = entityManager.createNamedQuery("findPersonHistoric");
    query.setParameter("personId", dbPerson.getPersonId());
    List<DBPersonHistoric> dbPersonHistoricList = query.getResultList();
    String dataInicial;
    String horaInicial;
    if (dbPersonHistoricList.size() > 0)
    {
      dbPersonHistoricOld = dbPersonHistoricList.get(0);
      dataInicial = dbPersonHistoricOld.getDatainicial();
      horaInicial =  dbPersonHistoricOld.getHorainicial();
    }
    else
    {
      dataInicial = dbPerson.getStddgr() == null ?
        UNKNOWN_DATE : dbPerson.getStddgr();
      horaInicial = dbPerson.getStdhgr() == null ?
        DateFormat.getInstance().formatTime(new Date()):dbPerson.getStdhgr();
    }

    DBPersonHistoric newPersonHistoric = new DBPersonHistoric();
    newPersonHistoric.copyFrom(dbPerson);
    newPersonHistoric.setDatainicial(dataInicial);
    newPersonHistoric.setHorainicial(horaInicial);
    newPersonHistoric.setDatafinal(stddmod);
    newPersonHistoric.setHorafinal(stdhmod);
    auditCreation(newPersonHistoric);
    entityManager.persist(newPersonHistoric);

  }

  /**
   * removePerson
   *
   * No se elimina el registro. Se marca el registro como baja con:
   *     BAIXASW = '1'
   *     VALDATA = 'AAAAMMDD'
   * Se guarda el histórico con BAIXASW='0'
   *
   *
   * @param personId
   * @return
   */
  @Override
  public boolean removePerson(String personId)
  {
    VersionIdentifier versionPersonId = new VersionIdentifier(
      getEndpoint().getEntity(Person.class), personId);
    if (versionPersonId.hasVersion())
    {
      return false;
    }

    if (!isKernelAdmin())
      throw new WebServiceException(NOT_AUTHORIZED);

    DBPerson dbPerson =
      entityManager.getReference(DBPerson.class, versionPersonId.getId());

    DateFormat formatDate = DateFormat.getInstance();
    Date now = new Date();

    storePersonHistoric(dbPerson, formatDate.formatDate(now),
      formatDate.formatTime(now));

    dbPerson.setBaixasw("1");
    dbPerson.setValdata(DateFormat.getInstance().today());

    entityManager.merge(dbPerson);
    return true;
  }

  @Override
  public List<Person> findPersons(PersonFilter globalFilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonFilter filter =
      getEndpoint().toLocal( PersonFilter.class  , globalFilter);

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
      person.setBirthDate(dbPerson.getBirthDate());
      person.setPersonTypeId(DicTypeAdmin.getInstance(PersonType.class)
        .toGlobalId(endpoint, dbPerson.getPersonType()));
      persons.add(getEndpoint().toGlobal(Person.class, person));
    }

    return persons;
  }

  @Override
  public List<PersonView> findPersonViews(PersonFilter globalfilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonFilter filter =
      getEndpoint().toLocal(PersonFilter.class, globalfilter);

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
      personView.setPersonTypeId(DicTypeAdmin.getInstance(PersonType.class)
        .toGlobalId(endpoint, dbPerson.getPersonType()));
      personViews.add(getEndpoint().toGlobal(PersonView.class, personView));
    }
    return personViews;
  }

  @Override
  public int countPersons(PersonFilter globalFilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonFilter filter =
      getEndpoint().toLocal(PersonFilter.class, globalFilter);

    Query query = entityManager.createNamedQuery("countPersons");
    setPersonFilterParameters(query, filter);
    query.setFirstResult(0);
    query.setMaxResults(1);
    Number count = (Number) query.getSingleResult();
    return count.intValue();
  }

  /* Contact */
  //TODO: TELECO2
  @Override
  public Contact loadContact(String globalContactId)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String contactId = getEndpoint().toLocalId(Contact.class, globalContactId);
    if (contactId.startsWith("1" + PK_SEPARATOR)) // person contact: teleco1
    {
      DBTelecoPK telecoPK = new DBTelecoPK(contactId);
      DBTeleco dbTeleco = entityManager.find(DBTeleco.class, telecoPK);
      if (dbTeleco == null)
      {
        throw new WebServiceException("kernel:CONTACT_NOT_FOUND");
      }
      Contact contact = new Contact();
      dbTeleco.copyTo(getEndpoint(), contact);
      return endpoint.toGlobal(Contact.class,contact);
    }
    else
    {
      // teleco2
      return null;
    }
  }

  @Override
  public Contact storeContact(Contact globalContact)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    Contact contact = getEndpoint().toLocal(Contact.class, globalContact);
    validateContact(contact);
    if (contact.getContactId() == null) // create new contact
    {
      if (contact.getAddressId() == null) // teleco1
      {
        String personId = contact.getPersonId();

        //Read contact counter
        Query query = entityManager.createNamedQuery("readPersonContactCounter");
        query.setParameter("personId", personId);
        Object counter = query.getSingleResult();
        int contactNumber = 1;
        if (counter != null)
          contactNumber = ((Number)counter).intValue() + 1;

        DBTeleco dbTeleco = new DBTeleco();
        dbTeleco.copyFrom(getEndpoint(), contact);
        dbTeleco.setContactNumber(contactNumber);
        auditCreation(dbTeleco);
        entityManager.persist(dbTeleco);
        String contactId =
          "1" + PK_SEPARATOR + personId + PK_SEPARATOR + contactNumber;
        contact.setContactId(contactId);
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
        dbTeleco.copyFrom(getEndpoint(), contact);
        auditUpdate(dbTeleco);
        entityManager.merge(dbTeleco);
      }
      else
      {
        // teleco2
      }
    }
    return endpoint.toGlobal(Contact.class, contact);
  }

  @Override
  public boolean removeContact(String globalContactId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String contactId = getEndpoint().toLocalId(Contact.class, globalContactId);
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
  public List<ContactView> findContactViews(ContactFilter globalFilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    ContactFilter filter =
      getEndpoint().toLocal(ContactFilter.class, globalFilter);

    List<ContactView> contactViews = new ArrayList<ContactView>();
    VersionIdentifier vPersonId = new VersionIdentifier(filter.getPersonId());

    String personId = vPersonId.getId();
    if ("".equals(personId))
    {
      personId = null;
    }
    if (personId != null || filter.getValue() != null) // teleco1
    {
      Query query = entityManager.createNamedQuery("findTelecos");
      query.setParameter("personId", personId);
      String contactTypeId = DicTypeAdmin.getInstance(ContactType.class)
        .toLocalId(endpoint, filter.getContactTypeId());
      query.setParameter("contactTypeId", contactTypeId);
      query.setParameter("value", filter.getValue());

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0)
      {
        query.setMaxResults(maxResults);
      }

      List<Object[]> resultList = query.getResultList();
      for (Object[] row : resultList)
      {
        DBTeleco dbTeleco = (DBTeleco) row[0];
        String contactTypeLabel = (String) row[1];
        ContactView contactView = new ContactView();
        contactView.setContactId("1" + PK_SEPARATOR + dbTeleco.getPersonId() +
                PK_SEPARATOR + dbTeleco.getContactNumber());
        String typeId = DicTypeAdmin.getInstance(ContactType.class)
          .toGlobalId(endpoint, dbTeleco.getContactTypeId());
        contactView.setContactTypeId(typeId);
        contactView.setContactTypeLabel(contactTypeLabel);
        contactView.setValue(dbTeleco.getValue());
        contactView.setComments(dbTeleco.getComments());
        PersonView personView = new PersonView();
        personView.setPersonId(dbTeleco.getPersonId());
        contactView.setPerson(personView);
        contactViews.add(endpoint.toGlobal(ContactView.class, contactView));
      }
    }
    else
    {
      // teleco2
    }
    return contactViews;
  }

  @Override
  public int countContacts(ContactFilter globalFilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    ContactFilter filter =
      getEndpoint().toLocal(ContactFilter.class, globalFilter);
    int count = 0;

    VersionIdentifier vPersonId = new VersionIdentifier(filter.getPersonId());

    String personId = vPersonId.getId();
    if ("".equals(personId))
    {
      personId = null;
    }
    if (personId != null || filter.getValue() != null) // teleco1
    {
      Query query = entityManager.createNamedQuery("countTelecos");
      query.setParameter("personId", personId);
      String contactTypeId = DicTypeAdmin.getInstance(ContactType.class)
        .toLocalId(endpoint, filter.getContactTypeId());
      query.setParameter("contactTypeId", contactTypeId);
      query.setParameter("value", filter.getValue());

      query.setFirstResult(1);
      query.setMaxResults(1);

      Number number = (Number)query.getSingleResult();
      count = number.intValue();
    }
    else
    {
      // teleco2
    }
    return count;
  }


  /* Address */
  @Override
  public Address loadAddress(String globalAddressId)
  {
    VersionIdentifier vAddressId = new VersionIdentifier(
      getEndpoint().getEntity(Address.class), globalAddressId);
    if (!vAddressId.hasVersion())
    {
      DBAddress dbAddress =
        entityManager.find(DBAddress.class, vAddressId.getId());
      if (dbAddress == null)
      {
        throw new WebServiceException("kernel:ADDRESS_NOT_FOUND");
      }
      Address address = new Address();
      dbAddress.copyTo(getEndpoint(), address);
      return getEndpoint().toGlobal(Address.class, address);
    }
    String[] ids = vAddressId.getIds();
    String[] versionId = vAddressId.getVersionId();

    String caseId = null;
    String personId = null;
    String representantId = null;
    String nd = (ids.length == 2) ? ids[1] : null;
    String addressId = addressId = ids[0];

    if (versionId != null && versionId.length > 1)
    {
      caseId = versionId[0];
      personId = versionId[1];
      if (versionId.length > 2)
      {
        representantId = versionId[2];
      }
    }

    String queryName = (representantId == null) ? "findFotoAdrecaPersona" :
      "findFotoAdrecaRepresentant";

    Query query = entityManager.createNamedQuery(queryName + "Expedient");
    query.setParameter("caseId", caseId);
    query.setParameter("personId", personId);
    query.setParameter("addressId", addressId);
    query.setParameter("addressNumber", nd);
    if (representantId != null)
    {
      query.setParameter("representantId", representantId);
    }
    query.setFirstResult(0);
    query.setMaxResults(1);

    List<Object[]> dbPersonList = query.getResultList();
    Object[] dbAddressVersion =
      (dbPersonList == null || dbPersonList.size() < 1) ?
        null : dbPersonList.get(0);
    if (dbPersonList == null || dbPersonList.size() < 1)
    {
      query = entityManager.createNamedQuery(queryName + "Interessat");
      query.setParameter("caseId", caseId);
      query.setParameter("personId", personId);
      query.setParameter("addressId", null);
      query.setParameter("addressNumber", nd);
      if (representantId != null)
      {
        query.setParameter("representantId", representantId);
      }
      query.setFirstResult(0);
      query.setMaxResults(1);

      dbPersonList = query.getResultList();
      dbAddressVersion = (dbPersonList == null || dbPersonList.size() < 1) ?
        null : dbPersonList.get(0);
    }
    if (dbAddressVersion == null)
    {
      throw new WebServiceException("kernel:PERSON_NOT_FOUND");
    }

    Address address = new Address();
    String domicili = (String) dbAddressVersion[0];
    DBAddress dbAddress = (DBAddress) dbAddressVersion[2];
    dbAddress.copyTo(getEndpoint(), address);
    address.setAddressId(vAddressId.getLocalVersionedId());
    address.setComments(domicili);

    return getEndpoint().toGlobal(Address.class, address);
  }

  @Override
  public Address storeAddress(Address globalAddress)
  {
    User user = UserCache.getUser(wsContext);
    if (!isKernelAdmin(user) && !isUserInRole(writeRole, user))
      throw new WebServiceException(NOT_AUTHORIZED);

    checkCity(globalAddress.getStreetId(), user);

    Address address = getEndpoint().toLocal(Address.class, globalAddress);

    VersionIdentifier vId = new VersionIdentifier(address.getAddressId());
    if (vId.hasVersion())
    {
      throw new WebServiceException("NOT_ALLOWED");
    }

    validateAddress(address);
    DBAddress dbAddress;
    if (address.getAddressId() == null) // insert new address
    {
      dbAddress = new DBAddress();
      dbAddress.copyFrom(getEndpoint(), address);
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
      dbAddress.copyFrom(getEndpoint(), address);
      auditUpdate(dbAddress);
      dbAddress = entityManager.merge(dbAddress);
    }
    dbAddress.copyTo(getEndpoint(), address);
    return endpoint.toGlobal(Address.class,address);
  }

  @Override
  public boolean removeAddress(String globalAddressId)
  {
    Entity entity = getEndpoint().getEntity(Address.class);
    VersionIdentifier vId = new VersionIdentifier(entity, globalAddressId);
    if (vId.hasVersion())
    {
      return false;
    }

    if (!isKernelAdmin())
      throw new WebServiceException(NOT_AUTHORIZED);

    DBAddress dbAddress =
            entityManager.getReference(DBAddress.class, vId.getId());
    entityManager.remove(dbAddress);
    return true;
  }

  @Override
  public List<Address> findAddresses(AddressFilter globalFilter)
  {
    if (globalFilter.getAddressIdList().isEmpty() &&
      StringUtils.isBlank(globalFilter.getAddressTypeId()) &&
      StringUtils.isBlank(globalFilter.getStreetTypeId())&&
      StringUtils.isBlank(globalFilter.getCityName()) &&
      StringUtils.isBlank(globalFilter.getStreetName()) &&
      StringUtils.isBlank(globalFilter.getCountryName()) &&
      StringUtils.isBlank(globalFilter.getDescription()) &&
      StringUtils.isBlank(globalFilter.getNumber()) &&
      StringUtils.isBlank(globalFilter.getGisReference()) &&
      globalFilter.getMaxResults() == 0)
      throw new WebServiceException("FILTER_NOT_ALLOWED");

    AddressFilter filter =
      getEndpoint().toLocal(AddressFilter.class, globalFilter);

    List<Address> addresses = new ArrayList();
    List<String> simpleAddressIdList = new ArrayList();
    List<String> versionedAddressIdList = new ArrayList();

    for(String addressId : filter.getAddressIdList())
    {
      VersionIdentifier vAddressId = new VersionIdentifier(addressId);
      if (vAddressId.hasVersion())
      {
        versionedAddressIdList.add(addressId);
      }
      else
      {
        simpleAddressIdList.add(addressId);
      }
    }

    if (filter.getAddressIdList().isEmpty() || simpleAddressIdList.size()>0)
    {
      Query query = entityManager.createNamedQuery("findAddresses");
      //Si no es filtra per cap ID llavors no es tenen en compte les versions.
      //Si sí que es filtra per id però son tots versionats llavors no s'ha de
      //fer aquesta cerca.
      filter.getAddressIdList().retainAll(simpleAddressIdList);
      setAddressFilterParameters(query, filter);

      List<DBAddress> resultList = query.getResultList();
      for (DBAddress dbAddress : resultList)
      {
        Address address = new Address();
        dbAddress.copyTo(endpoint, address);
        addresses.add(address);
      }
    }
    if (versionedAddressIdList.size()>0)
    {

      //Si tinc almenys un id versionat -> Buscar Fotos a CASES
      filter.getAddressIdList().clear();
      filter.getAddressIdList().addAll(versionedAddressIdList);

      List<Object[]> resultList = findFotosAdreces(filter);
      for (Object[] row : resultList)
      {
        DBAddress dbAddress = (DBAddress)row[2];
        Address address = new Address();
        dbAddress.copyTo(endpoint, address);
        addresses.add(address);
      }
    }
    return addresses;
  }

  @Override
  public List<AddressView> findAddressViews(AddressFilter globalFilter)
  {
    if (globalFilter.getAddressIdList().isEmpty() &&
      StringUtils.isBlank(globalFilter.getAddressTypeId()) &&
      StringUtils.isBlank(globalFilter.getStreetTypeId())&&
      StringUtils.isBlank(globalFilter.getCityName()) &&
      StringUtils.isBlank(globalFilter.getStreetName()) &&
      StringUtils.isBlank(globalFilter.getCountryName()) &&
      StringUtils.isBlank(globalFilter.getDescription()) &&
      StringUtils.isBlank(globalFilter.getNumber()) &&
      StringUtils.isBlank(globalFilter.getGisReference()) &&
      globalFilter.getMaxResults() == 0)
      throw new WebServiceException("FILTER_NOT_ALLOWED");

    AddressFilter filter =
      getEndpoint().toLocal(AddressFilter.class, globalFilter);

    List<AddressView> addressViews = new ArrayList<AddressView>();
    List<String> simpleAddressIdList = new ArrayList<String>();
    List<String> versionedAddressIdList = new ArrayList<String>();

    for(String addressId : filter.getAddressIdList())
    {
      VersionIdentifier vAddressId = new VersionIdentifier(addressId);
      if (vAddressId.hasVersion())
      {
        versionedAddressIdList.add(addressId);
      }
      else
      {
        simpleAddressIdList.add(addressId);
      }
    }

    if (filter.getAddressIdList().isEmpty() || simpleAddressIdList.size()>0)
    {
      Query query = entityManager.createNamedQuery("findAddresses");
      //Si no es filtra per cap ID llavors no es tenen en compte les versions.
      //Si sí que es filtra per id però son tots versionats llavors no s'ha de
      //fer aquesta cerca.
      filter.getAddressIdList().retainAll(simpleAddressIdList);
      setAddressFilterParameters(query, filter);

      List<DBAddress> resultList = query.getResultList();
      for (DBAddress dbAddress : resultList)
      {
//        DBAddress dbAddress = (DBAddress) row[0];
        AddressView addressView = new AddressView();
        addressView.setAddressId(dbAddress.getAddressId());
        addressView.setAddressTypeId(DicTypeAdmin.getInstance(AddressType.class)
              .toGlobalId(endpoint, dbAddress.getAddressTypeId()));
        getEndpoint().toGlobal(AddressView.class, addressView);
        DBStreet dbStreet = dbAddress.getStreet();
        String description = describeAddress(dbAddress, dbStreet);
        addressView.setDescription(description);
        if (dbStreet != null)
        {
          DBCity dbCity = dbStreet.getCity();
          DBProvince dbProvince = dbCity.getProvince();
          DBCountry dbCountry = dbProvince.getCountry();
          addressView.setCity(dbCity.getName());
          addressView.setProvince(dbProvince.getName());
          addressView.setCountry(dbCountry.getName());
        }
        else
        {
          addressView.setCity("");
          addressView.setProvince("");
          addressView.setCountry("");
        }
        addressViews.add(getEndpoint().toGlobal(AddressView.class, addressView));
      }
    }
    if (versionedAddressIdList.size()>0)
    {

      //Si tinc almenys un id versionat -> Buscar Fotos a CASES
      filter.getAddressIdList().clear();
      filter.getAddressIdList().addAll(versionedAddressIdList);

      List<Object[]> resultList = findFotosAdreces(filter);
      for (Object[] row : resultList)
      {

        //DBPersonAddress dbPersonAddress = (DBPersonAddress)row[0];
        //DBPerson dbPerson = (DBPerson) row[1];
        DBAddress dbAddress = (DBAddress) row[2];
        String descripcio = (String)row[3];
        DBCity dbCity = (DBCity) row[4];
        DBProvince dbProvince = (DBProvince) row[5];
        DBCountry dbCountry = (DBCountry) row[6];

        AddressView addressView = new AddressView();
        addressView.setAddressId(dbAddress.getAddressId());
        addressView.setDescription(descripcio);
        addressView.setCity(dbCity.getName());
        addressView.setProvince(dbProvince.getName());
        addressView.setCountry(dbCountry.getName());
        addressView.setAddressTypeId(DicTypeAdmin.getInstance(AddressType.class)
              .toGlobalId(endpoint, dbAddress.getAddressTypeId()));
        addressViews.add(getEndpoint().toGlobal(AddressView.class, addressView));
      }

    }
    return addressViews;
  }

  @Override
  public int countAddresses(AddressFilter globalFilter)
  {
    AddressFilter filter =
      getEndpoint().toLocal(AddressFilter.class, globalFilter);

    Query query = entityManager.createNamedQuery("countAddresses");
    setAddressFilterParameters(query, filter);

    query.setFirstResult(0);
    query.setMaxResults(1);

    Number count = (Number) query.getSingleResult();
    return count.intValue();
  }


  /* PersonAddress */
  @Override
  public PersonAddress loadPersonAddress(String globalPersonAddressId)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    Entity entity = getEndpoint().getEntity(PersonAddress.class);
    VersionIdentifier vPersonAddressId =
      new VersionIdentifier(entity, globalPersonAddressId);

    DBPersonAddressPK pk = new DBPersonAddressPK(vPersonAddressId.getId());
    DBPersonAddress dbPersonAddress =
            entityManager.find(DBPersonAddress.class, pk);
    if (dbPersonAddress == null)
    {
      throw new WebServiceException("kernel:PERSON_ADDRESS_NOT_FOUND");
    }
    PersonAddress personAddress = new PersonAddress();
    dbPersonAddress.copyTo(getEndpoint(), personAddress);
    return personAddress;
  }

  @Override
  public PersonAddress storePersonAddress(PersonAddress globalPersonAddress)
  {
    User user = UserCache.getUser(wsContext);

    if (!isKernelAdmin(user) && !isUserInRole(writeRole, user))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonAddress personAddress =
      getEndpoint().toLocal(PersonAddress.class, globalPersonAddress);
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
      int persvnum = ((Number) query.getSingleResult()).intValue();

      DBPersonAddress dbPersonAddress = new DBPersonAddress();
      dbPersonAddress.copyFrom(getEndpoint(), personAddress);
      String addressNumber = (new DecimalFormat("#0")).format(persvnum);
      dbPersonAddress.setPersonAddressNumber(addressNumber);

      dbPersonAddress.setAuditoriaCreacio(user.getUserId());

      entityManager.persist(dbPersonAddress);
      String personAddressId = personId + KernelManager.PK_SEPARATOR + persvnum;
      personAddress.setPersonAddressId(personAddressId);
    }
    else // update
    {
      // TODO: implement update. Updates are unusual
      throw new WebServiceException("NOT_IMPLEMENTED");
    }
    return getEndpoint().toGlobal(PersonAddress.class, personAddress);
  }

  @Override
  public boolean removePersonAddress(String globalPersonAddressId)
  {
    Entity entity = getEndpoint().getEntity(PersonAddress.class);
    VersionIdentifier vPersonAddressId =
      new VersionIdentifier(entity, globalPersonAddressId);

    if (vPersonAddressId.hasVersion())
    {
      return false;
    }

    if (!isKernelAdmin())
      throw new WebServiceException(NOT_AUTHORIZED);

    DBPersonAddressPK pk = new DBPersonAddressPK(vPersonAddressId.getId());
    DBPersonAddress dbPersonAddress =
      entityManager.getReference(DBPersonAddress.class, pk);
    entityManager.remove(dbPersonAddress);

    return true;
  }

  @Override
  public List<PersonAddressView> findPersonAddressViews(
          PersonAddressFilter globalFilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonAddressFilter filter =
      getEndpoint().toLocal(PersonAddressFilter.class, globalFilter);
    List<PersonAddressView> personAddressViews =
      new ArrayList<PersonAddressView>();

    String personId = filter.getPersonId();
    String addressId = filter.getAddressId();

    if (StringUtils.isBlank(filter.getAddressId()) &&
        StringUtils.isBlank(filter.getPersonId()))
      throw new WebServiceException("FILTER_NOT_ALLOWED");

    VersionIdentifier vPersonId =
      (personId != null) ? new VersionIdentifier(personId) : null;
    VersionIdentifier vAddressId =
      (addressId != null) ? new VersionIdentifier(addressId) : null;


    if (vPersonId!=null &&  vPersonId.hasVersion()) {
      personId = vPersonId.getId();
    }

    if (vAddressId != null && vAddressId.hasVersion() &&
      vAddressId.getVersionTypeId() != null)
    {
      AddressFilter addressFilter = new AddressFilter();
      addressFilter.getAddressIdList().add(addressId);

      List<Object []> resultList = findFotosAdreces(addressFilter);

      for (Object[] row : resultList)
      {
        //DBPersonAddress dbPersonAddress = (DBPersonAddress) row[0];

        DBPersonAddress dbPersonAddress = (DBPersonAddress)row[0];
        DBPerson dbPerson = (DBPerson) row[1];
        DBAddress dbAddress = (DBAddress) row[2];
        String descripcio = (String)row[3];
        DBCity dbCity = (DBCity) row[4];
        DBProvince dbProvince = (DBProvince) row[5];
        DBCountry dbCountry = (DBCountry) row[6];

        PersonAddressView personAddressView = new PersonAddressView();
        personAddressView.setPersonAddressId(getEndpoint().toGlobalId(
          PersonAddress.class, dbPersonAddress.getPersonAddressId()));

        PersonView personView = new PersonView();
        personView.setPersonId(getEndpoint().toGlobalId(
          PersonView.class, personId));
        personView.setFullName(dbPerson.getFullName());
        personView.setNif(dbPerson.getNif());
        personView.setPassport(dbPerson.getPassport());
        personAddressView.setPerson(personView);

        AddressView addressView = new AddressView();
        addressView.setAddressId(getEndpoint().toGlobalId(
          AddressView.class, dbAddress.getAddressId()));
        addressView.setDescription(descripcio);
        addressView.setCity(dbCity.getName());
        addressView.setProvince(dbProvince.getName());
        addressView.setCountry(dbCountry.getName());
        personAddressView.setAddress(addressView);

        personAddressViews.add(personAddressView);
      }
    }
    else
    {
      Query query = entityManager.createNamedQuery("findPersonAddresses");
      query.setParameter("personId", personId);
      query.setParameter("addressId", addressId);

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0)
      {
        query.setMaxResults(maxResults);
      }

      List<DBPersonAddress> resultList = query.getResultList();
      for (DBPersonAddress dbPersonAddress : resultList)
      {
        DBPerson dbPerson = dbPersonAddress.getPerson();
        DBAddress dbAddress = dbPersonAddress.getAddress();
        PersonAddressView personAddressView = new PersonAddressView();
        personAddressView.setPersonAddressId(getEndpoint().toGlobalId(
          PersonAddress.class, dbPersonAddress.getPersonAddressId()));

        PersonView personView = new PersonView();
        personView.setPersonId(getEndpoint().toGlobalId(PersonView.class,
          dbPerson.getPersonId()));
        personView.setFullName(dbPerson.getFullName());
        personView.setNif(dbPerson.getNif());
        personView.setPassport(dbPerson.getPassport());
        personAddressView.setPerson(personView);

        AddressView addressView = new AddressView();
        DBStreet dbStreet = dbAddress.getStreet();
        String description = describeAddress(dbAddress, dbStreet);
        addressView.setAddressId(getEndpoint().toGlobalId(AddressView.class,
          dbAddress.getAddressId()));
        addressView.setDescription(description);
        if (dbStreet != null)
        {
          DBCity dbCity = dbStreet.getCity();
          DBProvince dbProvince = dbCity.getProvince();
          DBCountry dbCountry = dbProvince.getCountry();
          addressView.setCity(dbCity.getName());
          addressView.setProvince(dbProvince.getName());
          addressView.setCountry(dbCountry.getName());
        }
        else
        {
          addressView.setCity("");
          addressView.setProvince("");
          addressView.setCountry("");
        }
        personAddressView.setAddress(addressView);

        personAddressViews.add(personAddressView);
      }
    }

    return personAddressViews;
  }


  /* PersonRepresentant */
  @Override
  public PersonRepresentant loadPersonRepresentant(
          String globalPersonRepresentantId)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String personRepresentantId = getEndpoint()
      .toLocalId(PersonRepresentant.class, globalPersonRepresentantId);

    DBPersonRepresentantPK pk =
      new DBPersonRepresentantPK(personRepresentantId);
    DBPersonRepresentant dbPersonRepresentant =
      entityManager.find(DBPersonRepresentant.class, pk);

    if (dbPersonRepresentant == null)
    {
      throw new WebServiceException("kernel:REPRESENTANT_NOT_FOUND");
    }

    PersonRepresentant personRepresentant = new PersonRepresentant();
    dbPersonRepresentant.copyTo(getEndpoint(),personRepresentant);
    return getEndpoint().toGlobal(PersonRepresentant.class, personRepresentant);
  }

  @Override
  public PersonRepresentant storePersonRepresentant(
          PersonRepresentant globalPersonRepresentant)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonRepresentant personRepresentant = getEndpoint().toLocal(
      PersonRepresentant.class, globalPersonRepresentant);
    validatePersonRepresentant(personRepresentant);
    String personRepresentantId = personRepresentant.getPersonRepresentantId();
    DBPersonRepresentant dbPersonRepresentant;
    if (personRepresentantId == null) // new
    {
      dbPersonRepresentant = new DBPersonRepresentant();
      dbPersonRepresentant.copyFrom(getEndpoint(), personRepresentant);
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
        dbPersonRepresentant2.copyFrom(getEndpoint(),personRepresentant);
        auditUpdate(dbPersonRepresentant);
        entityManager.remove(dbPersonRepresentant);
        entityManager.persist(dbPersonRepresentant2);
      }
      else
      {
        dbPersonRepresentant.copyFrom(getEndpoint(),personRepresentant);
        auditUpdate(dbPersonRepresentant);
        entityManager.merge(dbPersonRepresentant);
      }
    }
    personRepresentant.setPersonRepresentantId(
            personRepresentant.getPersonId() + KernelManager.PK_SEPARATOR +
            personRepresentant.getRepresentantId());
    return getEndpoint().toGlobal(PersonRepresentant.class, personRepresentant);
  }

  @Override
  public boolean removePersonRepresentant(String globalPersonRepresentantId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String personRepresentantId = getEndpoint().toLocalId(
      PersonRepresentant.class, globalPersonRepresentantId);

    DBPersonRepresentantPK pk =
            new DBPersonRepresentantPK(personRepresentantId);
    DBPersonRepresentant dbPersonRepresentant =
            entityManager.getReference(DBPersonRepresentant.class, pk);
    entityManager.remove(dbPersonRepresentant);

    return true;
  }

  @Override
  public List<PersonRepresentantView> findPersonRepresentantViews(
          PersonRepresentantFilter globalFilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonRepresentantFilter filter = getEndpoint().toLocal(
      PersonRepresentantFilter.class, globalFilter);

    List<PersonRepresentantView> personRepresentantViews =
            new ArrayList<PersonRepresentantView>();
    String personId = filter.getPersonId();
    if (personId != null)
    {
      Query query = entityManager.createNamedQuery("findPersonRepresentants");
      query.setParameter("id", personId);

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0)
      {
        query.setMaxResults(maxResults);
      }

      List<Object[]> results = query.getResultList();
      for (Object[] row : results)
      {
        DBPersonRepresentant dbPersonRepresentant =
                (DBPersonRepresentant) row[0];
        DBPerson dbPerson = (DBPerson) row[1];
        String representantId = dbPersonRepresentant.getRepresentantId();

        PersonRepresentantView personRepresentantView =
                new PersonRepresentantView();

        PersonView personView = new PersonView();
        personView.setPersonId(getEndpoint().toGlobalId(
          Person.class, representantId));
        personView.setFullName(dbPerson.getFullName());
        personView.setNif(dbPerson.getNif());
        personView.setPassport(dbPerson.getPassport());

        personRepresentantView.setPersonRepresentantId(
                PKUtil.makeMatrixPK(
                getEndpoint().getEntity(PersonRepresentant.class),
                personId, representantId));
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


  /* Room */
  @Override
  public Room loadRoom(String globalRoomId)
  {
    String roomId = getEndpoint().toLocalId(Room.class, globalRoomId);

    DBRoomPK dbRoomPK = new DBRoomPK(roomId);
    DBRoom dbRoom = entityManager.find(DBRoom.class, dbRoomPK);
    if (dbRoom == null)
      throw new WebServiceException("kernel:ROOM_NOT_FOUND");
    Room room = new Room();
    dbRoom.copyTo(room);

    return getEndpoint().toGlobal(Room.class, room);
  }

  @Override
  public Room storeRoom(Room globalRoom)
  {
    if (!isKernelAdmin())
      throw new WebServiceException(NOT_AUTHORIZED);

    Room room = getEndpoint().toLocal(Room.class, globalRoom);

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
        String maxRoomId  = (String)query.getSingleResult();
        roomId = Integer.parseInt(maxRoomId) + 1;
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
        throw new WebServiceException("ADDRESS_UPDATE_UNSUPPORTED");
      dbRoom = entityManager.find(DBRoom.class, roomPK);
      dbRoom.copyFrom(room);
      dbRoom = entityManager.merge(dbRoom);
    }
    dbRoom.copyTo(room);

    return getEndpoint().toGlobal(Room.class, room);
  }

  @Override
  public boolean removeRoom(String globalRoomId)
  {
    if (!isKernelAdmin())
      throw new WebServiceException(NOT_AUTHORIZED);

    String roomId = getEndpoint().toLocalId(Room.class, globalRoomId);
    DBRoomPK roomPK = new DBRoomPK(roomId);
    DBRoom dbRoom =
      entityManager.getReference(DBRoom.class, roomPK);
    entityManager.remove(dbRoom);
    return true;
  }

  @Override
  public int countRooms(RoomFilter globalFilter)
  {
    RoomFilter filter = getEndpoint().toLocal(RoomFilter.class, globalFilter);
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
    RoomFilter filter = getEndpoint().toLocal(RoomFilter.class, globalFilter);
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
        rooms.add(getEndpoint().toGlobal(Room.class, room));
      }
    }
    return rooms;
  }

  @Override
  public List<RoomView> findRoomViews(RoomFilter globalFilter)
  {
    RoomFilter filter = getEndpoint().toLocal(RoomFilter.class, globalFilter);
    List<RoomView> roomViews = new ArrayList();
    Query query = entityManager.createNamedQuery("findRooms");
    setRoomFilterParameters(query, filter);

    List<DBRoom> dbRooms = query.getResultList();
    for (DBRoom dbRoom : dbRooms)
    {
      RoomView roomView = new RoomView();
      dbRoom.copyTo(roomView);

      AddressView addressView = new AddressView();
      DBAddress dbAddress = dbRoom.getAddress();
      if (dbAddress != null)
      {
        addressView.setAddressId(dbAddress.getAddressId());
        DBStreet dbStreet = dbAddress.getStreet();
        if (dbStreet != null)
        {
          String description = describeAddress(dbAddress, dbStreet);
          addressView.setDescription(description);
          DBCity dbCity = dbStreet.getCity();
          if (dbCity != null)
          {
            addressView.setCity(dbCity.getName());
            DBProvince dbProvince = dbCity.getProvince();
            if (dbProvince != null)
            {
              addressView.setProvince(dbProvince.getName());
              DBCountry dbCountry = dbProvince.getCountry();
              if (dbCountry != null)
                addressView.setCountry(dbCountry.getName());
            }
          }
        }
      }

      roomView.setAddressView(addressView);
      roomViews.add(getEndpoint().toGlobal(RoomView.class, roomView));
    }
    return roomViews;
  }

  /* Country */
  @Override
  public Country loadCountry(String globalCountryId)
  {
    String countryId = getEndpoint().toLocalId(Country.class, globalCountryId);
    DBCountry dbCountry = entityManager.find(DBCountry.class, countryId);
    if (dbCountry == null)
    {
      throw new WebServiceException("kernel:COUNTRY_NOT_FOUND");
    }
    Country country = new Country();
    dbCountry.copyTo(getEndpoint(), country);
    return country;
  }

  @Override
  public Country storeCountry(Country globalCountry)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    Country country = getEndpoint().toLocal(Country.class, globalCountry);
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
    return getEndpoint().toGlobal(Country.class, country);
  }

  @Override
  public boolean removeCountry(String globalCountryId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String countryId = getEndpoint().toLocalId(Country.class, globalCountryId);

    DBCountry dbCountry =
            entityManager.getReference(DBCountry.class, countryId);
    entityManager.remove(dbCountry);

    return true;
  }


  @Override
  public int countCountries(CountryFilter globalFilter)
  {
    CountryFilter filter =
      getEndpoint().toLocal(CountryFilter.class, globalFilter);

    List<Country> countries = new ArrayList<Country>();
    Query query = entityManager.createNamedQuery("countCountries");
    String countryName = filter.getCountryName();
    if (countryName != null)
    {
      countryName = countryName.toLowerCase();
    }
    query.setParameter("name", countryName);

    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0)
    {
      query.setMaxResults(maxResults);
    }

    Number total = (Number)query.getSingleResult();
    return total.intValue();
  }

  @Override
  public List<Country> findCountries(CountryFilter globalFilter)
  {
    CountryFilter filter =
      getEndpoint().toLocal(CountryFilter.class, globalFilter);

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
    if (maxResults > 0)
    {
      query.setMaxResults(maxResults);
    }

    List<DBCountry> dbCountries = query.getResultList();
    for (DBCountry dbCountry : dbCountries)
    {
      Country country = new Country();
      dbCountry.copyTo(getEndpoint(), country);
      countries.add(country);
    }
    return countries;
  }


  /* Provinces */
  @Override
  public Province loadProvince(String globalProvinceId)
  {
    String provinceId =
      getEndpoint().toLocalId(Province.class, globalProvinceId);

    DBProvince dbProvince = entityManager.find(DBProvince.class,
      new DBProvincePK(provinceId));
    if (dbProvince == null)
    {
      throw new WebServiceException("kernel:PROVINCE_NOT_FOUND");
    }
    Province province = new Province();
    dbProvince.copyTo(getEndpoint(), province);
    return province;
  }

  @Override
  public Province storeProvince(Province globalProvince)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    Province province = getEndpoint().toGlobal(Province.class, globalProvince);

    String provinceId = province.getProvinceId();
    if (provinceId == null) // new province
    {
      String countryId = province.getCountryId();
      Query query = entityManager.createNamedQuery("incrementProvinceCounter");
      query.setParameter("countryId", countryId);
      query.executeUpdate();
      query = entityManager.createNamedQuery("readProvinceCounter");
      query.setParameter("countryId", countryId);
      int paisvnum = ((Number) query.getSingleResult()).intValue();

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
    return getEndpoint().toGlobal(Province.class, province);
  }

  @Override
  public boolean removeProvince(String globalProvinceId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String provinceId =
      getEndpoint().toLocalId(Province.class, globalProvinceId);

    DBProvince dbProvince = entityManager.getReference(DBProvince.class,
            new DBProvincePK(provinceId));
    entityManager.remove(dbProvince);
    return true;
  }

  @Override
  public List<Province> findProvinces(ProvinceFilter globalFilter)
  {
    ProvinceFilter filter = getEndpoint().toLocal(
            ProvinceFilter.class, globalFilter);

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
    if (maxResults > 0)
    {
      query.setMaxResults(maxResults);
    }

    List<DBProvince> dbProvinces = query.getResultList();
    for (DBProvince dbProvince : dbProvinces)
    {
      Province province = new Province();
      dbProvince.copyTo(getEndpoint(), province);
      provinces.add(province);
    }
    return provinces;
  }

  @Override
  public int countProvinces(ProvinceFilter globalFilter)
  {
    ProvinceFilter filter = getEndpoint().toLocal(
            ProvinceFilter.class, globalFilter);

    Query query = entityManager.createNamedQuery("countProvinces");
    query.setParameter("countryId", filter.getCountryId());
    String provinceName = filter.getProvinceName();
    if (provinceName != null)
    {
      provinceName = provinceName.toLowerCase();
    }
    query.setParameter("name", provinceName);
    query.setFirstResult(0);
    query.setMaxResults(1);

    Number total = (Number)query.getSingleResult();
    return total.intValue();
  }


  /* City */
  @Override
  public City loadCity(String globalCityId)
  {
    String cityId = getEndpoint().toLocalId(City.class, globalCityId);

    DBCity dbCity = entityManager.find(DBCity.class,
            new DBCityPK(cityId));
    if (dbCity == null)
    {
      throw new WebServiceException("kernel:CITY_NOT_FOUND");
    }

    City city = new City();
    dbCity.copyTo(getEndpoint(), city);
    return city;
  }

  @Override
  public City storeCity(City globalCity)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    City city = getEndpoint().toLocal(City.class, globalCity);

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
      int provvnum = ((Number) query.getSingleResult()).intValue();

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
    return getEndpoint().toGlobal(City.class, city);
  }

  @Override
  public boolean removeCity(String globalCityId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String cityId = getEndpoint().toLocalId(City.class, globalCityId);

    DBCity dbCity = entityManager.getReference(DBCity.class,
            new DBCityPK(cityId));
    entityManager.remove(dbCity);
    return true;
  }

  @Override
  public List<City> findCities(CityFilter globalFilter)
  {
    CityFilter filter = getEndpoint().toLocal(CityFilter.class, globalFilter);

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

    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0)
    {
      query.setMaxResults(maxResults);
    }

    List<DBCity> dbCities = query.getResultList();
    for (DBCity dbCity : dbCities)
    {
      City city = new City();
      dbCity.copyTo(getEndpoint(), city);
      cities.add(city);
    }
    return cities;
  }

  @Override
  public int countCities(CityFilter globalFilter)
  {
    CityFilter filter = getEndpoint().toLocal(CityFilter.class, globalFilter);

    Query query = entityManager.createNamedQuery("countCities");
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

    query.setFirstResult(0);
    query.setMaxResults(1);

    Number result = (Number)query.getSingleResult();
    return result.intValue();
  }


  /* Street */
  @Override
  public Street loadStreet(String globalStreetId)
  {
    String streetId = getEndpoint().toLocalId(Street.class, globalStreetId);

    DBStreet dbStreet = entityManager.find(DBStreet.class,
            new DBStreetPK(streetId));
    if (dbStreet == null)
    {
      throw new WebServiceException("kernel:STREET_NOT_FOUND");
    }
    Street street = new Street();
    dbStreet.copyTo(getEndpoint(), street);
    return street;
  }

  @Override
  public Street storeStreet(Street globalStreet)
  {
    User user = UserCache.getUser(wsContext);
    checkCity(globalStreet.getCityId(), user);

    Street street = getEndpoint().toLocal(Street.class, globalStreet);

    String streetId = street.getStreetId();
    if (streetId == null) // new street
    {
      String cityId = street.getCityId();
      DBCityPK cityPK = new DBCityPK(cityId);
      int munivnum;
      if ("direct".equals(System.getProperty("kernel.munivnum")))
      {
        Query query = entityManager.createNamedQuery("incrementStreetCounter");
        query.setParameter("countryId", cityPK.getCountryId());
        query.setParameter("provinceId", cityPK.getProvinceId());
        query.setParameter("cityId", cityPK.getCityId());
        query.executeUpdate();
        query = entityManager.createNamedQuery("readStreetCounter");
        query.setParameter("countryId", cityPK.getCountryId());
        query.setParameter("provinceId", cityPK.getProvinceId());
        query.setParameter("cityId", cityPK.getCityId());
        munivnum = ((Number) query.getSingleResult()).intValue();
      }
      else // update munivnum with select max
      {
        Query query = entityManager.createQuery(
         "SELECT max(s.streetId) FROM DBStreet s " +
         "WHERE s.countryId = :countryId AND s.provinceId = :provinceId AND " +
         "s.cityId = :cityId");
        query.setParameter("countryId", cityPK.getCountryId());
        query.setParameter("provinceId", cityPK.getProvinceId());
        query.setParameter("cityId", cityPK.getCityId());
        String maxStreetId = (String) query.getSingleResult();
        if (maxStreetId == null)
        {
          munivnum = 1;
        }
        else
        {
          munivnum = Integer.parseInt(maxStreetId) + 1;
        }
        DBCity dbCity = entityManager.find(DBCity.class, cityPK);
        dbCity.setMunivnum(munivnum);
        entityManager.merge(dbCity);
        entityManager.flush();
      }
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
    return getEndpoint().toGlobal(Street.class, street);
  }

  @Override
  public boolean removeStreet(String globalStreetId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String streetId = getEndpoint().toLocalId(Street.class, globalStreetId);
    DBStreet dbStreet = entityManager.getReference(DBStreet.class,
            new DBStreetPK(streetId));
    entityManager.remove(dbStreet);
    return true;
  }

  @Override
  public List<Street> findStreets(StreetFilter globalFilter)
  {
    StreetFilter filter =
      getEndpoint().toLocal(StreetFilter.class, globalFilter);

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
    if (maxResults > 0)
    {
      query.setMaxResults(maxResults);
    }

    List<DBStreet> dbStreets = query.getResultList();
    for (DBStreet dbStreet : dbStreets)
    {
      Street street = new Street();
      dbStreet.copyTo(getEndpoint(), street);
      streets.add(street);
    }
    return streets;
  }

  @Override
  public int countStreets(StreetFilter globalFilter)
  {
    StreetFilter filter =
      getEndpoint().toLocal(StreetFilter.class, globalFilter);

    Query query = entityManager.createNamedQuery("countStreets");
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

    Number result = (Number)query.getSingleResult();
    return result.intValue();
  }

  /* KernelList */
  @Override
  public KernelListItem loadKernelListItem(KernelList list,
          String globalItemId)
  {
    String itemId = getEndpoint().toLocalId(KernelListItem.class, globalItemId);

    String listId = getKernelListId(list);
    DBKernelListItemPK pk = new DBKernelListItemPK(listId, itemId);
    DBKernelListItem dbItem = entityManager.find(DBKernelListItem.class, pk);
    if (dbItem == null)
    {
      return null;
    }
    KernelListItem kernelListItem = new KernelListItem();
    dbItem.copyTo(kernelListItem);
    return kernelListItem;
  }

  @Override
  public KernelListItem storeKernelListItem(KernelList list,
          KernelListItem kernelListItem)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

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
  public boolean removeKernelListItem(KernelList list, String globalItemId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String itemId = getEndpoint().toLocalId(KernelListItem.class, globalItemId);

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
    if (list == KernelList.STREET_TYPE)
    {
      // TODO: DBStreetType
      Query query = entityManager.createNamedQuery("findStreetTypes");
      List<DBStreetType> dbStreetTypes = query.getResultList();
      for (DBStreetType dbStreetType : dbStreetTypes)
      {
        KernelListItem kernelListItem = new KernelListItem();
        kernelListItem.setItemId(dbStreetType.getStreetTypeId());
        kernelListItem.setLabel(dbStreetType.getAbbreviation());
        kernelListItem.setDescription(dbStreetType.getDescription());
        kernelListItems.add(kernelListItem);
      }
    }
    else // look into DBKernelListItem
    {
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
        {
          itemId = itemId.substring(0, length);
        }
        kernelListItem.setItemId(itemId);
        kernelListItem.setLabel(dbKernelListItem.getLabel());
        kernelListItem.setDescription(dbKernelListItem.getDescription());
        kernelListItems.add(kernelListItem);
      }
    }
    return kernelListItems;
  }

  @Override
  public List<PersonDocumentView> findPersonDocumentViews(
    PersonDocumentFilter globalFilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonDocumentFilter filter =
      getEndpoint().toLocal(PersonDocumentFilter.class, globalFilter);

    if (StringUtils.isBlank(filter.getDocId()) &&
        StringUtils.isBlank(filter.getPersonId()))
      throw new WebServiceException("FILTER_NOT_ALLOWED");

    Query query = entityManager.createNamedQuery("findPersonDocumentViews");
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());

    String personId = StringUtils.stripStart(filter.getPersonId(), "0");
    query.setParameter("personId", personId);
    query.setParameter("docId", filter.getDocId());

    List<PersonDocumentView> documentViewList =
      new ArrayList<PersonDocumentView>();

    List<Object[]> dbDocumentList = query.getResultList();
    DocumentFilter documentFilter = new DocumentFilter();

    List<Object[]> docViews = new ArrayList<Object[]>();
    for(Object[] row : dbDocumentList)
    {
      DBPerson dbPerson = (DBPerson)row[0];
      DBPersonDocument dbPersonDocument = (DBPersonDocument)row[1];

      PersonDocumentView docView = new PersonDocumentView();

      docView.setPersonDocId(
              getEndpoint().toGlobalId(PersonDocument.class,
              dbPersonDocument.getPersonDocId()));
      PersonView personView = new PersonView();
      personView.setPersonId(getEndpoint().toGlobalId(
        Person.class, dbPerson.getPersonId()));
      personView.setFullName(dbPerson.getFullName());
      personView.setNif(dbPerson.getNif());
      personView.setPassport(dbPerson.getPassport());
      docView.setPersonView(personView);
      docView.setPersonDocTypeId(DictionaryConstants.PERSON_DOCUMENT_TYPE);

      documentFilter.getDocId().add(dbPersonDocument.getDocId()); //

      documentViewList.add(docView);

      docViews.add(new Object[] {dbPersonDocument.getDocId(), docView});
    }

    if (documentFilter.getDocId() != null &&
      documentFilter.getDocId().size() > 0)
    {
      //Invokes document WS findDocument
      try
      {
        Credentials credentials = SecurityUtils.getCredentials(wsContext);
        WSDirectory directory = WSDirectory.getInstance();
        WSEndpoint docEndpoint =
          directory.getEndpoint(DocumentManagerService.class);

        DocumentManagerPort port = docEndpoint.getPort(
          DocumentManagerPort.class,
          credentials.getUserId(), credentials.getPassword());

        documentFilter.setVersion(0); //obtain always last version
        documentFilter.setFirstResult(0);
        documentFilter.setMaxResults(0);
        documentFilter.setIncludeContentMetadata(true);
        List<Document> documentList = port.findDocuments(documentFilter);

        HashMap<String, Document> docs = new HashMap<>();

        //Parse WS result and completes the rowlist of the return table
        for (Document document : documentList)
        {
          String localDocId =
            getEndpoint().toLocalId(Document.class, document.getDocId());
          docs.put(localDocId, document);
        }

        for(Object [] docIdCaseDocumentView : docViews )
        {
          String docId = (String)docIdCaseDocumentView[0];
          PersonDocumentView view =
            (PersonDocumentView)docIdCaseDocumentView[1];
          if (docId!=null) view.setDocument(docs.get(docId));
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
    }
    return documentViewList;
  }

  @Override
  public PersonDocument loadPersonDocument(String globalPersonDocId)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String personDocId =
      getEndpoint().toLocalId(PersonDocument.class, globalPersonDocId);

    PersonDocument personDocument = null;
    if (personDocId != null)
    {
      DBPersonDocumentPK pk = new DBPersonDocumentPK(personDocId);
      DBPersonDocument dbPersonDocument =
        entityManager.find(DBPersonDocument.class, pk);
      if (dbPersonDocument != null)
      {
        personDocument = new PersonDocument();
        dbPersonDocument.copyTo(getEndpoint(), personDocument);
      }
    }
    return personDocument;
  }

  @Override
  public boolean removePersonDocument(String globalPersonDocId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String personDocId =
      getEndpoint().toLocalId(PersonDocument.class, globalPersonDocId);

    if (personDocId != null)
    {
      DBPersonDocumentPK pk = new DBPersonDocumentPK(personDocId);
      DBPersonDocument dbPersonDocument =
        entityManager.find(DBPersonDocument.class, pk);
      if (dbPersonDocument != null)
      {
        dbPersonDocument.setEliminat("1");
        entityManager.merge(dbPersonDocument);
        return true;
      }
    }

    return false;
  }

  @Override
  public PersonDocument storePersonDocument(PersonDocument globalPersonDocument)
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    User user = UserCache.getUser(credentials);

    if (!isKernelAdmin(user) && !isUserInRole(writeRole, user))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonDocument personDocument =
      getEndpoint().toLocal(PersonDocument.class, globalPersonDocument);

    String personDocId = personDocument.getPersonDocId();

    validatePersonDocument(personDocument);

    DBPersonDocument dbPersonDocument;

    if (personDocId == null) //insert
    {
      String docId = personDocument.getDocId();
      try
      {
        WSDirectory directory = WSDirectory.getInstance();
        WSEndpoint docEndpoint =
          directory.getEndpoint(DocumentManagerService.class);

        DocumentManagerPort port = docEndpoint.getPort(
          DocumentManagerPort.class,
          credentials.getUserId(), credentials.getPassword());

        //Get document type
        Document document = port.loadDocument(docId, 0, ContentInfo.ID);
        String docTypeId =
          docEndpoint.toLocalId("Type", document.getDocTypeId());
        String docnompc = DocumentUtils.getFilename(document.getTitle());
        String personId =
          StringUtils.leftPad(personDocument.getPersonId(), 8, "0");
        String observacions = personDocument.getComments();
        if (docTypeId != null)
        {
          String[] parts = splitDocTypeId(docTypeId);
          if (parts.length == 3)
          {
            String doctip = parts[0];
            String doccod = parts[1];
            String modelcod = parts[2];
            dbPersonDocument =
              new DBPersonDocument(doctip, doccod, modelcod, docnompc, docId,
                personId, observacions);
            DBPersonDocumentPK pk =
              new DBPersonDocumentPK(dbPersonDocument.getPersonDocId());
            if (entityManager.find(DBPersonDocument.class, pk) != null)
              throw new WebServiceException("kernel:DUPLICATED_DOCUMENT");

            dbPersonDocument.setAuditoriaCreacio(user.getUserId());

            try
            {
              entityManager.persist(dbPersonDocument);
              entityManager.flush();
              dbPersonDocument.copyTo(personDocument);
            }
            catch(Exception e)
            {
              throw new WebServiceException("UNSUPPORTED_DOCUMENT");
              //TODO: validate document type.
            }
          }
          else
            throw new WebServiceException("UNSUPPORTED_DOCUMENT_TYPE");
        }
        else
          throw new WebServiceException("UNSUPPORTED_DOCUMENT_TYPE");
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
    }
    else //update
    {
      DBPersonDocumentPK pk = new DBPersonDocumentPK(personDocId);
      dbPersonDocument =
        entityManager.find(DBPersonDocument.class, pk);
      if (dbPersonDocument != null)
      {
        String docId = personDocument.getDocId();
        //Cannot change docId
        if (docId != null && !docId.equals(dbPersonDocument.getDocId()))
          throw new WebServiceException("NOT_IMPLEMENTED");

        dbPersonDocument.setObservacions(personDocument.getComments());
        dbPersonDocument.setAuditoriaModificacio(user.getUserId());
        entityManager.merge(dbPersonDocument);
        dbPersonDocument.copyTo(personDocument);
      }
    }

    return personDocument;
  }

  @Override
  public List<AddressDocumentView> findAddressDocumentViews(
    AddressDocumentFilter filter)
  {
    throw new UnsupportedOperationException("NOT_SUPPORTED_YET");
  }

  @Override
  public AddressDocument loadAddressDocument(String addressDocId)
  {
    throw new UnsupportedOperationException("NOT_SUPPORTED_YET");
  }

  @Override
  public boolean removeAddressDocument(String addressDocId)
  {
    throw new UnsupportedOperationException("NOT_SUPPORTED_YET");
  }

  @Override
  public AddressDocument storeAddressDocument(AddressDocument addressDocument)
  {
    throw new UnsupportedOperationException("NOT_SUPPORTED_YET");
  }

  @Override
  public PersonPerson loadPersonPerson(String globalPersonPersonId)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String personPersonId =
      getEndpoint().toLocalId(PersonPerson.class, globalPersonPersonId);

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
          personPerson.setPersonPersonTypeId(
            DictionaryConstants.PERSON_PERSON_TYPE);
      }
    }

    return getEndpoint().toGlobal(PersonPerson.class, personPerson);
  }

  @Override
  public PersonPerson storePersonPerson(PersonPerson globalPersonPerson)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonPerson personPerson = globalPersonPerson;

    String personPersonId = personPerson.getPersonPersonId();

    validatePersonPerson(personPerson);

    DBPersonPerson dbPersonPerson;
    if (personPersonId == null) //insert
    {
      dbPersonPerson = new DBPersonPerson(personPerson);
      entityManager.persist(dbPersonPerson);
    }
    else //update
    {
      dbPersonPerson = entityManager.merge(new DBPersonPerson(personPerson));
    }

    return getEndpoint().toGlobal(PersonPerson.class, dbPersonPerson);
  }

  @Override
  public boolean removePersonPerson(String globalPersonPersonId)
  {
    if (!isKernelAdmin() && !isUserInRole(writeRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    String personPersonId =
      getEndpoint().toLocalId(PersonPerson.class, globalPersonPersonId);

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
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    List<PersonPersonView> personPersonViewList =
      new ArrayList<PersonPersonView>();

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
        personPersonView.setPersonPersonTypeId(personPersonTypeId != null ?
          personPersonTypeId :
          DictionaryConstants.PERSON_PERSON_TYPE);

        personPersonViewList.add(personPersonView);
      }
    }

    return personPersonViewList;
  }

  public int countPersonPersons(PersonPersonFilter filter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

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

  /* private methods */
  private String[] splitDocTypeId(String docTypeId)
  {
    String doctip = "    ";
    String doccod = "    ";
    String modelcod = "    ";

    if (docTypeId != null && !docTypeId.equals("Document"))
    {
      if (docTypeId.startsWith("DOC_") && docTypeId.length() == 13)
      {
        doctip = "DOC ";
        doccod = replaceUnderscore(docTypeId.substring(4, 8));
        modelcod = replaceUnderscore(docTypeId.substring(9, 13));
      }
      else if (docTypeId.length() == 14)
      {
        doctip = replaceUnderscore(docTypeId.substring(0, 4));
        doccod = replaceUnderscore(docTypeId.substring(5, 9));
        modelcod = replaceUnderscore(docTypeId.substring(10, 14));
      }
    }

    return new String[]{doctip, doccod, modelcod};
  }

  private String replaceUnderscore(String text)
  {
    char[] arr = text.toCharArray();
    boolean previousWord = false;
    for (int i = 0; i < arr.length; i++)
    {
      char ch = arr[i];
      if (ch == '_')
      {
        if (!previousWord)
          arr[i] = '*';
        else
          arr[i] = ' ';
      }
      if (!previousWord)
        previousWord = String.valueOf(ch).matches("[a-zA-Z0-9]");
    }

    return String.valueOf(arr);
  }

  private String getKernelListId(KernelList list)
  {
    String listId = null;
    switch(list)
    {
      case PERSON_PARTICLE:
        listId = "PCOG";
        break;
      case ADDRESS_PARTICLE:
        listId = "PCOG";
        break;
    }
    return listId;
  }

  private int getKernelListSize(KernelList list)
  {
    int size = 8;
    switch(list)
    {
      case PERSON_PARTICLE:
        size = 6;
        break;
      case ADDRESS_PARTICLE:
        size = 6;
        break;
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
    catch(Exception ex)
    {
      throw new WebServiceException(ex);
    }
  }

  private void checkStorePerson(Person person, String role)
  {
    Set roles = UserCache.getUser(wsContext).getRoles();
    if (!roles.contains(role))
    {
      String personId = person.getPersonId();
      if (personId != null) // is an update
      {
        String queryString =
          MatrixConfig.getProperty("KernelManager.checkStorePersonQuery");
        if (queryString != null)
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
      filter.setNif(person.getNif().trim());
      setPersonFilterParameters(query, filter);
      int count = ((Number)query.getSingleResult()).intValue();
      if (count > 0) throw new WebServiceException("kernel:DUPLICATED_NIF");
    }
  }

  private boolean isUserInRole(String role)
  {
    User user = UserCache.getUser(wsContext);
    return isUserInRole(role, user);
  }

  private boolean isUserInRole(String role, User user)
  {
    return user.isInRole(role);
  }

  private boolean isKernelAdmin()
  {
    return isUserInRole(KERNEL_ADMIN_ROLE);
  }

  private boolean isKernelAdmin(User user)
  {
    return isUserInRole(KERNEL_ADMIN_ROLE, user);
  }

  private String addPercent(String text)
  {
    if (text == null)
    {
      return null;
    }
    if ("".equals(text))
    {
      return "";
    }
    return "%" + text.toLowerCase().trim() + "%";
  }

  private String likePattern(String pattern)
  {
    if (pattern.length() == 0)
    {
      return null;
    }
    StringBuilder buffer = new StringBuilder("% ");
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
    String nowDate = DateFormat.getInstance().formatDate(now);
    String nowTime = DateFormat.getInstance().formatTime(now);

    base.setStddgr(nowDate);
    base.setStdhgr(nowTime);
    base.setStddmod(nowDate);
    base.setStdhmod(nowTime);
  }

  private void auditUpdate(DBEntityBase base)
  {
    base.setStdumod(getUserId());
    Date now = new Date();
    base.setStddmod(DateFormat.getInstance().formatDate(now));
    base.setStdhmod(DateFormat.getInstance().formatTime(now));
  }

  private String describeAddress(DBAddress dbAddress, DBStreet dbStreet)
  {
    StringBuilder buffer = new StringBuilder();

    if (dbAddress.getPostOfficeBox() != null && !"0".equals(
      dbAddress.getPostOfficeBox().trim()) &&
      !"".equals(dbAddress.getPostOfficeBox().trim()))
    {
      buffer.append(" APARTAT DE CORREUS ")
        .append(dbAddress.getPostOfficeBox());
    }
    if (dbStreet != null)
    {
      if (dbStreet.getStreetTypeId() != null)
      {
        buffer.append(" ").append(dbStreet.getStreetTypeId());
      }
      if (dbStreet.getName() != null)
      {
        buffer.append(" ").append(dbStreet.getName());
      }
    }

    if (dbAddress.getKm() != null && dbAddress.getKm() != 0)
    {
      buffer.append(" Km. ").append(dbAddress.getKm());
    }
    if (dbAddress.getHm() != null && dbAddress.getHm() != 0)
    {
      buffer.append(" Hm. ").append(dbAddress.getHm());
    }

    if (dbAddress.getNumber1() != null)
    {
      buffer.append(" ").append(dbAddress.getNumber1());
    }
    if (dbAddress.getBis1() != null)
    {
      buffer.append(" ").append(dbAddress.getBis1());
    }
    if (dbAddress.getNumber2() != null)
    {
      buffer.append("-").append(dbAddress.getNumber2());
    }
    if (dbAddress.getBis2() != null)
    {
      buffer.append(" ").append(dbAddress.getBis1());
    }
    if (dbAddress.getBlock() != null)
    {
      buffer.append(" ").append(dbAddress.getBlock());
    }
    if (dbAddress.getStair() != null)
    {
      buffer.append(" ESC.").append(dbAddress.getStair());
    }
    if (dbAddress.getFloor() != null)
    {
      buffer.append(" ").append(dbAddress.getFloor());
    }
    if (dbAddress.getDoor() != null)
    {
      buffer.append(" ");
      buffer.append(dbAddress.getDoor());
    }

//    if (dbAddress.getPostalCode() != null)
//    {
//      buffer.append(" CP " + dbAddress.getPostalCode());
//    }

    return buffer.toString().trim();
  }

  private String getStringFromIdList(Class entityClass, List<String> idList)
  {
    Entity entity = getEndpoint().getEntity(entityClass);
    if (idList == null)
    {
      return null;
    }
    if (idList.isEmpty())
    {
      return null;
    }
    StringBuilder buffer = new StringBuilder(" ");
    for (String globalId : idList)
    {
      /*
      VersionIdentifier vId = new VersionIdentifier(entity, globalId);
      buffer.append(vId.getId()); */
      buffer.append(entity.toLocalId(globalId));
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
    query.setParameter( "day", DateFormat.getInstance().formatDate(now));
    query.setParameter( "hour", DateFormat.getInstance().formatTime(now));
    int numUpdated = query.executeUpdate();

    if (numUpdated == 1)
    {
      query = entityManager.createNamedQuery("readGenericCounter");
      query.setParameter("claupref", claupref);
      query.setParameter("claucod", claucod);
      query.setParameter("clauorigen", clauorigen);
      Number value = (Number) query.getSingleResult();
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

    String idList = getStringFromIdList(Person.class, personIdList);
    query.setParameter("idList", idList);
    query.setParameter("name", addPercent(filter.getName()));
    query.setParameter("firstSurname", addPercent(filter.getFirstSurname()));
    query.setParameter("secondSurname", addPercent(filter.getSecondSurname()));
    query.setParameter("fullName", addPercent(filter.getFullName()));
    query.setParameter("nif", addPercent(filter.getNif()));
    query.setParameter("passport", filter.getPassport() != null ?
      filter.getPassport().toLowerCase().trim() : null);
    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    query.setMaxResults(maxResults);
  }

  private void setAddressFilterParameters(Query query, AddressFilter filter)
  {
    List<String> addressIdList = filter.getAddressIdList();

    query.setParameter("idList",
      getStringFromIdList(Address.class, addressIdList));

    String countryName = filter.getCountryName();
    if (countryName != null)
    {
      countryName = countryName.trim();
      countryName = likePattern(countryName);
    }
    query.setParameter("countryName", countryName);

    String cityName = filter.getCityName();
    if (cityName != null)
    {
      cityName = cityName.trim();
      cityName = likePattern(cityName);
    }
    query.setParameter("cityName", cityName);

    String streetName = filter.getStreetName();
    if (streetName != null)
    {
      streetName = streetName.trim();
      streetName = likePattern(streetName);
    }
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
    if (comments != null)
    {
      comments = likePattern(comments);
    }
    query.setParameter("comments", comments);

    String addressTypeId = filter.getAddressTypeId();
    if (addressTypeId != null)
    {
      addressTypeId = DicTypeAdmin.getInstance(AddressType.class)
              .toLocalId(endpoint, addressTypeId);
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
    query.setMaxResults(maxResults == 0 ? 10 : maxResults);
  }

  private void setRoomFilterParameters(Query query, RoomFilter filter)
  {
    query.setParameter("idList",
      getStringFromIdList(Room.class, filter.getRoomIdList()));
    query.setParameter("addressId", filter.getAddressId());
    query.setParameter("roomName", addPercent(filter.getRoomName()));
    query.setParameter("roomTypeId", filter.getRoomTypeId());
    query.setParameter("capacity", filter.getCapacity());
    query.setParameter("comments" , addPercent(filter.getComments()));
    query.setParameter("spaceId", filter.getSpaceId());
    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    if (maxResults > 0) query.setMaxResults(maxResults);
  }

  private void validatePerson(Person person)
  {
    String personTypeId = person.getPersonTypeId();
    if (StringUtils.isBlank(personTypeId)) personTypeId = null;

    String nif = person.getNif();
    if (StringUtils.isBlank(nif)) nif = null;

    if (nif == null)
    {
      // if nif is null, personTypeId can not be null
      if (personTypeId == null)
        throw new WebServiceException("kernel:TYPEID_IS_MANDATORY");

      if (personTypeId.equals(F.getTypeId())
        || personTypeId.equals(R.getTypeId())
        || personTypeId.equals(E.getTypeId())
        || personTypeId.equals(J.getTypeId()))
        throw new WebServiceException("kernel:NIF_IS_MANDATORY");
    }
    else // nif not null
    {
      nif = nif.trim();
      if (nif.length() == 8)
      {
        // calculate control digit
        char control = KernelUtils.calculateNIFControl(nif);
        if (control == KernelUtils.CONTROL_ERROR)
          throw new WebServiceException("kernel:INVALID_NIF");
        nif += control;
        person.setNif(nif);
      }
      else
      {
        if (!KernelUtils.isValidNIF(nif))
          throw new WebServiceException("kernel:INVALID_NIF");
      }
      switch (KernelUtils.getNIFType(nif))
      {
        case KernelUtils.NATIONAL_NIF:
          personTypeId = F.getTypeId();
          person.setPersonTypeId(personTypeId);
          break;
        case KernelUtils.RESIDENT_NIF: // NIE
          personTypeId = R.getTypeId();
          person.setPersonTypeId(personTypeId);
          break;
        case KernelUtils.LEGAL_NIF: // CIF
          String letter = String.valueOf(nif.charAt(0));
          boolean isEntity = "PQR".contains(letter);
          personTypeId = isEntity ? E.getTypeId() : J.getTypeId();
          person.setPersonTypeId(personTypeId);
          break;
        default:
          break;
      }
    }

    personTypeId = getEndpoint().toGlobalId(Type.class, personTypeId);
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
      getEndpoint().toGlobalId(Type.class, personAddressTypeId);
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
      getEndpoint().toGlobalId(Type.class, personDocument.getPersonDocTypeId());
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

    String repTypeId = getEndpoint().toGlobalId(Type.class,
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
      getEndpoint().toGlobalId(Type.class, contact.getContactTypeId());
    org.santfeliu.dic.Type type = TypeCache.getInstance().getType(
      contactTypeId);

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

    addressTypeId = getEndpoint().toGlobalId(Type.class, addressTypeId);
    org.santfeliu.dic.Type type =
      TypeCache.getInstance().getType(addressTypeId);

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

    roomTypeId = getEndpoint().toGlobalId(Type.class, roomTypeId);
    org.santfeliu.dic.Type type = TypeCache.getInstance().getType(roomTypeId);

    HashSet unvalidable = new HashSet();
    unvalidable.add("roomId");

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(room, unvalidable);
  }



  private void validatePersonPerson(PersonPerson personPerson)
  {
    if (StringUtils.isBlank(personPerson.getRelPersonId()))
      throw new WebServiceException("kernel:INVALID_PERSON_PERSON");
    if (StringUtils.isBlank(personPerson.getPersonId()))
      throw new WebServiceException("kernel:INVALID_PERSON_PERSON");
    if (StringUtils.isBlank(personPerson.getPersonPersonTypeId()))
      throw new WebServiceException("kernel:INVALID_PERSON_PERSON_TYPE");

    String typeId = personPerson.getPersonPersonTypeId();
    typeId = getEndpoint().toGlobalId(org.matrix.dic.Type.class, typeId);
    Type type = TypeCache.getInstance().getType(typeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(personPerson, "personPersonId");
  }

  @Override
  public int countPersonAddresses(PersonAddressFilter globalFilter)
  {
    if (!isKernelAdmin() && !isUserInRole(readRole))
      throw new WebServiceException(NOT_AUTHORIZED);

    PersonAddressFilter filter =
      getEndpoint().toLocal(PersonAddressFilter.class, globalFilter);

    String personId = filter.getPersonId();
    String addressId = filter.getAddressId();
    VersionIdentifier vPersonId =
      (personId != null) ? new VersionIdentifier(personId) : null;
    VersionIdentifier vAddressId =
      (addressId != null) ? new VersionIdentifier(addressId) : null;


    if (vPersonId!=null &&  vPersonId.hasVersion()) {
      personId = vPersonId.getId();
    }

    if (vAddressId != null && vAddressId.hasVersion() &&
      vAddressId.getVersionTypeId() != null)
    {
      AddressFilter addressFilter = new AddressFilter();
      addressFilter.getAddressIdList().add(addressId);

      return countFotosAdreces(addressFilter);
    }
    else
    {
      Query query = entityManager.createNamedQuery("countPersonAddresses");
      query.setParameter("personId", personId);
      query.setParameter("addressId", addressId);

      query.setFirstResult(0);
      query.setMaxResults(1);

      Number numTotal = (Number)query.getSingleResult();
      return numTotal==null?0:numTotal.intValue();
    }
  }



  /**
   * return: String vAddressId = (String)row[0];
   *   String personId = (String)row[1];
   *   String personnd = (String)row[2];
   *   String description = (String)row[3];
   *   DBCity dbCity = (DBCity) row[4];
   *   DBProvince dbProvince = (DBProvince) row[5];
   *   DBCountry dbCountry = (DBCountry) row[6];
   * */
  private List<Object[]> findFotosAdreces(AddressFilter filter)
  {
    int idsCount = filter.getAddressIdList().size();
    Query query;

    query = entityManager.createNamedQuery("findAdrecesPersonaExpedient");
    setAddressFilterParameters(query, filter);
    query.setParameter("versionType",
      "$" + VersionType.FOTO_ADDRESS_CASE_PERSONDOM.getVersionTypeId() + "*");
    List<Object[]> resultList = query.getResultList();

    if (resultList.size() < idsCount)
    {
      query = entityManager.createNamedQuery("findAdrecesInteressatExpedient");
      setAddressFilterParameters(query, filter);
      query.setParameter("versionType",
        "$" + VersionType.FOTO_ADDRESS_CASE_PERSONDOM.getVersionTypeId() + "*");
      resultList.addAll(query.getResultList());
    }

    if (resultList.size() < idsCount)
    {
      query = entityManager.createNamedQuery("findAdrecesRepresentantExpedient");
      setAddressFilterParameters(query, filter);
      query.setParameter("versionType", "$" +
        VersionType.FOTO_ADDRESS_CASE_REPRESENTANTDOM.getVersionTypeId() + "*");
      resultList.addAll(query.getResultList());
    }

    if (resultList.size() < idsCount)
    {
      String queryName = "findAdrecesRepresentantInteressatExpedient";
      query = entityManager.createNamedQuery(queryName);
      setAddressFilterParameters(query, filter);
      query.setParameter("versionType", "$" +
        VersionType.FOTO_ADDRESS_CASE_REPRESENTANTDOM.getVersionTypeId() + "*");
      resultList.addAll(query.getResultList());
    }

    return resultList;
  }

  private int countFotosAdreces(AddressFilter filter)
  {
    int total = 0;
    int idsCount = filter.getAddressIdList().size();
    Query query;

    query = entityManager.createNamedQuery("countAdrecesPersonaExpedient");
    setAddressFilterParameters(query, filter);
    query.setParameter("versionType", "$" +
      VersionType.FOTO_ADDRESS_CASE_PERSONDOM.getVersionTypeId() + "*");
    Number num = (Number) query.getSingleResult();
    total += num == null ? 0 : num.intValue();

    if (total < idsCount || idsCount == 0)
    {
      query = entityManager.createNamedQuery("countAdrecesInteressatExpedient");
      setAddressFilterParameters(query, filter);
      query.setParameter("versionType", "$" +
        VersionType.FOTO_ADDRESS_CASE_PERSONDOM.getVersionTypeId() + "*");

      num = (Number) query.getSingleResult();
      total += num == null ? 0 : num.intValue();
    }

    if (total < idsCount || idsCount == 0)
    {
      query = entityManager.createNamedQuery("countAdrecesRepresentantExpedient");
      setAddressFilterParameters(query, filter);
      query.setParameter("versionType", "$" +
        VersionType.FOTO_ADDRESS_CASE_REPRESENTANTDOM.getVersionTypeId() + "*");

      num = (Number) query.getSingleResult();
      total += num == null ? 0 : num.intValue();
    }

    if (total < idsCount || idsCount == 0)
    {
      String queryName = "countAdrecesRepresentantInteressatExpedient";
      query = entityManager.createNamedQuery(queryName);
      setAddressFilterParameters(query, filter);
      query.setParameter("versionType", "$" +
        VersionType.FOTO_ADDRESS_CASE_REPRESENTANTDOM.getVersionTypeId() + "*");

      num = (Number) query.getSingleResult();
      total += num == null ? 0 : num.intValue();
    }
    return total;
  }

  private void checkCity(String id, User user) throws WebServiceException
  {
    // id can be a cityId or streetId
    if (id == null)
    {
      return;
    }
    if (user.getRoles().contains(KernelConstants.KERNEL_ADMIN_ROLE))
    {
      return;
    }

    String blockedCityId
      = MatrixConfig.getProperty("KernelManager.blockedCityId");
    if (blockedCityId == null)
    {
      return;
    }

    if (id.startsWith(blockedCityId))
    {
      throw new WebServiceException("NOT_AUTHORIZED");
    }
  }

  private void validatePersonFilter(PersonFilter filter)
  {
    if (filter.getPersonId().isEmpty()
      && StringUtils.isBlank(filter.getNif())
      && StringUtils.isBlank(filter.getName())
      && StringUtils.isBlank(filter.getFirstSurname())
      && StringUtils.isBlank(filter.getSecondSurname())
      && StringUtils.isBlank(filter.getFullName())
      && StringUtils.isBlank(filter.getPassport())
      && filter.getMaxResults() == 0)
    {
      throw new WebServiceException("FILTER_NOT_ALLOWED");
    }
  }
}
