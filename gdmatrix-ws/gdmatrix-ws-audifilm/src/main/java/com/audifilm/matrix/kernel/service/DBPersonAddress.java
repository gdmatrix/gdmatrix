package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.common.service.PKUtil;
import org.matrix.kernel.Address;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonAddress;
import org.matrix.util.WSEndpoint;


public class DBPersonAddress extends DBGenesysEntity
{
  private String personId;
  private String addressId;
  private String personAddressNumber;
  private String stdapladd;
  private String stdaplmod;
  private String baixasw;
  private String valdata;
  
  private DBPerson person;
  private DBAddress address;

  public DBPersonAddress()
  {
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setPersonAddressNumber(String persnd)
  {
    this.personAddressNumber = persnd;
  }

  public String getPersonAddressNumber()
  {
    return personAddressNumber;
  }

  public void setStdapladd(String stdapladd)
  {
    this.stdapladd = stdapladd;
  }

  public String getStdapladd()
  {
    return stdapladd;
  }

  public void setStdaplmod(String stdaplmod)
  {
    this.stdaplmod = stdaplmod;
  }

  public String getStdaplmod()
  {
    return stdaplmod;
  }

  public void setBaixasw(String baixasw)
  {
    this.baixasw = baixasw;
  }

  public String getBaixasw()
  {
    return baixasw;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }

  /* relationships */
  public void setAddress(DBAddress address)
  {
    this.address = address;
  }

  public DBAddress getAddress()
  {
    return address;
  }

  public void setPerson(DBPerson person)
  {
    this.person = person;
  }

  public DBPerson getPerson()
  {
    return person;
  }

  public void copyTo(WSEndpoint endpoint, PersonAddress personAddress)
  {
    personAddress.setPersonAddressId(
      PKUtil.makeMatrixPK(endpoint.getEntity(PersonAddress.class),
            personId, personAddressNumber));
    personAddress.setPersonId(endpoint.toGlobalId(Person.class ,personId));
    personAddress.setAddressId(endpoint.toGlobalId(Address.class, addressId));
  }
  
  public void copyFrom(WSEndpoint endpoint, PersonAddress personAddress)
  {
    personId = endpoint.toLocalId(Person.class, personAddress.getPersonId());
    addressId = endpoint.toLocalId(Address.class ,personAddress.getAddressId());
  }

  @Override
  public String[] getIds()
  {
    return new String[] {personId, personAddressNumber};
  }

  public String getPersonAddressId() {
    return PKUtil.composePK(personId, personAddressNumber);
  }
}
