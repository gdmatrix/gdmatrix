package com.audifilm.matrix.security.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

public class DBNivellUsuari extends DBGenesysEntity
{

  String aplId;
  String nivellId;
  String userId;


  @Override
  public String[] getIds()
  {
    return new String[] {
        aplId, nivellId, userId
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

  public String getNivellId()
  {
    return nivellId;
  }

  public void setNivellId(String nivellId)
  {
    this.nivellId = nivellId;
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
