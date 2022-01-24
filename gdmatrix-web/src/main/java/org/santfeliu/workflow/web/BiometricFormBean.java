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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.signature.Property;
import org.matrix.signature.PropertyList;
import org.matrix.signature.SignatureManagerPort;
import org.matrix.signature.SignatureManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.transform.TransformationManager;
import org.santfeliu.doc.transform.TransformationRequest;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


/**
 *
 * @author unknown
 */
public class BiometricFormBean extends FormBean
{
  //Visible signature default values
  private static final String SIGN_DEFAULT_PAGE = "1";
  private static final String SIGN_DEFAULT_POSX = "163";
  private static final String SIGN_DEFAULT_POSY = "213";
  private static final String SIGN_DEFAULT_SIZEX = "70";
  private static final String SIGN_DEFAULT_SIZEY = "40";

  //Parameter fields
  private String message;
  private String sigId;
  private String pdfDocId;
  private String deviceName;
  private String signerName;
  private String signerIdent;
  private String signerIdentType;
  private String xsl;
  private String apiBaseUrl;
  private String apiUsername;
  private String apiPassword;
  private String signSizeX;
  private String signSizeY;
  private String signPosX;
  private String signPosY;
  private String signPosPage;
  private String signPosAnchor;

  //Internal fields
  private String docTitle;
  private String docTypeId;
  private String docGui;
  private boolean alreadySigned;

  //Transient objects
  transient Document signedPdfDoc;

  public BiometricFormBean()
  {

  }

  public Document getSignedPdfDoc()
  {
    return signedPdfDoc;
  }

