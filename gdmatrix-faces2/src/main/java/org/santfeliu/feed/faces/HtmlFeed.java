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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.matrix.feed.Feed;
import org.matrix.feed.FeedConstants;
import org.matrix.feed.FeedFilter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.feed.util.FeedUtils;
import org.santfeliu.feed.util.FeedUtils.FeedReading;
import org.santfeliu.feed.util.FeedUtils.Row;
import org.santfeliu.feed.web.FeedConfigBean;
import org.santfeliu.misc.widget.WidgetCache;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.enc.HtmlEncoder;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj
 */
public class HtmlFeed extends UIComponentBase
{  
  //Parameters
  private String _url;
  private Integer _rows;
  private String _source;
  private String _style;
  private String _styleClass;
  private String _sourceStyle;
  private String _sourceStyleClass;
  private String _dateStyle;
  private String _dateStyleClass;
  private String _headLineStyle;
  private String _headLineStyleClass;
  private String _summaryStyle;
  private String _summaryStyleClass;
  private String _var;
  private String _dateFormat;
  private Boolean _renderImage;  
  private Boolean _renderSource;
  private Boolean _renderDate;  
  private Boolean _renderHeadLine;    
  private Boolean _renderSummary;
  private String _displayOrder;
  private List _invalidImagePrefixes;
  private List _invalidSummaryStrings;  
  private Integer _summaryMaxSize;
  private Translator _translator;
  private String _translationGroup;
  private Boolean _enableTranslation;
  private List _oneEntrySourceUrls;
  private Integer _headLineMaxSize;  
  private Map<String, String> feedIconMap = new HashMap<String, String>();

  public HtmlFeed()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "Feed";
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
  
  public int getSummaryMaxSize()
  {
    if (_summaryMaxSize != null) return _summaryMaxSize.intValue();
    ValueExpression ve = getValueExpression("summaryMaxSize");
    Integer v = ve != null ? (Integer)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.intValue() : 0;
  }

  public void setSummaryMaxSize(int _summaryMaxSize)
  {
    this._summaryMaxSize = _summaryMaxSize;
  }  

