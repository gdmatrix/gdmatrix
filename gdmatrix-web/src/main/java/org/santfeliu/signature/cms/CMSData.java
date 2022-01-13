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
package org.santfeliu.signature.cms;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.Security;
import java.security.cert.X509Certificate;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
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
 * @author unknown
 */
@Deprecated
public class CMSData
{
  CMSSignedData cms;

  public CMSData(CMSSignedData cms)
  {
    this.cms = cms;
  }

  public CMSData(InputStream isCMS) throws CMSException
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
    cms = CMSUtils.addTimeStamp(serviceURI, cms);
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

  public List<CMSSignature> getSignatures() throws Exception
  {
    ArrayList<CMSSignature> signatures = new ArrayList();
    Store certStore = cms.getCertificates();
    SignerInformationStore siStore = cms.getSignerInfos();
    Collection signers = siStore.getSigners();
    for (Object elem : signers)
    {
      SignerInformation signer = (SignerInformation)elem;
      CMSSignature signature = new CMSSignature();
      signatures.add(signature);

      org.bouncycastle.cms.SignerId sid = signer.getSID();
      Collection certCollection = certStore.getMatches(sid);
      X509CertificateHolder certificateHolder =
        (X509CertificateHolder)certCollection.iterator().next();
      X509Certificate certificate =
        new JcaX509CertificateConverter().setProvider( "BC" )
        .getCertificate(certificateHolder);
      signature.setCertificate(certificate);

      String signerName = certificate.getSubjectDN().getName();
      signature.loadProperties(signerName);

      signature.setSignature(
        Base64.getEncoder().encodeToString(signer.getSignature()).toUpperCase());

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
          DERSequence seq = (DERSequence)attrib.getAttrValues().getObjectAt(0);
          ContentInfo timeStampToken = ContentInfo.getInstance(seq);
          SignedData sd = SignedData.getInstance(timeStampToken.getContent());
          ASN1Encodable content = sd.getEncapContentInfo().getContent();
//          TSTInfo tstInfo = new TSTInfo((ASN1Sequence)
//            new ASN1InputStream(((DEROctetString)content).getOctets()).readObject());
          TSTInfo tstInfo =
            TSTInfo.getInstance(((ASN1OctetString)content).getOctets());
          signature.setTimeStampDate(tstInfo.getGenTime().getDate());
        }
      }

      // signature validation
//      signature.setValid(signer.verify(signature.getCertificate(), "BC"));
      signature.setValid(
        signer.verify(new JcaSimpleSignerInfoVerifierBuilder()
          .setProvider("BC").build(signature.getCertificate())));
    }
    Collections.sort(signatures);
    return signatures;
  }

  public static void main(String [] args)
    throws CMSException
  {
    File f = new File("C:/Temp/content2.p7s");

    InputStream in;
    try
    {
      in = new BufferedInputStream(new FileInputStream(f));
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      return;
    }

    CMSData parser = new CMSData(in);
    try
    {
      List<CMSSignature> signatures = parser.getSignatures();
      for( CMSSignature s : signatures )
      {
        System.out.println( s.getSigningTime() + " " + s.getCertificateName()
          + " " + s.getCertificateProperties());
      }
    }
    catch(Exception ex)
    {
      System.err.println("ERROR 1:" + ex.getMessage());
      ex.printStackTrace();
    }

    try
    {
      byte [] signedContent = parser.getSignedContent();
      FileOutputStream out = new FileOutputStream("c:/Temp/content.doc");
      out.write(signedContent);
      out.flush();
      out.close();
    }
    catch(Exception ex)
    {
      System.err.println("ERROR 2:" + ex.getMessage());
      ex.printStackTrace();
    }
  }

  static
  {
    Security.addProvider(new BouncyCastleProvider());
  }
}
