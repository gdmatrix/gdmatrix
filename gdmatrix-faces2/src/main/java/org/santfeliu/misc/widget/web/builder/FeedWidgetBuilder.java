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

import java.util.List;
import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.feed.faces.HtmlFeed;

/**
 *
 * @author lopezrj
 */

public class FeedWidgetBuilder extends WidgetBuilder
{
  public FeedWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    HtmlFeed component = new HtmlFeed();
    
    component.getAttributes().put("nodeId", widgetDef.getMid());

    Map properties = widgetDef.getProperties();
    if (properties != null)
    {
      //Url
      String url = (String)properties.get("url");
      if (isValueReference(url))
        UIComponentTagUtils.setValueBinding(context, component, "url", url);
      else
      {
        if (url != null) component.setUrl(url);
      }

      //Translator
      setTranslationProperties(component, properties, 
        getStrictTranslationGroup(widgetDef, "feed"), context);      

      String enableTranslation = (String)properties.get("enableTranslation");
      if (enableTranslation == null) enableTranslation = "false";
      component.setEnableTranslation(Boolean.valueOf(enableTranslation));      
      
      //rows
      String rows = (String)properties.get("rows");
      if (rows == null) rows = "5";
      component.setRows(Integer.valueOf(rows).intValue());

      //source
      String source = (String)properties.get("source");
      if (source == null) source = "Feed";
      component.setSource(source);
      
      //style
      component.setStyle((String)properties.get("style"));      
      String styleClass = (String)properties.get("styleClass");
      if (styleClass == null) styleClass = "feed";
      component.setStyleClass(styleClass);

      component.setSourceStyle((String)properties.get("sourceStyle"));
      String sourceStyleClass = (String)properties.get("sourceStyleClass");
      if (sourceStyleClass == null) sourceStyleClass = "source";
      component.setSourceStyleClass(sourceStyleClass);

      component.setDateStyle((String)properties.get("dateStyle"));
      String dateStyleClass = (String)properties.get("dateStyleClass");
      if (dateStyleClass == null) dateStyleClass = "date";
      component.setDateStyleClass(dateStyleClass);

      component.setHeadLineStyle((String)properties.get("headLineStyle"));
      String headLineStyleClass = (String)properties.get("headLineStyleClass");
      if (headLineStyleClass == null) headLineStyleClass = "headLine";
      component.setHeadLineStyleClass(headLineStyleClass);

      component.setSummaryStyle((String)properties.get("summaryStyle"));
      String summaryStyleClass = (String)properties.get("summaryStyleClass");
      if (summaryStyleClass == null) summaryStyleClass = "summary";
      component.setSummaryStyleClass(summaryStyleClass);

      component.setVar((String)properties.get("var"));

      String dateFormat = (String)properties.get("dateFormat");
      if (dateFormat == null) dateFormat = "dd/MM/yyyy";
      component.setDateFormat(dateFormat);

      String renderImage = (String)properties.get("renderImage");
      if (renderImage == null) renderImage = "true";
      component.setRenderImage(Boolean.valueOf(renderImage));

      String renderSource = (String)properties.get("renderSource");
      if (renderSource == null) renderSource = "true";
      component.setRenderSource(Boolean.valueOf(renderSource));

      String renderDate = (String)properties.get("renderDate");
      if (renderDate == null) renderDate = "true";
      component.setRenderDate(Boolean.valueOf(renderDate));

      String renderHeadLine = (String)properties.get("renderHeadLine");
      if (renderHeadLine == null) renderHeadLine = "true";
      component.setRenderHeadLine(Boolean.valueOf(renderHeadLine));

      String renderSummary = (String)properties.get("renderSummary");
      if (renderSummary == null) renderSummary = "true";
      component.setRenderSummary(Boolean.valueOf(renderSummary));

      String displayOrder = (String)properties.get("displayOrder");
      if (displayOrder == null) displayOrder = "source,date,headLine,summary";
      component.setDisplayOrder(displayOrder);
      
      //Invalid image prefix
      String invalidImagePrefix = 
        (String)properties.get("invalidImagePrefix");
      if (isValueReference((String)properties.get("invalidImagePrefix")))
        UIComponentTagUtils.setValueBinding(context, component, 
          "invalidImagePrefix", invalidImagePrefix);
      else
      {
        List<String> invalidImagePrefixes = 
          widgetDef.getMultivaluedProperty("invalidImagePrefix");
        component.setInvalidImagePrefixes(invalidImagePrefixes);
      }

      //Invalid image prefix
      String invalidSummaryString = 
        (String)properties.get("invalidSummaryString");
      if (isValueReference((String)properties.get("invalidSummaryString")))
        UIComponentTagUtils.setValueBinding(context, component, 
          "invalidSummaryString", invalidSummaryString);
      else
      {
        List<String> invalidSummaryStrings = 
          widgetDef.getMultivaluedProperty("invalidSummaryString");
        component.setInvalidSummaryStrings(invalidSummaryStrings);
      }
      
      //One entry source urls
      String oneEntrySourceUrls = 
        (String)properties.get("oneEntrySourceUrls");
      if (isValueReference((String)properties.get("oneEntrySourceUrls")))
        UIComponentTagUtils.setValueBinding(context, component, 
          "oneEntrySourceUrls", oneEntrySourceUrls);
      else
      {
        List<String> oneEntrySourceUrlList = 
          widgetDef.getMultivaluedProperty("oneEntrySourceUrls");
        component.setOneEntrySourceUrls(oneEntrySourceUrlList);
      }
      
      //summary max size
      String summaryMaxSize = (String)properties.get("summaryMaxSize");
      if (summaryMaxSize == null) summaryMaxSize = "300";
      component.setSummaryMaxSize(Integer.valueOf(summaryMaxSize).intValue());      

      //headLine max size
      String headLineMaxSize = (String)properties.get("headLineMaxSize");
      if (headLineMaxSize == null) headLineMaxSize = "0";
      component.setHeadLineMaxSize(Integer.valueOf(headLineMaxSize).intValue());
    }

    return component;
  }

}
