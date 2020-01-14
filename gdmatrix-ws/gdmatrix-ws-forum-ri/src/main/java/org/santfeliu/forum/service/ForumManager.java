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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang.StringUtils;
import org.matrix.forum.Answer;
import org.matrix.forum.Forum;
import org.matrix.forum.ForumConstants;
import org.matrix.forum.ForumManagerPort;
import org.matrix.forum.ForumFilter;
import org.matrix.forum.ForumView;
import org.matrix.forum.Question;
import org.matrix.forum.QuestionFilter;
import org.matrix.forum.QuestionView;
import org.matrix.forum.ForumStatus;
import org.matrix.forum.ForumType;
import org.matrix.util.WSDirectory;
import org.santfeliu.jpa.JPA;
import org.matrix.util.WSEndpoint;
import org.santfeliu.forum.store.JPQLFindForumsInfoQueryBuilder;
import org.santfeliu.forum.store.JPQLFindQuestionsQueryBuilder;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.MailSender;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.audit.Auditor;
import org.santfeliu.ws.WSExceptionFactory;
import org.santfeliu.ws.WSUtils;

/**
 *
 * @author lopezrj
 */
@WebService(endpointInterface = "org.matrix.forum.ForumManagerPort")
@HandlerChain(file="handlers.xml")
@JPA
public class ForumManager implements ForumManagerPort
{
  @Resource
  WebServiceContext wsContext;

  @PersistenceContext
  public EntityManager entityManager;

  protected static final Logger log = Logger.getLogger("Forum");

  static final int MAX_FORUM_NAME_LENGTH = 200;
  static final int MAX_FORUM_DESCRIPTION_LENGTH = 4000;
  static final int MAX_FORUM_EMAILFROM_LENGTH = 100;
  static final int MAX_FORUM_EMAILTO_LENGTH = 100;
  static final int MAX_FORUM_GROUP_LENGTH = 50;
  static final int MAX_FORUM_ADMIN_ROLE_LENGTH = 50;
  static final int MAX_QUESTION_TITLE_LENGTH = 1000;
  static final int MAX_QUESTION_TEXT_LENGTH = 4000;
  static final int MAX_ANSWER_TEXT_LENGTH = 4000;
  
  static final int MAX_OUTPUT_INDEX = 999999999; // invisible index
  static final String SMTP_HOST = "mail.smtp.host";

  // Forums

