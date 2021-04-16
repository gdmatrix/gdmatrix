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
package org.santfeliu.survey.service;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import javax.xml.ws.WebServiceContext;

import javax.xml.ws.WebServiceException;
import org.matrix.survey.AnswerList;
import org.matrix.survey.Survey;
import org.matrix.survey.SurveyManagerPort;
import org.matrix.survey.SurveyTable;
import org.matrix.survey.SurveyView;

import org.matrix.survey.SurveyMetaData;
import org.santfeliu.ws.WSExceptionFactory;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.MultiInstance;

/**
 *
 * @author lopezrj
 */
@WebService(endpointInterface = "org.matrix.survey.SurveyManagerPort")
@HandlerChain(file="handlers.xml")
@MultiInstance
public class SurveyManager implements SurveyManagerPort 
{
  private static final Logger LOGGER = Logger.getLogger("Survey");  
  
  private static final int ANSWER_TEXT_MAX_SIZE = 100;
  private static final int SURVEY_TEXT_MAX_SIZE = 100;
  
  @Resource
  WebServiceContext wsContext;

  @PersistenceContext(unitName="survey_ri")
  public EntityManager entityManager;

  @Initializer
  public void initialize(String endpointName)
  {
  }  

  @Override
  public SurveyMetaData getSurveyMetaData()
  {
    SurveyMetaData metaData = new SurveyMetaData();
    metaData.setAnswerTextMaxSize(ANSWER_TEXT_MAX_SIZE);
    metaData.setSurveyTextMaxSize(SURVEY_TEXT_MAX_SIZE);
    return metaData;
  }

  @Override
  public String storeSurvey(String text, List<String> answers)
  {
    String surveyId = null;
    try 
    {
      LOGGER.log(Level.INFO, "storeSurvey {0}", text);
      validateSurvey(text, answers);
  
      DBSurvey dbSurvey = new DBSurvey();
      dbSurvey.setText(text);
      dbSurvey.setStrOpen("Y");
      SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");      
      dbSurvey.setStartDay(dayFormat.format(new Date()));
      dbSurvey.setEndDay(null);    
      entityManager.persist(dbSurvey);
      entityManager.flush();
      surveyId = dbSurvey.getSurveyId();
      for (int i = 0; i < answers.size(); i++) 
      {
        String answer = answers.get(i);
        DBAnswer dbAnswer = new DBAnswer();
        dbAnswer.setSurveyId(surveyId);
        dbAnswer.setAnswerId(String.valueOf(i));
        dbAnswer.setText(answer);
        dbAnswer.setVoteCount(0);
        entityManager.persist(dbAnswer);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "storeSurvey failed", ex);
      throw WSExceptionFactory.create(ex);
    }    
    return surveyId;
  }

  @Override
  public void openSurvey(String surveyId)
  {
    LOGGER.log(Level.INFO, "openSurvey {0}", surveyId);        
    Query query = entityManager.createNamedQuery("switchSurvey");
    query.setParameter("surveyId", surveyId);
    query.setParameter("open", "Y");
    query.executeUpdate();
  }

  @Override
  public void closeSurvey(String surveyId)
  {
    LOGGER.log(Level.INFO, "closeSurvey {0}", surveyId);        
    Query query = entityManager.createNamedQuery("switchSurvey");
    query.setParameter("surveyId", surveyId);
    query.setParameter("open", "N");
    query.executeUpdate();
  }

  @Override
  public void voteSurvey(String surveyId, String answerId)
  {
    LOGGER.log(Level.INFO, "voteSurvey surveyId {0} answerId {1}", 
      new String[]{surveyId, answerId});        
    Query query = entityManager.createNamedQuery("voteSurvey");
    query.setParameter("surveyId", surveyId);
    query.setParameter("answerId", answerId);
    query.executeUpdate();
  }

  @Override
  public Survey loadSurvey(String surveyId)
  {
    LOGGER.log(Level.INFO, "loadSurvey {0}", surveyId);    
    Query surveyQuery = entityManager.createNamedQuery("findSurvey");
    surveyQuery.setParameter("surveyId", surveyId);
    Object[] obj = null;
    try
    {
      obj = (Object[])surveyQuery.getSingleResult();
    }
    catch (NoResultException ex)
    {
      throw new WebServiceException("survey:SURVEY_NOT_FOUND");
    }
    Survey survey = new Survey();
    survey.setSurveyId((String)obj[0]);
    survey.setOpen(((String)obj[2]).equalsIgnoreCase("Y"));
    survey.setText((String)obj[1]);
    survey.setVoteCount(new Integer(String.valueOf(obj[4])));
    survey.setAnswerList(new AnswerList());
    
    Query query = entityManager.createNamedQuery("findAnswers");
    query.setParameter("surveyId", surveyId);
    AnswerList answerList = survey.getAnswerList();
    List<DBAnswer> answers = query.getResultList();
    for (DBAnswer answer : answers)
    {
      answerList.getAnswerList().add(answer);
    }
    survey.setAnswerList(answerList);        
    return survey;
  }    

  @Override
  public SurveyTable findSurveys()
  {
    SurveyTable surveyTable = new SurveyTable();
    try
    {
      LOGGER.log(Level.INFO, "findSurveys");    
      List<SurveyView> surveyRowList = surveyTable.getSurveyViewList();
      Query query = entityManager.createNamedQuery("findSurveys");
      List<Object[]> queryResult = query.getResultList();
      for (Object[] resultItem : queryResult)
      {
        SurveyView surveyView = new SurveyView();
        surveyView.setSurveyId((String)resultItem[0]);
        surveyView.setOpen(((String)resultItem[2]).equalsIgnoreCase("Y"));
        surveyView.setText((String)resultItem[1]);
        surveyView.setStartDay((String)resultItem[3]);
        surveyView.setVoteCount(new Integer(String.valueOf(resultItem[4])));
        surveyRowList.add(surveyView);
      }
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "findSurveys failed", ex);
      throw WSExceptionFactory.create(ex);
    }
    return surveyTable;
  }
  
  /**** private methods ****/

  private void validateSurvey(String text, List<String> answers)
    throws Exception
  {
    SurveyMetaData metaData = getSurveyMetaData();
    if (text == null || text.trim().length() == 0)
    {
      throw new Exception("VALUE_IS_MANDATORY");
    }
    else if (text.length() > metaData.getSurveyTextMaxSize())
    {
      throw new Exception("VALUE_TOO_LARGE");
    }
    else if (answers == null || answers.isEmpty())
    {
      throw new Exception("survey:SURVEY_MUST_HAVE_ANSWERS");
    }
    for (String answer : answers)
    {
      if (answer == null || answer.trim().length() == 0)
      {
        throw new Exception("VALUE_IS_MANDATORY");
      }
      else if (answer.length() > metaData.getAnswerTextMaxSize())
      {
        throw new Exception("VALUE_TOO_LARGE");
      }
    }
  }
  
}
