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
package org.santfeliu.doc.service;


import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import javax.annotation.Resource;

import javax.jws.WebService;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

import javax.xml.ws.handler.MessageContext;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import static org.matrix.dic.DictionaryConstants.WRITE_ACTION;
import static org.matrix.dic.DictionaryConstants.DELETE_ACTION;
import static org.matrix.dic.DictionaryConstants.CREATE_ACTION;
import org.matrix.doc.DocumentManagerMetaData;
import org.matrix.security.AccessControl;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import static org.matrix.doc.DocumentConstants.READ_ROLE;
import static org.matrix.doc.DocumentConstants.WRITE_ROLE;
import static org.matrix.doc.DocumentConstants.DELETE_ROLE;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
//import org.matrix.doc.DocumentPerson;
//import org.matrix.doc.DocumentPersonFilter;
//import org.matrix.doc.DocumentPersonView;
import org.matrix.dic.Property;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;
import org.matrix.doc.State;

import org.matrix.doc.DocumentMetaData;
import org.matrix.security.SecurityConstants;

import org.matrix.util.ExternalEntity;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.WSTypeValidator;
import org.santfeliu.doc.store.ContentStore;
import org.santfeliu.doc.store.ContentStoreConnection;
import org.santfeliu.doc.store.DocumentStore;
import org.santfeliu.doc.store.DocumentStoreConnection;
import org.santfeliu.doc.store.cntora.OracleContentStore;
import org.santfeliu.doc.store.docjpa.JPADocumentStore;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.security.UserCache;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TemporaryDataSource;
import org.santfeliu.ws.WSExceptionFactory;

import org.santfeliu.doc.util.droid.Droid;
import org.santfeliu.security.User;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.log.CSVLogger;
import org.santfeliu.ws.WSUtils;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;


/**
 *
 * @author blanquepa, realor
 */
@WebService(endpointInterface = "org.matrix.doc.DocumentManagerPort")
public class DocumentManager implements DocumentManagerPort
{
  public static final String DOCUMENT_STORE = "documentStore";  
  public static final String CONTENT_STORE = "contentStore"; 
  public static final String ADMIN_ROLE = "adminRole";
  
  private static final String REC_DOC_NOT_UPDATABLE = 
    "doc:RECORDED_DOCUMENT_NOT_UPDATABLE";
  private static final String NOT_LOCK_OWNER = 
    "doc:NOT_LOCK_OWNER";
  private static final String RELATION_EXISTS =
    "doc:RELATION_EXISTS_WITH_ANOTHER_DOCUMENT";
  
  @Resource
  WebServiceContext wsContext;
  private WSEndpoint endpoint;
  
  private DocumentStore documentStore;
  private ContentStore contentStore;
  private Droid droid;
  
  protected static final Logger log = Logger.getLogger("Document");

  @Deprecated
  private static final int DOCUMENT_TITLE_MAX_SIZE = 512;
  @Deprecated
  private static final int DOCUMENT_RELATION_NAME_MAX_SIZE = 255;

  public static final String LOG_CONFIG = "org.santfeliu.ws.logConfig";

  protected static CSVLogger csvLogger;


  static
  {
    String logConfig = MatrixConfig.getPathProperty(LOG_CONFIG);
    if (logConfig != null)
    {
      csvLogger = CSVLogger.getInstance(logConfig);
    }
  }

