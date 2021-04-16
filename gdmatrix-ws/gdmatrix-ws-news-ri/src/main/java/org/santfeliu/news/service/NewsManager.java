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
package org.santfeliu.news.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.matrix.doc.DocumentFilter;
import org.matrix.news.New;
import org.matrix.news.NewDocument;
import org.matrix.news.NewSection;
import org.matrix.news.NewStoreOptions;
import org.matrix.news.NewView;
import org.matrix.news.NewsFilter;
import org.matrix.news.NewsManagerPort;
import org.matrix.news.NewsManagerMetaData;
import org.matrix.news.SectionFilter;
import org.matrix.news.SectionView;
import org.matrix.news.Source;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.ws.WSUtils;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.MultiInstance;

/**
 *
 * @author lopezrj
 */
@WebService(endpointInterface = "org.matrix.news.NewsManagerPort")
@HandlerChain(file="handlers.xml")
@MultiInstance
public class NewsManager implements NewsManagerPort
{
  private static final Logger LOGGER = Logger.getLogger("News");  

  private static final int NEW_HEADLINE_MAX_SIZE = 1000;
  private static final int NEW_SUMMARY_MAX_SIZE = 4000;
  private static final int NEW_KEYWORDS_MAX_SIZE = 1000;
  private static final int NEW_CUSTOM_URL_MAX_SIZE = 1000;
  private static final int NEW_SECTION_PRIORITY_MAX_SIZE = 6;

  static final String PK_SEPARATOR = ";";

  @Resource
  WebServiceContext wsContext;

  @PersistenceContext(unitName="news_ri")
  public EntityManager entityManager;
  
  @Initializer
  public void initialize(String endpointName)
  {
  }

  @Override
  public NewsManagerMetaData getManagerMetaData()
  {
    NewsManagerMetaData metaData = new NewsManagerMetaData();
    return metaData;
  }

  @Override
  public New storeNew(New newObject, NewStoreOptions storeOptions)
  {
    LOGGER.log(Level.INFO, "storeNew {0}", newObject.getNewId());
    validateNew(newObject);
    
    if (storeOptions.isCleanSummary())
    {
      newObject.setSummary(HTMLNormalizer.cleanHTML(newObject.getSummary(), false));
    }
    if (storeOptions.isCleanText())
    {
      newObject.setText(HTMLNormalizer.cleanHTML(newObject.getText(), false));
    }
    newObject.setHeadline(HTMLNormalizer.replaceSpecialChars(newObject.getHeadline()));
    newObject.setSummary(HTMLNormalizer.replaceSpecialChars(newObject.getSummary()));
    if (newObject.getKeywords() != null)
    {
      newObject.setKeywords(HTMLNormalizer.replaceSpecialChars(newObject.getKeywords()));
    }
    newObject.setText(HTMLNormalizer.replaceSpecialChars(newObject.getText()));
    DBNew dbNew = new DBNew(newObject);
    if (newObject.getNewId() == null) //Insert
    {
      SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat hourFormat = new SimpleDateFormat("HHmmss");
    
      Date now = new Date();
      dbNew.setRegisterDate(dayFormat.format(now));
      dbNew.setRegisterTime(hourFormat.format(now));
      entityManager.persist(dbNew);
      newObject.setNewId(dbNew.getNewId());
      newObject.setRegisterDate(dbNew.getRegisterDate());
      newObject.setRegisterTime(dbNew.getRegisterTime());
    }
    else //Update
    {
      newObject.setTotalReadingCount(getTotalNewCounter(newObject.getNewId()));
      dbNew.setTotalReadingCount(newObject.getTotalReadingCount());
      entityManager.merge(dbNew);
    }
    return newObject;
  }

  @Override
  public New loadNew(String newId)
  {
    LOGGER.log(Level.INFO, "loadNew {0}", newId);        
    New newObject = new New();
    DBNew dbNew = entityManager.find(DBNew.class, newId);
    if (dbNew == null) throw new WebServiceException("news:NEW_NOT_FOUND");
    dbNew.copyTo(newObject);
    return newObject;
  }

