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
package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Properties;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.template.WebTemplate;

/**
 *
 * @author blanquepa
 */
public class ColumnDefinition implements Serializable
{
  public static final String IMAGE_TYPE = "image";
  public static final String LINK_TYPE = "link";
  public static final String CUSTOM_TYPE = "custom";
  public static final String SUBMIT_TYPE = "submit";

  public static final String NODE_SOURCE = "node";
  public static final String TYPE_SOURCE = "type";

  private ResultsManager resultsManager;
  private String name;
  private Converter converter;
  private Comparator comparator;
  private boolean sortConverted;
  private String style;
  private String styleClass;
  private String alias;
  private String description;
  private String source; //node, type, �renderer?
  private String type; //image, link, custom
  private ColumnRenderer renderer;
  private String valuePrefix;
  private String valueSuffix;
  private String valueTemplate;

  public ColumnDefinition(String name)
  {
    this.name = name;
    renderer = new DefaultColumnRenderer();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public ResultsManager getResultsManager()
  {
    return resultsManager;
  }

  public void setResultsManager(ResultsManager resultsManager)
  {
    this.resultsManager = resultsManager;
  }

  public Comparator getComparator()
  {
    return comparator;
  }

  public void setComparator(Comparator comparator)
  {
    this.comparator = comparator;
  }

  public Converter getConverter()
  {
    return converter;
  }

  public void setConverter(Converter converter)
  {
    this.converter = converter;
  }

  public boolean isSortConverted()
  {
    return sortConverted;
  }

  public void setSortConverted(boolean sortConverted)
  {
    this.sortConverted = sortConverted;
  }

  public String getAlias()
  {
    return alias;
  }

  public void setAlias(String alias)
  {
    this.alias = alias;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public ColumnRenderer getRenderer()
  {
    return renderer;
  }

  public void setRenderer(ColumnRenderer renderer)
  {
    this.renderer = renderer;
  }

  public String getValuePrefix()
  {
    return valuePrefix;
  }

  public void setValuePrefix(String valuePrefix)
  {
    this.valuePrefix = valuePrefix;
  }

  public String getValueSuffix()
  {
    return valueSuffix;
  }

  public void setValueSuffix(String valueSuffix)
  {
    this.valueSuffix = valueSuffix;
  }

  public String getValueTemplate()
  {
    return valueTemplate;
  }

  public void setValueTemplate(String valueTemplate)
  {
    this.valueTemplate = valueTemplate;
  }

  public boolean isImageType()
  {
    return type != null && type.equalsIgnoreCase(IMAGE_TYPE);
  }

  public boolean isLinkType()
  {
    return type != null && type.equalsIgnoreCase(LINK_TYPE);
  }

  public boolean isCustomType()
  {
    return type != null && type.equalsIgnoreCase(CUSTOM_TYPE);
  }

  public boolean isSubmitType()
  {
    return type != null && type.equalsIgnoreCase(SUBMIT_TYPE);
  }


  public boolean isNodeSource()
  {
    return source != null && source.equalsIgnoreCase(NODE_SOURCE);
  }

  public boolean isTypeSource()
  {
    return source != null && source.equalsIgnoreCase(TYPE_SOURCE);
  }

  public Object getValue(Object row, boolean convertValue)
  {
    Object value = null;
    String columnName = name;

    //Prefix support
    if (columnName.contains(":"))
      columnName = columnName.substring(columnName.indexOf(":") + 1);

    value = getRenderer().getValue(columnName, row);

    //Converter
    if (convertValue && converter != null)
      value = converter.getAsString(FacesContext.getCurrentInstance(), null, value);

    //Prefix & Suffix
    if (valuePrefix != null && value != null)
      value = valuePrefix + value;

    if (valueSuffix != null && value != null)
      value = value + valueSuffix;

    if (valueTemplate != null)
      value = getValue(row, valueTemplate, value);
    else if (String.valueOf(value).matches(".*\\$\\{[a-zA-Z_0-9]+\\}.*"))
      value = getValue(row, String.valueOf(value), value);

    return value;
  }

  private Object getValue(Object row, String templateString, Object value)
  {
    Properties properties = new Properties();


    WebTemplate template = WebTemplate.create(templateString);
    Set<String> variables = template.getReferencedVariables();

    for (String columnName : variables)
    {
      if (!columnName.equals(this.name))
      {
        ColumnDefinition colDef = resultsManager.getColumnDefinition(columnName);
        String columnValue = String.valueOf(colDef.getValue(row, true));

        if (colDef.getAlias() != null)
          properties.setProperty(colDef.getAlias(), columnValue);
        else
          properties.setProperty(columnName, columnValue);
      }
      else
        properties.setProperty(columnName, 
          String.valueOf(PojoUtils.getDeepStaticProperty(row, columnName)));
    }

    return template.merge(properties);
  }
}
