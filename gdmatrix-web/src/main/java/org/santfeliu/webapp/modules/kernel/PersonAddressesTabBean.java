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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.matrix.web.WebUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.santfeliu.faces.ManualScoped;
import org.santfeliu.kernel.web.KernelConfigBean;
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
  private static final String ADDRESS_OBJECT_BEAN = "addressObjectBean";
  
  @Inject
  private PersonObjectBean personObjectBean;
    
  //Helpers
  private ResultListHelper<PersonAddressView> resultListHelper;
  
  private int firstRow;  
  private PersonAddress editing;
  
  private SelectItem addressSelectItem;
  
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
      AddressObjectBean addressObjectBean = 
        WebUtils.getBacking(ADDRESS_OBJECT_BEAN);
      return addressObjectBean.getDescription(editing.getAddressId());
    }
    return null;
  }
  
  //Address selection
  public SelectItem getAddressSelectItem()
  {
    return addressSelectItem;
  }
  
  public void setAddressSelectItem(SelectItem item)
  {
    addressSelectItem = item;
  }  

  public void onAddressSelect(SelectEvent<SelectItem> event) 
  {
    SelectItem item = event.getObject();
    String addressId = (String) item.getValue();
    editing.setAddressId(addressId);
  }  

  public void onAddressClear() 
  {
    editing.setAddressId(null);
  }  
  
  public List<SelectItem> completeAddress(String query)
  {
    return completeAddress(query, editing.getAddressId());
  }  
    
  private List<SelectItem> completeAddress(String query, String addressId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    AddressObjectBean addressObjectBean = 
      WebUtils.getBacking(ADDRESS_OBJECT_BEAN);
    
    //Add current item
    if (!isNew(editing))
    {
      String description = "";
      if (addressId != null)
        description = addressObjectBean.getDescription(addressId);
      items.add(new SelectItem(addressId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      AddressFilter filter = new AddressFilter();
      filter.setStreetName(query);  
      filter.setMaxResults(10);
      List<AddressView> addresses = 
        KernelConfigBean.getPort().findAddressViews(filter);
      if (addresses != null)
      {       
        for (AddressView address : addresses)
        {
          String description = addressObjectBean.getDescription(address);
          SelectItem item = new SelectItem(address.getAddressId(), description);
          items.add(item);
        }
      }
    }
    else
    {
      //Add favorites TODO
//      items.addAll(addressObjectBean.getFavorites()); 
    }
    
    return items;
  }  

  public void setSelectedAddress(String addressId)
  {
    editing.setAddressId(addressId);
    if (addressSelectItem == null || 
      !addressId.equals(addressSelectItem.getValue()))
    {
      AddressObjectBean addressObjectBean = 
        WebUtils.getBacking(ADDRESS_OBJECT_BEAN);
      
      String description = addressObjectBean.getDescription(addressId);
      addressSelectItem = new SelectItem(addressId, description);       
    }    
    showDialog();
  }
  
  public String editAddress(PersonAddressView row)
  {
    String personAddressId = null;
    if (row != null)
      personAddressId = row.getPersonAddressId();

    return editAddress(personAddressId);
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
    addressSelectItem = null;
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
    return new Object[]{ editing, addressSelectItem };
  }

  @Override
  public void restoreState(Serializable state)
  {
    try
    {
      Object[] stateArray = (Object[])state;
      editing = (PersonAddress)stateArray[0];
      addressSelectItem = (SelectItem)stateArray[1];
      
      resultListHelper.find();
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
    
  private String editAddress(String personAddressId)
  {
    try
    {
      if (personAddressId != null && !isEditing(personAddressId))
      {
        editing = KernelConfigBean.getPort().loadPersonAddress(personAddressId);
        loadAddressSelectItem();     
      }
      else if (personAddressId == null)
      {
        editing = new PersonAddress();
      }
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
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
        KernelManagerPort port = KernelConfigBean.getPort();
        port.storePersonAddress(editing);
        editing = null;
        addressSelectItem = null;
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
      
      KernelManagerPort port = KernelConfigBean.getPort();
      port.removePersonAddress(rowPersonAddressId);
      
      info("REMOVE_OBJECT");      
      return null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }    

  private void loadAddressSelectItem()
  {
    if (editing != null)
    {
      AddressObjectBean addressObjectBean = 
        WebUtils.getBacking(ADDRESS_OBJECT_BEAN);
      
      if (editing.getAddressId() != null)
      {
        String description = 
          addressObjectBean.getDescription(editing.getAddressId());
        addressSelectItem = 
          new SelectItem(editing.getAddressId(), description);
      }
    }
  }  
  
  private void showDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('editDataDialog').show();");    
  }  

  private void hideDialog()
  {
    PrimeFaces current = PrimeFaces.current();
    current.executeScript("PF('editDataDialog').hide();");    
  }  
  
  private boolean isEditing(String pageObjectId)
  {
    if (editing == null)
      return false;
    
    String personAddressId = editing.getPersonAddressId();    
    return personAddressId != null 
      && personAddressId.equals(pageObjectId);
  }  

  private class PersonAddressResultListHelper extends 
    ResultListHelper<PersonAddressView>
  {
    @Override
    public List<PersonAddressView> getResults(int maxResults)
    {
      try
      {
        PersonAddressFilter filter = new PersonAddressFilter();
        filter.setPersonId(personObjectBean.getObjectId());
        filter.setFirstResult(firstRow);
        filter.setMaxResults(maxResults);
        return KernelConfigBean.getPort().findPersonAddressViews(filter);
      }
      catch (Exception ex)
      {
        error(ex);
      }
      return null;
    }
  }

}