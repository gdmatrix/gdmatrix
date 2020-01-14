package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.PKUtil;
import java.io.Serializable;


public class DBPersonAddressPK implements Serializable
{
  private String personId;
  private String personAddressNumber;

  public DBPersonAddressPK()
  {
  }

  public DBPersonAddressPK(String personAddressId)
  {
    String ids[] = PKUtil.decomposePK(personAddressId);
    this.personId = (ids==null)?null:ids[0];
    this.personAddressNumber = (ids==null)?null:ids[1];
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonAddressNumber(String personAddressNumber)
  {
    this.personAddressNumber = personAddressNumber;
  }

  public String getPersonAddressNumber()
  {
    return personAddressNumber;
  }
  
  public boolean equals(Object o)
  {
    DBPersonAddressPK pk = (DBPersonAddressPK)o;
    return pk.getPersonId().equals(personId) &&
      pk.getPersonAddressNumber().equals(personAddressNumber);
  }
  
  public int hashCode()
  {
    return (personId + personAddressNumber).hashCode();
  }
}
