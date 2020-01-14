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
package org.santfeliu.doc.sendfile;

import java.io.File;
import java.net.URL;
import org.santfeliu.util.MimeTypeMap;

/**
 *
 * @author realor
 */
public class FileMonitor extends Thread
{
  private boolean end = false;
  private SendFileApplet applet;
  private final Object lock = new Object();
  private String docId;
  private File file;
  private long lastModified;

  public FileMonitor(SendFileApplet applet, String docId, File file)
  {
    this.applet = applet;
    this.docId = docId;
    this.file = file;
    this.lastModified = file.lastModified();
  }

  public String getDocId()
  {
    return docId;
  }

  @Override
  public void run()
  {
    try
    {
      System.out.println("FileMonitor started.");
      applet.getAppletContext().showStatus(
        applet.getLocalizedMessage("Monitoring") + "...");
      while (!end)
      {
        if (file.lastModified() != lastModified && file.canRead())
        {
          lastModified = file.lastModified();
          UploadTask task = new UploadTask(applet);
          task.setDocId(docId);
          task.setFile(file);
          task.setShowOverlay(false);
          task.execute();
        }
        else
        {
          synchronized (lock)
          {
            lock.wait(1000);
          }
        }
      }
      applet.getAppletContext().showStatus("");
    }
    catch (InterruptedException ex)
    {
      ex.printStackTrace();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      unlockDocument();
    }
    System.out.println("FileMonitor end.");
  }

  public void end()
  {
    end = true;
    synchronized (lock)
    {
      lock.notify();
    }
  }

  public static File getFileFor(String docId, String contentType)
  {
    String extension = MimeTypeMap.getMimeTypeMap().getExtension(contentType);
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File file = new File(tmpDir, docId + "." + extension);
    return file;
  }

  public static boolean backupPreviousFile(File file)
  {
    boolean backup = true;
    if (file.exists())
    {
      // file exists, rename it;
      if (file.isFile())
      {
        File backupFile = new File(file.getParentFile(),
          "backup-" + System.currentTimeMillis() + "-" + file.getName());
        backup = file.renameTo(backupFile);
      }
      else throw new RuntimeException("FILE_IS_A_DIRECTORY");
    }
    return backup;
  }

  private void unlockDocument()
  {
    System.out.println("Unlock document");
    try
    {
      URL url = new URL(applet.getServletURL());
      System.out.println("Connecting to " + url);
      ServletClient client = new ServletClient(url, applet.getSessionId());
      client.unlockDocument(docId);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
