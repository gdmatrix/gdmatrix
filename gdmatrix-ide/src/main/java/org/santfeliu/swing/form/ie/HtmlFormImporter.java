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
import java.awt.Rectangle;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
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
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;


/**
 *
 * @author realor
 */
public class HtmlFormImporter
{
  public HtmlFormImporter()
  {
  }

  public void importPanel(InputStream is, FormDesigner panel)
  {
    Tidy tidy = new Tidy();
    tidy.setOnlyErrors(true);
    tidy.setInputEncoding(HtmlFormExporter.CHARSET);

    Document document = tidy.parseDOM(is, null);
    Node node = document.getFirstChild();
    node = findNode(node, "html");
    if (node != null)
    {
      node = node.getFirstChild();
      node = findNode(node, "body");
      if (node != null)
      {
        node = node.getFirstChild();
        node = findNode(node, "div");
        if (node != null)
        {
          node = node.getFirstChild();
          while (node != null)
          {
            importComponent(node, panel);
            node = node.getNextSibling();
          }
        }
      }
    }
  }

  private void importComponent(Node node, FormDesigner panel)
  {
    if (node instanceof Element)
    {
      Element element = (Element)node;
      String name = element.getNodeName();
      if (name.equalsIgnoreCase("div"))
      {
        Map styles = getStyles(element.getAttribute("style"));
        if (styles.containsKey("line-height"))
        {
          // OutputText
          OutputTextView view = new OutputTextView();
          view.setBounds(new Rectangle(100, 100, 100, 50));
          applyStyles(view, styles);
          view.setId(element.getAttribute("id"));
          view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
          view.setStyleClass(element.getAttribute("class"));
          view.setRenderer(element.getAttribute("renderer"));
          panel.addComponentView(view);
          Node child = element.getFirstChild();
          if (child instanceof Text)
          {
            Text textElem = (Text)child;
            String text = textElem.getData();
            if (text != null)
            {
              // &nbsp; (Java char: 160)
              if (text.charAt(0) == 160) text = null;
            }
            view.setText(text);
          }
        }
        else
        {
          // OutputTextArea
          OutputTextAreaView view = new OutputTextAreaView();
          view.setBounds(new Rectangle(100, 100, 100, 50));
          applyStyles(view, styles);
          view.setId(element.getAttribute("id"));
          view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
          view.setStyleClass(element.getAttribute("class"));
          view.setRenderer(element.getAttribute("renderer"));
          panel.addComponentView(view);
          String text = getNodeContent(element, 0);
          if (text != null)
          {
            // &nbsp; (Java char: 160)
            if (text.length() == 0) text = null;
            else if (text.charAt(0) == 160) text = null;
          }
          view.setText(text);
        }
      }
      else if (name.equalsIgnoreCase("label"))
      {
        // Label
        LabelView view = new LabelView();
        view.setBounds(new Rectangle(100, 100, 100, 50));
        applyStyles(view, getStyles(element.getAttribute("style")));
        view.setId(element.getAttribute("id"));
        view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
        view.setStyleClass(element.getAttribute("class"));
        view.setRenderer(element.getAttribute("renderer"));
        view.setForElement(element.getAttribute("for"));
        panel.addComponentView(view);
        Node child = element.getFirstChild();
        if (child instanceof Text)
        {
          Text textElem = (Text)child;
          String text = textElem.getData();
          if (text != null)
          {
            // &nbsp; (Java char: 160)
            if (text.charAt(0) == 160) text = null;
          }
          view.setText(text);
        }
      }
      else if (name.equalsIgnoreCase("input"))
      {
        String type = element.getAttribute("type");
        if ("text".equalsIgnoreCase(type))
        {
          // InputText
          InputTextView view = new InputTextView();
          view.setBounds(new Rectangle(200, 200, 100, 50));
          applyStyles(view, getStyles(element.getAttribute("style")));
          view.setId(element.getAttribute("id"));
          view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
          view.setStyleClass(element.getAttribute("class"));
          view.setRenderer(element.getAttribute("renderer"));
          view.setVariable(element.getAttribute("name"));
          view.setMaxLength(getInteger(element.getAttribute("maxlength")));
          view.setFormat(element.getAttribute("format"));
          view.setTabindex(getInteger(element.getAttribute("tabindex")));
          view.setRequired("true".equals(element.getAttribute("required")));
          view.setDisabled(element.getAttribute("disabled"));
          panel.addComponentView(view);
        }
        else if ("radio".equalsIgnoreCase(type))
        {
          // RadioButton
           RadioButtonView view = new RadioButtonView();
           view.setBounds(new Rectangle(200, 200, 16, 16));
           applyStyles(view, getStyles(element.getAttribute("style")));
           view.setId(element.getAttribute("id"));
           view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
           view.setStyleClass(element.getAttribute("class"));
           view.setRenderer(element.getAttribute("renderer"));
           view.setVariable(element.getAttribute("name"));
           view.setValue(element.getAttribute("value"));
           view.setFormat(element.getAttribute("format"));
           view.setTabindex(getInteger(element.getAttribute("tabindex")));
           view.setChecked(element.getAttribute("checked").length() > 0);
           view.setOnChange(element.getAttribute("onchange"));
           panel.addComponentView(view);
        }
        else if ("checkbox".equalsIgnoreCase(type))
        {
          // CheckBox
          CheckBoxView view = new CheckBoxView();
          view.setBounds(new Rectangle(200, 200, 16, 16));
          view.setId(element.getAttribute("id"));
          view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
          view.setStyleClass(element.getAttribute("class"));
          view.setRenderer(element.getAttribute("renderer"));
          applyStyles(view, getStyles(element.getAttribute("style")));
          view.setVariable(element.getAttribute("name"));
          view.setChecked(element.getAttribute("checked").length() > 0);
          view.setTabindex(getInteger(element.getAttribute("tabindex")));
          view.setOnChange(element.getAttribute("onchange"));
          panel.addComponentView(view);
        }
        else if ("submit".equalsIgnoreCase(type))
        {
          // submit button
          ButtonView view = new ButtonView();
          view.setBounds(new Rectangle(200, 200, 16, 16));
          view.setId(element.getAttribute("id"));
          view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
          view.setStyleClass(element.getAttribute("class"));
          view.setRenderer(element.getAttribute("renderer"));
          applyStyles(view, getStyles(element.getAttribute("style")));
          view.setVariable(element.getAttribute("name"));
          view.setText(element.getAttribute("value"));
          view.setTabindex(getInteger(element.getAttribute("tabindex")));
          panel.addComponentView(view);
        }
      }
      else if (name.equalsIgnoreCase("select"))
      {
        // SelectBox
        SelectBoxView view = new SelectBoxView();
        view.setBounds(new Rectangle(300, 200, 200, 0));
        String value = element.getAttribute("size");
        if (value != null && value.length() > 0)
        {
          try
          {
            view.setSize(new Integer(value));
          }
          catch (NumberFormatException ex)
          {
          }
        }
        applyStyles(view, getStyles(element.getAttribute("style")));
        if (view.getHeight() == 0)
        {
          // apply layout correction
          String off = element.getAttribute("offset");
          int offset = (StringUtils.isBlank(off)) ? 0 : Integer.parseInt(off);
          int boxHeight = (int)view.getSelectBoxBounds().getHeight();
          int viewHeight = 2 * offset + boxHeight +
            view.parseWidth(view.getBorderTopWidth()) + // border top
            view.parseWidth(view.getBorderBottomWidth()); // border bottom
          view.setY(view.getY() - offset);
          view.setHeight(viewHeight);
        }
        view.setTabindex(getInteger(element.getAttribute("tabindex")));
        view.setDisabled(element.getAttribute("disabled"));
        view.setId(element.getAttribute("id"));
        view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
        view.setStyleClass(element.getAttribute("class"));
        view.setRenderer(element.getAttribute("renderer"));
        view.setVariable(element.getAttribute("name"));
        view.setConnection(element.getAttribute("connection"));
        view.setSql(decodeSql(element.getAttribute("sql")));
        view.setUsername(element.getAttribute("username"));
        view.setPassword(element.getAttribute("password"));
        view.setDataref(element.getAttribute("dataref"));
        view.setOnChange(element.getAttribute("onchange"));
        view.setMultiple(getBoolean(element.getAttribute("multiple")));

        panel.addComponentView(view);
        Node child = element.getFirstChild();
        while (child != null)
        {
          if (child instanceof Element)
          {
            Element option = (Element)child;
            if (option.getNodeName().equalsIgnoreCase("option"))
            {
              String code = option.getAttribute("value");
              Node text = option.getFirstChild();
              if (text instanceof Text)
              {
                String label = ((Text)text).getData();
                view.getOptions().add(new String[]{label, code});
              }
              else
              {
                view.getOptions().add(new String[]{"", code});
              }
            }
          }
          child = child.getNextSibling();
        }
      }
      else if (name.equalsIgnoreCase("textarea"))
      {
        // InputTextArea
        InputTextAreaView view = new InputTextAreaView();
        view.setBounds(new Rectangle(300, 200, 200, 50));
        applyStyles(view, getStyles(element.getAttribute("style")));
        view.setId(element.getAttribute("id"));
        view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
        view.setStyleClass(element.getAttribute("class"));
        view.setRenderer(element.getAttribute("renderer"));
        view.setVariable(element.getAttribute("name"));
        view.setFormat(element.getAttribute("format"));
        view.setMaxLength(getInteger(element.getAttribute("maxlength")));
        view.setRequired("true".equals(element.getAttribute("required")));
        view.setDisabled(element.getAttribute("disabled"));
        view.setTabindex(getInteger(element.getAttribute("tabindex")));
        panel.addComponentView(view);
      }
      else if (name.equalsIgnoreCase("img"))
      {
        // Image
        ImageView view = new ImageView();
        view.setBounds(new Rectangle(300, 200, 200, 50));
        applyStyles(view, getStyles(element.getAttribute("style")));
        view.setId(element.getAttribute("id"));
        view.setOutputOrder(getInteger(element.getAttribute("data-outputorder")));
        view.setStyleClass(element.getAttribute("class"));
        view.setRenderer(element.getAttribute("renderer"));
        view.setUrl(element.getAttribute("src"));
        view.setAlt(element.getAttribute("alt"));
        panel.addComponentView(view);
      }
      else if (name.equals("script"))
      {
        ScriptView view = new ScriptView();
        view.setType(element.getAttribute("type"));
        Node child = element.getFirstChild();
        if (child instanceof Text)
        {
          Text textElem = (Text)child;
          String text = textElem.getData();
          if (text != null)
          {
            // &nbsp; (Java char: 160)
            if (text.charAt(0) == 160) text = null;
          }
          view.setCode(text);
        }
        panel.addComponentView(view);
      }
    }
    else if (node instanceof Comment)
    {
      Comment comment = (Comment)node;
      String text = comment.getData();
      if (text != null)
      {
        text = text.trim();
        if (text.startsWith("${") && text.endsWith("}"))
        {
          text = text.substring(2, text.length() - 1);
          ScriptView view = new ScriptView();
          view.setType("serverscript");
          view.setCode(text);
          panel.addComponentView(view);
        }
      }
    }
  }

