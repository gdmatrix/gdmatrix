package com.audifilm.matrix.kernel.service;

import java.io.Serializable;

public class DBProvincePK implements Serializable
{
  private String countryId;
  private String provinceId;
  
  public DBProvincePK()
  {
  }

  public DBProvincePK(String provinceId)
  {
    String ids[] = provinceId.split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
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
  
  public boolean equals(Object o)
  {
    DBProvincePK pk = (DBProvincePK)o;
    return pk.getCountryId().equals(countryId) &&
      pk.getProvinceId().equals(provinceId);
  }
  
  public int hashCode()
  {
    return (countryId + KernelManager.PK_SEPARATOR + provinceId).hashCode();
  }
}
