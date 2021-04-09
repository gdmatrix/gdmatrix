package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.security.Action;
import com.audifilm.matrix.common.service.PKUtil;
import com.audifilm.matrix.common.service.VersionIdentifier;
import com.audifilm.matrix.dic.service.casedocument.CaseDocumentType;
import com.audifilm.matrix.dic.service.cases.CaseExpedientType;
import com.audifilm.matrix.dic.service.caseperson.CasePersonType;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import com.audifilm.matrix.security.service.DBGrupUsuari;
import com.audifilm.matrix.util.TextUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.matrix.security.AccessControl;
import org.matrix.cases.Case;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseMetaData;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.cases.Demand;
import org.matrix.cases.DemandFilter;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.cases.Problem;
import org.matrix.cases.ProblemFilter;
import org.matrix.cases.ProblemView;
import org.matrix.dic.Property;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.Person;
import org.matrix.security.User;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.jpa.JPA;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.ws.WSExceptionFactory;
import org.santfeliu.ws.WSUtils;
import com.audifilm.matrix.security.service.SecurityManager;
import com.audifilm.matrix.util.ConfigProperties;
import java.util.AbstractList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.cases.InterventionProblem;
import org.matrix.cases.InterventionProblemFilter;
import org.matrix.cases.InterventionProblemView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.DocumentManagerService;
import org.santfeliu.jpa.JPAQuery;
import org.santfeliu.ws.annotations.MultiInstance;

/**
 *
 * @author comasfc
 */
@WebService(endpointInterface = "org.matrix.cases.CaseManagerPort")
@HandlerChain(file = "handlers.xml")
@MultiInstance
public class CaseManager implements CaseManagerPort
{
  @Resource
  WebServiceContext wsContext;

  @PersistenceContext(unitName = "cases_g5")
  public EntityManager entityManager;


  //static String CASE_ADMIN_ROLE = ConfigProperties.getProperty("com.audifilm.matrix.security.adminRoleId", "CASE_ADMIN");
  final static public String CASE_ADMIN_PROPERTYNAME = "com.audifilm.matrix.security.adminRoleId";

  WSEndpoint endpoint = null;

  KernelService kernelService = null;
  DictionaryService dictionaryService = null;
  SecurityManager securityManager = null;

  protected static final Logger log = Logger.getLogger(CaseManagerPort.class.getName());
  
  private static final String REGENT = "REGENT";
  private static final String REGSOR = "REGSOR";

//  private static final int CASE_TITLE_MAX_SIZE = 50;
//  private static final int CASE_DESCRIPTION_MAX_SIZE = 255;
//  private static final int CASE_PERSON_COMMENTS_MAX_SIZE = 255;
//  private static final int CASE_ADDRESS_COMMENTS_MAX_SIZE = 0;
//  private static final int CASE_PROBLEM_COMMENTS_MAX_SIZE = 0;
//  private static final int CASE_DEMAND_COMMENTS_MAX_SIZE = 0;
//  private static final int CASE_INTERVENTION_COMMENTS_MAX_SIZE = 0;

  public WSEndpoint getEndpoint()
  {
    if (endpoint==null) {
      String endpointName = WSUtils.getServletAdapter(wsContext).getName();
      endpoint = WSDirectory.getInstance().getEndpoint(endpointName);
    }
    return endpoint;
  }


