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
package org.santfeliu.news.client;

import java.net.URL;
import java.util.List;
import org.matrix.news.New;
import org.matrix.news.NewDocument;
import org.matrix.news.NewSection;
import org.matrix.news.NewStoreOptions;
import org.matrix.news.NewView;
import org.matrix.news.NewsFilter;
import org.matrix.news.NewsManagerMetaData;
import org.matrix.news.NewsManagerPort;
import org.matrix.news.NewsManagerService;
import org.matrix.news.SectionFilter;
import org.matrix.news.SectionView;
import org.matrix.news.Source;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ws.WSCallCache;

/**
 *
 * @author lopezrj
 */
public class NewsManagerClient implements NewsManagerPort
{
  NewsManagerPort port;
  private static WSCallCache cache;

  public NewsManagerClient()
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    init(wsDirectory, null, null);
  }

  public NewsManagerClient(URL wsDirectoryURL)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
    init(wsDirectory, null, null);
  }

  public NewsManagerClient(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    init(wsDirectory, userId, password);
  }

  public NewsManagerClient(URL wsDirectoryURL, String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
    init(wsDirectory, userId, password);
  }

  public NewsManagerClient(NewsManagerPort port)
  {
    this.port = port;
  }

  public static WSCallCache getCache()
  {
    if (cache == null)
    {
      cache = new WSCallCache("news");
    }
    return cache;
  }  

  public NewsManagerPort getPort()
  {
    return port;
  }

  @Override
  public NewsManagerMetaData getManagerMetaData()
  {
    return getPort().getManagerMetaData();
  }
    
  //NON-CACHED METHODS

  @Override
  public New storeNew(New _new, NewStoreOptions storeOptions)
  {    
    New n = port.storeNew(_new, storeOptions); 
    getCache().clear();
    return n;
  }
  
  @Override
  public New loadNew(String newId)
  {
    return port.loadNew(newId);
  }

  @Override
  public int incrementNewCounter(String newId, String sectionId)
  {    
    return port.incrementNewCounter(newId, sectionId);    
  }

  @Override
  public int getTotalNewCounter(String newId)
  {
    return port.getTotalNewCounter(newId);        
  }
  
  @Override
  public boolean removeNew(String newId)
  {    
    boolean r = port.removeNew(newId);
    getCache().clear();
    return r;
  }
  
  @Override
  public int countNewsBySection(SectionFilter filter)
  {        
    return port.countNewsBySection(filter);
  }

  @Override
  public List<SectionView> findNewsBySection(SectionFilter filter)
  {    
    return port.findNewsBySection(filter);
  }

  @Override
  public int countNews(NewsFilter filter)
  {    
    return port.countNews(filter);
  }

  @Override
  public List<New> findNews(NewsFilter filter)
  {    
    return port.findNews(filter);
  }

  @Override
  public List<NewView> findNewViews(NewsFilter filter)
  {
    return port.findNewViews(filter);
  }

  @Override
  public NewSection loadNewSection(String newSectionId)
  {
    return port.loadNewSection(newSectionId);
  }

  @Override
  public NewSection storeNewSection(NewSection newSection)
  {    
    NewSection ns = port.storeNewSection(newSection);
    getCache().clear();
    return ns;
  }

  @Override
  public boolean removeNewSection(String newSectionId)
  {    
    boolean r = port.removeNewSection(newSectionId);    
    getCache().clear();
    return r;
  }
  
  @Override
  public List<NewSection> findNewSections(String newId)
  {       
    return port.findNewSections(newId);    
  }

  @Override
  public NewDocument storeNewDocument(NewDocument newDocument)
  {    
    NewDocument nd = port.storeNewDocument(newDocument);
    getCache().clear();
    return nd;
  }

  @Override
  public boolean removeNewDocument(String newDocumentId)
  {    
    boolean r = port.removeNewDocument(newDocumentId);
    getCache().clear();
    return r;
  }
  
  @Override
  public List<NewDocument> findNewDocuments(String newId, String docType)
  {
    return port.findNewDocuments(newId, docType);
  }

  @Override
  public List<Source> findSources()
  {
    return port.findSources();    
  }

  //CACHED METHODS

  public New loadNewFromCache(String newId)
  {    
    return (New)getCache().getCallResult(port, "loadNew", new Object[]{newId});
  }

  public int getTotalNewCounterFromCache(String newId)
  {
    return (Integer)getCache().getCallResult(port, "getTotalNewCounter", 
      new Object[]{newId});
  }

  public int countNewsBySectionFromCache(SectionFilter filter)
  {        
    return (Integer)getCache().getCallResult(port, "countNewsBySection", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});
  }

  public List<SectionView> findNewsBySectionFromCache(SectionFilter filter)
  {    
    return (List<SectionView>)getCache().getCallResult(port, 
      "findNewsBySection", new Object[]{filter}, 
      new Object[]{getModifiedFilter(filter)});
  }

  public int countNewsFromCache(NewsFilter filter)
  {    
    return (Integer)getCache().getCallResult(port, "countNews", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});
  }

  public List<New> findNewsFromCache(NewsFilter filter)
  {    
    return (List<New>)getCache().getCallResult(port, "findNews", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});
  }

  public List<NewView> findNewViewsFromCache(NewsFilter filter)
  {
    return (List<NewView>)getCache().getCallResult(port, "findNewViews", 
      new Object[]{filter}, new Object[]{getModifiedFilter(filter)});
  }

  public NewSection loadNewSectionFromCache(String newSectionId)
  {
    return (NewSection)getCache().getCallResult(port, "loadNewSection", 
      new Object[]{newSectionId});
  }

  public List<NewSection> findNewSectionsFromCache(String newId)
  {       
    return (List<NewSection>)getCache().getCallResult(port, "findNewSections", 
      new Object[]{newId});    
  }

  public List<NewDocument> findNewDocumentsFromCache(String newId, 
    String docType)
  {
    return (List<NewDocument>)getCache().getCallResult(port, "findNewDocuments", 
      new Object[]{newId, docType});
  }

  public List<Source> findSourcesFromCache()
  {
    return (List<Source>)getCache().getCallResult(port, "findSources", 
      new Object[]{});
  }

  private void init(WSDirectory wsDirectory, String userId, String password)
  {
    WSEndpoint endpoint = wsDirectory.getEndpoint(NewsManagerService.class);    
    port = endpoint.getPort(NewsManagerPort.class, userId, password);
  }  
  
  private NewsFilter getModifiedFilter(NewsFilter filter)
  {
    NewsFilter auxFilter = new NewsFilter();
    auxFilter.setContent(filter.getContent());
    auxFilter.setEndDateTime(filter.getEndDateTime() == null ? null : 
      filter.getEndDateTime().substring(0, 12));
    auxFilter.setFirstResult(filter.getFirstResult());
    auxFilter.setMaxResults(filter.getMaxResults());
    auxFilter.setMinPubDateTime(filter.getMinPubDateTime() == null ? null : 
      filter.getMinPubDateTime().substring(0, 12));
    auxFilter.getSectionId().addAll(filter.getSectionId());
    auxFilter.setStartDateTime(filter.getStartDateTime() == null ? null : 
      filter.getStartDateTime().substring(0, 12));
    auxFilter.setExcludeDrafts(filter.isExcludeDrafts());
    auxFilter.setExcludeNotPublished(filter.isExcludeNotPublished());
    auxFilter.setUserId(filter.getUserId());    
    return auxFilter;
  }
  
  private SectionFilter getModifiedFilter(SectionFilter filter)
  {
    SectionFilter auxFilter = new SectionFilter();
    auxFilter.setContent(filter.getContent());
    auxFilter.setStartDateTime(filter.getStartDateTime() == null ? null : 
      filter.getStartDateTime().substring(0, 12));
    auxFilter.setEndDateTime(filter.getEndDateTime() == null ? null : 
      filter.getEndDateTime().substring(0, 12));
    auxFilter.setMinPubDateTime(filter.getMinPubDateTime() == null ? null : 
      filter.getMinPubDateTime().substring(0, 12));
    auxFilter.setFirstResult(filter.getFirstResult());
    auxFilter.setMaxResults(filter.getMaxResults());
    auxFilter.getSectionId().addAll(filter.getSectionId());
    auxFilter.getExcludeDrafts().addAll(filter.getExcludeDrafts());
    auxFilter.setUserId(filter.getUserId());
    return auxFilter;    
  }
  
}
