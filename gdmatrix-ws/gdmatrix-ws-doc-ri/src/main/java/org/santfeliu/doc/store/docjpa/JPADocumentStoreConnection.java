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
package org.santfeliu.doc.store.docjpa;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;

import org.matrix.security.AccessControl;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.matrix.dic.Type;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;
import org.matrix.doc.State;

import org.matrix.util.ExternalEntity;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.service.DBType;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.store.DocumentStoreConnection;
import org.santfeliu.doc.store.FindDocumentsQueryBuilder;
import org.santfeliu.doc.store.FindSummariesQueryBuilder;
import org.santfeliu.jpa.JPAUtils;
import org.santfeliu.jpa.JPAQuery;


/**
 *
 * @author blanquepa
 */
public class JPADocumentStoreConnection implements DocumentStoreConnection
{
  public static final int MAX_RESULTS = 100;
  public static final int SUMMARY_MAX_RESULTS = 10;

  public static final String PK_SEPARATOR = ";";
  
  private EntityManager em;
  private WSEndpoint endpoint;

  public JPADocumentStoreConnection(WSEndpoint endpoint)
    throws Exception
  {
    this.endpoint = endpoint;
    em = JPAUtils.createEntityManager("doc_ri", endpoint.getName());
    try
    {
      EntityTransaction tx = em.getTransaction();
      tx.begin();
    }
    catch(Exception ex)
    {
      em.close();
      throw ex;
    }
  }

  @Override
  public Document storeDocument(Document document)
    throws Exception
  {
    String docId = document.getDocId();
    int version = document.getVersion();
    DBDocument dbDocument = new DBDocument();
    dbDocument.copyFrom(document, endpoint);

    DBDocument currentDocument = 
      em.find(DBDocument.class, new DBDocumentPK(docId, version));
    
    if (currentDocument != null) //update
    {
      em.merge(dbDocument);
      updateLastVersionColumn(em, dbDocument.getDocId(), 0);
    }
    else //insert
    {
      em.persist(dbDocument);
      docId = dbDocument.getDocId();
      if (docId != null)
        updateLastVersionColumn(em, dbDocument.getDocId(), dbDocument.getVersion());
    }
    
    dbDocument.copyTo(document, endpoint);

    storeSystemProperties(em, docId, version, document);
   
    return document;     
  }

  private void storeSystemProperties(EntityManager em, String docId, int version,
    Document document)
    throws Exception
  {
    if (document.getAuthorId() !=  null && document.getAuthorId().size() > 0)
      persistAuthors(em, docId, version,
        DocumentUtils.getProperty(document, "authorId"));
    if (document.getClassId() !=  null && document.getClassId().size() > 0)
      persistClassIds(em, docId, version,
        DocumentUtils.getProperty(document, "classId"));
    if (document.getCaseId() !=  null && document.getCaseId().size() > 0)
      persistCaseIds(em, docId, version,
        DocumentUtils.getProperty(document, "caseId"));
  }

  //User properties
  @Override
  public void storeProperties(String docId, int version,
    List<Property> properties)
    throws Exception
  {
    for (Property property : properties)
    {
      String name = property.getName();
      if (!"docId".equals(name) && !"docId".equals(name) 
        && !"version".equals(name) && !"title".equals(name)
        && !"language".equals(name) && !"docTypeId".equals(name)
        && !"state".equals(name) && !"authorId".equals(name)
        && !"classId".equals(name) && !"caseId".equals(name)
        && !"creationDate".equals(name))
        persistUserProperty(em, docId, version, property);
    }    
  }

  public void removeProperties(String docId, int version,
    List<Property> properties)
  {
    removeProperties(em, docId, version);
  }