  public void setSignedPdfDoc(Document signedPdfDoc)
  {
    this.signedPdfDoc = signedPdfDoc;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public String getDeviceName()
  {
    return deviceName;
  }

  public void setDeviceName(String deviceName)
  {
    this.deviceName = deviceName;
  }

  public String getSignerName()
  {
    return signerName;
  }

  public void setSignerName(String signerName)
  {
    this.signerName = signerName;
  }

  public String getSignerIdent()
  {
    return signerIdent;
  }

  public void setSignerIdent(String signerIdent)
  {
    this.signerIdent = signerIdent;
  }

  public String getSignerIdentType()
  {
    return signerIdentType;
  }

  public void setSignerIdentType(String signerIdentType)
  {
    this.signerIdentType = signerIdentType;
  }

  public String getXsl()
  {
    return xsl;
  }

  public void setXsl(String xsl)
  {
    this.xsl = xsl;
  }

  public String getSigId()
  {
    return sigId;
  }

  public void setSigId(String sigId)
  {
    this.sigId = sigId;
  }

  public String getPdfDocId()
  {
    return pdfDocId;
  }

  public void setPdfDocId(String pdfDocId)
  {
    this.pdfDocId = pdfDocId;
  }

  public String getApiBaseUrl()
  {
    return apiBaseUrl;
  }

  public void setApiBaseUrl(String apiBaseUrl)
  {
    this.apiBaseUrl = apiBaseUrl;
  }

  public String getApiUsername()
  {
    return apiUsername;
  }

  public void setApiUsername(String apiUsername)
  {
    this.apiUsername = apiUsername;
  }

  public String getApiPassword()
  {
    return apiPassword;
  }

  public void setApiPassword(String apiPassword)
  {
    this.apiPassword = apiPassword;
  }

  public String getSignPosX()
  {
    return signPosX;
  }

  public void setSignPosX(String signPosX)
  {
    this.signPosX = signPosX;
  }

  public String getSignPosY()
  {
    return signPosY;
  }

  public void setSignPosY(String signPosY)
  {
    this.signPosY = signPosY;
  }

  public String getSignSizeX()
  {
    return signSizeX;
  }

  public void setSignSizeX(String signSizeX)
  {
    this.signSizeX = signSizeX;
  }

  public String getSignSizeY()
  {
    return signSizeY;
  }

  public void setSignSizeY(String signSizeY)
  {
    this.signSizeY = signSizeY;
  }

  public String getSignPosPage()
  {
    return signPosPage;
  }

  public void setSignPosPage(String signPosPage)
  {
    this.signPosPage = signPosPage;
  }

  public String getSignPosAnchor()
  {
    return signPosAnchor;
  }

  public void setSignPosAnchor(String signPosAnchor)
  {
    this.signPosAnchor = signPosAnchor;
  }

  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setForwardEnabled(false);
    instanceBean.setBackwardEnabled(false);

    Object value;
    value = parameters.get("message");
    if (value != null && !"null".equals(value)) message = String.valueOf(value);
    value = parameters.get("sigId");
    if (value != null && !"null".equals(value)) sigId = String.valueOf(value);
    value = parameters.get("pdfDocId");
    if (value != null && !"null".equals(value)) pdfDocId = String.valueOf(value);
    value = parameters.get("deviceName");
    if (value != null && !"null".equals(value)) deviceName = String.valueOf(value);
    value = parameters.get("signerName");
    if (value != null && !"null".equals(value)) signerName = String.valueOf(value);
    value = parameters.get("signerIdent");
    if (value != null && !"null".equals(value)) signerIdent = String.valueOf(value);
    value = parameters.get("signerIdentType");
    if (value != null && !"null".equals(value)) signerIdentType = String.valueOf(value);
    value = parameters.get("xsl");
    if (value != null && !"null".equals(value)) xsl = String.valueOf(value);
    value = parameters.get("apiBaseUrl");
    if (value != null && !"null".equals(value)) apiBaseUrl = String.valueOf(value);
    if (!apiBaseUrl.endsWith("/")) apiBaseUrl += "/";
    value = parameters.get("apiUsername");
    if (value != null && !"null".equals(value)) apiUsername = String.valueOf(value);
    value = parameters.get("apiPassword");
    if (value != null && !"null".equals(value)) apiPassword = String.valueOf(value);
    value = parameters.get("signSizeX");
    if (value != null && !"null".equals(value)) signSizeX = String.valueOf(value);
    else signSizeX = SIGN_DEFAULT_SIZEX;
    value = parameters.get("signSizeY");
    if (value != null && !"null".equals(value)) signSizeY = String.valueOf(value);
    else signSizeY = SIGN_DEFAULT_SIZEY;
    value = parameters.get("signPosX");
    if (value != null && !"null".equals(value)) signPosX = String.valueOf(value);
    else signPosX = SIGN_DEFAULT_POSX;
    value = parameters.get("signPosY");
    if (value != null && !"null".equals(value)) signPosY = String.valueOf(value);
    else signPosY = SIGN_DEFAULT_POSY;
    value = parameters.get("signPosPage");
    if (value != null && !"null".equals(value)) signPosPage = String.valueOf(value);
    else signPosPage = SIGN_DEFAULT_PAGE;
    value = parameters.get("signPosAnchor");
    if (value != null && !"null".equals(value)) signPosAnchor = String.valueOf(value);

    try
    {
      init();
      if (alreadySigned)
      {
        message = "El document de signatura ja està signat biomètricament";
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "biometric_form";
  }

  public Map submit()
  {
    HashMap variables = new HashMap();
    if (signedPdfDoc != null && signedPdfDoc.getDocId() != null)
    {
      variables.put("biometricPDFDocId", signedPdfDoc.getDocId());
    }
    return variables;
  }

  public String check() throws Exception
  {
    if (alreadySigned)
    {
      InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
      return instanceBean.forward();
    }
    else
    {
      String docStatus = apiGetDocumentStatus();
      if ("Signed".equals(docStatus)) //El document ha estat signat
      {
        byte[] docContent = apiDownloadSignedDocument();
        storeSignedDocumentLocally(docContent);
        if (sigId != null)
        {
          addReferenceToSignatureFile();
        }
        apiDeleteDocument(true);
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        return instanceBean.forward();
      }
      else if ("Rejected".equals(docStatus)) //El document ha estat refusat
      {
        apiDeleteDocument(false);
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        return instanceBean.forward();
      }
      else if ("Unsigned".equals(docStatus))
      {
        info("El document encara no ha estat signat");
      }
    }
    return null;
  }

  private void init() throws Exception
  {
    validateParams();
    if (sigId != null) //Signature file mode
    {
      //Obtenim el document XML a signar
      DocumentManagerClient docClient = getDocumentManagerClient();
      //Document xmlDoc = docClient.loadDocumentByName("sf:SIGNATURE", "sigId", sigId, null, 0);
      Document xmlDoc = docClient.loadDocumentByName(null, "sigId", sigId, null, 0);
      if (isXMLDocumentSigned(xmlDoc)) //ja està signat
      {
        alreadySigned = true;
      }
      else //no està signat -> El convertim en PDF per a treballar amb la API
      {
        alreadySigned = false;
        docTitle = xmlDoc.getTitle();
        docTypeId = xmlDoc.getDocTypeId();
        String fileName = "Fitxer " + deviceName + "_" + sigId;
        docGui = apiGetDocGui(fileName);
        if (docGui == null)
        {
          //Fem la conversió
          TransformationManager.init();
          TransformationRequest trReq = new org.santfeliu.doc.transform.TransformationRequest();
          trReq.setTransformerId("xml");
          trReq.setTransformationName("pdf");
          Map options = new java.util.HashMap();
          if (xsl != null)
          {
            options.put(org.santfeliu.doc.transform.impl.XMLTransformer.XSL_PROPERTY, xsl);
          }
          trReq.setOptions(options);
          DataHandler dh = TransformationManager.transform(xmlDoc, trReq);
          //Pujem el document PDF a signar a la plataforma ViDSigner
          docGui = apiUploadDocument(dh, fileName);
        }
      }
    }
    else if (pdfDocId != null) //Direct PDF Mode
    {
      alreadySigned = false;
      DocumentManagerClient docClient = getDocumentManagerClient();
      Document pdfDoc =  docClient.loadDocument(pdfDocId, 0, ContentInfo.ALL);
      docTitle = pdfDoc.getTitle();
      docTypeId = pdfDoc.getDocTypeId();
      String fileName = "Fitxer " + deviceName + "_" + pdfDocId;
      docGui = apiGetDocGui(fileName);
      if (docGui == null)
      {
        DataHandler dh = pdfDoc.getContent().getData();
        //Pujem el document PDF a signar a la plataforma ViDSigner
        docGui = apiUploadDocument(dh, fileName);
      }
    }
  }

  private void addReferenceToSignatureFile() throws Exception
  {
    try
    {
      String type = "url";
      String text = "http://" + getHost() + "/documents/" +
        signedPdfDoc.getContent().getContentId();
      PropertyList propertyList = new PropertyList();
      Property pAux = new Property();
      pAux.setName("nomfitxer");
      pAux.setValue("Document amb signatura biomètrica");
      propertyList.getProperty().add(pAux);
      pAux = new Property();
      pAux.setName("titol");
      pAux.setValue("Document adjunt");
      propertyList.getProperty().add(pAux);
      pAux = new Property();
      pAux.setName("biometric");
      pAux.setValue("true");
      propertyList.getProperty().add(pAux);
      getSignatureManagerPort().addData(sigId, type, text.getBytes(), propertyList);
    }
    catch (Exception ex)
    {
      throw new Exception("No s'ha pogut afegir la referència al PDF signat al document de signatura");
    }
  }

  private boolean isXMLDocumentSigned(Document xmlDoc) throws Exception
  {
    InputStream is = null;
    try
    {
      is = xmlDoc.getContent().getData().getInputStream();
      DocumentBuilder docBuilder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
      org.w3c.dom.Document doc = docBuilder.parse(is);
      return (doc.getElementsByTagName("biometric").getLength() > 0);
    }
    catch (Exception ex)
    {
      throw new Exception("No s'ha pogut comprovar si el document ja ha estat signat biomètricament");
    }
    finally
    {
      try
      {
        if (is != null) is.close();
      }
      catch (Exception ex) { }
    }
  }

  //API REST METHODS
  private String apiUploadDocument(DataHandler dh, String fileName) throws Exception
  {
    try
    {
      //String fileName = "PDF per a signar biometricament";
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      dh.writeTo(output);
      byte[] contentByteArray = output.toByteArray();
      String base64Content = Base64.getMimeEncoder().
        encodeToString(contentByteArray);
      String requestContent =
        "{" +
        "  \"DocContent\":\"" + base64Content + "\"," +
        "  \"FileName\":\"" + fileName + "\"," +
        "  \"OrderedSignatures\":false," +
        "  \"Signers\":" +
        "  [" +
        "    {" +
        "      \"DeviceName\":\"" + deviceName + "\"," +
        "      \"NumberID\":\"" + signerIdent + "\"," +
        "      \"SignerGUI\":null," +
        "      \"SignerName\":\"" + signerName + "\"," +
        "      \"TypeOfID\":\"" + signerIdentType + "\"," +
        "      \"Visible\":" +
        getVisibleSignatureBlock() +
        "    }" +
        "  ]" +
        "}";
      String url = apiBaseUrl + "documents";
      HttpClient client = new HttpClient();
      PostMethod method = new PostMethod(url);
      method.addRequestHeader("Authorization", getAuthString());
      method.addRequestHeader("Content-Type", "application/json; charset=utf-8");
      method.setRequestBody(requestContent);
      try
      {
        int statusCode = client.executeMethod(method);
        byte[] response = method.getResponseBody();
        String jsonResponse = new String(response);
        if (statusCode != HttpStatus.SC_OK)
        {
          String errorMsg = "Method failed: " + method.getStatusLine() + ": " + jsonResponse;
          logError(errorMsg);
          throw new Exception(errorMsg);
        }
        else
        {
          return extractDirectValueFromJson(jsonResponse, "DocGUI");
        }
      }
      catch (HttpException ex)
      {
        System.err.println("Fatal protocol violation: " + ex.getMessage());
        throw ex;
      }
      catch (IOException ex)
      {
        System.err.println("Fatal transport error: " + ex.getMessage());
        throw ex;
      }
      finally
      {
        method.releaseConnection();
      }
    }
    catch (Exception ex)
    {
      throw new Exception("No s'ha pogut enviar el document a signar a ViDSigner");
    }
  }

  private void apiDeleteDocument(boolean signed) throws Exception
  {
    try
    {
      String url = apiBaseUrl + (signed ? "signeddocuments/" : "documents/") + docGui;
      HttpClient client = new HttpClient();
      DeleteMethod method = new DeleteMethod(url);
      method.addRequestHeader("Authorization", getAuthString());
      try
      {
        int statusCode = client.executeMethod(method);
        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NO_CONTENT)
        {
          byte[] response = method.getResponseBody();
          String jsonResponse = new String(response);
          String errorMsg = "Method failed: " + method.getStatusLine() + ": " + jsonResponse;
          logError(errorMsg);
          throw new Exception(errorMsg);
        }
        else
        {
          //nothing here
        }
      }
      catch (HttpException ex)
      {
        System.err.println("Fatal protocol violation: " + ex.getMessage());
        throw ex;
      }
      catch (IOException ex)
      {
        System.err.println("Fatal transport error: " + ex.getMessage());
        throw ex;
      }
      finally
      {
        method.releaseConnection();
      }
    }
    catch (Exception ex)
    {
      throw new Exception("No s'ha pogut esborrar el document a ViDSigner");
    }
  }

  private String apiGetDocumentStatus() throws Exception
  {
    try
    {
      String url = apiBaseUrl + "documentinfo/" + docGui;
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(url);
      method.addRequestHeader("Authorization", getAuthString());
      try
      {
        int statusCode = client.executeMethod(method);
        byte[] response = method.getResponseBody();
        String jsonResponse = new String(response);
        if (statusCode != HttpStatus.SC_OK)
        {
          String errorMsg = "Method failed: " + method.getStatusLine() + ": " + jsonResponse;
          logError(errorMsg);
          throw new Exception(errorMsg);
        }
        else
        {
          return extractDirectValueFromJson(jsonResponse, "DocStatus");
        }
      }
      catch (HttpException ex)
      {
        System.err.println("Fatal protocol violation: " + ex.getMessage());
        throw(ex);
      }
      catch (IOException ex)
      {
        System.err.println("Fatal transport error: " + ex.getMessage());
        throw(ex);
      }
      finally
      {
        method.releaseConnection();
      }
    }
    catch (Exception ex)
    {
      throw new Exception("No s'ha pogut avaluar si el document ha estat signat");
    }
  }

  private byte[] apiDownloadSignedDocument() throws Exception
  {
    try
    {
      String url = apiBaseUrl + "signeddocuments/" + docGui;
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(url);
      method.addRequestHeader("Authorization", getAuthString());
      try
      {
        int statusCode = client.executeMethod(method);
        byte[] response = method.getResponseBody();
        String jsonResponse = new String(response);
        if (statusCode != HttpStatus.SC_OK)
        {
          String errorMsg = "Method failed: " + method.getStatusLine() + ": " + jsonResponse;
          logError(errorMsg);
          throw new Exception(errorMsg);
        }
        else
        {
          String docContentBase64 = extractDirectValueFromJson(jsonResponse, "DocContent");
          byte[] docContent = Base64.getMimeDecoder().decode(docContentBase64);
          return docContent;
        }
      }
      catch (HttpException ex)
      {
        System.err.println("Fatal protocol violation: " + ex.getMessage());
        throw(ex);
      }
      catch (IOException ex)
      {
        System.err.println("Fatal transport error: " + ex.getMessage());
        throw(ex);
      }
      finally
      {
        method.releaseConnection();
      }
    }
    catch (Exception ex)
    {
      throw new Exception("No s'ha pogut descarregar el document signat per ViDSigner");
    }
  }

  private String apiGetDocGui(String fileName) throws Exception
  {
    try
    {
      //String url = apiBaseUrl + "documentsbydevice?device=" + deviceName;
      String url = apiBaseUrl + "documentlist/all";
      HttpClient client = new HttpClient();
      HttpMethod method = new GetMethod(url);
      method.addRequestHeader("Authorization", getAuthString());
      try
      {
        int statusCode = client.executeMethod(method);
        byte[] response = method.getResponseBody();
        String jsonResponse = new String(response);
        if (statusCode != HttpStatus.SC_OK)
        {
          String errorMsg = "Method failed: " + method.getStatusLine() + ": " + jsonResponse;
          logError(errorMsg);
          throw new Exception(errorMsg);
        }
        else
        {
          try
          {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray)parser.parse(jsonResponse);
            for (Object obj : jsonArray)
            {
              JSONObject jsonObj = (JSONObject)obj;
              if (jsonObj.get("FileName") != null && jsonObj.get("FileName").equals(fileName))
              {
                return (String)jsonObj.get("DocGUI");
              }
            }
            return null;
          }
          catch (Exception ex)
          {
            throw new Exception("No s'ha pogut obtenir el valor de la propietat FileName del document");
          }
        }
      }
      catch (HttpException ex)
      {
        System.err.println("Fatal protocol violation: " + ex.getMessage());
        throw(ex);
      }
      catch (IOException ex)
      {
        System.err.println("Fatal transport error: " + ex.getMessage());
        throw(ex);
      }
      finally
      {
        method.releaseConnection();
      }
    }
    catch (Exception ex)
    {
      throw new Exception("No s'ha pogut obtenir el docGUI del document");
    }
  }

