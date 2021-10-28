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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.tsp.TSTInfo;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;

/**
 *
 * @author realor
 */
@Deprecated
public class CMSUtils
{
  public static void main(String args[])
  {
    try
    {
      CMSUtils cms = new CMSUtils();

      if (true)
      {
        cms.addTimeStamp("http://psis.catcert.net/psis/catcert/tsp",
          new File("c:/test.p7m"),
          new File("c:/test2.p7m"));
      }

//      if (true)
//      {
//        cms.dumpFile(
//          new File("c:/demo_out.p7s"),
//          new File("c:/demo_out.txt"));
//      }
//
//      if (false)
//      {
//        ContentInfo contentInfo = cms.createTimeStamp(
//          "http://psis.catcert.net/psis/catcert/tsp", "prova".getBytes());
//
//        System.out.println("TimeStampResponse: " +
//          contentInfo.getContentType().getId());
//
//        System.out.println("recovering TST info...");
//        TSTInfo tstInfo = CMSUtils.recoverTSTInfo(contentInfo);
//
//        System.out.println("TST info recovered: ");
//        System.out.println("TST Nonce: " + tstInfo.getNonce().getValue());
//        System.out.println("TST Serial Number: " +
//          Integer.toHexString(tstInfo.getSerialNumber().getValue().intValue()));
//        System.out.println("TST dateTime: " +
//          tstInfo.getGenTime().getTime());
//      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void dumpFile(File inFile, File outFile) throws Exception
  {
    ASN1InputStream is = new ASN1InputStream(new FileInputStream(inFile));
    String dump = ASN1Dump.dumpAsString(is.readObject(), true);
    PrintWriter writer = new PrintWriter(outFile);
    try
    {
      writer.print(dump);
    }
    finally
    {
      writer.close();
    }
  }

  public static CMSSignedData addTimeStamp(String serviceURI,
    CMSSignedData signedData) throws Exception
  {
    CMSSignedData newSignedData;
    
    SignerInformationStore sigStore = signedData.getSignerInfos();
    ArrayList siList = new ArrayList();
    for (Object o : sigStore.getSigners())
    {
      Store certStore =
        signedData.getCertificates();
      SignerInformation si = (SignerInformation)o;
      SignerId sigId = si.getSID();
      Collection certCollection = certStore.getMatches(sigId);
      X509CertificateHolder certificateHolder =
        (X509CertificateHolder)certCollection.iterator().next();
      X509Certificate certificate = 
        new JcaX509CertificateConverter().setProvider( "BC" )
        .getCertificate(certificateHolder);         
      System.out.println(certificate.getSubjectDN().getName());

      // get signature
      byte[] signature = si.getSignature();

      // signed attributes
      System.out.println("SignedAttributes:");
      AttributeTable signedAttributes = si.getSignedAttributes();
      printAttributeTable(signedAttributes);

      // unsigned attributes
      System.out.println("UnsignedAttributes:");
      AttributeTable unsignedAttributes = si.getUnsignedAttributes();
      printAttributeTable(unsignedAttributes);

      ASN1ObjectIdentifier tsId =
        new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.14");
      Attribute att = unsignedAttributes == null ?
        null : unsignedAttributes.get(tsId);
      if (att == null)
      {
        System.out.println("creating timeStamp...");
        ContentInfo timeStampToken = createTimeStamp(serviceURI, signature);
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(timeStampToken);
        DERSet attributeValues = new DERSet(vector);
        Attribute attribute = new Attribute(tsId, attributeValues);
        Hashtable attrMap = new Hashtable();
        attrMap.put(tsId, attribute);
        AttributeTable table = new AttributeTable(attrMap);
        SignerInformation newSi =
          SignerInformation.replaceUnsignedAttributes(si, table);
        siList.add(newSi);
      }
      else System.out.println("Timestamp present");
    }

    if (!siList.isEmpty()) // replace signers
    {
      newSignedData = CMSSignedData.replaceSigners(signedData,
        new SignerInformationStore(siList));
      newSignedData = new CMSSignedData(newSignedData.getEncoded());
    }
    else newSignedData = signedData;

    return newSignedData;
  }

  public static void addTimeStamp(String serviceURI, File inFile, File outFile)
    throws Exception
  {
    CMSSignedData signedData = new CMSSignedData(new FileInputStream(inFile));
    signedData = addTimeStamp(serviceURI, signedData);

    if (outFile != null)
    {
      FileOutputStream out = new FileOutputStream(outFile);
      out.write(signedData.getEncoded());
      out.flush();
      out.close();
    }
  }

  public static ContentInfo createTimeStamp(String serviceURI, byte[] message)
    throws Exception
  {
    String nonce = String.valueOf((int)(Math.random() * 1000000));
    // es crea la peticio a la TSA
    TimeStampReq timeStampRequest = createTimeStampRequest(
      message, // message
      nonce, // nonce
      true, // requireCert
      null, // extensions
      "1.3.14.3.2.26", // digestAlgorithm identifier
      "0.4.0.2023.1.1"); // timestampPolicy

    // s'envia la peticio creada
    TimeStampResp timeStampResponse =
      sendTimestampRequest(timeStampRequest, serviceURI);

    ContentInfo contentInfo = timeStampResponse.getTimeStampToken();
    return contentInfo;
  }

  public static TimeStampReq createTimeStampRequest(
    byte[] message, String nonce, boolean requireCert,
    Extensions extensions, String digestAlgorithm, String timestampPolicy)
    throws NoSuchAlgorithmException
  {
    MessageDigest md = MessageDigest.getInstance("SHA1");
    byte[] hashedMsg = md.digest(message);
    ASN1ObjectIdentifier identifier = new ASN1ObjectIdentifier(digestAlgorithm);    
    org.bouncycastle.asn1.tsp.MessageImprint imprint =
            new org.bouncycastle.asn1.tsp.MessageImprint(
            new AlgorithmIdentifier(identifier), hashedMsg);

    TimeStampReq request = new TimeStampReq(
      imprint,
      timestampPolicy != null ? new ASN1ObjectIdentifier(timestampPolicy) : null,
      nonce != null ? new ASN1Integer(nonce.getBytes()) : null,
      ASN1Boolean.getInstance(requireCert),
      extensions);
    return request;
  }

  public static void printAttributeTable(AttributeTable table) throws Exception
  {
    if (table == null) return;
    Hashtable map = table.toHashtable();
    for (Object e : map.keySet())
    {
      String key = e.toString();
      Attribute at = (Attribute)map.get(e);
      System.out.print(key + "=");
      printAttribute(at);
    }
  }

  public static void printAttribute(Attribute attribute) throws Exception
  {
    ASN1Set set = attribute.getAttrValues();
    ASN1Primitive der = set.getObjectAt(0).toASN1Primitive();
    System.out.println(der.getClass());
    if (der instanceof DEROctetString)
    {
      DEROctetString octet = (DEROctetString)der;
      byte[] data = octet.getOctets();
      System.out.println(new String(data, "UTF-16LE"));
    }
    else if (der instanceof ASN1UTCTime)
    {
      ASN1UTCTime utcTime = (ASN1UTCTime)der;
      String time = utcTime.getAdjustedTime();
      System.out.println(time);
    }
    else if (der instanceof ASN1ObjectIdentifier)
    {
      ASN1ObjectIdentifier id = (ASN1ObjectIdentifier)der;
      System.out.println(id.getId());
    }
  }

  public static TSTInfo recoverTSTInfo(ContentInfo contentInfo)
    throws IOException
  {
    SignedData sd = SignedData.getInstance(contentInfo.getContent());
    ASN1Encodable content = sd.getEncapContentInfo().getContent();
//    TSTInfo tstInfo = new TSTInfo((ASN1Sequence)
//     new ASN1InputStream(((DEROctetString)content).getOctets()).readObject());
    TSTInfo tstInfo = TSTInfo.getInstance(((ASN1OctetString)content).getOctets());    
    return tstInfo;
  }

  private static TimeStampResp sendTimestampRequest(
    TimeStampReq req, String serviceURI) throws Exception
  {
    return sendData(new ByteArrayInputStream(req.getEncoded()), serviceURI);
  }

  private static TimeStampResp sendData(InputStream dataToBeSent,
    String serviceURI) throws Exception
  {
    URL url = new URL(serviceURI);
    URLConnection conn = url.openConnection();
    conn.setDoInput(true);
    conn.setDoOutput(true);

    // post request data
    OutputStream os = conn.getOutputStream();
    byte buffer[] = new byte[4096];
    int numRead = dataToBeSent.read(buffer);
    while (numRead > 0)
    {
      os.write(buffer, 0, numRead);
      numRead = dataToBeSent.read(buffer);
    }
    os.flush();

    // read response
    InputStream response = conn.getInputStream();
    ASN1InputStream asn1Is = new ASN1InputStream(response);
//    TimeStampResp tspResp = new TimeStampResp((ASN1Sequence)asn1Is.readObject());
    Enumeration e = ((ASN1Sequence)asn1Is.readObject()).getObjects();
    PKIStatusInfo pkiStatusInfo = PKIStatusInfo.getInstance(e.nextElement());
    ContentInfo timeStampToken = null;
    if (e.hasMoreElements())
    {
      timeStampToken = ContentInfo.getInstance(e.nextElement());
    }
    TimeStampResp tspResp = new TimeStampResp(pkiStatusInfo, timeStampToken); 
    return tspResp;
  }

  static
  {
    Security.addProvider(new BouncyCastleProvider());
  }
}
