package org.santfeliu.kernel.web;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.matrix.kernel.City;
import org.matrix.kernel.CityFilter;
import org.matrix.kernel.Country;

import org.matrix.kernel.CountryFilter;
import org.matrix.kernel.Province;

import org.matrix.kernel.ProvinceFilter;
import org.matrix.kernel.Street;

import org.matrix.kernel.StreetFilter;

import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;
import org.santfeliu.web.obj.ObjectDescriptionCache;
import org.santfeliu.web.obj.PageBean;

@CMSManagedBean
public class CountryToStreetBean extends PageBean
{
  private transient List<SelectItem> countrySelectItems;
  private transient List<SelectItem> provinceSelectItems;
  private transient List<SelectItem> citySelectItems;
  private transient List<SelectItem> streetSelectItems;

  private Country country;
  private Province province;
  private City city;
  private Street street;
  
  private boolean editing = false;

  public CountryToStreetBean()
  {
  }

  @CMSAction
  public String show()
  {
    return show(null, true);
  }

  public void setCountry(Country country)
  {
    this.country = country;
  }

  public Country getCountry()
  {
    return country;
  }

  public void setProvince(Province province)
  {
    this.province = province;
  }

  public Province getProvince()
  {
    return province;
  }

  public void setCity(City city)
  {
    this.city = city;
  }

  public City getCity()
  {
    return city;
  }

  public void setStreet(Street street)
  {
    this.street = street;
  }

  public Street getStreet()
  {
    return street;
  }

  public boolean isEditing()
  {
    return editing;
  }

  public boolean isEditableCountry()
  {
    CountryBean countryBean = (CountryBean)getBean("countryBean");
    return !ControllerBean.NEW_OBJECT_ID.equals(countryBean.getObjectId());
  }

  public boolean isEditableProvince()
  {
    ProvinceBean provinceBean = (ProvinceBean)getBean("provinceBean");
    return !ControllerBean.NEW_OBJECT_ID.equals(provinceBean.getObjectId());
  }

  public boolean isEditableCity()
  {
    CityBean cityBean = (CityBean)getBean("cityBean");
    return !ControllerBean.NEW_OBJECT_ID.equals(cityBean.getObjectId());
  }

  public boolean isEditableStreet()
  {
    StreetBean streetBean = (StreetBean)getBean("streetBean");
    return !ControllerBean.NEW_OBJECT_ID.equals(streetBean.getObjectId());
  }

  public boolean isSelectableCountry()
  {
    CountryBean countryBean = (CountryBean)getBean("countryBean");
    return !countryBean.isNew() &&
      countryBean == getControllerBean().getSearchObjectBean();
  }

  public boolean isSelectableProvince()
  {
    ProvinceBean provinceBean = (ProvinceBean)getBean("provinceBean");
    return !provinceBean.isNew() &&
      provinceBean == getControllerBean().getSearchObjectBean();
  }

  public boolean isSelectableCity()
  {
    CityBean cityBean = (CityBean)getBean("cityBean");
    return !cityBean.isNew() &&
      cityBean == getControllerBean().getSearchObjectBean();
  }

  public boolean isSelectableStreet()
  {
    StreetBean streetBean = (StreetBean)getBean("streetBean");
    return !streetBean.isNew() &&
      streetBean == getControllerBean().getSearchObjectBean();
  }

