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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.santfeliu.util.enc.HtmlDecoder;

/**
 *
 * @author unknown
 */
public class SendFileApplet extends JApplet
{
  // parameters
  private String sessionId;
  private String servletURL;
  private String language;
  private int maxFileSize = 0; // 0: no limit
  private Set<String> validExtensions = new HashSet<String>();
  private List<SelectItem> languages = new ArrayList<SelectItem>();
  private List<SelectItem> documentTypes = new ArrayList<SelectItem>();
  private Map documentProperties = new HashMap();
  private boolean overlayActive = false;
  private FileMonitor fileMonitor;  
  static ResourceBundle bundle;

  public String getSessionId()
  {
    return sessionId;
  }
  
  public String getLanguage()
  {
    return language;
  }

  public String getServletURL()
  {
    return servletURL;
  }

  public int getMaxFileSize()
  {
    return maxFileSize;
  }

  public Set<String> getValidExtensions()
  {
    return validExtensions;
  }

  public Map getDocumentProperties()
  {
    return documentProperties;
  }

  public List<SelectItem> getDocumentTypes()
  {
    return documentTypes;
  }

  public List<SelectItem> getLanguages()
  {
    return languages;
  }

  public void setDocType(String docTypeId, String docTypeDesc)
  {
    SelectItem docType = new SelectItem(docTypeId,
      HtmlDecoder.decode(docTypeDesc));
    if (documentTypes != null && !documentTypes.contains(docType))
      documentTypes.add(docType);
  }

  public void setParameter(String name, String value)
  {
    if (documentProperties != null)
    {
      Object prop = documentProperties.get(name);
      if (prop == null)
      {
        List list = new ArrayList();
        list.add(value);
        documentProperties.put(name, list);
      }
      else
      {
        if (prop instanceof Collection)
        {
          Collection values = (Collection)prop;
          if (!values.contains(value))
            values.add(value);
        }
      }
    }
    System.out.println(documentProperties);
  }

  // ************* standard applet methods ***************
  @Override
  public void init()
  {
    System.out.println(">>> init");

    // read sessionId parameter
    sessionId = getParameter("sessionId");

    // read servletURL parameter
    servletURL = getParameter("servletURL");

    // read language parameter
    language = getParameter("language");
    
    // read maxFileSize parameter
    String sMaxFileSize = getParameter("maxFileSize");
    if (sMaxFileSize != null)
    {
      try
      {
        sMaxFileSize = sMaxFileSize.trim().toLowerCase();
        int length = sMaxFileSize.length();
        if (sMaxFileSize.endsWith("mb"))
        {
          sMaxFileSize = sMaxFileSize.substring(0, length - 2);
          maxFileSize = Integer.parseInt(sMaxFileSize) * 1024 * 1024;
        }
        else if (sMaxFileSize.endsWith("kb"))
        {
          sMaxFileSize = sMaxFileSize.substring(0, length - 2);
          maxFileSize = Integer.parseInt(sMaxFileSize) * 1024;
        }
        else // bytes
        {
          maxFileSize = Integer.parseInt(sMaxFileSize);
        }
      }
      catch (NumberFormatException ex)
      {
        ex.printStackTrace();
      }
    }

    // read validExtensions parameter
    String exts = getParameter("validExtensions");
    if (exts != null)
    {
      String array[] = exts.split(",");
      for (String s : array)
      {
        validExtensions.add(s.trim().toLowerCase());
      }
    }

    Locale locale = null;
    if (language == null)
      locale = Locale.getDefault();
    else
      locale = new Locale(language);
    bundle = ResourceBundle.getBundle(
      "org.santfeliu.doc.sendfile.resources.SendFileBundle", locale);
    Locale.setDefault(locale);

    String[] languageArray = new String[]
      {"ca", "es", "en", "fr", "de", "it", "pt", "ru", "ar", "zh", "ro", "bg"};

    for (String lang : languageArray)
    {
      SelectItem selectItem = new SelectItem(lang,
        new Locale(lang).getDisplayLanguage().toUpperCase());
      languages.add(selectItem);
    }
    languages.add(new SelectItem("%%", "UNIVERSAL"));
    initSystemLookAndFeel();
  }

  @Override
  public void start()
  {
    System.out.println(">>> start");
  }

  @Override
  public void stop()
  {
    System.out.println(">>> stop");
  }
  
  @Override
  public void destroy()
  {
    System.out.println(">>> destroy");
    if (fileMonitor != null)
    {
      killFileMonitor();
    }
  }


  // **************** SendFileApplet actions ***************
  public void sendFile()
  {
    UploadTask task = new UploadTask(this);
    task.setCommand("sendFile");
    task.executeInBackground();
  }

  public void updateFile(String docId)
  {
    UploadTask task = new UploadTask(this);
    task.setDocId(docId);
    task.setCommand("updateFile");
    task.executeInBackground();
  }

  public void editFile(String docId, String language)
  {
    DownloadTask task = new DownloadTask(this);
    task.setDocId(docId);
    task.setLanguage(language);
    task.executeInBackground();
  }

  public void stopEditing()
  {
    if (fileMonitor != null)
    {
      String docId = fileMonitor.getDocId();
      killFileMonitor();
      notifyEndOfTransmission("editFile", docId);
    }
  }

  public void configure()
  {
  }

  // ****************** callback functions ******************

  void notifyEndOfTransmission(String command, String docId)
  {
    try
    {
      String urlString = "javascript:htmlSendFile_endTransmission('" +
        command + "','" + docId + "');";
      System.out.println(urlString);
      URL url = new URL(urlString);
      getAppletContext().showDocument(url);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  void showOverlay()
  {
    try
    {
      if (!overlayActive)
      {
        String urlString = "javascript:htmlSendFile_showOverlay();";
        System.out.println(urlString);
        URL url = new URL(urlString);
        getAppletContext().showDocument(url);
        overlayActive = true;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  void hideOverlay()
  {
    try
    {
      if (overlayActive)
      {
        String urlString = "javascript:htmlSendFile_hideOverlay();";
        System.out.println(urlString);
        URL url = new URL(urlString);
        getAppletContext().showDocument(url);
        overlayActive = false;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  void showEditPanel(final String message, final String buttonLabel)
  {
    try
    {
      String text = message.replace('\\', '/');
      URL url = new URL("javascript:htmlSendFile_showEditPanel(\""
        + text + "\", \"" + buttonLabel + "\");");
      getAppletContext().showDocument(url);
    }
    catch (Exception ex)
    {
    }
  }

  // internal methods

  void createFileMonitor(String docId, File file)
  {
    fileMonitor = new FileMonitor(this, docId, file);
    fileMonitor.start();
  }

  void killFileMonitor()
  {
    if (fileMonitor != null)
    {
      fileMonitor.end();
      while (fileMonitor.isAlive())
      {
        Thread.yield();
      }
      fileMonitor = null;
    }
  }

  String getLocalizedMessage(String key)
  {
    String value = null;
    try
    {
      value = bundle.getString(key);
    }
    catch (Exception ex)
    {
      value = key;
    }
    return value;
  }

  private void initSystemLookAndFeel()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        try
        {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ex)
        {
        }
      }
    });
  }
}