  private String getAuthString()
  {
    String userPass = apiUsername + ":" + apiPassword;
    String encodedUserPass = Base64.getMimeEncoder().
      encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
    String authValue = String.format("Basic " + encodedUserPass);
    return authValue;
  }

  private String getVisibleSignatureBlock()
  {
    if (signPosAnchor != null)
    {
      return
          "      {" +
          "        \"SizeX\":" + signSizeX + "," +
          "        \"SizeY\":" + signSizeY + "," +
          "        \"Anchor\":\"" + signPosAnchor + "\"" +
          "      }";
    }
    else
    {
      return
          "      {" +
          "        \"Page\":" + signPosPage + "," +
          "        \"PosX\":" + signPosX + "," +
          "        \"PosY\":" + signPosY + "," +
          "        \"SizeX\":" + signSizeX + "," +
          "        \"SizeY\":" + signSizeY + "," +
          "        \"SignatureField\":null" +
          "      }";
    }
  }

  private String extractDirectValueFromJson(String json, String propName) throws Exception
  {
    try
    {
      JSONParser parser = new JSONParser();
      JSONObject jsonObj = (JSONObject)parser.parse(json);
      return (jsonObj.get(propName) != null ? String.valueOf(jsonObj.get(propName)) : null);
    }
    catch (Exception ex)
    {
      throw new Exception("No s'ha pogut obtenir el valor de la propietat " + propName + " del document");
    }
  }

