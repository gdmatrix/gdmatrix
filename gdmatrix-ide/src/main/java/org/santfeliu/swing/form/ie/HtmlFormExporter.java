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
package org.santfeliu.swing.form.ie;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.swing.form.util.AccessibilityUtils;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.FormDesigner;
import org.santfeliu.swing.form.view.ButtonView;
import org.santfeliu.swing.form.view.CheckBoxView;
import org.santfeliu.swing.form.view.ImageView;
import org.santfeliu.swing.form.view.InputTextAreaView;
import org.santfeliu.swing.form.view.InputTextView;
import org.santfeliu.swing.form.view.LabelView;
import org.santfeliu.swing.form.view.OutputTextAreaView;
import org.santfeliu.swing.form.view.OutputTextView;
import org.santfeliu.swing.form.view.RadioButtonView;
import org.santfeliu.swing.form.view.ScriptView;
import org.santfeliu.swing.form.view.SelectBoxView;
import org.santfeliu.util.enc.HtmlEncoder;


/**
 *
 * @author realor
 */
public class HtmlFormExporter
{
  public static final String CHARSET = "utf-8";
  public static final String TITLE = "Visual Form";

  public HtmlFormExporter()
  {
  }

  public void exportPanel(OutputStream os, FormDesigner panel) throws Exception
  {
    OutputStreamWriter writer = new OutputStreamWriter(os, CHARSET);
    try
    {
      Dimension dim = panel.getMinimumSize();
      writer.write("<!DOCTYPE HTML PUBLIC " +
        "\"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
        "\"http://www.w3.org/TR/html4/loose.dtd\">\n" +
        "<html><head><meta http-equiv=\"Content-Type\" " +
        "content=\"text/html; charset=" + CHARSET + "\">" +
        "<title>" + TITLE + "</title></head>\n" +
        "<body><div id=\"panel\" style=\"position:relative;width:" +
        dim.width + "px;height:" + (dim.height + 10) + "px\">\n");
      List<ComponentView> componentViewList =
        AccessibilityUtils.getSortedComponentListByOutputOrder(panel.getComponentViews());
      for (ComponentView componentView : componentViewList)
      {
        exportComponent(writer, componentView);
      }
      writer.write("</div></body></html>");
    }
    finally
    {
      writer.close();
    }
  }

