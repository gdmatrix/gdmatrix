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

/**
 *
 * @author unknown
 */
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
