package org.santfeliu.doc.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.faces.context.FacesContext;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.servlet.WorkServlet;

/**
 *
 * @author realor
 */
public class DocumentUrlBuilder
{
  public static final String TRANSFORM_PROPERTY = "transform#";
  public static final String DOC_SERVLET_URL = "/documents/";
  public static final String WORK_PREFIX = "work:";

  public static String getDocumentUrl(Document document)
  {
    return getDocumentUrl(document, false);
  }

  public static String getDocumentUrl(Document document, boolean downloadable)
  {
    String url = "";
    if (document != null)
    {
      String contextPath = getContextPath();
      String title = document.getTitle();
      if (title == null)
      {
        title = "";
      }
      Content content = document.getContent();
      if (content != null)
      {
        String mimeType = content.getContentType();
        String transformation = getTransformation(mimeType);
        if (transformation == null || downloadable)
        {
          String contentId = content.getContentId();
          String extension =
            MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
          String filename = DocumentUtils.getFilename(title) + "." + extension;
          url = contextPath + DOC_SERVLET_URL + contentId + "/" + filename;
          if (downloadable) 
            url += "?" + DocumentReader.SAVEAS_PARAM + "=" + filename;
        }
        else // transformation
        {
          String docId = document.getDocId();
          int version = document.getVersion();
          String work;
          if (transformation.startsWith(WORK_PREFIX))
          {
            work = WorkServlet.URL_PATTERN;
            transformation = transformation.substring(WORK_PREFIX.length());
          }
          else
          {
            work = "";
          }
          url = contextPath + work + DOC_SERVLET_URL +
            docId + "?" + DocumentReader.VERSION_PARAM + "=" + version +
            "&" + transformation;
        }
      }
    }
    return url;
  }
  
  public static String getMarkupUrl(Document document, String searchExpression)
  {
    String url = "";
    if (document != null && searchExpression != null 
      && searchExpression.trim().length() > 0)
    {
      String contextPath = getContextPath();
      String title = document.getTitle();
      if (title == null)
      {
        title = "";
      }
      Content content = document.getContent();
      if (content != null)
      {
        String uuid = content.getContentId();
        String filename = DocumentUtils.getFilename(title) + ".html";
        try
        {
          searchExpression = URLEncoder.encode(searchExpression, "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
        }
        url = contextPath + DOC_SERVLET_URL + uuid + "/" + filename +
          "?" + DocumentReader.MARKUP_PARAM + "=" + searchExpression + "#ctx1";
      }
      return url;
    }
    return null;
  }
  
  public static String getProtectedDocumentUrl(Document document)
  {
    String url = "";
    if (document != null)
    {
      String contextPath = getContextPath();
      String title = document.getTitle();
      if (title == null)
      {
        title = "";
      }
      Content content = document.getContent();
      if (content != null)
      {
        String mimeType = content.getContentType();        
        String extension =
          MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
        String filename = DocumentUtils.getFilename(title) + "." + extension;
          
        String docId = document.getDocId();
        url = contextPath + DOC_SERVLET_URL +
          docId + "/" + filename;
      }
    }
    return url;  
  }

  private static String getContextPath()
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    return facesContext.getExternalContext().getRequestContextPath();
  }

  private static String getTransformation(String mimeType)
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String propertyName = TRANSFORM_PROPERTY + mimeType;
    return userSessionBean.getSelectedMenuItem().getProperty(propertyName);
  }
}
