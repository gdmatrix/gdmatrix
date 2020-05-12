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
import org.santfeliu.faces.component.HtmlOutputLink;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.div.Div;
import org.santfeliu.faces.component.HtmlOutputText;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public class ButtonWidgetBuilder extends WidgetBuilder
{
  public ButtonWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext facesContext)
  {
    Map properties = widgetDef.getProperties();

    String label = (String)properties.get("label");
    if (label == null) label = (String)properties.get("description");
    String ariaLabel = (String)properties.get("ariaLabel");
    String detail = (String)properties.get("detail");

    Application application = facesContext.getApplication();
    
    Div div = new Div();
    String divStyleClass = (String)properties.get("divStyleClass");
    div.setStyleClass(divStyleClass);

    HtmlOutputLink outputLink = new HtmlOutputLink();
    String url = (String)properties.get("url");
    String target = (String)properties.get("target");
    outputLink.setId(widgetDef.getWidgetId() + "_link");
    outputLink.setValue(url);
    outputLink.setTarget(target);
    if (ariaLabel != null || label != null)
    {
      String auxLabel = (ariaLabel != null ? ariaLabel.trim() : label.trim());
      if ("_blank".equals(target))
      {
        String openNewWindowLabel = 
          MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
        auxLabel += " (" + openNewWindowLabel + ")";
      }
      outputLink.setAriaLabel(auxLabel);
      outputLink.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      outputLink.setValueBinding("translationGroup",
        application.createValueBinding("#{userSessionBean.translationGroup}"));
    }
    div.getChildren().add(outputLink);

    HtmlPanelGroup iconGroup = new HtmlPanelGroup();
    String iconStyle = (String)properties.get("iconStyle");  
    iconGroup.setId(widgetDef.getWidgetId() + "_icon");
    iconGroup.setStyle(iconStyle);
    iconGroup.setStyleClass("icon");
    outputLink.getChildren().add(iconGroup);

    if (label != null)
    {  
      HtmlOutputText labelOutputText = new HtmlOutputText();
      labelOutputText.setId(widgetDef.getWidgetId() + "_label");
      labelOutputText.setStyleClass("label");
      labelOutputText.setValue(label);
      labelOutputText.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      labelOutputText.setValueBinding("translationGroup",     
        application.createValueBinding("#{userSessionBean.translationGroup}"));
      outputLink.getChildren().add(labelOutputText);
    }

    if (detail != null)
    {
      HtmlOutputText detailOutputText = new HtmlOutputText();
      detailOutputText.setId(widgetDef.getWidgetId() + "_detail");
      detailOutputText.setStyleClass("detail");
      detailOutputText.setValue(detail);
      detailOutputText.setEscape(false);
      detailOutputText.setValueBinding("translator",
        application.createValueBinding("#{userSessionBean.translator}"));
      detailOutputText.setValueBinding("translationGroup",     
        application.createValueBinding("#{userSessionBean.translationGroup}"));
      div.getChildren().add(detailOutputText);
    }
    return div;
  }
}
