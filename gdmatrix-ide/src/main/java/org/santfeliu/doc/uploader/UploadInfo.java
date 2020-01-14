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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author realor
 */
public class UploadInfo
{
  public static final String UPLOAD_FILE_EXTENSION = "upl";
  File file;
  String docId;
  String caseId;
  long lastModified;
  boolean synced;
  Throwable error;
  
  public UploadInfo(File sourceFile)
  {
    this.file = new File(sourceFile.getAbsolutePath() + "." + 
      UPLOAD_FILE_EXTENSION);
  }
  
  public boolean isUploaded()
  {
    return file.exists();
  }

  public String getDocId()
  {
    if (!synced) read();
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
    synced = false;
  }

  public String getCaseId()
  {
    if (!synced) read();
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
    synced = false;
  }

  public long getLastModified()
  {
    if (!synced) read();
    return lastModified;
  }

  public void setLastModified(long lastModified)
  {
    this.lastModified = lastModified;
    synced = false;
  }
  
  public Throwable getError()
  {
    return error;
  }

  public void setError(Throwable error)
  {
    this.error = error;
  }
  
  public void read()
  {
    if (!file.exists()) return;
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      docId = reader.readLine();
      lastModified = Long.parseLong(reader.readLine());
      caseId = reader.readLine();
      synced = true;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public void write()
  {
    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(docId);
      writer.newLine();
      writer.write(String.valueOf(lastModified));
      writer.newLine();
      if (caseId != null)
      {
        writer.write(caseId);
        writer.newLine();
      }
      writer.close();
      synced = true;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public void delete()
  {
    file.delete();
    docId = null;
    caseId = null;
    lastModified = 0;
    synced = false;
  }
}
