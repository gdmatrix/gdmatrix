package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBCaseStatePK extends GenesysPK
{
  public String caseId;
  public String estatNum;

  public DBCaseStatePK()
  {

  }

  public DBCaseStatePK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.caseId = ids[0];
    this.estatNum = ids[1];
  }

  public DBCaseStatePK(String caseId, String estatNum)
  {
    super(caseId, estatNum);
    this.caseId = caseId;
    this.estatNum = estatNum;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getEstatNum()
  {
    return estatNum;
  }

  public void setEstatNum(String estatNum)
  {
    this.estatNum = estatNum;
  }

}