  private void exportComponent(Writer writer, ComponentView componentView)
    throws IOException
  {
    if (componentView instanceof LabelView)
    {
      LabelView view = (LabelView)componentView;
      writer.write("<label style=\"" + getStyle(view) +
        ";line-height:" + view.getContentHeight() + "px;overflow:hidden\"");
      writeCommonAttributes(writer, view);
      String forElement = view.getForElement();
      if (forElement != null)
      {
        writer.write(" for=\"" + forElement + "\"");
      }
      String text = view.getText();
      if (text == null) text = "&nbsp;";
      writer.write(">" + HtmlEncoder.encode(text));
      writer.write("</label>\n");
    }
    else if (componentView instanceof OutputTextView)
    {
      OutputTextView view = (OutputTextView)componentView;
      writer.write("<div style=\"" + getStyle(view) +
        ";line-height:" + view.getContentHeight() + "px;overflow:hidden\"");
      writeCommonAttributes(writer, view);
      String text = view.getText();
      if (text == null) text = "&nbsp;";
      writer.write(">" + HtmlEncoder.encode(text));
      writer.write("</div>\n");
    }
    else if (componentView instanceof OutputTextAreaView)
    {
      OutputTextAreaView view = (OutputTextAreaView)componentView;
      writer.write("<div style=\"" + getStyle(view) + ";overflow:auto\"");
      writeCommonAttributes(writer, view);
      String text = view.getText();
      if (text == null) text = "&nbsp;";
      writer.write(">" + text);
      writer.write("</div>\n");
    }
    else if (componentView instanceof InputTextAreaView)
    {
      InputTextAreaView view = (InputTextAreaView)componentView;
      String variable = view.getVariable();
      String format = view.getFormat();
      Integer maxLength = view.getMaxLength();
      writer.write("<textarea name=\"" + variable + "\"");
      if (format != null)
      {
        writer.write(" format=\"" + format + "\"");
      }
      if (maxLength != null && maxLength.intValue() > 0)
      {
        writer.write(" maxlength=\"" + maxLength.intValue() + "\"");
      }
      writeCommonAttributes(writer, view);
      if (view.getTabindex() != null)
      {
        writer.write(" tabindex=\"" + view.getTabindex() + "\"");
      }
      writer.write(" required=\"" + view.isRequired() + "\"");
      if (view.getDisabled() != null && view.getDisabled().trim().length() > 0)
      {
        writer.write(" disabled=\"" + view.getDisabled() + "\"");
      }
      writer.write(" style=\"" + getStyle(view) + "\"></textarea>\n");
    }
    else if (componentView instanceof InputTextView)
    {
      InputTextView view = (InputTextView)componentView;
      String variable = view.getVariable();
      writer.write("<input type=\"text\" name=\"" + variable + "\"");
      Integer maxLength = view.getMaxLength();
      if (maxLength != null && maxLength.intValue() > 0)
      {
        writer.write(" maxlength=\"" + maxLength + "\"");
      }
      String format = view.getFormat();
      if (format != null)
      {
        writer.write(" format=\"" + format + "\"");
      }
      writeCommonAttributes(writer, view);
      if (view.getTabindex() != null)
      {
        writer.write(" tabindex=\"" + view.getTabindex() + "\"");
      }
      writer.write(" required=\"" + view.isRequired() + "\"");
      if (!StringUtils.isBlank(view.getDisabled()))
      {
        writer.write(" disabled=\"" + view.getDisabled() + "\"");
      }
      if (!StringUtils.isBlank(view.getInfoIcon()))
      {
        writer.write(" infoicon=\"" + view.getInfoIcon() + "\"");
      }
      if (!StringUtils.isBlank(view.getInfoText()))
      {
        writer.write(" infotext=\"" + view.getInfoText() + "\"");
      }      
      if (!StringUtils.isBlank(view.getHelpText()))
      {
        writer.write(" helptext=\"" + view.getHelpText() + "\"");
      }          
      writer.write(" style=\"" + getStyle(view) +
        ";line-height:" + view.getContentHeight() + "px\">\n");
    }
    else if (componentView instanceof SelectBoxView)
    {
      SelectBoxView view = (SelectBoxView)componentView;
      int boxHeight = (int)view.getSelectBoxBounds().getHeight();
      int offset = (view.getContentHeight() - boxHeight) / 2;
      int boxTop = view.getY() + offset;

      String style = getStyle(view, false);
      style += ";top:" + boxTop + "px";
      style += ";left:" + view.getX() + "px";
      style += ";width:" + view.getContentWidth() + "px";

      writer.write("<select name=\"" + view.getVariable() +
        "\" style=\"" + style + "\" offset=\"" + offset + "\"");
      writeCommonAttributes(writer, view);
      if (view.getTabindex() != null)
      {
        writer.write(" tabindex=\"" + view.getTabindex() + "\"");
      }
      if (view.getDisabled() != null && view.getDisabled().trim().length() > 0)
      {
        writer.write(" disabled=\"" + view.getDisabled() + "\"");
      }
      if (view.getSize() != null)
      {
        writer.write(" size=\"" + view.getSize() + "\"");
      }
      if (view.getMultiple() != null && view.getMultiple())
      {
        writer.write(" multiple=\"true\"");
      }
      if (!StringUtils.isBlank(view.getInfoIcon()))
      {
        writer.write(" infoicon=\"" + view.getInfoIcon() + "\"");
      } 
      if (!StringUtils.isBlank(view.getInfoText()))
      {
        writer.write(" infotext=\"" + view.getInfoText() + "\"");
      }  
      if (!StringUtils.isBlank(view.getHelpText()))
      {
        writer.write(" helptext=\"" + view.getHelpText() + "\"");
      }       

      // dynamic options
      if (view.getConnection() != null)
      {
        writer.write(" connection=\"" + view.getConnection().trim() + "\"");
      }
      if (view.getSql() != null)
      {
        writer.write(" sql=\"" + encodeAttribute(view.getSql()) + "\"");
      }
      if (view.getUsername() != null)
      {
        writer.write(" username=\"" + view.getUsername().trim() + "\"");
      }
      if (view.getPassword() != null)
      {
        writer.write(" password=\"" + view.getPassword().trim() + "\"");
      }
      if (!StringUtils.isBlank(view.getDataref()))
      {
        writer.write(" dataref=\"" + encodeAttribute(view.getDataref()) + "\"");
      }
      if (!StringUtils.isBlank(view.getOnChange()))
      {
        writer.write(" onchange=\"" + view.getOnChange() + "\"");
      }
      if (view.getTranslate() != null && view.getTranslate())
      {
        writer.write(" translate=\"" + view.getTranslate().toString() + "\"");
      }
      writer.write(">");

      // static options
      Vector options = view.getOptions();
      for (int i = 0; i < options.size(); i++)
      {
        String[] option = (String[])options.elementAt(i);
        writer.write("<option value=\"" + option[1] + "\">" +
          HtmlEncoder.encode(option[0]) + "</option>");
      }
      writer.write("</select>\n");
    }
    else if (componentView instanceof ImageView)
    {
      ImageView view = (ImageView)componentView;
      writer.write("<img src=\"" + view.getUrl() +
        "\" alt=\"" + view.getAlt() +
        "\" style=\"" + getStyle(view) + "\"");
      writeCommonAttributes(writer, view);
      writer.write(">\n");
    }
    else if (componentView instanceof RadioButtonView)
    {
      RadioButtonView view = (RadioButtonView)componentView;
      writer.write("<input type=\"radio\" name=\"" +
        view.getVariable() + "\" value=\"" + view.getValue() +
        "\"" + (view.isChecked() ? " checked=\"true\"" : ""));
      String format = view.getFormat();
      if (format != null)
      {
        writer.write(" format=\"" + format + "\"");
      }
      writeCommonAttributes(writer, view);
      if (view.getTabindex() != null)
      {
        writer.write(" tabindex=\"" + view.getTabindex() + "\"");
      }
      if (!StringUtils.isBlank(view.getOnChange()))
      {
        writer.write(" onchange=\"" + view.getOnChange() + "\"");
      }
      writer.write(" style=\"" + getStyle(view) + "\">\n");
    }
    else if (componentView instanceof CheckBoxView)
    {
      CheckBoxView view = (CheckBoxView)componentView;
      writer.write("<input type=\"checkbox\" name=\"" +
        view.getVariable() + "\"" +
        (view.isChecked() ? " checked=\"true\"" : ""));
      writer.write(" style=\"" + getStyle(view) + "\"");
      writeCommonAttributes(writer, view);
      if (view.getTabindex() != null)
      {
        writer.write(" tabindex=\"" + view.getTabindex() + "\"");
      }
      if (!StringUtils.isBlank(view.getOnChange()))
      {
        writer.write(" onchange=\"" + view.getOnChange() + "\"");
      }
      if (view.getDisabled() != null && view.getDisabled().trim().length() > 0)
      {
        writer.write(" disabled=\"" + view.getDisabled() + "\"");
      }      
      writer.write(">\n");
    }
    else if (componentView instanceof ButtonView)
    {
      ButtonView view = (ButtonView)componentView;
      writer.write("<input type=\"submit\" name=\"" +
        view.getVariable() + "\" value=\"" + view.getText() + "\"");
      writer.write(" style=\"" + getStyle(view) + "\"");
      writeCommonAttributes(writer, view);
      if (view.getTabindex() != null)
      {
        writer.write(" tabindex=\"" + view.getTabindex() + "\"");
      }
      writer.write(">\n");
    }
    else if (componentView instanceof ScriptView)
    {
      ScriptView view = (ScriptView)componentView;
      String code;
      if ("serverscript".equals(view.getType())) // server script
      {
        code = "<!--${" + view.getCode() + "}-->\n";
      }
      else // client script
      {
        code = "<script type=\"" + view.getType() + "\">" +
          view.getCode() + "</script>\n";
      }
      writer.write(code);
    }
  }

