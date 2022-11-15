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

import java.io.FileInputStream;
import java.io.InputStream;
import org.santfeliu.util.MatrixConfig;

/**
 * Certificates stored in MatrixConfig folder, "/etc/matrix" by default.
 * Key and KeyStore properties defined as context-param in web.xml.
 * 
 * @author blanquepa
 */
public class MatrixConfigCertificateStore extends CertificateStore
{
  private static final String KEY_STORE_PATH = "keyStorePath";
  private static final String KEY_STORE_TYPE = "keyStoreType";
  private static final String KEY_STORE_PASSWORD = "keyStorePassword";
  private static final String KEY_ALIAS = "keyAlias";
  private static final String KEY_PASSWORD = "keyPassword";  
  
  public MatrixConfigCertificateStore(String name) throws Exception
  {
    super(name);
  }
  
  @Override
  protected void init() throws Exception
  {
  }  

  /**
   * Defined as context paramater: 
   * org.santfeliu.signature.certificate.{name}.keyAlias
   * 
   * @return key alias
   */
  @Override
  protected String getKeyAlias()
  {
    return getProperty(KEY_ALIAS);
  }

  /**
   * Defined as context paramater: 
   * org.santfeliu.signature.certificate.{name}.keyStoreType
   * 
   * @return keyStore type
   */
  @Override
  protected String getKeyStoreType()
  {
    return getProperty(KEY_STORE_TYPE);
  }

  /**
   * Defined as context paramater: 
   * org.santfeliu.signature.certificate.{name}.keyStorePassword
   * 
   * @return keyStore password
   */  
  @Override
  protected String getKeyStorePassword()
  {
    return  getProperty(KEY_STORE_PASSWORD);
  }

  /**
   * Defined as context parameter:
   * org.santfeliu.signature.certificate.{name}.keyPassword
   * 
   * @return 
   */
  @Override
  protected String getKeyPassword()
  {
    return getProperty(KEY_PASSWORD);  
  }
  
  /**
   * Needs keyStore path defined as context parameter:
   * org.santfeliu.signature.certificate.{name}.keyStorePath
   * 
   * @return key content stream
   * @throws Exception 
   */
  @Override
  protected InputStream getFileStream() throws Exception
  {
    String keyStorePath = 
      MatrixConfig.getPathProperty(PACKAGE + "." + name + "." + KEY_STORE_PATH);
    return new FileInputStream(keyStorePath);
  }  
  
  private String getProperty(String propName)
  {
    return MatrixConfig.getProperty(PACKAGE + "." + name + "." + propName); 
  }
  
}
