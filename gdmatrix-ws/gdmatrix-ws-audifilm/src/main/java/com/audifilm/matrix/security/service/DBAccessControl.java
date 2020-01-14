package com.audifilm.matrix.security.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

public class DBAccessControl extends DBGenesysEntity
{

  String aplId;
  String itemId;
  String controlId;
  String nivellId;
  String mode;


  @Override
  public String[] getIds()
  {
    return new String[] {
        aplId,itemId,controlId,nivellId
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

  public String getControlId()
  {
    return controlId;
  }

  public void setControlId(String controlId)
  {
    this.controlId = controlId;
  }

  public String getItemId()
  {
    return itemId;
  }

  public void setItemId(String itemId)
  {
    this.itemId = itemId;
  }

  public String getMode()
  {
    return mode;
  }

  public void setMode(String mode)
  {
    this.mode = mode;
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