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
package org.santfeliu.form.type.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.santfeliu.form.Field;
import org.santfeliu.form.View;
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
public class HtmlParser
{
  private HtmlForm form;
  private Map<String, String> labelById = new HashMap();
  private Map<String, HtmlField> fieldById = new HashMap();

  static final String[][] viewTypesByTag =
  {
    {"p", View.GROUP},
    {"div", View.GROUP},
    {"span", View.GROUP},
    {"label", View.GROUP},
    {"h1", View.GROUP},
    {"h2", View.GROUP},
    {"h3", View.GROUP},
    {"h4", View.GROUP},
    {"h5", View.GROUP},
    {"table", View.GROUP},
    {"tr", View.GROUP},
    {"td", View.GROUP},
    {"hd", View.GROUP},
    {"th", View.GROUP},
    {"theader", View.GROUP},
    {"tfooter", View.GROUP},
    {"b", View.STYLE},
    {"u", View.STYLE},
    {"i", View.STYLE},
    {"ul", View.GROUP},
    {"li", View.ITEM},
    {"ol", View.GROUP},
    {"option", View.ITEM},
    {"form", View.GROUP}
  };

  public HtmlParser(HtmlForm form)
  {
    this.form = form;
  }

  public void parse(InputStream is) throws IOException
  {
    parse(new InputStreamReader(is, form.encoding));
  }

  public void parse(Reader reader) throws IOException
  {
    // parser stream with Tidy
    try
    {
      Tidy tidy = new Tidy();
      tidy.setOnlyErrors(true);
      tidy.setShowWarnings(false);
      tidy.setInputEncoding(form.getEncoding());
      Properties props = new Properties();
      props.put("new-blocklevel-tags", "style");
      tidy.setConfigurationFromProps(props);      
      Document dom = tidy.parseDOM(reader, null);
      parseDOM(dom);
      updateFieldLabels();
    }
    finally
    {
      reader.close();
    }
  }

  private void parseDOM(Document dom) throws IOException
  {
    Node node = dom.getFirstChild();
    node = findNode(node, "html");
    if (node == null) throw new IOException("Invalid html document.");

    // look for body element
    node = node.getFirstChild();
    node = findNode(node, "body");
    if (node == null) throw new IOException("Invalid html document.");

    // reset data
    form.fields.clear();
    form.viewsByRef.clear();
    
    form.rootView = new HtmlView();
    form.rootView.setViewType(View.GROUP);
    populateView(node, form.rootView, null);
    parseChildren(node, form.rootView);
  }

  private void parseNode(Node node, HtmlView parentView) throws IOException
  {
    if (node instanceof Text)
    {
      HtmlView view = new HtmlView();
      populateView(node, view, parentView);
      view.setViewType(View.LABEL);
      String text = node.getNodeValue();
      view.setProperty("text", text);
    }
    else if (node instanceof Comment)
    {
      // remove comments
    }
    else if (node instanceof Element)// normal tag
    {
      Element element = (Element)node;
      String tag = element.getNodeName().toLowerCase();
      if (tag.equals("input")) // it's a input
      {
        String type = element.getAttribute("type");
        if (type == null || type.length() == 0) type = "text";
        type = type.toLowerCase();
        
        HtmlField field = getField(element);        
        if (field != null)
        {
          HtmlView view;
          if (type.equals("text"))
          {
            view = new HtmlInputTextView();
            view.setViewType(View.TEXTFIELD);            
          }
          else if (type.equals("password"))
          {
            view = new HtmlInputTextView();
            view.setViewType(View.PASSWORDFIELD);
          }
          else if (type.equals("radio"))
          {
            view = new HtmlRadioView();
            view.setViewType(View.RADIO);
          }
          else if (type.equals("checkbox"))
          {
            view = new HtmlCheckBoxView();
            view.setViewType(View.CHECKBOX);
            field.setType(Field.BOOLEAN);
            field.setMinOccurs(1); // mandatory when view is checkbox
          }
          else if (type.equals("submit") || type.equals("button"))
          {
            view = new HtmlView();
            view.setViewType(View.BUTTON);
          }
          else if (type.equals("hidden"))
          {
            view = new HtmlView();
            view.setViewType(View.UNKNOWN);
            field.setReadOnly(false);
          }
          else
          {
            view = new HtmlView();
            view.setViewType(View.UNKNOWN);
          }
          view.setProperty("type", type);
          populateView(element, view, parentView);
          form.linkView(view, field);
        }
      }
      else if (tag.equals("select"))
      {
        HtmlSelectView view = new HtmlSelectView();        
        populateView(node, view, parentView);
        view.setViewType(View.SELECT);
        HtmlField field = getField(element);
        form.linkView(view, field);
        parseChildren(node, view);
        List<String> parameters = view.getParameters(form);
        for (String parameter : parameters)
        {
          form.addReadOnlyField(parameter);
        }
      }
      else if (tag.equals("textarea"))
      {
        HtmlTextAreaView view = new HtmlTextAreaView();
        populateView(node, view, parentView);
        view.setViewType(View.TEXTFIELD);
        HtmlField field = getField(element);
        form.linkView(view, field);
      }
      else if (tag.equals("label"))
      {
        HtmlView view = new HtmlView();
        populateView(node, view, parentView);
        view.setViewType(View.GROUP);
        String id = element.getAttribute("for");
        if (id != null)
        {
          labelById.put(id, getTextContent(element));
        }
        parseChildren(node, view);
      }
      else
      {
        HtmlView view = new HtmlView();
        populateView(node, view, parentView);
        String viewType = getViewType(tag);
        view.setViewType(viewType);
        parseChildren(node, view);
      }
    }
  }

