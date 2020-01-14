package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.common.service.PKUtil;
import com.audifilm.matrix.common.service.VersionType;
import com.audifilm.matrix.common.service.VersionIdentifier;
import com.audifilm.matrix.dic.service.cases.CaseExpedientType;
import com.audifilm.matrix.dic.service.caseperson.CasePersonType;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import com.audifilm.matrix.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import javax.mail.Address;
import javax.persistence.EntityManager;

import org.matrix.cases.Case;
import org.matrix.cases.CaseAddress;
import org.matrix.cases.CasePerson;
import org.matrix.cases.CasePersonView;
import org.matrix.dic.Property;
import org.matrix.kernel.Contact;
import org.matrix.kernel.Person;
import org.matrix.kernel.PersonView;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class DBCase extends DBGenesysEntity
{
  String caseId; //caseId
  String caseTypeId; //caseTypeId
  String caseTypeNum;
  String description = ""; //Description
  String assumpte = ""; //Title
  String subassumpte = ""; //comments
  String registryDate; //startDate
  String registryTime; //startTime

  //PROPERTIES
  String extrcod = null;
  String extracte = null;
  
  String sdetext;
  String arxsigtop = null;
  String identval = null;
  String idiomacod = null;
  String eventexpr = String.format("%0999d",0);
  String persId = "0";
  String persnd = null;
  String reprId = null;
  //String sdenumtexp;
  String reprnd = null;
  String addressId = null;
  String sderel = null;
  String resracodec = null;
  String resrorg = null;
  //String sdedreg;
  //String sdehreg;
  String resrdata = null;
  String treccod = null;
  String persnom = "";
  String perscog1 = "";
  String perscog2 = "";
  String domicili = "";
  String repnom = null;
  String repcog1 = null;
  String repcog2 = null;
  String repdomi = null;
  String persnif = null;
  String repnif = null;
  String assumcod = null;
  String subassumcod = null;
  String sdenumcont = "";
  String fcontacn = null;
  String numconordre = null;
  String tipocod = null ;
  String licitacion = null;
  //String descassumpte;
  //String descsubassumpte;
  String transcod = "";
  String transcodarea = "";
  String assumcodorg = "";
  String subassumcodorg = "";
  String seccod = null;
  String subseccod = null;
  String sercod = null;
  String subsercod = null;
  String plataforma = null;

  String fotoPersonaId;
  String fotoRepresentantId;
  String fotoAdrecaPersonaId;
  String fotoAdrecaRepresentantId;

  DBKernelListItem extracteObj = null;

  private List<DBCaseState> caseStates;
  private List<Property> propList;

  public List<DBCaseState> getCaseStates() {
    if (caseStates==null) {
      caseStates = new ArrayList<DBCaseState>();
    }
    return caseStates;
  }
  public void setCaseStates(List<DBCaseState> states) {
    if (caseStates==null) {
      caseStates = new ArrayList<DBCaseState>();
    }
    caseStates.addAll(states);
  }

  public List<Property> getProperties()
  {
    if (propList==null) {
      propList = new ArrayList<Property>();
    }
    return propList;
  }
  public void setProperties(List<Property> list)
  {
    getProperties().addAll(list);
  }

  public String getAssumpte()
  {
    return assumpte;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public String getDescription()
  {
    return sdetext;
  }

  public String getCaseTypeId()
  {
    return caseTypeId;
  }

  public String getCaseTypeNum()
  {
    return caseTypeNum;
  }

  public String getRegistryDate()
  {
    return registryDate;
  }

  public String getRegistryTime()
  {
    return registryTime;
  }

  public String getSubassumpte()
  {
    return subassumpte;
  }

  public void setAssumpte(String assumpte)
  {
    this.assumpte = assumpte;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public void setDescription(String description)
  {
    this.sdetext = description;
  }

  public void setCaseTypeId(String caseTypeId)
  {
    this.caseTypeId = caseTypeId;
  }

  public void setCaseTypeNum(String caseTypeNum)
  {
    this.caseTypeNum = caseTypeNum;
  }

  public void setRegistryDate(String registryDate)
  {
    this.registryDate = registryDate;
  }

  public void setRegistryTime(String registryTime)
  {
    this.registryTime = registryTime;
  }

  public void setSubassumpte(String subassumpte)
  {
    this.subassumpte = subassumpte;
  }

  public String getArxsigtop()
  {
    return arxsigtop;
  }

  public void setArxsigtop(String arxsigtop)
  {
    this.arxsigtop = arxsigtop;
  }

  public String getAssumcod()
  {
    return assumcod;
  }

  public void setAssumcod(String assumcod)
  {
    this.assumcod = assumcod;
  }

  public String getAssumcodorg()
  {
    return assumcodorg;
  }

  public void setAssumcodorg(String assumcodorg)
  {
    this.assumcodorg = assumcodorg;
  }



  public String getDomicili()
  {
    return domicili;
  }

  public void setDomicili(String domicili)
  {
    this.domicili = domicili;
  }

  public String getEventexpr()
  {
    return eventexpr;
  }

  public void setEventexpr(String eventexpr)
  {
    this.eventexpr = eventexpr;
  }

  public String getExtrcod()
  {
    return extrcod;
  }

  public void setExtrcod(String extrcod)
  {
    this.extrcod = extrcod;
  }

  public String getFcontacn()
  {
    return fcontacn;
  }

  public void setFcontacn(String fcontacn)
  {
    this.fcontacn = fcontacn;
  }

  public String getIdentval()
  {
    return identval;
  }

  public void setIdentval(String identval)
  {
    this.identval = identval;
  }

  public String getIdiomacod()
  {
    return idiomacod;
  }

  public void setIdiomacod(String idiomacod)
  {
    this.idiomacod = idiomacod;
  }

  public String getLicitacion()
  {
    return licitacion;
  }

  public void setLicitacion(String licitacion)
  {
    this.licitacion = licitacion;
  }

  public String getNumconordre()
  {
    return numconordre;
  }

  public void setNumconordre(String numconordre)
  {
    this.numconordre = numconordre;
  }

  public String getPersId()
  {
    return persId;
  }

  public void setPersId(String perscod)
  {
    this.persId = perscod;
  }

  public String getPerscog1()
  {
    return perscog1;
  }

  public void setPerscog1(String perscog1)
  {
    this.perscog1 = perscog1;
  }

  public String getPerscog2()
  {
    return perscog2;
  }

  public void setPerscog2(String perscog2)
  {
    this.perscog2 = perscog2;
  }

  public String getPersnd()
  {
    return persnd;
  }

  public void setPersnd(String persnd)
  {
    this.persnd = persnd;
  }

  public String getPersnif()
  {
    return persnif;
  }

  public void setPersnif(String persnif)
  {
    this.persnif = persnif;
  }

  public String getPersnom()
  {
    return persnom;
  }

  public void setPersnom(String persnom)
  {
    this.persnom = persnom;
  }

  public String getPlataforma()
  {
    return plataforma;
  }

  public void setPlataforma(String plataforma)
  {
    this.plataforma = plataforma;
  }

  public String getRepcog1()
  {
    return repcog1;
  }

  public void setRepcog1(String repcog1)
  {
    this.repcog1 = repcog1;
  }

  public String getRepcog2()
  {
    return repcog2;
  }

  public void setRepcog2(String repcog2)
  {
    this.repcog2 = repcog2;
  }

  public String getRepdomi()
  {
    return repdomi;
  }

  public void setRepdomi(String repdomi)
  {
    this.repdomi = repdomi;
  }

  public String getRepnif()
  {
    return repnif;
  }

  public void setRepnif(String repnif)
  {
    this.repnif = repnif;
  }

  public String getRepnom()
  {
    return repnom;
  }

  public void setRepnom(String repnom)
  {
    this.repnom = repnom;
  }

  public String getReprId()
  {
    return reprId;
  }

  public void setReprId(String reprcod)
  {
    this.reprId = reprcod;
  }

  public String getReprnd()
  {
    return reprnd;
  }

  public void setReprnd(String reprnd)
  {
    this.reprnd = reprnd;
  }

  public String getResracodec()
  {
    return resracodec;
  }

  public void setResracodec(String resracodec)
  {
    this.resracodec = resracodec;
  }

  public String getResrdata()
  {
    return resrdata;
  }

  public void setResrdata(String resrdata)
  {
    this.resrdata = resrdata;
  }

  public String getResrorg()
  {
    return resrorg;
  }

  public void setResrorg(String resrorg)
  {
    this.resrorg = resrorg;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setAddressId(String addresscod)
  {
    this.addressId = addresscod;
  }


  public String getSdenumcont()
  {
    return sdenumcont;
  }

  public void setSdenumcont(String sdenumcont)
  {
    this.sdenumcont = sdenumcont;
  }

  public String getSderel()
  {
    return sderel;
  }

  public void setSderel(String sderel)
  {
    this.sderel = sderel;
  }


  public String getSeccod()
  {
    return seccod;
  }

  public void setSeccod(String seccod)
  {
    this.seccod = seccod;
  }

  public String getSercod()
  {
    return sercod;
  }

  public void setSercod(String sercod)
  {
    this.sercod = sercod;
  }

  public String getSubassumcod()
  {
    return subassumcod;
  }

  public void setSubassumcod(String subassumcod)
  {
    this.subassumcod = subassumcod;
  }

  public String getSubassumcodorg()
  {
    return subassumcodorg;
  }

  public void setSubassumcodorg(String subassumcodorg)
  {
    this.subassumcodorg = subassumcodorg;
  }

  public String getSubseccod()
  {
    return subseccod;
  }

  public void setSubseccod(String subseccod)
  {
    this.subseccod = subseccod;
  }

  public String getSubsercod()
  {
    return subsercod;
  }

  public void setSubsercod(String subsercod)
  {
    this.subsercod = subsercod;
  }

  public String getTipocod()
  {
    return tipocod;
  }

  public void setTipocod(String tipocod)
  {
    this.tipocod = tipocod;
  }

  public String getTranscod()
  {
    return transcod;
  }

  public void setTranscod(String transcod)
  {
    this.transcod = transcod;
  }

  public String getTranscodarea()
  {
    return transcodarea;
  }

  public void setTranscodarea(String transcodarea)
  {
    this.transcodarea = transcodarea;
  }

  public String getTreccod()
  {
    return treccod;
  }

  public void setTreccod(String treccod)
  {
    this.treccod = treccod;
  }

  public String getSdetext() {
    return this.sdetext;
  }

  public void setSdetext(String sdetext) {
    this.sdetext = sdetext;
  }


  public void copyTo(WSEndpoint endpoint, Case exp)
  {
    
    exp.setCaseId(endpoint.toGlobalId(Case.class,getCaseId()));
    exp.setCaseTypeId(DicTypeAdmin.getInstance(CaseExpedientType.class).toGlobalId(endpoint, caseTypeId));

    exp.setTitle( CaseManager.evalFieldExpression("Case.title", this, "${value}", TextUtil.encodeEmpty(caseTypeNum)));
    exp.setDescription( CaseManager.evalFieldExpression("Case.description", this, "${value}",  TextUtil.encodeEmpty(sdetext) ));
    exp.setComments(  CaseManager.evalFieldExpression("Case.comments", this, "${value}", "" ));

    exp.setStartDate(registryDate);
    exp.setStartTime(registryTime);

    exp.setCreationDateTime(getCreationDateTime());
    exp.setCreationUserId(getStdugr());
    exp.setChangeDateTime(getChangeDateTime());
    exp.setChangeUserId(getStdumod());
  }

  public void copyFrom(WSEndpoint endpoint, Case localCase)
  {
    setCaseId(localCase.getCaseId());
    setCaseTypeId(DicTypeAdmin.getInstance(CaseExpedientType.class).toLocalId(endpoint, localCase.getCaseTypeId()));
    //setAssumpte(localCase.getTitle());
    setDescription(localCase.getDescription());
    //setSubassumpte(localCase.getComments());

    String dregistre = localCase.getStartDate();
    String hregistre = localCase.getStartTime();

    setRegistryDate((dregistre==null || dregistre.equals(""))?TextUtil.toStringDate():dregistre);
    setRegistryTime((hregistre==null || hregistre.equals(""))?TextUtil.toStringTime():hregistre);
  }

  public void copyTo(WSEndpoint endpoint, CasePerson exp)
  {
    exp.setCasePersonTypeId(
            DicTypeAdmin.getInstance(CasePersonType.class)
            .toGlobalId(endpoint, CasePersonType.InteressatPrincipal));

    exp.setCasePersonId(
            PKUtil.makeMatrixPK(endpoint.getEntity(CasePerson.class),
              getCaseId(), getPersId()));
    
    exp.setCaseId(endpoint.getEntity(Case.class).toGlobalId(getCaseId()));
    exp.setPersonId(endpoint.toGlobalId(Person.class, getFotoPersonaId()));
    exp.setAddressId(endpoint.toGlobalId(Address.class, getFotoAdrecaPersonaId()));
    exp.setRepresentantPersonId(endpoint.toGlobalId(Person.class, getFotoRepresentantId()));
    exp.setRepresentantAddressId(endpoint.toGlobalId(Address.class,
        getFotoAdrecaRepresentantId()));

    exp.setCreationDateTime(getCreationDateTime());
    exp.setCreationUserId(getStdugr());
    exp.setChangeDateTime(getChangeDateTime());
    exp.setChangeUserId(getStdumod());
    exp.setComments(
            TextUtil.encodeEmpty(((persnom==null)?"":persnom + " ")
            + ((perscog1==null)?"":perscog1 + " ")
            + ((perscog2==null)?"":perscog2).trim()));

    //exp.setContactId(endpoint.toGlobalId(Contact.class, PKUtil.composePK("1", getPersId(), getFcontacn())));
    exp.getContactId().clear();
    String contactId = 
      endpoint.toGlobalId(Contact.class, PKUtil.composePK("1", getPersId(), getFcontacn()));
    if (contactId != null && contactId.trim().length() > 0)
      exp.getContactId().add(contactId);
  }

  public void copyTo(WSEndpoint endpoint,CasePersonView casePersonView)
  {
    casePersonView.setCasePersonId(
            PKUtil.makeMatrixPK(endpoint.getEntity(CasePerson.class),
            getCaseId(),
            getPersId()));

    casePersonView.setCasePersonTypeId(
            DicTypeAdmin.getInstance(CasePersonType.class)
            .toGlobalId(endpoint, CasePersonType.InteressatPrincipal));

    PersonView personView = new PersonView();
    personView.setFullName((TextUtil.encodeEmpty(getPersnom()) 
            + " " + TextUtil.encodeEmpty(getPerscog1())
            + " " + TextUtil.encodeEmpty(getPerscog2())).trim());

    personView.setNif( TextUtil.encodeEmpty(getPersnif()));
    personView.setPersonId(endpoint.toGlobalId(Person.class,
        getFotoPersonaId()));
    casePersonView.setPersonView(personView);

    PersonView representantView = new PersonView();
    representantView.setFullName((TextUtil.encodeEmpty(getRepnom()) + " "
            + TextUtil.encodeEmpty(getRepcog1()) + " "
            + TextUtil.encodeEmpty(getRepcog2())).trim());

    representantView.setNif( TextUtil.encodeEmpty(getRepnif()) );
    representantView.setPersonId(
            endpoint.toGlobalId(Person.class,
            getFotoRepresentantId()));
    
    casePersonView.setRepresentantPersonView(representantView);

    Case caseObj  = new Case();
    copyTo(endpoint, caseObj);
    casePersonView.setCaseObject(caseObj);
    casePersonView.setComments(
            TextUtil.encodeEmpty(((persnom==null)?"":persnom + " ")
            + ((perscog1==null)?"":perscog1 + " ")
            + ((perscog2==null)?"":perscog2).trim()));
  }

  public void copyTo(WSEndpoint endpoint, CaseAddress caseAddress)
  {
    caseAddress.setCaseId(endpoint.toGlobalId(Case.class, caseId));
    caseAddress.setAddressId(endpoint.toGlobalId(Address.class, addressId));
    caseAddress.setCaseAddressId(
            PKUtil.makeMatrixPK(
            endpoint.getEntity(CaseAddress.class), caseId, addressId));
  }


  public DBCaseState getActualState() {
    List<DBCaseState> states = getCaseStates();

    DBCaseState actualState = null;
    for(DBCaseState state : caseStates) {
      if (actualState==null)
      {
        actualState = state;
        continue;
      }
      if (state.compareTo(actualState) > 0) {
        actualState = state;
      }
    }

    return actualState;
  }

  public void loadCaseProperties(EntityManager entityManager) {
    if (propList == null) propList = new ArrayList<Property>();

    propList.clear();
    propList.addAll(DBCaseProperty.getPropertyList(this.getClass(), this));
    propList.addAll(DBCaseProperty.loadCaseExtendedProperties(entityManager, this));

  }

  public void copyCaseProperties(List<Property> properties) {
    if (propList == null) propList = new ArrayList<Property>();

    propList.clear();
    propList.addAll(properties);

    DBCaseProperty.copyPropertyList(properties, this.getClass(), this);

  }


  public String[] getIds()
  {
    String [] ids = {caseId};
    return ids;
  }

  public String getFotoPersonaId()
  {
    VersionIdentifier vId = getFotoPersonaVersionId();
    return vId==null?null:vId.getLocalVersionedId();
  }

  public String getFotoRepresentantId()
  {
    VersionIdentifier vId = getFotoRepresentantVersionId();
    return vId==null?null:vId.getLocalVersionedId();
  }

  public String getFotoAdrecaPersonaId()
  {
    VersionIdentifier vId = getFotoAdrecaPersonaVersionId();
    return vId==null?null:vId.getLocalVersionedId();
  }

  public String getFotoAdrecaRepresentantId()
  {
    VersionIdentifier vId = getFotoAdrecaRepresentantVersionId();
    return vId==null?null:vId.getLocalVersionedId();
  }

  private VersionIdentifier getFotoPersonaVersionId()
  {
    String id = getPersId();
    return (id==null)?null: new VersionIdentifier(
        VersionType.FOTO_PERSON_CASE_PERSON,
        getPersId(),
        new String [] {getCaseId()}
        );
  }
  public VersionIdentifier getFotoRepresentantVersionId()
  {
    String id = getReprId();
    return (id==null)?null:new VersionIdentifier(
            VersionType.FOTO_PERSON_CASE_REPRESENTANT,
            getReprId(),
            new String [] {getCaseId(), getPersId()}
            );
  }

  public VersionIdentifier getFotoAdrecaPersonaVersionId()
  {
    String id = getPersnd();
    return (id==null)?null:new VersionIdentifier(
            VersionType.FOTO_ADDRESS_CASE_PERSONDOM,
            (getPersnd()==null || getPersnd().equals(""))
              ?null
              : new String [] {getPersId(), getPersnd()},
            new String [] {getCaseId(), getPersId()});
  }

  public VersionIdentifier getFotoAdrecaRepresentantVersionId()
  {
    String id = getReprnd();
    return (id==null)?null:new VersionIdentifier(
            VersionType.FOTO_ADDRESS_CASE_REPRESENTANTDOM,
            (getReprnd()==null || getReprnd().equals(""))
              ?null
              : new String [] {getReprId(), getReprnd()},
            new String [] {getCaseId(), getPersId(), getReprId()});
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
  public void setFotoAdrecaRepresentantId(String fotoAdrecaRepresentantId)
  {
    this.fotoAdrecaRepresentantId = fotoAdrecaRepresentantId;
  }





}
