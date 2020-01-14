package com.audifilm.matrix.kernel.service;

import java.io.Serializable;


public class DBStreetPK implements Serializable
{
  private String countryId;
  private String provinceId;
  private String cityId;
  private String streetId;

  public DBStreetPK()
  {
  }

  public DBStreetPK(String streetId)
  {
    String ids[] = streetId.split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
    this.cityId = ids[2];
    this.streetId = ids[3];
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

  public void setStreetId(String streetId)
  {
    this.streetId = streetId;
  }

  public String getStreetId()
  {
    return streetId;
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
    DBStreetPK pk = (DBStreetPK)o;
    return pk.getCountryId().equals(countryId) &&
      pk.getProvinceId().equals(provinceId) && 
      pk.getCityId().equals(cityId) &&
      pk.getStreetId().equals(streetId);
  }
  
  public int hashCode()
  {
    return (countryId + KernelManager.PK_SEPARATOR + 
      provinceId + KernelManager.PK_SEPARATOR + 
      cityId + KernelManager.PK_SEPARATOR + streetId).hashCode();
  }
}