  private void parseChildren(Node node, HtmlView view) throws IOException
  {
    // render children
    Node childNode = node.getFirstChild();
    while (childNode != null)
    {
      parseNode(childNode, view);
      childNode = childNode.getNextSibling();
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

  private HtmlField getField(Element element)
  {
    HtmlField field = null;
    String reference = element.getAttribute("name");
    if (reference != null)
    {
      field = (HtmlField)form.getField(reference);
      if (field == null)
      {
        field = new HtmlField();
        field.setReference(reference);
        field.setLabel(reference); // default label
        field.setType(Field.TEXT); // default type
        field.setMinOccurs(0); // default value in this case
        field.setMaxOccurs(1); // default value in this case
        form.fields.add(field);
      }
      // process format property
      String format = element.getAttribute("format");
      if (format != null && format.length() > 0)
      {
        String formatType;
        String formatPattern;
        int index = format.indexOf(":");
        if (index != -1)
        {
          formatType = format.substring(0, index).toLowerCase();
          formatPattern = format.substring(index + 1);
        }
        else
        {
          formatType = format.toLowerCase();
          formatPattern = null;
        }
        if (HtmlForm.TEXT_FORMAT.equals(formatType))
        {
          field.setType(Field.TEXT);
        }
        else if (HtmlForm.BOOLEAN_FORMAT.equals(formatType))
        {
          field.setType(Field.BOOLEAN);
        }
        else if (HtmlForm.NUMBER_FORMAT.equals(formatType))
        {
          field.setType(Field.NUMBER);
        }
        else if (HtmlForm.DATE_FORMAT.equals(formatType))
        {
          field.setType(Field.DATE);
        }
        else if (HtmlForm.TIME_FORMAT.equals(formatType))
        {
          field.setType(Field.TIME);
        }
        else if (HtmlForm.DATETIME_FORMAT.equals(formatType))
        {
          field.setType(Field.DATETIME);
        }
      }
      // process required property
      String required = element.getAttribute("required");
      if (required != null &&
          required.length() > 0 && !"false".equalsIgnoreCase(required))
      {
        field.setMinOccurs(1);
      }
      // process multiple & multivalued property
      String multiple = element.getAttribute("multiple");
      if (multiple != null &&
          multiple.length() > 0 && !"false".equalsIgnoreCase(multiple))
      {
        field.setMaxOccurs(0);
      }
      String multiValued = element.getAttribute("multivalued");
      if (multiValued != null &&
          multiValued.length() > 0 && !"false".equalsIgnoreCase(multiValued))
      {
        field.setMaxOccurs(0);
      }
      // process disabled property
      String disabled = element.getAttribute("disabled");
      if (disabled != null &&
          disabled.length() > 0 && !"false".equalsIgnoreCase(disabled))
      {
        field.setReadOnly(true);
        field.setMinOccurs(0);
      }
      // register field by id
      String id = element.getAttribute("id");
      if (id != null && id.length() > 0)
      {
        fieldById.put(id, field);
      }
    }
    return field;
  }

  private void updateFieldLabels()
  {
    for (Map.Entry<String, String> entry : labelById.entrySet())
    {
      String id = entry.getKey();
      String label = entry.getValue();
      HtmlField field = (HtmlField)fieldById.get(id);
      if (field != null)
      {
        field.setLabel(label);
      }
    }
  }

  private String getTextContent(Node node)
  {
    StringBuilder buffer = new StringBuilder();
    Node child = node.getFirstChild();
    while (child != null)
    {
      if (child instanceof Text)
      {
        buffer.append(child.getNodeValue());
      }
      else if(child instanceof Element)
      {
        buffer.append(getTextContent(child));
      }
      child = child.getNextSibling();
    }
    return buffer.toString();
  }

  private void populateView(Node node, HtmlView view, HtmlView parentView)
  {
    if (node instanceof Element)
    {
      Element element = (Element)node;
      String id = element.getAttribute("id");
      if (id != null && id.length() > 0) view.setId(id);
      NamedNodeMap map = element.getAttributes();
      int length = map.getLength();
      for (int i = 0; i < length; i++)
      {
        Node propNode = map.item(i);
        String name = propNode.getNodeName();
        String value = propNode.getNodeValue();
        view.properties.put(name, value);
      }
    }
    view.tag = node.getNodeName();
    if (parentView != null) parentView.getChildren().add(view);
  }

  private String getViewType(String tag)
  {
    int i = 0;
    String viewType = null;
    while (i < viewTypesByTag.length && viewType == null)
    {
      if (viewTypesByTag[i][0].equals(tag))
        viewType = viewTypesByTag[i][1];
      i++;
    }
    if (viewType == null) viewType = View.UNKNOWN;
    return viewType;
  }
}
