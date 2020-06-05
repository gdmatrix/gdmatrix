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
package org.santfeliu.signature.certificate;

import java.io.InputStream;
import java.util.List;
import javax.activation.DataHandler;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.MatrixConfig;

/**
 * Certificates stored in document manager as documents of the type defined 
 * in "org.santfeliu.signature.certificate.docTypeId" context parameter 
 * (sf:CertificatDigital) by default.
 * 
 * All properties defined as document metadata.
 * @author blanquepa
 */
public class DocumentManagerCertificateStore extends CertificateStore
{
  private static final String DOCTYPEID_PARAM = 
    PACKAGE + ".DocumentManagerCertificateStore.docTypeId";
  public static final String DEFAULT_DOCTYPEID = 
    "sf:CertificatDigital";
  
  private static final String KEYSTORE_TYPE = "ksType";
  private static final String KEYSTORE_PASSWORD = "ksPassword";
  private static final String KEY_ALIAS = "alias";
  private static final String KEY_PASSWORD = "password";
  
  private Document certDocument;
  private DocumentManagerClient client;
  
  public DocumentManagerCertificateStore(String name) throws Exception
  {
    super(name);
  }
  
  @Override
  protected void init() throws Exception
  {
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");
    client = new DocumentManagerClient(userId, password);
    
    DocumentFilter df = new DocumentFilter();
    String docTypeId = MatrixConfig.getProperty(DOCTYPEID_PARAM);
    df.setDocTypeId(docTypeId != null ? docTypeId : DEFAULT_DOCTYPEID);
    
    //It's assumed that certificate name equals to its alias.
    DictionaryUtils.setProperty(df, KEY_ALIAS, this.name);
    df.getOutputProperty().add(KEY_ALIAS);
    df.getOutputProperty().add(KEY_PASSWORD);
    df.getOutputProperty().add(KEYSTORE_TYPE);
    df.getOutputProperty().add(KEYSTORE_PASSWORD);
    
    List<Document> documents = client.findDocuments(df);
    if (documents != null && !documents.isEmpty())
      certDocument = documents.get(0);
    else
      throw new Exception("CERTIFICATE_DOCUMENT_NOT_FOUND");
  }  

  @Override
  protected String getKeyAlias()
  {
    String keyAlias = null;
    if (certDocument != null)
      keyAlias = DocumentUtils.getPropertyValue(certDocument, KEY_ALIAS);
    return keyAlias;
  }

  @Override
  protected String getKeyPassword()
  {
    String keyPassword = null;
    if (certDocument != null)    
      keyPassword = DocumentUtils.getPropertyValue(certDocument, KEY_PASSWORD);
    return keyPassword;
  }

  @Override
  protected String getKeyStoreType()
  {
    String keyStoreType = null;
    if (certDocument != null)     
      keyStoreType = 
        DocumentUtils.getPropertyValue(certDocument, KEYSTORE_TYPE);
    return keyStoreType;
  }

  @Override
  protected InputStream getFileStream() throws Exception
  {
    if (certDocument == null) 
      return null;
    
    Content content = certDocument.getContent();
    if (content == null) 
      return null;
    
    content = client.loadContent(content.getContentId());
    DataHandler dataHandler = content.getData();
    if (dataHandler == null)
      return null;
    
    return dataHandler.getInputStream();
  }

  @Override
  protected String getKeyStorePassword()
  {
    String keyStorePassword = null;
    if (certDocument != null)      
      keyStorePassword = 
        DocumentUtils.getPropertyValue(certDocument, KEYSTORE_PASSWORD);
    
    //If no keyStorePassword defined then takes key password
    if (keyStorePassword == null)
      keyStorePassword = getKeyPassword();
    
    return keyStorePassword;
  }

  
}
