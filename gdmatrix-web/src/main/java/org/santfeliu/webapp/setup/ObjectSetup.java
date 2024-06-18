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
package org.santfeliu.webapp.setup;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author realor
 */
public class ObjectSetup implements Serializable
{
  private String typeId;
  private String viewId;
  private String label;
  private String findOnFirstLoad;
  private String smartSearchTipDocId;
  private String defaultSearchTabSelector;
  private List<SearchTab> searchTabs = new ArrayList<>();
  private List<EditTab> editTabs = new ArrayList<>();
  private PropertyMap properties = new PropertyMap();
  private ScriptActions scriptActions = new ScriptActions();
  private MergeMode mergeMode = MergeMode.ADD;
  public enum MergeMode 
  {
    ADD,
    PRESERVE
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  public String getViewId()
  {
    return viewId;
  }

  public void setViewId(String viewId)
  {
    this.viewId = viewId;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getFindOnFirstLoad()
  {
    return findOnFirstLoad;
  }

  public void setFindOnFirstLoad(String findOnFirstLoad)
  {
    this.findOnFirstLoad = findOnFirstLoad;
  }

  public String getSmartSearchTipDocId()
  {
    return smartSearchTipDocId;
  }

  public void setSmartSearchTipDocId(String smartSearchTipDocId)
  {
    this.smartSearchTipDocId = smartSearchTipDocId;
  }

  public String getDefaultSearchTabSelector()
  {
    return defaultSearchTabSelector;
  }

  public void setDefaultSearchTabSelector(String defaultSearchTabSelector)
  {
    this.defaultSearchTabSelector = defaultSearchTabSelector;
  }

  public List<SearchTab> getSearchTabs()
  {
    return searchTabs;
  }

  public void setSearchTabs(List<SearchTab> searchTabs)
  {
    this.searchTabs = searchTabs;
  }

  public List<EditTab> getEditTabs()
  {
    return editTabs;
  }

  public void setEditTabs(List<EditTab> editTabs)
  {
    this.editTabs = editTabs;
  }

  public PropertyMap getProperties()
  {
    return properties;
  }

  public void setProperties(PropertyMap properties)
  {
    this.properties = properties;
  }

  public ScriptActions getScriptActions()
  {
    return scriptActions;
  }

  public void setScriptActions(ScriptActions scriptActions)
  {
    this.scriptActions = scriptActions;
  }

  public MergeMode getMergeMode()
  {
    return mergeMode;
  }

  public void setMergeMode(MergeMode mergeMode)
  {
    this.mergeMode = mergeMode;
  }

  @Override
  public String toString()
  {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  public static void write(ObjectSetup config, Writer writer)
  {
    Gson gson = new Gson();
    gson.toJson(config, writer);
  }

  public static ObjectSetup read(Reader reader) throws IOException
  {
    Gson gson = new Gson();
    return gson.fromJson(reader, ObjectSetup.class);
  }

  public EditTab findEditTabByViewId(String viewId)
  {
    return findEditTabByViewId(viewId, null);
  }
  
  public EditTab findEditTabByViewId(String viewId, String subViewId)
  {
    if (viewId != null)
    {
      for (EditTab editTab : editTabs)
      {
        if (viewId.equals(editTab.getViewId()))
        {
          if (subViewId == null) 
          {
            return editTab;
          }
          else //check subViewId
          {
            if (subViewId.equals(editTab.getSubviewId()))
            {
              return editTab;
            }
          }
        }
      }
    }
    return null;
  }

  public SearchTab findSearchTabByViewId(String viewId)
  {
    if (viewId != null)
    {
      for (SearchTab searchTab : searchTabs)
      {
        if (viewId.equals(searchTab.getViewId()))
          return searchTab;
      }
    }

    return null;
  }

  public void merge(ObjectSetup defaultSetup) throws Exception
  {
    mergeProperties(defaultSetup.getProperties(), getProperties());

    //Search tabs
    if (searchTabs.isEmpty() && !defaultSetup.getSearchTabs().isEmpty())
    {
      for (SearchTab defaultEditTab : defaultSetup.getSearchTabs())
      {
        searchTabs.add(defaultEditTab);
      }
    }
    else
    {
      if (mergeMode == MergeMode.PRESERVE)
      {
        List<SearchTab> auxList = new ArrayList(defaultSetup.getSearchTabs());
        for (SearchTab searchTab : searchTabs)
        {
          SearchTab defSearchTab =
            defaultSetup.findSearchTabByViewId(searchTab.getViewId());
          if (defSearchTab != null) //merge
          {
            int i = auxList.indexOf(defSearchTab);
            if (i >= 0)
            {
              auxList.remove(i);
              mergeSearchTab(defSearchTab, searchTab);
              auxList.add(i, searchTab);
            }
          }
          else //add
          {
            auxList.add(searchTab);
          }          
        }
        searchTabs.clear();
        searchTabs.addAll(auxList);        
      }
      else
      {
        for (SearchTab searchTab : searchTabs)
        {
          SearchTab defSearchTab =
            defaultSetup.findSearchTabByViewId(searchTab.getViewId());
          if (defSearchTab != null)
            mergeSearchTab(defSearchTab, searchTab);
        }
      }
    }

    //Edit tabs
    if (editTabs.isEmpty() && !defaultSetup.getEditTabs().isEmpty())
    {
      for (EditTab defaultEditTab : defaultSetup.getEditTabs())
      {
        editTabs.add(defaultEditTab);
      }
    }
    else
    {
      if (mergeMode == MergeMode.PRESERVE)
      {
        List<EditTab> auxList = new ArrayList(defaultSetup.getEditTabs());
        for (EditTab editTab : editTabs)
        {
          EditTab defEditTab = defaultSetup.findEditTabByViewId(
            editTab.getViewId(), editTab.getSubviewId());
          if (defEditTab != null) //merge
          {
            int i = auxList.indexOf(defEditTab);
            if (i >= 0)
            {
              auxList.remove(i);
              mergeEditTab(defEditTab, editTab);
              auxList.add(i, editTab);
            }
          }
          else //add
          {
            auxList.add(editTab);
          }
        }
        editTabs.clear();
        editTabs.addAll(auxList);        
      }
      else
      {
        for (EditTab editTab : editTabs)
        {
          EditTab defEditTab =
            defaultSetup.findEditTabByViewId(editTab.getViewId());
          if (defEditTab != null)
            mergeEditTab(defEditTab, editTab);
        }
      }
    }
  }

  private void mergeSearchTab(SearchTab defaultSearchTab,
    SearchTab searchTab)
  {
    //Properties
    PropertyMap defaultPropertyMap = defaultSearchTab.getProperties();
    PropertyMap propertyMap = searchTab.getProperties();
    if (propertyMap == null)
    {
      propertyMap = new PropertyMap();
      searchTab.setProperties(propertyMap);
    }
    mergeProperties(defaultPropertyMap, propertyMap);

    //Columns
    List<Column> defaultColumns = defaultSearchTab.getColumns();
    List<Column> columns = searchTab.getColumns();
    if (columns == null)
    {
      columns = new ArrayList();
      searchTab.setColumns(columns);
    }
    mergeColumns(defaultColumns, columns);
    
    //Custom columns
    List<Column> defaultCustomColumns = defaultSearchTab.getCustomColumns();
    List<Column> customColumns = searchTab.getCustomColumns();
    if (customColumns == null)
    {
      customColumns = new ArrayList();
      searchTab.setCustomColumns(customColumns);
    }
    mergeColumns(defaultCustomColumns, customColumns);    
  }

  private void mergeEditTab(EditTab defaultEditTab, EditTab editTab)
  {
    //Properties
    PropertyMap defaultPropertyMap = defaultEditTab.getProperties();
    PropertyMap propertyMap = editTab.getProperties();
    if (propertyMap == null)
    {
      propertyMap = new PropertyMap();
      editTab.setProperties(propertyMap);
    }
    mergeProperties(defaultPropertyMap, propertyMap);

    //Columns
    List<Column> defaultColumns = defaultEditTab.getColumns();
    List<Column> columns = editTab.getColumns();
    if (columns == null)
    {
      columns = new ArrayList();
      editTab.setColumns(columns);
    }
    mergeColumns(defaultColumns, columns);

    //Custom columns
    List<Column> defaultCustomColumns = defaultEditTab.getCustomColumns();
    List<Column> customColumns = editTab.getCustomColumns();
    if (customColumns == null)
    {
      customColumns = new ArrayList();
      editTab.setCustomColumns(customColumns);
    }
    mergeColumns(defaultCustomColumns, customColumns);

    //Roles
    List<String> defaultReadRoles = defaultEditTab.getReadRoles();
    List<String> readRoles = editTab.getReadRoles();
    if (readRoles == null)
    {
      readRoles = new ArrayList();
      editTab.setReadRoles(readRoles);
    }
    mergeRoles(defaultReadRoles, readRoles);

    List<String> defaultWriteRoles = defaultEditTab.getWriteRoles();
    List<String> writeRoles = editTab.getWriteRoles();
    if (writeRoles == null)
    {
      writeRoles = new ArrayList();
      editTab.setWriteRoles(writeRoles);
    }
    mergeRoles(defaultWriteRoles, writeRoles);
    
    //OrderBy
    List<String> defaultOrderBy = defaultEditTab.getOrderBy();
    List<String> orderBy = editTab.getOrderBy();
    if (orderBy == null)
    {
      orderBy = new ArrayList();
      editTab.setOrderBy(orderBy);
    }
    mergeOrderBy(defaultOrderBy, orderBy);     

    //GroupBy
    String defaultGroupBy = defaultEditTab.getGroupBy();
    String groupBy = editTab.getGroupBy();
    if (defaultGroupBy != null && groupBy == null)
    {
      editTab.setGroupBy(defaultGroupBy);
    }
    
    //Icons
    if (editTab.getIcon() == null)
      editTab.setIcon(defaultEditTab.getIcon());
  }

  private void mergeProperties(PropertyMap defaultPropertyMap,
    PropertyMap propertyMap)
  {
    if (defaultPropertyMap != null)
    {
      Set<String> propertyNames = defaultPropertyMap.keySet();
      for (String propertyName : propertyNames)
      {
        Object property = propertyMap.get(propertyName);
        if (property == null)
        {
          propertyMap.put(propertyName, property);
        }
      }
    }
  }

  private void mergeColumns(List<Column> defaultColumns,
    List<Column> columns)
  {
    if (defaultColumns != null && !defaultColumns.isEmpty()
      && columns != null && columns.isEmpty())
    {
      columns.addAll(defaultColumns);
    }
  }

  private void mergeRoles(List<String> defaultRoles, List<String> roles)
  {
    if (defaultRoles != null && !defaultRoles.isEmpty()
      && roles != null && roles.isEmpty())
    {
      roles.addAll(defaultRoles);
    }
  }

  private void mergeOrderBy(List<String> defaultOrderBy, List<String> orderBy)
  {
    if (defaultOrderBy != null && !defaultOrderBy.isEmpty()
      && orderBy != null && orderBy.isEmpty())
    {
      orderBy.addAll(defaultOrderBy);
    }
  }  
  
}
