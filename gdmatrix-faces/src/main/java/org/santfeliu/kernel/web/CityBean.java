package org.santfeliu.kernel.web;

import org.matrix.kernel.City;

import org.matrix.kernel.Province;

import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;


public class CityBean extends ObjectBean
{
  public CityBean()
  {
  }

  public String getObjectTypeId()
  {
    return "City";
  }

  public String getDescription()
  {
    CountryToStreetBean bean = 
      (CountryToStreetBean)getBean("countryToStreetBean");
    return getDescription(bean.getCity(), bean.getProvince());
  }
  
  public String getDescription(String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId)) return "";
    try
    {
      City city = KernelConfigBean.getPort().loadCity(objectId);
      Province province = KernelConfigBean.getPort().loadProvince(
        city.getProvinceId());
      return getDescription(city, province);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }
  
  private String getDescription(City city, Province province)
  {
    StringBuffer buffer = new StringBuffer();
    if (city != null) buffer.append(city.getName());
    if (province != null) buffer.append(" (" + province.getName() + ")");
    return buffer.toString();
  }
}
