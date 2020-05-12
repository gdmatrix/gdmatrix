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
package org.santfeliu.survey;

import java.util.List;
import org.matrix.survey.SurveyConstants;
import org.matrix.survey.Answer;
import org.matrix.survey.AnswerList;

import org.santfeliu.util.Table;

/**
 *
 * @author unknown
 */
public class AnswerListConverter
{
  public AnswerListConverter()
  {
  }
  
  public static Table toTable(AnswerList answerList)
  {
    Table tableAnswers = new Table(new String[]{
      SurveyConstants.ANSWERID, 
      SurveyConstants.TEXT, 
      SurveyConstants.VOTES
    });
    List<Answer> answers = answerList.getAnswerList();
    for (Answer answer : answers)
    {
      String answerId = answer.getAnswerId();
      String answerText = answer.getText();
      String answerVotes = String.valueOf(answer.getVoteCount());
      tableAnswers.addRow(answerId, answerText, answerVotes);
    }
    return tableAnswers;
  }
  
  public static AnswerList toAnswerList(Table table) throws Exception
  {
    AnswerList answerList = new AnswerList();
    for (int i = 0; i < table.getRowCount(); i++) 
    {
      String answerId = String.valueOf(table.getElementAt(i, SurveyConstants.ANSWERID));
      String answerText = (String)(table.getElementAt(i, SurveyConstants.TEXT));          
      String answerVotes = String.valueOf(table.getElementAt(i, SurveyConstants.VOTES));
      Answer answer = new Answer();
      answer.setAnswerId(answerId);
      answer.setText(answerText);
      answer.setVoteCount(new Integer(answerVotes));
      answerList.getAnswerList().add(answer);
    }
    return answerList;
  }
  
  
}
