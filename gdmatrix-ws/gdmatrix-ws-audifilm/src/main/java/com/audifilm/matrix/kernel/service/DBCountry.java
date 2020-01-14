package com.audifilm.matrix.kernel.service;

import org.matrix.kernel.Country;

import org.matrix.util.WSEndpoint;


public class DBCountry extends DBEntityBase
{
  private String countryId;
  private String name;
  private int paisvnum;
  private String baixasw;
  private String valdata;
  
  public DBCountry()
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

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setPaisvnum(int paisvnum)
  {
    this.paisvnum = paisvnum;
  }

  public int getPaisvnum()
  {
    return paisvnum;
  }

  public void setBaixasw(String baixasw)
  {
    this.baixasw = baixasw;
  }

  public String getBaixasw()
  {
    return baixasw;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }
  
  public void copyFrom(Country country)
  {
    this.countryId = country.getCountryId();
    this.name = country.getName();
  }
  
  public void copyTo(WSEndpoint endpoint, Country country)
  {
    country.setCountryId(endpoint.toGlobalId(Country.class, countryId));
    country.setName(name);
  }
}