  @Override
  public int incrementNewCounter(String newId, String sectionId)
  {
    LOGGER.log(Level.INFO, "incrementNewCounter newId {0} sectionId {1}",
      new String[]{newId, sectionId});
    int totalCounter = increaseTotalReadCount(newId);
    if (sectionId != null)
    {
      increaseSectionReadCount(newId, sectionId);
    }
    return totalCounter;
  }

  @Override
  public int getTotalNewCounter(String newId)
  {
    LOGGER.log(Level.INFO, "getTotalNewCounter {0}", newId);
    DBNew dbNew = entityManager.find(DBNew.class, newId);
    return dbNew.getTotalReadingCount();
  }

  @Override
  public boolean removeNew(String newId)
  {
    try
    {
      LOGGER.log(Level.INFO, "removeNew {0}", newId);
      DBNew dbNew = entityManager.getReference(DBNew.class, newId);
      // Sections
      Query query = entityManager.createNamedQuery("removeNewSections");
      query.setParameter("newId", newId);
      query.executeUpdate();
      // Documents
      query = entityManager.createNamedQuery("removeNewDocuments");
      query.setParameter("newId", newId);
      query.executeUpdate();
      // New object
      entityManager.remove(dbNew);
      return true;
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
  }

  @Override
  public int countNews(NewsFilter filter)
  {
    LOGGER.log(Level.INFO, "countNews");
    Query query = entityManager.createNamedQuery("countNews");
    setNewsFilterParameters(query, filter);
    return ((Number)query.getSingleResult()).intValue();
  }

  @Override
  public List<New> findNews(NewsFilter filter)
  {
    LOGGER.log(Level.INFO, "findNews");
    List<New> result = new ArrayList<New>();
    Query query = entityManager.createNamedQuery("listNews");
    setNewsFilterParameters(query, filter);
    query.setFirstResult(filter.getFirstResult());
    int maxRows = filter.getMaxResults();
    query.setMaxResults(maxRows == 0 ? 10 : maxRows);      
    List<Object[]> newArrayList = query.getResultList();
    if (newArrayList != null)
    {
      for (Object[] newArray : newArrayList)
      {
        New n = new New();
        n.setNewId(String.valueOf(newArray[0]));
        n.setHeadline((String)newArray[1]);
        n.setSummary((String)newArray[2]);
        n.setRegisterDate((String)newArray[3]);
        n.setRegisterTime((String)newArray[4]);
        n.setStartDate((String)newArray[5]);
        n.setStartTime((String)newArray[6]);
        n.setEndDate((String)newArray[7]);
        n.setEndTime((String)newArray[8]);
        if (newArray[9] != null)
        {
          boolean draft = "Y".equalsIgnoreCase((String)newArray[9]);
          n.setDraft(draft);
        }
        if (newArray[10] != null)
        {
          n.setSource((String)newArray[10]);
        }
        if (newArray[11] != null)
        {
          n.setUserId((String)newArray[11]);
        }        
        if (newArray[12] != null)
        {
          n.setKeywords((String)newArray[12]);
        }
        if (newArray[13] != null)
        {
          n.setCustomUrl((String)newArray[13]);
        }
        if (newArray[14] != null)
        {
          n.setCustomUrlTarget((String)newArray[14]);
        }
        result.add(n);
      }
    }
    return result;
  }

  @Override
  public List<NewView> findNewViews(NewsFilter filter)
  {
    LOGGER.log(Level.INFO, "findNewViews");
    List<NewView> result = new ArrayList<NewView>();
    Query query = entityManager.createNamedQuery("listNews");
    setNewsFilterParameters(query, filter);
    query.setFirstResult(filter.getFirstResult());
    int maxRows = filter.getMaxResults();
    query.setMaxResults(maxRows == 0 ? 10 : maxRows);      
    List<Object[]> newArrayList = query.getResultList();
    if (newArrayList != null)
    {
      for (Object[] newArray : newArrayList)
      {
        NewView nv = new NewView();
        nv.setNewId(String.valueOf(newArray[0]));
        nv.setHeadline((String)newArray[1]);
        nv.setSummary((String)newArray[2]);
        nv.setRegisterDate((String)newArray[3]);
        nv.setRegisterTime((String)newArray[4]);
        nv.setStartDate((String)newArray[5]);
        nv.setStartTime((String)newArray[6]);
        nv.setEndDate((String)newArray[7]);
        nv.setEndTime((String)newArray[8]);
        if (newArray[9] != null)
        {
          boolean draft = "Y".equalsIgnoreCase((String)newArray[9]);
          nv.setDraft(draft);
        }
        if (newArray[12] != null)
        {
          nv.setKeywords((String)newArray[12]);
        }
        if (newArray[13] != null)
        {
          nv.setCustomUrl((String)newArray[13]);
        }
        if (newArray[14] != null)
        {
          nv.setCustomUrlTarget((String)newArray[14]);
        }
        result.add(nv);
      }
    }
    return result;
  }

  @Override
  public NewSection storeNewSection(NewSection newSection)
  {
    LOGGER.log(Level.INFO, "storeNewSection newId {0},sectionId {1}",
      new String[]{newSection.getNewId(), newSection.getSectionId()});
    validateNewSection(newSection);
    DBNewSection dbNewSection = new DBNewSection(newSection);    
    if (newSection.getNewSectionId() == null) //Insert
    {
      if (newSection.getPriority() == null)
      {
        int priority = getMaxPriorityInSection(dbNewSection.getSectionId()) + 1;
        dbNewSection.setPriority(priority);
      }
      dbNewSection.setNewSectionId(newSection.getNewId() + 
        PK_SEPARATOR + newSection.getSectionId());
      entityManager.persist(dbNewSection);
      newSection.setNewSectionId(dbNewSection.getNewSectionId());
      newSection.setPriority(dbNewSection.getPriority());
    }
    else //Update
    {
      entityManager.merge(dbNewSection);
    }
    return newSection;
  }    

  @Override
  public boolean removeNewSection(String newSectionId)
  {
    try
    {
      LOGGER.log(Level.INFO, "removeNewSection {0}", newSectionId);
      DBNewSectionPK pk = new DBNewSectionPK(newSectionId);
      DBNewSection dbNewSection = 
        entityManager.getReference(DBNewSection.class, pk);
      entityManager.remove(dbNewSection);      
      return true;
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
  }

  @Override
  public List<NewSection> findNewSections(String newId)
  {
    LOGGER.log(Level.INFO, "findNewSections newId:{0}", newId);
    List<NewSection> newSectionList = new ArrayList<NewSection>();
    Query query = entityManager.createNamedQuery("listNewSections");
    query.setParameter("newId", newId);
    List<DBNewSection> dbNewSectionList = query.getResultList();
    for (DBNewSection dbNewSection : dbNewSectionList)
    {
      NewSection ns = new NewSection();
      dbNewSection.copyTo(ns);
      newSectionList.add(ns);
    }
    return newSectionList;
  }

  @Override
  public NewDocument storeNewDocument(NewDocument newDocument)
  {
    LOGGER.log(Level.INFO, "storeNewDocument newId {0},documentId {1}",
      new String[]{newDocument.getNewId(), newDocument.getDocumentId()});
    validateNewDocument(newDocument);
    WSEndpoint endpoint = getWSEndpoint();
    DBNewDocument dbNewDocument = new DBNewDocument(newDocument, endpoint);
    if (newDocument.getNewDocumentId() == null) 
    {
      dbNewDocument.setNewDocumentId(newDocument.getNewId() + 
        PK_SEPARATOR + newDocument.getDocumentId());
      entityManager.persist(dbNewDocument);
      newDocument.setNewDocumentId(dbNewDocument.getNewDocumentId());
    }
    else
    {
      entityManager.merge(dbNewDocument);
    }
    return newDocument;              
  }

  @Override
  public boolean removeNewDocument(String newDocumentId)
  {
    try
    {
      LOGGER.log(Level.INFO, "removeNewDocument {0}", newDocumentId);
      DBNewDocumentPK pk = new DBNewDocumentPK(newDocumentId);
      DBNewDocument dbNewDocument =
        entityManager.getReference(DBNewDocument.class, pk);
      entityManager.remove(dbNewDocument);
      return true;
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
  }

  @Override
  public List<NewDocument> findNewDocuments(String newId, String docType)
  {
    LOGGER.log(Level.INFO, "findNewDocuments newId:{0} docType:{1}",
      new String[]{newId, docType});
    List<NewDocument> newDocumentList = new ArrayList<NewDocument>();
    newDocumentList = doListNewDocuments(newId, docType);
    return newDocumentList;
  }
  
  @Override
  public NewSection loadNewSection(String newSectionId)
  {
    LOGGER.log(Level.INFO, "loadNewSection {0}", newSectionId);        
    NewSection newSection = new NewSection();
    DBNewSection dbNewSection = entityManager.find(DBNewSection.class, 
      new DBNewSectionPK(newSectionId));
    if (dbNewSection == null) 
      throw new WebServiceException("news:SECTION_NOT_FOUND");
    dbNewSection.copyTo(newSection);
    return newSection;    
  }

  @Override
  public List<Source> findSources()
  {
    LOGGER.log(Level.INFO, "findSources");
    Query query = entityManager.createNamedQuery("listSources");
    List<DBSource> list = query.getResultList();
    List<Source> resultList = new ArrayList<Source>();
    for (DBSource dbS : list) 
    {
      Source s = new Source();
      dbS.copyTo(s);
      resultList.add(s);
    }
    return resultList;
  }

  @Override
  public int countNewsBySection(SectionFilter filter)
  {
    LOGGER.log(Level.INFO, "countNewsBySection");
    int result = 0;
    for (int i = 0; i < filter.getSectionId().size(); i++)
    {
      String sectionId = filter.getSectionId().get(i);
      boolean excludeDrafts = false;
      if (filter.getExcludeDrafts().size() > i)
        excludeDrafts = filter.getExcludeDrafts().get(i);
      result += countNewsInSection(filter, sectionId, excludeDrafts);
    }
    return result;
  }  

  @Override
  public List<SectionView> findNewsBySection(SectionFilter filter)
  {
    LOGGER.log(Level.INFO, "findNewsBySection");
    List<SectionView> result = new ArrayList<SectionView>();
    for (int i = 0; i < filter.getSectionId().size(); i++)
    {
      String sectionId = filter.getSectionId().get(i);
      boolean excludeDrafts = false;
      if (filter.getExcludeDrafts().size() > i)
        excludeDrafts = filter.getExcludeDrafts().get(i);
      SectionView sv = findNewsInSection(filter, sectionId, excludeDrafts);
      result.add(sv);
    }    
    if (result.size() > 0) fillDocumentFields(result);
    return result;
  }
  
  /**** private methods ****/
  
  private int increaseSectionReadCount(String newId, String sectionId)
  {
    DBNewSectionPK pk = new DBNewSectionPK();
    pk.setNewId(newId);
    pk.setSectionId(sectionId);
    DBNewSection dbNewSection = entityManager.find(DBNewSection.class, pk);
    int newCounterValue = 0;
    if (dbNewSection != null)
    {
      newCounterValue = dbNewSection.getReadingCount() + 1;
      dbNewSection.setReadingCount(newCounterValue);
      entityManager.merge(dbNewSection);
    }
    return newCounterValue;
  }

  private int increaseTotalReadCount(String newId)
  {
    DBNew dbNew = entityManager.find(DBNew.class, newId);
    int newCounterValue = dbNew.getTotalReadingCount() + 1;
    dbNew.setTotalReadingCount(newCounterValue);
    entityManager.merge(dbNew);
    return newCounterValue;
  }

  private void setNewsFilterParameters(Query query, NewsFilter filter)
  {
    String newIds = null;
    if (!filter.getNewId().isEmpty())
    {
      newIds = " ";
      for (String newId : filter.getNewId())
      {
        newIds += (newId + " ");
      }
    }
    query.setParameter("newIds", newIds);
    query.setParameter("content", "%" + (filter.getContent() == null ? "" : 
      filter.getContent().toLowerCase()) + "%");    
    query.setParameter("minDate", filter.getStartDateTime());
    query.setParameter("maxDate", filter.getEndDateTime());
    String sections = " ";
    for (String section : filter.getSectionId())
    {
      sections += (section + " ");
    }
    query.setParameter("sections", sections);
    query.setParameter("excludeDrafts", 
      (filter.isExcludeDrafts() ? "Y" : "N"));
    query.setParameter("excludeNotPublished", 
      (filter.isExcludeNotPublished() ? "Y" : "N"));
    query.setParameter("minPubDate", filter.getMinPubDateTime());
    query.setParameter("userId", filter.getUserId());
  }

  private void setSectionFilterParameters(Query query, SectionFilter filter, 
    String sectionId, boolean excludeDrafts)
  {
    String newIds = null;
    if (!filter.getNewId().isEmpty())
    {
      newIds = " ";
      for (String newId : filter.getNewId())
      {
        newIds += (newId + " ");
      }
    }
    query.setParameter("newIds", newIds);
    query.setParameter("content", "%" + (filter.getContent() == null ? "" : 
      filter.getContent().toLowerCase()) + "%");

    query.setParameter("minDate", filter.getStartDateTime());
    query.setParameter("maxDate", filter.getEndDateTime());
    query.setParameter("sectionId", sectionId);
    query.setParameter("minPubDate", filter.getMinPubDateTime());
    query.setParameter("excludeDrafts", excludeDrafts ? "Y" : "N");    
    query.setParameter("userId", filter.getUserId());
  }

  private List<NewDocument> doListNewDocuments(String newId, String docType)
  {
    List<NewDocument> result = new ArrayList<NewDocument>();
    Set<String> nonFilteredDocIdSet = new HashSet<String>();
    Map<String, DBNewDocument> documentMap = 
      new HashMap<String, DBNewDocument>();
    Query query = entityManager.createNamedQuery("listNewDocuments");
    query.setParameter("newId", newId);
    query.setParameter("docType", docType);
    List<DBNewDocument> dbNewDocumentList = query.getResultList();
    for (DBNewDocument dbNewDocument : dbNewDocumentList)
    {
      documentMap.put(dbNewDocument.getDocumentId(), dbNewDocument);
      dbNewDocument.setNewDocumentId(dbNewDocument.getNewId() + 
        PK_SEPARATOR + dbNewDocument.getDocumentId());
      nonFilteredDocIdSet.add(dbNewDocument.getDocumentId());
    }
    if (nonFilteredDocIdSet.size() > 0)
    {
      WSEndpoint endpoint = getWSEndpoint(); 
      DocumentFilter filter = new DocumentFilter();
      filter.getDocId().addAll(nonFilteredDocIdSet);
      filter.setIncludeContentMetadata(true);
      List<org.matrix.doc.Document> docList = 
        getDocumentManagerClient().findDocuments(filter);
      for (org.matrix.doc.Document doc : docList)
      {
        String docId = doc.getDocId();
        DBNewDocument dbNewDocument = documentMap.get(docId);
        if (dbNewDocument != null)
        {
          dbNewDocument.setTitle(doc.getTitle());
          if (doc.getContent() != null)
          {
            dbNewDocument.setContentId(doc.getContent().getContentId());
            dbNewDocument.setMimeType(doc.getContent().getContentType());
          }
          NewDocument newDocument = new NewDocument();
          dbNewDocument.copyTo(newDocument, endpoint);
          result.add(newDocument);
        }
      }
    }
    return result;
  }
    
  private int countNewsInSection(SectionFilter filter, String sectionId, 
    boolean excludeDrafts)
  {
    Query query = entityManager.createNamedQuery("countNewsInSection");
    setSectionFilterParameters(query, filter, sectionId, excludeDrafts);
    return ((Number)query.getSingleResult()).intValue();
  }

  private SectionView findNewsInSection(SectionFilter filter, String sectionId, 
    boolean excludeDrafts)
  {
    String newIdString = " ";
    Map<String, NewView> newViewMap = new HashMap<String, NewView>();
    SectionView result = new SectionView();
    result.setSectionId(sectionId);
    Query query = entityManager.createNamedQuery("findNewsInSection");
    setSectionFilterParameters(query, filter, sectionId, excludeDrafts);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<Object[]> newArrayList = query.getResultList();
    if (newArrayList != null)
    {
      for (Object[] newArray : newArrayList)
      {
        NewView nv = new NewView();
        nv.setNewId(String.valueOf(newArray[0]));
        nv.setHeadline((String)newArray[1]);
        nv.setSummary((String)newArray[2]);
        nv.setRegisterDate((String)newArray[3]);
        nv.setRegisterTime((String)newArray[4]);
        nv.setStartDate((String)newArray[5]);
        nv.setStartTime((String)newArray[6]);
        nv.setEndDate((String)newArray[7]);
        nv.setEndTime((String)newArray[8]);
        if (newArray[9] != null)
        {
          boolean draft = "Y".equalsIgnoreCase((String)newArray[9]);
          nv.setDraft(draft);
        }
        if (newArray[11] != null)
        {
          nv.setPriority(Integer.valueOf(String.valueOf(newArray[11])));            
        }
        if (newArray[12] != null)
        {
          nv.setReadingCount(Integer.valueOf(String.valueOf(newArray[12])));            
        }
        if (newArray[13] != null)
        {
          nv.setKeywords((String)newArray[13]);
        }        
        if (newArray[14] != null)
        {
          nv.setSticky("Y".equalsIgnoreCase((String)newArray[14]));
        }
        if (newArray[15] != null)
        {
          nv.setCustomUrl((String)newArray[15]);
        }
        if (newArray[16] != null)
        {
          nv.setCustomUrlTarget((String)newArray[16]);
        }
        result.getNewView().add(nv);
        newIdString += nv.getNewId() + " ";
        newViewMap.put(nv.getNewId(), nv);
      }
    }
    WSEndpoint endpoint = getWSEndpoint();
    query = entityManager.createNamedQuery("listNewsDocuments");
    query.setParameter("newId", newIdString);
    query.setParameter("docType", null);
    List<Object[]> newDocArrayList = query.getResultList();
    for (Object[] newDocArray : newDocArrayList)
    {
      String newId = (String)newDocArray[0];
      NewView nv = newViewMap.get(newId);
      String docId = (String)newDocArray[1];
      String docType = (String)newDocArray[2];
      DBNewDocument dbNewDocument = new DBNewDocument();
      dbNewDocument.setNewId(newId);
      dbNewDocument.setDocumentId(docId);
      dbNewDocument.setNewDocTypeId(docType);
      NewDocument nd = new NewDocument();
      dbNewDocument.copyTo(nd, endpoint);
      nv.getNewDocument().add(nd);
    }
    return result;    
  }

  private void fillDocumentFields(List<SectionView> sectionViewList)
  {
    Set<String> nonFilteredDocIdSet = new HashSet<String>();
    for (SectionView sv : sectionViewList)
    {
      for (NewView nv : sv.getNewView())
      {
        for (NewDocument nd : nv.getNewDocument())
        {
          if (!nonFilteredDocIdSet.contains(nd.getDocumentId()))
          {
            nonFilteredDocIdSet.add(nd.getDocumentId());
          }
        }
      }
    }
    if (nonFilteredDocIdSet.size() > 0)
    {
      Map<String, List<NewDocument>> documentMap =
        new HashMap<String, List<NewDocument>>();
      Set<String> filteredDocIdSet = new HashSet<String>();
      DocumentFilter filter = new DocumentFilter();
      filter.getDocId().addAll(nonFilteredDocIdSet);
      filter.setIncludeContentMetadata(true);
      List<org.matrix.doc.Document> docList =
        getDocumentManagerClient().findDocuments(filter);
      for (org.matrix.doc.Document doc : docList)
      {
        filteredDocIdSet.add(doc.getDocId());
      }
      for (SectionView sv : sectionViewList)
      {
        for (NewView nv : sv.getNewView())
        {
          List<NewDocument> filteredDocumentList = new ArrayList<NewDocument>();
          for (NewDocument doc : nv.getNewDocument())
          {
            if (filteredDocIdSet.contains(doc.getDocumentId()))
            {
              filteredDocumentList.add(doc);
              if (!documentMap.containsKey(doc.getDocumentId()))
              {
                documentMap.put(doc.getDocumentId(), new ArrayList<NewDocument>());
              }
              documentMap.get(doc.getDocumentId()).add(doc);
            }
          }
          nv.getNewDocument().clear();
          nv.getNewDocument().addAll(filteredDocumentList);
        }
      }
      for (org.matrix.doc.Document doc : docList)
      {
        String docId = doc.getDocId();
        String title = doc.getTitle();
        String mimeType = null;
        String contentId = null;
        if (doc.getContent() != null)
        {
          mimeType = doc.getContent().getContentType();
          contentId = doc.getContent().getContentId();
        }
        List<NewDocument> newDocList = documentMap.get(docId);
        for (NewDocument newDoc : newDocList)
        {
          newDoc.setMimeType(mimeType);
          newDoc.setTitle(title);
          newDoc.setContentId(contentId);
        }
      }
    }
  }

  private void validateNew(New newObject)
  {    
    if (newObject.getHeadline() == null ||
      newObject.getHeadline().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }
    else if (newObject.getHeadline().length() > NEW_HEADLINE_MAX_SIZE)
    {
      throw new WebServiceException("VALUE_TOO_LARGE");
    }
    if (newObject.getSummary() != null &&
      newObject.getSummary().length() > NEW_SUMMARY_MAX_SIZE)
    {
      throw new WebServiceException("VALUE_TOO_LARGE");
    }
    if (newObject.getKeywords() != null &&
      newObject.getKeywords().length() > NEW_KEYWORDS_MAX_SIZE)
    {
      throw new WebServiceException("VALUE_TOO_LARGE");
    }
    if (newObject.getCustomUrl() != null &&
      newObject.getCustomUrl().length() > NEW_CUSTOM_URL_MAX_SIZE)
    {
      throw new WebServiceException("VALUE_TOO_LARGE");
    }
  }

  private void validateNewSection(NewSection newSection)
  {
    if (newSection.getPriority() != null)
    {
      int absPriority = Math.abs(newSection.getPriority());
      if (String.valueOf(absPriority).length() > NEW_SECTION_PRIORITY_MAX_SIZE)
      {
        throw new WebServiceException("VALUE_TOO_LARGE");
      }
    }
  }

  private void validateNewDocument(NewDocument newDocument)
  {
    if (newDocument.getNewDocTypeId() == null ||
      newDocument.getNewDocTypeId().trim().length() == 0)
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }
  }

  private int getMaxPriorityInSection(String sectionId)
  {
    Query query = entityManager.createNamedQuery("getMaxPriorityInSection");
    query.setParameter("sectionId", sectionId);
    Object value = query.getSingleResult();
    int priority = 0;
    if (value != null) priority = ((Number)value).intValue();
    return priority;
  }

  private DocumentManagerClient getDocumentManagerClient()
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    DocumentManagerClient client = 
      new DocumentManagerClient(credentials.getUserId(),
      credentials.getPassword());
    return client;
  }
  
  private WSEndpoint getWSEndpoint()
  {
    String endpointName = WSUtils.getServletAdapter(wsContext).getName();
    return WSDirectory.getInstance().getEndpoint(endpointName);
  }

}
