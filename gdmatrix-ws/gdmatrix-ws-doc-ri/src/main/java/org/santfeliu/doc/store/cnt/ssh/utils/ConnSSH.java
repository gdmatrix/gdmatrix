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
package org.santfeliu.doc.store.cnt.ssh.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 *
 * @author xeviserrats
 * @author blanquepa 
 */
public class ConnSSH implements Closeable
{

  private static final Logger log = Logger.getLogger(ConnSSH.class.getName());

  private static final int NUM_SEGONS_ESPERAR_RETRY = 5;

  private static final int INIT_CONNECTION_NUM_REINTENTS = 5;

  private Session session = null;
  private ChannelSftp sftpChannel;
  private String dirBase = null;

  private String servidor;
  private String usuari;
  private String pass;

  public static InfoModeSSH getInfoModeSSH(Connection c) throws Exception
  {
    ResultSet rs = null;
    PreparedStatement ps = null;
    try
    {
      ps = c.prepareStatement("select CLIENT, pathcli, usr, pass "
        + "from apl_aliescli where TRIM(ALIESCLI)=TRIM(?)"
        + " and TRIM(aplicacio)=TRIM(?)");
      ps.setString(1, "MATRIXSSH");
      ps.setString(2, "NCL");
      rs = ps.executeQuery();

      if (rs != null && rs.next())
      {
        String wServidor = rs.getString(1).trim();
        String wDirBase = rs.getString(2).trim();
        String wUsuari = rs.getString(3).trim();
        String wContrasenya = rs.getString(4).trim();

        log.log(Level.FINE, "servidor: '" + wServidor + "' dirbase: '" 
          + wDirBase + "' usuari: '" + wUsuari + "'");

        return new InfoModeSSH(wServidor, wUsuari, wContrasenya, wDirBase);
      }
      else
      {
        throw 
          new Exception("No se ha encontado del APL_ALIESCLI[MATRIXSSH|NCL].");
      }
    }
    catch (Exception e)
    {
      log.log(Level.WARNING, e.getMessage(), e);
      throw e;
    }
    finally
    {
      if (rs != null)
      {
        rs.close();
      }
      if (ps != null)
      {
        ps.close();
      }
    }
  }

  public static ConnSSH createConSSH(Connection c) throws Exception
  {
    ConnSSH wConnSSH = new ConnSSH();
    InfoModeSSH wInfoModeSSH = getInfoModeSSH(c);

    boolean wConnectionOK = false;
    boolean wIsException = false;

    int wNumReintent = 0;
    while (!wConnectionOK && wNumReintent < INIT_CONNECTION_NUM_REINTENTS)
    {
      wNumReintent++;

      try
      {
        wConnSSH.init(wInfoModeSSH.servidor, wInfoModeSSH.usuari, 
          wInfoModeSSH.contrasenya, wInfoModeSSH.dirBase);

        wConnectionOK = true;

        if (wIsException)
        {
          log.log(Level.SEVERE, 
            "NOS HEMOS CONECTADO CORRECTAMENTE EN EL INTENTO '" 
              + wNumReintent + "'.");
        }
      }
      catch (JSchException e)
      {
        log.log(Level.SEVERE, "ERROR CONNECTING SHH. Intento '" 
          + wNumReintent + "' de '" + INIT_CONNECTION_NUM_REINTENTS + "'.");
        log.log(Level.WARNING, e.getMessage(), e);

        // la primera vegada ens esperem 1 segon, la segon 2, la tercera 3 ...
        Thread.sleep(1000 * wNumReintent);

        /*
        * Si es produeix una JSchException ens esperem 2 segon i hi tornem
        * 
         com.jcraft.jsch.JSchException: connection is closed by foreign host
             at com.jcraft.jsch.Session.connect(Session.java:269)
             at com.jcraft.jsch.Session.connect(Session.java:183)
             at com.audifilm.gestdocs.util.ModeIndexSSHFitxerUtils$ConnSSH.init
             (ModeIndexSSHFitxerUtils.java:52)				
         */
        wConnectionOK = false;
        wIsException = true;
      }
    }

    return wConnSSH;
  }

  public void init(String pServidor, String pUsuari, String pContrasenya, 
    String pDirBase) throws Exception
  {
    dirBase = pDirBase;

    servidor = pServidor;
    usuari = pUsuari;
    pass = pContrasenya;

    try
    {
      JSch jsch = new JSch();
      session = jsch.getSession(pUsuari, pServidor, 22);
      session.setConfig("StrictHostKeyChecking", "no");
      session.setPassword(pContrasenya);
      session.connect();

      Channel channel = session.openChannel("sftp");
      channel.connect();
      sftpChannel = (ChannelSftp) channel;
    }
    catch (Exception e)
    {
      System.out.println("Impsible connectar en '" + pServidor + "' Usuario: '" 
        + pUsuari + "' Dir: '" + pDirBase + "'.");
      log.log(Level.SEVERE, "Impsible connectar en '" + pServidor 
        + "' Usuario: '" + pUsuari + "' Dir: '" + pDirBase + "'.");
      log.log(Level.SEVERE, e.getClass() + " " + e.getMessage());
      throw e;
    }
  }

  public ChannelSftp getCanal()
  {
    return sftpChannel;
  }

