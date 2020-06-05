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
package org.santfeliu.signature.service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import javax.xml.ws.WebServiceContext;
import org.matrix.signature.DataHash;

import org.matrix.signature.PropertyList;
import org.matrix.signature.SignatureManagerPort;

import org.santfeliu.security.SecurityProvider;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.signature.PropertyListConverter;
import org.santfeliu.signature.SignedDocument;
import org.santfeliu.signature.SignedDocumentStore;
import org.santfeliu.signature.certificate.CertificateStore;
import org.santfeliu.signature.certificate.MatrixConfigCertificateStore;
import org.santfeliu.signature.xmldsig.ByteArrayOutputStream;
import org.santfeliu.util.MatrixConfig;


/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.signature.SignatureManagerPort")
@HandlerChain(file="handlers.xml")
public class SignatureManager implements SignatureManagerPort
{
  @Resource
  WebServiceContext wsContext;

  protected static final Logger log = Logger.getLogger("Signature");

  public static final String[][] documentTypes = new String[][]
  {
    {"mxades", "org.santfeliu.signature.xmldsig.XMLSignedDocument"},
    {"xmldsig", "org.santfeliu.signature.xmldsig.XMLSignedDocument"}
  };

  // Matrix config properties
  public static final String STORE_CLASS_NAME = "storeClassName";
  public static final String VALIDATE_CERTIFICATE = "validateCertificate";
  public static final String USER_SIGN_OID = "userSignOID";
  public static final String USER_SIGN_HASH = "userSignHash";
  public static final String SYSTEM_SIGN_OID = "systemSignOID";
  public static final String SYSTEM_SIGN_HASH = "systemSignHash";


  
  // document properties
  public static final String SIGNED_DOCUMENT_TYPE = "type";
  public static final String SIGNING_REQUEST_TIME = "SigningRequestTime";
  public static final String SIGNERS = "signers";
  
  // other constants
  public static final String SIGNER_ID = "SERIALNUMBER";

  // signature states
  private static final int NO_SIGNATURE = 0;
  private static final int SIGNATURE_PENDENT = 1;
  private static final int SIGNATURE_TIMEOUT = 2;

  private SignedDocumentStore store;
  private boolean validateCertificate;
  private String userSignOID;
  private String userSignHash;
  private String systemSignOID;
  private String systemSignHash;

