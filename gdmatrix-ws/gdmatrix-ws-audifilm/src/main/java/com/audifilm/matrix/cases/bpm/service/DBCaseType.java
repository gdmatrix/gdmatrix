package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBCaseType extends DBGenesysEntity
{
  String typeId;
  String description;
  String longDescription;
  String organizationId;
  String caseCounter;
  String departamentId;
  String areaId;
  String arxiuId;
  String texpdmax;
  String texpdmig;
  String rbrecap;
  String prioritat;
  String llibreId;
  String trecId;
  String swoff;

  public String getAreaId()
  {
    return areaId;
  }

  public void setAreaId(String areaId)
  {
    this.areaId = areaId;
  }

  public String getArxiuId()
  {
    return arxiuId;
  }

  public void setArxiuId(String arxiuId)
  {
    this.arxiuId = arxiuId;
  }

  public String getCaseCounter()
  {
    return caseCounter;
  }

  public void setCaseCounter(String caseCounter)
  {
    this.caseCounter = caseCounter;
  }

  public String getDepartamentId()
  {
    return departamentId;
  }

  public void setDepartamentId(String departamentId)
  {
    this.departamentId = departamentId;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getLlibreId()
  {
    return llibreId;
  }

  public void setLlibreId(String llibreId)
  {
    this.llibreId = llibreId;
  }

  public String getLongDescription()
  {
    return longDescription;
  }

  public void setLongDescription(String longDescription)
  {
    this.longDescription = longDescription;
  }

  public String getOrganizationId()
  {
    return organizationId;
  }

  public void setOrganizationId(String organizationId)
  {
    this.organizationId = organizationId;
  }

  public String getPrioritat()
  {
    return prioritat;
  }

  public void setPrioritat(String prioritat)
  {
    this.prioritat = prioritat;
  }

  public String getRbrecap()
  {
    return rbrecap;
  }

  public void setRbrecap(String rbrecap)
  {
    this.rbrecap = rbrecap;
  }

  public String getSwoff()
  {
    return swoff;
  }

  public void setSwoff(String swoff)
  {
    this.swoff = swoff;
  }

  public String getTexpdmax()
  {
    return texpdmax;
  }

  public void setTexpdmax(String texpdmax)
  {
    this.texpdmax = texpdmax;
  }

  public String getTexpdmig()
  {
    return texpdmig;
  }

  public void setTexpdmig(String texpdmig)
  {
    this.texpdmig = texpdmig;
  }

  public String getTrecId()
  {
    return trecId;
  }

  public void setTrecId(String trecId)
  {
    this.trecId = trecId;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setTypeId(String typeId)
  {
    this.typeId = typeId;
  }

  @Override
  public String[] getIds()
  {
    return new String[] {typeId};
  }

  
  
}
