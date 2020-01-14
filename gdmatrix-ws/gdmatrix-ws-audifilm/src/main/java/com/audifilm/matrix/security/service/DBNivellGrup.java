package com.audifilm.matrix.security.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

public class DBNivellGrup extends DBGenesysEntity
{

  String areaId;
  String departamentId;
  String grupId;

  String aplId;
  String nivellId;


  @Override
  public String[] getIds()
  {
    return new String[] {
        areaId,departamentId,grupId,aplId,nivellId
      };
  }

  public String getAplId()
  {
    return aplId;
  }

  public void setAplId(String aplId)
  {
    this.aplId = aplId;
  }

  public String getAreaId()
  {
    return areaId;
  }

  public void setAreaId(String areaId)
  {
    this.areaId = areaId;
  }

  public String getDepartamentId()
  {
    return departamentId;
  }

  public void setDepartamentId(String departamentId)
  {
    this.departamentId = departamentId;
  }

  public String getGrupId()
  {
    return grupId;
  }

  public void setGrupId(String grupId)
  {
    this.grupId = grupId;
  }

  public String getNivellId()
  {
    return nivellId;
  }

  public void setNivellId(String nivellId)
  {
    this.nivellId = nivellId;
  }

  

}