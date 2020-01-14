package org.santfeliu.kernel.web;

import org.matrix.kernel.Country;
import org.matrix.kernel.Province;

import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;


public class ProvinceBean extends ObjectBean
{
  public ProvinceBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Province";
  }
  
  public String getDescription()
  {
    CountryToStreetBean bean = 
      (CountryToStreetBean)getBean("countryToStreetBean");
    return getDescription(bean.getProvince(), bean.getCountry());
  }
  
  public String getDescription(String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId)) return "";
    try
    {
      Province province = KernelConfigBean.getPort().loadProvince(objectId);
      Country country = KernelConfigBean.getPort().loadCountry(
        province.getCountryId());
      return getDescription(province, country);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;    
  }
  
  private String getDescription(Province province, Country country)
  {
    StringBuffer buffer = new StringBuffer();
    if (province != null) buffer.append(province.getName());
    if (country != null) buffer.append(" (" + country.getName() + ")");
    return buffer.toString();
  }
}
