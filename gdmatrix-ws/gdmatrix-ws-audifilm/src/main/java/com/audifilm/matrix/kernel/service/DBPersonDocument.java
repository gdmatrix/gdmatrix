package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.common.service.PKUtil;
import com.audifilm.matrix.dic.service.person.Person;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Document;
import org.matrix.kernel.PersonDocument;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author blanquepa
 */
public class DBPersonDocument extends DBGenesysEntity
{
  public static final String DOCTYPEID_PREFIX = "DOC_";
  public static final String APLCOD = "NCL ";
  public static final String DOCORIGEN = "PERSCODP  ";

  String aplcod;
  String docorigen;
  String doccod;
  String modelcod;
  String docnompc;
  String personId; //identificador
  String doctip;

  String docId; //idnreg
  String caseId;

  String stdugr;
  String stdumod;
  String stddgr;
  String stddmod;
  String stdhgr;
  String stdhmod;
  String estatsig;
  String motiurebu;
  String descriptor;
  String guid;
  String eliminat;
  String texpcod;
  String observacions;
  String docprotect;
  String acciogen;
  String estatgen;
  String estatenv;
  String orgcod;
  String doctitol;
  String datasessio;
  String fechanoti;
  String docnum;
  String estatcod;
  String perscod;
  String persnd;
  String estatnot;
  String docnomprop;
  String propcod;
  String carrecdesc;
  String transcod;
  String treccod;

  public DBPersonDocument()
  {
  }

  public DBPersonDocument(String doctip, String doccod, String modelcod, String docnompc,
    String docId, String personId, String observacions)
  {
    this.aplcod = APLCOD;
    this.docorigen = DOCORIGEN;
    this.doctip = StringUtils.rightPad(doctip, 4);
    this.doccod = StringUtils.rightPad(doccod, 4);
    this.modelcod = StringUtils.rightPad(modelcod, 4);
    this.docnompc = docnompc;
    this.docId = docId;
    this.personId = StringUtils.leftPad(personId, 8, "0");
    this.observacions = observacions;
  }

  public String getAcciogen()
  {
    return acciogen;
  }

  public void setAcciogen(String acciogen)
  {
    this.acciogen = acciogen;
  }

  public String getAplcod()
  {
    return aplcod;
  }

  public void setAplcod(String aplcod)
  {
    this.aplcod = StringUtils.rightPad(aplcod, 4);
  }

  public String getCarrecdesc()
  {
    return carrecdesc;
  }

