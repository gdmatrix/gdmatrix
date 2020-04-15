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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author realor
 *
 * A utility to export/import contents to/from a directory
 */
public class OracleContentBackup
{
  private final Properties config;
  private static final Logger LOGGER = Logger.getLogger("ContentBackup");
  private final byte[] buffer = new byte[1024];

  public OracleContentBackup(Properties config)
  {
    this.config = config;
  }

  public void exportContents() throws Exception
  {
    LOGGER.log(Level.INFO, "Export contents to {0}",
      getDirectory().getAbsoluteFile());

    long t0 = System.currentTimeMillis();
    Connection conn = connect();
    try
    {
      Statement stmt = conn.createStatement();
      try
      {
        ResultSet rs = selectContents(stmt);
        try
        {
          int contentCount = 0;
          while (rs.next())
          {
            exportContent(rs);
            contentCount++;
          }
          long t1 = System.currentTimeMillis();
          double hours = (t1 - t0) / 3600000.0;
          LOGGER.log(Level.INFO, "Exported {0} contents in {1} hours.",
            new Object[]{contentCount, hours});
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
      conn.close();
    }
  }

  public void importContents() throws Exception
  {
    Connection conn = connect();    
    try
    {
      conn.setAutoCommit(false);
      PreparedStatement ps1 = conn.prepareStatement(
        "insert into cnt_content (uuid, filetype, mimetype, captureuser, " +
        "capturedate, language, contentsize, creationdate, puid) values " +
        "(?, ?, ?, ?, ?, ?, ?, ?, ?)");
      try
      {
        PreparedStatement ps2 = conn.prepareStatement(
          "insert into cnt_internal (uuid, data, fmt) values (?, ?, ?)");
        try
        {
          PreparedStatement ps3 = conn.prepareStatement(
            "insert into cnt_external (uuid, url, fmt) values (?, ?, ?)");
          try
          {
            PreparedStatement ps4 = conn.prepareStatement(
              "select 1 from cnt_internal where uuid = ?");
            try
            {
              File dir = getDirectory();
              LOGGER.log(Level.INFO, "Importing contents from {0}...",
                dir.getAbsolutePath());

              File[] files = dir.listFiles(new FileFilter()
              {
                @Override
                public boolean accept(File pathname)
                {
                  return pathname.getName().endsWith(".properties");
                }
              });
              LOGGER.log(Level.INFO, "{0} contents found.", files.length);

              long t0 = System.currentTimeMillis();
              int contentCount = 0;
              for (File file : files)
              {
                Properties metadata = new Properties();
                FileInputStream is = new FileInputStream(file);
                try
                {
                  metadata.load(is);
                  String uuid = metadata.getProperty("uuid");
                  String contentSize = metadata.getProperty("contentsize");
                  long size = contentSize == null ? 
                    0 : Long.parseLong(contentSize) / 1024;

                  LOGGER.log(Level.INFO, "Importing content {0}, size: {1} Kb", 
                    new Object[]{uuid, size});
                  if (contentExists(uuid, ps4))
                  {
                    LOGGER.log(Level.INFO, "Content already exists.");                  
                  }
                  else
                  {
                    importContentMetadata(metadata, ps1);
                    String filetype = metadata.getProperty("filetype");
                    if ("I".equals(filetype)) // internal file
                    {
                      importInternalContent(metadata, ps2);
                    }
                    else
                    {
                      importExternalContent(metadata, ps3);
                    }
                    conn.commit();
                    contentCount++;
                    LOGGER.log(Level.INFO, "Content created.");                  
                  }
                }
                catch (Exception ex)
                {
                  LOGGER.log(Level.SEVERE, ex.toString());
                }
                finally
                {
                  is.close();
                }
              }
              long t1 = System.currentTimeMillis();
              double hours = (t1 - t0) / 3600000.0;
              LOGGER.log(Level.INFO, "{0} contents imported in {1} hours.", 
                new Object[]{contentCount, hours});
            }
            finally
            {
              ps4.close();
            }
          }
          finally
          {
            ps3.close();
          }
        }
        finally
        {
          ps2.close();
        }
      }
      finally
      {
        ps1.close();
      }
    }
    finally
    {
      conn.close();
    }
  }

  private Connection connect() throws Exception
  {
    String jdbcUrl = config.getProperty("jdbcUrl");
    if (jdbcUrl == null) 
      throw new RuntimeException("jdbcUrl not specified!");

    String jdbcUsername = config.getProperty("jdbcUsername");
    if (jdbcUsername == null) 
      throw new RuntimeException("jdbcUsername not specified!");

    String jdbcPassword = config.getProperty("jdbcPassword");
    if (jdbcPassword == null) 
      throw new RuntimeException("jdbcPassword not specified!");

    Class.forName("oracle.jdbc.driver.OracleDriver");
    return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
  }

  private ResultSet selectContents(Statement stmt)
    throws SQLException
  {
    String startDateTime = config.getProperty("startDateTime");
    String endDateTime = config.getProperty("endDateTime");

    String query = "select c.uuid uuid, c.filetype filetype, " +
      "c.mimetype mimetype, c.captureuser captureuser, " +
      "c.capturedate capturedate, c.language language, " +
      "c.contentsize contentsize, c.creationdate creationdate, " +
       "c.puid puid, i.data data, i.fmt fmt_i, e.url url, e.fmt fmt_e " +
      "from cnt_content c, cnt_internal i, cnt_external e\n" +
      "where c.uuid = i.uuid (+) and c.uuid = e.uuid (+)";

    if (startDateTime != null || endDateTime != null)
    {
      query += " and ";
      if (startDateTime != null)
      {
        query += " c.capturedate >= '" + startDateTime + "'";
      }
      if (endDateTime != null)
      {
        if (startDateTime != null) query += " and ";
        query += " c.capturedate <= '" + endDateTime + "'";
      }
    }
    query += " order by c.capturedate";

    LOGGER.log(Level.INFO, "Executing query {0}...", query);

    return stmt.executeQuery(query);
  }

  private void exportContent(ResultSet rs) throws SQLException, IOException
  {
    String uuid = rs.getString("uuid");
    String filetype = rs.getString("filetype");

    Properties metadata = new Properties();
    setProperty(metadata, "uuid", uuid);
    setProperty(metadata, "filetype", filetype);
    setProperty(metadata, "mimetype",rs.getString("mimetype"));
    setProperty(metadata, "captureuser", rs.getString("captureuser"));
    setProperty(metadata, "capturedate", rs.getString("capturedate"));
    setProperty(metadata, "language", rs.getString("language"));
    setProperty(metadata, "contentsize", rs.getString("contentsize"));
    setProperty(metadata, "creationdate", rs.getString("creationdate"));
    setProperty(metadata, "puid", rs.getString("puid"));
    Blob blob;
    if (filetype.equals("I"))
    {
      blob = rs.getBlob("data");
      setProperty(metadata, "fmt", rs.getString("fmt_i"));
    }
    else
    {
      blob = null;
      setProperty(metadata, "url", rs.getString("url"));
      setProperty(metadata, "fmt", rs.getString("fmt_e"));
    }

    LOGGER.log(Level.INFO, "Saving metadata for content {0}", uuid);
    File metadataFile = getMetadataFile(uuid);
    FileOutputStream mos = new FileOutputStream(metadataFile);
    try
    {
      metadata.store(mos, "Content metadata");
    }
    finally
    {
      mos.close();
    }

    if (blob != null)
    {
      String contentSize = metadata.getProperty("contentsize");
      long size = contentSize == null ? 0 : Long.parseLong(contentSize) / 1024;
      String captureDate = metadata.getProperty("capturedate");
      LOGGER.log(Level.INFO,
        "Saving data for content {0}, size: {1} kb, capturedate: {2}",
        new Object[]{uuid, size, captureDate});
      File dataFile = getDataFile(uuid);
      FileOutputStream dos = new FileOutputStream(dataFile);
      try
      {
        InputStream is = blob.getBinaryStream();
        try
        {
          writeToStream(is, dos);
        }
        finally
        {
          is.close();
        }
      }
      finally
      {
        dos.close();
      }
    }
  }

  private void setProperty(Properties properties, String name, String value)
  {
    if (value != null)
    {
      properties.setProperty(name, value);
    }
  }

  private void writeToStream(InputStream is, OutputStream os)
    throws IOException
  {
    int len = is.read(buffer);
    while (len != -1)
    {
      os.write(buffer, 0, len);
      len = is.read(buffer);
    }
  }
  
  private boolean contentExists(String uuid, PreparedStatement ps)
    throws SQLException
  {
    boolean exists = false;
    ps.setString(1, uuid);
    ResultSet rs = ps.executeQuery();
    try
    {
      exists = rs.next();
    }
    finally
    {
      rs.close();
    }
    return exists;
  }

  private void importContentMetadata(Properties metadata, 
    PreparedStatement ps) throws SQLException
  {
    ps.setString(1, metadata.getProperty("uuid"));
    ps.setString(2, metadata.getProperty("filetype"));
    ps.setString(3, metadata.getProperty("mimetype"));
    ps.setString(4, metadata.getProperty("captureuser"));
    ps.setString(5, metadata.getProperty("capturedate"));
    ps.setString(6, metadata.getProperty("language"));
    ps.setString(7, metadata.getProperty("contentsize"));
    ps.setString(8, metadata.getProperty("creationdate"));
    ps.setString(9, metadata.getProperty("puid"));
    ps.execute();
  }

  private void importInternalContent(Properties metadata, PreparedStatement ps)
    throws SQLException, IOException
  {
    String uuid = metadata.getProperty("uuid");
    ps.setString(1, uuid);
    File dataFile = getDataFile(uuid);
    FileInputStream is = new FileInputStream(dataFile);
    try
    {
      ps.setBlob(2, is);
    }
    finally
    {
      is.close();
    }
    ps.setString(3, metadata.getProperty("fmt"));
    ps.execute();
  }

  private void importExternalContent(Properties metadata, PreparedStatement ps)
    throws SQLException
  {
    ps.setString(1, metadata.getProperty("uuid"));
    ps.setString(2, metadata.getProperty("url"));
    ps.setString(3, metadata.getProperty("fmt"));
    ps.execute();
  }

  public File getDirectory()
  {
    String dirName = config.getProperty("directory");
    if (dirName == null) dirName = "./contents";
    File dir = new File(dirName);
    if (!dir.exists()) dir.mkdirs();
    return dir;
  }

  private File getMetadataFile(String uuid)
  {
    return new File(getDirectory(), uuid + ".properties");
  }

  private File getDataFile(String uuid)
  {
    return new File(getDirectory(), uuid + ".bin");
  }

  public static void main(String[] args)
  {
    if (args.length < 1)
    {
      System.out.println("Arguments: <config_file.properties>");
      System.out.println("Properties: action=[export|import], " + 
        "jdbcUrl, jdbcUsername, jdbcPassword, directory, " +
        "startDateTime (optional), endDateTime (optional).");
    }
    else
    {
      try
      {
        String configFilename = args[0];
        File configFile = new File(configFilename);
        Properties config = new Properties();
        InputStream is = new FileInputStream(configFile);
        try
        {
          config.load(is);
        }
        finally
        {
          is.close();
        }
        String action = config.getProperty("action");
        OracleContentBackup backup = new OracleContentBackup(config);
        if (action.equals("import"))
        {
          backup.importContents();
        }
        else // default action: export
        {
          backup.exportContents();
        }
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.SEVERE, ex.toString());
        System.exit(-1);
      }
    }
  }
}
