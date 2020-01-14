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
import org.santfeliu.news.faces.HtmlNewsCarousel;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj
 */
public class NewsCarouselWidgetBuilder extends WidgetBuilder
{
  private static final int DEFAULT_TRANSITION_TIME = 3000;
  private static final int DEFAULT_ROWS = 10;
  private static final String DEFAULT_MORE_INFO_LABEL = "INFORMACIÓ";
  private static final String DEFAULT_MORE_NEWS_LABEL = "ACTUALITAT";
  private static final String DEFAULT_DRAFT_TEXT = "Esborrany";
  private static final String DEFAULT_STYLE_CLASS = "newsCarousel";
  private static final String DEFAULT_VAR = "n";

  public NewsCarouselWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    HtmlNewsCarousel component = new HtmlNewsCarousel();

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

      setTranslationProperties(component, properties, "new", context); 

      //rows
      String rows = (String)properties.get("rows");
      if (rows != null)
        component.setRows(Integer.valueOf(rows).intValue());
      else
        component.setRows(DEFAULT_ROWS);

      //transitionTime
      String transitionTime = (String)properties.get("transitionTime");
      if (transitionTime != null)
        component.setTransitionTime(Integer.valueOf(transitionTime).intValue());
      else
        component.setTransitionTime(DEFAULT_TRANSITION_TIME);

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
      
    }
    return component;
  }
}