  @Override
  public void storeRelatedDocuments(String docId, int version,
    List<RelatedDocument> relDocs)
    throws Exception
  {
    removeRelatedDocuments(em, docId);
    
    for (RelatedDocument relDoc : relDocs)
    {
      if (isReverseRelation(relDoc.getRelationType()))
      {
        //Persist reverse relation
        String revDocId = relDoc.getDocId();
        int revVersion = relDoc.getVersion();

        RelatedDocument revRelDoc = new RelatedDocument();
        revRelDoc.setDocId(docId);
        revRelDoc.setVersion(version);
        revRelDoc.setRelationType(revertRelation(relDoc.getRelationType()));
        revRelDoc.setName(relDoc.getName());
        revRelDoc.setCaptureDateTime(relDoc.getCaptureDateTime());
        revRelDoc.setCaptureUserId(relDoc.getCaptureUserId());
        revRelDoc.setChangeDateTime(relDoc.getChangeDateTime());
        revRelDoc.setChangeUserId(relDoc.getChangeUserId());

        DBRelatedDocument dbRelDoc =
          new DBRelatedDocument(revDocId, revRelDoc);
        em.persist(dbRelDoc);
      }
      else
      {
        //Persist direct relation
        DBRelatedDocument dbRelDoc =
          new DBRelatedDocument(docId, relDoc);
        em.persist(dbRelDoc);
      }
    }
  }

  @Override
  public void storeAccessControlList(Document document)
  {
    List<DBAccessControl> dbAccessControlList = loadAccessControlList(em,
      document.getDocId(), document.getVersion());

    storeAccessControlList(document, dbAccessControlList);
  }

  @Override
  public Document loadDocument(String docId, int version)
    throws Exception
  {
    Document document = null;
    if (docId != null)
    {
      try 
      {
        Integer.valueOf(docId);
      }
      catch(NumberFormatException e)
      {
        return null;
      }
      
      if (version == 0)
        version = getDocumentLastVersion(em, docId);

      DBDocumentPK pk = new DBDocumentPK(docId, version);
      DBDocument dbDocument = em.find(DBDocument.class, pk);
      if (dbDocument == null)
        return null;
      
      document = new Document();
      dbDocument.copyTo(document, endpoint);
      
      //Properties
      List<Property> properties = 
        getDocumentProperties(em, docId, version);
      document.getProperty().addAll(properties);
      setSysPropertiesFromPropertiesList(document);      
    
      //RelatedDocuments
      document.getRelatedDocument().addAll(
        getRelatedDocuments(em, docId, version));

      //AccessControl
      List<DBAccessControl> accessControlList =
        loadAccessControlList(em, docId, version);
      document.getAccessControl().addAll(accessControlList);
    }
    return document;
  } 
  
  @Override
  public boolean removeDocument(String docId, int version, boolean persistent) throws Exception
  {
    boolean result;    
    removeProperties(em, docId, version);
    
    //Number maxVersion = getDocumentMaxVersion(em, docId);
    Number versionsCount = getNotDeletedVersionsCount(em, docId, version);
    if (versionsCount == null || versionsCount.intValue() == 0 
      || version == DocumentConstants.DELETE_ALL_VERSIONS
      || version == DocumentConstants.PERSISTENT_DELETE)
    {
      removeRelatedDocuments(em, docId);
    }
    removeAccessControlList(em, docId, version);

    if (persistent)
    {
      result = (removeDocument(em, docId, version) > 0);
      if (result && docId != null)
        updateLastVersionColumn(em, docId, 0);
    }
    else
    {
      Document document = loadDocument(docId, version);
      document.setState(State.DELETED);
      document = storeDocument(document);
      result = document.getState().equals(State.DELETED);
    }
    return result;
  }
  
  @Override
  public void removeProperties(String docId, int version) throws Exception
  {
    removeProperties(em, docId, version);
  }
  
