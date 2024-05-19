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
package cat.aoc.psis;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;
import oasis.names.tc.dss._1_0.core.schema.Base64Data;
import oasis.names.tc.dss._1_0.core.schema.Base64Signature;
import oasis.names.tc.dss._1_0.core.schema.DocumentHash;
import oasis.names.tc.dss._1_0.core.schema.DocumentType;
import oasis.names.tc.dss._1_0.core.schema.DocumentWithSignature;
import oasis.names.tc.dss._1_0.core.schema.IncludeObject;
import oasis.names.tc.dss._1_0.core.schema.InlineXMLType;
import oasis.names.tc.dss._1_0.core.schema.InputDocuments;
import oasis.names.tc.dss._1_0.core.schema.KeySelector;
import oasis.names.tc.dss._1_0.core.schema.OptionalInputs;
import oasis.names.tc.dss._1_0.core.schema.OptionalOutputs;
import oasis.names.tc.dss._1_0.core.schema.Result;
import oasis.names.tc.dss._1_0.core.schema.ReturnUpdatedSignature;
import oasis.names.tc.dss._1_0.core.schema.SignRequest;
import oasis.names.tc.dss._1_0.core.schema.SignResponse;
import oasis.names.tc.dss._1_0.core.schema.SignatureObjectType;
import oasis.names.tc.dss._1_0.core.schema.Timestamp;
import oasis.names.tc.dss._1_0.core.schema.VerifyRequest;
import oasis.names.tc.dss._1_0.core.schema.VerifyResponse;
import oasis.names.tc.dss._1_0.core.wsdl.DigitalSignatureService;
import oasis.names.tc.dss._1_0.core.wsdl.SOAPport;
import oasis.names.tc.dss._1_0.profiles.xss.ReturnSignedResponse;
import oasis.names.tc.dss._1_0.profiles.xss.ReturnX509CertificateInfo;
import oasis.names.tc.dss._1_0.profiles.xss.X509CertificateInfo;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.santfeliu.security.SecurityProvider;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.ws.WSTracer;
import org.w3._2000._09.xmldsig_.DigestMethodType;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.X509DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author realor
 * @author blanquepa
 */
public class PSIS implements SecurityProvider
{
  private static final Logger LOGGER = Logger.getLogger("PSIS");

  private static final int CONNECT_TIMEOUT = 30000; // 30 seconds
  private static final int READ_TIMEOUT = 300000; // 5 minutes
  private static DigitalSignatureService service;
  private static String dssServiceURL;
  private static String dssPdfServiceURL;
  private static int maxNumRetries = 2;
  private static int retryInterval = 5000; // miliseconds

  public static final String VALID_CERTIFICATE =
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:resultminor:valid:certificate:Definitive";
  public static final String CMS_TIMESTAMP =
    "urn:ietf:rfc:3161";
  public static final String XML_TIMESTAMP =
    "oasis:names:tc:dss:1.0:core:schema:XMLTimeStampToken";

  public PSIS()
  {
    initService();
  }

  @Override
  public String getName()
  {
    return "PSIS 1.0 - Agència Catalana de Certificació - www.catcert.net";
  }