  public List<SelectItem> getCountrySelectItems()
  {
    if (countrySelectItems == null)
    {
      System.out.println("countrySelectItems");
      countrySelectItems = new ArrayList<SelectItem>();
      countrySelectItems.add(new SelectItem("", " "));
      try
      {
        CountryFilter filter = new CountryFilter();
        List<Country> countries = 
          KernelConfigBean.getPort().findCountries(filter);
        for (Country country : countries)
        {
          countrySelectItems.add(
            new SelectItem(country.getCountryId(), country.getName()));
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return countrySelectItems;
  }

  public List<SelectItem> getProvinceSelectItems()
  {
    if (provinceSelectItems == null)
    {
      System.out.println("provinceSelectItems");    
      provinceSelectItems = new ArrayList<SelectItem>();
      provinceSelectItems.add(new SelectItem("", " "));
      try
      {
        CountryBean countryBean = (CountryBean)getBean("countryBean");
        ProvinceFilter filter = new ProvinceFilter();
        filter.setCountryId(countryBean.getObjectId());
        if (!ControllerBean.NEW_OBJECT_ID.equals(countryBean.getObjectId()))
        {
          List<Province> provinces = 
            KernelConfigBean.getPort().findProvinces(filter);
          for (Province province : provinces)
          {
            provinceSelectItems.add(
              new SelectItem(province.getProvinceId(), province.getName()));
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return provinceSelectItems;
  }

  public List<SelectItem> getCitySelectItems()
  {
    if (citySelectItems == null)
    {
      System.out.println("citySelectItems");        
      citySelectItems = new ArrayList<SelectItem>();
      citySelectItems.add(new SelectItem("", " "));
      try
      {
        ProvinceBean provinceBean = (ProvinceBean)getBean("provinceBean");
        CityFilter filter = new CityFilter();
        filter.setProvinceId(provinceBean.getObjectId());
        if (!ControllerBean.NEW_OBJECT_ID.equals(provinceBean.getObjectId()))
        {
          List<City> cities = 
            KernelConfigBean.getPort().findCities(filter);
          for (City city : cities)
          {
            citySelectItems.add(
              new SelectItem(city.getCityId(), city.getName()));
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return citySelectItems;
  }

  public List<SelectItem> getStreetSelectItems()
  {
    if (streetSelectItems == null)
    {
      System.out.println("streetSelectItems");        
      streetSelectItems = new ArrayList<SelectItem>();
      streetSelectItems.add(new SelectItem("", " "));
      try
      {
        CityBean cityBean = (CityBean)getBean("cityBean");
        StreetFilter filter = new StreetFilter();
        filter.setCityId(cityBean.getObjectId());
        if (!ControllerBean.NEW_OBJECT_ID.equals(cityBean.getObjectId()))
        {
          List<Street> streets = 
            KernelConfigBean.getPort().findStreets(filter);
          for (Street street : streets)
          {
            String label = street.getName();
            if (street.getStreetTypeId() != null)
            {
              label += " (" + street.getStreetTypeId() + ")";
            }
            streetSelectItems.add(new SelectItem(street.getStreetId(), label));
          }
        }
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return streetSelectItems;
  }

  // actions

  public String showCountry()
  {
    return show(findMid("countryBean"), false);
  }

  public String showProvince()
  {
    return show(findMid("provinceBean"), false);
  }

  public String showCity()
  {
    return show(findMid("cityBean"), false);
  }

  public String showStreet()
  {
    return show(findMid("streetBean"), false);
  }

  public String editCountry()
  {
    editing = true;
    return show(findMid("countryBean"), false);
  }

  public String editProvince()
  {
    editing = true;
    return show(findMid("provinceBean"), false);
  }

  public String editCity()
  {
    editing = true;
    return show(findMid("cityBean"), false);
  }

  public String editStreet()
  {
    editing = true;
    return show(findMid("streetBean"), false);
  }

  public String storeCountry()
  {
    try
    {
      country = KernelConfigBean.getPort().storeCountry(country);
      editing = false;
      ObjectBean objectBean = getObjectBean();
      objectBean.setObjectId(country.getCountryId());
      objectBean.getObjectHistory().setObject(objectBean.getObjectId());
      ObjectDescriptionCache.getInstance().clearDescription(
        objectBean, objectBean.getObjectId());
      countrySelectItems = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeProvince()
  {
    try
    {
      province = KernelConfigBean.getPort().storeProvince(province);
      editing = false;
      ObjectBean objectBean = getObjectBean();
      objectBean.setObjectId(province.getProvinceId());
      objectBean.getObjectHistory().setObject(objectBean.getObjectId());
      ObjectDescriptionCache.getInstance().clearDescription(
        objectBean, objectBean.getObjectId());
      provinceSelectItems = null;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String storeCity()
  {
    try
    {
      city = KernelConfigBean.getPort().storeCity(city);
      editing = false;
      ObjectBean objectBean = getObjectBean();
      objectBean.setObjectId(city.getCityId());
      objectBean.getObjectHistory().setObject(objectBean.getObjectId());
      ObjectDescriptionCache.getInstance().clearDescription(
        objectBean, objectBean.getObjectId());
      citySelectItems = null;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String storeStreet()
  {
    try
    {
      street = KernelConfigBean.getPort().storeStreet(street);
      editing = false;
      ObjectBean objectBean = getObjectBean();
      objectBean.setObjectId(street.getStreetId());
      objectBean.getObjectHistory().setObject(objectBean.getObjectId());
      ObjectDescriptionCache.getInstance().clearDescription(
        objectBean, objectBean.getObjectId());
      streetSelectItems = null;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String addCountry()
  {
    editing = true;
    CountryBean countryBean = (CountryBean)getBean("countryBean");
    countryBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
    return show(findMid("countryBean"), false);
  }

  public String addProvince()
  {
    editing = true;
    ProvinceBean provinceBean = (ProvinceBean)getBean("provinceBean");
    provinceBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
    return show(findMid("provinceBean"), false);
  }

  public String addCity()
  {
    editing = true;
    CityBean cityBean = (CityBean)getBean("cityBean");
    cityBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
    return show(findMid("cityBean"), false);
  }

  public String addStreet()
  {
    editing = true;
    StreetBean streetBean = (StreetBean)getBean("streetBean");
    streetBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
    return show(findMid("streetBean"), false);
  }

  public String removeCountry()
  {
    try
    {
      KernelConfigBean.getPort().removeCountry(country.getCountryId());
      editing = false;
      CountryBean countryBean = (CountryBean)getBean("countryBean");
      countryBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
      countrySelectItems = null;
      return show(findMid("countryBean"), false);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String removeProvince()
  {
    try
    {
      KernelConfigBean.getPort().removeProvince(province.getProvinceId());
      editing = false;
      ProvinceBean provinceBean = (ProvinceBean)getBean("provinceBean");
      provinceBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
      provinceSelectItems = null;
      return show(findMid("provinceBean"), false);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String removeCity()
  {
    try
    {
      KernelConfigBean.getPort().removeCity(city.getCityId());
      editing = false;
      CityBean cityBean = (CityBean)getBean("cityBean");
      cityBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
      citySelectItems = null;
      return show(findMid("cityBean"), false);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String removeStreet()
  {
    try
    {
      KernelConfigBean.getPort().removeStreet(street.getStreetId());
      editing = false;
      StreetBean streetBean = (StreetBean)getBean("streetBean");
      streetBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
      streetSelectItems = null;
      return show(findMid("streetBean"), false);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String cancel()
  {
    editing = false;
    return null;
  }

  // private methods

  private String show(String mid, boolean sync)
  {
    String outcome = null;
    try
    {
      if (mid != null) getControllerBean().setCurrentMid(mid);

      ObjectBean objectBean = getObjectBean();
      if (objectBean instanceof CountryBean)
      {
        String countryId = objectBean.getObjectId();
        loadCountry(countryId, sync);
        clearProvince();
        outcome = "country";
      }
      else if (objectBean instanceof ProvinceBean)
      {
        String provinceId = getObjectId();
        loadProvince(provinceId, sync);
        clearCity();
        outcome = "province";
      }
      else if (objectBean instanceof CityBean)
      {
        String cityId = getObjectId();
        loadCity(cityId, sync);
        clearStreet();
        outcome = "city";
      }
      else if (objectBean instanceof StreetBean)
      {
        String streetId = getObjectId();
        loadStreet(streetId, sync);
        outcome = "street";
      }
      if (!sync)
      {
        // register in history list
        objectBean.getObjectHistory().setObject(objectBean.getObjectId());
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return outcome;
  }

  private void loadCountry(String countryId, boolean sync)
    throws Exception
  {
    if (countryId != null && !ControllerBean.NEW_OBJECT_ID.equals(countryId))
    {
      this.country = KernelConfigBean.getPort().loadCountry(countryId);
      if (sync)
      {
        CountryBean countryBean = (CountryBean)getBean("countryBean");
        countryBean.setObjectId(countryId);
      }
    }
    else
    {
      this.country = new Country();
    }
  }
  
  private void loadProvince(String provinceId, boolean sync)
    throws Exception
  {
    if (provinceId != null && !ControllerBean.NEW_OBJECT_ID.equals(provinceId))
    {
      this.province = KernelConfigBean.getPort().loadProvince(provinceId);
      if (sync)
      {
        ProvinceBean provinceBean = (ProvinceBean)getBean("provinceBean");
        provinceBean.setObjectId(provinceId);
        loadCountry(province.getCountryId(), sync);
      }
    }
    else
    {
      this.province = new Province();
      if (country != null)
      {
        this.province.setCountryId(country.getCountryId());
      }
    }
  }

  private void loadCity(String cityId, boolean sync)
    throws Exception
  {
    if (cityId != null && !ControllerBean.NEW_OBJECT_ID.equals(cityId))
    {
      this.city = KernelConfigBean.getPort().loadCity(cityId);
      if (sync)
      {
        CityBean cityBean = (CityBean)getBean("cityBean");
        cityBean.setObjectId(cityId);
        loadProvince(city.getProvinceId(), sync);
      }
    }
    else
    {
      this.city = new City();
      if (province != null)
      {
        this.city.setProvinceId(province.getProvinceId());
      }
    }
  }

  private void loadStreet(String streetId, boolean sync)
    throws Exception
  {
    if (streetId != null && !ControllerBean.NEW_OBJECT_ID.equals(streetId))
    {
      this.street = KernelConfigBean.getPort().loadStreet(streetId);
      if (sync)
      {
        StreetBean streetBean = (StreetBean)getBean("streetBean");
        streetBean.setObjectId(streetId);
        loadCity(street.getCityId(), sync);
      }
    }
    else
    {
      this.street = new Street();
      if (city != null)
      {
        this.street.setCityId(city.getCityId());
      }
    }
  }
  
  private void clearCountry()
  {
    CountryBean countryBean = (CountryBean)getBean("countryBean");
    countryBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
    country = null;
    countrySelectItems = null;
    clearProvince();
  }
  
  private void clearProvince()
  {
    ProvinceBean provinceBean = (ProvinceBean)getBean("provinceBean");
    provinceBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
    province = null;
    provinceSelectItems = null;
    clearCity();
  }

  private void clearCity()
  {
    CityBean cityBean = (CityBean)getBean("cityBean");
    cityBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
    city = null;
    citySelectItems = null;
    clearStreet();
  }

  private void clearStreet()
  {
    StreetBean streetBean = (StreetBean)getBean("streetBean");
    streetBean.setObjectId(ControllerBean.NEW_OBJECT_ID);
    street = null;
    streetSelectItems = null;
  }

  private String findMid(String beanName)
  {
    MenuItemCursor menuItem = 
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();

    menuItem.moveParent();
    menuItem.moveFirstChild();
    String mid = null;
    while (!menuItem.isNull() && mid == null)
    {
      String nodeBeanName =
        menuItem.getProperty(ControllerBean.OBJECT_BEAN_PROPERTY);
      if (beanName.equals(nodeBeanName))
      {
        mid = menuItem.getMid();
      }
      else menuItem.moveNext();
    }
    return mid;
  }
}
