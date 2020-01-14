package org.santfeliu.workflow.web;

import cat.mobileid.MobileIdWS;
import cat.mobileid.PdfSignatureWS;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.signature.Property;
import org.matrix.signature.PropertyList;
import org.matrix.signature.SignatureManagerPort;
import org.matrix.signature.SignatureManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.transform.TransformationManager;
import org.santfeliu.doc.transform.TransformationRequest;
import static org.santfeliu.doc.transform.impl.XMLTransformer.XSL_PROPERTY;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


public class MobileIdFormBean extends FormBean
{
  public static final String ERROR_PREFIX = "ERROR: ";

  private String message;
  private String sigId;
  private String signerName;
  private String signerIdent;
  private String signerIdentType;
  private String signedPdfTitle = "Signed document";
  private String signedPdfDocTypeId = "Document";
  private String xsl;
  private String ticket;
  
  //Internal fields
  private boolean alreadySigned;
  
  //Transient objects
  transient Document signedPdfDoc;

  public MobileIdFormBean()
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
 
  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setForwardEnabled(false);
    instanceBean.setBackwardEnabled(false);

    Object value;
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("sigId");
    if (value != null) sigId = String.valueOf(value);
    value = parameters.get("signerIdent");
    if (value != null) signerIdent = String.valueOf(value);
    value = parameters.get("signerIdentType");
    if (value != null) signerIdentType = String.valueOf(value);
    value = parameters.get("xsl");
    if (value != null) xsl = String.valueOf(value);
    value = parameters.get("signedPdfTitle");
    if (value != null) signedPdfTitle = String.valueOf(value);
    value = parameters.get("signedPdfDocTypeId");
    if (value != null) signedPdfDocTypeId = String.valueOf(value);

    DocumentManagerClient docClient = getDocumentManagerClient();
    Document xmlDoc = 
      docClient.loadDocumentByName("sf:SIGNATURE", "sigId", sigId, null, 0);
    try
    {
      if (isXMLDocumentSigned(xmlDoc))
      {
        alreadySigned = true;
      }
      else
      {
        alreadySigned = false;

        //Fem la conversió
        TransformationManager.init();
        TransformationRequest trReq = 
          new org.santfeliu.doc.transform.TransformationRequest();
        trReq.setTransformerId("xml");
        trReq.setTransformationName("pdf");
        Map options = new java.util.HashMap();
        options.put(XSL_PROPERTY, xsl);
        trReq.setOptions(options);
        DataHandler dh = TransformationManager.transform(xmlDoc, trReq);
        File tempFile = IOUtils.writeToFile(dh);

        String subject = MatrixConfig.getProperty("mobileId.pdfSignature.subject");
        String source = MatrixConfig.getProperty("mobileId.source");
        
        PdfSignatureWS client = getPdfSignatureWS();
        Map<String, String> result = client.signPdf(25, MobileIdWS.DOCTYPE_NIF,
          signerIdent, subject, Integer.parseInt(source), tempFile);
        
        tempFile.delete();

        String errorCode = result.get("error");
        if (!"0".equals(errorCode)) throw new Exception(errorCode);

        ticket = result.get("ticket");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "mobileid_form";
  }

  public Map submit()
  {
    HashMap variables = new HashMap();
    if (signedPdfDoc != null && signedPdfDoc.getDocId() != null)
    {
      variables.put("signedPDFDocId", signedPdfDoc.getDocId());
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
      try
      {
        PdfSignatureWS client = getPdfSignatureWS();
        Map<String, String> result = client.checkSignPdf(ticket);
        
        String errorCode = result.get("error");
        if (!"0".equals(errorCode)) throw new Exception(errorCode);

        String urlSignedFile = result.get("urlSignedFile");
        URL url = new URL(urlSignedFile);
        File tempFile = IOUtils.writeToFile(url.openStream());
        DocumentManagerClient docClient = getDocumentManagerClient();
        signedPdfDoc = new Document();
        signedPdfDoc.setDocTypeId(signedPdfDocTypeId);
        Content content = new Content();
        content.setContentType("application/pdf");
        content.setData(new DataHandler(new FileDataSource(tempFile)));
        signedPdfDoc.setContent(content);
        signedPdfDoc.setTitle(signedPdfTitle);
        signedPdfDoc = docClient.storeDocument(signedPdfDoc);
        tempFile.delete();
        addReferenceToSignatureFile();
        InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
        return instanceBean.forward();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        error(ex);
      }
    }
    return "mobileid_form";
  }

  public PdfSignatureWS getPdfSignatureWS() throws Exception
  {
    String endpoint = MatrixConfig.getProperty("mobileId.pdfSignature.endpoint");
    String keyStoreFilename = MatrixConfig.getProperty(
      "mobileId.keyStore.filename");
    String password = MatrixConfig.getProperty(
      "mobileId.keyStore.password");
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
    return new PdfSignatureWS(endpoint, ks, keyStorePassword);    
  }
  
  private boolean isXMLDocumentSigned(Document xmlDoc) throws Exception
  {
    InputStream is = xmlDoc.getContent().getData().getInputStream();   
    try
    {
      DocumentBuilder docBuilder = 
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
      org.w3c.dom.Document doc = docBuilder.parse(is);
      return (doc.getElementsByTagName("mobileId").getLength() > 0);
    }
    finally
    {
      is.close();
    }
  }
  
  private void addReferenceToSignatureFile() throws Exception
  {
    try
    {
      String type = "url";
      String url = "http://" + getHost() + getContextPath() + "/documents/" + 
        signedPdfDoc.getContent().getContentId();
      PropertyList propertyList = new PropertyList();
      Property pAux = new Property();
      pAux.setName("title");
      pAux.setValue(signedPdfTitle);
      propertyList.getProperty().add(pAux);
      pAux = new Property();
      pAux.setName("mobileId");
      pAux.setValue("true");
      propertyList.getProperty().add(pAux);
      getSignatureManagerPort().addData(sigId, type, url.getBytes(), propertyList);      
    }
    catch (Exception ex)
    {
      throw new Exception("CAN_NOT_LINK_SIGNED_PDF");
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
}