  public void setCarrecdesc(String carrecdesc)
  {
    this.carrecdesc = carrecdesc;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getDatasessio()
  {
    return datasessio;
  }

  public void setDatasessio(String datasessio)
  {
    this.datasessio = datasessio;
  }

  public String getDescriptor()
  {
    return descriptor;
  }

  public void setDescriptor(String descriptor)
  {
    this.descriptor = descriptor;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getDoccod()
  {
    return doccod;
  }

  public void setDoccod(String doccod)
  {
    this.doccod = StringUtils.rightPad(doccod, 4);
  }

  public String getDocnompc()
  {
    return docnompc;
  }

  public void setDocnompc(String docnompc)
  {
    this.docnompc = docnompc;
  }

  public String getDocnomprop()
  {
    return docnomprop;
  }

  public void setDocnomprop(String docnomprop)
  {
    this.docnomprop = docnomprop;
  }

  public String getDocnum()
  {
    return docnum;
  }

  public void setDocnum(String docnum)
  {
    this.docnum = docnum;
  }

  public String getDocorigen()
  {
    return docorigen;
  }

  public void setDocorigen(String docorigen)
  {
    this.docorigen = StringUtils.rightPad(docorigen, 10);
  }

  public String getDocprotect()
  {
    return docprotect;
  }

  public void setDocprotect(String docprotect)
  {
    this.docprotect = docprotect;
  }

  public String getDoctip()
  {
    return doctip;
  }

  public void setDoctip(String doctip)
  {
    this.doctip = StringUtils.rightPad(doctip, 4);
  }

  public String getDoctitol()
  {
    return doctitol;
  }

  public void setDoctitol(String doctitol)
  {
    this.doctitol = doctitol;
  }

  public String getEliminat()
  {
    return eliminat;
  }

  public void setEliminat(String eliminat)
  {
    this.eliminat = eliminat;
  }

  public String getEstatcod()
  {
    return estatcod;
  }

  public void setEstatcod(String estatcod)
  {
    this.estatcod = estatcod;
  }

  public String getEstatenv()
  {
    return estatenv;
  }

  public void setEstatenv(String estatenv)
  {
    this.estatenv = estatenv;
  }

  public String getEstatgen()
  {
    return estatgen;
  }

  public void setEstatgen(String estatgen)
  {
    this.estatgen = estatgen;
  }

  public String getEstatnot()
  {
    return estatnot;
  }

  public void setEstatnot(String estatnot)
  {
    this.estatnot = estatnot;
  }

  public String getEstatsig()
  {
    return estatsig;
  }

  public void setEstatsig(String estatsig)
  {
    this.estatsig = estatsig;
  }

  public String getFechanoti()
  {
    return fechanoti;
  }

  public void setFechanoti(String fechanoti)
  {
    this.fechanoti = fechanoti;
  }

  public String getGuid()
  {
    return guid;
  }

  public void setGuid(String guid)
  {
    this.guid = guid;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getModelcod()
  {
    return modelcod;
  }

  public void setModelcod(String modelcod)
  {
    String pmodelcod = StringUtils.rightPad(modelcod, 4);
    this.modelcod = pmodelcod;
  }

  public String getMotiurebu()
  {
    return motiurebu;
  }

  public void setMotiurebu(String motiurebu)
  {
    this.motiurebu = motiurebu;
  }

  public String getObservacions()
  {
    return observacions;
  }

  public void setObservacions(String observacions)
  {
    this.observacions = observacions;
  }

  public String getOrgcod()
  {
    return orgcod;
  }

  public void setOrgcod(String orgcod)
  {
    this.orgcod = orgcod;
  }

  public String getPerscod()
  {
    return perscod;
  }

  public void setPerscod(String perscod)
  {
    this.perscod = perscod;
  }

  public String getPersnd()
  {
    return persnd;
  }

  public void setPersnd(String persnd)
  {
    this.persnd = persnd;
  }

  public String getPropcod()
  {
    return propcod;
  }

  public void setPropcod(String propcod)
  {
    this.propcod = propcod;
  }

  public String getTexpcod()
  {
    return texpcod;
  }

  public void setTexpcod(String texpcod)
  {
    this.texpcod = texpcod;
  }

  public String getTranscod()
  {
    return transcod;
  }

  public void setTranscod(String transcod)
  {
    this.transcod = transcod;
  }

  public String getTreccod()
  {
    return treccod;
  }

  public void setTreccod(String treccod)
  {
    this.treccod = treccod;
  }

  @Override
  public String[] getIds()
  {
    return new String[] {
      APLCOD, DOCORIGEN, doccod, modelcod, docnompc, personId
    };
  }

  public String getPersonDocId()
  {
    return PKUtil.composePK(
            APLCOD, DOCORIGEN, doccod, modelcod, docnompc, personId
            );
  }  

  public void copyTo(PersonDocument personDocument)
  {
    DBPersonDocumentPK pk = 
      new DBPersonDocumentPK(APLCOD, DOCORIGEN, doccod, modelcod, docnompc,
      personId);
    personDocument.setPersonDocId(pk.toString());

    personDocument.setPersonId(personId);
    personDocument.setDocId(docId);
    personDocument.setVersion(0);
    personDocument.setPersonDocTypeId(DictionaryConstants.PERSON_DOCUMENT_TYPE);
    personDocument.setCreationDateTime(getCreationDateTime());
    personDocument.setCreationUserId(getStdugr());
    personDocument.setChangeDateTime(getChangeDateTime());
    personDocument.setChangeUserId(getStdumod());
    personDocument.setComments(getObservacions());
  }

  public void copyTo(WSEndpoint endpoint, PersonDocument personDocument)
  {
    personDocument.setPersonDocId(
      endpoint.toGlobalId(PersonDocument.class, getPersonDocId()));
    personDocument.setPersonId(endpoint.toGlobalId(Person.class, getPersonId()));
    personDocument.setDocId(endpoint.toGlobalId(Document.class, getDocId()));
    personDocument.setPersonDocTypeId(DictionaryConstants.PERSON_DOCUMENT_TYPE);
    personDocument.setComments(getObservacions());
  }

}
