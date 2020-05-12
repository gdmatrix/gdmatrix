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
package org.santfeliu.forum.web;

/**
 *
 * @author blanquepa
 */
public class UserHit implements Comparable
{
  private String userId;
  private String forumId;
  private long time;

  public UserHit(String userId, String forumId, long time)
  {
    this.forumId = forumId;
    this.userId = userId;
    this.time = time;
  }

  public UserHit(UserHit hit)
  {
    this.forumId = hit.getForumId();
    this.userId = hit.getUserId();
    this.time = hit.getTime();
  }

  public long getTime()
  {
    return time;
  }

  public void setTime(long time)
  {
    this.time = time;
  }

  public String getForumId()
  {
    return forumId;
  }

  public void setForumId(String forumId)
  {
    this.forumId = forumId;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public int getTimeGap()
  {
    return Long.valueOf((System.currentTimeMillis() - time) / 1000).intValue();
  }

  public boolean isExpired(long gap)
  {
    return (System.currentTimeMillis() - time > gap);
  }

  public int compareTo(Object o)
  {
    UserHit hit = (UserHit)o;
    Long ltime = Long.valueOf(time);
    Long lotime = Long.valueOf(hit.getTime());

    return lotime.intValue() - ltime.intValue();
  }
}
