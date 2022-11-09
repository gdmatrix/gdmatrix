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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.Event;
import org.matrix.agenda.EventFilter;
import org.matrix.agenda.Theme;
import org.matrix.agenda.ThemeFilter;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonFilter;
import static org.matrix.pf.agenda.EventConfigBacking.LOAD_METADATA_PROPERTY;
import org.matrix.pf.kernel.PersonBacking;
import org.matrix.pf.web.SearchBacking;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedPage;
import org.matrix.security.SecurityConstants;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.ControllerBean;

/**
 *
 * @author blanquepa
 */
@Named("eventSearchBacking")
public class EventSearchBacking extends SearchBacking 
  implements TypedPage
{
  private static final String PERSON_BACKING = "personBacking";  
  
  public static final String OUTCOME = "pf_event_search";
  
  private EventBacking eventBacking;
  
  private EventFilter filter;
  private TypedHelper typedHelper;
  
  private String searchEventTypeId;
  
  private SelectItem personSelectItem;
  
  public EventSearchBacking()
  {   
  }
  
  @PostConstruct
  public void init()
  {
    eventBacking = WebUtils.getBacking("eventBacking");
    filter = new EventFilter();
    String typeId = getMenuItemTypeId();
    if (typeId != null)
      filter.getEventTypeId().add(typeId);
    smartValue = null;
    typedHelper = new TypedHelper(this); 
  }
  
  public EventFilter getFilter()
  {
    return filter;
  }

  public void setFilter(EventFilter filter)
  {
    this.filter = filter;
  }

  public List<String> getEventId()
  {
    return filter.getEventId();
  }

  public void setEventId(List<String> eventIds)
  {
    filter.getEventId().clear();
    if (eventIds != null && !eventIds.isEmpty())
      this.filter.getEventId().addAll(eventIds);
  }
  
  public Date getFromDate()
  {
    if (filter.getStartDateTime() != null)
      return TextUtils.parseInternalDate(filter.getStartDateTime());
    else
      return null;
  }
  
  public void setFromDate(Date date)
  {
    if (date != null)
      filter.setStartDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    else
      filter.setStartDateTime(null);
  }
  
  public Date getToDate()
  {
    if (filter.getEndDateTime() != null)
      return TextUtils.parseInternalDate(filter.getEndDateTime());
    else
      return null;
  }
  
  public void setToDate(Date date)
  {
    if (date != null)
      filter.setEndDateTime(TextUtils.formatDate(date, "yyyyMMddHHmmss"));
    else
      filter.setEndDateTime(null);
  }

  public String getSearchEventTypeId() 
  {
    return searchEventTypeId;
  }

  public void setSearchEventTypeId(String searchEventTypeId) 
  {
    this.searchEventTypeId = searchEventTypeId;
  }

  public String getThemeId() 
  {
    if (!filter.getThemeId().isEmpty())
      return filter.getThemeId().get(0);
    else
      return null;
  }

  public void setThemeId(String themeId) 
  {
    filter.getThemeId().clear();
    if (themeId != null && themeId.length() > 0)
    {
      filter.getThemeId().add(themeId);
    }
  }

  public SelectItem getPersonSelectItem() 
  {
    return personSelectItem;
  }

  public void setPersonSelectItem(SelectItem personSelectItem) 
  {
    this.personSelectItem = personSelectItem;
  }

  public void setSelectedPerson(String personId)
  {
    filter.setPersonId(personId);
    if (personSelectItem == null || 
      !personId.equals(personSelectItem.getValue()))
    {
      PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);    
      String description = getDescription(personBacking, personId);
      personSelectItem = new SelectItem(personId, description);       
    }    
  }

  public boolean isPublicType(String typeId)
  {
    if (!StringUtils.isBlank(typeId))
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        return type.canPerformAction(DictionaryConstants.READ_ACTION, 
            Collections.singleton(SecurityConstants.EVERYONE_ROLE));
      }
    }
    return false;
  }
  
  //TODO Filter themes
  public List<SelectItem> getThemeSelectItems()
  {
    List<SelectItem> themeSelectItems = new ArrayList();    
    try
    {
      themeSelectItems.add(new SelectItem(ControllerBean.NEW_OBJECT_ID, " "));
      List<Theme> themes =
        AgendaConfigBean.getPort().findThemesFromCache(new ThemeFilter());
      for (Theme theme : themes)
      {
        SelectItem item = new SelectItem(theme.getThemeId(), theme.getDescription());
        themeSelectItems.add(item);
      }
      return FacesUtils.sortSelectItems(themeSelectItems);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return themeSelectItems;
  }

  @Override
  public EventBacking getObjectBacking()
  {
    return eventBacking;
  }
  
  @Override
  public String show()
  {
    if (filter.getStartDateTime() == null)
    {
      setFromDate(new Date());
    }   
    String outcome = super.show();
    return outcome;
  }
     
  @Override
  public String search()
  {
    smartValue = convert(filter);
    
    filter.getEventTypeId().clear();
    filter.getEventTypeId().add(searchEventTypeId);
    if (filter.getStartDateTime() == null)
    {
      setFromDate(new Date());
    }
    
    return super.search();
  }
  
  @Override
  public String smartSearch()
  {
    filter = convert(smartValue); 
    filter.getEventTypeId().clear();    
    String typeId = getMenuItemTypeId();
    if (typeId != null)
      filter.getEventTypeId().add(typeId);
    setFromDate(new Date());    
    return super.search();
  }

  @Override
  public String clear()
  {
    filter = new EventFilter();
    searchEventTypeId = getMenuItemTypeId();
    setFromDate(new Date());
    setToDate(null);
    smartValue = null;
    personSelectItem = null;
    bigListHelper.reset();
    return null;
  }
  
  @Override
  public int countResults()
  {
    try
    {
      return AgendaConfigBean.getPort().countEvents(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List<Event> getResults(int firstResult, int maxResults)
  {
    List<Event> results = null;
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      results = AgendaConfigBean.getPort().findEvents(filter);
      loadMetadata(results);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return results;
  }  
  
  private EventFilter convert(String smartValue)
  {
    filter = new EventFilter();
    if (smartValue != null)
    {
      try
      {
        Integer.valueOf(smartValue);
        filter.getEventId().add(smartValue);
      }
      catch (NumberFormatException ex)
      {
        if (!StringUtils.isBlank(smartValue))
          filter.setContent(smartValue);
      }
    }  
    return filter;
  }
    
  private String convert(EventFilter filter)
  {
    String value = null;
    if (!filter.getEventId().isEmpty())
      value = filter.getEventId().get(0);
    else if (!StringUtils.isBlank(filter.getContent()))
    {
      value = filter.getContent();
      filter.setContent("%" + filter.getContent() + "%");
    }
    return value;
  }

  @Override
  public String getTypeId()
  {
    return searchEventTypeId != null ? searchEventTypeId : getMenuItemTypeId();
  }
  
  @Override
  public String getOutcome()
  {
    return OUTCOME;
  }
  
  @Override
  public String getRootTypeId()
  {
    return eventBacking.getRootTypeId();
  }
  
  @Override
  public String getConfigTypeId() 
  {
    return getMenuItemTypeId();
  }  

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  public void onPersonSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String personId = (String)item.getValue();
    filter.setPersonId(personId);
  }

  public void onPersonClear() 
  {
    filter.setPersonId(null);
  }

  public List<SelectItem> completePerson(String query)
  {
    ArrayList<SelectItem> items = new ArrayList();
    PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);
    
    //Query search
    if (query != null && query.length() >= 3)
    {
      PersonFilter personFilter = new PersonFilter();
      personFilter.setFullName(query);
      personFilter.setMaxResults(10);
      List<Person> persons = 
        KernelConfigBean.getPort().findPersons(personFilter);
      if (persons != null)
      {       
        for (Person person : persons)
        {
          String description = personBacking.getDescription(person);
          SelectItem item = new SelectItem(person.getPersonId(), description);
          items.add(item);
        }
      }
    }
    else
    {
      //Add favorites
      items.addAll(personBacking.getFavorites()); 
    }
    
    return items;
  }  
  
  //TODO: Move to SearchBean
  private void loadMetadata(List<Event> results) throws Exception
  {    
    boolean loadMetadata = (getProperty(LOAD_METADATA_PROPERTY) != null
      && getProperty(LOAD_METADATA_PROPERTY).equalsIgnoreCase("true"));
    if (loadMetadata)
    {
      for (int i = 0; i < results.size(); i++)
      {
        Event event = 
          AgendaConfigBean.getPort().loadEvent(results.get(i).getEventId());
        results.set(i, event);
      }
    }
  }  
  
}
