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
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author blanquepa
 */
public abstract class WidgetBuilder
{
  private boolean folded;
  private boolean standalone;
  
  public WidgetBuilder()
  {
  }

  public boolean isFolded()
  {
    return folded;
  }

  public void setFolded(boolean folded)
  {
    this.folded = folded;
  }

  public boolean isStandalone()
  {
    return standalone;
  }

  public void setStandalone(boolean standalone)
  {
    this.standalone = standalone;
  }

  public abstract UIComponent getComponent(
    WidgetDefinition widgetDef, FacesContext context);

  protected boolean isValueReference(String value)
  {
    if (value != null)
      return value.contains("#{");

    else return false;
  }

  protected Object getValue(FacesContext context, String expr)
  {
    ValueBinding vb = context.getApplication().createValueBinding(expr);
    return vb.getValue(context);
  }

  public static WidgetBuilder getInstance(String builderName)
  {
    try
    {
      String builderClassName = (builderName.indexOf(".") == -1) ?
        WidgetBuilder.class.getPackage().getName() + "." + builderName :
        builderName;

      Class widgetBuilderClass = Class.forName(builderClassName);
      Constructor c = widgetBuilderClass.getConstructor();
      WidgetBuilder widgetContent = (WidgetBuilder)c.newInstance();
      return widgetContent;
    }
    catch(Exception ex)
    {
      return null;
    }
  }

  protected void setTranslationProperties(UIComponent component,
    Map properties, String defaultGroup, FacesContext context)
  {
    // Translator
    String translator = (String)properties.get("translator");
    if (isValueReference(translator))
      UIComponentTagUtils.setValueBinding(context, component, "translator",
        translator);
    else
      UIComponentTagUtils.setValueBinding(context, component, "translator",
        "#{applicationBean.translator}");

    // Translation group
    String translationGroup = (String)properties.get("translationGroup");
    if (translationGroup == null)
      component.getAttributes().put("translationGroup", defaultGroup);
    else if (isValueReference(translationGroup))
      UIComponentTagUtils.setValueBinding(context, component, "translationGroup",
        translationGroup);
    else
      component.getAttributes().put("translationGroup", translationGroup);
  }
  
  protected String getStrictTranslationGroup(WidgetDefinition widgetDef, 
    String widgetType)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMM");    
    String defaultGroup = widgetType + "_" + widgetDef.getMid() + "_" + 
      format.format(new Date());
    return defaultGroup;
  }  
  
}