  public KernelService getKernelService()
  {
    if (kernelService==null)
    {
      try
      {

         Credentials credentials = SecurityUtils.getCredentials(wsContext);
         User user = UserCache.getUser(credentials);

         kernelService = new KernelService(user.getUserId(), user.getPassword());
      }
      catch(Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }

    return kernelService;
  }

  public DictionaryService getDictionaryService()
  {
    if (dictionaryService==null)
    {
      try
      {
         Credentials credentials = SecurityUtils.getCredentials(wsContext);
         User user = UserCache.getUser(credentials);          

         dictionaryService = new DictionaryService(user.getUserId(), user.getPassword());
      }
      catch(Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }

    return dictionaryService;
  }


  public SecurityManager getSecurityManager()
  {
    if (securityManager==null)
    {
      try
      {
        securityManager = new SecurityManager();
      }
      catch(Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }

    return securityManager;
  }

  public CaseMetaData getCaseMetaData()
  {
    CaseMetaData caseMetaData = new CaseMetaData();

//    caseMetaData.setCaseAddressCommentsMaxSize(CASE_ADDRESS_COMMENTS_MAX_SIZE);
//    caseMetaData.setCaseDescriptionMaxSize(CASE_DESCRIPTION_MAX_SIZE);
//    caseMetaData.setCasePersonCommentsMaxSize(CASE_PERSON_COMMENTS_MAX_SIZE);
//    caseMetaData.setCaseTitleMaxSize(CASE_TITLE_MAX_SIZE);
//
//    caseMetaData.setDemandCommentsMaxSize(CASE_DEMAND_COMMENTS_MAX_SIZE);
//    caseMetaData.setInterventionCommentsMaxSize(CASE_INTERVENTION_COMMENTS_MAX_SIZE);
//    caseMetaData.setProblemCommentsMaxSize(CASE_PROBLEM_COMMENTS_MAX_SIZE);

    return caseMetaData;
  }

  public Case loadCase(String matrixCaseId)
  {
    String caseId = getEndpoint().toLocalId(Case.class, matrixCaseId);

    DBCase dbCase = entityManager.find(DBCase.class, caseId);
    if (dbCase == null)
    {
      throw new WebServiceException("case:CASE_NOT_FOUND");
    }
    validarSeguretats(dbCase);

    Query query = entityManager.createNamedQuery("findLastCaseState");
    query.setParameter("caseId", caseId);
    query.setFirstResult(0);
    query.setMaxResults(1);

    List<DBCaseState> dbCasesStateList = query.getResultList();
    if (!dbCasesStateList.isEmpty()) {
      DBCaseState dbState = dbCasesStateList.get(0);
      dbCase.setCaseState(dbState);
    }

    Case oCase = new Case();
    dbCase.copyTo(getEndpoint(), oCase);

    List<String> classIdList = findCaseClassId(dbCase.getCaseId());
    if (classIdList!=null && !classIdList.isEmpty()) {
      for(String classId: classIdList) {
        if (classId!=null) oCase.getClassId().add(classId);
      }
    }

    //Properties
    dbCase.loadCaseProperties(entityManager);
    oCase.getProperty().addAll(dbCase.getProperties());

    //AccessControl
    oCase.getAccessControl().addAll(loadAccessControlList(dbCase));

    return oCase;
  }

  public Case storeCase(Case globalCase)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      log.log(Level.INFO, "storeCase caseId:{0}",
        new Object[]{globalCase.getCaseId() != null ?globalCase.getCaseId() : "NEW"});

      if (!entityManager.getTransaction().isActive()) entityManager.getTransaction().begin();

      Case localCase = getEndpoint().toLocal(Case.class, globalCase);
      if (localCase.getCaseId()!=null)
      {
        String caseId = localCase.getCaseId();
        if (!validarSeguretats(Case.class.getName(), Action.M))
        {
          throw new Exception("cases:NOT_AUTHORIZED_TO_MODIFY_CASE");
        }
        DBCase dbCase = entityManager.find(DBCase.class, caseId);
        dbCase.setAuditoriaModificacio(user.getUserId());

        dbCase.copyFrom(getEndpoint(), localCase);
        dbCase = entityManager.merge(dbCase);

        //Merge propietats

      }
      else
      {
        if (!validarSeguretats(Case.class.getName(), Action.A))
        {
          throw new Exception("cases:NOT_AUTHORIZED_TO_CREATE_CASE");
        }
        DBCase dbCase = null;
        dbCase = new DBCase();
        dbCase.copyFrom(getEndpoint(), globalCase);

        String caseId = getNextCaseId();
        dbCase.setCaseId(caseId);

        String caseTypeNum = getNextNumExpedientParticular(dbCase.getCaseTypeId());
        dbCase.setCaseTypeNum(caseTypeNum);


        dbCase.setAuditoriaCreacio(user.getUserId());

        entityManager.persist(dbCase);
        entityManager.flush();
        //Crear propietats
        localCase.setCaseId(caseId);
      }
      //Store AccessList

      if (entityManager.getTransaction().isActive()) entityManager.getTransaction().commit();

      Case newGlobalCase = getEndpoint().toGlobal(Case.class, localCase);
      return newGlobalCase;
    }
    catch (Exception ex)
    {
      if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
      log.log(Level.SEVERE, "storeCase failed");
        throw WSExceptionFactory.create(ex);
    }


  }

  public boolean removeCase(String caseId)
  {
    if (!validarSeguretats(Case.class.getName(), Action.D))
    {
      throw new WebServiceException("case:NOT_ALLOWED");
    }

    return false;
  }

  public List<Case> findCases(CaseFilter globalFilter)
  {
    
    CaseFilter localFilter = getEndpoint().toLocal(CaseFilter.class, globalFilter);

    //TODO: Provisional a falta d'implementació de seguretats. El métode
    // getQuery() retorna la query adient en funció dels rols de l'usuari.
    JPAQuery query = null;
    org.santfeliu.security.User user = UserCache.getUser(wsContext);
    //boolean isValidUser = validarSeguretats(Case.class.getName(), Action.I);
    try
    {
      if (localFilter.getCaseTypeId()!=null) {
        String caseTypeId = DicTypeAdmin.getInstance(CaseExpedientType.class).toLocalId(getEndpoint(), localFilter.getCaseTypeId());
        localFilter.setCaseTypeId(caseTypeId);
      }

      FindCasesQueryBuilder queryBuilder = new FindCasesQueryBuilder(localFilter, getSecurityManager(), user);
      queryBuilder.setCounterQuery(false);
      queryBuilder.setCaseAdmin(isUserCaseAdmin());

      query = queryBuilder.getFilterCasesQuery(entityManager);

      if (query!=null) {
        query.setFirstResult(localFilter.getFirstResult());
        int maxResults = localFilter.getMaxResults();
        query.setMaxResults(maxResults == 0 ? 100 : maxResults);
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(CaseManager.class.getName()).log(Level.SEVERE, null, ex);
      return Collections.emptyList();
    }

//    JPAQuery query = new JPAQuery(em.createNamedQuery("findCasesWithClassId"));

    if (query==null) return Collections.emptyList();

    List<Object []> dbCaseList = query.getResultList();
    List<Case> caseList = new ArrayList<Case>();
    for(Object[] row: dbCaseList)
    {
      DBCase dbCase = (DBCase)row[0];
      String classId = (String)row[1];
      Case ocase = new Case();
      dbCase.copyTo(getEndpoint(), ocase);
      
      if (classId!=null) {
        ocase.getClassId().add(classId);
      }
      caseList.add(ocase);
    }
    return caseList;
  }

  public int countCases(CaseFilter globalFilter)
  {
    CaseFilter localFilter = getEndpoint().toLocal(CaseFilter.class, globalFilter);

    JPAQuery query = null;
    //boolean isValidUser = validarSeguretats(Case.class.getName(), Action.I);
    org.santfeliu.security.User user = UserCache.getUser(wsContext);
    try
    {
      if (localFilter.getCaseTypeId()!=null) {
        String caseTypeId = DicTypeAdmin.getInstance(CaseExpedientType.class).toLocalId(getEndpoint(), localFilter.getCaseTypeId());
        localFilter.setCaseTypeId(caseTypeId);
      }

      FindCasesQueryBuilder queryBuilder = new FindCasesQueryBuilder(localFilter,getSecurityManager(), user);
      /*
      queryBuilder.setFilterTypeId(
        DicTypeAdmin.getInstance(CaseExpedientType.class).toLocalId(getEndpoint(), localFilter.getCaseTypeId()));
      queryBuilder.setPropertiesFilter(localFilter.getProperty());
       */
      queryBuilder.setCounterQuery(true);
      queryBuilder.setCaseAdmin(isUserCaseAdmin());
      
      query = queryBuilder.getFilterCasesQuery(entityManager);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return 0;
    }

//    JPAQuery query = new JPAQuery( entityManager.createNamedQuery("countCasesWithClassId"));

    if (query==null) return 0;

    query.setFirstResult(0);
    query.setMaxResults(1);

    Number count = (Number)query.getResultCount();
    return count.intValue();
  }

  public CasePerson loadCasePerson(String globalCasePersonId)
  {
    if (!validarSeguretats(Case.class.getName(), Action.I))
    {
      return null;
    }


    String casePersonId = getEndpoint().toLocalId(CasePerson.class, globalCasePersonId);
    VersionIdentifier vId = new VersionIdentifier(casePersonId);
    String id[] = vId.getIds();

    Query query = entityManager.createNamedQuery("findCasePersons");
    query.setParameter("caseId", id[0]);
    query.setParameter("personId", id[1]);


    List<DBCase> dbCaseList = query.getResultList();
    if (dbCaseList.size()>0) 
    {
      CasePerson casePerson = new CasePerson();
      DBCase dbCase = dbCaseList.get(0);
      dbCase.copyTo(getEndpoint(), casePerson);
      return casePerson;
    }
    
    if (!validarSeguretats(CasePerson.class.getName(), Action.I))
    {
      return null;
    }
    query = entityManager.createNamedQuery("findInteressats");
    query.setParameter("caseId", id[0]);
    query.setParameter("personId", id[1]);

    List<DBInteressat> interessatList = query.getResultList();
    if (interessatList.size()>0)
    {
      CasePerson casePerson = new CasePerson();
      DBInteressat dbInteressat = interessatList.get(0);
      dbInteressat.copyTo(getEndpoint(), casePerson);
      return casePerson;
    }
    return null;
  }

  public CasePerson storeCasePerson(CasePerson globalCasePerson)
  {
    try
    {

      if (globalCasePerson.getCaseId()==null && !validarSeguretats(CasePerson.class.getName(), Action.A))
      {
        throw new WebServiceException("case:NOT_ALLOWED");
      }
      
      if (globalCasePerson.getCaseId()!=null && !validarSeguretats(CasePerson.class.getName(), Action.M))
      {
        throw new WebServiceException("case:NOT_ALLOWED");
      }

      CasePerson newLocalCasePerson = null;
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);
      
      CasePerson localCasePerson = getEndpoint().toLocal(CasePerson.class, globalCasePerson);
      String casePersonTypeId = DicTypeAdmin.getInstance(CasePersonType.class)
              .toLocalId(endpoint, localCasePerson.getCasePersonTypeId());
      String caseId = localCasePerson.getCaseId();
      String personId = localCasePerson.getPersonId();
      if (personId!=null) personId = (new VersionIdentifier(personId)).getId();

      String personAddressId = localCasePerson.getAddressId();
      //if (personAddressId!=null)  personAddressId = (new VersionIdentifier(personAddressId)).getId();
      String persnd = getKernelService().findPersonAddressNumber(personId, personAddressId, true);

      String representantId = localCasePerson.getRepresentantPersonId();
      if (representantId!=null) representantId = (new VersionIdentifier(representantId)).getId();

      String representantAddressId = localCasePerson.getRepresentantAddressId();
      //if (representantAddressId!=null)  representantAddressId = (new VersionIdentifier(representantAddressId)).getId();
      String representantnd = null;
      if (representantId!=null && representantAddressId!=null)
      {
        representantnd = getKernelService().findPersonAddressNumber(representantId, representantAddressId, true);
      }
      DBCase dbCase = entityManager.find(DBCase.class, caseId);
      if (dbCase == null)
      {
        throw new WebServiceException("case:CASE_NOT_FOUND");
      }
      
      if (CasePersonType.InteressatPrincipal.equals(casePersonTypeId)) {
        //Persona de l'Expedient
        dbCase.setPersId(personId);
        dbCase.setPersnd(persnd);
        dbCase.setReprId(representantId);
        dbCase.setReprnd(representantnd);
        dbCase = entityManager.merge(dbCase);

        newLocalCasePerson = localCasePerson;
      }
      else
      {
        DBInteressat dbInteressat = null;
        if (localCasePerson.getCasePersonId()!=null)
        {
          dbInteressat = entityManager.getReference(DBInteressat.class, new DBInteressatPK(caseId, personId, personAddressId));
        }
        //Interessats
        if (dbInteressat!=null && dbInteressat.getCaseId().equalsIgnoreCase(caseId)
                && dbInteressat.getPersonId().equalsIgnoreCase(personId))
        {
          dbInteressat.copyFrom(this, localCasePerson);
          dbInteressat.setAuditoriaModificacio(user.getUserId());
          dbInteressat = entityManager.merge(dbInteressat);
        }
        else
        {
          if (dbInteressat!=null) entityManager.remove(dbInteressat);
          dbInteressat = new DBInteressat();
          dbInteressat.copyFrom(this, localCasePerson);
          dbInteressat.setAuditoriaCreacio(user.getUserId());
          entityManager.persist(dbInteressat);
        }
        newLocalCasePerson = new CasePerson();
        dbInteressat.copyTo(getEndpoint(), newLocalCasePerson);
      }
      return getEndpoint().toGlobal(CasePerson.class, newLocalCasePerson);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeCasePerson failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeCasePerson(String casePersonId)
  {
    return false;
  }

  public List<CasePersonView> findCasePersonViews(CasePersonFilter filter)
  {
    if (!validarSeguretats(Case.class.getName(), Action.I))
    {
      return Collections.emptyList();
    }

    List<CasePersonView> casePersonViewList = new ArrayList<CasePersonView>();

    int maxResults = filter.getMaxResults();

    String caseId = (new VersionIdentifier(getEndpoint().getEntity(Case.class), filter.getCaseId())).getId();
    String personId = (new VersionIdentifier(getEndpoint().getEntity(Person.class), filter.getPersonId())).getId();

    Query query1 = entityManager.createNamedQuery("findCasePersons");
    query1.setParameter("caseId", caseId);
    query1.setParameter("personId", personId);
    query1.setFirstResult(filter.getFirstResult());
    query1.setMaxResults(maxResults == 0 ? 100 : maxResults);

    List<DBCase> dbCaseList = query1.getResultList();
    for(DBCase dbCase : dbCaseList)
    {
      CasePersonView casePersonView = new CasePersonView();
      dbCase.copyTo(getEndpoint(), casePersonView);
      casePersonViewList.add(casePersonView);
    }
    
    if (validarSeguretats(CasePerson.class.getName(), Action.I))
    {
      Query query2 = entityManager.createNamedQuery("findInteressats");
      query2.setParameter("caseId", caseId);
      query2.setParameter("personId", personId);
      query2.setFirstResult(filter.getFirstResult());
      query2.setMaxResults(maxResults == 0 ? 100 : maxResults);

      List<DBInteressat> dbList = query2.getResultList();
      for(DBInteressat interessat: dbList)
      {
        CasePersonView casePersonView = new CasePersonView();
        interessat.copyTo(getEndpoint(), casePersonView);
        casePersonViewList.add(casePersonView);
      }
    }

    return casePersonViewList;
  }

  public int countCasePersons(CasePersonFilter filter)
  {
    if (!validarSeguretats(Case.class.getName(), Action.I))
    {
      return 0;
    }


    String caseId = (new VersionIdentifier(getEndpoint().getEntity(Case.class), filter.getCaseId())).getId();
    String personId = (new VersionIdentifier(getEndpoint().getEntity(Person.class), filter.getPersonId())).getId();

    Query query1 = entityManager.createNamedQuery("findCasePersons");
    query1.setParameter("caseId", caseId);
    query1.setParameter("personId", personId);
    Number count1 = (Number)query1.getSingleResult();

    Number count2 = null;
    if (validarSeguretats(Case.class.getName(), Action.I))
    {
      Query query2 = entityManager.createNamedQuery("findInteressats");
      query2.setParameter("caseId", caseId);
      query2.setParameter("personId", personId);
      count2 = (Number)query2.getSingleResult();
    }

    return (count1==null?0:count1.intValue()) + (count2==null?0:count2.intValue());
  }

  public CaseAddress loadCaseAddress(String globalCaseAddressId)
  {
    if (!validarSeguretats(Case.class.getName(), Action.I))
    {
      throw new WebServiceException("case:NOT_ALLOWED");
    }

    String caseAddressId = getEndpoint().toLocalId(CaseAddress.class , globalCaseAddressId);
    VersionIdentifier vId = new VersionIdentifier(caseAddressId);
    String id[] = vId.getIds();
    
    String caseId = id[0];
    String addressId = id[1];
    
    Query query = entityManager.createNamedQuery("findCaseAddresses");
    query.setParameter("caseId", caseId);
    query.setParameter("addressId", addressId);
    query.setFirstResult(0);
    query.setMaxResults(1);

    List<DBCaseAddress> listDbCaseAddress = (List<DBCaseAddress>)query.getResultList();
    if (listDbCaseAddress!=null && listDbCaseAddress.size()>0)
    {
      DBCaseAddress dbCaseAddress = listDbCaseAddress.listIterator().next();
      CaseAddress caseAddress = new CaseAddress();
      dbCaseAddress.copyTo(getEndpoint(), caseAddress);
      return caseAddress;
    }

    if (!validarSeguretats(CaseAddress.class.getName(), Action.I))
    {
      throw new WebServiceException("case:NOT_ALLOWED");
    }

    Query query2 = entityManager.createNamedQuery("findCaseAddressCases");
    query2.setParameter("caseId", caseId);
    query2.setParameter("addressId", addressId);
    query2.setFirstResult(0);
    query2.setMaxResults(1);

    List<DBCase> listDbCase = (List<DBCase>)query2.getResultList();
    if (listDbCase!=null && listDbCase.size()>0)
    {
      DBCase dbCase = listDbCase.listIterator().next();
      CaseAddress caseAddress = new CaseAddress();
      dbCase.copyTo(getEndpoint(), caseAddress);
      return caseAddress;
    }
    return null;
  }

  public CaseAddress storeCaseAddress(CaseAddress caseAddress)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean removeCaseAddress(String caseAddressId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public List<CaseAddressView> findCaseAddressViews(CaseAddressFilter filter)
  {
    if (!validarSeguretats(CaseAddress.class.getName(), Action.I))
    {
      throw new WebServiceException("case:NOT_ALLOWED");
    }
    try
    {
      log.log(Level.INFO, "findCaseAddressViews caseId:{0} addressId:{1}",
        new Object[]{filter.getCaseId(), filter.getAddressId()});

      //Init return table and its rowlist
      List<CaseAddressView> caseAddressViewList = new ArrayList();

      String localCaseId = getEndpoint().toLocalId(Case.class, filter.getCaseId());
      String localAddressId = getEndpoint().toLocalId(Case.class, filter.getAddressId());

      HashMap<String, CaseAddressView> addressIdMap = new HashMap();

      Query query2 = entityManager.createNamedQuery("findCaseAddressCases");
      query2.setParameter("caseId", localCaseId);
      query2.setParameter("addressId", localAddressId);
      query2.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query2.setMaxResults(maxResults);

      List<DBCase> caseList = query2.getResultList();
      for(DBCase dbCase : caseList )
      {
        if (dbCase.getAddressId()!=null)
        {
          CaseAddressView caseAddressView = new CaseAddressView();
          caseAddressView.setCaseAddressId(
                  PKUtil.makeMatrixPK(
                    getEndpoint().getEntity(CaseAddress.class),
                    dbCase.getCaseId(), dbCase.getAddressId()
                  ));

          Case caseObj = new Case();
          dbCase.copyTo(getEndpoint(), caseObj);
          caseAddressView.setCaseObject(caseObj);

          addressIdMap.put( dbCase.getAddressId(), caseAddressView);
          caseAddressViewList.add(caseAddressView);
        }
      }

      Query query1 = entityManager.createNamedQuery("findCaseAddresses");
      query1.setParameter("caseId", localCaseId);
      query1.setParameter("addressId", localAddressId);
      query1.setFirstResult(filter.getFirstResult());
      maxResults = filter.getMaxResults();
      if (maxResults > 0) query1.setMaxResults(maxResults);

      List<DBCaseAddress> caseAddressList = query1.getResultList();
      for(DBCaseAddress caseAddress : caseAddressList)
      {
        CaseAddressView caseAddressView = new CaseAddressView();
        caseAddressView.setCaseAddressId(
          PKUtil.makeMatrixPK(getEndpoint().getEntity(CaseAddress.class), caseAddress.getCaseId(), caseAddress.getAddressId())
          );
        Case caseObj = new Case();
        DBCase dbCase = caseAddress.getCaseObject();
        dbCase.copyTo(getEndpoint(), caseObj);
        caseAddressView.setCaseObject(caseObj);

        addressIdMap.put(caseAddress.getAddressId(), caseAddressView);

        caseAddressViewList.add(caseAddressView);
      }

      //Invokes external object WS find
      try
      {
        if (addressIdMap.size()>0)
        {
          AddressFilter addressFilter = new AddressFilter();
          addressFilter.getAddressIdList().addAll(addressIdMap.keySet());
          addressFilter.setFirstResult(0);
          addressFilter.setMaxResults(0);

          List<AddressView> addressViewList = getKernelService().getKernelPort().findAddressViews(getEndpoint().toGlobal(AddressFilter.class, addressFilter));
          //Parse WS result and completes the rowlist of the return table
          for (AddressView addressView : addressViewList)
          {
            CaseAddressView caseAddressView
              = addressIdMap.get(getEndpoint().toLocalId(Address.class,addressView.getAddressId()));
            if (caseAddressView != null)
            {
              caseAddressView.setAddressView(getEndpoint().toGlobal(AddressView.class, addressView));
            }
          }
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }

      return caseAddressViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findCaseAddressViews failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public int countCaseAddresses(CaseAddressFilter filter)
  {
    if (!validarSeguretats(CaseAddress.class.getName(), Action.I))
    {
      throw new WebServiceException("case:NOT_ALLOWED");
    }

    try
    {
      log.log(Level.INFO, "findCaseAddressViews caseId:{0} addressId:{1}",
        new Object[]{filter.getCaseId(), filter.getAddressId()});

      //Init return table and its rowlist
      String localCaseId = getEndpoint().toLocalId(Case.class, filter.getCaseId());
      String localAddressId = getEndpoint().toLocalId(Case.class, filter.getAddressId());

      Query query1 = entityManager.createNamedQuery("countCaseAddressCases");
      query1.setParameter("caseId", localCaseId);
      query1.setParameter("addressId", localAddressId);
      query1.setFirstResult(0);
      query1.setMaxResults(1);

      Number result1 = (Number)query1.getSingleResult();

      Query query2 = entityManager.createNamedQuery("countCaseAddresses");
      query2.setParameter("caseId", localCaseId);
      query2.setParameter("addressId", localAddressId);
      query2.setFirstResult(0);
      query2.setMaxResults(1);

      Number result2 = (Number)query2.getSingleResult();

      return result1.intValue() + result2.intValue();
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findCaseAddressViews failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public CaseDocument loadCaseDocument(String globalCaseDocumentId)
  {
    try
    {
      String caseDocId = getEndpoint().toLocalId(CaseDocument.class, globalCaseDocumentId);

      log.log(Level.INFO, "loadCaseDocument caseDocumentId:{0}", new Object[]{caseDocId});

      CaseDocument caseDocument = null;
      if (caseDocId != null)
      {
        Query query = entityManager.createNamedQuery("findCaseDocument");
        query.setFirstResult(0);
        query.setMaxResults(1);

        String [] ids = PKUtil.decomposePK(caseDocId);

        query.setParameter("caseId", (ids==null)?null:ids[0]);
        query.setParameter("aplcod", (ids==null)?null:ids[1]);
        query.setParameter("docorigen", (ids==null)?null:ids[2]);
        query.setParameter("doccod", (ids==null)?null:ids[3]);
        query.setParameter("modelcod", (ids==null)?null:ids[4]);
        query.setParameter("docnompc", (ids==null)?null:ids[5]);
        query.setParameter("identificador", (ids==null)?null:ids[6]);

        List<DBCaseDocument> dbCaseDocumentList = query.getResultList();
        if (dbCaseDocumentList.size()>0)
        {
          caseDocument = new CaseDocument();
          dbCaseDocumentList.get(0).copyTo( getEndpoint(), caseDocument);
        }

      }
      return caseDocument;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadCaseDocument failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public CaseDocument storeCaseDocument(CaseDocument caseDocument)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public boolean removeCaseDocument(String caseDocumentId)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public List<CaseDocumentView> findCaseDocumentViews(CaseDocumentFilter globalFilter)
  {
    CaseDocumentFilter filter = getEndpoint().toLocal(CaseDocumentFilter.class, globalFilter);
    
    String allowNativeQueries = 
      ConfigProperties.getProperty("com.audifilm.matrix.cases.allowNativeSQL", "false");

    boolean isNative = Boolean.valueOf(allowNativeQueries);
    Query query = null;
    if (!isNative)
      query = entityManager.createNamedQuery("findCaseDocumentViews");
    else
      query = entityManager.createNamedQuery("findCaseAndResDocumentViews");
    
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());

    query.setParameter("caseId", filter.getCaseId());
    query.setParameter("docId", filter.getDocId());
    
    List<CaseDocumentView> documentViewList = new ArrayList<CaseDocumentView>();
    
    FindCaseDocumentViewsList dbDocumentList = 
      new FindCaseDocumentViewsList(isNative, query.getResultList());
    DocumentFilter documentFilter = new DocumentFilter();

    List<Object []> docViews = new ArrayList<Object[]>();
    for(Object [] row : dbDocumentList)
    {
      DBCase dbCaseObj = (DBCase)row[0];
      DBCaseDocument dbCaseDocument = (DBCaseDocument)row[1];
      

      CaseDocumentView docView = new CaseDocumentView();

      docView.setCaseDocId(
              getEndpoint().toGlobalId(CaseDocument.class,
              dbCaseDocument.getCaseDocumentId()));

      Case globalCase = new Case();
      dbCaseObj.copyTo(getEndpoint(), globalCase);
      docView.setCaseObject(globalCase);
      
      //docView.setCaseDocTypeId(DictionaryConstants.CASE_DOCUMENT_TYPE);      
      if (REGENT.equals(dbCaseDocument.getDocorigen()))
        docView.setCaseDocTypeId(CaseDocumentType.Types.RegistreEntrada.getTypeId());
      else if (REGSOR.equals(dbCaseDocument.getDocorigen()))
        docView.setCaseDocTypeId(CaseDocumentType.Types.RegistreSortida.getTypeId());
      else
        docView.setCaseDocTypeId(DictionaryConstants.CASE_DOCUMENT_TYPE);  
      CaseDocumentView globalDocView = getEndpoint().toGlobal(CaseDocumentView.class, docView);

      documentFilter.getDocId().add(dbCaseDocument.getDocId()); //


      documentViewList.add(globalDocView);

      docViews.add(new Object[] {dbCaseDocument.getDocId(), globalDocView});
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

        Hashtable<String, Document> docs = new Hashtable<String, Document>();

        //Parse WS result and completes the rowlist of the return table
        for (Document document : documentList)
        {
          String localDocId = getEndpoint().toLocalId(Document.class, document.getDocId());
          docs.put(localDocId, document);
        }

        for(Object [] docIdCaseDocumentView : docViews )
        {
          String docId = (String)docIdCaseDocumentView[0];
          CaseDocumentView view = (CaseDocumentView)docIdCaseDocumentView[1];
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

  public int countCaseDocuments(CaseDocumentFilter globalFilter)
  {
    CaseDocumentFilter filter = getEndpoint().toLocal(CaseDocumentFilter.class, globalFilter);

    Query query = entityManager.createNamedQuery("findCaseDocumentViews");
    query.setFirstResult(0);
    query.setMaxResults(1);

    query.setParameter("caseId", filter.getCaseId());
    query.setParameter("docId", filter.getDocId());
    
    return query.getResultList().size();

//    Number count = (Number)query.getSingleResult();
//    return count.intValue();
  }





  public CaseCase loadCaseCase(String globalCaseCaseId)
  {
    String caseCaseId =
            getEndpoint().toLocalId(CaseCase.class, globalCaseCaseId);

    DBCaseCasePK caseCasePK = new DBCaseCasePK(caseCaseId);

    DBCaseCase dbCaseCase = entityManager.find(DBCaseCase.class, caseCasePK);
    if (dbCaseCase == null)  
    {
      throw new WebServiceException("case:CASE_NOT_FOUND");
    }

    CaseCase caseCase = new CaseCase();
    dbCaseCase.copyTo(getEndpoint() , caseCase);
    return caseCase;
  }

  public CaseCase storeCaseCase(CaseCase caseCase)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public boolean removeCaseCase(String caseCaseId)
  {
    return false;
  }

  public List<CaseCaseView> findCaseCaseViews(CaseCaseFilter globalFilter)
  {
    CaseCaseFilter filter = getEndpoint().toLocal(CaseCaseFilter.class, globalFilter);

    List<CaseCaseView> caseCaseViewList = new ArrayList<CaseCaseView>();

    Query query = entityManager.createNamedQuery("findCaseCases");
    query.setParameter("caseId" , filter.getCaseId());
    query.setParameter("relCaseId" , filter.getRelCaseId());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults((filter.getMaxResults()==0)? 100: filter.getMaxResults());

    List<Object[]> dbList = query.getResultList();
    for (Object[] row : dbList)
    {
      DBCaseCase dbCaseCase = (DBCaseCase)row[0];
      DBCase dbMainCase = (DBCase)row[1];
      DBCase dbRelCase = (DBCase)row[2];

      Case mainCase = new Case();
      dbMainCase.copyTo(getEndpoint(), mainCase);

      Case relCase = new Case();
      dbRelCase.copyTo(getEndpoint(), relCase);

      CaseCaseView caseCaseView = new CaseCaseView();
      caseCaseView.setMainCase(mainCase);
      caseCaseView.setRelCase(relCase);
      caseCaseView.setComments("");
      caseCaseView.setCaseCaseTypeId(DictionaryConstants.CASE_CASE_TYPE);

      caseCaseView.setCaseCaseId(
          PKUtil.makeMatrixPK(
            getEndpoint().getEntity(CaseCase.class),
            dbCaseCase.getCaseId(),
            dbCaseCase.getRelCaseId()));

      caseCaseViewList.add(caseCaseView);
    }

    return caseCaseViewList;
  }

  public int countCaseCases(CaseCaseFilter globalFilter)
  {
    CaseCaseFilter filter = getEndpoint().toLocal(CaseCaseFilter.class, globalFilter);

    Query query = entityManager.createNamedQuery("countCaseCases");
    query.setParameter("caseId" , filter.getCaseId());
    query.setParameter("relCaseId" , filter.getRelCaseId());

    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  

  private void setCasePersonFilterParameters(Query query, CasePersonFilter filter)
  {
    query.setParameter("caseId", getEndpoint().toLocalId(Case.class, filter.getCaseId()));
    query.setParameter("personId", getEndpoint().toLocalId(CasePerson.class, filter.getPersonId()));

    query.setFirstResult(filter.getFirstResult());
    int maxResults = filter.getMaxResults();
    query.setMaxResults(maxResults == 0 ? 100 : maxResults);
  }




  public int countInterventions(InterventionFilter filter)
  {
    return 0;
  }

  public int countProblems(ProblemFilter filter)
  {
    return 0;
  }

  public int countDemands(DemandFilter filter)
  {
    return 0;
  }


  private List<Property> getProperiesList(Object obj, String prefix)
  {
    List<Property> propertiesList = new ArrayList();

    Class cls = obj.getClass();

    Field [] fields = cls.getDeclaredFields();

    for(Field f : fields) {
      try
      {
        Method m = cls.getMethod("get" + f.getName().substring(0,1).toUpperCase() + f.getName().substring(1));
        Object value = m.invoke(obj);

        propertiesList.add(DBCaseProperty.getInstance(prefix + "_" + f.getName(), (value==null?null:value.toString())));
      }
      catch(Exception ex)
      {
        //Logger.getLogger(TextUtil.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return propertiesList;
  }

  public List<String> findCaseVolumes(String caseId)
  {
    return Collections.EMPTY_LIST;
  }

  public InterventionProblem loadInterventionProblem(String intProbId)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public InterventionProblem storeInterventionProblem(InterventionProblem interventionProblem)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public boolean removeInterventionProblem(String intProbId)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public int countInterventionProblems(InterventionProblemFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public List<InterventionProblemView> findInterventionProblemViews(InterventionProblemFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  private enum EstrategiaComptador {
          DARRER_ASSIGNAT, //indica que el valor actual del comptador es el darrer n?mero que s'ha assignat.
          PROPER_PER_ASSIGNAR; //indica que el valor actual es el proper que s'ha d'assignar.
  };


  private int getNextCounterValue(String claupref, String claucod,
    String clauorigen, String claudesc,EstrategiaComptador estrategiaComptador)
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    User user = UserCache.getUser(credentials);
 
    int counter;

    try {
      // update counter
      Date now = new Date();
      Query query = entityManager.createNamedQuery("incrementGenericCounter");
      query.setParameter("claupref", claupref);
      query.setParameter("claucod", claucod);
      query.setParameter("clauorigen", clauorigen);
      query.setParameter("userId", user.getUserId());
      query.setParameter("day", TextUtil.toStringDate(now));
      query.setParameter("hour", TextUtil.toStringTime(now));
      int numUpdated = query.executeUpdate();
      if (numUpdated == 1)
      {
        query = entityManager.createNamedQuery("readGenericCounter");
        query.setParameter("claupref", claupref);
        query.setParameter("claucod", claucod);
        query.setParameter("clauorigen", clauorigen);
        Number value = (Number)query.getSingleResult();
        counter = value.intValue();
        return (estrategiaComptador.equals(EstrategiaComptador.PROPER_PER_ASSIGNAR))?
          counter - 1:counter;
      }
      else // counter row do not exists, then create it.
      {
        counter = 1; // starting value
        DBCounter dbCounter = new DBCounter();
        dbCounter.setClaupref(claupref);
        dbCounter.setClaucod(claucod);
        dbCounter.setClauorigen(clauorigen);
        dbCounter.setClaudesc(claudesc);
        dbCounter.setCounter(
           (estrategiaComptador.equals(EstrategiaComptador.PROPER_PER_ASSIGNAR))?
              counter+1:counter);
        dbCounter.setAuditoriaCreacio(user.getUserId());
        entityManager.persist(dbCounter);
        return counter;
      }
    }
    catch(Exception ex)
    {
      log.log(Level.SEVERE, "getNextCounterValue failed");
      return 0;
    }
  }

  private String getNextCaseId()
  {
    String anyActual = TextUtil.toStringDate().substring(0,4);
    int number = getNextCounterValue( 
            "NEXP" + anyActual, "NEXP" + anyActual, "SDE",
            "Numero Expedient", EstrategiaComptador.PROPER_PER_ASSIGNAR);
    return "X" + anyActual + TextUtil.formatAlineatEsq(number, 6);
  }
  private String getNextActaId()
  {
    String anyActual = TextUtil.toStringDate().substring(0,4);
    int number = getNextCounterValue( 
            "NEXP" + anyActual, "NEXP" + anyActual, "SDE",
            "Numero Expedient", EstrategiaComptador.PROPER_PER_ASSIGNAR);
    return "A" + anyActual + TextUtil.formatAlineatEsq(number, 6);
  }

  private String getNextNumExpedientParticular(String typeId)
  {
    String anyActual = TextUtil.toStringDate().substring(0,4);
    int number = getNextCounterValue( 
            anyActual + typeId, anyActual + typeId, "SDE",
            "Numero Expedient particular", EstrategiaComptador.PROPER_PER_ASSIGNAR);
    return typeId + anyActual + TextUtil.formatAlineatEsq(number, 6);

  }

  public Intervention loadIntervention(String intId)
  {
    return null;
  }

  public Intervention storeIntervention(Intervention intervention)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean removeIntervention(String intId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public List<InterventionView> findInterventionViews(InterventionFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Problem loadProblem(String probId)
  {
    return null;
  }

  public Problem storeProblem(Problem problem)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean removeProblem(String probId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public List<ProblemView> findProblemViews(ProblemFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Demand loadDemand(String demandId)
  {
    return null;
  }

  public Demand storeDemand(Demand demand)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean removeDemand(String demandId)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public List<Demand> findDemands(DemandFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public CaseEvent loadCaseEvent(String caseEventId)
  {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  public CaseEvent storeCaseEvent(CaseEvent caseEvent)
  {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  public boolean removeCaseEvent(String caseEventId)
  {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  public int countCaseEvents(CaseEventFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet."); 
  }

  public List<CaseEventView> findCaseEventViews(CaseEventFilter filter)
  {
    throw new UnsupportedOperationException("Not supported yet."); 
  }  
  
  private boolean validarSeguretats(String module, Action accio)
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);

    User user = UserCache.getUser(credentials);
    if (user == null) return false;


    return isUserAllowed(accio.getActionName());

  }


  private boolean validarSeguretats(DBCase dbCase) throws WebServiceException
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    org.santfeliu.security.User user = UserCache.getUser(credentials);

    if (user==null) throw new WebServiceException("case:NOT_ALLOWED");
    if (user.isInRole(getCaseAdminRoleId())) return true;

    if (validarSeguretats(dbCase.getCaseTypeId())) return true;
    
    if (!validarSeguretats(Case.class.getName(), Action.I))
    {
      if (!validarSeguretats(dbCase.getCaseTypeId()))
        throw new WebServiceException("case:NOT_ALLOWED");
    }


    List<DBGrupUsuari> grups = getSecurityManager().findGrupsUsuari(user.getUserId() , null, null, null);

    if (grups.size()>0) {
        StringBuilder areaGrupDeps = new StringBuilder();
        boolean primer=true;
        for(DBGrupUsuari grup: grups) {

          areaGrupDeps.append(primer?"'":", '");
          areaGrupDeps.append(grup.getAreaId().trim());
          areaGrupDeps.append(grup.getDepartamentId().trim());
          areaGrupDeps.append(grup.getGrupId().trim());
          areaGrupDeps.append("'");

          primer = false;
        }
        StringBuilder whereSecurity = new StringBuilder();
        whereSecurity.append("SELECT count(distinct cs.caseId) FROM CaseState cs ");
        //whereSecurity.append(" LEFT JOIN cs.responsibles csr ");
        whereSecurity.append(" WHERE cs.caseId = :caseId ");
        whereSecurity.append(" AND (concat(trim(cs.areaId),concat(trim(cs.departamentId),trim(cs.grupId))) IN (").append(areaGrupDeps).append(")");
        //whereSecurity.append(" OR concat(trim(csr.areaId),concat(trim(csr.departamentId),trim(csr.grupId))) IN (").append(areaGrupDeps).append(")");
        whereSecurity.append(" )");

        JPAQuery query = new JPAQuery(entityManager.createQuery(whereSecurity.toString()));
        try
        {
          query.setParameter("caseId", dbCase.getCaseId());
        }
        catch (Exception ex)
        {
          Logger.getLogger(CaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        Number result = query.getResultCount();
        if (result.longValue()<1) throw new WebServiceException("case:NOT_ALLOWED");
    }


    return true;
  }


  private boolean validarSeguretats(String caseTypeId)
  {
    List<String> roles = UserCache.getUser(wsContext).getRolesList();
    for (String role : roles)
    {
      if (RoleTypes.containsType(role, caseTypeId))
        return true;
    }
    return false;
  }




  private List<AccessControl> loadAccessControlList(DBCase dbCase)
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    User user = UserCache.getUser(credentials);
    if (user == null) return Collections.emptyList();

    List<AccessControl> accessControlList =
        getSecurityManager().findAccessControlList(
              Case.class.getName(),
              user.getUserId());

    if (accessControlList==null) {
      accessControlList = new ArrayList<AccessControl>();

      String caseAdminRoleId = getCaseAdminRoleId();

      AccessControl accessControl = new AccessControl();
      accessControl.setAction(DictionaryConstants.READ_ACTION);
      accessControl.setRoleId(caseAdminRoleId);
      accessControlList.add(accessControl);

      accessControl = new AccessControl();
      accessControl.setAction(DictionaryConstants.CREATE_ACTION);
      accessControl.setRoleId(caseAdminRoleId);
      accessControlList.add(accessControl);

      accessControl = new AccessControl();
      accessControl.setAction(DictionaryConstants.WRITE_ACTION);
      accessControl.setRoleId(caseAdminRoleId);
      accessControlList.add(accessControl);
    }

    return accessControlList;
  }

  private boolean isUserAllowed(String action)
  {
    User user = UserCache.getUser(wsContext);
    if (user==null) return false;

    if (UserCache.getUser(wsContext).isInRole(getCaseAdminRoleId())) {
      return true;
    } else {
      return getSecurityManager().canDoAction(user.getUserId(),  org.matrix.cases.Case.class.getName(), action);
    }
  }


  private boolean isUserCaseAdmin()
  {
    User user = UserCache.getUser(wsContext);
    if (user==null) return false;

    return (UserCache.getUser(wsContext).isInRole(getCaseAdminRoleId()));
  }




  private List<String> findCaseClassId(String caseId)
  {
    Query query = entityManager.createNamedQuery("findCaseClassId");
    query.setFirstResult(0);
    query.setMaxResults(0);
    query.setParameter("caseId", caseId);

    return (List<String>)query.getResultList();
  }


  public static String getFieldExpression(String field, String defaultExpression) {
    return ConfigProperties
            .getProperty("com.audifilm.matrix.cases.service.CaseManager." + field,
            defaultExpression);
  }


  public static String evalFieldExpression(String field, Object cas, String defaultExpression, String defaultValue) {
    String expression = getFieldExpression(field, defaultExpression);
    if (expression==null) return defaultValue;
    
    return evalExpression(expression, cas, defaultValue);
  }

  public static String evalExpression(String expression, Object cas, String defaultValue) {
    if (expression == null || expression.equals("")) return defaultValue;

    StringBuilder sbValue = new StringBuilder();
    Pattern pattern = Pattern.compile("\\$\\{([\\w.]+)\\}");
    Matcher matcher = pattern.matcher(expression);

    boolean found = false;
    int lastIndex = 0;
    while (matcher.find(lastIndex)) {
        String exp = matcher.group();
        if (lastIndex<matcher.start()) {
          sbValue.append(expression.substring(lastIndex, matcher.start()));
        }
        String valueExp = matcher.group(1);
        if (valueExp.equals("value")) {
          sbValue.append(defaultValue);
        } else {
          sbValue.append(TextUtil.encodeEmpty(getValueOfField(matcher.group(1), cas)));
        }
        lastIndex = matcher.end();
    }
    if (lastIndex<expression.length()) sbValue.append(expression.substring(lastIndex));
    return sbValue.toString();
  }

  public static String getValueOfField(String field,Object obj ) {
    Class cls;
    if (field.startsWith("c.")) {
      cls = obj.getClass();
    } else {
      return field;
    }

    String fieldName = field.substring(2);
    try {
      Object value = cls.getMethod(fieldName).invoke(obj);
      return (value==null)?"":value + "";
    } catch(Exception ex) {
      System.err.println("GetValueOfField: " + field + " " + ex.getMessage());
      ex.printStackTrace();
      return null;
    }
  }

  public String getCaseAdminRoleId() {
    return ConfigProperties.getProperty("com.audifilm.matrix.security.adminRoleId", "CASE_ADMIN");
  }
  
  private class FindCaseDocumentViewsList extends AbstractList<Object[]>
  {
    private final boolean nativeQuery;
    private final List results;

    public FindCaseDocumentViewsList(boolean nativeQuery, List results)
    {
      this.nativeQuery = nativeQuery;
      this.results = results;
    }

    public int size()
    {
      return results.size();
    }

    public Object[] get(int index)
    {
      if (!nativeQuery)
        return (Object[]) results.get(index);
      else
      {
        Object[] result = new Object[2];
        Object[] row = (Object[]) results.get(index);
        DBCase dbCase = new DBCase();
        dbCase.setCaseId((String) row[0]);
        dbCase.setCaseTypeId((String) row[1]);
        dbCase.setCaseTypeNum((String) row[2]);
        dbCase.setSdetext((String) row[3]);
        dbCase.setRegistryDate((String) row[4]);
        dbCase.setRegistryTime((String) row[5]);
        dbCase.setStdugr((String) row[6]);
        dbCase.setStdumod((String) row[7]);
        dbCase.setStddgr((String) row[8]);
        dbCase.setStddmod((String) row[9]);
        dbCase.setStdhgr((String) row[10]);
        dbCase.setStdhmod((String) row[11]);
        result[0] = dbCase;

        DBCaseDocument dbCaseDocument = new DBCaseDocument();
        dbCaseDocument.setAplcod((String) row[12]);
        dbCaseDocument.setDocorigen((String) row[13]);
        dbCaseDocument.setDoccod((String) row[14]);
        dbCaseDocument.setModelcod((String) row[15]);
        dbCaseDocument.setDocnompc((String) row[16]);
        dbCaseDocument.setIdentificador((String) row[17]);
        dbCaseDocument.setDocId(String.valueOf(row[18]));
        dbCaseDocument.setCaseId(row[19] != null ? String.valueOf(row[19]) : null);
        result[1] = dbCaseDocument;

        return result;
      } 
    } 
  }
}

