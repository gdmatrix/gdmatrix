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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.santfeliu.form.View;
import org.santfeliu.webapp.modules.ide.VisualEditorBean.ElementDef;

/**
 *
 * @author granadogj
 */
public class HtmlViewWrapper
{

  private HtmlView view;

  public HtmlViewWrapper(HtmlView view)
  {
    this.view = view;
  }

  // Returns the wrapped view
  public HtmlView getUnderlyingView()
  {
    return view;
  }

  public String getNativeViewType()
  {
    return view.getNativeViewType();
  }

  public void setNativeViewType(String type)
  {
    view.setNativeViewType(type);
  }

  public String getViewType()
  {
    return view.getViewType();
  }

  public void setViewType(String type)
  {
    view.setViewType(type);
  }

  public String getId()
  {
    return view.getId();
  }

  public void setId(String id)
  {
    view.setId(id);
    view.setProperty("id", id);
  }

  public String getReference()
  {
    return view.getReference();
  }

  public void setReference(String reference)
  {
    view.setReference(reference);
  }

  public List<View> getChildren()
  {
    return this.view.getChildren();
  }

  public String getProperty(String propertyName)
  {
    return view.getProperty(propertyName);
  }

  public void setProperty(String propertyName, String newValue)
  {
    view.setProperty(propertyName, newValue);
  }

  public String getName()
  {
    return view.getProperty("name");
  }

  public void setName(String name)
  {
    view.setProperty("name", name);
  }

  public String getFor()
  {
    return view.getProperty("for");
  }

  public void setFor(String id)
  {
    view.setProperty("for", id);
  }

  public String getStyleClass()
  {
    return view.getProperty("class");
  }

  public void setStyleClass(String styleClass)
  {
    if (styleClass == null || styleClass.trim().isEmpty())
    {
      view.removeProperty("class");
    }
    else
    {
      view.setProperty("class", styleClass);
    }
  }

  // Return the "class" property without the estructural instructions (col)
  public String getBaseStyleClass()
  {
    String css = view.getProperty("class");
    if (css == null || css.trim().isEmpty())
    {
      return "";
    }

    return css.replaceAll("(?:\\s|^)col-\\d+(?=\\s|$)", "")
      .replaceAll("(?:\\s|^)md:col-\\d+(?=\\s|$)", "")
      .replaceAll("(?:\\s|^)lg:col-\\d+(?=\\s|$)", "")
      .replaceAll("(?:\\s|^)xl:col-\\d+(?=\\s|$)", "")
      .trim();
  }

  public void setBaseStyleClass(String baseCss)
  {
    String currentCss = view.getProperty("class");
    StringBuilder gridCss = new StringBuilder();

    if (currentCss != null)
    {
      Matcher m = Pattern
        .compile("(?:^|\\s)(col-\\d+|md:col-\\d+|lg:col-\\d+|xl:col-\\d+)(?=\\s|$)").matcher(currentCss);
      while (m.find())
      {
        gridCss.append(" ").append(m.group(1));
      }
    }
    String finalCss = ((baseCss != null ? baseCss : "") + gridCss.toString()).replaceAll("\\s+", " ").trim();

    if (finalCss.isEmpty())
    {
      view.removeProperty("class");
    }
    else
    {
      view.setProperty("class", finalCss);
    }
  }

  public String getStyle()
  {
    return view.getProperty("style");
  }

  public void setStyle(String style)
  {
    if (style == null || style.trim().isEmpty())
    {
      view.removeProperty("style");
    }
    else
    {
      view.setProperty("style", style);
    }
  }

  public String getPlaceholder()
  {
    return view.getProperty("placeholder");
  }

  public void setPlaceholder(String placeholder)
  {
    if (placeholder == null || placeholder.trim().isEmpty())
    {
      view.removeProperty("placeholder");
    }
    else
    {
      view.setProperty("placeholder", placeholder);
    }
  }

  public String getHelpText()
  {
    return view.getProperty("helptext");
  }

  public void setHelpText(String helpText)
  {
    if (helpText == null || helpText.trim().isEmpty())
    {
      view.removeProperty("helptext");
    }
    else
    {
      view.setProperty("helptext", helpText);
    }
  }
  
  public String getInfoText()
  {
    return view.getProperty("infotext");
  }
  
  public void setInfoText(String infoText)
  {
    if (infoText == null || infoText.trim().isEmpty())
    {
      view.removeProperty("infotext");
    }
    else
    {
      view.setProperty("infotext", infoText);
    }
  }

  public String getFormat()
  {
    return view.getProperty("format");
  }

