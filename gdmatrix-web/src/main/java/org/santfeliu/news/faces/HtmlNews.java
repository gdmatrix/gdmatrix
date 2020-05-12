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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.matrix.news.NewView;
import org.matrix.news.SectionFilter;
import org.matrix.news.SectionView;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.news.web.NewsConfigBean;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class HtmlNews extends UIComponentBase
{
  public static final String IMAGE_SERVLET_PATH = "/imgscale/";
  private List _section;
  private Integer _rows;
  private Translator _translator;
  private String _translationGroup;
  private String _style;
  private String _styleClass;
  private String _dateStyle;
  private String _dateStyleClass;
  private String _headLineStyle;
  private String _headLineStyleClass;
  private String _imageStyle;
  private String _imageStyleClass;
  private String _summaryStyle;
  private String _summaryStyleClass;
  private String _var;
  private String _url;
  private String _dateFormat;
  private String _imageWidth;
  private String _imageHeight;
  private String _imageCrop;
  private Boolean _renderSummary;
  private List _excludeDrafts;
  private String _draftText;
  private String _sectionStyle;
  private String _sectionStyleClass;
  private Boolean _renderDate;  
  private String _urlSeparator;
  private Integer _maxSummaryChars;  
  
  public HtmlNews()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "News";
  }

  public List getSection()
  {
    if (_section != null) return _section;
    ValueExpression ve = getValueExpression("section");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setSection(List _section)
  {
    this._section = _section;
  }

  public int getRows()
  {
    if (_rows != null) return _rows.intValue();
    ValueExpression ve = getValueExpression("rows");
    Integer v = ve != null ? (Integer)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.intValue() : 0;
  }

  public void setRows(int _rows)
  {
    this._rows = _rows;
  }

  public int getMaxSummaryChars()
  {
    if (_maxSummaryChars != null) return _maxSummaryChars.intValue();
    ValueExpression ve = getValueExpression("maxSummaryChars");
    Integer v = ve != null ? (Integer)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.intValue() : 0;
  }

  public void setMaxSummaryChars(int _maxSummaryChars)
  {
    this._maxSummaryChars = _maxSummaryChars;
  }  
  
  public Translator getTranslator()
  {
    if (_translator != null)
      return _translator;
    ValueExpression ve = getValueExpression("translator");
    return ve != null? (Translator) ve.getValue(getFacesContext().getELContext()): null;
  }

  public void setTranslator(Translator _translator)
  {
    this._translator = _translator;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null)
      return _translationGroup;
    ValueExpression ve = getValueExpression("translationGroup");
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): "new";
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyle(String _style)
  {
    this._style = _style;
  }

  public String getUrlSeparator()
  {
    if (_urlSeparator != null) return _urlSeparator;
    ValueExpression ve = getValueExpression("urlSeparator");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setUrlSeparator(String _urlSeparator)
  {
    this._urlSeparator = _urlSeparator;
  }  
  
  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String _styleClass)
  {
    this._styleClass = _styleClass;
  }

  public String getDateStyle()
  {
    if (_dateStyle != null) return _dateStyle;
    ValueExpression ve = getValueExpression("dateStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDateStyle(String _dateStyle)
  {
    this._dateStyle = _dateStyle;
  }

  public String getDateStyleClass()
  {
    if (_dateStyleClass != null) return _dateStyleClass;
    ValueExpression ve = getValueExpression("dateStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setDateStyleClass(String _dateStyleClass)
  {
    this._dateStyleClass = _dateStyleClass;
  }

  public String getHeadLineStyle()
  {
     if (_headLineStyle != null) return _headLineStyle;
    ValueExpression ve = getValueExpression("headLineStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setHeadLineStyle(String _headLineStyle)
  {
    this._headLineStyle = _headLineStyle;
  }

  public String getHeadLineStyleClass()
  {
    if (_headLineStyleClass != null) return _headLineStyleClass;
    ValueExpression ve = getValueExpression("headLineStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setHeadLineStyleClass(String _headLineStyleClass)
  {
    this._headLineStyleClass = _headLineStyleClass;
  }

  public String getImageStyle()
  {
    if (_imageStyle != null) return _imageStyle;
    ValueExpression ve = getValueExpression("imageStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setImageStyle(String _imageStyle)
  {
    this._imageStyle = _imageStyle;
  }

  public String getImageStyleClass()
  {
    if (_imageStyleClass != null) return _imageStyleClass;
    ValueExpression ve = getValueExpression("imageStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setImageStyleClass(String _imageStyleClass)
  {
    this._imageStyleClass = _imageStyleClass;
  }

  public String getSummaryStyle()
  {
    if (_summaryStyle != null) return _summaryStyle;
    ValueExpression ve = getValueExpression("summaryStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;

  }

  public void setSummaryStyle(String _summaryStyle)
  {
    this._summaryStyle = _summaryStyle;
  }

  public String getSummaryStyleClass()
  {
    if (_summaryStyleClass != null) return _summaryStyleClass;
    ValueExpression ve = getValueExpression("summaryStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setSummaryStyleClass(String _summaryStyleClass)
  {
    this._summaryStyleClass = _summaryStyleClass;
  }

  public String getSectionStyle()
  {
    if (_sectionStyle != null) return _sectionStyle;
    ValueExpression ve = getValueExpression("sectionStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setSectionStyle(String _sectionStyle)
  {
    this._sectionStyle = _sectionStyle;
  }

  public String getSectionStyleClass()
  {
    if (_sectionStyleClass != null) return _sectionStyleClass;
    ValueExpression ve = getValueExpression("sectionStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setSectionStyleClass(String _sectionStyleClass)
  {
    this._sectionStyleClass = _sectionStyleClass;
  }

  public String getUrl()
  {
    if (_url != null) return _url;
    ValueExpression ve = getValueExpression("url");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setUrl(String _url)
  {
    this._url = _url;
  }

  public String getVar()
  {
    return _var;
  }

  public void setVar(String _var)
  {
    this._var = _var;
  }

  public String getDateFormat()
  {
    if (_dateFormat != null) return _dateFormat;
    ValueExpression ve = getValueExpression("dateFormat");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDateFormat(String _dateFormat)
  {
    this._dateFormat = _dateFormat;
  }

  public String getImageHeight()
  {
    if (_imageHeight != null) return _imageHeight;
    ValueExpression ve = getValueExpression("imageHeight");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setImageHeight(String _imageHeight)
  {
    this._imageHeight = _imageHeight;
  }

  public String getImageWidth()
  {
    if (_imageWidth != null) return _imageWidth;
    ValueExpression ve = getValueExpression("imageWidth");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setImageWidth(String _imageWidth)
  {
    this._imageWidth = _imageWidth;
  }

  public String getImageCrop()
  {
    if (_imageCrop != null) return _imageCrop;
    ValueExpression ve = getValueExpression("imageCrop");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setImageCrop(String _imageCrop)
  {
    this._imageCrop = _imageCrop;
  }

  public boolean isRenderSummary()
  {
    if (_renderSummary != null) return _renderSummary.booleanValue();
    ValueExpression ve = getValueExpression("renderSummary");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : Boolean.TRUE.booleanValue();
  }

  public void setRenderSummary(boolean _renderSummary)
  {
    this._renderSummary = _renderSummary;
  }

  public boolean isRenderDate()
  {
    if (_renderDate != null) return _renderDate.booleanValue();
    ValueExpression ve = getValueExpression("renderDate");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : Boolean.TRUE.booleanValue();
  }

  public void setRenderDate(Boolean _renderDate)
  {
    this._renderDate = _renderDate;
  }

  public List getExcludeDrafts()
  {
    if (_excludeDrafts != null) return _excludeDrafts;
    ValueExpression ve = getValueExpression("excludeDrafts");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setExcludeDrafts(List _excludeDrafts)
  {
    this._excludeDrafts = _excludeDrafts;
  }

  public String getDraftText()
  {
    if (_draftText != null) return _draftText;
    ValueExpression ve = getValueExpression("draftText");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDraftText(String _draftText)
  {
    this._draftText = _draftText;
  }

  @Override
  public void processValidators(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processValidators(context);
  }

  @Override
  public void processUpdates(FacesContext context)
  {
    if (context == null) throw new NullPointerException("context");
    if (!isRendered()) return;
    super.processUpdates(context);
    try
    {
      updateModel(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }

  public void updateModel(FacesContext context)
  {
    if (_section != null)
    {
      ValueExpression ve = getValueExpression("section");
      if (ve != null)
      {
        if (!ve.isReadOnly(context.getELContext()))
        {
          ve.setValue(context.getELContext(), _section);
          _section = null;
        }
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    String clientId = getClientId(context);
    ResponseWriter writer = context.getResponseWriter();
    try
    {
      SectionFilter filter = new SectionFilter();

      filter.setFirstResult(0);
      if (_rows != null)
        filter.setMaxResults(_rows.intValue());
      filter.setContent("");

      filter.getSectionId().clear();
      filter.getSectionId().addAll(getSection());

      filter.setStartDateTime(
        TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));
      filter.setEndDateTime(
        TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));

      filter.getExcludeDrafts().clear();
      filter.getExcludeDrafts().addAll(getExcludeDrafts());
               
      List<SectionView> sections = 
        NewsConfigBean.getPort().findNewsBySectionFromCache(filter);
      Translator translator = getTranslator();
      if (sections != null && sections.size() > 0)
        encodeSections(sections, writer, translator, clientId);

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      FacesUtils.addMessage(this, "CAN_NOT_SHOW_NEWS", null,
        FacesMessage.SEVERITY_ERROR);
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[29];
    values[0] = super.saveState(context);
    values[1] = _section;
    values[2] = _rows;
    values[3] = _translator;
    values[4] = _translationGroup;
    values[5] = _dateStyle;
    values[6] = _dateStyleClass;
    values[7] = _headLineStyle;
    values[8] = _headLineStyleClass;
    values[9] = _imageStyle;
    values[10] = _imageStyleClass;
    values[11] = _summaryStyle;
    values[12] = _summaryStyleClass;
    values[13] = _var;
    values[14] = _url;
    values[15] = _style;
    values[16] = _styleClass;
    values[17] = _dateFormat;
    values[18] = _imageWidth;
    values[19] = _imageHeight;
    values[20] = _imageCrop;
    values[21] = _renderSummary;
    values[22] = _excludeDrafts;
    values[23] = _draftText;
    values[24] = _sectionStyle;
    values[25] = _sectionStyleClass;
    values[26] = _renderDate;
    values[27] = _urlSeparator;
    values[28] = _maxSummaryChars;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _section = (List)values[1];
    _rows = (Integer)values[2];
    _translator = (Translator)values[3];
    _translationGroup = (String)values[4];
    _dateStyle = (String)values[5];
    _dateStyleClass = (String)values[6];
    _headLineStyle = (String)values[7];
    _headLineStyleClass = (String)values[8];
    _imageStyle = (String)values[9];
    _imageStyleClass = (String)values[10];
    _summaryStyle = (String)values[11];
    _summaryStyleClass = (String)values[12];
    _var = (String)values[13];
    _url = (String)values[14];
    _style = (String)values[15];
    _styleClass = (String)values[16];
    _dateFormat = (String)values[17];
    _imageWidth = (String)values[18];
    _imageHeight = (String)values[19];
    _imageCrop = (String)values[20];
    _renderSummary = (Boolean)values[21];
    _excludeDrafts = (List)values[22];
    _draftText = (String)values[23];
    _sectionStyle = (String)values[24];
    _sectionStyleClass = (String)values[25];
    _renderDate = (Boolean)values[26];
    _urlSeparator = (String)values[27];    
    _maxSummaryChars = (Integer)values[28];    
  }

//Private
  private void encodeSections(List<SectionView> sections,
    ResponseWriter writer, Translator translator, String clientId)
    throws IOException
  {
    boolean onlyOneSection = (sections.size() == 1);
    for (SectionView section : sections)
    {
      List<NewView> news = section.getNewView();
      if (news != null && news.size() > 0)
      {
        if (!onlyOneSection)
        {
          writer.startElement("div", this);

          String sectionStyle = getSectionStyle();
          if (sectionStyle != null)
            writer.writeAttribute("style", sectionStyle, null);
          String sectionStyleClass = getSectionStyleClass();
          if (sectionStyleClass != null)
            writer.writeAttribute("class", sectionStyleClass, null);
          writer.startElement("div", this);
          writer.writeAttribute("class", "title", null);
          writer.write(getSectionDesc(section.getSectionId()));
          writer.endElement("div");
        }
        encodeNews(news, writer, translator, clientId);
        if (!onlyOneSection)
        {
          writer.endElement("div");
        }
      }
    }
  }

  private void encodeNews(List<NewView> news, ResponseWriter writer,
    Translator translator, String clientId)
    throws IOException
  {
    writer.startElement("ul", this);
    String style = getStyle();
    if (style != null)
      writer.writeAttribute("style", style, null);
    String styleClass = getStyleClass();
    if (styleClass != null)
      writer.writeAttribute("class", styleClass, null);

    int count = 0;
    for (NewView _new : news)
    {
      writer.startElement("li", this);
      String rowClass = (count % 2 == 0) ? "new1" : "new2";
      writer.writeAttribute("class", rowClass, null);
      encodeNew(_new, writer, translator, clientId);
      writer.endElement("li");
      count++;
    }
    writer.endElement("ul");
  }

  private void encodeNew(NewView newView, ResponseWriter writer,
    Translator translator, String clientId) throws IOException
  {
    if (_var != null)
    {
      Map requestMap =
        getFacesContext().getExternalContext().getRequestMap();
      requestMap.put(getVar(), newView);
    }
    
    String headline = newView.getHeadline();
    String url;
    boolean externalURLMode = 
      (headline != null && headline.contains(getUrlSeparator()));
    if (externalURLMode)
    {
      int idx = headline.lastIndexOf(getUrlSeparator());
      url = headline.substring(idx + 3);
      headline = headline.substring(0, idx);
    }
    else
    {
      url = getUrl();
    }    

    if (url != null)
    {
      writer.startElement("a", this);
      writer.writeAttribute("href", url, null);
      if (headline != null)
      {
        String text = translateText(headline, translator, null);
        writer.writeAttribute("aria-label", text, null);
      }
    }
    
    //LEFT column
    String contentId = NewsConfigBean.getListImageContentId(newView);
    boolean hasImage = (contentId != null) && (contentId.length() != 0);
    if (hasImage)
    {
      writer.startElement("div", this);
      writer.writeAttribute("class", "leftColumn", null);
      
      writer.startElement("img", this);

      String imageStyle = getImageStyle();
      if (imageStyle != null)
        writer.writeAttribute("style", imageStyle, null);
      String imageStyleClass = getImageStyleClass();
      if (imageStyleClass != null)
        writer.writeAttribute("class", imageStyleClass, null);

      String imageUrl = MatrixConfig.getProperty("contextPath") +
        IMAGE_SERVLET_PATH + contentId;
      String imageWidth = getImageWidth();
      String imageHeight = getImageHeight();
      if (imageWidth != null && imageHeight != null)
        imageUrl = imageUrl + "?width=" +  String.valueOf(imageWidth)
          + "&height=" + String.valueOf(imageHeight);
      String imageCrop = getImageCrop();
      if (imageCrop != null)
        imageUrl = imageUrl + "&crop=" + imageCrop;

      writer.writeAttribute("src", imageUrl, null);
      writer.writeAttribute("alt", "", null);
      writer.endElement("img");
      
      writer.endElement("div");
    }

    //RIGHT column
    writer.startElement("div", this);  
    writer.writeAttribute("class", hasImage ? "rightColumn" : "column", null);

    //Date division
    if (isRenderDate())
    {
      writer.startElement("div", this);
      String dateStyle = getDateStyle();
      if (dateStyle != null)
        writer.writeAttribute("style", dateStyle, null);
      String dateStyleClass = getDateStyleClass();
      if (dateStyleClass != null)
        writer.writeAttribute("class", dateStyleClass, null);

      String dateFormat = getDateFormat() != null ? getDateFormat() : "dd/MM/yyyy";

      String newDate =
        TextUtils.formatDate(TextUtils.parseInternalDate(newView.getStartDate()),
          dateFormat);

      if (newView.isDraft())
        newDate = newDate + " (" + getDraftText() + ")";

      renderPlainText(newDate, writer, translator, null);
      writer.endElement("div");
    }

    //Headline division
    writer.startElement("p", this);
    String headLineStyle = getHeadLineStyle();
    if (headLineStyle != null)
      writer.writeAttribute("style", headLineStyle, null);
    String headLineStyleClass = getHeadLineStyleClass();
    if (headLineStyleClass != null)
      writer.writeAttribute("class", headLineStyleClass, null);

    renderPlainText(headline, writer, translator, null);
    
    writer.endElement("p"); // headline

    //Summary division
    if (isRenderSummary())
    {
      writer.startElement("p", this);
      String summaryStyle = getSummaryStyle();
      if (summaryStyle != null)
        writer.writeAttribute("style", summaryStyle, null);
      String summaryStyleClass = getSummaryStyleClass();
      if (summaryStyleClass != null)
        writer.writeAttribute("class", summaryStyleClass, null);
      String summary = newView.getSummary();
      if (summary != null && getMaxSummaryChars() > 0)
      {
        summary = HTMLNormalizer.cleanHTML(summary, true);
        int limit = getMaxSummaryChars();
        if (summary.length() <= limit) limit = Integer.MAX_VALUE;
        summary = TextUtils.wordWrap(summary, limit, null);
        renderHtmlText(summary, writer, translator, null);
      }
      writer.endElement("p");
    }
    
    writer.endElement("div"); //right column
    
    if (url != null)
    {
      writer.endElement("a");
    }
  }

  private void renderPlainText(String text, ResponseWriter writer, 
    Translator translator, String trGroupSuffix) throws IOException
  {
    String textToRender = null;
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      if (!translationGroup.contains(":") && trGroupSuffix != null) 
        translationGroup = translationGroup + ":" + trGroupSuffix;      
      StringWriter sw = new StringWriter();
      translator.translate(new StringReader(text), sw, "text/plain",
        userLanguage, translationGroup);
      textToRender = sw.toString();
    }
    else textToRender = text;

    String lines[] = textToRender.split("\n");
    writer.writeText(lines[0], JSFAttr.VALUE_ATTR);
    for (int i = 1; i < lines.length; i++)
    {
      writer.startElement("br", this);
      writer.endElement("br");
      writer.writeText(lines[i], JSFAttr.VALUE_ATTR);
    }
  }

  private void renderHtmlText(String text, ResponseWriter writer, 
    Translator translator, String trGroupSuffix) throws IOException
  {
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      if (!translationGroup.contains(":") && trGroupSuffix != null) 
        translationGroup = translationGroup + ":" + trGroupSuffix;
      translator.translate(new StringReader(text),
        writer, "text/html", userLanguage, translationGroup);
    }
    else writer.write(text);
  }
  
  private String translateText(String text, Translator translator, 
    String trGroupSuffix)
  {    
    try
    {
      if (translator != null)
      {
        String userLanguage = FacesUtils.getViewLanguage();
        String translationGroup = getTranslationGroup();
        if (!translationGroup.contains(":") && trGroupSuffix != null) 
          translationGroup = translationGroup + ":" + trGroupSuffix;
        StringWriter sw = new StringWriter();
        translator.translate(new StringReader(text), sw, "text/plain",
          userLanguage, translationGroup);
        return sw.toString();
      }
    }
    catch (IOException ex)
    {
      
    }
    return text;
  }

  private String getSectionDesc(String mid)
  {
    String result = null;
    try
    {
      MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
        getMenuItemByMid(mid);
      Map nodeProperties = (Map)mic.getDirectProperties();
      result = (String)nodeProperties.get("label");
    }
    catch (Exception ex)
    {

    }
    return (result != null ? result : mid);
  }
  
}
