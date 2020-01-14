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
package org.santfeliu.misc.gallery.web;

import java.io.Serializable;
import org.matrix.cases.CaseDocumentView;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.santfeliu.doc.util.DocumentUtils;

/**
 *
 * @author blanquepa
 */
public class GalleryItem implements Serializable
{
  private CaseDocumentView view;
  private String mid;
  private int index;
  private int width;
  private int height;

  public int getIndex()
  {
    return index;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public String getMid()
  {
    return mid;
  }

  public void setMid(String mid)
  {
    this.mid = mid;
  }

  public CaseDocumentView getView()
  {
    return view;
  }

  public void setView(CaseDocumentView view)
  {
    this.view = view;
  }

  public int getHeight()
  {
    return height;
  }

  public void setHeight(int height)
  {
    this.height = height;
  }

  public int getWidth()
  {
    return width;
  }

  public void setWidth(int width)
  {
    this.width = width;
  }

  public Document getDocument()
  {
    Document document = null;
    if (view != null) document = view.getDocument();

    return document;
  }

  public Content getContent()
  {
    Content content = null;
    Document document = getDocument();
    if (document != null)
      content = document.getContent();

    return content;
  }

  public String getDocId()
  {
    String docId = null;
    Document document = getDocument();
    if (document != null) docId =  document.getDocId();

    return docId;
  }

  public String getActionUrl()
  {
    if (mid != null)
      return "go.faces?xmid=" + mid + "&docid=" + getDocId();
    else
      return null;
  }

  public String getThumbnailUrl()
  {
    return getURL(true) + "?width=" + width  + "&height=" + height + "&crop=auto";
  }

  public String getImageUrl()
  {
    return getURL(false);
  }

  public String getFilename()
  {
    String filename = "";
    Document document = getDocument();
    if (document != null)
    {
      Content content = document.getContent();
      filename = DocumentUtils.getFilename(
        document.getTitle(), content.getContentType());
    }
    return filename;
  }

  public boolean isImage()
  {
    Content content = getContent();
    if (content != null)
      return (content.getContentType() != null &&
        content.getContentType().startsWith("image"));
    else
      return false;
  }

  private String getURL(boolean scaled)
  {
    String url = "";

    Document document = getDocument();
    if (document != null)
    {
      Content content = getDocument().getContent();
      if (content != null)
      {
        String filename =
          DocumentUtils.getFilename(document.getTitle(), content.getContentType());
        String servlet =
          scaled ? GalleryBean.IMAGE_SERVLET_URL : GalleryBean.DOC_SERVLET_URL;
        url =  servlet + content.getContentId() + "/" + filename;
      }
    }

    return url;
  }
}
