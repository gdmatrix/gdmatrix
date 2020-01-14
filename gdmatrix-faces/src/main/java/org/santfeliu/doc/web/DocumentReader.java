package org.santfeliu.doc.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;
import org.matrix.security.SecurityConstants;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.doc.DocumentCache;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.doc.transform.TransformationRequest;
import org.santfeliu.doc.transform.TransformationManager;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.Utilities;
import org.santfeliu.web.UserSessionBean;


public class DocumentReader
{
  public static final String VERSION_PARAM = "version";
  public static final String MARKUP_PARAM = "markup";
  public static final String SAVEAS_PARAM = "saveas";
  public static final String LANGUAGE_PARAM = "language";
  public static final String CACHE_PARAM = "cache";
  public static final String TRANSFORM_TO_PARAM = "transform-to";
  public static final String TRANSFORM_WITH_PARAM = "transform-with";

  private long defaultCacheTime = 10000; // 10 seconds

  public DocumentReader(long defaultCacheTime)
  {
    this.defaultCacheTime = defaultCacheTime;
  }

  public void processRequest(HttpServletRequest request,
                             HttpServletResponse response,
                             String servletInfo)
    throws ServletException, IOException
  {
    DocumentRequest docReq = null;
    try
    {
      docReq = parseRequest(request);
    }
    catch (Exception ex)
    {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (docReq == null)
    {
      writeServletInfo(response, servletInfo);
    }
    else
    {
      try
      {
        processDocumentRequest(docReq, response);
      }
      catch (Exception ex)
      {
        if (!response.isCommitted())
        {
          String msg = ex.getMessage();
          if (msg != null && (msg.contains("FILE_NOT_FOUND") ||
                              msg.contains("DOCUMENT_NOT_FOUND")))
          {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
          }
          else if (msg != null && msg.contains("ACTION_DENIED"))
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
          else
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
      }
    }
  }

  private DocumentRequest parseRequest(HttpServletRequest request)
    throws Exception
  {
    String contextPath = request.getContextPath();
    String servletPath = request.getServletPath();
    String uri = request.getRequestURI();
    int semicolon = uri.indexOf(";"); //aviod jsession
    if (semicolon > 0)
      uri = uri.substring(0, semicolon);
    String ref = uri.substring((contextPath + servletPath).length());
    if (ref.length() == 0) return null; // show info

    ref = ref.substring(1);

    // This line is for compatibility with old URLs. Remove in the future.
    if (ref.startsWith("document/")) ref = ref.substring(9);

    DocumentRequest docReq = new DocumentRequest();
    int index = ref.indexOf("/");
    String code = null;
    if (index == -1) // no filename specified
    {
      code = ref;
    }
    else
    {
      code = ref.substring(0, index);
      docReq.filename = ref.substring(index + 1);
    }

    //Headers
    docReq.modifiedSince = request.getDateHeader("If-Modified-Since");
    docReq.etag = request.getHeader("If-None-Match");

    //Parameters
    String transformTo = request.getParameter(TRANSFORM_TO_PARAM);
    if (transformTo != null)
    {
      docReq.transform = new TransformationRequest();
      if (transformTo.startsWith("fmt/") || transformTo.startsWith("x-fmt/"))
      {
        docReq.transform.setTargetFormatId(transformTo);
      }
      else // it is a content-type
      {
        docReq.transform.setTargetContentType(transformTo);
      }
      docReq.transform.setOptions(getOptions(request));
    }
    String transformWith = request.getParameter(TRANSFORM_WITH_PARAM);
    if (transformWith != null)
    {
      docReq.transform = new TransformationRequest();
      int pos = transformWith.indexOf("/");
      if (pos == -1)
      {
        docReq.transform.setTransformerId(transformWith);
      }
      else
      {
        String transformerId = transformWith.substring(0, pos);
        String transformationName = transformWith.substring(pos + 1);
        if (transformerId.length() == 0) transformerId = null;
        if (transformationName.length() == 0) transformationName = null;
        docReq.transform.setTransformerId(transformerId);
        docReq.transform.setTransformationName(transformationName);
      }
      docReq.transform.setOptions(getOptions(request));
    }

    // set markup option
    docReq.markup = request.getParameter(MARKUP_PARAM);
    // attachment
    docReq.saveAs = request.getParameter(SAVEAS_PARAM);

    // set code
    if (Utilities.isUUID(code))
    {
      // contentId
      docReq.contentId = code;
    }
    else
    {
      docReq.docId = code;
      docReq.language = request.getParameter(LANGUAGE_PARAM) != null ?
        request.getParameter(LANGUAGE_PARAM) :
        request.getLocale().getLanguage();

      if (request.getParameter(VERSION_PARAM) == null)
        docReq.version = DocumentConstants.LAST_VERSION;
      else
        docReq.version = Integer.parseInt(request.getParameter(VERSION_PARAM));

      String cacheTime = request.getParameter(CACHE_PARAM);
      if (cacheTime == null)
        docReq.cacheTime = defaultCacheTime;
      else
        docReq.cacheTime = Long.parseLong(cacheTime);

      Credentials credentials = SecurityUtils.getCredentials(request, false);
      if (credentials == null)
      {
        credentials = UserSessionBean.getCredentials(request);
      }
      docReq.username = credentials.getUserId();
      docReq.password = credentials.getPassword();
    }
    return docReq;
  }

  private void processDocumentRequest(DocumentRequest docReq,
    HttpServletResponse response) throws Exception
  {
    DataHandler dataHandler = null;

    CachedDocumentManagerClient client =
      getDocumentManagerClient(docReq.username, docReq.password);

    if (docReq.transform != null)
    {
      if (docReq.docId != null) // Transform document
      {
        // load all document metadata, but not content
        Document document = client.loadDocument(
          docReq.docId, docReq.version, ContentInfo.METADATA);

        String relName = docReq.transform.toString();
        RelatedDocument trDoc = DocumentUtils.getRelatedDocument(
          document, RelationType.TRANSFORMATION, relName);
        if (trDoc != null) // Transform was found as related document
        {
          document = client.loadDocument(trDoc.getDocId(), trDoc.getVersion());
          Content content = document.getContent();
          dataHandler = content.getData();
          response.setContentType(content.getContentType());
        }
        else
        {
          Content content = document.getContent();
          content = client.loadContent(content.getContentId());
          document.setContent(content);
          dataHandler =
            TransformationManager.transform(document, docReq.transform);
          response.setContentType(dataHandler.getContentType());
        }
      }
      else // Transform content
      {
        Content content = client.loadContent(docReq.contentId);
        dataHandler =
          TransformationManager.transform(content, docReq.transform);
        response.setContentType(dataHandler.getContentType());
      }
      // cache result for few seconds to avoid double request
      response.setDateHeader("Expires", System.currentTimeMillis() + 3000);      
    }
    else // Send content
    {
      if (docReq.contentId == null)
      {
        // FIX: docReq.version is ignored!!!
        docReq.contentId = DocumentCache.getDocument(docReq.docId,
          docReq.language, docReq.username, docReq.password, docReq.cacheTime);
      }

      // Call to WS and get content
      if (docReq.markup != null)
      {
        // Markup
        dataHandler = client.markupContent(docReq.contentId, docReq.markup);
        response.setContentType("text/html");
      }
      else
      {
        // Static content
        if (docReq.contentId.equals(docReq.etag) && docReq.saveAs == null)
        {
          response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
          return;
        }

        String docLanguage = TranslationConstants.UNIVERSAL_LANGUAGE;
        Content content = client.loadContent(docReq.contentId);
        dataHandler = content.getData();
        if (dataHandler != null)
        {
          // Internal content
          // set ETag
          response.setHeader("ETag", docReq.contentId);

          if (content.getLanguage() != null)
            docLanguage = content.getLanguage();

          // set Last-Modified
          SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
          Date date = df.parse(content.getCaptureDateTime());
          long lastModified = date.getTime();
          if (docReq.modifiedSince == lastModified && docReq.saveAs == null)
          {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
          }
          // set Last-Modified
          response.setDateHeader("Last-Modified", lastModified);
          // set Content-Length
          long size = content.getSize();
          response.setContentLength((int)size);
          // set Content-Language
          if (!TranslationConstants.UNIVERSAL_LANGUAGE.equals(docLanguage))
          {
            response.setLocale(new Locale(docLanguage));
          }
          // set Content-Type
          response.setContentType(content.getContentType());
        }
        else
        {
          // External content
          String url = content.getUrl();
          if (url != null) response.sendRedirect(url);
        }
      }
    }

    if (dataHandler != null)
    {
      // set Date
      response.setDateHeader("Date", System.currentTimeMillis());
      // set Content-Disposition if needed
      setContentDispositionHeader(docReq, response);
      // write to response
      IOUtils.writeToStream(dataHandler, response.getOutputStream());
    }
  }

  private void setContentDispositionHeader(DocumentRequest docReq, HttpServletResponse response)
  {
    String contentType = response.getContentType();
    if (docReq.saveAs == null && contentType != null &&
        (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
         contentType.equals("application/tcq")))
    {
      String filename =
        docReq.filename != null ? docReq.filename : docReq.contentId + "." +
          MimeTypeMap.getMimeTypeMap().getExtension(contentType);
      docReq.saveAs = filename;
    }

    if (docReq.saveAs != null) response.addHeader("Content-Disposition",
        "attachment; filename=" + docReq.saveAs);
  }

  private CachedDocumentManagerClient getDocumentManagerClient(String username,
    String password) throws Exception
  {
    CachedDocumentManagerClient client =
      new CachedDocumentManagerClient(username, password);

    return client;
  }

  private void writeServletInfo(HttpServletResponse response,
    String servletInfo) throws IOException
  {
    PrintWriter writer = response.getWriter();
    writer.print("<html><body><p>");
    writer.print(servletInfo);
    writer.print("</p></body></html>");
  }

  private Map getOptions(HttpServletRequest request)
  {
    HashMap options = new HashMap();
    Enumeration enu = request.getParameterNames();
    while (enu.hasMoreElements())
    {
      String paramName = (String)enu.nextElement();
      if (!paramName.equals(TRANSFORM_TO_PARAM) &&
        !paramName.equals(TRANSFORM_WITH_PARAM) &&
        !paramName.equals(DocumentReader.LANGUAGE_PARAM) &&
        !paramName.equals(DocumentReader.VERSION_PARAM) &&
        !paramName.equals(SecurityConstants.USERID_PARAMETER) &&
        !paramName.equals(SecurityConstants.PASSWORD_PARAMETER))
      {
        options.put(paramName, request.getParameter(paramName));
      }
    }
    return options;
  }

  // encapsulates the parameters of a document request
  class DocumentRequest
  {
    String contentId;
    String docId;
    int version;
    String language;
    long cacheTime;
    long time = 0;
    String username;
    String password;
    String markup;
    long modifiedSince;
    String etag;
    String saveAs;
    String filename;
    TransformationRequest transform;

    public String getCacheKey()
    {
      if (docId == null) return null;
      return docId + "/" + language + "/" + version;
    }

    @Override
    public String toString()
    {
      StringBuilder buffer = new StringBuilder();
      buffer.append("UUID=[").append(contentId);
      buffer.append("] docId=[").append(docId);
      buffer.append("] version=[").append(version);
      buffer.append("] cacheTime=[").append(cacheTime);
      buffer.append("] username=[").append(username);
      buffer.append("]");
      return buffer.toString();
    }
  }
}
