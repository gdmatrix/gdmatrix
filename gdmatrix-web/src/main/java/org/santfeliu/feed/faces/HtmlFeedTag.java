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
package org.santfeliu.feed.faces;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import static javax.faces.webapp.UIComponentTag.isValueReference;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author lopezrj
 */
public class HtmlFeedTag extends UIComponentTag
{  
  private String url;
  private String rows;
  private String source;
  private String style;
  private String styleClass;
  private String sourceStyle;
  private String sourceStyleClass;  
  private String dateStyle;
  private String dateStyleClass;
  private String headLineStyle;
  private String headLineStyleClass;
  private String summaryStyle;
  private String summaryStyleClass;
  private String var;
  private String dateFormat;
  private String renderImage;
  private String renderSource;
  private String renderDate;
  private String renderHeadLine;
  private String renderSummary;  
  private String displayOrder;
  private String invalidImagePrefix;
  private String invalidSummaryString;
  private String summaryMaxSize;
  private String translator;
  private String translationGroup;
  private String enableTranslation;
  private String oneEntrySourceUrls;
  private String headLineMaxSize;
  
  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }  
  
  public String getRows()
  {
    return rows;
  }

  public void setRows(String rows)
  {
    this.rows = rows;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }
  
  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getSourceStyle() 
  {
    return sourceStyle;
  }

  public void setSourceStyle(String sourceStyle) 
  {
    this.sourceStyle = sourceStyle;
  }

  public String getSourceStyleClass() 
  {
    return sourceStyleClass;
  }

  public void setSourceStyleClass(String sourceStyleClass) 
  {
    this.sourceStyleClass = sourceStyleClass;
  }

  public String getDateStyle()
  {
    return dateStyle;
  }

  public void setDateStyle(String dateStyle)
  {
    this.dateStyle = dateStyle;
  }

  public String getDateStyleClass()
  {
    return dateStyleClass;
  }

  public void setDateStyleClass(String dateStyleClass)
  {
    this.dateStyleClass = dateStyleClass;
  }

  public String getHeadLineStyle()
  {
    return headLineStyle;
  }

  public void setHeadLineStyle(String headLineStyle)
  {
    this.headLineStyle = headLineStyle;
  }

  public String getHeadLineStyleClass()
  {
    return headLineStyleClass;
  }

  public void setHeadLineStyleClass(String headLineStyleClass)
  {
    this.headLineStyleClass = headLineStyleClass;
  }

  public String getSummaryStyle()
  {
    return summaryStyle;
  }

  public void setSummaryStyle(String summaryStyle)
  {
    this.summaryStyle = summaryStyle;
  }

  public String getSummaryStyleClass()
  {
    return summaryStyleClass;
  }

  public void setSummaryStyleClass(String summaryStyleClass)
  {
    this.summaryStyleClass = summaryStyleClass;
  }

  public String getVar()
  {
    return var;
  }

  public void setVar(String var)
  {
    this.var = var;
  }

  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  public String getRenderImage()
  {
    return renderImage;
  }

  public void setRenderImage(String renderImage)
  {
    this.renderImage = renderImage;
  }  
  
  public String getRenderSource() 
  {
    return renderSource;
  }

  public void setRenderSource(String renderSource) 
  {
    this.renderSource = renderSource;
  }
  
  public String getRenderDate()
  {
    return renderDate;
  }

  public void setRenderDate(String renderDate)
  {
    this.renderDate = renderDate;
  }  

  public String getRenderHeadLine() 
  {
    return renderHeadLine;
  }

  public void setRenderHeadLine(String renderHeadLine) 
  {
    this.renderHeadLine = renderHeadLine;
  }

  public String getRenderSummary()
  {
    return renderSummary;
  }

  public void setRenderSummary(String renderSummary)
  {
    this.renderSummary = renderSummary;
  }

  public String getDisplayOrder()
  {
    return displayOrder;
  }

  public void setDisplayOrder(String displayOrder)
  {
    this.displayOrder = displayOrder;
  }

  public String getInvalidImagePrefix()
  {
    return invalidImagePrefix;
  }

  public void setInvalidImagePrefix(String invalidImagePrefix)
  {
    this.invalidImagePrefix = invalidImagePrefix;
  } 

  public String getInvalidSummaryString()
  {
    return invalidSummaryString;
  }

  public void setInvalidSummaryString(String invalidSummaryString)
  {
    this.invalidSummaryString = invalidSummaryString;
  }

  public String getSummaryMaxSize()
  {
    return summaryMaxSize;
  }

  public void setSummaryMaxSize(String summaryMaxSize)
  {
    this.summaryMaxSize = summaryMaxSize;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this.translationGroup = translationGroup;
  }

  public String getTranslator()
  {
    return translator;
  }

  public void setTranslator(String translator)
  {
    this.translator = translator;
  }  

  public String getEnableTranslation()
  {
    return enableTranslation;
  }

  public void setEnableTranslation(String enableTranslation)
  {
    this.enableTranslation = enableTranslation;
  }

  public String getOneEntrySourceUrls()
  {
    return oneEntrySourceUrls;
  }

  public void setOneEntrySourceUrls(String oneEntrySourceUrls)
  {
    this.oneEntrySourceUrls = oneEntrySourceUrls;
  }

  public String getHeadLineMaxSize()
  {
    return headLineMaxSize;
  }

  public void setHeadLineMaxSize(String headLineMaxSize)
  {
    this.headLineMaxSize = headLineMaxSize;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      if (url != null)
      {
        if (isValueReference(url))
        {
          ValueBinding vb = context.getApplication().createValueBinding(url);
          component.setValueBinding("url", vb);
        }
        else
        {
          UIComponentTagUtils.setStringProperty(
            context, component, "url", url);
        }
      }
      if (invalidImagePrefix != null)
      {
        if (isValueReference(invalidImagePrefix))
        {
          ValueBinding vb = context.getApplication().createValueBinding(invalidImagePrefix);
          component.setValueBinding("invalidImagePrefix", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "invalidImagePrefix", invalidImagePrefix);
      }      
      if (invalidSummaryString != null)
      {
        if (isValueReference(invalidSummaryString))
        {
          ValueBinding vb = context.getApplication().createValueBinding(invalidSummaryString);
          component.setValueBinding("invalidSummaryString", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "invalidSummaryString", invalidSummaryString);
      }      
      if (oneEntrySourceUrls != null)
      {
        if (isValueReference(oneEntrySourceUrls))
        {
          ValueBinding vb = context.getApplication().createValueBinding(oneEntrySourceUrls);
          component.setValueBinding("oneEntrySourceUrls", vb);
        }
        else
          UIComponentTagUtils.setStringProperty(
            context, component, "oneEntrySourceUrls", oneEntrySourceUrls);
      }      
      UIComponentTagUtils.setIntegerProperty(
        context, component, "rows", rows);
      UIComponentTagUtils.setStringProperty(
        context, component, "source", source);
      UIComponentTagUtils.setStringProperty(
        context, component, "style", style);
      UIComponentTagUtils.setStringProperty(
        context, component, "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "sourceStyle", sourceStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "sourceStyleClass", sourceStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "dateStyle", dateStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "dateStyleClass", dateStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "headLineStyle", headLineStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "headLineStyleClass", headLineStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "summaryStyle", summaryStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "summaryStyleClass", summaryStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "var", var);
      UIComponentTagUtils.setStringProperty(
        context, component, "dateFormat", dateFormat);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderImage", renderImage);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderSource", renderSource);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderDate", renderDate);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderHeadLine", renderHeadLine);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderSummary", renderSummary);
      UIComponentTagUtils.setStringProperty(
        context, component, "displayOrder", displayOrder);
      UIComponentTagUtils.setIntegerProperty(
        context, component, "summaryMaxSize", summaryMaxSize); 
      UIComponentTagUtils.setIntegerProperty(
        context, component, "headLineMaxSize", headLineMaxSize); 
      if (translator != null)
      {
        if (isValueReference(translator))
        {
          ValueBinding vb = context.getApplication().createValueBinding(translator);
          component.setValueBinding("translator", vb);
        }
      }
      UIComponentTagUtils.setStringProperty(
        context, component, "translationGroup", translationGroup);  
      UIComponentTagUtils.setBooleanProperty(
        context, component, "enableTranslation", enableTranslation);      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void release()
  {
    super.release();    
    url = null;
    rows = null;
    source = null;
    style = null;
    styleClass = null;
    sourceStyle = null;
    sourceStyleClass = null;  
    dateStyle = null;
    dateStyleClass = null;
    headLineStyle = null;
    headLineStyleClass = null;
    summaryStyle = null;
    summaryStyleClass = null;
    var = null;
    dateFormat = null;
    renderImage = null;
    renderSource = null;
    renderDate = null;
    renderHeadLine = null;
    renderSummary = null; 
    displayOrder = null;
    invalidImagePrefix = null;
    invalidSummaryString = null;
    summaryMaxSize = null;
    translator = null;
    translationGroup = null;
    enableTranslation = null;
    oneEntrySourceUrls = null;
    headLineMaxSize = null;    
  }

  @Override
  public String getComponentType()
  {
    return "FeedWidget";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }
}
