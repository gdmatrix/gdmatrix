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
package org.matrix.pf.cases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.matrix.pf.kernel.AddressBacking;
import org.matrix.pf.kernel.KernelUtils;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.ResultListHelper;
import org.matrix.pf.web.helper.ResultListPage;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.primefaces.event.SelectEvent;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
@Named
public class CaseAddressesBacking extends PageBacking 
  implements TypedTabPage, ResultListPage
{
  private static final String GROUPBY_PROPERTY = "groupBy";
  
  private static final String CASE_BACKING = "caseBacking";
  private static final String ADDRESS_BACKING = "addressBacking";
    
  private static final String OUTCOME = "pf_case_addresses";  
  
  private CaseBacking caseBacking;
  
  //Helpers
  private TypedHelper typedHelper;
  private ResultListHelper<CaseAddressView> resultListHelper;
  private TabHelper tabHelper;
  
  private CaseAddress editing;
  
  private SelectItem addressSelectItem;

  private boolean importPersons = false;

  
  public CaseAddressesBacking()
  {
  }
  
  @PostConstruct
  public void init()
  {
    caseBacking = WebUtils.getBacking(CASE_BACKING);   
    typedHelper = new TypedHelper(this);
    resultListHelper = new ResultListHelper(this);
    tabHelper = new TabHelper(this);
    populate();
  }
  
  public CaseAddress getEditing()
  {
    return editing;
  }

  public void setEditing(CaseAddress editing)
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
      return editing.getCaseAddressId();
    else
      return null;
  }


  public String getPageObjectDescription()
  {
    if (editing != null)
    {
      AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
      return addressBacking.getDescription(editing.getAddressId());
    }
    return null;
  }
  
  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.CASE_ADDRESS_TYPE;
  }

  @Override
  public CaseBacking getObjectBacking()
  {
    return caseBacking;
  }
  
  @Override
  public String getTypeId()
  {
    return caseBacking.getTabTypeId();
  }
  
  @Override
  public ResultListHelper<CaseAddressView> getResultListHelper()
  {
    return resultListHelper;
  }  
  
  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }  

  public TabHelper getTabHelper()
  {
    return tabHelper;
  }
  
  public List<CaseAddressView> getRows()
  {
    return resultListHelper.getRows();
  }
  
  public String getViewStartDate()
  {
    String date = "";
    CaseAddressView row = (CaseAddressView)getValue("#{row}");
    if (row != null)
    {
      date = row.getStartDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }

  public String getViewEndDate()
  {
    String date = "";
    CaseAddressView row = (CaseAddressView)getValue("#{row}");
    if (row != null)
    {
      date = row.getEndDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }   
  
  public String getCaseAddressTypeId()
  {
    String typeId = null;
    CaseAddressView row = (CaseAddressView)getValue("#{row}");
    if (row != null)
    {
      typeId = row.getCaseAddressTypeId();
      if (typeId != null)
      {
        Type type = TypeCache.getInstance().getType(typeId);
        return type.getDescription();
      }
    }
    return typeId;    
  }
  
  public Date getStartDate()
  {
    if (editing != null && editing.getStartDate() != null)
      return TextUtils.parseInternalDate(editing.getStartDate());
    else
      return null;
  }
  
  public Date getEndDate()
  {
    if (editing != null && editing.getEndDate() != null)
      return TextUtils.parseInternalDate(editing.getEndDate());
    else
      return null;
  }  
    
  public void setStartDate(Date date)
  {
    if (date != null && editing != null)
      editing.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
  }
  
  public void setEndDate(Date date)
  {
    if (date != null && editing != null)
      editing.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
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
  
  public List<SelectItem> completeAddress(String query)
  {
    return completeAddress(query, editing.getAddressId());
  }  

  public List<SelectItem> getFavorites()
  {
    AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);
    return addressBacking.getFavorites();     
  }      
    
  public boolean isRenderGroupedResults()
  {
    return getProperty(GROUPBY_PROPERTY) != null;
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
        description = addressBacking.getDescription(addressId);
      items.add(new SelectItem(addressId, description));
    }
        
    //Query search
    if (query != null && query.length() >= 3)
    {
      AddressFilter filter = new AddressFilter();
      filter.setDescription(query);
      filter.setMaxResults(10);
      List<AddressView> addresss = 
        KernelConfigBean.getPort().findAddressViews(filter);
      if (addresss != null)
      {       
        for (AddressView address : addresss)
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

  public boolean isImportPersons()
  {
    return importPersons;
  }

  public void setImportPersons(boolean importPersons)
  {
    this.importPersons = importPersons;
  }
  
  public void setSelectedAddress(String addressId)
  {
    editing.setAddressId(addressId);
    if (addressSelectItem == null || 
      !addressId.equals(addressSelectItem.getValue()))
    {
      AddressBacking addressBacking = WebUtils.getBacking(ADDRESS_BACKING);    
      String description = addressBacking.getDescription(addressId);
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
  
  public String editAddress(CaseAddressView row)
  {
    String caseAddressId = null;
    if (row != null)
      caseAddressId = row.getCaseAddressId();

    return editAddress(caseAddressId);
  } 
  
  public String createAddress()
  {
    editing = new CaseAddress();
    return null;
  }  
  
  public String removeAddress(CaseAddressView row)
  {
    try
    {
      if (row == null)
        throw new Exception("ADDRESS_MUST_BE_SELECTED");
      
      String rowCaseAddressId = row.getCaseAddressId();
      
      if (editing != null && rowCaseAddressId.equals(editing.getCaseAddressId()))
        editing = null;
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCaseAddress(rowCaseAddressId);
      
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
        throw new Exception("ADDRESS_MUST_BE_SELECTED"); 
                            
      String caseId = caseBacking.getObjectId();
      editing.setCaseId(caseId);
      
      if (editing.getCaseAddressTypeId() == null)
        editing.setCaseAddressTypeId(typedHelper.getTypeId());
                  
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeCaseAddress(editing);
      
      if (importPersons)
      {
        importPersonsFromEditingAddress();
        importPersons = false;
      }      

      cancel();
      return show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
    

  @Override
  public List<CaseAddressView> getResults(int firstResult, int maxResults)
  {
    try
    {
      CaseAddressFilter filter = new CaseAddressFilter();
      filter.setCaseId(caseBacking.getObjectId());        
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return CaseConfigBean.getPort().findCaseAddressViews(filter);
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
    editing = new CaseAddress();
  }
  
  @Override
  public String cancel()
  {
    editing = null;
    addressSelectItem = null;
    importPersons = false;
    return null;
  }  
  
  @Override
  public void reset()
  {
    cancel();
    resultListHelper.reset();
  }
    
  private boolean isNew(CaseAddress caseAddress)
  {
    return (caseAddress != null && caseAddress.getCaseAddressId() == null);
  }  
    
  private String editAddress(String caseAddressId)
  {
    try
    {
      if (caseAddressId != null && !isEditing(caseAddressId))
      {
        editing = CaseConfigBean.getPort().loadCaseAddress(caseAddressId);
        loadAddressSelectItem();     
      }
      else if (caseAddressId == null)
      {
        editing = new CaseAddress();
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
          addressBacking.getDescription(editing.getAddressId());
        addressSelectItem = 
          new SelectItem(editing.getAddressId(), description);
      }
    }
  }  
  
  private void importPersonsFromEditingAddress()
  {
    if (editing != null)
    {
      String addressId = editing.getAddressId();
      if (addressId != null)
      {
        try
        {
          List<String> personIdList = getCurrentCasePersonsList();
          List<PersonAddressView> personAddressViewList = 
            getPersonAddressViewList(addressId);
          for (PersonAddressView personAddressView : personAddressViewList)
          {
            String personId = personAddressView.getPerson().getPersonId();
            if (!personIdList.contains(personId))
            {
              CasePerson casePerson = new CasePerson();
              casePerson.setCaseId(caseBacking.getObjectId());
              casePerson.setPersonId(personId);
              CaseConfigBean.getPort().storeCasePerson(casePerson);
            }
          }
          CasePersonsBacking casePersonsBacking = 
            WebUtils.getBacking("casePersonsBacking");
          casePersonsBacking.reset();
        }
        catch(Exception ex)
        {
          error(ex);
        }
      }
    }   
  }  
  
    
  private List<String> getCurrentCasePersonsList()
    throws Exception
  {
    CasePersonFilter casePersonFilter = new CasePersonFilter();
    casePersonFilter.setCaseId(caseBacking.getObjectId());
    List<CasePersonView> casePersonViewList = 
      CaseConfigBean.getPort().findCasePersonViews(casePersonFilter);
    List<String> personIdList = new ArrayList();
    for(CasePersonView casePersonView : casePersonViewList)
    {
      personIdList.add(casePersonView.getPersonView().getPersonId());
    }
    
    return personIdList;
  }   
  
  private List<PersonAddressView> getPersonAddressViewList(String personId)
    throws Exception
  {
    PersonAddressFilter filter = new PersonAddressFilter();
    filter.setPersonId(personId);
    return KernelConfigBean.getPortAsAdmin().findPersonAddressViews(filter);
  }  

}
