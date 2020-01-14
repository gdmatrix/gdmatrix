package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBCaseCasePK extends GenesysPK
{

  public String caseId;
  public String relCaseId;

  public DBCaseCasePK()
  {

  }

  public DBCaseCasePK(String caseId, String relCaseId)
  {
    super(caseId, relCaseId);
    this.caseId = caseId;
    this.relCaseId = relCaseId;
  }

  public DBCaseCasePK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.caseId = ids[0];
    this.relCaseId = ids[1];
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getRelCaseId()
  {
    return relCaseId;
  }

  public void setRelCaseId(String relCaseId)
  {
    this.relCaseId = relCaseId;
  }


}