  public SignatureManager()
  {
    try
    {
      log.info("SignatureManager init");    
      String storeClassName =
        MatrixConfig.getClassProperty(SignatureManager.class, STORE_CLASS_NAME);
      validateCertificate = "true".equals(
        MatrixConfig.getClassProperty(SignatureManager.class, VALIDATE_CERTIFICATE));
      userSignOID = 
        MatrixConfig.getClassProperty(SignatureManager.class, USER_SIGN_OID);
      userSignHash = 
        MatrixConfig.getClassProperty(SignatureManager.class, USER_SIGN_HASH);
      systemSignOID = 
        MatrixConfig.getClassProperty(SignatureManager.class, SYSTEM_SIGN_OID);
      systemSignHash = 
        MatrixConfig.getClassProperty(SignatureManager.class, SYSTEM_SIGN_HASH);
      
      if (storeClassName == null)
        throw new Exception("UNDEFINED_STORE_CLASS_NAME");

      // create SignedDocumentStore
      store = createStore(storeClassName, MatrixConfig.getProperties());
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, "SignatureManager init failed", ex);
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String createDocument(String docType, PropertyList propertyList)
  {
    try
    {
      log.log(Level.INFO, "createDocument: docType:{0}", docType);
      // create SignedDocument
      SignedDocument document = createDocumentInstance(docType);
      document.newDocument();
      if (propertyList != null)
      {
        document.getProperties().putAll(
          PropertyListConverter.toMap(propertyList));
      }
      String sigId = store.createSignedDocument(document);
      return sigId;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String putDocument(org.matrix.signature.SignedDocument sdoc)
  {
    try
    {
      log.log(Level.INFO, "putDocument: docType:{0}", sdoc.getType());
      String docType = sdoc.getType();
      byte[] data = sdoc.getData();
      Map properties = PropertyListConverter.toMap(sdoc.getProperties());
  
      SignedDocument document = createDocumentInstance(docType);
      document.parseDocument(new ByteArrayInputStream(data));
      if (properties != null)
      {
        document.getProperties().putAll(properties);
      }
      // verify document signatures
      if (!document.verifyDocument())
      {
        throw new RuntimeException("signature:INVALID_SIGNATURE");
      }
      String sigId = store.createSignedDocument(document);
      return sigId;
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public byte[] getDocumentData(String sigId)
  {
    try
    {
      log.log(Level.INFO, "getDocumentData: sigId:{0}", sigId);
      SignedDocument document = store.loadSignedDocument(sigId);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      document.writeDocument(os);
      return os.toByteArray();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public org.matrix.signature.SignedDocument getDocument(String sigId)
  {
    try
    {
      log.log(Level.INFO, "getDocument: sigId:{0}", sigId);
      SignedDocument document = store.loadSignedDocument(sigId);
      Map properties = document.getProperties();
  
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      document.writeDocument(os);
      
      org.matrix.signature.SignedDocument sdoc = 
        new org.matrix.signature.SignedDocument();
  
      sdoc.setType((String)properties.get(SIGNED_DOCUMENT_TYPE));
      sdoc.setData(os.toByteArray());
      sdoc.setProperties(PropertyListConverter.toPropertyList(properties));
  
      return sdoc;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public String setDocumentProperties(String sigId, PropertyList propertyList)
  {
    try
    {
      log.log(Level.INFO, "setDocumentProperties: sigId:{0}", sigId);
      Map properties = PropertyListConverter.toMap(propertyList);
      if (properties != null)
      {
        SignedDocument document = store.loadSignedDocument(sigId);
        document.getProperties().putAll(properties); // remove DELETE properties
        store.updateSignedDocument(sigId, document);
      }
      return "done";
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public String addData(String sigId, String dataType, 
    byte[] data, PropertyList propertyList)
  {
    try
    {
      log.log(Level.INFO, "addData: sigId:{0} dataType:{1}", 
        new Object[]{sigId, dataType});
      SignedDocument document = store.loadSignedDocument(sigId);
      Map properties = PropertyListConverter.toMap(propertyList);
  
      int signatureState = getSignatureState(document);
      if (signatureState == SIGNATURE_PENDENT)
      {
        throw new RuntimeException("signature:INVALID_SIGNATURE_STATE");
      }
      else if (signatureState == SIGNATURE_TIMEOUT)
      {
        document.removeSignature();
        document.getProperties().remove(SIGNING_REQUEST_TIME);
      }
      String result = document.addData(dataType, data, properties);
      store.updateSignedDocument(sigId, document);
  
      return result;
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }    
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public byte[] addSignature(String sigId, byte[] certData)
  {
    try
    {
      log.log(Level.INFO, "addSignature: sigId:{0}", sigId);
      SignedDocument document = store.loadSignedDocument(sigId);
  
      int signatureState = getSignatureState(document);
      if (signatureState == SIGNATURE_PENDENT)
      {
        throw new RuntimeException("signature:INVALID_SIGNATURE_STATE");
      }
      else if (signatureState == SIGNATURE_TIMEOUT)
      {
        document.removeSignature();
        document.getProperties().remove(SIGNING_REQUEST_TIME);
      }
      CertificateFactory cf = CertificateFactory.getInstance("X509");
      X509Certificate cert = (X509Certificate)
        cf.generateCertificate(new ByteArrayInputStream(certData));
  
      String signerNIF = null; // the NIF or NIE
      String signerCIF = null; // the CIF
      if (validateCertificate)
      {
        Map attributes = new HashMap();
        // validate in CATCert
        log.log(Level.INFO, "validating certificate sigId:{0}", sigId);
        boolean valid = false;

        SecurityProvider provider = SecurityUtils.getSecurityProvider();
        valid = provider.validateCertificate(certData, attributes);

        if (!valid) throw new RuntimeException("INVALID_CERTIFICATE");
        signerNIF = (String)attributes.get(SecurityProvider.NIF);
        signerCIF = (String)attributes.get(SecurityProvider.CIF);
      }
      else // accept any certificate
      {
        Map attributes = SecurityUtils.getCertificateAttributes(cert);
        signerNIF = (String)attributes.get(SIGNER_ID);
      }
      String signerList = (String)document.getProperties().get(SIGNERS);
      if (!isValidSigner(signerNIF, signerCIF, signerList))
        throw new RuntimeException("signature:INVALID_SIGNER");
  
      byte[] dataToSign = document.addSignature(cert, 
        userSignOID, userSignHash);
  
      document.getProperties().put(SIGNING_REQUEST_TIME,
        String.valueOf(System.currentTimeMillis()));
  
      store.updateSignedDocument(sigId, document);
  
      return dataToSign;
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String endSignature(String sigId, byte[] signatureData)
  {
    try
    {
      log.log(Level.INFO, "endSignature: sigId:{0}", sigId);
      SignedDocument document = store.loadSignedDocument(sigId);
      
      int signatureState = getSignatureState(document);
      if (signatureState == SIGNATURE_PENDENT)
      {
        document.setSignatureValue(signatureData);
        document.getProperties().remove(SIGNING_REQUEST_TIME);
  
        // verify document signatures
        if (document.verifyDocument()) // is OK
        {
          store.updateSignedDocument(sigId, document);
          return "signature is valid";
        }
        else
        {
          document.removeSignature(); // undo signature
          store.updateSignedDocument(sigId, document);
          throw new RuntimeException("signature:INVALID_SIGNATURE");
        }
      }
      else if (signatureState == SIGNATURE_TIMEOUT)
      {
        throw new RuntimeException("signature:SIGNATURE_TIMEOUT");
      }
      else // NO_SIGNATURE
      {
        throw new RuntimeException("signature:INVALID_SIGNATURE_STATE");
      }
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String addSystemSignature(String sigId, String name)
  {
    try
    {
      log.log(Level.INFO, "addSystemSignature: sigId:{0}", sigId);
      SignedDocument document = store.loadSignedDocument(sigId);
  
      if (name == null) throw new RuntimeException("UNDEFINED_CERTIFICATE");
 
      int signatureState = getSignatureState(document);
      if (signatureState == SIGNATURE_PENDENT)
      {
        throw new Exception("signature:INVALID_SIGNATURE_STATE");
      }
      else if (signatureState == SIGNATURE_TIMEOUT)
      {
        document.removeSignature();
        document.getProperties().remove(SIGNING_REQUEST_TIME);
      }

      CertificateStore certStore = CertificateStore.getInstance(name);
      
      X509Certificate certificate = certStore.getCertificate();
      byte[] dataToSign = document.addSignature(certificate, 
        systemSignOID, systemSignHash);
      Signature signature = Signature.getInstance("SHA1withRSA");
      signature.initSign(certStore.getPrivateKey());
      signature.update(dataToSign);
      byte[] signatureData = signature.sign();
      document.setSignatureValue(signatureData);
  
      // verify document signatures
      if (document.verifyDocument()) // is OK
      {
        store.updateSignedDocument(sigId, document);
        return "signature is valid";
      }
      else
      {
        document.removeSignature(); // undo signature
        store.updateSignedDocument(sigId, document);
        throw new Exception("signature:INVALID_SIGNATURE");
      }
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String abortSignature(String sigId)
  {
    try
    {
      log.log(Level.INFO, "abortSignature: sigId:{0}", sigId);  
      SignedDocument document = store.loadSignedDocument(sigId);
  
      int signatureState = getSignatureState(document);
      if (signatureState == SIGNATURE_PENDENT || 
          signatureState == SIGNATURE_TIMEOUT)
      {
        document.removeSignature();
        document.getProperties().remove(SIGNING_REQUEST_TIME);
        store.updateSignedDocument(sigId, document);
      }
      return "signature aborted";
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public List<DataHash> digestData(String sigId)
  {
    try
    {
      log.log(Level.INFO, "digestData: sigId:{0}", sigId);  
      SignedDocument document = store.loadSignedDocument(sigId);  
      return document.digestData();
    }
    catch (RuntimeException ex)
    {
      throw ex;  
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String addExternalSignature(String sigId, byte[] signature)
  {
    try
    {
      log.log(Level.INFO, "addExternalSignature: sigId:{0}", sigId);  
      SignedDocument document = store.loadSignedDocument(sigId);
  
      int signatureState = getSignatureState(document);
      if (signatureState == SIGNATURE_PENDENT || 
          signatureState == SIGNATURE_TIMEOUT)
      {
        throw new RuntimeException("signature:INVALID_SIGNATURE_STATE");
      }
      document.addExternalSignature(signature);
      store.updateSignedDocument(sigId, document);
      return "done";
    }
    catch (RuntimeException ex)
    {
      throw ex;  
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public String endDocument(String sigId, PropertyList propertyList)
  {
    try
    {
      log.log(Level.INFO, "endDocument: sigId:{0}", sigId);    
      Map properties = PropertyListConverter.toMap(propertyList);
      SignedDocument document = store.loadSignedDocument(sigId);
      if (properties != null)
      {
        document.getProperties().putAll(properties);
      }
      int signatureState = getSignatureState(document);
      if (signatureState == SIGNATURE_PENDENT)
      {
        throw new RuntimeException("signature:INVALID_SIGNATURE_STATE");
      }
      else if (signatureState == SIGNATURE_TIMEOUT)
      {
        document.removeSignature();
        document.getProperties().remove(SIGNING_REQUEST_TIME);
        store.updateSignedDocument(sigId, document);
      }
      else // NO_SIGNATURE pendent
      {
        store.updateSignedDocument(sigId, document);
      }
      store.closeSignedDocument(sigId);
      return "document ended";
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String abortDocument(String sigId)
  {
    try
    {
      log.log(Level.INFO, "abortDocument: sigId:{0}", sigId);   
      store.deleteSignedDocument(sigId);
    
      return "document destroyed";
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  // ********** private methods ***********

  private boolean isValidSigner(
    String signerNIF, String signerCIF, String signerList)
  {
    if (signerList == null) return true;
    if (signerNIF == null && signerCIF == null) return false;

    boolean valid = false;
    StringTokenizer tokenizer = new StringTokenizer(signerList, " ,;");
    while (!valid && tokenizer.hasMoreTokens())
    {
      String token = tokenizer.nextToken();
      valid = token.equalsIgnoreCase(signerNIF) || 
        token.equalsIgnoreCase(signerCIF);
    }
    return valid;
  }

  private SignedDocument createDocumentInstance(String docType)
    throws Exception
  {
    // find document type class
    String typeClassName = null;
    int i = 0;
    while (i < documentTypes.length && typeClassName == null)
    {
      String t = documentTypes[i][0];
      if (t.equals(docType))
      {
        typeClassName = documentTypes[i][1];
      }
      else i++;
    }
    if (typeClassName == null) throw new Exception("UNSUPPORTED_DOCUMENT_TYPE");
    Class typeClass = Class.forName(typeClassName);
    SignedDocument document = (SignedDocument)typeClass.newInstance();
    document.getProperties().put(SIGNED_DOCUMENT_TYPE, docType);
    return document;
  }

  private SignedDocumentStore createStore(String storeClassName, 
    Properties properties) throws Exception
  {
    Class storeClass = Class.forName(storeClassName);
    SignedDocumentStore newStore = (SignedDocumentStore)storeClass.newInstance();
    newStore.init(properties);
    return newStore;
  }

  private int getSignatureState(SignedDocument document)
  {
    String stime = (String)document.getProperties().get(SIGNING_REQUEST_TIME);
    if (stime == null) return NO_SIGNATURE;
    
    long time = Long.parseLong(stime);
    return (System.currentTimeMillis() - time < 60000) ?
      SIGNATURE_PENDENT : SIGNATURE_TIMEOUT;
  }
}
