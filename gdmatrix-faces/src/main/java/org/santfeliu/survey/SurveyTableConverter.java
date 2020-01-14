package org.santfeliu.survey;

import java.util.List;
import org.matrix.survey.SurveyConstants;
import org.matrix.survey.SurveyView;
import org.matrix.survey.SurveyTable;
import org.santfeliu.util.Table;

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
      surveyRow.setVoteCount(new Integer(votes));      
      surveyTable.getSurveyViewList().add(surveyRow);
    }   
    return surveyTable;
  }

}