  @Override
  public List<Document> findDocuments(DocumentFilter filter,
    List<String> userRoles, boolean isAdminUser)
    throws Exception
  {
    List<Document> documents = new ArrayList();
   
    filter.setRolesDisabled(false);
    String docTypeId = filter.getDocTypeId();
    if (!StringUtils.isBlank(docTypeId))
    {
      ExternalEntity typeEntity = this.endpoint.getExternalEntity("Type");
      Type type = em.find(DBType.class, typeEntity.toLocalId(docTypeId));
      filter.setDocTypeId(type.getTypePath());
    }

    FindDocumentsQueryBuilder queryBuilder =
      FindDocumentsQueryBuilder.getInstance();
    queryBuilder.setCounterQuery(false);
    queryBuilder.setRoles(userRoles);
    queryBuilder.setFilter(filter);
    Query query = queryBuilder.getQuery(em);

    List<Object> documentList = query.getResultList();

    //Populate documents
    if (documentList != null && documentList.size() > 0)
    {
      Map<DBDocumentPK, Document> documentsMap = new HashMap();
      List<DBDocumentPK> docKeys = new ArrayList();

      for(Object doc : documentList)
      {
        Document document;
        if (doc instanceof Vector) //Native query
        {
          Vector dbDocument = (Vector)doc;
          document = new Document();
          document = copyTo(document, dbDocument.toArray(), endpoint);
        }
        else if (doc instanceof Object[])
        {
          Object[] dbDocument = (Object[])doc;
          document = new Document();
          document = copyTo(document, dbDocument, endpoint);
        }
        else //JPQL query
        {
          DBDocument dbDocument = (DBDocument)doc;
          document = new Document();
          dbDocument.copyTo(document, endpoint);
        }
        documents.add(document);

        //Create documentsMap to populate documents with additional properties
        DBDocumentPK key =
          new DBDocumentPK(document.getDocId(), document.getVersion());
        docKeys.add(key);

        documentsMap.put(key, document);
      }

      //Add content summary
      if (filter.isSummary())
      {
        List summaries =
          findSummaries(em, docKeys, filter.getContentSearchExpression(),
          filter.getMaxResults());
        if (summaries != null)
          setSummaries(summaries, documentsMap);
      }

      //Add output properties
      List outputProperties = filter.getOutputProperty();
      if (outputProperties != null && outputProperties.size() > 0)
      {
        List<Object[]> dbProperties =
          findDocumentProperties(em, docKeys);
        if (dbProperties != null)
          setOutputProperties(documentsMap, dbProperties, outputProperties);
      }
    }

    return documents;
  }

