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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
@CMSManagedBean
public class GalleryBean extends WebBean implements Serializable
{
  @CMSProperty @Deprecated
  public static final String HEADER_DOCUMENT_PROPERTY = "header.document";
  @CMSProperty @Deprecated
  public static final String FOOTER_DOCUMENT_PROPERTY = "footer.document";
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String PAGE_SIZE_PROPERTY = "pageSize";
  @CMSProperty(mandatory=true)
  public static final String CASEID_PROPERTY = "caseId";
  @CMSProperty
  public static final String IMAGE_WIDTH_PROPERTY = "imageWidth";
  @CMSProperty
  public static final String IMAGE_HEIGHT_PROPERTY = "imageHeight";
  @CMSProperty
  public static final String SORT_IMAGES_PROPERTY = "sortImages";

  static final String IMAGE_SERVLET_URL = "/imgscale/";
  static final String DOC_SERVLET_URL = "/documents/";

  private static final int DEFAULT_PAGE_SIZE = 20;
  private transient HtmlBrowser headerBrowser = new HtmlBrowser();
  private transient HtmlBrowser footerBrowser = new HtmlBrowser();

  private String imageWidth;
  private String imageHeight;
  private String caseId;
  private List<GalleryItem> items;
  private int currentIndex;

  public GalleryBean()
  {
  }

  //Accessors
  public int getCurrentIndex()
  {
    return currentIndex;
  }

  public void setCurrentIndex(int firstIndex)
  {
    this.currentIndex = firstIndex;
  }

  public String getImageHeight()
  {
    return imageHeight;
  }

  public void setImageHeight(String imageHeight)
  {
    this.imageHeight = imageHeight;
  }

  public String getImageWidth()
  {
    return imageWidth;
  }

