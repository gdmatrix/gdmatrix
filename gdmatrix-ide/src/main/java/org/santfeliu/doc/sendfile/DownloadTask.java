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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import javax.swing.JOptionPane;
import org.santfeliu.swing.ProgressDialog;

/**
 *
 * @author realor
 */
public class DownloadTask extends BaseHeavyTask
{
  private String docId;
  private File file;
  private String language;
  private int length;
  private int totalRead;
  private ProgressDialog progressDialog;
  private boolean editionCanceled = false;

  public DownloadTask(SendFileApplet applet)
  {
    super(applet);
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(String language)
  {
    this.language = language;
  }

  @Override
  public void execute()
  {
    System.out.println("DownloadTask started");
    boolean documentLocked = false;
    try
    {
      // show showOverlay
      applet.showOverlay();

      // FIX: Lock document before load
      System.out.println("lockDocument " + docId);
      client.lockDocument(docId);
      documentLocked = true;
      
      Map document = client.loadDocument(docId);
      System.out.println(document);
      String contentType = (String)document.get("contentType");
 
      file = FileMonitor.getFileFor(docId, contentType);
      if (FileMonitor.backupPreviousFile(file))
      {
        invokeAndWait("initProgressDialog");
        try
        {
          byte block[] = new byte[4096];
          String contentId = (String)document.get("contentId");
          URL url = new URL(applet.getServletURL() + "/" + contentId);
          System.out.println("Downloading " + url);
          URLConnection urlConn = url.openConnection();
          length = urlConn.getContentLength();
          invokeLater("setProgressLength");
          System.out.println("Length: " + length);
          System.out.println("Downloading to " + file);
          FileOutputStream os = new FileOutputStream(file);
          try
          {
            InputStream is = url.openStream();
            try
            {
              int numRead = is.read(block);
              while (numRead > 0 && !progressDialog.isCanceled())
              {
                totalRead += numRead;
                os.write(block, 0, numRead);
                invokeLater("notifyProgress");
                numRead = is.read(block);
              }
              editionCanceled = progressDialog.isCanceled();
            }
            finally
            {
              is.close();
            }
          }
          finally
          {
            os.close();
          }
        }
        finally
        {
          invokeLater("closeProgressDialog");
        }
      }
      else // can't backup previous file, already open?
      {
        invokeAndWait("askContinueEdition");
      }

      // launch application to open file or end
      if (editionCanceled)
      {
        if (documentLocked) unlockDocument();
        applet.hideOverlay();
      }
      else
      {
        launchApplication(file);
        applet.showEditPanel(
          applet.getLocalizedMessage("EditingFile") + " " +
          file.getCanonicalPath() + "...",
          applet.getLocalizedMessage("EditButtonLabel"));
        applet.createFileMonitor(docId, file);
      }
    }
    catch (Exception ex)
    {
      if (documentLocked) unlockDocument();
      showError(ex);
      applet.hideOverlay();
    }
  }

  public void initProgressDialog()
  {
    String title = applet.getLocalizedMessage("Downloading") + " " + docId;
    progressDialog = new ProgressDialog(title, 400, 120);
    progressDialog.setMinimum(0);
    progressDialog.setMaximum(1000);
    progressDialog.setStatus(applet.getLocalizedMessage("Requesting") + "...");
    progressDialog.setLocationRelativeTo(null);
    progressDialog.setVisible(true);
  }

  public void setProgressLength()
  {
    progressDialog.setStatus(applet.getLocalizedMessage("Downloading") + "...");
    progressDialog.setMaximum((int)length);
  }

  public void notifyProgress()
  {
    progressDialog.setValue(totalRead);
    progressDialog.setStatus(applet.getLocalizedMessage("Downloading") + " (" +
      getSizeString(totalRead) + " / " + getSizeString(length) + ")...");
  }

  public void closeProgressDialog()
  {
    progressDialog.setVisible(false);
    progressDialog.dispose();
  }

  public void askContinueEdition()
  {
    int option = JOptionPane.showConfirmDialog(null,
      applet.getLocalizedMessage("FileAlreadyOpen"),
      applet.getLocalizedMessage("Warning"), 
      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    editionCanceled = option != JOptionPane.OK_OPTION;
  }

  private void launchApplication(File file) throws IOException
  {
    final String os = System.getProperty("os.name");
    if (os.startsWith("Windows"))
    {
      Runtime.getRuntime().exec(
        new String[]{"cmd", "/c", "start", "\"\"", file.getCanonicalPath()});
    }
    else if (os.startsWith("Mac OS"))
    {
      Runtime.getRuntime().exec(
        new String[]{"open", file.getCanonicalPath()});
    }
    else if (os.startsWith("Linux") && gnomeRunning())
    {
      Runtime.getRuntime().exec(
        new String[]{"gnome-open", file.getCanonicalPath()});
    }
    else
    {
      throw new IOException("unknown way to open " + file);
    }
  }

  private static final boolean gnomeRunning()
  {
    try
    {
      return Runtime.getRuntime().exec(
        new String[]{ "pgrep", "-u",
        System.getProperty("user.name"), "nautilus" }).waitFor() == 0;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  private void unlockDocument()
  {
    System.out.println("unlockDocument " + docId);
    try
    {
      client.unlockDocument(docId);
    }
    catch (Exception ex2)
    {
    }
  }
}
