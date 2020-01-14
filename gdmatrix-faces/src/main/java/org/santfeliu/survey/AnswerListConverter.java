package org.santfeliu.survey;

import java.util.List;
import org.matrix.survey.SurveyConstants;
import org.matrix.survey.Answer;
import org.matrix.survey.AnswerList;

import org.santfeliu.util.Table;

public class AnswerListConverter
{
  public AnswerListConverter()
  {
  }
  
  public static Table toTable(AnswerList answerList)
  {
    Table tableAnswers = new Table(new String[]{
      SurveyConstants.ANSWERID, 
      SurveyConstants.TEXT, 
      SurveyConstants.VOTES
    });
    List<Answer> answers = answerList.getAnswerList();
    for (Answer answer : answers)
    {
      String answerId = answer.getAnswerId();
      String answerText = answer.getText();
      String answerVotes = String.valueOf(answer.getVoteCount());
      tableAnswers.addRow(answerId, answerText, answerVotes);
    }
    return tableAnswers;
  }
  
  public static AnswerList toAnswerList(Table table) throws Exception
  {
    AnswerList answerList = new AnswerList();
    for (int i = 0; i < table.getRowCount(); i++) 
    {
      String answerId = String.valueOf(table.getElementAt(i, SurveyConstants.ANSWERID));
      String answerText = (String)(table.getElementAt(i, SurveyConstants.TEXT));          
      String answerVotes = String.valueOf(table.getElementAt(i, SurveyConstants.VOTES));
      Answer answer = new Answer();
      answer.setAnswerId(answerId);
      answer.setText(answerText);
      answer.setVoteCount(new Integer(answerVotes));
      answerList.getAnswerList().add(answer);
    }
    return answerList;
  }
  
  
}
