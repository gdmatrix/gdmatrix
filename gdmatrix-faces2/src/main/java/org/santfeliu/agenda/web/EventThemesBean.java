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
package org.santfeliu.agenda.web;

import java.util.List;

import javax.faces.model.SelectItem;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.EventTheme;
import org.matrix.agenda.EventThemeFilter;
import org.matrix.agenda.EventThemeView;


import org.santfeliu.web.obj.PageBean;


/**
 *
 * @author unknown
 */
public class EventThemesBean extends PageBean
{
  private EventTheme editingEventTheme;
  private List<EventThemeView> rows;
  
  public EventThemesBean()
  {
    load();
  }

  public EventTheme getEditingEventTheme()
  {
    return editingEventTheme;
  }

  public void setEditingEventTheme(EventTheme editingEventTheme)
  {
    this.editingEventTheme = editingEventTheme;
  }

  public List<EventThemeView> getRows()
  {
    return rows;
  }

  public void setRows(List<EventThemeView> rows)
  {
    this.rows = rows;
  }

  public String show()
  {
    return "event_themes";
  }

  @Override
  public String store()
  {
    if (editingEventTheme != null)
    {
      storeEventTheme();
    }
    else
    {
      load();
    }
    return show();
  }
  
  public String showTheme()
  {
    return getControllerBean().showObject("Theme",
      (String)getValue("#{row.themeId}"));
  }
  
  public String searchTheme()
  {
    return getControllerBean().searchObject("Theme",
      "#{eventThemesBean.editingEventTheme.themeId}");
  }

  public String removeEventTheme()
  {
    try
    {
      EventThemeView row = (EventThemeView)getRequestMap().get("row");
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.removeEventTheme(row.getEventThemeId());
      load();
      //reload to prevent CONCURRENCY_ERROR message
      getControllerBean().clearBeans(getSelectedMenuItem());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeEventTheme()
  {
    try
    {
      if (editingEventTheme == null || editingEventTheme.getThemeId() == null)
        throw new Exception("THEME_MUST_BE_SELECTED");
      
      String eventId = getObjectId();
      editingEventTheme.setEventId(eventId);
      
//      preStore();
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.storeEventTheme(editingEventTheme);
//      postStore();
      
      editingEventTheme = null;
      load();
      //reload to prevent CONCURRENCY_ERROR message
      getControllerBean().clearBeans(getSelectedMenuItem());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String createEventTheme()
  {
    editingEventTheme = new EventTheme();
    return null;
  }
    
  public String cancelEventTheme()
  {
    editingEventTheme = null;
    return null;
  }
  
  public List<SelectItem> getThemeSelectItems()
  {
    ThemeBean themeBean = (ThemeBean)getBean("themeBean");
    List<SelectItem> items = 
      themeBean.getAllSelectItems(editingEventTheme.getThemeId());

    return items;
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        EventThemeFilter filter = new EventThemeFilter();
        filter.setEventId(getObjectId());
        rows = 
          AgendaConfigBean.getPort().findEventThemeViewsFromCache(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }
}
