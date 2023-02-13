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
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.primefaces.PrimeFaces;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class AddressPersonsTabBean extends TabBean
{
  private List<PersonAddressView> rows;
  private PersonAddress editing;
  private int firstRow;
  
  @Inject
  AddressObjectBean addressObjectBean;
  
  @Override
  public ObjectBean getObjectBean()
  {
    return addressObjectBean;
  }

  public List<PersonAddressView> getRows()
  {
    return rows;
  }

  public void setRows(List<PersonAddressView> rows)
  {
    this.rows = rows;
  }

  public PersonAddress getEditing()
  {
    return editing;
  }

  public void setEditing(PersonAddress editing)
  {
    this.editing = editing;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }
  
  @Override
  public void load() throws Exception
  {
    System.out.println("load addressPersons:" + objectId);    
    if (!NEW_OBJECT_ID.equals(objectId))
    {    
      try
      {
        PersonAddressFilter filter = new PersonAddressFilter();
        filter.setAddressId(addressObjectBean.getObjectId());
        filter.setMaxResults(0);
        rows = KernelModuleBean.getPort(false).findPersonAddressViews(filter);
      }
      catch(Exception ex)
      {
        error(ex);
      }
    }
    else
      rows = Collections.emptyList();
  }
  
  public void create()
  {
    editing = new PersonAddress();
  }
  
  public void onPersonClear()
  {
    editing.setPersonId(null);
  }    
  
  @Override
  public void store() throws Exception
  {
    storePerson();
    load();
  }

  public void remove(PersonAddressView row) throws Exception
  {
    removePerson(row);
    load();
  }

  public String cancel()
  {
    editing = null;
    info("CANCEL_OBJECT");
    return null;
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
      editing = (PersonAddress)stateArray[0];

      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  private void storePerson()
  {
    try
    {
      if (editing != null)
      {
        if (editing.getPersonId() == null || editing.getPersonId().isEmpty())
        {
          throw new Exception("PERSON_MUST_BE_SELECTED");
        }

        String addressId = addressObjectBean.getObjectId();
        editing.setAddressId(addressId);
        KernelModuleBean.getPort(false).storePersonAddress(editing);
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

  private String removePerson(PersonAddressView row)
  {
    try
    {
      if (row == null)
        throw new Exception("PERSON_MUST_BE_SELECTED");

      String rowId = row.getPersonAddressId();

      if (editing != null &&
        rowId.equals(editing.getPersonAddressId()))
        editing = null;

      KernelModuleBean.getPort(false).removePersonAddress(rowId);

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
    current.executeScript("PF('addressPersonsDialog').show();");
  }

  private void hideDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('addressPersonsDialog').hide();");
  }  

}
