package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.common.service.PKUtil;
import org.matrix.cases.Case;
import org.matrix.cases.CaseDocument;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Document;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class DBCaseDocument extends DBGenesysEntity
{

  String aplcod;
  String docorigen;
  String doccod;
  String modelcod;
  String docnompc;
  String identificador;
  String doctip;

  String docId;
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
    this.aplcod = aplcod;
  }

  public String getCarrecdesc()
  {
    return carrecdesc;
  }

  public void setCarrecdesc(String carrecdesc)
  {
    this.carrecdesc = carrecdesc;
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

  public String getDoccod()
  {
    return doccod;
  }

  public void setDoccod(String doccod)
  {
    this.doccod = doccod;
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
    this.docorigen = docorigen;
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
    this.doctip = doctip;
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

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getIdentificador()
  {
    return identificador;
  }

  public void setIdentificador(String identificador)
  {
    this.identificador = identificador;
  }

  public String getModelcod()
  {
    return modelcod;
  }

  public void setModelcod(String modelcod)
  {
    this.modelcod = modelcod;
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

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
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
      aplcod, docorigen, doccod, modelcod, docnompc, identificador
    };
  }

  public String getCaseDocumentId()
  {
    return PKUtil.composePK(
            caseId, aplcod, docorigen, doccod, modelcod, docnompc, identificador
            );
  }

  public void copyTo(WSEndpoint endpoint, CaseDocument caseDocument)
  {
    caseDocument.setCaseDocId(endpoint.toGlobalId(CaseDocument.class, getCaseDocumentId()));
    caseDocument.setCaseId(endpoint.toGlobalId(Case.class, getCaseId()));
    caseDocument.setDocId(endpoint.toGlobalId(Document.class, getDocId()));
    caseDocument.setCaseDocTypeId(DictionaryConstants.CASE_DOCUMENT_TYPE);
  }
}
