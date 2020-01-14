package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.common.service.PKUtil;
import com.audifilm.matrix.common.service.VersionType;
import com.audifilm.matrix.common.service.VersionIdentifier;
import com.audifilm.matrix.dic.service.caseperson.CasePersonType;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import com.audifilm.matrix.util.TextUtil;
import org.matrix.cases.Case;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonView;
import org.matrix.kernel.Address;
import org.matrix.kernel.Contact;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonAddressView;
import org.matrix.kernel.PersonView;
import org.matrix.util.WSEndpoint;
  
/**
 *  
 * @author comasfc
 */
public class DBInteressat extends DBGenesysEntity
{

  String caseId;
  String personId;
  String personAddressNumber;
  String representantId;
  String representantAddressNumber;
  String notisw;
  String motiu;
  String trecId;
  String personName;
  String personSurname1;
  String personSurname2;
  String addressDescription;
  String representantName;
  String representantSurname1;
  String representantSurname2;
  String representantAddressDescription;
  String personNif;
  String representantNif;
  String licitador;
  String datos;
  String fcontacn;
  String numconordre;
  String valnotint;
  String plataforma;

  String fotoPersonaId;
  String fotoRepresentantId;
  String fotoAdrecaPersonaId;
  String fotoAdrecaRepresenantId;

  DBCase caseObject;

  public DBCase getCaseObject()
  {
    return caseObject;
  }

  public void setCaseObject(DBCase caseObject)
  {
    this.caseObject = caseObject;
  }

  public String getAddressDescription()
  {
    return addressDescription;
  }

