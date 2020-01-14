package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBCaseStateResponsible extends DBGenesysEntity
{
  
  String caseId;
  String estatNum;


  String caseStateReferece;
  String areaId;
  String departamentId;
  String grupId;
  String area;
  String departament;
  String grup;

  DBCaseState caseState;

  @Override
  public String[] getIds()
  {
    //<!-- BPM -->
    return new String[] { caseId, estatNum, areaId, departamentId, grupId};
  }

  public String getArea()
  {
    return area;
  }

  public void setArea(String area)
  {
    this.area = area;
  }

  public String getAreaId()
  {
    return areaId;
  }

  public void setAreaId(String areaId)
  {
    this.areaId = areaId;
  }

  public String getCaseId()
  {
    return caseId;
  }

  public void setCaseId(String caseId)
  {
    this.caseId = caseId;
  }

    public String getEstatNum() {
        return estatNum;
    }

    public void setEstatNum(String estatNum) {
        this.estatNum = estatNum;
    }  
  
  public String getCaseStateReferece()
  {
    return caseStateReferece;
  }

  public void setCaseStateReferece(String caseStateReferece)
  {
    this.caseStateReferece = caseStateReferece;
  }

  public String getDepartamentId()
  {
    return departamentId;
  }

  public void setDepartamentId(String departamentId)
  {
    this.departamentId = departamentId;
  }

  public String getDepartament()
  {
    return departament;
  }

  public void setDepartament(String departament)
  {
    this.departament = departament;
  }

  public String getGrup()
  {
    return grup;
  }

  public void setGrup(String grup)
  {
    this.grup = grup;
  }

  public String getGrupId()
  {
    return grupId;
  }

  public void setGrupId(String grupId)
  {
    this.grupId = grupId;
  }

  public DBCaseState getCaseState()
  {
    return caseState;
  }

  public void setCaseState(DBCaseState estatResponsables)
  {
    this.caseState = estatResponsables;
  }

  



  public String getPropertyValue()
  {

      StringBuilder builder = new StringBuilder();
      builder.append(areaId).append(";");
      builder.append(departamentId).append(";");
      builder.append(grupId).append(";");
      builder.append(area).append(";");
      builder.append(departament).append(";");
      builder.append(grup).append(";");

      return builder.toString();
  }

  public void setPropertyValue(String propertyValue)
  {
      if (propertyValue==null) return;
      String [] values = propertyValue.split(";");

      int i=0;
      if (i>=values.length) return;
      areaId = values[i++];
      if (i>=values.length) return;
      departamentId = values[i++];
      if (i>=values.length) return;
      grupId = values[i++];
      if (i>=values.length) return;
      area = values[i++];
      if (i>=values.length) return;
      departament = values[i++];
      if (i>=values.length) return;
      grup = values[i++];

  }
}
