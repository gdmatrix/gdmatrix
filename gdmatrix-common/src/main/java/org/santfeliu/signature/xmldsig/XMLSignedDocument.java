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
package org.santfeliu.signature.xmldsig;

import com.sun.org.apache.xml.internal.security.utils.UnsyncBufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.DigesterOutputStream;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.matrix.signature.DataHash;
import org.santfeliu.security.SecurityProvider;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.signature.SignedDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import static org.apache.xml.security.algorithms.MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256;

/**
 *
 * @author realor
 */
public class XMLSignedDocument implements SignedDocument
{
  static final Logger LOGGER = Logger.getLogger("XMLSignedDocument");

  private Document doc;
  private Element root;
  private String BaseURI;
  private final ArrayList datas = new ArrayList();
  private final ArrayList signatures = new ArrayList();
  private final Map properties = new HashMap();

  static final String CHARSET = "UTF-8";

  static final String VERSION = "1.2";
  static final String TAG_SIGNED_DOCUMENT = "SignedDocument";
  static final String TAG_DATA = "Data";
  static final String TAG_CONTENT = "Content";
  static final String TAG_PROPERTIES = "Properties";

  static final String MATRIX_URI = "http://gdmatrix.org/xmldsig";
  static final String MATRIX_NS = "mx";
  static final String TAG_MATRIX_DOCUMENTS = "documents";
  static final String TAG_MATRIX_DOCUMENT = "document";
  static final String TAG_MATRIX_NAME = "name";
  static final String TAG_MATRIX_HASH = "hash";
  static final String TAG_MATRIX_ALGORITHM = "algorithm";

  static final String XADES_URI = "http://uri.etsi.org/01903/v1.2.2#";
  static final String XMLDSIG_NS = "ds";
  static final String XADES_NS = "xades";
  static final String TAG_QUALIFYING_PROPERTIES = "QualifyingProperties";
  static final String TAG_SIGNED_PROPERTIES = "SignedProperties";
  static final String TAG_UNSIGNED_PROPERTIES = "UnsignedProperties";
  static final String TAG_SIGNED_SIGNATURE_PROPERTIES =
    "SignedSignatureProperties";
  static final String TAG_SIGNING_TIME = "SigningTime";
  static final String TAG_SIGNING_CERTIFICATE = "SigningCertificate";
  static final String TAG_CERT = "Cert";
  static final String TAG_CERT_DIGEST = "CertDigest";
  static final String TAG_ISSUER_SERIAL = "IssuerSerial";

  static final String TAG_SIGNATURE_POLICY_IDENTIFIER =
    "SignaturePolicyIdentifier";
  static final String TAG_SIGNATURE_POLICY_IMPLIED = "SignaturePolicyImplied";
  static final String TAG_SIGNATURE_POLICY_ID = "SignaturePolicyId";
  static final String TAG_SIG_POLICY_ID = "SigPolicyId";
  static final String TAG_SIG_POLICY_HASH = "SigPolicyHash";

  static final String TAG_UNSIGNED_SIGNATURE_PROPERTIES =
    "UnsignedSignatureProperties";
  static final String TAG_SIGNATURE_TIMESTAMP = "SignatureTimeStamp";
  static final String TAG_INCLUDE = "Include";
  static final String TAG_XML_TIMESTAMP = "XMLTimeStamp";

  static final String ATT_VERSION = "version";

  /* algorithms */
  static final String HASH_ALGO_ID = ALGO_ID_DIGEST_SHA256;
  static final String HASH_ALGO = "SHA-256";
  static final String SIGN_ALGO_ID = XMLSignature.ALGO_ID_SIGNATURE_RSA;

  static boolean createTimeStamp = true; // XAdES T
  static boolean policy = true;
  static boolean policyImplied = true;


  public XMLSignedDocument()
  {
  }