  @Override
  public int countDocuments(DocumentFilter filter, List<String> userRoles,
    boolean isAdminUser)
    throws Exception
  {
    int count = 0;

    String docTypeId = filter.getDocTypeId();
    if (!StringUtils.isBlank(docTypeId))
    {
      ExternalEntity typeEntity = this.endpoint.getExternalEntity("Type");
      Type type = em.find(DBType.class, typeEntity.toLocalId(docTypeId));
      filter.setDocTypeId(type.getTypePath());
    }
    FindDocumentsQueryBuilder queryBuilder =
      FindDocumentsQueryBuilder.getInstance();
    queryBuilder.setCounterQuery(true);
    queryBuilder.setRoles(userRoles);
    queryBuilder.setFilter(filter);
    Query query = queryBuilder.getQuery(em);

    try
    {
      Object part = query.getSingleResult();
      if (part != null)
      {
          if (part instanceof Vector)
            count += ((Number)((Vector)part).get(0)).intValue();
          else
            count += ((Number)part).intValue();
      }
    }
    catch (NoResultException e)
    {
      e.printStackTrace();
      count = 0;
    }
    
    return count;
  }

//  public DocumentPerson loadDocumentPerson(String docPersonId) throws Exception
//  {
//    DBDocumentPerson dbDocPerson =
//      em.find(DBDocumentPerson.class, docPersonId);
//
//    DocumentPerson docPerson = new DocumentPerson();
//    dbDocPerson.copyTo(docPerson);
//
//    return docPerson;
//  }
//
//  public boolean removeDocumentPerson(String docPersonId) throws Exception
//  {
//    DBDocumentPerson dbDocPerson =
//      em.getReference(DBDocumentPerson.class, docPersonId);
//    if (dbDocPerson == null)
//      return false;
//
//    em.remove(dbDocPerson);
//    return true;
//  }
//
//  public DocumentPerson storeDocumentPerson(DocumentPerson docPerson,
//    String userId) throws Exception
//  {
//    if (docPerson.getDocPersonId() == null)
//      return createDocumentPerson(docPerson, userId);
//
//    //Needs to load audit properties
//    DBDocumentPerson dbDocPerson = em.getReference(DBDocumentPerson.class,
//      docPerson.getDocPersonId());
//    dbDocPerson.copyFrom(docPerson);
//
//    Auditor.auditChange(dbDocPerson, userId);
//    em.merge(dbDocPerson);
//
//    return dbDocPerson;
//  }
//
//  private DocumentPerson createDocumentPerson(DocumentPerson docPerson, String userId) throws Exception
//  {
//    DBDocumentPerson dbDocPerson = new DBDocumentPerson(docPerson);
//    Auditor.auditCreation(dbDocPerson, userId);
//    em.persist(dbDocPerson);
//
//    return dbDocPerson;
//  }
//
//  public List<DocumentPersonView>
//    findDocumentPersonViews(DocumentPersonFilter filter) throws Exception
//  {
//    List<DocumentPersonView> docPersViewList = new ArrayList();
//    Query query = em.createNamedQuery("findDocumentPersons");
//    query.setFirstResult(filter.getFirstResult());
//    query.setMaxResults(filter.getMaxResults());
//    query.setParameter("docId", filter.getDocId());
//    query.setParameter("version", filter.getVersion());
//    query.setParameter("personId", filter.getPersonId());
//
//    List<DBDocumentPerson> dbDocPersonList = query.getResultList();
//    if (dbDocPersonList != null && !dbDocPersonList.isEmpty())
//    {
//      HashMap<String, List<DocumentPersonView>> personIdMap = new HashMap();
//      for (DBDocumentPerson dbDocPerson : dbDocPersonList)
//      {
//
//        DocumentPersonView docPersView = new DocumentPersonView();
//
//        docPersView.setDocPersonId(dbDocPerson.getDocPersonId());
//        docPersView.setDocPersonTypeId(dbDocPerson.getDocPersonTypeId());
//
//        String personId = dbDocPerson.getPersonId();
//        List<DocumentPersonView> dpvList = personIdMap.get(personId);
//        if (dpvList == null)
//          dpvList = new ArrayList();
//        dpvList.add(docPersView);
//        personIdMap.put(personId, dpvList);
//
//        docPersViewList.add(docPersView);
//      }
//
//      KernelManagerPort port =
//          WSPortFactory.getPort(KernelManagerPort.class,
//          MatrixConfig.getProperty("ws", "global.servicesURL") + "kernel?wsdl");
//
//      PersonFilter personFilter = new PersonFilter();
//      personFilter.getPersonId().addAll(personIdMap.keySet());
//      personFilter.setFirstResult(0);
//      personFilter.setMaxResults(0);
//      List<PersonView> personViewList = port.findPersonViews(personFilter);
//
//      for (PersonView personView : personViewList)
//      {
//        List<DocumentPersonView> dpvList = personIdMap.get(personView.getPersonId());
//        for (DocumentPersonView dpv : dpvList)
//        {
//          if (dpv != null)
//            dpv.setPersonView(personView);
//        }
//      }
//    }
//
//    return docPersViewList;
//  }

  @Override
  public boolean isContentInUse(String contentId)
    throws Exception
  {
    Query query = em.createNamedQuery("isContentInUse");
    query.setMaxResults(1);
    query.setParameter("contentId", contentId);
    List results = query.getResultList();
    return results.size() > 0;
  }
  
  @Override
  public void commit()
  {
    EntityTransaction tx = em.getTransaction();
    if (tx.isActive())
    {
      tx.commit();
    }
  }

  @Override
  public void rollback()
  {
    EntityTransaction tx = em.getTransaction();
    if (tx.isActive())
    {
      tx.rollback();
    }
  }

  @Override
  public void close()
  {
    em.close();
  }

