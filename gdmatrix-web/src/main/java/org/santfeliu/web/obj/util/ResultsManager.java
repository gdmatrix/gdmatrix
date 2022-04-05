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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.convert.Converter;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;

/**
 *
 * @author blanquepa
 */
public class ResultsManager extends WebBean implements Serializable
{
  public static final String DEFAULT_COLUMN_NAME = "*";
  public static final String ORDERBY = "orderBy";
  public static final String ORDERBY_DESCENDING_SUFFIX = "desc";
  
  public static final String COLUMN_TYPES_PROPERTY = "columnType";
  public static final String COLUMN_NAMES_PROPERTY = "columnName";
  public static final String COLUMN_STYLES_PROPERTY = "columnStyle";
  public static final String COLUMN_STYLECLASSES_PROPERTY = "columnStyleClass";
  public static final String COLUMN_SOURCES_PROPERTY = "columnSource";
  public static final String COLUMN_CONVERTERS_PROPERTY = "columnConverter";
  public static final String COLUMN_COMPARATORS_PROPERTY = "columnComparator";
  public static final String COLUMN_RENDERERS_PROPERTY = "columnRenderer";
  public static final String COLUMN_VALUE_PREFIX_PROPERTY = "columnValuePrefix";
  public static final String COLUMN_VALUE_SUFFIX_PROPERTY = "columnValueSuffix";
  public static final String COLUMN_VALUE_TEMPLATE_PROPERTY = 
    "columnValueTemplate";
  public static final String ROW_STYLECLASS_PROPERTY = "rowStyleClass";

  private static final String STYLECLASS_SUFFIX = "Column";

  private List<String> defaultColumnNames;
  private final String bundleClassName;
  private final String bundlePrefix;

  private HashMap<String, ColumnDefinition> columns;
  private List<String> columnNames;

  private boolean ascending = false;
  private String typeIdPropName;
  private RowStyleClassGenerator rowStyleClassGenerator;
  
  private static Logger LOGGER = 
    Logger.getLogger(ResultsManager.class.getName());

  public ResultsManager(String bundleClassName, String bundlePrefix)
  {
    this.defaultColumnNames = new ArrayList();
    this.bundleClassName = bundleClassName;
    this.bundlePrefix = bundlePrefix;
  }

  public ResultsManager(String bundleClassName, String bundlePrefix,
    String typeIdProperty)
  {
    this.defaultColumnNames = new ArrayList();
    this.bundleClassName = bundleClassName;
    this.bundlePrefix = bundlePrefix;
    this.typeIdPropName = typeIdProperty;
  }
  
  public final void setColumns(String mid)
  {
    if (columns == null)
      columns = new HashMap();

    if (columnNames == null)
      columnNames = new ArrayList();
    else
      columnNames.clear();
    
    MenuItemCursor cursor =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid);
    setColumnAttributes(COLUMN_NAMES_PROPERTY, cursor); //Name & alias
    setColumnAttributes(COLUMN_STYLES_PROPERTY, cursor);
    setColumnAttributes(COLUMN_STYLECLASSES_PROPERTY, cursor);    
    setColumnAttributes(COLUMN_SOURCES_PROPERTY, cursor);
    setColumnAttributes(COLUMN_TYPES_PROPERTY, cursor);
    setColumnAttributes(COLUMN_CONVERTERS_PROPERTY, cursor);
    setColumnAttributes(COLUMN_COMPARATORS_PROPERTY, cursor);
    setColumnAttributes(COLUMN_RENDERERS_PROPERTY, cursor);
    setColumnAttributes(COLUMN_VALUE_PREFIX_PROPERTY, cursor);
    setColumnAttributes(COLUMN_VALUE_SUFFIX_PROPERTY, cursor);
    setColumnAttributes(COLUMN_VALUE_TEMPLATE_PROPERTY, cursor);
    setRowAttributes(ROW_STYLECLASS_PROPERTY, cursor);

