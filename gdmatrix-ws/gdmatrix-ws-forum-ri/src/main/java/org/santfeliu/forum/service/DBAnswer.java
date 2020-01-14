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

import org.matrix.forum.Answer;
import org.matrix.forum.Question;
import org.matrix.security.User;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.audit.Auditable;

/**
 *
 * @author lopezrj
 */
public class DBAnswer extends Answer implements Auditable
{
  //Relationship
  private DBQuestion answerQuestion;
  
  public DBAnswer()
  {
  }
  
  public DBAnswer(Answer answer, WSEndpoint endpoint)
  {
    copyFrom(answer, endpoint);
  }

  public void copyTo(Answer answer, WSEndpoint endpoint)
  {    
    answer.setAnswerId(endpoint.toGlobalId(Answer.class,
      this.getAnswerId()));
    answer.setQuestionId(endpoint.toGlobalId(Question.class,
      this.getQuestionId()));
    answer.setText(this.getText());
    answer.setComments(this.getComments());
    answer.setCreationDateTime(this.getCreationDateTime());
    answer.setCreationUserId(endpoint.toGlobalId(User.class,
      this.getCreationUserId()));
    answer.setChangeDateTime(this.getChangeDateTime());
    answer.setChangeUserId(endpoint.toGlobalId(User.class,
      this.getChangeUserId()));
  }

  public void copyFrom(Answer answer, WSEndpoint endpoint)
  {
    setAnswerId(endpoint.toLocalId(Answer.class,
      answer.getAnswerId()));
    setQuestionId(endpoint.toLocalId(Question.class,
      answer.getQuestionId()));
    setText(answer.getText());
    setComments(answer.getComments());
  }

  public DBQuestion getAnswerQuestion()
  {
    return answerQuestion;
  }

  public void setAnswerQuestion(DBQuestion answerQuestion)
  {
    this.answerQuestion = answerQuestion;
  }
}
