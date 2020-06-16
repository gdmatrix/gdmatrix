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
package org.santfeliu.web.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.CachedDocumentManagerClient;

import java.io.FileOutputStream;
import java.io.IOException;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.activation.FileDataSource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.matrix.dic.Property;
import org.matrix.doc.DocumentConstants;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.security.util.StringCipher;

import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.pdf.signature.PDFSigner;
import org.santfeliu.util.pdf.signature.DocumentInfo;
import org.santfeliu.util.pdf.signature.SignatureInfo;
import org.santfeliu.util.pdf.signature.SignatureLevel;

/**
 *
 * @author blanquepa
 * @author realor
 */
public class ScannerServlet extends HttpServlet
{
  private static final long DEFAULT_TIMEOUT_IN_SECONDS = 600;
  private static final String TOKEN_NOT_FOUND = "TOKEN_NOT_FOUND";
  private static final String INVALID_TOKEN = "INVALID_TOKEN";
  private static final String INVALID_SCAN = "INVALID_SCAN";
  private static final String DEFAULT_TITLE = "Scanned document";
  private static final String DEFAULT_DOCTYPE = "Document";

  public static String HEADERS_PREFIX = "scan_";
  public static final String TOKEN_PREFIX = "SCAN";
  public static final String TOKEN_HEADER = "_token";
  public static final String LOCATION_HEADER = "_signatureLocation";
  public static final String REASON_HEADER = "_signatureReason";
  public static final String VISIBLE_SIGNATURE_HEADER = "_visibleSignature";
  public static final String PDF_TITLE = "_pdfTitle";
  public static final String PDF_SUBJECT = "_pdfSubject";
  public static final String PDF_AUTHOR = "_pdfAuthor";
  public static final String PDF_KEYWORDS = "_pdfKeywords";
  public static final String TOKEN_PROPERTY = "_tokenProperty";

  public static final String KEY_STORE_PATH = "keyStorePath";
  public static final String KEY_STORE_TYPE = "keyStoreType";
  public static final String KEY_STORE_PASSWORD = "keyStorePassword";

  private static final String SCAN_TIME_GAP = "scanTimeGap";
  private static final String MEMORY_THRESHOLD = "memoryThreshold";  
  private static final String MAX_FILE_SIZE = "maxFileSize"; 
  private static final String MAX_REQUEST_SIZE = "maxReqyestSize"; 

  String keyStorePath = MatrixConfig.getPathProperty(
    "org.santfeliu.signature.certificate.tramitacio." + KEY_STORE_PATH);
  String keyStoreType = MatrixConfig.getProperty(
    "org.santfeliu.signature.certificate.tramitacio." + KEY_STORE_TYPE);
  String keyStorePassword = MatrixConfig.getProperty(
    "org.santfeliu.signature.certificate.tramitacio." + KEY_STORE_PASSWORD);
  String tsaUrl = MatrixConfig.getProperty("org.santfeliu.security.tspURL");

  //Multipart form-data constants
  public static final int DEFAULT_MEMORY_THRESHOLD = 1024 * 1024 * 3;  // 3MB
  public static final int DEFAULT_MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
  public static final int DEFAULT_MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 50MB

  private ScannedSource scannedSource;

