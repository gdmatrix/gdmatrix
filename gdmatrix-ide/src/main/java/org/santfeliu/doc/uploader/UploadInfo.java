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
import org.apache.commons.lang.StringUtils;

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
  String error;

  public UploadInfo(File sourceFile)
  {
    this.file = new File(sourceFile.getAbsolutePath() + "." +
      UPLOAD_FILE_EXTENSION);

    this.read();
  }

  public boolean isUploaded()
  {
    return file.exists();
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public long getLastModified()
  {
    return lastModified;
  }

  public void setLastModified(long lastModified)
  {
    this.lastModified = lastModified;
  }

  public String getError()
  {
    return error;
  }

  public void setError(String error)
  {
    this.error = error;
  }

  public final void read()
  {
    if (!file.exists()) return;
    try (BufferedReader reader = new BufferedReader(new FileReader(file)))
    {
      docId = reader.readLine();
      if (docId == null) return;

      String lm = reader.readLine();
      if (lm == null) return;
      lastModified = Long.parseLong(lm);

      caseId = reader.readLine();
      if (caseId == null) return;

      if ("-".equals(caseId)) caseId = null;

      StringBuilder buffer = new StringBuilder();
      String line = reader.readLine();
      while (line != null)
      {
        buffer.append(line).append("\n");
        line = reader.readLine();
      }
      error = buffer.toString();
      if (StringUtils.isBlank(error)) error = null;
    }
    catch (Exception ex)
    {
      error = ex.toString();
    }
  }

  public final void write()
  {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
    {
      writer.write(docId);
      writer.newLine();
      writer.write(String.valueOf(lastModified));
      writer.newLine();
      if (caseId == null)
      {
        writer.write("-");
      }
      else
      {
        writer.write(caseId);
      }
      writer.newLine();
      if (error != null)
      {
        writer.write(error);
      }
      writer.newLine();
    }
    catch (Exception ex)
    {
      error = ex.toString();
    }
  }

  public void delete()
  {
    file.delete();
    docId = null;
    caseId = null;
    lastModified = 0;
    error = null;
  }
}
