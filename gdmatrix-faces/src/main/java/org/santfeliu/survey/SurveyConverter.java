package org.santfeliu.survey;

import java.util.HashMap;
import java.util.Map;

import org.matrix.survey.SurveyConstants;
import org.matrix.survey.AnswerList;
import org.matrix.survey.Survey;

import org.santfeliu.util.Table;

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