  public void setAddressDescription(String addressDescription)
  {
    this.addressDescription = addressDescription;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getDatos()
  {
    return datos;
  }

  public void setDatos(String datos)
  {
    this.datos = datos;
  }

  public String getFcontacn()
  {
    return fcontacn;
  }

  public void setFcontacn(String fcontacn)
  {
    this.fcontacn = fcontacn;
  }

  public String getLicitador()
  {
    return licitador;
  }

  public void setLicitador(String licitador)
  {
    this.licitador = licitador;
  }

  public String getMotiu()
  {
    return motiu;
  }

  public void setMotiu(String motiu)
  {
    this.motiu = motiu;
  }

  public String getNotisw()
  {
    return notisw;
  }

  public void setNotisw(String notisw)
  {
    this.notisw = notisw;
  }

  public String getNumconordre()
  {
    return numconordre;
  }

  public void setNumconordre(String numconordre)
  {
    this.numconordre = numconordre;
  }

  public String getPersonAddressNumber()
  {
    return personAddressNumber;
  }

  public void setPersonAddressNumber(String personAddressNum)
  {
    this.personAddressNumber = personAddressNum;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonName()
  {
    return personName;
  }

  public void setPersonName(String personName)
  {
    this.personName = personName;
  }

  public String getPersonNif()
  {
    return personNif;
  }

  public void setPersonNif(String personNif)
  {
    this.personNif = personNif;
  }

  public String getPersonSurname1()
  {
    return personSurname1;
  }

  public void setPersonSurname1(String personSurname1)
  {
    this.personSurname1 = personSurname1;
  }

  public String getPersonSurname2()
  {
    return personSurname2;
  }

  public void setPersonSurname2(String personSurname2)
  {
    this.personSurname2 = personSurname2;
  }

  public String getPlataforma()
  {
    return plataforma;
  }

  public void setPlataforma(String plataforma)
  {
    this.plataforma = plataforma;
  }
  
  public String getRepresentantAddressDescription()
  {
    return representantAddressDescription;
  }

  public void setRepresentantAddressDescription(String representantAddressDescription)
  {
    this.representantAddressDescription = representantAddressDescription;
  }

  public String getRepresentantAddressNumber()
  {
    return representantAddressNumber;
  }

  public void setRepresentantAddressNumber(String representantAddressNum)
  {
    this.representantAddressNumber = representantAddressNum;
  }

  public String getRepresentantId()
  {
    return representantId;
  }

  public void setRepresentantId(String representantId)
  {
    this.representantId = representantId;
  }

  public String getRepresentantName()
  {
    return representantName;
  }

  public void setRepresentantName(String representantName)
  {
    this.representantName = representantName;
  }

  public String getRepresentantNif()
  {
    return representantNif;
  }

  public void setRepresentantNif(String representantNif)
  {
    this.representantNif = representantNif;
  }

  public String getRepresentantSurname1()
  {
    return representantSurname1;
  }

  public void setRepresentantSurname1(String representantSurname1)
  {
    this.representantSurname1 = representantSurname1;
  }

  public String getRepresentantSurname2()
  {
    return representantSurname2;
  }

  public void setRepresentantSurname2(String representantSurname2)
  {
    this.representantSurname2 = representantSurname2;
  }

  public String getTrecId()
  {
    return trecId;
  }

  public void setTrecId(String trecId)
  {
    this.trecId = trecId;
  }

  public String getValnotint()
  {
    return valnotint;
  }

  public void setValnotint(String valnotint)
  {
    this.valnotint = valnotint;
  }

  public String getId()
  {
    return PKUtil.composePK(caseId, personId, personAddressNumber);
  }

  @Override
  public String[] getIds()
  {
    return new String[] { caseId, personId, personAddressNumber };
  }

  public void copyTo(WSEndpoint endpoint,CasePerson casePerson)
  {
    if (getMotiu()!=null)
    {
      casePerson.setCasePersonTypeId(
        DicTypeAdmin.getInstance(CasePersonType.class).toGlobalId(endpoint, getMotiu())
      );
    }
     
    casePerson.setCasePersonId(
            PKUtil.makeMatrixPK(endpoint.getEntity(CasePerson.class),
            getCaseId(),
            getPersonId()));

    casePerson.setCaseId(
            PKUtil.makeMatrixPK(endpoint.getEntity(Case.class),
            getCaseId()));

    casePerson.setPersonId(endpoint.toGlobalId(Person.class, getFotoPersonaId()));
    casePerson.setAddressId(endpoint.toGlobalId(Address.class, getFotoAdrecaPersonaId()));
    casePerson.setRepresentantPersonId(endpoint.toGlobalId(Person.class, getFotoRepresentantId()));
    casePerson.setRepresentantAddressId(endpoint.toGlobalId(Address.class, getFotoAdrecaRepresentantId()));

        //casePerson.setContactId(endpoint.toGlobalId(Contact.class, PKUtil.composePK("1", getPersonId(), getFcontacn())));
    casePerson.getContactId().clear();
    String contactId =
      endpoint.toGlobalId(Contact.class, PKUtil.composePK("1", getPersonId(), getFcontacn()));
    if (contactId != null && contactId.trim().length() > 0)
      casePerson.getContactId().add(contactId);

    
    casePerson.setCreationUserId(getStdugr());
    casePerson.setCreationDateTime(getCreationDateTime());
    casePerson.setChangeUserId(getStdumod());
    casePerson.setChangeDateTime(getChangeDateTime());
  }

  public void copyTo(WSEndpoint endpoint,CasePersonView casePerson)
  {
    casePerson.setCasePersonId(
            PKUtil.makeMatrixPK(endpoint.getEntity(CasePerson.class),
            getCaseId(),
            getPersonId()));
 
    if (getMotiu()!=null)
    {
      casePerson.setCasePersonTypeId(
        DicTypeAdmin.getInstance(CasePersonType.class).toGlobalId(endpoint, getMotiu())      
      );
    }

    PersonView personView = new PersonView();
    personView.setFullName( (TextUtil.encodeEmpty(getPersonName()) 
            + " " + TextUtil.encodeEmpty(getPersonSurname1())
            + " " + TextUtil.encodeEmpty(getPersonSurname2())).trim()  );
    personView.setNif( TextUtil.encodeEmpty(getPersonNif()));
    personView.setPersonId(endpoint.toGlobalId(Person.class, getFotoPersonaId()));
    casePerson.setPersonView(personView);

    PersonView representantView = new PersonView();
    representantView.setFullName( (TextUtil.encodeEmpty(getRepresentantName()) 
            + " " + TextUtil.encodeEmpty(getRepresentantSurname1())
            + " " + TextUtil.encodeEmpty(getRepresentantSurname2())).trim() );
    representantView.setNif( TextUtil.encodeEmpty(getRepresentantNif()) );
    representantView.setPersonId(endpoint.toGlobalId(Person.class, getFotoRepresentantId()));
    
    casePerson.setRepresentantPersonView(representantView);



    Case caseObj = new Case();
    if (caseObject!=null) caseObject.copyTo(endpoint, caseObj);
    casePerson.setCaseObject(caseObj);



  }

  public void copyFrom(CaseManager caseManager, CasePerson casePerson)
  {
    String personId = casePerson.getPersonId();
    String representantId = casePerson.getRepresentantPersonId();

    this.setCaseId(casePerson.getCaseId());
    this.setPersonId(personId);
    this.setRepresentantId(representantId);

    casePerson.getCasePersonTypeId();

   String contactId = casePerson.getContactId().isEmpty() ?
      null : casePerson.getContactId().get(0);
    String [] ids = PKUtil.decomposePK(contactId);
    this.setFcontacn((ids!=null && ids.length>=3)?ids[2]:null);

    if (casePerson.getCasePersonTypeId()!=null)
    {
      this.setMotiu(
         DicTypeAdmin.getInstance(CasePersonType.class)
         .toLocalId(caseManager.getEndpoint(), casePerson.getCasePersonTypeId()));
    }

    if (personId!=null && !personId.equals(""))
    {
      PersonAddressView personAddressView = caseManager.getKernelService().loadPersonAddressView(casePerson.getPersonId(), casePerson.getAddressId(), true);
      Person person = caseManager.getKernelService().loadPerson(casePerson.getPersonId());
      if (person!=null)
      {
        this.setPersonName(person.getName());
        this.setPersonSurname1(person.getFirstSurname());
        this.setPersonSurname2(person.getSecondSurname());

        if (person.getNif()!=null && !"".equals(person.getNif().trim()))
        {
          this.setPersonNif(person.getNif());
        }
        else
        {
          this.setPersonNif(person.getPassport());
        }
        this.setPersonAddressNumber( KernelService.getPersonAddressNumber(personAddressView.getPersonAddressId()));
        if (personAddressView.getAddress()!=null)
        {
          this.setAddressDescription(personAddressView.getAddress().getDescription() + " - " + personAddressView.getAddress().getCity());
        }
        personAddressView = null;
        person = null;
      }
    }

    if (representantId!=null && !representantId.equals(""))
    {
      Person representant = caseManager.getKernelService().loadPerson(casePerson.getRepresentantPersonId());
      if (representant!=null)
      {
        PersonAddressView personAddressView = caseManager.getKernelService().loadPersonAddressView(casePerson.getPersonId(), casePerson.getAddressId(), true);

        this.setRepresentantName(representant.getName());
        this.setRepresentantSurname1(representant.getFirstSurname());
        this.setRepresentantSurname2(representant.getSecondSurname());
        if (representant.getNif()!=null && !"".equals(representant.getNif().trim()))
        {
          this.setRepresentantNif(representant.getNif());
        }
        else
        {
          this.setRepresentantNif(representant.getPassport());
        }
        this.setRepresentantAddressNumber( KernelService.getPersonAddressNumber(personAddressView.getPersonAddressId()) );
        if (personAddressView.getAddress()!=null)
        {
          this.setRepresentantAddressDescription(personAddressView.getAddress().getDescription() + " - " + personAddressView.getAddress().getCity());
        }
        personAddressView = null;
        representant=null;
      }
    }
  }


  public String getFotoPersonaId()
  {
    return getFotoPersonaVersionId().getLocalVersionedId();
  }

  public String getFotoRepresentantId()
  {
    return getFotoRepresentantVersionId().getLocalVersionedId();
  }

  public String getFotoAdrecaPersonaId()
  {
    return getFotoAdrecaPersonaVersionId().getLocalVersionedId();
  }

  public String getFotoAdrecaRepresentantId()
  {
    return getFotoAdrecaRepresentantVersionId().getLocalVersionedId();
  }

  private VersionIdentifier getFotoPersonaVersionId()
  {
    return new VersionIdentifier(
        VersionType.FOTO_PERSON_CASE_PERSON,
        getPersonId(),
        new String [] {getCaseId()}
        );
  }
  public VersionIdentifier getFotoRepresentantVersionId()
  {
    return new VersionIdentifier(
        VersionType.FOTO_PERSON_CASE_REPRESENTANT,
        getRepresentantId(),
        new String [] {getCaseId(), getPersonId()}
        );
  }

  public VersionIdentifier getFotoAdrecaPersonaVersionId()
  {
    return new VersionIdentifier(
        VersionType.FOTO_ADDRESS_CASE_PERSONDOM,
        (getPersonAddressNumber()==null || getPersonAddressNumber().equals(""))
              ?null:
              (new String [] {getPersonId(), getPersonAddressNumber()}),
        new String [] {getCaseId(), getPersonId()});
  }

  public VersionIdentifier getFotoAdrecaRepresentantVersionId()
  {
    return new VersionIdentifier(
        VersionType.FOTO_ADDRESS_CASE_REPRESENTANTDOM,
        (getRepresentantAddressNumber()==null || getRepresentantAddressNumber().equals(""))
            ?null:
            (new String [] {getRepresentantId(), getRepresentantAddressNumber()}),
        new String [] {getCaseId(), getPersonId(), getRepresentantId()});
  }

  public void setFotoPersonaId(String fotoPersonaId)
  {
    this.fotoPersonaId = fotoPersonaId;
  }

  public void setFotoRepresentantId(String fotoRepresentantId)
  {
    this.fotoRepresentantId = fotoRepresentantId;
  }
  public void setFotoAdrecaPersonaId(String fotoAdrecaPersonaId)
  {
    this.fotoAdrecaPersonaId = fotoAdrecaPersonaId;
  }
  public void setFotoAdrecaRepresentantId(String fotoAdrecaRepresenantId)
  {
    this.fotoAdrecaRepresenantId = fotoAdrecaRepresenantId;
  }
}
 