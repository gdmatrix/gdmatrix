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
package org.santfeliu.doc.uploader;

import org.santfeliu.matrix.ide.DocumentUploaderPanel;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.santfeliu.util.script.DefaultScriptable;


/**
 *
 * @author real
 */
public class Scanner extends SwingWorker<List<DocumentInfo>, DocumentInfo>
{
  DocumentUploaderPanel uploader;
  File dir;
  FileFilter fileFilter;
  String filter;
  String properties;
  LinkedList<File> directories = new LinkedList<File>();
  int dirCount;

  public Scanner(DocumentUploaderPanel uploader, File dir, String pattern, 
    String filter, String properties)
  {
    this.uploader = uploader;
    this.dir = dir;
    this.properties = properties;
    if ("".equals(pattern)) pattern = null;
    this.fileFilter = new ScanFileFilter(pattern);
    if (filter != null && filter.length() > 0)
    {
      this.filter = filter;
    }
  }

  @Override
  protected List<DocumentInfo> doInBackground() throws Exception
  {
    uploader.getDocuments().clear();
    directories.clear();
    directories.add(dir);
    dirCount = 0;
    while (!directories.isEmpty() && !isCancelled())
    {
      File currentDir = directories.remove(0);
      exploreDirectory(currentDir);
      dirCount++;
      updateProgress();
    }
    return uploader.getDocuments();
  }

  @Override
  public void process(List<DocumentInfo> docInfos)    
  {
    for (DocumentInfo docInfo : docInfos)
    {
      FileInfo fileInfo = docInfo.getFile();
      String basePath = dir.getAbsolutePath();
      String path = fileInfo.getPath().substring(basePath.length());
      
      uploader.getDocumentsTableModel().addRow(new Object[]{ 
        fileInfo.getState(), fileInfo.getPosition(),
        path, fileInfo.getLength()});
    }
  }

  @Override
  public void done()            
  {
    int docCount = uploader.getDocuments().size();

    String message;
    if (isCancelled())
    {
      message = "Cancelled.";
    }
    else
    {
      message = "Done.";
    }
    uploader.setStatus(message + " " + docCount + " files found.");
    uploader.setButtonsEnabled(true);
    uploader.showStatusPanel(200);
  }

  private void exploreDirectory(File currentDir)
  {
    File[] files = currentDir.listFiles(fileFilter);
    if (files == null) return;
    Arrays.sort(files, new Comparator<File>()
    {
      @Override
      public int compare(File o1, File o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });
    int i = 0;
    int position = 0;
    while (i < files.length && !isCancelled())
    {
      File file = files[i];
      if (file.isDirectory())
      {
        directories.add(file);
      }
      else
      {
        FileInfo fileInfo = new FileInfo(file, position);
        if (filter == null || evaluateFilter(filter, fileInfo))
        {
          position++;

          DocumentInfo docInfo = new DocumentInfo(fileInfo);
          if (properties != null && properties.length() > 0)
          {
            evaluateProperties(properties, docInfo);
          }
          uploader.getDocuments().add(docInfo);
          publish(docInfo);
        }
      }
      i++;
    }
  }

  private boolean evaluateFilter(String filter, FileInfo fileInfo)
  {
    Context ctx = ContextFactory.getGlobal().enterContext();
    try
    {
      Scriptable scope = new DefaultScriptable(ctx);
      scope.put("file", scope, 
        new NativeJavaObject(scope, fileInfo, FileInfo.class));
      Object result = ctx.evaluateString(scope, filter, "<filter>", 1, null);
      return Context.toBoolean(result);
    }
    catch (Exception ex)
    {
      return false;
    }
    finally
    {
      Context.exit();
    }
  }

  private void evaluateProperties(String properties, DocumentInfo docInfo)
  {
    Context ctx = ContextFactory.getGlobal().enterContext();
    try
    {
      FileInfo fileInfo = docInfo.getFile();
      Scriptable scope = new DefaultScriptable(ctx);
      scope.put("document", scope, 
        new NativeJavaObject(scope, docInfo, DocumentInfo.class));
      scope.put("file", scope, 
        new NativeJavaObject(scope, fileInfo, FileInfo.class));
      ctx.evaluateString(scope, properties, "<properties>", 1, null);        
    }
    catch (Exception ex)
    {
    }
    finally
    {
      Context.exit();
    }
  }

  private void updateProgress()
  {
    final int total = dirCount + directories.size();
    final int percentCompleted = 
      (int)(100.0 * ((double)dirCount / (double)total));    
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        JProgressBar progressBar = uploader.getProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        int currentPercent = progressBar.getValue();
        if (percentCompleted > currentPercent)
        {
          progressBar.setValue(percentCompleted);
          progressBar.setString("Scanning..." + percentCompleted + "%");
        }
      }
    });
  }
}

class ScanFileFilter implements FileFilter
{
  Pattern pattern;

  ScanFileFilter()
  {
  }

  ScanFileFilter(String filePattern)
  {
    if (filePattern != null)
    {
      pattern = Pattern.compile(filePattern);
    }
  }

  @Override
  public boolean accept(File file)
  {
    if (file.isDirectory())
    {
      return true;
    }
    else if (file.getName().endsWith("." + UploadInfo.UPLOAD_FILE_EXTENSION))
    {
      return false;
    }
    else if (pattern != null)
    {
      String path = file.getAbsolutePath();
      path = path.replace("\\", "/");
      return pattern.matcher(path).matches();
    }
    return true;
  }
}