  private String getStyle(ComponentView view)
  {
    return getStyle(view, true);
  }

  private String getStyle(ComponentView view, boolean setBounds)
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("position:absolute;padding:0px");
    if (setBounds)
    {
      buffer.append(";left:").append(view.getX()).append("px");
      buffer.append(";top:").append(view.getY()).append("px");
      buffer.append(";width:").append(view.getContentWidth()).append("px");
      buffer.append(";height:").append(view.getContentHeight()).append("px");
    }
    if (view.getTextAlign() != null)
      buffer.append(";text-align:").append(view.getTextAlign());

    if (view.getFontFamily() != null)
      buffer.append(";font-family:").append(view.getFontFamily());

    if (view.getFontSize() > 0)
      buffer.append(";font-size:").append(view.getFontSize()).append("px");

    if (view.getForeground() != null)
      buffer.append(";color:").append(getRGBColor(view.getForeground()));
    if (view.getBackground() != null)
      buffer.append(";background:").append(getRGBColor(view.getBackground()));

    String topWidth = getWidthString(view.getBorderTopWidth());
    if (topWidth != null)
      buffer.append(";border-top-width:").append(topWidth);
    String bottomWidth = getWidthString(view.getBorderBottomWidth());
    if (bottomWidth != null)
      buffer.append(";border-bottom-width:").append(bottomWidth);
    String leftWidth = getWidthString(view.getBorderLeftWidth());
    if (leftWidth != null)
      buffer.append(";border-left-width:").append(leftWidth);
    String rightWidth = getWidthString(view.getBorderRightWidth());
    if (rightWidth != null)
      buffer.append(";border-right-width:").append(rightWidth);

