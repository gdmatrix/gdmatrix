package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBAplDocument extends DBGenesysEntity {

  String aplId;
  String docorigen;
  String docId;
  String docdesc;
  String aliesdoc;
  String aliesplant;
  String ext;
  String seg;
  String max;
  String icona;
  String bloqueig;
  String acces;
  String classId;
  String modef;

  @Override
  public String[] getIds()
  {
    return new String[] {
      aplId, docorigen, docId
    };
  }

  public String getClassId()
  {
    return classId;
  }

  public void setClassId(String classId)
  {
    this.classId = classId;
  }

  public String getAcces()
  {
    return acces;
  }

  public void setAcces(String acces)
  {
    this.acces = acces;
  }

  public String getAliesdoc()
  {
    return aliesdoc;
  }

  public void setAliesdoc(String aliesdoc)
  {
    this.aliesdoc = aliesdoc;
  }

  public String getAliesplant()
  {
    return aliesplant;
  }

  public void setAliesplant(String aliesplant)
  {
    this.aliesplant = aliesplant;
  }

  public String getAplId()
  {
    return aplId;
  }

  public void setAplId(String aplId)
  {
    this.aplId = aplId;
  }

  public String getBloqueig()
  {
    return bloqueig;
  }

  public void setBloqueig(String bloqueig)
  {
    this.bloqueig = bloqueig;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getDocdesc()
  {
    return docdesc;
  }

  public void setDocdesc(String docdesc)
  {
    this.docdesc = docdesc;
  }

  public String getDocorigen()
  {
    return docorigen;
  }

  public void setDocorigen(String docorigen)
  {
    this.docorigen = docorigen;
  }

  public String getExt()
  {
    return ext;
  }

  public void setExt(String ext)
  {
    this.ext = ext;
  }

  public String getIcona()
  {
    return icona;
  }

  public void setIcona(String icona)
  {
    this.icona = icona;
  }

  public String getMax()
  {
    return max;
  }

  public void setMax(String max)
  {
    this.max = max;
  }

  public String getModef()
  {
    return modef;
  }

  public void setModef(String modef)
  {
    this.modef = modef;
  }

  public String getSeg()
  {
    return seg;
  }

  public void setSeg(String seg)
  {
    this.seg = seg;
  }





}