  //*************************** private methods *****************************
  private boolean isReverseRelation(RelationType relType)
  {
    if (relType != null)
    {
      String relName = relType.toString();
      return relName != null && relName.startsWith("REV_");
    }
    else
      return false;
  }

  private RelationType revertRelation(RelationType relType)
  {
    if (isReverseRelation(relType))
    {
      String text = relType.toString();
      return RelationType.valueOf(text.substring(4));
    }
    else
    {
      return RelationType.valueOf("REV_" + relType.toString());
    }
  }

  //DB Methods
  private void persistUserProperty(EntityManager em, 
    String docId, int version, Property property)
  {
     List<DBProperty> dbProperties = toDBProperties(property);
     for (DBProperty dbProperty : dbProperties)
     {
       dbProperty.setDocId(docId);
       dbProperty.setVersion(version);
       em.persist(dbProperty);
     }
   }
   
  private void persistAuthors(EntityManager em, 
    String docId, int version, Property property)
  {
     //TODO: Tabla propia
     removeProperty(em, docId, version, "authorId");
     persistUserProperty(em, docId, version, property);
   }

  private void persistClassIds(EntityManager em, 
    String docId, int version, Property property)
  {
     //TODO: Tabla propia
     removeProperty(em, docId, version, "classId");
     persistUserProperty(em, docId, version, property);
   }

  private void persistCaseIds(EntityManager em, 
    String docId, int version, Property property)
  {
     //TODO: Tabla propia
     removeProperty(em, docId, version, "caseId");
     persistUserProperty(em, docId, version, property);
   }

  private List<Property> getDocumentProperties(EntityManager em,
    String docId, int version)
  {
     List<Property> properties = 
      toProperties(loadDBProperties(em, docId, version));
      
     return properties;
  }

  private List<RelatedDocument> getRelatedDocuments(EntityManager em, 
    String docId, int version)  throws Exception
  {
    List<RelatedDocument> relDocs = new ArrayList();
    List<Object[]> dbRelDocs = 
      findRelatedDocuments(em, docId);
       
    for (Object[] item : dbRelDocs)      
    {
      if (item != null)
      {
        DBRelatedDocument dbRelDoc = (DBRelatedDocument)item[0];
        Integer ver = (Integer)item[1];
        RelatedDocument relDoc = new RelatedDocument();
        dbRelDoc.copyTo(relDoc);
        relDoc.setVersion(ver);
        relDocs.add(relDoc);
      }
    }
    
    List<Object[]> dbReverseRelDocs = 
      findReverseRelatedDocuments(em, docId);
    for (Object[] item : dbReverseRelDocs)      
    {
      DBRelatedDocument dbRevRelDoc = (DBRelatedDocument)item[0];
      Integer ver = (Integer)item[1];
      RelatedDocument relDoc = new RelatedDocument();
      String relType = dbRevRelDoc.getRelationType();
      relType = "REV_" + relType; 
      
      relDoc.setRelationType(RelationType.fromValue(relType));
      relDoc.setDocId(dbRevRelDoc.getDocId());
      relDoc.setVersion(ver);
      relDoc.setName(dbRevRelDoc.getName());
      relDoc.setCaptureDateTime(dbRevRelDoc.getCaptureDateTime());
      relDoc.setCaptureUserId(dbRevRelDoc.getCaptureUserId());
      relDoc.setChangeDateTime(dbRevRelDoc.getChangeDateTime());
      relDoc.setChangeUserId(dbRevRelDoc.getChangeUserId());
      
      if (!containsOpposite(relDocs, relDoc))
        relDocs.add(relDoc);
    }
    
    return relDocs;
  }
  
  private boolean containsOpposite(List<RelatedDocument> list, RelatedDocument relDoc)
  {
    if (relDoc == null)
      return false;
    
    for (RelatedDocument item : list)
    {
      if (item.getName().equals(relDoc.getName()) 
        && DocumentUtils.revertRelation(
          item.getRelationType()).equals(relDoc.getRelationType()))
        return true;
    }
    
    return false;
  }
  
