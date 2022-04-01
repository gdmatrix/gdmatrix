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

import java.util.Date;
import java.util.Random;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

/**
 *
 * @author xeviserrats
 * @author blanquepa
 */
public class FileSSHUtils
{

  /**
   * Si no existeix, crea l'estructura de directoris de 4 nivells.
   *
   * @param pFTP
   * @param pGUID
   * @throws Exception
   */
  public static String mkdirByGUID(ChannelSftp pFTP, String pDirBase,
    String pGUID) throws Exception
  {
    String wDirChar1 = pGUID.substring(0, 1).toUpperCase();
    String wDirChar2 = pGUID.substring(1, 2).toUpperCase();
    String wDirChar3 = pGUID.substring(2, 3).toUpperCase();

    String wActualDir = pDirBase;
    if (!wActualDir.endsWith("/"))
    {
      wActualDir += "/";
    }

    mkdir(pFTP, wActualDir + wDirChar1);
    mkdir(pFTP, wActualDir + wDirChar1 + "/" + wDirChar2);
    mkdir(pFTP, wActualDir + wDirChar1 + "/" + wDirChar2 + "/" + wDirChar3);

    return pDirBase + wDirChar1 + "/" + wDirChar2 + "/" + wDirChar3;
  }

  public static boolean existeixFitxer(ChannelSftp pSFTP, String pRemoteFile)
    throws Exception
  {
    return existeixFitxer(pSFTP, pRemoteFile, true);
  }

  public static boolean existeixFitxer(ChannelSftp pSFTP, String pRemoteFile,
    boolean pRetryOnException) throws Exception
  {
    try
    {
      Vector wVector = pSFTP.ls(pRemoteFile);

      if (wVector != null)
      {
        return wVector != null && wVector.size() > 0;
      }
      else
      {
        throw new IllegalArgumentException("Vector NULL");
      }
    }
    catch (SftpException e)
    {
      if (e.id == 2)
      {
        return false;
      }
      else
      {
        throw new Exception(e);
      }
    }
    catch (Exception e)
    {
      throw new Exception(e);
    }
  }

  public static void mkdir(ChannelSftp pFTP, String wDirectori)
    throws Exception
  {
    if (!existeixFitxer(pFTP, wDirectori))
    {
      // pot ser que dos threads diferents vulguin crear el directori 
      //alhora, ens hem de protegir de la concurrencia al crear el directori
      try
      {
        pFTP.mkdir(wDirectori);
      }
      catch (Exception e)
      {
        Thread.sleep(1000 + getRandomWait());
        try
        {
          if (!existeixFitxer(pFTP, wDirectori, false))
          {
            pFTP.mkdir(wDirectori);
          }
        }
        catch (Exception ex)
        {
        }
      }
    }
  }

  /**
   * Retorna un valor aleatori entre 0 i 1000.
   *
   * @return
   */
  public static int getRandomWait()
  {
    return new Random(new Date().getTime()).nextInt(1000);
  }
}
