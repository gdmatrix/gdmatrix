package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBCaseVariable extends DBGenesysEntity
{
  String caseId;
  String caseTypeId;
  String variableId;
  String value;
  String x;
  String y;
  String tramcod;

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getCaseTypeId()
  {
    return caseTypeId;
  }

  public void setCaseTypeId(String caseTypeId)
  {
    this.caseTypeId = caseTypeId;
  }

  public String getVariableId()
  {
    return variableId;
  }

  public void setVariableId(String variableId)
  {
    this.variableId = variableId;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getX()
  {
    return x;
  }

  public void setX(String x)
  {
    this.x = x;
  }

  public String getY()
  {
    return y;
  }

  public void setY(String y)
  {
    this.y = y;
  }

  public String getTramcod()
  {
    return tramcod;
  }

  public void setTramcod(String tramcod)
  {
    this.tramcod = tramcod;
  }

  @Override
  public String[] getIds()
  {
    return new String [] {caseId, caseTypeId, variableId};
  }

}
