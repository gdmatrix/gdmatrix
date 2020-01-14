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


  @Override
  public String[] getIds()
  {
    return new String [] {caseId, caseTypeId, variableId};
  }

}
