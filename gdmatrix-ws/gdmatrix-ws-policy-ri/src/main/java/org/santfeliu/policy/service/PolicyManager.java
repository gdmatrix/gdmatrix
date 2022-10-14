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
package org.santfeliu.policy.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.policy.PolicyManagerPort;
import org.matrix.policy.CasePolicy;
import org.matrix.policy.CasePolicyFilter;
import org.matrix.policy.CasePolicyView;
import org.matrix.policy.ClassPolicy;
import org.matrix.policy.ClassPolicyFilter;
import org.matrix.policy.ClassPolicyView;
import org.matrix.policy.DisposalHold;
import org.matrix.policy.DisposalHoldFilter;
import org.matrix.policy.DocumentPolicy;
import org.matrix.policy.DocumentPolicyFilter;
import org.matrix.policy.DocumentPolicyView;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyFilter;
import org.matrix.policy.PolicyState;
import org.matrix.dic.Property;
import org.matrix.policy.PolicyConstants;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.WSTypeValidator;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.ws.WSUtils;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.MultiInstance;

/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.policy.PolicyManagerPort")
@HandlerChain(file="handlers.xml")
@MultiInstance
public class PolicyManager implements PolicyManagerPort
{
  private static final Logger LOGGER = Logger.getLogger("Policy");

  @Resource
  WebServiceContext wsContext;

  @PersistenceContext(unitName="policy_ri")
  EntityManager entityManager;

  private WSEndpoint endpoint;

  @Initializer
  public void initialize(String endpointName)
  {
    // create emf
  }

  // Policy
  @Override
  public Policy loadPolicy(String policyId)
  {
    LOGGER.log(Level.INFO, "loadPolicy {0}", policyId);
    DBPolicy dbPolicy = entityManager.find(DBPolicy.class,
      getWSEndpoint().toLocalId(Policy.class, policyId));

    if (dbPolicy == null)
      throw new WebServiceException("policy:POLICY_NOT_FOUND");

    Policy policy = new Policy();
    dbPolicy.copyTo(policy);

    //Properties
    List<DBPolicyProperty> dbPropertyList =
      loadPolicyPropertyList(policyId);
    Property property = null;
    for (DBPolicyProperty dbProperty : dbPropertyList)
    {
      if (dbProperty.getIndex() == 0)
      {
        property = new Property();
        policy.getProperty().add(property);
      }
      dbProperty.copyTo(property);
    }

    policy = getWSEndpoint().toGlobal(Policy.class, policy);
    return policy;
  }

  @Override
  public Policy storePolicy(Policy policy)
  {
    LOGGER.log(Level.INFO, "storePolicy {0}", policy.getPolicyId());

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    policy = getWSEndpoint().toLocal(Policy.class, policy);

    validatePolicy(policy);

    String policyId = policy.getPolicyId();
    if (policyId == null) // new
    {
      DBPolicy dbPolicy = new DBPolicy();
      auditPolicy(policy, dbPolicy, user);
      dbPolicy.copyFrom(policy);
      entityManager.persist(dbPolicy);
      dbPolicy.copyTo(policy);
    }
    else // update
    {
      DBPolicy dbPolicy =
        entityManager.getReference(DBPolicy.class, policyId);
      auditPolicy(policy, dbPolicy, user);
      dbPolicy.copyFrom(policy);
      entityManager.merge(dbPolicy);
      dbPolicy.copyTo(policy);
    }

    //Properties
    List<DBPolicyProperty> dbPropertyList =
      loadPolicyPropertyList(policyId);

    List<Property> propertyList = policy.getProperty();
    if (propertyList != null)
    {
      for (Property property : propertyList)
      {
        List<String> values = property.getValue();
        for (int index = 0; index < values.size(); index++)
        {
          DBPolicyProperty dbProperty =
            extractDBPolicyProperty(property.getName(), index, dbPropertyList);

          if (dbProperty == null)
          {
            // persist property
            DBPolicyProperty dbPolicyProperty = new DBPolicyProperty();
            dbPolicyProperty.copyFrom(policyId, index, property);
            entityManager.persist(dbPolicyProperty);
          }
          else
          {
            // update property
            String value = values.get(index);
            if (!dbProperty.getValue().equals(value))
            {
              dbProperty.setValue(value);
              entityManager.merge(dbProperty);
            }
          }
        }
      }
    }
    // Remove old properties
    for (DBPolicyProperty dbProperty : dbPropertyList)
    {
      entityManager.remove(dbProperty);
    }
    policy = getWSEndpoint().toGlobal(Policy.class, policy);

    return policy;
  }

