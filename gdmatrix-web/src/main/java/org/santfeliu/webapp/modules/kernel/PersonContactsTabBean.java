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
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.Contact;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class PersonContactsTabBean extends TabBean
{

  @Inject
  private PersonObjectBean personObjectBean;

  //Helpers
  private List<ContactView> rows;
  private int firstRow;
  private Contact editing;

  public PersonContactsTabBean()
  {
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
  }

  public List<ContactView> getRows()
  {
    return rows;
  }

  public void setRows(List<ContactView> rows)
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

  public Contact getEditing()
  {
    return editing;
  }

  public void setEditing(Contact contact)
  {
    this.editing = contact;
  }
  
  public String getContactTypeId()
  {
    return editing.getContactTypeId();
  }
  
  public void setContactTypeId(String contactTypeId)
  {
    if (editing != null)
    {
      editing.setContactTypeId(contactTypeId);
    }
  }

  @Override
  public void load()
  {
    if (!NEW_OBJECT_ID.equals(getObjectId()))
    {    
      try
      {    
        ContactFilter filter = new ContactFilter();
        filter.setPersonId(getObjectId());
        rows = KernelModuleBean.getPort(false).findContactViews(filter);
      }
      catch(Exception ex)
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
    editing = new Contact();
  }

  public void edit(ContactView contact)
  {
    try
    {
      String contactId = null;
      if (contact != null)
      {
        contactId = contact.getContactId();
      }

      if (contactId != null)
      {
        this.editing = KernelModuleBean.getPort(false).loadContact(contactId);
        this.editing.setContactId(contactId);
      }
      else
      {
        this.editing = new Contact();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public void store() throws Exception
  {
    try
    {
      if (editing != null)
      {
        editing.setPersonId(getObjectId());
        KernelModuleBean.getPort(false).storeContact(editing);
        editing = null;
        growl("STORE_OBJECT");        
      }
      load();
    }
    catch(Exception ex)
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
  
  public void remove(ContactView contact)
  {
    try
    {
      if (contact != null)
      {
        String contactId = contact.getContactId();
        KernelModuleBean.getPort(false).removeContact(contactId);
      }
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public Serializable saveState()
  {
    return new Object[] { editing };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      if (!isNew())
      {
        Object[] stateArray = (Object[])state;
        editing = (Contact)stateArray[0];
        load();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

}
