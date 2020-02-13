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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.agenda.AgendaManagerPort;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.agenda.Event;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;

import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.kernel.PersonView;
import org.santfeliu.agenda.client.AgendaManagerClient;
import org.santfeliu.cases.web.CaseConfigBean;

import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.web.obj.TypifiedPageBean;


/**
 *
 * @author unknown
 */
public class AttendantsBean extends TypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_attendantTypeId";
  public static final String GROUP_CASE_ID_PROPERTY = "_groupCaseId";

  private Attendant attendant;
  private List<AttendantView> rows;
  private Map<String, Attendant> unavailableAttendants = new HashMap();
  private transient List<SelectItem> groupSelectItems;

  public AttendantsBean()
  {
    super(DictionaryConstants.ATTENDANT_TYPE, "AGENDA_ADMIN");

    EventMainBean eventMainBean = (EventMainBean)getBean("eventMainBean");
    Event event = eventMainBean.getEvent();
    Type eventType = TypeCache.getInstance().getType(event.getEventTypeId());
    if (eventType != null)
    {
      PropertyDefinition pd =
        eventType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        setRootTypeId(pd.getValue().get((0)));
    }
    load();
  }
  
  public void setRows(List<AttendantView> rows)
  {
    this.rows = rows;
  }

  @Override
  public List<AttendantView> getRows()
  {
    return rows;
  }

  public boolean isHidden()
  {
    if (attendant != null && attendant.isHidden() != null)
      return attendant.isHidden().booleanValue();
    else
      return false;
  }

  public void setHidden(boolean hidden)
  {
    attendant.setHidden(hidden);
  }

  public Attendant getAttendant()
  {
    return attendant;
  }
  
  public String show()
  {
    return "attendants";
  }
  
  @Override
  public String store()
  {
    if (attendant != null)
    {
      storeAttendant();
    }
    else
    {
      load();
    }
    return show();
  }

  public String showPerson()
  {
    return getControllerBean().showObject("Person",
      (String)getValue("#{row.personView.personId}"));
  }

  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{attendantsBean.attendant.personId}");
  }

  public String removePerson()
  {
    try
    {
      AttendantView row = (AttendantView)getRequestMap().get("row");
      preRemove();
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.removeAttendant(row.getAttendantId());
      postRemove();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeAttendant()
  {
    try
    {     
      if (attendant == null || StringUtils.isBlank(attendant.getPersonId()))
        throw new Exception("ATTENDANT_MUST_BE_SELECTED");
    
      String eventId = getObjectId();
      attendant.setEventId(eventId);    

      preStore();
      
      AgendaManagerPort port = AgendaConfigBean.getPort();
      port.storeAttendant(attendant);
      
      postStore();
      
      attendant = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  
  
  public String cancelAttendant()
  {
    attendant = null;
    return null;
  }

  public String createAttendant()
  {
    attendant = new Attendant();
    return null;
  }
  
  public String editAttendant()
  {
    try
    {
      AttendantView row = (AttendantView)getExternalContext().
        getRequestMap().get("row");   
      String attendantId = row.getAttendantId();
      if (attendantId != null)
        attendant =
          AgendaConfigBean.getPort().loadAttendantFromCache(attendantId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(attendant.getPersonId());
  }
  
  public String getAttendedLabel()
  {
    String attended = (String)getValue("#{row.attended}");
    if ("S".equals(attended)) return "SI";
    else if ("N".equals(attended)) return "NO";
    else if ("J".equals(attended)) return "FJ";
    else return "";
  }

  public List<SelectItem> getGroupSelectItems()
  {
    if (groupSelectItems == null)
    {
      groupSelectItems = new ArrayList<SelectItem>();
      try
      {
        if (attendant != null)
        {
          String typeId = attendant.getAttendantTypeId();
          if (typeId != null && typeId.length() > 0)
          {
            TypeCache typeCache = TypeCache.getInstance();
            Type type = typeCache.getType(typeId);
            PropertyDefinition propDef =
              type.getPropertyDefinition(GROUP_CASE_ID_PROPERTY);
            if (propDef != null)
            {
              List<String> values = propDef.getValue();
              if (!values.isEmpty())
              {
                String caseId = values.get(0);
                CaseManagerPort port = CaseConfigBean.getPort();
                CasePersonFilter filter = new CasePersonFilter();
                filter.setCaseId(caseId);
                List<CasePersonView> views = port.findCasePersonViews(filter);
                if (!views.isEmpty())
                {
                  SelectItem selectItem = new SelectItem();
                  selectItem.setLabel("---------------------------");
                  selectItem.setValue(" ");
                  groupSelectItems.add(selectItem);
                  for (CasePersonView view : views)
                  {
                    PersonView personView = view.getPersonView();
                    selectItem = new SelectItem();
                    selectItem.setValue(personView.getPersonId());
                    selectItem.setDescription(personView.getFullName());
                    selectItem.setLabel(personView.getFullName());
                    groupSelectItems.add(selectItem);
                  }
                  FacesUtils.sortSelectItems(groupSelectItems);
                }
              }
            }
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return groupSelectItems;
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        AttendantFilter filter = new AttendantFilter();
        String eventId = getObjectId();
        filter.setEventId(eventId);
        AgendaManagerClient port = AgendaConfigBean.getPort();
        rows = port.findAttendantViewsFromCache(filter);

        List<Attendant> unavailableList = port.findAttendantsOccupancyFromCache(eventId);
        for (Attendant unavailable : unavailableList)
        {
          unavailableAttendants.put(unavailable.getPersonId(), unavailable);
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void setAttendant(Attendant attendant)
  {
    this.attendant = attendant;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public boolean isAttendantAvailable()
  {
    String personId = (String)getValue("#{row.personView.personId}");
    return (unavailableAttendants.get(personId) == null);
  }

  @Override
  protected String getRowTypeId(Object row)
  {
    AttendantView attRow = (AttendantView)row;
    return attRow.getAttendantTypeId();
  }
  
  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
  
  public String getRowTypeDescription()
  {
    AttendantView attendantView = (AttendantView)getValue("#{row}");
    if (attendantView != null && attendantView.getAttendantTypeId() != null)
    {
      String typeId = attendantView.getAttendantTypeId();
      if (typeId != null)
      {  
        TypeCache typeCache = TypeCache.getInstance();
        try
        {
          Type type = typeCache.getType(typeId);
          if (type != null)
            return type.getDescription();
        }
        catch (Exception ex)
        {          
        }
        return typeId;
      }  
    }
    return "";
  }  
}