  private String getNodeContent(Node node, int indent)
  {
    StringBuilder buffer = new StringBuilder();
    Node child = node.getFirstChild();
    while (child != null)
    {
      if (child instanceof Text)
      {
        Text text = (Text)child;
        buffer.append(text.getData());
      }
      else if (child instanceof Element)
      {
        Element element = (Element)child;
        String tag = element.getNodeName();
        String searchTag = " " + tag.toLowerCase() + " ";
        boolean inlineTag =
          " br b u i span a label strong ".indexOf(searchTag) != -1;
        NamedNodeMap attributes = element.getAttributes();
        if (!inlineTag)
        {
          buffer.append("\n");
          indent(buffer, indent);
        }
        buffer.append("<").append(tag);
        for (int i = 0; i < attributes.getLength(); i++) // attributes
        {
          Attr attribute = (Attr)attributes.item(i);
          buffer.append(" ").append(attribute.getName()).append("=").
            append("\"").append(attribute.getValue()).append("\"");
        }
        if (child.getFirstChild() == null)
        {
          buffer.append("/>");
          if (tag.equalsIgnoreCase("br") || tag.equalsIgnoreCase("hr"))
          {
            buffer.append("\n");
          }
        }
        else
        {
          buffer.append(">");
          if (tag.equalsIgnoreCase("script"))
          {
            buffer.append("\n");
          }
          String nodeContent = getNodeContent(child, indent + 2);
          if (tag.equalsIgnoreCase("script"))
          {
            buffer.append(nodeContent.trim());
            buffer.append("\n");
            indent(buffer, indent);
          }
          else
          {
            buffer.append(nodeContent);
            if (nodeContent.indexOf("\n") != -1) // contains \n
            {
              if (!nodeContent.endsWith("\n")) buffer.append("\n");
              indent(buffer, indent);
            }
          }
          buffer.append("</").append(tag).append(">");
        }
      }
      child = child.getNextSibling();
    }
    return buffer.toString();
  }