  @Override
  public boolean removePolicy(String policyId)
  {
    LOGGER.log(Level.INFO, "removePolicy {0}", policyId);

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    boolean removed;
    try
    {
      DBPolicy dbPolicy =
        entityManager.getReference(DBPolicy.class, policyId);

      //Properties
      List<DBPolicyProperty> dbPropertyList =
        loadPolicyPropertyList(policyId);
      for (DBPolicyProperty dbProperty : dbPropertyList)
      {
        entityManager.remove(dbProperty);
      }

      entityManager.remove(dbPolicy);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      removed = false;
    }
    return removed;
  }

  @Override
  public int countPolicies(PolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "countPolicies {0}", filter.getPolicyTypeId());
    Query query = entityManager.createNamedQuery("countPolicies");
    applyFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<Policy> findPolicies(PolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "findPolicies {0}", filter.getPolicyTypeId());
    List<Policy> results = new ArrayList<>();
    Query query = entityManager.createNamedQuery("findPolicies");
    applyFilter(query, filter);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBPolicy> dbPolicies =
      (List<DBPolicy>)query.getResultList();
    for (DBPolicy dbPolicy : dbPolicies)
    {
      Policy policy = new Policy();
      dbPolicy.copyTo(policy);
      results.add(policy);
    }
    return results;
  }

  // ClassPolicy
  @Override
  public ClassPolicy loadClassPolicy(String classPolicyId)
  {
    LOGGER.log(Level.INFO, "loadClassPolicy {0}", classPolicyId);
    DBClassPolicy dbClassPolicy =
      entityManager.find(DBClassPolicy.class, classPolicyId);
    if (dbClassPolicy == null)
      throw new WebServiceException("policy:CLASS_POLICY_NOT_FOUND");
    ClassPolicy classPolicy = new ClassPolicy();
    dbClassPolicy.copyTo(classPolicy);
    return classPolicy;
  }

  @Override
  public ClassPolicy storeClassPolicy(ClassPolicy classPolicy)
  {
    LOGGER.log(Level.INFO, "storeClassPolicy {0}",
      classPolicy.getClassPolicyId());

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    validateClassPolicy(classPolicy);

    String classPolicyId = classPolicy.getClassPolicyId();
    if (classPolicyId == null) // new
    {
      DBClassPolicy dbClassPolicy = new DBClassPolicy();
      auditClassPolicy(classPolicy, dbClassPolicy, user);
      dbClassPolicy.copyFrom(classPolicy);
      entityManager.persist(dbClassPolicy);
      dbClassPolicy.copyTo(classPolicy);
    }
    else // update
    {
      DBClassPolicy dbClassPolicy =
        entityManager.getReference(DBClassPolicy.class, classPolicyId);
      auditClassPolicy(classPolicy, dbClassPolicy, user);
      dbClassPolicy.copyFrom(classPolicy);
      entityManager.merge(dbClassPolicy);
      dbClassPolicy.copyTo(classPolicy);
    }
    return classPolicy;
  }

  @Override
  public boolean removeClassPolicy(String classPolicyId)
  {
    LOGGER.log(Level.INFO, "removeClassPolicy {0}", classPolicyId);

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    boolean removed;
    try
    {
      DBClassPolicy dbClassPolicy =
        entityManager.getReference(DBClassPolicy.class, classPolicyId);
      entityManager.remove(dbClassPolicy);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      removed = false;
    }
    return removed;
  }

  @Override
  public int countClassPolicies(ClassPolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "countClassPolicies {0}", filter.getClassId());
    Query query = entityManager.createNamedQuery("countClassPolicies");
    applyFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<ClassPolicy> findClassPolicies(ClassPolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "findClassPolicies {0}", filter.getClassId());
    throw new WebServiceException("NOT_IMPLEMENTED");
  }

  @Override
  public List<ClassPolicyView> findClassPolicyViews(ClassPolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "findClassPolicyViews {0}", filter.getClassId());
    List<ClassPolicyView> results = new ArrayList<>();
    Query query = entityManager.createNamedQuery("findClassPolicyViews");
    applyFilter(query, filter);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<Object[]> rows = query.getResultList();
    for (Object[] row : rows)
    {
      ClassPolicyView view = new ClassPolicyView();

      ClassPolicy classPolicy = new ClassPolicy();
      ((DBClassPolicy)row[0]).copyTo(classPolicy);
      view.setClassPolicy(classPolicy);

      if (StringUtils.isBlank(filter.getPolicyId()))
      {
        Policy policy = new Policy();
        ((DBPolicy)row[1]).copyTo(policy);
        view.setPolicy(policy);
      }

      if (StringUtils.isBlank(filter.getClassId()))
      {
        org.matrix.classif.Class cls = new org.matrix.classif.Class();
        cls.setClassId(classPolicy.getClassId());
        view.setClazz(cls);
      }

      results.add(view);
    }
    return results;
  }


  // CasePolicy
  @Override
  public CasePolicy loadCasePolicy(String casePolicyId)
  {
    LOGGER.log(Level.INFO, "loadCasePolicy {0}", casePolicyId);
    DBCasePolicy dbCasePolicy =
      entityManager.find(DBCasePolicy.class, casePolicyId);
    if (dbCasePolicy == null)
      throw new WebServiceException("policy:CASE_POLICY_NOT_FOUND");
    CasePolicy casePolicy = new CasePolicy();
    dbCasePolicy.copyTo(casePolicy);
    return casePolicy;
  }

  @Override
  public CasePolicy storeCasePolicy(CasePolicy casePolicy)
  {
    LOGGER.log(Level.INFO, "storeCasePolicy {0}", casePolicy.getCasePolicyId());

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    validateCasePolicy(casePolicy);

    String casePolicyId = casePolicy.getCasePolicyId();
    if (casePolicyId == null) // new
    {
      DBCasePolicy dbCasePolicy = new DBCasePolicy();
      auditCasePolicy(casePolicy, dbCasePolicy, user);
      dbCasePolicy.copyFrom(casePolicy);
      entityManager.persist(dbCasePolicy);
      dbCasePolicy.copyTo(casePolicy);
    }
    else // update
    {
      DBCasePolicy dbCasePolicy =
        entityManager.getReference(DBCasePolicy.class, casePolicyId);
      auditCasePolicy(casePolicy, dbCasePolicy, user);
      dbCasePolicy.copyFrom(casePolicy);
      entityManager.merge(dbCasePolicy);
      dbCasePolicy.copyTo(casePolicy);
    }
    return casePolicy;
  }

  @Override
  public boolean removeCasePolicy(String casePolicyId)
  {
    LOGGER.log(Level.INFO, "removeCasePolicy {0}", casePolicyId);

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    boolean removed;
    try
    {
      DBCasePolicy dbCaseDisp =
        entityManager.getReference(DBCasePolicy.class, casePolicyId);
      entityManager.remove(dbCaseDisp);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      removed = false;
    }
    return removed;
  }

  @Override
  public int countCasePolicies(CasePolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "countCasePolicies {0}", filter.getPolicyId());
    Query query = entityManager.createNamedQuery("countCasePolicies");
    applyFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<CasePolicy> findCasePolicies(CasePolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "findCasePolicies {0}", filter.getPolicyId());
    throw new WebServiceException("NOT_IMPLEMENTED");
  }

  @Override
  public List<CasePolicyView> findCasePolicyViews(CasePolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "findCasePolicyViews {0}", filter.getPolicyId());
    List<CasePolicyView> results = new ArrayList<>();
    Query query = entityManager.createNamedQuery("findCasePolicyViews");
    applyFilter(query, filter);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<Object[]> rows = query.getResultList();
    List<String> caseIdList = new ArrayList<>();
    for (Object[] row : rows)
    {
      CasePolicyView view = new CasePolicyView();
      CasePolicy casePolicy = new CasePolicy();
      Policy policy = new Policy();
      view.setCasePolicy(casePolicy);
      view.setPolicy(policy);
      DBCasePolicy dbCasePolicy = (DBCasePolicy)row[0];
      DBPolicy dbPolicy = (DBPolicy)row[1];
      dbCasePolicy.copyTo(casePolicy);
      dbPolicy.copyTo(policy);
      caseIdList.add(dbCasePolicy.getCaseId());
      results.add(view);
    }
    if (caseIdList.size() > 0) // find case info
    {
      HashMap<String, Case> caseMap = new HashMap<>();
      CaseManagerPort casePort = getCaseManagerPort();
      CaseFilter caseFilter = new CaseFilter();
      caseFilter.getCaseId().addAll(caseIdList);
      List<Case> cases = casePort.findCases(caseFilter);
      for (Case cas : cases)
      {
        caseMap.put(cas.getCaseId(), cas);
      }
      for (CasePolicyView view : results)
      {
        Case cas = caseMap.get(view.getCasePolicy().getCaseId());
        view.setCase(cas);
      }
    }
    return results;
  }

  // DocumentPolicy
  @Override
  public DocumentPolicy loadDocumentPolicy(String docPolicyId)
  {
    LOGGER.log(Level.INFO, "loadDocumentPolicy {0}", docPolicyId);
    DBDocumentPolicy dbDocPolicy =
      entityManager.find(DBDocumentPolicy.class, docPolicyId);
    if (dbDocPolicy == null)
      throw new WebServiceException("policy:DOCUMENT_POLICY_NOT_FOUND");
    DocumentPolicy docDisp = new DocumentPolicy();
    dbDocPolicy.copyTo(docDisp);
    return docDisp;
  }

  @Override
  public DocumentPolicy storeDocumentPolicy(DocumentPolicy docPolicy)
  {
    LOGGER.log(Level.INFO, "storeDocumentPolicy {0}", docPolicy.getPolicyId());

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    validateDocumentPolicy(docPolicy);

    String docPolicyId = docPolicy.getDocPolicyId();

    if (docPolicyId == null) // new
    {
      DBDocumentPolicy dbDocPolicy = new DBDocumentPolicy();
      auditDocumentPolicy(docPolicy, dbDocPolicy, user);
      dbDocPolicy.copyFrom(docPolicy);
      entityManager.persist(dbDocPolicy);
      dbDocPolicy.copyTo(docPolicy);
    }
    else // update
    {
      DBDocumentPolicy dbDocPolicy =
        entityManager.getReference(DBDocumentPolicy.class, docPolicyId);
      auditDocumentPolicy(docPolicy, dbDocPolicy, user);
      dbDocPolicy.copyFrom(docPolicy);
      entityManager.merge(dbDocPolicy);
      dbDocPolicy.copyTo(docPolicy);
    }
    return docPolicy;
  }

  @Override
  public boolean removeDocumentPolicy(String docPolicyId)
  {
    LOGGER.log(Level.INFO, "removeDocumentPolicy {0}", docPolicyId);

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    boolean removed;
    try
    {
      DBDocumentPolicy dbDocPolicy =
        entityManager.getReference(DBDocumentPolicy.class, docPolicyId);
      entityManager.remove(dbDocPolicy);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      removed = false;
    }
    return removed;
  }

  @Override
  public int countDocumentPolicies(DocumentPolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "countDocumentPolicies {0}", filter.getPolicyId());
    Query query = entityManager.createNamedQuery("countDocumentPolicies");
    applyFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<DocumentPolicy> findDocumentPolicies(DocumentPolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "findDocumentPolicies {0}", filter.getPolicyId());
    throw new WebServiceException("NOT_IMPLEMENTED");
  }

  @Override
  public List<DocumentPolicyView> findDocumentPolicyViews(
    DocumentPolicyFilter filter)
  {
    LOGGER.log(Level.INFO, "findDocumentPolicyViews {0}", filter.getPolicyId());
    List<DocumentPolicyView> results = new ArrayList<>();
    Query query = entityManager.createNamedQuery("findDocumentPolicyViews");
    applyFilter(query, filter);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<Object[]> rows = query.getResultList();
    List<String> docIdList = new ArrayList<>();
    for (Object[] row : rows)
    {
      DocumentPolicyView view = new DocumentPolicyView();
      DocumentPolicy docPolicy = new DocumentPolicy();
      Policy policy = new Policy();
      view.setDocPolicy(docPolicy);
      view.setPolicy(policy);
      DBDocumentPolicy dbDocPolicy = (DBDocumentPolicy)row[0];
      DBPolicy dbPolicy = (DBPolicy)row[1];
      dbDocPolicy.copyTo(docPolicy);
      dbPolicy.copyTo(policy);
      docIdList.add(dbDocPolicy.getDocId());
      results.add(view);
    }
    if (docIdList.size() > 0)
    {
      // find document info
      HashMap<String, Document> docMap = new HashMap<>();
      DocumentManagerPort docPort = getDocumentManagerPort();
      DocumentFilter docFilter = new DocumentFilter();
      docFilter.getDocId().addAll(docIdList);
      docFilter.setIncludeContentMetadata(true);
      docFilter.getOutputProperty().add("classId");
      List<Document> documents = docPort.findDocuments(docFilter);
      for (Document document : documents)
      {
        docMap.put(document.getDocId(), document);
      }
      for (DocumentPolicyView view : results)
      {
        Document document = docMap.get(view.getDocPolicy().getDocId());
        view.setDocument(document);
      }
    }
    return results;
  }


  // DisposalHold
  @Override
  public DisposalHold loadDisposalHold(String dispHoldId)
  {
    LOGGER.log(Level.INFO, "loadDisposalHold {0}", dispHoldId);
    DBDisposalHold dbDispHold =
      entityManager.find(DBDisposalHold.class, dispHoldId);
    if (dbDispHold == null)
      throw new WebServiceException("policy:DISPOSAL_HOLD_NOT_FOUND");
    DisposalHold dispHold = new DisposalHold();
    dbDispHold.copyTo(dispHold);
    return dispHold;
  }

  @Override
  public DisposalHold storeDisposalHold(DisposalHold dispHold)
  {
    LOGGER.log(Level.INFO, "storeDisposalHold {0}", dispHold.getDispHoldId());

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    String dispHoldId = dispHold.getDispHoldId();
    if (dispHoldId == null) // new
    {
      DBDisposalHold dbDispHold = new DBDisposalHold();
      auditDisposalHold(dispHold, dbDispHold, user);
      dbDispHold.copyFrom(dispHold);
      entityManager.persist(dbDispHold);
      dbDispHold.copyTo(dispHold);
    }
    else // update
    {
      DBDisposalHold dbDispHold =
        entityManager.getReference(DBDisposalHold.class, dispHoldId);
      auditDisposalHold(dispHold, dbDispHold, user);
      dbDispHold.copyFrom(dispHold);
      entityManager.merge(dbDispHold);
      dbDispHold.copyTo(dispHold);
    }
    return dispHold;
  }

  @Override
  public boolean removeDisposalHold(String dispHoldId)
  {
    LOGGER.log(Level.INFO, "removeDisposalHold {0}", dispHoldId);

    User user = UserCache.getUser(wsContext);

    if (!isUserAdmin(user))
      throw new WebServiceException("ACTION_DENIED");

    boolean removed;
    try
    {
      DBDisposalHold dbDispHold =
        entityManager.getReference(DBDisposalHold.class, dispHoldId);
      entityManager.remove(dbDispHold);
      removed = true;
    }
    catch (EntityNotFoundException ex)
    {
      removed = false;
    }
    return removed;
  }

  @Override
  public int countDisposalHolds(DisposalHoldFilter filter)
  {
    LOGGER.log(Level.INFO, "countDisposalHolds {0}", filter.getStartDate());
    Query query = entityManager.createNamedQuery("countDisposalHolds");
    applyFilter(query, filter);
    Number count = (Number)query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<DisposalHold> findDisposalHolds(DisposalHoldFilter filter)
  {
    LOGGER.log(Level.INFO, "findDisposalHolds {0}", filter.getStartDate());
    List<DisposalHold> results = new ArrayList<>();
    Query query = entityManager.createNamedQuery("findDisposalHolds");
    applyFilter(query, filter);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBDisposalHold> dbDispHolds =
      (List<DBDisposalHold>)query.getResultList();
    for (DBDisposalHold dbDispHold : dbDispHolds)
    {
      DisposalHold dispHold = new DisposalHold();
      dbDispHold.copyTo(dispHold);
      results.add(dispHold);
    }
    return results;
  }


  // analize methods
  @Override
  public String analizeDocument(String docId)
  {
    LOGGER.log(Level.INFO, "analizeDocument {0}", docId);
    throw new WebServiceException("NOT_IMPLEMENTED");
  }

  @Override
  public String analizeCase(String caseId)
  {
    LOGGER.log(Level.INFO, "analizeCase {0}", caseId);
    throw new WebServiceException("NOT_IMPLEMENTED");
  }

  // private methods

  private List<DBPolicyProperty> loadPolicyPropertyList(String policyId)
  {
    Query query = entityManager.createNamedQuery("findPolicyProperties");
    query.setParameter("policyId", policyId);
    return query.getResultList();
  }

  private DBPolicyProperty extractDBPolicyProperty(String name, int index,
    List<DBPolicyProperty> dbPropertyList)
  {
    DBPolicyProperty dbProperty = null;
    int i = 0;
    while (i < dbPropertyList.size() && dbProperty == null)
    {
      DBPolicyProperty pi = dbPropertyList.get(i);
      if (pi.getName().equals(name) && pi.getIndex() == index)
      {
        dbProperty = dbPropertyList.remove(i);
      }
      else i++;
    }
    return dbProperty;
  }


  private void applyFilter(Query query, PolicyFilter filter)
  {
    query.setParameter("title", addWildcards(filter.getTitle()));
    query.setParameter("description", addWildcards(filter.getDescription()));
    query.setParameter("policyTypeId",
      getWSEndpoint().toLocalId(Type.class, filter.getPolicyTypeId()));
    query.setParameter("activationDateExpression",
      filter.getActivationDateExpression());
    query.setParameter("activationCondition", filter.getActivationCondition());
    query.setParameter("mandate", addWildcards(filter.getMandate()));
    query.setParameter("evaluationCode", filter.getEvaluationCode());
  }

  private void applyFilter(Query query, DisposalHoldFilter filter)
  {
    query.setParameter("title", addWildcards(filter.getTitle()));
    query.setParameter("reason", addWildcards(filter.getReason()));
    query.setParameter("startDate", filter.getStartDate());
    query.setParameter("endDate", filter.getEndDate());
  }

  private void applyFilter(Query query, ClassPolicyFilter filter)
  {
    query.setParameter("classId", filter.getClassId());
    query.setParameter("policyId", filter.getPolicyId());
  }

  private String padDate(String date, String hhmmss)
  {
    if (date == null) return null;
    if (date.length() == 8) date += hhmmss;
    return date;
  }

  private void applyFilter(Query query, CasePolicyFilter filter)
  {
    query.setParameter("caseId", filter.getCaseId());
    query.setParameter("policyId", filter.getPolicyId());
    query.setParameter("policyTypeId",
      getWSEndpoint().toLocalId(Type.class, filter.getPolicyTypeId()));
    query.setParameter("activationDate", filter.getActivationDate());

    query.setParameter("startCreationDateTime",
      padDate(filter.getStartCreationDate(), "000000"));
    query.setParameter("endCreationDateTime",
      padDate(filter.getEndCreationDate(), "235959"));

    query.setParameter("startApprovalDateTime",
      padDate(filter.getStartApprovalDate(), "000000"));
    query.setParameter("endApprovalDateTime",
      padDate(filter.getEndApprovalDate(), "235959"));

    query.setParameter("startActivationDate", filter.getStartActivationDate());
    query.setParameter("endActivationDate", filter.getEndActivationDate());

    query.setParameter("startExecutionDateTime",
      padDate(filter.getStartExecutionDate(), "000000"));
    query.setParameter("endExecutionDateTime",
      padDate(filter.getEndExecutionDate(), "235959"));

    query.setParameter("dispHoldId", filter.getDispHoldId());
    if (filter.getState() != null)
    {
      query.setParameter("stateValue",
        DBPolicyState.toDB(filter.getState()));
    }
    else
    {
      query.setParameter("stateValue", null);
    }
  }

  private void applyFilter(Query query, DocumentPolicyFilter filter)
  {
    query.setParameter("docId", filter.getDocId());
    query.setParameter("policyId", filter.getPolicyId());
    query.setParameter("policyTypeId",
      getWSEndpoint().toLocalId(Type.class, filter.getPolicyTypeId()));
    query.setParameter("activationDate", filter.getActivationDate());

    query.setParameter("startCreationDateTime",
      padDate(filter.getStartCreationDate(), "000000"));
    query.setParameter("endCreationDateTime",
      padDate(filter.getEndCreationDate(), "235959"));

    query.setParameter("startApprovalDateTime",
      padDate(filter.getStartApprovalDate(), "000000"));
    query.setParameter("endApprovalDateTime",
      padDate(filter.getEndApprovalDate(), "235959"));

    query.setParameter("startActivationDate", filter.getStartActivationDate());
    query.setParameter("endActivationDate", filter.getEndActivationDate());

    query.setParameter("startExecutionDateTime",
      padDate(filter.getStartExecutionDate(), "000000"));
    query.setParameter("endExecutionDateTime",
      padDate(filter.getEndExecutionDate(), "235959"));

    query.setParameter("dispHoldId", filter.getDispHoldId());

    if (filter.getState() != null)
    {
      query.setParameter("stateValue",
        DBPolicyState.toDB(filter.getState()));
    }
    else
    {
      query.setParameter("stateValue", null);
    }
  }

  private String addWildcards(String value)
  {
    if (value == null || value.length() == 0) return null;
    return "%" + value.toUpperCase() + "%";
  }

  private void auditPolicy(
    Policy policy, DBPolicy dbPolicy, User user)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(new Date());
    if (policy.getPolicyId() == null)
    {
      dbPolicy.setCreationDateTime(dateTime);
      dbPolicy.setCreationUserId(user.getUserId());
      dbPolicy.setChangeDateTime(dateTime);
      dbPolicy.setChangeUserId(user.getUserId());
    }
    else
    {
      dbPolicy.setChangeDateTime(dateTime);
      dbPolicy.setChangeUserId(user.getUserId());
    }
  }

  private void auditDisposalHold(
    DisposalHold dispHold, DBDisposalHold dbDispHold, User user)
  {
  }

  private void auditClassPolicy(
    ClassPolicy classPolicy, DBClassPolicy dbClassPolicy, User user)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(new Date());
    if (classPolicy.getClassPolicyId() == null)
    {
      dbClassPolicy.setCreationDateTime(dateTime);
      dbClassPolicy.setCreationUserId(user.getUserId());
      dbClassPolicy.setChangeDateTime(dateTime);
      dbClassPolicy.setChangeUserId(user.getUserId());
    }
    else
    {
      dbClassPolicy.setChangeDateTime(dateTime);
      dbClassPolicy.setChangeUserId(user.getUserId());
    }
  }

  private void auditCasePolicy(
    CasePolicy casePolicy, DBCasePolicy dbCasePolicy, User user)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(new Date());
    if (casePolicy == null || casePolicy.getCasePolicyId() == null)
    {
      dbCasePolicy.setCreationDateTime(dateTime);
      dbCasePolicy.setCreationUserId(user.getUserId());
    }
    PolicyState newState = (casePolicy == null) ?
      PolicyState.PENDENT : casePolicy.getState();
    PolicyState prevState =
      DBPolicyState.fromDB(dbCasePolicy.getStateValue());
    if (!PolicyState.APPROVED.equals(prevState) &&
        PolicyState.APPROVED.equals(newState))
    {
      dbCasePolicy.setApprovalDateTime(dateTime);
      dbCasePolicy.setApprovalUserId(user.getUserId());
    }
    else if (!PolicyState.EXECUTED.equals(prevState) &&
        PolicyState.EXECUTED.equals(newState))
    {
      dbCasePolicy.setExecutionDateTime(dateTime);
      dbCasePolicy.setExecutionUserId(user.getUserId());
    }
  }

  private void auditDocumentPolicy(
    DocumentPolicy docPolicy, DBDocumentPolicy dbDocPolicy, User user)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(new Date());
    if (docPolicy == null || docPolicy.getDocPolicyId() == null)
    {
      dbDocPolicy.setCreationDateTime(dateTime);
      dbDocPolicy.setCreationUserId(user.getUserId());
    }
    PolicyState newState = (docPolicy == null) ?
      PolicyState.PENDENT : docPolicy.getState();
    PolicyState prevState =
      DBPolicyState.fromDB(dbDocPolicy.getStateValue());
    if (!PolicyState.APPROVED.equals(prevState) &&
        PolicyState.APPROVED.equals(newState))
    {
      dbDocPolicy.setApprovalDateTime(dateTime);
      dbDocPolicy.setApprovalUserId(user.getUserId());
    }
    else if (!PolicyState.EXECUTED.equals(prevState) &&
        PolicyState.EXECUTED.equals(newState))
    {
      dbDocPolicy.setExecutionDateTime(dateTime);
      dbDocPolicy.setExecutionUserId(user.getUserId());
    }
  }

  private DocumentManagerPort getDocumentManagerPort()
  {
    try
    {
      WSDirectory wsDirectory = WSDirectory.getInstance();
      WSEndpoint ep =
        wsDirectory.getEndpoint(DocumentManagerService.class);
      return ep.getPort(DocumentManagerPort.class,
        MatrixConfig.getProperty("adminCredentials.userId"),
        MatrixConfig.getProperty("adminCredentials.password"));
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
      WSEndpoint ep =
        wsDirectory.getEndpoint(CaseManagerService.class);
      return ep.getPort(CaseManagerPort.class,
        MatrixConfig.getProperty("adminCredentials.userId"),
        MatrixConfig.getProperty("adminCredentials.password"));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
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

  private void validatePolicy(Policy policy)
  {
    String policyTypeId = policy.getPolicyTypeId();
    if (policyTypeId == null)
      throw new WebServiceException("policy:POLICY_TYPE_UNDEFINED");
    policyTypeId = getWSEndpoint().toGlobalId(org.matrix.dic.Type.class,
      policyTypeId);

    org.santfeliu.dic.Type type = TypeCache.getInstance().getType(policyTypeId);

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(policy, "policyId");
  }

  private void validateDocumentPolicy(DocumentPolicy docPolicy)
  {
    org.santfeliu.dic.Type type =
      TypeCache.getInstance().getType(DictionaryConstants.DOCUMENT_POLICY_TYPE);

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(docPolicy, "docPolicyId");
  }

  private void validateCasePolicy(CasePolicy casePolicy)
  {
    org.santfeliu.dic.Type type =
      TypeCache.getInstance().getType(DictionaryConstants.CASE_POLICY_TYPE);

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(casePolicy, "casePolicyId");
  }

  private void validateClassPolicy(ClassPolicy classPolicy)
  {
    org.santfeliu.dic.Type type =
      TypeCache.getInstance().getType(DictionaryConstants.CLASS_POLICY_TYPE);

    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(classPolicy, "classPolicyId");
  }

  private boolean isUserAdmin(User user)
  {
    Set<String> userRoles = user.getRoles();
    return userRoles.contains(PolicyConstants.POLICY_ADMIN_ROLE);
  }
}
