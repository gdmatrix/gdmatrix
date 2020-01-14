package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.common.service.PKUtil;
import org.matrix.cases.Case;
import org.matrix.cases.CaseAddress;
import org.matrix.kernel.Address;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class DBCaseAddress extends DBGenesysEntity
{
  String caseId;
  String addressId;
  DBCase caseObject;
  

  @Override
  public String[] getIds()
  {
    return new String [] {caseId, addressId};
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public DBCase getCaseObject()
  {
    return caseObject;
  }

  public void setCaseObject(DBCase caseObject)
  {
    this.caseObject = caseObject;
  }

  public void copyTo(WSEndpoint endpoint, CaseAddress caseAddress) {
    caseAddress.setCaseId(endpoint.toGlobalId(Case.class, caseId));
    caseAddress.setAddressId(endpoint.toGlobalId(Address.class, addressId));
    caseAddress.setCaseAddressId(PKUtil.makeMatrixPK(endpoint.getEntity(CaseAddress.class), caseId, addressId));
  }

}
