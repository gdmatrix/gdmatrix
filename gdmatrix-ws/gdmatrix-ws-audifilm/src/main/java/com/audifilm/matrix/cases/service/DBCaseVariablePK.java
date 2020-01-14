package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBCaseVariablePK extends GenesysPK
{

  public String caseTypeId;
  public String variableId;
  public String caseId;

  public DBCaseVariablePK()
  {
  }

  public DBCaseVariablePK(String caseTypeId, String variableId)
  {
    super(caseTypeId, variableId);

    this.caseTypeId = caseTypeId;
    this.variableId = variableId;
  }

  public DBCaseVariablePK(String pk)
  {
    super(pk);

    String ids[] = getIds();
    this.caseId = ids[0];
    this.caseTypeId = ids[1];
    this.variableId = ids[2];
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
}
