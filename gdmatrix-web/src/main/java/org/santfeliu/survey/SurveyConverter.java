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

import java.util.HashMap;
import java.util.Map;

import org.matrix.survey.SurveyConstants;
import org.matrix.survey.AnswerList;
import org.matrix.survey.Survey;

import org.santfeliu.util.Table;

/**
 *
 * @author unknown
 */
public class SurveyConverter
{
  public SurveyConverter()
  {
  }
  
  public static Map toMap(Survey survey)
  {
    Map mapResult = new HashMap();
    mapResult.put(SurveyConstants.SURVID, survey.getSurveyId()); 
    mapResult.put(SurveyConstants.TEXT, survey.getText());
    mapResult.put(SurveyConstants.OPEN, (survey.isOpen() ? "Y" : "N"));
    mapResult.put(SurveyConstants.ANSWERS, AnswerListConverter.toTable(survey.getAnswerList())); 
    mapResult.put(SurveyConstants.TOTALVOTES, String.valueOf(survey.getVoteCount()));
    return mapResult;
  }
  
  public static Survey toSurvey(Map map) throws Exception
  {
    Survey surveyResult = new Survey();

    String surveyId = String.valueOf(map.get(SurveyConstants.SURVID));
    String text = (String)(map.get(SurveyConstants.TEXT)); 
    boolean open = "Y".equalsIgnoreCase((String)(map.get(SurveyConstants.OPEN)));
    AnswerList answerList = AnswerListConverter.toAnswerList((Table)map.get(SurveyConstants.ANSWERS));
    int voteCount = new Integer(String.valueOf(map.get(SurveyConstants.TOTALVOTES)));

    surveyResult.setSurveyId(surveyId);
    surveyResult.setText(text);
    surveyResult.setOpen(open);
    surveyResult.setAnswerList(answerList);
    surveyResult.setVoteCount(voteCount);

    return surveyResult;
  }
}