  private int getDocumentLastVersion(EntityManager em, String docId)
     throws Exception
  {
      int lastversion = 0;
      try
      {
        Document document = getLastVersionDocument(em, docId);
        lastversion = document.getVersion();
      }
      catch (NoResultException e)
      {
        lastversion = 0;
      }
      return lastversion;
    }   

  //Private DB Queries
  private void removeProperties(EntityManager em, String docId, int version)
  {
    Query query = 
      em.createNamedQuery("removeDocumentProperties");
    query.setParameter("docId", docId);
    query.setParameter("version", version);
    query.executeUpdate();
  }

  private void removeProperty(EntityManager em, String docId, int version,
    String propName)
  {
    Query query =
      em.createNamedQuery("removeDocumentProperty");
    query.setParameter("docId", docId);
    query.setParameter("version", version);
    query.setParameter("propName", propName);
    query.executeUpdate();
  }

  private void removeRelatedDocuments(EntityManager em, 
    String docId)
  {
    Query query = 
      em.createNamedQuery("removeRelatedDocuments");
    query.setParameter("docId", docId);
    query.executeUpdate();
    em.flush();
  }
  
  private int removeDocument(EntityManager em, 
    String docId, int version)
  {
    Query query = 
      em.createNamedQuery("removeDocument");
    query.setParameter("docId", docId);
    query.setParameter("version", version);
    return query.executeUpdate();
  }  

  private List<DBProperty> loadDBProperties(EntityManager em, 
    String docId, int version)
  {
    Query query = 
    em.createNamedQuery("loadDocumentProperties");
    query.setParameter("docId", docId);
    query.setParameter("version", version);
    
    return query.getResultList();
  }
  
  private List<Object[]> findRelatedDocuments(EntityManager em,
   String docId)
  {
    Query query =
    em.createNamedQuery("findRelatedDocuments");
    query.setParameter("docId", docId);
    return query.getResultList();
  }
  
  private List<Object[]> findReverseRelatedDocuments(EntityManager em,
   String docId)
  {
    List<DBRelatedDocument> dbRelDocs = new ArrayList();
    
    Query query =
    em.createNamedQuery("findReverseRelatedDocuments");
    query.setParameter("docId", docId);
    return query.getResultList();
  }
  
  private Document getLastVersionDocument(EntityManager em, String docId)
    throws Exception
  {
    Query query = em.createNamedQuery("selectLastVersionDocument");
    query.setParameter("docId", docId);
    DBDocument dbDocument = (DBDocument) query.getSingleResult();
    Document document = new Document();
    dbDocument.copyTo(document, endpoint);
    document.setIncremental(true);
    
    return document;
  }
  
  private Number getDocumentMaxVersion(EntityManager em, String docId)
  {
    Query query;
    query = em.createNamedQuery("selectDocumentMaxVersion");
    query.setParameter("docId", docId);
    return (Number)query.getSingleResult();    
  }
  
  private Number getNotDeletedVersionsCount(EntityManager em, String docId, int version)
  {
    Query query;
    query = em.createNamedQuery("countNotDeletedVersions");
    query.setParameter("docId", docId);
    query.setParameter("version", version);
    return (Number)query.getSingleResult();    
  }
  
  private void updateLastVersionColumn(EntityManager em, String docId, int version)
    throws Exception
  {
    Query query = null;

    if (version == 0)
    {
      query = em.createNamedQuery("selectDocumentMaxVersion");
      query.setParameter("docId", docId);
      Number result = getDocumentMaxVersion(em, docId);
      if (result != null)
        version = result.intValue();
    }

    if (version > 0)
    {
      query = em.createNamedQuery("unsetLastVersionColumn");
      query.setParameter("docId", docId);
      query.setParameter("version", version);
      query.executeUpdate();

      query = em.createNamedQuery("setLastVersionColumn");
      query.setParameter("docId", docId);
      query.setParameter("version", version);
      query.executeUpdate();
    }
  }
 
