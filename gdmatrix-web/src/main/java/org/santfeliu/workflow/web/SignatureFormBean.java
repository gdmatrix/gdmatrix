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
package org.santfeliu.workflow.web;

import cat.aoc.valid.DocumentToSign;
import cat.aoc.valid.ValidClient;
import cat.mobileid.HashSignatureWS;
import cat.mobileid.MobileIdWS;
import com.sun.org.apache.xml.internal.security.utils.UnsyncBufferedOutputStream;
//import org.apache.xml.security.utils.UnsyncBufferedOutputStream;
import java.io.ByteArrayInputStream;
import org.apache.xml.security.utils.XMLUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.DigesterOutputStream;
import org.json.simple.JSONObject;
import org.matrix.signature.DataHash;
import org.matrix.signature.SignatureManagerPort;
import org.matrix.signature.SignatureManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.matrixclient.model.SignatureMatrixClientModel;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.form.Form;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 * @author unknown
 */
public class SignatureFormBean extends FormBean
{
  public static final String ERROR_PREFIX = "ERROR: ";

  transient HtmlBrowser browser;
  private String message;
  private String sigId; // sigId of document to sign
  private String result;
  private boolean IFrame;
  private String mobileidTicket;
  private String mobileidObject;
  
  private SignatureMatrixClientModel model;

  public SignatureFormBean()
  {
    model = new SignatureMatrixClientModel();
  }

  public void setBrowser(HtmlBrowser browser)
  {
    this.browser = browser;
  }

  public HtmlBrowser getBrowser()
  {
    return browser;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public boolean isIFrame()
  {
    return IFrame;
  }

  public void setIFrame(boolean IFrame)
  {
    this.IFrame = IFrame;
  }

  public void setDocument(String document)
  {
    this.sigId = document;
  }

  public String getDocument()
  {
    return sigId;
  }

  public String getSigId()
  {
    return sigId;
  }

  public void setSigId(String sigId)
  {
    this.sigId = sigId;
  }

  public void setResult(String result)
  {
    this.result = result;
  }

  public String getResult()
  {
    return result;
  }

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setForwardEnabled(false);
    instanceBean.setBackwardEnabled(false);

    browser = new HtmlBrowser();
    browser.setUrl(null);
    
    Object value;
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("document");
    if (value != null) sigId = String.valueOf(value);
    value = parameters.get("url");
    if (value != null) browser.setUrl(String.valueOf(value));
    value = parameters.get("iframe");
    if (value != null) IFrame = Boolean.parseBoolean(String.valueOf(value));

    return "signature_form";
  }

  @Override
  public Map submit()
  {
    HashMap variables = new HashMap();
    variables.put("result", result);
    return variables;
  }

  // Applet method
  public String sign()
  {
    if (result.indexOf(ERROR_PREFIX) == -1) // NO ERROR: OK or CANCEL
    {
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      return instanceBean.forward();    
    }
    else
    {
      String message = result.substring(ERROR_PREFIX.length());
      error(message);
    }
    return null;
  }
    
  // MatrixClient methods
  public SignatureMatrixClientModel getModel()
  {
    return model;
  }

  public void setModel(SignatureMatrixClientModel model)
  {
    this.model = model;
  }
  
