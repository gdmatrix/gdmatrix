package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBInteressatPK extends GenesysPK
{
  String caseId;
  String personId;
  String personAddressNumber;

  public DBInteressatPK()
  {

  }

  public DBInteressatPK(String caseId, String personId, String personAddressNumber)
  {
    super(caseId, personId, personAddressNumber);
    this.caseId = caseId;
    this.personId = personId;
    this.personAddressNumber = personAddressNumber;
  }

  public DBInteressatPK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.caseId = ids[0];
    this.personId = ids[1];
    this.personAddressNumber = (ids.length>2?ids[2]:null);
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getPersonAddressNumber()
  {
    return personAddressNumber;
  }

  public void setPersonAddressNumber(String personAddressNumber)
  {
    this.personAddressNumber = personAddressNumber;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

}
