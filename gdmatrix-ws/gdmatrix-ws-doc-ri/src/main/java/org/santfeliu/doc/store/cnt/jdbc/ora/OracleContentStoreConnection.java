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
package org.santfeliu.doc.store.cnt.jdbc.ora;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import org.santfeliu.doc.store.cnt.jdbc.JdbcContentStoreConnection;
import org.santfeliu.util.IOUtils;


/**
 *
 * @author blanquepa
 */
public class OracleContentStoreConnection extends JdbcContentStoreConnection
{

  public OracleContentStoreConnection(Connection conn, Properties config)
  {
    super(conn, config);
  }
  
  @Override
  public void createTables() throws Exception
  {
    super.createTables();
    createTable("createMarkupTable");
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

  /***** private methods *****/  
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
