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

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.forum.Answer;
import org.matrix.forum.Forum;
import org.matrix.forum.ForumConstants;
import org.matrix.forum.ForumFilter;
import org.matrix.forum.Question;
import org.matrix.forum.QuestionFilter;
import org.matrix.forum.QuestionView;
import org.matrix.forum.ForumType;
import org.matrix.forum.ShowAnswered;
import org.matrix.forum.ShowVisible;
import org.matrix.forum.OrderBy;
import org.matrix.forum.ForumView;
import org.matrix.forum.ForumStatus;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.BigList;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
@CMSManagedBean
public class ForumCatalogueBean extends WebBean implements Serializable
{
  //Paginator constants
  @CMSProperty
  public static final String FORUMS_PAGE_SIZE_PROPERTY = "forumsPageSize";
  @CMSProperty
  public static final String QUESTIONS_PAGE_SIZE_PROPERTY = "questionsPageSize";
  //Node properties
  @CMSProperty
  public static final String FORUM_ID_PROPERTY = "forumId";
  @CMSProperty
  public static final String FORUM_GROUP_PROPERTY = "forumGroup";
  @CMSProperty
  public static final String FORUM_NAME_LABEL_PROPERTY = "forumNameLabel";
  @CMSProperty
  public static final String PARTICIPATE_LABEL_PROPERTY = "participateLabel";
  @CMSProperty
  public static final String READ_LABEL_PROPERTY = "readLabel";
  @CMSProperty
  public static final String SETUP_LABEL_PROPERTY = "setupLabel";
  @CMSProperty
  public static final String AWAITING_RESPONSE_LABEL_PROPERTY =
    "awaitingResponseLabel";
  @CMSProperty
  public static final String ANSWERER_ALIAS_PROPERTY = "answererAlias";
  @CMSProperty
  public static final String ASK_QUESTION_LABEL_PROPERTY = "askQuestionLabel";
  @CMSProperty
  public static final String QUESTION_LABEL_PROPERTY = "questionLabel";
  @CMSProperty
  public static final String OTHER_FORUMS_LABEL_PROPERTY = "otherForumsLabel";
  @CMSProperty
  public static final String QUESTION_SUBMITTED_LABEL_PROPERTY =
    "questionSubmittedLabel";
  @CMSProperty
  public static final String OPEN_STATUS_LABEL_PROPERTY = "openStatusLabel";
  @CMSProperty
  public static final String CLOSED_STATUS_LABEL_PROPERTY = "closedStatusLabel";
  @CMSProperty
  public static final String OPEN_BEFORE_STATUS_LABEL_PROPERTY =
    "openBeforeStatusLabel";
  @CMSProperty
  public static final String CLOSED_BEFORE_STATUS_LABEL_PROPERTY =
    "closedBeforeStatusLabel";
  @CMSProperty
  public static final String REFRESH_TIME_PROPERTY = "refreshTime";
  @CMSProperty
  public static final String RENDER_FORUM_TYPE_ICON_PROPERTY =
    "renderForumTypeIcon";
  @CMSProperty
  public static final String PENDING_QUESTIONS_LABEL_PROPERTY =
    "pendingQuestionsLabel";
  @CMSProperty
  public static final String UPDATE_NODE_ROLEID_PROPERTY = "roles.update";
  @CMSProperty
  public static final String QUESTION_MAX_LENGTH_PROPERTY =
    "questionMaxLength";

  @CMSProperty
  public static final String MAX_FILE_SIZE_PROPERTY = "maxFileSize";
  @CMSProperty
  public static final String DOCTYPEID_PROPERTY = "docTypeId";
  @CMSProperty
  public static final String LINKED_DOCUMENT_TITLE_PROPERTY = "linkedDocumentTitle";
  
  private static final String DEFAULT_SETUP_LABEL = "Setup";
  private static final String DEFAULT_PARTICIPATE_LABEL = "Participate";
  private static final String DEFAULT_READ_LABEL = "Read";
  private static final String DEFAULT_AWAITING_RESPONSE_LABEL =
    "Awaiting response";
  private static final String DEFAULT_ASK_QUESTION_LABEL = "Ask question";
  private static final String DEFAULT_QUESTION_LABEL = "Question";
  private static final String DEFAULT_OTHER_FORUMS_LABEL = "Other forums";
  private static final String DEFAULT_QUESTION_SUBMITTED_LABEL =
    "Question submmitted";
  private static final String DEFAULT_REFRESH_TIME = "60000"; // 1 minute

  private static final int FORUMS_PAGE_SIZE = 10;
  private static final int QUESTIONS_PAGE_SIZE = 10;
  private static final int CACHE_SIZE = 15;

  //Global variables
  private ForumView currentForumView;
  private Question currentQuestion;
  private List<Answer> questionAnswers;
  private Answer currentAnswer;

