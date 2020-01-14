package org.santfeliu.faces.component;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;

/**
 *
 * @author realor
 */
public class HtmlSplitter extends UIPanel
{
  // orientation
  public static final String VERTICAL = "vertical";
  public static final String HORIZONTAL = "horizontal";

  // stretch
  public static final String WIDTH = "width";
  public static final String HEIGHT = "height";
  public static final String BOTH = "both";

  private static final String JS_SPLITTER_ENCODED = "JS_SPLITTER_ENCODED";

  private String _orientation;
  private String _stretch;
  private String _style;
  private String _styleClass;  
  private String _firstStyle;
  private String _firstStyleClass;
  private String _lastStyle;
  private String _lastStyleClass;  

  public HtmlSplitter()
  {
    setRendererType(null);
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  public void setOrientation(String orientation)
  {
    this._orientation = orientation;
  }

  public String getOrientation()
  {
    if (_orientation != null) return _orientation;
    ValueBinding vb = getValueBinding("orientation");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStretch(String stretch)
  {
    this._stretch = stretch;
  }

  public String getStretch()
  {
    if (_stretch != null) return _stretch;
    ValueBinding vb = getValueBinding("stretch");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyle(String style)
  {
    this._style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    this._styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setFirstStyle(String firstStyle)
  {
    this._firstStyle = firstStyle;
  }

  public String getFirstStyle()
  {
    if (_firstStyle != null) return _firstStyle;
    ValueBinding vb = getValueBinding("firstStyle");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setFirstStyleClass(String firstStyleClass)
  {
    this._firstStyleClass = firstStyleClass;
  }

  public String getFirstStyleClass()
  {
    if (_firstStyleClass != null) return _firstStyleClass;
    ValueBinding vb = getValueBinding("firstStyleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setLastStyle(String lastStyle)
  {
    this._lastStyle = lastStyle;
  }

  public String getLastStyle()
  {
    if (_lastStyle != null) return _lastStyle;
    ValueBinding vb = getValueBinding("lastStyle");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setLastStyleClass(String lastStyleClass)
  {
    this._lastStyleClass = lastStyleClass;
  }

  public String getLastStyleClass()
  {
    if (_lastStyleClass != null) return _lastStyleClass;
    ValueBinding vb = getValueBinding("lastStyleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    String clientId = getClientId(context);
    
    Map requestMap = context.getExternalContext().getRequestMap();
    if (requestMap.get(JS_SPLITTER_ENCODED) == null)
    {
      requestMap.put(JS_SPLITTER_ENCODED, "true");
      encodeJavascript(context, writer);
    }
    writer.startElement("div", this);
    writer.writeAttribute("id", clientId, null);
    String style = getStyle();
    if (style != null)
      writer.writeAttribute("style", style, null);
    String styleClass = getStyleClass();
    if (styleClass != null)
      writer.writeAttribute("class", styleClass, null);
  }

  @Override
  public void encodeChildren(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    String clientId = getClientId(context);

    // begin first facet
    writer.startElement("div", this);
    writer.writeAttribute("id", clientId + "_first", null);
    String firstStyle = getFirstStyle();
    if (firstStyle != null)
      writer.writeAttribute("style", firstStyle, null);
    String firstStyleClass = getFirstStyleClass();
    if (firstStyleClass != null)
      writer.writeAttribute("class", firstStyleClass, null);

    UIComponent first = getFacet("first");
    if (first != null && first.isRendered())
    {
      RendererUtils.renderChild(context, first);
    }
    writer.endElement("div");
    // end first facet

    // begin last facet
    writer.startElement("div", this);
    writer.writeAttribute("id", clientId + "_last", null);
    String lastStyle = getLastStyle();
    if (lastStyle != null)
      writer.writeAttribute("style", lastStyle, null);
    String lastStyleClass = getLastStyleClass();
    if (lastStyleClass != null)
      writer.writeAttribute("class", lastStyleClass, null);

    UIComponent last = getFacet("last");
    if (last != null && last.isRendered())
    {
      RendererUtils.renderChild(context, last);
    }
    writer.endElement("div");
    // end last facet
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    ResponseWriter writer = context.getResponseWriter();
    String clientId = getClientId(context);

    writer.endElement("div");

    // position: input hidden
    writer.startElement("input", null);
    writer.writeAttribute("id", clientId + "_pos", null);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", clientId + "_pos", null);
    writer.writeAttribute("value", "200", null);
    writer.endElement("input");

    // script
    String position = "200";
    String stretch = getStretch();
    boolean stretchWidth = WIDTH.equals(stretch) || BOTH.equals(stretch);
    boolean stretchHeight = HEIGHT.equals(stretch) || BOTH.equals(stretch);

    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    writer.writeText("new Splitter(\"" + clientId + "\"," + 
      position + "," + stretchWidth + "," + stretchHeight + ");", null);
    writer.endElement("script");
  }

  @Override
  public void decode(FacesContext context)
  {
    Map paramsMap = context.getExternalContext().getRequestParameterMap();
    String clientId = getClientId(context);

    String position = (String)paramsMap.get(clientId + "_pos");
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[9];
    values[0] = super.saveState(context);
    values[1] = _orientation;
    values[2] = _stretch;
    values[3] = _style;
    values[4] = _styleClass;
    values[5] = _firstStyle;
    values[6] = _firstStyleClass;
    values[7] = _lastStyle;
    values[8] = _lastStyleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _orientation = (String)values[1];
    _stretch = (String)values[2];
    _style = (String)values[3];
    _styleClass = (String)values[4];
    _firstStyle = (String)values[5];
    _firstStyleClass = (String)values[6];
    _lastStyle = (String)values[7];
    _lastStyleClass = (String)values[8];
  }

  private void encodeJavascript(FacesContext context, ResponseWriter writer)
    throws IOException
  {
    writer.startElement("script", this);
    writer.writeAttribute("type", "text/javascript", null);
    String contextPath = context.getExternalContext().getRequestContextPath();
    writer.writeAttribute("src",
      contextPath + "/plugins/splitter/splitter.js", null);
    writer.endElement("script");
  }
}
