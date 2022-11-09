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
package org.matrix.pf.kernel;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TabPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.kernel.web.KernelConfigBean;

/**
 *
 * @author lopezrj-sf
 */
@Named
public class PersonAddressesBacking extends PageBacking 
  implements TabPage, ResultListPage
{  
  private static final String PERSON_BACKING = "personBacking";
  private static final String ADDRESS_BACKING = "addressBacking";
  private static final String OUTCOME = "pf_person_addresses";   
  
  private PersonBacking personBacking;
  
  //Helpers
  private ResultListHelper<PersonAddressView> resultListHelper;
  private TabHelper tabHelper;
  
  private PersonAddress editing;
  
  private SelectItem addressSelectItem;
  
  public PersonAddressesBacking()
  {
    //Let to super class constructor.   
  }
  
  @PostConstruct
  public void init()
  {
    personBacking = WebUtils.getBacking(PERSON_BACKING);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
    populate();
  }

  public PersonAddress getEditing()
  {
    return editing;
  }

  public void setEditing(PersonAddress editing)
  {
    this.editing = editing;
  } 

  public boolean isNew()
  {
    return isNew(editing);
  }
  
  @Override
  public String getPageObjectId()
  {
    if (editing != null)
      return editing.getPersonAddressId();
    else
      return null;
  }
  
  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
      return getDescription(addressBacking, editing.getAddressId());
    }
    return null;
  }

  @Override
  public PersonBacking getObjectBacking()
  {
    return personBacking;
  }

  @Override
  public ResultListHelper<PersonAddressView> getResultListHelper()
  {
    return resultListHelper;
  }  
  
  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }
  
  public List<PersonAddressView> getRows()
  {
    return resultListHelper.getRows();
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
  
  public List<SelectItem> getFavorites()
  {
    AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
    return addressBacking.getFavorites();     
  }  
  
  private List<SelectItem> completeAddress(String query, String addressId)
  {
    ArrayList<SelectItem> items = new ArrayList();
    AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
    
    //Add current item
    if (!isNew(editing))
    {
      String description = "";
      if (addressId != null)
        description = getDescription(addressBacking, addressId);
      items.add(new SelectItem(addressId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      AddressFilter filter = new AddressFilter();
      filter.setStreetName("\"" + query + "\"");
      filter.setMaxResults(10);
      List<AddressView> addresses = 
        KernelConfigBean.getPort().findAddressViews(filter);
      if (addresses != null)
      {       
        for (AddressView address : addresses)
        {
          String description = addressBacking.getDescription(address);
          SelectItem item = new SelectItem(address.getAddressId(), description);
          items.add(item);
        }
      }
    }
    else
    {
      //Add favorites
      items.addAll(addressBacking.getFavorites()); 
    }
    
    return items;
  }  
    
  public String getContactIcon(ContactView contact)
  {
    return KernelUtils.getContactTypeIconName(contact.getContactTypeId());
  }

  public void setSelectedAddress(String addressId)
  {
    editing.setAddressId(addressId);
    if (addressSelectItem == null || 
      !addressId.equals(addressSelectItem.getValue()))
    {
      AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);    
      String description = getDescription(addressBacking, addressId);
      addressSelectItem = new SelectItem(addressId, description);       
    }    
    showDialog();
  }
  
  @Override
  public String show(String pageObjectId)
  {
    editAddress(pageObjectId);
    showDialog();
    return isEditing(pageObjectId) ? OUTCOME : show();
  }  
  
  @Override
  public String show()
  {    
    populate();
    return OUTCOME;
  }
  
  public String editAddress(PersonAddressView row)
  {
    String personAddressId = null;
    if (row != null)
      personAddressId = row.getPersonAddressId();

    return editAddress(personAddressId);
  } 
  
  public String createAddress()
  {
    editing = new PersonAddress();
    return null;
  }  
  
  public String removeAddress(PersonAddressView row)
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
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }  

  public String storeAddress()
  {
    try
    {
      if (editing == null)
        return null;
      
      //Address must be selected
      if (editing.getAddressId() == null || editing.getAddressId().isEmpty())
      {
        throw new Exception("ADDRESS_MUST_BE_SELECTED"); 
      }
                            
      String personId = personBacking.getObjectId();
      editing.setPersonId(personId);
      KernelManagerPort port = KernelConfigBean.getPort();
      port.storePersonAddress(editing);
      editing = null;
      addressSelectItem = null;
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
  public List<PersonAddressView> getResults(int firstResult, int maxResults)
  {
    try
    {
      PersonAddressFilter filter = new PersonAddressFilter();
      filter.setPersonId(personBacking.getObjectId());        
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return KernelConfigBean.getPort().findPersonAddressViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public String store()
  {
    return storeAddress();
  }
  
  @Override
  public void load()
  {
    resultListHelper.search();
  }

  @Override
  public void create()
  {
    editing = new PersonAddress();
  }
  
  @Override
  public String cancel()
  {
    editing = null;
    addressSelectItem = null;
    info("CANCEL_OBJECT");          
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
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

  private void loadAddressSelectItem()
  {
    if (editing != null)
    {
      AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
      
      if (editing.getAddressId() != null)
      {
        String description = 
          getDescription(addressBacking, editing.getAddressId());
        addressSelectItem = 
          new SelectItem(editing.getAddressId(), description);
      }
    }
  }  

}