  public String documentSigned()
  {
    try
    {
      result = (String)model.parseResult();
      if (result != null)
      {
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        return instanceBean.forward();         
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  /* cancel signature */ 
  public String cancelSignature()
  {
    result = "CANCEL";
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    return instanceBean.forward(); 
  }
  
  // VALid methods
  public String signValid()
  {
    try
    {
      // get VALid accessToken from session
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String accessToken = (String)userSessionBean.getAttribute("accessToken");
      if (accessToken == null)
        throw new Exception("NOT_LOGGED_WITH_VALID");

      // gets hashes of docsToSign
      ArrayList<DocumentToSign> docsToSign = new ArrayList<DocumentToSign>();
      SignatureManagerPort port = getSignatureManagerPort();
      List<DataHash> dataHashes = port.digestData(sigId);
      for (DataHash dataHash : dataHashes)
      {
        DocumentToSign docToSign = new DocumentToSign();
        docToSign.setName(dataHash.getName());
        docToSign.setHash(new String(Base64.encodeBase64(dataHash.getHash())));
        docToSign.setAlgorithm(dataHash.getAlgorithm());
        docsToSign.add(docToSign);
      }
      // create basic signature of docsToSign with VALid
      ValidClient client = new ValidClient();
      client.setBaseUrl(MatrixConfig.getProperty("valid.baseUrl"));
      client.setClientId(MatrixConfig.getProperty("valid.clientId"));
      client.setClientSecret(MatrixConfig.getProperty("valid.clientSecret"));
      client.setRedirectUrl(MatrixConfig.getProperty("valid.redirectUrl"));
      JSONObject signResult = client.getBasicSignature(accessToken, docsToSign);
      String status = (String)signResult.get("status");
      if ("ko".equals(status))
        throw new Exception((String)signResult.get("error"));

      String evidence = (String)signResult.get("evidence");
      byte[] bytes = Base64.decodeBase64(evidence.getBytes());
      port.addExternalSignature(sigId, bytes);

      result = "OK";
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      return instanceBean.forward();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  // Mobileid methods
  public int getMobileidState()
  {
    return mobileidTicket == null ? 0 : 1;
  }

  public String signMobileid()
  {
    mobileidTicket = null;
    try
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String NIF = userSessionBean.getNIF();

      String mobileidObjectId = UUID.randomUUID().toString();
      
      Document doc = 
        DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = doc.createElement("ds:Signature");
      root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", 
        "http://www.w3.org/2000/09/xmldsig#");
      doc.appendChild(root);
      Element objectElem = doc.createElement("ds:Object");
      objectElem.setAttribute("Id", mobileidObjectId);
      root.appendChild(objectElem);

      SignatureManagerPort port = getSignatureManagerPort();
      List<DataHash> dataHashes = port.digestData(sigId);
      for (DataHash dataHash : dataHashes)
      {
        Element docElem = doc.createElement("document");
        objectElem.appendChild(docElem);
        Element nomElem = doc.createElement("nom");
        nomElem.setTextContent(dataHash.getName());
        docElem.appendChild(nomElem);
        Element resumElem = doc.createElement("resum");
        resumElem.setTextContent(new String(Base64.encodeBase64(dataHash.getHash())));
        docElem.appendChild(resumElem);
        Element algElem = doc.createElement("algorisme");
        algElem.setTextContent(dataHash.getAlgorithm());
        docElem.appendChild(algElem);
      }
      
      MessageDigestAlgorithm mda = MessageDigestAlgorithm.getInstance(
        objectElem.getOwnerDocument(), Constants.ALGO_ID_DIGEST_SHA1);
      mda.reset();
      DigesterOutputStream diOs = new DigesterOutputStream(mda);
      OutputStream os = new UnsyncBufferedOutputStream(diOs);
      XMLSignatureInput output = new XMLSignatureInput(objectElem);         
      output.updateOutputStream(os);
      os.flush();
      String hexHash = new String(Hex.encodeHex(diOs.getDigestValue()));    

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      XMLUtils.outputDOMc14nWithComments(objectElem, bos);
      mobileidObject = new String(bos.toByteArray(), "UTF-8");
      
      String subject = MatrixConfig.getProperty("mobileId.hashSignature.subject");
      String source = MatrixConfig.getProperty("mobileId.source");
      
      HashSignatureWS client = getMobileidClient();
      Map<String, String> data = client.signHash(5, MobileIdWS.DOCTYPE_NIF,
        NIF, subject, Integer.parseInt(source), hexHash,
        getContextURL() + "/signatures/" + sigId, "1");

      mobileidTicket = data.get("ticket");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String pinEntered()
  {
    try
    {
      HashSignatureWS client = getMobileidClient();
      Map<String, String> data = client.checkSignHash(mobileidTicket);
      String error = data.get("error");
      if ("0".equals(error))
      {
        URL url = new URL(client.getSignedDocumentUrl(data));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try
        {
          IOUtils.copy(url.openStream(), os);
        }
        finally
        {
          os.close();
        }
        String document = new String(os.toByteArray(), "UTF-8");
        int index = document.indexOf("<ds:X509Certificate>");
        if (index != -1)
        {
          int index2 = document.indexOf("</ds:X509Certificate>");
          if (index2 != -1)
          {
            String certBase64 = document.substring(index + 20, index2);
            System.out.println(certBase64);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream is = new ByteArrayInputStream(
              Base64.decodeBase64(certBase64.getBytes()));
            try
            {
              X509Certificate certificate = 
                (X509Certificate)cf.generateCertificate(is);
              String subjectDN = certificate.getSubjectDN().toString();
              document = document.replace("<ds:X509Data>", 
                "<ds:X509Data><ds:X509SubjectName>" + subjectDN + 
                "</ds:X509SubjectName>");
            }
            finally
            {
              is.close();
            }
          }
        }
        document = document.replace("</ds:Signature>", 
          mobileidObject + "</ds:Signature>");
        SignatureManagerPort port = getSignatureManagerPort();
        port.addExternalSignature(sigId, document.getBytes("UTF-8"));
        
        result = "OK";
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        return instanceBean.forward();
      }
      else throw new Exception("ERROR: " + error);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String pinCancelled()
  {
    mobileidTicket = null; // enter PIN
    return null;
  }
  
  private SignatureManagerPort getSignatureManagerPort()
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(SignatureManagerService.class);
    return endpoint.getPort(SignatureManagerPort.class);
  }
  
  private HashSignatureWS getMobileidClient() throws Exception
  {
    String endpoint = MatrixConfig.getProperty("mobileId.hashSignature.endpoint");
    String keyStoreFilename = MatrixConfig.getProperty(
      "mobileId.keyStore.filename");
    String password = MatrixConfig.getProperty("mobileId.keyStore.password");    
    char[] keyStorePassword = password.toCharArray();

    File certificateDir = new File(MatrixConfig.getDirectory(), "certificates");
    File certificateFile = new File(certificateDir, keyStoreFilename);
    KeyStore ks = KeyStore.getInstance("PKCS12");
    InputStream is = new FileInputStream(certificateFile);
    try
    {
      ks.load(is, keyStorePassword);
    }
    finally
    {
      is.close();
    }
    return new HashSignatureWS(endpoint, ks, keyStorePassword);
  }  
}
