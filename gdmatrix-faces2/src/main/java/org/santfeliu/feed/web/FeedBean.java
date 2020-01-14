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

import org.matrix.feed.Feed;
import org.santfeliu.feed.client.FeedManagerClient;
import org.santfeliu.web.obj.ObjectBean;

/**
 *
 * @author unknown
 */
public class FeedBean extends ObjectBean
{
  public FeedBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Feed";
  }

  @Override
  public String cancel()  
  {
    FeedManagerClient.getCache().clear();
    return super.cancel();
  }
  
  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        FeedConfigBean.getPort().removeFeed(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return getControllerBean().show();
  }
  
  @Override
  public String getDescription()
  {
    FeedMainBean feedMainBean = (FeedMainBean)getBean("feedMainBean");
    Feed feed = feedMainBean.getFeed();
    return getFeedDescription(feed);
  }

  @Override
  public String getDescription(String oid)
  {
    String description = "";
    try
    {
      Feed feed = FeedConfigBean.getPort().loadFeedFromCache(oid);
      description = getFeedDescription(feed);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  private String getFeedDescription(Feed feed)
  {
    StringBuilder sb = new StringBuilder();  
    if (feed.getName() != null)
    {
      sb.append(feed.getName());
      sb.append(" ");
    }
    sb.append("(");
    sb.append(feed.getFeedId());
    sb.append(")");
    return sb.toString();
  }  
  
}
