package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.common.service.PKUtil;
import com.audifilm.matrix.dic.service.person.PersonType;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import com.audifilm.matrix.util.TextUtil;
import java.util.Vector;
import org.matrix.kernel.Address;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressView;
import org.matrix.kernel.PersonView;
import org.matrix.util.WSEndpoint;


public class DBFotoPersona extends DBGenesysEntity
{
  private String personId;
  private String personType;
  private String name;
  private String firstSurname;
  private String secondSurname;
  private String nif;
  private String personAddressNumber;
  private String domicili;
  private String addressId;
  
  public DBFotoPersona()
  {
  }

  public DBFotoPersona(String personId, Vector fields)
  {
     this(personId, fields.toArray());
  }

  public DBFotoPersona(String personId, Object [] fields)
  {
     this.personId = personId;

     this.name = (String)fields[1];
     this.firstSurname = (String)fields[2];
     this.secondSurname = (String)fields[3];
     this.nif = (String)fields[4];
     this.personType = (String)fields[5];
     this.personAddressNumber = (String)fields[6];
     this.addressId = (String)fields[7];
     this.domicili = (String)fields[8];
  }



  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonType(String personType)
  {
    this.personType = personType;
  }

  public String getPersonType()
  {
    return personType;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setFirstSurname(String firstSurname)
  {
    this.firstSurname = firstSurname;
  }

  public String getFirstSurname()
  {
    return firstSurname;
  }

  public void setSecondSurname(String secondSurname)
  {
    this.secondSurname = secondSurname;
  }

  public String getSecondSurname()
  {
    return secondSurname;
  }

  public void setNif(String nifnum)
  {
    this.nif = nifnum;
  }

  public String getDomicili()
  {
    return domicili;
  }

  public void setDomicili(String domicili)
  {
    this.domicili = domicili;
  }

  public String getPersonAddressNumber()
  {
    return personAddressNumber;
  }

  public void setPersonAddressNumber(String personAddressNumber)
  {
    this.personAddressNumber = personAddressNumber;
  }

  public String getNif()
  {
    return nif;
  }

  /* helper methods */
  public String getFullName()
  {
    if (name == null) return null;
    StringBuffer buffer = new StringBuffer(name);
    if (firstSurname != null)
      buffer.append(" " + firstSurname);
    if (secondSurname != null)
      buffer.append(" " + secondSurname);
    return buffer.toString();
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  /* Conversion methods */
  /**
  public void copyFrom(Person person)
  {
    this.personId = person.getPersonId();
    switch (person.getPersonType())
    {
      case NATIONAL_PERSON:
        this.personType = "F"; break;
      case RESIDENT_PERSON:
        this.personType = "R"; break;
      case FOREIGN_PERSON:
        this.personType = "P"; break;      
      case UNIDENTIFIED_PERSON:
        this.personType = "Z"; break;
      case NATIONAL_JURISTIC_PERSON:
        this.personType = "J"; break;
      case FOREIGN_JURISTIC_PERSON:
        this.personType = "E"; break;
      case UNIDENTIFIED_JURISTIC_PERSON:
        this.personType = "Y"; break;
    }
    this.name = person.getName();
    if (this.name != null)
    {
      this.name = this.name.toUpperCase();
    }
    this.firstSurname = person.getFirstSurname();
    if (this.firstSurname != null)
    {
      this.firstSurname = this.firstSurname.toUpperCase();
    }
    this.secondSurname = person.getSecondSurname();
    if (this.secondSurname != null)
    {
      this.secondSurname = this.secondSurname.toUpperCase();
    }
    this.nif = person.getNif();
  } */

  public void copyTo(WSEndpoint endpoint, Person person)
  {
    person.setPersonId(TextUtil.encodeEmpty(this.personId));
    if (personType!=null) {
      person.setPersonTypeId(DicTypeAdmin.getInstance(PersonType.class)
        .toGlobalId(endpoint, personType));
    }
    person.setName(TextUtil.encodeEmpty(name));
    person.setFirstSurname(TextUtil.encodeEmpty(firstSurname));
    person.setSecondSurname(TextUtil.encodeEmpty(secondSurname));
    person.setNif(TextUtil.encodeEmpty(nif));
  }

  public void copyTo(WSEndpoint endpoint, PersonAddress personAddress)
  {
    personAddress.setPersonAddressId(endpoint.toGlobalId(PersonAddress.class, PKUtil.composePK(this.personId, this.personAddressNumber)));
    personAddress.setPersonId(endpoint.toGlobalId(Person.class, getPersonId()));
    personAddress.setAddressId(endpoint.toGlobalId(Address.class, getAddressId()));
  }

  public void copyTo(WSEndpoint endpoint, PersonAddressView personAddressView)
  {
    personAddressView.setPersonAddressId(endpoint.toGlobalId(PersonAddress.class, PKUtil.composePK(this.personId, this.personAddressNumber)));

    PersonView personView = new PersonView();
    AddressView addressView = new AddressView();
    copyTo( endpoint, personView);
    copyTo( endpoint, addressView);

    personAddressView.setPerson(personView);
    personAddressView.setAddress(addressView);

  }

  public void copyTo(WSEndpoint endpoint, AddressView addressView)
  {
    addressView.setAddressId(endpoint.toGlobalId(Address.class, getAddressId()));
    addressView.setDescription(getDomicili());
  }

  public void copyTo(WSEndpoint endpoint, Address address)
  {
    address.setAddressId(endpoint.toGlobalId(Address.class, getAddressId()));
    address.setComments(getDomicili());
  }

  public void copyTo(WSEndpoint endpoint, PersonView personView)
  {
    personView.setPersonId(endpoint.toGlobalId(Person.class, getPersonId()));
    personView.setFullName(getFullName());
    switch(PersonType.Types.getType(getPersonType()))
    {
      case X:
      case P:
      {
        personView.setPassport(getNif());
      }
      default:
      {
        personView.setNif(getNif());
      }
    }
  }

  @Override
  public String[] getIds()
  {
    return new String [] {personId};
  }
}
