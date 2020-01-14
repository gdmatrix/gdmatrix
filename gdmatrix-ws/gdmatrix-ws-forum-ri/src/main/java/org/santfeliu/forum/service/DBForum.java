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
package org.santfeliu.forum.service;

import java.util.List;
import org.matrix.forum.Forum;
import org.matrix.forum.ForumType;
import org.matrix.security.User;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.audit.Auditable;

/**
 *
 * @author lopezrj
 */
public class DBForum extends Forum implements Auditable
{
  private String typeValue;
  //Relationship
  private List<DBQuestion> questions;
  
  public DBForum()
  {
  }
  
  public DBForum(Forum forum, WSEndpoint endpoint)
  {
    copyFrom(forum, endpoint);
  }

  public void copyTo(Forum forum, WSEndpoint endpoint)
  {    
    forum.setForumId(endpoint.toGlobalId(Forum.class, this.getForumId()));
    forum.setName(this.getName());
    forum.setDescription(this.getDescription());
    forum.setStartDateTime(this.getStartDateTime());
    forum.setEndDateTime(this.getEndDateTime());
    forum.setCreationDateTime(this.getCreationDateTime());
    forum.setCreationUserId(endpoint.toGlobalId(User.class, 
      this.getCreationUserId()));
    if ("N".equals(this.getTypeValue()))
      forum.setType(ForumType.NORMAL);
    else if ("I".equals(this.getTypeValue()))
      forum.setType(ForumType.INTERVIEW);
    else if ("U".equals(this.getTypeValue()))
      forum.setType(ForumType.UNCENSORED_INTERVIEW);
    forum.setEmailFrom(this.getEmailFrom());
    forum.setEmailTo(this.getEmailTo());
    forum.setLastInputIndex(this.getLastInputIndex());
    forum.setLastOutputIndex(this.getLastOutputIndex());
    forum.setGroup(this.getGroup());
    forum.setAdminRoleId(this.getAdminRoleId());
    forum.setMaxQuestions(this.getMaxQuestions());
  }

  public void copyFrom(Forum forum, WSEndpoint endpoint)
  {
    setForumId(endpoint.toLocalId(Forum.class, forum.getForumId()));
    setName(forum.getName());
    setDescription(forum.getDescription());
    setStartDateTime(forum.getStartDateTime());
    setEndDateTime(forum.getEndDateTime());
    setCreationDateTime(forum.getCreationDateTime());
    setCreationUserId(endpoint.toLocalId(User.class,
      forum.getCreationUserId()));
    setType(forum.getType());
    switch (forum.getType())
    {
      case NORMAL: setTypeValue("N"); break;
      case INTERVIEW: setTypeValue("I"); break;
      case UNCENSORED_INTERVIEW: setTypeValue("U"); break;
    }
    setEmailFrom(forum.getEmailFrom());
    setEmailTo(forum.getEmailTo());
    setGroup(forum.getGroup());
    setAdminRoleId(forum.getAdminRoleId());
    setMaxQuestions(forum.getMaxQuestions());
  }

  public String getTypeValue()
  {
    return typeValue;
  }

  public void setTypeValue(String typeValue)
  {
    this.typeValue = typeValue;
  }

  public List<DBQuestion> getQuestions()
  {
    return questions;
  }

  public void setQuestions(List<DBQuestion> questions)
  {
    this.questions = questions;
  }
}
