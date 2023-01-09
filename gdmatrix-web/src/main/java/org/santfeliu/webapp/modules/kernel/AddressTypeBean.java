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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.KernelList;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.webapp.TypeBean;

/**
 *
 * @author blanquepa
 */
@Named
@ApplicationScoped
public class AddressTypeBean extends TypeBean<Address, AddressFilter>
{
  private static final String DEFAULT_CITY_NAME = "defaultCityName";

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
  public AddressFilter queryToFilter(String query, String typeId)
  {
    AddressFilter filter = new AddressFilter();
    if (!StringUtils.isBlank(query))
    {
      filter.setStreetName(query);
    }

    if (StringUtils.isBlank(typeId))
    {
      filter.setAddressTypeId(typeId);
    }

    // TODO: Check this routine
//    if (query != null)
//    {
//      if (query.matches("\\d+"))
//        filter.getAddressIdList().add(query);
//      else
//      {
//        String[] parts = query.split("\\s");
//        if (parts != null && parts.length > 1)
//        {
//          //TODO: search in street types.
//          List<SelectItem> streetTypes = getStreetTypeSelectItems().stream()
//            .filter(item -> item.getLabel().equalsIgnoreCase(parts[0]))
//            .collect(Collectors.toList());
//
//          if (streetTypes != null && !streetTypes.isEmpty())
//          {
//            filter.setStreetTypeId((String) streetTypes.get(0).getValue());
//            query = query.substring(parts[0].length() + 1);
//          }
//        }
//
//        Pattern pattern =
//          Pattern.compile("([[a-zA-Z]|\\s|ç|']+)\\W*(\\d*)\\W*\\w*");
//        Matcher matcher = pattern.matcher(query);
//        if (matcher.find())
//        {
//          String streetName = matcher.group(1);
//          if (streetName != null)
//            filter.setStreetName(streetName.trim());
//
//          String streetNumber = matcher.group(2);
//          if (streetNumber != null)
//            filter.setNumber(streetNumber.trim());
//        }
//
//        String defaultCityName = getProperty(DEFAULT_CITY_NAME);
//        if (defaultCityName != null)
//          filter.setCityName(defaultCityName);
//      }
//    }

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
      return KernelModuleBean.getPort(true).findAddresses(filter);
    }
    catch (Exception ex)
    {
      return Collections.EMPTY_LIST;
    }
  }

  private List<SelectItem> getStreetTypeSelectItems()
  {
    if (streetTypeSelectItems == null)
    {
      try
      {
        streetTypeSelectItems = FacesUtils.getListSelectItems(
          KernelModuleBean.getPort(false).listKernelListItems(
            KernelList.STREET_TYPE), "itemId", "description", true);
      }
      catch (Exception ex)
      {
        streetTypeSelectItems = Collections.EMPTY_LIST;
      }
    }
    return streetTypeSelectItems;
  }


}
