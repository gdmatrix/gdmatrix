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
import java.lang.reflect.Constructor;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public abstract class CertificateStore
{
  protected static final String PACKAGE = "org.santfeliu.signature.certificate";  
  private static final String STORE_CLASS_PARAM = PACKAGE + "." + "storeClass";
  
  protected String name;
  protected KeyStore keyStore;
  
  public CertificateStore(String name) throws Exception 
  {
    this.name = name;
    init();
    loadKeyStore();
  }
  
  public X509Certificate getCertificate() throws KeyStoreException
  {
    return (X509Certificate)keyStore.getCertificate(getKeyAlias());
  }

  public PrivateKey getPrivateKey() throws Exception
  {
    return (PrivateKey)keyStore.getKey(getKeyAlias(), 
      getKeyPassword().toCharArray());
  }
  
  private void loadKeyStore() throws Exception
  {
    keyStore = KeyStore.getInstance(getKeyStoreType());
    InputStream is = getFileStream();
    if (is == null)
      throw new Exception("signature:INVALID_KEYSTORE_CONTENT");
    else
      keyStore.load(is, getKeyStorePassword().toCharArray());      
  }
  
  public static CertificateStore getInstance(String name) throws Exception
  {
    CertificateStore certStore = null;
    
    String storeClassName = MatrixConfig.getProperty(STORE_CLASS_PARAM); 
    if (storeClassName != null)
    {
      try
      {
        Class storeClass = Class.forName(storeClassName);
        Constructor constructor = storeClass.getConstructor(String.class);
        certStore = (CertificateStore)constructor.newInstance(name);
      }
      catch (ClassNotFoundException ex)
      {
        throw new Exception("signature:INVALID_CERTIFICATE_STORE", ex);
      }
      catch (Exception ex)
      {
        throw new Exception("signature:CANNOT_INSTANTIATE_CERTIFICATE_STORE", ex);
      }
    }
    else
      certStore = new MatrixConfigCertificateStore(name);
    
    return certStore;    
  }
  
  protected abstract void init() throws Exception;
  
  protected abstract String getKeyAlias();
  
  protected abstract String getKeyPassword();  
  
  protected abstract String getKeyStoreType();
  
  protected abstract InputStream getFileStream() throws Exception;
  
  protected abstract String getKeyStorePassword();  
  
}
