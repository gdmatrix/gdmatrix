package com.audifilm.matrix.security.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBDepartament extends DBGenesysEntity{
  String areaId;
  String departamentId;
  String description;

  public DBDepartament()
  {
    this.areaId = null;
    this.departamentId = null;
    this.description = null;
  }


  @Override
  public String[] getIds()
  {
    return new String[] {
      areaId, departamentId
    };
  }

  public String getAreaId() {
    return areaId;
  }

  public String getDepartamentId() {
    return departamentId;
  }

  public String getDescription() {
    return description;
  }

  public void setAreaId(String areaId) {
    this.areaId = areaId;
  }

  public void setDepartamentId(String departamentId) {
    this.departamentId = departamentId;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

}