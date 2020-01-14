package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.PKUtil;
import org.matrix.kernel.City;
import org.matrix.kernel.Street;

import org.matrix.util.WSEndpoint;


public class DBStreet extends DBEntityBase
{
  private String countryId;
  private String provinceId;
  private String cityId;
  private String streetId;
  private String streetTypeId; // references DBStreetType
  private String particle;
  private String name;
  private String description;
  private String valdata;
  private String baixasw;

  private DBCity city;
  private DBStreetType streetType;
 
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

  public void setStreetId(String streetId)
  {
    this.streetId = streetId;
  }

  public String getStreetId()
  {
    return streetId;
  }

  public void setStreetTypeId(String streetTypeId)
  {
    this.streetTypeId = streetTypeId;
  }

  public String getStreetTypeId()
  {
    return streetTypeId;
  }

  public void setParticle(String particle)
  {
    this.particle = particle;
  }

  public String getParticle()
  {
    return particle;
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
  public void setCity(DBCity city)
  {
    this.city = city;
  }

  public DBCity getCity()
  {
    return city;
  }

  public void setStreetType(DBStreetType streetType)
  {
    this.streetType = streetType;
  }

  public DBStreetType getStreetType()
  {
    return streetType;
  }

  public void copyFrom(Street street)
  {
    String ids[] = street.getStreetId().split(KernelManager.PK_SEPARATOR);
    this.countryId = ids[0];
    this.provinceId = ids[1];
    this.cityId = ids[2];
    this.streetId = ids[3];
    this.name = street.getName();
    this.particle = street.getParticle();
    this.streetTypeId = street.getStreetTypeId();
  }
  
  public void copyTo(WSEndpoint endpoint, Street street)
  {
    street.setCityId(
            PKUtil.makeMatrixPK(
              endpoint.getEntity(City.class),
              countryId, provinceId, cityId));
    street.setStreetId(
            PKUtil.makeMatrixPK(
              endpoint.getEntity(Street.class),
              countryId, provinceId, cityId, streetId));
    street.setName(name);
    street.setStreetTypeId(streetTypeId);
    street.setParticle(particle);
  }
}
