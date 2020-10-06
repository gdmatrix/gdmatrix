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
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.news.faces.HtmlNewsCarousel2;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj
 */
public class NewsCarousel2WidgetBuilder extends WidgetBuilder
{
  private static final int DEFAULT_TRANSITION_TIME = 3000;
  private static final int DEFAULT_MAX_SUMMARY_CHARS = 0;
  private static final int DEFAULT_ROWS = 9;
  private static final int DEFAULT_NEWS_PER_BLOCK = 3;  
  private static final String DEFAULT_MORE_INFO_LABEL = "INFORMACIÓ";
  private static final String DEFAULT_MORE_NEWS_LABEL = "ACTUALITAT";
  private static final String DEFAULT_MORE_NEWS_ARIA_LABEL = "Més notícies";
  private static final String DEFAULT_PREV_BLOCK_LABEL = "Mostrar bloc anterior";
  private static final String DEFAULT_PREV_BLOCK_ICON_URL = "/images/previous.gif";    
  private static final String DEFAULT_SHOW_BLOCK_LABEL = "Mostrar bloc";
  private static final String DEFAULT_NEXT_BLOCK_LABEL = "Mostrar bloc següent";
  private static final String DEFAULT_NEXT_BLOCK_ICON_URL = "/images/next.gif";  
  private static final String DEFAULT_DRAFT_TEXT = "Esborrany";
  private static final String DEFAULT_STYLE_CLASS = "newsCarousel";
  private static final String DEFAULT_VAR = "n";

  public NewsCarousel2WidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    HtmlNewsCarousel2 component = new HtmlNewsCarousel2();

    component.getAttributes().put("nodeId", widgetDef.getMid());    
    
    Map properties = widgetDef.getProperties();
    if (properties != null)
    {
      //Section
      String section = (String)properties.get("section");
      if (isValueReference(section))
        UIComponentTagUtils.setValueBinding(context, component, "section",
          section);
      else
        component.setSection(section);

      setTranslationProperties(component, properties, 
        getStrictTranslationGroup(widgetDef, "newsCarousel"), context);
      
      //rows
      String rows = (String)properties.get("rows");
      if (rows != null)
        component.setRows(Integer.valueOf(rows).intValue());
      else
        component.setRows(DEFAULT_ROWS);

      //newsPerBlock
      String newsPerBlock = (String)properties.get("newsPerBlock");
      if (newsPerBlock != null)
        component.setNewsPerBlock(Integer.valueOf(newsPerBlock).intValue());
      else
        component.setNewsPerBlock(DEFAULT_NEWS_PER_BLOCK);

      //transitionTime
      String transitionTime = (String)properties.get("transitionTime");
      if (transitionTime != null)
        component.setTransitionTime(Integer.valueOf(transitionTime).intValue());
      else
        component.setTransitionTime(DEFAULT_TRANSITION_TIME);

      //maxSummaryChars
      String maxSummaryChars = (String)properties.get("maxSummaryChars");
      if (maxSummaryChars != null)
        component.setMaxSummaryChars(Integer.valueOf(maxSummaryChars).intValue());
      else
        component.setMaxSummaryChars(DEFAULT_MAX_SUMMARY_CHARS);

      //style
      component.setStyle((String)properties.get("newsCarouselStyle"));

      String newStyleClass = (String)properties.get("newsCarouselStyleClass");
      if (newStyleClass == null) newStyleClass = DEFAULT_STYLE_CLASS;
      component.setStyleClass(newStyleClass);      

      //button labels
      String moreInfoLabel = (String)properties.get("moreInfoLabel");
      if (moreInfoLabel == null) moreInfoLabel = DEFAULT_MORE_INFO_LABEL;
      component.setMoreInfoLabel(moreInfoLabel);

      String moreNewsLabel = (String)properties.get("moreNewsLabel");
      if (moreNewsLabel == null) moreNewsLabel = DEFAULT_MORE_NEWS_LABEL;
      component.setMoreNewsLabel(moreNewsLabel);

      String moreNewsAriaLabel = (String)properties.get("moreNewsAriaLabel");
      if (moreNewsAriaLabel == null) moreNewsAriaLabel = 
        DEFAULT_MORE_NEWS_ARIA_LABEL;
      component.setMoreNewsAriaLabel(moreNewsAriaLabel);

      String prevBlockLabel = (String)properties.get("prevBlockLabel");
      if (prevBlockLabel == null) prevBlockLabel = DEFAULT_PREV_BLOCK_LABEL;
      component.setPrevBlockLabel(prevBlockLabel);
      
      String prevBlockIconURL = (String)properties.get("prevBlockIconURL");
      if (prevBlockIconURL == null) prevBlockIconURL = DEFAULT_PREV_BLOCK_ICON_URL;
      component.setPrevBlockIconURL(prevBlockIconURL);
      
      String showBlockLabel = (String)properties.get("showBlockLabel");
      if (showBlockLabel == null) showBlockLabel = DEFAULT_SHOW_BLOCK_LABEL;
      component.setShowBlockLabel(showBlockLabel);

      String nextBlockLabel = (String)properties.get("nextBlockLabel");
      if (nextBlockLabel == null) nextBlockLabel = DEFAULT_NEXT_BLOCK_LABEL;
      component.setNextBlockLabel(nextBlockLabel);

      String nextBlockIconURL = (String)properties.get("nextBlockIconURL");
      if (nextBlockIconURL == null) nextBlockIconURL = DEFAULT_NEXT_BLOCK_ICON_URL;
      component.setNextBlockIconURL(nextBlockIconURL);
      
      component.setDraftText(DEFAULT_DRAFT_TEXT);

      //var
      String var = (String)properties.get("var");
      if (var == null) var = DEFAULT_VAR;
      component.setVar(var);

      String moreInfoURLValue = (String)properties.get("moreInfoURL");
      UIComponentTagUtils.setStringProperty(context, component, "moreInfoURL",
        moreInfoURLValue);

      String moreNewsURLValue = (String)properties.get("moreNewsURL");
      UIComponentTagUtils.setStringProperty(context, component, "moreNewsURL",
        moreNewsURLValue);

      String urlSeparator = (String)properties.get("urlSeparator");
      if (urlSeparator == null) urlSeparator = "###";
      component.setUrlSeparator(urlSeparator);
      
      String excludeDrafts = (String)properties.get("excludeDrafts");
      if (excludeDrafts == null || excludeDrafts.equals("false"))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        MenuItemCursor sectionNode =
          userSessionBean.getMenuModel().getMenuItem(section);
        String editorRole = sectionNode.getProperty("roles.update");
        boolean excluded = !userSessionBean.isUserInRole(editorRole);
        component.setExcludeDrafts(excluded);
      }
      else
        component.setExcludeDrafts(true);
      
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
      
      String renderDate = (String)properties.get("renderDate");
      if (renderDate != null)
        component.setRenderDate(Boolean.valueOf(renderDate));      
    }
    return component;
  }
}
