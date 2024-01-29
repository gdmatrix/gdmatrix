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
package org.santfeliu.news.faces;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author blanquepa
 */
public class HtmlNewsTag extends UIComponentTag
{
  private String section;
  private String rows;
  private String translator;
  private String translationGroup;
  private String style;
  private String styleClass;
  private String row1StyleClass;
  private String row2StyleClass;
  private String dateStyle;
  private String dateStyleClass;
  private String sourceStyle;
  private String sourceStyleClass;  
  private String headLineStyle;
  private String headLineStyleClass;
  private String imageStyle;
  private String imageStyleClass;
  private String summaryStyle;
  private String summaryStyleClass;
  private String var;
  private String url;
  private String rss;
  private String dateFormat;
  private String imageWidth;
  private String imageHeight;
  private String imageCrop;
  private String showMoreUrl;
  private String showMoreText;
  private String renderSummary;
  private String excludeDrafts;
  private String renderDate;
  private String renderSource;
  private String urlSeparator;
  private String maxHeadlineChars;
  private String maxSummaryChars;
  private String decodeText;
  private String mixSections;
  private String invalidSummaryStrings;
  private String oneEntrySourceUrls;
  private String renderImage;
  private String renderHeadline;
  private String enableTranslation;
  

  public String getRows()
  {
    return rows;
  }

  public void setRows(String rows)
  {
    this.rows = rows;
  }

  public String getSection()
  {
    return section;
  }

  public void setSection(String section)
  {
    this.section = section;
  }

  public String getMaxHeadlineChars()
  {
    return maxHeadlineChars;
  }

  public void setMaxHeadlineChars(String maxHeadlineChars)
  {
    this.maxHeadlineChars = maxHeadlineChars;
  }

  public String getMaxSummaryChars()
  {
    return maxSummaryChars;
  }