  //Conversion and transformation methods
  private void setSysPropertiesFromPropertiesList(Document document)
  {
    List<Property> removeProperties = new ArrayList();
    for (Property property : document.getProperty())
    {
      String name = property.getName();
      if ("authorId".equals(name))
      {
        document.getAuthorId().clear();
        document.getAuthorId().addAll(property.getValue());
        removeProperties.add(property);
      }
      else if ("classId".equals(name))
      {
        document.getClassId().clear();
        document.getClassId().addAll(property.getValue());
        removeProperties.add(property);
      }
      else if ("caseId".equals(name))
      {
        document.getCaseId().clear();
        document.getCaseId().addAll(property.getValue());
        removeProperties.add(property);
      }
    }
    document.getProperty().removeAll(removeProperties);
  }
  
  private Document copyTo(Document document,
    Object[] docVector, WSEndpoint endpoint)
  {
    document.setDocId(String.valueOf(docVector[0]));
    document.setVersion(((Number)docVector[1]).intValue());
    document.setTitle((String)docVector[2]);
    String docTypeId = (String)docVector[3];
    document.setDocTypeId(
      endpoint.getExternalEntity("Type").toGlobalId(docTypeId));
    document.setState(State.valueOf((String)docVector[4]));
    document.setLanguage((String)docVector[5]);
    document.setCaptureDateTime((String)docVector[6]);
    document.setCaptureUserId((String)docVector[7]);
    document.setChangeDateTime((String)docVector[8]);
    document.setChangeUserId((String)docVector[9]);
    document.setLockUserId((String)docVector[10]);
    document.setCreationDate((String)docVector[13]);

    Content content = new Content();
    content.setContentId((String)docVector[12]);

    document.setContent(content);

    return document;
  }  
  
  private void setOutputProperties(
    Map<DBDocumentPK, Document> documentsMap, List<Object[]> docProps,
    List<String> outputProperties)
  {
    for (Object[] docProp : docProps)
    {
      String docId = (String)docProp[0];
      int version = ((Number)docProp[1]).intValue();
      String propname = (String)docProp[2];
      String value = (String)docProp[3];
      
      if (propname != null)
      {
        if (outputProperties.contains(propname))
        {
          DBDocumentPK key = new DBDocumentPK(docId, version);
          Document document = documentsMap.get(key);
          
          if ("authorId".equals(propname))
            document.getAuthorId().add(value);
          else if ("classId".equals(propname))
            document.getClassId().add(value);
          else if ("caseId".equals(propname))
            document.getCaseId().add(value);
          else
          {
            Property property = DictionaryUtils.getProperty(document, propname);
            if (property == null)
            {
              property = new Property();
              property.setName(propname);
              document.getProperty().add(property);
            } 
            property.getValue().add(value);
          }
        }
      }
    }    
  }  
  
  private List<Property> toProperties(List<DBProperty> dbProperties)
  {
    List<Property> properties = new ArrayList();
    Property property = null;
    for (DBProperty dbProperty : dbProperties)
    {
      if (property == null)
      {
        property = new Property();
        property.setName(dbProperty.getName());
      }
      else if (!property.getName().equals(dbProperty.getName()))
      {
        properties.add(property);              
        property = new Property();
        property.setName(dbProperty.getName());
      }
      property.getValue().add(dbProperty.getValue());
    }
    if (property != null)
      properties.add(property);
    
    return properties;
  }  
  
  private List<DBProperty> toDBProperties(Property property)
  {
    int index = 0;
    List<DBProperty> result = new ArrayList();
    for(String v : property.getValue())
    {
      DBProperty dbProperty = new DBProperty();
      dbProperty.setName(property.getName());
      dbProperty.setIndex(index++);
      dbProperty.setValue(v);
      result.add(dbProperty);
    }
    return result;
  } 
  
  private List<Vector> findSummaries(EntityManager em,
    List<DBDocumentPK> docKeys, String searchExpression, int maxResults)
  {
    List<Vector> summaries = null;
    try
    {
      FindSummariesQueryBuilder queryBuilder =
        FindSummariesQueryBuilder.getInstance();
      queryBuilder.setSearchExpression(searchExpression);
      queryBuilder.setKeys(docKeys);
      Query query = queryBuilder.getQuery(em);
      query.setMaxResults(maxResults);
      summaries = query.getResultList();
    }
    catch (Exception ex)
    {
    }

    return summaries;
  }

