package org.santfeliu.survey.web;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.html.HtmlDataTable;

import org.matrix.survey.SurveyManagerPort;

import org.santfeliu.survey.SurveyTableConverter;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;

@CMSManagedBean
public class AdminSurveyBean extends WebBean
{
  private HtmlDataTable surveysDataTable;
  private HtmlDataTable newAnswersDataTable;
  private String inputSurveyText;
  private boolean renderCreationPanel;

  public AdminSurveyBean() throws Exception
  {
  }

  public String newSurvey()
  {
    renderCreationPanel = true;    
    return null;
  }

  public void setSurveysDataTable(HtmlDataTable surveysDataTable)
  {
    this.surveysDataTable = surveysDataTable;
  }

  public HtmlDataTable getSurveysDataTable() throws Exception
  {
    if (surveysDataTable == null) surveysDataTable = new HtmlDataTable();    
    reloadSurveysList();
    return surveysDataTable;
  }

  public void setNewAnswersDataTable(HtmlDataTable newAnswersDataTable)
  {
    this.newAnswersDataTable = newAnswersDataTable;
  }

  public HtmlDataTable getNewAnswersDataTable()
  {
    if (newAnswersDataTable == null) 
    {
      newAnswersDataTable = new HtmlDataTable();
      newAnswersDataTable.setValue(new ArrayList());
    }
    return newAnswersDataTable;
  }

  public String openSurvey()
  {
    try
    {
      Map rowData = (Map)surveysDataTable.getRowData();
      String survid = String.valueOf(rowData.get("survid"));
      SurveyManagerPort port = SurveyConfigBean.getPort();
      port.openSurvey(survid);
      reloadSurveysList();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String closeSurvey()
  {
    try
    {
      SurveyManagerPort port = SurveyConfigBean.getPort();
      Map rowData = (Map)surveysDataTable.getRowData();
      String survid = String.valueOf(rowData.get("survid"));
      port.closeSurvey(survid);
      reloadSurveysList();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  private void reloadSurveysList() throws Exception
  {
    SurveyManagerPort port = SurveyConfigBean.getPort();
    surveysDataTable.setValue(SurveyTableConverter.toTable(port.findSurveys()));
  }

  @CMSAction
  public String showSurveys() 
  {
    return "survey_admin";
  }

  public String saveNewSurvey()
  {
    try
    {
      SurveyManagerPort port = SurveyConfigBean.getPort();

      int numAnswers = newAnswersDataTable.getRowCount();
      List<String> answers = new ArrayList<String>();
      for (int i = 0; i < numAnswers; i++)
      {
        String answerText = (String)(((Map)(((ArrayList)newAnswersDataTable.
          getValue()).get(i))).get("text"));
        if ((answerText != null) && (answerText.length() > 0))
          answers.add(answerText);
      }
      port.storeSurvey(inputSurveyText, answers);
      surveysDataTable.setValue(SurveyTableConverter.toTable(
        port.findSurveys()));
      renderCreationPanel = false;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelNewSurvey() 
  {
    renderCreationPanel = false;
    this.inputSurveyText = "";
    this.newAnswersDataTable.setValue(new ArrayList());
    return null;
  }
  
  public String addNewAnswer() 
  {
    HashMap mapAnswer = new HashMap();
    mapAnswer.put("text", "");
    ((ArrayList)newAnswersDataTable.getValue()).add(mapAnswer);
    return null;
  }
  
  public String removeNewAnswer() 
  {
    int i = newAnswersDataTable.getRowIndex();
    ((ArrayList)newAnswersDataTable.getValue()).remove(i);
    return null;
  }

  public void setInputSurveyText(String inputSurveyText)
  {
    this.inputSurveyText = inputSurveyText;
  }

  public String getInputSurveyText()
  {
    return inputSurveyText;
  }

  public void setRenderCreationPanel(boolean renderCreationPanel)
  {
    this.renderCreationPanel = renderCreationPanel;
  }

  public boolean isRenderCreationPanel()
  {
    return renderCreationPanel;
  }
}
