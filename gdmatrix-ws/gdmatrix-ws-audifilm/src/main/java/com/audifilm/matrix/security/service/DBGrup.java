package com.audifilm.matrix.security.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBGrup extends DBGenesysEntity {

  String areaId;
  String departamentId;
  String grupId;
  String description;

  public DBGrup()
  {
    this.areaId = null;
    this.departamentId = null;
    this.grupId = null;
    this.description = null;
  }


  @Override
  public String[] getIds()
  {
    return new String[] {
      areaId, departamentId, grupId
    };
  }

  public String getAreaId() {
    return areaId;
  }

  public String getDepartamentId() {
    return departamentId;
  }

  public String getGrupId() {
    return grupId;
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

  public void setGrupId(String grupId) {
    this.grupId = grupId;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
