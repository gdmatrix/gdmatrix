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
package org.santfeliu.webapp.modules.assistant;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.assistant.openai.Assistant;
import org.santfeliu.webapp.modules.assistant.openai.File;
import static org.santfeliu.webapp.modules.assistant.openai.File.ASSISTANTS_PURPOSE;
import org.santfeliu.webapp.modules.assistant.openai.OpenAI;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class AssistantFilesBean extends WebBean implements Serializable
{
  public static final String ASSISTANT_FILE_DOCTYPEID = "AssistantFile";
  public static final String ASSISTANT_FILE_ID_PROPERTY = "fileId";
  public static final String ASSISTANT_FILE_NAME_PROPERTY = "filename";

  int phase = 0;
  String editingFileId;
  String editingFilename;
  String lastFileId;
  String docId;
  int docVersion;
  String contentId;
  String fileChangeDateTime;
  String extension;
  Long fileSize;
  boolean invalidFileId;

  transient OpenAI openAI = new OpenAI();
  transient HashMap<String, File> fileCache = new HashMap<>();

  @Inject
  AssistantBean assistantBean;

  @PostConstruct
  public void init()
  {
    String apiKey = MatrixConfig.getProperty("openai.apiKey");
    openAI.setApiKey(apiKey);
  }

  public File getFile(String fileId)
  {
    File file = fileCache.get(fileId);
    if (file == null)
    {
      try
      {
        file = openAI.retrieveFile(fileId);
      }
      catch (Exception ex)
      {
        file = new File();
        file.setId(fileId);
        file.setFilename("");
        file.setPurpose(ASSISTANTS_PURPOSE);
      }
      fileCache.put(fileId, file);
    }
    return file;
  }

  public String getEditingFileId()
  {
    return editingFileId;
  }

  public String getEditingFilename()
  {
    return editingFilename;
  }

  public void setEditingFilename(String filename)
  {
    editingFilename = filename;
  }

  public void addFile()
  {
    editingFileId = null;
    editingFilename = null;
    fileChangeDateTime = null;
    lastFileId = null;
    docId = null;
    contentId = null;
    invalidFileId = false;
    extension = null;
    phase = 1;
    assistantBean.setDialogVisible(true);
  }

  public void editFile(String fileId)
  {
    editingFileId = fileId;
    File file = getFile(fileId);
    editingFilename = file.getFilename();
    lastFileId = file.getId();
    loadFilename(editingFilename);
    phase = 2;
    assistantBean.setDialogVisible(true);
  }

  public void removeFile(String fileId)
  {
    Assistant assistant = assistantBean.getAssistant();
    assistant.getFileIds().remove(fileId);
  }

  public List<String> completeFilename(String name)
  {
    List<String> results = new ArrayList<>();
    try
    {
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(ASSISTANT_FILE_DOCTYPEID);
      Property property = new Property();
      property.setName(ASSISTANT_FILE_NAME_PROPERTY);
      property.getValue().add("%" + name + "%");
      filter.getProperty().add(property);
      filter.getOutputProperty().add(ASSISTANT_FILE_NAME_PROPERTY);
      List<Document> documents =
        assistantBean.getDocPort().findDocuments(filter);
      for (Document document : documents)
      {
        String value = DictionaryUtils.getPropertyValue(document.getProperty(),
          ASSISTANT_FILE_NAME_PROPERTY);
        results.add(value);
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
    return results;
  }

  public void loadFilename(String filename)
  {
    try
    {
      docId = null;
      docVersion = 0;
      contentId = null;
      fileChangeDateTime = null;
      fileSize = null;
      invalidFileId = true;
      if (!StringUtils.isBlank(filename))
      {
        DocumentFilter filter = new DocumentFilter();
        filter.setDocTypeId(ASSISTANT_FILE_DOCTYPEID);
        Property property = new Property();
        property.setName(ASSISTANT_FILE_NAME_PROPERTY);
        property.getValue().add(filename);
        filter.getProperty().add(property);
        filter.getOutputProperty().add(ASSISTANT_FILE_ID_PROPERTY);
        filter.setIncludeContentMetadata(true);
        List<Document> documents =
          assistantBean.getDocPort().findDocuments(filter);
        if (!documents.isEmpty())
        {
          Document document = documents.get(0);
          loadDocumentProperties(document);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void uploadFile(FileUploadEvent event)
  {
    UploadedFile fileToUpload = event.getFile();

    java.io.File fileToStore = null;
    try
    {
      String filename = fileToUpload.getFileName();
      int index = filename.lastIndexOf(".");
      extension = index == -1 ? "bin" : filename.substring(index + 1);

      fileToStore = java.io.File.createTempFile("assistant", "." + extension);

      try (InputStream is = fileToUpload.getInputStream())
      {
        IOUtils.writeToFile(is, fileToStore);
      }

      String contentType =
        MimeTypeMap.getMimeTypeMap().getContentType(fileToStore);

      File openAIFile = openAI.uploadFile(fileToStore,
        editingFilename, contentType, ASSISTANTS_PURPOSE);

      lastFileId = openAIFile.getId();
      invalidFileId = false;

      Document document = new Document();
      document.setDocId(docId);
      document.setVersion(DocumentConstants.NEW_VERSION);
      document.setTitle(editingFilename);
      document.setDocTypeId(ASSISTANT_FILE_DOCTYPEID);

      Property property = new Property();
      property.setName(ASSISTANT_FILE_ID_PROPERTY);
      property.getValue().add(lastFileId);
      document.getProperty().add(property);

      property = new Property();
      property.setName(ASSISTANT_FILE_NAME_PROPERTY);
      property.getValue().add(editingFilename);
      document.getProperty().add(property);

      Content content = new Content();
      content.setData(new DataHandler(new FileDataSource(fileToStore)));
      content.setContentType(contentType);
      document.setContent(content);
      document = assistantBean.getDocPort().storeDocument(document);

      docId = document.getDocId();
      docVersion = document.getVersion();
      fileChangeDateTime = document.getChangeDateTime();
      fileSize = document.getContent().getSize();
      growl("DOCUMENT_UPLOADED", new Object[]{ docId });
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      if (fileToStore != null)
      {
        fileToStore.delete();
      }
    }
  }

  public void purgeFile()
  {
    if (docId != null)
    {
      DocumentManagerPort docPort = assistantBean.getDocPort();
      Document document = docPort.loadDocument(docId,
        DocumentConstants.LAST_VERSION, ContentInfo.METADATA);
      loadDocumentProperties(document);

      DocumentFilter filter = new DocumentFilter();
      filter.getDocId().add(docId);
      filter.setVersion(DocumentConstants.FIND_ALL_VERSIONS);
      filter.getOutputProperty().add(ASSISTANT_FILE_ID_PROPERTY);
      List<Document> versions = docPort.findDocuments(filter);
      for (Document version : versions)
      {
        int v = version.getVersion();
        String fileId = DictionaryUtils.getPropertyValue(version.getProperty(),
          ASSISTANT_FILE_ID_PROPERTY);
        if (v != docVersion)
        {
          try
          {
            docPort.removeDocument(docId, v);
            openAI.deleteFile(fileId);
          }
          catch (Exception ex)
          {
            // ignore
          }
        }
      }
      growl("DOCUMENT_PURGED");
    }
  }

  public int getPhase()
  {
    return phase;
  }

  public void setPhase(int phase)
  {
    if (phase == 2)
    {
      loadFilename(editingFilename);
    }
    else
    {
      docId = null;
      contentId = null;
      fileChangeDateTime = null;
      lastFileId = null;
      invalidFileId = false;
      extension = null;
      fileSize = null;
    }
    this.phase = phase;
  }

  public void acceptFile()
  {
    List<String> fileIds = assistantBean.getAssistant().getFileIds();
    if (lastFileId != null && !fileIds.contains(lastFileId))
    {
      fileIds.add(lastFileId);
      if (editingFileId != null)
      {
        fileIds.remove(editingFileId);
      }
    }
    resetFile();
  }

  public void cancelFile()
  {
    resetFile();
  }

  public void resetFile()
  {
    docId = null;
    contentId = null;
    editingFilename = null;
    editingFileId = null;
    fileChangeDateTime = null;
    lastFileId = null;
    fileSize = null;
    phase = 0;
    assistantBean.setDialogVisible(true);
  }

  public String getDocIdAndVersion()
  {
    return docId == null ? "" : docId + " / " + docVersion;
  }

  public String getContentId()
  {
    return contentId;
  }

  public boolean isInvalidFileId()
  {
    return invalidFileId;
  }

  public String getFileChangeDateTime()
  {
    return fileChangeDateTime;
  }

  public String getLastFileId()
  {
    return lastFileId;
  }

  public Long getFileSize()
  {
    return fileSize;
  }

  public String getContentType()
  {
    if (extension == null) return null;
    return MimeTypeMap.getMimeTypeMap().getContentType("file." + extension);
  }

  public String getDownloadUrl()
  {
    return "/documents/" + contentId +
      "?saveas=" + editingFilename + "." + extension;
  }

  private void loadDocumentProperties(Document document)
  {
    docId = document.getDocId();
    docVersion = document.getVersion();
    lastFileId = DictionaryUtils.getPropertyValue(document.getProperty(),
      ASSISTANT_FILE_ID_PROPERTY);
    fileChangeDateTime = document.getChangeDateTime();
    Content content = document.getContent();
    if (content != null)
    {
      fileSize = content.getSize();
      contentId = content.getContentId();
      extension = MimeTypeMap.getMimeTypeMap()
        .getExtension(content.getContentType());
    }
    if (lastFileId != null)
    {
      // ensure file exists in openai.
      File file = getFile(lastFileId);
      if (!StringUtils.isBlank(file.getFilename()))
      {
        invalidFileId = false;
      }
    }
  }
}
