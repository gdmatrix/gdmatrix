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
package org.santfeliu.feed.web;

import java.util.List;
import org.matrix.feed.Feed;
import org.matrix.feed.FeedFilter;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author unknown
 */
@CMSManagedBean
public class FeedSearchBean extends BasicSearchBean
{
  private String feedIdInput;
  private FeedFilter filter;
  
  public FeedSearchBean()
  {
    filter = new FeedFilter();
  }

  public String getFeedIdInput()
  {
    return feedIdInput;
  }

  public void setFeedIdInput(String feedIdInput)
  {
    this.feedIdInput = feedIdInput;
  }

  public FeedFilter getFilter()
  {
    return filter;
  }

  public void setFilter(FeedFilter filter)
  {
    this.filter = filter;
  }

  public int countResults()
  {
    try
    {
      setFilterFeedId();
      return FeedConfigBean.getPort().countFeedsFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {      
      setFilterFeedId();
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return FeedConfigBean.getPort().findFeedsFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  @CMSAction
  public String show()
  {
    return "feed_search";
  }

  public String selectFeed()
  {
    Feed row = (Feed)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String feedId = row.getFeedId();
    return getControllerBean().select(feedId);
  }

  public String showFeed()
  {
    return getControllerBean().showObject("Feed",
      (String)getValue("#{row.feedId}"));
  }

  private void setFilterFeedId()
  {
    filter.getFeedId().clear();
    if (feedIdInput != null)
    {
      for (String feedId : feedIdInput.split(";"))
      {
        if (!feedId.isEmpty()) filter.getFeedId().add(feedId);
      }
    }
  }

}
