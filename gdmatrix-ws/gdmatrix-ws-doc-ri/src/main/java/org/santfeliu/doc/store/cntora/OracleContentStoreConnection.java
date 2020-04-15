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
package org.santfeliu.doc.store.cntora;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.santfeliu.doc.store.ContentStoreConnection;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.TemporaryDataSource;


/**
 *
 * @author blanquepa
 */
public class OracleContentStoreConnection implements ContentStoreConnection
{
  public static final String INTERNAL = "I";
  public static final String EXTERNAL = "E";

  public static final String FMT_TEXT = "TEXT";
  public static final String FMT_BINARY = "BINARY";
  public static final String FMT_IGNORE = "IGNORE";

  private Connection conn;
  private Properties config;

  public OracleContentStoreConnection(Connection conn, Properties config)
  {
    try
    {
      this.conn = conn;
      this.config = config;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  // ** Content operations : access via JDBC
  @Override
  public Content storeContent(Content content, File file)
    throws Exception
  {
    boolean internal = (file != null);
    if (internal)
    {
      InputStream is = new FileInputStream(file);
      insertContentMetaData(conn, content);
      insertInternalContent(conn, content, is);
    }
    else // external
    {
      insertContentMetaData(conn, content);
      insertExternalContent(conn, content);
    }
    return content;
  }

  @Override
  public Content copyContent(Content content, String currentContentId)
    throws Exception
  {
    copyContentMetaData(conn, content, currentContentId);
    if (content.getUrl() != null)
      copyExternalContent(conn, content, currentContentId);
    else
      copyInternalContent(conn, content, currentContentId);

    return content;
  }

  @Override
  public Content loadContent(String contentId, ContentInfo contentInfo)
    throws Exception
  {
    Content content = null;
    String sql = config.getProperty("selectContentSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      prepStmt.setString(1, contentId);
      ResultSet rs = prepStmt.executeQuery();
      try
      {
        if (rs.next())
        {
          content = new Content();
          String fileType = rs.getString(2);
          content.setContentId(rs.getString(1));
          if (!ContentInfo.ID.equals(contentInfo))
          {
            content.setContentType(rs.getString(3));
            content.setFormatId(rs.getString(4));
            content.setLanguage(rs.getString(5));
            content.setCaptureUserId(rs.getString(6));
            content.setCaptureDateTime(rs.getString(7));
            content.setSize(new Long(rs.getLong(8)));
            if (INTERNAL.equals(fileType))
            {
              if (ContentInfo.ALL.equals(contentInfo))
              {
                File file = IOUtils.writeToFile(rs.getBinaryStream(9));
                DataHandler dh = new DataHandler(
                  new TemporaryDataSource(file, content.getContentType()));
                content.setData(dh);
              }
            }
            else if (EXTERNAL.equals(fileType))
            {
              if (!ContentInfo.ID.equals(contentInfo))
                content.setUrl(rs.getString(10));
            }
          }
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      prepStmt.close();
    }
    return content;
  }

  @Override
  public boolean removeContent(String contentId)
    throws Exception
  {
    deleteInternalContent(conn, contentId);
    deleteExternalContent(conn, contentId);
    return deleteContentMetaData(conn, contentId) > 0;
  }

  @Override
  public List<Content> findContents(Set<String> contentIds)
    throws Exception
  {
    List<Content> contents = new ArrayList();
    int contentsSize = contentIds.size();
    if (contentsSize > 0)
    {
      String sql = config.getProperty("findContentsSQL");

      int count = 1;
      while (count < contentsSize)
      {
        sql = sql + " or uuid = ?";
        count++;
      }

      PreparedStatement prepStmt = conn.prepareStatement(sql);
      try
      {
        count = 1;
        for (String contentId : contentIds)
        {
          prepStmt.setString(count, contentId);
          count++;
        }

        System.out.println(sql);
        System.out.println(contentIds);

        ResultSet rs = prepStmt.executeQuery();
        try
        {
          while (rs.next())
          {
            Content content = new Content();
            content.setContentId(rs.getString(1));
            content.setContentType(rs.getString(2));
            content.setFormatId(rs.getString(3));
            content.setLanguage(rs.getString(4));
            content.setCaptureUserId(rs.getString(5));
            content.setCaptureDateTime(rs.getString(6));
            content.setSize(new Long(rs.getLong(7)));
            contents.add(content);
          }
        }
        finally
        {
          rs.close();
        }
      }
      finally
      {
        prepStmt.close();
      }
    }
    return contents;
  }

  @Override
  public File markupContent(String contentId, String searchExpression)
    throws Exception
  {
    File file = null;

    // **** select fileType and mimeType
    String fileType = null;
    String mimeType = null;
    String sqlContent = config.getProperty("selectContentTypeSQL");
    PreparedStatement stmtContent = conn.prepareStatement(sqlContent);
    try
    {
      stmtContent.setString(1, contentId);
      ResultSet rs = stmtContent.executeQuery();
      try
      {
        if (rs.next())
        {
          fileType = rs.getString(1);
          mimeType = rs.getString(2);
        }
        else throw new Exception("doc:INVALID_CONTENTID");
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      stmtContent.close();
    }
    file = File.createTempFile("mkp", ".tmp");
    FileOutputStream out = new FileOutputStream(file);
    // call markup
    if (mimeType != null && mimeType.startsWith("text/") &&
      !"text/html".equals(mimeType))
    {
      // markup for non html text files
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      oracleMarkupContent(contentId, searchExpression, fileType,
        "{{{",
        "}}}",
        " ",
        " ", bout);
      // Oracle don't transform text files to html
      // this transformation must be performed here for non html text files
      createHTMLFile(bout.toByteArray(), out);
    }
    else
    {
      // markup for binary or html files
      oracleMarkupContent(contentId, searchExpression, fileType,
        "<a name=\"ctx%CURNUM\" style=\"color:red\"><b>",
        "</b></a>",
        "<a href=\"#ctx%PREVNUM\">&lt;&lt;&nbsp;</a>",
        "<a href=\"#ctx%NEXTNUM\">&nbsp;&gt;&gt;</a>", out);
    }
    out.close();
    return file;
  }

  @Override
  public void rollback()
    throws SQLException
  {
    conn.rollback();
  }

  @Override
  public void commit()
    throws SQLException
  {
    conn.commit();
  }

  @Override
  public void close()
    throws SQLException
  {
    conn.close();
  }

  /***** private methods *****/
  private void insertContentMetaData(Connection conn, Content content)
    throws Exception
  {
    String sql = config.getProperty("insertContentMetaDataSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      String fileType = content.getData() == null ? EXTERNAL : INTERNAL;
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, fileType);
      prepStmt.setString(3, content.getContentType());
      prepStmt.setString(4, content.getFormatId());
      prepStmt.setString(5, content.getLanguage());
      prepStmt.setString(6, content.getCaptureUserId());
      prepStmt.setString(7, content.getCaptureDateTime());
      prepStmt.setLong(8, content.getSize());
      prepStmt.setString(9, content.getCreationDate());
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void insertInternalContent(Connection conn, Content content,
    InputStream is) throws Exception
  {
    String sql = config.getProperty("insertInternalContentSQL");
    try
    {
      PreparedStatement prepStmt = conn.prepareStatement(sql);
      try
      {
        prepStmt.setString(1, content.getContentId());
        prepStmt.setString(2, getFormat(content.getContentType()));
        prepStmt.setBinaryStream(3, is, content.getSize().intValue());
        prepStmt.executeUpdate();
      }
      finally
      {
        prepStmt.close();
      }
    }
    finally
    {
      is.close();
    }
  }

  private void insertExternalContent(Connection conn, Content content)
    throws Exception
  {
    String sql = config.getProperty("insertExternalContentSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, getFormat(content.getContentType()));
      prepStmt.setString(3, content.getUrl());
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void copyContentMetaData(Connection conn, Content content,
    String contentId)
    throws Exception
  {
    String sql = config.getProperty("copyContentMetaDataSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, content.getLanguage());
      prepStmt.setString(3, content.getCaptureUserId());
      prepStmt.setString(4, content.getCaptureDateTime());
      prepStmt.setString(5, contentId);
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void copyInternalContent(Connection conn, Content content,
    String contentId) throws Exception
  {
    String sql = config.getProperty("copyInternalContentSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, contentId);
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private void copyExternalContent(Connection conn, Content content,
    String contentId) throws Exception
  {
    String sql = config.getProperty("copyExternalContentSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, contentId);
      prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private int deleteContentMetaData(Connection conn, String contentId)
    throws Exception
  {
    String sql = config.getProperty("deleteContentMetaDataSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      prepStmt.setString(1, contentId);
      return prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private int deleteInternalContent(Connection conn, String contentId)
    throws Exception
  {
    String sql = config.getProperty("deleteInternalContentSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      prepStmt.setString(1, contentId);
      return prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private int deleteExternalContent(Connection conn, String contentId)
    throws Exception
  {
    String sql = config.getProperty("deleteExternalContentSQL");
    PreparedStatement prepStmt = conn.prepareStatement(sql);
    try
    {
      prepStmt.setString(1, contentId);
     return prepStmt.executeUpdate();
    }
    finally
    {
      prepStmt.close();
    }
  }

  private String getFormat(String mimeType)
  {
    String format;
    if (mimeType.startsWith("text/"))
    {
      format = FMT_TEXT;
    }
    else if (mimeType.startsWith("image/")
      || mimeType.startsWith("video/")
      || mimeType.startsWith("audio/")
      || mimeType.equalsIgnoreCase("application/octet-stream"))
    {
      format = FMT_IGNORE;
    }
    else
    {
      format = FMT_BINARY;
    }
    return format;
  }

  private void createHTMLFile(byte[] textData, OutputStream out)
    throws Exception
  {
    // TODO: improve html generation
    PrintWriter pr = new PrintWriter(out);
    String s = new String(textData, "UTF-8");
    s = s.replaceAll("&", "&amp;");
    s = s.replaceAll("<", "&lt;");
    s = s.replaceAll(">", "&gt;");
    s = s.replaceAll(" ", "&nbsp;");
    s = s.replaceAll("\n", "<br>");
    s = s.replace("{{{", "<span style=\"background:yellow\">");
    s = s.replace("}}}", "</span>");
    s = "<html><body style=\"font-family:courier new;font-size:12px\">" + s +
      "</body></html>";
    pr.write(s);
    pr.flush();
  }

  private void oracleMarkupContent(
    String contentId, String searchExpression, String fileType,
    String startTag, String endTag, String prevTag, String nextTag,
    OutputStream out) throws Exception
  {
    // ******* generate random query_id
    int query_id = (int)(Math.random() * 1000000);

    // ******** select rowid
    String rowid = "";
    String sqlRowid = EXTERNAL.equals(fileType) ?
      config.getProperty("selectExternalRowidSQL") :
      config.getProperty("selectInternalRowidSQL");
    PreparedStatement stmtRowid = conn.prepareStatement(sqlRowid);
    try
    {
      stmtRowid.setString(1, contentId);
      ResultSet rs = stmtRowid.executeQuery();
      try
      {
        if (rs.next())
        {
          rowid = rs.getString(1);
        }
        else throw new Exception("doc:INVALID_CONTENTID");
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      stmtRowid.close();
    }

    // ******** execute ctx_doc.markup procedure
    String sqlCall = EXTERNAL.equals(fileType) ?
      config.getProperty("markupExternalCall") :
      config.getProperty("markupInternalCall");
    PreparedStatement stmtCall = conn.prepareCall(sqlCall);
    try
    {
      stmtCall.setString(1, rowid);
      stmtCall.setString(2, searchExpression);
      stmtCall.setInt(3, query_id);
      stmtCall.setString(4, startTag);
      stmtCall.setString(5, endTag);
      stmtCall.setString(6, prevTag);
      stmtCall.setString(7, nextTag);
      stmtCall.executeUpdate();
    }
    finally
    {
      stmtCall.close();
    }

    // ******** read markup
    String sqlSelect = config.getProperty("selectMarkupSQL");
    PreparedStatement stmtSel = conn.prepareStatement(sqlSelect);
    try
    {
      stmtSel.setInt(1, query_id);
      ResultSet rs = stmtSel.executeQuery();
      try
      {
        if (rs.next())
        {
          Clob clob = rs.getClob(1);
          InputStream in = clob.getAsciiStream();
          IOUtils.writeToStream(in, out);
        }
      }
      finally
      {
        rs.close();
      }
    }
    finally
    {
      stmtSel.close();
    }

    // ******** remove markup
    String sqlDel = config.getProperty("deleteMarkupSQL");
    PreparedStatement stmtDel = conn.prepareStatement(sqlDel);
    try
    {
      stmtDel.setInt(1, query_id);
      stmtDel.executeUpdate();
    }
    finally
    {
      stmtDel.close();
    }
    // commit delete
    conn.commit();
  }
}
