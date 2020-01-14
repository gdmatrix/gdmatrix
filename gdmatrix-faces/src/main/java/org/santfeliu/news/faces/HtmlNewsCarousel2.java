package org.santfeliu.news.faces;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.matrix.news.NewView;
import org.matrix.news.SectionFilter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.news.web.NewsConfigBean;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.ApplicationBean;

/**
 *
 * @author lopezrj
 */
public class HtmlNewsCarousel2 extends UIComponentBase
{
  public static final String IMAGE_SERVLET_PATH = "/imgscale/";

  private String _section;
  private Integer _rows;
  private Integer _transitionTime;
  private Translator _translator;
  private String _translationGroup;
  private String _style;
  private String _styleClass;
  private String _var;
  private String _moreInfoLabel;
  private String _moreInfoURL;
  private String _moreNewsLabel;
  private String _moreNewsAriaLabel;
  private String _moreNewsURL;
  private String _prevBlockLabel;
  private String _prevBlockIconURL;
  private String _showBlockLabel;
  private String _nextBlockLabel;
  private String _nextBlockIconURL;
  private Boolean _excludeDrafts;
  private String _draftText;
  private String _imageWidth;
  private String _imageHeight;
  private String _imageCrop;  
  private Integer _maxSummaryChars;
  private Boolean _renderDate;  
  private Integer _newsPerBlock;