  public Forum loadForum(String forumId)
  {
    log.log(Level.INFO, "loadForum {0}", new Object[]{forumId});
    if (forumId == null)
      throw new WebServiceException("forum:FORUMID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();

    String localForumId = endpoint.toLocalId(Forum.class, forumId);
    DBForum dbForum = entityManager.find(DBForum.class, 
      endpoint.toLocalId(Forum.class, localForumId));
    if (dbForum == null)
      throw new WebServiceException("forum:FORUM_NOT_FOUND");
    Forum forum = new Forum();
    dbForum.copyTo(forum, endpoint);
    return forum;
  }

  public Forum storeForum(Forum forum)
  {
    WSEndpoint endpoint = getWSEndpoint();
    User user = UserCache.getUser(wsContext);
    
    String localForumId = endpoint.toLocalId(Forum.class, forum.getForumId());
    log.log(Level.INFO, "storeForum {0}", new Object[]{localForumId});

    checkForum(forum);
    
    if (localForumId == null) //insert
    {
      DBForum dbForum = new DBForum(forum, endpoint);
      Auditor.auditCreation(dbForum, user.getUserId());
      entityManager.persist(dbForum);
      dbForum.copyTo(forum, endpoint);
    }
    else //update
    {
      DBForum dbForum = entityManager.find(DBForum.class, localForumId);
      if (dbForum == null)
        throw new WebServiceException("forum:INVALID_FORUM");
      if (!isUserForumAdmin(user, dbForum))
        throw new WebServiceException("forum:USER_CAN_NOT_UPDATE_FORUM");      
      dbForum.copyFrom(forum, endpoint);
      Auditor.auditChange(dbForum, user.getUserId());
      entityManager.merge(dbForum);
    }
    return forum;
  }

  public boolean removeForum(String forumId)
  {
    log.log(Level.INFO, "removeForum {0}", new Object[]{forumId});

    WSEndpoint endpoint = getWSEndpoint();
    User user = UserCache.getUser(wsContext);

    if (forumId == null)
      throw new WebServiceException("forum:FORUMID_IS_MANDATORY");

    DBForum dbForum = null;
    String localForumId = endpoint.toLocalId(Forum.class, forumId);
    try
    {
      dbForum = entityManager.getReference(DBForum.class, localForumId);
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
    if (!isUserForumAdmin(user, dbForum))
      throw new WebServiceException("forum:USER_CAN_NOT_REMOVE_FORUM");
    Query query = entityManager.createNamedQuery("removeForumAnswers");
    query.setParameter("forumId", localForumId);
    query.executeUpdate();
    query = entityManager.createNamedQuery("removeForumQuestions");
    query.setParameter("forumId", localForumId);
    query.executeUpdate();
    entityManager.remove(dbForum);
    return true;
  }

  public int countForums(ForumFilter filter)
  {
    log.log(Level.INFO, "countForums");
    WSEndpoint endpoint = getWSEndpoint();
    Query query = entityManager.createNamedQuery("countForums");
    applyForumFilter(query, filter, endpoint);
    return ((Number)query.getSingleResult()).intValue();
  }

  public List<Forum> findForums(ForumFilter filter)
  {
    log.log(Level.INFO, "findForums");
    WSEndpoint endpoint = getWSEndpoint();
    List<DBForum> dbForumList = doFindForums(endpoint, filter);
    ArrayList<Forum> forumList = new ArrayList<Forum>();
    for (DBForum dbForum : dbForumList)
    {
      Forum forum = new Forum();
      dbForum.copyTo(forum, endpoint);
      forumList.add(forum);
    }
    return forumList;
  }

  public List<ForumView> findForumViews(ForumFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findForumViews");
      WSEndpoint endpoint = getWSEndpoint();
      User user = UserCache.getUser(wsContext);
      boolean userSuperAdmin = isUserSuperAdmin(user);

      ForumFilter localFilter = endpoint.toLocal(ForumFilter.class, filter);
      List<DBForum> dbForumList = doFindForums(endpoint, localFilter);
      if (dbForumList.isEmpty()) return Collections.EMPTY_LIST;
      
      Map<String, ForumView> forumViewMap = new HashMap<String, ForumView>();
      List<String> localForumIdList = new ArrayList<String>();
      for (DBForum dbForum : dbForumList)
      {
        localForumIdList.add(dbForum.getForumId());
        Forum forum = new Forum();
        dbForum.copyTo(forum, endpoint);
        ForumView forumView = new ForumView();
        forumView.setForum(forum);
        forumView.setEditable(isUserForumAdmin(user, forum));
        forumView.setStatus(getForumStatus(forum));
        forumViewMap.put(dbForum.getForumId(), forumView);
      }
      // populate forum info with other queries
      JPQLFindForumsInfoQueryBuilder queryBuilder =
        new JPQLFindForumsInfoQueryBuilder();
      queryBuilder.setForumIdList(localForumIdList);
      queryBuilder.setUserSuperAdmin(userSuperAdmin);
      queryBuilder.setUserId(user.getUserId());
      queryBuilder.setUserRolesList(user.getRolesList());
      Query query;
      List<Object[]> rowList;
      queryBuilder.setElement(JPQLFindForumsInfoQueryBuilder.QUESTIONS);
      query = queryBuilder.getQuery(entityManager);
      rowList = query.getResultList();
      for (Object[] row : rowList)
      {
        String localForumId = String.valueOf(row[0]);
        int count = ((Number)row[1]).intValue();
        ForumView forumView = forumViewMap.get(localForumId);
        forumView.setVisibleQuestionCount(count);
      }
      queryBuilder.setElement(JPQLFindForumsInfoQueryBuilder.ANSWERS);
      query = queryBuilder.getQuery(entityManager);
      rowList = query.getResultList();
      for (Object[] row : rowList)
      {
        String localForumId = String.valueOf(row[0]);
        int count = ((Number)row[1]).intValue();
        ForumView forumView = forumViewMap.get(localForumId);
        forumView.setVisibleAnswerCount(count);
      }
      queryBuilder.setElement(JPQLFindForumsInfoQueryBuilder.PENDENT_QUESTIONS);
      query = queryBuilder.getQuery(entityManager);
      rowList = query.getResultList();
      for (Object[] row : rowList)
      {
        String localForumId = String.valueOf(row[0]);
        int count = ((Number)row[1]).intValue();
        ForumView forumView = forumViewMap.get(localForumId);
        forumView.setPendentQuestionCount(count);
      }

      ArrayList<ForumView> forumViewList = new ArrayList<ForumView>();
      for (String localForumId : localForumIdList)
      {
        forumViewList.add(forumViewMap.get(localForumId));
      }
      return forumViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findForumViews", ex);
      throw WSExceptionFactory.create(ex);
    }
  }

  // Questions

  public Question loadQuestion(String questionId)
  {
    log.log(Level.INFO, "loadQuestion {0}", new Object[]{questionId});
    return doLoadQuestion(questionId, false);
  }

  public Question readQuestion(String questionId)
  {
    log.log(Level.INFO, "readQuestion {0}", new Object[]{questionId});
    return doLoadQuestion(questionId, true);
  }

  public Question storeQuestion(Question question)
  {
    String questionId = question.getQuestionId();
    log.log(Level.INFO, "storeQuestion {0}", new Object[]{questionId});

    WSEndpoint endpoint = getWSEndpoint();
    User user = UserCache.getUser(wsContext);
    checkQuestion(question);

    String localForumId =
      endpoint.toLocalId(Forum.class, question.getForumId());
    if (localForumId == null)
      throw new WebServiceException("forum:FORUMID_IS_MANDATORY");

    lockForum(localForumId);

    DBForum dbForum = entityManager.find(DBForum.class, localForumId);
    if (dbForum == null)
      throw new WebServiceException("forum:FORUM_NOT_FOUND");

    boolean userForumAdmin = isUserForumAdmin(user, dbForum);

    // remove tags from text
    String text = question.getText();
    question.setText(TextUtils.removeTags(text));
    
    if (questionId == null) //insert new question
    {
      if (!userForumAdmin)
      {
        ForumStatus status = getForumStatus(dbForum);
        switch(status)
        {
          case CLOSED:
            throw new WebServiceException("forum:FORUM_IS_CLOSED");
          case CLOSED_BEFORE:
            throw new WebServiceException("forum:MAX_QUESTIONS_LIMIT_REACHED");
        }      
      }
      DBQuestion dbQuestion = new DBQuestion(question, endpoint);
      // update input index
      int inputIndex = dbForum.getLastInputIndex() + 1;
      dbForum.setLastInputIndex(inputIndex);
      dbQuestion.setInputIndex(inputIndex);
      if (question.isVisible())
      {
        // update output index
        int ouputIndex = dbForum.getLastOutputIndex() + 1;
        dbForum.setLastOutputIndex(ouputIndex);
        dbQuestion.setOutputIndex(ouputIndex);
      }
      else dbQuestion.setOutputIndex(MAX_OUTPUT_INDEX);

      Auditor.auditCreation(dbQuestion, user.getUserId());
      dbQuestion.setActivityDateTime(dbQuestion.getCreationDateTime());
      updateQuestionText(dbQuestion);
      entityManager.persist(dbQuestion);
      sendEMail(dbForum, dbQuestion);
      dbQuestion.copyTo(question, endpoint);
    }
    else // update question
    {
      String localQuestionId =
        endpoint.toLocalId(Question.class, questionId);
      DBQuestion dbQuestion = entityManager.find(DBQuestion.class,
        localQuestionId);
      if (dbQuestion == null)
        throw new WebServiceException("forum:INVALID_QUESTION");
      if (!userForumAdmin)
        throw new WebServiceException("forum:USER_CAN_NOT_UPDATE_QUESTION");
      boolean wasVisible = "Y".equalsIgnoreCase(dbQuestion.getStrVisible());
      dbQuestion.copyFrom(question, endpoint);
      if (!wasVisible && question.isVisible())
      {
        // update output index
        int ouputIndex = dbForum.getLastOutputIndex() + 1;
        dbForum.setLastOutputIndex(ouputIndex);
        dbQuestion.setOutputIndex(ouputIndex);
      }
      Auditor.auditChange(dbQuestion, user.getUserId());
      dbQuestion.setActivityDateTime(dbQuestion.getChangeDateTime());
      updateQuestionText(dbQuestion);
      entityManager.merge(dbQuestion);
      dbQuestion.copyTo(question, endpoint);
    }
    return question;
  }

  public boolean removeQuestion(String questionId)
  {
    log.log(Level.INFO, "removeQuestion {0}", new Object[]{questionId});
    if (questionId == null)
      throw new WebServiceException("forum:QUESTIONID_IS_MANDATORY");

    User user = UserCache.getUser(wsContext);

    WSEndpoint endpoint = getWSEndpoint();
    DBQuestion dbQuestion = null;
    String localQuestionId = endpoint.toLocalId(Question.class, questionId);
    try
    {
      dbQuestion = entityManager.getReference(DBQuestion.class,
        localQuestionId);
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }

    DBForum dbForum = dbQuestion.getQuestionForum();
    if (ForumType.INTERVIEW.equals(dbForum.getType()) &&
      "Y".equals(dbQuestion.getStrVisible())) // question is visible
    {
      if (!isUserSuperAdmin(user))
      {
        throw new WebServiceException("forum:USER_CAN_NOT_REMOVE_QUESTION");
      }
    }

    if (!isUserForumAdmin(user, dbForum))
      throw new WebServiceException("forum:USER_CAN_NOT_REMOVE_QUESTION");

    Query query = entityManager.createNamedQuery("removeQuestionAnswers");
    query.setParameter("questionId", localQuestionId);
    query.executeUpdate();
    entityManager.remove(dbQuestion);
    return true;
  }

  public int countQuestionViews(QuestionFilter filter)
  {
    try
    {
      log.log(Level.INFO, "countQuestionViews");
      WSEndpoint endpoint = getWSEndpoint();
      User user = UserCache.getUser(wsContext);
      String forumId = filter.getForumId();
      if (forumId == null)
        throw new WebServiceException("forum:FORUMID_IS_MANDATORY");

      String localForumId = endpoint.toLocalId(Forum.class, forumId);
      DBForum dbForum = entityManager.find(DBForum.class, localForumId);
      if (dbForum == null)
        throw new WebServiceException("forum:INVALID_FORUM");

      boolean userForumAdmin = isUserForumAdmin(user, dbForum);

      JPQLFindQuestionsQueryBuilder queryBuilder =
        new JPQLFindQuestionsQueryBuilder();
      queryBuilder.setCounterQuery(true);
      queryBuilder.setUserId(user.getUserId());
      queryBuilder.setFilter(endpoint.toLocal(QuestionFilter.class, filter));
      queryBuilder.setUserForumAdmin(userForumAdmin);

      Query query = queryBuilder.getQuery(entityManager);
      return ((Number)query.getSingleResult()).intValue();
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "countQuestionViews", ex);
      throw WSExceptionFactory.create(ex);
    }
  }

