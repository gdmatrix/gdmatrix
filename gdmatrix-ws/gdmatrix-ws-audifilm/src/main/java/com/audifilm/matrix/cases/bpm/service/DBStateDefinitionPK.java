package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBStateDefinitionPK extends GenesysPK
{
  public String caseTypeId;
  public String stateId;

  public DBStateDefinitionPK()
  {

  }

  public DBStateDefinitionPK(String caseTypeId, String stateId)
  {
    super(caseTypeId, stateId);
    this.caseTypeId = caseTypeId;
    this.stateId = stateId;
  }

  public DBStateDefinitionPK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.caseTypeId = ids[0];
    this.stateId = ids[1];
  }

  public String getCaseTypeId()
  {
    return caseTypeId;
  }

  public void setCaseTypeId(String caseTypeId)
  {
    this.caseTypeId = caseTypeId;
  }

  public String getStateId()
  {
    return stateId;
  }

  public void setStateId(String stateId)
  {
    this.stateId = stateId;
  }

}
