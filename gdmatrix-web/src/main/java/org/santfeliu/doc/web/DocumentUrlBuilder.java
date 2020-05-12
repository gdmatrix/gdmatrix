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