  public void setMaxSummaryChars(String maxSummaryChars)
  {
    this.maxSummaryChars = maxSummaryChars;
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

  public String getRow1StyleClass()
  {
    return row1StyleClass;
  }

  public void setRow1StyleClass(String row1StyleClass)
  {
    this.row1StyleClass = row1StyleClass;
  }

  public String getRow2StyleClass()
  {
    return row2StyleClass;
  }

  public void setRow2StyleClass(String row2StyleClass)
  {
    this.row2StyleClass = row2StyleClass;
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

  public String getHeadLineStyleClass()
  {
    return headLineStyleClass;
  }

  public void setHeadLineStyleClass(String headLineStyleClass)
  {
    this.headLineStyleClass = headLineStyleClass;
  }

  public String getHeadLineStyle()
  {
    return headLineStyle;
  }

  public void setHeadLineStyle(String headLineStyle)
  {
    this.headLineStyle = headLineStyle;
  }

  public String getImageStyle()
  {
    return imageStyle;
  }

  public void setImageStyle(String imageStyle)
  {
    this.imageStyle = imageStyle;
  }

  public String getImageStyleClass()
  {
    return imageStyleClass;
  }

  public void setImageStyleClass(String imageStyleClass)
  {
    this.imageStyleClass = imageStyleClass;
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

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getVar()
  {
    return var;
  }

  public void setVar(String var)
  {
    this.var = var;
  }

  public String getRss()
  {
    return rss;
  }

  public void setRss(String rss)
  {
    this.rss = rss;
  }

  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  public String getImageCrop()
  {
    return imageCrop;
  }

  public void setImageCrop(String imageCrop)
  {
    this.imageCrop = imageCrop;
  }

  public String getImageHeight()
  {
    return imageHeight;
  }

  public void setImageHeight(String imageHeight)
  {
    this.imageHeight = imageHeight;
  }

  public String getImageWidth()
  {
    return imageWidth;
  }

  public void setImageWidth(String imageWidth)
  {
    this.imageWidth = imageWidth;
  }

  public String getExcludeDrafts()
  {
    return excludeDrafts;
  }

  public void setExcludeDrafts(String excludeDrafts)
  {
    this.excludeDrafts = excludeDrafts;
  }

  public String getRenderSummary()
  {
    return renderSummary;
  }

  public void setRenderSummary(String renderSummary)
  {
    this.renderSummary = renderSummary;
  }

  public String getRenderDate()
  {
    return renderDate;
  }

  public void setRenderDate(String renderDate)
  {
    this.renderDate = renderDate;
  }

  public String getRenderSource()
  {
    return renderSource;
  }

  public void setRenderSource(String renderSource)
  {
    this.renderSource = renderSource;
  }

  public String getDecodeText()
  {
    return decodeText;
  }

  public void setDecodeText(String decodeText)
  {
    this.decodeText = decodeText;
  }

  public String getUrlSeparator()
  {
    return urlSeparator;
  }

  public void setUrlSeparator(String urlSeparator)
  {
    this.urlSeparator = urlSeparator;
  }

  public String getShowMoreText()
  {
    return showMoreText;
  }

  public void setShowMoreText(String showMoreText)
  {
    this.showMoreText = showMoreText;
  }

  public String getShowMoreUrl()
  {
    return showMoreUrl;
  }

  public void setShowMoreUrl(String showMoreUrl)
  {
    this.showMoreUrl = showMoreUrl;
  }

  public String getMixSections()
  {
    return mixSections;
  }

  public void setMixSections(String mixSections)
  {
    this.mixSections = mixSections;
  }

  public String getInvalidSummaryStrings()
  {
    return invalidSummaryStrings;
  }

  public void setInvalidSummaryStrings(String invalidSummaryStrings)
  {
    this.invalidSummaryStrings = invalidSummaryStrings;
  }

  public String getOneEntrySourceUrls()
  {
    return oneEntrySourceUrls;
  }

  public void setOneEntrySourceUrls(String oneEntrySourceUrls)
  {
    this.oneEntrySourceUrls = oneEntrySourceUrls;
  }

  public String getRenderImage()
  {
    return renderImage;
  }

  public void setRenderImage(String renderImage)
  {
    this.renderImage = renderImage;
  }

  public String getRenderHeadline()
  {
    return renderHeadline;
  }

  public void setRenderHeadline(String renderHeadline)
  {
    this.renderHeadline = renderHeadline;
  }

  public String getEnableTranslation()
  {
    return enableTranslation;
  }

  public void setEnableTranslation(String enableTranslation)
  {
    this.enableTranslation = enableTranslation;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      UIComponentTagUtils.setIntegerProperty(
        context, component, "rows", rows);
      if (section != null)
      {
        if (isValueReference(section))
        {
          ValueBinding vb = context.getApplication().createValueBinding(section);
          component.setValueBinding("section", vb);
        }
        else
        {
          UIComponentTagUtils.setStringProperty(
            context, component, "section", section);
        }
      }
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
      UIComponentTagUtils.setStringProperty(
        context, component, "style", style);
      UIComponentTagUtils.setStringProperty(
        context, component, "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "row1StyleClass", row1StyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "row2StyleClass", row2StyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "dateStyle", dateStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "dateStyleClass", dateStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "sourceStyle", sourceStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "sourceStyleClass", sourceStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "headLineStyle", headLineStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "headLineStyleClass", headLineStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "imageStyle", imageStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "imageStyleClass", imageStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "summaryStyle", summaryStyle);
      UIComponentTagUtils.setStringProperty(
        context, component, "summaryStyleClass", summaryStyleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "var", var);
      UIComponentTagUtils.setStringProperty(
        context, component, "url", url);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "rss", rss);
      UIComponentTagUtils.setStringProperty(
        context, component, "dateFormat", dateFormat);
      UIComponentTagUtils.setStringProperty(
        context, component, "imageWidth", imageWidth);
      UIComponentTagUtils.setStringProperty(
        context, component, "imageHeight", imageHeight);
      UIComponentTagUtils.setStringProperty(
        context, component, "imageCrop", imageCrop);
      UIComponentTagUtils.setStringProperty(
        context, component, "showMoreUrl", showMoreUrl);
      UIComponentTagUtils.setStringProperty(
        context, component, "showMoreText", showMoreText);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderSummary", renderSummary);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "excludeDrafts", excludeDrafts);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderDate", renderDate);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderSource", renderSource);
      UIComponentTagUtils.setStringProperty(
        context, component, "urlSeparator", urlSeparator);
      UIComponentTagUtils.setIntegerProperty(
        context, component, "maxHeadlineChars", maxHeadlineChars);
      UIComponentTagUtils.setIntegerProperty(
        context, component, "maxSummaryChars", maxSummaryChars);      
      UIComponentTagUtils.setBooleanProperty(
        context, component, "decodeText", decodeText);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "mixSections", mixSections);
      UIComponentTagUtils.setStringProperty(
        context, component, "invalidSummaryStrings", invalidSummaryStrings);
      UIComponentTagUtils.setStringProperty(
        context, component, "oneEntrySourceUrls", oneEntrySourceUrls);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderHeadline", renderHeadline);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderImage", renderImage);
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
    rows = null;
    section = null;
    translator = null;
    translationGroup = null;
  }

  @Override
  public String getComponentType()
  {
    return "NewsWidget";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }
}
