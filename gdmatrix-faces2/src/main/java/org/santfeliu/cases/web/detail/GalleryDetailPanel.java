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
package org.santfeliu.cases.web.detail;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.CaseDocumentView;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.misc.gallery.web.GalleryBean;
import org.santfeliu.misc.gallery.web.GalleryItem;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.DetailPanel;

/**
 *
 * @author blanquepa
 */
public class GalleryDetailPanel extends DetailPanel
{
  List<GalleryItem> items;
  private static final String GALLERY_MID = "galleryMid";
  private static final String IMAGE_WIDTH = "imageWidth";
  private static final String IMAGE_HEIGHT = "imageHeight";
  private static final String THUMBNAIL_WIDTH = "thumbnailWidth";
  private static final String THUMBNAIL_HEIGHT = "thumbnailHeight";
  private static final String THUMBNAIL_HOVER_MODE = "thumbnailHoverMode";
  private static final String SORT_VALUE = "sortValue";
  private static final String CASE_DOCUMENT_TYPE_PROPERTY = "caseDocumentTypeId";
  private static final String PROPERTY_NAME = "propertyName";
  private static final String PROPERTY_VALUE = "propertyValue";
  private static final String CONTINUE_TIME = "continueTime";
  private static final String TRANSITION_TIME = "transitionTime";
  private static final String MAIN_IMAGE_CLICK_MODE = "mainImageClickMode";
  private static final String RENDER_MAIN_IMAGE = "renderMainImage";
  private static final String RENDER_NAV_LINKS = "renderNavLinks";
  private static final String RENDER_THUMBNAILS = "renderThumbnails";
  private static final String THUMBNAIL_CLICK_MODE = "thumbnailClickMode";
  private static final String THUMBNAIL_SHIFT_MODE = "thumbnailShiftMode";
  private static final String THUMBNAIL_COUNT = "thumbnailCount";
  private static final String THUMBNAIL_WINDOW = "thumbnailWindow";
  private static final String THUMBNAIL_PREV_LABEL = "thumbnailPrevLabel";
  private static final String THUMBNAIL_NEXT_LABEL = "thumbnailNextLabel";
  private static final String THUMBNAIL_PREV_ICON_URL = "thumbnailPrevIconUrl";
  private static final String THUMBNAIL_NEXT_ICON_URL =  "thumbnailNextIconUrl";


  @Override
  public void loadData(DetailBean detailBean)
  {
    try
    {
      this.items = new ArrayList();
      String caseId = ((CaseDetailBean) detailBean).getCaseId();
      String galleryMid = getProperty(GALLERY_MID);
      String imageWidth = getProperty(IMAGE_WIDTH);
      String imageHeight = getProperty(IMAGE_HEIGHT);
      String sortValue = getProperty(SORT_VALUE);
      String allowedTypeId = getProperty(CASE_DOCUMENT_TYPE_PROPERTY);
      String docPropName = getProperty(PROPERTY_NAME);
      String docPropValue = getProperty(PROPERTY_VALUE);
      
      boolean sort = (sortValue == null ? true : sortValue.equals("true"));

      List<GalleryItem> list =
        GalleryBean.getGalleryItems(galleryMid, caseId, imageWidth,
        imageHeight, 0, 0, sort);

      if (!StringUtils.isBlank(allowedTypeId))
      {
        for (GalleryItem item : list)
        {
          if (allowedTypeId.equals(item.getView().getCaseDocTypeId()))
            this.items.add(item);
        }
      }
      else if (!StringUtils.isBlank(docPropName)
        && !StringUtils.isBlank(docPropValue))
      {
        for (GalleryItem item : list)
        {
          Document document = DocumentConfigBean.getClient().loadDocument(
            item.getDocument().getDocId(), 0, ContentInfo.METADATA);

          Property property = DictionaryUtils.getProperty(document, docPropName);
          if (property != null && property.getValue() != null &&
            property.getValue().contains(docPropValue))
          {
            this.items.add(item);
          }
        }
      }
      else
      {
        this.items.addAll(list);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public List<GalleryItem> getItems()
  {
    return items;
  }

  public void setItems(List<GalleryItem> items)
  {
    this.items = items;
  }

  public List<String> getImageIds()
  {
    List ids = new ArrayList();
    if (isRenderContent())
    {
      for (GalleryItem item : items)
      {
        CaseDocumentView view = item.getView();
        if (view != null)
        {
          Document document = view.getDocument();
          if (document != null)
          {
            Content content = document.getContent();
            if (content != null && content.getContentId() != null)
              ids.add(content.getContentId());
          }  
        }
      }
    }
    return ids;
  }

  public String getThumbnailWidth()
  {
    String width = getProperty(THUMBNAIL_WIDTH);
    return width != null ? width : "50";
  }

  public String getThumbnailHeight()
  {
    String height = getProperty(THUMBNAIL_HEIGHT);
    return height != null ? height : "50";
  }

  public String getThumbnailHoverMode()
  {
    String property = getProperty(THUMBNAIL_HOVER_MODE);
    return property != null ? property : "none";
  }

  public String getImageWidth()
  {
    String width = getProperty(IMAGE_WIDTH);
    return width != null ? width : "0";
  }

  public String getImageHeight()
  {
    String height = getProperty(IMAGE_HEIGHT);
    return height != null ? height : "0";
  }

  public String getContinueTime()
  {
    return getProperty(CONTINUE_TIME);
  }

  public String getTransitionTime()
  {
    return getProperty(TRANSITION_TIME);
  }

  public String getMainImageClickMode()
  {
    return getProperty(MAIN_IMAGE_CLICK_MODE);
  }

  public boolean getRenderMainImage()
  {
    String render = getProperty(RENDER_MAIN_IMAGE);
    return render != null ? Boolean.parseBoolean(render) : true;
  }

  public boolean getRenderNavLinks()
  {
    String render = getProperty(RENDER_NAV_LINKS);
    return render != null ? Boolean.parseBoolean(render) : true;
  }

  public boolean getRenderThumbnails()
  {
    String render = getProperty(RENDER_THUMBNAILS);
    return render != null ? Boolean.parseBoolean(render) : true;
  }

  public String getThumbnailShiftMode()
  {
    return getProperty(THUMBNAIL_SHIFT_MODE);
  }

  public String getThumbnailClickMode()
  {
    return getProperty(THUMBNAIL_CLICK_MODE);
  }

  public String getThumbnailWindow()
  {
    return getProperty(THUMBNAIL_WINDOW);    
  }

  public String getThumbnailCount()
  {
    return getProperty(THUMBNAIL_COUNT);    
  }

  public String getThumbnailPrevLabel()
  {
    String property = getProperty(THUMBNAIL_PREV_LABEL);
    return property != null ? property : "Mostrar miniatura anterior";
  }

  public String getThumbnailNextLabel()
  {
    String property = getProperty(THUMBNAIL_NEXT_LABEL);
    return property != null ? property : "Mostrar miniatura següent";
  }

  public String getThumbnailPrevIconUrl()
  {
    return getProperty(THUMBNAIL_PREV_ICON_URL);
  }

  public String getThumbnailNextIconUrl()
  {
    return getProperty(THUMBNAIL_NEXT_ICON_URL);
  }

  @Override
  public boolean isRenderContent()
  {
    return items != null && !items.isEmpty();
  }

  @Override
  public String getType()
  {
    return "gallery";
  }

}
