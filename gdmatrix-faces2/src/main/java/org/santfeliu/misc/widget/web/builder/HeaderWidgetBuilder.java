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
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import org.santfeliu.faces.component.HtmlGraphicImage;
import org.santfeliu.faces.component.HtmlOutputLink;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.div.Div;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.faces.component.HtmlOutputText;
import org.santfeliu.faces.menu.view.HtmlTreeMenu;

/**
 *
 * @author realor
 */
public class HeaderWidgetBuilder extends WidgetBuilder
{
  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    Application application = context.getApplication();
    
    Map properties = widgetDef.getProperties();

    Div div = new Div();
    String headerStyleClass = (String)properties.get("headerStyleClass");
    if (headerStyleClass != null)
    {
      div.setStyleClass(headerStyleClass + (isFolded() ? "" : " unfolded"));
    }

    String widgetId = widgetDef.getWidgetId();
        
    if ("header".equals(widgetDef.getDragAreaPosition()))
    {
      Div divDragArea = new Div();
      divDragArea.setId("dragArea_" + widgetId);
      divDragArea.setForceId(true);
      divDragArea.setStyleClass("dragArea");
      div.getChildren().add(divDragArea);

      String title = (String)properties.get("title");
      if (title != null)
      {
        HtmlOutputText titleText = new HtmlOutputText();
        titleText.setId(widgetId + "_title");
        titleText.setValue(title);
        titleText.setStyleClass("title");
        titleText.setEscape(false);
        setTranslationProperties(titleText, properties, "widget", context);
        divDragArea.getChildren().add(titleText);
      }

      String subTitle = (String)properties.get("subTitle");
      if (subTitle != null)
      {
        HtmlOutputText subTitleText = new HtmlOutputText();
        subTitleText.setId(widgetId + "_subtitle");
        subTitleText.setValue(subTitle);
        subTitleText.setStyleClass("subtitle");
        subTitleText.setEscape(false);
        setTranslationProperties(subTitleText, properties, "widget", context);
        divDragArea.getChildren().add(subTitleText);
      }
    }
    
    String widgetDescription = widgetDef.getAriaDescription();    
    
    if ("header".equals(widgetDef.getCloseLinkPosition()))
    {
      String closeImageURL = (String)properties.get("closeImageURL");    
      if (closeImageURL != null && !"none".equals(closeImageURL))
      {
        HtmlOutputLink closeLink = new HtmlOutputLink();
        closeLink.setId(widgetId + "_close");        
        closeLink.setStyleClass("widget_close");
        closeLink.setValue("#");
        closeLink.setOnclick("removeWidget('" + widgetId + "');return false;");

        String closeText = (String)properties.get("closeText");
        if (closeText == null) 
        {
          closeText = "Close widget";
        }
        
        closeLink.setTitle(closeText + ": " + widgetDescription);
        closeLink.setValueBinding("translator",
          application.createValueBinding("#{userSessionBean.translator}"));
        closeLink.setValueBinding("translationGroup",
          application.createValueBinding("#{userSessionBean.translationGroup}"));        
        
        HtmlGraphicImage image = new HtmlGraphicImage();
        image.setId(widgetId + "_closeImg");
        image.setUrl(closeImageURL);
        image.setAlt(closeText + ": " + widgetDescription);
        image.setValueBinding("translator",
          application.createValueBinding("#{userSessionBean.translator}"));
        image.setValueBinding("translationGroup",
          application.createValueBinding("#{userSessionBean.translationGroup}"));
        closeLink.getChildren().add(image);
        div.getChildren().add(closeLink);
      }
    }

