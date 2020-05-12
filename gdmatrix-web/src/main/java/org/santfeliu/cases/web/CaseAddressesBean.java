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
package org.santfeliu.cases.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;
import org.matrix.cases.Case;

import org.matrix.cases.CaseAddress;
import org.matrix.cases.CaseAddressFilter;
import org.matrix.cases.CaseAddressView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonFilter;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

import org.santfeliu.kernel.web.AddressBean;
import org.santfeliu.kernel.web.KernelConfigBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.TypifiedPageBean;


/**
 *
 * @author unknown
 */
public class CaseAddressesBean extends TypifiedPageBean
{
  public static final String ROOT_TYPE_ID_PROPERTY = "_addressRootTypeId";
  public static final String ORDERBY_PROPERTY = "_addressesOrderBy";
  
  private CaseAddress editingAddress;
  private List<CaseAddressView> rows;
  private boolean importPersons = false;

  public CaseAddressesBean()
  {
    super(DictionaryConstants.CASE_ADDRESS_TYPE, "CASE_ADMIN");

    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();
    Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
    if (caseType != null)
    {
      PropertyDefinition pd =
        caseType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        setRootTypeId(pd.getValue().get((0)));
    }

    PropertyDefinition opd =
      caseType.getPropertyDefinition(ORDERBY_PROPERTY);
    if (opd != null && opd.getValue() != null && opd.getValue().size() > 0)
    {
      String orderByString = opd.getValue().get(0);
      String[] array = orderByString.split(",");
      if (array != null)
        orderBy = Arrays.asList(array);
    }    
    load();
  }
  
  public void setEditingAddress(CaseAddress editingAddress)
  {
    this.editingAddress = editingAddress;
  }

  public CaseAddress getEditingAddress()
  {
    return editingAddress;
  }

  public void setRows(List<CaseAddressView> rows)
  {
    this.rows = rows;
  }

  public List<CaseAddressView> getRows()
  {
    return rows;
  }

  public String show()
  {
    return "case_addresses";
  }

  public String store()
  {
    if (editingAddress != null)
    {
      storeAddress();
    }
    else
    {
      load();
    }
    return show();
  }
  
  public String showAddress()
  {
    return getControllerBean().showObject("Address",
     (String)getValue("#{row.addressView.addressId}"));
  }
  
  public String searchAddress()
  {
    return getControllerBean().searchObject("Address",
      "#{caseAddressesBean.editingAddress.addressId}");
  }
  
  public String removeAddress()
  {
    try
    {
      CaseAddressView row = (CaseAddressView)getRequestMap().get("row");
      preRemove();      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCaseAddress(row.getCaseAddressId());
      postRemove();
      load();
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
      preStore();
      String caseId = getObjectId();
      editingAddress.setCaseId(caseId);
      
      CaseManagerPort port = CaseConfigBean.getPort();
      port.storeCaseAddress(editingAddress);
      if (importPersons)
      {
        importPersonsFromEditingAddress();
        importPersons = false;
      }
      postStore();      
      editingAddress = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String createAddress()
  {
    editingAddress = new CaseAddress();
    return null;
  }
  
  public String editAddress()
  {
    try
    {
      CaseAddressView row = (CaseAddressView)getExternalContext().
        getRequestMap().get("row");   
      String caseAddressId = row.getCaseAddressId();
      if (caseAddressId != null)
        editingAddress = 
          CaseConfigBean.getPort().loadCaseAddress(caseAddressId);
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelAddress()
  {
    editingAddress = null;
    return null;
  }
  
  @Override
  public boolean isModified()
  {
    return editingAddress != null;
  }

  public void importPersonsFromEditingAddress()
  {
    if (editingAddress != null)
    {
      String addressId = editingAddress.getAddressId();
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
              casePerson.setCaseId(getObjectId());
              casePerson.setPersonId(personId);
              CaseConfigBean.getPort().storeCasePerson(casePerson);
            }
          }
          getControllerBean().clearBean("casePersonsBean");
        }
        catch(Exception ex)
        {
          error(ex);
        }
      }
    }
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
  
  private List<PersonAddressView> getPersonAddressViewList(String addressId)
    throws Exception
  {
    PersonAddressFilter filter = new PersonAddressFilter();
    filter.setAddressId(addressId);
    return KernelConfigBean.getPort().findPersonAddressViews(filter);
  }
  
  private List<String> getCurrentCasePersonsList()
    throws Exception
  {
    CasePersonFilter casePersonFilter = new CasePersonFilter();
    casePersonFilter.setCaseId(getObjectId());
    List<CasePersonView> casePersonViewList = 
      CaseConfigBean.getPort().findCasePersonViews(casePersonFilter);
    List<String> personIdList = new ArrayList();
    for(CasePersonView casePersonView : casePersonViewList)
    {
      personIdList.add(casePersonView.getPersonView().getPersonId());
    }
    
    return personIdList;
  }
  
  public List<SelectItem> getAddressSelectItems()
  {
    AddressBean addressBean = (AddressBean)getBean("addressBean");
    return addressBean.getSelectItems(editingAddress.getAddressId());
  }
  
  private void load()
  {
    try
    {
      if (!isNew())
      {
        CaseAddressFilter filter = new CaseAddressFilter();
        filter.setCaseId(getObjectId());
        rows = 
          CaseConfigBean.getPort().findCaseAddressViews(filter);        
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void setImportPersons(boolean importPersons)
  {
    this.importPersons = importPersons;
  }

  public boolean isImportPersons()
  {
    return importPersons;
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  public String getRowTypeDescription()
  {
    CaseAddressView caseAddressView = (CaseAddressView)getValue("#{row}");
    if (caseAddressView != null && caseAddressView.getCaseAddressId() != null)
    {
      String typeId = caseAddressView.getCaseAddressTypeId();
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
      }  
    }
    return "";
  }
  
  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
  
  @Override
  protected String getRowTypeId(Object row)
  {
    CaseAddressView caseAddressView = (CaseAddressView)row;
    return caseAddressView.getCaseAddressTypeId();
  }  
}
