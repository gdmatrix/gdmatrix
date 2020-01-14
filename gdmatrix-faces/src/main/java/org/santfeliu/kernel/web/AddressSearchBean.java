package org.santfeliu.kernel.web;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.TypeFilter;

import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.KernelConstants;
import org.matrix.kernel.KernelList;
import org.santfeliu.dic.web.TypeBean;
import org.santfeliu.dic.web.TypeSearchBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.BasicSearchBean;

public class AddressSearchBean extends BasicSearchBean
{
  @CMSProperty
  private static final String DEFAULT_CITY_NAME = "defaultCityName";
  
  private AddressFilter filter;
  private String addressId;
  private String cityName;
  private List<SelectItem> typeSelectItems;
  private List<SelectItem> streetTypeSelectItems;

  public AddressSearchBean()
  {
    filter = new AddressFilter();
  }

  public String getCityName()
  {
    return cityName;
  }

  public void setCityName(String cityName)
  {
    this.cityName = cityName;
  }
  
  public void setFilter(AddressFilter filter)
  {
    this.filter = filter;
  }

  public AddressFilter getFilter()
  {
    return filter;
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public int countResults()
  {
    try
    {
      filter.setCityName(cityName);
      filter.getAddressIdList().clear();
      if (addressId != null && addressId.trim().length() > 0)
      {
        filter.getAddressIdList().add(addressId);
      }
      return KernelConfigBean.getPort().countAddresses(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setCityName(cityName);
      filter.getAddressIdList().clear();
      if (addressId != null && addressId.trim().length() > 0)
      {
        filter.getAddressIdList().add(addressId);
      }
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return KernelConfigBean.getPort().findAddressViews(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String showAddress()
  {
    return getControllerBean().showObject("Address",
     (String)getValue("#{row.addressId}"));
  }

  public String selectAddress()
  {
    return getControllerBean().select((String)getValue("#{row.addressId}"));
  }

  public String show()
  {
    if (StringUtils.isBlank(cityName))
    {
      String defaultCityName = getProperty(DEFAULT_CITY_NAME);
      if (defaultCityName != null)
        cityName = defaultCityName;
    }
    return "address_search";
  }
  
  public String searchType()
  {
    TypeSearchBean typeSearchBean = (TypeSearchBean)getBean("typeSearchBean");
    if (typeSearchBean == null)
      typeSearchBean = new TypeSearchBean();

    typeSearchBean.setRootTypeId(DictionaryConstants.ADDRESS_TYPE);
    typeSearchBean.setFilter(new TypeFilter());
    typeSearchBean.search();

    return getControllerBean().searchObject("Type",
      "#{addressSearchBean.filter.addressTypeId}");
  }

  public List<SelectItem> getTypeSelectItems()
  {
    try
    {
      TypeBean typeBean = (TypeBean)getBean("typeBean");
      String[] actions = {DictionaryConstants.READ_ACTION};
      typeSelectItems = typeBean.getAllSelectItems(DictionaryConstants.ADDRESS_TYPE,
        KernelConstants.KERNEL_ADMIN_ROLE, actions, true);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return typeSelectItems;
  } 
  
  public List<SelectItem> getStreetTypeSelectItems()
  {
    if (streetTypeSelectItems == null)
    {
      streetTypeSelectItems = new ArrayList<javax.faces.model.SelectItem>();      
      try
      {
        streetTypeSelectItems = FacesUtils.getListSelectItems(
          KernelConfigBean.getPort().listKernelListItems(
          KernelList.STREET_TYPE),
          "itemId", "description", true);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return streetTypeSelectItems;
  }  
  
  public String clearFilter()
  {
    this.addressId = null;
    this.cityName = null;
    filter.getAddressIdList().clear();
    filter.setDescription(null);
    filter.setCountryName(null);
    filter.setCityName(null);
    filter.setStreetName(null);
    filter.setNumber(null);
    filter.setKm(null);
    filter.setBlock(null);
    filter.setEntranceHall(null);
    filter.setStair(null);
    filter.setFloor(null);
    filter.setDoor(null);
    filter.setPostalCode(null);
    filter.setPostOfficeBox(null);
    filter.setGisReference(null);
    filter.setComments(null);
    filter.setAddressTypeId(null);
    filter.setStreetTypeId(null);
    filter.setFirstResult(0);
    filter.setMaxResults(0);
    
    //Clear results
    reset();

    return show();    
  }  
}
