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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.agenda.Attendant;
import org.matrix.agenda.AttendantFilter;
import org.matrix.agenda.AttendantView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Person;
import org.matrix.web.WebUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.ReferenceHelper;
import org.santfeliu.webapp.helpers.ResultListHelper;
import org.santfeliu.webapp.modules.kernel.PersonObjectBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class EventPersonsTabBean extends TabBean
{    
  private static final String TYPE_BEAN = "typeBean";  
  
  @Inject
  EventObjectBean eventObjectBean;

  @Inject
  PersonObjectBean personObjectBean;
  
  //Helpers
  private ResultListHelper<AttendantView> resultListHelper;  
  ReferenceHelper<Person> personReferenceHelper;   
  
  private int firstRow;
  private Attendant editing;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
    resultListHelper = new EventPersonResultListHelper();
    personReferenceHelper = 
      new PersonReferenceHelper(DictionaryConstants.PERSON_TYPE);    
  }  
  
  @Override
  public ObjectBean getObjectBean()
  {
    return eventObjectBean;
  }

  public ReferenceHelper<Person> getPersonReferenceHelper() 
  {
    return personReferenceHelper;
  }  
  
  //TODO Move to superclass
  public String getRootTypeId()
  {
    return DictionaryConstants.ATTENDANT_TYPE;
  }  
  
  public Attendant getEditing() 
  {
    return editing;
  }

  public void setEditing(Attendant editing) 
  {
    this.editing = editing;
  }

  public ResultListHelper<AttendantView> getResultListHelper()
  {
    return resultListHelper;
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
      return personObjectBean.getDescription(editing.getPersonId());
    }
    return null;
  }  
  
  //TODO Use ObjectDescriptor
  public String getAttendantTypeDescription()
  {
    String typeId = null;
    AttendantView row = (AttendantView)getValue("#{row}");
    if (row != null)
    {
      typeId = row.getAttendantTypeId();
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        if (type != null) return type.getDescription();
      }
    }
    return typeId;    
  }

  public String getAttendedLabel()
  {
    String attended = (String)getValue("#{row.attended}");
    if (attended == null) return "";
    else switch (attended) 
    {
      case "S":
        return "SI";
      case "N":
        return "NO";
      case "J":
        return "FJ";
      default:
        return "";
    }
  }  
  
  //TODO Use TypedHelper  
  public List<Type> getAllTypes()
  {
    TypeCache tc = TypeCache.getInstance();
    List<Type> types = new ArrayList<>();
    List<SelectItem> items = getAllTypeItems();
    for (SelectItem i : items)
    {
      types.add(tc.getType(String.valueOf(i.getValue())));
    }
    return types;
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
    editing.setPersonId(personId);
  }  

  public void onPersonClear() 
  {
    editing.setPersonId(null);
  }  
    
  public void setSelectedPerson(String personId)
  {
    editing.setPersonId(personId);
    showDialog();
  }  
  
  public String edit(AttendantView row)
  {
    String attendantId = null;
    if (row != null)
      attendantId = row.getAttendantId();

    return editPerson(attendantId);
  }  
  
  @Override
  public void load()
  {
    resultListHelper.find();
  }
  
  public void create()
  {
    editing = new Attendant();
  }    

  @Override
  public void store()
  {
    storePerson();
    resultListHelper.find();
  }
  
  public void remove(AttendantView row)
  {
    removePerson(row);
    resultListHelper.find();
  }
  
  public String cancel()
  {
    editing = null;
    info("CANCEL_OBJECT");          
    return null;
  }  
  
  public void reset()
  {
    cancel();
    resultListHelper.clear();
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
      editing = (Attendant)stateArray[0];
      
      resultListHelper.find();
    }
    catch (Exception ex)
    {
      error(ex);
    }
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
        editing = 
          AgendaModuleBean.getClient(false).loadAttendantFromCache(attendantId);
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
  
  private void storePerson()
  {
    try
    {
      if (editing != null)
      {
        //Person must be selected
        if (editing.getPersonId() == null || editing.getPersonId().isEmpty())
        {
          throw new Exception("PERSON_MUST_BE_SELECTED"); 
        }

        String eventId = eventObjectBean.getObjectId();
        editing.setEventId(eventId);
        AgendaModuleBean.getClient(false).storeAttendant(editing);
        editing = null;
        info("STORE_OBJECT");
        hideDialog();
      }
    }
    catch (Exception ex)
    {
      error(ex);
      showDialog();
    }
  }  
  
  private String removePerson(AttendantView row)
  {
    try
    {
      if (row == null)
        throw new Exception("PERSON_MUST_BE_SELECTED");
      
      String rowAttendantId = row.getAttendantId();
      
      if (editing != null && 
        rowAttendantId.equals(editing.getAttendantId()))
        editing = null;
            
      AgendaModuleBean.getClient(false).removeAttendant(rowAttendantId);
      
      info("REMOVE_OBJECT");      
      return null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }    

  private void showDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('personDataDialog').show();");    
  }  

  private void hideDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('personDataDialog').hide();");    
  }  
  
  private boolean isEditing(String pageObjectId)
  {
    if (editing == null)
      return false;
    
    String attendantId = editing.getAttendantId();    
    return attendantId != null 
      && attendantId.equals(pageObjectId);
  }

  private List<SelectItem> getAllTypeItems()
  {
    return getAllTypeItems(DictionaryConstants.READ_ACTION,
      DictionaryConstants.CREATE_ACTION, DictionaryConstants.WRITE_ACTION);
  }

  private List<SelectItem> getAllTypeItems(String... actions)
  {
    try
    {
      String typeId = getRootTypeId();
      TypeBean typeBean = WebUtils.getBacking(TYPE_BEAN);
      String adminRole = eventObjectBean.getAdminRole();
      return typeBean.getAllSelectItems(typeId, adminRole, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return Collections.emptyList();
  }
  
  private class EventPersonResultListHelper extends 
    ResultListHelper<AttendantView>
  {
    @Override
    public List<AttendantView> getResults(int firstResult, int maxResults)
    {
      try
      {
        AttendantFilter filter = new AttendantFilter();
        filter.setEventId(eventObjectBean.getObjectId());
        filter.setFirstResult(firstResult);
        filter.setMaxResults(maxResults);        
        return AgendaModuleBean.getClient(false).
          findAttendantViewsFromCache(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return null;
    }
  }
  
  private class PersonReferenceHelper extends ReferenceHelper<Person>
  {
    public PersonReferenceHelper(String typeId)
    {
      super(typeId);
    }

    @Override
    public String getId(Person person)
    {
      return person.getPersonId();
    }

    @Override
    public String getSelectedId()
    {
      return editing != null ? editing.getPersonId() : "";
    }

    @Override
    public void setSelectedId(String value)
    {
      setSelectedPerson(value);
    }
  }
  
}
