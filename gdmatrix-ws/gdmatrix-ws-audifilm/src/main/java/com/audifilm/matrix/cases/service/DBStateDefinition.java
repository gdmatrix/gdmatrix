package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBStateDefinition extends DBGenesysEntity
{
  String caseTypeId;
  String stateId;
  String description;
  String areaId;
  String departamentId;
  String grupId;

  public String getCaseTypeId()
  {
    return caseTypeId;
  }

  public void setCaseTypeId(String caseTypeId)
  {
    this.caseTypeId = caseTypeId;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getStateId()
  {
    return stateId;
  }

  public void setStateId(String stateId)
  {
    this.stateId = stateId;
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

  @Override
  public String[] getIds()
  {
    return new String[] {
      caseTypeId, stateId
    };
  }
  
}
