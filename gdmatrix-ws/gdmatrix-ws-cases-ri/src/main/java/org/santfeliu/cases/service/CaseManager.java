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
package org.santfeliu.cases.service;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AgendaConstants;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.AgendaManagerService;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.cases.Case;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CaseConstants;
import org.matrix.cases.Demand;
import org.matrix.cases.DemandFilter;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseEvent;
import org.matrix.cases.CaseEventFilter;
import org.matrix.cases.CaseEventView;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.cases.Problem;
import org.matrix.cases.ProblemFilter;
import org.matrix.cases.ProblemView;
import org.matrix.dic.Property;
import org.matrix.cases.CaseMetaData;
import org.matrix.cases.InterventionProblem;
import org.matrix.cases.InterventionProblemFilter;
import org.matrix.cases.InterventionProblemView;
import org.matrix.security.AccessControl;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;
import org.matrix.security.SecurityConstants;
import org.matrix.util.ExternalEntity;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.service.DBType;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.util.WSTypeValidator;
import org.santfeliu.jpa.JPA;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.audit.Auditor;
import org.santfeliu.util.keywords.KeywordsManager;
import org.santfeliu.ws.WSExceptionFactory;
import org.santfeliu.ws.WSUtils;


/**
 *
 * @author unknown
 */
@WebService(endpointInterface = "org.matrix.cases.CaseManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class CaseManager implements CaseManagerPort
{
  @Resource
  WebServiceContext wsContext;
  
  @PersistenceContext
  public EntityManager em;
  
  protected static final Logger log = Logger.getLogger("Cases");  
  
  public static final String PK_SEPARATOR = ";";

  private WSEndpoint endpoint;

  public CaseManager()
  {
  }

  public CaseMetaData getCaseMetaData()
  {
    CaseMetaData metaData = new CaseMetaData();
    return metaData;
  }

  public Case loadCase(String caseId)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      log.log(Level.INFO, "loadCase caseId:{0}",
        new Object[]{caseId});

      if (caseId == null)
        throw new Exception("cases:CASEID_IS_MANDATORY");

      DBCase dbCase = em.find(DBCase.class, caseId);
      if (dbCase == null)
        throw new Exception("cases:CASE_NOT_FOUND");

      //AccessControl
      List<DBAccessControl> dbAccessControlList = loadAccessControlList(caseId);
      dbCase.getAccessControl().addAll(dbAccessControlList);

      if (!canUserReadCase(user, dbCase))
        throw new Exception("cases:NOT_AUTHORIZED_TO_LOAD_CASE");

      //Properties
      List<DBCaseProperty> dbProperties = 
              loadDBProperties("loadCaseProperties", caseId);
      List<Property> properties = toProperties(dbProperties);
      dbCase.getProperty().addAll(properties);

      //Add classId
      for (Property prop : properties)
      {
        if ("classId".equals(prop.getName()))
          dbCase.getClassId().addAll(prop.getValue());
      }

      return dbCase;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadCase failed");
      throw WSExceptionFactory.create(ex);
    }
  }

  public Case storeCase(Case caseObject)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      log.log(Level.INFO, "storeCase caseId:{0}",
        new Object[]{caseObject.getCaseId() != null ?
          caseObject.getCaseId() : "NEW"});

      Type type = getCaseType(caseObject);

      validateCase(caseObject, type);

      String caseId = caseObject.getCaseId();

      DBCase dbCase = null;
      boolean isNew = (caseId == null);
      if (isNew) //NEW
      {
        if (!canUserCreateCase(user, caseObject))
          throw new Exception("cases:NOT_AUTHORIZED_TO_CREATE_CASE");

        dbCase = new DBCase(caseObject);
        Auditor.auditCreation(dbCase, user.getUserId());

        em.persist(dbCase);
        caseId = dbCase.getCaseId();
        em.flush();

        addNominalRoles(user, dbCase);

        //Generate typeCaseId if needed (@deprecated)
        String typeCaseId = generateTypeCaseId(type);
        if (typeCaseId != null)
        {
          PojoUtils.setDynamicProperty(dbCase.getProperty(),
            CaseConstants.TYPECASEID, typeCaseId, Property.class);
        }
        //Store properties
        createCaseProperties(dbCase);
        //Store AccessList
        storeAccessControlList(dbCase, null);        
      }
      else //UPDATE
      {
        dbCase = em.find(DBCase.class, caseId);

        if (!canUserModifyCase(user, dbCase))
          throw new Exception("cases:NOT_AUTHORIZED_TO_MODIFY_CASE");
        
        List<DBAccessControl> dbCurrentACL = loadAccessControlList(caseId);
        dbCase.copyFrom(caseObject);
        Auditor.auditChange(dbCase, user.getUserId());
        em.merge(dbCase);
        
        //Store properties (merge)
        updateCaseProperties(dbCase);
        //Store AccessList
        storeAccessControlList(dbCase, dbCurrentACL);
        //Update closing data
        if (mustCloseRelatedCases(dbCase, type))
        {
          Query query = em.createNamedQuery("closeCaseCases");
          query.setParameter("caseId", caseId);
          query.setParameter("endDate", dbCase.getEndDate());
          query.executeUpdate();
        }
      }
      return dbCase;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeCase failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  private boolean mustCloseRelatedCases(DBCase dbCase, Type type)
  {
    if (dbCase.getEndDate() == null) return false;
    PropertyDefinition propDef =
      type.getPropertyDefinition("_closeRelatedCasesOnClose");
    return (propDef != null && !propDef.getValue().isEmpty() &&
      "true".equals(propDef.getValue().get(0)));
  }

  public boolean removeCase(String caseId)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      log.log(Level.INFO, "removeCase caseId:{0}",
        new Object[]{caseId});

      if (caseId == null) return false;

      DBCase dbCase = em.find(DBCase.class, caseId);
      if (!canUserDeleteCase(user, dbCase))
        throw new Exception("cases:NOT_AUTHORIZED_TO_DELETE_CASE");

      removeCaseProperties(caseId);

      removeCaseCases(caseId);

      Query query = em.createNamedQuery("removeCaseAccessControl");
      query.setParameter("caseId", caseId);
      query.executeUpdate();

      query = em.createNamedQuery("removeCasePersons");
      query.setParameter("caseId", caseId);
      query.executeUpdate();

      query = em.createNamedQuery("removeCaseAddresses");
      query.setParameter("caseId", caseId);
      query.executeUpdate();

      query = em.createNamedQuery("removeDemands");
      query.setParameter("caseId", caseId);
      query.executeUpdate();

      query = em.createNamedQuery("removeCaseDocuments");
      query.setParameter("caseId", caseId);
      query.executeUpdate();

      query = em.createNamedQuery("findInterventions");
      query.setParameter("caseId", caseId);
      List<DBIntervention> dbInterventionList = query.getResultList();
      for (DBIntervention dbIntervention : dbInterventionList)
      {
        query = em.createNamedQuery("removeInterventionProperties");
        query.setParameter("id", dbIntervention.getIntId());
        query.executeUpdate();
        
        query = em.createNamedQuery("removeInterventionProblems");
        query.setParameter("intId", dbIntervention.getIntId());
        query.executeUpdate();
        
        em.remove(dbIntervention);
      }
      
      query = em.createNamedQuery("findCaseEvents");
      query.setParameter("caseId", caseId);
      query.setParameter("eventId", null);
      query.setParameter("caseEventTypeId", null);
      List<DBCaseEvent> dbCaseEventList = query.getResultList();
      for (DBCaseEvent dbCaseEvent : dbCaseEventList)
      {
        query = em.createNamedQuery("removeCaseEventProperties");
        query.setParameter("id", dbCaseEvent.getCaseEventId());
        query.executeUpdate();
        em.remove(dbCaseEvent);
      }      
      
      query = em.createNamedQuery("removeProblems");
      query.setParameter("caseId", caseId);
      query.executeUpdate();      

      if (dbCase == null) return false;
      em.remove(dbCase);

      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeCase failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public List<Case> findCases(CaseFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findCases");
      
      validateCaseFilter(filter);

      String caseTypeId = filter.getCaseTypeId();
      if (!StringUtils.isBlank(caseTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(caseTypeId));
        filter.setCaseTypeId(type.getTypePath());
      }

      return doFindCases(filter);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findCases failed");
        throw WSExceptionFactory.create(ex);
    }
  }
  
  private List<Case> doFindCases(CaseFilter filter) throws Exception
  {
    FindCasesQueryBuilder queryBuilder = FindCasesQueryBuilder.getInstance();
    queryBuilder.setFilter(filter);
    queryBuilder.setUserRoles(UserCache.getUser(wsContext).getRolesList());
    queryBuilder.setCounterQuery(false);
    Query query = queryBuilder.getQuery(em);
    List<DBCase> dbCaseList = query.getResultList();

    List<Case> caseList = new ArrayList<Case>();
    JPAUtils.copyList(dbCaseList, caseList, Case.class);    

    return caseList;
  }
  
  public int countCases(CaseFilter filter)
  {
    try
    {
      log.log(Level.INFO, "countCases");

      String caseTypeId = filter.getCaseTypeId();
      if (!StringUtils.isBlank(caseTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(caseTypeId));
        filter.setCaseTypeId(type.getTypePath());
      }
    
      return doCountCases(filter);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "countCases failed");
        throw WSExceptionFactory.create(ex);
    }
  }
  
  public int doCountCases(CaseFilter filter) throws Exception
  {
    FindCasesQueryBuilder queryBuilder = FindCasesQueryBuilder.getInstance();
    queryBuilder.setFilter(filter);
    queryBuilder.setUserRoles(UserCache.getUser(wsContext).getRolesList());
    queryBuilder.setCounterQuery(true);
    Query query = queryBuilder.getQuery(em);
    return ((Number)query.getSingleResult()).intValue();    
  }

  public CasePerson loadCasePerson(String casePersonId)
  {
    try
    {
      log.log(Level.INFO, "loadCasePerson casePersonId:{0}",
        new Object[]{casePersonId});

      CasePerson casePerson = null;

      if (casePersonId != null)
      {
        DBCasePerson dbCasePerson =
          em.find(DBCasePerson.class, casePersonId);

        if (dbCasePerson != null)
        {
          casePerson = new CasePerson();
          dbCasePerson.copyTo(casePerson);
          casePerson.setCasePersonId(casePersonId);
        }
      }

      return casePerson;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadCasePerson failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public CasePerson storeCasePerson(CasePerson casePerson)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      String casePersonId = casePerson.getCasePersonId();
      log.log(Level.INFO, "storeCasePerson casePersonId:{0}",
        new Object[]{casePersonId != null ? casePersonId : "NEW"});

      validateCasePerson(casePerson);

      DBCasePerson dbCasePerson = null;
      if (casePersonId == null) //insert
      {
        dbCasePerson = new DBCasePerson(casePerson);
        Auditor.auditCreation(dbCasePerson, user.getUserId());
        em.persist(dbCasePerson);
      }
      else //update
      {
        //Needs to load creation audit properties
        dbCasePerson = em.getReference(DBCasePerson.class, casePersonId);
        dbCasePerson.copyFrom(casePerson);
        Auditor.auditChange(dbCasePerson, user.getUserId());
        em.merge(dbCasePerson);
      }

      return dbCasePerson;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeCasePerson failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeCasePerson(String id)
  {
    try
    {
      log.log(Level.INFO, "removeCasePerson casePersonId:{0}",
        new Object[]{id});

      DBCasePerson dbCasePerson = em.getReference(DBCasePerson.class, id);
      if (dbCasePerson == null)
        return false;
      em.remove(dbCasePerson);

      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeCasePerson failed");
        throw WSExceptionFactory.create(ex);
    }
  }
  
  public List<CasePersonView> findCasePersonViews(CasePersonFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findCasePersonViews caseId:{0} personId:{1}",
        new Object[]{filter.getCaseId(), filter.getPersonId()});

      if (StringUtils.isBlank(filter.getCaseId()) && StringUtils.isBlank(filter.getPersonId()))
        throw new Exception("FILTER_NOT_ALLOWED");
        
      List<CasePersonView> casePersonViewList = new ArrayList();

      String casePersonTypeId = filter.getCasePersonTypeId();
      if (!StringUtils.isBlank(casePersonTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(casePersonTypeId));
        filter.setCasePersonTypeId(type.getTypePath() + "%");
      }
      
      //execute DB findQuery
      Query query = setQueryParameters("findCasePersons", filter);

      List<DBCasePerson> dbRowList = query.getResultList();

      if (dbRowList != null && !dbRowList.isEmpty())
      {
        //Only cases with user read privileges are allowed. FindCases method 
        //returns this allowed cases.
        Set<String> allCaseIds = new HashSet();
        Set<String> allowedCaseIds = new HashSet();
        
        //populate caseId filter
        for (DBCasePerson dbRow : dbRowList)
        {
          allCaseIds.add(dbRow.getCaseId());
        }
        
        //findCases
        if (allCaseIds != null && !allCaseIds.isEmpty())
        {
          CaseFilter caseFilter = new CaseFilter();
          caseFilter.getCaseId().addAll(allCaseIds);
          List<Case> cases = doFindCases(caseFilter);
          //populate only allowed caseIds set
          for (Case cas : cases)
          {
            allowedCaseIds.add(cas.getCaseId());
          }
        }        
        
        //Parse Query result and set row fields (View)
        HashMap<String, List<CasePersonView>> personIdMap = new HashMap();
        int firstResult = filter.getFirstResult();
        int maxResults = filter.getMaxResults();
        if (maxResults == 0) maxResults = Integer.MAX_VALUE;
        int index = 0;
        int added = 0;          
        for(DBCasePerson dbCasePerson : dbRowList)
        {
          if (index++ >= firstResult && added < maxResults)
          {
            if (allowedCaseIds.contains(dbCasePerson.getCaseId()))
            {
              CasePersonView casePersonView = new CasePersonView();
              casePersonView.setCasePersonId(dbCasePerson.getCasePersonId());
              casePersonView.setComments(dbCasePerson.getComments());
              casePersonView.setCasePersonTypeId(dbCasePerson.getCasePersonTypeId());
              casePersonView.setStartDate(dbCasePerson.getStartDate());
              casePersonView.setEndDate(dbCasePerson.getEndDate());

              String caseId = dbCasePerson.getCaseId();
              String personId = dbCasePerson.getPersonId();
              if (personId != null) //Set PersonView
              {
                List list = personIdMap.get(personId);
                if (list == null) list = new ArrayList();
                list.add(casePersonView);
                personIdMap.put(personId, list);
              }
              if (caseId != null) //Set CaseView
              {
                DBCase dbCase = dbCasePerson.getCaseObject();
                Case caseObject = new Case();
                caseObject.setCaseId(caseId);
                caseObject.setTitle(dbCase.getTitle());
                caseObject.setDescription(dbCase.getDescription());
                caseObject.setCaseTypeId(dbCase.getCaseTypeId());
                caseObject.setComments(dbCase.getComments());
                caseObject.setStartDate(dbCase.getStartDate());
                caseObject.setStartTime(dbCase.getStartTime());
                caseObject.setEndDate(dbCase.getEndDate());
                caseObject.setEndTime(dbCase.getEndTime());
                casePersonView.setCaseObject(caseObject);
              }
              casePersonViewList.add(casePersonView);
              if (++added == maxResults) break;
            }
          }
        }

        if (!personIdMap.isEmpty())
        {
          //Invokes kernel WS findPersons
          try
          {
            KernelManagerPort port = getKernelManagerPort();

            PersonFilter personFilter = new PersonFilter();
            personFilter.getPersonId().addAll(personIdMap.keySet());
            personFilter.setFirstResult(0);
            personFilter.setMaxResults(0);
            List<PersonView> personViewList = port.findPersonViews(personFilter);

            //Parse WS result and completes the rowlist of the return table
            for (PersonView personView : personViewList)
            {
              List<CasePersonView> cpvList = personIdMap.get(personView.getPersonId());
              for (CasePersonView casePersonView : cpvList)
              {
                casePersonView.setPersonView(personView);
              }
            }
          }
          catch (Exception e)
          {
            throw new WebServiceException(e);
          }
        }
      }

      return casePersonViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findCasePersonViews failed");
        throw WSExceptionFactory.create(ex);
    }
  }  
  
  public int countCasePersons(CasePersonFilter filter)
  {
    try
    {
      log.log(Level.INFO, "countCasePersons caseId:{0} personId:{1}",
        new Object[]{filter.getCaseId(), filter.getPersonId()});
      
      if (StringUtils.isBlank(filter.getCaseId()) && StringUtils.isBlank(filter.getPersonId()))
        throw new Exception("FILTER_NOT_ALLOWED");
      
      String casePersonTypeId = filter.getCasePersonTypeId();
      if (!StringUtils.isBlank(casePersonTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(casePersonTypeId));
        filter.setCasePersonTypeId(type.getTypePath() + "%");
      }  

      Query query = setQueryParameters("findCasePersons", filter);

      List<DBCasePerson> dbRowList = query.getResultList();

      if (dbRowList != null && !dbRowList.isEmpty())
      {
        //Only cases with user read privileges are allowed. FindCases method 
        //returns this allowed cases.
        Set<String> allCaseIds = new HashSet();
        
        //populate caseId filter
        for (DBCasePerson dbRow : dbRowList)
        {
          allCaseIds.add(dbRow.getCaseId());
        }
        
        //findCases
        if (allCaseIds != null && !allCaseIds.isEmpty())
        {
          CaseFilter caseFilter = new CaseFilter();
          caseFilter.getCaseId().addAll(allCaseIds);
          return doCountCases(caseFilter);
        }
      }
      return 0;

      //return count("countCasePersons", filter);
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "countCasePersons failed");
      throw WSExceptionFactory.create(ex);
    }
  }  
  
  public CaseAddress loadCaseAddress(String id)
  {
    try
    {
      log.log(Level.INFO, "loadCaseAddress caseAddressId:{0}",
        new Object[]{id});

      CaseAddress caseAddress = null;

      if (id != null)
      {
        String[] pk = getSplitPk(id, 2);
        DBCaseAddress dbCaseAddress =
          em.find(DBCaseAddress.class, new DBCaseAddressPK(pk[0], pk[1]));

        if (dbCaseAddress != null)
        {
          caseAddress = new CaseAddress();
          JPAUtils.copy((CaseAddress)dbCaseAddress, caseAddress);
          caseAddress.setCaseAddressId(id);
        }
      }
    
      return caseAddress;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadCaseAddress failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public CaseAddress storeCaseAddress(CaseAddress caseAddress)
  {
    try
    {
      String caseAddressId = caseAddress.getCaseAddressId();

      log.log(Level.INFO, "storeCaseAddress caseAddressId:{0}",
        new Object[]{caseAddressId != null ? caseAddressId : "NEW"});

      validateCaseAddress(caseAddress);

      DBCaseAddress dbCaseAddress = null;
      if (caseAddressId == null)
      {
        dbCaseAddress = new DBCaseAddress(caseAddress);
        em.persist(dbCaseAddress);
      }
      else
      {
        String dbCaseAddressId = getCompositePk(
          new String[]{caseAddress.getCaseId(),caseAddress.getAddressId()});
        if (dbCaseAddressId.equals(caseAddressId))
        {
          em.merge(new DBCaseAddress(caseAddress));
        }
        else
        {
          dbCaseAddress =
            em.getReference(DBCaseAddress.class, new DBCaseAddressPK(caseAddressId));
          if (dbCaseAddress != null)
            em.remove(dbCaseAddress);
          em.persist(new DBCaseAddress(caseAddress));
        }
      }

      return dbCaseAddress;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeCaseAddress failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeCaseAddress(String id)
  {
    try
    {
      log.log(Level.INFO, "removeCaseAddress caseAddressId:{0}", new Object[]{id});

      String[] pk = getSplitPk(id, 2);
      DBCaseAddress dbCaseAddress =
        em.getReference(DBCaseAddress.class, new DBCaseAddressPK(pk[0], pk[1]));
      if (dbCaseAddress == null)
        return false;
      em.remove(dbCaseAddress);

      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeCaseAddress failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public List<CaseAddressView> findCaseAddressViews(CaseAddressFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findCaseAddressViews caseId:{0} addressId:{1}",
        new Object[]{filter.getCaseId(), filter.getAddressId()});
      
      if (StringUtils.isBlank(filter.getCaseId()) && StringUtils.isBlank(filter.getAddressId()))
        throw new Exception("FILTER_NOT_ALLOWED");      

      //Init return table and its rowlist
      List<CaseAddressView> caseAddressViewList = new ArrayList();

      //execute DB findQuery
      Query query = setQueryParameters("findCaseAddresses", filter);

      List<DBCaseAddress> dbCaseAddressList = query.getResultList();

      if (dbCaseAddressList != null && !dbCaseAddressList.isEmpty())
      {
        //Only cases with user read privileges are allowed. FindCases method 
        //returns this allowed cases.
        Set<String> allCaseIds = new HashSet();
        Set<String> allowedCaseIds = new HashSet();
        
        //populate caseId filter
        for (DBCaseAddress dbRow : dbCaseAddressList)
        {
          allCaseIds.add(dbRow.getCaseId());
        }
        
        //findCases
        if (allCaseIds != null && !allCaseIds.isEmpty())
        {
          CaseFilter caseFilter = new CaseFilter();
          caseFilter.getCaseId().addAll(allCaseIds);
          List<Case> cases = doFindCases(caseFilter);
          //populate only allowed caseIds set
          for (Case cas : cases)
          {
            allowedCaseIds.add(cas.getCaseId());
          }
        }            
        
        //Parse Query result and set row fields
        HashMap<String, CaseAddressView> addressIdMap = new HashMap();
        int firstResult = filter.getFirstResult();
        int maxResults = filter.getMaxResults();
        if (maxResults == 0) maxResults = Integer.MAX_VALUE;
        int index = 0;
        int added = 0;         
        for(DBCaseAddress dbCaseAddress : dbCaseAddressList)
        {
          if (index++ >= firstResult && added < maxResults)
          {
            if (allowedCaseIds.contains(dbCaseAddress.getCaseId()))
            {          
              String caseId = dbCaseAddress.getCaseId();
              String addressId = dbCaseAddress.getAddressId();
              CaseAddressView caseAddressView = new CaseAddressView();
              caseAddressView.setComments(dbCaseAddress.getComments());
              caseAddressView.setStartDate(dbCaseAddress.getStartDate());
              caseAddressView.setEndDate(dbCaseAddress.getEndDate());
              caseAddressView.setCaseAddressTypeId(dbCaseAddress.getCaseAddressTypeId());
              if (addressId != null)
              {
                caseAddressView.setCaseAddressId(
                  getCompositePk(new String[]{caseId, addressId}));
                addressIdMap.put(addressId, caseAddressView);
              }
              if (caseId != null) //Set CaseView
              {
                DBCase dbCase = dbCaseAddress.getCaseObject();
                Case caseObject = new Case();
                caseObject.setCaseId(caseId);
                caseObject.setTitle(dbCase.getTitle());
                caseObject.setDescription(dbCase.getDescription());
                caseObject.setCaseTypeId(dbCase.getCaseTypeId());
                caseObject.setComments(dbCase.getComments());
                caseAddressView.setCaseObject(caseObject);
              }
              caseAddressViewList.add(caseAddressView);
              if (++added == maxResults) break;
            }
          }
        }

        if (!addressIdMap.isEmpty())
        {
          //Invokes external object WS find
          try
          {
            KernelManagerPort port = getKernelManagerPort();
            AddressFilter addressFilter = new AddressFilter();
            addressFilter.getAddressIdList().addAll(addressIdMap.keySet());
            addressFilter.setFirstResult(0);
            addressFilter.setMaxResults(0);
            List<AddressView> addressViewList = port.findAddressViews(addressFilter);

            //Parse WS result and completes the rowlist of the return table
            for (AddressView addressView : addressViewList)
            {
              CaseAddressView caseAddressView = addressIdMap.get(addressView.getAddressId());
              if (caseAddressView != null)
                caseAddressView.setAddressView(addressView);
            }
          }
          catch (Exception e)
          {
            throw new WebServiceException(e);
          }
        }
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
    try
    {
      log.log(Level.INFO, "countCaseAddresses caseId:{0} addressId:{1}",
        new Object[]{filter.getCaseId(), filter.getAddressId()});
      
      if (StringUtils.isBlank(filter.getCaseId()) && StringUtils.isBlank(filter.getAddressId()))
        throw new Exception("FILTER_NOT_ALLOWED");

      Query query = setQueryParameters("findCaseAddresses", filter);

      List<DBCasePerson> dbRowList = query.getResultList();

      if (dbRowList != null && !dbRowList.isEmpty())
      {
        //Only cases with user read privileges are allowed. 
        Set<String> allCaseIds = new HashSet();
        
        //populate caseId filter
        for (DBCasePerson dbRow : dbRowList)
        {
          allCaseIds.add(dbRow.getCaseId());
        }
        
        //findCases
        if (allCaseIds != null && !allCaseIds.isEmpty())
        {
          CaseFilter caseFilter = new CaseFilter();
          caseFilter.getCaseId().addAll(allCaseIds);
          return doCountCases(caseFilter);
        }
      }
      return 0;
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  public Intervention loadIntervention(String intId)
  {
    try
    {
      log.log(Level.INFO, "loadIntervention intId:{0}",
        new Object[]{intId});

      DBIntervention dbIntervention =
        em.find(DBIntervention.class, intId);

//      Query query =
//        em.createNamedQuery("loadInterventionProperties");
//      query.setParameter("intId", intId);
//      List<Property> props = query.getResultList();      
      
      List<DBInterventionProperty> dbProperties = 
        loadDBProperties("loadInterventionProperties", intId);
      List<Property> props = toProperties(dbProperties);      

      dbIntervention.getProperty().addAll(props);

      return dbIntervention;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadIntervention failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public Intervention storeIntervention(Intervention intervention)
  {
    try
    {
      String intId = intervention.getIntId();

      log.log(Level.INFO, "storeIntervention intervention:{0}",
        new Object[]{intId != null ? intId : "NEW"});

      validateIntervention(intervention);

      DBIntervention dbIntervention = new DBIntervention(intervention);
      if (dbIntervention.getIntId() == null)
      {
        em.persist(dbIntervention);
        intervention.setIntId(dbIntervention.getIntId());
        storeInterventionProperties(intervention, false);
      }
      else
      {
        em.merge(dbIntervention);
        storeInterventionProperties(intervention, true);        
      }
      return dbIntervention;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeIntervention failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeIntervention(String intId)
  {
    try
    {
      log.log(Level.INFO, "removeIntervention intervention:{0}",
        new Object[]{intId});

//      Query query =
//        em.createNamedQuery("removeInterventionProperties");
//      query.setParameter("intId", intId);
//      query.executeUpdate();
      removeInterventionProperties(intId);
      
      Query query = em.createNamedQuery("removeInterventionProblems");
      query.setParameter("intId", intId);
      query.executeUpdate();

      DBIntervention dbIntervention =
        em.getReference(DBIntervention.class, intId);
      if (dbIntervention == null)
        return false;
      em.remove(dbIntervention);

      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeIntervention failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public List<InterventionView> findInterventionViews(InterventionFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findInterventionViews caseId:{0}",
        new Object[]{filter.getCaseId()});
      
      validateInterventionFilter(filter); 
      
      List<InterventionView> interventionViewList = new ArrayList();

      String intTypeId = filter.getIntTypeId();
      if (!StringUtils.isBlank(intTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(intTypeId));
        filter.setIntTypeId(type.getTypePath());
      }
      
      FindInterventionsQueryBuilder queryBuilder = 
        FindInterventionsQueryBuilder.getInstance();
      queryBuilder.setFilter(filter);
      queryBuilder.setUserRoles(UserCache.getUser(wsContext).getRolesList());
      queryBuilder.setCounterQuery(false);
      Query query = queryBuilder.getQuery(em);
      List<DBIntervention> dbInterventionList = query.getResultList();
      
      HashMap<String, PersonView> personViewMap = 
        new HashMap<String, PersonView>();
      HashMap<String, List<DBInterventionProperty>> propsMap = 
        new HashMap<String, List<DBInterventionProperty>>();
      for (DBIntervention dbIntervention : dbInterventionList)
      {
        String personId = dbIntervention.getPersonId();
        if (personId != null)
          personViewMap.put(personId, null);
        propsMap.put(dbIntervention.getIntId(), null);
      }

      //Invokes external object WS find
      try
      {
        if (!personViewMap.isEmpty())
        {
          KernelManagerPort port = getKernelManagerPort();
          PersonFilter personFilter = new PersonFilter();
          personFilter.getPersonId().addAll(personViewMap.keySet());
          personFilter.setFirstResult(0);
          personFilter.setMaxResults(0);
          List<PersonView> personViewList = port.findPersonViews(personFilter);
          for (PersonView personView : personViewList)
          {
            personViewMap.put(personView.getPersonId(), personView);
          }
        }
        
        //Get properties with only 1 query: Map of interventionId -> Properties
        if (!propsMap.isEmpty())
        {
          JPAFindInterventionPropertiesQueryBuilder propsQueryBuilder = 
            new JPAFindInterventionPropertiesQueryBuilder();
          propsQueryBuilder.setInterventionIds(propsMap.keySet());
          Query auxQuery = propsQueryBuilder.getQuery(em);

          List<DBInterventionProperty> dbProperties = auxQuery.getResultList();
          for (DBInterventionProperty dbPropertyAux : dbProperties)
          {
            List props = propsMap.get(dbPropertyAux.getId());
            if (props == null)
            {
              props = new ArrayList();
              propsMap.put(dbPropertyAux.getId(), props);
            }
            props.add(dbPropertyAux);
          }                  
        }        

        //Parse WS result and completes the rowlist of the return table
        for (DBIntervention dbIntervention : dbInterventionList)
        {
          InterventionView interventionView = new InterventionView();
          interventionView.setIntId(dbIntervention.getIntId());
          interventionView.setIntTypeId(dbIntervention.getIntTypeId());
          interventionView.setCaseId(dbIntervention.getCaseObject().getCaseId());
          interventionView.setUserId(dbIntervention.getUserId());
          interventionView.setStartDate(dbIntervention.getStartDate());
          interventionView.setEndDate(dbIntervention.getEndDate());
          interventionView.setStartTime(dbIntervention.getStartTime());
          interventionView.setEndTime(dbIntervention.getEndTime());
          interventionView.setComments(dbIntervention.getComments());

          if (!filter.isExcludeMetadata())
          {
//            List<DBInterventionProperty> dbProperties = 
//              loadDBProperties("loadInterventionProperties", dbIntervention.getIntId());

            List<DBInterventionProperty> dbProperties = 
              propsMap.get(dbIntervention.getIntId()); 
            if (dbProperties != null && !dbProperties.isEmpty())
            {
              List<Property> props = toProperties(dbProperties);
              interventionView.getProperty().addAll(props);
            }
          }

          interventionView.setPersonView(
            personViewMap.get(dbIntervention.getPersonId()));
          interventionViewList.add(interventionView);
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }

      return interventionViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findInterventionViews failed");
        throw WSExceptionFactory.create(ex);
    }
  }
  
  public int countInterventions(InterventionFilter filter)
  {
    try
    {
      String intTypeId = filter.getIntTypeId();
      if (!StringUtils.isBlank(intTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(intTypeId));
        filter.setIntTypeId(type.getTypePath());
      }      
      
      FindInterventionsQueryBuilder queryBuilder = FindInterventionsQueryBuilder.getInstance();
      queryBuilder.setFilter(filter);
      queryBuilder.setUserRoles(UserCache.getUser(wsContext).getRolesList());
      queryBuilder.setCounterQuery(true);
      Query query = queryBuilder.getQuery(em);
      return ((Number)query.getSingleResult()).intValue();      
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  public CaseDocument loadCaseDocument(String caseDocId)
  {
    try
    {
      log.log(Level.INFO, "loadCaseDocument caseDocumentId:{0}", new Object[]{caseDocId});

      CaseDocument caseDocument = null;

      if (caseDocId != null)
      {
        String[] pk = getSplitPk(caseDocId, 2);
        DBCaseDocument dbCaseDocument =
          em.find(DBCaseDocument.class,  new DBCaseDocumentPK(pk[0], pk[1]));

        if (dbCaseDocument != null)
        {
          caseDocument = new CaseDocument();
          JPAUtils.copy((CaseDocument)dbCaseDocument, caseDocument);
          if (caseDocument.getCaseDocTypeId() == null)
            caseDocument.setCaseDocTypeId(DictionaryConstants.CASE_DOCUMENT_TYPE);
          caseDocument.setCaseDocId(caseDocId);
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
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      String caseDocId = caseDocument.getCaseDocId();
      log.log(Level.INFO, "storeCaseDocument caseDocumentId:{0}",
        new Object[]{caseDocId != null ? caseDocId : "NEW"});

      validateCaseDocument(caseDocument);

      DBCaseDocument dbCaseDocument = null;

      if (caseDocId == null) //insert
      {
          String dbCaseDocumentId = getCompositePk(
            new String[]{caseDocument.getCaseId(), caseDocument.getDocId()});

          dbCaseDocument = em.find(DBCaseDocument.class,
              new DBCaseDocumentPK(dbCaseDocumentId));

          if (dbCaseDocument != null)
            throw new WebServiceException("cases:DUPLICATED_DOCUMENT");

          dbCaseDocument = new DBCaseDocument(caseDocument);
          Auditor.auditCreation(dbCaseDocument, user.getUserId());

          em.persist(dbCaseDocument);
      }
      else //update
      {
        dbCaseDocument = em.getReference(DBCaseDocument.class,
          new DBCaseDocumentPK(caseDocId));

        String dbCaseDocumentId = getCompositePk(
            new String[]{caseDocument.getCaseId(), caseDocument.getDocId()});
        if (caseDocId.equals(dbCaseDocumentId))
        {
          dbCaseDocument.copyFrom(caseDocument); //Merging
          Auditor.auditChange(dbCaseDocument, user.getUserId());
          dbCaseDocument = em.merge(dbCaseDocument);
        }
        else //Document changed
        {
          if (dbCaseDocument != null) em.remove(dbCaseDocument);
          dbCaseDocument = new DBCaseDocument(caseDocument);
          Auditor.auditCreation(dbCaseDocument, user.getUserId());
          em.persist(dbCaseDocument);
        }
      }

      return dbCaseDocument;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeCaseDocument failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeCaseDocument(String id)
  {
    try
    {
      log.log(Level.INFO, "removeCaseDocument caseDocumentId:{0}",
        new Object[]{id});

      String[] pk = getSplitPk(id, 2);
      DBCaseDocument dbCaseDocument = em.getReference(DBCaseDocument.class,
        new DBCaseDocumentPK(pk[0], pk[1]));
      if (dbCaseDocument == null)
        return false;
      em.remove(dbCaseDocument);

      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeCaseDocument failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public List<CaseDocumentView> findCaseDocumentViews(CaseDocumentFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findCaseDocumentViews caseId:{0} docId:{1}",
        new Object[]{filter.getCaseId(), filter.getDocId()});

      List<CaseDocumentView> caseDocumentViewList = new ArrayList();

      String caseId = filter.getCaseId();
      if (caseId != null) //Get documents
      {
        caseDocumentViewList = getCaseDocumentRows(caseId, filter);
      }
      else
      {
        String docId = filter.getDocId();
        if (docId != null)
        {
          //Get cases
          Query query = setQueryParameters("findCaseDocuments", filter);
          List<Object[]> dbList = query.getResultList();
          if (dbList != null && !dbList.isEmpty())
          {
            //Parse Query result and set row fields (View)
            Map cdMap = new HashMap();
            CaseFilter caseFilter = new CaseFilter();
            for (Object[] row : dbList)
            {
              DBCaseDocument dbCaseDocument = (DBCaseDocument)row[0];
              String localCaseId = dbCaseDocument.getCaseId();
              String globalCaseId = getWSEndpoint().toGlobalId(Case.class,
                localCaseId);
              cdMap.put(globalCaseId, dbCaseDocument);
              caseFilter.getCaseId().add(globalCaseId);
            }
            //Invokes document WS findCases
            try
            {
              CaseManagerPort port = getCaseManagerPort();
              caseFilter.setFirstResult(0);
              caseFilter.setMaxResults(0);
              List<Case> caseList = port.findCases(caseFilter);
              //Parse WS result and completes the rowlist of the return table
              for (Case caseObject : caseList)
              {
                DBCaseDocument dbCaseDocument =
                  (DBCaseDocument)cdMap.get(caseObject.getCaseId());
                CaseDocumentView caseDocumentView = new CaseDocumentView();
                caseDocumentView.setCaseObject(caseObject);
                caseDocumentView.setCaseDocId(
                  getCompositePk(new String[]{caseObject.getCaseId(),
                  docId}));
                String caseDocTypeId = dbCaseDocument.getCaseDocTypeId();
                caseDocumentView.setCaseDocTypeId(caseDocTypeId != null ?
                    caseDocTypeId : DictionaryConstants.CASE_DOCUMENT_TYPE);
                caseDocumentView.setComments(dbCaseDocument.getComments());
                caseDocumentViewList.add(caseDocumentView);
              }
            }
            catch (Exception e)
            {
              throw new WebServiceException(e);
            }
          }
        }
      }
      
      List<CaseDocumentView> finalCaseDocumentViewList = new ArrayList();
      int firstResult = filter.getFirstResult();
      int maxResults = filter.getMaxResults();
      if (maxResults == 0) maxResults = Integer.MAX_VALUE;
      int index = 0;
      int added = 0;
      for (CaseDocumentView caseDocumentView : caseDocumentViewList)
      {
        if (index++ >= firstResult && added < maxResults)
        {
          finalCaseDocumentViewList.add(caseDocumentView);
          if (++added == maxResults) return finalCaseDocumentViewList;
        }
      }
      return finalCaseDocumentViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findCaseDocumentViews failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public List<String> findCaseVolumes(String caseId)
  {
    try
    {
      log.log(Level.INFO, "findCaseVolumes caseId:{0}",
        new Object[]{caseId});

      Query query = em.createNamedQuery("findCaseVolumes");
      query.setParameter("caseId", caseId);
      return query.getResultList();
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findCaseVolumes failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public int countCaseDocuments(CaseDocumentFilter filter)
  {
    try
    {
      String caseId = filter.getCaseId();
      if (caseId != null) //Get documents
      {
        List<CaseDocumentView> rows = new ArrayList();
        //If a related document not exists then it isn't considered
        rows = getCaseDocumentRows(caseId, filter);
        if (rows != null)
          return rows.size();
        else
          return 0;
      }
      else
        return count("countCaseDocuments", filter);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  public CaseEvent loadCaseEvent(String caseEventId)
  {
    try
    {
      log.log(Level.INFO, "loadCaseEvent caseEventId:{0}", new Object[]{caseEventId});

      DBCaseEvent dbCaseEvent = null;

      if (caseEventId != null)
      {
        dbCaseEvent = em.find(DBCaseEvent.class, caseEventId);
        //Properties
        List<DBCaseEventProperty> dbProperties = 
          loadDBProperties("loadCaseEventProperties", caseEventId);
        List<Property> properties = toProperties(dbProperties);
        dbCaseEvent.getProperty().addAll(properties);
      }
      return dbCaseEvent;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadCaseEvent failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public CaseEvent storeCaseEvent(CaseEvent caseEvent)
  {
    try
    {
      String caseEventId = caseEvent.getCaseEventId();
      log.log(Level.INFO, "storeCaseEvent caseEventId:{0}",
        new Object[]{caseEventId != null ? caseEventId : "NEW"});

      validateCaseEvent(caseEvent);

      DBCaseEvent dbCaseEvent = new DBCaseEvent(caseEvent);
      if (caseEventId == null) //insert
      {
        //dbCaseEvent = new DBCaseEvent(caseEvent);
        em.persist(dbCaseEvent);
        storeCaseEventProperties(dbCaseEvent, false);
      }
      else //update
      {
        em.merge(dbCaseEvent);
        storeCaseEventProperties(dbCaseEvent, true);
      }      
      return dbCaseEvent;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeCaseEvent failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeCaseEvent(String caseEventId)
  {
    try
    {
      log.log(Level.INFO, "removeCaseEvent caseEventId:{0}",
        new Object[]{caseEventId});

      DBCaseEvent dbCaseEvent = em.getReference(DBCaseEvent.class,
        caseEventId);
      if (dbCaseEvent == null) 
        return false;
      else
      {
        removeCaseEventProperties(caseEventId);
        em.remove(dbCaseEvent);
        return true;
      }
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeCaseEvent failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public int countCaseEvents(CaseEventFilter filter)
  {
    try
    {
      String caseEventTypeId = filter.getCaseEventTypeId();
      if (!StringUtils.isBlank(caseEventTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(caseEventTypeId));
        filter.setCaseEventTypeId(type.getTypePath() + "%");
      }      
      return count("countCaseEvents", filter);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }
  
  public List<CaseEventView> findCaseEventViews(CaseEventFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findCaseEventViews caseId:{0} eventId:{1}",
        new Object[]{filter.getCaseId(), filter.getEventId()});

      List<CaseEventView> caseEventViewList = new ArrayList();

      if (StringUtils.isBlank(filter.getCaseId()) && 
        StringUtils.isBlank(filter.getEventId()))
          throw new Exception("FILTER_NOT_ALLOWED");

      String caseEventTypeId = filter.getCaseEventTypeId();
      if (!StringUtils.isBlank(caseEventTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, 
          typeEntity.toLocalId(caseEventTypeId));
        filter.setCaseEventTypeId(type.getTypePath() + "%");
      }
      
      if (filter.getCaseId() != null) //Get events
      {
        caseEventViewList = getCaseEventRows(filter);
      }
      else if (filter.getEventId() != null) //Get cases
      {
        caseEventViewList = getEventCaseRows(filter);
      }
      
      List<CaseEventView> finalCaseEventViewList = new ArrayList();
      int firstResult = filter.getFirstResult();
      int maxResults = filter.getMaxResults();
      if (maxResults == 0) maxResults = Integer.MAX_VALUE;
      int index = 0;
      int added = 0;
      for (CaseEventView caseEventView : caseEventViewList)
      {
        if (index++ >= firstResult && added < maxResults)
        {
          if (!filter.isExcludeMetadata())
          {          
            List<DBCaseEventProperty> dbProperties = 
              loadDBProperties("loadCaseEventProperties", caseEventView.getCaseEventId());
            List<Property> properties = toProperties(dbProperties);
            caseEventView.getProperty().addAll(properties);
          }
          finalCaseEventViewList.add(caseEventView);
          if (++added == maxResults) return finalCaseEventViewList;
        }
      }
      return finalCaseEventViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findCaseEventViews failed");
        throw WSExceptionFactory.create(ex);
    }
  }  
  
  public Problem loadProblem(String caseProbId)
  {
    try
    {
      log.log(Level.INFO, "loadProblem caseProbId:{0}",
        new Object[]{caseProbId});

      DBProblem dbProblem = em.find(DBProblem.class, caseProbId);

      return dbProblem;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadProblem failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public Problem storeProblem(Problem problem)
  {
    try
    {
      log.log(Level.INFO, "storeProblem probId:{0}",
        new Object[]{problem.getProbId() != null ?
          problem.getProbId() : "NEW"});
      validateProblem(problem);

      DBProblem dbProblem = new DBProblem(problem);
      String caseProbId = dbProblem.getProbId();
      if (caseProbId == null)
        em.persist(dbProblem);
      else
        dbProblem = em.merge(dbProblem);

      return dbProblem;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeProblem failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeProblem(String caseProbId)
  {
    try
    {
      log.log(Level.INFO, "removeProblem casePersonId:{0}",
        new Object[]{caseProbId});

      DBProblem dbProblem = em.getReference(DBProblem.class, caseProbId);
      if (dbProblem == null)
        return false;
      em.remove(dbProblem);
      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeProblem failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public List<ProblemView> findProblemViews(ProblemFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findProblemViews caseId:{0} ",
        new Object[]{filter.getCaseId()});

      List<ProblemView> problemViewList = new ArrayList();

      Query query = em.createNamedQuery("findProblems");
      query.setParameter("caseId", filter.getCaseId());

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);

      List<DBProblem> dbProblemList = query.getResultList();

      HashSet<String> personIds = new HashSet();
      for(DBProblem dbProblem : dbProblemList)
      {
        String personId = dbProblem.getPersonId();
        if (personId != null)
          personIds.add(personId);
      }

      //Invokes external object WS find
      try
      {
        List<PersonView> personViewList = new ArrayList();
        if (!personIds.isEmpty())
        {
          KernelManagerPort port = getKernelManagerPort();
          PersonFilter personFilter = new PersonFilter();
          personFilter.getPersonId().addAll(personIds);
          personFilter.setFirstResult(0);
          personFilter.setMaxResults(0);
          personViewList = port.findPersonViews(personFilter);
        }

        //Parse WS result and completes the rowlist of the return table
        for (DBProblem dbProblem : dbProblemList)
        {
          ProblemView problemView = new ProblemView();

          problemView.setProbId(dbProblem.getProbId());
          problemView.setProbTypeId(dbProblem.getProbTypeId());
          problemView.setCaseId(filter.getCaseId());
          String personId = dbProblem.getPersonId();
          if (personId != null)
          {
            Iterator it = personViewList.iterator();
            boolean found = false;
            while (!found && it.hasNext())
            {
              PersonView personView = (PersonView)it.next();
              found = personId.equals(personView.getPersonId());
              if (found)
                problemView.setPersonView(personView);
            }
          }

          problemViewList.add(problemView);
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }

      return problemViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findProblemViews failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public int countProblems(ProblemFilter filter)
  {
    try
    {
      return count("countProblems", filter);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  public Demand loadDemand(String demandId)
  {
    try
    {
      log.log(Level.INFO, "loadDemand demandId:{0}",
        new Object[]{demandId});

      DBDemand dbDemand = em.find(DBDemand.class, demandId);

      return dbDemand;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadDemand failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public Demand storeDemand(Demand caseDemand)
  {
    try
    {
      log.log(Level.INFO, "storeDemand caseDemandId:{0}",
        new Object[]{caseDemand.getDemandId() != null ?
          caseDemand.getDemandId() : "NEW"});

      validateDemand(caseDemand);

      DBDemand dbDemand = new DBDemand(caseDemand);
      String caseDemandId = dbDemand.getDemandId();
      if (caseDemandId == null)
        em.persist(dbDemand);
      else
        dbDemand = em.merge(dbDemand);

      return dbDemand;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeDemand failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeDemand(String caseDemandId)
  {
    try
    {
      log.log(Level.INFO, "removeDemand caseDemandId:{0}",
        new Object[]{caseDemandId});

      DBDemand dbDemand = em.getReference(DBDemand.class, caseDemandId);
      if (dbDemand == null)
        return false;
      em.remove(dbDemand);
      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeDemand failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public List<Demand> findDemands(DemandFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findDemands caseId:{0}",
        new Object[]{filter.getCaseId()});

      Query query = em.createNamedQuery("findDemands");
      query.setParameter("caseId", filter.getCaseId());

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);

      List<DBDemand> dbDemandList = query.getResultList();
      List<Demand> demandList = new ArrayList();
      JPAUtils.copyList(dbDemandList, demandList, Demand.class);

      return demandList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findDemands failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public int countDemands(DemandFilter filter)
  {
    try
    {
      return count("countDemands", filter);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }

  public CaseCase loadCaseCase(String caseCaseId)
  {
    try
    {      
      DBCaseCase dbCaseCase = null;

      if (caseCaseId != null)
      {
        dbCaseCase =
          em.find(DBCaseCase.class, caseCaseId);

        if (dbCaseCase != null)
        {
//          caseCase = new CaseCase();
//          JPAUtils.copy((CaseCase)dbCaseCase, caseCase);
          dbCaseCase.setCaseCaseId(caseCaseId);
          if (dbCaseCase.getCaseCaseTypeId() == null)
            dbCaseCase.setCaseCaseTypeId(DictionaryConstants.CASE_CASE_TYPE);
        }
        
      //Properties
        List<DBCaseCaseProperty> dbProperties = 
          loadDBProperties("loadCaseCaseProperties", caseCaseId);
        List<Property> properties = toProperties(dbProperties);
        dbCaseCase.getProperty().addAll(properties);        
      }

      return dbCaseCase;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadCaseCase failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public CaseCase storeCaseCase(CaseCase caseCase)
  {
    try
    {
      DBCaseCase dbCaseCase = null;          

      String caseCaseId = caseCase.getCaseCaseId();

      validateCaseCase(caseCase);

      if (caseCaseId == null) //insert
      {
        dbCaseCase = new DBCaseCase(caseCase);
        em.persist(dbCaseCase);
        storeCaseCaseProperties(dbCaseCase, false);        
      }
      else //update
      {
        dbCaseCase = em.merge(new DBCaseCase(caseCase));
        storeCaseCaseProperties(caseCase, true);        
      }
      
      return dbCaseCase;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeCaseCase failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeCaseCase(String caseCaseId)
  {
    try
    {
      log.log(Level.INFO, "removeCaseCase caseCaseId:{0}",
        new Object[]{caseCaseId});
      
      CaseCase caseCase = loadCaseCase(caseCaseId);

      if (caseCase == null)
        return false;
      else
      {
        if (!caseCase.getProperty().isEmpty())
          removeCaseCaseProperties(caseCaseId);
        em.remove((DBCaseCase)caseCase);
      }

      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeCaseCase failed");
        throw WSExceptionFactory.create(ex);
    }
  }
  
  public List<CaseCaseView> findCaseCaseViews(CaseCaseFilter filter)
  {
    try
    {
      List<CaseCaseView> caseCaseViewList = new ArrayList<CaseCaseView>();

      if (StringUtils.isBlank(filter.getCaseId()) && StringUtils.isBlank(filter.getRelCaseId()))
        throw new Exception("FILTER_NOT_ALLOWED");    

      String caseCaseTypeId = filter.getCaseCaseTypeId();
      if (!StringUtils.isBlank(caseCaseTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(caseCaseTypeId));
        filter.setCaseCaseTypeId(type.getTypePath() + "%");
      }      

      Query query = setQueryParameters("findCaseCases", filter);
      List<Object[]> dbList = query.getResultList();
      if (!dbList.isEmpty())
      {
        Set<String> caseIdSet = new HashSet<String>();
        for (Object[] row : dbList)
        {
          DBCaseCase dbCaseCase = (DBCaseCase)row[0];
          String caseId =
            getWSEndpoint().toGlobalId(Case.class, dbCaseCase.getCaseId());
          String relCaseId =
            getWSEndpoint().toGlobalId(Case.class, dbCaseCase.getRelCaseId());
          caseIdSet.add(caseId);
          caseIdSet.add(relCaseId);
        }
        CaseManagerPort port = getCaseManagerPort();

        Map prefixMap = new HashMap();
        for (String caseId : caseIdSet)
        {
          String[] split = caseId.split(":");
          String prefix = (split.length > 1 ? split[0] : "");
          if (!prefixMap.containsKey(prefix))
            prefixMap.put(prefix, new ArrayList<String>());
          ((List<String>)prefixMap.get(prefix)).add(caseId);
        }

        Map<String, Case> readableCaseMap = new HashMap<String, Case>();
        for (Object prefix : prefixMap.keySet())
        {
          CaseFilter caseFilter = new CaseFilter();
          caseFilter.getCaseId().addAll((List<String>)prefixMap.get(prefix));
          caseFilter.setDateComparator("0");
          List<Case> caseList = port.findCases(caseFilter);
          for (Case cas : caseList)
          {
            readableCaseMap.put(cas.getCaseId(), cas);
          }
        }

        int firstResult = filter.getFirstResult();
        int maxResults = filter.getMaxResults();
        if (maxResults == 0) maxResults = Integer.MAX_VALUE;
        int index = 0;
        int added = 0;
        for (Object[] row : dbList)
        {
          DBCaseCase dbCaseCase = (DBCaseCase)row[0];
          String caseId =
            getWSEndpoint().toGlobalId(Case.class, dbCaseCase.getCaseId());
          String relCaseId =
            getWSEndpoint().toGlobalId(Case.class, dbCaseCase.getRelCaseId());

          if (readableCaseMap.containsKey(caseId)
            && readableCaseMap.containsKey(relCaseId))
          {
            if (index++ >= firstResult && added < maxResults)
            {
              CaseCaseView caseCaseView = new CaseCaseView();
              Case mainCase = readableCaseMap.get(caseId);
              Case relCase = readableCaseMap.get(relCaseId);
              caseCaseView.setMainCase(mainCase);
              caseCaseView.setRelCase(relCase);
              caseCaseView.setComments(dbCaseCase.getComments());
              caseCaseView.setCaseCaseId(dbCaseCase.getCaseCaseId());
              String rowCaseCaseTypeId = dbCaseCase.getCaseCaseTypeId();
              caseCaseView.setCaseCaseTypeId(rowCaseCaseTypeId != null ?
                rowCaseCaseTypeId :
                DictionaryConstants.CASE_CASE_TYPE);
              caseCaseView.setStartDate(dbCaseCase.getStartDate());
              caseCaseView.setEndDate(dbCaseCase.getEndDate());

              //Properties
              if (!filter.isExcludeMetadata())
              {
                List<DBCaseCaseProperty> dbProperties = 
                  loadDBProperties("loadCaseCaseProperties", dbCaseCase.getCaseCaseId());
                List<Property> properties = toProperties(dbProperties);
                caseCaseView.getProperty().addAll(properties);                
              }
              caseCaseViewList.add(caseCaseView);
              if (++added == maxResults) return caseCaseViewList;
            }
          }
        }
      }
      return caseCaseViewList;
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }  
  
  public int countCaseCases(CaseCaseFilter filter)
  {
    try
    {
      String caseCaseTypeId = filter.getCaseCaseTypeId();
      if (!StringUtils.isBlank(caseCaseTypeId))
      {
        ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
        org.matrix.dic.Type type = em.find(DBType.class, typeEntity.toLocalId(caseCaseTypeId));
        filter.setCaseCaseTypeId(type.getTypePath() + "%");
      }      
      return count("countCaseCases", filter);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }
  
  public InterventionProblem loadInterventionProblem(String intProbId)
  {
    try
    {
      InterventionProblem interventionProblem = null;

      if (intProbId != null)
      {
        DBInterventionProblem dbInterventionProblem =
          em.find(DBInterventionProblem.class, intProbId);

        if (dbInterventionProblem != null)
        {
          interventionProblem = new InterventionProblem();
          JPAUtils.copy((InterventionProblem)dbInterventionProblem, interventionProblem);
          interventionProblem.setIntProbId(intProbId);
        }
      }

      return interventionProblem;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "loadInterventionProblem failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public InterventionProblem storeInterventionProblem(InterventionProblem interventionProblem)
  {
    try
    {
      String intProbId = interventionProblem.getIntProbId();

      validateInterventionProblem(interventionProblem);

      DBInterventionProblem dbInterventionProblem = null;
      if (intProbId == null) //insert
      {
        dbInterventionProblem = new DBInterventionProblem(interventionProblem);
        em.persist(dbInterventionProblem);
      }
      else //update
      {
        dbInterventionProblem = em.merge(new DBInterventionProblem(interventionProblem));
      }

      return dbInterventionProblem;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "storeInterventionProblem failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public boolean removeInterventionProblem(String intProbId)
  {
    try
    {
      log.log(Level.INFO, "removeInterventionProblem intProbId:{0}",
        new Object[]{intProbId});

      DBInterventionProblem dbInterventionProblem =
        em.find(DBInterventionProblem.class, intProbId);

      if (dbInterventionProblem == null)
        return false;
      em.remove(dbInterventionProblem);

      return true;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "removeInterventionProblem failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public List<InterventionProblemView> 
      findInterventionProblemViews(InterventionProblemFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findInterventionProblemViews caseId:{0} personId:{1}",
        new Object[]{filter.getIntId(), filter.getProbId()});

      List<InterventionProblemView> intProbViewList = new ArrayList();

      //execute DB findQuery
      Query query = setQueryParameters("findInterventionProblems", filter);

      query.setFirstResult(filter.getFirstResult());
      int maxResults = filter.getMaxResults();
      if (maxResults > 0) query.setMaxResults(maxResults);

      List<DBInterventionProblem> dbRowList = query.getResultList();
      

      if (dbRowList != null && !dbRowList.isEmpty())
      {
        //Parse Query result and set row fields (View)
        HashMap<String, List<InterventionProblemView>> personIdMap = new HashMap();
        for(DBInterventionProblem dbInterventionProblem : dbRowList)
        {
          InterventionProblemView intProbView = new InterventionProblemView();
          intProbView.setIntProbId(dbInterventionProblem.getIntProbId());
          intProbView.setComments(dbInterventionProblem.getComments());

          String intId = dbInterventionProblem.getIntId();
          String probId = dbInterventionProblem.getProbId();
          if (intId != null) //Set InterventionView
          {
            DBIntervention dbIntervention = dbInterventionProblem.getIntervention();
            Intervention intervention = new Intervention();
            intervention.setIntId(intId);
            dbIntervention.copyTo(intervention);
            intProbView.setIntervention(intervention);
          }
          
          if (probId != null) //Set ProblemView
          {
            DBProblem dbProblem = dbInterventionProblem.getProblem();
            Problem problem = new Problem();
            problem.setProbId(probId);
            dbProblem.copyTo(problem);
            intProbView.setProblem(problem);
          }
          intProbViewList.add(intProbView);
        }
      }

      return intProbViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findInterventionProblemViews failed");
        throw WSExceptionFactory.create(ex);
    }
  }

  public int countInterventionProblems(InterventionProblemFilter filter)
  {
    try
    {
      return count("countInterventionProblems", filter);
    }
    catch (Exception ex)
    {
      throw WSExceptionFactory.create(ex);
    }
  }  
  
  //Private methods
  private List<CaseDocumentView> getCaseDocumentRows(String caseId,
    CaseDocumentFilter filter)
  {
    List<CaseDocumentView> caseDocumentViewList = new ArrayList();
    //execute DB findQuery
    Query query = setQueryParameters("findCaseDocuments", filter);
    List<Object[]> dbList = query.getResultList();
    if (dbList != null && !dbList.isEmpty())
    {
      //Parse Query result and set row fields (View)
      Map cdMap = new HashMap();
      DocumentFilter documentFilter = new DocumentFilter();
      for (Object[] row : dbList)
      {
        DBCaseDocument dbCaseDocument = (DBCaseDocument)row[0];
        String docId = dbCaseDocument.getDocId();
        cdMap.put(docId, dbCaseDocument);
        documentFilter.getDocId().add(docId);
      }
      //Invokes document WS findDocument
      try
      {
        DocumentManagerPort port = getDocumentManagerPort();
        documentFilter.setVersion(0); //obtain always last version
        documentFilter.setFirstResult(0);
        documentFilter.setMaxResults(0);
        documentFilter.setIncludeContentMetadata(true);
        documentFilter.getOutputProperty().addAll(filter.getOutputProperty());
        List<Document> documentList = port.findDocuments(documentFilter);

        //Parse WS result and completes the rowlist of the return table
        for (Document document : documentList)
        {
          DBCaseDocument dbCaseDocument =
            (DBCaseDocument)cdMap.get(document.getDocId());
          CaseDocumentView caseDocumentView = new CaseDocumentView();
          caseDocumentView.setCaseDocId(
            getCompositePk(new String[]{caseId, document.getDocId()}));
          String caseDocTypeId = dbCaseDocument.getCaseDocTypeId();
          caseDocumentView.setCaseDocTypeId(caseDocTypeId != null ?
          caseDocTypeId : DictionaryConstants.CASE_DOCUMENT_TYPE);
          caseDocumentView.setComments(dbCaseDocument.getComments());
          caseDocumentViewList.add(caseDocumentView);
          if (caseDocumentView != null)
            caseDocumentView.setDocument(document);
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
    }

    return caseDocumentViewList;
  }

  private List<CaseEventView> getCaseEventRows(CaseEventFilter filter)
  {
    String caseId = filter.getCaseId();
    List<CaseEventView> caseEventViewList = new ArrayList();
    //execute DB findQuery    
    Query query = setQueryParameters("findCaseEvents", filter);
    List<DBCaseEvent> dbList = query.getResultList();
    if (dbList != null && !dbList.isEmpty())
    {
      //Parse Query result and set row fields (View) 
      Map ceMap = new HashMap();
      EventFilter eventFilter = new EventFilter();
      eventFilter.setFirstResult(0);
      eventFilter.setMaxResults(0);
      for (DBCaseEvent dbCaseEvent : dbList)
      {
        //DBCaseEvent dbCaseEvent = (DBCaseEvent)row[0];
        String eventId = dbCaseEvent.getEventId();
        ceMap.put(eventId, dbCaseEvent);        
        eventFilter.getEventId().add(eventId);
      }
      //Invokes agenda WS findEvents
      try
      {
        AgendaManagerPort port = getAgendaManagerPort();
        List<Event> eventList = port.findEvents(eventFilter);

        //Parse WS result and completes the rowlist of the return table
        for (Event event : eventList)
        {
          if (!AgendaConstants.DELETED_EVENT_DATETIME.equals(event.getStartDateTime()))
          {
            DBCaseEvent dbCaseEvent =
              (DBCaseEvent)ceMap.get(event.getEventId());
            CaseEventView caseEventView = new CaseEventView();
            caseEventView.setCaseEventId(dbCaseEvent.getCaseEventId());
            String caseEventTypeId = dbCaseEvent.getCaseEventTypeId();
            caseEventView.setCaseEventTypeId(caseEventTypeId != null ?
              caseEventTypeId : DictionaryConstants.CASE_EVENT_TYPE);
            caseEventView.setEvent(event);          
            caseEventViewList.add(caseEventView);
          }
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
    }
    return caseEventViewList;
  }  
  
  private List<CaseEventView> getEventCaseRows(CaseEventFilter filter)
  {
    String eventId = filter.getEventId();
    List<CaseEventView> caseEventViewList = new ArrayList();    
    //Get cases
    Query query = setQueryParameters("findCaseEvents", filter);
    List<DBCaseEvent> dbList = query.getResultList();
    if (dbList != null && !dbList.isEmpty())
    {
      //Parse Query result and set row fields (View)
      Map ceMap = new HashMap();            
      CaseFilter caseFilter = new CaseFilter();
      for (DBCaseEvent dbCaseEvent : dbList)
      {
        //DBCaseEvent dbCaseEvent = (DBCaseEvent)row[0];
        String localCaseId = dbCaseEvent.getCaseId();
        String globalCaseId = getWSEndpoint().toGlobalId(Case.class,
          localCaseId);
        ceMap.put(globalCaseId, dbCaseEvent);
        caseFilter.getCaseId().add(globalCaseId);
      }
      //Invokes event WS findCases
      try
      {
        CaseManagerPort port = getCaseManagerPort();
        caseFilter.setFirstResult(0);
        caseFilter.setMaxResults(0);
        List<Case> caseList = port.findCases(caseFilter);
        //Parse WS result and completes the rowlist of the return table
        for (Case caseObject : caseList)
        {
          DBCaseEvent dbCaseEvent =
            (DBCaseEvent)ceMap.get(caseObject.getCaseId());
          CaseEventView caseEventView = new CaseEventView();
          caseEventView.setCaseEventId(dbCaseEvent.getCaseEventId());
          caseEventView.setCaseObject(caseObject);
          String caseEventTypeId = dbCaseEvent.getCaseEventTypeId();
          caseEventView.setCaseEventTypeId(caseEventTypeId != null ?
              caseEventTypeId : DictionaryConstants.CASE_EVENT_TYPE);
          caseEventViewList.add(caseEventView);
        }
      }
      catch (Exception e)
      {
        throw new WebServiceException(e);
      }
    }
    return caseEventViewList;
  }
  
  private String[] getSplitPk(String id, int pkElementsCount)
  {
    String[] pk = id.split(PK_SEPARATOR);
    if (pk.length != pkElementsCount) 
      throw new WebServiceException("INVALID_ID_FORMAT");
    
    return pk;
  }
  
  private String getCompositePk(String[] keyElements)
  {
    String compositePk = null;
    
    if (keyElements != null)
    {
      StringBuffer sb = new StringBuffer();
      sb.append(keyElements[0]);
      for (int i = 1; i < keyElements.length; i++)
      {
        sb.append(PK_SEPARATOR);
        sb.append(keyElements[i]);
      }
      compositePk = sb.toString();
    }
    
    return compositePk;
  }
  
  private Query setQueryParameters(String queryName, Object filter)
  {
    Query query = em.createNamedQuery(queryName);
    try
    {
      Class filterClass = filter.getClass();
      Method[] methods = filterClass.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++)
      {
        String methodName = methods[i].getName();
        if (methodName != null && 
           (methodName.startsWith("get") || methodName.startsWith("is")) &&
           (!"getFirstResult".equals(methodName) && 
            !"getMaxResults".equals(methodName) &&
            !"getTypeList".equals(methodName) &&
            !"isExcludeMetadata".equals(methodName) &&
            !"getOutputProperty".equals(methodName)))
        {   
          String fieldName = 
            (methodName.startsWith("get") ? 
             methodName.substring(3) : methodName.substring(2));
          fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
          Object paramValue = methods[i].invoke(filter, new Object[]{});
          query.setParameter(fieldName, paramValue);
        }
      }
    }
    catch (Exception e)
    {
      throw new WebServiceException(e);
    }    
    return query;
  }

  private void applyCaseEventFilter(Query query, CaseEventFilter filter,
    WSEndpoint endpoint)
  {
    query.setParameter("caseId", filter.getCaseId());
    query.setParameter("eventId", filter.getEventId());
  }  
  
  private int count(String queryName, Object filter)
  {
    Query query = setQueryParameters(queryName, filter);
    query.setFirstResult(0);
    query.setMaxResults(1);

    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  private void createCaseProperties(Case caseObject) throws Exception
  {
    storeCaseProperties(caseObject, false);
  }

  private void updateCaseProperties(Case caseObject) throws Exception
  {
    storeCaseProperties(caseObject, true);
  }

  private void storeCaseProperties(Case caseObject, boolean isUpdate) throws Exception
  {
    String caseId = caseObject.getCaseId();

    //Remove current
    if (isUpdate)
      removeCaseProperties(caseId);

    //Store classId as property
    Property classIdProperty =
      DictionaryUtils.getProperty(caseObject, "classId");
    if (classIdProperty != null)
    {
      persistProperty(em, DBCaseProperty.class, caseId, classIdProperty);
      if (caseObject.getClassId() != null && !caseObject.getClassId().isEmpty())
      {
        Property p =
          DictionaryUtils.getPropertyByName(caseObject.getProperty(), "classId");
        caseObject.getProperty().remove(p);
      }
    }

    //Store keywords
    Type type = getCaseType(caseObject);
    KeywordsManager.storeKeywords(caseObject, type, "title");

    //Insert new properties
    for (Property property : caseObject.getProperty())
    {
      persistProperty(em, DBCaseProperty.class, caseId, property);
    }

  }
  
  private void storeCaseCaseProperties(CaseCase caseCase, boolean isUpdate) throws Exception
  {
    String caseCaseId = caseCase.getCaseCaseId();

    //Remove current properties
    if (isUpdate) removeCaseCaseProperties(caseCaseId);

    if (!caseCase.getProperty().isEmpty())
    {
      //Insert new properties
      for (Property property : caseCase.getProperty())
      {
        persistProperty(em, DBCaseCaseProperty.class, caseCaseId, property);
      }
    }
  }  
  
  private void storeCaseEventProperties(CaseEvent caseEvent, boolean isUpdate) throws Exception
  {
    String caseEventId = caseEvent.getCaseEventId();

    //Remove current properties
    if (isUpdate) removeCaseEventProperties(caseEventId);

    if (!caseEvent.getProperty().isEmpty())
    {
      //Insert new properties
      for (Property property : caseEvent.getProperty())
      {
        persistProperty(em, DBCaseEventProperty.class, caseEventId, property);
      }
    }
  }
  
  private void storeInterventionProperties(Intervention intervention, boolean isUpdate) throws Exception
  {
    String intId = intervention.getIntId();
    
    //Remove current properties
    if (isUpdate) removeInterventionProperties(intId);
    
    if (!intervention.getProperty().isEmpty())
    {
      //Insert new properties
      for (Property property : intervention.getProperty())
      {
        persistProperty(em, DBInterventionProperty.class, intId, property);
      }
    }
  }  

  private String generateTypeCaseId(Type type) throws Exception
  {
    String typeCaseId = null;
    PropertyDefinition typeCaseIdPd =
      type.getPropertyDefinition(CaseConstants.TYPECASEID);
    if (typeCaseIdPd != null)
    {
      TypeCaseIdSequenceManager seqStore = new TypeCaseIdSequenceManager(em);
      String format = null;
      PropertyDefinition formatPd =
        type.getPropertyDefinition("_typeCaseIdFormat");
      if (formatPd != null)
        format = formatPd.getValue().get(0);
      typeCaseId = seqStore.getTypeCaseId(type.getTypeId(), format);

      if (typeCaseId == null)
        throw new Exception("cases:INVALID_TYPECASEID");
    }
    return typeCaseId;
  }

  private Type getCaseType(Case caseObject)
  {
    Type type = null;
    if (caseObject != null)
    {
      String caseTypeId = caseObject.getCaseTypeId();
      caseTypeId =
        getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, caseTypeId);
      type = TypeCache.getInstance().getType(caseTypeId);
    }
    
    return type;
  }

  private List<DBAccessControl> loadAccessControlList(String caseId)
  {
    Query query = em.createNamedQuery("findCaseAccessControl");
    query.setParameter("caseId", caseId);
    return query.getResultList();
  }

  private void storeAccessControlList(Case caseObject,
    List<DBAccessControl> dbAccessControlList)
  {
    String caseId = caseObject.getCaseId();
    ArrayList<AccessControl> ACL = new ArrayList<AccessControl>();
    ACL.addAll(caseObject.getAccessControl());

    if (dbAccessControlList != null)
    {
      for (DBAccessControl dbAccessControl : dbAccessControlList)
      {
        AccessControl accessControl =
           extractAccessControl(dbAccessControl, ACL);
        if (accessControl == null)
        {
          // remove ac
          em.remove(dbAccessControl);
        }
        else
        {
          // update ac
          dbAccessControl.copyFrom(accessControl);
          em.merge(dbAccessControl);
        }
      }
    }
    // insert new ac
    for (AccessControl accessControl : ACL)
    {
      DBAccessControl dbAccessControl =
        new DBAccessControl(accessControl);
      dbAccessControl.setCaseId(caseId);
      em.persist(dbAccessControl);
    }
  }

  private <T extends DBProperty> void persistProperty(EntityManager em, Class<T> dbClass, String parentId,
    Property property) throws Exception
  {
     List<T> dbProperties = toDBProperties(dbClass, property);
     for (DBProperty dbProperty : dbProperties)
     {
       dbProperty.setId(parentId);
       em.persist(dbProperty);
     }
  }

  private void removeCaseProperties(String caseId)
  {
    removeProperties(em, "removeCaseProperties", caseId);
  }
  
  private void removeCaseCaseProperties(String caseCaseId)
  {
    removeProperties(em, "removeCaseCaseProperties", caseCaseId);
  }  
  
  private void removeInterventionProperties(String intId)
  {
    removeProperties(em, "removeInterventionProperties", intId);
  }
  
  private void removeCaseEventProperties(String caseEventId)
  {
    removeProperties(em, "removeCaseEventProperties", caseEventId);
  }
  
  private void removeProperties(EntityManager em, String queryName, String idValue)
  {
    Query query =
      em.createNamedQuery(queryName);
    query.setParameter("id", idValue);
    query.executeUpdate();
  }
  
  private void removeCaseCases(String caseId)
  {
    Set<DBCaseCase> dbCaseCaseSet = new HashSet();

    Query query = em.createNamedQuery("findCaseCases");
    query.setParameter("caseId", caseId);
    query.setParameter("relCaseId", null);      
    query.setParameter("caseCaseTypeId", null);
    List<Object[]> dbList = query.getResultList();
    if (dbList != null)
    {
      for (Object[] row : dbList)
      {
        DBCaseCase dbCaseCase = (DBCaseCase)row[0];
        dbCaseCaseSet.add(dbCaseCase);
      }        
    }

    query = em.createNamedQuery("findCaseCases");
    query.setParameter("caseId", null);
    String globalCaseId = getWSEndpoint().toGlobalId(Case.class, caseId);
    query.setParameter("relCaseId", globalCaseId); 
    query.setParameter("caseCaseTypeId", null);
    dbList = query.getResultList();
    if (dbList != null)
    {
      for (Object[] row : dbList)
      {
        DBCaseCase dbCaseCase = (DBCaseCase)row[0];
        dbCaseCaseSet.add(dbCaseCase);
      }
    }

    for (DBCaseCase dbCaseCase : dbCaseCaseSet)
    {
      removeCaseCaseProperties(dbCaseCase.getCaseCaseId());
      em.remove(dbCaseCase);
    }    
  }  
  
  private <T extends DBProperty> List<T> loadDBProperties(String queryName,
           String idValue)
  {
    Query query = em.createNamedQuery(queryName);
    query.setParameter("id", idValue);
    return query.getResultList();
  }  
  
  private <T extends DBProperty> List<T> toDBProperties(Class<T> dbClass, Property property) 
          throws Exception
  {
    int index = 0;
    List result = new ArrayList();
    for(String v : property.getValue())
    {
      T dbProperty = (T)dbClass.newInstance();
      dbProperty.setName(property.getName());
      dbProperty.setIndex(index++);
      dbProperty.setValue(v);
      result.add(dbProperty);
    }
    return result;    
  }

  private <T extends DBProperty> List<Property> toProperties(List<T> dbProperties)
  {
    HashMap<String,Property> auxMap = new HashMap();
    List<Property> properties = new ArrayList();

    for (T dbProperty : dbProperties)
    {
      Property property = auxMap.get(dbProperty.getName());
      if (property == null)
      {
        property = new Property();
        property.setName(dbProperty.getName());
        auxMap.put(dbProperty.getName(), property);
        properties.add(property);
      }
      property.getValue().add(dbProperty.getValue());
    }

    return properties;
  }

  private AccessControl extractAccessControl(DBAccessControl dbAccessControl,
    List<AccessControl> ACL)
  {
    AccessControl ac = null;
    int i = 0;
    while (i < ACL.size() && ac == null)
    {
      AccessControl aci = ACL.get(i);
      if (aci.getRoleId().equals(dbAccessControl.getRoleId()) &&
          aci.getAction().equals(dbAccessControl.getAction()))
      {
        ac = ACL.remove(i);
      }
      else i++;
    }
    return ac;
  }

  private boolean canUserReadCase(User user, Case caseObject)
  {
    return canUserDoAction(user, caseObject, DictionaryConstants.READ_ACTION);
  }

  private boolean canUserCreateCase(User user, Case caseObject)
  {
    return checkTypeACL(user.getRoles(), caseObject, DictionaryConstants.CREATE_ACTION);
  }

  private boolean canUserModifyCase(User user, Case caseObject)
  {
    return canUserDoAction(user, caseObject, DictionaryConstants.WRITE_ACTION);
  }

  private boolean canUserDeleteCase(User user, Case caseObject)
  {
    return canUserDoAction(user, caseObject, DictionaryConstants.DELETE_ACTION);
  }

  private boolean checkTypeACL(Set<String> userRoles, Case caseObject, String action)
  {
    String caseTypeId = caseObject.getCaseTypeId();
    try
    {
      caseTypeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, caseTypeId);
      Type caseType = TypeCache.getInstance().getType(caseTypeId);
      return (caseType.canPerformAction(action, userRoles)
        || userRoles.contains(CaseConstants.CASE_ADMIN_ROLE));
    }
    catch (Exception ex)
    {
      return userRoles.contains(CaseConstants.CASE_ADMIN_ROLE);
    }
  }

  private boolean checkCaseACL(Set<String> userRoles, Case caseObject, String action)
  {
    List<AccessControl> acl = caseObject.getAccessControl();
    for (AccessControl ac : acl)
    {
      if (ac.getAction().equals(action) && userRoles.contains(ac.getRoleId()))
        return true;
    }
    return false;
  }

  private boolean canUserDoAction(User user, Case caseObject, String action)
  {
    Set<String> userRoles = user.getRoles();
    if (checkTypeACL(userRoles, caseObject, action))
      return true;
    else
      return checkCaseACL(userRoles, caseObject, action);
  }

  private void addNominalRoles(User user, Case caseObject)
  {
    AccessControl ac = new AccessControl();
    ac.setRoleId(SecurityConstants.SELF_ROLE_PREFIX + user.getUserId().trim() +
      SecurityConstants.SELF_ROLE_SUFFIX);
    ac.setAction(DictionaryConstants.READ_ACTION);
    caseObject.getAccessControl().add(ac);
    ac = new AccessControl();
    ac.setRoleId(SecurityConstants.SELF_ROLE_PREFIX + user.getUserId().trim() +
      SecurityConstants.SELF_ROLE_SUFFIX);
    ac.setAction(DictionaryConstants.WRITE_ACTION);
    caseObject.getAccessControl().add(ac);
    ac = new AccessControl();
    ac.setRoleId(SecurityConstants.SELF_ROLE_PREFIX + user.getUserId().trim() +
      SecurityConstants.SELF_ROLE_SUFFIX);
    ac.setAction(DictionaryConstants.DELETE_ACTION);
    caseObject.getAccessControl().add(ac);
  }

  /**
   * 
   * @param caseId
   * @param personId
   * @return personId is a person stored as a CasePerson. If personId is 
   * omitted return true.
   */
  private boolean isValidCasePerson(String caseId, String personId)
  {
    if (personId == null || "".equals(personId))
      return true;
      
    Query query = em.createNamedQuery("findCasePersons");
    query.setParameter("caseId", caseId);
    query.setParameter("personId", personId);
    query.setParameter("casePersonTypeId", null);    
    
    List result = query.getResultList();
    
    return (result != null && !result.isEmpty());
  }
  
  private void validateCase(Case caseObject, Type type)
  {
    //Module constraints validation
    if (type == null)
      throw new WebServiceException("cases:INVALID_CASE_TYPE");

    //Remove typeCaseId entries
    for (int i = 0; i < caseObject.getProperty().size(); i++)
    {
      if (CaseConstants.TYPECASEID.equals(caseObject.getProperty().get(i).getName()))
        caseObject.getProperty().remove(i);
    }

    //Dictionary properties validation
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(caseObject, "caseId");
  }

  private void validateCaseAddress(CaseAddress caseAddress)  
  {
    if (caseAddress.getAddressId() == null || 
      caseAddress.getAddressId().equals(""))
      throw new WebServiceException("cases:INVALID_CASE_ADDRESS");
    if (caseAddress.getStartDate() != null && caseAddress.getEndDate() != null &&
      caseAddress.getStartDate().compareTo(caseAddress.getEndDate()) > 0)
      throw new WebServiceException("cases:INVALID_CASE_ADDRESS_DATE_RANGE");

      Type type =
        TypeCache.getInstance().getType(DictionaryConstants.CASE_ADDRESS_TYPE);
      WSTypeValidator validator = new WSTypeValidator(type);
      validator.validate(caseAddress, "caseAddressId");
  }
  
  private void validateCasePerson(CasePerson casePerson)
    throws WebServiceException
  {
    if (casePerson.getPersonId() == null || casePerson.getPersonId().equals(""))
      throw new WebServiceException("cases:INVALID_CASE_PERSON");
    if (casePerson.getCasePersonTypeId() == null ||
      casePerson.getCasePersonTypeId().trim().length() == 0)
      throw new WebServiceException("cases:INVALID_CASE_PERSON_TYPE");
    
    String startDate = casePerson.getStartDate();    
    if (!TextUtils.isValidInternalDate(startDate))
      throw new WebServiceException("INVALID_DATE_FORMAT");
    String endDate = casePerson.getEndDate();
    if (!TextUtils.isValidInternalDate(endDate))
      throw new WebServiceException("INVALID_DATE_FORMAT");

    if (startDate != null && endDate != null)
    {
      if (startDate.compareTo(endDate) > 0)
        throw new WebServiceException("INVALID_PERIOD");
    }

    String casePersonTypeId = casePerson.getCasePersonTypeId();
    if (casePersonTypeId == null)
      casePersonTypeId = DictionaryConstants.CASE_PERSON_TYPE;
    else
      casePersonTypeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, casePersonTypeId);
    Type type = TypeCache.getInstance().getType(casePersonTypeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(casePerson, "casePersonId");
  }
  
  private void validateIntervention(Intervention intervention)
  {
    if (intervention.getIntTypeId() == null ||
      intervention.getIntTypeId().trim().length() == 0)
      throw new WebServiceException("cases:INVALID_CASE_INTERVENTION_TYPE");

    //Dictionary properties validation
    String intTypeId = intervention.getIntTypeId();
    if (intTypeId == null)
      intTypeId = DictionaryConstants.INTERVENTION_TYPE;
    else
      intTypeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, intTypeId);
    Type type = TypeCache.getInstance().getType(intTypeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(intervention, "intId");
  }
  
  private void validateDemand(Demand demand)
  {
    if (demand.getDemandTypeId() == null ||
      demand.getDemandTypeId().trim().length() == 0)
      throw new WebServiceException("cases:INVALID_CASE_DEMAND_TYPE");

    String demandTypeId = demand.getDemandTypeId();
    if (demandTypeId != null)
      demandTypeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, demandTypeId);
    Type type = TypeCache.getInstance().getType(demandTypeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(demand, "demandId");
  }
  
  private void validateProblem(Problem problem)
  {
    if (problem.getProbTypeId() == null ||
      problem.getProbTypeId().trim().length() == 0)
      throw new WebServiceException("cases:INVALID_CASE_PROBLEM_TYPE");

    if (!isValidCasePerson(problem.getCaseId(), problem.getPersonId()))
      throw new WebServiceException("cases:INVALID_CASE_PERSON");

    String probTypeId = problem.getProbTypeId();
    if (probTypeId != null)
      probTypeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, probTypeId);
    Type type = TypeCache.getInstance().getType(probTypeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(problem, "probId");
  }

  private void validateCaseDocument(CaseDocument caseDocument)
  {
    if (caseDocument.getDocId() == null || caseDocument.getDocId().equals(""))
      throw new WebServiceException("cases:INVALID_CASE_DOCUMENT");
    if (caseDocument.getCaseId() == null || caseDocument.getCaseId().equals(""))
      throw new WebServiceException("cases:INVALID_DOCUMENT_CASE");
    if (caseDocument.getCaseDocTypeId() == null ||
        caseDocument.getCaseDocTypeId().trim().length() == 0)
      throw new WebServiceException("cases:INVALID_CASE_DOCUMENT_TYPE");

    String typeId = caseDocument.getCaseDocTypeId();
    if (typeId != null)
      typeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, typeId);
    Type type = TypeCache.getInstance().getType(typeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(caseDocument, "caseDocId");
  }

  private void validateCaseEvent(CaseEvent caseEvent)
  {
    if (caseEvent.getCaseId() == null || caseEvent.getCaseId().equals(""))
      throw new WebServiceException("cases:INVALID_EVENT_CASE");
    if (caseEvent.getEventId() == null || caseEvent.getEventId().equals(""))
      throw new WebServiceException("cases:INVALID_CASE_EVENT");
    if (caseEvent.getCaseEventTypeId() == null ||
        caseEvent.getCaseEventTypeId().trim().length() == 0)
      throw new WebServiceException("cases:INVALID_CASE_EVENT_TYPE");

    String typeId = caseEvent.getCaseEventTypeId();
    typeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, typeId);
    Type type = TypeCache.getInstance().getType(typeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(caseEvent, "caseEventId");    
  }

  private void validateCaseCase(CaseCase caseCase)
  {
    if (caseCase.getRelCaseId() == null || caseCase.getRelCaseId().equals(""))
      throw new WebServiceException("cases:INVALID_CASE_CASE");
    if (caseCase.getCaseId() == null || caseCase.getCaseId().equals(""))
      throw new WebServiceException("cases:INVALID_CASE_CASE");
    if (caseCase.getCaseCaseTypeId() == null ||
        caseCase.getCaseCaseTypeId().trim().length() == 0)
      throw new WebServiceException("cases:INVALID_CASE_CASE_TYPE");
    if (caseCase.getStartDate() != null && caseCase.getEndDate() != null &&
      caseCase.getStartDate().compareTo(caseCase.getEndDate()) > 0)
      throw new WebServiceException("cases:INVALID_CASE_CASE_DATE_RANGE");

    String typeId = caseCase.getCaseCaseTypeId();
    typeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class, typeId);
    Type type = TypeCache.getInstance().getType(typeId);
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(caseCase, "caseCaseId");
  }
  
  private void validateInterventionProblem(InterventionProblem interventionProblem)
  {
    if (interventionProblem.getProbId() == null || interventionProblem.getProbId().equals(""))
      throw new WebServiceException("cases:INVALID_INTERVENTION_PROBLEM");
    if (interventionProblem.getIntId() == null || interventionProblem.getIntId().equals(""))
      throw new WebServiceException("cases:INVALID_INTERVENTION_PROBLEM");
  }

  private void validateInterventionFilter(InterventionFilter filter) 
    throws Exception
  {   
    if (StringUtils.isBlank(filter.getCaseId()) &&
        StringUtils.isBlank(filter.getIntTypeId()) &&
        StringUtils.isBlank(filter.getFromDate()) &&
        StringUtils.isBlank(filter.getToDate()) && 
        StringUtils.isBlank(filter.getPersonId()) && 
        StringUtils.isBlank(filter.getComments()) &&
        filter.getProperty().isEmpty() &&
        filter.getMaxResults() == 0)
      throw new Exception("FILTER_NOT_ALLOWED");
  }
  
  private void validateCaseFilter(CaseFilter filter) throws Exception
  {
    if (filter.getCaseId().isEmpty() &&
        StringUtils.isBlank(filter.getCaseTypeId()) &&
        StringUtils.isBlank(filter.getDescription()) &&
        StringUtils.isBlank(filter.getFromDate()) &&
        StringUtils.isBlank(filter.getToDate()) && 
        StringUtils.isBlank(filter.getPersonId()) && 
        StringUtils.isBlank(filter.getSearchExpression()) && 
        StringUtils.isBlank(filter.getTitle()) &&
        StringUtils.isBlank(filter.getState()) && 
        filter.getProperty().isEmpty() &&
        filter.getMaxResults() == 0)
      throw new Exception("FILTER_NOT_ALLOWED");    
  }
  
  private WSEndpoint getWSEndpoint()
  {
    if (endpoint == null)
    {
      String endpointName = WSUtils.getServletAdapter(wsContext).getName();
      endpoint = WSDirectory.getInstance().getEndpoint(endpointName);
    }
    return endpoint;
  }

  private KernelManagerPort getKernelManagerPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(KernelManagerService.class);

      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      return endpoint.getPort(KernelManagerPort.class, credentials.getUserId(),
        credentials.getPassword());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private DocumentManagerPort getDocumentManagerPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(DocumentManagerService.class);

      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      return endpoint.getPort(DocumentManagerPort.class, credentials.getUserId(),
        credentials.getPassword());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private AgendaManagerPort getAgendaManagerPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(AgendaManagerService.class);

      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      return endpoint.getPort(AgendaManagerPort.class, credentials.getUserId(),
        credentials.getPassword());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  private CaseManagerPort getCaseManagerPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(CaseManagerService.class);

      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      return endpoint.getPort(CaseManagerPort.class, credentials.getUserId(),
        credentials.getPassword());
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(
      new URL("http://localhost/wsdirectory"));
    WSEndpoint endpoint = wsDirectory.getEndpoint(CaseManagerService.class);
    CaseManagerPort casePort = endpoint.getPort(CaseManagerPort.class, 
      "xxxxxx", "yyyyyy");
    
    CaseEventFilter filter;
    List<CaseEventView> cevList;
    
    //Searching case events
    filter = new CaseEventFilter();
    String caseId = "sf:8626";
    filter.setCaseId(caseId);
    filter.setFirstResult(0);
    filter.setMaxResults(2);
    cevList = casePort.findCaseEventViews(filter);
    System.out.println("Case " + caseId);
    for (CaseEventView cev : cevList)
    {
      System.out.println(" - Event " + cev.getEvent().getEventId() + " - " + cev.getEvent().getSummary());
    }
    
    //Searching event cases
    filter = new CaseEventFilter();
    String eventId = "42891";
    filter.setEventId(eventId);    
    cevList = casePort.findCaseEventViews(filter);
    System.out.println("Event " + eventId);
    for (CaseEventView cev : cevList)
    {
      System.out.println(" - Case " + cev.getCaseObject().getCaseId() + " - " + cev.getCaseObject().getTitle());
    }
    
  }


}
