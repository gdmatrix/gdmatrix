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
package org.matrix.pf.agenda;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.EventTheme;
import org.matrix.agenda.EventThemeFilter;
import org.matrix.agenda.EventThemeView;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TabPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.agenda.web.AgendaConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named("eventThemesBacking")
public class EventThemesBacking extends PageBacking 
  implements TabPage, ResultListPage
{
  private static final String THEME_BACKING = "themeBacking";
  private static final String EVENT_BACKING = "eventBacking";
  
  private static final String OUTCOME = "pf_event_themes";
  
  private EventBacking eventBacking;
  
  //Helpers
  private ResultListHelper<EventThemeView> resultListHelper;
  private TabHelper tabHelper;
  
  private EventTheme editing;
  private SelectItem themeSelectItem;

  public EventThemesBacking()
  { 
  }
  
  @PostConstruct
  public void init()
  {
    eventBacking = WebUtils.getBacking(EVENT_BACKING);   
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
  }

  public EventTheme getEditing() 
  {
    return editing;
  }

  public void setEditing(EventTheme editing) 
  {
    this.editing = editing;
  }

  public boolean isNew()
  {
    return isNew(editing);
  }

  @Override
  public EventBacking getObjectBacking()
  {
    return eventBacking;
  }
  
  @Override
  public String getPageObjectId()
  {
    if (editing != null)
      return editing.getThemeId();
    else
      return null;
  }
  
  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      ThemeBacking themeBacking = WebUtils.getBacking(THEME_BACKING);
      return getDescription(themeBacking, editing.getThemeId());
    }
    return null;
  }
  
  @Override
  public ResultListHelper<EventThemeView> getResultListHelper()
  {
    return resultListHelper;
  }  

  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }
  
  public List<EventThemeView> getRows()
  {
    return resultListHelper.getRows();
  }
 
  //Person selection
  public SelectItem getThemeSelectItem()
  {
    return themeSelectItem;
  }
  
  public void setThemeSelectItem(SelectItem item)
  {
    themeSelectItem = item;
  }
  
  public void onThemeSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String themeId = (String)item.getValue();
    setSelectedTheme(themeId);
  }  

  public void onThemeClear() 
  {
    editing.setThemeId(null);    
  }  
  
  public void setSelectedTheme(String themeId)
  {
    editing.setThemeId(themeId);
    
    if (themeSelectItem == null || 
      !themeId.equals(themeSelectItem.getValue()))
    {
      themeSelectItem = newThemeSelectItem(themeId);
    }
    
    showDialog();    
  }
  
  public List<SelectItem> completeTheme(String query)
  {
    return completeTheme(query, editing.getThemeId());
  }  

  public List<SelectItem> getFavorites()
  {
    ThemeBacking themeBacking = WebUtils.getBacking(THEME_BACKING);
    return themeBacking.getFavorites();
  }
  
  private List<SelectItem> completeTheme(String query, String themeId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    ThemeBacking themeBacking = WebUtils.getBacking(THEME_BACKING);
    
    //Add current item
    if (!isNew(editing))
    {
      String description = "";
      if (themeId != null)
        description = themeBacking.getDescription(themeId);
      items.add(new SelectItem(themeId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      ThemeFilter filter = new ThemeFilter();
      filter.setDescription(query);      
      filter.setMaxResults(10);
      try
      {
        List<Theme> themes = AgendaConfigBean.getPort().findThemes(filter);
        if (themes != null)
        {       
          for (Theme theme : themes)
          {
            String description = themeBacking.getDescription(theme);
            SelectItem item = new SelectItem(theme.getThemeId(), description);
            items.add(item);
          }
        }        
      }
      catch (Exception ex) 
      { 
      }
    }
    else
    {
      //Add favorites
      items.addAll(themeBacking.getFavorites()); 
    }    
    return items;
  }  
   
  @Override
  public String show(String pageObjectId)
  {
    return show();
  }  
  
  @Override
  public String show()
  {    
    populate();
    return OUTCOME;
  }
 
  public String createEventTheme()
  {
    editing = new EventTheme();    
    return null;
  }  
  
  public String removeEventTheme(EventThemeView row)
  {
    try
    {
      if (row == null)
        throw new Exception("THEME_MUST_BE_SELECTED");
      
      String rowEventThemeId = row.getEventThemeId();
      
      if (editing != null && rowEventThemeId.equals(editing.getEventThemeId()))
        editing = null;
      
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.removeEventTheme(rowEventThemeId);
      
      info("REMOVE_OBJECT");      
      return show();
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
      if (editing == null)
        return null;
      
      //Person must be selected
      if (editing.getThemeId() == null || 
        editing.getThemeId().isEmpty())
      {
        throw new Exception("THEME_MUST_BE_SELECTED"); 
      }     
                      
      String eventId = eventBacking.getObjectId();
      editing.setEventId(eventId);
      
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.storeEventTheme(editing);
    
      editing = null;
      themeSelectItem = null; 
    
      info("STORE_OBJECT");
      hideDialog();
      return show();
    }
    catch (Exception ex)
    {     
      error(ex);
      showDialog();
    }
    return null;
  }

  @Override
  public List<EventThemeView> getResults(int firstResult, int maxResults)
  {
    try
    {
      AgendaManagerClient client = AgendaConfigBean.getPort();
      String eventId = eventBacking.getObjectId();
      EventThemeFilter filter = new EventThemeFilter();
      filter.setEventId(eventId);
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return client.findEventThemeViewsFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String store()
  {
    return storeEventTheme();
  }
  
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new EventTheme();
  }
  
  @Override
  public String cancel()
  {
    editing = null;
    themeSelectItem = null;
    info("CANCEL_OBJECT");
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
  }     

  private boolean isNew(EventTheme eventTheme)
  {
    return (eventTheme != null && eventTheme.getEventThemeId() == null);
  }  
  
  private SelectItem newThemeSelectItem(String themeId)
  {
    ThemeBacking themeBacking = WebUtils.getBacking(THEME_BACKING);    
    
    String description = 
      themeBacking.getDescription(themeId);
    return new SelectItem(themeId, description);    
  }
    
}