  @Override
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
  }

  @Override
  protected void doGet(HttpServletRequest request, 
    HttpServletResponse response) throws ServletException, java.io.IOException
  {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, 
    HttpServletResponse response) throws ServletException, java.io.IOException
  {
    processRequest(request, response);
  }

  public void processRequest(HttpServletRequest request,
    HttpServletResponse response) throws IOException
  {
    try
    {
      scannedSource = new ScannedSource(getServletConfig(), request);      
      if (!scannedSource.existsToken())
        throw new Exception(TOKEN_NOT_FOUND);

      Token token = decryptToken(scannedSource.getToken());
      if (token.getPrefix() == null || !token.getPrefix().equals(TOKEN_PREFIX))
        throw new Exception(INVALID_TOKEN);

      if (isValidScan(token.getDateTime()))
      {
        InputStream is = scannedSource.getInputStream();
        File signedFile = null;
        try
        {
          //Sign document
          signedFile = File.createTempFile("sign", ".pdf");
          signDocument(is, signedFile, keyStorePath, 
            keyStorePassword.toCharArray(), scannedSource);

          //Upload to document manager
          Document document = uploadDocument(signedFile, 
            scannedSource.getDocProperties(), token.getCredentials());
          if (document != null)
          {
            System.out.println("Scanned in " + (System.currentTimeMillis() - 
              getScanTime(token.getDateTime())) + " milliseconds");
            response.addHeader("docId", document.getDocId());
          }
          else
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
              "DOCUMENT_NOT_CREATED");
        }
        finally
        {
          is.close();
          if (signedFile != null)
          {
            signedFile.delete();
          }
        }
      }
    }
    catch (Exception ex)
    {
      String message = ex.getMessage();
      if (message == null)
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
      else
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);

      ex.printStackTrace();
    }
  }

  public static String formatToken(String prefix, String dateTime, String credentials)
  {
    return prefix + ":" + dateTime + ":" + credentials;
  }

  private Token parseToken(String dcrToken)
  {
    if (dcrToken == null)
      return null;
    return new Token(dcrToken);
  }

  private Token decryptToken(String token)
  {
    String secret = MatrixConfig.getProperty(
      "org.santfeliu.security.urlCredentialsCipher.secret");
    StringCipher cipher = new StringCipher(secret);
    return parseToken(cipher.decrypt(token));
  }

  private boolean isValidScan(String tokenValue) throws Exception
  {
    long scanTime = getScanTime(tokenValue);
    long gap = DEFAULT_TIMEOUT_IN_SECONDS;
    String configGap = getServletConfig().getInitParameter(SCAN_TIME_GAP);
    if (configGap != null)
      gap = Long.parseLong(configGap);

    long now = System.currentTimeMillis();
    if (scanTime + (gap * 1000) < now) 
      throw new Exception(INVALID_SCAN);
    else
      return true;
  }

  private long getScanTime(String dt) throws ParseException
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    Date date = df.parse(dt);
    return date.getTime();
  }
  
  private void signDocument(InputStream src, File file, String ksPath,
    char[] ksPassword, ScannedSource source)
    throws Exception
  {
    //Get request headers
    String location = source.getParameter(LOCATION_HEADER);
    String reason = source.getParameter(REASON_HEADER);
    
    String visible = source.getParameter(VISIBLE_SIGNATURE_HEADER);  
    boolean visibleSignature
            = (visible != null && !visible.equalsIgnoreCase("false"));
    
    String pdfTitle = source.getParameter(PDF_TITLE);
    String pdfSubject = source.getParameter(PDF_SUBJECT);
    String pdfAuthor = source.getParameter(PDF_AUTHOR);
    String pdfKeywords = source.getParameter(PDF_KEYWORDS);

    // sign
    try (FileOutputStream fos = new FileOutputStream(file))
    {
      PDFSigner signer = PDFSigner.getInstance();
      DocumentInfo documentInfo = new DocumentInfo();
      documentInfo.setTitle(pdfTitle);
      documentInfo.setSubject(pdfSubject);
      documentInfo.setAuthor(pdfAuthor);
      documentInfo.setKeywords(pdfKeywords);
      
      SignatureInfo sigInfo = new SignatureInfo(location, reason);
      signer.setTsaUrl(tsaUrl);
      signer.sign(src, fos, ksPath, String.valueOf(ksPassword),
        SignatureLevel.LTA, null, null, sigInfo, documentInfo);      
    }    

  }
 
  private Document uploadDocument(File file, List<Property> properties, 
    Credentials credentials) throws FileNotFoundException, IOException
  {
    String userId = credentials != null ? credentials.getUserId() : 
      MatrixConfig.getProperty("adminCredentials.userId");
    String password = credentials != null ? credentials.getPassword() : 
      MatrixConfig.getProperty("adminCredentials.password");
    
    CachedDocumentManagerClient client =
      new CachedDocumentManagerClient(userId, password);

    Document document = new Document();
    document.setTitle(DEFAULT_TITLE);
    document.setDocTypeId(DEFAULT_DOCTYPE);
    document.setLanguage(DocumentConstants.UNIVERSAL_LANGUAGE);
    DataHandler dataHandler = new DataHandler(new FileDataSource(file));
    Content content = new Content();
    content.setData(dataHandler);
    content.setContentType("application/pdf");
    document.setContent(content);
    DictionaryUtils.setProperty(document, "sigId", UUID.randomUUID().toString());
    DictionaryUtils.setProperties(document, properties);
    return client.storeDocument(document);
  }
  
  private static class ScannedSource
  {
    private HttpServletRequest request;
    private FileItem fileItem;
    private Map<String,String> parameters;
    private String token;

    public ScannedSource(ServletConfig config, HttpServletRequest request) throws FileUploadException
    {
      this.request = request;      
      if (parameters == null)
        parameters = new HashMap();
      
      if (ServletFileUpload.isMultipartContent(request))      
      {
        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        String mem = config.getInitParameter(MEMORY_THRESHOLD);
        String maxFile = config.getInitParameter(MAX_FILE_SIZE);
        String maxRequest = config.getInitParameter(MAX_REQUEST_SIZE);
        // sets memory threshold - beyond which files are stored in disk
        factory.setSizeThreshold(mem != null ? Integer.valueOf(mem) : DEFAULT_MEMORY_THRESHOLD);
        // sets temporary location to store files
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);
        // sets maximum size of upload file
        upload.setFileSizeMax(maxFile != null ? Integer.valueOf(maxFile) : DEFAULT_MAX_FILE_SIZE);
        // sets maximum size of request (include file + form data)
        upload.setSizeMax(maxRequest != null ? Integer.valueOf(maxRequest) : DEFAULT_MAX_REQUEST_SIZE);

        // parses the request's content to extract file data
        List<FileItem> formItems = upload.parseRequest(request);
        if (formItems != null && formItems.size() > 0)
        {
          // iterates over form's fields
          for (FileItem item : formItems)
          {
            if (!item.isFormField())
              this.fileItem = item;
            else
            {
              String name = item.getFieldName();
              name = name.substring(HEADERS_PREFIX.length());
              String value = item.getString();
              parameters.put(name, value);
              if (name.equals(TOKEN_HEADER))
                token = value;
            }
          }
        }
      }
      else
      {
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements())
        {
          String headerName = (String) headerNames.nextElement();
          if (headerName.startsWith(HEADERS_PREFIX))
          {
            String header = request.getHeader(headerName);
            headerName = headerName.substring(HEADERS_PREFIX.length());
            parameters.put(headerName, header);
            if (headerName.equals(TOKEN_HEADER))
              token = header;            
          }
        }        
      }
    }

    public Map getParameters()
    {
      return parameters;
    }

    public String getParameter(String name)
    {
      String parameter = parameters.get(name);
      if (parameter == null && name.startsWith("_")) //if looking for a system property tries if it comes from request header (case insensitive)
        parameter = parameters.get(name.toLowerCase());
      return parameter;
    }

    public InputStream getInputStream() throws IOException
    {
      if (fileItem != null)
        return fileItem.getInputStream();
      else if (request != null)
        return request.getInputStream();
      else
        return null;
    }

    public String getToken()
    {
      return token;
    }
    
    public boolean existsToken()
    {
      return token != null;
    }
    
    public List<Property> getDocProperties()
    {
      List<Property> result = new ArrayList();
      Iterator<String> it = parameters.keySet().iterator();
      while(it.hasNext())
      {
        String headerName = (String)it.next();
        if (!headerName.startsWith("_"))
        {
          DictionaryUtils.addProperty(result, headerName, parameters.get(headerName));
        }
      }

      String tokenProperty = parameters.get(TOKEN_PROPERTY);
      if (tokenProperty != null && !tokenProperty.equalsIgnoreCase("false"))
      {
        DictionaryUtils.addProperty(result, tokenProperty, token);
      }      
      return result;
    }
  }

  class InputStreamDataSource implements DataSource
  {

    private InputStream inputStream;

    public InputStreamDataSource(InputStream inputStream)
    {
      this.inputStream = inputStream;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
      return new BufferedInputStream(inputStream);
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
      throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getContentType()
    {
      return "*/*";
    }

    @Override
    public String getName()
    {
      return "InputStreamDataSource";
    }
  }

  private class Token
  {
    private String prefix;
    private String dateTime;
    private String strCredentials;

    public Token(String strToken)
    {
      String[] parts = strToken.split(":");
      if (parts.length >= 2)
      {
        prefix = parts[0];
        dateTime = parts[1];
      }
      if (parts.length == 3)
      {
        String encCredentials = parts[2];
        String secret
                = MatrixConfig.getProperty("org.santfeliu.security.urlCredentialsCipher.secret");
        StringCipher cipher = new StringCipher(secret);
        strCredentials = cipher.decrypt(encCredentials);
      }
    }

    public String getPrefix()
    {
      return prefix;
    }

    public void setPrefix(String prefix)
    {
      this.prefix = prefix;
    }

    public String getDateTime()
    {
      return dateTime;
    }

    public void setDateTime(String dateTime)
    {
      this.dateTime = dateTime;
    }

    public Credentials getCredentials()
    {
      return SecurityUtils.getURLCredentialsCipher()
              .getCredentials(this.strCredentials);
    }
  } 
  
  

}
