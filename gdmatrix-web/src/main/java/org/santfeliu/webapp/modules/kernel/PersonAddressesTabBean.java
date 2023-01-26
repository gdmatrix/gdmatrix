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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.primefaces.PrimeFaces;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.TabBean;
import org.santfeliu.webapp.helpers.ResultListHelper;

/**
 *
 * @author lopezrj-sf
 */
@Named
@ManualScoped
public class PersonAddressesTabBean extends TabBean
{
  @Inject
  private PersonObjectBean personObjectBean;

  @Inject
  private AddressObjectBean addressObjectBean;

  //Helpers
  private ResultListHelper<PersonAddressView> resultListHelper;

  private int firstRow;
  private PersonAddress editing;

  public PersonAddressesTabBean()
  {
  }

  @PostConstruct
  public void init()
  {
    resultListHelper = new PersonAddressResultListHelper();
  }

  @Override
  public ObjectBean getObjectBean()
  {
    return personObjectBean;
  }

  public PersonAddress getEditing()
  {
    return editing;
  }

  public void setEditing(PersonAddress editing)
  {
    this.editing = editing;
  }

  public ResultListHelper<PersonAddressView> getResultListHelper()
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
      return addressObjectBean.getDescription(editing.getAddressId());
    }
    return null;
  }

  public void setAddressId(String addressId)
  {
    editing.setAddressId(addressId);
    showDialog();
  }

  public String getAddressId()
  {
    return editing.getAddressId();
  }    
  
  public void onAddressClear()
  {
    editing.setAddressId(null);
  }

  @Override
  public void load()
  {
    resultListHelper.find();
  }

  public void create()
  {
    editing = new PersonAddress();
  }
  
  @Override
  public void store()
  {
    storeAddress();
    resultListHelper.find();
  }

  public void remove(PersonAddressView row)
  {
    removeAddress(row);
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
      editing = (PersonAddress)stateArray[0];

      if (!isNew()) resultListHelper.find();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  private boolean isNew(PersonAddress personAddress)
  {
    return (personAddress != null &&
      personAddress.getPersonAddressId() == null);
  }

  private void storeAddress()
  {
    try
    {
      if (editing != null)
      {
        //Address must be selected
        if (editing.getAddressId() == null || editing.getAddressId().isEmpty())
        {
          throw new Exception("ADDRESS_MUST_BE_SELECTED");
        }

        String personId = personObjectBean.getObjectId();
        editing.setPersonId(personId);
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

  private String removeAddress(PersonAddressView row)
  {
    try
    {
      if (row == null)
        throw new Exception("ADDRESS_MUST_BE_SELECTED");

      String rowPersonAddressId = row.getPersonAddressId();

      if (editing != null &&
        rowPersonAddressId.equals(editing.getPersonAddressId()))
        editing = null;

      KernelModuleBean.getPort(false).removePersonAddress(rowPersonAddressId);

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
    current.executeScript("PF('addressDataDialog').show();");
  }

  private void hideDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('addressDataDialog').hide();");
  }

  private class PersonAddressResultListHelper extends
    ResultListHelper<PersonAddressView>
  {
    @Override
    public List<PersonAddressView> getResults(int firstResult, int maxResults)
    {
      try
      {
        PersonAddressFilter filter = new PersonAddressFilter();
        filter.setPersonId(personObjectBean.getObjectId());
        filter.setFirstResult(firstResult);
        filter.setMaxResults(maxResults);
        return KernelModuleBean.getPort(false).findPersonAddressViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return null;
    }
  }

}