  private SignatureManagerPort getSignatureManagerPort()
  {
    String adminUserId = MatrixConfig.getProperty("adminCredentials.userId");
    String adminPwd = MatrixConfig.getProperty("adminCredentials.password");
    WSEndpoint endpoint =
      WSDirectory.getInstance().getEndpoint(SignatureManagerService.class);
    return endpoint.getPort(SignatureManagerPort.class, adminUserId, adminPwd);
  }

  private DocumentManagerClient getDocumentManagerClient()
  {
    String adminUserId = MatrixConfig.getProperty("adminCredentials.userId");
    String adminPwd = MatrixConfig.getProperty("adminCredentials.password");
    return new DocumentManagerClient(adminUserId, adminPwd);
  }

  private String getHost()
  {
    return java.lang.System.getProperty("host");
  }

  private void validateParams() throws Exception
  {
    if (sigId == null && pdfDocId == null)
      throw new Exception("S'ha d'especificar el document XML de signatura o PDF a signar");
    if (sigId != null && xsl == null)
      throw new Exception("S'ha d'especificar la transformació XSL a aplicar al document XML de signatura");
    if (deviceName == null)
      throw new Exception("S'ha d'especificar el nom del dispositiu");
    if (signerName == null)
      throw new Exception("S'ha d'especificar el nom del signatari");
    if (signerIdent == null)
      throw new Exception("S'ha d'especificar el identificador (NIF, NIE, ...) del signatari");
    if (signerIdentType == null)
      throw new Exception("S'ha d'especificar el tipus de document identificador (NIF, NIE, ...) del signatari");
    if (apiBaseUrl == null)
      throw new Exception("S'ha d'especificar la URL de la VidSigner API");
    if (apiUsername == null)
      throw new Exception("S'ha d'especificar el nom d'usuari per a accedir a la VidSigner API");
    if (apiPassword == null)
      throw new Exception("S'ha d'especificar la paraula de pas per a accedir a la VidSigner API");
  }

