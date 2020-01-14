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
import org.matrix.forum.Question;
import org.matrix.security.User;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.audit.Auditable;

/**
 *
 * @author lopezrj
 */
public class DBQuestion extends Question implements Auditable
{   
  private String strVisible;
  //Relationships
  private DBForum questionForum;
  private List<DBAnswer> answers;

  public DBQuestion()
  {
  }
  
  public DBQuestion(Question question, WSEndpoint endpoint)
  {
    copyFrom(question, endpoint);
  }

  public void copyTo(Question question, WSEndpoint endpoint)
  {
    question.setQuestionId(endpoint.toGlobalId(Question.class,
      this.getQuestionId()));
    question.setForumId(endpoint.toGlobalId(Forum.class,
      this.getForumId()));
    question.setTitle(this.getTitle());
    question.setText(this.getText());
    question.setCreationDateTime(this.getCreationDateTime());
    question.setCreationUserId(endpoint.toGlobalId(User.class,
      this.getCreationUserId()));
    question.setChangeDateTime(this.getChangeDateTime());
    question.setChangeUserId(endpoint.toGlobalId(User.class,
      this.getChangeUserId()));
    question.setActivityDateTime(this.getActivityDateTime());
    question.setReadCount(this.getReadCount());
    question.setVisible("Y".equalsIgnoreCase(this.getStrVisible()));
    question.setInputIndex(this.getInputIndex());
    question.setOutputIndex(outputIndex == ForumManager.MAX_OUTPUT_INDEX ?
      0 : outputIndex);
  }

  public void copyFrom(Question question, WSEndpoint endpoint)
  {
    setQuestionId(endpoint.toLocalId(Question.class,
      question.getQuestionId()));
    setForumId(endpoint.toLocalId(Forum.class,
      question.getForumId()));
    setTitle(question.getTitle());
    setText(question.getText());
    setVisible(question.isVisible());
    setStrVisible(question.isVisible() ? "Y" : "N");
  }

  public String getStrVisible()
  {
    return strVisible;
  }

  public void setStrVisible(String strVisible)
  {
    this.strVisible = strVisible;
  }

  public List<DBAnswer> getAnswers()
  {
    return answers;
  }

  public void setAnswers(List<DBAnswer> answers)
  {
    this.answers = answers;
  }

  public DBForum getQuestionForum()
  {
    return questionForum;
  }

  public void setQuestionForum(DBForum questionForum)
  {
    this.questionForum = questionForum;
  }  
}