  private void indent(StringBuilder builder, int spaces)
  {
    for (int i = 0; i < spaces; i++)
    {
      builder.append(" ");
    }
  }

  private Map getStyles(String style)
  {
    Map map = new HashMap();
    if (style != null)
    {
      String[] tokens = style.split(";");
      for (int i = 0; i < tokens.length; i++)
      {
        String token = tokens[i];
        int index = token.indexOf(":");
        if (index != -1)
        {
          String name = token.substring(0, index).trim();
          String value = token.substring(index + 1).trim();
          map.put(name, value);
        }
      }
    }
    return map;
  }

  private void applyStyles(ComponentView view, Map styles)
  {
    Iterator iter = styles.entrySet().iterator();
    while (iter.hasNext())
    {
      Map.Entry entry = (Map.Entry)iter.next();
      String name = (String)entry.getKey();
      String value = (String)entry.getValue();
      if (name.equals("top"))
      {
        view.setY(getPixels(value));
      }
      else if (name.equals("left"))
      {
        view.setX(getPixels(value));
      }
      else if (name.equals("text-align"))
      {
        view.setTextAlign(value);
      }
      else if (name.equals("font-family"))
      {
        view.setFontFamily(value);
      }
      else if (name.equals("font-size"))
      {
        view.setFontSize(new Integer(getPixels(value)));
      }
      else if (name.equals("background"))
      {
        view.setBackground(getColor(value));
      }
      else if (name.equals("foreground"))
      {
        view.setForeground(getColor(value));
      }
      else if (name.equals("border-top-width"))
      {
        view.setBorderTopWidth(value);
      }
      else if (name.equals("border-bottom-width"))
      {
        view.setBorderBottomWidth(value);
      }
      else if (name.equals("border-left-width"))
      {
        view.setBorderLeftWidth(value);
      }
      else if (name.equals("border-right-width"))
      {
        view.setBorderRightWidth(value);
      }
      else if (name.equals("border-top-color"))
      {
        view.setBorderTopColor(getColor(value));
      }
      else if (name.equals("border-bottom-color"))
      {
        view.setBorderBottomColor(getColor(value));
      }
      else if (name.equals("border-left-color"))
      {
        view.setBorderLeftColor(getColor(value));
      }
      else if (name.equals("border-right-color"))
      {
        view.setBorderRightColor(getColor(value));
      }
      else if (name.equals("border-top-style"))
      {
        view.setBorderTopStyle(value);
      }
      else if (name.equals("border-bottom-style"))
      {
        view.setBorderBottomStyle(value);
      }
      else if (name.equals("border-left-style"))
      {
        view.setBorderLeftStyle(value);
      }
      else if (name.equals("border-right-style"))
      {
        view.setBorderRightStyle(value);
      }
    }
    String contentWidth = (String)styles.get("width");
    if (contentWidth != null)
    {
      view.setContentWidth(view.parseWidth(contentWidth));
    }
    String contentHeight = (String)styles.get("height");
    if (contentHeight != null)
    {
      view.setContentHeight(view.parseWidth(contentHeight));
    }
  }

