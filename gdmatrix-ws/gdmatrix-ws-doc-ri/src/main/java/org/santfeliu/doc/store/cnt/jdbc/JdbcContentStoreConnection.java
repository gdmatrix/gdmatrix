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
package org.santfeliu.doc.store.cnt.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class JdbcContentStoreConnection implements ContentStoreConnection
{
  public static final String INTERNAL = "I";
  public static final String EXTERNAL = "E";

  public static final String FMT_TEXT = "TEXT";
  public static final String FMT_BINARY = "BINARY";
  public static final String FMT_IGNORE = "IGNORE";
  
  private static final Logger LOGGER = 
    Logger.getLogger(JdbcContentStoreConnection.class.getName()); 
  protected Connection conn;
  protected Properties config;

  public JdbcContentStoreConnection(Connection conn, Properties config)
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
  
  public void createTables() throws Exception
  {
    createTable("createContentTable");
    createTable("createInternalTable");
    createTable("createExternalTable");
  }  

  // ** Content operations : access via JDBC
  @Override
  public Content storeContent(Content content, File file)
    throws Exception
  {
    boolean internal = (file != null);
    if (internal)
    {
      try (InputStream is = new FileInputStream(file))
      {
        insertContentMetaData(conn, content);
        insertInternalContent(conn, content, is);
      }
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
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, contentId);
      try (ResultSet rs = prepStmt.executeQuery()) 
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
            content.setSize(rs.getLong(8));
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

      LOGGER.log(Level.INFO, "SQL: {0}", sql);
      try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
      {
        count = 1;
        for (String contentId : contentIds)
        {
          prepStmt.setString(count, contentId);
          count++;
        }

        LOGGER.log(Level.INFO, contentIds.toString());

        try (ResultSet rs = prepStmt.executeQuery()) 
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
            content.setSize(rs.getLong(7));
            contents.add(content);
          }
        }
      }
    }
    return contents;
  }
  
  @Override
  public File markupContent(String contentId, String searchExpression) 
    throws Exception
  {
    throw new Exception("Not implemented yet.");
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
  protected void createTable(String queryName) throws Exception
  {
    String sql = config.getProperty(queryName);
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.executeUpdate();
    }    
  }
    
  protected void insertContentMetaData(Connection conn, Content content)
    throws Exception
  {
    String sql = config.getProperty("insertContentMetaDataSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
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
  }

  protected void insertInternalContent(Connection conn, Content content,
    InputStream is) throws Exception
  {
    String sql = config.getProperty("insertInternalContentSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);

    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, getFormat(content.getContentType()));
      prepStmt.setBinaryStream(3, is, content.getSize().intValue());
      prepStmt.executeUpdate();
    }
  }

  protected void insertExternalContent(Connection conn, Content content)
    throws Exception
  {
    String sql = config.getProperty("insertExternalContentSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, getFormat(content.getContentType()));
      prepStmt.setString(3, content.getUrl());
      prepStmt.executeUpdate();
    }
  }

  protected void copyContentMetaData(Connection conn, Content content,
    String contentId)
    throws Exception
  {
    String sql = config.getProperty("copyContentMetaDataSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, content.getLanguage());
      prepStmt.setString(3, content.getCaptureUserId());
      prepStmt.setString(4, content.getCaptureDateTime());
      prepStmt.setString(5, contentId);
      prepStmt.executeUpdate();
    }
  }

  protected void copyInternalContent(Connection conn, Content content,
    String contentId) throws Exception
  {
    String sql = config.getProperty("copyInternalContentSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, contentId);
      prepStmt.executeUpdate();
    }
  }

  protected void copyExternalContent(Connection conn, Content content,
    String contentId) throws Exception
  {
    String sql = config.getProperty("copyExternalContentSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, content.getContentId());
      prepStmt.setString(2, contentId);
      prepStmt.executeUpdate();
    }
  }

  protected int deleteContentMetaData(Connection conn, String contentId)
    throws Exception
  {
    String sql = config.getProperty("deleteContentMetaDataSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, contentId);
      return prepStmt.executeUpdate();
    }
  }

  protected int deleteInternalContent(Connection conn, String contentId)
    throws Exception
  {
    String sql = config.getProperty("deleteInternalContentSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, contentId);
      return prepStmt.executeUpdate();
    }
  }

  protected int deleteExternalContent(Connection conn, String contentId)
    throws Exception
  {
    String sql = config.getProperty("deleteExternalContentSQL");
    LOGGER.log(Level.INFO, "SQL: {0}", sql);
    try (PreparedStatement prepStmt = conn.prepareStatement(sql)) 
    {
      prepStmt.setString(1, contentId);
      return prepStmt.executeUpdate();
    }
  }

  protected String getFormat(String mimeType)
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

}
