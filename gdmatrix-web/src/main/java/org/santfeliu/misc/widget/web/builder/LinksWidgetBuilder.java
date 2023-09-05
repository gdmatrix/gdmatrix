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
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.faces.links.HtmlLinks;

/**
 *
 * @author lopezrj-sf
 */
public class LinksWidgetBuilder extends WidgetBuilder
{
  private static final int DEFAULT_ROWS = 10;
  private static final int DEFAULT_MAX_SUMMARY_CHARS = Integer.MAX_VALUE;
  private static final boolean DEFAULT_RENDER_DESCRIPTION = true;

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    HtmlLinks component = new HtmlLinks();
    component.getAttributes().put("nodeId", widgetDef.getMid());
    Map properties = widgetDef.getProperties();
    if (properties != null)
    {
      //NodeId
      String nodeId = (String)properties.get("nodeId");
      if (isValueReference(nodeId))
      {
        UIComponentTagUtils.setValueBinding(context, component, "nodeId",
          nodeId);
      }
      else
      {
        component.setNodeId(nodeId);
      }

      //Translator
      setTranslationProperties(component, properties,
        getStrictTranslationGroup(widgetDef, "link"), context);

      //rows
      String rows = (String)properties.get("rows");
      if (rows != null)
        component.setRows(Integer.valueOf(rows));
      else
        component.setRows(DEFAULT_ROWS);

      //maxSummaryChars
      String maxDescriptionChars =
        (String)properties.get("maxDescriptionChars");
      if (maxDescriptionChars != null)
        component.setMaxDescriptionChars(Integer.valueOf(maxDescriptionChars));
      else
        component.setMaxDescriptionChars(DEFAULT_MAX_SUMMARY_CHARS);

      //style
      component.setStyle((String)properties.get("linksStyle"));
      String styleClass = (String)properties.get("linksStyleClass");
      if (styleClass == null) styleClass = "linksWidget";
      component.setStyleClass(styleClass);

      component.setLabelStyle((String)properties.get("labelStyle"));
      String labelStyleClass = (String)properties.get("labelStyleClass");
      if (labelStyleClass == null) labelStyleClass = "linkLabel";
      component.setLabelStyleClass(labelStyleClass);

      component.setImageStyle((String)properties.get("imageStyle"));
      String imageStyleClass = (String)properties.get("imageStyleClass");
      if (imageStyleClass == null)
        imageStyleClass = "linkImage";
      component.setImageStyleClass(imageStyleClass);

      component.setDescriptionStyle(
        (String)properties.get("descriptionStyle"));
      String descriptionStyleClass =
        (String)properties.get("descriptionStyleClass");
      if (descriptionStyleClass == null)
        descriptionStyleClass = "linkDescription";
      component.setDescriptionStyleClass(descriptionStyleClass);

      String row1StyleClass = (String)properties.get("row1StyleClass");
      if (row1StyleClass == null) row1StyleClass = "link1";
      component.setRow1StyleClass(row1StyleClass);

      String row2StyleClass = (String)properties.get("row2StyleClass");
      if (row2StyleClass == null) row2StyleClass = "link2";
      component.setRow2StyleClass(row2StyleClass);

      //image dimensions
      String imageWidth = (String)properties.get("imageWidth");
      if (imageWidth != null)
        component.setImageWidth(imageWidth);
      String imageHeight = (String)properties.get("imageHeight");
      if (imageHeight != null)
        component.setImageHeight(imageHeight);
      String imageCrop = (String)properties.get("imageCrop");
      if (imageCrop != null)
        component.setImageCrop(imageCrop);

      String renderDescription = (String)properties.get("renderDescription");
      if (renderDescription != null)
        component.setRenderDescription(Boolean.valueOf(renderDescription));
      else
        component.setRenderDescription(DEFAULT_RENDER_DESCRIPTION);
    }

    return component;
  }

}
