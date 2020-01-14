package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.PKUtil;
import org.matrix.kernel.Country;
import org.matrix.kernel.Province;

import org.matrix.util.WSEndpoint;

public class DBProvince extends DBEntityBase
{
  private String countryId;
  private String provinceId;
  private String name;
  private int provvnum;
  private int autocod; 
  private String valdata;
  private String baixasw;

  private DBCountry country;

  public DBProvince()
  {
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

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setProvvnum(int provvnum)
  {
    this.provvnum = provvnum;
  }

  public int getProvvnum()
  {
    return provvnum;
  }

  public void setAutocod(int autocod)
  {
    this.autocod = autocod;
  }

  public int getAutocod()
  {
    return autocod;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }

  public void setBaixasw(String baixasw)
  {
    this.baixasw = baixasw;
  }

  public String getBaixasw()
  {
    return baixasw;
  }
  
  /* relationships */
  public void setCountry(DBCountry country)
  {
    this.country = country;
  }

  public DBCountry getCountry()
  {
    return country;
  }
  
  public void copyFrom(Province province)
  {
    String ids[] = province.getProvinceId().split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
    this.name = province.getName();
  }
  
  public void copyTo(WSEndpoint endpoint, Province province)
  {
    province.setProvinceId(
      PKUtil.makeMatrixPK(endpoint.getEntity(Province.class),
        countryId, provinceId));
    province.setCountryId(endpoint.toGlobalId(Country.class, countryId));
    province.setName(name);
  }
}
