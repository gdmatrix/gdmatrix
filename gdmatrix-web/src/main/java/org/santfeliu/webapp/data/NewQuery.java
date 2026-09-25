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
package org.santfeliu.webapp.data;

import java.util.List;
import org.matrix.news.NewView;
import org.matrix.news.NewsFilter;
import org.matrix.news.NewsManagerPort;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.webapp.modules.news.NewsModuleBean;

/**
 *
 * @author blanquepa
 */
public class NewQuery extends DataQuery<NewView>
{
  NewsFilter filter;

  public NewQuery(Credentials credentials)
  {
    super(credentials);
    filter = new NewsFilter();
  }
  
  public NewQuery newId(String newId)
  {
    filter.getNewId().add(newId);
    return this;
  }  

    public NewQuery sectionId(String sectionId)
  {
    filter.getSectionId().add(sectionId);
    return this;
  } 

  public NewQuery content(String content)
  {
    filter.setContent(content);
    return this;
  }

  public NewQuery startDateTime(String startDateTime)
  {
    filter.setStartDateTime(startDateTime);
    return this;
  }

  public NewQuery endDateTime(String endDateTime)
  {
    filter.setEndDateTime(endDateTime);
    return this;
  }
  
  public NewQuery minPubDateTime(String minPubDateTime)
  {
    filter.setMinPubDateTime(minPubDateTime);
    return this;
  }  

  public NewQuery excludeDrafts(Boolean excludeDrafts)
  {
    filter.setExcludeDrafts(excludeDrafts);
    return this;
  }  
  
  public NewQuery excludeNotPublished(Boolean excludeNotPublished)
  {
    filter.setExcludeNotPublished(excludeNotPublished);
    return this;
  }    

  @Override
  public NewQuery firstResult(Number firstResult)
  {
    filter.setFirstResult(firstResult.intValue());
    return this;
  }
  
  @Override
  public NewQuery maxResults(Number maxResults)
  {
    filter.setMaxResults(maxResults.intValue());
    return this;
  }
  
  @Override
  public List<NewView> execute()
  {          
    NewsManagerPort port = 
      NewsModuleBean.getPort(credentials.getUserId(), credentials.getPassword());
    return port.findNewViews(filter);
  }  
}
