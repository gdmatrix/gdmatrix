package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.common.service.PKUtil;
import org.matrix.cases.Case;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseCaseView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class DBCaseCase extends DBGenesysEntity
{
  String caseId;
  String relCaseId;
  DBCase mainCase;
  DBCase relCase;

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public DBCase getMainCase()
  {
    return mainCase;
  }

  public void setMainCase(DBCase mainCase)
  {
    this.mainCase = mainCase;
  }

  public DBCase getRelCase()
  {
    return relCase;
  }

  public void setRelCase(DBCase relCase)
  {
    this.relCase = relCase;
  }

  public String getRelCaseId()
  {
    return relCaseId;
  }

  public void setRelCaseId(String relCaseId)
  {
    this.relCaseId = relCaseId;
  }

  public void copyTo(WSEndpoint endpoint, CaseCase caseCase)
  {
    DBCaseCasePK pk = new DBCaseCasePK(caseId, relCaseId);
    caseCase.setCaseCaseId(PKUtil.makeMatrixPK(endpoint.getEntity(CaseCase.class) ,pk.getIds()));
    caseCase.setCaseId(endpoint.toGlobalId(Case.class, caseId));
    caseCase.setRelCaseId(endpoint.toGlobalId(Case.class, relCaseId));
    caseCase.setCaseCaseTypeId(DictionaryConstants.CASE_CASE_TYPE);
  }

  public void copyTo(WSEndpoint endpoint, CaseCaseView caseCaseView)
  {
    DBCaseCasePK pk = new DBCaseCasePK(caseId, relCaseId);
    caseCaseView.setCaseCaseId(PKUtil.makeMatrixPK(endpoint.getEntity(CaseCase.class) ,pk.getIds()));

    Case mCase = new Case();
    mainCase.copyTo(endpoint, mCase);

    Case rCase = new Case();
    relCase.copyTo(endpoint, rCase);

    caseCaseView.setMainCase(mCase);
    caseCaseView.setRelCase(rCase);
  }

  @Override
  public String[] getIds()
  {
    return new String [] {caseId, relCaseId};
  }

}
