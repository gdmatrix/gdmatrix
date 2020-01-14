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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.custom.div.Div;
import org.santfeliu.doc.faces.HtmlDocuments;
import org.santfeliu.misc.widget.web.WidgetDefinition;
import org.santfeliu.web.obj.util.ResultsManager;

/**
 *
 * @author blanquepa
 */
public class DocumentsWidgetBuilder extends WidgetBuilder
{
  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef, FacesContext context)
  {
    ResultsManager resultsManager = new ResultsManager(
      "org.santfeliu.doc.web.resources.DocumentBundle", "");
    resultsManager.setColumns(widgetDef.getMid());

    Div containerDiv = new Div();

    Map properties = widgetDef.getProperties();
    if (properties != null)
    {
      String widgetId = widgetDef.getWidgetId();
      String docTypeId = (String)properties.get("docTypeId");
      String propertyName = (String)properties.get("propertyName");
      String propertyValue = (String)properties.get("propertyValue");
      String searchMid = (String)properties.get("documentSearchMid");
      String directDownload = (String)properties.get("directDownload");
      String style = (String)properties.get("style");
      String styleClass = (String)properties.get("styleClass");
      if (styleClass == null) styleClass = "documentList";
      String pageSize = (String)properties.get("pageSize");
      int size = (pageSize != null ? Integer.parseInt(pageSize) : 9);
      List orderBy = widgetDef.getMultivaluedProperty("orderBy");

      HtmlDocuments component = new HtmlDocuments();
      component.setResultsManager(resultsManager);
      component.setRows(size);

      if (docTypeId != null)
        component.setDocTypeId(docTypeId);
      if (propertyName != null)
        component.setPropertyName(propertyName);
      if (propertyValue != null)
        component.setPropertyValues(Collections.singletonList(propertyValue));
      if (style != null)
        component.setStyle(style);
      if (styleClass != null)
        component.setStyleClass(styleClass);
      if (orderBy != null && !orderBy.isEmpty())
        component.setOrderBy(orderBy);
      if (directDownload != null)
        component.setDirectDownload(Boolean.valueOf(directDownload));

      return component;
    }

    return containerDiv;
  }
}