  public void setFormat(String format)
  {
    if (format == null || format.trim().isEmpty())
    {
      view.removeProperty("format");
    }
    else
    {
      view.setProperty("format", format);
    }
  }

  public String getValue()
  {
    return view.getProperty("value");
  }

  public void setValue(String value)
  {
    if (value == null || value.trim().isEmpty())
    {
      view.removeProperty("value");
    }
    else
    {
      view.setProperty("value", value);
    }
  }

  // == Boolean properties ==
  public boolean isRequired()
  {
    return view.getProperty("required") != null;
  }

  public void setRequired(boolean required)
  {
    if (required)
    {
      view.setProperty("required", "required");
    }
    else
    {
      view.removeProperty("required");
    }
  }

  public boolean isDisabled()
  {
    return view.getProperty("disabled") != null;
  }

  public void setDisabled(boolean disabled)
  {
    if (disabled)
    {
      view.setProperty("disabled", "disabled");
    }
    else
    {
      view.removeProperty("disabled");
    }
  }

  public boolean isReadonly()
  {
    return view.getProperty("readonly") != null;
  }

  public void setReadonly(boolean readonly)
  {
    if (readonly)
    {
      view.setProperty("readonly", "readonly");
    }
    else
    {
      view.removeProperty("readonly");
    }
  }

  public boolean isResizable()
  {
    ElementDef def = ElementDef.fromView(this);
    return def != null && def.isResizable();
  }

  // == For Input Elements ==
  public String getInputType()
  {
    return view.getProperty("type");
  }

  public void setInputType(String type)
  {
    if (type == null || type.trim().isEmpty())
    {
      view.removeProperty("type");
    }
    else
    {
      view.setProperty("type", type);
    }
  }

  public void setInnerText(String text)
  {
    view.getChildren().clear();

    if (text != null && !text.isEmpty())
    {
      try
      {
        HtmlForm tempForm = new HtmlForm();
        HtmlParser parser = new HtmlParser(tempForm);

        // Parse the text by wraping it to ensure that it forms a valid tree
        parser.parse(new java.io.StringReader("<div>" + text + "</div>"));

        HtmlView tempRoot = (HtmlView) tempForm.getRootView();

        if (tempRoot != null)
        {
          HtmlView container = tempRoot;
          if (!"div".equals(container.getNativeViewType()) && !container.getChildren().isEmpty())
          {
            container = (HtmlView) container.getChildren().get(0);
          }

          // Pass the real nodes to our view
          view.getChildren().addAll(container.getChildren());
          return;
        }
      }
      catch (Exception ex)
      {
        System.out.println("Visual editor error parsing HTML: " + ex.getMessage());
      }

      // Fallback: If it fails save it in a #text node
      HtmlView textNode = new HtmlView();
      textNode.setNativeViewType("#text");
      textNode.setViewType(View.TEXT);
      textNode.setProperty("text", text);
      view.getChildren().add(textNode);
    }
  }

  public String getInnerText()
  {
    if (view.getChildren().isEmpty())
    {
      return "";
    }

    StringBuilder html = new StringBuilder();
    for (Object obj : view.getChildren())
    {
      html.append(reconstructHtml((HtmlView) obj));
    }
    return html.toString();
  }

  // Auxiliary method that travels through the tree by joining labels
  private String reconstructHtml(HtmlView node)
  {
    if ("#text".equals(node.getNativeViewType()))
    {
      Object prop = node.getProperty("text");
      return prop != null ? prop.toString() : "";
    }

    String tag = node.getNativeViewType();
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(tag).append(">");

//    // Includete atributes
//    for (String name : node.getPropertyNames())
//    {
//      Object value = node.getProperty(name);
//      if (value != null)
//      {
//        sb.append(" ").append(name).append("=\"").append(value).append("\"");
//      }
//    }
//    sb.append(">");

    for (Object obj : node.getChildren())
    {
      sb.append(reconstructHtml((HtmlView) obj));
    }

    // Close the label as long as it is not one of the self-conclusive
    if (!"br".equals(tag) && !"hr".equals(tag) && !"img".equals(tag))
    {
      sb.append("</").append(tag).append(">");
    }

    return sb.toString();
  }

  // Only for labels
  public String getLabelTextDef()
  {
    String text = getInnerText();
    return (text != null && !text.trim().isEmpty()) ? text : "Label";
  }

