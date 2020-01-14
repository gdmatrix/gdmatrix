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
package org.santfeliu.kernel.web;

import java.util.List;

import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.KernelConstants;

import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.PersonPerson;
import org.matrix.kernel.PersonPersonFilter;
import org.matrix.kernel.PersonPersonView;
import org.santfeliu.dic.TypeCache;

import org.santfeliu.web.obj.DynamicTypifiedPageBean;


/**
 *
 * @author unknown
 */
public class PersonPersonsBean extends DynamicTypifiedPageBean
{
  private PersonPerson editingPerson;
  private List<PersonPersonView> rows;

  public PersonPersonsBean()
  {
    super(DictionaryConstants.PERSON_PERSON_TYPE,
      KernelConstants.KERNEL_ADMIN_ROLE);
    load();
  }

  public PersonPerson getEditingPerson()
  {
    return editingPerson;
  }

  public void setEditingPerson(PersonPerson editingPerson)
  {
    this.editingPerson = editingPerson;
  }

  public List<PersonPersonView> getRows()
  {
    return rows;
  }

  public void setRows(List<PersonPersonView> rows)
  {
    this.rows = rows;
  }

  public boolean isModified()
  {
    return editingPerson != null;
  }
  
  public String show()
  {
    return "person_persons";
  }

  public String store()
  {
    if (editingPerson != null)
    {
      storePerson();
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
      (String)getValue("#{row.relPersonView.personId}"));
  }

  public String removePerson()
  {
    try
    {
      PersonPersonView row =
        (PersonPersonView)getRequestMap().get("row");
      preRemove();
      KernelManagerPort port = KernelConfigBean.getPort();
      port.removePersonPerson(row.getPersonPersonId());
      postRemove();
      load();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String editPerson()
  {
    try
    {
      PersonPersonView row =
        (PersonPersonView)getRequestMap().get("row");

      KernelManagerPort port = KernelConfigBean.getPort();
      editingPerson =
        port.loadPersonPerson(row.getPersonPersonId());
      setCurrentTypeId(editingPerson.getPersonPersonTypeId());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }
  
  public String addPerson()
  {
    editingPerson = new PersonPerson();
    editingPerson.setPersonId(getObjectId());
    return null;
  }

  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{personPersonsBean.editingPerson.relPersonId}");
  }

  public String storePerson()
  {
    try
    {
      preStore();
      org.matrix.dic.Type type = getCurrentType();
      if (type != null)
        editingPerson.setPersonPersonTypeId(type.getTypeId());      
      KernelConfigBean.getPort().storePersonPerson(editingPerson);
      postStore();
      editingPerson = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelPerson()
  {
    editingPerson = null;
    load();
    return null;
  }

  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(editingPerson.getRelPersonId());
  }

  
  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String getTypeDescription()
  {
    PersonPersonView row = (PersonPersonView)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String typeId = row.getPersonPersonTypeId();
    String description =
      TypeCache.getInstance().getType(typeId).getDescription();

    return (description != null ? description : typeId);
  }
  
  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
  
  @Override
  protected String getRowTypeId(Object row)
  {
    PersonPersonView ppRow = (PersonPersonView)row;
    return ppRow.getPersonPersonTypeId();
  }  

  protected void load()
  {
    try
    {
      if (!isNew())
      {
        PersonPersonFilter filter = new PersonPersonFilter();
        filter.setPersonId(getObjectId());
        rows = KernelConfigBean.getPort().findPersonPersonViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
