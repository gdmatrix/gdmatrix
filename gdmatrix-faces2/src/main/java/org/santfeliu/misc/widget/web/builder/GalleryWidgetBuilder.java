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
package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.datalist.HtmlDataList;
import org.apache.myfaces.custom.div.Div;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.misc.gallery.web.GalleryBean;
import org.santfeliu.misc.gallery.web.GalleryItem;

/**
 *
 * @author blanquepa
 */
public class GalleryWidgetBuilder extends WidgetBuilder
{

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    Div containerDiv = new Div();

    Map properties = widgetDef.getProperties();
    if (properties != null)
    {
      String widgetId = widgetDef.getWidgetId();
      String caseId = (String)properties.get("caseId");
      String galleryMid = (String)properties.get("galleryMid");
      String imageWidth = (String)properties.get("imageWidth");
      String imageHeight = (String)properties.get("imageHeight");
      String galleryStyle = (String)properties.get("galleryStyle");
      String galleryStyleClass = (String)properties.get("galleryStyleClass");
      if (galleryStyleClass == null)
        galleryStyleClass = "gallery";
      String imagesPerPage = (String)properties.get("imagesPerPage");
      int pageSize = (imagesPerPage != null ? Integer.parseInt(imagesPerPage) : 9);
      try
      {
        containerDiv.setStyle(galleryStyle);
        containerDiv.setStyleClass(galleryStyleClass);

        Div dataListDiv = new Div();
        dataListDiv.setId(widgetId + "_dataListDiv");
        HtmlDataList dataList = new HtmlDataList();
        dataList.setId(widgetId + "_dataList");
        dataListDiv.getChildren().add(dataList);

        //Data list div
        String sortValue = (String)properties.get("sortImages");
        boolean sort = (sortValue == null ? true : sortValue.equals("true"));
        List<GalleryItem> items =
          GalleryBean.getGalleryItems(galleryMid, caseId, imageWidth,
          imageHeight, 0, 0, sort);
        List<GalleryItem> itemsAux = new ArrayList<GalleryItem>();
        itemsAux.addAll((List<GalleryItem>)items.subList(0, 
          pageSize >= items.size() ? items.size() : pageSize));
        dataList.setValue(itemsAux);
        dataList.setVar("item");

        HtmlOutputLink outputLink = new HtmlOutputLink();
        outputLink.setId(widgetId + "_action");
        UIComponentTagUtils.setValueBinding(context, outputLink, "value",
          "#{item.actionUrl}");
        outputLink.setStyleClass("thumbnail");

        HtmlGraphicImage graphicImage = new HtmlGraphicImage();
        UIComponentTagUtils.setValueBinding(context, graphicImage, "url",
          "#{item.thumbnailUrl}");
        graphicImage.setId(widgetId + "_image");
        //graphicImage.setTitle(caseId);
        UIComponentTagUtils.setValueBinding(context, graphicImage, "title",
          "#{galleryBean.itemDescription}");
        UIComponentTagUtils.setValueBinding(context, graphicImage, "alt",
          "#{galleryBean.itemDescription}");

        outputLink.getChildren().add(graphicImage);
        dataList.getChildren().add(outputLink);

        containerDiv.getChildren().add(dataListDiv);

      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    return containerDiv;
  }

}