  private int getPixels(String value)
  {
    int pixels = 0;
    if (value.endsWith("px"))
    {
      value = value.substring(0, value.length() - 2);
    }
    try
    {
      pixels = Integer.parseInt(value);
    }
    catch (NumberFormatException ex)
    {
    }
    return pixels;
  }

  private Integer getInteger(String value)
  {
    if (value == null) return null;
    try
    {
      return new Integer(value);
    }
    catch (NumberFormatException ex)
    {
      return new Integer(0);
    }
  }

  private Boolean getBoolean(String value)
  {
    if (value == null) return null;
    try
    {
      return Boolean.valueOf(value);
    }
    catch (Exception ex)
    {
    }
    return null;
  }

  private Color getColor(String value)
  {
    if (value.startsWith("#"))
    {
      value = value.substring(1);
    }
    try
    {
      int rgb = Integer.parseInt(value, 16);
      return new Color(rgb);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  private Node findNode(Node node, String nodeName)
  {
    Node foundNode = null;
    Node currentNode = node;
    while (foundNode == null && currentNode != null)
    {
      String name = currentNode.getNodeName();
      if (nodeName.equalsIgnoreCase(name))
      {
        foundNode = currentNode;
      }
      else currentNode = currentNode.getNextSibling();
    }
    return foundNode;
  }

  private String decodeSql(String sql)
  {
    if (sql != null)
    {
      sql = sql.replaceAll("\\\\n", "\n");
    }
    return sql;
  }
}
