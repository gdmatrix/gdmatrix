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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressFilter;
import org.santfeliu.webapp.TypeBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.ObjectSetup;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class AddressTypeBean extends TypeBean<Address, AddressFilter>
{
  private static final String DEFAULT_CITY_NAME = "defaultCityName";
  private static final String BUNDLE_PREFIX = "$$kernelBundle.";  

  @Inject
  StreetTypeBean streetTypeBean;

  private List<SelectItem> streetTypeSelectItems;

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.ADDRESS_TYPE;
  }

  @Override
  public String getObjectId(Address address)
  {
    return address.getAddressId();
  }

  @Override
  public String describe(Address address)
  {
    String city = "";
    String street = streetTypeBean.getDescription(address.getStreetId());

    int index = street.indexOf("(");
    if (index != -1)
    {
      city = street.substring(index);
      street = street.substring(0, index).trim();
    }

    return street
      + " " + (address.getNumber1() != null ? address.getNumber1() : "")
      + " " + (address.getNumber2() != null ? address.getNumber2() : "")
      + " " + (address.getFloor() != null ? address.getFloor() : "")
      + " " + (address.getDoor() != null ? address.getDoor() : "")
      + " " + city;
  }

  @Override
  public Address loadObject(String objectId)
  {
    try
    {
      return KernelModuleBean.getPort(false).loadAddress(objectId);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  @Override
  public String getTypeId(Address address)
  {
    return address.getAddressTypeId();
  }

  @Override
  public ObjectSetup createObjectSetup()
  {
    ObjectSetup objectSetup = new ObjectSetup();
    objectSetup.setViewId("/pages/kernel/address.xhtml");

    List<EditTab> editTabs = new ArrayList<>();
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_main", 
      "/pages/kernel/address_main.xhtml"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_persons", 
      "/pages/kernel/address_persons.xhtml", "addressPersonsTabBean"));
    editTabs.add(new EditTab(BUNDLE_PREFIX + "tab_cases", 
      "/pages/kernel/address_cases.xhtml", "addressCasesTabBean"));    
    objectSetup.setEditTabs(editTabs);
    
    return objectSetup;
  }

  @Override
  public AddressFilter queryToFilter(String query, String typeId)
  {
    AddressFilter filter = new AddressFilter();
    if (!StringUtils.isBlank(query))
    {
      if (query.matches("\\d+"))
        filter.getAddressIdList().add(query); 
      else
      {
        String[] parts = query.split("\\s");
        if (parts != null && parts.length > 1)    
        {
          String streetTypeId = (String)
            streetTypeBean.getStreetTypeSelectItems().stream()
            .filter(i -> i.getLabel().equalsIgnoreCase(parts[0]))
            .map(SelectItem::getValue)
            .findFirst()
            .orElse(null);

          if (streetTypeId != null)
          {
            filter.setStreetTypeId(streetTypeId);
            query = query.substring(parts[0].length() + 1);
          } 
        }
        
        int index = query.indexOf(",");
        if (index > 0)
        {
          String streetNumber = query.substring(index + 1).trim();
          filter.setNumber(streetNumber);          
          query = query.substring(0, index).trim();
        }  
        filter.setStreetName(query);
      } 
    }
      
    return filter;
  }

  @Override
  public String filterToQuery(AddressFilter filter)
  {
    String value;
    if (!filter.getAddressIdList().isEmpty())
      value = filter.getAddressIdList().get(0);
    else
    {
      StringBuilder sbValue = new StringBuilder();
      if (!StringUtils.isBlank(filter.getStreetTypeId()))
        sbValue.append(filter.getStreetTypeId());
      if (!StringUtils.isBlank(filter.getStreetName()))
        sbValue.append(" ").append(filter.getStreetName());
      if (!StringUtils.isBlank(filter.getNumber()))
        sbValue.append(", ").append(filter.getNumber());
      value = sbValue.toString().trim();
    }
    return value;
  }

  @Override
  public List<Address> find(AddressFilter filter)
  {
    try
    {
      filter.setStreetName(setWildcards(filter.getStreetName()));
      return KernelModuleBean.getPort(true).findAddresses(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }
  
  private String setWildcards(String text)
  {
    if (text != null && !text.startsWith("\"") && !text.endsWith("\""))
      text = "%" + text.replaceAll("^%|%$", "") + "%" ;
    else if (text != null && text.startsWith("\"") && text.endsWith("\""))
      text = text.replaceAll("^\"|\"$", "");
    return text;
  } 
    
}