  //Filter variables
  private ForumFilter forumFilter;
  private QuestionFilter questionFilter;

  //Data variables
  private boolean singleForum;
  private DataModel forumsDataModel;
  private DataModel questionsDataModel;

  private UploadedFile uploadedFile;
  private int scroll;
  private boolean preserveScroll = false;
  private String questionHash = null;
  
  public ForumCatalogueBean()
  {
    forumFilter = new ForumFilter();
    forumsDataModel =
      new DataModel(FORUMS_PAGE_SIZE_PROPERTY, FORUMS_PAGE_SIZE, CACHE_SIZE);
    questionFilter = new QuestionFilter();
    questionsDataModel = 
      new DataModel(QUESTIONS_PAGE_SIZE_PROPERTY, QUESTIONS_PAGE_SIZE,
        CACHE_SIZE);
  }

  public int getScroll()
  {
    return preserveScroll ? scroll : 0;
  }

  public void setScroll(int scroll)
  {
    this.scroll = scroll;
    preserveScroll = false;
  }
  
  public UploadedFile getUploadedFile()
  {
    return uploadedFile;
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    this.uploadedFile = uploadedFile;
    System.out.println("uploadedFile: " + uploadedFile);
  }
  
  public long getMaxFileSize()
  {
    String value = getProperty(MAX_FILE_SIZE_PROPERTY);
    if (value == null) return 0;
    return Long.parseLong(value);
  }
  
