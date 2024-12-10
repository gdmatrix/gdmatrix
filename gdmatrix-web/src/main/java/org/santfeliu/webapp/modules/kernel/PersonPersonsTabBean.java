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
package org.santfeliu.webapp.modules.kernel;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.PersonPerson;
import org.matrix.kernel.PersonPersonFilter;
import org.matrix.kernel.PersonPersonView;
import org.matrix.kernel.PersonView;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class PersonPersonsTabBean extends TabBean
{
  @Inject
  private PersonObjectBean personObjectBean;

  private List<PersonPersonView> rows;
  private int firstRow;
  private PersonPerson editing;

  public PersonPersonsTabBean()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
  }

  public PersonPerson getEditing()
  {
    return editing;
  }

  public void setEditing(PersonPerson personPerson)
  {
    this.editing = personPerson;
  }

  public List<PersonPersonView> getRows()
  {
    return rows;
  }

  public void setRows(List<PersonPersonView> rows)
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
      return personObjectBean.getDescription(editing.getPersonId());
    }
    return null;
  }

  public void setPersonId(String personId)
  {
    if (editing != null)
    {
      editing.setPersonId(personId);
    }
  }

  public String getPersonId()
  {
    return editing.getPersonId();
  }

  public void onPersonClear()
  {
    editing.setPersonId(null);
  }

  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {
      try
      {
        PersonPersonFilter filter = new PersonPersonFilter();
        filter.setPersonId(getObjectId());
        rows = KernelModuleBean.getPort(false).findPersonPersonViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    else
    {
      rows = Collections.emptyList();
      firstRow = 0;
    }
  }

  public void create()
  {
    editing = new PersonPerson();
  }
  
  public void edit(PersonPersonView personPersonView)
  {
    try
    {
      String personPersonId = null;
      if (personPersonView != null)
      {
        personPersonId = personPersonView.getPersonPersonId();
        if (personPersonId != null)
        {
          this.editing = 
            KernelModuleBean.getPort(false).loadPersonPerson(personPersonId);
        }
        else
        {
          this.editing = new PersonPerson();
        }        
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  

  @Override
  public void store()
  {
    storePerson();
    load();
  }

  public void remove(PersonPersonView row)
  {
    removePerson(row);
    load();
  }

  public String cancel()
  {
    editing = null;
    return null;
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
      editing = (PersonPerson)stateArray[0];

      if (!isNew()) load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(PersonPerson personPerson)
  {
    return (personPerson != null &&
      personPerson.getPersonPersonId() == null);
  }

  private void storePerson()
  {
    try
    {
      if (editing != null)
      {
        //Person must be selected
        if (editing.getRelPersonId() == null || editing.getRelPersonId().isEmpty())
        {
          throw new Exception("PERSON_MUST_BE_SELECTED");
        }

        editing.setPersonId(getObjectId());
        KernelModuleBean.getPort(false).storePersonPerson(editing);
        editing = null;
        growl("STORE_OBJECT");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private String removePerson(PersonPersonView row)
  {
    try
    {
      if (row == null)
        throw new Exception("PERSON_MUST_BE_SELECTED");

      String rowPersonPersonId = row.getPersonPersonId();

      if (editing != null &&
        rowPersonPersonId.equals(editing.getPersonPersonId()))
        editing = null;

      KernelModuleBean.getPort(false).removePersonPerson(rowPersonPersonId);

      growl("REMOVE_OBJECT");
      return null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

}
