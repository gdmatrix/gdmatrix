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
package org.santfeliu.util.pdf.signature;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.santfeliu.security.SecurityProvider;
import org.santfeliu.security.util.SecurityUtils;

/**
 *
 * @author blanquepa
 */
public abstract class PDFSigner
{
  public static final SignatureLevel DEFAULT_SIGNATURE_LEVEL = 
    SignatureLevel.B;
  
  protected String ksPath;
  protected char[] ksPassword;
  protected KeyStore keyStore;
  protected String alias;  
  
  protected String tsaUrl;
  
  public PDFSigner()
  {
  }
  
  public PDFSigner(String tsaUrl)
  {
    this.tsaUrl = tsaUrl;
  }
  
  public static PDFSigner getInstance()
  {
    //Only exists Pdfbox implementation at the moment.
    return new PdfboxSigner();
  }
  
  public String getTsaUrl()
  {
    return tsaUrl;
  }

  public void setTsaUrl(String tsaUrl)
  {
    this.tsaUrl = tsaUrl;
  }
  
  protected String getAlias() throws KeyStoreException, 
    NoSuchAlgorithmException, UnrecoverableKeyException, 
    CertificateExpiredException, CertificateNotYetValidException
  {
    Enumeration<String> aliases = this.keyStore.aliases();
    alias = null;
    Certificate cert = null;
    while (aliases.hasMoreElements())
    {
      alias = aliases.nextElement();
      Certificate[] certChain = keyStore.getCertificateChain(alias);
      if (certChain == null)
      {
          continue;
      }
      cert = certChain[0];
      if (cert instanceof X509Certificate)
      {
          // avoid expired certificate
          ((X509Certificate) cert).checkValidity();
      }
      break;
    }
    return alias;
  }
  
  public void sign(InputStream is, OutputStream os, String ksPath,
    String ksPassword) throws Exception
  {
    sign(is, os, ksPath, ksPassword, null, null, null, null, null);
  }
  
  public void sign(InputStream is, OutputStream os, String ksPath,
    String ksPassword, SignatureLevel sigLevel, 
    DigestAlgorithm digAlg, EncryptionAlgorithm encAlg,
    SignatureInfo sigInfo, DocumentInfo docInfo) throws Exception
  {
    if (sigLevel == null)
      sigLevel = DEFAULT_SIGNATURE_LEVEL;

    this.ksPath = ksPath;
    this.ksPassword = ksPassword.toCharArray();

    BouncyCastleProvider provider = new BouncyCastleProvider();
    Security.addProvider(provider);
    this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    this.keyStore.load(new FileInputStream(ksPath), this.ksPassword);

    this.alias = getAlias();

    if (digAlg != null)
      digAlg = DigestAlgorithm.SHA256;
    if (encAlg != null)
      encAlg = EncryptionAlgorithm.RSA;

    doSign(is, os, sigLevel, digAlg, encAlg, sigInfo, docInfo);
  }
  
  public void timestamp(InputStream is, OutputStream os, 
    boolean ltvEnabled) throws Exception
  {
    if (tsaUrl == null)
      throw new Exception("No TSA defined");
    
    doTimestamp(is, os, ltvEnabled);    
  }  
  
  public void preserve(InputStream is, OutputStream os)
    throws Exception
  {
    if (tsaUrl == null)
      throw new Exception("No TSA defined");
           
    doPreserve(is, os);
  }
  
  public boolean validate(InputStream is, OutputStream os)
  {
    SecurityProvider securityProvider = SecurityUtils.getSecurityProvider();
    byte[] pdf;
    try
    {
      pdf = IOUtils.toByteArray(is);
      return securityProvider.validateSignaturePDF(pdf, os);      
    }
    catch (IOException ex)
    {
      Logger.getLogger(PDFSigner.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }
  
  protected abstract void doSign(InputStream is, OutputStream os, 
    SignatureLevel sigLevel, DigestAlgorithm digAlg, 
    EncryptionAlgorithm encAlg, SignatureInfo sigInfo, DocumentInfo docInfo) 
      throws Exception;
  
  protected abstract void doTimestamp(InputStream is, OutputStream os,
    boolean ltvEnabled) throws Exception;

  protected abstract void doPreserve(InputStream is, OutputStream os)
    throws Exception;
}
    