  // Get the legend of a Fieldset
  public String getLegendText()
  {
    if ("fieldset".equals(view.getNativeViewType()))
    {
      for (Object childObj : view.getChildren())
      {
        HtmlView child = (HtmlView) childObj;
        if ("legend".equals(child.getNativeViewType()))
        {
          HtmlViewWrapper legendWrapper = new HtmlViewWrapper(child);
          String text = legendWrapper.getInnerText();
          return (text != null && !text.trim().isEmpty()) ? text : "Fieldset";
        }
      }
    }
    return "Fieldset";
  }

  // Button property
  public String getButtonValueDef()
  {
    String val = getValue();
    return (val != null && !val.trim().isEmpty()) ? val : "Button";
  }

  // == Image properties == //
  public String getSrc()
  {
    String src = view.getProperty("src");
    return (src != null && !src.trim().isEmpty()) ? src : "";
  }

  public void setSrc(String src)
  {
    if (src == null || src.trim().isEmpty())
    {
      view.removeProperty("src");
    }
    else
    {
      view.setProperty("src", src);
    }
  }

  public String getAlt()
  {
    return view.getProperty("alt");
  }

  public void setAlt(String alt)
  {
    if (alt == null || alt.trim().isEmpty())
    {
      view.removeProperty("alt");
    }
    else
    {
      view.setProperty("alt", alt);
    }
  }

  // == Maplibre properties == //
  public String getRenderer()
  {
    String renderer = view.getProperty("renderer");
    return (renderer != null && !renderer.trim().isEmpty()) ? renderer : "";
  }

  public void setRenderer(String renderer)
  {
    if (renderer == null || renderer.trim().isEmpty())
    {
      view.removeProperty("renderer");
    }
    else
    {
      view.setProperty("renderer", renderer);
    }
  }

  // == Select Properties == //
  public boolean isMultiple()
  {
    String multiple = view.getProperty("multiple");
    return multiple != null && !multiple.trim().isEmpty() && !"false".equalsIgnoreCase(multiple);
  }

  public void setMultiple(boolean multiple)
  {
    if (multiple)
    {
      view.setProperty("multiple", "true");
    }
    else
    {
      view.removeProperty("multiple");
    }
  }

  public String getUsername()
  {
    String username = view.getProperty("username");
    return (username != null && !username.trim().isEmpty()) ? username : "";
  }

  public void setUsername(String username)
  {
    if (username == null || username.trim().isEmpty())
    {
      view.removeProperty("username");
    }
    else
    {
      view.setProperty("username", username);
    }
  }

  public String getPassword()
  {
    String password = view.getProperty("password");
    return (password != null && !password.trim().isEmpty()) ? password : "";
  }

  public void setPassword(String password)
  {
    if (password == null || password.trim().isEmpty())
    {
      view.removeProperty("password");
    }
    else
    {
      view.setProperty("password", password);
    }
  }

  public String getConnection()
  {
    String connection = view.getProperty("connection");
    return (connection != null && !connection.trim().isEmpty()) ? connection : "";
  }

  public void setConnection(String connection)
  {
    if (connection == null || connection.trim().isEmpty())
    {
      view.removeProperty("connection");
    }
    else
    {
      view.setProperty("connection", connection);
    }
  }

  public String getSql()
  {
    String sql = view.getProperty("sql");
    return (sql != null && !sql.trim().isEmpty()) ? sql : "";
  }

  public void setSql(String sql)
  {
    if (sql == null || sql.trim().isEmpty())
    {
      view.removeProperty("sql");
    }
    else
    {
      view.setProperty("sql", sql);
    }
  }

  public String getDataRef()
  {
    String dataref = view.getProperty("dataref");
    return (dataref != null && !dataref.trim().isEmpty()) ? dataref : "";
  }

  public void setDataRef(String dataref)
  {
    if (dataref == null || dataref.trim().isEmpty())
    {
      view.removeProperty("dataref");
    }
    else
    {
      view.setProperty("dataref", dataref);
    }
  }
  
  public String getGridClass() 
  {
    String styleClass = getStyleClass();
    if (styleClass == null || styleClass.trim().isEmpty())
    {
      return getDefaultGridClass();
    }
    
    StringBuilder gridClasses = new StringBuilder();
    Matcher m = Pattern.compile("(?:sm:|md:|lg:|xl:)?col-\\d+")
      .matcher(styleClass);
    
    while (m.find())
    {
      gridClasses.append(m.group()).append(" ");
    }
    
    String result = gridClasses.toString().trim();
    return result.isEmpty() ? getDefaultGridClass() : result;
  }
  
  private String getDefaultGridClass()
  {
    ElementDef def = ElementDef.fromView(this);
    return def != null ? def.getDefaultWidth() : "col-12";
  }
}
