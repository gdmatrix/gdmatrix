package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.dic.service.person.PersonType;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import org.matrix.kernel.Person;
import org.matrix.kernel.Sex;
import org.matrix.util.WSEndpoint;


public class DBPerson extends DBEntityBase
{
  private String personId;
  private String personType;
  private String name;
  private String firstParticle;
  private String firstSurname;
  private String secondParticle;
  private String secondSurname;
  private String sex;
  private String nifnum;
  private String nifdc;
  private String passport;
  private String birthDate;
  private String nationalityId; // countryId
  private String birthCountryId;
  private String birthProvinceId;
  private String birthCityId;  
  private String fatherName;
  private String motherName;
  private int persvnum;
  private int contvnum;
  private String stdapladd;
  private String stdaplmod;
  private String baixasw;
  private String valdata;
  private String persdcannif;
  private String persdconnif;
  private String nifnump;
  private String niforig;
  private String perscodold;
  private String idiocod;

  public DBPerson()
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

  public void setFirstParticle(String firstParticle)
  {
    this.firstParticle = firstParticle;
  }

  public String getFirstParticle()
  {
    return firstParticle;
  }

  public void setFirstSurname(String firstSurname)
  {
    this.firstSurname = firstSurname;
  }

  public String getFirstSurname()
  {
    return firstSurname;
  }

  public void setSecondParticle(String secondParticle)
  {
    this.secondParticle = secondParticle;
  }

  public String getSecondParticle()
  {
    return secondParticle;
  }

  public void setSecondSurname(String secondSurname)
  {
    this.secondSurname = secondSurname;
  }

  public String getSecondSurname()
  {
    return secondSurname;
  }

  public void setSex(String sex)
  {
    this.sex = sex;
  }

  public String getSex()
  {
    return sex;
  }

  public void setNifnum(String nifnum)
  {
    this.nifnum = nifnum;
  }

  public String getNifnum()
  {
    return nifnum;
  }

  public void setNifdc(String nifdc)
  {
    this.nifdc = nifdc;
  }

  public String getNifdc()
  {
    return nifdc;
  }

  public void setPassport(String passport)
  {
    this.passport = passport;
  }

  public String getPassport()
  {
    return passport;
  }

  public void setBirthDate(String bithDate)
  {
    this.birthDate = bithDate;
  }

  public String getBirthDate()
  {
    return birthDate;
  }

  public void setNationalityId(String nationalityId)
  {
    this.nationalityId = nationalityId;
  }

  public String getNationalityId()
  {
    return nationalityId;
  }

  public void setBirthCountryId(String birthCountryId)
  {
    this.birthCountryId = birthCountryId;
  }

  public String getBirthCountryId()
  {
    return birthCountryId;
  }

  public void setBirthProvinceId(String birthProvinceId)
  {
    this.birthProvinceId = birthProvinceId;
  }

  public String getBirthProvinceId()
  {
    return birthProvinceId;
  }

  public void setBirthCityId(String birthCityId)
  {
    this.birthCityId = birthCityId;
  }

  public String getBirthCityId()
  {
    return birthCityId;
  }

  public void setFatherName(String fatherName)
  {
    this.fatherName = fatherName;
  }

  public String getFatherName()
  {
    return fatherName;
  }

  public void setMotherName(String motherName)
  {
    this.motherName = motherName;
  }

  public String getMotherName()
  {
    return motherName;
  }

  public void setPersvnum(int persvnum)
  {
    this.persvnum = persvnum;
  }

  public int getPersvnum()
  {
    return persvnum;
  }

  public void setContvnum(int contvnum)
  {
    this.contvnum = contvnum;
  }

