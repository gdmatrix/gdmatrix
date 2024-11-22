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
import org.matrix.survey.SurveyView;
import org.matrix.survey.SurveyTable;
import org.santfeliu.util.Table;

/**
 *
 * @author unknown
 */
public class SurveyTableConverter
{
  public SurveyTableConverter()
  {
  }

  public static Table toTable(SurveyTable surveyTable)
  {
    Table tableSurveys = new Table(new String[]{
      SurveyConstants.SURVID,
      SurveyConstants.TEXT,
      SurveyConstants.OPEN,
      SurveyConstants.STARTDATE,
      SurveyConstants.VOTES
    });
    List<SurveyView> surveyRows = surveyTable.getSurveyViewList();
    for (SurveyView surveyRow : surveyRows)
    {
      String surveyId = surveyRow.getSurveyId();
      String text = surveyRow.getText();
      String open = (surveyRow.isOpen() ? "Y" : "N");
      String startDate = surveyRow.getStartDay();
      String votes = String.valueOf(surveyRow.getVoteCount());
      tableSurveys.addRow(
        surveyId,
        text,
        open,
        startDate,
        votes);
    }
    return tableSurveys;
  }

  public static SurveyTable toSurveyTable(Table table) throws Exception
  {
    SurveyTable surveyTable = new SurveyTable();
    for (int i = 0; i < table.getRowCount(); i++)
    {
      String surveyId = String.valueOf(table.getElementAt(i, SurveyConstants.SURVID));
      String text = (String)(table.getElementAt(i, SurveyConstants.TEXT));
      String open = (String)(table.getElementAt(i, SurveyConstants.OPEN));
      String startDay = (String)(table.getElementAt(i, SurveyConstants.STARTDATE));
      String votes = String.valueOf(table.getElementAt(i, SurveyConstants.VOTES));
      SurveyView surveyRow = new SurveyView();
      surveyRow.setSurveyId(surveyId);
      surveyRow.setText(text);
      surveyRow.setOpen("Y".equalsIgnoreCase(open));
      surveyRow.setStartDay(startDay);
      surveyRow.setVoteCount(Integer.valueOf(votes));
      surveyTable.getSurveyViewList().add(surveyRow);
    }
    return surveyTable;
  }

}
