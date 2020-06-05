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
package org.santfeliu.signature.store;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.matrix.dic.DictionaryConstants;
import org.matrix.security.AccessControl;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.Document;
import org.matrix.doc.State;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.signature.SignedDocument;
import org.santfeliu.signature.SignedDocumentStore;
import org.santfeliu.util.MemoryDataSource;

/**
 *
 * @author realor
 */
public class DocumentManagerStore implements SignedDocumentStore
{
  public static final String DEFAULT_DOCTYPEID = "SIGNATURE";

  private String userId;
  private String password;

  public DocumentManagerStore()
  {
  }

  @Override
  public void init(Properties properties)
  {
    this.userId = properties.getProperty("adminCredentials.userId");
    this.password = properties.getProperty("adminCredentials.password");
  }

  @Override
  public String createSignedDocument(SignedDocument signedDocument)
    throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    signedDocument.writeDocument(bos);

    DataSource dataSource = new MemoryDataSource(
      bos.toByteArray(), "document", signedDocument.getMimeType());
    
    Document newDocument = new Document();
    newDocument.setDocTypeId(DEFAULT_DOCTYPEID);
    newDocument.setState(State.DRAFT);
    if (!signedDocument.getProperties().containsKey(DocumentConstants.TITLE))
    {
      newDocument.setTitle("SignedDocument");
    }
    DataHandler dh = new DataHandler(dataSource);
    DocumentUtils.setContentData(newDocument, dh);
    DocumentUtils.setProperty(newDocument, "SignedDocumentClass", 
      signedDocument.getClass().getName());
    DocumentUtils.setProperties(newDocument, signedDocument.getProperties());

    String signatureLanguage = 
      (String)signedDocument.getProperties().get(DocumentConstants.LANGUAGE);
    if (signatureLanguage == null) signatureLanguage = 
      TranslationConstants.UNIVERSAL_LANGUAGE;
    newDocument.setLanguage(signatureLanguage);    
    
    // service call
    newDocument = client.storeDocument(newDocument);

    // lock signedDocument to avoid accidental removal
    client.lockDocument(newDocument.getDocId(), 0);

    return docIdToSigId(newDocument.getDocId());
  }

  @Override
  public void updateSignedDocument(String sigId, SignedDocument signedDocument)
    throws Exception
  {
    signedDocument.setId(sigId);
    
    String docId = sigIdToDocId(sigId);

    DocumentManagerClient client = getDocumentManagerClient();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    signedDocument.writeDocument(bos);

    DataSource dataSource = new MemoryDataSource(
      bos.toByteArray(), "document", signedDocument.getMimeType());

    Document newDocument = new Document();
    DocumentUtils.setProperties(newDocument, signedDocument.getProperties());
    newDocument.setDocId(docId);
    newDocument.setVersion(0);
    newDocument.setState(State.DRAFT);
    if (!signedDocument.getProperties().containsKey(DocumentConstants.TITLE))
    {
      newDocument.setTitle("SignedDocument");
    }
    DataHandler dh = new DataHandler(dataSource);
    DocumentUtils.setContentData(newDocument, dh);
    DocumentUtils.setProperty(newDocument, "SignedDocumentClass", 
      signedDocument.getClass().getName());
    DocumentUtils.setProperty(newDocument, "sigId", sigId);

    String signatureLanguage = 
      (String)signedDocument.getProperties().get(DocumentConstants.LANGUAGE);
    if (signatureLanguage == null) signatureLanguage = 
      TranslationConstants.UNIVERSAL_LANGUAGE;
    newDocument.setLanguage(signatureLanguage);
    newDocument.setIncremental(false);

    client.storeDocument(newDocument);  
  }

  @Override
  public SignedDocument loadSignedDocument(String sigId)
    throws Exception
  {
    String docId = sigIdToDocId(sigId);

    DocumentManagerClient client = getDocumentManagerClient();
    Document document = client.loadDocument(docId, 0);
    
    String docClassName = 
      DocumentUtils.getPropertyValue(document, "SignedDocumentClass");
    Class docClass = Class.forName(docClassName);
    SignedDocument signedDocument = (SignedDocument)docClass.newInstance();

    DataHandler dataHandler = document.getContent().getData();
    signedDocument.parseDocument(dataHandler.getInputStream());

    // load user properties
    Map docMap = DocumentUtils.getUserProperties(document);
    Iterator iter = docMap.keySet().iterator();
    while (iter.hasNext())
    {
      // take first value of user properties
      String propertyName = String.valueOf(iter.next());
      List list = (List)docMap.get(propertyName);
      if (list.size() > 0 && list.get(0) != null)
      {
        String propertyValue = String.valueOf(list.get(0));
        signedDocument.getProperties().put(propertyName, propertyValue);
      }
    }
    // load system properties
    signedDocument.getProperties().put(DocumentConstants.TITLE,
      document.getTitle());
    signedDocument.getProperties().put(DocumentConstants.DOCTYPEID,
      document.getDocTypeId());
    signedDocument.getProperties().put(DocumentConstants.CREATION_DATE,
      document.getCreationDate());
    if (document.getClassId().size() > 0)
    {
      signedDocument.getProperties().put(DocumentConstants.CLASSID,
        document.getClassId().get(0));
    }
    
    //load signedDocument roles as properties
    for (AccessControl ac : document.getAccessControl())
    {
      if (DictionaryConstants.READ_ACTION.equals(ac.getAction())
        && signedDocument.getProperties().get(DocumentConstants.READ_ROLE) == null)
        signedDocument.getProperties().put(DocumentConstants.READ_ROLE, ac.getRoleId());
      else if (DictionaryConstants.WRITE_ACTION.equals(ac.getAction())
        && signedDocument.getProperties().get(DocumentConstants.WRITE_ROLE) == null)
        signedDocument.getProperties().put(DocumentConstants.WRITE_ROLE, ac.getRoleId());
    }
    
    checkSigId(sigId, signedDocument);

    return signedDocument;
  }

  @Override
  public void closeSignedDocument(String sigId)
    throws Exception
  {
    String docId = sigIdToDocId(sigId);

    DocumentManagerClient client = getDocumentManagerClient();
    Document document = client.loadDocument(docId);
    document.setState(State.COMPLETE);
    client.storeDocument(document);
    client.unlockDocument(docId, document.getVersion());
  }

  @Override
  public void deleteSignedDocument(String sigId)
    throws Exception
  {
    String docId = sigIdToDocId(sigId);

    DocumentManagerClient client = getDocumentManagerClient();
    client.removeDocument(docId, -2);
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    return new DocumentManagerClient(userId, password);
  }

  // sigId format: docId-UUID

  private String docIdToSigId(String docId)
  {
    return docId + "-" + UUID.randomUUID().toString();
  }

  private String sigIdToDocId(String sigId)
  {
    int index = sigId.indexOf("-");
    if (index == -1) return sigId;

    String docId = sigId.substring(0, index);
    return docId;
  }
  
  private void checkSigId(String sigId, SignedDocument signedDocument)
    throws Exception
  {
    String docSigId = (String)signedDocument.getProperties().get("sigId");
    if (docSigId != null)
    {
      if (!docSigId.equals(sigId)) throw new Exception("signature:INVALID_SIGID");
    }
  }
}
