package org.santfeliu.kernel.web;


import org.matrix.kernel.Country;

import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.ObjectBean;

public class CountryBean extends ObjectBean
{
  public CountryBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Country";
  }
  
  public String getDescription()
  {
    CountryToStreetBean bean = 
      (CountryToStreetBean)getBean("countryToStreetBean");
    return getDescription(bean.getCountry());
  }
  
  public String getDescription(String objectId)
  {
    if (ControllerBean.NEW_OBJECT_ID.equals(objectId)) return "";
    try
    {
      Country country = KernelConfigBean.getPort().loadCountry(objectId);
      return getDescription(country);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }
  
  private String getDescription(Country country)
  {
    return country == null ? "" : country.getName();
  }
}