  public HtmlNewsCarousel2()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "News";
  }

  public String getSection()
  {
    if (_section != null) return _section;
    ValueBinding vb = getValueBinding("section");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setSection(String _section)
  {
    this._section = _section;
  }

  public int getRows()
  {
    if (_rows != null) return _rows.intValue();
    ValueBinding vb = getValueBinding("rows");
    Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
    return v != null ? v.intValue() : 9;
  }

  public void setRows(int _rows)
  {
    this._rows = _rows;
  }

  public int getNewsPerBlock()
  {
    if (_newsPerBlock != null) return _newsPerBlock.intValue();
    ValueBinding vb = getValueBinding("newsPerBlock");
    Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
    return v != null ? v.intValue() : 3;    
  }

  public void setNewsPerBlock(int _newsPerBlock)
  {
    this._newsPerBlock = _newsPerBlock;
  }

  public int getTransitionTime()
  {
    if (_transitionTime != null) return _transitionTime.intValue();
    ValueBinding vb = getValueBinding("transitionTime");
    Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
    return v != null ? v.intValue() : 0;
  }

  public void setTransitionTime(int _transitionTime)
  {
    this._transitionTime = _transitionTime;
  }

  public int getMaxSummaryChars()
  {
    if (_maxSummaryChars != null) return _maxSummaryChars.intValue();
    ValueBinding vb = getValueBinding("maxSummaryChars");
    Integer v = vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
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
    ValueBinding vb = getValueBinding("translator");
    return vb != null? (Translator) vb.getValue(getFacesContext()): null;
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
    ValueBinding vb = getValueBinding("translationGroup");
    return vb != null? (String) vb.getValue(getFacesContext()): "newsCarousel";
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyle(String _style)
  {
    this._style = _style;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyleClass(String _styleClass)
  {
    this._styleClass = _styleClass;
  }

  public String getVar()
  {
    return _var;
  }

  public void setVar(String _var)
  {
    this._var = _var;
  }

  public String getMoreInfoLabel()
  {
    if (_moreInfoLabel != null)
      return _moreInfoLabel;
    ValueBinding vb = getValueBinding("moreInfoLabel");
    return vb != null ? (String) vb.getValue(getFacesContext()) : null;
  }

  public void setMoreInfoLabel(String _moreInfoLabel)
  {
    this._moreInfoLabel = _moreInfoLabel;
  }

  public String getMoreInfoURL()
  {
    if (_moreInfoURL != null) return _moreInfoURL;
    ValueBinding vb = getValueBinding("moreInfoURL");
    return vb != null ? (String) vb.getValue(getFacesContext()) : null;
  }

  public void setMoreInfoURL(String _moreInfoURL)
  {
    this._moreInfoURL = _moreInfoURL;
  }

  public String getMoreNewsLabel()
  {
    if (_moreNewsLabel != null)
      return _moreNewsLabel;
    ValueBinding vb = getValueBinding("moreNewsLabel");
    return vb != null ? (String) vb.getValue(getFacesContext()) : "Més notícies";
  }

  public void setMoreNewsLabel(String _moreNewsLabel)
  {
    this._moreNewsLabel = _moreNewsLabel;
  }

  public String getMoreNewsAriaLabel()
  {
    if (_moreNewsAriaLabel != null)
      return _moreNewsAriaLabel;
    ValueBinding vb = getValueBinding("moreNewsAriaLabel");
    return vb != null ? (String) vb.getValue(getFacesContext()) : "Més notícies";
  }

  public void setMoreNewsAriaLabel(String _moreNewsAriaLabel)
  {
    this._moreNewsAriaLabel = _moreNewsAriaLabel;
  }
  
  public String getPrevBlockLabel()
  {
    if (_prevBlockLabel != null)
      return _prevBlockLabel;
    ValueBinding vb = getValueBinding("prevBlockLabel");
    return vb != null ? (String) vb.getValue(getFacesContext()) : "Mostrar bloc anterior";    
  }

  public void setPrevBlockLabel(String _prevBlockLabel)
  {
    this._prevBlockLabel = _prevBlockLabel;
  }

  public String getShowBlockLabel()
  {
    if (_showBlockLabel != null)
      return _showBlockLabel;
    ValueBinding vb = getValueBinding("showBlockLabel");
    return vb != null ? (String) vb.getValue(getFacesContext()) : "Mostrar bloc";    
  }

  public void setShowBlockLabel(String _showBlockLabel)
  {
    this._showBlockLabel = _showBlockLabel;
  }

  public String getNextBlockLabel()
  {
    if (_nextBlockLabel != null)
      return _nextBlockLabel;
    ValueBinding vb = getValueBinding("nextBlockLabel");
    return vb != null ? (String) vb.getValue(getFacesContext()) : "Mostrar bloc següent";    
  }

  public void setNextBlockLabel(String _nextBlockLabel)
  {
    this._nextBlockLabel = _nextBlockLabel;
  }

  public String getPrevBlockIconURL()
  {
    if (_prevBlockIconURL != null) return _prevBlockIconURL;
    ValueBinding vb = getValueBinding("prevBlockIconURL");
    return vb != null ? (String) vb.getValue(getFacesContext()) : null;
  }

  public void setPrevBlockIconURL(String _prevBlockIconURL)
  {
    this._prevBlockIconURL = _prevBlockIconURL;
  }

  public String getNextBlockIconURL()
  {
    if (_nextBlockIconURL != null) return _nextBlockIconURL;
    ValueBinding vb = getValueBinding("nextBlockIconURL");
    return vb != null ? (String) vb.getValue(getFacesContext()) : null;
  }

  public void setNextBlockIconURL(String _nextBlockIconURL)
  {
    this._nextBlockIconURL = _nextBlockIconURL;
  }
  
  public String getMoreNewsURL()
  {
    if (_moreNewsURL != null) return _moreNewsURL;
    ValueBinding vb = getValueBinding("moreNewsURL");
    return vb != null ? (String) vb.getValue(getFacesContext()) : null;
  }

  public void setMoreNewsURL(String _moreNewsURL)
  {
    this._moreNewsURL = _moreNewsURL;
  }

  public boolean getExcludeDrafts()
  {
    if (_excludeDrafts != null) return _excludeDrafts.booleanValue();
    ValueBinding vb = getValueBinding("excludeDrafts");
    Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
    return v != null ? v.booleanValue() : Boolean.FALSE.booleanValue();
  }

  public void setExcludeDrafts(boolean _excludeDrafts)
  {
    this._excludeDrafts = _excludeDrafts;
  }

  public String getDraftText()
  {
    if (_draftText != null) return _draftText;
    ValueBinding vb = getValueBinding("draftText");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setDraftText(String _draftText)
  {
    this._draftText = _draftText;
  }

  public String getImageHeight()
  {
    if (_imageHeight != null) return _imageHeight;
    ValueBinding vb = getValueBinding("imageHeight");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setImageHeight(String _imageHeight)
  {
    this._imageHeight = _imageHeight;
  }

  public String getImageWidth()
  {
    if (_imageWidth != null) return _imageWidth;
    ValueBinding vb = getValueBinding("imageWidth");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setImageWidth(String _imageWidth)
  {
    this._imageWidth = _imageWidth;
  }

  public String getImageCrop()
  {
    if (_imageCrop != null) return _imageCrop;
    ValueBinding vb = getValueBinding("imageCrop");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setImageCrop(String _imageCrop)
  {
    this._imageCrop = _imageCrop;
  }

  public boolean isRenderDate()
  {
    if (_renderDate != null) return _renderDate.booleanValue();
    ValueBinding vb = getValueBinding("renderDate");
    Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
    return v != null ? v.booleanValue() : Boolean.FALSE.booleanValue();
  }

  public void setRenderDate(Boolean _renderDate)
  {
    this._renderDate = _renderDate;
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
      ValueBinding vb = getValueBinding("section");
      if (vb != null)
      {
        if (!vb.isReadOnly(context))
        {
          vb.setValue(context, _section);
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
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeAttribute("src", "/plugins/carousel2/carousel.js?v=" + 
      ApplicationBean.getCurrentInstance().getResourcesVersion(), null);
    writer.endElement("script");
    try
    {
      SectionFilter filter = new SectionFilter();

      filter.setFirstResult(0);

      filter.setMaxResults(getRows());
      filter.setContent("");

      filter.getSectionId().clear();
      filter.getSectionId().add(getSection());

      filter.setStartDateTime(
        TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));
      filter.setEndDateTime(
        TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));

      filter.getExcludeDrafts().add(getExcludeDrafts());
      
      List<NewView> newList = NewsConfigBean.getPort().
        findNewsBySectionFromCache(filter).get(0).getNewView();
      
      int newCount = 0;
      if (newList != null && newList.size() > 0)
      {
        Translator translator = getTranslator();
        encodeNews(newList, writer, translator, clientId);
        newCount = newList.size();
      }
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeText("var newsCarouselNewCount = " + newCount + ";", null);
      writer.writeText("var newsCarouselBlockSize = " + getNewsPerBlock() + ";", null);
      writer.writeText("var newsCarouselIntervalId = 0;", null);
      writer.writeText("var newsCarouselIndex = 1;", null);      
      writer.writeText("var newsCarouselTransitionTime = " +
        getTransitionTime() + ";", null);
      writer.writeText("newsCarouselMakeNavPanelVisible();", null);
      writer.writeText("newsCarouselChange(1);", null);
      writer.writeText("newsCarouselStart();", null);
      writer.endElement("script");
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
    Object values[] = new Object[27];
    values[0] = super.saveState(context);
    values[1] = _section;
    values[2] = _rows;
    values[3] = _translator;
    values[4] = _translationGroup;
    values[5] = _var;
    values[6] = _style;
    values[7] = _styleClass;
    values[8] = _transitionTime;
    values[9] = _moreInfoLabel;
    values[10] = _moreNewsLabel;
    values[11] = _moreInfoURL;
    values[12] = _moreNewsURL;
    values[13] = _excludeDrafts;
    values[14] = _draftText;
    values[15] = _imageWidth;
    values[16] = _imageHeight;
    values[17] = _imageCrop; 
    values[18] = _maxSummaryChars;     
    values[19] = _renderDate;
    values[20] = _newsPerBlock;
    values[21] = _prevBlockLabel;
    values[22] = _showBlockLabel;
    values[23] = _nextBlockLabel; 
    values[24] = _moreNewsAriaLabel;
    values[25] = _prevBlockIconURL;
    values[26] = _nextBlockIconURL;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _section = (String)values[1];
    _rows = (Integer)values[2];
    _translator = (Translator)values[3];
    _translationGroup = (String)values[4];
    _var = (String)values[5];
    _style = (String)values[6];
    _styleClass = (String)values[7];
    _transitionTime = (Integer)values[8];
    _moreInfoLabel = (String)values[9];
    _moreNewsLabel = (String)values[10];
    _moreInfoURL = (String)values[11];
    _moreNewsURL = (String)values[12];
    _excludeDrafts = (Boolean)values[13];
    _draftText = (String)values[14];
    _imageWidth = (String)values[15];
    _imageHeight = (String)values[16];
    _imageCrop = (String)values[17];    
    _maxSummaryChars = (Integer)values[18];
    _renderDate = (Boolean)values[19];    
    _newsPerBlock = (Integer)values[20];
    _prevBlockLabel = (String)values[21];
    _showBlockLabel = (String)values[22];
    _nextBlockLabel = (String)values[23];
    _moreNewsAriaLabel = (String)values[24];
    _prevBlockIconURL = (String)values[25];
    _nextBlockIconURL = (String)values[26];    
  }

//Private
  private void encodeNews(List<NewView> news, ResponseWriter writer,
    Translator translator, String clientId)
    throws IOException
  {
    writer.startElement("div", this); 
    String style = getStyle();
    if (style != null) writer.writeAttribute("style", style, null);
    String styleClass = getStyleClass();
    if (styleClass != null) writer.writeAttribute("class", styleClass, null);
    int index = 1;
    for (NewView newView : news)
    {
      encodeImageLayer(newView, writer, index);
      index++;
    }          
    encodeInfoLayer(news, writer, translator, clientId);
    encodeBottomLayer(writer, translator, news.size());
    writer.endElement("div");
  }

  private void encodeImageLayer(NewView newView, ResponseWriter writer,
    int index) throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("id", "newsCarouselImageLayer" + index, null);
    writer.writeAttribute("class", "imageLayer", null);
    writer.writeAttribute("aria-hidden", "true", null);
    if (index == 1)
    {
      writer.writeAttribute("style", "visibility:visible;", null);
    }
    else
    {
      writer.writeAttribute("style", "visibility:hidden;", null);
    }
    encodeNewImage(newView, writer, index);
    writer.endElement("div");
  }

  private void encodeNewImage(NewView newView, ResponseWriter writer, int index) 
    throws IOException
  {
    String var = getVar();
    if (var != null)
    {
      Map requestMap =
        getFacesContext().getExternalContext().getRequestMap();
      requestMap.put(var, newView);
    }
    String docURL = getNewViewImageURL(newView);
    if (docURL != null && docURL.length() > 0)
    {
      Translator translator = getTranslator();
      String trHeadline = "";
      if (newView.getHeadline() != null)
      {
        trHeadline = translateText(newView.getHeadline(), translator, null);
      }      
      String newURL = getNewURL(newView);
      if (newURL != null)
      {
        writer.startElement("a", this);
        writer.writeAttribute("href", newURL, null);        
        writer.writeAttribute("title", trHeadline, null);
      }
      writer.startElement("img", this);
      writer.writeAttribute("id", "newsCarouselNewImage" + index, null);
      writer.writeAttribute("class", "image", null);
      writer.writeAttribute("src", docURL, null);
      writer.writeAttribute("alt", trHeadline, null);      
      if (newURL != null)
      {
        writer.endElement("a");
      }
    }
  }

  private void encodeInfoLayer(List<NewView> news, ResponseWriter writer,
    Translator translator, String clientId) throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("class", "infoLayer", null);
        
    int index = 1;
    for (NewView newView : news)
    {
      if (getVar() != null)
      {
        Map requestMap =
          getFacesContext().getExternalContext().getRequestMap();
        requestMap.put(getVar(), newView);
      }      
      writer.startElement("div", this);
      writer.writeAttribute("id", "newsCarouselInfoLayer" + index, null);
      writer.writeAttribute("onmouseover", "newsCarouselMouseOverItem(" + index + ");", null);
      writer.writeAttribute("onfocus", "newsCarouselMouseOverItem(" + index + ");", null);      
      writer.writeAttribute("onmouseout", "newsCarouselMouseOutItem();", null);
      writer.writeAttribute("onblur", "newsCarouselMouseOutItem();", null);
      if (index >= 1 && index <= getNewsPerBlock())
      {
        writer.writeAttribute("style", "display:block;", null);        
      }
      else
      {
        writer.writeAttribute("style", "display:none;", null);        
      }
      
      if (index == 1)
      {
        writer.writeAttribute("class", "newInfo selected", null);
      }
      else
      {
        writer.writeAttribute("class", "newInfo", null);
      }
      String newURL = getNewURL(newView);
      if (newURL != null)
      {
        writer.startElement("a", this);
        writer.writeAttribute("href", newURL, null);
        if (newView.getHeadline() != null)
        {
          String trHeadline = translateText(newView.getHeadline(), translator, 
            null);
          writer.writeAttribute("aria-label", trHeadline, null);
        }
      }
      
      writer.startElement("p", this);
      writer.writeAttribute("class", "headline", null);
      String headline = newView.getHeadline();
      if (headline == null) headline = "";
      renderPlainText(headline, writer, translator, null);
      if (newView.isDraft() && !getExcludeDrafts())
      {
        renderPlainText(" (", writer, null, null);
        renderPlainText(getDraftText(), writer, translator, null);
        renderPlainText(")", writer, null, null);
      }
      writer.endElement("p"); //.headline

      writer.startElement("p", this);
      writer.writeAttribute("class", "summary", null);
      if (isRenderDate())
      {
        String dateString = null;
        try
        {
          Date d = TextUtils.parseInternalDate(newView.getStartDate());
          if (d != null) dateString = TextUtils.formatDate(d, "dd/MM/yyyy");                    
        }
        catch (Exception ex) { }
        if (dateString != null)
        {
          writer.startElement("span", this);
          writer.writeAttribute("class", "date", null);
          writer.write(dateString);
          writer.endElement("span");          
        }
      }      
      String summary = newView.getSummary();
      if (summary == null) summary = ""; 
      summary = HTMLNormalizer.cleanHTML(summary, true);      
      if (getMaxSummaryChars() > 0)
      {
        summary = TextUtils.wordWrap(summary, getMaxSummaryChars(), null);
      }      
      renderHtmlText(summary, writer, translator, null);      
      writer.endElement("p"); //.summary

      if (newURL != null)
      {
        writer.endElement("a");
      }      
      
      writer.endElement("div"); //#newsCarouselInfoLayer<x>
      index++;
    }    
    writer.endElement("div");
  }
    
  private void encodeBottomLayer(ResponseWriter writer, Translator translator, 
    int newCount) throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("class", "bottomLayer", null);
    encodeNavPanelLayer(writer, translator, newCount);
    encodeMoreNewsLayer(writer, translator);
    writer.endElement("div");    
  }
  
  private void encodeNavPanelLayer(ResponseWriter writer, Translator translator, int newCount) 
    throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("id", "newsCarouselNavPanel", null);        
    writer.writeAttribute("style", "visibility:hidden", null);
    writer.writeAttribute("class", "navPanel", null);

    writer.startElement("div", this);
    writer.writeAttribute("class", "goPrevious", null);
    writer.startElement("a", this);
    writer.writeAttribute("href", "javascript:newsCarouselGoPrevious();", null);
    String prevLabel = translateText(getPrevBlockLabel(), translator, null);    
    writer.writeAttribute("title", prevLabel, null);
    //writer.write("&nbsp;");
    writer.startElement("img", this);
    writer.writeAttribute("src", getPrevBlockIconURL(), null);
    writer.writeAttribute("alt", prevLabel, null);    
    writer.endElement("a");
    writer.endElement("div");
    
    int blockCount = (newCount + getNewsPerBlock() - 1) / getNewsPerBlock();    
    
    for (int i = 1; i <= blockCount; i++)
    {
      writer.startElement("div", this);
      writer.writeAttribute("id", "newsCarouselPageSelect" + i, null);    
      writer.writeAttribute("class", "goToPage" + (i == 1 ? " selected" : ""), null);
      writer.startElement("a", this);
      writer.writeAttribute("href", "javascript:newsCarouselChange(" + 
        String.valueOf(1 + ((i - 1) * getNewsPerBlock())) + ");", null);
      String idxLabel = translateText(getShowBlockLabel() + " " + i, translator, null);      
      writer.writeAttribute("title", idxLabel, null);          
      writer.writeAttribute("aria-label", idxLabel, null);          
      writer.write(String.valueOf(i));
      writer.endElement("a");
      writer.endElement("div");
    }
    
    writer.startElement("div", this);
    writer.writeAttribute("class", "goNext", null);
    writer.startElement("a", this);
    writer.writeAttribute("href", "javascript:newsCarouselGoNext();", null);
    String nextLabel = translateText(getNextBlockLabel(), translator, null);     
    writer.writeAttribute("title", nextLabel, null); 
    //writer.write("&nbsp;");
    writer.startElement("img", this);
    writer.writeAttribute("src", getNextBlockIconURL(), null);
    writer.writeAttribute("alt", nextLabel, null);
    
    writer.endElement("a");
    writer.endElement("div");
    
    writer.endElement("div");
  }  
  
  private void encodeMoreNewsLayer(ResponseWriter writer, Translator translator) 
    throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("class", "moreNews", null);
    writer.startElement("a", this);
    writer.writeAttribute("href", getSectionURL(), null);
    writer.writeAttribute("class", "showMoreNewsLink", null);
    String showMoreAriaLabel = translateText(getMoreNewsAriaLabel(), translator, null);    
    writer.writeAttribute("title", showMoreAriaLabel, null);
    writer.writeAttribute("aria-label", showMoreAriaLabel, null);    
    writer.startElement("span", this);
    String showMoreLabel = translateText(getMoreNewsLabel(), translator, null);
    writer.write(showMoreLabel);
    writer.endElement("span");
    writer.endElement("a");
    writer.endElement("div");     
  }  

  private String getNewURL(NewView newView)
  {
    String moreInfoURL = getMoreInfoURL();
    if (moreInfoURL != null) 
    {
      return moreInfoURL;
    }
    else
    {
      return MatrixConfig.getProperty("contextPath") +
        "/go.faces?xmid=" + getSection() + "&newid=" + newView.getNewId();
    }
  }

  private String getSectionURL()
  {
    String moreNewsURL = getMoreNewsURL();
    if (moreNewsURL != null)
    {
      return moreNewsURL;
    }
    else
    {
      return MatrixConfig.getProperty("contextPath") +
        "/go.faces?xmid=" + getSection();
    }
  }

  private String getNewViewImageURL(NewView newView)
  {
    String contentId = NewsConfigBean.getCarouselImageContentId(newView);
    if (contentId == null)
    {
      contentId = NewsConfigBean.getDetailsImageContentId(newView);
    }
    if (contentId == null)
    {
      contentId = NewsConfigBean.getListImageContentId(newView);
    }
    if ((contentId != null) && (contentId.length() != 0))      
    {
      return MatrixConfig.getProperty("contextPath") +
        IMAGE_SERVLET_PATH + contentId +
        "?width=" + getImageWidth() + 
        "&height=" + getImageHeight() + 
        "&crop=" + getImageCrop();      
    }
    return "";
  }

  private void renderPlainText(String text,
    ResponseWriter writer, Translator translator, String trGroupSuffix) throws IOException
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

  private void renderHtmlText(String text,
    ResponseWriter writer, Translator translator, String trGroupSuffix) throws IOException
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
    String trGroupSuffix) throws IOException
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
    return text;
  }  

}
