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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class CACertificateStore
{
  private static final Logger LOGGER = Logger.getLogger("CACertificateStore");
  private static final String KS_PASSWORD = "changeit";
  private static CACertificateStore instance;
  private List<X509Certificate> caCerts;

  public static synchronized CACertificateStore getInstance()
  {
    if (instance == null)
    {
      instance = new CACertificateStore();
      try
      {
        instance.loadCertificates();
      }
      catch (Exception ex)
      {
        instance.caCerts = Collections.EMPTY_LIST;
        LOGGER.log(Level.SEVERE, "Error loading CA certificates", ex);
      }
    }
    return instance;
  }

  public List<X509Certificate> getCACertificates()
  {
    return caCerts;
  }

  public X509Certificate getIssuerCertificate(X509Certificate cert)
  {
    for (X509Certificate caCert : caCerts)
    {
      if (caCert.getSubjectX500Principal().equals(
        cert.getIssuerX500Principal()))
      {
        return caCert;
      }
    }
    return null;
  }

  public List<X509Certificate> getCertificateChain(X509Certificate cert)
  {
    List<X509Certificate> certChain = new ArrayList<>();
    X509Certificate issuer = getIssuerCertificate(cert);
    while (issuer != null && issuer != cert)
    {
      certChain.add(issuer);
      cert = issuer;
      issuer = getIssuerCertificate(cert);
    }
    return certChain;
  }

  public X509CRL getCertificateCRL(X509Certificate cert) throws Exception
  {
    byte[] crlDPExtensionValue = cert.getExtensionValue("2.5.29.31");
    if (crlDPExtensionValue == null) return null;

    ASN1InputStream asn1In = new ASN1InputStream(crlDPExtensionValue);
    DEROctetString crlDEROctetString = (DEROctetString)asn1In.readObject();
    ASN1InputStream asn1InOctets = new ASN1InputStream(crlDEROctetString.getOctets());
    ASN1Primitive crlDERObject = asn1InOctets.readObject();
    CRLDistPoint distPoint = CRLDistPoint.getInstance(crlDERObject);
    List<String> crlUrls = new ArrayList<>();
    for (DistributionPoint dp : distPoint.getDistributionPoints())
    {
      DistributionPointName dpn = dp.getDistributionPoint();
      if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME)
      {
        GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
        for (GeneralName genName : genNames)
        {
          if (genName.getTagNo() == GeneralName.uniformResourceIdentifier)
          {
            String url = DERIA5String.getInstance(genName.getName())
              .getString().trim();
            crlUrls.add(url);
          }
        }
      }
    }
    for (String crlUrl : crlUrls)
    {
      try
      {
        URL url = new URL(crlUrl);
        InputStream crlStream = url.openStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509CRL)cf.generateCRL(crlStream);
      }
      catch (IOException | CRLException | CertificateException ex)
      {
        LOGGER.log(Level.SEVERE, "Can't load CRL from {0}", crlUrl);
      }
    }
    return null;
  }

  private void loadCertificates() throws Exception
  {
    List<X509Certificate> caCertsList = new ArrayList<>();

    String relativeCacertsPath =
      "/lib/security/cacerts".replace("/", File.separator);
    String filename = System.getProperty("java.home") + relativeCacertsPath;

    LOGGER.log(Level.INFO, "Loading cacerts from {0}", filename);

    String password = MatrixConfig.getPathProperty(
      "org.santfeliu.signature.certificate.CACertificateStore.password");
    if (password == null) password = KS_PASSWORD;

    KeyStore keyStore;
    try (FileInputStream is = new FileInputStream(filename))
    {
      keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(is, password.toCharArray());
    }
    Enumeration<String> aliases = keyStore.aliases();
    while (aliases.hasMoreElements())
    {
      Certificate caCert = keyStore.getCertificate(aliases.nextElement());
      if (caCert instanceof X509Certificate)
      {
        caCertsList.add((X509Certificate)caCert);
        LOGGER.log(Level.INFO, "CA Certificate loaded: {0}",
          ((X509Certificate)caCert).getSubjectDN());
      }
    }
    caCerts = caCertsList;
  }
}
