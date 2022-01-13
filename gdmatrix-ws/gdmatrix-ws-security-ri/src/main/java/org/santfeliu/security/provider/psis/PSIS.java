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
package org.santfeliu.security.provider.psis;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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
import oasis.names.tc.dss._1_0.core.schema.SignaturePtr;
import oasis.names.tc.dss._1_0.core.schema.VerifyRequest;
import oasis.names.tc.dss._1_0.core.schema.VerifyResponse;
import oasis.names.tc.dss._1_0.core.wsdl.DigitalSignatureService;
import oasis.names.tc.dss._1_0.core.wsdl.SOAPport;
import oasis.names.tc.dss._1_0.profiles.xss.ReturnX509CertificateInfo;
import oasis.names.tc.dss._1_0.profiles.xss.X509CertificateInfo;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.commons.io.IOUtils;
import org.apache.xml.security.Init;
import org.apache.xml.security.utils.XMLUtils;
import org.santfeliu.security.SecurityProvider;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.net.HttpClient;
import org.santfeliu.util.template.Template;
import org.w3._2000._09.xmldsig_.DigestMethodType;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.X509DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author realor
 * @author blanquepa
 */
public class PSIS implements SecurityProvider
{
  private static final int CONNECT_TIMEOUT = 30000; // 30 seconds
  private static final int READ_TIMEOUT = 300000; // 5 minutes
  private static DigitalSignatureService service;
  private static String dssServiceURL;
  private static String dssPdfServiceURL;

