package com.audifilm.matrix.kernel.service;

import java.io.Serializable;

public class DBCityPK implements Serializable
{
  private String countryId;
  private String provinceId;
  private String cityId;
  
  public DBCityPK()
  {
  }

  public DBCityPK(String cityId)
  {
    String ids[] = cityId.split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
    this.cityId = ids[2];
  }

  public void setCountryId(String countryId)
  {
    this.countryId = countryId;
  }

  public String getCountryId()
  {
    return countryId;
  }

  public void setProvinceId(String provinceId)
  {
    this.provinceId = provinceId;
  }

  public String getProvinceId()
  {
    return provinceId;
  }

  public void setCityId(String cityId)
  {
    this.cityId = cityId;
  }

  public String getCityId()
  {
    return cityId;
  }

  public boolean equals(Object o)
  {
    DBCityPK pk = (DBCityPK)o;
    return pk.getCountryId().equals(countryId) &&
      pk.getProvinceId().equals(provinceId) && 
      pk.getCityId().equals(cityId);
  }
  
  public int hashCode()
  {
    return (countryId + KernelManager.PK_SEPARATOR + provinceId +
      KernelManager.PK_SEPARATOR + cityId).hashCode();
  }
}
