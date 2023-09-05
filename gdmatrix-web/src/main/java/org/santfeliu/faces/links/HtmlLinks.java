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
package org.santfeliu.faces.links;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.Utilities;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj-sf
 */
@FacesComponent(value = "HtmlLinks")
public class HtmlLinks extends UIComponentBase
{
  private static final String IMAGE_SERVLET_PATH = "/imgscale/";

  private String _nodeId;
  private Integer _rows;
  private Translator _translator;
  private String _translationGroup;
  private String _style;
  private String _styleClass;
  private String _labelStyle;
  private String _labelStyleClass;
  private String _imageStyle;
  private String _imageStyleClass;
  private String _descriptionStyle;
  private String _descriptionStyleClass;
  private String _imageWidth;
  private String _imageHeight;
  private String _imageCrop;
  private Boolean _renderDescription;
  private Integer _maxDescriptionChars;
  private String _row1StyleClass;
  private String _row2StyleClass;

  public HtmlLinks()
  {
    setRendererType(null);
  }

  @Override
  public String getFamily()
  {
    return "Links";
  }

  public String getNodeId()
  {
    if (_nodeId != null) return _nodeId;
    ValueExpression ve = getValueExpression("nodeId");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setNodeId(String _nodeId)
  {
    this._nodeId = _nodeId;
  }

  public int getRows()
  {
    if (_rows != null) return _rows;
    ValueExpression ve = getValueExpression("rows");
    Integer v = ve != null ?
      (Integer)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v : 0;
  }

  public void setRows(int _rows)
  {
    this._rows = _rows;
  }

  public int getMaxDescriptionChars()
  {
    if (_maxDescriptionChars != null) return _maxDescriptionChars;
    ValueExpression ve = getValueExpression("maxDescriptionChars");
    Integer v = ve != null ?
      (Integer)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v : 0;
  }

  public void setMaxDescriptionChars(int _maxDescriptionChars)
  {
    this._maxDescriptionChars = _maxDescriptionChars;
  }

  public Translator getTranslator()
  {
    if (_translator != null) return _translator;
    ValueExpression ve = getValueExpression("translator");
    return ve != null ?
      (Translator)ve.getValue(getFacesContext().getELContext()): null;
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
    if (_translationGroup != null) return _translationGroup;
    ValueExpression ve = getValueExpression("translationGroup");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()): "link";
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueExpression ve = getValueExpression("style");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyle(String _style)
  {
    this._style = _style;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueExpression ve = getValueExpression("styleClass");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setStyleClass(String _styleClass)
  {
    this._styleClass = _styleClass;
  }

  public String getLabelStyle()
  {
     if (_labelStyle != null) return _labelStyle;
    ValueExpression ve = getValueExpression("labelStyle");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setLabelStyle(String _labelStyle)
  {
    this._labelStyle = _labelStyle;
  }

  public String getLabelStyleClass()
  {
    if (_labelStyleClass != null) return _labelStyleClass;
    ValueExpression ve = getValueExpression("labelStyleClass");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setLabelStyleClass(String _labelStyleClass)
  {
    this._labelStyleClass = _labelStyleClass;
  }

  public String getImageStyle()
  {
    if (_imageStyle != null) return _imageStyle;
    ValueExpression ve = getValueExpression("imageStyle");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setImageStyle(String _imageStyle)
  {
    this._imageStyle = _imageStyle;
  }

  public String getImageStyleClass()
  {
    if (_imageStyleClass != null) return _imageStyleClass;
    ValueExpression ve = getValueExpression("imageStyleClass");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setImageStyleClass(String _imageStyleClass)
  {
    this._imageStyleClass = _imageStyleClass;
  }

  public String getDescriptionStyle()
  {
    if (_descriptionStyle != null) return _descriptionStyle;
    ValueExpression ve = getValueExpression("descriptionStyle");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDescriptionStyle(String _descriptionStyle)
  {
    this._descriptionStyle = _descriptionStyle;
  }

  public String getDescriptionStyleClass()
  {
    if (_descriptionStyleClass != null) return _descriptionStyleClass;
    ValueExpression ve = getValueExpression("descriptionStyleClass");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setDescriptionStyleClass(String _descriptionStyleClass)
  {
    this._descriptionStyleClass = _descriptionStyleClass;
  }

  public String getImageHeight()
  {
    if (_imageHeight != null) return _imageHeight;
    ValueExpression ve = getValueExpression("imageHeight");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setImageHeight(String _imageHeight)
  {
    this._imageHeight = _imageHeight;
  }

  public String getImageWidth()
  {
    if (_imageWidth != null) return _imageWidth;
    ValueExpression ve = getValueExpression("imageWidth");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setImageWidth(String _imageWidth)
  {
    this._imageWidth = _imageWidth;
  }

  public String getImageCrop()
  {
    if (_imageCrop != null) return _imageCrop;
    ValueExpression ve = getValueExpression("imageCrop");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setImageCrop(String _imageCrop)
  {
    this._imageCrop = _imageCrop;
  }

  public boolean isRenderDescription()
  {
    if (_renderDescription != null) return _renderDescription;
    ValueExpression ve = getValueExpression("renderDescription");
    Boolean v = ve != null ?
      (Boolean)ve.getValue(getFacesContext().getELContext()) : null;
    return v != null ? v.booleanValue() : Boolean.TRUE.booleanValue();
  }

  public void setRenderDescription(boolean _renderDescription)
  {
    this._renderDescription = _renderDescription;
  }

  public String getRow1StyleClass()
  {
    if (_row1StyleClass != null) return _row1StyleClass;
    ValueExpression ve = getValueExpression("row1StyleClass");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setRow1StyleClass(String _row1StyleClass)
  {
    this._row1StyleClass = _row1StyleClass;
  }

  public String getRow2StyleClass()
  {
    if (_row2StyleClass != null) return _row2StyleClass;
    ValueExpression ve = getValueExpression("row2StyleClass");
    return ve != null ?
      (String)ve.getValue(getFacesContext().getELContext()) : null;
  }

  public void setRow2StyleClass(String _row2StyleClass)
  {
    this._row2StyleClass = _row2StyleClass;
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
  }

  @Override
  public void encodeBegin(FacesContext context)
  {
    if (!isRendered()) return;
    ResponseWriter writer = context.getResponseWriter();
    try
    {
      encodeLinks(writer);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      FacesUtils.addMessage(this, "CAN_NOT_SHOW_LINKS", null,
        FacesMessage.SEVERITY_ERROR);
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[20];
    values[0] = super.saveState(context);
    values[1] = _nodeId;
    values[2] = _rows;
    values[3] = _labelStyle;
    values[4] = _labelStyleClass;
    values[5] = _imageStyle;
    values[6] = _imageStyleClass;
    values[7] = _descriptionStyle;
    values[8] = _descriptionStyleClass;
    values[9] = _style;
    values[10] = _styleClass;
    values[11] = _imageWidth;
    values[12] = _imageHeight;
    values[13] = _imageCrop;
    values[14] = _renderDescription;
    values[15] = _maxDescriptionChars;
    values[16] = _translator;
    values[17] = _translationGroup;
    values[18] = _row1StyleClass;
    values[19] = _row2StyleClass;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _nodeId = (String)values[1];
    _rows = (Integer)values[2];
    _labelStyle = (String)values[3];
    _labelStyleClass = (String)values[4];
    _imageStyle = (String)values[5];
    _imageStyleClass = (String)values[6];
    _descriptionStyle = (String)values[7];
    _descriptionStyleClass = (String)values[8];
    _style = (String)values[9];
    _styleClass = (String)values[10];
    _imageWidth = (String)values[11];
    _imageHeight = (String)values[12];
    _imageCrop = (String)values[13];
    _renderDescription = (Boolean)values[14];
    _maxDescriptionChars = (Integer)values[15];
    _translator = (Translator)values[16];
    _translationGroup = (String)values[17];
    _row1StyleClass = (String)values[18];
    _row2StyleClass = (String)values[19];
  }

  private void encodeLinks(ResponseWriter writer)
    throws IOException
  {
    writer.startElement("ul", this);
    String style = getStyle();
    if (style != null)
      writer.writeAttribute("style", style, null);
    String styleClass = getStyleClass();
    if (styleClass != null)
      writer.writeAttribute("class", styleClass, null);

    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    MenuItemCursor rootNode =
      userSessionBean.getMenuModel().getMenuItem(getNodeId());
    if (!rootNode.isNull())
    {
      Translator translator = getTranslator();
      int i = 0;
      MenuItemCursor cursor = rootNode.getFirstChild();
      int maxRows = getRows();
      while (!cursor.isNull() && i < maxRows)
      {
        writer.startElement("li", this);
        String rowClass = (i % 2 == 0) ? getRow1StyleClass() :
          getRow2StyleClass();
        writer.writeAttribute("class", rowClass, null);
        encodeLink(cursor, writer, translator);
        writer.endElement("li");
        i++;
        cursor.moveNext();
      }
    }
    writer.endElement("ul");
  }

  private void encodeLink(MenuItemCursor cursor, ResponseWriter writer,
    Translator translator) throws IOException
  {
    String url = cursor.getDirectProperty("url");
    String label = cursor.getDirectProperty("linkLabel");
    if (label == null)
      label = cursor.getDirectProperty("label");
    String description = cursor.getDirectProperty("linkDescription");
    if (description == null)
      description = cursor.getDirectProperty("description");
    String imageUrl = cursor.getDirectProperty("imageUrl");
    String target = cursor.getDirectProperty("target");

    if (url != null)
    {
      writer.startElement("a", this);
      writer.writeAttribute("href", url, null);
      if (target != null) writer.writeAttribute("target", target, null);
      if (label != null)
      {
        String text = translateText(label, translator, null);
        if ("_blank".equals(target))
        {
          String openNewWindowLabel =
            MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
          String translatedLabel =
            translateText(openNewWindowLabel, translator, null);
          text = text + " (" + translatedLabel + ")";
        }
        writer.writeAttribute("aria-label", text, null);
      }
    }

    //LEFT column
    boolean hasImage = (imageUrl != null) && (imageUrl.length() != 0);
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

      String imageId = null;
      try
      {
        imageId = String.valueOf(Integer.parseInt(imageUrl)); //docId
      }
      catch (NumberFormatException ex)
      {
        if (Utilities.isUUID(imageUrl))
        {
          imageId = imageUrl;
        }
      }

      if (imageId != null) //Image stored in database
      {
        imageUrl = MatrixConfig.getProperty("contextPath") +
          IMAGE_SERVLET_PATH + imageId;
        String imageWidth = getImageWidth();
        String imageHeight = getImageHeight();
        String imageCrop = getImageCrop();
        if (imageWidth != null || imageHeight != null || imageCrop != null)
        {
          StringBuilder imgTransform = new StringBuilder();
          if (imageWidth != null && imageHeight != null)
          {
            imgTransform.append("?width=");
            imgTransform.append(String.valueOf(imageWidth));
            imgTransform.append("&height=");
            imgTransform.append(String.valueOf(imageHeight));
          }
          if (imageCrop != null)
          {
            imgTransform.append(imgTransform.length() == 0 ? "?": "&");
            imgTransform.append("crop=");
            imgTransform.append(imageCrop);
          }
          imageUrl += imgTransform.toString();
        }
      }

      writer.writeAttribute("src", imageUrl, null);
      writer.writeAttribute("alt", "", null);
      writer.endElement("img");

      writer.endElement("div");
    }

    //RIGHT column
    writer.startElement("div", this);
    writer.writeAttribute("class", hasImage ? "rightColumn" : "column", null);

    //Label division
    writer.startElement("p", this);
    String labelStyle = getLabelStyle();
    if (labelStyle != null)
      writer.writeAttribute("style", labelStyle, null);
    String labelStyleClass = getLabelStyleClass();
    if (labelStyleClass != null)
      writer.writeAttribute("class", labelStyleClass, null);
    renderPlainText(label, writer, translator, null);
    writer.endElement("p"); //label

    //Summary division
    if (isRenderDescription())
    {
      writer.startElement("p", this);
      String descriptionStyle = getDescriptionStyle();
      if (descriptionStyle != null)
        writer.writeAttribute("style", descriptionStyle, null);
      String descriptionStyleClass = getDescriptionStyleClass();
      if (descriptionStyleClass != null)
        writer.writeAttribute("class", descriptionStyleClass, null);
      if (description != null && getMaxDescriptionChars() > 0)
      {
        description = HTMLNormalizer.cleanHTML(description, true);
        int limit = getMaxDescriptionChars();
        if (description.length() <= limit) limit = Integer.MAX_VALUE;
        description = TextUtils.wordWrap(description, limit, null);
        renderHtmlText(description, writer, translator, null);
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
    String textToRender;
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

}
