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
package org.santfeliu.doc.fmt.p7m;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.tsp.TSTInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;

/**
 *
 * @author realor
 */
public class P7MDocument
{
  CMSSignedData cms;

  public P7MDocument(CMSSignedData cms)
  {
    this.cms = cms;
  }

  public P7MDocument(InputStream isCMS) throws CMSException
  {
    this.cms = new CMSSignedData(isCMS);
  }

  public byte[] getSignedContent()
  {
    CMSProcessable data = cms.getSignedContent();
    return (byte[])data.getContent();
  }

  public byte[] getEncoded() throws IOException
  {
    return cms.getEncoded();
  }

  public void addTimeStamps(String serviceURI) throws Exception
  {
    cms = P7MUtils.addTimeStamp(serviceURI, cms);
  }

  public boolean writeSignedContent(OutputStream out)
    throws IOException
  {
    try
    {
      CMSProcessable data = cms.getSignedContent();
      data.write(out);
      return true;
    }
    catch (CMSException ex)
    {
      return false;
    }
  }

  public List<P7MSignature> getSignatures() throws Exception
  {
    ArrayList<P7MSignature> signatures = new ArrayList();
//    CertStore certStore = cms.getCertificatesAndCRLs("Collection", "BC");
    Store certStore = cms.getCertificates();
    SignerInformationStore siStore = cms.getSignerInfos();
    Collection signers = siStore.getSigners();
    for (Object elem : signers)
    {
      SignerInformation signer = (SignerInformation)elem;
      P7MSignature signature = new P7MSignature();
      signatures.add(signature);

      Collection certCollection = certStore.getMatches(signer.getSID());
//      Collection certCollection = certStore.getCertificates(certSelector);
      X509CertificateHolder certificateHolder =
        (X509CertificateHolder)certCollection.iterator().next();
      X509Certificate certificate =
        new JcaX509CertificateConverter().setProvider( "BC" )
        .getCertificate(certificateHolder);
      signature.setCertificate(certificate);

      signature.loadProperties();

      signature.setSignature(Base64.getMimeEncoder().encodeToString(
        signer.getSignature()).toUpperCase());

      // **** signed attributes ****
      AttributeTable table = signer.getSignedAttributes();
      Hashtable attributes = table.toHashtable();

      // signingTime
      Attribute attrib = (Attribute)attributes.get(
        new ASN1ObjectIdentifier("1.2.840.113549.1.9.5"));
      if (attrib != null)
      {
        ASN1UTCTime time = (ASN1UTCTime)attrib.getAttrValues().getObjectAt(0);
        String timeString = time.getAdjustedTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss'GMT+'00:00");
        signature.setSigningDate(df.parse(timeString));
      }

      // filename
      DEROctetString octet;
      attrib = (Attribute) attributes.get(
        new ASN1ObjectIdentifier("1.3.6.1.4.1.311.88.2.1"));
      if (attrib != null)
      {
        octet = (DEROctetString) attrib.getAttrValues().getObjectAt(0);
        if (octet != null)
        {
          signature.setFilename(new String(octet.getOctets(), "UTF-16LE"));
        }
      }

      // decretNumber
      attrib = (Attribute) attributes.get(
        new ASN1ObjectIdentifier("1.3.6.1.4.1.311.88.2.2"));
      if (attrib != null)
      {
        octet = (DEROctetString) attrib.getAttrValues().getObjectAt(0);
        if (octet != null)
        {
          signature.setDecretNumber(new String(octet.getOctets(), "UTF-16LE"));
        }
      }

      // **** unsigned attributes ****
      table = signer.getUnsignedAttributes();
      if (table != null)
      {
        attributes = table.toHashtable();
        // timeStampToken
        attrib = (Attribute)attributes.get(
          new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.14"));
        if (attrib != null)
        {
          ASN1Sequence seq = (ASN1Sequence)attrib.getAttrValues().getObjectAt(0);
          ContentInfo timeStampToken = ContentInfo.getInstance(seq);
          SignedData sd = SignedData.getInstance(timeStampToken.getContent());
          ASN1Set certificates = sd.getCertificates();
          ASN1Primitive derCert = certificates.getObjectAt(0).toASN1Primitive();
          byte[] certBytes = derCert.getEncoded();
          CertificateFactory certFactory = CertificateFactory.getInstance("X509");
          X509Certificate tsCertificate = (X509Certificate)certFactory.
            generateCertificate(new ByteArrayInputStream(certBytes));
          signature.setTimeStampCertificate(tsCertificate);

          ASN1Encodable content = sd.getEncapContentInfo().getContent();
//          TSTInfo tstInfo = new TSTInfo((ASN1Sequence)
//            new ASN1InputStream(((ASN1OctetString)content).getOctets()).readObject());
          TSTInfo tstInfo = TSTInfo.getInstance(((ASN1OctetString)content).getOctets());
          signature.setTimeStampDate(tstInfo.getGenTime().getDate());
        }
      }

      // signature validation

      signature.setValid(
        signer.verify(new JcaSimpleSignerInfoVerifierBuilder()
          .setProvider("BC").build(signature.getCertificate()))
      );
    }
    Collections.sort(signatures);
    return signatures;
  }

  public boolean checkSignaturesAndTimeStamps(boolean log) throws Exception
  {
    boolean valid = true;
    Date now = new Date();
    List<P7MSignature> signatures = getSignatures();
    for (P7MSignature s : signatures)
    {
      Date certInvalidityDate = s.getCertificate().getNotAfter();
      Date tsDate = s.getTimeStampDate();
      Date tsInvalidityDate = null;
      if (log)
      {
        System.out.println("Subject: " + s.getSubjectName());
        System.out.println("Signed: " + s.getSigningDate());
        System.out.println("TimeStamp: " + tsDate);
        System.out.println("Not after: " + certInvalidityDate);
      }
      if (s.getTimeStampDate() != null)
      {
        X509Certificate tsCert = s.getTimeStampCertificate();
        tsInvalidityDate = tsCert.getNotAfter();
        System.out.println("TS Not after: " + tsInvalidityDate);
        boolean validSig =
          certInvalidityDate.after(tsDate) && tsInvalidityDate.after(now);
        System.out.println("Signature Valid: " + validSig);
        valid = valid && validSig;

      }
      else
      {
        System.out.println("TS is missing.");
        valid = false;
      }
      System.out.println("----------------------------------");
    }
    return valid;
  }

  public static void main(String[] args) throws CMSException
  {
    try
    {
      File f = new File("C:/test2.p7m");
      P7MDocument doc = new P7MDocument(new FileInputStream(f));
      System.out.println("Valid: " + doc.checkSignaturesAndTimeStamps(true));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  static
  {
    Security.addProvider(new BouncyCastleProvider());
  }
}