  public void rename(String pRemote, String pNewName) throws SftpException
  {
    try
    {
      getCanal().rename(pRemote, pNewName);
    }
    catch (SftpException e)
    {
      log.log(Level.SEVERE, "RENAME REMOTE: '" + pRemote + "' REMOTE_FILE: '" 
        + pNewName + "'");
      log.log(Level.SEVERE, "Politica reintento.");
      log.log(Level.SEVERE, e.getMessage(), e);

      try
      {
        Thread.sleep(NUM_SEGONS_ESPERAR_RETRY * 1000);

        try
        {
          if (getCanal() != null && !getCanal().isClosed())
          {
            getCanal().disconnect();
          }
        }
        catch (Exception e33)
        {
        }

        init(servidor, usuari, pass, dirBase);

        getCanal().rename(pRemote, pNewName);
      }
      catch (Exception ex)
      {
        throw new SftpException(-5000, "Retry fail", ex);
      }
    }
  }

  /**
   * Llegim el fitxer en inputstream /(binari). A verue si solventem els
   * problemes al legir els fixters XML (a l'�ltim caracter posa un salt de
   * linia diferent).
   *
   * @param pRemoteFile
   * @param pLocalFile
   * @throws IOException
   */
  private void readFileFromInputStream(String pRemoteFile, File pLocalFile) 
    throws IOException
  {
    InputStream is = null;
    BufferedOutputStream out = null;
    try
    {
      is = new BufferedInputStream(getCanal().get(pRemoteFile));
      
      // 4 Kb.  Block Size Linux EXT3
      out = new BufferedOutputStream(new FileOutputStream(pLocalFile), 4 * 1024); 	

      byte[] buf = new byte[64 * 1024];
      int len;
      while ((len = is.read(buf)) > 0)
      {
        out.write(buf, 0, len);
      }
    }
    catch (Exception e)
    {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new IOException(e);
    }
    finally
    {
      if (is != null)
      {
        is.close();
      }
      if (out != null)
      {
        out.close();
      }
    }
  }

  public void get(String pRemoteFile, File pLocalFile) throws SftpException
  {
    try
    {
      readFileFromInputStream(pRemoteFile, pLocalFile);
      //getCanal().get(pRemoteFile, pLocalFile.getAbsolutePath());
    }
    catch (Exception e)
    {
      log.log(Level.SEVERE, "GET REMOTE: '" + pRemoteFile + "' LOCAL_FILE: '" 
        + pLocalFile + "'");
      log.log(Level.SEVERE, "Politica reintento.");
      log.log(Level.SEVERE, e.getMessage(), e);

      try
      {
        Thread.sleep(NUM_SEGONS_ESPERAR_RETRY * 1000);

        try
        {
          if (getCanal() != null && !getCanal().isClosed())
          {
            getCanal().disconnect();
          }
        }
        catch (Exception e33)
        {
        }

        init(servidor, usuari, pass, dirBase);

        readFileFromInputStream(pRemoteFile, pLocalFile);

        //getCanal().get(pRemoteFile, pLocalFile.getAbsolutePath());
      }
      catch (Exception ex)
      {
        throw new SftpException(-5000, "Retry fail", ex);
      }
    }
  }

  private void putImpl(File wOrigen, String wRemoteFile) throws Exception
  {
    InputStream is = null;
    try
    {
      is = new BufferedInputStream(new FileInputStream(wOrigen));

      getCanal().put(is, wRemoteFile);
    }
    catch (Exception e)
    {
      throw e;
    }
    finally
    {
      if (is != null)
      {
        is.close();
      }
    }
  }

  public void put(File wOrigen, String wRemoteFile) throws SftpException
  {
    try
    {
      putImpl(wOrigen, wRemoteFile);
    }
    catch (Exception e)
    {
      log.log(Level.SEVERE, "PUT : FICHERO: '" + wOrigen.getAbsolutePath() 
        + "' REMOTE_FILE: '" + wRemoteFile + "'");
      log.log(Level.SEVERE, "Politica reintento.");
      log.log(Level.SEVERE, e.getMessage(), e);

      try
      {
        Thread.sleep(NUM_SEGONS_ESPERAR_RETRY * 1000 
          + FileSSHUtils.getRandomWait());

        try
        {
          if (getCanal() != null && !getCanal().isClosed())
          {
            getCanal().disconnect();
          }
        }
        catch (Exception e33)
        {
        }

        init(servidor, usuari, pass, dirBase);

        putImpl(wOrigen, wRemoteFile);
      }
      catch (Exception ex)
      {
        log.log(Level.SEVERE, e.getMessage(), e);

        throw new SftpException(-5001, "retry put failed.", e);
      }
    }
  }

  public void close() throws IOException
  {
    try
    {
      if (sftpChannel != null)
      {
        sftpChannel.disconnect();
        sftpChannel.exit();
        sftpChannel = null;
      }
    }
    catch (Exception e)
    {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    try
    {
      if (session != null)
      {
        session.disconnect();
        session = null;
      }
    }
    catch (Exception e)
    {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }

  public String getFileName(String pGUID) throws Exception
  {
    return pGUID.toUpperCase() + ".data";
  }

  public String getDirectoriByIdNReg(Connection c, String pGUID) 
    throws Exception
  {
    String wDirectori = dirBase;

    if (!dirBase.endsWith("/"))
    {
      wDirectori += "/";
    }

    return FileSSHUtils.mkdirByGUID(getCanal(), wDirectori, pGUID);
  }
}
