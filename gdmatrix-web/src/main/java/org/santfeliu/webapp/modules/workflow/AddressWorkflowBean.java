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
package org.santfeliu.webapp.modules.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.Country;
import org.matrix.kernel.CountryFilter;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.Properties;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.form.AddressForm;
import org.santfeliu.workflow.form.Form;

/**
 *
 * @author realor
 */
@Named
@RequestScoped
public class AddressWorkflowBean extends WorkflowBean
{
  private static final String NONE = "none";
  private static final String NEW = "new";
  private static final int DEFAULT_MAX_RESULTS = 10;

  private String message;
  private String prefix;
  private boolean addressRequired = true;
  private boolean creationAllowed = false;
  private String creationAddressTypeId;
  private String forcedCity;
  private int maxResults = DEFAULT_MAX_RESULTS;

  private String addressId;
  private String countryId;
  private String province;
  private String city;
  private String streetType;
  private String street;
  private String number;
  private boolean bis;
  private boolean noNumber;
  private Double km;
  private String block;
  private String entranceHall;
  private String stair;
  private String floor;
  private String door;
  private String postalCode;
  private String postOfficeBox;

  private List<SelectItem> addressSelectItems;
  private boolean rowParity;

  @Inject
  WorkflowInstanceBean instanceBean;

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();
    Object value;
    value = parameters.get("prefix");
    if (value != null) prefix = String.valueOf(value);
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("addressRequired");
    if (value != null) addressRequired = Boolean.parseBoolean((String)value);
    value = parameters.get("forcedCity");
    if (value != null) forcedCity = String.valueOf(value);
    value = parameters.get("maxResults");
    if (value != null) maxResults = Integer.valueOf((String)value);

//    value = parameters.get("creationAddressTypeId");
//    if (value != null) creationAddressTypeId = String.valueOf(value);
//    value = parameters.get("creationAllowed");
//    if (value != null) creationAllowed = Boolean.parseBoolean((String)value);

    loadPreviousVariables();

    instanceBean.setForwardEnabled(false);