  @Override
  public boolean validateCertificate(byte[] certEncoded, Map attributes)
  {
    try
    {
      SOAPport port = service.getDssPortSoap();

      Map requestContext = ((BindingProvider)port).getRequestContext();
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        dssServiceURL);
      requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
      requestContext.put("com.sun.xml.ws.request.timeout", READ_TIMEOUT);

      VerifyRequest request = new VerifyRequest();
      request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:XSS");

      // SignatureObjectType
      SignatureObjectType so = new SignatureObjectType();
      X509DataType x509 = new X509DataType();
      JAXBElement elem = new JAXBElement(
        new QName("http://www.w3.org/2000/09/xmldsig#", "X509Certificate"),
        byte[].class, certEncoded);
      x509.getX509IssuerSerialOrX509SKIOrX509SubjectName().add(elem);
      SignatureObjectType.Other other = new SignatureObjectType.Other();
      other.setX509Data(x509);

      so.setOther(other);
      request.setSignatureObject(so);

      // OptionalInputs
      OptionalInputs oi = new OptionalInputs();
      ReturnX509CertificateInfo certInfo = new ReturnX509CertificateInfo();

      AttributeType nifAttribute = new AttributeType();
      nifAttribute.setName(NIF);
      certInfo.getAttributeDesignator().add(nifAttribute);

      AttributeType cifAttribute = new AttributeType();
      cifAttribute.setName(CIF);
      certInfo.getAttributeDesignator().add(cifAttribute);

      AttributeType orgAttribute = new AttributeType();
      orgAttribute.setName(ORGANIZATION_NAME);
      certInfo.getAttributeDesignator().add(orgAttribute);

      AttributeType titleAttribute = new AttributeType();
      titleAttribute.setName(TITLE);
      certInfo.getAttributeDesignator().add(titleAttribute);

      AttributeType deptAttribute = new AttributeType();
      deptAttribute.setName(DEPARTMENT);
      certInfo.getAttributeDesignator().add(deptAttribute);

      AttributeType commonNameAttribute = new AttributeType();
      commonNameAttribute.setName(COMMON_NAME);
      certInfo.getAttributeDesignator().add(commonNameAttribute);

      AttributeType givenNameAttribute = new AttributeType();
      givenNameAttribute.setName(GIVEN_NAME);
      certInfo.getAttributeDesignator().add(givenNameAttribute);

      AttributeType surnameAttribute = new AttributeType();
      surnameAttribute.setName(SURNAME);
      certInfo.getAttributeDesignator().add(surnameAttribute);

      AttributeType altNameAttribute = new AttributeType();
      altNameAttribute.setName(ALT_NAME);
      certInfo.getAttributeDesignator().add(altNameAttribute);

      AttributeType countryAttribute = new AttributeType();
      countryAttribute.setName(COUNTRY);
      certInfo.getAttributeDesignator().add(countryAttribute);

      AttributeType emailAttribute = new AttributeType();
      emailAttribute.setName(EMAIL);
      certInfo.getAttributeDesignator().add(emailAttribute);

      oi.getServicePolicyOrClaimedIdentityOrLanguage().add(certInfo);
      request.setOptionalInputs(oi);

      // call ws
      VerifyResponse response = port.verify(request);
      attributes.put("RESULT_MINOR", response.getResult().getResultMinor());
      attributes.put("RESULT_MAJOR", response.getResult().getResultMajor());
      if (VALID_CERTIFICATE.equals(response.getResult().getResultMinor()))
      {
        List list = response.getOptionalOutputs().
          getDocumentWithSignatureOrVerifyManifestResultsOrProcessingDetails();
        for (Object o : list)
        {
          if (o instanceof X509CertificateInfo)
          {
            X509CertificateInfo ci = (X509CertificateInfo)o;
            List aList = ci.getAttribute();
            for (Object a : aList)
            {
              AttributeType at = (AttributeType)a;
              List av = at.getAttributeValue();
              if (av.size() > 0)
              {
                attributes.put(at.getName(), av.get(0));
              }
            }
          }
        }
        return true;
      }
      else
      {
        if (response.getResult() != null)
        {
          if (response.getResult().getResultMessage() != null)
          {
            attributes.put("RESULT_MESSAGE",
              response.getResult().getResultMessage().getValue());
          }
        }
        return false;
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException("security:CERTIFICATE_VALIDATOR_ERROR");
    }
  }

