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
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.servlet.http.HttpServletRequest;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.faces.matrixclient.model.DefaultMatrixClientModel;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.TemporaryDataSource;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.obj.PageBean;
 
/**
 *
 * @author unknown
 */
public class DocumentContentBean extends PageBean
{
  private static final String FLAGS_PATH_URL = "/common/translation/images/flags/";
  private static final String IMAGE_PATH_URL = "/common/doc/images/";
  private static final String DOC_SERVLET_URL = "/documents/";

  private Content content;
  private String title;
  
  private TempFile tempFile = new TempFile();
  private UploadedFile uploadedFile;
  private String contentUrl;
  private String contentId;
  
  private DefaultMatrixClientModel model;

  public DocumentContentBean()
  {
    load(false);
    model = new DefaultMatrixClientModel();
  }

  @Override
  public String show()
  {
    return "document_content";
  }

  public String refresh()
  {
    load(true);
    return "document_content";
  }

  @Override
  public String store()
  {
    if (content != null)
    {
      load(false);
    }
    return show();
  }

  public String storeContent()
  {
    try
    {
      createContent();
      DocumentMainBean documentMainBean = getDocumentMainBean(false);
      Document document = documentMainBean.getDocument();
      document.setContent(content);
      documentMainBean.store();

      Document storedDocument = documentMainBean.getDocument();

      String objectId =
        DocumentConfigBean.toObjectId(storedDocument.getDocId(), storedDocument.getVersion());
      getControllerBean().show(getSelectedMenuItem().getMid(), objectId);
      refresh();
    }
    catch (Exception ex)
    {
      error("Error a l'adjuntar el contingut. " + ex.getMessage());
      DocumentMainBean documentMainBean = getDocumentMainBean(true);
      Document document = documentMainBean.getDocument();
      content = document.getContent();
    }
    return null;
  }

  public String cancelNewContent()
  {
    tempFile.reset();
    return null;
  }

  public void uploadFile(javax.faces.event.ValueChangeEvent ev)
  {
    try
    {
      if (ev.getNewValue() != null)
      {
        this.uploadedFile = (UploadedFile) ev.getNewValue();
        if (!tempFile.isEmptyFile())
          tempFile.reset();
        tempFile.setUploadedFile(uploadedFile);
      }
    }
    catch (Exception ex)
    {
      error("Error a l'adjuntar el contingut. " + ex.getMessage());
    }
  }

  public String getContentId()
  {
    return contentId;
  }

  public void setContentId(String contentId)
  {
    this.contentId = contentId;
  }

  public String getContentUrl()
  {
    return contentUrl;
  }

  public void setContentUrl(String contentUrl)
  {
    this.contentUrl = contentUrl;
  }

  public UploadedFile getUploadedFile()
  {
    return uploadedFile;
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    this.uploadedFile = uploadedFile;
  }

  public Content getContent()
  {
    return content;
  }

  public void setContent(Content content)
  {
    this.content = content;
  }

  public String getContentLanguage()
  {
    if (content == null)
      return null;
    
    return DocumentUtils.extendLanguage(content.getLanguage());
  }

  public String getLanguageFlag()
  {
    if (content == null)
      return null;
    
    return DocumentUtils.getLanguageFlag(FLAGS_PATH_URL, getContent().getLanguage());
  }

  public String getContentStorageType()
  {
    if (content == null)
      return "NO CONTENT";
    if (content.getUrl() != null)
      return "EXTERNAL";
    else
      return "INTERNAL";
  }

  public String getContentSize()
  {
    long size = 0;
    
    if (content != null)
      size = content.getSize();
    
    return DocumentUtils.getSizeString(size);
  }

  public String getContentTypeImage()
  {
    if (content == null)
      return null;
    else
      return DocumentBean.getContentTypeIcon(content.getContentType());
  }

  public Date getCaptureDateTime()
  {
    if (content != null && content.getCaptureDateTime() != null)
    {
      try
      {
        return new SimpleDateFormat("yyyyMMddHHmmss").parse(content.getCaptureDateTime());
      }
      catch (ParseException ex)
      {
        Logger.getLogger(DocumentContentBean.class.getName()).log(Level.INFO, null, ex);
        return null;
      }
    }
    else
      return null;
  }

  public String getDocumentURL()
  {
    return getDocumentURL(false, false);
  }
  
  public String getDownloadURL()
  {
    return getDocumentURL(true, false);
  }

  public String getFullURL()
  {
    return getDocumentURL(false, true, 10);
  }

  public String getPreviewURL()
  {
    if (content == null)
      return null;

    String contextPath = getExternalContext().getRequestContextPath();
    String contentType = content.getContentType();
    if (contentType != null && contentType.startsWith("image"))
      return getDocumentURL(false, false);
    else
      return contextPath + IMAGE_PATH_URL + "previewdoc.png";
  }

