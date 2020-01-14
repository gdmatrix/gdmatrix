package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBCaseStatePK extends GenesysPK
{
  public String caseId;
  public String caseStateReferece;

  public DBCaseStatePK()
  {

  }

  public DBCaseStatePK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.caseId = ids[0];
    this.caseStateReferece = ids[1];
  }

  public DBCaseStatePK(String caseId, String caseStateReference)
  {
    super(caseId, caseStateReference);
    this.caseId = caseId;
    this.caseStateReferece = caseStateReference;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getCaseStateReferece()
  {
    return caseStateReferece;
  }

  public void setCaseStateReferece(String caseStateReferece)
  {
    this.caseStateReferece = caseStateReferece;
  }

}
