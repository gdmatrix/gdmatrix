package org.santfeliu.survey.web;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.survey.Answer;
import org.matrix.survey.AnswerList;
import org.matrix.survey.Survey;
import org.matrix.survey.SurveyManagerPort;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

@CMSManagedBean
public class SurveyBean extends WebBean implements Serializable
{
  @CMSProperty
  public static final String PARAM_SURVID = "survey.survid";
  @CMSProperty
  public static final String PARAM_BAR_WIDTH = "survey.barWidth";
  @CMSProperty
  public static final String PARAM_BAR_HEIGHT = "survey.barHeight";
  @CMSProperty
  public static final String PARAM_BAR_IMAGE = "survey.barImage";
  @CMSProperty
  public static final String PARAM_REMAIN_IMAGE = "survey.remainImage";

  private Survey survey;
  private List<SelectItem> answerItemList;
  private String answerSelected;
  private boolean viewResults;
    
  public SurveyBean()
  {
    viewResults = false;
  }

// SET & GET METHODS

  public Survey getSurvey()
  {
    return survey;
  }

  public void setSurvey(Survey survey)
  {
    this.survey = survey;
  }

  public List<SelectItem> getAnswerItemList()
  {
    if (answerItemList == null)
    {
      loadAnswerItemList();
    }
    return answerItemList;
  }

  public void setAnswerItemList(List<SelectItem> answerItemList)
  {
    this.answerItemList = answerItemList;
  }

  public String getAnswerSelected()
  {
    return answerSelected;
  }

  public void setAnswerSelected(String answerSelected)
  {
    this.answerSelected = answerSelected;
  }

  public boolean isViewResults()
  {
    return viewResults;
  }

  public void setViewResults(boolean viewResults)
  {
    this.viewResults = viewResults;
  }

  @CMSAction
  public String show()
  {
    load();
    return "survey_index";
  }

  public String vote()
  {
    try
    {
      SurveyManagerPort port = SurveyConfigBean.getPort();
      port.voteSurvey(survey.getSurveyId(), answerSelected);
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return view();
  }

  public String view()
  {
    viewResults = true;    
    return null;
  }

  public String back() 
  {
    viewResults = false;
    return null;
  }

  public List<Answer> getAnswerList()
  {
    List<Answer> emptyList = new ArrayList<Answer>();
    if (survey != null)
    {
      if (survey.getAnswerList() != null)
      {
        return survey.getAnswerList().getAnswerList();
      }
    }
    return emptyList;
  }
  
  private void load()
  {
    try
    {
      SurveyManagerPort port = SurveyConfigBean.getPort();
      survey = port.loadSurvey(getSurvIdProperty());
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private void loadAnswerItemList()
  {
    answerItemList = new ArrayList();
    AnswerList answerList = survey.getAnswerList();
    for (Answer answer : answerList.getAnswerList())
    {
      String value = answer.getAnswerId();
      String label = answer.getText();
      answerItemList.add(new SelectItem(value, label));
    }
    answerSelected = "0";
  }

  private String getSurvIdProperty()
  {
    return getProperty(PARAM_SURVID, null);
  }

  public String getBarImage()
  {
    return getProperty(PARAM_BAR_IMAGE, null);
  }

  public String getRemainImage()
  {
    return getProperty(PARAM_REMAIN_IMAGE, null);
  }

  public int getBarWidth()
  {
    return Integer.valueOf(getProperty(PARAM_BAR_WIDTH, "400"));
  }

  public int getBarHeight()
  {
    return Integer.valueOf(getProperty(PARAM_BAR_HEIGHT, "20"));
  }

  public int getAnswerWidth()
  {
    float percent = calcAnswerPercent();
    float totalWidth = (float)getBarWidth();
    return Math.round(percent * totalWidth);
  }

  public int getRemainWidth()
  {
    return getBarWidth() - getAnswerWidth();
  }

  public String getAnswerPercent()
  {
    float f = calcAnswerPercent() * 100;
    DecimalFormat dec = new DecimalFormat("###.##");
    return dec.format(f);
  }

  private float calcAnswerPercent()
  {
    Answer answer = (Answer)getValue("#{answer}");
    int answerVotes = answer.getVoteCount();
    int totalVotes = survey.getVoteCount();
    if (totalVotes == 0) return 0.0f;
    return (float)answerVotes / (float)totalVotes;
  }

  private String getProperty(String propertyName, String defaultValue)
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String value = (String)mic.getProperties().get(propertyName);
    return (value != null ? value : defaultValue);
  }

  public static void main(String[] args)
  {
    float f = 0.156346457f;
    f = f * 100;
    DecimalFormat dec = new DecimalFormat("###.##");
    System.out.println(dec.format(f));
  }
  
}
