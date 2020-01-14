package com.audifilm.matrix.kernel.service;

import java.io.Serializable;

public class DBTelecoPK implements Serializable
{
  private String personId;
  private int contactNumber;

  public DBTelecoPK()
  {
  }

  public DBTelecoPK(String contactId)
  {
    String ids[] = contactId.split(KernelManager.PK_SEPARATOR);
    personId = ids[1];
    contactNumber = Integer.parseInt(ids[2]);
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setContactNumber(int contactNumber)
  {
    this.contactNumber = contactNumber;
  }

  public int getContactNumber()
  {
    return contactNumber;
  }
  
  public boolean equals(Object o)
  {
    DBTelecoPK pk = (DBTelecoPK)o;
    return pk.getPersonId().equals(personId) && 
      pk.contactNumber == contactNumber;
  }
  
  public int hashCode()
  {
    return (personId + KernelManager.PK_SEPARATOR + contactNumber).hashCode();
  }
}
