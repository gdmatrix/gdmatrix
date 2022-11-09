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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.PersonView;
import org.matrix.pf.kernel.PersonBacking;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.agenda.web.AgendaConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named("eventPersonsBacking")
public class EventPersonsBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{
  private static final String PERSON_BACKING = "personBacking";
  private static final String EVENT_BACKING = "eventBacking";
  
  private static final String OUTCOME = "pf_event_persons";
  
  private static final String GROUPBY_PROPERTY = "groupBy";
  
  private EventBacking eventBacking;
  
  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<AttendantView> resultListHelper;
  private TabHelper tabHelper;
  
  private Attendant editing;
  private final Map<String, Attendant> unavailableAttendants = new HashMap();    
  private SelectItem personSelectItem;

  public EventPersonsBacking()
  { 
  }
  
  @PostConstruct
  public void init()
  {
    eventBacking = WebUtils.getBacking(EVENT_BACKING);   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
  }
  
  public Attendant getEditing()
  {
    return editing;
  }

  public void setEditing(Attendant editing)
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
      return editing.getAttendantId();
    else
      return null;
  }
  
  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);
      return getDescription(personBacking, editing.getPersonId());
    }
    return null;
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ATTENDANT_TYPE;
  }
  
  @Override
  public String getTypeId()
  {
    return eventBacking.getTabTypeId();    
  }
  
  @Override
  public ResultListHelper<AttendantView> getResultListHelper()
  {
    return resultListHelper;
  }  
  
  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }  

  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }
  
  public List<AttendantView> getRows()
  {
    return resultListHelper.getRows();
  }
    
  public String getAttendantTypeId()
  {
    String typeId = null;
    AttendantView row = (AttendantView)getValue("#{row}");
    if (row != null)
    {
      typeId = row.getAttendantTypeId();
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        return type.getDescription();
      }
    }
    return typeId;    
  }
    
  public boolean isRenderGroupedResults()
  {
    return getProperty(GROUPBY_PROPERTY) != null;
  }  
  
  //Person selection
  public SelectItem getPersonSelectItem()
  {
    return personSelectItem;
  }
  
  public void setPersonSelectItem(SelectItem item)
  {
    personSelectItem = item;
  }
  
  public boolean isHidden()
  {
    if (editing != null && editing.isHidden() != null)
      return editing.isHidden();
    else
      return false;
  }

  public void setHidden(boolean hidden)
  {
    editing.setHidden(hidden);
  }  
  
  public void onPersonSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String personId = (String)item.getValue();
    setSelectedPerson(personId);
  }  

  public void onPersonClear() 
  {
    editing.setPersonId(null);    
  }
  
  public void setSelectedPerson(String personId)
  {
    editing.setPersonId(personId);    
    if (personSelectItem == null || 
      !personId.equals(personSelectItem.getValue()))
    {
      personSelectItem = newPersonSelectItem(personId);
    }
    showDialog();    
  }
  
  public List<SelectItem> completePerson(String query)
  {
    return completePerson(query, editing.getPersonId());
  }  

  public List<SelectItem> getFavorites()
  {
    PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);
    return personBacking.getFavorites();
  }
  
  private List<SelectItem> completePerson(String query, String personId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);
    
    //Add current item
    if (!isNew(editing))
    {
      String description = "";
      if (personId != null)
        description = personBacking.getDescription(personId);
      items.add(new SelectItem(personId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      PersonFilter filter = new PersonFilter();
      filter.setFullName(query);
      filter.setMaxResults(10);
      List<PersonView> persons =
        KernelConfigBean.getPort().findPersonViews(filter);
      
      if (persons != null)
      {       
        for (PersonView person : persons)
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
   
  @Override
  public String show(String pageObjectId)
  {
    editPerson(pageObjectId);
    showDialog();
    return (isEditing(pageObjectId) ? OUTCOME : show());
  }  
  
  @Override
  public String show()
  {    
    populate();
    return OUTCOME;
  }
  
  public String editPerson(AttendantView row)
  {
    String attendantId = null;
    if (row != null)
      attendantId = row.getAttendantId();

    return editPerson(attendantId);
  } 
  
  public String createPerson()
  {
    editing = new Attendant();
    return null;
  }  
  
  public String removePerson(AttendantView row)
  {
    try
    {
      if (row == null)
        throw new Exception("PERSON_MUST_BE_SELECTED");
      
      String rowAttendantId = row.getAttendantId();
      
      if (editing != null && rowAttendantId.equals(editing.getAttendantId()))
        editing = null;
      
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.removeAttendant(rowAttendantId);
      
      info("REMOVE_OBJECT");      
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  public String storePerson()
  {
    try
    {
      if (editing == null)
        return null;
      
      //Person must be selected
      if (editing.getPersonId() == null || 
        editing.getPersonId().isEmpty())
      {
        throw new Exception("PERSON_MUST_BE_SELECTED"); 
      }     
                      
      String eventId = eventBacking.getObjectId();
      editing.setEventId(eventId);
      
      if (editing.getAttendantTypeId() == null)
      {
        editing.setAttendantTypeId(typedHelper.getTypeId());
      }
                  
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.storeAttendant(editing);
    
      editing = null;
      personSelectItem = null; 
    
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
  public List<AttendantView> getResults(int firstResult, int maxResults)
  {
    try
    {
      AgendaManagerClient client = AgendaConfigBean.getPort();
      String eventId = eventBacking.getObjectId();
      
      List<Attendant> unavailableList = 
        client.findAttendantsOccupancyFromCache(eventId);
      for (Attendant unavailable : unavailableList)
      {
        unavailableAttendants.put(unavailable.getPersonId(), unavailable);
      }      
      
      AttendantFilter filter = new AttendantFilter();
      filter.setEventId(eventId);
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return client.findAttendantViewsFromCache(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public boolean isAttendantAvailable(String personId)
  {
    return (unavailableAttendants.get(personId) == null);
  }  
  
  @Override
  public String store()
  {
    return storePerson();
  }
  
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new Attendant();
  }
  
  @Override
  public String cancel()
  {
    editing = null;
    personSelectItem = null;    
    info("CANCEL_OBJECT");
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
  }     
  
  public String getAttendedLabel()
  {
    String attended = (String)getValue("#{row.attended}");
    if ("S".equals(attended)) return "SI";
    else if ("N".equals(attended)) return "NO";
    else if ("J".equals(attended)) return "FJ";
    else return "";
  }

  private boolean isNew(Attendant attendant)
  {
    return (attendant != null && attendant.getAttendantId() == null);
  }  
    
  private String editPerson(String attendantId)
  {
    try
    {
      if (attendantId != null && !isEditing(attendantId))
      {
        editing = AgendaConfigBean.getPort().loadAttendant(attendantId);
        loadPersonSelectItem();
      }
      else if (attendantId == null)
      {
        editing = new Attendant();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  private void loadPersonSelectItem()
  {
    if (editing != null)
    {
      String personId = editing.getPersonId();      
      if (personId != null)
        personSelectItem = newPersonSelectItem(personId);      
    }
  }  
  
  private SelectItem newPersonSelectItem(String personId)
  {
    PersonBacking personBacking = WebUtils.getBacking(PERSON_BACKING);    
    
    String description = 
      personBacking.getDescription(personId);
    return new SelectItem(personId, description);    
  }
    
}