  public DocumentManager()
  {
    try
    {
      initDocumentStore();
      initContentStore();
      initDroid();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
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

  /* Content operations */

  @Override
  public Content loadContent(String contentId)
  {
    try
    {
      logOperation("loadContent", "IN", contentId);

      ContentStoreConnection conn = contentStore.getConnection();
      try
      {
        if (!validateContentId(contentId))
          throw new WebServiceException("doc:INVALID_CONTENTID");
        Content content = conn.loadContent(contentId, ContentInfo.ALL);
        if (content == null)
          throw new WebServiceException("doc:CONTENT_NOT_FOUND");
        describeContent(content);
        logOperation("loadContent", "OUT", content.getContentId());
        return content;
      }
      catch (Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      logOperation("loadContent", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public Content storeContent(Content content)
  {
    try
    {
      logOperation("storeContent", "IN", content.getContentId() != null ?
        content.getContentId() : "NEW");

      ContentStoreConnection conn = contentStore.getConnection();
      try
      {      
        Content storedContent = internalStoreContent(conn, content);
        conn.commit();
        logOperation("storeContent", "OUT", storedContent.getContentId());
        return storedContent;
      }
      catch(Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      logOperation("storeContent", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }
  
  @Override
  public boolean removeContent(String contentId)
  {
    try
    {
      logOperation("removeContent", "IN", contentId);

      ContentStoreConnection conn = contentStore.getConnection();
      try
      {
        boolean deleted = conn.removeContent(contentId);
        conn.commit();
        logOperation("removeContent", "OUT", String.valueOf(deleted));
        return deleted;
      }
      catch(Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      logOperation("removeContent", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public DataHandler markupContent(String contentId, String searchExpression)
  {
    try
    {
      logOperation("markupContent", "IN", "contentId=" + contentId +
        "&search=" + searchExpression);

      ContentStoreConnection conn = contentStore.getConnection();
      try
      {
        File file = conn.markupContent(contentId, searchExpression);
        TemporaryDataSource ds = new TemporaryDataSource(file);
        logOperation("markupContent", "OUT", ds.toString());
        return new DataHandler(ds);
      }
      catch (Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception ex)
    {
      logOperation("markupContent", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  /* Document operations */
  @Override
  public Document loadDocument(String docId, int version, 
    ContentInfo contentInfo)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      logOperation("loadDocument", "IN", "docId=" + docId
        + "&version=" + version, user.getUserId());
      
      //If negative version ignores logical deletes.
      boolean ignoreDeleted = version < 0;
      if (ignoreDeleted)
        version = (0 - version);

      DocumentStoreConnection docConn =
        documentStore.newConnection(getWSEndpoint());
      try
      {
        //load Document
        Document document = docConn.loadDocument(docId, version);
        
        if (document == null || 
          (State.DELETED.equals(document.getState()) && !ignoreDeleted))
          throw new WebServiceException("doc:DOCUMENT_NOT_FOUND");

        String docTypeId = getWSEndpoint()
          .toGlobalId(org.matrix.dic.Type.class, document.getDocTypeId());
        Type docType = TypeCache.getInstance().getType(docTypeId);
        if (!canUserReadDocument(user, document, docType))
          throw new WebServiceException("ACTION_DENIED");

        document.setIncremental(false);
      
        //load Content (if document has content)  
        Content content = document.getContent();
        if (content != null && !ContentInfo.ID.equals(contentInfo))
        {
          ContentStoreConnection conConn = contentStore.getConnection();
          try
          {
            String contentId = content.getContentId();
            content = conConn.loadContent(contentId, contentInfo);
            if (!contentInfo.equals(ContentInfo.ID)) describeContent(content);
            document.setContent(content);
            conConn.commit();
          }
          catch (Exception ex)
          {
            conConn.rollback();
            throw ex;
          }
          finally
          {
            conConn.close();
          }
        }
        docConn.commit();
        logOperation("loadDocument", "OUT", "docId=" + document.getDocId() +
          "&version=" + document.getVersion() + 
          (content != null ? "&contentId=" + content.getContentId() : ""));
        return document;
      }
      catch (Exception ex)
      {
        docConn.rollback();
        throw ex;
      }
      finally
      {
        docConn.close();
      }
    }
    catch(Exception ex)
    {
      logOperation("loadDocument", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public Document storeDocument(Document document)
  {
    Document storedDocument = null;

    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      logStoreDocument(document, "IN", user.getUserId());

      //User & System properties synchronization (User -> System)
      DocumentUtils.transferUserToSystemProperties(document);

      String docTypeId = getWSEndpoint()
        .toGlobalId(org.matrix.dic.Type.class, document.getDocTypeId());
      Type type = TypeCache.getInstance().getType(docTypeId);

      //Validations
      validateDocument(document, type); //Module constraints validations

      DocumentStoreConnection docConn = 
        documentStore.newConnection(getWSEndpoint());
      try
      {
        if (document.getDocId() == null)
          storedDocument = createDocument(docConn, document, type, user);
        else
          storedDocument = updateDocument(docConn, document, type, false, user);
        logStoreDocument(storedDocument, "OUT", user.getUserId());
        docConn.commit();
      }
      catch (Exception ex)
      {
        docConn.rollback();
        throw ex;
      }
      finally
      {
        docConn.close();
      }

      return storedDocument;                      
    }
    catch (Exception ex)
    {
      logOperation("storeDocument", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public boolean removeDocument(String docId, int version)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      logOperation("removeDocument", "IN", "docId=" + docId +
        "&version=" + version, user.getUserId());

      DocumentStoreConnection docConn = 
        documentStore.newConnection(getWSEndpoint());
      try
      {
        Document lastVersionDocument = docConn.loadDocument(docId, 0); 
        if (lastVersionDocument == null)
          return false;

        String docTypeId =
          getWSEndpoint().toGlobalId(org.matrix.dic.Type.class,
          lastVersionDocument.getDocTypeId());
        
        boolean persistentDelete = 
          (version == DocumentConstants.PERSISTENT_DELETE);
        Type docType =
          TypeCache.getInstance().getType(docTypeId);
        if (!canUserDeleteDocument(user, lastVersionDocument, docType))
          throw new WebServiceException("ACTION_DENIED");
        
        int lastVersion = lastVersionDocument.getVersion();
        int fromVersion;
        int toVersion;
        if (version == DocumentConstants.LAST_VERSION)
        {
          fromVersion = toVersion = lastVersion;
        }
        else if (version == DocumentConstants.DELETE_ALL_VERSIONS 
                 || persistentDelete)
        {
          fromVersion = 1;
          toVersion = lastVersion;
        }
        else if (version == DocumentConstants.DELETE_OLD_VERSIONS)
        {
          fromVersion = 1;
          toVersion = lastVersion - 1;
        }
        else
        {
          fromVersion = toVersion = version;
        }

        boolean result = false;
        ContentStoreConnection conConn = contentStore.getConnection();    
        try
        {
          DocumentFilter filter = new DocumentFilter();
          filter.getDocId().add(docId);
          filter.setVersion(-1);
          filter.getStates().add(State.DRAFT);
          filter.getStates().add(State.COMPLETE);
          filter.getStates().add(State.RECORD);
          filter.getStates().add(State.DELETED);
          List<Document> documents = docConn.findDocuments(filter, 
            UserCache.getUser(wsContext).getRolesList(), true);
          for (Document document : documents)
          {
            int ver = document.getVersion();
            if (ver >= fromVersion && ver <= toVersion)
            {
              if (State.RECORD.equals(document.getState()) && !isUserAdmin())
                throw new WebServiceException(REC_DOC_NOT_UPDATABLE);
              String lockUser = document.getLockUserId();
              if (!checkLocking(lockUser)) 
                throw new Exception(NOT_LOCK_OWNER);
              boolean removed = 
                docConn.removeDocument(docId, ver, persistentDelete);
              Content content = document.getContent();
              if (removed && content != null && persistentDelete) 
              {
                result = true;
                if (!docConn.isContentInUse(content.getContentId()))
                  conConn.removeContent(content.getContentId());
              }
            }
          }
          conConn.commit();
        }
        catch (Exception ex)
        {
          conConn.rollback();
          throw ex;
        }
        finally
        {
          conConn.close();
        }
        docConn.commit();
        logOperation("removeDocument", "OUT", String.valueOf(result));
        return result;
      }
      catch (Exception ex)
      {
        docConn.rollback();
        throw ex;
      }
      finally
      {
        docConn.close();
      }
    }
    catch (Exception ex)
    {
      logOperation("removeDocument", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public List<Document> findDocuments(DocumentFilter documentFilter)
  {
    try
    {
      logOperation("findDocuments", "IN", "");
      
      validateDocumentFilter(documentFilter);
      
      List<Document> result;
      DocumentStoreConnection docConn =
        documentStore.newConnection(getWSEndpoint());
      try
      {
        result = docConn.findDocuments(documentFilter, 
          UserCache.getUser(wsContext).getRolesList(),
          isUserAdmin());

        if (documentFilter.isIncludeContentMetadata())
        {
          //create documents map (Map<contentId, documents>)
          Map<String, List<Document>> documentsByContentMap = new HashMap();
          for (Document document : result)
          {
            Content content = document.getContent();
            if (content != null)
            {
              String contentId = content.getContentId();
              List<Document> contentDocuments = 
                documentsByContentMap.get(contentId);
              if (contentDocuments == null)
              {
                contentDocuments = new ArrayList();
                documentsByContentMap.put(contentId, contentDocuments);
              }
              contentDocuments.add(document);
            }
          }

          //populate documents with its contents using previous map
          ContentStoreConnection storeConn = contentStore.getConnection();
          try
          {
            List<Content> contents = 
              storeConn.findContents(documentsByContentMap.keySet());
            for (Content content : contents)
            {
              describeContent(content);
              List<Document> contentDocuments = 
                documentsByContentMap.get(content.getContentId());
              for (Document contentDocument : contentDocuments)
              {
                contentDocument.setContent(content);
              }
            }
            storeConn.commit();
          }
          catch (Exception ex)
          {
            storeConn.rollback();
            throw ex;
          }
          finally
          {
            storeConn.close();
          }
        }

        docConn.commit();
        logOperation("findDocuments", "OUT", result != null ? result.size() +
          " items found" : "0 items found");
      }
      catch (Exception ex)
      {
        docConn.rollback();
        throw ex;
      }
      finally
      {
        docConn.close();
      }
      return result;
    }
    catch(Exception ex)
    {
      logOperation("findDocuments", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }

  @Override
  public int countDocuments(DocumentFilter documentFilter)
  {
    try
    {
      logOperation("countDocuments", "IN", "");

      int result;
      DocumentStoreConnection conn = 
        documentStore.newConnection(getWSEndpoint());
      try
      {
        result = conn.countDocuments(documentFilter, 
          UserCache.getUser(wsContext).getRolesList(),
          isUserAdmin());
        conn.commit();
        logOperation("countDocuments", "OUT", String.valueOf(result));
      }
      catch (Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        conn.close();
      }
      return result;
    }
    catch(Exception ex)
    {
      logOperation("countDocuments", "FAULT", ex.getMessage());
      throw WSExceptionFactory.create(ex);
    }
  }
  
  @Override
  public void lockDocument(String docId, int version)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      logOperation("lockDocument", "IN", 
        "docId=" + docId + "&version=" + version, user.getUserId());

      DocumentStoreConnection conn = 
        documentStore.newConnection(getWSEndpoint());
      try
      {
        Document document = conn.loadDocument(docId, version);
        if (document == null)
          throw new Exception("doc:DOCUMENT_NOT_FOUND");
        String lockUser = document.getLockUserId();
        if (lockUser != null && !lockUser.equals(user.getUserId()))
          throw new Exception("doc:DOCUMENT_LOCKED_BY_ANOTHER_USER");
        else if (lockUser != null && lockUser.equals(user.getUserId()))
          throw new Exception("doc:USER_ALREADY_OWNS_LOCK");

        document.setLockUserId(credentials.getUserId());
        document.setIncremental(true);

        String docTypeId =
          getWSEndpoint().toGlobalId(org.matrix.dic.Type.class,
          document.getDocTypeId());
        Type type = TypeCache.getInstance().getType(docTypeId);
        updateDocument(conn, document, type, true, user);
        conn.commit();
        logOperation("lockDocument", "OUT", "locked", user.getUserId());
      }
      catch (Exception e)
      {
        conn.rollback();
        throw e;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception e)
    {
      logOperation("lockDocument", "FAULT", e.getMessage());
      throw WSExceptionFactory.create(e);
    }
  }

  @Override
  public void unlockDocument(String docId, int version)
  {
    try
    {
      Credentials credentials = SecurityUtils.getCredentials(wsContext);
      User user = UserCache.getUser(credentials);

      logOperation("unlockDocument", "IN", "docId=" + docId +
        "&version=" + version, user.getUserId());

      DocumentStoreConnection conn = 
        documentStore.newConnection(getWSEndpoint());
      try
      {
        Document document = conn.loadDocument(docId, version);
        if (document == null)      
          throw new Exception("doc:DOCUMENT_NOT_FOUND");
        String lockUser = document.getLockUserId();
        if (lockUser != null)
        {
          if (!lockUser.equals(user.getUserId()) && !isUserAdmin())
            throw new Exception("doc:NOT_LOCK_OWNER");
        
          document.setLockUserId(null);
          document.setIncremental(true);

          String docTypeId =
            getWSEndpoint().toGlobalId(org.matrix.dic.Type.class,
            document.getDocTypeId());
          Type type =
            TypeCache.getInstance().getType(docTypeId);
          updateDocument(conn, document, type, true, user);
        }
        conn.commit();
        logOperation("unlockDocument", "OUT", "unlocked", user.getUserId());
      }
      catch (Exception ex)
      {
        conn.rollback();
        throw ex;
      }
      finally
      {
        conn.close();
      }
    }
    catch (Exception e)
    {
      logOperation("unlockDocument", "FAULT", e.getMessage());
      throw WSExceptionFactory.create(e);
    }
  }

//  public DocumentPerson loadDocumentPerson(String docPersonId)
//  {
//    logOperation("loadDocumentPerson", "IN", docPersonId);
//
//    try
//    {
//      DocumentStoreConnection conn = documentStore.newConnection(getWSEndpoint());
//      try
//      {
//        DocumentPerson docPerson = conn.loadDocumentPerson(docPersonId);
//
//        if (docPerson == null)
//          throw new WebServiceException("doc:DOCUMENT_PERSON_NOT_FOUND");
//
//        //TODO: check user roles???
//
//        conn.commit();
//        logOperation("loadDocumentPerson", "OUT", docPerson.getDocPersonId());
//        return docPerson;
//      }
//      catch (Exception ex)
//      {
//        conn.rollback();
//        throw ex;
//      }
//      finally
//      {
//        conn.close();
//      }
//
//    }
//    catch (Exception e)
//    {
//      logOperation("loadDocumentPerson", "FAULT", e.getMessage());
//      throw WSExceptionFactory.create(e);
//    }
//  }
//
//  public DocumentPerson storeDocumentPerson(DocumentPerson docPerson)
//  {
//    Credentials credentials = SecurityUtils.getCredentials(wsContext);
//    User user = UserCache.getUser(credentials);
//
//    logOperation("storeDocumentPerson", "IN",
//      docPerson.getDocPersonId() != null ? docPerson.getDocPersonId() : "NEW",
//      user.getUserId());
//
//    try
//    {
//      DocumentStoreConnection conn = documentStore.newConnection(getWSEndpoint());
//      try
//      {
//        validateDocumentPerson(docPerson);
//        docPerson = conn.storeDocumentPerson(docPerson, user.getUserId());
//        conn.commit();
//        logOperation("storeDocumentPerson", "OUT", docPerson.getDocPersonId());
//        return docPerson;
//      }
//      catch (Exception ex)
//      {
//        conn.rollback();
//        throw ex;
//      }
//      finally
//      {
//        conn.close();
//      }
//
//    }
//    catch (Exception e)
//    {
//      logOperation("storeDocumentPerson", "FAULT", e.getMessage());
//      throw WSExceptionFactory.create(e);
//    }
//  }
//
//  public boolean removeDocumentPerson(String docPersonId)
//  {
//    logOperation("removeDocumentPerson", "IN", docPersonId);
//
//    try
//    {
//      DocumentStoreConnection conn = documentStore.newConnection(getWSEndpoint());
//      try
//      {
//        boolean result = conn.removeDocumentPerson(docPersonId);
//        conn.commit();
//        logOperation("removeDocumentPerson", "OUT", String.valueOf(result));
//        return result;
//      }
//      catch (Exception ex)
//      {
//        conn.rollback();
//        throw ex;
//      }
//      finally
//      {
//        conn.close();
//      }
//    }
//    catch (Exception e)
//    {
//      logOperation("removeDocumentPerson", "FAULT", e.getMessage());
//      throw WSExceptionFactory.create(e);
//    }
//  }
//
//  public List<DocumentPersonView> findDocumentPersonViews(DocumentPersonFilter filter)
//  {
//    logOperation("findDocumentPersonViews", "IN", "");
//
//    try
//    {
//      DocumentStoreConnection conn = documentStore.newConnection(getWSEndpoint());
//      try
//      {
//        List<DocumentPersonView> result = conn.findDocumentPersonViews(filter);
//        conn.commit();
//        logOperation("findDocumentPersonViews", "OUT",
//          result != null ? result.size() + " items found" : "0 items found");
//        return result;
//      }
//      catch (Exception ex)
//      {
//        conn.rollback();
//        throw ex;
//      }
//      finally
//      {
//        conn.close();
//      }
//    }
//    catch (Exception e)
//    {
//      logOperation("findDocumentPersonViews", "FAULT", e.getMessage());
//      throw WSExceptionFactory.create(e);
//    }
//  }


  /*************** private methods ****************/
  //DocumentStore initilization
  private void initDocumentStore() throws Exception
  {
    System.out.println("Initializing DocumentStore");
    String documentStoreClassName = 
      MatrixConfig.getClassProperty(DocumentManager.class, DOCUMENT_STORE);

    Class documentStoreClass;
    if (documentStoreClassName != null)
      documentStoreClass = Class.forName(documentStoreClassName);
    else //default documentStore
      documentStoreClass = JPADocumentStore.class;

    documentStore = (DocumentStore)documentStoreClass.newInstance();

    documentStore.init(MatrixConfig.getClassProperties(documentStoreClass));
    System.out.println(documentStore.getClass() + " initialized");      
  }

  private void initContentStore() throws Exception
  {
    System.out.println("Initializing ContentStore");  
    String contentStoreClassName = 
      MatrixConfig.getClassProperty(DocumentManager.class, CONTENT_STORE);
    if (contentStoreClassName != null)
    {
      contentStore = 
        (ContentStore)Class.forName(contentStoreClassName).newInstance();
    }
    else if (documentStore instanceof ContentStore)
    {
      // some implementations implements DocumentStore and ContentStore
      contentStore = (ContentStore)documentStore;
    }
    else
    { 
      //default contentStore
      contentStore = new OracleContentStore();
    }
    contentStore.init(null);
    System.out.println(contentStore.getClass() + " initialized");          
  }

  private void initDroid() throws Exception
  {
    File baseDir = MatrixConfig.getDirectory();
    droid = new Droid(baseDir);
  }

  private Document createDocument(DocumentStoreConnection docConn,
    Document document, Type docType, User user) throws Exception
  {
    if (!docType.isInstantiable())
      throw new WebServiceException("doc:NOT_INSTANTIABLE");

    if (!canUserCreateDocument(user, docType))
      throw new WebServiceException("ACTION_DENIED");

    String time = getCurrentDateTime("yyyyMMddHHmmss");
    String date = getCurrentDateTime("yyyyMMdd");
    String username = getCurrentUsername();
    
    Content storedContent = null;
    Content content = document.getContent();  
    ContentStoreConnection conConn = contentStore.getConnection();
    try
    {
      //Prepare document
      if (document.getState() == null)
        document.setState(State.COMPLETE);    
      document.setCaptureDateTime(time);
      document.setChangeDateTime(time);
      document.setCaptureUserId(username);
      document.setChangeUserId(username);
      document.setVersion(1);
      if (document.getCreationDate() == null)
        document.setCreationDate(date);
      setAutoLanguage(document);
      
      //Store content (or load if exists contentId)
      if (content != null && content.getContentId() == null) //document with content
      {
        if (document.getLanguage() != null)
          content.setLanguage(document.getLanguage());
        storedContent = internalStoreContent(conConn, content);
      }
      else if (content != null && content.getContentId() != null)
        storedContent = 
          conConn.loadContent(content.getContentId(), ContentInfo.METADATA);
      
      //Prepare ACL
      prepareAccessControlList(document, null);

      //Store Document
      List<Property> clonedProps = 
        DocumentUtils.cloneProperties(document.getProperty());
      document = docConn.storeDocument(document);        
      document.setContent(storedContent);

      //Store Properties
      docConn.storeProperties(document.getDocId(), document.getVersion(), 
        clonedProps);

      //Store Related Documents
      docConn.storeRelatedDocuments(document.getDocId(), document.getVersion(),
        document.getRelatedDocument());

      //Store AccessControl
      String userId = user.getUserId().trim();
      String autoLoginUserId =
        MatrixConfig.getProperty("org.santfeliu.web.autoLogin.userId");
      if (!userId.equals(SecurityConstants.ANONYMOUS) && 
        !userId.equals(autoLoginUserId))
      {
        addNominalRole(user, document);
      }
      
      docConn.storeAccessControlList(document);

      conConn.commit();
      
      return document;
    }
    catch (Exception ex)
    {
      conConn.rollback();
      throw ex;
    }
    finally
    {
      conConn.close();
    }
  }
  
  private Document updateDocument(DocumentStoreConnection docConn,
    Document document, Type docType, boolean lockOp, User user) throws Exception
  {
    String time = getCurrentDateTime("yyyyMMddHHmmss");
    String username = getCurrentUsername();
    String docId = document.getDocId();
    int version = document.getVersion();

    //get instance of the current document
    Document currentDocument = getCurrentDocument(docConn, docId, version);
    String currentDocTypeId =
      getWSEndpoint().toGlobalId(org.matrix.dic.Type.class,
      currentDocument.getDocTypeId());
    Type currentType =
      TypeCache.getInstance().getType(currentDocTypeId);

    if (!canUserModifyDocument(user, currentDocument, currentType))
      throw new WebServiceException("ACTION_DENIED");
    
    if (docType != null && currentType != null &&
       !docType.getTypeId().equals(currentType.getTypeId()) &&
       !canUserCreateDocument(user, docType))
      throw new WebServiceException("ACTION_DENIED");

    if (!checkLocking(currentDocument.getLockUserId()) && !isUserAdmin())
      throw new Exception("doc:NOT_LOCK_OWNER");

    //get last version of this document
    boolean notNewAndNotLast = 
      version != DocumentConstants.LAST_VERSION 
      && version != DocumentConstants.NEW_VERSION; 
    Document lastVersionDocument = (notNewAndNotLast ?
        docConn.loadDocument(docId, DocumentConstants.LAST_VERSION) : 
        currentDocument);
    if (lastVersionDocument == null)
      throw new WebServiceException("doc:DOCUMENT_NOT_FOUND");
    // document recorded, only administrator users can modify this document
    if (State.RECORD.equals(lastVersionDocument.getState()) && !isUserAdmin())
      throw new WebServiceException("doc:RECORDED_DOCUMENT_NOT_UPDATABLE");
    //only last version of documents can be recorded
    if (version != 0 && version != -1 
        && version != lastVersionDocument.getVersion() 
        && State.RECORD.equals(document.getState()))
      throw new WebServiceException("doc:VERSION_NOT_RECORDABLE");
    
    Content content = document.getContent();  
    ContentStoreConnection conConn = contentStore.getConnection();
    try
    {
      //Prepare Document & content
      if (document.isIncremental())
      {
        mergeDocument(document, currentDocument);
        DocumentUtils.transferUserToSystemProperties(document);
      }
      document = setAutomaticValues(document, currentDocument,
        lastVersionDocument.getVersion(), time, username, lockOp);

      boolean copyContent =
        (!document.getLanguage().equals(currentDocument.getLanguage()) &&
        (content != null 
        && !document.getLanguage().equals(content.getLanguage()))
        && content.getLanguage() != null); //no change if content's language is null.

      if (content!= null && content.getLanguage() == null
        && document.getLanguage() != null)
        content.setLanguage(document.getLanguage());
      
      //Store content (or load if exists id)
      Content storedContent = null;

      if (content != null && content.getContentId() == null) //doc. has content
        storedContent = internalStoreContent(conConn, content);
      else if ((content != null && content.getContentId() != null)  ||
        document.isIncremental())
      {
        if (copyContent)
        {
          String currentContentId = currentDocument.getContent().getContentId();
          content.setContentId(generateUUID());
          content.setLanguage(document.getLanguage());
          content.setCaptureDateTime(time);
          content.setCaptureUserId(username);
          storedContent = conConn.copyContent(content, currentContentId);
        }
        else
        {
          String contentId = content != null ? content.getContentId() :
            currentDocument.getContent().getContentId();
          storedContent =
            conConn.loadContent(contentId, ContentInfo.METADATA);
        }
        if (storedContent == null)
          throw new WebServiceException("doc:CONTENT_NOT_FOUND");
      }
      document.setContent(storedContent); //Needed to store contentId

      //Prepare Properties
      if (document.isIncremental())
        mergeProperties(document, currentDocument);

      //Prepare ACL
      prepareAccessControlList(document, currentDocument);

      //Store Properties
      docConn.removeProperties(document.getDocId(), document.getVersion());
      docConn.storeProperties(document.getDocId(), document.getVersion(),
        document.getProperty());

      //Store Document
      document = docConn.storeDocument(document);
      document.setContent(storedContent); //Needed to return content

      //Prepare Related Documents (validate too)
      prepareRelatedDocuments(docConn, document, currentDocument, time, username);

      //Store Related Documents
      docConn.storeRelatedDocuments(docId, document.getVersion(),
        document.getRelatedDocument());

      //Store ACL
      docConn.storeAccessControlList(document);
      
      //Remove previous Content (if is not used)
      String contentId = DocumentUtils.getContentId(currentDocument);
      if (contentId != null && !docConn.isContentInUse(contentId))
        conConn.removeContent(contentId);
      
      conConn.commit();
      
      return document;
    }
    catch (Exception ex)
    {
      conConn.rollback();
      throw ex;
    }
    finally
    {
      conConn.close();
    }    
  }

  private Content internalStoreContent(ContentStoreConnection conn, 
    Content content) throws Exception
  {
    if (content.getContentId() != null)
      throw new WebServiceException("doc:CONTENTS_ARE_IMMUTABLE");
    
    if (content.getData() == null && content.getUrl() == null)
      throw new WebServiceException("doc:DATA_OR_URL_NOT_FOUND");
      
    File dataFile = getDataFile(content); // for internal contents
    setContentValues(content, dataFile);

    Content storedContent = conn.storeContent(content, dataFile);
    content.setData(null);
    content.setUrl(null);
    if (dataFile != null) dataFile.delete();
    
    return storedContent;
  }

  private void setContentValues(Content content, File dataFile)
  {
    if (dataFile != null)
    {
      content.setSize(dataFile.length());
      try
      {
        identifyContent(content, dataFile);
      }
      catch(Exception e)
      {
        log.log(Level.WARNING, "Introspection of content " + 
          Level.INFO, content.getContentId() + " failed: " + e.getMessage());
      }
    }
    else // for external contents
    {
      //content.setSize(getExternalContentLength(content.getUrl()));
      content.setSize(new Long(0));
    }
    if (content.getContentType() == null)
      content.setContentType("application/octet-stream");
    content.setContentId(generateUUID());
    content.setCaptureDateTime(getCurrentDateTime("yyyyMMddHHmmss"));
    content.setCaptureUserId(getCurrentUsername());
  }

  private Document setAutomaticValues(Document document, 
    Document currentDocument, int lastVersion, String time, String username, 
    boolean lockOp) 
      throws Exception
  {
    if (document.getState() == null) 
      document.setState(State.COMPLETE);    
    document.setChangeDateTime(time);
    document.setChangeUserId(username);

    switch (document.getVersion())
    {
    // last version
      case DocumentConstants.LAST_VERSION:
        if (!lockOp)
          document.setLockUserId(currentDocument.getLockUserId());
        document.setCaptureDateTime(currentDocument.getCaptureDateTime());
        document.setCaptureUserId(currentDocument.getCaptureUserId());
        document.setVersion(lastVersion);
        break;
    // new version
      case DocumentConstants.NEW_VERSION:
        document.setCaptureDateTime(time);
        document.setCaptureUserId(username);
        document.setVersion(lastVersion + 1);
        break;
    // specificVersion
      default:
        if (!lockOp)
          document.setLockUserId(currentDocument.getLockUserId());
        document.setCaptureDateTime(currentDocument.getCaptureDateTime());
        document.setCaptureUserId(currentDocument.getCaptureUserId());
        break;
    }

    //Language setting
    setAutoLanguage(document);

    return document;
  }
  
  /**
   * If document language is null then set it with content language 
   * (or UNIVERSAL in the case that content or content's language are null) 
   * @param document 
   */ 
  private void setAutoLanguage(Document document)
  {
    if (document.getLanguage() == null && document.getContent() != null &&
      document.getContent().getLanguage() != null) 
        document.setLanguage(document.getContent().getLanguage());
    else if (document.getLanguage() == null)
      document.setLanguage(DocumentConstants.UNIVERSAL_LANGUAGE);    
  }
  
  private Document getCurrentDocument(DocumentStoreConnection conn, 
    String docId, int version)
    throws Exception
  {
    Document currentDocument;
    int currentVersion = version;
    if (version == DocumentConstants.NEW_VERSION)
      currentVersion = DocumentConstants.LAST_VERSION;
  
    currentDocument = conn.loadDocument(docId, currentVersion);

    if (currentDocument == null)
      throw new WebServiceException("doc:DOCUMENT_NOT_FOUND");

    return currentDocument;
  }

  private void prepareRelatedDocuments(DocumentStoreConnection docConn,
    Document newDocument, Document currentDocument, String time, 
    String username)
    throws Exception
  {
    List<RelatedDocument> result = new ArrayList();
    List<RelatedDocument> processed = new ArrayList();
    List<RelatedDocument> current = null;    
    
    if (currentDocument != null)
      current = currentDocument.getRelatedDocument();
    
    RelatedDocumentsMap currentRelDocsMap = new RelatedDocumentsMap(current);
    for (RelatedDocument newRelDoc : newDocument.getRelatedDocument())
    {
      RelationType relType = newRelDoc.getRelationType();
      String relName = newRelDoc.getName();
      String docId = newRelDoc.getDocId();
//      int version = newRelDoc.getVersion();

      List<RelatedDocument> relationList = 
        currentRelDocsMap.getRelationList(relType, relName);
      validateRelation(docConn, relationList, newRelDoc, newDocument);

      if (relationList != null) //if exists relation with same type and name
      {
        RelatedDocument currentRelDoc = 
          currentRelDocsMap.getRelatedDocument(relType, relName, docId);
                
        result.add(newRelDoc);

        if (currentRelDoc == null) //if previously doesn't exists then set new data
        {
          newRelDoc.setChangeDateTime(time);
          newRelDoc.setChangeUserId(username);
          newRelDoc.setCaptureDateTime(time);
          newRelDoc.setCaptureUserId(username);
        }
        else //if exists then set the current data and put in processed list
        {
          processed.add(currentRelDoc);
          newRelDoc.setChangeDateTime(currentRelDoc.getChangeDateTime());
          newRelDoc.setChangeUserId(currentRelDoc.getChangeUserId());
          newRelDoc.setCaptureDateTime(currentRelDoc.getCaptureDateTime());
          newRelDoc.setCaptureUserId(currentRelDoc.getCaptureUserId());
        }
      }
      else
      {
        newRelDoc.setCaptureDateTime(time);
        newRelDoc.setChangeDateTime(time);
        newRelDoc.setCaptureUserId(username);
        newRelDoc.setChangeUserId(username);
        result.add(newRelDoc);        
      }
    }
    
    newDocument.getRelatedDocument().clear();
    newDocument.getRelatedDocument().addAll(result);
    
    if (newDocument.isIncremental()) //incremental: add not processed
    {
      List<RelatedDocument> relDocs = currentRelDocsMap.getRelatedDocuments();
      relDocs.removeAll(processed);
      for(RelatedDocument relDoc : relDocs)
      {
        newDocument.getRelatedDocument().add(relDoc);
      }
    }
  }
  
  /**
   * Check if exists another relations with same type and name
   */
  private void validateRelation(DocumentStoreConnection docConn, 
    List<RelatedDocument> relationList, RelatedDocument newRelatedDocument,
    Document newDocument)
    throws Exception
  {
    String newDocId = newDocument.getDocId();
    RelationType relType = newRelatedDocument.getRelationType();
    String relName = newRelatedDocument.getName();
    if (relationList != null && !isReverseRelation(relType))
    {
      String relationDocId = relationList.get(0).getDocId();
      if (!newRelatedDocument.getDocId().equals(relationDocId))
      {
        RelatedDocumentsMap newRelDocsMap = 
          new RelatedDocumentsMap(newDocument.getRelatedDocument());
        List<RelatedDocument> newRelDocs = 
          newRelDocsMap.getRelationList(relType, relName);
        if (newRelDocs != null && newRelDocs.size() > 1)
          throw new WebServiceException(RELATION_EXISTS);
      }
    }
    else if (isReverseRelation(relType))
    {
      String revDocId = newRelatedDocument.getDocId();
      int revVersion = newRelatedDocument.getVersion();
      Document revDocument = getCurrentDocument(docConn, revDocId, revVersion);
      if (revDocument != null)
      {
        RelatedDocumentsMap revRelDocsMap = 
          new RelatedDocumentsMap(revDocument.getRelatedDocument());
        RelationType revertedType = DocumentUtils.revertRelation(relType);
        List<RelatedDocument> revRelList = 
          revRelDocsMap.getRelationList(revertedType, relName);
        if (revRelList != null)
        {
          String revRelDocId = revRelList.get(0).getDocId();
          if (!newDocId.equals(revRelDocId))
            throw new WebServiceException(RELATION_EXISTS);
        }
      }
    }
  }

  /**
   * Convert deprecated 'readRole' and 'writeRole' security properties to
   * AccessControl and adds to AccessControlList.
   * ACL has preference, only get security properties if ACL is not defined.
   */
  private void prepareAccessControlList(Document document,
    Document currentDocument)
  {
    //Only if document not provide ACL
    if (!(document.getAccessControl() != null
      && document.getAccessControl().size() > 0))
    {
      boolean hasRoleProperties = false;
      for (Property p : document.getProperty())
      {
        String name = p.getName();
        if (READ_ROLE.equals(name) || WRITE_ROLE.equals(name) ||
          DELETE_ROLE.equals(name))
        {
          hasRoleProperties = true;
          String action = name.equals(READ_ROLE) ? READ_ACTION : 
            (name.equals(WRITE_ROLE) ? WRITE_ACTION : DELETE_ACTION);
          for (String value : p.getValue())
          {
            //Add security properties to ACL
            AccessControl ac = new AccessControl();
            ac.setRoleId(value);
            ac.setAction(action);
            document.getAccessControl().add(ac);
          }
        }
      }
      
      //if incremental and document not provide role properties
      if (document.isIncremental() && !hasRoleProperties && 
        currentDocument != null)
      {
        document.getAccessControl().clear();
        document.getAccessControl().addAll(currentDocument.getAccessControl());
      }
    }

    deleteSecurityProperties(document.getProperty());
  }

  /**
   * Remove deprecated 'readRole' & 'writeRole'.
   * @param document
   */
  private void deleteSecurityProperties(List<Property> properties)
  {
    if (properties != null && properties.size() > 0)
    {
      List<Property> delete = new ArrayList(2);
      for (Property p : properties)
      {
        if (p.getName().equals(READ_ROLE) ||
            p.getName().equals(WRITE_ROLE) ||
            p.getName().equals(DELETE_ROLE))
        {
          delete.add(p);
        }
      }
      properties.removeAll(delete);
    }
  }

  private void addNominalRole(User user, Document document)
  {
    Map map = new HashMap();
    for (AccessControl acl : document.getAccessControl())
    {
      map.put(acl.getRoleId() + ";" + acl.getAction(), acl);
    }

    String roleId = SecurityConstants.SELF_ROLE_PREFIX + 
      user.getUserId().trim() + SecurityConstants.SELF_ROLE_SUFFIX;
    AccessControl ac = new AccessControl();
    ac.setRoleId(roleId);
    ac.setAction(DictionaryConstants.READ_ACTION);
    if (map.get(roleId + ";" + DictionaryConstants.READ_ACTION) == null)
      document.getAccessControl().add(ac);

    ac = new AccessControl();
    ac.setRoleId(roleId);
    ac.setAction(DictionaryConstants.WRITE_ACTION);
    if (map.get(roleId + ";" + DictionaryConstants.WRITE_ACTION) == null)
      document.getAccessControl().add(ac);

    ac = new AccessControl();
    ac.setRoleId(roleId);
    ac.setAction(DictionaryConstants.DELETE_ACTION);
    if (map.get(roleId + ";" + DictionaryConstants.DELETE_ACTION) == null)
      document.getAccessControl().add(ac);
  }

  private void mergeDocument(Document newDocument, Document currentDocument)
  {
    if (newDocument.getTitle() == null)
      newDocument.setTitle(currentDocument.getTitle());
    if (newDocument.getLanguage() == null)
      newDocument.setLanguage(currentDocument.getLanguage());
    if (newDocument.getDocTypeId() == null)
      newDocument.setDocTypeId(currentDocument.getDocTypeId());
    if (newDocument.getState() == null)
      newDocument.setState(currentDocument.getState());
    if (newDocument.getCreationDate() == null)
      newDocument.setCreationDate(currentDocument.getCreationDate());
    if (newDocument.getAuthorId() == null || 
        newDocument.getAuthorId().isEmpty())
    {
      newDocument.getAuthorId().clear();
      newDocument.getAuthorId().addAll(currentDocument.getAuthorId());
    }
    if (newDocument.getClassId() == null || newDocument.getClassId().isEmpty())
    {
      newDocument.getClassId().clear();
      newDocument.getClassId().addAll(currentDocument.getClassId());
    }
    if (newDocument.getCaseId() == null || newDocument.getCaseId().isEmpty())
    {
      newDocument.getCaseId().clear();
      newDocument.getCaseId().addAll(currentDocument.getCaseId());
    }
  }
  
  private void mergeProperties(Document newDocument, Document currentDocument)
  {
    Map<String, Property> currentPropertiesMap = 
      createPropertiesMap(currentDocument);
    for (Property property : newDocument.getProperty())
    {
      currentPropertiesMap.remove(property.getName());
    }
    for(Property property : currentPropertiesMap.values())
    {
      newDocument.getProperty().add(property);
    }    
  }

  private Map createPropertiesMap(Document document)
  {
    Map propertiesMap = new HashMap();
    if (document != null)
    {
      for(Property property : document.getProperty())
      {
        String name = property.getName();
        if (!name.equals(READ_ROLE) &&
            !name.equals(WRITE_ROLE) &&
            !name.equals(DELETE_ROLE))
        {
          propertiesMap.put(property.getName(), property);
        }
      }    
    }
    return propertiesMap;
  }

  private File getDataFile(Content content) throws Exception
  {
    File dataFile = null;
    DataHandler dh = content.getData();
    if (dh != null) // internal
    {
      DataSource ds = dh.getDataSource();
      if (ds instanceof javax.activation.FileDataSource)
      {
        dataFile = ((javax.activation.FileDataSource)ds).getFile();
      }
      else if (ds != null)
      {
        if (content.getContentType() != null)
        {
          String ext = 
            MimeTypeMap.getMimeTypeMap().getExtension(content.getContentType());
          dataFile = File.createTempFile("stream", "." + ext);
          dataFile.deleteOnExit();
          IOUtils.writeToFile(ds.getInputStream(), dataFile);
        }
        else
          dataFile = IOUtils.writeToFile(ds.getInputStream());
      }
    }

    return dataFile;
  }

  private void logOperation(String operation, String messageType, 
    String message)
  {
    logOperation(operation, messageType, message, getCurrentUsername());
  }

  private void logOperation(String operation, String messageType,
    String message, String userId)
  {
    log.log(Level.INFO, "{0} {1}: {2}", 
      new Object[]{operation, messageType, message});

    if (csvLogger != null)
    {
      // TODO: CHECK URL
      HttpServletRequest request =
        (HttpServletRequest)wsContext.getMessageContext().
          get(MessageContext.SERVLET_REQUEST);
      String url = request.getRequestURL().toString();
      String ip = request.getRemoteAddr();

      csvLogger.log(
        getCurrentDateTime("dd/MM/yyyy-HH:mm:ss"),
        userId, ip, url, operation, messageType, message);
    }
  }

  private void logStoreDocument(Document document, String messageType,
    String userId)
  {
    String message = "docId=" + document.getDocId() +
        "&version=" + document.getVersion() +
        "&title=" + document.getTitle() +
        "&language" + document.getLanguage();

    //log content
    Content content = document.getContent();
    if (content != null)
    {
      if (content.getContentId() != null)
        message = message + "&contentId=" + content.getContentId();
      else
        message = message + (content.getUrl() != null ?
          "&url=" + content.getUrl() : "&newDataHandler");
    }

    //log properies
    for (Property p : document.getProperty())
    {
      message = message + "&" + DocumentUtils.printProperty(p);
    }

    //log acl
    for (AccessControl acl : document.getAccessControl())
    {
      message = message + "&acl=" + acl.getRoleId() + "," + acl.getAction();
    }
    
    logOperation("storeDocument", messageType, message, userId);
  }

  private String getCurrentDateTime(String format)
  {
    SimpleDateFormat df = new SimpleDateFormat(format);
    return df.format(new Date());
  }
  
  private String getCurrentUsername()
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    return credentials.getUserId();
  }
  
  private String generateUUID()
  {
    UUID uuid = UUID.randomUUID();
    return uuid.toString();
  }     
  
  private boolean isUserAdmin()
  {
    Set<String> userRoles = UserCache.getUser(wsContext).getRoles();
    return userRoles.contains(DocumentConstants.DOC_ADMIN_ROLE);
  }
  
  private long getExternalContentLength(String url)
  {
    long length = 0;
    URLConnection urlConnection;
    try
    {
      urlConnection = new URL(url).openConnection();
      length = urlConnection.getContentLength();
    }
    catch (IOException e)
    {
      length = -1;
    }    
    return length;
  }

  private void describeContent(Content content)
  {
    if (droid != null)
    {
      String formatId = content.getFormatId();
      if (formatId != null)
      {
        FileFormat format = droid.getFileFormat(formatId);
        setContentFormat(content, format);
      }
    }
  }

  private void identifyContent(Content content, File dataFile)
  {
    if (droid != null)
    {
      FileFormat format = droid.identify(dataFile.getAbsolutePath());
      if (format != null)
        setContentFormat(content, format);
    }
  }

  private boolean validateContentId(String contentId)
  {
    if (contentId == null || contentId.length() != 36)
      return false;
    try
    {
      UUID.fromString(contentId);
    }
    catch(Exception ex)
    {
      return false;
    }
    return true;
  }

  private void setContentFormat(Content content, FileFormat format)
  {
    String description = format.getName();
    String version = format.getVersion();
    if (version != null)
    {
      description += " version " + version;
    }
    content.setFormatId(format.getPUID());
    content.setFormatDescription(description);
    if (format.getMimeType() != null)
    {
      content.setContentType(format.getMimeType());
    }
  }
  
  private boolean checkLocking(String lockUser)
  {
    Credentials credentials = SecurityUtils.getCredentials(wsContext);
    return (lockUser == null || 
      lockUser.trim().equals(credentials.getUserId().trim()));
  }  

  private boolean canUserReadDocument(User user, Document document, Type type)
  {
    Set<String> userRoles = user.getRoles();

    return userRoles.contains(DocumentConstants.DOC_ADMIN_ROLE)
      || checkTypeACL(userRoles, type, READ_ACTION)
      || checkDocumentACL(userRoles, document, READ_ACTION);
  }

  private boolean canUserCreateDocument(User user, Type type)
  {
    Set<String> userRoles = user.getRoles();

    return (userRoles.contains(DocumentConstants.DOC_ADMIN_ROLE)
      || checkTypeACL(userRoles, type, CREATE_ACTION));
  }

  private boolean canUserModifyDocument(User user, 
    Document currentDocument, Type currentType)
  {
    Set<String> userRoles = user.getRoles();

    return (userRoles.contains(DocumentConstants.DOC_ADMIN_ROLE)
      || checkTypeACL(userRoles, currentType, WRITE_ACTION)
      || checkDocumentACL(userRoles, currentDocument, WRITE_ACTION));
  }

  private boolean canUserDeleteDocument(User user, Document document, Type type)
  {
    Set<String> userRoles = user.getRoles();

    return (userRoles.contains(DocumentConstants.DOC_ADMIN_ROLE)
      || checkTypeACL(userRoles, type, DELETE_ACTION)
      || checkDocumentACL(userRoles, document, DELETE_ACTION));
  }

  private boolean checkTypeACL(Set<String> userRoles, Type type, String action)
  {
    if (type == null || type.getTypeId() == null)
      return false;
    else
      return (type.canPerformAction(action, userRoles));
  }

  private boolean checkDocumentACL(Set<String> userRoles, Document document,
    String action)
  {
    List<AccessControl> acl = document.getAccessControl();
    for (AccessControl ac : acl)
    {
      if (ac.getAction().equals(action) && userRoles.contains(ac.getRoleId()))
        return true;
    }
    return false;
  }
  
  private boolean isReverseRelation(RelationType relType)
  {
    if (relType != null)
    {
      String relName = relType.toString();
      return relName != null && relName.startsWith("REV_");
    }
    else
      return false;
  }
  
//  private RelationType revertRelation(RelationType relType)
//  {
//    if (isReverseRelation(relType))
//    {
//      String text = relType.toString();
//      return RelationType.valueOf(text.substring(4));
//    }
//    else
//    {
//      return RelationType.valueOf("REV_" + relType.toString());
//    }
//  }

  private void validateDocument(Document document, Type type)
  {
    //Validate type
    if (document.getDocId() == null || !document.isIncremental())
    {
      if (type == null || type.getTypeId() == null || 
        type.getTypeId().trim().length() == 0)
      {
        throw new WebServiceException("dic:TYPE_NOT_FOUND");
      }

      Type rootType = type.getRootType();

      ExternalEntity typeEntity = getWSEndpoint().getExternalEntity("Type");
      String rootTypeId = typeEntity.toLocalId(rootType.getTypeId());
      if (!DictionaryConstants.DOCUMENT_TYPE.equals(rootTypeId))
        throw new WebServiceException("doc:NOT_DOCUMENT_TYPE");
    }

    if (type == null && !document.isIncremental())
      throw new WebServiceException("dic:TYPE_NOT_FOUND");

    //Validate system properties
    String title = document.getTitle();
    if ((title == null || title.trim().length() == 0)
      && !document.isIncremental())
    {
      throw new WebServiceException("VALUE_IS_MANDATORY");
    }

    String docTypeId = document.getDocTypeId();
    if ((docTypeId == null || docTypeId.trim().length() == 0)
      && !document.isIncremental())
      throw new WebServiceException("doc:DOCTYPEID_NOT_FOUND");

    for (String authorId : document.getAuthorId())
    {
      if (authorId == null || authorId.trim().length() == 0)
      {
        throw new WebServiceException("doc:INVALID_DOCUMENT_AUTHOR");
      }
    }

    //Related documents
    for (RelatedDocument relDoc : document.getRelatedDocument())
    {
      if (relDoc.getDocId() == null || relDoc.getDocId().trim().length() == 0)
      {
        throw new WebServiceException("doc:DOCUMENT_NOT_FOUND");
      }
      if (relDoc.getName() == null || relDoc.getName().trim().length() == 0)
      {
        throw new WebServiceException("VALUE_IS_MANDATORY");
      }
    }

    //Dictionary validations
    Set<String> unvalidable = new HashSet();
    unvalidable.add("docId");
    unvalidable.add("version");
    WSTypeValidator validator = new WSTypeValidator(type);
    validator.validate(document, unvalidable);
  }

  private WSEndpoint getWSEndpoint()
  {
    if (endpoint == null)
    {
      String endpointName = WSUtils.getServletAdapter(wsContext).getName();
      endpoint = WSDirectory.getInstance().getEndpoint(endpointName);
    }
    return endpoint;
  }

  private void validateDocumentFilter(DocumentFilter filter) throws Exception
  {
    if (filter.getDocId().isEmpty() &&
        StringUtils.isBlank(filter.getContentId()) &&
        StringUtils.isBlank(filter.getTitle()) &&
        StringUtils.isBlank(filter.getStartDate()) &&
        StringUtils.isBlank(filter.getEndDate()) &&
        StringUtils.isBlank(filter.getDocTypeId()) &&
        StringUtils.isBlank(filter.getContentSearchExpression()) &&
        StringUtils.isBlank(filter.getMetadataSearchExpression()) &&
        StringUtils.isBlank(filter.getLanguage()) &&
        filter.getClassId().isEmpty() &&
        filter.getProperty().isEmpty() &&
        filter.getMaxResults() == 0)
      throw new Exception("FILTER_NOT_ALLOWED");
  }
  
  private class RelatedDocumentsMap
  {
    private Map<String, List<RelatedDocument>> documents;
    
    public RelatedDocumentsMap(List<RelatedDocument> relDocs)
    {
      documents = new HashMap();
      putDocuments(relDocs);
    }
    
    public RelatedDocument getRelatedDocument(RelationType relType, 
      String relName, String docId)
    {
      return getFromList(getRelationList(relType, relName), docId);
    }
    
    public List<RelatedDocument> getRelationList(RelationType relType,
      String relName)
    {
      String key = relType.toString() + ";" + relName;

      return documents.get(key);
    }
    
    public List<RelatedDocument> getRelatedDocuments()
    {
      List<RelatedDocument> result = new ArrayList();
      Collection<List<RelatedDocument>> lists = documents.values();
      for (List<RelatedDocument> list : lists)
      {
        result.addAll(list);
      }
      
      return result;
    }
    
    private void putDocuments(List<RelatedDocument> relDocs)
    {
      if (documents == null)
        documents = new HashMap();
      
      if (relDocs != null)
      {
        for (RelatedDocument relDoc : relDocs)
        {
          String key = 
            relDoc.getRelationType().toString() + ";" + relDoc.getName();
          List<RelatedDocument> list = documents.get(key);
          if (list == null)
          {
            list = new ArrayList();
            documents.put(key, list);
          }
          list.add(relDoc);  
        }
      }
    }    
    
    private RelatedDocument getFromList(List<RelatedDocument> list, 
      String docId)
    {
      RelatedDocument result = null;
      if (list != null)
      {
        for (RelatedDocument relDoc : list)
        {
          if (docId.equals(relDoc.getDocId()))
            result = relDoc;
        }
      }
      return result;
    }


  }
}
