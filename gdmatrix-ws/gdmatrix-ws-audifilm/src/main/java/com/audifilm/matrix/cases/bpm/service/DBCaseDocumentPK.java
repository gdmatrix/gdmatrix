package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBCaseDocumentPK extends GenesysPK
{

  String aplcod;
  String docorigen;
  String doccod;
  String modelcod;
  String docnompc;
  String identificador;

  public DBCaseDocumentPK(String aplcod,
                          String docorigen,
                          String doccod,
                          String modelcod,
                          String docnompc,
                          String identificador)
  {
    super( aplcod, docorigen, doccod, modelcod, docnompc, identificador);
    this.aplcod = aplcod;
    this.docorigen = docorigen;
    this.doccod = doccod;
    this.modelcod = modelcod;
    this.docnompc = docnompc;
    this.identificador = identificador;
  }

  public DBCaseDocumentPK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.aplcod = ids[0];
    this.docorigen = ids[1];
    this.doccod = ids[2];
    this.modelcod = ids[3];
    this.docnompc = ids[4];
    this.identificador = ids[5];
  }

  public String getAplcod()
  {
    return aplcod;
  }

  public void setAplcod(String aplcod)
  {
    this.aplcod = aplcod;
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

  public String getDocorigen()
  {
    return docorigen;
  }

  public void setDocorigen(String docorigen)
  {
    this.docorigen = docorigen;
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

}