  public void setImageWidth(String imageWidth)
  {
    this.imageWidth = imageWidth;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public void setItems(List<GalleryItem> items)
  {
    this.items = items;
  }

  public List<GalleryItem> getItems() throws Exception
  {
    return items;
  }

  public HtmlBrowser getHeaderBrowser()
  {
    if (this.headerBrowser == null)
    {
      this.headerBrowser = new HtmlBrowser();
    }

    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(HEADER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(HEADER_DOCUMENT_PROPERTY);

    if (docId != null)
    {
      headerBrowser.setUrl(getContextPath() + DOC_SERVLET_URL + docId);
      return headerBrowser;
    }
    else
    {
      return null;
    }
  }

  public void setHeaderBrowser(HtmlBrowser headerBrowser)
  {
    this.headerBrowser = headerBrowser;
  }

  public HtmlBrowser getFooterBrowser()
  {
    if (this.footerBrowser == null)
    {
      this.footerBrowser = new HtmlBrowser();
    }

    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(FOOTER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(FOOTER_DOCUMENT_PROPERTY);

    if (docId != null)
    {
      footerBrowser.setUrl(DOC_SERVLET_URL + docId);
      return footerBrowser;
    }
    else
    {
      return null;
    }
  }

  public void setFooterBrowser(HtmlBrowser footerBrowser)
  {
    this.footerBrowser = footerBrowser;
  }

  public int getPageSize()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String pageSize =
      (String) mic.getDirectProperties().get(PAGE_SIZE_PROPERTY);
    return (pageSize != null ? Integer.parseInt(pageSize) : DEFAULT_PAGE_SIZE);
  }

  //Actions
  @CMSAction
  public String searchImages() throws Exception
  {
    //Get items
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String nCaseId = (String) mic.getDirectProperties().get(CASEID_PROPERTY);

    if (items == null || !nCaseId.equals(caseId))
    {
      caseId = nCaseId;
      imageWidth = (String) mic.getDirectProperties().get(IMAGE_WIDTH_PROPERTY);
      imageHeight = (String) mic.getDirectProperties().get(
        IMAGE_HEIGHT_PROPERTY);
      String sortValue = (String) mic.getDirectProperties().get(
        SORT_IMAGES_PROPERTY);
      boolean sort = (sortValue == null ? true : sortValue.equals("true"));
      items = getGalleryItems(mic.getMid(), caseId, imageWidth, imageHeight,
        sort);
    }

    Map requestParameters = getExternalContext().getRequestParameterMap();
    String index = (String)requestParameters.get("index");
    if (index != null)
    {
      int i = Integer.parseInt(index);
      if (i < 0) i = 0;
      else if (i >= items.size()) i = items.size() - 1;
      currentIndex = i;
      return "gallery_image";
    }
    else
    {
      String docId = (String)requestParameters.get("docid");
      if (docId != null)
      {
        int i = 0;
        for (GalleryItem item : items)
        {
          if (docId.equals(item.getDocId()))
          {
            currentIndex = i;
            return "gallery_image";
          }
          i++;
        }
      }
    }

    return "gallery_thumbnails";
  }

  public String showImage()
  {
    GalleryItem currentItem = (GalleryItem)getValue("#{item}");
    currentIndex = currentItem.getIndex();
    return "gallery_image";
  }

  public String showThumbnails()
  {
    return "gallery_thumbnails";
  }

  public String getItemDescription()
  {
    GalleryItem item = (GalleryItem)getValue("#{item}");
    String comments = item.getView().getComments();
    if (comments != null)
    {
      int i = comments.indexOf("$");
      if (i < 0) return comments;
      else return comments.substring(i + 1);
    }
    return comments;
  }
  
  public String getItemDefaultDescription()
  {
    GalleryItem item = (GalleryItem)getValue("#{item}");
    String docId = item.getView().getDocument().getDocId();
    return "Mostrar imatge " + docId;
  }

  public static List<GalleryItem> getGalleryItems(String mid, String caseId,
    String imageWidth, String imageHeight, int firstResult, int maxResults,
    boolean sort) throws Exception
  {    
    List<GalleryItem> items = new ArrayList();

    if (caseId == null) return items;

    CaseDocumentFilter filter = new CaseDocumentFilter();
    filter.setCaseId(caseId);
    filter.setFirstResult(firstResult);
    filter.setMaxResults(maxResults);

    List<CaseDocumentView> views =
      CaseConfigBean.getPort().findCaseDocumentViews(filter);

    for (CaseDocumentView view : views)
    {
      int index = items.size();
      GalleryItem item = new GalleryItem();
      item.setView(view);
      item.setIndex(index);
      item.setMid(mid);
      if (imageWidth != null)
        item.setWidth(imageWidth != null ? Integer.parseInt(imageWidth) : 0);
      if (imageHeight != null)
        item.setHeight(imageHeight != null ? Integer.parseInt(imageHeight) : 0);
      if (item.isImage())
        items.add(item);
    }

    if (sort)
    {
      for (GalleryItem item : items)
      {
        item.setIndex(getItemIndex(item));
      }
      Collections.sort(items, new Comparator()
      {
        public int compare(Object o1, Object o2)
        {
          GalleryItem item1 = (GalleryItem)o1;
          GalleryItem item2 = (GalleryItem)o2;
          return item2.getIndex() - item1.getIndex();
        }
      });

      int i = 0;
      for (GalleryItem item : items)
      {
        item.setIndex(i++);
      }
    }
    return items;
  }

  public static List<GalleryItem> getGalleryItems(String mid, String caseId,
    String imageWidth, String imageHeight, boolean sort) throws Exception
  {
    return getGalleryItems(mid, caseId, imageWidth, imageHeight, 0, 0, sort);
  }

  public static GalleryItem getGalleryItem(String caseId,
    String imageWidth, String imageHeight, String caseDocTypeId)
  {
    try
    {
      List<GalleryItem> items = getGalleryItems(null, caseId, imageWidth,
        imageHeight, true);
      if (items != null && items.size() > 0)
      {
        for (GalleryItem item : items)
        {
          if (caseDocTypeId != null && caseDocTypeId.equals(
            item.getView().getCaseDocTypeId()))
            return item;
        }
      }
    }
    catch (Exception ex)
    {
    }

    return null;
  }

  private static int getItemIndex(GalleryItem item)
  {
    String comments = item.getView().getComments();
    if (comments != null)
    {
      int i = comments.indexOf("$");
      if (i >= 0)
      {
        try
        {
          return Integer.valueOf(comments.substring(0, i));
        }
        catch (Exception ex)
        {
        }
      }
    }
    return Integer.MAX_VALUE;
  }
}