  public List<QuestionView> findQuestionViews(QuestionFilter filter)
  {
    try
    {
      log.log(Level.INFO, "findQuestionViews");
      WSEndpoint endpoint = getWSEndpoint();
      // Questions query
      User user = UserCache.getUser(wsContext);
      String forumId = filter.getForumId();
      if (forumId == null)
        throw new WebServiceException("forum:FORUMID_IS_MANDATORY");

      String localForumId = endpoint.toLocalId(Forum.class, forumId);
      DBForum dbForum = entityManager.find(DBForum.class, localForumId);
      if (dbForum == null)
        throw new WebServiceException("forum:INVALID_FORUM");

      boolean userForumAdmin = isUserForumAdmin(user, dbForum);

      JPQLFindQuestionsQueryBuilder queryBuilder =
        new JPQLFindQuestionsQueryBuilder();
      queryBuilder.setCounterQuery(false);
      queryBuilder.setUserId(user.getUserId());
      queryBuilder.setFilter(endpoint.toLocal(QuestionFilter.class, filter));
      queryBuilder.setUserForumAdmin(userForumAdmin);
      Query query = queryBuilder.getQuery(entityManager);
      
      List<DBQuestion> dbQuestionList = query.getResultList();
      Map<String, QuestionView> questionViewMap =
        new HashMap<String, QuestionView>();
      List<QuestionView> questionViewList = new ArrayList();
      List<String> visibleQuestionIds = new ArrayList();
      for (DBQuestion dbQuestion : dbQuestionList)
      {
        QuestionView questionView = new QuestionView();
        Question question = new Question();
        dbQuestion.copyTo(question, endpoint);
        questionView.setQuestion(question);
        questionViewMap.put(dbQuestion.getQuestionId(), questionView);
        questionViewList.add(questionView);
        if (question.isVisible() || userForumAdmin)
        {
          // this question answer must be loaded
          visibleQuestionIds.add(dbQuestion.getQuestionId());
        }
      }
      // Answers query
      if (!visibleQuestionIds.isEmpty())
      {
        Query answersQuery =
          entityManager.createNamedQuery("findQuestionsAnswers");
        answersQuery.setParameter("questionId", 
          listToString(visibleQuestionIds));
        List<Object[]> rowList = answersQuery.getResultList();
        for (Object[] row : rowList)
        {
          String localQuestionId = (String)row[0];
          DBAnswer dbAnswer = (DBAnswer)row[1];
          Answer answer = new Answer();
          dbAnswer.copyTo(answer, endpoint);
          QuestionView questionView = questionViewMap.get(localQuestionId);
          questionView.getAnswer().add(answer);
        }
      }
      return questionViewList;
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "findQuestionViews", ex);
      throw WSExceptionFactory.create(ex);
    }
  }

  // Answers

  public Answer loadAnswer(String answerId)
  {
    log.log(Level.INFO, "loadAnswer {0}", new Object[]{answerId});
    if (answerId == null)
      throw new WebServiceException("forum:ANSWERID_IS_MANDATORY");

    User user = UserCache.getUser(wsContext);

    WSEndpoint endpoint = getWSEndpoint();
    String localAnswerId = endpoint.toLocalId(Answer.class, answerId);
    DBAnswer dbAnswer = entityManager.find(DBAnswer.class, localAnswerId);
    if (dbAnswer == null)
      throw new WebServiceException("forum:ANSWER_NOT_FOUND");
    DBForum dbForum = dbAnswer.getAnswerQuestion().getQuestionForum();
    if (!isUserForumAdmin(user, dbForum))
    {
      DBQuestion dbQuestion = entityManager.find(DBQuestion.class,
        dbAnswer.getQuestionId());
      if (!"Y".equalsIgnoreCase(dbQuestion.getStrVisible()))
        throw new WebServiceException("forum:ANSWER_NOT_VISIBLE");
    }
    Answer answer = new Answer();
    dbAnswer.copyTo(answer, endpoint);
    return answer;
  }

  public Answer storeAnswer(Answer answer)
  {
    String answerId = answer.getAnswerId();
    log.log(Level.INFO, "storeAnswer {0}", new Object[]{answerId});
    checkAnswer(answer); 
    WSEndpoint endpoint = getWSEndpoint();
    User user = UserCache.getUser(wsContext);    

    if (answer.getQuestionId() == null)
      throw new WebServiceException("forum:QUESTIONID_IS_MANDATORY");

    String localQuestionId =
      endpoint.toLocalId(Question.class, answer.getQuestionId());
    DBQuestion dbQuestion =
      entityManager.find(DBQuestion.class, localQuestionId);
    if (dbQuestion == null)
      throw new WebServiceException("forum:QUESTION_NOT_FOUND");

    DBForum dbForum = dbQuestion.getQuestionForum();
    DBAnswer dbAnswer;
    if (answerId == null) // insert new answer
    {
      dbAnswer = new DBAnswer(answer, endpoint);
      Auditor.auditCreation(dbAnswer, user.getUserId());
      updateAnswerText(dbAnswer);
      entityManager.persist(dbAnswer);
      sendEMail(dbForum, dbAnswer);
      dbAnswer.copyTo(answer, endpoint);
    }
    else // update answer
    {
      String localAnswerId =
        endpoint.toLocalId(Answer.class, answerId);
      dbAnswer = entityManager.find(DBAnswer.class, localAnswerId);
      if (dbAnswer == null)
        throw new WebServiceException("forum:INVALID_ANSWER");

      if (!isUserForumAdmin(user, dbForum))
        throw new WebServiceException("forum:USER_CAN_NOT_UPDATE_ANSWER");

      dbAnswer.copyFrom(answer, endpoint);
      Auditor.auditChange(dbAnswer, user.getUserId());
      updateAnswerText(dbAnswer);
      entityManager.merge(dbAnswer);
      dbAnswer.copyTo(answer, endpoint);
    }
    // update question activityDateTime
    dbQuestion.setActivityDateTime(dbAnswer.getChangeDateTime());
    return answer;
  }

  public boolean removeAnswer(String answerId)
  {
    log.log(Level.INFO, "removeAnswer {0}", new Object[]{answerId});
    if (answerId == null)
      throw new WebServiceException("forum:ANSWERID_IS_MANDATORY");

    User user = UserCache.getUser(wsContext);

    WSEndpoint endpoint = getWSEndpoint();
    DBAnswer dbAnswer = null;
    String localAnswerId = endpoint.toLocalId(Answer.class, answerId);
    try
    {
      dbAnswer = entityManager.getReference(DBAnswer.class, localAnswerId);
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
    DBForum dbForum = dbAnswer.getAnswerQuestion().getQuestionForum();
    if (!isUserForumAdmin(user, dbForum))
      throw new WebServiceException("forum:USER_CAN_NOT_REMOVE_ANSWER");
    entityManager.remove(dbAnswer);
    return true;
  }

  // Private methods

  private Question doLoadQuestion(String questionId, boolean incrementReadCount)
  {
    if (questionId == null)
      throw new WebServiceException("forum:QUESTIONID_IS_MANDATORY");

    WSEndpoint endpoint = getWSEndpoint();
    User user = UserCache.getUser(wsContext);

    String localQuestionId = endpoint.toLocalId(Question.class, questionId);
    DBQuestion dbQuestion = 
       entityManager.find(DBQuestion.class, localQuestionId);
    if (dbQuestion == null)
      throw new WebServiceException("forum:QUESTION_NOT_FOUND");

    DBForum dbForum = dbQuestion.getQuestionForum();
    
    if (!isUserForumAdmin(user, dbForum) &&
      !"Y".equalsIgnoreCase(dbQuestion.getStrVisible()))
      throw new WebServiceException("forum:QUESTION_NOT_VISIBLE");

    if (incrementReadCount)
    {
      dbQuestion.setReadCount(dbQuestion.getReadCount() + 1);
    }
    Question question = new Question();
    dbQuestion.copyTo(question, endpoint);
    return question;
  }

  private List<DBForum> doFindForums(WSEndpoint endpoint, ForumFilter filter)
  {
    Query query = entityManager.createNamedQuery("findForums");
    applyForumFilter(query, filter, endpoint);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    return query.getResultList();
  }

  private void applyForumFilter(Query query, ForumFilter filter,
    WSEndpoint endpoint)
  {
    List<String> localForumIdList = endpoint.toLocalIds(Forum.class,
      filter.getForumId());
    query.setParameter("forumId", listToString(localForumIdList));
    query.setParameter("name", likePattern(filter.getName(), false));
    query.setParameter("description",
      likePattern(filter.getDescription(), false));
    query.setParameter("group", likePattern(filter.getGroup(), true));
  }

  private String likePattern(String value, boolean caseSensitive)
  {
    if (value == null || value.length() == 0)
    {
      return null;
    }
    else if (caseSensitive)
    {
      return "%" + value + "%";
    }
    else
    {
      return "%" + value.toUpperCase() + "%";
    }
  }

  private WSEndpoint getWSEndpoint()
  {
    String endpointName = WSUtils.getServletAdapter(wsContext).getName();
    return WSDirectory.getInstance().getEndpoint(endpointName);
  }

  private String listToString(List<String> list)
  {
    String result = TextUtils.collectionToString(list, ",");
    if (result != null) result = "," + result + ",";
    return result;
  }

  private boolean isUserForumAdmin(User user, Forum forum)
  {
    return isUserSuperAdmin(user) || user.isInRole(forum.getAdminRoleId());
  }

  private boolean isUserSuperAdmin(User user)
  {
    return user.isInRole(ForumConstants.FORUM_ADMIN_ROLE);
  }

  private void sendEMail(DBForum dbForum, DBQuestion dbQuestion)
  {
    try
    {
      String eMailFrom = dbForum.getEmailFrom();
      String eMailTo = dbForum.getEmailTo();
      if ((eMailFrom != null) && (eMailFrom.trim().length() > 0) &&
          (eMailTo != null) && (eMailTo.trim().length() > 0))
      {
        String host = MatrixConfig.getProperty(SMTP_HOST);
        String[] recArray = new String[1];
        recArray[0] = eMailTo.trim();
        String subject = "Nova pregunta al fòrum " +
          dbForum.getForumId() + " (" + dbForum.getName() + ")";
        StringBuilder buffer = new StringBuilder();
        if (!StringUtils.isBlank(dbQuestion.getTitle()))
        {
          buffer.append(dbQuestion.getTitle());
          buffer.append("\n\n");
        }
        buffer.append(dbQuestion.getText());
        
        MailSender.sendMail(host, eMailFrom.trim(), recArray, subject, 
          buffer.toString(), true);
      }
    }
    catch (Exception ex)
    {
      throw new WebServiceException("forum:EMAIL_SENDING_ERROR");
    }
  }

  private void sendEMail(DBForum dbForum, DBAnswer dbAnswer)
  {
    try
    {
      String eMailFrom = dbForum.getEmailFrom();
      String eMailTo = dbForum.getEmailTo();
      if ((eMailFrom != null) && (eMailFrom.trim().length() > 0) &&
          (eMailTo != null) && (eMailTo.trim().length() > 0))
      {
        String host = MatrixConfig.getProperty(SMTP_HOST);
        String[] recArray = new String[1];
        recArray[0] = eMailTo.trim();
        String subject = "Nova resposta al fòrum " +
          dbForum.getForumId() + " (" + dbForum.getName() + ")";
        String text = dbAnswer.getText();
        MailSender.sendMail(host, eMailFrom.trim(), recArray, subject, text,
          true);
      }
    }
    catch (Exception ex)
    {
      throw new WebServiceException("forum:EMAIL_SENDING_ERROR");
    }
  }

  private void checkForum(Forum forum)
  {
    if (forum.getStartDateTime() == null ||
      forum.getStartDateTime().trim().length() == 0)
    {
      throw new WebServiceException("forum:STARTDATETIME_IS_MANDATORY");
    }
    else if (!isValidDateTime(forum.getStartDateTime()))
    {
      throw new WebServiceException("forum:INVALID_STARTDATETIME");
    }
    if (forum.getEndDateTime() != null &&
      forum.getEndDateTime().trim().length() > 0 &&
      !isValidDateTime(forum.getEndDateTime()))
    {
      throw new WebServiceException("forum:INVALID_ENDDATETIME");
    }
    if (forum.getName() == null || forum.getName().trim().length() == 0)
    {
      throw new WebServiceException("forum:NAME_IS_MANDATORY");
    }
    else if (forum.getName().length() > MAX_FORUM_NAME_LENGTH)
      throw new WebServiceException("forum:NAME_TOO_LONG");
    if (forum.getDescription() != null &&
      forum.getDescription().length() > MAX_FORUM_DESCRIPTION_LENGTH)
      throw new WebServiceException("forum:DESCRIPTION_TOO_LONG");
    if (forum.getEmailFrom() != null &&
      forum.getEmailFrom().length() > MAX_FORUM_EMAILFROM_LENGTH)
      throw new WebServiceException("forum:EMAILFROM_TOO_LONG");
    if (forum.getEmailTo() != null &&
      forum.getEmailTo().length() > MAX_FORUM_EMAILTO_LENGTH)
      throw new WebServiceException("forum:EMAILTO_TOO_LONG");
    if (forum.getGroup() == null || forum.getGroup().trim().length() == 0)
    {
      throw new WebServiceException("forum:GROUP_IS_MANDATORY");
    }
    else if (forum.getGroup().length() > MAX_FORUM_GROUP_LENGTH)
    {
      throw new WebServiceException("forum:GROUP_TOO_LONG");
    }
    if (forum.getAdminRoleId() == null ||
      forum.getAdminRoleId().trim().length() == 0)
    {
      throw new WebServiceException("forum:ADMIN_ROLE_IS_MANDATORY");
    }
    else if (forum.getAdminRoleId().length() > MAX_FORUM_ADMIN_ROLE_LENGTH)
      throw new WebServiceException("forum:ADMIN_ROLE_TOO_LONG");
  }

  private void checkQuestion(Question question)
  {
    if (question.getTitle() != null &&
      question.getTitle().length() > MAX_QUESTION_TITLE_LENGTH)
      throw new WebServiceException("forum:TITLE_TOO_LONG");
    if (question.getText() != null &&
      question.getText().length() > MAX_QUESTION_TEXT_LENGTH)
      throw new WebServiceException("forum:TEXT_TOO_LONG");
  }

  private void checkAnswer(Answer answer)
  {
    if ((answer.getText() == null || answer.getText().trim().length() == 0) &&
      (answer.getComments() == null || answer.getComments().trim().length() == 0))
      throw new WebServiceException("forum:INVALID_ANSWER");
    if (answer.getText() != null &&
      answer.getText().length() > MAX_ANSWER_TEXT_LENGTH)
      throw new WebServiceException("forum:TEXT_TOO_LONG");
  }

  private boolean isValidDateTime(String dateTime)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    try
    {
      format.parse(dateTime);
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  private void lockForum(String localForumId)
  {
    Query query = entityManager.createNamedQuery("lockForum");
    query.setParameter("forumId", localForumId);
    query.executeUpdate();
  }

  private boolean isMaxQuestionsLimitReached(Forum forum)
  {
    return forum.getLastInputIndex() >= forum.getMaxQuestions();
  }

  private ForumStatus getForumStatus(Forum forum)
  {
    try
    {
      Date startDate = null;
      Date endDate = null;
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      if (forum.getStartDateTime() != null)
        startDate = dateFormat.parse(forum.getStartDateTime());
      if (forum.getEndDateTime() != null)
        endDate = dateFormat.parse(forum.getEndDateTime());
      Date now = new Date();

      if (startDate != null && now.before(startDate)
        && isMaxQuestionsLimitReached(forum))
      {
        return ForumStatus.CLOSED_BEFORE;
      }
      else if (startDate != null && now.before(startDate)
        && !isMaxQuestionsLimitReached(forum))
      {
        return ForumStatus.OPEN_BEFORE;
      }
      else if (endDate != null && now.after(endDate))
      {
        return ForumStatus.CLOSED;
      }
      else
      {
        return ForumStatus.OPEN;
      }
    }
    catch (ParseException pex)
    {
      throw new WebServiceException("forum:INVALID_DATE");
    }
  }

  private void updateQuestionText(DBQuestion dbQuestion)
  {
    String title = dbQuestion.getTitle();
    if (title != null)
    {
      String fixedTitle = TextUtils.replaceSpecialChars(title);
      dbQuestion.setTitle(fixedTitle);
    }
    String text = dbQuestion.getText();
    if (text != null)
    {
      String fixedText = TextUtils.replaceSpecialChars(text);
      dbQuestion.setText(fixedText);
    }
  }

  private void updateAnswerText(DBAnswer dbAnswer)
  {
    String text = dbAnswer.getText();
    if (text != null)
    {
      String fixedText = TextUtils.replaceSpecialChars(text);
      dbAnswer.setText(fixedText);
    }
    String comments = dbAnswer.getComments();
    if (comments != null)
    {
      String fixedComments = TextUtils.replaceSpecialChars(comments);
      dbAnswer.setComments(fixedComments);
    }    
    parseTextIndexes(dbAnswer);
  }

  private void parseTextIndexes(DBAnswer dbAnswer)
  {
    // Looking for input indexes
    String text = dbAnswer.getText();
    Set<String> inputIndexSet = new HashSet<String>();
    StringBuilder sb = new StringBuilder();
    int lastIndex = text.indexOf("#", 0);
    while (lastIndex >= 0)
    {
      int i = lastIndex + 1;
      boolean endIndex = false;
      while (!endIndex && i < text.length())
      {
        char c = text.charAt(i++);
        if (c == 13 || c == 32 || c == '.' || c == ',' || c == ';' ||
          c == '!' || c == '?' || c == ')')
        {
          endIndex = true;
          String auxInputIndex = sb.toString();
          try
          {
            Integer.parseInt(auxInputIndex);
            inputIndexSet.add(auxInputIndex);
          }
          catch (NumberFormatException ex)
          {
          }
          sb = new StringBuilder();
        }
        else
        {
          sb.append(c);
        }
      }
      if (!endIndex)
      {
        String auxInputIndex = sb.toString();
        try
        {
          Integer.parseInt(auxInputIndex);
          inputIndexSet.add(auxInputIndex);
        }
        catch (NumberFormatException ex)
        {
        }        
      }
      lastIndex = text.indexOf("#", lastIndex + 1);
    }
    // Replacing input indexes for output indexes
    if (!inputIndexSet.isEmpty())
    {
      List<String> inputIndexList = new ArrayList<String>(inputIndexSet);
      Query query = entityManager.createNamedQuery("getIndicesCorrespondence");
      query.setParameter("questionId", dbAnswer.getQuestionId());
      query.setParameter("inputIndexList", listToString(inputIndexList));
      List<Object[]> rowList = query.getResultList();
      for (Object[] row : rowList)
      {
        String inputIndex = "#" + String.valueOf(row[0]);
        String outputIndex = String.valueOf(row[1]);
        text = text.replace(inputIndex, outputIndex);
      }
      dbAnswer.setText(text);
    }
  }
}
