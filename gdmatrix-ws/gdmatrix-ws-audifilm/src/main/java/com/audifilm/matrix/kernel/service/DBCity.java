package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.PKUtil;
import org.matrix.kernel.City;
import org.matrix.kernel.Province;
import org.matrix.util.WSEndpoint;


public class DBCity extends DBEntityBase
{
  private String countryId;
  private String provinceId;
  private String cityId;
  private String name;
  private String description;
  private String munidc;
  private int munivnum;
  private int illavnum;
  private int tramvnum;
  private String comcod;
  private String valdata;
  private String baixasw;
  private String municps;
  
  private DBProvince province;
  
  public DBCity()
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

  public void setCityId(String cityId)
  {
    this.cityId = cityId;
  }

  public String getCityId()
  {
    return cityId;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setMunidc(String munidc)
  {
    this.munidc = munidc;
  }

  public String getMunidc()
  {
    return munidc;
  }

  public void setMunivnum(int munivnum)
  {
    this.munivnum = munivnum;
  }

  public int getMunivnum()
  {
    return munivnum;
  }

  public void setIllavnum(int illavnum)
  {
    this.illavnum = illavnum;
  }

  public int getIllavnum()
  {
    return illavnum;
  }

  public void setTramvnum(int tramvnum)
  {
    this.tramvnum = tramvnum;
  }

  public int getTramvnum()
  {
    return tramvnum;
  }

  public void setComcod(String comcod)
  {
    this.comcod = comcod;
  }

  public String getComcod()
  {
    return comcod;
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

  public void setMunicps(String municps)
  {
    this.municps = municps;
  }

  public String getMunicps()
  {
    return municps;
  }

  /* relationships */
  public void setProvince(DBProvince province)
  {
    this.province = province;
  }

  public DBProvince getProvince()
  {
    return province;
  }

  public void copyFrom(City city)
  {
    String ids[] = city.getCityId().split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
    this.cityId = ids[2];
    this.name = city.getName();
  }
  
  public void copyTo(WSEndpoint endpoint, City city)
  {
    city.setProvinceId(
            PKUtil.makeMatrixPK(endpoint.getEntity(Province.class),
            countryId, provinceId));
    city.setCityId(
            PKUtil.makeMatrixPK(endpoint.getEntity(City.class),
            countryId, provinceId, cityId));
    city.setName(name);
  }
}
