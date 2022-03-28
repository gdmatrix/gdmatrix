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
package org.santfeliu.doc.store.cnt.ssh;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;

import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.santfeliu.doc.store.cnt.jdbc.ora.OracleContentStoreConnection;
import org.santfeliu.doc.store.cnt.ssh.utils.ConnSSH;
import org.santfeliu.doc.store.cnt.ssh.utils.FileSSHUtils;
import org.santfeliu.doc.store.cnt.ssh.utils.InfoModeSSH;
import org.santfeliu.util.TemporaryDataSource;

/**
 * 
 * @author xeviserrats
 * @author blanquepa
 */
public class SSHFileContentStoreConnection extends OracleContentStoreConnection
{
  private static final Logger log = 
    Logger.getLogger(SSHFileContentStoreConnection.class.getName());

  public SSHFileContentStoreConnection(Connection conn, Properties config)
  {
    super(conn, config);
  }

  @Override
  public Content storeContent(Content content, File file) throws Exception
  {
    boolean internal = (file != null);
    if (internal)
    {
      insertContentMetaData(conn, content);
      insertInternalContentFile(conn, content, file);
    }
    else // external
    {
      insertContentMetaData(conn, content);
      insertExternalContent(conn, content);
    }
    return content;
  }

  protected void insertInternalContentFile(Connection conn, Content content, 
    File pFile) throws Exception
  {
    try (ConnSSH wConnSSH = new ConnSSH())
    {
      InfoModeSSH wSSH = ConnSSH.getInfoModeSSH(conn);
      wConnSSH.init(wSSH.servidor, wSSH.usuari, wSSH.contrasenya, wSSH.dirBase);

      String wRemoteDir = 
        wConnSSH.getDirectoriByIdNReg(conn, content.getContentId());
      String wRemoteFile = 
        wRemoteDir + "/" + wConnSSH.getFileName(content.getContentId());

      if (FileSSHUtils.existeixFitxer(wConnSSH.getCanal(), wRemoteFile))
      {
        String wAra = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String wNewName = wRemoteFile + "_" + wAra + ".deleted";

        log.log(Level.INFO, "El fichero ''{0}'' existe. Se modificará: ''{1}''", 
          new Object[]{wRemoteFile, wNewName});

        try
        {
          wConnSSH.rename(wRemoteFile, wNewName);
        }
        catch (Exception e)
        {
          log.log(Level.WARNING, e.getMessage(), e);
        }
      }

      wConnSSH.put(pFile, wRemoteFile);

      String sql = config.getProperty("insertInternalContentSQL");

      try (PreparedStatement prepStmt = conn.prepareStatement(sql);)
      {
        prepStmt.setString(1, content.getContentId());
        prepStmt.setString(2, getFormat(content.getContentType()));
        prepStmt.executeUpdate();
      }
    }
  }

  @Override
  public Content loadContent(String contentId, ContentInfo contentInfo) 
    throws Exception
  {
    Content content = null;
    String sql = config.getProperty("selectContentSQL");

    try (ConnSSH wConnSSH = new ConnSSH();
      PreparedStatement prepStmt = conn.prepareStatement(sql))
    {
      InfoModeSSH wSSH = ConnSSH.getInfoModeSSH(conn);
      wConnSSH.init(wSSH.servidor, wSSH.usuari, wSSH.contrasenya, wSSH.dirBase);

      File wFileGetDocument = File.createTempFile("MATRIX_GET_GILE_", ".dat");

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
            content.setSize(new Long(rs.getLong(8)));
            if (INTERNAL.equals(fileType))
            {
              if (ContentInfo.ALL.equals(contentInfo))
              {
                String cid = content.getContentId();
                String wRemoteDir = 
                  wConnSSH.getDirectoriByIdNReg(conn, cid);
                String wRemoteFile = 
                  wRemoteDir + "/" + wConnSSH.getFileName(cid);

                wConnSSH.get(wRemoteFile, wFileGetDocument);

                DataHandler dh = new DataHandler(
                  new TemporaryDataSource(wFileGetDocument, 
                    content.getContentType()));
                content.setData(dh);
              }
            }
            else
            {
              if (EXTERNAL.equals(fileType))
              {
                if (!ContentInfo.ID.equals(contentInfo))
                {
                  content.setUrl(rs.getString(10));
                }
              }
            }
          }
        }
      }
    }
    return content;
  }
}
