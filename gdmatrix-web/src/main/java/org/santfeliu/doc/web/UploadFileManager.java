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
package org.santfeliu.doc.web;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.activation.DataHandler;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.TemporaryDataSource;

/**
 *
 * @author blanquepa
 */
public class UploadFileManager implements Serializable
{
  private UploadedFile uploadedFile;
  private String filePath;
  private String fileName;
  private String fileSize;

  private String docTitle;
  private boolean renderTitle;
  private String docTypeId = "Document";
  private boolean renderDocTypeId;
  private String docLanguage;
  private boolean renderDocLanguage;

  private int maxFileSize;
  private List validExtensions;

  public UploadFileManager()
  {
    fileName = null;
    fileSize = null;
    filePath = null;
    renderTitle = true;
    renderDocTypeId = true;
    renderDocLanguage = true;
    docLanguage =
      MatrixConfig.getProperty("org.santfeliu.translation.defaultLanguage");
    if (docLanguage == null)
      docLanguage = Locale.getDefault().getLanguage();
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public String getFilePath()
  {
    return filePath;
  }

  public void setFilePath(String filePath)
  {
    this.filePath = filePath;
  }

  public String getFileSize()
  {
    return fileSize;
  }

  public void setFileSize(String fileSize)
  {
    this.fileSize = fileSize;
  }

  public String getDocTitle()
  {
    return docTitle;
  }

  public void setDocTitle(String docTitle)
  {
    this.docTitle = docTitle;
  }

  public String getDocTypeId()
  {
    return docTypeId;
  }

  public void setDocTypeId(String docTypeId)
  {
    this.docTypeId = docTypeId;
  }

  public String getDocLanguage()
  {
    return docLanguage;
  }

  public void setDocLanguage(String docLanguage)
  {
    this.docLanguage = docLanguage;
  }

  public boolean isRenderDocLanguage()
  {
    return renderDocLanguage;
  }

  public void setRenderDocLanguage(boolean renderDocLanguage)
  {
    this.renderDocLanguage = renderDocLanguage;
  }

  public boolean isRenderDocTypeId()
  {
    return renderDocTypeId;
  }

  public void setRenderDocTypeId(boolean renderDocTypeId)
  {
    this.renderDocTypeId = renderDocTypeId;
  }

  public boolean isRenderTitle()
  {
    return renderTitle;
  }

  public void setRenderTitle(boolean renderTitle)
  {
    this.renderTitle = renderTitle;
  }

  public UploadedFile getUploadedFile()
  {
    return uploadedFile;
  }

  public void setUploadedFile(UploadedFile uploadedFile) throws IOException
  {
    this.uploadedFile = uploadedFile;
  }

  public int getMaxFileSize()
  {
    return maxFileSize;
  }

  public void setMaxFileSize(int maxFileSize)
  {
    this.maxFileSize = maxFileSize;
  }

  public void setMaxFileSize(String sMaxFileSize)
  {
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
        maxFileSize = 0;
      }
    }
  }

  public List getValidExtensions()
  {
    return validExtensions;
  }

  public void setValidExtensions(List validExtensions)
  {
    this.validExtensions = validExtensions;
  }

  public void setValidExtensions(String validExtensions)
  {
    if (validExtensions != null)
    {
      this.validExtensions = new ArrayList();
      String array[] = validExtensions.split(",");
      for (String s : array)
      {
        this.validExtensions.add(s.trim().toLowerCase());
      }
    }
  }

  public void setFileProperties(UploadedFile uploadedFile) throws IOException
  {
    File file = IOUtils.writeToFile(uploadedFile.getInputStream());
    filePath = file.getAbsolutePath();
    fileName = uploadedFile.getName();
    if (fileName != null)
    {
      int index = fileName.lastIndexOf("\\");
      if (index != -1)
      {
        fileName = fileName.substring(index + 1);
      }
      index = fileName.lastIndexOf(".");
      if (index != -1)
      {
        docTitle = fileName.substring(0, index);
      }
      else
      {
        docTitle = fileName;
      }
    }
    fileSize = DocumentUtils.getSizeString(file.length());
  }

  //Actions
  public void uploadFile(UploadedFile uploadedFile) throws Exception
  {
    if (uploadedFile != null)
    {
      this.uploadedFile = uploadedFile;
      if (!isEmptyFile())
        reset();
      setFileProperties(uploadedFile);
    }
  }

  public Document storeFile(Map<String, Object> properties) throws Exception
  {
    Document document = new Document();
    document.setTitle(getDocTitle());
    document.setDocTypeId(getDocTypeId());
    document.setLanguage(getDocLanguage());
    DocumentUtils.setProperties(document, properties);

    Content content = new Content();
    DataHandler dh = new DataHandler(getDataSource());
    content.setData(dh);
    String contentType = dh.getContentType();
    if ("application/octet-stream".equals(contentType))
      contentType = null;
    content.setContentType(contentType);
    content.setContentId(null);
    document.setContent(content);

    Document doc = DocumentConfigBean.getClient().storeDocument(document);

    reset();

    return doc;
  }

  public void cancelUpload()
  {
    reset();
  }

  public boolean isEmptyFile()
  {
    return filePath == null;
  }

  private void reset()
  {
    fileName = null;
    fileSize = null;
    if (filePath != null)
    {
      File file = new File(filePath);
      file.delete();
      filePath = null;
    }
    renderTitle = true;
    renderDocLanguage = true;
    renderDocTypeId = true;
  }

  private TemporaryDataSource getDataSource()
  {
    File file = new File(filePath);
    MimeTypeMap mimeTypeMap = MimeTypeMap.getMimeTypeMap();
    String mimeType = mimeTypeMap.getContentType(fileName);
    return new TemporaryDataSource(file, mimeType);
  }

  public boolean validFileSize(long fileSize)
  {
    return maxFileSize == 0 || maxFileSize >= (int)fileSize;
  }

  public boolean validFileExtension(String filename)
  {
    if (validExtensions == null || validExtensions.isEmpty())
      return true;
    else
    {
      int index = filename.lastIndexOf(".");
      if (index != -1)
      {
        String extension = filename.substring(index + 1).toLowerCase();
        return validExtensions.contains(extension);
      }
      return false;
    }
  }
}