    if (view.getBorderTopColor() != null)
      buffer.append(";border-top-color:").append(getRGBColor(view.getBorderTopColor()));
    if (view.getBorderBottomColor() != null)
      buffer.append(";border-bottom-color:").append(getRGBColor(view.getBorderBottomColor()));
    if (view.getBorderLeftColor() != null)
      buffer.append(";border-left-color:").append(getRGBColor(view.getBorderLeftColor()));
    if (view.getBorderRightColor() != null)
      buffer.append(";border-right-color:").append(getRGBColor(view.getBorderRightColor()));

    if (view.getBorderTopStyle() != null)
      buffer.append(";border-top-style:").append(view.getBorderTopStyle());
    if (view.getBorderBottomStyle() != null)
      buffer.append(";border-bottom-style:").append(view.getBorderBottomStyle());
    if (view.getBorderLeftStyle() != null)
      buffer.append(";border-left-style:").append(view.getBorderLeftStyle());
    if (view.getBorderRightStyle() != null)
      buffer.append(";border-right-style:").append(view.getBorderRightStyle());

    return buffer.toString();
  }

  private String getRGBColor(Color color)
  {
    int rgb = color.getRGB() & 0x00FFFFFF;
    String code = "000000" + Integer.toHexString(rgb).toUpperCase();
    code = code.substring(code.length() - 6);
    return "#" + code;
  }

  private String getWidthString(String text)
  {
    if (text != null)
    {
      text = text.trim();
      if (text.length() > 0)
      {
        char ch = text.charAt(text.length() - 1);
        if (Character.isDigit(ch))
        {
          text = text + "px"; // pixels by default
        }
      }
      else text = null;
    }
    return text;
  }

  private String encodeAttribute(String sql)
  {
    sql = sql.trim();
    sql = sql.replaceAll("\n", "\\\\n");
    return sql;
  }

  private void writeCommonAttributes(Writer writer, ComponentView view)
    throws IOException
  {
    if (view.getId() != null)
    {
      writer.write(" id=\"" + view.getId() + "\"");
    }
    if (view.getStyleClass() != null)
    {
      writer.write(" class=\"" + view.getStyleClass() + "\"");
    }
    if (view.getRenderer() != null)
    {
      writer.write(" renderer=\"" + view.getRenderer() + "\"");
    }
    if (view.getOutputOrder() != null)
    {
      writer.write(" data-outputorder=\"" + view.getOutputOrder() + "\"");
    }
  }
}
