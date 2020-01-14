package com.audifilm.matrix.kernel.service;

public class DBStreetType extends DBEntityBase
{
  private String streetTypeId;
  private String description;
  private String abbreviation;
  private String baixasw;
  private String valdata;
  
  public DBStreetType()
  {
  }

  public void setStreetTypeId(String streetTypeId)
  {
    this.streetTypeId = streetTypeId;
  }

  public String getStreetTypeId()
  {
    return streetTypeId;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public void setAbbreviation(String abbreviation)
  {
    this.abbreviation = abbreviation;
  }

  public String getAbbreviation()
  {
    return abbreviation;
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
}
