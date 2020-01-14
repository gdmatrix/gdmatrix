package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBVariablePK extends GenesysPK
{
  public String caseTypeId;
  public String variableId;

  public DBVariablePK()
  {

  }

  public DBVariablePK(String caseTypeId, String variableId)
  {
    super(caseTypeId, variableId);

    this.caseTypeId = caseTypeId;
    this.variableId = variableId;
  }

  public DBVariablePK(String pk)
  {
    super(pk);
    
    String ids[] = getIds();
    this.caseTypeId = ids[0];
    this.variableId = ids[1];
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
