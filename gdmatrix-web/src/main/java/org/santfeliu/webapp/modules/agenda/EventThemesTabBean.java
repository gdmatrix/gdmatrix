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
package org.santfeliu.webapp.modules.agenda;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.EventTheme;
import org.matrix.agenda.EventThemeFilter;
import org.matrix.agenda.EventThemeView;
import org.primefaces.event.SelectEvent;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ViewScoped
public class EventThemesTabBean extends TabBean
{
  private List<EventThemeView> rows;

  private int firstRow;
  private EventTheme editing;

  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  ThemeObjectBean themeObjectBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public EventTheme getEditing()
  {
    return editing;
  }

  public void setEditing(EventTheme editing)
  {
    this.editing = editing;
  }

  public List<EventThemeView> getRows()
  {
    return rows;
  }

  public void setRows(List<EventThemeView> rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public String getPageObjectDescription()
  {
    if (editing != null && !isNew(editing))
    {
      return themeObjectBean.getDescription(editing.getThemeId());
    }
    return null;
  }

  public void onThemeSelect(SelectEvent<SelectItem> event)
  {
    SelectItem item = event.getObject();
    String themeId = (String)item.getValue();
    editing.setThemeId(themeId);
  }

  public void onThemeClear()
  {
    editing.setThemeId(null);
  }

  @Override
  public void load()
  {
    if (!isNew())
    {
      try
      {
        EventThemeFilter filter = new EventThemeFilter();
        filter.setEventId(eventObjectBean.getObjectId());
        rows = AgendaModuleBean.getClient(false).
          findEventThemeViewsFromCache(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else rows = Collections.EMPTY_LIST;
  }

  public void create()
  {
    editing = new EventTheme();
  }

  @Override
  public void store()
  {
    try
    {
      if (editing != null)
      {
        //Person must be selected
        if (editing.getThemeId() == null || editing.getThemeId().isEmpty())
        {
          throw new Exception("THEME_MUST_BE_SELECTED");
        }

        String eventId = eventObjectBean.getObjectId();
        editing.setEventId(eventId);
        AgendaModuleBean.getClient(false).storeEventTheme(editing);
        editing = null;
        load();
        growl("STORE_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void remove(EventThemeView row)
  {
    try
    {
      if (row == null)
        throw new Exception("THEME_MUST_BE_SELECTED");

      String rowEventThemeId = row.getEventThemeId();
      if (editing != null && rowEventThemeId.equals(editing.getEventThemeId()))
      {
        editing = null;
      }

      AgendaModuleBean.getClient(false).removeEventTheme(rowEventThemeId);
      load();
      growl("REMOVE_OBJECT");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void cancel()
  {
    editing = null;
  }

  @Override
  public boolean isDialogVisible()
  {
    return (editing != null);
  }  
  
  @Override
  public Serializable saveState()
  {
    return new Object[]{ editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (EventTheme)stateArray[0];

      if (!isNew()) load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(EventTheme eventTheme)
  {
    return (eventTheme != null && eventTheme.getEventThemeId() == null);
  }

}