  public int getContvnum()
  {
    return contvnum;
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

  public String getNifnump()
  {
    return nifnump;
  }

  public void setNifnump(String nifnump)
  {
    this.nifnump = nifnump;
  }

  public String getNiforig()
  {
    return niforig;
  }

  public void setNiforig(String niforig)
  {
    this.niforig = niforig;
  }

  public String getPerscodold()
  {
    return perscodold;
  }

  public void setPerscodold(String perscodold)
  {
    this.perscodold = perscodold;
  }

  public String getPersdcannif()
  {
    return persdcannif;
  }

  public void setPersdcannif(String persdcannif)
  {
    this.persdcannif = persdcannif;
  }

  public String getPersdconnif()
  {
    return persdconnif;
  }

  public void setPersdconnif(String persdconnif)
  {
    this.persdconnif = persdconnif;
  }

  public String getIdiocod()
  {
    return idiocod;
  }

  public void setIdiocod(String idiocod)
  {
    this.idiocod = idiocod;
  }

  

  /* helper methods */
  public String getFullName()
  {
    if (name == null) return null;
    StringBuffer buffer = new StringBuffer(name);
    if (firstParticle != null)
      buffer.append(" " + firstParticle);
    if (firstSurname != null)
      buffer.append(" " + firstSurname);
    if (secondParticle != null)
      buffer.append(" " + secondParticle);
    if (secondSurname != null)
      buffer.append(" " + secondSurname);
    return buffer.toString();
  }
  
  public String getNif()
  {
    if (nifnum == null) return null;
    StringBuffer buffer = new StringBuffer(nifnum);
    if (nifdc != null)
      buffer.append(nifdc);
    return buffer.toString();
  }

  /* Conversion methods */
  public void copyFrom(WSEndpoint endpoint, Person globalPerson)
  {
    Person person = endpoint.toLocal(Person.class, globalPerson);

    this.personId = person.getPersonId();
    this.personType = DicTypeAdmin
      .getInstance(com.audifilm.matrix.dic.service.person.PersonType.class)
      .toLocalId(endpoint, person.getPersonTypeId());
    
    this.name = person.getName();
    if (this.name != null)
    {
      this.name = this.name.toUpperCase();
    }
    this.firstParticle = person.getFirstParticle();
    this.firstSurname = person.getFirstSurname();
    if (this.firstSurname != null)
    {
      this.firstSurname = this.firstSurname.toUpperCase();
    }
    this.secondParticle = person.getSecondParticle();
    this.secondSurname = person.getSecondSurname();
    if (this.secondSurname != null)
    {
      this.secondSurname = this.secondSurname.toUpperCase();
    }
    switch (person.getSex())
    {
      case NONE:
        this.sex = "0"; break;
      case MALE:
        this.sex = "1"; break;
      case FEMALE:
        this.sex = "2"; break;
    }
    this.nifnum = null;
    this.nifdc = null;
    String nif = person.getNif();
    if (nif != null)
    {
      nif = nif.replaceAll("\\-", "");
      nif = nif.replaceAll("\\.", "");
      if (nif.length() >= 8)
      {
        this.nifnum = nif.substring(0, 8);
        if (nif.length() >= 9)
        {
          this.nifdc = nif.substring(8, 9);
        }
        else if (Character.isDigit(nifnum.charAt(0))) // NIF
        {
          String chunck = "TRWAGMYFPDXBNJZSQVHLCKET";
          try
          {
            int num = Integer.parseInt(nifnum);
            char dc = chunck.charAt(num % 23);
            this.nifdc = "" + dc;
          }
          catch (Exception ex)
          {
            this.nifdc = null;
          }
        }
        else // CIF
        {
        }
      }
    }
    this.passport = person.getPassport();
    this.birthDate = person.getBirthDate();
    this.nationalityId = person.getNationalityId();
    if (person.getBirthCityId() == null)
    {
      this.birthCountryId = null;
      this.birthProvinceId = null;
      this.birthCityId = null;
    }
    else
    {
      String ids[] = person.getBirthCityId().split(KernelManager.PK_SEPARATOR);
      this.birthCountryId = ids[0];
      this.birthProvinceId = ids[1];
      this.birthCityId = ids[2];
    }
    this.fatherName = person.getFatherName();
    this.motherName = person.getMotherName();
  }

  public void copyTo(WSEndpoint endpoint, Person person)
  {
    person.setPersonId(this.personId);
    person.setPersonTypeId(DicTypeAdmin.getInstance(PersonType.class)
      .toGlobalId(endpoint, personType));
    person.setName(name);
    person.setFirstParticle(firstParticle);
    person.setFirstSurname(firstSurname);
    person.setSecondParticle(secondParticle);
    person.setSecondSurname(secondSurname);
    if (sex == null)
    {
      person.setSex(Sex.NONE);
    }
    else
    {
      switch (sex.charAt(0))
      {
        case '0':
          person.setSex(Sex.NONE); break;
        case '1':
          person.setSex(Sex.MALE); break;
        case '2':
          person.setSex(Sex.FEMALE); break;
      }
    }
    if (nifnum != null)
    {
      if (nifdc != null)
      {
        person.setNif(nifnum + nifdc);
      }
      else
      {
        person.setNif(nifnum);
      }
    }
    person.setPassport(passport);
    person.setBirthDate(birthDate);
    person.setNationalityId(nationalityId);
    if (birthCityId != null)
    {
      person.setBirthCityId(
        birthCountryId + KernelManager.PK_SEPARATOR + birthProvinceId + 
        KernelManager.PK_SEPARATOR + birthCityId);
    }
    person.setFatherName(fatherName);
    person.setMotherName(motherName);


  }
}