  @Override
  public boolean validateSignatureCMS(byte[] cms, OutputStream out)
  {
    SOAPport port = service.getDssPortSoap();

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
      dssServiceURL);
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, READ_TIMEOUT);

    VerifyRequest request = new VerifyRequest();
    request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:XSS");

    Base64Signature signatureb64 = new Base64Signature();
    signatureb64.setType("urn:ietf:rfc:3369");
    signatureb64.setValue(cms);

    SignatureObjectType signatureObject = new SignatureObjectType();
    signatureObject.setBase64Signature(signatureb64);

    request.setSignatureObject(signatureObject);

    OptionalInputs optInputs = new OptionalInputs();
    ReturnSignedResponse signResp = new ReturnSignedResponse();
    optInputs.getServicePolicyOrClaimedIdentityOrLanguage().add(signResp);

    request.setOptionalInputs(optInputs);

    VerifyResponse response = port.verify(request);
    Result result = response.getResult();

    String resultMajor = result.getResultMajor();
    String resultMinor = result.getResultMinor();

    LOGGER.log(Level.FINE, "ResultMajor: {0}", resultMajor);
    LOGGER.log(Level.FINE, "ResultMinor: {0}", resultMinor);

    return resultMinor != null && resultMinor.contains(":valid:");
  }

  @Override
  public boolean validateSignatureXML(Element signature, OutputStream out)
  {
    SOAPport port = service.getDssPortSoap();

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
      dssServiceURL);
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, READ_TIMEOUT);

    VerifyRequest request = new VerifyRequest();
    request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:XSS");

    InputDocuments inputDocuments = new InputDocuments();
    DocumentType doc = new DocumentType();
    doc.setID("doc");
    doc.setRefURI("");

    InlineXMLType inline = new InlineXMLType();
    inline.setAny(signature);
    doc.setInlineXML(inline);
    inputDocuments.getDocumentOrTransformedDataOrDocumentHash().add(doc);
    request.setInputDocuments(inputDocuments);

    VerifyResponse response = port.verify(request);

    Result result = response.getResult();

    String resultMajor = result.getResultMajor();
    String resultMinor = result.getResultMinor();

    LOGGER.log(Level.FINE, "ResultMajor: {0}", resultMajor);
    LOGGER.log(Level.FINE, "ResultMinor: {0}", resultMinor);

    if (result.getResultMessage() != null)
    {
      LOGGER.log(Level.FINE, "ResultMessage: {0}",
        result.getResultMessage().getValue());
    }
    return resultMinor != null && resultMinor.contains(":valid:");
  }

  @Override
  public boolean validateSignaturePDF(byte[] pdf, OutputStream out)
  {
    String docMimeType = "application/pdf";

    SOAPport port = service.getDssPortSoap();

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
      dssPdfServiceURL);
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, READ_TIMEOUT);

    VerifyRequest request = new VerifyRequest();
    request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:DSS_PDF");

    InputDocuments inputDocuments = new InputDocuments();
    Base64Data b64data = new Base64Data();
    b64data.setValue(pdf);
    b64data.setMimeType(docMimeType);
    DocumentType document = new DocumentType();
    document.setBase64Data(b64data);
    inputDocuments.getDocumentOrTransformedDataOrDocumentHash().add(document);
    request.setInputDocuments(inputDocuments);

    VerifyResponse response = port.verify(request);
    Result result = response.getResult();

    if (result != null)
    {
      String resultMajor = result.getResultMajor();
      String resultMinor = result.getResultMinor();

      LOGGER.log(Level.FINE, "ResultMajor: {0}", resultMajor);
      LOGGER.log(Level.FINE, "ResultMinor: {0}", resultMinor);

      if (out != null)
        JAXB.marshal(response.getResult(), out);

      return resultMinor != null && resultMinor.contains(":valid:");
    }

    return false;
  }

  @Override
  public boolean preserveSignatureXML(Element signature, OutputStream out)
  {
    SOAPport port = service.getDssPortSoap();

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
      dssServiceURL);
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, READ_TIMEOUT);

    VerifyRequest request = new VerifyRequest();
    request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:XSS");

    InputDocuments inputDocuments = new InputDocuments();
    DocumentType doc = new DocumentType();
    doc.setID("doc");
    doc.setRefURI("");

    InlineXMLType inline = new InlineXMLType();
    inline.setAny(signature);
    doc.setInlineXML(inline);
    inputDocuments.getDocumentOrTransformedDataOrDocumentHash().add(doc);
    request.setInputDocuments(inputDocuments);

    OptionalInputs optInputs = new OptionalInputs();
    ReturnUpdatedSignature rt = new ReturnUpdatedSignature();
    rt.setType("urn:oasis:names:tc:dss:1.0:profiles:XAdES:forms:ES-LTV");
    optInputs.getServicePolicyOrClaimedIdentityOrLanguage().add(rt);
    request.setOptionalInputs(optInputs);

    // Execució del servei
    VerifyResponse response = port.verify(request);
    JAXB.marshal(response.getResult(), System.out);
    Result result = response.getResult();

    if (result != null)
    {
      String resultMajor = result.getResultMajor();
      String resultMinor = result.getResultMinor();

      LOGGER.log(Level.FINE, "ResultMajor: {0}", resultMajor);
      LOGGER.log(Level.FINE, "ResultMinor: {0}", resultMinor);

      boolean valid = resultMinor != null && resultMinor.contains(":valid:");

      //if (valid)
      {
        OptionalOutputs optOutputs = response.getOptionalOutputs();
        List docWithSignature = optOutputs
          .getDocumentWithSignatureOrVerifyManifestResultsOrProcessingDetails();
        if (docWithSignature != null && !docWithSignature.isEmpty())
        {
          DocumentWithSignature docSig =
            (DocumentWithSignature) docWithSignature.get(0);
          Base64Data sig = docSig.getDocument().getBase64Data();

          try
          {
            out.write(sig.getValue());
            out.flush();
            out.close();
          }
          catch (IOException ex)
          {
            Logger.getLogger(PSIS.class.getName()).log(Level.SEVERE, null, ex);
            return false;
          }
        }
      }
      return valid;
    }
    return false;
  }

  @Override
  public boolean preserveSignaturePDF(byte[] pdf, OutputStream out)
  {
    String docMimeType = "application/pdf";

    SOAPport port = service.getDssPortSoap();

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
      dssPdfServiceURL);
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, READ_TIMEOUT);

    VerifyRequest request = new VerifyRequest();
    request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:DSS_PDF");

    InputDocuments inputDocuments = new InputDocuments();
    Base64Data b64data = new Base64Data();
    b64data.setValue(pdf);
    b64data.setMimeType(docMimeType);
    DocumentType document = new DocumentType();
    document.setBase64Data(b64data);
    inputDocuments.getDocumentOrTransformedDataOrDocumentHash().add(document);
    request.setInputDocuments(inputDocuments);

    OptionalInputs optInputs = new OptionalInputs();
    ReturnUpdatedSignature rt = new ReturnUpdatedSignature();
    rt.setType("urn:oasis:names:tc:dss:1.0:profiles:XAdES:forms:ES-LTV");
    optInputs.getServicePolicyOrClaimedIdentityOrLanguage().add(rt);

    request.setOptionalInputs(optInputs);

    // Service execution
    VerifyResponse response = port.verify(request);
    Result result = response.getResult();

    if (result != null)
    {
      String resultMajor = result.getResultMajor();
      String resultMinor = result.getResultMinor();

      LOGGER.log(Level.FINE, "ResultMajor: {0}", resultMajor);
      LOGGER.log(Level.FINE, "ResultMinor: {0}", resultMinor);

      boolean valid = resultMinor != null && resultMinor.contains(":valid:");

      if (valid)
      {
        OptionalOutputs optOutputs = response.getOptionalOutputs();
        List docWithSignature = optOutputs
          .getDocumentWithSignatureOrVerifyManifestResultsOrProcessingDetails();
        if (docWithSignature != null && !docWithSignature.isEmpty())
        {
          DocumentWithSignature doc =
            (DocumentWithSignature) docWithSignature.get(0);
          Base64Data sig = doc.getDocument().getBase64Data();

          try
          {
            out.write(sig.getValue());
            out.flush();
            out.close();
          }
          catch (IOException ex)
          {
            Logger.getLogger(PSIS.class.getName()).log(Level.SEVERE, null, ex);
            return false;
          }
        }
      }
      return valid;
    }
    return false;
  }

  @Override
  public byte[] createCMSTimeStamp(byte[] digest,
    String digestMethod, byte[] certEncoded)
  {
    boolean retry = true;
    int retries = 0;

    SOAPport port = service.getDssPortSoap();
    WSTracer tracer = WSTracer.bind((BindingProvider)port);

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
      dssServiceURL);
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, READ_TIMEOUT);

    LOGGER.log(Level.INFO, "PSIS URL: {0}", dssServiceURL);

    while (true)
    {
      try
      {
        // call to PSIS service
        SignRequest request = new SignRequest();
        request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:timestamping");
        InputDocuments io = new InputDocuments();
        DocumentHash hash = new DocumentHash();
        hash.setID("Doc1");
        hash.setDigestValue(digest);
        DigestMethodType dm = new DigestMethodType();
        dm.setAlgorithm(digestMethod);
        hash.setDigestMethod(dm);
        io.getDocumentOrTransformedDataOrDocumentHash().add(hash);

        OptionalInputs oi = new OptionalInputs();

        KeySelector ks = new KeySelector();
        oi.getServicePolicyOrClaimedIdentityOrLanguage().add(ks);

        KeyInfoType ki = new KeyInfoType();
        ks.setKeyInfo(ki);

        if (certEncoded == null)
        {
          try
          {
            String certBase64 = readFile("cat/aoc/psis/tst.cer");
            certEncoded = Base64.getMimeDecoder().decode(certBase64);
          }
          catch (Exception ex)
          {
            retry = false;
            throw ex;
          }
        }
        JAXBElement x509Certificate =
          new JAXBElement(new QName("http://www.w3.org/2000/09/xmldsig#",
          "X509Certificate"), byte[].class, certEncoded);

        X509DataType x509Data = new X509DataType();
        x509Data.getX509IssuerSerialOrX509SKIOrX509SubjectName()
          .add(x509Certificate);

        JAXBElement x509Data2 =
          new JAXBElement(new QName("http://www.w3.org/2000/09/xmldsig#",
          "X509Data"), X509DataType.class, x509Data);

        ki.getContent().add(x509Data2);

        JAXBElement signType =
          new JAXBElement(new QName("urn:oasis:names:tc:dss:1.0:core:schema",
          "SignatureType"), String.class, CMS_TIMESTAMP);
        oi.getServicePolicyOrClaimedIdentityOrLanguage().add(signType);
        request.setOptionalInputs(oi);

        IncludeObject includeObject = new IncludeObject();
        includeObject.setObjId("Doc1");
        includeObject.setWhichDocument(hash);
        includeObject.setHasObjectTagsAndAttributesSet(false);
        includeObject.setCreateReference(true);
        oi.getServicePolicyOrClaimedIdentityOrLanguage().add(includeObject);
        request.setInputDocuments(io);

        SignResponse response = port.sign(request);
        Result result = response.getResult();

        String resultMajor = result.getResultMajor();
        String resultMinor = result.getResultMinor();

        LOGGER.log(Level.FINE, "ResultMajor: {0}", resultMajor);
        LOGGER.log(Level.FINE, "ResultMinor: {0}", resultMinor);

        if (response.getResult().getResultMessage() != null)
        {
          LOGGER.log(Level.INFO, "ResultMessage: {0}",
            result.getResultMessage().getValue());
        }
        if (resultMajor != null && resultMajor.contains(":Success"))
        {
          Timestamp timestamp = response.getSignatureObject().getTimestamp();
          return timestamp.getRFC3161TimeStampToken();
        }
        retry = false;
        throw new Exception(resultMajor);
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.SEVERE, "TIMESTAMP_GENERATOR_ERROR", ex);

        logMessages(tracer);

        if (!retry || retries >= maxNumRetries)
          throw new RuntimeException("TIMESTAMP_GENERATOR_ERROR", ex);

        retries++;

        try
        {
          // wait and retry
          Thread.sleep(retryInterval);
        }
        catch (InterruptedException iex)
        {
          throw new RuntimeException("TIMESTAMP_GENERATOR_ERROR");
        }
      }
    }
  }

  @Override
  public Element createXMLTimeStamp(byte[] digest,
    String digestMethod, byte[] certEncoded)
  {
    boolean retry = true;
    int retries = 0;

    SOAPport port = service.getDssPortSoap();
    WSTracer tracer = WSTracer.bind((BindingProvider)port);

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
      dssServiceURL);
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, READ_TIMEOUT);

    LOGGER.log(Level.INFO, "PSIS URL: {0}", dssServiceURL);

    while (true)
    {
      try
      {
        // call to PSIS service
        SignRequest request = new SignRequest();
        request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:timestamping");

        InputDocuments io = new InputDocuments();
        DocumentHash hash = new DocumentHash();
        hash.setID("Doc");
        hash.setDigestValue(digest);
        DigestMethodType dm = new DigestMethodType();
        dm.setAlgorithm(digestMethod);
        hash.setDigestMethod(dm);
        io.getDocumentOrTransformedDataOrDocumentHash().add(hash);

        OptionalInputs oi = new OptionalInputs();

        KeySelector ks = new KeySelector();
        oi.getServicePolicyOrClaimedIdentityOrLanguage().add(ks);

        KeyInfoType ki = new KeyInfoType();
        ks.setKeyInfo(ki);

        if (certEncoded == null)
        {
          try
          {
            String certBase64 = readFile(
              "org/santfeliu/security/provider/psis/tst.cer");
            certEncoded = Base64.getMimeDecoder().decode(certBase64);
          }
          catch (Exception ex)
          {
            retry = false;
            throw ex;
          }
        }
        JAXBElement x509Certificate =
          new JAXBElement(new QName("http://www.w3.org/2000/09/xmldsig#",
          "X509Certificate"), byte[].class, certEncoded);

        X509DataType x509Data = new X509DataType();
        x509Data.getX509IssuerSerialOrX509SKIOrX509SubjectName()
          .add(x509Certificate);

        JAXBElement x509Data2 =
          new JAXBElement(new QName("http://www.w3.org/2000/09/xmldsig#",
          "X509Data"), X509DataType.class, x509Data);

        ki.getContent().add(x509Data2);

        JAXBElement signType =
          new JAXBElement(new QName("urn:oasis:names:tc:dss:1.0:core:schema",
          "SignatureType"), String.class, XML_TIMESTAMP);
        oi.getServicePolicyOrClaimedIdentityOrLanguage().add(signType);
        request.setOptionalInputs(oi);

        request.setInputDocuments(io);

        SignResponse response = port.sign(request);
        Result result = response.getResult();

        String resultMajor = result.getResultMajor();
        String resultMinor = result.getResultMinor();

        LOGGER.log(Level.FINE, "ResultMajor: {0}", resultMajor);
        LOGGER.log(Level.FINE, "ResultMinor: {0}", resultMinor);

        if (response.getResult().getResultMessage() != null)
        {
          LOGGER.log(Level.INFO, "ResultMessage: {0}",
            result.getResultMessage().getValue());
        }
        if (resultMajor != null && resultMajor.contains(":Success"))
        {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          dbf.setNamespaceAware(true);
          javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
          byte[] message = tracer.getInboundMessage();
          Document doc = db.parse(new ByteArrayInputStream(message));
          NodeList nodeList = doc.getElementsByTagNameNS(
            "http://www.w3.org/2000/09/xmldsig#", "Signature");
          if (nodeList.getLength() == 1)
          {
            return (Element)nodeList.item(0);
          }
        }
        retry = false;
        throw new Exception(resultMajor);
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.SEVERE, "TIMESTAMP_GENERATOR_ERROR", ex);

        logMessages(tracer);

        if (!retry || retries >= maxNumRetries)
          throw new RuntimeException("TIMESTAMP_GENERATOR_ERROR", ex);

        retries++;

        try
        {
          // wait and retry
          Thread.sleep(retryInterval);
        }
        catch (InterruptedException iex)
        {
          throw new RuntimeException("TIMESTAMP_GENERATOR_ERROR");
        }
      }
    }
  }

  private void logMessages(WSTracer tracer)
  {
    try
    {
      byte[] outBytes = tracer.getOutboundMessage();
      if (outBytes != null)
      {
        String out = new String(outBytes, "UTF-8");
        LOGGER.log(Level.INFO, "outbound message: {0}", out);
      }

      byte[] inBytes = tracer.getInboundMessage();
      if (inBytes != null)
      {
        String in = new String(inBytes, "UTF-8");
        LOGGER.log(Level.INFO, "inbound message: {0}", in);
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  private String readFile(final String filename) throws Exception
  {
    StringBuilder sb = new StringBuilder();
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    InputStream is = cl.getResourceAsStream(filename);
    try
    {
      byte[] buffer = new byte[4096];
      int numRead = is.read(buffer);
      while (numRead != -1)
      {
        sb.append(new String(buffer, 0, numRead));
        numRead = is.read(buffer);
      }
    }
    finally
    {
      is.close();
    }
    return sb.toString();
  }

  private void initService()
  {
    try
    {
      LOGGER.info("Initializing PSIS client...");
      Class cls = getClass();
      String className = cls.getName();
      int index = className.lastIndexOf(".");
      String path = "/" + className.substring(0, index).replace('.', '/');
      URL wsdlLocation = cls.getResource(path + "/PSIS.wsdl");

      LOGGER.log(Level.INFO, "PSIS WSDL: {0}", wsdlLocation);

      dssServiceURL = MatrixConfig.getProperty("cat.aoc.psis.serviceURL");
      LOGGER.log(Level.INFO, "PSIS URL: {0}", dssServiceURL);

      dssPdfServiceURL = MatrixConfig.getProperty(
        "cat.aoc.psis.pdf.serviceURL");
      LOGGER.log(Level.INFO, "PSIS PDF URL: {0}", dssPdfServiceURL);

      String value = MatrixConfig.getProperty("cat.aoc.psis.maxNumRetries");
      if (value != null)
      {
        maxNumRetries = Integer.parseInt(value);
      }
      LOGGER.log(Level.INFO, "PSIS maxNumRetries: {0}", maxNumRetries);

      value = MatrixConfig.getProperty("cat.aoc.psis.retryInterval");
      if (value != null)
      {
        retryInterval = Integer.parseInt(value);
      }
      LOGGER.log(Level.INFO, "PSIS retryInterval: {0}", retryInterval);

      QName qname = new QName("urn:oasis:names:tc:dss:1.0:core:wsdl",
        "digitalSignatureService");

      service = new DigitalSignatureService(wsdlLocation, qname);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /* test methods */
  private void testValidateCertificate() throws Exception
  {
    String filename = "c:/test.cer";

    X509Certificate certificate =
      (X509Certificate) CertificateFactory.getInstance("X.509").
      generateCertificate(new FileInputStream(filename));

    Map attributes = new HashMap();
    validateCertificate(certificate.getEncoded(), attributes);
    for (Object e : attributes.entrySet())
    {
      Map.Entry entry = (Map.Entry)e;
      String key = (String)entry.getKey();
      String value = (String)entry.getValue();
      System.out.println(key + "=" + value);
    }
  }

  private void testValidateSignatureXML() throws Exception
  {
    String filename = "c:/signature1.xml";
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
    Document document = db.parse(new FileInputStream(filename));

    validateSignatureXML(document.getDocumentElement(), null);
  }

  private void testCreateCMSTimeStamp() throws Exception
  {
    MatrixConfig.setProperty("cat.aoc.psis.serviceURL",
      "http://psisbeta.catcert.net/psis/catcert-test/dss");
    PSIS psis = new PSIS();
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.reset();
    md.update("HOLA".getBytes());
    byte[] digest = md.digest();

    byte[] ts = psis.createCMSTimeStamp(digest,
      "http://www.w3.org/2001/04/xmlenc#sha256", null);
    System.out.println(Base64.getEncoder().encodeToString(ts));
  }

  public static void main(String[] args)
  {
    try
    {
      PSIS psis = new PSIS();
      psis.testCreateCMSTimeStamp();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
