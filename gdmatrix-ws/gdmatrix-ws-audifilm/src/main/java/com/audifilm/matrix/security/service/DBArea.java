package com.audifilm.matrix.security.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBArea extends DBGenesysEntity{
  String areaId;
  String description;

  public DBArea()
  {
    this.areaId = null;
    this.description = null;
  }


  @Override
  public String[] getIds()
  {
    return new String[] {
      areaId
    };
  }

  public String getAreaId() {
    return areaId;
  }

  public String getDescription() {
    return description;
  }

  public void setAreaId(String areaId) {
    this.areaId = areaId;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
