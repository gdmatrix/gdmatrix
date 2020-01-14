package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBSituacio extends DBGenesysEntity
{
  String caseId;
  String areaId;
  String depId;
  String grupId;
  String daccepta;
  String haccepta;
  String dtrasllat;
  String htrasllat;
  String observacions;
  String areacodd;
  String depcodd;
  String grupcodd;
  String idseg;
  String ident;
  String areadesc;
  String depdesc;
  String grupdesc;
  String areadescd;
  String depdescd;
  String grupdescd;

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

  public String getAreaId()
  {
    return areaId;
  }

  public void setAreaId(String areaId)
  {
    this.areaId = areaId;
  }

  public String getDestiAreaId()
  {
    return areacodd;
  }

  public void setDestiAreaId(String areacodd)
  {
    this.areacodd = areacodd;
  }

  public String getArea()
  {
    return areadesc;
  }

  public void setArea(String areadesc)
  {
    this.areadesc = areadesc;
  }

  public String getDestiArea()
  {
    return areadescd;
  }

  public void setDestiArea(String areadescd)
  {
    this.areadescd = areadescd;
  }

  public String getDataAcceptacio()
  {
    return daccepta;
  }

  public void setDataAcceptacio(String daccepta)
  {
    this.daccepta = daccepta;
  }

  public String getDeptId()
  {
    return depId;
  }

  public void setDeptId(String depId)
  {
    this.depId = depId;
  }

  public String getDestiDeptId()
  {
    return depcodd;
  }

  public void setDestiDeptId(String depcodd)
  {
    this.depcodd = depcodd;
  }

  public String getDepartament()
  {
    return depdesc;
  }

  public void setDepartament(String depdesc)
  {
    this.depdesc = depdesc;
  }

  public String getDestiDepartament()
  {
    return depdescd;
  }

  public void setDestiDepartament(String depdescd)
  {
    this.depdescd = depdescd;
  }

  public String getDataTrasllat()
  {
    return dtrasllat;
  }

  public void setDataTrasllat(String dtrasllat)
  {
    this.dtrasllat = dtrasllat;
  }

  public String getGrupId()
  {
    return grupId;
  }

  public void setGrupId(String grupId)
  {
    this.grupId = grupId;
  }

  public String getDestiGrupId()
  {
    return grupcodd;
  }

  public void setDestiGrupId(String grupcodd)
  {
    this.grupcodd = grupcodd;
  }

  public String getGrup()
  {
    return grupdesc;
  }

  public void setGrup(String grupdesc)
  {
    this.grupdesc = grupdesc;
  }

  public String getDestiGrup()
  {
    return grupdescd;
  }

  public void setDestiGrup(String grupdescd)
  {
    this.grupdescd = grupdescd;
  }

  public String getHoraAcceptacio()
  {
    return haccepta;
  }

  public void setHoraAcceptacio(String haccepta)
  {
    this.haccepta = haccepta;
  }

  public String getHoraTrasllat()
  {
    return htrasllat;
  }

  public void setHoraTrasllat(String htrasllat)
  {
    this.htrasllat = htrasllat;
  }

  public String getIdent()
  {
    return ident;
  }

  public void setIdent(String ident)
  {
    this.ident = ident;
  }

  public String getIdseg()
  {
    return idseg;
  }

  public void setIdseg(String idseg)
  {
    this.idseg = idseg;
  }

  public String getObservacions()
  {
    return observacions;
  }

  public void setObservacions(String observacions)
  {
    this.observacions = observacions;
  }


  public String getPropertyValue()
  {
      return
        caseId + ":" +
        (areaId==null?"":areaId) + ":" +
        (depId==null?"":depId) + ":" +
        (grupId==null?"":grupId) + ":" +
        (daccepta==null?"":daccepta) + ":" + 
        (haccepta==null?"":haccepta) + ":" +
        (dtrasllat==null?"":dtrasllat) + ":" +
        (htrasllat==null?"":htrasllat) + ":" +
        (observacions==null?"":observacions) + ":" +
        (areacodd==null?"":areacodd) + ":" +
        (depcodd==null?"":depcodd) + ":" +
        (grupcodd==null?"":grupcodd) + ":" +
        (idseg==null?"":idseg) + ":" +
        (ident==null?"":ident) + ":" +
        (areadesc==null?"":areadesc) + ":" +
        (depdesc==null?"":depdesc) + ":" +
        (grupdesc==null?"":grupdesc) + ":" +
        (areadescd==null?"":areadescd) + ":" +
        (depdescd==null?"":depdescd) + ":" +
        (grupdescd==null?"":grupdescd);
  }

  public void setPropertyValue(String propertyValue)
  {
      if (propertyValue==null) return;

      String [] values = propertyValue.split(":");
      int i=0;
      if (values.length<=i) return;
      caseId = values[i++];
      if (values.length<=i) return;
      areaId = values[i++];
      if (values.length<=i) return;
      depId = values[i++];
      if (values.length<=i) return;
      grupId = values[i++];
      if (values.length<=i) return;
      daccepta = values[i++];
      if (values.length<=i) return;
      haccepta = values[i++];
      if (values.length<=i) return;
      dtrasllat = values[i++];
      if (values.length<=i) return;
      htrasllat = values[i++];
      if (values.length<=i) return;
      observacions = values[i++];
      if (values.length<=i) return;
      areacodd = values[i++];
      if (values.length<=i) return;
      depcodd = values[i++];
      if (values.length<=i) return;
      grupcodd = values[i++];
      if (values.length<=i) return;
      idseg = values[i++];
      if (values.length<=i) return;
      ident = values[i++];
      if (values.length<=i) return;
      areadesc = values[i++];
      if (values.length<=i) return;
      depdesc = values[i++];
      if (values.length<=i) return;
      grupdesc = values[i++];
      if (values.length<=i) return;
      areadescd = values[i++];
      if (values.length<=i) return;
      depdescd = values[i++];
      if (values.length<=i) return;
      grupdescd = values[i++];
  }


  @Override
  public String[] getIds()
  {
    String [] ids = {caseId};
    return ids;
  }

}
