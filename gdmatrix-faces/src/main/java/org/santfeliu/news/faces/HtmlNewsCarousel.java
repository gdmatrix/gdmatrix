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
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author lopezrj
 */
public class HtmlNewsCarousel extends UIComponentBase
{
  public static final String DOC_SERVLET_PATH = "/documents/";

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
  private String _moreNewsURL;
  private Boolean _excludeDrafts;
  private String _draftText;

  public HtmlNewsCarousel()
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
    return v != null ? v.intValue() : 0;
  }

  public void setRows(int _rows)
  {
    this._rows = _rows;
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
    return vb != null? (String) vb.getValue(getFacesContext()): null;
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
    return vb != null ? (String) vb.getValue(getFacesContext()) : null;
  }

  public void setMoreNewsLabel(String _moreNewsLabel)
  {
    this._moreNewsLabel = _moreNewsLabel;
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
    writer.writeAttribute("src", "/plugins/carousel/carousel.js", null);
    writer.endElement("script");
    try
    {
      SectionFilter filter = new SectionFilter();

      filter.setFirstResult(0);
//      if (_rows != null)
//        filter.setMaxResults(_rows.intValue());
      filter.setMaxResults(getRows());
      filter.setContent("");

      filter.getSectionId().clear();
      filter.getSectionId().add(getSection());

      filter.setStartDateTime(
        TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));
      filter.setEndDateTime(
        TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));

      filter.getExcludeDrafts().add(getExcludeDrafts());

      Translator translator = getTranslator();
      List<NewView> newList = NewsConfigBean.getPort().findNewsBySectionFromCache(filter).
        get(0).getNewView();
      int newCount = 0;
      if (newList != null && newList.size() > 0)
      {
        encodeNews(newList, writer, translator, clientId);
        newCount = newList.size();
      }
      writer.startElement("script", this);
      writer.writeAttribute("type", "text/javascript", null);
      writer.writeText("var newsCarouselNewCount = " + newCount + ";", null);
      writer.writeText("var newsCarouselTransitionTime = " +
        getTransitionTime() + ";", null);
      writer.writeText("var imageURLArray = [];", null);
      int i = 0;
      for (NewView newView : newList)
      {
        writer.writeText("imageURLArray[" + i + "] = '" +
          getNewViewImageURL(newView) + "';", null);
        i++;
      }
      writer.writeText("newsCarouselStart()", null);
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
    Object values[] = new Object[15];
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
    encodeMaskLayer(writer);
    int index = 1;
    for (NewView newView : news)
    {
      writer.startElement("div", this);
      writer.writeAttribute("id", "newsCarouselNewLayer" + index, null);
      writer.writeAttribute("class", "newLayer", null);
      if (index == 1)
      {
        writer.writeAttribute("style", "visibility:visible;", null);
      }
      else
      {
        writer.writeAttribute("style", "visibility:hidden;", null);
      }
      encodeNew(newView, writer, translator, clientId, index, news.size());
      writer.endElement("div");
      index++;
    }
    writer.endElement("div");
  }

  private void encodeMaskLayer(ResponseWriter writer) throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("class", "maskLayer", null);

    writer.startElement("div", this);
    writer.writeAttribute("class", "topLeftCorner", null);
    writer.endElement("div");

    writer.startElement("div", this);
    writer.writeAttribute("class", "topRightCorner", null);
    writer.endElement("div");

    writer.startElement("div", this);
    writer.writeAttribute("class", "bottomLeftCorner", null);
    writer.endElement("div");

    writer.startElement("div", this);
    writer.writeAttribute("class", "bottomRightCorner", null);
    writer.endElement("div");

    writer.startElement("a", this);
    writer.writeAttribute("href", "javascript:newsCarouselGoPrevious();", null);
    writer.startElement("div", this);
    writer.writeAttribute("class", "goPreviousButton", null);
    writer.endElement("div");
    writer.endElement("a");

    writer.startElement("a", this);
    writer.writeAttribute("href", "javascript:newsCarouselGoNext();", null);
    writer.startElement("div", this);
    writer.writeAttribute("class", "goNextButton", null);
    writer.endElement("div");
    writer.endElement("a");

    writer.endElement("div");
  }

  private void encodeNew(NewView newView, ResponseWriter writer,
    Translator translator, String clientId, int index, int newsCount)
    throws IOException
  {
    String var = getVar();
    if (var != null)
    {
      Map requestMap =
        getFacesContext().getExternalContext().getRequestMap();
      requestMap.put(var, newView);
    }
    encodeNewImageLayer(newView, writer, index, newsCount);
    encodeNewInfoLayer(newView, writer, translator, clientId);
  }

  private void encodeNewImageLayer(NewView newView, ResponseWriter writer,
    int index, int newsCount) throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("class", "image", null);
    String docURL = getNewViewImageURL(newView);
    if (docURL != null && docURL.length() > 0)
    {
      writer.startElement("img", this);
      writer.writeAttribute("id", "newsCarouselNewImage" + index, null);
      if (index == newsCount || index == 1 || index == 2) //first load
      {
        writer.writeAttribute("src", docURL, null);
      }
      writer.writeAttribute("alt", newView.getHeadline(), null);
    }
    writer.endElement("div");
  }

  private void encodeNewInfoLayer(NewView newView, ResponseWriter writer,
    Translator translator, String clientId) throws IOException
  {
    writer.startElement("div", this);
    writer.writeAttribute("class", "info", null);
    writer.startElement("div", this);
    writer.writeAttribute("class", "textLayer", null);
    writer.startElement("div", this);
    writer.writeAttribute("class", "headline", null);
    String headline = newView.getHeadline();
    if (headline == null) headline = "";
    String newURL = getNewURL(newView);
    if (newURL != null)
    {
      writer.startElement("a", this);
      writer.writeAttribute("href", newURL, null);
      writer.writeAttribute("name", clientId + ":value", null);
      renderPlainText(headline, writer, translator);
      if (newView.isDraft() && !getExcludeDrafts())
      {
        renderPlainText(" (", writer, null);
        renderPlainText(getDraftText(), writer, translator);
        renderPlainText(")", writer, null);      
      }
      writer.endElement("a");
    }
    else
    {
      renderPlainText(headline, writer, translator);
      if (newView.isDraft() && !getExcludeDrafts())
      {
        renderPlainText(" (", writer, null);
        renderPlainText(getDraftText(), writer, translator);
        renderPlainText(")", writer, null);
      }
    }
    writer.endElement("div");

    writer.startElement("div", this);
    writer.writeAttribute("class", "summary", null);
    String summary = newView.getSummary();
    if (summary == null) summary = "";
    renderHtmlText(summary, writer, translator);
    writer.endElement("div");
    writer.endElement("div");

    writer.startElement("div", this);
    writer.writeAttribute("class", "buttonsLayer", null);
    writer.startElement("div", this);
    writer.writeAttribute("class", "moreInfo", null);
    writer.startElement("a", this);
    writer.writeAttribute("href", newURL, null);
    writer.writeAttribute("class", "showMoreInfoLink", null);
    writer.startElement("span", this);
    renderPlainText(getMoreInfoLabel(), writer, translator);
    writer.endElement("span");
    writer.endElement("a");    
    writer.endElement("div");
    writer.startElement("div", this);
    writer.writeAttribute("class", "moreNews", null);
    writer.startElement("a", this);
    writer.writeAttribute("href", getSectionURL(), null);
    writer.writeAttribute("class", "showMoreNewsLink", null);
    writer.startElement("span", this);
    renderPlainText(getMoreNewsLabel(), writer, translator);
    writer.endElement("span");
    writer.endElement("a");
    writer.endElement("div");
    writer.endElement("div");
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
    String docId = NewsConfigBean.getCarouselImageDocId(newView);
    if (docId == null)
    {
      docId = NewsConfigBean.getDetailsImageDocId(newView);
    }
    if (docId == null)
    {
      docId = NewsConfigBean.getListImageDocId(newView);
    }
    if ((docId != null) && (docId.length() != 0))
    {
      return MatrixConfig.getProperty("contextPath") + DOC_SERVLET_PATH + docId;
    }
    return "";
  }

  private void renderPlainText(String text,
    ResponseWriter writer, Translator translator) throws IOException
  {
    String textToRender = null;
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
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
    ResponseWriter writer, Translator translator) throws IOException
  {
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String translationGroup = getTranslationGroup();
      translator.translate(new StringReader(text),
        writer, "text/html", userLanguage, translationGroup);
    }
    else writer.write(text);
  }

}