  @Override
  public void newDocument() throws Exception
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
    doc = db.newDocument();
    root = doc.createElement(TAG_SIGNED_DOCUMENT);
    root.setAttribute(ATT_VERSION, VERSION);
    doc.appendChild(root);
    BaseURI = new File("c:/signature.xml").toURI().toURL().toString();
    datas.clear();
    signatures.clear();
  }

  @Override
  public void parseDocument(InputStream is) throws Exception
  {
    datas.clear();
    signatures.clear();

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
    doc = db.parse(is);
    root = (Element)doc.getFirstChild();

    Node sdocNode = findNode(doc.getFirstChild(), TAG_SIGNED_DOCUMENT);
    // load Data sections
    Node dfNode = findNode(sdocNode.getFirstChild(), TAG_DATA);
    while (dfNode != null)
    {
      Data data = new Data((Element)dfNode, BaseURI);
      datas.add(data);
      dfNode = findNode(dfNode.getNextSibling(), TAG_DATA);
    }
    // load Signatures
    Node sigNode = findNode(sdocNode.getFirstChild(),
      XMLDSIG_NS + ":" + Constants._TAG_SIGNATURE);
    while (sigNode != null)
    {
      XMLSignature signature = new XMLSignature((Element)sigNode, BaseURI);
      signatures.add(signature);
      sigNode = findNode(sigNode.getNextSibling(),
        XMLDSIG_NS + ":" + Constants._TAG_SIGNATURE);
    }

    // set Id attributes
    NodeList nodes = doc.getElementsByTagName("*");
    for (int index = 0; index < nodes.getLength(); index++)
    {
      Node node = nodes.item(index);
      if (node instanceof Element)
      {
        Element element = (Element)node;
        if (element.hasAttribute("Id"))
        {
          element.setIdAttribute("Id", true);
        }
      }
    }
    LOGGER.log(Level.FINE, "Datas: {0}", datas.size());
    LOGGER.log(Level.FINE, "Signatures: {0}", signatures.size());
  }

  @Override
  public String addData(String dataType, byte[] content, Map properties)
    throws Exception
  {
    Data dataSection = new Data(doc, BaseURI);
    String id = getUniqueId();
    dataSection.setId(id);
    dataSection.setType(dataType);
    dataSection.setContent(doc, content);
    dataSection.setProperties(doc, properties);
    root.appendChild(dataSection.getElement());
    datas.add(dataSection);
    dataSection.getElement().setIdAttribute("Id", true);
    return id;
  }

  @Override
  public byte[] addSignature(X509Certificate cert,
    String policyId, String policyDigest) throws Exception
  {
    // Create signature
    XMLSignature signature = new XMLSignature(doc, BaseURI, SIGN_ALGO_ID);
    root.appendChild(signature.getElement());
    String signatureId = getUniqueId();
    signature.setId(signatureId);
    signature.getElement().setIdAttribute("Id", true);

    Transforms transforms = getTransforms();

    // adding keyInfo:X509Data
    X509Data X509Data = new X509Data(doc);
    X509Data.addIssuerSerial(cert.getIssuerDN().getName(),
      cert.getSerialNumber());
    X509Data.addSubjectName(cert);
    X509Data.addCertificate(cert);
    signature.getKeyInfo().add(X509Data);

    // adding keyInfo:KeyValue
    signature.addKeyInfo(cert.getPublicKey());

    // add XAdES object
    String signedPropertiesId = getUniqueId();
    ObjectContainer obj = createXAdESObject(cert,
      signatureId, signedPropertiesId, policyId, policyDigest);
    signature.appendObject(obj);
    signature.addDocument("#" + signedPropertiesId,
      transforms, HASH_ALGO_ID, getUniqueId(), XADES_URI + "SignedProperties");

    // add Documents object
    String documentsId = getUniqueId();
    ObjectContainer docCont = createDocumentObject(documentsId);
    signature.appendObject(docCont);
    signature.addDocument("#" + documentsId, transforms, HASH_ALGO_ID);

    // adding signature to XMLSignedDocument
    signatures.add(signature);

    // calculate digest
    SignedInfo signedInfo = signature.getSignedInfo();
    signedInfo.generateDigestValues();

    // get data to sign
    org.santfeliu.signature.xmldsig.ByteArrayOutputStream bos =
      new org.santfeliu.signature.xmldsig.ByteArrayOutputStream();
    signedInfo.signInOctetStream(bos);
    byte[] dataToSign = bos.toByteArray();

    return dataToSign;
  }

  @Override
  public void setSignatureValue(byte[] signatureData)
    throws Exception
  {
    // get last signature
    XMLSignature signature =
      (XMLSignature)signatures.get(signatures.size() - 1);

    // ------------- adding signatureData --------------
    Element signatureValueElem = (Element)findNode(
      signature.getElement().getFirstChild(),
      XMLDSIG_NS + ":" + Constants._TAG_SIGNATUREVALUE);

    // set signatureValueId
    signatureValueElem.setAttribute("Id", getUniqueId());

    while (signatureValueElem.hasChildNodes())
    {
      signatureValueElem.removeChild(signatureValueElem.getFirstChild());
    }
    String base64codedValue = Base64.getMimeEncoder().
      encodeToString(signatureData);

    if (base64codedValue.length() > 76)
    {
      base64codedValue = "\n" + base64codedValue + "\n";
    }
    Text t = signature.getDocument().createTextNode(base64codedValue);
    signatureValueElem.appendChild(t);

    if (createTimeStamp)
    {
      addTimeStamp(HASH_ALGO_ID);
    }
  }

  @Override
  public void removeSignature() throws Exception
  {
    // remove the last signature
    Node node = doc.getFirstChild().getLastChild();
    if (node.getLocalName().equals(Constants._TAG_SIGNATURE))
    {
      doc.getFirstChild().removeChild(node);
      signatures.remove(signatures.size() - 1);
    }
  }

  @Override
  public List<DataHash> digestData() throws Exception
  {
    ArrayList<DataHash> dataHashList = new ArrayList<>();
    for (int i = 0; i < datas.size(); i++)
    {
      Data data = (Data)datas.get(i);
      DataHash dataHash = new DataHash();
      dataHash.setName(data.getId());
      dataHash.setHash(data.digest(HASH_ALGO_ID));
      dataHash.setAlgorithm(HASH_ALGO);
      dataHashList.add(dataHash);
      if ("url".equals(data.getType()))
      {
        // add additional DataHash for the external content referenced by url
        String url = data.getText();
        URL docUrl = new URL(url);
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGO);
        URLConnection conn = docUrl.openConnection();
        conn.setConnectTimeout(3600000); // 1 hour
        conn.setReadTimeout(3600000); // 1 hour
        try (InputStream is = conn.getInputStream())
        {
          int n = 0;
          byte[] buffer = new byte[8192];
          while (n != -1)
          {
            n = is.read(buffer);
            if (n > 0)
            {
              messageDigest.update(buffer, 0, n);
            }
          }
          byte[] hash = messageDigest.digest();
          dataHash = new DataHash();
          dataHash.setName(url);
          dataHash.setHash(hash);
          dataHash.setAlgorithm(HASH_ALGO);
          dataHashList.add(dataHash);
        }
      }
    }
    return dataHashList;
  }

  @Override
  public void addExternalSignature(byte[] signatureElementData) throws Exception
  {
    ByteArrayInputStream is = new ByteArrayInputStream(signatureElementData);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
    Document signDoc = db.parse(is);
    Element documentElement = signDoc.getDocumentElement();
    if (!documentElement.getNodeName().
      equals(XMLDSIG_NS + ":" + Constants._TAG_SIGNATURE))
      throw new Exception("INVALID_SIGNATURE_FORMAT");

    Element signatureElement = (Element)doc.importNode(documentElement, true);
    doc.getDocumentElement().appendChild(signatureElement);

    XMLSignature signature = new XMLSignature(signatureElement, BaseURI);
    signatures.add(signature);
  }

  @Override
  public boolean verifyDocument() throws Exception
  {
    boolean valid = true;
    for (int i = 0; i < signatures.size(); i++)
    {
      boolean r = verifySignature(i);
      LOGGER.log(Level.FINE, "Verifing signature {0}: {1}", new Object[]{i, r});
      valid = valid && r;
    }
    return valid;
  }

  @Override
  public void writeDocument(OutputStream os)
    throws Exception
  {
    String preambul = "<?xml version=\"1.0\" encoding=\"" + CHARSET + "\"?>";
    os.write(preambul.getBytes());
    XMLUtils.outputDOMc14nWithComments(doc, os);
  }

  @Override
  public void setId(String id)
  {
    Element elem = (Element)doc.getFirstChild();
    elem.setAttribute("Id", id);
  }

  @Override
  public String getId()
  {
    Element elem = (Element)doc.getFirstChild();
    return elem.getAttribute("Id");
  }

  @Override
  public String getMimeType()
  {
    return "text/xml";
  }

  @Override
  public Map getProperties()
  {
    return properties;
  }

  //*****************************************************************

  public Data getData(int index)
  {
    return (Data)datas.get(index);
  }

  public XMLSignature getSignature(int index)
  {
    return (XMLSignature)signatures.get(index);
  }

  public boolean verifySignature(int index) throws Exception
  {
    XMLSignature signature = (XMLSignature)signatures.get(index);
    SignedInfo signedInfo = signature.getSignedInfo();
    int numRef = signedInfo.getLength();
    for (int i = 0; i < numRef; i++)
    {
      Reference ref = signedInfo.item(i);
      String uri = ref.getURI();
      LOGGER.log(Level.FINE, "Validating reference {0}: {1}",
        new Object[]{uri, ref.verify()});
    }

    KeyInfo keyInfo = signature.getKeyInfo();
    if (keyInfo != null)
    {
      X509Certificate cert = keyInfo.getX509Certificate();
      if (cert != null)
      {
        LOGGER.info("Certificate found");
        return signature.checkSignatureValue(cert);
      }
      else
      {
        LOGGER.warning("Did not find a Certificate");
        PublicKey pk = signature.getKeyInfo().getPublicKey();
        if (pk != null)
        {
          return signature.checkSignatureValue(pk);
        }
        else
        {
          LOGGER.warning(
            "Did not find a public key, so I can't check the signature");
          return false;
        }
      }
    }
    else
    {
      LOGGER.warning(
        "Did not find key info, so I can't check the signature");
      return false;
    }
  }

  public int getDataCount()
  {
    return datas.size();
  }

  public int getSignaturesCount()
  {
    return signatures.size();
  }

  public byte[] toByteArray() throws Exception
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    writeDocument(os);
    return os.toByteArray();
  }

  public Document getDOM()
  {
    return doc;
  }

  private String getUniqueId()
  {
    return "id-" + UUID.randomUUID().toString();
  }

  private Node findNode(Node node, String name)
  {
    boolean stop = false;
    while (node != null && !stop)
    {
      String nodeName = node.getNodeName();
      if (nodeName != null)
      {
        if (nodeName.equalsIgnoreCase(name)) stop = true;
        else node = node.getNextSibling();
      }
      else
      {
        node = node.getNextSibling();
      }
    }
    return node;
  }

  private String getMimeType(String contentType)
  {
    int index = contentType.indexOf(";");
    return (index == -1) ? contentType : contentType.substring(0, index);
  }

  private ObjectContainer createXAdESObject(X509Certificate cert,
    String signatureId, String signedPropertiesId,
    String policyId, String policyDigest) throws Exception
  {
    Document doc = getDOM();
    ObjectContainer obj = new ObjectContainer(doc);

    Element qualPropElement =
      doc.createElementNS(XADES_URI, XADES_NS + ":" + TAG_QUALIFYING_PROPERTIES);
    obj.appendChild(qualPropElement);
    qualPropElement.setAttributeNS(Constants.NamespaceSpecNS,
      "xmlns:" + XADES_NS, XADES_URI);
    qualPropElement.setAttribute("Target", "#" + signatureId);

    Element sigPropElement =
      doc.createElementNS(XADES_URI, XADES_NS + ":" + TAG_SIGNED_PROPERTIES);
    qualPropElement.appendChild(sigPropElement);
    sigPropElement.setAttribute("Id", signedPropertiesId);

    sigPropElement.setIdAttribute("Id", true);

    Element signaturePropElement =
      doc.createElementNS(XADES_URI, XADES_NS + ":" + TAG_SIGNED_SIGNATURE_PROPERTIES);
    sigPropElement.appendChild(signaturePropElement);

    Element sigTimeElement =
      doc.createElementNS(XADES_URI, XADES_NS + ":" + TAG_SIGNING_TIME);
    signaturePropElement.appendChild(sigTimeElement);

    SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    Calendar calendar = Calendar.getInstance();
    String stime = dateFormat.format(calendar.getTime());
    stime = stime.substring(0, 22) + ":" + stime.substring(22);
    sigTimeElement.appendChild(doc.createTextNode(stime));

    Element sigCertElement =
      doc.createElementNS(XADES_URI, XADES_NS + ":" + TAG_SIGNING_CERTIFICATE);
    signaturePropElement.appendChild(sigCertElement);

    Element certElement = doc.createElementNS(XADES_URI, XADES_NS + ":" + TAG_CERT);
    sigCertElement.appendChild(certElement);

    Element certDigestElement =
      doc.createElementNS(XADES_URI, XADES_NS + ":" + TAG_CERT_DIGEST);
    certElement.appendChild(certDigestElement);

    Element digestMethodElement =
      doc.createElement(XMLDSIG_NS + ":" + Constants._TAG_DIGESTMETHOD);
    certDigestElement.appendChild(digestMethodElement);
    digestMethodElement.setAttribute("Algorithm", HASH_ALGO_ID);

    MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGO);
    byte[] digest = messageDigest.digest(cert.getEncoded());

    Element digestValueElement =
      doc.createElement(XMLDSIG_NS + ":" + Constants._TAG_DIGESTVALUE);
    certDigestElement.appendChild(digestValueElement);
    digestValueElement.appendChild(doc.createTextNode(
      Base64.getMimeEncoder().encodeToString(digest)));

    Element issuerSerialElement =
      doc.createElementNS(XADES_URI, XADES_NS + ":" + TAG_ISSUER_SERIAL);
    certElement.appendChild(issuerSerialElement);

    Element issuerNameElement =
      doc.createElement(XMLDSIG_NS + ":" + Constants._TAG_X509ISSUERNAME);
    issuerSerialElement.appendChild(issuerNameElement);
    issuerNameElement.appendChild(
      doc.createTextNode(cert.getIssuerDN().getName()));

    Element issuerSNElement =
      doc.createElement(XMLDSIG_NS + ":" + Constants._TAG_X509SERIALNUMBER);
    issuerSerialElement.appendChild(issuerSNElement);
    issuerSNElement.appendChild(
      doc.createTextNode(cert.getSerialNumber().toString()));

    if (policy)
    {
      Element sigPolElement = doc.createElementNS(XADES_URI,
        XADES_NS + ":" + TAG_SIGNATURE_POLICY_IDENTIFIER);
      signaturePropElement.appendChild(sigPolElement);

      if (policyImplied)
      {
        Element polElement = doc.createElementNS(XADES_URI,
          XADES_NS + ":" + TAG_SIGNATURE_POLICY_IMPLIED);
        sigPolElement.appendChild(polElement);
      }
      else
      {
        Element sigPolIdElement = doc.createElementNS(XADES_URI,
          XADES_NS + ":" + TAG_SIGNATURE_POLICY_ID);
        sigPolElement.appendChild(sigPolIdElement);

        Element sigIdElement = doc.createElementNS(XADES_URI,
          XADES_NS + ":" + TAG_SIG_POLICY_ID);
        sigPolIdElement.appendChild(sigIdElement);
        sigIdElement.appendChild(doc.createTextNode(policyId));

        Element sigHashElement = doc.createElementNS(XADES_URI,
          XADES_NS + ":" + TAG_SIG_POLICY_HASH);
        sigPolIdElement.appendChild(sigHashElement);

        Element policyDigestMethodElement =
          doc.createElement(XMLDSIG_NS + ":" + Constants._TAG_DIGESTMETHOD);
        sigHashElement.appendChild(policyDigestMethodElement);
        policyDigestMethodElement.setAttribute("Algorithm", HASH_ALGO_ID);

        Element policyDigestValueElement =
          doc.createElement(XMLDSIG_NS + ":" + Constants._TAG_DIGESTVALUE);
        sigHashElement.appendChild(policyDigestValueElement);
        policyDigestValueElement.appendChild(doc.createTextNode(policyDigest));
      }
    }
    return obj;
  }

  private ObjectContainer createDocumentObject(String documentsId)
    throws Exception
  {
    Document doc = getDOM();
    ObjectContainer obj = new ObjectContainer(doc);

    Element element = obj.getElement();
    element.setAttribute("Id", documentsId);
    element.setIdAttribute("Id", true);

    Element documentsElement =
      doc.createElementNS(MATRIX_URI, MATRIX_NS + ":" + TAG_MATRIX_DOCUMENTS);
    documentsElement.setAttribute("xmlns:" + MATRIX_NS, MATRIX_URI);
    element.appendChild(documentsElement);

    List<DataHash> dataHashes = digestData();
    for (DataHash dataHash : dataHashes)
    {
      Element documentElement =
        doc.createElementNS(MATRIX_URI, MATRIX_NS + ":" + TAG_MATRIX_DOCUMENT);
      documentsElement.appendChild(documentElement);

      Element nameElement =
        doc.createElementNS(MATRIX_URI, MATRIX_NS + ":" + TAG_MATRIX_NAME);
      nameElement.setTextContent(dataHash.getName());
      documentElement.appendChild(nameElement);

      Element hashElement =
        doc.createElementNS(MATRIX_URI, MATRIX_NS + ":" + TAG_MATRIX_HASH);
      hashElement.setTextContent(
        Base64.getMimeEncoder().encodeToString(dataHash.getHash()));
      documentElement.appendChild(hashElement);

      Element algorithmElement =
        doc.createElementNS(MATRIX_URI, MATRIX_NS + ":" + TAG_MATRIX_ALGORITHM);
      algorithmElement.setTextContent(dataHash.getAlgorithm());
      documentElement.appendChild(algorithmElement);
    }
    return obj;
  }

  private void addTimeStamp(String digestMethod) throws Exception
  {
    Document doc = getDOM();
    XMLSignature signature = getSignature(getSignaturesCount() - 1);

    Element objectElement = (Element)(findNode(
      signature.getElement().getFirstChild(),
      XMLDSIG_NS + ":" + Constants._TAG_OBJECT));

    Element qualPropElement = (Element)objectElement.getFirstChild();

    Element unsigPropElement = doc.createElementNS(XADES_URI,
      XADES_NS + ":" + TAG_UNSIGNED_PROPERTIES);
    qualPropElement.appendChild(unsigPropElement);

    Element unsigSignaturePropElement = doc.createElementNS(XADES_URI,
      XADES_NS + ":" + TAG_UNSIGNED_SIGNATURE_PROPERTIES);
    unsigPropElement.appendChild(unsigSignaturePropElement);

    Element signatureTimeStamp =
      doc.createElementNS(XADES_URI,
      XADES_NS + ":" + TAG_SIGNATURE_TIMESTAMP);
    signatureTimeStamp.setAttribute("Id", getUniqueId());
    unsigSignaturePropElement.appendChild(signatureTimeStamp);

    // Include
    Element signatureValue = (Element)findNode(
      signature.getElement().getFirstChild(),
      XMLDSIG_NS + ":" + Constants._TAG_SIGNATUREVALUE);
    String signatureValueId = signatureValue.getAttribute("Id");
    Element include = doc.createElementNS(
      XADES_URI, XADES_NS + ":" + TAG_INCLUDE);
    include.setAttribute("referencedData", "false");
    include.setAttribute("URI", "#" + signatureValueId);
    signatureTimeStamp.appendChild(include);

    // CanonicalizationMethod
    Element canonicalizationMethod = doc.createElementNS(
      Constants.SignatureSpecNS,
      XMLDSIG_NS + ":" + Constants._TAG_CANONICALIZATIONMETHOD);
    canonicalizationMethod.setAttribute("Algorithm",
      "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
    signatureTimeStamp.appendChild(canonicalizationMethod);

    // XMLTimeStamp
    Element XMLTimeStamp = doc.createElementNS(
      XADES_URI, XADES_NS + ":" + TAG_XML_TIMESTAMP);
    XMLTimeStamp.setAttribute("xmlns:dss",
      "urn:oasis:names:tc:dss:1.0:core:schema");
    signatureTimeStamp.appendChild(XMLTimeStamp);

    // Add XML TimeStamp: <ds:signature>
    SecurityProvider provider = SecurityUtils.getSecurityProvider();
    byte[] digestTst = calculateSignatureDigest(signature, digestMethod);
    Element sigTimeStamp =
      provider.createXMLTimeStamp(digestTst, digestMethod, null);
    XMLTimeStamp.appendChild(XMLTimeStamp.getOwnerDocument().importNode(
      sigTimeStamp, true));
  }

  private Transforms getTransforms() throws TransformationException
  {
    Transforms transforms = new Transforms(doc);
    //transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
    transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);
    return transforms;
  }

  private byte[] calculateSignatureDigest(XMLSignature signature,
    String hashAlgorithmURN) throws Exception
  {
    Element signatureValue = (Element)findNode(
      signature.getElement().getFirstChild(),
      XMLDSIG_NS + ":" + Constants._TAG_SIGNATUREVALUE);

    MessageDigestAlgorithm mda = MessageDigestAlgorithm.getInstance(
      signature.getDocument(), hashAlgorithmURN);

    mda.reset();
    DigesterOutputStream diOs = new DigesterOutputStream(mda);
    OutputStream os = new UnsyncBufferedOutputStream(diOs);
    XMLSignatureInput output = new XMLSignatureInput(signatureValue);
    output.updateOutputStream(os);
    os.flush();
    return diOs.getDigestValue();
  }

  static
  {
    // init apache lib
    try
    {
      org.apache.xml.security.Init.init();
      ResourceResolver.registerAtStart(
        "org.santfeliu.signature.xmldsig.HTTPResolver");
      ResourceResolver.registerAtStart(
        "org.santfeliu.signature.xmldsig.DetachedResolver");
    }
    catch (Exception ex)
    {
      LOGGER.warning(ex.toString());
    }
  }
}
