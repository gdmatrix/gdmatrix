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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author realor
 */
public class ObjectSetup
{
  private String viewId;
  private String label;
  private List<SearchTab> searchTabs = new ArrayList<>();
  private List<EditTab> editTabs = new ArrayList<>();
  private PropertyMap properties = new PropertyMap();

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
    if (viewId != null)
    {    
      for (EditTab editTab : editTabs)
      {
        if (viewId.equals(editTab.getViewId()))
          return editTab;
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

}