    String foldImageURL = (String)properties.get("foldImageURL");
    if (foldImageURL != null && !"none".equals(foldImageURL))
    {      
      HtmlOutputLink foldLink = new HtmlOutputLink();
      foldLink.setId(widgetId + "_fold");
      if (isFolded())
      {
        //foldLink.setStyle("visibility:hidden; display: none");
        foldLink.setStyle("display: none");
        foldLink.getAttributes().put("aria-hidden", "true");
      }
      else
      {
        //foldLink.setStyle("visibility:hidden; display: block");
        foldLink.setStyle("display: block");
        foldLink.getAttributes().put("aria-hidden", "false");
      }
      foldLink.setStyleClass("widget_fold");
      foldLink.setValue("#");

      if (isStandalone())
      {
        foldLink.setOnclick("foldStandaloneWidget('" + widgetId + "');return false;");
      }
      else
      {
        foldLink.setOnclick("foldWidget('" + widgetId + "');return false;");
      } 
      
      String foldText = (String)properties.get("foldText");
      if (foldText == null) 
      {
        foldText = "Fold widget";
      }
      
      foldLink.setTitle(foldText + ": " + widgetDescription);
      foldLink.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      foldLink.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));            

      HtmlGraphicImage image = new HtmlGraphicImage();
      image.setId(widgetId + "_foldImg");
      image.setUrl(foldImageURL);
      image.setAlt(foldText + ": " + widgetDescription);
      image.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      image.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));      
      foldLink.getChildren().add(image);
      div.getChildren().add(foldLink);      
    }
    
    String unfoldImageURL = (String)properties.get("unfoldImageURL");
    if (unfoldImageURL != null && !"none".equals(unfoldImageURL))
    {
      //Unfold link
      HtmlOutputLink unfoldLink = new HtmlOutputLink();
      unfoldLink.setId(widgetId + "_unfold");
      if (isFolded())
      {
        //unfoldLink.setStyle("visibility:hidden; display: block");
        unfoldLink.setStyle("display: block");
        unfoldLink.getAttributes().put("aria-hidden", "false");
      }
      else
      {
        //unfoldLink.setStyle("visibility:hidden; display: none");
        unfoldLink.setStyle("display: none");
        unfoldLink.getAttributes().put("aria-hidden", "true");
      }
      unfoldLink.setStyleClass("widget_unfold");
      unfoldLink.setValue("#");
      if (isStandalone())
      {
        unfoldLink.setOnclick("foldStandaloneWidget('" + widgetId + "');return false;");
      }
      else
      {
        unfoldLink.setOnclick("foldWidget('" + widgetId + "');return false;");
      }
      
      String unfoldText = (String)properties.get("unfoldText");
      if (unfoldText == null) 
      {
        unfoldText = "Unfold widget";
      }

      unfoldLink.setTitle(unfoldText + ": " + widgetDescription);
      unfoldLink.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      unfoldLink.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));            
      
      HtmlGraphicImage image = new HtmlGraphicImage();
      image.setId(widgetId + "_unfoldImg");
      image.setUrl(unfoldImageURL);
      image.setAlt(unfoldText + ": " + widgetDescription);
      image.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      image.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));            
      unfoldLink.getChildren().add(image);
      div.getChildren().add(unfoldLink);
    }      

    String dragImageURL = (String)properties.get("dragImageURL");
    if (dragImageURL != null && !"none".equals(dragImageURL))
    {
      //Drag icon
      HtmlGraphicImage image = new HtmlGraphicImage();
      image.setId(widgetId + "_dragImg");
      image.setUrl(dragImageURL);
      image.setStyleClass("widget_drag");      
      String dragText = (String)properties.get("dragText");
      if (dragText == null) 
      {
        dragText = "Drag widget";
      }
      image.setAlt(dragText + ": " + widgetDescription);
      image.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      image.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));      
      div.getChildren().add(image);
    }
    
    String headerMenuMid = (String)properties.get("headerMenuMid");
    if (headerMenuMid != null && !"none".equals(headerMenuMid))
    {
      try
      {
        HtmlTreeMenu menu = new HtmlTreeMenu();
        menu.setId(widgetId + "_widgetHeaderMenu");
        menu.setStyleClass("widgetHeaderMenu");
        menu.setVar("item");
        menu.setBaseMid(headerMenuMid);
        menu.setExpandDepth(1);
        HtmlOutputLink itemLink = new HtmlOutputLink();
        UIComponentTagUtils.setValueBinding(context, itemLink, "value", "#{item.properties.actionURL}");
        UIComponentTagUtils.setValueBinding(context, itemLink, "target", "#{item.target == null ? 'blank' : item.target}");
        UIComponentTagUtils.setValueBinding(context, itemLink, "rendered", "#{item.rendered && item.properties.onWidgetHeaderMenu != 'false'}");
        UIComponentTagUtils.setValueBinding(context, itemLink, "title", "#{item.properties.title}");        
        itemLink.setValueBinding("translator",
          application.createValueBinding("#{userSessionBean.translator}"));
        itemLink.setValueBinding("translationGroup",
          application.createValueBinding("#{userSessionBean.translationGroup}"));        
        HtmlGraphicImage itemImage = new HtmlGraphicImage();        
        UIComponentTagUtils.setValueBinding(context, itemImage, "alt", "#{item.properties.title}");        
        itemImage.setValueBinding("translator",
          application.createValueBinding("#{userSessionBean.translator}"));
        itemImage.setValueBinding("translationGroup",
          application.createValueBinding("#{userSessionBean.translationGroup}"));
        UIComponentTagUtils.setValueBinding(context, itemImage, "url", "#{item.properties.imageURL}");
        itemLink.getChildren().add(itemImage);
        menu.getFacets().put("data", itemLink);
        div.getChildren().add(menu);
      }
      catch (Exception ex) { }      
    }
        
    return div;
  }  
}