  private void storeSignedDocumentLocally(byte[] docContent)
  {
    DataSource outDataSource =
      new MemoryDataSource(docContent, "Signed PDF", "application/pdf");
    DataHandler dh = new DataHandler(outDataSource);
    signedPdfDoc = new org.matrix.doc.Document();
    signedPdfDoc.setTitle(docTitle);
    signedPdfDoc.setDocTypeId(docTypeId);
    Content pdfContent = new org.matrix.doc.Content();
    pdfContent.setContentType("application/pdf");
    pdfContent.setData(dh);
    signedPdfDoc.setContent(pdfContent);
    signedPdfDoc.setLanguage(DocumentConstants.UNIVERSAL_LANGUAGE);
    org.matrix.dic.Property p = null;
    if (sigId != null)
    {
      p = new org.matrix.dic.Property();
      p.setName("xmlSigId");
      p.getValue().add(sigId);
    }
    else if (pdfDocId != null)
    {
      p = new org.matrix.dic.Property();
      p.setName("pdfDocId");
      p.getValue().add(pdfDocId);
    }
    if (p != null) signedPdfDoc.getProperty().add(p);
    DocumentManagerClient docClient = getDocumentManagerClient();
    signedPdfDoc = docClient.storeDocument(signedPdfDoc);
    signedPdfDoc = docClient.loadDocument(signedPdfDoc.getDocId(), 0, ContentInfo.ALL);
  }

  private void logError(String errorMsg)
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.getVariables().put("biometricError", errorMsg);
    System.err.println(errorMsg);
  }

}