    if (columnNames.isEmpty())
    {
      columnNames.addAll(defaultColumnNames);
      for (String columnName : defaultColumnNames)
      {
        ColumnDefinition colDef = this.columns.get(columnName);
        if (colDef == null)
          colDef = new ColumnDefinition(columnName);
        colDef.setResultsManager(this);
        columns.put(columnName, colDef);
      }
    }
  }

  public List<String> getColumnNames()
  {
    return this.columnNames;
  }

  public void setColumnNames(List<String> columnNames)
  {
    this.columnNames = columnNames;
  }

  public List<String> getDefaultColumnNames()
  {
    return defaultColumnNames;
  }

  public void setDefaultColumnNames(List<String> defaultColumnNames)
  {
    this.defaultColumnNames = defaultColumnNames;
  }

  public String getLocalizedColumnName()
  {
    String columnName = (String)getValue("#{column}");
    if (columns != null)
    {
      ColumnDefinition colDef = columns.get(columnName);
      if (colDef.getAlias() != null)
        columnName = colDef.getAlias();
    }

    if (!StringUtils.isBlank(columnName))
    {
      try
      {
        if (columnName.contains("."))
          columnName = columnName.substring(columnName.lastIndexOf(".") + 1);
        ResourceBundle bundle = ResourceBundle.getBundle(
          bundleClassName, getLocale());
        columnName = bundle.getString(bundlePrefix + columnName);
      }
      catch (Exception ex)
      {
      }
    }

    return columnName;
  }

  public void addDefaultColumn(String columnName)
  {
    registerColumn(new ColumnDefinition(columnName), true);
  }

  public void addDefaultColumn(ColumnDefinition columnDefinition)
  {
    registerColumn(columnDefinition, true);
  }

  private void registerColumn(ColumnDefinition columnDefinition,
    boolean isDefaultColumnName)
  {
    String columnName = columnDefinition.getName();
    if (isDefaultColumnName)
      defaultColumnNames.add(columnName);

    columnDefinition.setResultsManager(this);

    if (columns == null)
      columns = new HashMap();
    columns.put(columnName, columnDefinition);
  }

  public ColumnDefinition getColumnDefinition(String columnName)
  {
    if (columns != null)
      return columns.get(columnName);
    else
      return null;
  }

  public ColumnDefinition getColumnDefinition()
  {
    String column = (String)getValue("#{column}");
    if (column != null)
      return this.columns.get(column);
    else
      return null;
  }

  /*
   * column format: [render_type:]property_name
   */
  public Object getColumnValue(Object row, String column)
  {
    Object result = null;
    ColumnDefinition colDef = this.columns.get(column);

    if (colDef != null)
    {
      if (colDef.getRenderer() == null || 
          colDef.getRenderer() instanceof DefaultColumnRenderer)
      {
        if (colDef.isTypeSource())
        {
          colDef.setRenderer(
            new PropertyDefinitionColumnRenderer(this.typeIdPropName));
        }
        else if (colDef.isNodeSource())
          colDef.setRenderer(new NodeColumnRenderer());
        else if (colDef.getRenderer() == null)
          colDef.setRenderer(new DefaultColumnRenderer());
      }

      result = colDef.getValue(row, true);

      //complete URL values
      if ((colDef.isImageType() || colDef.isLinkType())
        && result != null 
        && !result.toString().toLowerCase().startsWith("http://"))
      {
        String regex = "[\\W&&[^\\.\\/\\:]]";
        result = result.toString().toLowerCase().replaceAll(regex, "_");
      }
    }

    return result;
  }

  public Object getColumnValue()
  {
    Object row = (Object)getValue("#{row}");
    String column = (String)getValue("#{column}");

    if (row != null && column != null)
      return getColumnValue(row, column);
    else
      return null;
  }

  public String getColumnStyle()
  {
    return getColumnStyle(getColumnName());
  }

  public String getColumnStyle(String column)
  {
    if (columns != null)
    {
      ColumnDefinition colDef = columns.get(column);
      if (colDef != null)
        return colDef.getStyle();
    }
    
    return "";
  }

  public String getColumnStyleClass(String columnName)
  {
    if (columns != null)
    {
      ColumnDefinition colDef = columns.get(columnName);
      if (colDef != null && colDef.getStyleClass() != null)
        return colDef.getStyleClass();
      else
      {
        columnName = columnName.replaceAll("\\.", "_");
        columnName = columnName.replaceAll("\\[", "_");
        columnName = columnName.replaceAll("\\]", "_");
        if (columnName.contains(":"))
          columnName = columnName.substring(columnName.indexOf(":") + 1);
        if (columnName.contains("@"))
          columnName = columnName.substring(0, columnName.lastIndexOf("@"));
        return columnName + STYLECLASS_SUFFIX;
      }
    }
    return null;
  }

  public String getColumnStyleClass()
  {
    return getColumnStyleClass(getColumnName());
  }

  public String getColumnName()
  {
    String columnName = (String)getValue("#{column}");
    return columnName;
  }

  public String getColumnDescription()
  {
    return getColumnDescription(getColumnName());
  }

  public String getColumnDescription(String column)
  {
    if (columns != null)
    {
      ColumnDefinition colDef = columns.get(column);
      if (colDef != null)
      {
        String description = colDef.getDescription();
        if (description != null)
        {
          Properties properties = new Properties();
          WebTemplate t = WebTemplate.create(description);
          if (t.getExpressionFragmentCount() > 0)
          {
            Set variables = t.getReferencedVariables();
            for (Object variable : variables)
            {
              if (!column.equals((String)variable))
              {
                Object value = 
                  getColumnValue(getValue("#{row}"), (String)variable);
                properties.put(variable, value);
              }
            }
            description = t.merge(properties);
          }
        }
        return description;
      }
    }

    return "";
  }

  public boolean isLinkColumn()
  {
    if (columns != null && columns.get(getColumnName()) != null)
      return this.columns.get(getColumnName()).isLinkType();
    else
      return false;
  }

  public boolean isImageColumn()
  {
    if (columns != null && columns.get(getColumnName()) != null)
      return this.columns.get(getColumnName()).isImageType();
    else
      return false;
  }

  public boolean isCustomColumn()
  {
    if (columns != null && columns.get(getColumnName()) != null)
      return this.columns.get(getColumnName()).isCustomType();
    else
      return false;
  }

  public boolean isSubmitColumn()
  {
    if (columns != null && columns.get(getColumnName()) != null)
      return this.columns.get(getColumnName()).isSubmitType();
    else
      return false;
  }

  public void sort(List rows)
  {
    sort(rows, (String)getValue("#{column}"));
  }

  public void sort(List rows, String columnName)
  {
    sort(rows, Arrays.asList(columnName));
  }

  public void sort(List rows, List<String> columnNames)
  {
    Collections.sort(rows, new ColumnComparator(columnNames, ascending));
    ascending = !ascending;
  }

  private void setRowAttributes(String propertyName, MenuItemCursor cursor)
  {
    rowStyleClassGenerator = null;
    
    String attribute = cursor.getProperty(propertyName);
    if (attribute != null)
    {
      List<String> tokens = parseAttribute(attribute);
      if (tokens != null)
      {
        String attName = tokens.get(0);
        if (propertyName.equals(ROW_STYLECLASS_PROPERTY))
        {
          try
          {
            rowStyleClassGenerator =
              (RowStyleClassGenerator)newInstance(tokens, attName, 1);
          }
          catch (Exception ex)
          {
            warn(ROW_STYLECLASS_PROPERTY, attribute, tokens, ex);
          }
        }
      }
    }
  }

  private void setColumnAttributes(String propertyName, MenuItemCursor cursor)
  {
    List<String> attributes = cursor.getDirectMultiValuedProperty(propertyName);
    if (propertyName != null && attributes != null && !attributes.isEmpty())
    {
      for (String attribute : attributes)
      {
        List<String> tokens = parseAttribute(attribute);
        if (tokens != null)
        {
          String attName = tokens.get(0);
          String attValue = null;
          if (tokens.size() >= 2)
            attValue = tokens.get(1);

          ColumnDefinition colDef = this.columns.get(attName);
          if (colDef == null)
          {
            colDef = new ColumnDefinition(attName);
            colDef.setResultsManager(this);
            this.columns.put(attName, colDef);
          }
          switch (propertyName)
          {
            case COLUMN_NAMES_PROPERTY:
              this.columnNames.add(attName);
              if (attValue != null)
                colDef.setAlias(attValue);
              if (tokens.size() >= 3)
                colDef.setDescription(tokens.get(2));
              break;
            case COLUMN_SOURCES_PROPERTY:
              colDef.setSource(attValue);
              break;
            case COLUMN_STYLES_PROPERTY:
              colDef.setStyle(attValue);
              break;
            case COLUMN_STYLECLASSES_PROPERTY:
              colDef.setStyleClass(attValue);
              break;
            case COLUMN_TYPES_PROPERTY:
              colDef.setType(attValue);
              break;
            case COLUMN_VALUE_PREFIX_PROPERTY:
              colDef.setValuePrefix(attValue);
              break;
            case COLUMN_VALUE_SUFFIX_PROPERTY:
              colDef.setValueSuffix(attValue);
              break;
            case COLUMN_VALUE_TEMPLATE_PROPERTY:
              colDef.setValueTemplate(attValue);
              break;
            case COLUMN_CONVERTERS_PROPERTY:
              try
              {
                Converter converter = 
                  (Converter)newInstance(tokens, attValue, 2);
                colDef.setConverter(converter);
              }
              catch (Exception ex)
              {
                warn(COLUMN_CONVERTERS_PROPERTY, attValue, tokens, ex);
              } 
              break;
            case COLUMN_COMPARATORS_PROPERTY:
              try
              {
                ColumnComparator comparator =
                  (ColumnComparator)newInstance(tokens, attValue, 2);
                colDef.setComparator(comparator);
              }
              catch (Exception ex)
              {
                warn(COLUMN_COMPARATORS_PROPERTY, attValue, tokens, ex);
              } 
              break;
            case COLUMN_RENDERERS_PROPERTY:
              try
              {
                ColumnRenderer renderer =
                  (ColumnRenderer)newInstance(tokens, attValue, 2);
                colDef.setRenderer(renderer);
              }
              catch (Exception ex)
              {
                warn(COLUMN_RENDERERS_PROPERTY, attValue, tokens, ex);
              } 
              break;
            default:
              break;
          }
        }
      }
    }
  }
  
  private void warn(String attName, String attValue, List<String> tokens,
    Exception ex)
  {
    Object[] params = new Object[]{attName, attValue, tokens, ex.getMessage()};
    LOGGER.log(Level.WARNING, "Invalid config. {0}::{1}::{2}::{3}", params);    
  }

  private List<String> parseAttribute(String attribute)
  {
    List<String> matchList = new ArrayList<String>();
    Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    Matcher regexMatcher = regex.matcher(attribute);
    while (regexMatcher.find())
    {
      if (regexMatcher.group(1) != null)
      {
        // Add double-quoted string without the quotes
        matchList.add(regexMatcher.group(1));
      } 
      else if (regexMatcher.group(2) != null)
      {
        // Add single-quoted string without the quotes
        matchList.add(regexMatcher.group(2));
      } else
      {
        // Add unquoted word
        matchList.add(regexMatcher.group());
      }
    }
    return matchList;
  }

  private Object newInstance(List<String> tokens, String className, 
    int minTokenSize) throws Exception
  {
    Class instanceClass = Class.forName(className);
    Object instance = null;
    if (tokens.size() == minTokenSize)
    {
      instance = instanceClass.newInstance();
    }
    else if (tokens.size() > minTokenSize)
    {
      Constructor[] constructors = instanceClass.getConstructors();
      Object[] parameters = 
        Arrays.copyOfRange(tokens.toArray(), minTokenSize, tokens.size());
      instance = newInstance(constructors, parameters);
    }
    return instance;
  }

  private Object newInstance(Constructor[] constructors, Object[] parameters)
  {
    if (constructors != null)
    {
      for (Constructor constructor : constructors)
      {
        try
        {
          Object newObject = constructor.newInstance(parameters);
          return newObject;
        }
        catch(Exception ex)
        {
        }
      }
    }
    return null;
  }

  public String getRowStyleClass()
  {
    Object row = (Object)getValue("#{row}");
    if (row != null && rowStyleClassGenerator != null)
      return rowStyleClassGenerator.getStyleClass(row);
    else
      return null;
  }

  public class ColumnComparator implements Comparator, Serializable
  {
    private final List<String> columnNames;
    private boolean ascending;

    public ColumnComparator(List<String> columnNames, boolean ascending)
    {
      this.columnNames = columnNames;
      this.ascending = ascending;
    }

    @Override
    public int compare(Object o1, Object o2)
    {
      int result = 0;
      for (String columnName : columnNames)
      {
        String[] array = columnName.split(":");
        if (array != null && array.length == 2 &&
          array[1].equalsIgnoreCase(ResultsManager.ORDERBY_DESCENDING_SUFFIX))
          ascending = false;

        ColumnDefinition colDef = columns.get(columnName);
        if (colDef != null)
        {
          Object column1 = colDef.getValue(o1, false);
          Object column2 = colDef.getValue(o2, false);

          if (column1 == null && column2 != null)
              return getResult(-1);
          else if (column1 == null && column2 == null)
              return getResult(0);
          else if (column1 != null && column2 == null)
              return getResult(1);
          else
          {
            Comparator c = colDef.getComparator();
            if (c != null)
              result = c.compare(column1, column2);
            else if (column1 != null)
              result = ((Comparable)column1).compareTo(column2);
          }

          if (result != 0)
            return getResult(result);
        }
      }

      return getResult(result);
    }

    private int getResult(int result)
    {
      return (!ascending ? result : result * -1);
    }
  }
}
