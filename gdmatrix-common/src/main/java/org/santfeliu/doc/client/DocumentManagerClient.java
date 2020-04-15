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
package org.santfeliu.doc.client;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import org.matrix.doc.DocumentManagerMetaData;
import org.matrix.translation.TranslationConstants;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.doc.OrderByProperty;
import org.matrix.dic.Property;
import org.matrix.doc.DocumentMetaData;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.ws.WSPortFactory;

/**
 *
 * @author realor
 */
public class DocumentManagerClient implements DocumentManagerPort
{
  DocumentManagerPort port;

  @Deprecated
  private static final int DOCUMENT_TITLE_MAX_SIZE = 512;
  @Deprecated
  private static final int DOCUMENT_RELATION_NAME_MAX_SIZE = 255;

  public DocumentManagerClient()
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    init(wsDirectory, null, null);
  }

  public DocumentManagerClient(URL wsDirectoryURL)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
    init(wsDirectory, null, null);
  }

  public DocumentManagerClient(String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    init(wsDirectory, userId, password);
  }

  public DocumentManagerClient(URL wsDirectoryURL, String userId, String password)
  {
    WSDirectory wsDirectory = WSDirectory.getInstance(wsDirectoryURL);
    init(wsDirectory, userId, password);
  }

  public DocumentManagerClient(DocumentManagerPort port)
  {
    this.port = port;
  }

  /*
   * @deprecated replaced by @link DocumentManagerClient(URL wsDirectoryURL)
   */
  @Deprecated
  public DocumentManagerClient(String wsdlLocation)
  {
    this(wsdlLocation, null, null);
  }

  /*
   * @deprecated replaced by @link DocumentManagerClient(URL wsDirectoryURL,
      String userId, String password)
   */
  @Deprecated
  public DocumentManagerClient(String wsdlLocation,
    String userId, String password)
  {
    try
    {
      port = WSPortFactory.getPort(DocumentManagerPort.class,
        wsdlLocation, userId, password, new MTOMFeature());
      // Enable HTTP chunking mode, otherwise HttpURLConnection buffers
      Map<String, Object> context = ((BindingProvider)port).getRequestContext();
      context.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public DocumentManagerPort getPort()
  {
    return port;
  }

  @Deprecated
  public DocumentMetaData getDocumentMetaData()
  {
    DocumentMetaData metaData = new DocumentMetaData();
    metaData.setDocumentTitleMaxSize(DOCUMENT_TITLE_MAX_SIZE);
    metaData.setDocumentRelationNameMaxSize(DOCUMENT_RELATION_NAME_MAX_SIZE);
    return metaData;
  }

  @Override
  public DocumentManagerMetaData getManagerMetaData()
  {
    DocumentManagerMetaData metaData = new DocumentManagerMetaData();
    metaData.setSupportVersions(true);
    return metaData;
  }

  @Override
  public Document storeDocument(Document document)
  {
    Content content = document.getContent();
    if (content != null && content.getContentId() != null &&
        content.getData() != null)
    {
      content.setData(null);
      document.setContent(content);
    }
    document = port.storeDocument(document);
    return document;
  }

  public Document loadDocument(String docId)
  {
    return loadDocument(docId, 0);
  }

  public Document loadDocument(String docId, int version)
  {
    try
    {
      Document document = port.loadDocument(docId, version, ContentInfo.ID);
      Content content = document.getContent();
      if (content != null)
      {
        String contentId = content.getContentId();
        content = loadContent(contentId);
        document.setContent(content);
      }
      return document;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Document loadDocument(String docId, int version,
     ContentInfo contentInfo)
  {
    return port.loadDocument(docId, version, contentInfo);
  }

  public Document loadDocumentByName(String docTypeId, String propertyName,
    String propertyValue, String language, int version)
  {
    Document document = null;
    DocumentFilter filter = new DocumentFilter();
    if (docTypeId != null)
      filter.setDocTypeId(docTypeId);
    Property p = new Property();
    p.setName(propertyName);
    p.getValue().add(propertyValue);
    filter.getProperty().add(p);
    if (version == -1)
    {
      OrderByProperty order = new OrderByProperty();
      order.setName("version");
      order.setDescending(true);
      filter.getOrderByProperty().add(order);
    }
    filter.setVersion(version);
    List<Document> documentList = findDocuments(filter);
    if (documentList.size() > 0)
    {
      // Find the best version
      Document bestDoc = null;
      if (language == null)
      {
        bestDoc = documentList.get(0);
      }
      else
      {
        int i = 0;
        boolean stop = false;
        while (i < documentList.size() && !stop)
        {
          Document doc = documentList.get(i);
          String docLanguage = doc.getLanguage();
          if (language.equals(docLanguage))
          {
            // Preference: 1
            bestDoc = doc;
            stop = true;
          }
          else if (TranslationConstants.UNIVERSAL_LANGUAGE.equals(docLanguage))
          {
            // Preference: 2
            if (bestDoc == null) bestDoc = doc;
          }
          i++;
        }
      }
      if (bestDoc != null)
      {
        String loadDocId = bestDoc.getDocId();
        int loadVersion = bestDoc.getVersion();
        document = loadDocument(loadDocId, loadVersion, ContentInfo.ALL);
      }
    }
    return document;
  }

  @Override
  public boolean removeDocument(String docId, int version)
  {
    return port.removeDocument(docId, version);
  }

  @Override
  public void lockDocument(String docId, int version)
  {
    port.lockDocument(docId, version);
  }

  @Override
  public void unlockDocument(String docId, int version)
  {
    port.unlockDocument(docId, version);
  }

  @Override
  public List<Document> findDocuments(DocumentFilter documentFilter)
  {
    return port.findDocuments(documentFilter);
  }

  public File getContentFile(String contentId)
  {
    File file = null;
    Content content = loadContent(contentId);
    if (content != null)
    {
      DataHandler data = content.getData();
      if (data != null)
      {
        FileDataSource ds = (FileDataSource)data.getDataSource();
        file = ds.getFile();
      }
    }
    return file;
  }

  @Override
  public Content loadContent(String contentId)
  {
    Content content = null;
    try
    {
      content = restoreContent(contentId);
      if (content == null)
      {
        content = port.loadContent(contentId);
        saveContent(content);
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    return content;
  }

  @Override
  public Content storeContent(Content content)
  {
    return port.storeContent(content);
  }

  @Override
  public boolean removeContent(String contentId)
  {
    return port.removeContent(contentId);
  }

  @Override
  public DataHandler markupContent(String docId, String searchExpression)
  {
    return port.markupContent(docId, searchExpression);
  }

  @Override
  public int countDocuments(DocumentFilter documentFilter)
  {
    return port.countDocuments(documentFilter);
  }

  private Content restoreContent(String contentId) throws IOException
  {
    Content content = null;
    File propertiesFile = getPropertiesFile(contentId);
    if (propertiesFile.exists())
    {
      FileInputStream contentProperties = new FileInputStream(propertiesFile);
      Properties properties = new Properties();
      properties.loadFromXML(contentProperties);

      content = new Content();
      content.setContentId(contentId);
      content.setContentType(properties.getProperty("contentType"));
      content.setFormatId(properties.getProperty("formatId"));
      content.setLanguage(properties.getProperty("language"));
      content.setSize(new Long(properties.getProperty("size")));
      content.setCreationDate(properties.getProperty("creationDate"));
      content.setCaptureDateTime(properties.getProperty("captureDate"));
      content.setCaptureUserId(properties.getProperty("captureUser"));
      content.setUrl(properties.getProperty("url"));

      File file = getContentFile(content);
      if (file.exists())
      {
        FileDataSource dataSource = new FileDataSource(file);
        DataHandler dataHandler = new DataHandler(dataSource);
        content.setData(dataHandler);
      }
      else if (content.getSize() > 0)
      {
        // inconsistency in cache!
        content = null;
      }
    }
    return content;
  }

  private void saveContent(Content content) throws IOException
  {
    String contentId = content.getContentId();

    // **** save content data ****
    DataHandler data = content.getData();
    if (data != null)
    {
      File tempFile = File.createTempFile(contentId, ".data");
      FileOutputStream os = new FileOutputStream(tempFile);
      try
      {
        data.writeTo(os);
      }
      finally
      {
        os.close();
      }
      // rename file to actual filename
      File contentFile = getContentFile(content);
      if (!tempFile.renameTo(contentFile))
      {
        // file already exits
        tempFile.delete();
      }
      content.setData(new DataHandler(new FileDataSource(contentFile)));
    }

    // **** save content properties ****
    {
      File tempFile = File.createTempFile(contentId, ".xml");
      FileOutputStream os = new FileOutputStream(tempFile);
      try
      {
        Properties properties = new Properties();
        if (content.getContentType() != null)
        {
          properties.setProperty("contentType", content.getContentType());
        }
        if (content.getFormatId() != null)
        {
          properties.setProperty("formatId", content.getFormatId());
        }
        if (content.getLanguage() != null)
        {
          properties.setProperty("language", content.getLanguage());
        }
        if (content.getSize() != null)
        {
          properties.setProperty("size", String.valueOf(content.getSize()));
        }
        if (content.getCreationDate() != null)
        {
          properties.setProperty("creationDate", content.getCreationDate());
        }
        if (content.getCaptureDateTime() != null)
        {
          properties.setProperty("captureDate", content.getCaptureDateTime());
        }
        if (content.getCaptureUserId() != null)
        {
          properties.setProperty("captureUser", content.getCaptureUserId());
        }
        if (content.getUrl() != null)
        {
          properties.setProperty("url", content.getUrl());
        }
        properties.storeToXML(os, contentId);
      }
      finally
      {
        os.close();
      }
      // rename file to actual filename
      File propertiesFile = getPropertiesFile(contentId);
      if (!tempFile.renameTo(propertiesFile))
      {
        // file already exits
        tempFile.delete();
      }
    }
  }

  private File getContentFile(Content content)
  {
    String contentId = content.getContentId();
    String contentType = content.getContentType();
    String extension = MimeTypeMap.getMimeTypeMap().getExtension(contentType);
    if (extension == null) extension = "bin";
    File cacheDir = getContentCacheDir();
    return  new File(cacheDir, contentId + ".data." + extension);
  }

  private File getPropertiesFile(String contentId)
  {
    File cacheDir = getContentCacheDir();
    return  new File(cacheDir, contentId + ".prop.xml");
  }

  private File getContentCacheDir()
  {
    String userDir = System.getProperty("user.home");
    File cacheDir = new File(userDir, ".cntcache");
    if (!cacheDir.exists())
    {
      cacheDir.mkdir();
    }
    return cacheDir;
  }

  private void init(WSDirectory wsDirectory, String userId, String password)
  {
    WSEndpoint endpoint = wsDirectory.getEndpoint(DocumentManagerService.class);
    // get port: enable MTOMFeature to prevent out of memory error
    port = endpoint.getPort(DocumentManagerPort.class,
      userId, password, new MTOMFeature());

    // Enable HTTP chunking mode, otherwise HttpURLConnection buffers
    Map<String, Object> context = ((BindingProvider)port).getRequestContext();
    context.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

    // set connection and read timeouts to 5 minutes (default is 3)
    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put("com.sun.xml.ws.connect.timeout", 5 * 60000);
    requestContext.put("com.sun.xml.ws.request.timeout", 5 * 60000);
  }

  public static void main(String[] args)
  {
    try
    {
      DocumentManagerClient client = new DocumentManagerClient("user", "******");

      Document document = new Document();
      document.setTitle("PROVA Fitxet gran");
      document.setDocTypeId("Document");
      Content content = new Content();
      content.setData(new DataHandler(new FileDataSource("c:/web.zip")));
      document.setContent(content);
      System.out.println("storing file...");
      document = client.storeDocument(document);
      System.out.println("finished. Document: " + document);

//      Document document = client.loadDocumentByName("a", "p1", "33", null, 28);
//      if (document != null)
//      {
//        System.out.println(document.getDocId() + " " + document.getVersion());
//        if (document.getContent() != null)
//        {
//          System.out.println(document.getContent().getContentId());
//          System.out.println(document.getContent().getCaptureDateTime());
//          System.out.println(document.getContent().getContentType());
//          System.out.println(document.getContent().getFormatId());
//          System.out.println(document.getContent().getSize());
//        }
//        System.out.println("-------------------------------------------");
//      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