  public static final String VALID_CERTIFICATE =
    "urn:oasis:names:tc:dss:1.0:profiles:XSS:resultminor:valid:certificate:Definitive";
  public static final String CMS_TIMESTAMP =
    "urn:ietf:rfc:3161";
  public static final String XML_TIMESTAMP =
    "urn:oasis:names:tc:dss:1.0:profiles:timestamping";

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
    try
    {
      String template = readFile(
        "org/santfeliu/security/provider/psis/XMLValidateCMS.xml");

      Map variables = new HashMap();
      variables.put("SIGNATURE", Base64.getEncoder().encodeToString(cms));
      String message = Template.create(template).merge(variables);

      HttpClient client = new HttpClient();
      client.setURL(dssServiceURL);
      client.setConnectTimeout(CONNECT_TIMEOUT);
      client.setReadTimeout(READ_TIMEOUT);
      client.doPost(message.getBytes());

      Document document = client.getContentAsXML();
      Node response = document.getFirstChild().getFirstChild().getFirstChild();
      Node resultNode = response.getFirstChild();
      Node resultMajorNode = resultNode.getFirstChild();
      Node resultMinorNode = resultMajorNode.getNextSibling();

      String resultMajor = resultMajorNode.getTextContent();
      String resultMinor = resultMinorNode.getTextContent();

      System.out.println("ResultMajor:" + resultMajor);
      System.out.println("ResultMinor:" + resultMinor);
      if (out != null)
      {
        if (!Init.isInitialized()) Init.init();
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
        XMLUtils.outputDOM(response, out);
      }
      return resultMinor != null && resultMinor.contains(":valid:");
    }
    catch (Exception ex)
    {
      throw new RuntimeException("security:SIGNATURE_VALIDATION_ERROR");
    }
  }

  @Override
  public boolean validateSignatureXML(Document signature, OutputStream out)
  {
    // TODO: not finished
    SOAPport port = service.getDssPortSoap();

    Map requestContext = ((BindingProvider)port).getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
      dssServiceURL);
    requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
    requestContext.put("com.sun.xml.ws.request.timeout", READ_TIMEOUT);

    VerifyRequest request = new VerifyRequest();
    request.setProfile("urn:oasis:names:tc:dss:1.0:profiles:XSS");

    // InputDocuments
    InputDocuments inputDocuments = new InputDocuments();
    DocumentType doc = new DocumentType();
    doc.setID("doc");
    doc.setRefURI("");
    InlineXMLType inline = new InlineXMLType();
    inline.setAny(signature.getDocumentElement());
    doc.setInlineXML(inline);
    inputDocuments.getDocumentOrTransformedDataOrDocumentHash().add(doc);
    request.setInputDocuments(inputDocuments);

    // OptionalInputs
    /*
    OptionalInputs oi = new OptionalInputs();
    JAXBElement elem = new JAXBElement(
      new QName("urn:oasis:names:tc:dss:1.0:core:schema",
      "ReturnProcessingDetails"), Object.class, null);
    oi.getServicePolicyOrClaimedIdentityOrLanguage().add(elem);
    request.setOptionalInputs(oi);
    */
    SignatureObjectType so = new SignatureObjectType();
    SignaturePtr sp = new SignaturePtr();
    sp.setWhichDocument(doc);
    sp.setXPath("Signature");
    so.setSignaturePtr(sp);
    request.setSignatureObject(so);

    VerifyResponse response = port.verify(request);
    System.out.println(response.getResult().getResultMinor());
    System.out.println(response.getResult().getResultMajor());
    if (response.getResult().getResultMessage() != null)
    {
      System.out.println(response.getResult().getResultMessage().getValue());
    }
    return false;
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
    requestContext.put("com.sun.xml.ws.request.timeout", READ_TIMEOUT);

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

      System.out.println("ResultMajor:" + resultMajor);
      System.out.println("ResultMinor:" + resultMinor);

      if (out != null)
        JAXB.marshal(response.getResult(), out);

      return resultMinor != null && resultMinor.contains(":valid:");
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
    requestContext.put("com.sun.xml.ws.request.timeout", READ_TIMEOUT);

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

    // Execució del servei
    VerifyResponse response = port.verify(request);
    Result result = response.getResult();

    if (result != null)
    {
      String resultMajor = result.getResultMajor();
      String resultMinor = result.getResultMinor();

      System.out.println("ResultMajor:" + resultMajor);
      System.out.println("ResultMinor:" + resultMinor);

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
    try
    {
      SOAPport port = service.getDssPortSoap();

      Map requestContext = ((BindingProvider)port).getRequestContext();
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        dssServiceURL);
      requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
      requestContext.put("com.sun.xml.ws.request.timeout", READ_TIMEOUT);

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
          String certBase64 = readFile(
            "org/santfeliu/security/provider/psis/tst.cer");
          certEncoded = Base64.getDecoder().decode(certBase64);
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
      }
      JAXBElement x509Certificate =
        new JAXBElement(new QName("http://www.w3.org/2000/09/xmldsig#",
        "X509Certificate"), byte[].class, certEncoded);

      X509DataType x509Data = new X509DataType();
      JAXBElement x509Data2 =
        new JAXBElement(new QName("http://www.w3.org/2000/09/xmldsig#",
        "X509Data"), X509DataType.class, x509Data);

      x509Data.getX509IssuerSerialOrX509SKIOrX509SubjectName()
        .add(x509Certificate);

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
      System.out.println(response.getResult().getResultMinor());
      System.out.println(response.getResult().getResultMajor());
      if (response.getResult().getResultMessage() != null)
      {
        System.out.println(response.getResult().getResultMessage().getValue());
      }
      return response.getSignatureObject().
        getTimestamp().getRFC3161TimeStampToken();
    }
    catch (Exception ex)
    {
      throw new RuntimeException("TIMESTAMP_GENERATOR_ERROR");
    }
  }

  @Override
  public Element createXMLTimeStamp(byte[] digest,
    String digestMethod, byte[] certEncoded)
  {
    try
    {
      String template = readFile(
        "org/santfeliu/security/provider/psis/XMLTSCreation.xml");

      if (certEncoded == null)
      {
        String certBase64 =
          readFile("org/santfeliu/security/provider/psis/tst.cer");
        certEncoded = Base64.getDecoder().decode(certBase64);
      }
      Map variables = new HashMap();
      variables.put("DIGEST_METHOD", digestMethod);
      variables.put("DIGEST_VALUE", Base64.getEncoder().encodeToString(digest));
      variables.put("CERTIFICATE", Base64.getEncoder().encodeToString(certEncoded));
      String message = Template.create(template).merge(variables);

      HttpClient client = new HttpClient();
      client.setURL(dssServiceURL);
      client.setConnectTimeout(CONNECT_TIMEOUT);
      client.setReadTimeout(READ_TIMEOUT);
      client.doPost(message.getBytes());

      Document doc = client.getContentAsXML();
      NodeList nodeList = doc.getElementsByTagNameNS(
        "http://www.w3.org/2000/09/xmldsig#", "Signature");

      if (nodeList.getLength() == 1)
      {
        return (Element)nodeList.item(0);
      }
      return null;
    }
    catch (Exception ex)
    {
      throw new RuntimeException("TIMESTAMP_GENERATOR_ERROR");
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
      System.out.println("Initializing PSIS client...");
      Class cls = getClass();
      String className = cls.getName();
      int index = className.lastIndexOf(".");
      String path = "/" + className.substring(0, index).replace('.', '/');
      URL wsdlLocation = cls.getResource(path + "/PSIS.wsdl");

      System.out.println("PSIS WSDL: " + wsdlLocation);

      dssServiceURL = MatrixConfig.getProperty(
        "org.santfeliu.security.provider.PSIS.serviceURL");
      System.out.println("PSIS URL: " + dssServiceURL);
      dssPdfServiceURL = MatrixConfig.getProperty(
        "org.santfeliu.security.provider.PSIS.pdf.serviceURL");
      System.out.println("PSIS PDF URL: " + dssPdfServiceURL);

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

    validateSignatureXML(document, null);
  }

  private void testCreateTimeStamp() throws Exception
  {
    byte[] digest = new byte[16];

    Element elem = createXMLTimeStamp(digest,
      "http://www.w3.org/2000/09/xmldsig#sha1", null);
    System.out.println(elem);
  }

  public static void main(String args[])
  {
    try
    {
      MatrixConfig.setProperty("org.santfeliu.security.provider.PSIS.pdf.serviceURL",
        "http://psisbeta.catcert.net/psis/catcert-test/dsspdf"
        //"http://psis.catcert.net/psis/catcert/dss"
        );
      PSIS psis = new PSIS();
      File file = new File("C:\\projectes\\PAdESJava1.6\\src\\prova.pdf");
      byte[] data = IOUtils.toByteArray(new FileInputStream(file));
      boolean valid =
        psis.preserveSignaturePDF(data, System.out);
      System.out.println("Valid:" + valid);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
