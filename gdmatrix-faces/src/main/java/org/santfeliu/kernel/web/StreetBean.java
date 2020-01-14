package org.santfeliu.kernel.web;

import org.matrix.kernel.City;
import org.matrix.kernel.Province;
import org.matrix.kernel.Street;

import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;


public class StreetBean extends ObjectBean
{
  public StreetBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Street";
  }
  
  public String getDescription()
  {
    CountryToStreetBean bean = 
      (CountryToStreetBean)getBean("countryToStreetBean");
    return getDescription(bean.getStreet(), bean.getCity(), bean.getProvince());
  }
  
  public String getDescription(String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId)) return "";
    try
    {
      Street street = KernelConfigBean.getPort().loadStreet(objectId);
      City city = KernelConfigBean.getPort().loadCity(street.getCityId());
      Province province = KernelConfigBean.getPort().loadProvince(
        city.getProvinceId());
      return getDescription(street, city, province);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }
  
  private String getDescription(Street street, City city, Province province)
  {
    StringBuffer buffer = new StringBuffer();
    if (street != null) 
      buffer.append(street.getStreetTypeId() + " " + street.getName());
    if (city != null)
      buffer.append(" - " + city.getName());
    if (province != null)
      buffer.append(" (" + province.getName() + ")");
    return buffer.toString();
  }
}
