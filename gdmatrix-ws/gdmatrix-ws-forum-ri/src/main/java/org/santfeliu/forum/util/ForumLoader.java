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
package org.santfeliu.forum.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.matrix.forum.Answer;
import org.matrix.forum.Forum;
import org.matrix.forum.ForumType;
import org.matrix.forum.Question;

/**
 *
 * @author realor
 */
public class ForumLoader
{
  public class DBParameters
  {
    public String driver;
    public String url;
    public String user;
    public String password;
  }

  public DBParameters sourceDB = new DBParameters();
  public DBParameters targetDB = new DBParameters();

  private String oldForumId;
  private String oldQuestionId;

  private String newForumId;
  private String newQuestionId;
  private int inputIndex;
  private int outputIndex;

  public static void main(String[] args)
  {
    try
    {
      ForumLoader loader = new ForumLoader();
      loader.sourceDB.driver = "oracle.jdbc.driver.OracleDriver";
      loader.sourceDB.url = "jdbc:oracle:thin:@******:******:******";
      loader.sourceDB.user = "******";
      loader.sourceDB.password = "******";

      loader.targetDB.driver = "oracle.jdbc.driver.OracleDriver";
      loader.targetDB.url = "jdbc:oracle:thin:@******:******:******";
      loader.targetDB.user = "******";
      loader.targetDB.password = "******";

      InputStream is = ForumLoader.class.getResourceAsStream("loader3.sql");
      
      loader.load(is);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void load(InputStream queryStream) throws Exception
  {
    // read query stream
    String query = readQuery(queryStream);
    System.out.println("Query: " + query);
    
    // start loading
    Class.forName(sourceDB.driver);
    Connection sourceConn = DriverManager.getConnection(
      sourceDB.url, sourceDB.user, sourceDB.password);
    try
    {
      Class.forName(targetDB.driver);
      Connection targetConn = DriverManager.getConnection(
      targetDB.url, targetDB.user, targetDB.password);
      targetConn.setAutoCommit(false);
      try
      {
        Statement stmt = sourceConn.createStatement();
        try
        {
          ResultSet rs = stmt.executeQuery(query);
          try
          {
            while (rs.next())
            {
              Forum forum = new Forum();
              Question question = new Question();
              Answer answer = new Answer();

              forum.setForumId(rs.getString("forumId"));
              forum.setName(rs.getString("forumName"));
              forum.setDescription(rs.getString("forumDescription"));
              forum.setStartDateTime(rs.getString("forumStartDateTime"));
              forum.setEndDateTime(rs.getString("forumEndDateTime"));
              forum.setMaxQuestions(rs.getInt("forumMaxQuestions"));
              String type = rs.getString("forumType");
              forum.setType(type.equals("I") ? ForumType.INTERVIEW : ForumType.NORMAL);
              forum.setGroup(rs.getString("forumGroup"));
              forum.setEmailFrom(rs.getString("forumEmailFrom"));
              forum.setEmailTo(rs.getString("forumEmailTo"));
              forum.setAdminRoleId(rs.getString("forumAdminRoleId"));
              forum.setCreationDateTime(rs.getString("forumCreationDateTime"));
              forum.setCreationUserId(rs.getString("forumCreationUserId"));
              forum.setChangeDateTime(rs.getString("forumChangeDateTime"));
              forum.setChangeUserId(rs.getString("forumChangeUserId"));

              question.setQuestionId(rs.getString("questionId"));
              question.setTitle(rs.getString("questionTitle"));
              question.setText(rs.getString("questionText"));
              String vs = rs.getString("questionVisible");
              question.setVisible("y".equalsIgnoreCase(vs) || "s".equalsIgnoreCase(vs));
              question.setCreationDateTime(rs.getString("questionCreationDateTime"));
              question.setCreationUserId(rs.getString("questionCreationUserId"));
              question.setChangeDateTime(rs.getString("questionChangeDateTime"));
              question.setChangeUserId(rs.getString("questionChangeUserId"));
              question.setActivityDateTime(rs.getString("questionActivityDateTime"));
              question.setReadCount(rs.getInt("questionReadCount"));

              answer.setText(rs.getString("answerText"));
              answer.setCreationDateTime(rs.getString("answerCreationDateTime"));
              answer.setCreationUserId(rs.getString("answerCreationUserId"));
              answer.setChangeDateTime(rs.getString("answerChangeDateTime"));
              answer.setChangeUserId(rs.getString("answerChangeUserId"));

              if (isNewForum(forum))
              {
                commitForum(targetConn); // commit previous forum
                createForum(targetConn, forum);
              }

              if (isNewQuestion(question))
              {                
                createQuestion(targetConn, question);
              }

              if (answer.getText() != null)
              {
                createAnswer(targetConn, answer);
              }
            }
            commitForum(targetConn); // commit last forum
          }
          finally
          {
            rs.close();
          }
        }
        finally
        {
          stmt.close();
        }
      }
      finally
      {
        targetConn.rollback();
        targetConn.close();
      }
    }
    finally
    {
      sourceConn.close();
    }
  }

  private void commitForum(Connection conn) throws Exception
  {
    if (oldForumId != null)
    {
      System.out.println("Committing forum " + oldForumId);

      // update indices of previous forum
      PreparedStatement prepStmt =  conn.prepareStatement(
        "update fru_forum " +
        "set lastinputindex=?, lastoutputindex=? where forumid=?");
      try
      {
        prepStmt.setInt(1, inputIndex);
        prepStmt.setInt(2, outputIndex);
        prepStmt.setString(3, oldForumId);
        prepStmt.executeUpdate();
      }
      finally
      {
        prepStmt.close();
      }
    }
    conn.commit();
  }

  private boolean isNewForum(Forum forum)
  {
    if (oldForumId == null) return true;
    return !oldForumId.equals(forum.getForumId());
  }

  private boolean isNewQuestion(Question question)
  {
    if (oldQuestionId == null) return true;
    return !oldQuestionId.equals(question.getQuestionId());
  }

  private void createForum(Connection conn, Forum forum) throws Exception
  {
    System.out.println("create forum " + forum.getForumId() + " " + forum.getName());

    oldForumId = forum.getForumId();
    newForumId = getId(conn, "FRU_FORUM");
    inputIndex = 0;
    outputIndex = 0;

    PreparedStatement prepStmt =  conn.prepareStatement(
      "insert into fru_forum (forumid, name, description, type, " +
      "grp, startdt, enddt," +
      "maxquestions, adminrole, emailto, emailfrom, " +
      "creationdt, creationuserid, changedt, changeuserid," +
      "lastinputindex, lastoutputindex) " +
      " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,0)");
    try
    {
      prepStmt.setString(1, newForumId);
      prepStmt.setString(2, forum.getName());
      prepStmt.setString(3, forum.getDescription());
      prepStmt.setString(4, 
        forum.getType().equals(ForumType.INTERVIEW) ? "I" : "N");
      prepStmt.setString(5, forum.getGroup());
      prepStmt.setString(6, forum.getStartDateTime());
      prepStmt.setString(7, forum.getEndDateTime());
      prepStmt.setInt(8, forum.getMaxQuestions());
      prepStmt.setString(9, forum.getAdminRoleId());
      prepStmt.setString(10, forum.getEmailTo());
      prepStmt.setString(11, forum.getEmailFrom());
      prepStmt.setString(12, forum.getCreationDateTime());
      prepStmt.setString(13, forum.getCreationUserId());
      prepStmt.setString(14, forum.getChangeDateTime());
      prepStmt.setString(15, forum.getChangeUserId());
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void createQuestion(Connection conn, Question question)
    throws Exception
  {
    System.out.println("create question " + question.getQuestionId());

    oldQuestionId = question.getQuestionId();    
    newQuestionId = getId(conn, "FRU_QUESTION");

    inputIndex++;
    if (question.isVisible())
    {
      outputIndex++;
    }

    PreparedStatement prepStmt =  conn.prepareStatement(
      "insert into fru_question (questionid, forumid, title, text, " +
      "inputindex, outputindex, visible, creationdt, creationuserid, " +
      "changedt, changeuserid, activitydt, readcount) " +
      "values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
    try
    {
      prepStmt.setString(1, newQuestionId);
      prepStmt.setString(2, newForumId);
      prepStmt.setString(3, question.getTitle());
      prepStmt.setString(4, question.getText());
      prepStmt.setInt(5, inputIndex);
      prepStmt.setInt(6, question.isVisible() ? outputIndex : 0);
      prepStmt.setString(7, question.isVisible() ? "Y" : "N");
      prepStmt.setString(8, question.getCreationDateTime());
      prepStmt.setString(9, question.getCreationUserId());
      prepStmt.setString(10, question.getChangeDateTime());
      prepStmt.setString(11, question.getChangeUserId());
      prepStmt.setString(12, question.getActivityDateTime());
      prepStmt.setInt(13, question.getReadCount());
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void createAnswer(Connection conn, Answer answer) throws Exception
  {
    System.out.println("create answer");

    String newAnswerId = getId(conn, "FRU_ANSWER");
    PreparedStatement prepStmt =  conn.prepareStatement(
      "insert into fru_answer (answerid, questionid, text," +
      "creationdt, creationuserid, changedt, changeuserid) " +
      "values (?,?,?,?,?,?,?)");
    try
    {
      prepStmt.setString(1, newAnswerId);
      prepStmt.setString(2, newQuestionId);
      prepStmt.setString(3, answer.getText());
      prepStmt.setString(4, answer.getCreationDateTime());
      prepStmt.setString(5, answer.getCreationUserId());
      prepStmt.setString(6, answer.getChangeDateTime());
      prepStmt.setString(7, answer.getChangeUserId());
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private String getId(Connection conn, String name) throws Exception
  {
    String id = null;
    PreparedStatement prepStmt = conn.prepareStatement(
     "update tableseq set value = value + 1 where counter = ?");
    try
    {
      prepStmt.setString(1, name);
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }

    PreparedStatement prepStmt2 = conn.prepareStatement(
     "select value from tableseq where counter = ?");
    try
    {
      prepStmt2.setString(1, name);
      ResultSet rs = prepStmt2.executeQuery();
      try
      {
        if (rs.next())
        {
          id = rs.getString(1);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt2.close();
    }
    return id;
  }

  private String readQuery(InputStream is) throws IOException
  {
    StringBuilder builder = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    try
    {
      String line = reader.readLine();
      while (line != null)
      {
        builder.append(line);
        builder.append('\n');
        line = reader.readLine();
      }
    }
    finally
    {
      reader.close();
    }
    return builder.toString();
  }
}