  public void uploadFile()
  {
    try
    {
      long maxFileSize = getMaxFileSize();
      
      if (uploadedFile != null)
      {
        if (uploadedFile.getSize() > maxFileSize)
          throw new Exception("MAX_SIZE_EXCEEDED");

        Forum forum = currentForumView.getForum();        
        
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        DocumentManagerClient docClient = new DocumentManagerClient(
          userSessionBean.getUserId(), userSessionBean.getPassword());
        
        Document document = new Document();
        String docTypeId = getProperty(DOCTYPEID_PROPERTY);
        if (docTypeId == null) docTypeId = "Document";
        document.setDocTypeId(docTypeId);
        
        String titlePattern = getProperty(LINKED_DOCUMENT_TITLE_PROPERTY);
        if (titlePattern == null) titlePattern = "${forum.name}";
        HashMap variables = new HashMap();
        variables.put("forum", forum);
        variables.put("question", currentQuestion);
        String title = WebTemplate.create(titlePattern).merge(variables);
        document.setTitle(title);
        
        Property property = new Property();
        property.setName("forumId");
        property.getValue().add(forum.getForumId());
        document.getProperty().add(property);
        
        InputStream is = uploadedFile.getInputStream();
        File tempFile = IOUtils.writeToFile(is);
        Content content = new Content();
        content.setLanguage(DocumentConstants.UNIVERSAL_LANGUAGE);
        content.setData(new DataHandler(new FileDataSource(tempFile)));
        MimeTypeMap mimeTypeMap = MimeTypeMap.getMimeTypeMap();
        String mimeType = mimeTypeMap.getContentType(uploadedFile.getName());
        content.setContentType(mimeType);
        document.setContent(content);
        document = docClient.storeDocument(document);
        String contentId = document.getContent().getContentId();
        String text = currentQuestion.getText();
        if (text == null) text = "";
        String docUrl = getContextURL() + "/documents/" + contentId;
        if (text.length() > 0 && !text.endsWith(" ")) text += " ";
        currentQuestion.setText(text + docUrl + " ");
        uploadedFile = null;
        tempFile.delete();
      }
      preserveScroll = true;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
 
  public String getQuestionHash()
  {
    if (questionHash == null)
    {
      questionHash = UUID.randomUUID().toString();
    }
    return questionHash;
  }
  
  public void setQuestionHash(String questionHash)
  {
    this.questionHash = questionHash;
  }
  
  public String getTextWithLinks()
  {
   String text = (String)getValue("#{row.question.text}");
   return text == null ? null : TextUtils.getTextWithLinks(text);
  }
  
  //Forum accesssors
  public ForumFilter getForumFilter()
  {
    return forumFilter;
  }

  public void setForumFilter(ForumFilter forumFilter)
  {
    this.forumFilter = forumFilter;
  }

  public String getForumIdFilter()
  {
    if (!forumFilter.getForumId().isEmpty())
      return forumFilter.getForumId().get(0);
    else
      return "";
  }

  public void setForumIdFilter(String forumId)
  {
    this.forumFilter.getForumId().clear();
    if (forumId != null && forumId.length() > 0)
      this.forumFilter.getForumId().add(forumId);
  }

  public int getQuestionMaxLength()
  {
    int maxLength = 4000;
    String value = getProperty(QUESTION_MAX_LENGTH_PROPERTY);
    if (value != null)
    {
      try
      {
        maxLength = Integer.parseInt(value);
      }
      catch (Exception ex)
      {
      }
    }
    return maxLength;
  }

  public DataModel getForumsData()
  {
    return forumsDataModel;
  }

  public void setForumsData(DataModel forumsData)
  {
    this.forumsDataModel = forumsData;
  }

  //Questions accesssors
  public QuestionFilter getQuestionFilter()
  {
    return questionFilter;
  }

  public void setQuestionFilter(QuestionFilter questionFilter)
  {
    this.questionFilter = questionFilter;
  }

  public DataModel getQuestionsData()
  {
    return questionsDataModel;
  }

  public void setQuestionsData(DataModel questionsData)
  {
    this.questionsDataModel = questionsData;
  }

  public ForumView getCurrentForumView()
  {
    return currentForumView;
  }

  public void setCurrentForumView(ForumView currentForumView)
  {
    this.currentForumView = currentForumView;
  }

  public Question getCurrentQuestion()
  {
    if (currentQuestion == null)
      currentQuestion = new Question();
    return currentQuestion;
  }

  public void setCurrentQuestion(Question currentQuestion)
  {
    this.currentQuestion = currentQuestion;
  }

  public List<Answer> getQuestionAnswers()
  {
    return questionAnswers;
  }

  public void setQuestionAnswers(List<Answer> questionAnswers)
  {
    this.questionAnswers = questionAnswers;
  }

  public Answer getCurrentAnswer()
  {
    return currentAnswer;
  }

  public void setCurrentAnswer(Answer currentAnswer)
  {
    this.currentAnswer = currentAnswer;
  }

  //Forums actions
  @CMSAction
  public String show()
  {
    String outcome = "forum_catalogue";
    currentQuestion = null;
    currentAnswer = null;
    questionAnswers = null;

    String forumId = getNodeProperty(FORUM_ID_PROPERTY);
    if (forumId != null)
      singleForum = true;
    else
    {
      singleForum = false;
      forumId = getRequestParameter(FORUM_ID_PROPERTY);
    }

    if (forumId != null)
    {
      //Local filtering is used to prevent the perpetuation of the forumId
      //during the subsequent navigation.
      ForumFilter filter = new ForumFilter();
      filter.getForumId().add(forumId);
      searchForums(filter);
      currentForumView = (ForumView)forumsDataModel.getRows().get(0);
      questionFilter.setForumId(forumId);
      refreshQuestions();
      outcome = "questions" + getOutcomeSuffix();
    }
    else
    {
      forumFilter.getForumId().clear();
//      String group = getParameter(FORUM_GROUP_PROPERTY);
//      if (group != null)
//        forumFilter.setGroup(group);
      searchForums(forumFilter);
    }

    return outcome;
  }

  public String searchForums()
  {
    return searchForums(forumFilter);
  }

  public String searchForums(ForumFilter filter)
  {
    setNodeFilters(filter);
    return search(forumsDataModel, new ForumsDataSource(filter));
  }

  public String createForum()
  {
    currentForumView = new ForumView();
    Forum forum = new Forum();
    currentForumView.setForum(forum);
    if (questionsDataModel.getRows() != null)
      questionsDataModel.getRows().clearCache();
    return "create_forum";
  }

  public String showForum()
  {
    currentForumView = (ForumView)getValue("#{row}");
    questionFilter.setForumId(currentForumView.getForum().getForumId());
    questionsDataModel.setFirstRowIndex(0);
    refreshQuestions();
    return "show_forum" + getOutcomeSuffix();
  }

  public String setupForum()
  {
    currentForumView = (ForumView)getValue("#{row}");
    return setupCurrentForum();
  }

  public String setupCurrentForum()
  {
    try
    {
      String forumId = currentForumView.getForum().getForumId();
      Forum forum = ForumConfigBean.getPort().loadForum(forumId);
      currentForumView.setForum(forum);
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    
    return "setup_forum";
  }

  public String storeForum()
  {
    try
    {
      Forum forum = currentForumView.getForum();
      boolean isCreation = forum.getForumId() == null;
      if (forum.getAdminRoleId() == null || forum.getAdminRoleId().length() == 0)
      {
        String adminRole = getNodeProperty(UPDATE_NODE_ROLEID_PROPERTY);
        forum.setAdminRoleId(adminRole);
      }
      if (forum.getGroup() == null || forum.getGroup().length() == 0)
      {
        String forumGroup = getNodeProperty(FORUM_GROUP_PROPERTY);
        if (forumGroup == null)
        {
          MenuItemCursor menuItem =
            UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
          forumGroup = menuItem.getMid();
        }
        forum.setGroup(forumGroup);
      }

      forum = ForumConfigBean.getPort().storeForum(forum);
      currentForumView.setForum(forum);
      if (isCreation)
      {
        questionFilter.setForumId(forum.getForumId());
        refreshQuestions();
      }
      refreshCurrentForum();
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "store" + getOutcomeSuffix();
  }

  public String removeForum()
  {
    try
    {
      Forum forum = currentForumView.getForum();
      ForumConfigBean.getPort().removeForum(forum.getForumId());
      searchForums(forumFilter);
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "remove";
  }

  public String cancelForum()
  {
    if (currentForumView == null ||
        currentForumView.getForum().getForumId() == null)
      return "forum_catalogue";
    else
      return "cancel" + getOutcomeSuffix();
  }

  public void refreshCurrentForum()
  {
    String forumId = currentForumView.getForum().getForumId();
    if (forumId != null)
    {
      ForumFilter filter = new ForumFilter();
      filter.getForumId().add(forumId);
      DataModel data =
        new DataModel(FORUMS_PAGE_SIZE_PROPERTY, FORUMS_PAGE_SIZE, CACHE_SIZE);
      search(data, new ForumsDataSource(filter));

      currentForumView = (ForumView)data.getRows().get(0);
    }
  }

  public String showForumHits()
  {
    return "forum_hits";
  }

  public String cancelForumHits()
  {
    return "cancel" + getOutcomeSuffix();
  }

  //Questions actions
  public String searchQuestions()
  {
    refreshCurrentForum();
    return refreshQuestions();
  }

  public String refreshQuestions()
  {
    ForumConfigBean.registerUserHit(currentForumView.getForum().getForumId());
    if (!isInterviewType())
      questionFilter.setOrderBy(null);
    else if (isInterviewType() && questionFilter.getOrderBy() == null)
      questionFilter.setOrderBy(OrderBy.OUTPUTINDEX);
    return search(questionsDataModel, new QuestionsDataSource(questionFilter));
  }

  public QuestionView refreshQuestion(String questionId)
    throws Exception
  {
    QuestionView qv = null;
    QuestionFilter filter = new QuestionFilter();
    filter.setForumId(currentForumView.getForum().getForumId());
    filter.getQuestionId().add(questionId);
    
    List<QuestionView> views =
      ForumConfigBean.getPort().findQuestionViews(filter);
    if (views != null)
      qv = views.get(0);

    return qv;
  }
  
  public String refreshCurrentQuestion()
  {
    try
    {
      QuestionView qv = refreshQuestion(currentQuestion.getQuestionId());
      questionAnswers = qv.getAnswer();
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return null;
  }  

  public String showQuestion()
  {
    try
    {
      QuestionView qv = (QuestionView) getValue("#{row}");
      currentQuestion = ForumConfigBean.getPort().
        readQuestion(qv.getQuestion().getQuestionId());
      qv.setQuestion(currentQuestion);
      questionAnswers = qv.getAnswer();
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "show_question";
  }

  public String editQuestion()
  {
    try
    {
      QuestionView qv = (QuestionView) getValue("#{row}");
      qv = refreshQuestion(qv.getQuestion().getQuestionId());
      if (qv != null)
      currentQuestion = qv.getQuestion();
      questionAnswers = qv.getAnswer();
      if (currentQuestion != null && isInterviewType())
      {
        currentAnswer = getFirstAnswer(qv);
        String awaitingText =
          getLabel(AWAITING_RESPONSE_LABEL_PROPERTY, DEFAULT_AWAITING_RESPONSE_LABEL);
        if (awaitingText.equals(currentAnswer.getText()))
          currentAnswer.setText(null);
      }
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "edit_question";
  }

  public String createQuestion()
  {
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();

      if (!isEditorUser()) // not administrator
      {
        refreshCurrentForum();

        //Check if has pendent questions
        if (isUserAwaitingResponse())
          throw new Exception(getLabel(PENDING_QUESTIONS_LABEL_PROPERTY, "PENDING_QUESTIONS_FOUND"));

        // check empty questions
        String text = currentQuestion.getText();
        String title = currentQuestion.getTitle();
        if (text == null || text.trim().length() == 0)
        {
          if (isInterviewType() ||
            (!isInterviewType() && (title == null || title.trim().length() == 0)))
          {
            throw new Exception("NULL_QUESTION_FORBIDDEN");
          }
        }
      }
      
      if (questionHash != null && 
          !questionHash.equals(userSessionBean.getAttribute("questionHash")))
      {
        currentQuestion.setForumId(currentForumView.getForum().getForumId());
        if (!isCensoredInterviewType())
          currentQuestion.setVisible(true);
        ForumConfigBean.getPort().storeQuestion(currentQuestion);

        userSessionBean.setAttribute("questionHash", questionHash);      
      }
      currentQuestion = null;
      questionHash = null;

      if (OrderBy.OUTPUTINDEX.equals(questionFilter.getOrderBy()))
        questionsDataModel.setFirstRowIndex(10000);
      searchQuestions(); //needs to update currentForumView status    
    }
    catch(Exception ex)
    {
      error(ex);
      return null;
    }
    return "store_question";
  }

  public String removeQuestion()
  {
    try
    {
      QuestionView qv = (QuestionView) getValue("#{row}");      
      ForumConfigBean.getPort().
        removeQuestion(qv.getQuestion().getQuestionId());
      searchQuestions();
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "remove_question";
  }

  public String setSelectedQuestionVisible()
  {
    try
    {
      QuestionView qv = (QuestionView) getValue("#{row}");
      setQuestionVisible(qv.getQuestion());
      refreshQuestions();
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return null;
  }

  public String cancelQuestion()
  {
    currentQuestion = null;
    currentAnswer = null;
    return "cancel";
  }

  public Answer getFirstAnswer()
  {
    QuestionView qv = (QuestionView)getValue("#{row}");
    Answer answer = getFirstAnswer(qv);

    if (answer.getText() == null)
    {
      String text =
        getLabel(AWAITING_RESPONSE_LABEL_PROPERTY, DEFAULT_AWAITING_RESPONSE_LABEL);
      answer.setText(text);
    }

    return answer;
  }

  public Answer getFirstAnswer(QuestionView questionView)
  {
    Answer answer = null;

    List<Answer> answers = questionView.getAnswer();
    if (answers != null && !answers.isEmpty())
      answer = answers.get(0);
    else
    {
      answer = new Answer();
      answer.setAnswerId(null);
      answer.setQuestionId(questionView.getQuestion().getQuestionId());
      answer.setCreationUserId(UserSessionBean.getCurrentInstance().getUserId());
    }
    
    return answer;
  }

  public String getFirstAnswerUserId()
  {
    String alias = getNodeProperty(ANSWERER_ALIAS_PROPERTY);
    if (alias != null)
      return alias;
    else
      return getFirstAnswer().getCreationUserId();
  }

  public String getQuestionStatus()
  {
    String status = "status";
    QuestionView qv = (QuestionView)getValue("#{row}");
    if (qv.getQuestion() != null)
    {
      Question question = qv.getQuestion();
      status = status + (qv.getAnswer().size() > 0 ? "Answered" : "Pending");
      status = status + (question.isVisible() ? "Visible" : "Invisible");
    }
    return status;
  }

  public Date getCurrentQuestionDateTime()
  {
    if (currentQuestion != null)
      return toDate(currentQuestion.getCreationDateTime());
    else
      return null;
  }

  public Date getQuestionDateTime()
  {
    QuestionView qv = (QuestionView)getValue("#{row}");
    if (qv.getQuestion() != null)
      return toDate(qv.getQuestion().getCreationDateTime());
    else
      return null;
  }

  public Date getForumStartDateTime()
  {
    if (currentForumView != null)
      return toDate(currentForumView.getForum().getStartDateTime());
    else
      return null;
  }

  public Date getForumEndDateTime()
  {
    if (currentForumView != null)
      return toDate(currentForumView.getForum().getEndDateTime());
    else
      return null;
  }

  public int getForumHits()
  {
    return ForumConfigBean.getForumHits(
      currentForumView.getForum().getForumId());
  }

  public List<UserHit> getForumHitList()
  {
    return ForumConfigBean.getForumHitList(
      currentForumView.getForum().getForumId());
  }

  public boolean isUserConnected()
  {
    QuestionView qv = (QuestionView)getValue("#{row}");
    if (qv == null)
      return false;
    String userId = qv.getQuestion().getCreationUserId();
    String forumId = currentForumView.getForum().getForumId();
    return ForumConfigBean.isUserConnected(userId, forumId);
  }

  //Answer actions
  public String editAnswer()
  {
    currentAnswer = (Answer)getValue("#{row}");
    if (currentAnswer == null)
    {
      currentAnswer = new Answer();
      currentAnswer.setQuestionId(currentQuestion.getQuestionId());
    }
    return "edit_answer";
  }

  public String storeNormalAnswer()
  {
    try
    {
      String answerId = currentAnswer.getAnswerId();
      currentAnswer = 
        ForumConfigBean.getPort().storeAnswer(currentAnswer);
      //If new answer add to list
      if (answerId == null)
        questionAnswers.add(currentAnswer);
      currentAnswer = null;
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "store_answer";
  }

  public String storeInterviewAnswer()
  {
    try
    {
      if (currentAnswer != null)
      {
        ForumConfigBean.getPort().storeAnswer(refreshAnswer(currentAnswer));
        currentAnswer = null;
      }
      currentQuestion = null;
      searchQuestions();
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "store_answer";
  }

  public String setQuestionVisible()
  {
    try
    {
      if (currentAnswer != null)
      {
        currentAnswer = refreshAnswer(currentAnswer);
        if (currentAnswer.getText() != null &&
            currentAnswer.getText().trim().length() > 0)
          ForumConfigBean.getPort().storeAnswer(currentAnswer);
        currentAnswer = null;
      }
      setQuestionVisible(currentQuestion);
      currentQuestion = null;
      searchQuestions();
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "store_answer";
  }

  public String removeAnswer()
  {
    try
    {
      Answer answer = (Answer)getValue("#{row}");
      ForumConfigBean.getPort().removeAnswer(answer.getAnswerId());
      questionAnswers.remove(answer);
      currentAnswer = null;
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
    return "remove";
  }

  public String cancelAnswer()
  {
    currentAnswer = null;
    return null;
  }

  public int getAnswerCount()
  {
    QuestionView qv = (QuestionView)getValue("#{row}");
    if (qv != null)
      return qv.getAnswer().size();
    else
      return 0;
  }

  public Date getAnswerDateTime()
  {
    Answer answer = (Answer)getValue("#{row}");
    return getAnswerDateTime(answer);
  }

  private Date getAnswerDateTime(Answer answer)
  {
    if (answer != null)
      return TextUtils.parseInternalDate(answer.getCreationDateTime());
    else
      return null;
  }
  
  //Renderers & Labels
  public String getParticipateButtonLabel()
  {
    return getLabel(PARTICIPATE_LABEL_PROPERTY, DEFAULT_PARTICIPATE_LABEL);
  }

  public String getReadButtonLabel()
  {
    return getLabel(READ_LABEL_PROPERTY, DEFAULT_READ_LABEL);
  }

  public String getSetupButtonLabel()
  {
    return getLabel(SETUP_LABEL_PROPERTY, DEFAULT_SETUP_LABEL);
  }

  public String getAskQuestionLabel()
  {
    return getLabel(ASK_QUESTION_LABEL_PROPERTY, DEFAULT_ASK_QUESTION_LABEL);
  }

  public String getQuestionLabel()
  {
    return getLabel(QUESTION_LABEL_PROPERTY, DEFAULT_QUESTION_LABEL);
  }

  public String getOtherForumsLabel()
  {
    return getLabel(OTHER_FORUMS_LABEL_PROPERTY, DEFAULT_OTHER_FORUMS_LABEL);
  }

  public String getOpenStatusLabel()
  {
    if (isUserAwaitingResponse())
      return getLabel(QUESTION_SUBMITTED_LABEL_PROPERTY,
        DEFAULT_QUESTION_SUBMITTED_LABEL);
    else
      return getLabel(OPEN_STATUS_LABEL_PROPERTY, ForumStatus.OPEN.toString());
  }

  public String getOpenBeforeStatusLabel()
  {
    if (isUserAwaitingResponse())
      return getLabel(QUESTION_SUBMITTED_LABEL_PROPERTY,
        DEFAULT_QUESTION_SUBMITTED_LABEL);
    else
      return getLabel(OPEN_BEFORE_STATUS_LABEL_PROPERTY,
        ForumStatus.OPEN_BEFORE.toString());
  }

  public String getClosedStatusLabel()
  {
    return getLabel(CLOSED_STATUS_LABEL_PROPERTY,
      ForumStatus.CLOSED.toString());
  }

  public String getClosedBeforeStatusLabel()
  {
    return getLabel(CLOSED_BEFORE_STATUS_LABEL_PROPERTY,
      ForumStatus.CLOSED_BEFORE.toString());
  }

  public String getForumNameLabel()
  {
    return getLabel(FORUM_NAME_LABEL_PROPERTY, "");
  }

  public String getStatusLabel()
  {
    switch (currentForumView.getStatus())
    {
      case OPEN:
        return getOpenStatusLabel();
      case OPEN_BEFORE:
        return getOpenBeforeStatusLabel();
      case CLOSED:
        return getClosedStatusLabel();
      case CLOSED_BEFORE:
        return getClosedBeforeStatusLabel();
    }
    return null;
  }

  public String getRefreshTime()
  {
    return getLabel(REFRESH_TIME_PROPERTY, DEFAULT_REFRESH_TIME);
  }

  public boolean isParticipantUser() // user that can make questions
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    List<String> roles = userSessionBean.getMenuModel().getSelectedMenuItem().
      getMultiValuedProperty("roles.participant");
    if (roles == null || roles.isEmpty()) return true;
    
    return userSessionBean.isUserInRole(roles);
  }
  
  public boolean isEditorUser() // administrator
  {
    //Module admin
    if (isAdminUser()) return true;
    Set userRoles = UserSessionBean.getCurrentInstance().getRoles();
    //Node admin
    if (userRoles.contains(getNodeProperty(UPDATE_NODE_ROLEID_PROPERTY)))
      return true;
    //Forum admin
    if (currentForumView == null)
      return false;
    String adminRole = currentForumView.getForum().getAdminRoleId();
    return userRoles.contains(adminRole);
  }

  public boolean isAdminUser() // global forum administator
  {
    Set userRoles = UserSessionBean.getCurrentInstance().getRoles();

    if (userRoles == null)
      return false;
    if (userRoles.contains(ForumConstants.FORUM_ADMIN_ROLE))
      return true;

    return false;
  }

  public boolean isRenderQuestionUser()
  {
    QuestionView qv = (QuestionView)getValue("#{row}");
    if (qv == null)
      return false;
    return qv.getQuestion().getText() != null
      && qv.getQuestion().getText().trim().length() > 0;
  }

  public boolean isRenderForumTypeIcon()
  {
    String render = (String)getNodeProperty(RENDER_FORUM_TYPE_ICON_PROPERTY);
    return !"false".equalsIgnoreCase(render);
  }

  /**
   *
   * @return true if cms node is configured to show only one unique forum.
   */
  public boolean isSingleForum()
  {
    return singleForum;
  }

  /**
   * @return true if the current forum is not active or it has reached
   * the limit of possible questions to be asked.
   */
  public boolean isReadOnly()
  {
    if (currentForumView != null)
      return (ForumStatus.CLOSED.equals(currentForumView.getStatus())
      || ForumStatus.CLOSED_BEFORE.equals(currentForumView.getStatus()));
    else
      return false;
  }

  public boolean isInterviewType()
  {
    if (currentForumView != null)
    {
      ForumType type = currentForumView.getForum().getType();
      return ForumType.INTERVIEW.equals(type) ||
             ForumType.UNCENSORED_INTERVIEW.equals(type);
    }
    else
      return false;
  }

  public boolean isCensoredInterviewType()
  {
    if (currentForumView != null)
    {
      ForumType type = currentForumView.getForum().getType();
      return ForumType.INTERVIEW.equals(type);
    }
    else
      return false;
  }

  public boolean isUncensoredInterviewType()
  {
    if (currentForumView != null)
    {
      ForumType type = currentForumView.getForum().getType();
      return ForumType.UNCENSORED_INTERVIEW.equals(type);
    }
    else
      return false;
  }

  public boolean isAnswerCreator()
  {
    Answer answer = (Answer)getValue("#{row}");
    if (answer != null)
    {
      return UserSessionBean.getCurrentInstance().getUserId()
        .equals(answer.getCreationUserId());
    }

    return false;
  }

  public boolean isInputIndexOrderBy()
  {
    if (questionFilter == null)
      return false;
    if (OrderBy.INPUTINDEX.equals(questionFilter.getOrderBy()))
      return true;
    else
      return false;
  }

  public boolean isUserAwaitingResponse()
  {
    return currentForumView.getPendentQuestionCount() > 0;
  }

  //SelectItems
  public SelectItem[] getForumTypeItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.forum.web.resources.ForumBundle", getLocale());
    return FacesUtils.getEnumSelectItems(ForumType.class, bundle);
  }

  public SelectItem[] getShowAnsweredItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.forum.web.resources.ForumBundle", getLocale());
    return FacesUtils.getEnumSelectItems(ShowAnswered.class, bundle);
  }

  public SelectItem[] getShowVisibleItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.forum.web.resources.ForumBundle", getLocale());
    return FacesUtils.getEnumSelectItems(ShowVisible.class, bundle);
  }

  public SelectItem[] getOrderByItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.forum.web.resources.ForumBundle", getLocale());
    return FacesUtils.getEnumSelectItems(OrderBy.class, bundle);
  }

  public void processQuestionFilterValueChange(ValueChangeEvent vce)
    throws AbortProcessingException
  {
      questionsDataModel.setFirstRowIndex(0);
  }

  public void processForumFilterValueChange(ValueChangeEvent vce)
    throws AbortProcessingException
  {
    forumsDataModel.setFirstRowIndex(0);
  }
  
  //Private methods
  private String search(DataModel dataModel, DataSource source)
  {
    dataModel.fillData(source);
    return null;
  }

  private Question setQuestionVisible(Question question) throws Exception
  {
    question.setVisible(true);
    return ForumConfigBean.getPort().storeQuestion(question);
  }

  private Answer refreshAnswer(Answer answer) throws Exception
  {
    if (answer.getAnswerId() == null)
    {
      QuestionView qv = refreshQuestion(currentQuestion.getQuestionId());
      Answer firstAnswer = getFirstAnswer(qv);
      if (firstAnswer.getAnswerId() != null)
        answer.setAnswerId(firstAnswer.getAnswerId());
    }

    return answer;
  }

  private void setNodeFilters(ForumFilter filter)
  {
    //Forum Group
    String group = getParameter(FORUM_GROUP_PROPERTY);
    if (group != null)
      filter.setGroup(group);
  }

  private String getLabel(String propertyName, String defaultLabel)
  {
    String label = getNodeProperty(propertyName);
    if (label == null)
      label = defaultLabel;
    return label;
  }

  //Get property from node definition or if not exists then try to get
  //request parameter.
  private String getParameter(String paramName)
  {
    String paramValue = getNodeProperty(paramName);
    if (paramValue == null)
      paramValue = getRequestParameter(paramName);

    return paramValue;
  }

  private String getNodeProperty(String name)
  {
    MenuItemCursor menuItem = 
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(name);
  }

  private String getRequestParameter(String name)
  {
    Map requestParameters = getExternalContext().getRequestParameterMap();
    return (String)requestParameters.get(name.toLowerCase());
  }
  
  private Date toDate(String dateTime)
  {
    if (dateTime != null)
      return TextUtils.parseInternalDate(dateTime);
    else
      return null;
  }

  public String getOutcomeSuffix()
  {
    if (isInterviewType())
      return "_interview";
    else
      return "_normal";
  }

  public class DataModel implements Serializable
  {
    private String pageSizeProperty;
    private int defaultPageSize;
    private int defaultCacheSize;
    private int firstRowIndex;
    private BigList rows;

    public DataModel(String pageSizeProperty, int defaultPageSize,
      int defaultCacheSize)
    {
      this.pageSizeProperty = pageSizeProperty;
      this.defaultPageSize = defaultPageSize;
      this.defaultCacheSize = defaultCacheSize;
    }

    public int getFirstRowIndex()
    {
      int size = getRowCount();
      if (size == 0)
      {
        firstRowIndex = 0;
      }
      else if (firstRowIndex >= size)
      {
        int pageSize = getPageSize();
        firstRowIndex = pageSize * ((size - 1) / pageSize);
      }
      return firstRowIndex;
    }

    public void setFirstRowIndex(int firstRowIndex)
    {
      this.firstRowIndex = firstRowIndex;
    }

    public int getPageSize()
    {
      String pageSize = getNodeProperty(pageSizeProperty);
      if (pageSize != null)
        return Integer.valueOf(pageSize).intValue();
      else
        return defaultPageSize;
    }

    public int getCacheSize()
    {
      if (getPageSize() != defaultPageSize)
        return getPageSize() + 5;
      else
        return defaultCacheSize;
    }

    public int getRowCount()
    {
      return this.rows == null ? 0 : rows.size();
    }

    public BigList getRows()
    {
      return this.rows;
    }

    public void fillData(final DataSource source)
    {
      this.rows = new BigList(getCacheSize(), getPageSize())
      {
        public int getElementCount()
        {
          return source.countResults();
        }

        public List getElements(int firstResult, int maxResults)
        {
          return source.getResults(firstResult, maxResults);
        }
      };
    }
  }

  public abstract class DataSource implements Serializable
  {
    public abstract int countResults();
    public abstract List getResults(int firstResult, int maxResults);
  }

  public class ForumsDataSource extends DataSource
  {
    private ForumFilter filter;

    public ForumsDataSource(ForumFilter filter)
    {
      this.filter = filter;
    }
    public int countResults()
    {
      try
      {
        int count = ForumConfigBean.getPort().countForums(filter);
        return count;
      }
      catch (Exception ex)
      {
        error("SEARCH_ERROR");
      }
      return 0;
    }

    public List getResults(int firstResult, int maxResults)
    {
      try
      {
        filter.setFirstResult(firstResult);
        filter.setMaxResults(maxResults);
        List result = 
          ForumConfigBean.getPort().findForumViews(filter);
        return result;
      }
      catch (Exception ex)
      {
        error("SEARCH_ERROR");
      }
      return null;
    }
  }

  public class QuestionsDataSource extends DataSource
  {
    private QuestionFilter filter;

    public QuestionsDataSource(QuestionFilter filter)
    {
      this.filter = filter;
    }

    @Override
    public int countResults()
    {
      try
      {
        int count = 
          ForumConfigBean.getPort().countQuestionViews(filter);
        return count;
      }
      catch (Exception ex)
      {
        error("SEARCH_ERROR");
      }
      return 0;
    }

    @Override
    public List getResults(int firstResult, int maxResults)
    {
      try
      {
        filter.setFirstResult(firstResult);
        filter.setMaxResults(maxResults);
        List result = 
          ForumConfigBean.getPort().findQuestionViews(filter);
        return result;
      }
      catch (Exception ex)
      {
        error("SEARCH_ERROR");
      }
      return null;
    }


  }
}