    return "/pages/workflow/address_form.xhtml";
  }

  @Override
  public Map submit()
  {
    HashMap variables = new HashMap();
    String varPrefix = "";

    if (prefix != null)
      varPrefix = this.prefix + "_";

    if (NONE.equals(addressId))
      addressId = null;

//    if (NEW.equals(addressId) && creationAddressTypeId != null)
//    {
//      Address address = new Address();
//    }

    variables.put(varPrefix + AddressForm.ADDRESS_ID, addressId);
    variables.put(varPrefix + AddressForm.COUNTRY_ID, countryId);
    variables.put(varPrefix + AddressForm.PROVINCE, province);
    variables.put(varPrefix + AddressForm.CITY, city);
    variables.put(varPrefix + AddressForm.STREET_TYPE, streetType);
    variables.put(varPrefix + AddressForm.NUMBER, number);
    variables.put(varPrefix + AddressForm.STREET, street);
    variables.put(varPrefix + AddressForm.BIS, bis);
    variables.put(varPrefix + AddressForm.NO_NUMBER, noNumber);
    variables.put(varPrefix + AddressForm.KM, km);
    variables.put(varPrefix + AddressForm.BLOCK, block);
    variables.put(varPrefix + AddressForm.ENTRANCE_HALL, entranceHall);
    variables.put(varPrefix + AddressForm.STAIR, stair);
    variables.put(varPrefix + AddressForm.FLOOR, floor);
    variables.put(varPrefix + AddressForm.DOOR, door);
    variables.put(varPrefix + AddressForm.POSTAL_CODE, postalCode);
    variables.put(varPrefix + AddressForm.POST_OFFICE_BOX, postOfficeBox);

    return variables;
  }

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getPrefix()
  {
    return prefix;
  }

  public void setPrefix(String prefix)
  {
    this.prefix = prefix;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getCountryId()
  {
    return countryId;
  }

  public void setCountryId(String countryId)
  {
    this.countryId = countryId;
  }

  public String getProvince()
  {
    return province;
  }

  public void setProvince(String province)
  {
    this.province = province;
  }

  public String getCity()
  {
    return city;
  }

  public void setCity(String city)
  {
    this.city = city;
  }

  public String getStreetType()
  {
    return streetType;
  }

  public void setStreetType(String streetType)
  {
    this.streetType = streetType;
  }

  public String getStreet()
  {
    return street;
  }

  public void setStreet(String street)
  {
    this.street = street;
  }

  public String getNumber()
  {
    return number;
  }

  public void setNumber(String number)
  {
    this.number = number;
  }

  public boolean isBis()
  {
    return bis;
  }

  public void setBis(boolean bis)
  {
    this.bis = bis;
  }

  public boolean isNoNumber()
  {
    return noNumber;
  }

  public void setNoNumber(boolean noNumber)
  {
    this.noNumber = noNumber;
  }

  public Double getKm()
  {
    return km;
  }

  public void setKm(Double km)
  {
    this.km = km;
  }

  public String getBlock()
  {
    return block;
  }

  public void setBlock(String block)
  {
    this.block = block;
  }

  public String getEntranceHall()
  {
    return entranceHall;
  }

  public void setEntranceHall(String entranceHall)
  {
    this.entranceHall = entranceHall;
  }

  public String getStair()
  {
    return stair;
  }

  public void setStair(String stair)
  {
    this.stair = stair;
  }

  public String getFloor()
  {
    return floor;
  }

  public void setFloor(String floor)
  {
    this.floor = floor;
  }

  public String getDoor()
  {
    return door;
  }

  public void setDoor(String door)
  {
    this.door = door;
  }

  public String getPostalCode()
  {
    return postalCode;
  }

  public void setPostalCode(String postalCode)
  {
    this.postalCode = postalCode;
  }

  public String getPostOfficeBox()
  {
    return postOfficeBox;
  }

  public void setPostOfficeBox(String postOfficeBox)
  {
    this.postOfficeBox = postOfficeBox;
  }

  public List<SelectItem> getAddressSelectItems()
  {
    List<SelectItem> result = new ArrayList();
    if (addressSelectItems != null)
      result.addAll(addressSelectItems);

    if (!isAddressRequired() && addressSelectItems != null)
    {
      SelectItem item = new SelectItem();
      item.setLabel("Continuar sense seleccionar cap adreça");
      item.setValue(NONE);
      result.add(item);
    }

    if (isCreationAllowed() && addressSelectItems != null)
    {
      SelectItem item = new SelectItem();
      item.setLabel("Crear l'adreça");
      item.setValue(NEW);
      result.add(item);
    }

    return result;
  }

  public void setAddressSelectItems(List<SelectItem> addressSelectItems)
  {
    this.addressSelectItems = addressSelectItems;
  }

  public String getSelectItemClass()
  {
    rowParity = !rowParity;
    return rowParity ? "row1" : "row2";
  }

  public List<SelectItem> getCountrySelectItems()
  {
    ArrayList selectItems = new ArrayList<SelectItem>();
    try
    {
      KernelManagerPort port = getKernelManagerPort();
      CountryFilter filter = new CountryFilter();
      List<Country> countries = port.findCountries(filter);
      for (Country country : countries)
      {
        SelectItem selectItem = new SelectItem();
        selectItem.setLabel(country.getName());
        selectItem.setValue(country.getCountryId());
        selectItems.add(selectItem);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return selectItems;
  }

  public String searchAddress()
  {
    KernelManagerPort port = getKernelManagerPort();
    AddressFilter filter = new AddressFilter();
    filter.setCityName(city);
    filter.setStreetName(street);
    filter.setNumber(number);
    filter.setFloor(floor);
    filter.setDoor(door);
    List<AddressView> addresses = port.findAddressViews(filter);
    // refineList
    refineList(addresses);

    // createSelectItems
    addressSelectItems = new ArrayList<SelectItem>();
    boolean addressIdInList = false;
    for (AddressView address : addresses)
    {
      SelectItem item = new SelectItem();
      item.setValue(address.getAddressId());
      item.setLabel(address.getDescription() + ", " +
        address.getCity() + " (" + address.getCountry() + ")");
      addressSelectItems.add(item);
      if (address.getAddressId().equals(addressId)) addressIdInList = true;
    }

    if (addressSelectItems.size() > maxResults)
      addressSelectItems = new ArrayList(addressSelectItems.subList(0, maxResults));

    if (!addressIdInList && addressSelectItems.size() > 0)
    {
      addressId = (String)addressSelectItems.get(0).getValue();
    }
    instanceBean.setForwardEnabled(true);
    return null;
  }

  private void loadPreviousVariables()
  {
    Map variables = instanceBean.getVariables();

    String varPrefix = "";
    if (prefix != null)
      varPrefix = this.prefix + "_";

    addressId = (String)variables.get(varPrefix + AddressForm.ADDRESS_ID);
    countryId = (String)variables.get(varPrefix + AddressForm.COUNTRY_ID);
    province = (String)variables.get(varPrefix + AddressForm.PROVINCE);
    city = (String)variables.get(varPrefix + AddressForm.CITY);
    if (city == null && forcedCity != null)
      city = forcedCity;
    streetType = (String)variables.get(varPrefix + AddressForm.STREET_TYPE);
    street = (String)variables.get(varPrefix + AddressForm.STREET);
    number = (String)variables.get(varPrefix + AddressForm.NUMBER);
    bis = Boolean.valueOf(String.valueOf(variables.get(varPrefix + AddressForm.BIS)));
    noNumber = Boolean.valueOf(String.valueOf(variables.get(varPrefix + AddressForm.NO_NUMBER)));
    km = toDouble(variables.get(varPrefix + AddressForm.KM));
    block = (String)variables.get(varPrefix + AddressForm.BLOCK);
    entranceHall = (String)variables.get(varPrefix + AddressForm.ENTRANCE_HALL);
    stair = (String)variables.get(varPrefix + AddressForm.STAIR);
    floor = (String)variables.get(varPrefix + AddressForm.FLOOR);
    door = (String)variables.get(varPrefix + AddressForm.DOOR);
    postalCode = (String)variables.get(varPrefix + AddressForm.POSTAL_CODE);
    postOfficeBox = (String)variables.get(varPrefix + AddressForm.POST_OFFICE_BOX);
  }

  private void refineList(List<AddressView> addresses)
  {
  }

  private Double toDouble(Object value)
  {
    Double num;
    if (value instanceof Number)
    {
      num = ((Number)value).doubleValue();
    }
    else num = null;
    return num;
  }

  private KernelManagerPort getKernelManagerPort()
  {
    try
    {
      WSDirectory dir = WSDirectory.getInstance();
      WSEndpoint endpoint = dir.getEndpoint(KernelManagerService.class);

      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String userId = userSessionBean.getUsername();
      String password = userSessionBean.getPassword();

      return endpoint.getPort(KernelManagerPort.class, userId, password);
    }
    catch (Exception ex)
    {
      throw new RuntimeException();
    }
  }

  public boolean isAddressRequired()
  {
    return addressRequired;
  }

  public void setAddressRequired(boolean addressRequired)
  {
    this.addressRequired = addressRequired;
  }

  public boolean isCreationAllowed()
  {
    return creationAllowed;
  }

  public void setCreationAllowed(boolean creationAllowed)
  {
    this.creationAllowed = creationAllowed;
  }

  public String getForcedCity()
  {
    return forcedCity;
  }

  public void setForcedCity(String forcedCity)
  {
    this.forcedCity = forcedCity;
  }
}
