package com.audifilm.matrix.security.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

public class DBGrupUsuari extends DBGenesysEntity
{

  String areaId;
  String departamentId;
  String grupId;
  String userId;

  @Override
  public String[] getIds()
  {
    return new String[] {
        areaId,departamentId,grupId,userId
      };
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

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }




}