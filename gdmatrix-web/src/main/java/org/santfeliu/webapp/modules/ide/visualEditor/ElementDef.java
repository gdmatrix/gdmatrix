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
package org.santfeliu.webapp.modules.ide.visualEditor;

import org.santfeliu.form.type.html.HtmlViewWrapper;

/**
 *
 * @author granadogj
 */
  public enum ElementDef
  {
    INPUT("Input Text", "input", true, true, "col-12 md:col-6", "text", "inputText_template.xhtml", "inputText_props.xhtml"),
    RADIO("Input Radio", "input", true, false, "", "radio", "inputRadio_template.xhtml", "inputRadio_props.xhtml"),
    CHECKBOX("Input CheckBox", "input", true, true, "col-12 md:col-6", "checkbox", "inputCheckbox_template.xhtml", "inputCheckbox_props.xhtml"),
    TEXTAREA("Textarea", "textarea", true, true, "col-12 md:col-12", null, "textArea_template.xhtml", "textArea_props.xhtml"),
    SELECT("Select", "select", true, true, "col-12 md:col-6", null, "select_template.xhtml", "select_props.xhtml"),
    BUTTON("Button", "button", false, true, "col-12 md:col-6", "button", "button_template.xhtml", "button_props.xhtml"),
    IMAGE("Image", "img", false, true, "col-12 md:col-6", null, "image_template.xhtml", "image_props.xhtml"),
    FIELDSET("Fieldset", "fieldset", false, true, "col-12 md:col-6", null, "fieldset_template.xhtml", "fieldset_props.xhtml"),
    MAPLIBRE("MapLibre", "div", false, false, "col-12 md:col-12", "mapLibre", "divMaplibre_template.xhtml", "divMaplibre_props.xhtml"),
    DIV("RawHtml", "div", false, true, "col-12", null, "div_template.xhtml", "div_props.xhtml");

    private final String label;
    private final String tag;
    private final boolean hasLabel;
    private final boolean resizable;
    private final String defaultWidth;
    private final String defaultType;
    private final String templateFile;
    private final String propFile;

    ElementDef(String label, String tag, boolean hasLabel, boolean resizable, String defaultWidth, String defaultType, String templateFile, String propFile)
    {
      this.label = label;
      this.tag = tag;
      this.hasLabel = hasLabel;
      this.resizable = resizable;
      this.defaultWidth = defaultWidth;
      this.defaultType = defaultType;
      this.templateFile = templateFile;
      this.propFile = propFile;
    }

    public static ElementDef fromTagAndType(String tag, String type)
    {
      if (tag == null)
      {
        return null;
      }

      if ("input".equals(tag) && (type == null || type.isEmpty()))
      {
        type = "text";
      }
      if ("button".equals(tag) && (type == null || type.isEmpty()))
      {
        type = "button";
      }

      for (ElementDef def : values())
      {
        if (def.getTag().equals(tag))
        {
          if (def.getDefaultType() == null)
          {
            return def;
          }
          if (def.getDefaultType().equals(type))
          {
            return def;
          }
        }
      }
      return null;
    }

    public static ElementDef fromView(HtmlViewWrapper wrapper)
    {
      if (wrapper == null || wrapper.getNativeViewType() == null)
      {
        return null;
      }

      String tag = wrapper.getNativeViewType();
      String type = wrapper.getInputType();

      if ("div".equals(tag) && "mapLibre".equals(wrapper.getRenderer()))
      {
        type = "mapLibre";
      }

      return fromTagAndType(tag, type);
    }

    public String getLabel()
    {
      return label;
    }

    public String getTag()
    {
      return tag;
    }

    public boolean isHasLabel()
    {
      return hasLabel;
    }

    public boolean isResizable()
    {
      return resizable;
    }

    public String getDefaultWidth()
    {
      return defaultWidth;
    }

    public String getDefaultType()
    {
      return defaultType;
    }

    public String getTemplateFile()
    {
      return templateFile;
    }

    public String getPropFile()
    {
      return propFile;
    }
  }