  public TempFile getTempFile()
  {
    return tempFile;
  }

  public void setTempFile(TempFile tempFile)
  {
    this.tempFile = tempFile;
  }

  public Content createContent() throws Exception
  {
    if (!tempFile.isEmptyFile())
    { //File
      content = new Content();

      DataHandler dh = new DataHandler(tempFile.getDataSource());
      content.setData(dh);
      String contentType = dh.getContentType();
      if ("application/octet-stream".equals(contentType))
        contentType = null;
      content.setContentType(contentType);
      content.setContentId(null);
    }
    else if (contentUrl != null && contentUrl.length() > 0)
    { //Url
      content = new Content();
      content.setUrl(contentUrl);
      URL url = new URL(contentUrl);
      URLDataSource ds = new URLDataSource(url);
      content.setContentType(ds.getContentType());
      content.setContentId(null);
      ds = null;
    }
    else if (contentId != null && contentId.length() > 0)
    { //ContentId
      content = new Content();
      content.setContentId(contentId);
    }
    return content;
  }

  public boolean isRenderMetadata()
  {
    return (isNew() || !tempFile.isEmptyFile());
  }

  public DefaultMatrixClientModel getModel() 
  {
    return model;
  }

  public void setModel(DefaultMatrixClientModel model) 
  {
    this.model = model;
  }
  
  public String documentEdited()
  {
    try
    {
      model.parseResult();
      return refresh();
    }
    catch (Exception ex)
    {
      error(ex);
    }

    return null;
  }  

  private String getDocumentURL(boolean downloadable, boolean fullUrl)
  {
    return getDocumentURL(downloadable, fullUrl, 0);
  }

  private String getDocumentURL(boolean downloadable, boolean fullUrl,
    int filenameLength)
  {
    if (content == null)
      return null;

    if (content.getUrl() != null)
      return content.getUrl();
    else
    {
      HttpServletRequest request = (HttpServletRequest)getExternalContext().getRequest();
      String contextPath = request.getContextPath();
      String serverName = HttpUtils.getServerName(request);
      String port = MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
      port = !"80".equals(port) ? ":" + port : "";

      String extension =
        MimeTypeMap.getMimeTypeMap().getExtension(content.getContentType());
      if (title == null)
        title = "document";

      String filename = DocumentUtils.getFilename(title);
      if (filename != null && filenameLength > 0 && filename.length() > filenameLength)
        filename = filename.substring(0, filenameLength - 1);
      filename = filename + "." + extension;

      String url = contextPath + DOC_SERVLET_URL + content.getContentId() + "/" + filename;
      if (fullUrl)
        url = "http://" + serverName + port + url;
      if (downloadable)
        url = url + "?saveas=" + filename;

      return url;
    }
  }

  private void load(boolean refreshDocument)
  {
    try
    {
      tempFile.reset();
      if (!isNew())
      {
        DocumentMainBean documentMainBean = getDocumentMainBean(refreshDocument);
        if (documentMainBean != null)
        {
          Document document = documentMainBean.getDocument();
          content = document.getContent();
          title = document.getTitle();
        }

        if (content != null)
          content.setData(null); //content.data not transient
      }
      else
      {
        content = new Content();
      }
    }
    catch (Exception e)
    {
      error(e.getMessage());
    }
  }

  private DocumentMainBean getDocumentMainBean(boolean refreshDocument)
  {
    DocumentMainBean documentMainBean =
      (DocumentMainBean)getBean("documentMainBean");

    if (documentMainBean != null && refreshDocument)
    {
      boolean createNewVersion = ((DocumentBean)getObjectBean()).isCreateNewVersion();
      documentMainBean.reload(createNewVersion);
    }

    return documentMainBean;
  }

  public class TempFile implements Serializable
  {
    private String filePath;
    private String fileName;
    private String fileSize;

    public TempFile()
    {
      fileName = null;
      fileSize = null;
      filePath = null;
    }

    public void setUploadedFile(UploadedFile uploadedFile) throws IOException
    {
      File file = IOUtils.writeToFile(uploadedFile.getInputStream());
      System.out.println("File uploaded: " + file.getName() +
        ", size: " + file.length() + " bytes");
      filePath = file.getAbsolutePath();
      fileName = uploadedFile.getName();
      fileSize = DocumentUtils.getSizeString(file.length());
    }

    public void reset()
    {
      fileName = null;
      fileSize = null;
      if (filePath != null)
      {
        File file = new File(filePath);
        file.delete();
        filePath = null;
      }
    }

    public boolean isEmptyFile()
    {
      return filePath == null;
    }

    public TemporaryDataSource getDataSource()
    {
      File file = new File(filePath);
      MimeTypeMap mimeTypeMap = MimeTypeMap.getMimeTypeMap();
      String mimeType = mimeTypeMap.getContentType(fileName);
      return new TemporaryDataSource(file, mimeType);
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
  }


}
