package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;

/**
 *
 * @author comasfc
 */
public class DBCaseState extends DBGenesysEntity
{

  private String caseId;
  private String caseStateReferece;
  private String caseTypeId;
  private String stateId;
  private String startDate;
  private String startTime;
  private String endDate;
  private String endTime;
  private String area;
  private String departament;
  private String grup;
  private String areaId;
  private String departamentId;
  private String grupId;
  private String description;
  private DBCase _case;

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

  public String getCaseStateReferece()
  {
    return caseStateReferece;
  }

  public void setCaseStateReferece(String caseStateReferece)
  {
    this.caseStateReferece = caseStateReferece;
  }

  public String getCaseTypeId()
  {
    return caseTypeId;
  }

  public void setCaseTypeId(String caseTypeId)
  {
    this.caseTypeId = caseTypeId;
  }

  public String getDepartament()
  {
    return departament;
  }

  public void setDepartament(String departament)
  {
    this.departament = departament;
  }

  public String getDepartamentId()
  {
    return departamentId;
  }

  public void setDepartamentId(String departamentId)
  {
    this.departamentId = departamentId;
  }

  public String getEndDate()
  {
    return endDate;
  }

  public void setEndDate(String endDate)
  {
    this.endDate = endDate;
  }

  public String getEndTime()
  {
    return endTime;
  }

  public void setEndTime(String endTime)
  {
    this.endTime = endTime;
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

  public String getStartDate()
  {
    return startDate;
  }

  public void setStartDate(String startDate)
  {
    this.startDate = startDate;
  }

  public String getStartTime()
  {
    return startTime;
  }

  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }

  public String getStateId()
  {
    return stateId;
  }

  public void setStateId(String stateId)
  {
    this.stateId = stateId;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public DBCase getCase()
  {
    return _case;
  }
  public void setCase(DBCase _case)
  {
    this._case = _case;
  }

  public int compareTo(DBCaseState o)
  {
    if (this.getEndDate()==null) {
      if (o.getEndDate()!=null) {
        return 1;
      } else {
        return (this.getStartDate() + this.getStartTime()).compareTo(o.getStartDate() + o.getStartTime());
      }
    } else {
      if (o.getEndDate()!=null) {
        return (this.getEndDate() + this.getEndTime()).compareTo(o.getEndDate() + o.getEndTime());
      } else {
        return -1;
      }
    }
  }

  @Override
  public String[] getIds()
  {
    return new String[] {caseId, caseStateReferece};
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