  private void setSummaries(List<Vector> summaries, Map<DBDocumentPK, Document>
    documentMap)
  {
    for (Vector summary : summaries)
    {
      String docId = String.valueOf(summary.get(0));
      int version = ((Number)summary.get(1)).intValue();
      DBDocumentPK key = new DBDocumentPK(docId, version);
      String text = (String)summary.get(2);
      char[] chars = text.toCharArray();
      for (int i = 0; i < chars.length; i++)
      {
        if (Character.isISOControl(chars[i])) chars[i] = ' ';
      }
      Document document = (Document)documentMap.get(key);
      document.setSummary(String.valueOf(chars));
    }
  }

  private List<Object[]> findDocumentProperties(EntityManager em,
    List<DBDocumentPK> docKeys) throws Exception
  {
    List<Object[]> dbProperties = null;

    if (docKeys != null && !docKeys.isEmpty())
    {
      StringBuffer buffer = new StringBuffer(
        "SELECT op.docId, op.version, op.name, op.value " +
        "FROM DBProperty op ");

      for (int i = 0; i < docKeys.size(); i++)
      {
        if (i == 0)
          buffer.append(" WHERE ");
        else
          buffer.append(" OR ");
        buffer.append(" (op.docId = :docId" + i +
          " AND op.version = :version" + i +") ");
      }
      buffer.append(" ORDER BY op.docId, op.version, op.name, op.index ");

      Query query = em.createQuery(buffer.toString());
      JPAQuery jpaQuery = new JPAQuery(query);
      for (int i = 0; i < docKeys.size(); i++)
      {
        jpaQuery.setParameter("docId" + i, docKeys.get(i).getDocId());
        jpaQuery.setParameter("version" + i, docKeys.get(i).getVersion());
      }
      dbProperties = jpaQuery.getResultList();
    }

    return dbProperties;
  }

  private void removeAccessControlList(EntityManager em, String docId, int version)
  {
    Query query = em.createNamedQuery("removeDocAccessControl");
    query.setParameter("docId", docId);
    query.setParameter("version", version);
    query.executeUpdate();
  }

  private List<DBAccessControl> loadAccessControlList(EntityManager em,
    String docId, int version)
  {
    Query query = em.createNamedQuery("findDocAccessControl");
    query.setParameter("docId", docId);
    query.setParameter("version", version);
    return query.getResultList();
  }

  private void storeAccessControlList(Document document,
    List<DBAccessControl> dbAccessControlList)
  {
    String docId = document.getDocId();
    int version = document.getVersion();
    ArrayList<AccessControl> ACL = new ArrayList<AccessControl>();
    ACL.addAll(document.getAccessControl());
    removeRepeated(ACL);

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
      dbAccessControl.setDocId(docId);
      dbAccessControl.setVersion(version);
      if (dbAccessControl.getRoleId() != null &&
        dbAccessControl.getAction() != null)
      {
        em.persist(dbAccessControl);
      }
    }
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

  private void removeRepeated(List<AccessControl> list)
  {
    LinkedHashSet<ACL> set = new LinkedHashSet();
    for (AccessControl item : list)
    {
      ACL acl = new ACL(item);
      set.add(acl);
    }
    list.clear();
    list.addAll(set);
  }

  private class ACL extends AccessControl
  {
    public ACL(AccessControl accessControl)
    {
      this.action = accessControl.getAction();
      this.roleId = accessControl.getRoleId();
    }
    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }

      final ACL other = (ACL) obj;
      if ((this.action == null) ? (other.action != null) : !this.action.equals(other.action))
      {
        return false;
      }
      if ((this.roleId == null) ? (other.roleId != null) : !this.roleId.equals(other.roleId))
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      return hash;
    }
  }

}