  public String getSource()
  {
    if (_source != null) return _source;
    ValueExpression ve = getValueExpression("source");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setSource(String _source)
  {
    this._source = _source;
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

  public String getSourceStyle()
  {
    if (_sourceStyle != null) return _sourceStyle;
    ValueExpression ve = getValueExpression("sourceStyle");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setSourceStyle(String _sourceStyle)
  {
    this._sourceStyle = _sourceStyle;
  }

  public String getSourceStyleClass()
  {
    if (_sourceStyleClass != null) return _sourceStyleClass;
    ValueExpression ve = getValueExpression("sourceStyleClass");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setSourceStyleClass(String _sourceStyleClass)
  {
    this._sourceStyleClass = _sourceStyleClass;
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

  public boolean isRenderImage()
  {
    if (_renderImage != null) return _renderImage.booleanValue();
    ValueExpression ve = getValueExpression("renderImage");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : Boolean.TRUE.booleanValue();
  }

  public void setRenderImage(boolean _renderImage)
  {
    this._renderImage = _renderImage;
  }
  
  public boolean isRenderSource()
  {
    if (_renderSource != null) return _renderSource.booleanValue();
    ValueExpression ve = getValueExpression("renderSource");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : Boolean.TRUE.booleanValue();
  }

  public void setRenderSource(boolean _renderSource)
  {
    this._renderSource = _renderSource;
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

  public boolean isRenderHeadLine()
  {
    if (_renderHeadLine != null) return _renderHeadLine.booleanValue();
    ValueExpression ve = getValueExpression("renderHeadLine");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : Boolean.TRUE.booleanValue();
  }

  public void setRenderHeadLine(Boolean _renderHeadLine)
  {
    this._renderHeadLine = _renderHeadLine;
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
  
  public String getDisplayOrder()
  {
    if (_displayOrder != null) return _displayOrder;
    ValueExpression ve = getValueExpression("displayOrder");
    return ve != null ? (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDisplayOrder(String _displayOrder)
  {
    this._displayOrder = _displayOrder;
  }
  
  public List getInvalidImagePrefixes()
  {
    if (_invalidImagePrefixes != null) return _invalidImagePrefixes;
    ValueExpression ve = getValueExpression("invalidImagePrefixes");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setInvalidImagePrefixes(List _invalidImagePrefixes)
  {
    this._invalidImagePrefixes = _invalidImagePrefixes;
  }

  public List getInvalidSummaryStrings()
  {
    if (_invalidSummaryStrings != null) return _invalidSummaryStrings;
    ValueExpression ve = getValueExpression("invalidSummaryStrings");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setInvalidSummaryStrings(List _invalidSummaryStrings)
  {
    this._invalidSummaryStrings = _invalidSummaryStrings;
  }

  public List getOneEntrySourceUrls()
  {
    if (_oneEntrySourceUrls != null) return _oneEntrySourceUrls;
    ValueExpression ve = getValueExpression("oneEntrySourceUrls");
    return ve != null ? (List)ve.getValue(getFacesContext().getELContext()) : null;    
  }

  public void setOneEntrySourceUrls(List _oneEntrySourceUrls)
  {
    this._oneEntrySourceUrls = _oneEntrySourceUrls;
  }

  public int getHeadLineMaxSize()
  {
    if (_headLineMaxSize != null) return _headLineMaxSize.intValue();
    ValueExpression ve = getValueExpression("headLineMaxSize");
    Integer v = ve != null ? (Integer)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.intValue() : 0;
  }

  public void setHeadLineMaxSize(int _headLineMaxSize)
  {
    this._headLineMaxSize = _headLineMaxSize;
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
    return ve != null? (String) ve.getValue(getFacesContext().getELContext()): "feed";
  }  
  
  public boolean isEnableTranslation()
  {
    if (_enableTranslation != null) return _enableTranslation.booleanValue();
    ValueExpression ve = getValueExpression("enableTranslation");
    Boolean v = ve != null ? (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : Boolean.FALSE.booleanValue();
  }

  public void setEnableTranslation(boolean _enableTranslation)
  {
    this._enableTranslation = _enableTranslation;
  }  

  private String getEntryIconUrl(String feedUrl)
  {    
    if (!feedIconMap.containsKey(feedUrl))
    {
      FeedFilter filter = new FeedFilter();
      filter.setUrl(feedUrl);
      try
      {
        List<Feed> feedList = FeedConfigBean.getPort().findFeedsFromCache(filter);
        if (feedList.isEmpty())
        {
          feedIconMap.put(feedUrl, null);
        }
        else
        {
          feedIconMap.put(feedUrl, feedList.get(0).getIconUrl());
        }
      }
      catch (Exception ex) 
      { 
        feedIconMap.put(feedUrl, null);
      }      
    }
    return feedIconMap.get(feedUrl);    
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
    if (_url != null)
    {
      ValueExpression ve = getValueExpression("url");
      if (ve != null)
      {
        if (!ve.isReadOnly(context.getELContext()))
        {
          ve.setValue(context.getELContext(), _url);
          _url = null;
        }
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    try
    {
      FeedReading feedReading = new FeedReading();
      feedReading.setFeedUrl(getUrl());
      feedReading.setFeedTitle(getSource());
      if (isLimitByFeedUrl())
      {
        feedReading.setRowCount(Integer.MAX_VALUE);
      }
      else
      {
        feedReading.setRowCount(getRows());
      }
      feedReading.setIncludeSource(true);
      feedReading.setIncludeImages(isRenderImage());
      if (isRenderImage())
      {
        feedReading.setInvalidImagePrefixList(getInvalidImagePrefixes());
      }

      List<Row> rowList;
      String nodeId = (String)getAttributes().get("nodeId");
      if (nodeId != null)
      {
        String workspaceId = UserSessionBean.getCurrentInstance().getWorkspaceId();        
        rowList = WidgetCache.getInstance().getWidgetObjects(workspaceId, nodeId);
        if (rowList == null)
        {
          try
          {            
            rowList = FeedUtils.getRowList(feedReading);
          }
          catch (Exception ex) 
          { 
            rowList = new ArrayList<Row>();
          }          
          WidgetCache.getInstance().setWidgetObjects(workspaceId, nodeId, rowList);
        }        
      }
      else
      {
        try
        {
          rowList = FeedUtils.getRowList(feedReading);
        }
        catch (Exception ex) 
        { 
          rowList = new ArrayList<Row>();
        }        
      }
      
      Translator translator = null;
      if (isEnableTranslation())
      {
        translator = getTranslator();
      }      
      encodeRows(rowList, writer, translator);
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
    Object values[] = new Object[30];
    values[0] = super.saveState(context);
    values[1] = _url;
    values[2] = _rows;
    values[3] = _source;
    values[4] = _style;
    values[5] = _styleClass;
    values[6] = _sourceStyle;
    values[7] = _sourceStyleClass;    
    values[8] = _dateStyle;
    values[9] = _dateStyleClass;
    values[10] = _headLineStyle;
    values[11] = _headLineStyleClass;
    values[12] = _summaryStyle;
    values[13] = _summaryStyleClass;
    values[14] = _var;
    values[15] = _dateFormat;
    values[16] = _renderImage;
    values[17] = _renderSource;
    values[18] = _renderDate;
    values[19] = _renderHeadLine;
    values[20] = _renderSummary;
    values[21] = _displayOrder;
    values[22] = _invalidImagePrefixes;
    values[23] = _summaryMaxSize;
    values[24] = _invalidSummaryStrings;
    values[25] = _translator;
    values[26] = _translationGroup;
    values[27] = _enableTranslation;
    values[28] = _oneEntrySourceUrls;
    values[29] = _headLineMaxSize;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _url = (String)values[1];
    _rows = (Integer)values[2];
    _source = (String)values[3];
    _style = (String)values[4];
    _styleClass = (String)values[5];
    _sourceStyle = (String)values[6];
    _sourceStyleClass = (String)values[7];
    _dateStyle = (String)values[8];
    _dateStyleClass = (String)values[9];
    _headLineStyle = (String)values[10];
    _headLineStyleClass = (String)values[11];
    _summaryStyle = (String)values[12];
    _summaryStyleClass = (String)values[13];
    _var = (String)values[14];
    _dateFormat = (String)values[15];
    _renderImage = (Boolean)values[16];
    _renderSource = (Boolean)values[17];
    _renderDate = (Boolean)values[18];
    _renderHeadLine = (Boolean)values[19];
    _renderSummary = (Boolean)values[20];
    _displayOrder = (String)values[21];
    _invalidImagePrefixes = (List)values[22];
    _summaryMaxSize = (Integer)values[23];
    _invalidSummaryStrings = (List)values[24];
    _translator = (Translator)values[25];
    _translationGroup = (String)values[26];
    _enableTranslation = (Boolean)values[27];
    _oneEntrySourceUrls = (List)values[28];
    _headLineMaxSize = (Integer)values[29];
  }

//Private
  private void encodeRows(List<Row> rows, ResponseWriter writer, Translator translator) 
    throws IOException
  {
    Set<String> feedUrlSet = new HashSet();
    writer.startElement("ul", this);
    String style = getStyle();
    if (style != null)
      writer.writeAttribute("style", style, null);
    String styleClass = getStyleClass();
    if (styleClass != null)
      writer.writeAttribute("class", styleClass, null);

    int count = 0;
    for (int i = 0; i < rows.size() && count < getRows(); i++)
    {
      Row row = rows.get(i);
      String sourceUrl = row.getSourceUrl();
      if (sourceUrl != null) 
      {
        sourceUrl = sourceUrl.replace("http://", "").replace("https://", "");
      }
      if (sourceUrl != null && isLimitByFeedUrl() && 
        getOneEntrySourceUrls().contains(sourceUrl) &&
        feedUrlSet.contains(sourceUrl))
      {
        //nothing here
      }
      else
      {
        writer.startElement("li", this);
        String rowClass = (count % 2 == 0) ? "row1" : "row2";
        writer.writeAttribute("class", rowClass, null);
        encodeRow(row, writer, getInvalidSummaryStrings(), translator);
        writer.endElement("li");
        count++;
        feedUrlSet.add(sourceUrl);
      }
    }
    writer.endElement("ul");
  }

  private void encodeRow(Row row, ResponseWriter writer, 
    List invalidSummaryStrings, Translator translator) throws IOException
  {
    if (_var != null)
    {
      Map requestMap =
        getFacesContext().getExternalContext().getRequestMap();
      requestMap.put(getVar(), row);
    }
    
    //Source
    if (isRenderSource())
    {
      writer.startElement("div", this);
      String style = getSourceStyle();
      if (style != null)
        writer.writeAttribute("style", style, null);
      String styleClass = getSourceStyleClass();
      if (styleClass != null)
        writer.writeAttribute("class", styleClass, null);        
      writer.startElement("a", this);
      writer.writeAttribute("href", row.getSourceUrl(), null);
      writer.writeAttribute("target", "_blank", null);      
      if (row.getSourceTitle() != null)
      {
        String openNewWindowLabel = 
          MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");        
        String translatedLabel = 
          translateText(openNewWindowLabel, translator, null);
        writer.writeAttribute("aria-label", 
          (row.getSourceTitle() + " (" + translatedLabel + ")"), null);
      }
      renderPlainText(row.getSourceTitle(), writer, null, null);
      writer.endElement("a");        
      writer.endElement("div");
    }
    
    if (row.getUrl() != null)
    {
      writer.startElement("a", this);
      writer.writeAttribute("href", row.getUrl(), null);
      if (row.getHeadLine() != null)
      {
        String ariaLabel = row.getHeadLine();
        if (FeedConstants.DEFAULT_EMPTY_ENTRY_HEADLINE.equals(ariaLabel))
        {
          ariaLabel = getCommonAriaLabel(row);
        }
        ariaLabel = HTMLNormalizer.cleanHTML(ariaLabel, true);
        String openNewWindowLabel = 
          MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
        ariaLabel = (ariaLabel != null ? 
          (ariaLabel + " (" + openNewWindowLabel + ")") : null);
        ariaLabel = translateText(ariaLabel, translator, null);
        writer.writeAttribute("aria-label", ariaLabel, null);
      }
      writer.writeAttribute("target", "_blank", null);        
    }    
    
    //Image
    if (isRenderImage())
    {
      String imageUrl = row.getImageUrl();
      if (imageUrl == null)
      {
        if (row.getSourceUrl() != null && !row.getSourceUrl().isEmpty()) 
          imageUrl = getEntryIconUrl(row.getSourceUrl());        
      }
      if (imageUrl != null)
      {        
        writer.startElement("div", this);
        writer.writeAttribute("class", "leftColumn", null);
        writer.startElement("img", this);
        writer.writeAttribute("src", getProxyUrl(imageUrl), null);
        writer.writeAttribute("alt", "", null);        
        writer.endElement("img");
        writer.endElement("div");
      }
    }    

    //Content: source, date, headline & summary
    writer.startElement("div", this);
    writer.writeAttribute("class", "rightColumn", null);
    
    for (String orderItem : getDisplayOrder().split(","))
    {
      //Date
      if ("date".equals(orderItem) && isRenderDate())
      {
        writer.startElement("div", this);      
        String style = getDateStyle();
        if (style != null)
          writer.writeAttribute("style", style, null);
        String styleClass = getDateStyleClass();
        if (styleClass != null)
          writer.writeAttribute("class", styleClass, null);
        if (row.getDate() != null)
        {
          String dateFormat = getDateFormat() != null ? getDateFormat() : 
            "dd/MM/yyyy";        
          try
          {
            String rowDate = TextUtils.formatDate(
              TextUtils.parseInternalDate(row.getDate()), dateFormat);
            renderPlainText(rowDate, writer, translator, null);        
          }
          catch (Exception ex) { }
        }
        writer.endElement("div");
      }

      //Headline
      if ("headLine".equals(orderItem) && isRenderHeadLine())
      {
        writer.startElement("p", this);
        String style = getHeadLineStyle();
        if (style != null)
          writer.writeAttribute("style", style, null);
        String styleClass = getHeadLineStyleClass();
        if (styleClass != null)
          writer.writeAttribute("class", styleClass, null);
        String headLine = HtmlEncoder.encode(row.getHeadLine());
        headLine = HTMLNormalizer.cleanHTML(headLine, true);
        if (getHeadLineMaxSize() > 0)
        {
          headLine = TextUtils.wordWrap(headLine, getHeadLineMaxSize(), null);
        }        
        renderHtmlText(headLine, writer, translator, null);
        writer.endElement("p");
      }

      //Summary
      if ("summary".equals(orderItem) && isRenderSummary())
      {
        String summary = row.getSummary();
        if (isValidSummary(summary, invalidSummaryStrings))
        {
          writer.startElement("p", this);
          String style = getSummaryStyle();
          if (style != null)
            writer.writeAttribute("style", style, null);
          String styleClass = getSummaryStyleClass();
          if (styleClass != null)
            writer.writeAttribute("class", styleClass, null); 
          summary = HTMLNormalizer.cleanHTML(summary, true);
          if (getSummaryMaxSize() > 0)
          {
            summary = TextUtils.wordWrap(summary, getSummaryMaxSize(), null);
          }
          renderHtmlText(summary, writer, translator, null);
          writer.endElement("p");
        }
      }      
    }
    writer.endElement("div");
        
    if (row.getUrl() != null)
    {
      writer.endElement("a");      
    }

  }

  private boolean isLimitByFeedUrl()
  {
    return (getOneEntrySourceUrls() != null && 
      !getOneEntrySourceUrls().isEmpty());
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
  
  private boolean isValidSummary(String summary, List invalidSummaryStrings)
  {
    if (summary != null && invalidSummaryStrings != null)
    {
      for (Object invalidString : invalidSummaryStrings)
      {
        if (summary.toLowerCase().contains(
          ((String)invalidString).toLowerCase())) return false; 
      }
    }
    return true;
  }   

  private String getProxyUrl(String url)
  {
    HttpServletRequest request = (HttpServletRequest)FacesContext.
      getCurrentInstance().getExternalContext().getRequest();
    if (request.isSecure())
    {
      String encodedUrl;
      try
      {
        encodedUrl = URLEncoder.encode(url, "UTF-8");
      }
      catch (Exception ex)
      {
        encodedUrl = url;
      }
      return ("/proxy?url=" + encodedUrl);
    }
    else
    {
      return url;
    }
  }
  
  private String getCommonAriaLabel(Row row)
  {
    StringBuilder sb = new StringBuilder();
    String entryDate = null;
    if (row.getDate() != null)
    {
      String dateFormat = getDateFormat() != null ? getDateFormat() : 
        "dd/MM/yyyy HH:mm";        
      try
      {
        entryDate = TextUtils.formatDate(
          TextUtils.parseInternalDate(row.getDate()), dateFormat);        
      }
      catch (Exception ex) { }
    }
    sb.append("Enllaç a ");
    sb.append(row.getSourceTitle());
    if (entryDate != null)
    {
      sb.append(" amb data ");
      sb.append(entryDate);      
    }
    return sb.toString();
  }
  
}
