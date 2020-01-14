package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBCaseAddressPK extends GenesysPK
{
  String caseId;
  String addressId;

  public DBCaseAddressPK()
  {

  }

  public DBCaseAddressPK(String caseId, String addressId)
  {
    super(caseId, addressId);
    this.caseId = caseId;
    this.addressId = addressId;
  }

  public DBCaseAddressPK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.caseId = ids[0];
    this.addressId = ids[1];
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

}
