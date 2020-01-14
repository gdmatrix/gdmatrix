package com.audifilm.matrix.security.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

public class DBNivellArea extends DBGenesysEntity
{

  String aplId;
  String areaId;
  String nivellId;

  @Override
  public String[] getIds()
  {
    return new String[] {
        aplId,areaId,nivellId
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

  public String getNivellId()
  {
    return nivellId;
  }

  public void setNivellId(String nivellId)
  {
    this.nivellId = nivellId;
  }

}
